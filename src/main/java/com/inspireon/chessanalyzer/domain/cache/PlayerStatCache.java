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
import com.inspireon.chessanalyzer.web.dtos.OpeningStat;
import com.inspireon.chessanalyzer.web.dtos.WinRateStat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class PlayerStatCache {

  //username-site -> games
  private ConcurrentHashMap<String, List<Game>> playerGames = new ConcurrentHashMap<String, List<Game>>();
  
  //username-site -> games
  private ConcurrentHashMap<String, TreeSet<OpeningStat>> playerOpeningStats = new ConcurrentHashMap<String, TreeSet<OpeningStat>>();
  
  //username-site -> opening category
  private ConcurrentHashMap<String, TreeSet<OpeningStat>> playerOpeningStyle = new ConcurrentHashMap<String, TreeSet<OpeningStat>>();

  //username-site -> opening category
  private ConcurrentHashMap<String, Map<DayOfWeek, WinRateStat>> dayOfWeekStats = new ConcurrentHashMap<String, Map<DayOfWeek, WinRateStat>>();
  
  //username-site -> num of backward moves
  private ConcurrentHashMap<String, Integer> backwardMoves = new ConcurrentHashMap<String, Integer>();

  public void reloadGames(String player, String site, List<Game> games) {
    playerGames.putIfAbsent(player + "-" + site, new ArrayList<Game>());
    playerGames.computeIfPresent(player + "-" + site, (key, val) -> games);
  }
  
  public void reloadOpeningStats(String player, String site, TreeSet<OpeningStat> openingStats) {
    playerOpeningStats.putIfAbsent(player + "-" + site, new TreeSet<OpeningStat>());
    playerOpeningStats.computeIfPresent(player + "-" + site, (key, val) -> openingStats);
  }

  public void reloadDayOfWeekStat(String player, String site, Map<DayOfWeek, WinRateStat> winRateByDay) {
    dayOfWeekStats.putIfAbsent(player + "-" + site, new HashMap<DayOfWeek, WinRateStat>());
    dayOfWeekStats.computeIfPresent(player + "-" + site, (key, val) -> winRateByDay);
  }
  
  public void reloadBackwardMoves(String player, String site, Integer numOfbackwardMoves) {
      backwardMoves.putIfAbsent(player + "-" + site, numOfbackwardMoves);
      backwardMoves.computeIfPresent(player + "-" + site, (key, val) -> numOfbackwardMoves);
    }
}
