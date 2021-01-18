package com.inspireon.chessanalyzer.common.enums;

public enum ChessSite {
    CHESS_COM("chess.com"),
    LICHESS("lichess.com");

    private String name;

    ChessSite(String name) {
      this.setName(name);
    }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
