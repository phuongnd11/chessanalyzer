package com.inspireon.chessanalyzer.web.dtos;

import lombok.Data;

@Data
public class PuzzleSolvedRequest {
    private String username;
    private int attempts;

    public PuzzleSolvedRequest() {}

    public PuzzleSolvedRequest(String username, int attempts) {
        this.username = username;
        this.attempts = attempts;
    }
}