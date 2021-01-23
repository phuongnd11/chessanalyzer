package com.inspireon.chessanalyzer.application.clients;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.inspireon.chessanalyzer.AppConfig;

@Component
public class ChessComApiClient {
  private BlockingQueue<ApiRequestInfo> queue;
  private Map<String, ApiRequestInfo> requestMap = new HashMap<>();
  
  @Autowired
  private AppConfig appConfig;
  
  @PostConstruct
  public void init() {
	  queue = new ArrayBlockingQueue<ApiRequestInfo>(appConfig.getChesscomQueueSize());
	  
	  Runnable runnable = () -> {
		  while(true) {
			  try {
				ApiRequestInfo apiRequestInfo = queue.take();
				if (apiRequestInfo != null) {
					String fullUrl = apiRequestInfo.getUrl();
					System.out.println("Calling chess.com api: " + fullUrl);
					BufferedInputStream in = new BufferedInputStream(new URL(fullUrl).openStream());
					apiRequestInfo.setInputStream(in);
					apiRequestInfo.setDone(true);
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		  }
      };
      
      ExecutorService executor = Executors.newFixedThreadPool(appConfig.getChesscomRequestsLimit());
      executor.execute(runnable);
  }
  
  public BufferedInputStream getPgnAsInputStream(String playerUserName, int year, int month) throws MalformedURLException, IOException{
    
    String fullUrl = "https://api.chess.com/pub/player/" + playerUserName + "/games/" + year + "/" + month + "/pgn";
    String threadId = String.valueOf(Thread.currentThread().getId());
    ApiRequestInfo apiRequestInfo = ApiRequestInfo.builder()
    		.threadId(threadId)
    		.url(fullUrl)
    		.build();
    try {
		queue.offer(apiRequestInfo, appConfig.getChesscomQueueInputTimeout(), TimeUnit.MILLISECONDS);
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
    requestMap.put(threadId, apiRequestInfo);
    
    // get results
    long timeout = appConfig.getChesscomResponseTimeout();
    long currentTime = System.currentTimeMillis();
    
    while (!apiRequestInfo.isDone() && System.currentTimeMillis() <= currentTime + timeout) {};
    BufferedInputStream bufferedInputStream = (BufferedInputStream) apiRequestInfo.getInputStream();
    requestMap.remove(threadId);
    return bufferedInputStream;
  }

  
}
