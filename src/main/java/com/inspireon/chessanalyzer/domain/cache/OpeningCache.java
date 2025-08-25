package com.inspireon.chessanalyzer.domain.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

<<<<<<< Upstream, based on choose_remote_name/master
import javax.annotation.PostConstruct;
=======
import jakarta.annotation.PostConstruct;
>>>>>>> 1ef77b2 Fix build errors

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.inspireon.chessanalyzer.AppConfig;
import com.inspireon.chessanalyzer.common.io.OpeningFileAccess;
import com.inspireon.chessanalyzer.domain.model.ChessOpening;
import com.inspireon.chessanalyzer.domain.model.ChessTempoResult;

import lombok.Getter;

@Getter
@Component
public class OpeningCache {

  @Autowired
  private AppConfig appConfig;
  
  @Autowired
  private OpeningFileAccess openingFileAccess;
  
  private List<ChessOpening> openings;
  
  private Map<String, ChessOpening> openingMap;

  @PostConstruct
  public void init() {
    ChessTempoResult chessTempoResult = openingFileAccess.getOpenings();
    openings = chessTempoResult.getOpenings();
    openingMap = new HashMap<String, ChessOpening>();
    for (ChessOpening opening : openings) {
      openingMap.put(opening.getName(), opening);
    }
  }
}