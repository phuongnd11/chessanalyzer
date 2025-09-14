package com.inspireon.chessanalyzer.common.io;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspireon.chessanalyzer.AppConfig;
import com.inspireon.chessanalyzer.domain.model.ChessTempoResult;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OpeningFileAccess {
  @Autowired
  private AppConfig appConfig;
  
  public ChessTempoResult getOpenings() {
    String jsonString;
    try {
      String absoluteOpeningPath = new ClassPathResource(appConfig.getOpeningBookPath()).getFile().getAbsolutePath();
      jsonString = Files.readString(Path.of(absoluteOpeningPath));
      log.info("Loaded Opening book from {}", absoluteOpeningPath);
      ObjectMapper mapper = new ObjectMapper();

      ChessTempoResult chessTempoResult = mapper.readValue(jsonString, ChessTempoResult.class);
      return chessTempoResult;
    } catch (Exception e) {
      log.error("Failed to read opening book: {}", e.toString(), e);
    }
    return null;
  }

}
