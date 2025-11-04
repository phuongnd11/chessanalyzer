package com.inspireon.chessanalyzer.application.service;

import com.inspireon.chessanalyzer.common.enums.TacticalTheme;
import com.inspireon.chessanalyzer.domain.documents.GeneratedPuzzleDocument;
import com.inspireon.chessanalyzer.domain.repository.GeneratedPuzzleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PuzzleService {

    @Autowired
    private GeneratedPuzzleRepository puzzleRepository;

    public GeneratedPuzzleDocument savePuzzle(GeneratedPuzzleDocument puzzle) {
        return puzzleRepository.save(puzzle);
    }

    public Optional<GeneratedPuzzleDocument> findById(String id) {
        return puzzleRepository.findById(id);
    }

    @Cacheable(value = "puzzles", key = "#theme + '_' + #difficulty")
    public List<GeneratedPuzzleDocument> findPuzzlesByThemeAndDifficulty(TacticalTheme theme, String difficulty) {
        return puzzleRepository.findByTacticalThemeAndDifficultyAndPublished(theme, difficulty);
    }

    public Page<GeneratedPuzzleDocument> findPublishedPuzzles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("qualityScore").descending());
        return puzzleRepository.findPublishedPuzzles(pageable);
    }

    public Page<GeneratedPuzzleDocument> findPuzzlesByTheme(TacticalTheme theme, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("qualityScore").descending());
        return puzzleRepository.findByTacticalThemeAndPublished(theme, pageable);
    }

    public List<GeneratedPuzzleDocument> findPuzzlesByRatingRange(int minRating, int maxRating) {
        return puzzleRepository.findByRatingRangeAndPublished(minRating, maxRating);
    }

    public List<GeneratedPuzzleDocument> findPuzzlesByGamePhase(String gamePhase) {
        return puzzleRepository.findByGamePhaseAndPublished(gamePhase);
    }

    public List<GeneratedPuzzleDocument> findPuzzlesByThemeAndPhase(TacticalTheme theme, String gamePhase) {
        return puzzleRepository.findByTacticalThemeAndGamePhaseAndPublished(theme, gamePhase);
    }

    @Cacheable(value = "userPuzzles", key = "#username")
    public List<GeneratedPuzzleDocument> findPuzzlesBySourceUsername(String username) {
        return puzzleRepository.findBySourceUsernameAndPublished(username);
    }

    public List<GeneratedPuzzleDocument> findPuzzlesByMultipleThemes(List<TacticalTheme> themes) {
        return puzzleRepository.findByTacticalThemeInAndPublished(themes);
    }

    public Page<GeneratedPuzzleDocument> findPuzzlesByDifficulty(String difficulty, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("rating").ascending());
        return puzzleRepository.findByDifficultyAndPublished(difficulty, pageable);
    }

    public List<GeneratedPuzzleDocument> findHighQualityPuzzles(int minQualityScore) {
        return puzzleRepository.findHighQualityPublishedPuzzles(minQualityScore);
    }

    public List<GeneratedPuzzleDocument> findDifficultPuzzles(double maxSuccessRate) {
        return puzzleRepository.findDifficultPuzzles(maxSuccessRate);
    }

    public List<GeneratedPuzzleDocument> findEasyPuzzles(double minSuccessRate) {
        return puzzleRepository.findEasyPuzzles(minSuccessRate);
    }

    public List<GeneratedPuzzleDocument> searchPuzzles(String searchTerm) {
        return puzzleRepository.findByTextSearch(searchTerm);
    }

    public List<GeneratedPuzzleDocument> findPuzzlesByTags(List<String> tags) {
        return puzzleRepository.findByTagsAndPublished(tags);
    }

    public List<GeneratedPuzzleDocument> getRandomPuzzles(TacticalTheme theme, String difficulty, int count) {
        List<GeneratedPuzzleDocument> puzzles = findPuzzlesByThemeAndDifficulty(theme, difficulty);
        
        // Shuffle and limit to requested count
        java.util.Collections.shuffle(puzzles);
        return puzzles.stream()
                .limit(count)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<GeneratedPuzzleDocument> getPersonalizedPuzzles(String username, List<TacticalTheme> weakThemes, int ratingRange) {
        // Find puzzles based on user's common mistake patterns
        return puzzleRepository.findByTacticalThemeInAndPublished(weakThemes);
    }

    public void publishPuzzle(String puzzleId) {
        Optional<GeneratedPuzzleDocument> puzzle = puzzleRepository.findById(puzzleId);
        if (puzzle.isPresent()) {
            GeneratedPuzzleDocument doc = puzzle.get();
            doc.setIsPublished(true);
            puzzleRepository.save(doc);
        }
    }

    public void verifyPuzzle(String puzzleId) {
        Optional<GeneratedPuzzleDocument> puzzle = puzzleRepository.findById(puzzleId);
        if (puzzle.isPresent()) {
            GeneratedPuzzleDocument doc = puzzle.get();
            doc.setIsVerified(true);
            puzzleRepository.save(doc);
        }
    }

    public void updatePuzzleStats(String puzzleId, boolean correct, double solvingTime) {
        Optional<GeneratedPuzzleDocument> puzzle = puzzleRepository.findById(puzzleId);
        if (puzzle.isPresent()) {
            GeneratedPuzzleDocument doc = puzzle.get();
            GeneratedPuzzleDocument.PuzzleStats stats = doc.getStats();
            
            stats.setTotalAttempts(stats.getTotalAttempts() + 1);
            if (correct) {
                stats.setCorrectSolves(stats.getCorrectSolves() + 1);
            }
            
            // Update average time
            double totalTime = stats.getAverageTime() * (stats.getTotalAttempts() - 1) + solvingTime;
            stats.setAverageTime(totalTime / stats.getTotalAttempts());
            
            // Update success rate
            stats.setSuccessRate((double) stats.getCorrectSolves() / stats.getTotalAttempts());
            
            puzzleRepository.save(doc);
        }
    }

    public long countPublishedPuzzles() {
        return puzzleRepository.countPublishedPuzzles();
    }

    public long countPuzzlesByTheme(TacticalTheme theme) {
        return puzzleRepository.countByTacticalThemeAndIsPublished(theme, true);
    }

    public List<GeneratedPuzzleDocument> findUnverifiedPuzzles() {
        return puzzleRepository.findUnverifiedPuzzles();
    }

    public void deleteById(String id) {
        puzzleRepository.deleteById(id);
    }

    public void deleteAll() {
        puzzleRepository.deleteAll();
    }
}