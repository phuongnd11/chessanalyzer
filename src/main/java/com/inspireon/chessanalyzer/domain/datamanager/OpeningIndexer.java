package com.inspireon.chessanalyzer.domain.datamanager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.game.GameResult;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import com.inspireon.chessanalyzer.AppConfig;
import com.inspireon.chessanalyzer.common.enums.ChessSite;
import com.inspireon.chessanalyzer.common.io.OpeningFileAccess;
import com.inspireon.chessanalyzer.domain.cache.PlayerStatCache;
import com.inspireon.chessanalyzer.domain.model.ChessOpening;
import com.inspireon.chessanalyzer.domain.model.ChessTempoResult;
import com.inspireon.chessanalyzer.web.dtos.OpeningStat;
import com.inspireon.chessanalyzer.web.dtos.WinRateStat;

@Service
public class OpeningIndexer {
  
  @Autowired
  private GameDataAccess gameDataAccess;
  
  @Autowired
  private PlayerStatCache playerStatCache;
  
  @Autowired
  private OpeningFileAccess openingFileAccess;
  
  @Autowired
  private AppConfig appConfig;
  
  public void indexOpening(String playerUsername) throws Exception {
    ChessTempoResult chessTempoResult = openingFileAccess.getOpenings();
    
    Map<String, ChessOpening> openings = new HashMap<String, ChessOpening>();
    
    for (ChessOpening chessOpening : chessTempoResult.getOpenings()) {
      openings.put(chessOpening.getLast_pos().split(" ")[0], chessOpening);
    }
    
    int numOfGames = 0;
    int numOfMonths = 0;
    TreeSet <OpeningStat> openingStats = new TreeSet<OpeningStat>();
    LocalDate localDate = LocalDate.now();
    SortedMap<String, OpeningStat> winrateByOpening = new TreeMap<String, OpeningStat>();
    Map<DayOfWeek, WinRateStat> winRateByDay = new HashMap<DayOfWeek, WinRateStat>();
    for (DayOfWeek dOW : DayOfWeek.values()) {
        winRateByDay.put(dOW, new WinRateStat());
    }
    while (true) {
      numOfMonths++;
      PgnHolder pgn = gameDataAccess.getPgnHolder(playerUsername, localDate);
      pgn.loadPgn(); 
        for (Game game: pgn.getGames()) {
          game.loadMoveText();  
          LocalDate playedDate = LocalDate.parse(game.getDate().replace('.', '-'));   
          MoveList moves = game.getHalfMoves();
          if (moves.size() < 4) continue;
          Board board = new Board();
          ChessOpening thisGameOpening = null;
          for (int i = 0; i < moves.size(); i++) {
            board.doMove(moves.get(i));
            if (openings.get(board.getFen().split(" ")[0]) != null)
            thisGameOpening = openings.get(board.getFen().split(" ")[0]);
            //System.out.println(board.getFen().split(" ")[0]);
            if (i == 15) break;
          }
          if (thisGameOpening == null) {
            thisGameOpening = new ChessOpening();
            thisGameOpening.setName("Unknown");
          }
              
          if (winrateByOpening.get(thisGameOpening.getName()) == null) {
            OpeningStat openStat = new OpeningStat(thisGameOpening.getName(), 0, 0, 0, null);
            winrateByOpening.put(thisGameOpening.getName(), openStat);
          }
          boolean isWhite = game.getWhitePlayer().getName().equalsIgnoreCase(playerUsername);
          if (game.getResult() == GameResult.WHITE_WON && isWhite) {
            winrateByOpening.get(thisGameOpening.getName()).addOneWin(isWhite);
            winRateByDay.get(playedDate.getDayOfWeek()).addOneWin();
          } else if (game.getResult() == GameResult.BLACK_WON && !isWhite) {
            winrateByOpening.get(thisGameOpening.getName()).addOneWin(isWhite);
            winRateByDay.get(playedDate.getDayOfWeek()).addOneWin();
          } else if (game.getResult() == GameResult.DRAW) {
            winrateByOpening.get(thisGameOpening.getName()).addOneDraw(isWhite);
            winRateByDay.get(playedDate.getDayOfWeek()).addOneDraw();
          } else {
            winRateByDay.get(playedDate.getDayOfWeek()).addOneGame();   
            winrateByOpening.get(thisGameOpening.getName()).addTotalGames(isWhite);
          }  
          winrateByOpening.get(thisGameOpening.getName()).getGameIds().add(game.getGameId());
       }
          
       numOfGames += pgn.getGames().size();
       localDate = localDate.minusMonths(1);
       if (numOfGames >= appConfig.getChesscomNumOfGamesLimit() || numOfMonths > appConfig.getChesscomNumOfMonthsLimit()) {
         break;
       }
       playerStatCache.reloadGames(playerUsername, ChessSite.CHESS_COM.getName(), pgn.getGames());
       playerStatCache.reloadDayOfWeekStat(playerUsername, ChessSite.CHESS_COM.getName(), winRateByDay);
    }
    
    winrateByOpening.entrySet().forEach(gameOpenin -> {
      if (gameOpenin.getValue().getTotalGames() > 2) {
        //System.out.println("winRate: " + Math.round(gameOpenin.getValue().getWon()*100/gameOpenin.getValue().getTotalGames()) +
        //    "   total games : " + gameOpenin.getValue().getTotalGames() + "   " + gameOpenin.getKey()) ;
        openingStats.add(gameOpenin.getValue());
      }
    });
    System.out.println(openingStats.size());
    playerStatCache.reloadOpeningStats(playerUsername, ChessSite.CHESS_COM.getName(), openingStats);
  }
}
