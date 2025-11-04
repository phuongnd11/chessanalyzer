package com.inspireon.chessanalyzer.domain.repository;

import com.inspireon.chessanalyzer.common.enums.TacticalTheme;
import com.inspireon.chessanalyzer.domain.documents.UserMistakeDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserMistakeRepository extends MongoRepository<UserMistakeDocument, String> {

    List<UserMistakeDocument> findByUsername(String username);

    List<UserMistakeDocument> findByUsernameOrderByDatePlayedDesc(String username);

    Page<UserMistakeDocument> findByUsername(String username, Pageable pageable);

    List<UserMistakeDocument> findByGameId(String gameId);

    List<UserMistakeDocument> findByUsernameAndTacticalTheme(String username, TacticalTheme tacticalTheme);

    List<UserMistakeDocument> findByUsernameAndGamePhase(String username, String gamePhase);

    List<UserMistakeDocument> findByUsernameAndDatePlayedBetween(String username, LocalDate startDate, LocalDate endDate);

    @Query("{ 'username': ?0, 'tactical_theme': ?1, 'game_phase': ?2 }")
    List<UserMistakeDocument> findByUsernameAndTacticalThemeAndGamePhase(String username, TacticalTheme tacticalTheme, String gamePhase);

    @Query("{ 'username': ?0, 'score_drop': { $gte: ?1 } }")
    List<UserMistakeDocument> findByUsernameAndScoreDropGreaterThanEqual(String username, Integer minScoreDrop);

    List<UserMistakeDocument> findByTacticalThemeAndPuzzleGenerated(TacticalTheme tacticalTheme, Boolean puzzleGenerated);

    @Query("{ 'puzzle_generated': false, 'score_drop': { $gte: ?0 } }")
    List<UserMistakeDocument> findUnprocessedMistakesWithMinScoreDrop(Integer minScoreDrop);

    @Query("{ 'username': ?0, 'tactical_theme': { $in: ?1 } }")
    List<UserMistakeDocument> findByUsernameAndTacticalThemeIn(String username, List<TacticalTheme> tacticalThemes);

    long countByUsername(String username);

    long countByUsernameAndTacticalTheme(String username, TacticalTheme tacticalTheme);

    @Query(value = "{ 'username': ?0 }", fields = "{ 'tactical_theme': 1, '_id': 0 }")
    List<UserMistakeDocument> findTacticalThemesByUsername(String username);
}