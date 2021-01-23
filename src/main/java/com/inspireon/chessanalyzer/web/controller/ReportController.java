package com.inspireon.chessanalyzer.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inspireon.chessanalyzer.application.service.ReportService;
import com.inspireon.chessanalyzer.web.dtos.PlayerOverview;


@CrossOrigin(origins = { "http://localhost:3000"})
@RestController
public class ReportController {
  @Autowired
  private ReportService reportService;
  
  @RequestMapping("/report/overview")
  public PlayerOverview getPlayerOverview(@RequestParam(value="playerUsername") String playerUsername) throws Exception {
    return reportService.getPlayerOverview(playerUsername);
  }
}