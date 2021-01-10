package com.inspireon.chessanalyzer.application;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import com.github.bhlangonijr.chesslib.game.Game;
import com.inspireon.chessanalyzer.dtos.OpeningStat;

public class PlayerStatCache {

	private static final PlayerStatCache instance = new PlayerStatCache();

	//username-site -> games
	private ConcurrentHashMap<String, List<Game>> playerGames = new ConcurrentHashMap<String, List<Game>>();
	
	//username-site -> games
	private ConcurrentHashMap<String, TreeSet<OpeningStat>> playerOpeningStats = new ConcurrentHashMap<String, TreeSet<OpeningStat>>();
	
	//username-site -> opening category
	private ConcurrentHashMap<String, TreeSet<OpeningStat>> playerOpeningStyle = new ConcurrentHashMap<String, TreeSet<OpeningStat>>();
		
	private PlayerStatCache() {
		// private constructor
	}

	public static PlayerStatCache getInstance() {
		return instance;
	}

	public void reloadGames(String player, String site, List<Game> games) {
		playerGames.putIfAbsent(player + "-" + site, new ArrayList<Game>());
		playerGames.computeIfPresent(player + "-" + site, (key, val) -> games);
	}
	
	public void reloadOpeningStats(String player, String site, TreeSet<OpeningStat> openingStats) {
		playerOpeningStats.putIfAbsent(player + "-" + site, new TreeSet<OpeningStat>());
		playerOpeningStats.computeIfPresent(player + "-" + site, (key, val) -> openingStats);
	}

	public ConcurrentHashMap<String, List<Game>> getPlayerGames() {
		return playerGames;
	}

	public void setPlayerGames(ConcurrentHashMap<String, List<Game>> playerGames) {
		this.playerGames = playerGames;
	}

	public ConcurrentHashMap<String, TreeSet<OpeningStat>> getPlayerOpeningStats() {
		return playerOpeningStats;
	}

	public void setPlayerOpeningStats(ConcurrentHashMap<String, TreeSet<OpeningStat>> playerOpeningStats) {
		this.playerOpeningStats = playerOpeningStats;
	}

}
