package com.inspireon.chessanalyzer.dtos;

import java.util.ArrayList;
import java.util.List;

public class OpeningStat implements Comparable<OpeningStat> {
	
	private String name;
	
	private Integer whiteWon;
	
	private Integer whiteDraw;
	
	private Integer totalWhite;
	
	private Integer blackWon;
	
	private Integer totalBlack;
	
	private Integer blackDraw;
	
	private Integer won;
	
	private Integer totalGames;
	
	private Integer draw;

	private List<String> gameIds;
	

	public OpeningStat(String name, Integer won, Integer lost, Integer draw, List<String> gameIds) {
		super();
		this.name = name;
		this.won = won;
		this.totalGames = lost;
		this.draw = draw;
		this.gameIds = gameIds;
		if (this.gameIds == null) {
			this.gameIds = new ArrayList<String>();
		}
		this.whiteWon = 0;
		this.whiteDraw = 0;
		this.totalWhite = 0;
		this.blackWon = 0;
		this.blackDraw = 0;
		this.totalBlack = 0;
	}
	
	public Integer getWinRate() {
		if (totalGames == 0) return -1;
		return Math.round(won * 100 / totalGames);
	}
	
	public Integer getWinRateAsWhite() {
		if (totalWhite == 0) return -1;
		return Math.round(whiteWon * 100 / totalWhite);
	}
	
	public Integer getWinRateAsBlack() {
		if (totalBlack == 0) return -1;
		return Math.round(blackWon * 100 / totalBlack);
	}
	
	public void addOneWin(boolean isWhite) {
		if (isWhite) {
			whiteWon++;
			totalWhite++;
		} else {
			blackWon++;
			totalBlack++;
		}
		won++;
		totalGames++;
	}
	
	public void addOneDraw(boolean isWhite) {
		if (isWhite) {
			whiteDraw++;
			totalWhite++;
		} else {
			blackDraw++;
			totalBlack++;
		}
		draw++;
		totalGames++;
	}
	
	public void addTotalGames(boolean isWhite) {
		if (isWhite) {
			totalWhite++;
		} else {
			totalBlack++;
		}		
		totalGames++;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getWon() {
		return won;
	}

	public void setWon(Integer won) {
		this.won = won;
	}

	public Integer getTotalGames() {
		return totalGames;
	}

	public void setTotalGames(Integer totalGames) {
		this.totalGames = totalGames;
	}

	public Integer getDraw() {
		return draw;
	}

	public void setDraw(Integer draw) {
		this.draw = draw;
	}

	public List<String> getGameIds() {
		return gameIds;
	}

	public void setGameIds(List<String> gameIds) {
		this.gameIds = gameIds;
	}


	public Integer getWhiteWon() {
		return whiteWon;
	}

	public void setWhiteWon(Integer whiteWon) {
		this.whiteWon = whiteWon;
	}

	public Integer getWhiteDraw() {
		return whiteDraw;
	}

	public void setWhiteDraw(Integer whiteDraw) {
		this.whiteDraw = whiteDraw;
	}

	public Integer getTotalWhite() {
		return totalWhite;
	}

	public void setTotalWhite(Integer totalWhite) {
		this.totalWhite = totalWhite;
	}

	public Integer getBlackWon() {
		return blackWon;
	}

	public void setBlackWon(Integer blackWon) {
		this.blackWon = blackWon;
	}

	public Integer getTotalBlack() {
		return totalBlack;
	}

	public void setTotalBlack(Integer totalBlack) {
		this.totalBlack = totalBlack;
	}

	public Integer getBlackDraw() {
		return blackDraw;
	}

	public void setBlackDraw(Integer blackDraw) {
		this.blackDraw = blackDraw;
	}

	@Override
	public int compareTo(OpeningStat o) {
		return o.totalGames.compareTo(this.totalGames);
	}
	
	
	
}
