package com.inspireon.chessanalyzer.model;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChessOpening {
  
  private String name;
  
  private List<String> moves_lalg;
  
  private String last_pos;

  public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
    
  }
  
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
