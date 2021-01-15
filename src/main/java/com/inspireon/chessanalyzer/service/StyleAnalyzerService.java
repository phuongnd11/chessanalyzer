package com.inspireon.chessanalyzer.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.inspireon.chessanalyzer.cache.OpeningCache;
import com.inspireon.chessanalyzer.datamanager.GameDataAccess;
import com.inspireon.chessanalyzer.dtos.OpeningStat;
import com.inspireon.chessanalyzer.dtos.OpeningStyle;
import com.inspireon.chessanalyzer.model.ChessOpening;

@Service
public class StyleAnalyzerService {
  
  @Autowired
  private GameDataAccess gameDataAccess;
  
  @Autowired
  private OpeningCache openingCache;
  
  public OpeningStyle analyzeOpeningStyle(String playerUsername) throws Exception {
    OpeningStyle openingStyle = new OpeningStyle();
    TreeSet<OpeningStat> openingStats = gameDataAccess.getOpenings(playerUsername);
    OpeningStat [] openingStatArray = new OpeningStat[openingStats.size()];
    openingStats.toArray(openingStatArray);
    
    Arrays.sort(openingStatArray, new Comparator<OpeningStat>() {
      @Override
      public int compare(OpeningStat o1, OpeningStat o2) {
        return o2.getTotalWhite().compareTo(o1.getTotalWhite());
      }      
    });
    
    ChessOpening mostPopularAsWhite = openingCache.getOpeningMap().get(openingStatArray[0].getName());
    
    OpeningStat mainWeaponAsWhite = null;
    
    OpeningStat mainWeaknessAsWhite = null;
    
    for (int i = 0; i < openingStatArray.length; i++) {
      if (mainWeaponAsWhite == null || openingStatArray[i].getWinRateAsWhite() > mainWeaponAsWhite.getWinRateAsWhite()) {
        if (openingStatArray[i].getTotalWhite() > 10)
          mainWeaponAsWhite = openingStatArray[i];
      }
      if (mainWeaknessAsWhite == null || openingStatArray[i].getWinRateAsWhite() < mainWeaknessAsWhite.getWinRateAsWhite()) {
        if (openingStatArray[i].getTotalWhite() > 10)
          mainWeaknessAsWhite = openingStatArray[i];
      }
    }
    
    Arrays.sort(openingStatArray, new Comparator<OpeningStat>() {
      @Override
      public int compare(OpeningStat o1, OpeningStat o2) {
        return o2.getTotalBlack().compareTo(o1.getTotalBlack());
      }      
    });
    
    OpeningStat mainWeaponAsBlack = null;
    
    OpeningStat mainWeaknessAsBlack = null;
    
    for (int i = 0; i < openingStatArray.length; i++) {
      if (mainWeaponAsBlack == null || openingStatArray[i].getWinRateAsBlack() > mainWeaponAsBlack.getWinRateAsBlack()) {
        if (openingStatArray[i].getTotalBlack() > 10) 
          mainWeaponAsBlack = openingStatArray[i];
      }
      if (mainWeaknessAsBlack == null || openingStatArray[i].getWinRateAsBlack() < mainWeaknessAsBlack.getWinRateAsBlack()) {
        if (openingStatArray[i].getTotalBlack() > 10) 
          mainWeaknessAsBlack = openingStatArray[i];
      }
    }
    
    ChessOpening mostPopularAgainstE4 = null;
    ChessOpening mostPopularAgainstD4 = null;
    
    for (int i = 0; i < openingStatArray.length; i++) {
      if (openingCache.getOpeningMap().get(openingStatArray[i].getName()).getMoves_lalg().get(0).equals("e2e4")) {
        mostPopularAgainstE4 = openingCache.getOpeningMap().get(openingStatArray[i].getName());
      } else if (openingCache.getOpeningMap().get(openingStatArray[i].getName()).getMoves_lalg().get(0).equals("d2d4")) {
        mostPopularAgainstD4 = openingCache.getOpeningMap().get(openingStatArray[i].getName());
      }
    }
    
    openingStyle.setFavoriteOpeningAsWhite(mostPopularAsWhite.getName());
    openingStyle.setFavoriteOpeningAsBlackAgainstE4(mostPopularAgainstE4.getName());
    openingStyle.setFavoriteOpeningAsBlackAgainstD4(mostPopularAgainstD4.getName());
    if (mainWeaponAsWhite != null)
      openingStyle.setStrongestOpeningAsWhite(mainWeaponAsWhite.getName());
    if (mainWeaknessAsWhite != null)
      openingStyle.setWeakestOpeningAsWhite(mainWeaknessAsWhite.getName());
    if (mainWeaponAsBlack != null)
      openingStyle.setStrongestOpeningAsBlack(mainWeaponAsBlack.getName());
    if (mainWeaknessAsBlack != null)
      openingStyle.setWeakestOpeningAsBlack(mainWeaknessAsBlack.getName());
    if (mostPopularAsWhite.getMoves_lalg().get(0).equals("e2e4")) {
      openingStyle.setPreferE4(true);
    } else if (mostPopularAsWhite.getMoves_lalg().get(0).equals("d2d4")) {
      openingStyle.setPreferD4(true);
    }
    
    boolean semi = false;
    if (!mostPopularAgainstE4.getMoves_lalg().get(1).equals("e7e5")) {
      openingStyle.setPreferSemiOpenAsBlack(true);
      semi = true;
    }
    if (!mostPopularAgainstD4.getMoves_lalg().get(1).equals("d7d5")) {
      openingStyle.setPreferSemiClosedAsBlack(true);
      semi = true;
    }
    openingStyle.setPreferSemiAsBlack(semi);
    return openingStyle;
  }
  
