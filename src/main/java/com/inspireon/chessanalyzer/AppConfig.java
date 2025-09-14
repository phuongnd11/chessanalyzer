package com.inspireon.chessanalyzer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Getter
@Configuration
public class AppConfig {
	
  @Value("${chessanalyzer.openingbook.path}")
  private String openingBookPath;
  
  @Value("${chessanalyzer.gamebase.folder}")
  private String gameBaseFolder;
  
  @Value("${chessanalyzer.chesscom.requests.limit:10}")
  private int chesscomRequestsLimit;
  
  @Value("${chessanalyzer.chesscom.response.timeout:10000}")
  private long chesscomResponseTimeout;
  
  @Value("${chessanalyzer.chesscom.months.limit:24}")
  private int chesscomNumOfMonthsLimit;
  
  @Value("${chessanalyzer.chesscom.games.limit:1000}")
  private long chesscomNumOfGamesLimit;

  @Value("${chessanalyzer.stockfish.path:}")
  private String stockfishPath;

  @Value("${chessanalyzer.engine.type:stockfish}")
  private String engineType;

  @Value("${chessanalyzer.chessapi.url:https://chess-api.com/v1}")
  private String chessApiUrl;

  @Value("${chessanalyzer.chessapi.default.depth:18}")
  private int chessApiDefaultDepth;

  @Value("${chessanalyzer.chessapi.default.variants:3}")
  private int chessApiDefaultVariants;

  @Value("${chessanalyzer.chessapi.default.thinking.time:50}")
  private int chessApiDefaultThinkingTime;
}
