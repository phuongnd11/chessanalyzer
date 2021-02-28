package com.inspireon.chessanalyzer.web.dtos;

import java.util.Map;

import com.inspireon.chessanalyzer.common.enums.Castle;
import com.inspireon.chessanalyzer.common.enums.Opponent;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PlayerOverview {
  
  private String title;
  
  private String style;
  
  private String trainingSuggestion;
  
  private String timeManagement;
  
  private String emotional;
  
  private Integer numberOfBackwardMoves;
  
  private Integer gamesAnalyzed;
  
  private Map<Opponent, WinRateStat> winRateByOpponentRating;
  
  private Map<Castle, WinRateStat> winRateByCastle;
}
