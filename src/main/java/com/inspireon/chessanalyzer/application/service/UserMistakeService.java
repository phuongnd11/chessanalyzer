package com.inspireon.chessanalyzer.application.service;

import com.inspireon.chessanalyzer.common.enums.TacticalTheme;
import com.inspireon.chessanalyzer.domain.documents.UserMistakeDocument;
import com.inspireon.chessanalyzer.domain.repository.UserMistakeRepository;
import com.inspireon.chessanalyzer.web.dtos.UserMistake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserMistakeService {

    @Autowired
    private UserMistakeRepository userMistakeRepository;

    public UserMistakeDocument saveMistake(UserMistakeDocument mistake) {
        return userMistakeRepository.save(mistake);
    }

    public UserMistakeDocument saveMistakeFromDto(UserMistake mistakeDto) {
        UserMistakeDocument document = convertToDocument(mistakeDto);
        return userMistakeRepository.save(document);
    }

    public Optional<UserMistakeDocument> findById(String id) {
        return userMistakeRepository.findById(id);
    }

    public List<UserMistakeDocument> findByUsername(String username) {
        return userMistakeRepository.findByUsername(username);
    }

    public List<UserMistakeDocument> findRecentMistakesByUsername(String username) {
        return userMistakeRepository.findByUsernameOrderByDatePlayedDesc(username);
    }

    public Page<UserMistakeDocument> findByUsernamePaged(String username, Pageable pageable) {
        return userMistakeRepository.findByUsername(username, pageable);
    }

    public List<UserMistakeDocument> findByGameId(String gameId) {
        return userMistakeRepository.findByGameId(gameId);
    }

    public List<UserMistakeDocument> findByUsernameAndTacticalTheme(String username, TacticalTheme theme) {
        return userMistakeRepository.findByUsernameAndTacticalTheme(username, theme);
    }

    public List<UserMistakeDocument> findByUsernameAndGamePhase(String username, String gamePhase) {
        return userMistakeRepository.findByUsernameAndGamePhase(username, gamePhase);
    }

    public List<UserMistakeDocument> findByDateRange(String username, LocalDate startDate, LocalDate endDate) {
        return userMistakeRepository.findByUsernameAndDatePlayedBetween(username, startDate, endDate);
    }

    public List<UserMistakeDocument> findMajorMistakes(String username, int minScoreDrop) {
        return userMistakeRepository.findByUsernameAndScoreDropGreaterThanEqual(username, minScoreDrop);
    }

    public List<UserMistakeDocument> findUnprocessedMistakes(int minScoreDrop) {
        return userMistakeRepository.findUnprocessedMistakesWithMinScoreDrop(minScoreDrop);
    }

    public List<UserMistakeDocument> findByMultipleThemes(String username, List<TacticalTheme> themes) {
        return userMistakeRepository.findByUsernameAndTacticalThemeIn(username, themes);
    }

    public long countMistakesByUsername(String username) {
        return userMistakeRepository.countByUsername(username);
    }

    public long countMistakesByTheme(String username, TacticalTheme theme) {
        return userMistakeRepository.countByUsernameAndTacticalTheme(username, theme);
    }

    public List<TacticalTheme> getUserCommonThemes(String username) {
        List<UserMistakeDocument> themes = userMistakeRepository.findTacticalThemesByUsername(username);
        return themes.stream()
                .map(UserMistakeDocument::getTacticalTheme)
                .distinct()
                .collect(Collectors.toList());
    }

    public void markPuzzleGenerated(String mistakeId, String puzzleId) {
        Optional<UserMistakeDocument> mistake = userMistakeRepository.findById(mistakeId);
        if (mistake.isPresent()) {
            UserMistakeDocument doc = mistake.get();
            doc.setPuzzleGenerated(true);
            doc.setPuzzleId(puzzleId);
            userMistakeRepository.save(doc);
        }
    }

    public void deleteById(String id) {
        userMistakeRepository.deleteById(id);
    }

    public void deleteAll() {
        userMistakeRepository.deleteAll();
    }

    public UserMistakeDocument markMistakeSolved(String mistakeId, String solvedByUsername, int attempts) {
        Optional<UserMistakeDocument> mistakeOpt = userMistakeRepository.findById(mistakeId);
        if (mistakeOpt.isPresent()) {
            UserMistakeDocument mistake = mistakeOpt.get();
            mistake.setIsSolved(true);
            mistake.setSolveAttempts(attempts);
            mistake.setSolvedAt(java.time.LocalDateTime.now());
            mistake.setSolvedBy(solvedByUsername);
            mistake.setUpdatedAt(java.time.LocalDateTime.now());
            return userMistakeRepository.save(mistake);
        }
        throw new IllegalArgumentException("Mistake with ID " + mistakeId + " not found");
    }

    public boolean isMistakeSolved(String mistakeId) {
        Optional<UserMistakeDocument> mistake = userMistakeRepository.findById(mistakeId);
        return mistake.isPresent() && Boolean.TRUE.equals(mistake.get().getIsSolved());
    }

    public List<UserMistakeDocument> findUnsolvedMistakesByUsername(String username, int limit) {
        return userMistakeRepository.findByUsername(username).stream()
                .filter(mistake -> !Boolean.TRUE.equals(mistake.getIsSolved()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private UserMistakeDocument convertToDocument(UserMistake dto) {
        UserMistakeDocument document = new UserMistakeDocument();
        document.setGameId(dto.getGameId());
        document.setUsername(dto.getUsername());
        document.setFen(dto.getFen());
        document.setUserMove(dto.getUserMove());
        document.setComputerMove(dto.getComputerMove());
        document.setGamePhase(dto.getGamePhase());
        document.setMoveNumber(dto.getMoveNumber());
        document.setScoreDrop(dto.getScoreDrop());
        document.setTacticalTheme(dto.getTacticalTheme());
        document.setEvidence(dto.getEvidence());
        return document;
    }

    public UserMistake convertToDto(UserMistakeDocument document) {
        UserMistake dto = new UserMistake(
            document.getGameId(),
            document.getUsername(),
            document.getFen(),
            document.getUserMove(),
            document.getComputerMove(),
            document.getGamePhase(),
            document.getMoveNumber(),
            document.getScoreDrop(),
            document.getTacticalTheme(),
            document.getEvidence()
        );
        return dto;
    }
}