package com.inspireon.chessanalyzer.web.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChessApiRequest {
    
    private String fen;
    private Integer variants;
    private Integer depth;
    private Integer maxThinkingTime;
    private String searchmoves;
    
    public ChessApiRequest() {}
    
    public ChessApiRequest(String fen) {
        this.fen = fen;
    }
    
    public ChessApiRequest(String fen, Integer variants, Integer depth, Integer maxThinkingTime) {
        this.fen = fen;
        this.variants = variants;
        this.depth = depth;
        this.maxThinkingTime = maxThinkingTime;
    }
    
    public String getFen() {
        return fen;
    }
    
    public void setFen(String fen) {
        this.fen = fen;
    }
    
    public Integer getVariants() {
        return variants;
    }
    
    public void setVariants(Integer variants) {
        this.variants = variants;
    }
    
    public Integer getDepth() {
        return depth;
    }
    
    public void setDepth(Integer depth) {
        this.depth = depth;
    }
    
    public Integer getMaxThinkingTime() {
        return maxThinkingTime;
    }
    
    public void setMaxThinkingTime(Integer maxThinkingTime) {
        this.maxThinkingTime = maxThinkingTime;
    }
    
    public String getSearchmoves() {
        return searchmoves;
    }
    
    public void setSearchmoves(String searchmoves) {
        this.searchmoves = searchmoves;
    }
}