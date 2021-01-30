package com.inspireon.chessanalyzer.domain.datamanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import com.inspireon.chessanalyzer.common.io.OpeningFileAccess;
import com.inspireon.chessanalyzer.domain.cache.PlayerStatCache;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
public class OpeningIndexerTest {

  private static final String TEST_GAMES_FOLDER_PATH =
      "src/test/resources/games/";
  private static final String TEST_PLAYER = "Barack Osaka";

  //@Autowired OpeningIndexer openingIndexer;

  @MockBean GameDataAccess mockGameDataAccess;

  //@MockBean PlayerStatCache playerStatCache;
  
  //@MockBean OpeningFileAccess openingFileAccess;

  private static List<Game> testGames;
  
  @BeforeAll
  static void setupAll() throws Exception {
    testGames = new ArrayList<>();

    File gamesFolder = new File(TEST_GAMES_FOLDER_PATH);
    assertTrue(gamesFolder.exists());
    assertTrue(gamesFolder.isDirectory());
    FilenameFilter pgnFilter = (dir, name) -> name.endsWith(".pgn");

    for (File pgnFile : gamesFolder.listFiles(pgnFilter)) {
      PgnHolder pgn = new PgnHolder(pgnFile.getAbsolutePath());
      pgn.loadPgn();
      testGames.addAll(pgn.getGames());
    }
  }

  @BeforeEach
  void setup() throws Exception {
    when(mockGameDataAccess.getGames(TEST_PLAYER)).thenReturn(testGames);
  }

  @Test
  void testSomething() throws Exception {
  }

}
