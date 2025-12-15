package com.inspireon.chessanalyzer.web.controller;

import com.inspireon.chessanalyzer.application.service.UserProgressService;
import com.inspireon.chessanalyzer.application.service.UserMistakeService;
import com.inspireon.chessanalyzer.application.service.WeeklyAnalysisService;
import com.inspireon.chessanalyzer.domain.documents.UserMistakeDocument;
import com.inspireon.chessanalyzer.web.dtos.PuzzleSolvedRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/puzzles")
@CrossOrigin(origins = {"http://localhost:3000", "https://funchess.io", "https://www.funchess.io"})
public class PuzzleController {

    @Autowired
    private UserMistakeService userMistakeService;

    @Autowired
    private UserProgressService userProgressService;

    @Autowired
    private WeeklyAnalysisService weeklyAnalysisService;

    @GetMapping("/mistakes/{username}")
    public ResponseEntity<List<UserMistakeDocument>> getUserMistakes(@PathVariable("username") String username) {
        List<UserMistakeDocument> mistakes = userMistakeService.findRecentMistakesByUsername(username);
        return ResponseEntity.ok(mistakes);
    }

    @PostMapping("/mistakes/{mistakeId}/solve")
    public ResponseEntity<Map<String, Object>> recordPuzzleSolved(
            @PathVariable("mistakeId") String mistakeId,
            @RequestParam("username") String username,
            @RequestParam("attempts") int attempts) {
        
        try {
            // Validate input parameters
            if (attempts < 1) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Attempts must be greater than 0"
                ));
            }
            
            // Check if mistake exists and is not already solved
            if (userMistakeService.isMistakeSolved(mistakeId)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error", 
                    "message", "Mistake is already marked as solved"
                ));
            }
            
            // Mark mistake as solved in the mistake document
            var solvedMistake = userMistakeService.markMistakeSolved(mistakeId, username, attempts);
            
            // Record in user progress
            userProgressService.recordMistakeSolved(username, mistakeId, attempts);
            
            // Return success response with statistics
            var totalSolved = userProgressService.getTotalSolvedMistakes(username);
            var totalMistakes = userMistakeService.countMistakesByUsername(username);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Puzzle solved successfully!",
                "mistakeId", mistakeId,
                "attempts", attempts,
                "solvedAt", solvedMistake.getSolvedAt().toString(),
                "progress", Map.of(
                    "totalSolved", totalSolved,
                    "totalMistakes", totalMistakes,
                    "progressPercentage", totalMistakes > 0 ? (totalSolved * 100.0 / totalMistakes) : 0
                )
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "An unexpected error occurred: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/mistakes/{mistakeId}/solve-json")
    public ResponseEntity<Map<String, Object>> recordPuzzleSolvedJson(
            @PathVariable("mistakeId") String mistakeId,
            @RequestBody PuzzleSolvedRequest request) {
        
        return recordPuzzleSolved(mistakeId, request.getUsername(), request.getAttempts());
    }

    @GetMapping("/mistakes/{username}/unsolved")
    public ResponseEntity<List<UserMistakeDocument>> getUnsolvedMistakes(
            @PathVariable("username") String username,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        
        List<UserMistakeDocument> unsolvedMistakes = userMistakeService.findUnsolvedMistakesByUsername(username, limit);
        
        // If we have fewer than the requested limit, try to get more immediately and continue async
        if (unsolvedMistakes.size() < limit) {
            try {
                // Get immediate results and trigger async continuation for more
                List<UserMistakeDocument> immediateResults = weeklyAnalysisService.findPuzzlesImmediateWithAsyncContinuation(
                    username, limit, unsolvedMistakes.size());
                
                // Combine existing unsolved with immediate results
                unsolvedMistakes.addAll(immediateResults);
                
            } catch (Exception e) {
                // Log error but don't fail the request
                System.err.println("Failed to get immediate puzzles for user " + username + ": " + e.getMessage());
            }
        }
        
        // If still no unsolved mistakes found, trigger traditional analysis
        if (unsolvedMistakes.isEmpty()) {
            try {
                // Trigger background analysis for new puzzles
                weeklyAnalysisService.analyzeAdditionalMistakesAsync(username, limit);
                
                // Check again for any newly found mistakes
                unsolvedMistakes = userMistakeService.findUnsolvedMistakesByUsername(username, limit);
            } catch (Exception e) {
                // Log error but don't fail the request
                System.err.println("Failed to trigger new analysis for user " + username + ": " + e.getMessage());
            }
        }
        
        return ResponseEntity.ok(unsolvedMistakes);
    }

    @GetMapping("/progress/{username}")
    public ResponseEntity<Map<String, Object>> getUserProgress(@PathVariable("username") String username) {
        var progress = userProgressService.getOrCreateUserProgress(username);
        long totalMistakes = userMistakeService.countMistakesByUsername(username);
        
        return ResponseEntity.ok(Map.of(
            "totalMistakes", totalMistakes,
            "solvedMistakes", progress.getSolvedMistakeIds().size(),
            "solvedPuzzles", progress.getSolvedPuzzleIds().size(),
            "progressPercentage", totalMistakes > 0 ? (progress.getSolvedMistakeIds().size() * 100.0 / totalMistakes) : 0
        ));
    }
}