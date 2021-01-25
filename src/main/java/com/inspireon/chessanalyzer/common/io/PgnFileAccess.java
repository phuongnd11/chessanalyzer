package com.inspireon.chessanalyzer.common.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.inspireon.chessanalyzer.AppConfig;

@Component
public class PgnFileAccess {
  @Autowired
  private AppConfig appConfig;
  
  public void writePgnFile(BufferedInputStream in, String playerUserName, int month) throws MalformedURLException, IOException {  
    String pgnFilePath = getPgnFilePath(playerUserName, month);
      File pgnFile = new File(pgnFilePath);
      System.out.println(pgnFile.getAbsolutePath());
      if (!pgnFile.exists()) {
        pgnFile.createNewFile();
      }
    FileOutputStream fileOutputStream = new FileOutputStream(pgnFile); 
      byte dataBuffer[] = new byte[1024];
      int bytesRead;
      while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
          fileOutputStream.write(dataBuffer, 0, bytesRead);
      }
  }
  
  public String getPgnFilePath(String playerUserName, int month) throws IOException {
    return new ClassPathResource(appConfig.getGameBaseFolder()).getFile().getAbsolutePath() + File.separator + playerUserName + "_" + month + ".pgn";
  }

}
