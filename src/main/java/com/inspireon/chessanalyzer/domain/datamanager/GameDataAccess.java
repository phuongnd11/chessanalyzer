package com.inspireon.chessanalyzer.domain.datamanager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import com.inspireon.chessanalyzer.application.clients.ChessComApiClient;
import com.inspireon.chessanalyzer.common.enums.ChessSite;
import com.inspireon.chessanalyzer.common.io.PgnFileAccess;
import com.inspireon.chessanalyzer.domain.cache.PlayerStatCache;
import com.inspireon.chessanalyzer.web.dtos.OpeningStat;

@Component
public class GameDataAccess {
  
  @Autowired
  private PgnFileAccess pgnFileAccess;
  
  @Autowired
  private ChessComApiClient chessComApiClient;
  
  @Autowired
  private PlayerStatCache playerStatCache;
  
  @Autowired
  private OpeningIndexer openingIndexer;
  
  public List<Game> getGames(String playerUsername) throws Exception {
    List<Game> games = playerStatCache.getPlayerGames().get(playerUsername + "-" + ChessSite.CHESS_COM.getName());
    
    if (games == null || games.size() == 0) {
      openingIndexer.indexOpening(playerUsername);
      games = playerStatCache.getPlayerGames().get(playerUsername + "-" + ChessSite.CHESS_COM.getName());
    }
    return games;
  }
  
  public TreeSet <OpeningStat> getOpenings(String playerUsername) throws Exception {
    TreeSet<OpeningStat> openingStatCache = playerStatCache.getPlayerOpeningStats().get(playerUsername + "-" + ChessSite.CHESS_COM.getName());
    if (openingStatCache != null && openingStatCache.size() > 0) {
      return openingStatCache;
    }
    
    openingIndexer.indexOpening(playerUsername);
    return playerStatCache.getPlayerOpeningStats().get(playerUsername + "-" + ChessSite.CHESS_COM.getName());
  }
  
  public PgnHolder getPgnHolder(String playerUsername, LocalDate localDate) throws IOException, MalformedURLException {
    PgnHolder pgn = null;
    if (!new File(getPgnFilePath(playerUsername, localDate.getMonthValue())).exists() || localDate.getMonth().equals(LocalDate.now().getMonth())) {
      loadPgnFile(playerUsername, localDate.getYear(), localDate.getMonthValue());
    } 
    pgn = new PgnHolder(getPgnFilePath(playerUsername, localDate.getMonthValue()));
    return pgn;
  }
  
  public void loadPgnFile(String playerUserName, int year, int month) throws MalformedURLException, IOException {  
      BufferedInputStream in = chessComApiClient.getPgnAsInputStream(playerUserName, year, month);
    
      pgnFileAccess.writePgnFile(in, playerUserName, month);
    }
  
  public String getPgnFilePath(String playerUserName, int month) throws IOException {
    return pgnFileAccess.getPgnFilePath(playerUserName, month);
  }
}
