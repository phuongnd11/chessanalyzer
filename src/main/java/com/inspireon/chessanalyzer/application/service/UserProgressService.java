package com.inspireon.chessanalyzer.application.service;

import com.inspireon.chessanalyzer.domain.documents.UserProgressDocument;
import com.inspireon.chessanalyzer.domain.repository.UserProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserProgressService {

    @Autowired
    private UserProgressRepository userProgressRepository;

    public UserProgressDocument getOrCreateUserProgress(String username) {
        return userProgressRepository.findByUsername(username)
                .orElseGet(() -> {
                    UserProgressDocument newProgress = new UserProgressDocument(username);
                    return userProgressRepository.save(newProgress);
                });
    }

    public Set<String> getSolvedMistakeIds(String username) {
        UserProgressDocument progress = userProgressRepository.findSolvedMistakeIdsByUsername(username);
        return progress != null ? progress.getSolvedMistakeIds() : new HashSet<>();
    }

    public Set<String> getSolvedPuzzleIds(String username) {
        UserProgressDocument progress = userProgressRepository.findSolvedPuzzleIdsByUsername(username);
        return progress != null ? progress.getSolvedPuzzleIds() : new HashSet<>();
    }

    public void markMistakeSolved(String username, String mistakeId) {
        UserProgressDocument progress = getOrCreateUserProgress(username);
        progress.addSolvedMistake(mistakeId);
        userProgressRepository.save(progress);
    }

    public void markPuzzleSolved(String username, String puzzleId) {
        UserProgressDocument progress = getOrCreateUserProgress(username);
        progress.addSolvedPuzzle(puzzleId);
        userProgressRepository.save(progress);
    }

    public boolean hasSolvedMistake(String username, String mistakeId) {
        UserProgressDocument progress = userProgressRepository.findByUsername(username).orElse(null);
        return progress != null && progress.hasSolvedMistake(mistakeId);
    }

    public boolean hasSolvedPuzzle(String username, String puzzleId) {
        UserProgressDocument progress = userProgressRepository.findByUsername(username).orElse(null);
        return progress != null && progress.hasSolvedPuzzle(puzzleId);
    }

    public void recordMistakeSolved(String username, String mistakeId, int attempts) {
        UserProgressDocument progress = getOrCreateUserProgress(username);
        progress.addSolvedMistake(mistakeId);
        userProgressRepository.save(progress);
    }

    public int getTotalSolvedMistakes(String username) {
        UserProgressDocument progress = userProgressRepository.findByUsername(username).orElse(null);
        return progress != null ? progress.getSolvedMistakeIds().size() : 0;
    }
}