  public String describeOpeningStyle(OpeningStyle openingStyle) {
    StringBuilder style = new StringBuilder();
    if (openingStyle.isPreferE4()) {
      style.append("You like to get an open position out of the opening as White");
    } else if (openingStyle.isPreferD4()) {
      style.append("You like to start the game in a closed position as White");
    } else {
      style.append("You like to attack the center from the flank as White");
    }
    
    if (openingStyle.isPreferSemiOpenAsBlack()) {
      style.append(" and semi-open");
    }
    if (openingStyle.isPreferSemiClosedAsBlack()) {
      style.append(" and semi-closed");
    }
    if (openingStyle.isPreferSemiAsBlack()) {
      style.append(" position as Black");
    } else {
      style.append(" and symmetrical position as Black");
    }
    
    if (openingStyle.getStrongestOpeningAsWhite() != null) {
      style.append(". Your main weapon is " + openingStyle.getStrongestOpeningAsWhite());
    } 
    if (openingStyle.getStrongestOpeningAsBlack() != null) {
      if (style.toString().contains("Your main weapon is")) {
        style.append(" and " + openingStyle.getStrongestOpeningAsBlack());
      } else {
        style.append(". Your main weapon is " + openingStyle.getStrongestOpeningAsBlack());
      }
    }
    if (openingStyle.getStrongestOpeningAsWhite() != null || openingStyle.getStrongestOpeningAsBlack() != null) {
      style.append(".");
    }
    
    if (openingStyle.getWeakestOpeningAsWhite() != null) {
      style.append(" Your weakness lies in " + openingStyle.getWeakestOpeningAsWhite());
    } 
    if (openingStyle.getWeakestOpeningAsBlack() != null) {
      if (style.toString().contains("Your weakness lies in")) {
        style.append(" and " + openingStyle.getWeakestOpeningAsBlack());
      } else {
        style.append(". Your weakness lies in " + openingStyle.getWeakestOpeningAsBlack());
      }
    }
    if (openingStyle.getWeakestOpeningAsWhite() != null || openingStyle.getWeakestOpeningAsBlack() != null) {
      style.append(".");
    }
    
    return style.toString();
  }
}
