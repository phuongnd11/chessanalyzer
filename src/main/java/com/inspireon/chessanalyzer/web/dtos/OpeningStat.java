package com.inspireon.chessanalyzer.web.dtos;

import com.github.bhlangonijr.chesslib.PieceType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import lombok.Data;

@Data
public class OpeningStat implements Comparable<OpeningStat> {
  public enum Perspective {
    AS_WHITE,
    AS_BLACK,
    EITHER
  }
  
  private String name;
  
  private Integer whiteWon;
  
  private Integer whiteDraw;
  
  private Integer totalWhite;
  
  private Integer blackWon;
  
  private Integer totalBlack;
  
  private Integer blackDraw;
  
  private Integer won;
  
  private Integer totalGames;
  
  private Integer draw;

  private List<String> gameIds;

  private Map<PieceType, Long> whitePieceMoveCount;

  private Map<PieceType, Long> blackPieceMoveCount;

  private Map<PieceType, Long> overallPieceMoveCount;
  

  public OpeningStat(String name, Integer won, Integer lost, Integer draw, List<String> gameIds) {
    super();
    this.name = name;
    this.won = won;
    this.totalGames = lost;
    this.draw = draw;
    this.gameIds = gameIds;
    if (this.gameIds == null) {
      this.gameIds = new ArrayList<String>();
    }
    this.whiteWon = 0;
    this.whiteDraw = 0;
    this.totalWhite = 0;
    this.blackWon = 0;
    this.blackDraw = 0;
    this.totalBlack = 0;
    this.whitePieceMoveCount = initPieceMoveCount();
    this.blackPieceMoveCount = initPieceMoveCount();
    this.overallPieceMoveCount = initPieceMoveCount();
  }

  private static Map<PieceType, Long> initPieceMoveCount() {
    Map<PieceType, Long> pieceMoveCount = new HashMap<>();
    for (PieceType piece : PieceType.values()) {
      if (piece == PieceType.NONE) {
        continue;
      }
      pieceMoveCount.put(piece, 0L);
    }
    return pieceMoveCount;
  }

  public Integer getWinRate(Perspective perspective) {
    switch (perspective) {
      case AS_WHITE:
        return getWinRateAsWhite();
      case AS_BLACK:
        return getWinRateAsBlack();
      default:
        return getWinRate();
    }
  }
  
  public Integer getWinRate() {
    if (totalGames == 0) return -1;
    return Math.round(won * 100 / totalGames);
  }

  /**
   * Whether this opening has higher winrate in a certain perspective compared to another opening.
   *
   * @return true if this opening has strictly higher winrate in {@code perspective} compared to the
   * {@code other} opening. Returns {@code true} if {@code other} is {@code null}.
   */
  public boolean hasHigherWinRate(OpeningStat other, Perspective perspective) {
    if (other == null) {
      return true;
    }
    return getWinRate(perspective) > other.getWinRate(perspective);
  }

  /**
   * Whether this opening has lower winrate in a certain perspective compared to another opening.
   *
   * @return true if this opening has strictly lower winrate in {@code perspective} compared to the
   * {@code other} opening. Returns {@code true} if {@code other} is {@code null}.
   */
  public boolean hasLowerWinRate(OpeningStat other, Perspective perspective) {
    if (other == null) {
      return true;
    }
    return getWinRate(perspective) < other.getWinRate(perspective);
  }
  
  public Integer getWinRateAsWhite() {
    if (totalWhite == 0) return -1;
    return Math.round(whiteWon * 100 / totalWhite);
  }
  
  public Integer getWinRateAsBlack() {
    if (totalBlack == 0) return -1;
    return Math.round(blackWon * 100 / totalBlack);
  }

