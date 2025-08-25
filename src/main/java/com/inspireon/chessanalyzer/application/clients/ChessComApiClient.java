package com.inspireon.chessanalyzer.application.clients;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

<<<<<<< Upstream, based on choose_remote_name/master
import javax.annotation.PostConstruct;
=======
import jakarta.annotation.PostConstruct;
>>>>>>> 1ef77b2 Fix build errors

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.inspireon.chessanalyzer.AppConfig;

@Component
public class ChessComApiClient {
  
  @Autowired
  private AppConfig appConfig;
  
  private ExecutorService executor;
  
  @PostConstruct
  public void init() {
    executor = Executors.newFixedThreadPool(appConfig.getChesscomRequestsLimit());
  }
  
  private BufferedInputStream requestApi(String fullUrl) {
	BufferedInputStream in = null;
	if (fullUrl != null) {
	  System.out.println("Calling chess.com api: " + fullUrl);
	  
	  try {
		in = new BufferedInputStream(new URL(fullUrl).openStream());
	  } catch (IOException e) {
		e.printStackTrace();
	  }
	}
	return in;
  }
  
  public BufferedInputStream getPgnAsInputStream(String playerUserName, LocalDate localDate) throws MalformedURLException, IOException{
    
    String fullUrl = "https://api.chess.com/pub/player/" + playerUserName + "/games/" + localDate.getYear() + "/" + localDate.getMonthValue() + "/pgn";
    Future<BufferedInputStream> future = executor.submit(() -> requestApi(fullUrl));

    // get results
    long timeout = appConfig.getChesscomResponseTimeout();
    
    BufferedInputStream bufferedInputStream = null;
	try {
		bufferedInputStream = future.get(timeout, TimeUnit.MILLISECONDS);
	} catch (InterruptedException | ExecutionException | TimeoutException e) {
		e.printStackTrace();
	}
    return bufferedInputStream;
  }

  
}
