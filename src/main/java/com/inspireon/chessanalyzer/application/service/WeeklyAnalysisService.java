package com.inspireon.chessanalyzer.application.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.inspireon.chessanalyzer.domain.datamanager.GameDataAccess;
import com.inspireon.chessanalyzer.AppConfig;
import com.inspireon.chessanalyzer.application.clients.ChessComApiClient;
import com.inspireon.chessanalyzer.application.clients.ChessApiClient;
import com.inspireon.chessanalyzer.web.dtos.PlayerProfile;
import com.inspireon.chessanalyzer.web.dtos.ChessApiResponse;
import com.inspireon.chessanalyzer.stockfish.StockfishClient;
import com.inspireon.chessanalyzer.stockfish.ComputerMove;
import com.inspireon.chessanalyzer.stockfish.engine.enums.Option;
import com.inspireon.chessanalyzer.stockfish.engine.enums.Query;
import com.inspireon.chessanalyzer.stockfish.engine.enums.QueryType;
import com.inspireon.chessanalyzer.stockfish.exceptions.StockfishInitException;
import com.inspireon.chessanalyzer.web.dtos.WeeklyAnalysis;
import com.inspireon.chessanalyzer.web.dtos.GameAnalysis;
import com.inspireon.chessanalyzer.web.dtos.UserMistake;
import com.inspireon.chessanalyzer.common.enums.TacticalTheme;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WeeklyAnalysisService {

  @Autowired
  private GameDataAccess gameDataAccess;

  @Autowired
  private AppConfig appConfig;
  
  @Autowired
  private TacticalPatternDetector tacticalPatternDetector;

  @Autowired
  private ChessComApiClient chessComApiClient;

  @Autowired
  private ChessApiClient chessApiClient;

  private static final int MULTI_PV = 3;
  private static final long MOVE_TIME_MS = 800; // per position

  public WeeklyAnalysis analyzeWeekly(String playerUsername, LocalDate weekStart) throws Exception {
    LocalDate start = (weekStart != null) ? weekStart : LocalDate.now().with(DayOfWeek.MONDAY);
    LocalDate end = start.plusDays(6);
    log.info("Starting weekly analysis for {} in range [{}..{}]", playerUsername, start, end);

    // Fetch player profile to get avatar
    PlayerProfile playerProfile = chessComApiClient.getPlayerProfile(playerUsername);
    String avatar = (playerProfile != null) ? playerProfile.getAvatar() : null;
    log.info("Player profile fetched for {}: avatar={}", playerUsername, avatar);

    List<Game> games = gameDataAccess.getGames(playerUsername);
    log.info("Loaded {} total games for {} from cache/indexer", games.size(), playerUsername);
    
    List<GameAnalysis> analyses = new ArrayList<>();
    StockfishClient stockfishClient = null;
    if ("stockfish".equalsIgnoreCase(appConfig.getEngineType())) {
      stockfishClient = buildClient();
      log.info("Using Stockfish engine for analysis");
    } else {
      log.info("Using Chess-API.com cloud service for analysis");
    }
    
    int gamesAnalyzed = 0;
    for (Game game : games) {
      if (gamesAnalyzed >= 2) {
        break;
      }
      
      LocalDate played = LocalDate.parse(game.getDate().replace('.', '-'));
      log.info("Analyzing game {} played {}", game.getGameId(), played);
      
      String color = game.getWhitePlayer().getName().equalsIgnoreCase(playerUsername) ? "WHITE" : "BLACK";
      boolean isWhite = color.equals("WHITE");
      
      List<UserMistake> mistakes = analyzeMistakes(stockfishClient, game, playerUsername, isWhite);
      
      // Extract game metadata
      String whitePlayer = game.getWhitePlayer().getName();
      String blackPlayer = game.getBlackPlayer().getName();
      String whiteElo = String.valueOf(game.getWhitePlayer().getElo());
      String blackElo = String.valueOf(game.getBlackPlayer().getElo());
      String gameResult = game.getResult() != null ? game.getResult().getDescription() : "Unknown";
      String timeControl = game.getEco() != null ? game.getEco() : "Unknown"; // Using ECO as proxy for game info
      
      log.info("Finished analysis for game {} mistakes found={} color={}", game.getGameId(), mistakes.size(), color);
      analyses.add(new GameAnalysis(game.getGameId(), played, color, 
                                   whitePlayer, blackPlayer, whiteElo, blackElo, 
                                   gameResult, timeControl, mistakes));
      gamesAnalyzed++;
    }

    return new WeeklyAnalysis(playerUsername, avatar, start, end, analyses.size(), analyses);
  }

  private List<UserMistake> analyzeMistakes(StockfishClient client, Game game, String playerUsername, boolean isWhite) 
      throws Exception {
    game.loadMoveText();
    MoveList moves = game.getHalfMoves();
    Board board = new Board();
    
    log.info("Starting mistake analysis for game {}, total moves: {}, isWhite: {}", game.getGameId(), moves.size(), isWhite);
    
    List<UserMistake> mistakes = new ArrayList<>();
    int openingMistakes = 0;
    int middlegameMistakes = 0;
    int endgameMistakes = 0;
    int playerMovesAnalyzed = 0;
    
    for (int i = 0; i < moves.size(); i++) {
      Move move = moves.get(i);
      boolean isPlayerMove = (i % 2 == 0 && isWhite) || (i % 2 == 1 && !isWhite);
      
      if (!isPlayerMove) {
        board.doMove(move);
        continue;
      }
      
      playerMovesAnalyzed++;
      int moveNumber = (i / 2) + 1;
      String gamePhase = getGamePhase(moveNumber);
      
      log.debug("Analyzing move {}: {} in {} (move #{}, player moves analyzed: {})", 
          i+1, move.toString(), gamePhase, moveNumber, playerMovesAnalyzed);
      
      if (shouldSkipMove(gamePhase, openingMistakes, middlegameMistakes, endgameMistakes)) {
        log.debug("Skipping move {} - phase limit reached for {}", moveNumber, gamePhase);
        board.doMove(move);
        continue;
      }
      
      List<ComputerMove> computerMoves = getBestMovesForPosition(client, board.getFen(), isWhite);
      
      if (computerMoves.isEmpty()) {
        log.debug("No computer moves found before position, skipping");
        board.doMove(move);
        continue;
      }
      
      String fenBefore = board.getFen();
      Board boardBeforeMove = board.clone(); // Clone board before applying user move
      board.doMove(move);
      
      List<ComputerMove> afterMoves = getBestMovesForPosition(client, board.getFen(), !isWhite);
      
      if (afterMoves.isEmpty()) {
        log.debug("No computer moves found after position, skipping");
        continue;
      }
      
      ComputerMove bestMove = computerMoves.get(0);
      int scoreAfter = (-1) * afterMoves.get(0).getScore();
      int scoreDrop = bestMove.getScore() - scoreAfter;
      
      log.debug("Move {}: bestScore={}, scoreAfter={}, scoreDrop={}, userMove={}, bestMove={}", 
          moveNumber, bestMove.getScore(), scoreAfter, scoreDrop, move.toString(), bestMove.getMove().toString());
      
      // Don't flag it as a mistake if user move equals the best move
      boolean isUserMoveBest = move.toString().equals(bestMove.getMove().toString());
      if (isUserMoveBest) {
        log.debug("Move {} is the best move, skipping mistake detection", moveNumber);
        continue;
      }
      
      // Only consider it a meaningful mistake if:
      // 1. The score drop is significant (> 100 centipawns)
      // 2. The position wasn't already lost (best move score > -200 centipawns)
      // 3. The move led to a significantly worse position
      boolean isPositionReasonable = bestMove.getScore() > -200;
      boolean isSignificantDrop = scoreDrop > 100;
      boolean isNotAlreadyLost = scoreAfter > -500; // Don't flag moves in completely lost positions
      
      log.info("Move {} filtering check: scoreDrop={}, bestScore={}, scoreAfter={}, significant={}, reasonable={}, notLost={}", 
          moveNumber, scoreDrop, bestMove.getScore(), scoreAfter, isSignificantDrop, isPositionReasonable, isNotAlreadyLost);
      
      if (isSignificantDrop && isPositionReasonable && isNotAlreadyLost) {
        // Detect tactical theme for the mistake
        TacticalTheme tacticalTheme = TacticalTheme.UNKNOWN;
        try {
          // Use the board state before the user's move for tactical analysis
          Move bestMoveObj = null;
          
          try {
            // Try to parse the move using Square objects
            String moveStr = bestMove.getMove();
            Square fromSquare = Square.fromValue(moveStr.substring(0, 2).toUpperCase());
            Square toSquare = Square.fromValue(moveStr.substring(2, 4).toUpperCase());
            bestMoveObj = new Move(fromSquare, toSquare);
            log.debug("Calling tactical detection: userMove={}, bestMove={}, scoreDrop={}", 
                move.toString(), bestMoveObj.toString(), scoreDrop);
            
            tacticalTheme = tacticalPatternDetector.detectTacticalTheme(
                boardBeforeMove, 
                move, 
                bestMoveObj, 
                gamePhase, 
                scoreDrop
            );
            
            log.info("Tactical theme detected: {} for move {} (scoreDrop={})", 
                tacticalTheme, move.toString(), scoreDrop);
          } catch (Exception moveParseException) {
            log.warn("Failed to parse best move '{}' for tactical detection: {}", 
                bestMove.getMove(), moveParseException.getMessage());
            // Skip tactical theme detection but still record the mistake
            tacticalTheme = TacticalTheme.UNKNOWN;
          }
              
        } catch (Exception e) {
          log.warn("Error detecting tactical theme for move {}: {}", move.toString(), e.getMessage());
          tacticalTheme = TacticalTheme.UNKNOWN;
        }
        
        UserMistake mistake = new UserMistake(
            game.getGameId(), 
            playerUsername, 
            fenBefore, 
            move.toString(), 
            bestMove.getMove().toString(),
            gamePhase,
            moveNumber,
            scoreDrop,
            tacticalTheme
        );
        
        mistakes.add(mistake);
        
        if (gamePhase.equals("OPENING")) openingMistakes++;
        else if (gamePhase.equals("MIDDLEGAME")) middlegameMistakes++;
        else if (gamePhase.equals("ENDGAME")) endgameMistakes++;
        
        log.info("MISTAKE FOUND in {} at move {}: theme={}, scoreDrop={}, bestScore={}, scoreAfter={}, userMove={}, bestMove={}", 
            gamePhase, moveNumber, tacticalTheme, scoreDrop, bestMove.getScore(), scoreAfter, move.toString(), bestMove.getMove().toString());
      } else {
        log.info("Move {} NOT flagged as mistake: scoreDrop={}, bestScore={}, scoreAfter={}, reasonable={}, significant={}, notLost={}", 
            moveNumber, scoreDrop, bestMove.getScore(), scoreAfter, isPositionReasonable, isSignificantDrop, isNotAlreadyLost);
      }
    }
    
    log.info("Analysis complete for game {}: playerMoves={}, mistakes={} (O:{}, M:{}, E:{})", 
        game.getGameId(), playerMovesAnalyzed, mistakes.size(), openingMistakes, middlegameMistakes, endgameMistakes);
    
    return mistakes;
  }
  
  private String getGamePhase(int moveNumber) {
    if (moveNumber <= 15) {
      return "OPENING";
    } else if (moveNumber <= 40) {
      return "MIDDLEGAME";  
    } else {
      return "ENDGAME";
    }
  }
  
  private boolean shouldSkipMove(String gamePhase, int openingMistakes, int middlegameMistakes, int endgameMistakes) {
    // Development mode: limit to 4 total mistakes per game for quick testing
    int totalMistakes = openingMistakes + middlegameMistakes + endgameMistakes;
    return totalMistakes >= 4;
  }
  
  private List<ComputerMove> getComputerMoves(List<String> moveLines, boolean isWhite) {
    List<ComputerMove> cmoves = new ArrayList<>();
    log.debug("Parsing {} engine response lines for isWhite={}", moveLines.size(), isWhite);
    
    for (String moveLine : moveLines) {
      log.debug("Processing engine line: {}", moveLine);
      
      if (moveLine.contains("bestmove")) {
        break;
      }

      int score = 0;
      String move = "";
      String [] tokens = moveLine.split(" ");
      for (int i = 0; i < tokens.length; i++) {
        if (tokens[i].equalsIgnoreCase("cp")) {
          score = Integer.parseInt(tokens[i+1]);  
        }
        if (tokens[i].equalsIgnoreCase("pv")) {          
          move = tokens[i+1];
        }
      }
      
      if (!move.isEmpty()) {
        String lineAfterMove =
          ((moveLine.indexOf(move) + move.length()) < moveLine.length() - 1)
              ? moveLine.substring(moveLine.indexOf(move) + move.length() + 1)
              : "";
        cmoves.add(new ComputerMove(move, lineAfterMove, score, isWhite));
        log.debug("Added computer move: {} with score: {}", move, score);
      }
    }
    
    log.debug("Parsed {} computer moves", cmoves.size());
    return cmoves;
  }

  private List<ComputerMove> getBestMovesForPosition(StockfishClient stockfishClient, String fen, boolean isWhite) throws Exception {
    if ("chess-api".equalsIgnoreCase(appConfig.getEngineType())) {
      // Use Chess-API.com
      List<ChessApiResponse> responses = chessApiClient.getBestMoves(fen);
      return convertChessApiResponsesToComputerMoves(responses, isWhite);
    } else {
      // Use Stockfish
      List<String> responses = stockfishClient.getBestMoves(new Query.Builder(QueryType.Best_Move)
          .setFen(fen)
          .setMovetime(MOVE_TIME_MS)
          .build());
      
      log.debug("Engine responses: {}", responses.size());
      return getComputerMoves(responses, isWhite);
    }
  }

  private List<ComputerMove> convertChessApiResponsesToComputerMoves(List<ChessApiResponse> responses, boolean isWhite) {
    List<ComputerMove> computerMoves = new ArrayList<>();
    
    for (ChessApiResponse response : responses) {
      if (response.getMove() != null && response.getCentipawns() != null) {
        int score = response.getCentipawnsAsInt();
        String move = response.getMove();
        String continuation = response.getContinuationArr() != null && !response.getContinuationArr().isEmpty() 
            ? String.join(" ", response.getContinuationArr()) : "";
        
        computerMoves.add(new ComputerMove(move, continuation, score, isWhite));
        log.debug("Converted Chess-API response to computer move: {} with score: {}", move, score);
      }
    }
    
    log.debug("Converted {} Chess-API responses to computer moves", computerMoves.size());
    return computerMoves;
  }

  private StockfishClient buildClient() throws StockfishInitException {
    String configured = appConfig.getStockfishPath();
    String engine = (configured != null && !configured.isBlank())
        ? configured
        : System.getProperty("user.home") + "/workplace/stockfish";
    // If a directory path is provided, assume the binary is named 'stockfish' inside it.
    java.nio.file.Path p = java.nio.file.Paths.get(engine);
    if (java.nio.file.Files.isDirectory(p)) {
      engine = p.resolve("stockfish").toString();
    }
    log.info("Using Stockfish binary at: {}", engine);
    return new StockfishClient.Builder()
        .setPath(engine)
        .setInstances(1)
        .setOption(Option.MultiPV, MULTI_PV)
        .setOption(Option.Threads, 2)
        .setOption(Option.Minimum_Thinking_Time, 50)
        .setOption(Option.Skill_Level, 20)
        .build();
  }
}
