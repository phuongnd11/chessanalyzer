package com.inspireon.chessanalyzer.io;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspireon.chessanalyzer.AppConfig;
import com.inspireon.chessanalyzer.model.ChessOpening;
import com.inspireon.chessanalyzer.model.ChessTempoResult;

@Component
public class OpeningFileAccess {
  @Autowired
  private AppConfig appConfig;
  
  public ChessTempoResult getOpenings() {
    String jsonString;
    try {
      String absoluteOpeningPath = new ClassPathResource(appConfig.getOpeningBookPath()).getFile().getAbsolutePath();
      jsonString = Files.readString(Path.of(absoluteOpeningPath));
      System.out.println("Loaded Opening book from " + absoluteOpeningPath);
      ObjectMapper mapper = new ObjectMapper();

      ChessTempoResult chessTempoResult = mapper.readValue(jsonString, ChessTempoResult.class);
      return chessTempoResult;
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

}
