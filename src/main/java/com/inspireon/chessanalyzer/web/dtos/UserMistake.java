package com.inspireon.chessanalyzer.web.dtos;

import com.inspireon.chessanalyzer.common.enums.TacticalTheme;

public class UserMistake {
  
  private String gameId;
  
  private String username;
  
  private String fen;
  
  private String userMove;
  
  private String computerMove;
  
  private String gamePhase;
  
  private int moveNumber;
  
  private int scoreDrop;
  
  private TacticalTheme tacticalTheme;
  
  private TacticalEvidence evidence;

  public UserMistake(String gameId, String username, String fen, String userMove, String computerMove) {
    super();
    this.gameId = gameId;
    this.username = username;
    this.fen = fen;
    this.userMove = userMove;
    this.computerMove = computerMove;
  }
  
  public UserMistake(String gameId, String username, String fen, String userMove, String computerMove, 
                    String gamePhase, int moveNumber, int scoreDrop) {
    super();
    this.gameId = gameId;
    this.username = username;
    this.fen = fen;
    this.userMove = userMove;
    this.computerMove = computerMove;
    this.gamePhase = gamePhase;
    this.moveNumber = moveNumber;
    this.scoreDrop = scoreDrop;
    this.tacticalTheme = TacticalTheme.UNKNOWN;
  }
  
  public UserMistake(String gameId, String username, String fen, String userMove, String computerMove, 
                    String gamePhase, int moveNumber, int scoreDrop, TacticalTheme tacticalTheme) {
    super();
    this.gameId = gameId;
    this.username = username;
    this.fen = fen;
    this.userMove = userMove;
    this.computerMove = computerMove;
    this.gamePhase = gamePhase;
    this.moveNumber = moveNumber;
    this.scoreDrop = scoreDrop;
    this.tacticalTheme = tacticalTheme;
    this.evidence = null; // Default to null, can be set separately
  }
  
  public UserMistake(String gameId, String username, String fen, String userMove, String computerMove, 
                    String gamePhase, int moveNumber, int scoreDrop, TacticalTheme tacticalTheme, TacticalEvidence evidence) {
    super();
    this.gameId = gameId;
    this.username = username;
    this.fen = fen;
    this.userMove = userMove;
    this.computerMove = computerMove;
    this.gamePhase = gamePhase;
    this.moveNumber = moveNumber;
    this.scoreDrop = scoreDrop;
    this.tacticalTheme = tacticalTheme;
    this.evidence = evidence;
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
  
  public String getGamePhase() {
    return gamePhase;
  }
  
  public void setGamePhase(String gamePhase) {
    this.gamePhase = gamePhase;
  }
  
  public int getMoveNumber() {
    return moveNumber;
  }
  
  public void setMoveNumber(int moveNumber) {
    this.moveNumber = moveNumber;
  }
  
  public int getScoreDrop() {
    return scoreDrop;
  }
  
  public void setScoreDrop(int scoreDrop) {
    this.scoreDrop = scoreDrop;
  }
  
  public TacticalTheme getTacticalTheme() {
    return tacticalTheme;
  }
  
  public void setTacticalTheme(TacticalTheme tacticalTheme) {
    this.tacticalTheme = tacticalTheme;
  }
  
  public TacticalEvidence getEvidence() {
    return evidence;
  }
  
  public void setEvidence(TacticalEvidence evidence) {
    this.evidence = evidence;
  }
  
}
