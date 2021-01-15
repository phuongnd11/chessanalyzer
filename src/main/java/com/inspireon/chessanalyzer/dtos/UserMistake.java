package com.inspireon.chessanalyzer.dtos;

public class UserMistake {
  
  private String gameId;
  
  private String username;
  
  private String fen;
  
  private String userMove;
  
  private String computerMove;
  

  public UserMistake(String gameId, String username, String fen, String userMove, String computerMove) {
    super();
    this.gameId = gameId;
    this.username = username;
    this.fen = fen;
    this.userMove = userMove;
    this.computerMove = computerMove;
  }

  public String getGameId() {
    return gameId;
  }

  public void setGameId(String gameId) {
    this.gameId = gameId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getFen() {
    return fen;
  }

  public void setFen(String fen) {
    this.fen = fen;
  }

  public String getUserMove() {
    return userMove;
  }

  public void setUserMove(String userMove) {
    this.userMove = userMove;
  }

  public String getComputerMove() {
    return computerMove;
  }

  public void setComputerMove(String computerMove) {
    this.computerMove = computerMove;
  }
  
}
