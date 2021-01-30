package com.inspireon.chessanalyzer.domain.datamanager;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.game.GameResult;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import com.inspireon.chessanalyzer.common.enums.ChessSite;
import com.inspireon.chessanalyzer.common.io.OpeningFileAccess;
import com.inspireon.chessanalyzer.domain.cache.PlayerStatCache;
import com.inspireon.chessanalyzer.domain.model.ChessOpening;
import com.inspireon.chessanalyzer.domain.model.ChessTempoResult;
import com.inspireon.chessanalyzer.web.dtos.OpeningStat;
import com.inspireon.chessanalyzer.web.dtos.OpeningStat.Perspective;
import com.inspireon.chessanalyzer.web.dtos.WinRateStat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OpeningIndexer {
  
  @Autowired
  private GameDataAccess gameDataAccess;
  
  @Autowired
  private PlayerStatCache playerStatCache;
  
  @Autowired
  private OpeningFileAccess openingFileAccess;

  // A static mapping of chess openings.
  private static Map<String, ChessOpening> openings;

  private static final int OPENING_LIMIT = 15;

  private static final int MINIMUM_MOVE_FOR_GAME = 4;

  
  public void indexOpening(String playerUsername) throws Exception {
    Map<String, ChessOpening> openings = getStaticOpeningMap();

    
    int numOfGames = 0;
    int numOfMonths = 0;

    SortedMap<String, OpeningStat> openingsStat = new TreeMap<String, OpeningStat>();
    Map<DayOfWeek, WinRateStat> winRateByDay = initWinRateByDay();

    LocalDate localDate = LocalDate.now();

    while (true) {
      numOfMonths++;
      PgnHolder pgn = gameDataAccess.getPgnHolder(playerUsername, localDate);
      pgn.loadPgn(); 
      for (Game game: pgn.getGames()) {
        game.loadMoveText();  
        if (game.getHalfMoves().size() < MINIMUM_MOVE_FOR_GAME) {
          continue;
        }

        OpeningAttributesForGame openingAttributeForGame = getOpeningAttributesForGame(game);
        String openingName = openingAttributeForGame.getChessOpening().getName();

        if (openingsStat.get(openingName) == null) {
          OpeningStat openStat = new OpeningStat(openingName, 0, 0, 0, null);
          openingsStat.put(openingName, openStat);
        }

        boolean isWhite = game.getWhitePlayer().getName().equalsIgnoreCase(playerUsername);
        openingsStat.get(openingName).addPieceMoveCounts(
            openingAttributeForGame.getWhitePieceMoveCount(),
            isWhite ? Perspective.AS_WHITE : Perspective.AS_BLACK);

        LocalDate playedDate = getDateFor(game);
        if (game.getResult() == GameResult.WHITE_WON && isWhite) {
          openingsStat.get(openingName).addOneWin(isWhite);
          winRateByDay.get(playedDate.getDayOfWeek()).addOneWin();
        } else if (game.getResult() == GameResult.BLACK_WON && !isWhite) {
          openingsStat.get(openingName).addOneWin(isWhite);
          winRateByDay.get(playedDate.getDayOfWeek()).addOneWin();
        } else if (game.getResult() == GameResult.DRAW) {
          openingsStat.get(openingName).addOneDraw(isWhite);
          winRateByDay.get(playedDate.getDayOfWeek()).addOneDraw();
        } else {
          winRateByDay.get(playedDate.getDayOfWeek()).addOneGame();   
          openingsStat.get(openingName).addTotalGames(isWhite);
        }  
        openingsStat.get(openingName).getGameIds().add(game.getGameId());
      }
          
      numOfGames += pgn.getGames().size();
      localDate = localDate.minusMonths(1);
      if (numOfGames >= 1000 || numOfMonths > 24) {
        break;
      }
      playerStatCache.reloadGames(playerUsername, ChessSite.CHESS_COM.getName(), pgn.getGames());
      playerStatCache.reloadDayOfWeekStat(
          playerUsername, ChessSite.CHESS_COM.getName(), winRateByDay);
    }
    
    TreeSet <OpeningStat> openingStats = new TreeSet<OpeningStat>();
    openingsStat.entrySet().forEach(gameOpening -> {
      if (gameOpening.getValue().getTotalGames() > 2) {
        //System.out.println(
        //    "winRate: "
        //        + Math.round(
        //            gameOpening.getValue().getWon()*100/gameOpening.getValue().getTotalGames())
        //        + "   total games : "
        //        + gameOpening.getValue().getTotalGames()
        //        + "   " + gameOpening.getKey()) ;
        openingStats.add(gameOpening.getValue());
      }
    });
    System.out.println(openingStats.size());
    playerStatCache.reloadOpeningStats(playerUsername, ChessSite.CHESS_COM.getName(), openingStats);
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

  private static OpeningAttributesForGame getOpeningAttributesForGame(Game game) {
    ChessOpening thisGameOpening = null;
    Board board = new Board();
    Map<PieceType, Long> whitePieceMoveCount = initPieceMoveCount();
    Map<PieceType, Long> blackPieceMoveCount = initPieceMoveCount();

    MoveList moves = game.getHalfMoves();
    for (int i = 0; i < moves.size(); i++) {
      // Stop after reaching the opening limit.
      if (i > OPENING_LIMIT) {
        break;
      }

      Move thisMove = moves.get(i);
      board.doMove(thisMove);
      if (openings.get(board.getFen().split(" ")[0]) != null) {
        thisGameOpening = openings.get(board.getFen().split(" ")[0]);
      }
      increasePieceMoveCountByOne(
          board, thisMove, whitePieceMoveCount, blackPieceMoveCount, isWhiteMove(i));
    }

    if (thisGameOpening == null) {
      thisGameOpening = new ChessOpening();
      thisGameOpening.setName("Unknown");
    }

    return OpeningAttributesForGame.builder()
        .chessOpening(thisGameOpening)
        .whitePieceMoveCount(whitePieceMoveCount)
        .blackPieceMoveCount(blackPieceMoveCount)
        .build();
  }

  private static boolean isWhiteMove(int moveIndex) {
    return moveIndex % 2 == 0;
  }

  private static void increasePieceMoveCountByOne(
      Board board,
      Move lastMove,
      Map<PieceType, Long> whitePieceMoveCount,
      Map<PieceType, Long> blackPieceMoveCount,
      boolean isWhiteMove) {
    PieceType piece = board.getPiece(lastMove.getTo()).getPieceType();
    if (isWhiteMove) {
      whitePieceMoveCount.put(piece, whitePieceMoveCount.get(piece) + 1);
    } else {
      blackPieceMoveCount.put(piece, blackPieceMoveCount.get(piece) + 1);
    }
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

  // Contains specific statistic at the opening stage of a specific game.
  @lombok.Builder
  @Data
  private static class OpeningAttributesForGame {

    // The the opening variation played.
    private ChessOpening chessOpening;

    // The statistic of piece moved by White.
    private Map<PieceType, Long> whitePieceMoveCount;

    // The statistic of piece moved by Black.
    private Map<PieceType, Long> blackPieceMoveCount;
  }
}
