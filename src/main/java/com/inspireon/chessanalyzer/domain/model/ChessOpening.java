package com.inspireon.chessanalyzer.domain.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChessOpening {
  
  private String name;
  
  private List<String> moves_lalg;
  
  private String last_pos;
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getMoves_lalg() {
    return moves_lalg;
  }

  public void setMoves_lalg(List<String> moves_lalg) {
    this.moves_lalg = moves_lalg;
  }

  public String getLast_pos() {
    return last_pos;
  }

  public void setLast_pos(String last_pos) {
    this.last_pos = last_pos;
  }
  
}
