package com.inspireon.chessanalyzer.domain.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChessTempoResult {
  private int total_openings;
  
  private List<ChessOpening> openings;

  public int getTotal_openings() {
    return total_openings;
  }

  public void setTotal_openings(int total_openings) {
    this.total_openings = total_openings;
  }

  public List<ChessOpening> getOpenings() {
    return openings;
  }

  public void setOpenings(List<ChessOpening> openings) {
    this.openings = openings;
  }
  
  
}
