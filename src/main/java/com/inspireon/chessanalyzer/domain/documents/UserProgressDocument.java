package com.inspireon.chessanalyzer.domain.documents;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Data
@Document(collection = "user_progress")
@CompoundIndex(name = "username_mistake_idx", def = "{'username' : 1, 'solved_mistake_ids' : 1}")
public class UserProgressDocument {

    @Id
    private String id;

    @Indexed
    @Field("username")
    private String username;

    @Field("solved_mistake_ids")
    private Set<String> solvedMistakeIds = new HashSet<>();

    @Field("solved_puzzle_ids")
    private Set<String> solvedPuzzleIds = new HashSet<>();

    @Indexed
    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    public UserProgressDocument() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UserProgressDocument(String username) {
        this();
        this.username = username;
    }

    public void addSolvedMistake(String mistakeId) {
        this.solvedMistakeIds.add(mistakeId);
        this.updatedAt = LocalDateTime.now();
    }

    public void addSolvedPuzzle(String puzzleId) {
        this.solvedPuzzleIds.add(puzzleId);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasSolvedMistake(String mistakeId) {
        return this.solvedMistakeIds.contains(mistakeId);
    }

    public boolean hasSolvedPuzzle(String puzzleId) {
        return this.solvedPuzzleIds.contains(puzzleId);
    }
}