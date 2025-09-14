package com.inspireon.chessanalyzer.web.dtos;

import java.time.LocalDate;
import java.util.List;

public class GameAnalysis {
    
    private String gameId;
    private LocalDate datePlayed;
    private String playerColor;
    private String whitePlayer;
    private String blackPlayer;
    private String whiteElo;
    private String blackElo;
    private String gameResult;
    private String timeControl;
    private List<UserMistake> mistakes;
    
    public GameAnalysis(String gameId, LocalDate datePlayed, String playerColor, List<UserMistake> mistakes) {
        this.gameId = gameId;
        this.datePlayed = datePlayed;
        this.playerColor = playerColor;
        this.mistakes = mistakes;
    }
    
    public GameAnalysis(String gameId, LocalDate datePlayed, String playerColor, 
                       String whitePlayer, String blackPlayer, String whiteElo, String blackElo,
                       String gameResult, String timeControl, List<UserMistake> mistakes) {
        this.gameId = gameId;
        this.datePlayed = datePlayed;
        this.playerColor = playerColor;
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.whiteElo = whiteElo;
        this.blackElo = blackElo;
        this.gameResult = gameResult;
        this.timeControl = timeControl;
        this.mistakes = mistakes;
    }
    
    public String getGameId() {
        return gameId;
    }
    
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
    
    public LocalDate getDatePlayed() {
        return datePlayed;
    }
    
    public void setDatePlayed(LocalDate datePlayed) {
        this.datePlayed = datePlayed;
    }
    
    public String getPlayerColor() {
        return playerColor;
    }
    
    public void setPlayerColor(String playerColor) {
        this.playerColor = playerColor;
    }
    
    public List<UserMistake> getMistakes() {
        return mistakes;
    }
    
    public void setMistakes(List<UserMistake> mistakes) {
        this.mistakes = mistakes;
    }
    
    public String getWhitePlayer() {
        return whitePlayer;
    }
    
    public void setWhitePlayer(String whitePlayer) {
        this.whitePlayer = whitePlayer;
    }
    
    public String getBlackPlayer() {
        return blackPlayer;
    }
    
    public void setBlackPlayer(String blackPlayer) {
        this.blackPlayer = blackPlayer;
    }
    
    public String getWhiteElo() {
        return whiteElo;
    }
    
    public void setWhiteElo(String whiteElo) {
        this.whiteElo = whiteElo;
    }
    
    public String getBlackElo() {
        return blackElo;
    }
    
    public void setBlackElo(String blackElo) {
        this.blackElo = blackElo;
    }
    
    public String getGameResult() {
        return gameResult;
    }
    
    public void setGameResult(String gameResult) {
        this.gameResult = gameResult;
    }
    
    public String getTimeControl() {
        return timeControl;
    }
    
    public void setTimeControl(String timeControl) {
        this.timeControl = timeControl;
    }
}