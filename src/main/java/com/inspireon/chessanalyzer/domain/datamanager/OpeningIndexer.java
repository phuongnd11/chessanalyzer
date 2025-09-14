package com.inspireon.chessanalyzer.domain.datamanager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.game.GameResult;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import com.inspireon.chessanalyzer.AppConfig;
import com.inspireon.chessanalyzer.common.enums.Castle;
import com.inspireon.chessanalyzer.common.enums.ChessSite;
import com.inspireon.chessanalyzer.common.enums.Opponent;
import com.inspireon.chessanalyzer.common.io.OpeningFileAccess;
import com.inspireon.chessanalyzer.domain.cache.PlayerStatCache;
import com.inspireon.chessanalyzer.domain.model.ChessOpening;
import com.inspireon.chessanalyzer.domain.model.ChessTempoResult;
import com.inspireon.chessanalyzer.web.dtos.OpeningStat;
import com.inspireon.chessanalyzer.web.dtos.OpeningStat.Perspective;
import com.inspireon.chessanalyzer.web.dtos.WinRateStat;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OpeningIndexer {
  
  @Autowired
  private GameDataAccess gameDataAccess;
  
  @Autowired
  private PlayerStatCache playerStatCache;
  
  @Autowired
  private OpeningFileAccess openingFileAccess;

  @Autowired
  private AppConfig appConfig;

  // A static mapping of chess openings.
  private static Map<String, ChessOpening> openings;

  private static final int OPENING_LIMIT = 30;

  private static final int MINIMUM_MOVE_FOR_GAME = 4;

  
  public void indexOpening(String playerUsername) throws Exception {
    log.info("Indexing openings for player: {}", playerUsername);
    Map<String, ChessOpening> openings = getStaticOpeningMap();
    
    int numOfGames = 0;
    int numOfMonths = 0;

    SortedMap<String, OpeningStat> openingsStat = new TreeMap<String, OpeningStat>();
    Map<DayOfWeek, WinRateStat> winRateByDay = initWinRateByDay();
    Map<Opponent, WinRateStat> winRateByOpponentRating = new HashMap<Opponent, WinRateStat>();
    winRateByOpponentRating.put(Opponent.LOWER_RATED, new WinRateStat());
    winRateByOpponentRating.put(Opponent.HIGHER_RATED, new WinRateStat());
    Map<Castle, WinRateStat> winRateByCastle = new HashMap<Castle, WinRateStat>();
    winRateByCastle.put(Castle.NONE, new WinRateStat());
    winRateByCastle.put(Castle.SHORT, new WinRateStat());
    winRateByCastle.put(Castle.LONG, new WinRateStat());
    
    LocalDate localDate = LocalDate.now();
    int totalBackwardMoves = 0;
    List<Game> listGames = new ArrayList<Game>();
    
    while (true) {
      numOfMonths++;
      PgnHolder pgn = gameDataAccess.getPgnHolder(playerUsername, localDate);
      pgn.loadPgn(); 
      log.info("Loaded {} games for {}/{}", pgn.getGames().size(), localDate.getYear(), localDate.getMonthValue());
     
      for (Game game: pgn.getGames()) {
        game.loadMoveText();  
        if (game.getHalfMoves().size() < MINIMUM_MOVE_FOR_GAME) {
          continue;
        }

        GameAttributes gameAttributes = getOpeningAttributesForGame(game, playerUsername);
        String openingName = gameAttributes.getChessOpening().getName();
        totalBackwardMoves += gameAttributes.getOpeningBackwardMoves();

        if (openingsStat.get(openingName) == null) {
          OpeningStat openStat = new OpeningStat(openingName, 0, 0, 0, null);
          openingsStat.put(openingName, openStat);
        }

        boolean isWhite = game.getWhitePlayer().getName().equalsIgnoreCase(playerUsername);
        boolean isHigherRated = false;
        if ((isWhite && game.getWhitePlayer().getElo() > game.getBlackPlayer().getElo()) || (!isWhite && game.getWhitePlayer().getElo() <= game.getBlackPlayer().getElo())) {
            isHigherRated = true;
        }
        openingsStat.get(openingName).addPieceMoveCounts(
            isWhite
                ? gameAttributes.getWhitePieceMoveCount()
                : gameAttributes.getBlackPieceMoveCount(),
            isWhite ? Perspective.AS_WHITE : Perspective.AS_BLACK);

        LocalDate playedDate = getDateFor(game);
        if (game.getResult() == GameResult.WHITE_WON && isWhite) {
          openingsStat.get(openingName).addOneWin(isWhite);
          winRateByDay.get(playedDate.getDayOfWeek()).addOneWin();
          winRateByOpponentRating.get(isHigherRated ? Opponent.LOWER_RATED : Opponent.HIGHER_RATED).addOneWin();       
          winRateByCastle.get(gameAttributes.getCastle()).addOneWin();
        } else if (game.getResult() == GameResult.BLACK_WON && !isWhite) {
          openingsStat.get(openingName).addOneWin(isWhite);
          winRateByDay.get(playedDate.getDayOfWeek()).addOneWin();
          winRateByOpponentRating.get(isHigherRated ? Opponent.LOWER_RATED : Opponent.HIGHER_RATED).addOneWin();
          winRateByCastle.get(gameAttributes.getCastle()).addOneWin();
        } else if (game.getResult() == GameResult.DRAW) {
          openingsStat.get(openingName).addOneDraw(isWhite);
          winRateByDay.get(playedDate.getDayOfWeek()).addOneDraw();
          winRateByOpponentRating.get(isHigherRated ? Opponent.LOWER_RATED : Opponent.HIGHER_RATED).addOneDraw();
          winRateByCastle.get(gameAttributes.getCastle()).addOneDraw();
        } else {
          winRateByDay.get(playedDate.getDayOfWeek()).addOneGame();   
          winRateByCastle.get(gameAttributes.getCastle()).addOneGame();
          winRateByOpponentRating.get(isHigherRated ? Opponent.LOWER_RATED : Opponent.HIGHER_RATED).addOneGame();
          openingsStat.get(openingName).addTotalGames(isWhite);
        }  
        openingsStat.get(openingName).getGameIds().add(game.getGameId());
      }
          
      numOfGames += pgn.getGames().size();
      log.info("Accumulated games so far: {} (months: {})", numOfGames, numOfMonths);
      localDate = localDate.minusMonths(1);
      if (numOfGames >= appConfig.getChesscomNumOfGamesLimit()
          || numOfMonths > appConfig.getChesscomNumOfMonthsLimit()) {
        log.info("Stopping indexing due to limit: gamesLimit={}, monthsLimit={}, totalGames={}, totalMonths={}",
            appConfig.getChesscomNumOfGamesLimit(), appConfig.getChesscomNumOfMonthsLimit(), numOfGames, numOfMonths);
        break;
      }
      listGames.addAll(pgn.getGames());
    }
    
    playerStatCache.reloadGames(playerUsername, ChessSite.CHESS_COM.getName(), listGames);
    playerStatCache.reloadDayOfWeekStat(
        playerUsername, ChessSite.CHESS_COM.getName(), winRateByDay);
    playerStatCache.reloadBackwardMoves(
        playerUsername, ChessSite.CHESS_COM.getName(), totalBackwardMoves);
    playerStatCache.reloadGamesAnalyzed(playerUsername, ChessSite.CHESS_COM.getName(), numOfGames);
    playerStatCache.reloadWinRateByOpponentRating(playerUsername, ChessSite.CHESS_COM.getName(), winRateByOpponentRating);
    playerStatCache.reloadWinRateByCastle(playerUsername, ChessSite.CHESS_COM.getName(), winRateByCastle);
    TreeSet <OpeningStat> openingStats = new TreeSet<OpeningStat>();
    openingsStat.entrySet().forEach(gameOpening -> {
      if (gameOpening.getValue().getTotalGames() > 2) {
        openingStats.add(gameOpening.getValue());
      }
    });
    log.info("Opening stats entries: {} (filtered with >2 games)", openingStats.size());
    playerStatCache.reloadOpeningStats(playerUsername, ChessSite.CHESS_COM.getName(), openingStats);
    playerStatCache.reloadGamesAnalyzed(playerUsername, ChessSite.CHESS_COM.getName(), numOfGames);
  }
  
  
  private Map<String, ChessOpening> getStaticOpeningMap() {
    if (openings != null) {
      return openings;
    }

    ChessTempoResult chessTempoResult = openingFileAccess.getOpenings();

    openings = new HashMap<>();
    for (ChessOpening chessOpening : chessTempoResult.getOpenings()) {
      openings.put(chessOpening.getLast_pos().split(" ")[0], chessOpening);
    }
    return openings;
  }

  private static Map<DayOfWeek, WinRateStat> initWinRateByDay() {
    Map<DayOfWeek, WinRateStat> winRateByDay = new HashMap();
    for (DayOfWeek dOW : DayOfWeek.values()) {
        winRateByDay.put(dOW, new WinRateStat());
    }
    return winRateByDay;
  }

  private LocalDate getDateFor(Game game) {
    return LocalDate.parse(game.getDate().replace('.', '-'));   
  }

  private static GameAttributes getOpeningAttributesForGame(Game game, String playerUsername) {
    Map<Integer, Map<PieceType, Long> > accumulatePieceMoveCount = new HashMap<>();
    Board board = new Board();
    MoveList moves = game.getHalfMoves();
    ChessOpening thisGameOpening = null;
    int lastOpeningMove = 0;
    int openingBackwardMoves = 0;
    boolean isWhite = game.getWhitePlayer().getName().equalsIgnoreCase(playerUsername);
    Castle castle = Castle.NONE;
    for (int i = 0; i < moves.size(); i++) {

      Move thisMove = moves.get(i);
      
      board.doMove(thisMove);
      // Only do opening analysis during the range.
      if (i <= OPENING_LIMIT) {
        if (openings.get(board.getFen().split(" ")[0]) != null) {
          ChessOpening currentOpening = openings.get(board.getFen().split(" ")[0]);
          if (thisGameOpening == null
              || !thisGameOpening.getName().equals(currentOpening.getName())) {
            lastOpeningMove = i;
          }
          thisGameOpening = openings.get(board.getFen().split(" ")[0]);
        }
        
        // Calculating backward move.
        if (isWhite && getPerspective(i) == Perspective.AS_WHITE) {
          if(moves.get(i).getFrom().getRank().compareTo(moves.get(i).getTo().getRank()) > 0) {
            openingBackwardMoves++;
          }
        }
        if (!isWhite && getPerspective(i) == Perspective.AS_BLACK) {
          if(moves.get(i).getFrom().getRank().compareTo(moves.get(i).getTo().getRank()) < 0) {
            openingBackwardMoves++;
          }
        }
        if ((isWhite && getPerspective(i) == Perspective.AS_WHITE) || (!isWhite && getPerspective(i) == Perspective.AS_BLACK)) {
          if (thisMove.getSan().equalsIgnoreCase("O-O")) {
            castle = Castle.SHORT;
          }
          if (thisMove.getSan().equalsIgnoreCase("O-O-O")) {
              castle = Castle.LONG;
          }
        }
      }
      calculatePieceMoveCount(
          board, thisMove, /* moveIndex= */ i, accumulatePieceMoveCount);
    }

    // For unknown openning, the last openning move will be the first move.
    if (thisGameOpening == null) {
      thisGameOpening = new ChessOpening();
      thisGameOpening.setName("Unknown");
    }

    return GameAttributes.builder()
        .chessOpening(thisGameOpening)
        .whitePieceMoveCount(
            getPieceMoveCountAfterOpening(
                accumulatePieceMoveCount, Perspective.AS_WHITE, lastOpeningMove, moves.size()))
        .blackPieceMoveCount(
            getPieceMoveCountAfterOpening(
                accumulatePieceMoveCount, Perspective.AS_BLACK, lastOpeningMove, moves.size()))
        .openingBackwardMoves(openingBackwardMoves)
        .castle(castle)
        .build();
  }

  /**
   * Record the accumulate of move by pieces up to |moveIndex|.
   */
  private static void calculatePieceMoveCount(
      Board board,
      Move move,
      int moveIndex,
      Map<Integer, Map<PieceType, Long> > pieceMoveCountForAllTurns) {
    // If we are still in the first turn, then instantiate all-zero-map, otherwise, copy the total
    // count since previous turn for this player.
    Map<PieceType, Long> pieceMoveCountToThisTurn =
        (moveIndex <= 1) // still in the first turn
            ? initPieceMoveCount()
            : makeDeepCopy(pieceMoveCountForAllTurns.get(moveIndex - 2));

    PieceType piece = board.getPiece(move.getTo()).getPieceType();
    pieceMoveCountToThisTurn.put(piece, pieceMoveCountToThisTurn.get(piece) + 1);
    pieceMoveCountForAllTurns.put(moveIndex, pieceMoveCountToThisTurn);

  }

  private static Map<PieceType, Long> initPieceMoveCount() {
    Map<PieceType, Long> pieceMoveCount = new HashMap<>();
    for (PieceType piece : PieceType.values()) {
      if (piece == PieceType.NONE) {
        continue;
      }
      pieceMoveCount.put(piece, 0L);
    }
    return pieceMoveCount;
  }

  private static Map<PieceType, Long> getPieceMoveCountAfterOpening(
      Map<Integer, Map<PieceType, Long> > accumulateCount,
      Perspective perspective,
      int lastOpeningMove,
      int totalMoves) {
    int lastMoveInGame = totalMoves - 1;

    int lastMoveByPlayer =
        getPerspective(lastMoveInGame) == perspective ? lastMoveInGame : lastMoveInGame - 1;

    // In case of unknown opening, we just return the total count from the beginning.
    if (lastOpeningMove == 0) {
      return makeDeepCopy(accumulateCount.get(lastMoveByPlayer));
    }

    int lastOpeningMoveByPlayer =
        getPerspective(lastOpeningMove) == perspective ? lastOpeningMove : lastOpeningMove - 1;

    Map<PieceType, Long> middleGameCount = initPieceMoveCount();
    for (PieceType piece : middleGameCount.keySet()) {
      long delta =
          accumulateCount.get(lastMoveByPlayer).get(piece)
              - accumulateCount.get(lastOpeningMoveByPlayer).get(piece);
      middleGameCount.put(piece, delta);
    }
    return middleGameCount;
  }

  private static Perspective getPerspective(int moveIndex) {
    return moveIndex % 2 == 0 ? Perspective.AS_WHITE : Perspective.AS_BLACK;
  }

  private static Map<PieceType, Long> makeDeepCopy(Map<PieceType, Long> target) {
    Map<PieceType, Long> copy = new HashMap<>();
    for (Map.Entry<PieceType, Long> entry : target.entrySet()) {
      copy.put(entry.getKey(), entry.getValue());
    }
    return copy;
  }

  // Contains specific statistic of a specific game.
  @lombok.Builder
  @Data
  private static class GameAttributes {

    // The the opening variation played.
    private ChessOpening chessOpening;

    // The statistic of piece moved by White after the middle game.
    private Map<PieceType, Long> whitePieceMoveCount;

    // The statistic of piece moved by Black after the middle game.
    private Map<PieceType, Long> blackPieceMoveCount;

    // The count of the backward moves in the opening by the queried player.
    private int openingBackwardMoves;
    
    private Castle castle;
  }
}
