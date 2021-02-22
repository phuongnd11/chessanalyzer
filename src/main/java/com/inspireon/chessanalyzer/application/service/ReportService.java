package com.inspireon.chessanalyzer.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.inspireon.chessanalyzer.common.enums.ChessSite;
import com.inspireon.chessanalyzer.domain.cache.PlayerStatCache;
import com.inspireon.chessanalyzer.web.dtos.OpeningStyle;
import com.inspireon.chessanalyzer.web.dtos.PlayerOverview;
import com.inspireon.chessanalyzer.web.dtos.PlayerOverview.PlayerOverviewBuilder;

@Service
public class ReportService {
  
  @Autowired
  private StyleAnalyzerService styleAnalyzerService;
  
  @Autowired
  private PlayerStatCache playerStatCache;
  
  public PlayerOverview getPlayerOverview(String playerUsername) throws Exception {
    PlayerOverviewBuilder builder = PlayerOverview.builder();
    OpeningStyle openingStyle = styleAnalyzerService.analyzeOpeningStyle(playerUsername);
    String style = styleAnalyzerService.describeOpeningStyle(openingStyle);
    
    return builder.style(style)
      .numberOfBackwardMoves(playerStatCache.getBackwardMoves().get(playerUsername + "-" + ChessSite.CHESS_COM.getName()))
      .gamesAnalyzed(playerStatCache.getGamesAnalyzed().get(playerUsername + "-" + ChessSite.CHESS_COM.getName()))
      .build();
  }
}
