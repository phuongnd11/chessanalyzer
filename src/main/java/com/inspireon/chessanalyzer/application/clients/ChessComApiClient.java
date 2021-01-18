package com.inspireon.chessanalyzer.application.clients;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.stereotype.Component;

@Component
public class ChessComApiClient {
  public BufferedInputStream getPgnAsInputStream(String playerUserName, int year, int month) throws MalformedURLException, IOException{
    
    String fullUrl = "https://api.chess.com/pub/player/" + playerUserName + "/games/" + year + "/" + month + "/pgn";
    System.out.println("Calling chess.com api: " + fullUrl);
      BufferedInputStream in = new BufferedInputStream(new URL(fullUrl).openStream());
      return in;
  }

}
