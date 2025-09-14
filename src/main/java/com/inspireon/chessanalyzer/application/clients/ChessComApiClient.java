package com.inspireon.chessanalyzer.application.clients;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspireon.chessanalyzer.web.dtos.PlayerProfile;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.inspireon.chessanalyzer.AppConfig;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ChessComApiClient {
  
  @Autowired
  private AppConfig appConfig;
  
  private ExecutorService executor;
  private ObjectMapper objectMapper;
  
  @PostConstruct
  public void init() {
    executor = Executors.newFixedThreadPool(appConfig.getChesscomRequestsLimit());
    objectMapper = new ObjectMapper();
  }
  
  private BufferedInputStream requestApi(String fullUrl) {
	BufferedInputStream in = null;
	if (fullUrl != null) {
	  log.info("Calling chess.com API: {}", fullUrl);
	  
	  try {
		in = new BufferedInputStream(new URL(fullUrl).openStream());
		log.debug("Opened InputStream to {}", fullUrl);
	  } catch (IOException e) {
		log.error("Failed to open stream to {}: {}", fullUrl, e.toString(), e);
	  }
	}
	return in;
  }
  
  public BufferedInputStream getPgnAsInputStream(String playerUserName, LocalDate localDate) throws MalformedURLException, IOException{
    
    String fullUrl = "https://api.chess.com/pub/player/" + playerUserName + "/games/" + localDate.getYear() + "/" + localDate.getMonthValue() + "/pgn";
    Future<BufferedInputStream> future = executor.submit(() -> requestApi(fullUrl));

    // get results
    long timeout = appConfig.getChesscomResponseTimeout();
    log.info("Waiting up to {} ms for Chess.com response ({} {}/{})", timeout, playerUserName, localDate.getYear(), localDate.getMonthValue());
    
    BufferedInputStream bufferedInputStream = null;
	try {
		bufferedInputStream = future.get(timeout, TimeUnit.MILLISECONDS);
		if (bufferedInputStream == null) {
		  log.warn("Chess.com response stream is null for {} {}/{}", playerUserName, localDate.getYear(), localDate.getMonthValue());
		} else {
		  log.info("Received Chess.com PGN stream for {} {}/{}", playerUserName, localDate.getYear(), localDate.getMonthValue());
		}
	} catch (InterruptedException | ExecutionException | TimeoutException e) {
		log.error("Timed out or failed getting PGN stream for {} {}/{}: {}", playerUserName, localDate.getYear(), localDate.getMonthValue(), e.toString(), e);
	}
    return bufferedInputStream;
  }

  public PlayerProfile getPlayerProfile(String playerUsername) {
    String fullUrl = "https://api.chess.com/pub/player/" + playerUsername;
    Future<PlayerProfile> future = executor.submit(() -> requestPlayerProfile(fullUrl));

    long timeout = appConfig.getChesscomResponseTimeout();
    log.info("Fetching player profile for {} with timeout {} ms", playerUsername, timeout);
    
    try {
      PlayerProfile profile = future.get(timeout, TimeUnit.MILLISECONDS);
      if (profile != null) {
        log.info("Received player profile for {}: avatar={}", playerUsername, profile.getAvatar());
      }
      return profile;
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      log.error("Failed to get player profile for {}: {}", playerUsername, e.toString(), e);
      return null;
    }
  }

  private PlayerProfile requestPlayerProfile(String fullUrl) {
    try {
      log.info("Calling chess.com player profile API: {}", fullUrl);
      BufferedInputStream in = new BufferedInputStream(new URL(fullUrl).openStream());
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      StringBuilder response = new StringBuilder();
      String line;
      
      while ((line = reader.readLine()) != null) {
        response.append(line);
      }
      
      PlayerProfile profile = objectMapper.readValue(response.toString(), PlayerProfile.class);
      log.info("Successfully parsed player profile for: {}", fullUrl);
      return profile;
      
    } catch (IOException e) {
      log.error("Failed to fetch player profile from {}: {}", fullUrl, e.toString(), e);
      return null;
    }
  }
  
}
