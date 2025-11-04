package com.inspireon.chessanalyzer.domain.repository;

import com.inspireon.chessanalyzer.common.enums.TacticalTheme;
import com.inspireon.chessanalyzer.domain.documents.GeneratedPuzzleDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneratedPuzzleRepository extends MongoRepository<GeneratedPuzzleDocument, String> {

    @Query("{ 'is_published': true }")
    Page<GeneratedPuzzleDocument> findPublishedPuzzles(Pageable pageable);

    @Query("{ 'tactical_theme': ?0, 'difficulty': ?1, 'is_published': true }")
    List<GeneratedPuzzleDocument> findByTacticalThemeAndDifficultyAndPublished(TacticalTheme tacticalTheme, String difficulty);

    @Query("{ 'tactical_theme': ?0, 'is_published': true }")
    Page<GeneratedPuzzleDocument> findByTacticalThemeAndPublished(TacticalTheme tacticalTheme, Pageable pageable);

    @Query("{ 'rating': { $gte: ?0, $lte: ?1 }, 'is_published': true }")
    List<GeneratedPuzzleDocument> findByRatingRangeAndPublished(Integer minRating, Integer maxRating);

    @Query("{ 'game_phase': ?0, 'is_published': true }")
    List<GeneratedPuzzleDocument> findByGamePhaseAndPublished(String gamePhase);

    @Query("{ 'tactical_theme': ?0, 'game_phase': ?1, 'is_published': true }")
    List<GeneratedPuzzleDocument> findByTacticalThemeAndGamePhaseAndPublished(TacticalTheme tacticalTheme, String gamePhase);

    @Query("{ 'source_username': ?0, 'is_published': true }")
    List<GeneratedPuzzleDocument> findBySourceUsernameAndPublished(String sourceUsername);

    @Query("{ 'tactical_theme': { $in: ?0 }, 'is_published': true }")
    List<GeneratedPuzzleDocument> findByTacticalThemeInAndPublished(List<TacticalTheme> tacticalThemes);

    @Query("{ 'difficulty': ?0, 'is_published': true }")
    Page<GeneratedPuzzleDocument> findByDifficultyAndPublished(String difficulty, Pageable pageable);

    @Query("{ 'is_published': true, 'quality_score': { $gte: ?0 } }")
    List<GeneratedPuzzleDocument> findHighQualityPublishedPuzzles(Integer minQualityScore);

    @Query("{ 'stats.success_rate': { $lt: ?0 }, 'is_published': true }")
    List<GeneratedPuzzleDocument> findDifficultPuzzles(Double maxSuccessRate);

    @Query("{ 'stats.success_rate': { $gt: ?0 }, 'is_published': true }")
    List<GeneratedPuzzleDocument> findEasyPuzzles(Double minSuccessRate);

    List<GeneratedPuzzleDocument> findBySourceMistakeId(String sourceMistakeId);

    @Query("{ 'is_verified': false, 'is_published': false }")
    List<GeneratedPuzzleDocument> findUnverifiedPuzzles();

    @Query("{ $text: { $search: ?0 }, 'is_published': true }")
    List<GeneratedPuzzleDocument> findByTextSearch(String searchTerm);

    @Query(value = "{ 'is_published': true }", count = true)
    long countPublishedPuzzles();

    long countByTacticalThemeAndIsPublished(TacticalTheme tacticalTheme, Boolean isPublished);

    @Query("{ 'tags': { $in: ?0 }, 'is_published': true }")
    List<GeneratedPuzzleDocument> findByTagsAndPublished(List<String> tags);

    @Query(value = "{ 'is_published': true }", fields = "{ 'fen': 1, 'computer_lines': 1, 'original_user_move': 1 }")
    List<GeneratedPuzzleDocument> findPuzzleDataOnly();
}