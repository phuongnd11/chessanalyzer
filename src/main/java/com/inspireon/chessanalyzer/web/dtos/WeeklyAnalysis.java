package com.inspireon.chessanalyzer.web.dtos;

import java.time.LocalDate;
import java.util.List;

public class WeeklyAnalysis {
    
    private String playerUsername;
    private String avatar;
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private int totalGamesAnalyzed;
    private List<GameAnalysis> gameAnalyses;
    
    public WeeklyAnalysis(String playerUsername, String avatar, LocalDate weekStart, LocalDate weekEnd, int totalGamesAnalyzed, List<GameAnalysis> gameAnalyses) {
        this.playerUsername = playerUsername;
        this.avatar = avatar;
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        this.totalGamesAnalyzed = totalGamesAnalyzed;
        this.gameAnalyses = gameAnalyses;
    }
    
    public String getPlayerUsername() {
        return playerUsername;
    }
    
    public void setPlayerUsername(String playerUsername) {
        this.playerUsername = playerUsername;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public LocalDate getWeekStart() {
        return weekStart;
    }
    
    public void setWeekStart(LocalDate weekStart) {
        this.weekStart = weekStart;
    }
    
    public LocalDate getWeekEnd() {
        return weekEnd;
    }
    
    public void setWeekEnd(LocalDate weekEnd) {
        this.weekEnd = weekEnd;
    }
    
    public int getTotalGamesAnalyzed() {
        return totalGamesAnalyzed;
    }
    
    public void setTotalGamesAnalyzed(int totalGamesAnalyzed) {
        this.totalGamesAnalyzed = totalGamesAnalyzed;
    }
    
    public List<GameAnalysis> getGameAnalyses() {
        return gameAnalyses;
    }
    
    public void setGameAnalyses(List<GameAnalysis> gameAnalyses) {
        this.gameAnalyses = gameAnalyses;
    }
}