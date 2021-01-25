package com.inspireon.chessanalyzer.web.dtos;

import lombok.Getter;

public class WinRateStat {
  private Integer won;

  private Integer totalGames;

  private Integer draw;
  
  public WinRateStat() {
    won = 0;
    draw = 0;
    totalGames = 0;
  }
  
  public void addOneWin() {
    won++;
    totalGames++;
  }
  
  public void addOneDraw() {
    draw++;
    totalGames++;
  }
  
  public void addOneGame() {
    totalGames++;
  }
  
  public Integer getWinRate() {
    if (totalGames == 0) return -1;
    return Math.round(won * 100 / totalGames);
  }
  
}
