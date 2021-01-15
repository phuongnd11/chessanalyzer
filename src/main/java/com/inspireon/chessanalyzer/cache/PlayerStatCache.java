package com.inspireon.chessanalyzer.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.github.bhlangonijr.chesslib.game.Game;
import com.inspireon.chessanalyzer.dtos.OpeningStat;

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


  public void reloadGames(String player, String site, List<Game> games) {
    playerGames.putIfAbsent(player + "-" + site, new ArrayList<Game>());
    playerGames.computeIfPresent(player + "-" + site, (key, val) -> games);
  }
  
  public void reloadOpeningStats(String player, String site, TreeSet<OpeningStat> openingStats) {
    playerOpeningStats.putIfAbsent(player + "-" + site, new TreeSet<OpeningStat>());
    playerOpeningStats.computeIfPresent(player + "-" + site, (key, val) -> openingStats);
  }

}
