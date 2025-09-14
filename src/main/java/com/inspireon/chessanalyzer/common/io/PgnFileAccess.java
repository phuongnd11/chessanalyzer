package com.inspireon.chessanalyzer.common.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.inspireon.chessanalyzer.AppConfig;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PgnFileAccess {
  @Autowired
  private AppConfig appConfig;
  
  public void writePgnFile(BufferedInputStream in, String playerUserName, LocalDate localDate) throws MalformedURLException, IOException {  
    String pgnFilePath = getPgnFilePath(playerUserName, localDate);
      File pgnFile = new File(pgnFilePath);
      log.info("Writing PGN to {}", pgnFile.getAbsolutePath());
      if (!pgnFile.exists()) {
  		pgnFile.getParentFile().mkdirs();
        boolean created = pgnFile.createNewFile();
        log.debug("Created new PGN file: {} -> {}", pgnFile.getAbsolutePath(), created);
      }
    long total = 0L;
    FileOutputStream fileOutputStream = null;
    try {
      fileOutputStream = new FileOutputStream(pgnFile);
      byte dataBuffer[] = new byte[4096];
      int bytesRead;
      while ((bytesRead = in.read(dataBuffer, 0, dataBuffer.length)) != -1) {
          fileOutputStream.write(dataBuffer, 0, bytesRead);
          total += bytesRead;
      }
      log.info("Wrote {} bytes to {}", total, pgnFile.getAbsolutePath());
    } finally {
      try { if (fileOutputStream != null) fileOutputStream.close(); } catch (IOException ignore) {}
      try { if (in != null) in.close(); } catch (IOException ignore) {}
    }
  }
  
  public String getPgnFilePath(String playerUserName, LocalDate localDate) throws IOException {
    return new ClassPathResource(appConfig.getGameBaseFolder()).getFile().getAbsolutePath() 
  		+ File.separator + playerUserName 
  		+ File.separator + playerUserName 
  		+ "_" + localDate.getYear() + "_" + localDate.getMonthValue() + ".pgn";
  }

}
