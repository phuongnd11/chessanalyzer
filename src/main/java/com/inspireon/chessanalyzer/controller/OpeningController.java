package com.inspireon.chessanalyzer.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspireon.chessanalyzer.dtos.OpeningStat;
import com.inspireon.chessanalyzer.dtos.UserMistake;
import com.inspireon.chessanalyzer.model.ChessOpening;
import com.inspireon.chessanalyzer.model.ChessTempoResult;
import com.inspireon.chessanalyzer.service.OpeningAnalyzerService;


@CrossOrigin(origins = { "http://localhost:3000"})
@RestController
public class OpeningController {
  
  @Autowired
  private OpeningAnalyzerService openingAnalyzerService;
  
  @RequestMapping("/opening/mistakes")
  public List <UserMistake> getOpeningMistakes(@RequestParam String playerUsername, @RequestParam String openingName) throws Exception {
    
    return openingAnalyzerService.getOpeningMistakes(playerUsername, openingName);
  }
  
  @RequestMapping("/opening")
  public TreeSet <OpeningStat> getOpenings(@RequestParam String playerUsername) throws Exception {
    return openingAnalyzerService.getOpenings(playerUsername);
  }
  
  public static void main(String[] args) throws IOException {
    String jsonString = Files.readString(Path.of(new ClassPathResource("openingbook/response.json").getFile().getAbsolutePath()));
    ObjectMapper mapper = new ObjectMapper();
    ChessTempoResult chessTempoResult = mapper.readValue(jsonString, ChessTempoResult.class);
    Map<String, ChessOpening> openings = new HashMap<String, ChessOpening>();
    for (ChessOpening chessOpening : chessTempoResult.getOpenings()) {
      openings.put(chessOpening.getName().split(":")[0], chessOpening);
    }
    int i = 0;
    for (Entry<String, ChessOpening> entr : openings.entrySet()) {
      System.out.println(++i + entr.getKey());
    }
  }
}
