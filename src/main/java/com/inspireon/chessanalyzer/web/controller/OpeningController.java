package com.inspireon.chessanalyzer.web.controller;

import java.util.List;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inspireon.chessanalyzer.application.service.OpeningAnalyzerService;
import com.inspireon.chessanalyzer.web.dtos.OpeningStat;
import com.inspireon.chessanalyzer.web.dtos.UserMistake;


@CrossOrigin(origins = { "http://localhost:3000"})
@RestController
public class OpeningController {
  
  @Autowired
  private OpeningAnalyzerService openingAnalyzerService;
  
  public List<UserMistake> getOpeningMistakes(
		    @RequestParam("playerUsername") String playerUsername,
		    @RequestParam("openingName") String openingName) throws Exception
 {
    
    return openingAnalyzerService.getOpeningMistakes(playerUsername, openingName);
  }
  
  @RequestMapping("/opening")
  public TreeSet<OpeningStat> getOpenings(@RequestParam("playerUsername") String playerUsername) throws Exception
  {
    return openingAnalyzerService.getOpenings(playerUsername);
  }
}
