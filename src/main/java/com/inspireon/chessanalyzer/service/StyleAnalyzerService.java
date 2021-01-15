package com.inspireon.chessanalyzer.service;


import com.google.auto.value.AutoValue;
import com.inspireon.chessanalyzer.cache.OpeningCache;
import com.inspireon.chessanalyzer.datamanager.GameDataAccess;
import com.inspireon.chessanalyzer.dtos.OpeningStat;
import com.inspireon.chessanalyzer.dtos.OpeningStat.Perspective;
import com.inspireon.chessanalyzer.dtos.OpeningStyle;
import com.inspireon.chessanalyzer.model.ChessOpening;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.TreeSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StyleAnalyzerService {
  
  @Autowired
  private GameDataAccess gameDataAccess;
  
  @Autowired
  private OpeningCache openingCache;

  private KeyOpenings getKeyOpenings(Perspective perspective, TreeSet<OpeningStat> openingStats) {

    OpeningStat currentBest = null;
    OpeningStat currentWorst = null;
    OpeningStat currentMostGames = null;

    // Stat only for Perspective.AS_BLACK.
    OpeningStat currentMostVsE4 = null;
    OpeningStat currentMostVsD4 = null;

    for (OpeningStat thisOpening : openingStats) {
      if (thisOpening.hasMoreGames(currentMostGames, perspective)) {
        currentMostGames = thisOpening;
      }

      if (perspective == Perspective.AS_BLACK) {
        ChessOpening opening = getOpening(thisOpening);
        if (isE4(opening) && thisOpening.hasMoreGames(currentMostVsE4, Perspective.AS_BLACK)) {
          currentMostVsE4 = thisOpening;
        }
        if (isD4(opening) && thisOpening.hasMoreGames(currentMostVsD4, Perspective.AS_BLACK)) {
          currentMostVsD4 = thisOpening;
        }
      }

      // Skip openings that has not been played more than 10 times for further analysis.
      if (thisOpening.getTotalGames(perspective) <= 10) {
        continue;
      }

      if (thisOpening.hasHigherWinRate(currentBest, perspective)) {
        currentBest = thisOpening;
      }
      if (thisOpening.hasLowerWinRate(thisOpening, perspective)) {
        currentWorst = thisOpening;
      }
    }

    return KeyOpenings.newBuilder()
        .setWeapon(Optional.ofNullable(currentBest))
        .setWeakness(Optional.ofNullable(currentWorst))
        .setMostPlayed(Optional.ofNullable(currentMostGames))
        .setMostPlayedAgainstE4(Optional.ofNullable(currentMostVsE4))
        .setMostPlayedAgainstD4(Optional.ofNullable(currentMostVsD4))
        .build();
  }

  public OpeningStyle analyzeOpeningStyle(String playerUsername)
      throws Exception {
    OpeningStyle openingStyle = new OpeningStyle();
    TreeSet<OpeningStat> openingStats = gameDataAccess.getOpenings(playerUsername);
    OpeningStat [] openingStatArray = new OpeningStat[openingStats.size()];
    openingStats.toArray(openingStatArray);

    // General opening alalysis.
    KeyOpenings whiteKeyOpenings = getKeyOpenings(Perspective.AS_WHITE, openingStats);
    KeyOpenings blackKeyOpenings = getKeyOpenings(Perspective.AS_BLACK, openingStats);
    
    // Deeper opening analysis.
    ChessOpening mostPopularAgainstD4 = null;
    
    for (OpeningStat opening : openingStats) {
      if (openingCache.getOpeningMap().get(opening.getName()).getMoves_lalg().get(0).equals("d2d4")) {
        mostPopularAgainstD4 = openingCache.getOpeningMap().get(opening.getName());
      }
    }
    

    // Set opening styles as White.
    whiteKeyOpenings.getWeapon().ifPresent(
        weapon -> openingStyle.setStrongestOpeningAsWhite(weapon.getName()));
    whiteKeyOpenings.getWeakness().ifPresent(
        weakness -> openingStyle.setWeakestOpeningAsWhite(weakness.getName()));
    whiteKeyOpenings.getMostPlayed().ifPresent(
        mostPlayed -> {
          openingStyle.setFavoriteOpeningAsWhite(mostPlayed.getName());
          if (isD4(getOpening(mostPlayed))) {
            openingStyle.setPreferE4(true);
          } else if (isD4(getOpening(mostPlayed))) {
            openingStyle.setPreferD4(true);
          }
        });

    // Set opening styles as Black.
    blackKeyOpenings.getWeapon().ifPresent(
        weapon -> openingStyle.setStrongestOpeningAsBlack(weapon.getName()));
    blackKeyOpenings.getWeakness().ifPresent(
        weakness -> openingStyle.setWeakestOpeningAsBlack(weakness.getName()));
    blackKeyOpenings.getMostPlayedAgainstE4().ifPresent(
        mostVsE4 -> {
          openingStyle.setFavoriteOpeningAsBlackAgainstE4(mostVsE4.getName());
          if (!isSymetric(getOpening(mostVsE4))) {
            openingStyle.setPreferSemiOpenAsBlack(true);
            openingStyle.setPreferSemiAsBlack(true);
          }
        });
    blackKeyOpenings.getMostPlayedAgainstE4().ifPresent(
        mostVsD4 -> {
          openingStyle.setFavoriteOpeningAsBlackAgainstD4(mostVsD4.getName());
          if (!isSymetric(getOpening(mostVsD4))) {
            openingStyle.setPreferSemiClosedAsBlack(true);
            openingStyle.setPreferSemiAsBlack(true);
          }
        });
    return openingStyle;
  }

  private boolean isE4(ChessOpening opening) {
    return opening.getMoves_lalg().get(0).equals("e2e4");
  }

  private boolean isD4(ChessOpening opening) {
    return opening.getMoves_lalg().get(0).equals("d2d4");
  }

  private boolean isSymetric(ChessOpening opening) {
    return (isE4(opening) && opening.getMoves_lalg().get(1).equals("e7e5"))
      || (isD4(opening) && opening.getMoves_lalg().get(1).equals("d7d5"));
  }

  private ChessOpening getOpening(OpeningStat stat) {
    return openingCache.getOpeningMap().get(stat.getName());
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

  @AutoValue
  static abstract class KeyOpenings {
    // Best opening.
    public abstract Optional<OpeningStat> getWeapon();

    // Worst opening.
    public abstract Optional<OpeningStat> getWeakness();

    // Most played opening.
    public abstract Optional<OpeningStat> getMostPlayed();

    // Most played against E4. Statistic only for Perspective.AS_BLACK.
    public abstract Optional<OpeningStat> getMostPlayedAgainstE4();

    // Most played against D4. Statistic only for Perspective.AS_BLACK.
    public abstract Optional<OpeningStat> getMostPlayedAgainstD4();

    public static Builder newBuilder() {
      return new AutoValue_StyleAnalyzerService_KeyOpenings.Builder()
          .setWeapon(Optional.empty())
          .setWeakness(Optional.empty())
          .setMostPlayed(Optional.empty())
          .setMostPlayedAgainstE4(Optional.empty())
          .setMostPlayedAgainstD4(Optional.empty());
    }

    @AutoValue.Builder
    abstract static class Builder {
      abstract Builder setWeapon(Optional<OpeningStat> value);
      abstract Builder setWeakness(Optional<OpeningStat> value);
      abstract Builder setMostPlayed(Optional<OpeningStat> value);
      abstract Builder setMostPlayedAgainstE4(Optional<OpeningStat> value);
      abstract Builder setMostPlayedAgainstD4(Optional<OpeningStat> value);
      abstract KeyOpenings build();
    }
  }
}
