package com.inspireon.chessanalyzer.web.dtos;

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
  
}