  public void addPieceMoveCounts(
      Map<PieceType, Long> pieceMoveCount, Perspective perspective) {
    if (perspective == Perspective.EITHER) {
      throw new IllegalArgumentException(
          "Adding piece move statistic must specify AS_BLACK or AS_WHITE specifically.");
    }

    for (Map.Entry<PieceType, Long> entry : pieceMoveCount.entrySet()) {
      PieceType piece = entry.getKey();
      if (perspective == Perspective.AS_WHITE) {
        whitePieceMoveCount.put(piece, whitePieceMoveCount.get(piece) + entry.getValue());
      } else if (perspective == Perspective.AS_BLACK) {
        blackPieceMoveCount.put(piece, blackPieceMoveCount.get(piece) + entry.getValue());
      }
      overallPieceMoveCount.put(piece, overallPieceMoveCount.get(piece) + entry.getValue());
    }
  }

  public Map<PieceType, Long> getPieceMoveCount(Perspective perspective) {
    switch (perspective) {
      case AS_WHITE:
        return whitePieceMoveCount;
      case AS_BLACK:
        return blackPieceMoveCount;
      default:
        return overallPieceMoveCount;
    }
  }
  
  public void addOneWin(boolean isWhite) {
    if (isWhite) {
      whiteWon++;
      totalWhite++;
    } else {
      blackWon++;
      totalBlack++;
    }
    won++;
    totalGames++;
  }
  
  public void addOneDraw(boolean isWhite) {
    if (isWhite) {
      whiteDraw++;
      totalWhite++;
    } else {
      blackDraw++;
      totalBlack++;
    }
    draw++;
    totalGames++;
  }
  
  public void addTotalGames(boolean isWhite) {
    if (isWhite) {
      totalWhite++;
    } else {
      totalBlack++;
    }    
    totalGames++;
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getWon() {
    return won;
  }

  public void setWon(Integer won) {
    this.won = won;
  }

  public Integer getTotalGames() {
    return totalGames;
  }

  public Integer getTotalGames(Perspective perspective) {
    switch (perspective) {
      case AS_WHITE:
        return totalWhite;
      case AS_BLACK:
        return totalBlack;
      default:
        return totalGames;
    }
  }

  /**
   * Whether this opening has been played in a certain perspective more than the other opening.
   *
   * @return true if this opening has strictly more games in {@code perspective} compared to the
   * {@code other} opening. Returns {@code true} if {@code other} is {@code null}.
   */
  public boolean hasMoreGames(OpeningStat other, Perspective perspective) {
    if (other == null) {
      return true;
    }
    return getTotalGames(perspective) > other.getTotalGames(perspective);
  }

  public void setTotalGames(Integer totalGames) {
    this.totalGames = totalGames;
  }

  public Integer getDraw() {
    return draw;
  }

  public void setDraw(Integer draw) {
    this.draw = draw;
  }

  public List<String> getGameIds() {
    return gameIds;
  }

  public void setGameIds(List<String> gameIds) {
    this.gameIds = gameIds;
  }


  public Integer getWhiteWon() {
    return whiteWon;
  }

  public void setWhiteWon(Integer whiteWon) {
    this.whiteWon = whiteWon;
  }

  public Integer getWhiteDraw() {
    return whiteDraw;
  }

  public void setWhiteDraw(Integer whiteDraw) {
    this.whiteDraw = whiteDraw;
  }

  public Integer getTotalWhite() {
    return totalWhite;
  }

  public void setTotalWhite(Integer totalWhite) {
    this.totalWhite = totalWhite;
  }

  public Integer getBlackWon() {
    return blackWon;
  }

  public void setBlackWon(Integer blackWon) {
    this.blackWon = blackWon;
  }

  public Integer getTotalBlack() {
    return totalBlack;
  }

  public void setTotalBlack(Integer totalBlack) {
    this.totalBlack = totalBlack;
  }

  public Integer getBlackDraw() {
    return blackDraw;
  }

  public void setBlackDraw(Integer blackDraw) {
    this.blackDraw = blackDraw;
  }

  @Override
  public int compareTo(OpeningStat o) {
    return this.getName().compareTo(o.getName());
  }
  
  
  
}
