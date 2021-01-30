package com.inspireon.chessanalyzer.web.controller;

import java.time.DayOfWeek;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inspireon.chessanalyzer.application.service.ReportService;
import com.inspireon.chessanalyzer.common.enums.ChessSite;
import com.inspireon.chessanalyzer.domain.cache.PlayerStatCache;
import com.inspireon.chessanalyzer.domain.datamanager.OpeningIndexer;
import com.inspireon.chessanalyzer.web.dtos.PlayerOverview;
import com.inspireon.chessanalyzer.web.dtos.WinRateStat;


@CrossOrigin(origins = { "http://localhost:3000"})
@RestController
public class ReportController {
  @Autowired
  private ReportService reportService;
  
  @Autowired
  private PlayerStatCache playerStatCache;
  
  @Autowired
  private OpeningIndexer openingIndexer;
  
  @RequestMapping("/report/overview")
  public PlayerOverview getPlayerOverview(@RequestParam(value="playerUsername") String playerUsername) throws Exception {
    return reportService.getPlayerOverview(playerUsername);
  }
  
  @RequestMapping("/report/winratebyday")
  public Map<DayOfWeek, WinRateStat> getWinRateByDayStat(@RequestParam String playerUsername) throws Exception {
    if (playerStatCache.getDayOfWeekStats().get(playerUsername + "-" + ChessSite.CHESS_COM.getName()) != null) {
      return playerStatCache.getDayOfWeekStats().get(playerUsername + "-" + ChessSite.CHESS_COM.getName());
    }
    else {
        openingIndexer.indexOpening(playerUsername);
      return playerStatCache.getDayOfWeekStats().get(playerUsername + "-" + ChessSite.CHESS_COM.getName());
    }
  }
}
