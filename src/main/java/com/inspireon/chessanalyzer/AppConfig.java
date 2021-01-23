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
  
  @Value("${chessanalyzer.chesscom.queue.size}")
  private int chesscomQueueSize;
  
  @Value("${chessanalyzer.chesscom.queue.put.timeout}")
  private long chesscomQueueInputTimeout;
  
  @Value("${chessanalyzer.chesscom.requests.limit}")
  private int chesscomRequestsLimit;
  
  @Value("${chessanalyzer.chesscom.response.timeout}")
  private long chesscomResponseTimeout;
}
