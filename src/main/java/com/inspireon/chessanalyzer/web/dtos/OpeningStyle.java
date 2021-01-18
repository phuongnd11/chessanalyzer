package com.inspireon.chessanalyzer.web.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpeningStyle {
  private boolean preferE4;
  private boolean preferD4;
  private boolean preferSemiAsBlack;
  private boolean preferSemiOpenAsBlack;
  private boolean preferSemiClosedAsBlack;
  private String favoriteOpeningAsWhite;
  private String favoriteOpeningAsBlack;
  private String favoriteOpeningAsBlackAgainstE4;
  private String favoriteOpeningAsBlackAgainstD4;
  private String strongestOpeningAsWhite;
  private String strongestOpeningAsBlack;
  private String weakestOpeningAsWhite;
  private String weakestOpeningAsBlack;
}
