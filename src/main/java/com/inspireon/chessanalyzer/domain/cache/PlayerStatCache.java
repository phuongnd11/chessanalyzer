package com.inspireon.chessanalyzer.domain.cache;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.github.bhlangonijr.chesslib.game.Game;
import com.inspireon.chessanalyzer.common.enums.Castle;
import com.inspireon.chessanalyzer.common.enums.Opponent;
import com.inspireon.chessanalyzer.web.dtos.OpeningStat;
import com.inspireon.chessanalyzer.web.dtos.WinRateStat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class PlayerStatCache {

  //username-site -> games
  private ConcurrentHashMap<String, List<Game>> playerGames = new ConcurrentHashMap<>();
  
  //username-site -> opening stats
  private ConcurrentHashMap<String, TreeSet<OpeningStat>> playerOpeningStats =
      new ConcurrentHashMap<>();
  
  //username-site -> opening style
  private ConcurrentHashMap<String, TreeSet<OpeningStat>> playerOpeningStyle =
      new ConcurrentHashMap<>();

  //username-site -> dayOfWeekStats
  private ConcurrentHashMap<String, Map<DayOfWeek, WinRateStat>> dayOfWeekStats =
      new ConcurrentHashMap<>();
  
  //username-site -> num of backward moves
  private ConcurrentHashMap<String, Integer> backwardMoves = new ConcurrentHashMap<>();
  
  //username-site -> num of games analyzed
  private ConcurrentHashMap<String, Integer> gamesAnalyzed = new ConcurrentHashMap<String, Integer>();

  //username-site -> winRateByOpponentRating
  private ConcurrentHashMap<String, Map<Opponent, WinRateStat>> winRateByOpponentRating = new ConcurrentHashMap<>();
  
  //username-site -> winRateByCastle
  private ConcurrentHashMap<String, Map<Castle, WinRateStat>> winRateByCastle = new ConcurrentHashMap<>();
  
  public void reloadGames(String player, String site, List<Game> games) {
    playerGames.putIfAbsent(player + "-" + site, new ArrayList<Game>());
    playerGames.computeIfPresent(player + "-" + site, (key, val) -> games);
  }
  
  public void reloadOpeningStats(String player, String site, TreeSet<OpeningStat> openingStats) {
    playerOpeningStats.putIfAbsent(player + "-" + site, new TreeSet<OpeningStat>());
    playerOpeningStats.computeIfPresent(player + "-" + site, (key, val) -> openingStats);
  }

  public void reloadDayOfWeekStat(
      String player, String site, Map<DayOfWeek, WinRateStat> winRateByDay) {
    dayOfWeekStats.putIfAbsent(player + "-" + site, new HashMap<DayOfWeek, WinRateStat>());
    dayOfWeekStats.computeIfPresent(player + "-" + site, (key, val) -> winRateByDay);
  }
  
  public void reloadWinRateByOpponentRating(String player, String site, Map<Opponent, WinRateStat> byOpponentRatingStats) {
    winRateByOpponentRating.putIfAbsent(player + "-" + site, new HashMap<Opponent, WinRateStat>());
    winRateByOpponentRating.computeIfPresent(player + "-" + site, (key, val) -> byOpponentRatingStats);
  }
  
  public void reloadWinRateByCastle(String player, String site, Map<Castle, WinRateStat> byCastleTypeStats) {
    winRateByCastle.putIfAbsent(player + "-" + site, new HashMap<Castle, WinRateStat>());
    winRateByCastle.computeIfPresent(player + "-" + site, (key, val) -> byCastleTypeStats);
  }
  
  public void reloadBackwardMoves(String player, String site, Integer numOfbackwardMoves) {
    backwardMoves.putIfAbsent(player + "-" + site, numOfbackwardMoves);
    backwardMoves.computeIfPresent(player + "-" + site, (key, val) -> numOfbackwardMoves);
  }
  
  public void reloadGamesAnalyzed(String player, String site, Integer numOfgamesAnalyzed) {
    gamesAnalyzed.putIfAbsent(player + "-" + site, numOfgamesAnalyzed);
    gamesAnalyzed.computeIfPresent(player + "-" + site, (key, val) -> numOfgamesAnalyzed);
  }
}
