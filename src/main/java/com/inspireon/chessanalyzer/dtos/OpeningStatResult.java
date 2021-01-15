package com.inspireon.chessanalyzer.dtos;

import java.util.List;

public class OpeningStatResult {
  
  private String name;
  
  private Integer winRate;
  
  private Integer numOfGames;

  private List<String> gameIds;
  
  public OpeningStatResult(String name, Integer winRate, Integer numOfGames) {
    super();
    this.name = name;
    this.winRate = winRate;
    this.numOfGames = numOfGames;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public Integer getWinRate() {
    return winRate;
  }

  public void setWinRate(Integer winRate) {
    this.winRate = winRate;
  }

  public Integer getNumOfGames() {
    return numOfGames;
  }

  public void setNumOfGames(Integer numOfGames) {
    this.numOfGames = numOfGames;
  }

  
}
