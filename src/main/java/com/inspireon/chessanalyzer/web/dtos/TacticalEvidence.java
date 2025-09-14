package com.inspireon.chessanalyzer.web.dtos;

import java.util.List;
import java.util.Map;

public class TacticalEvidence {
    
    private String opponentReply;
    private List<String> targets;
    private Integer seeGainCp;
    private Integer checksInLine;
    private Integer mateIn;
    private String square;
    private String byPiece;
    private Boolean absolutePin;
    private List<String> attackedSquares;
    private String lineOpened;
    private String defenderRemoved;
    private String overloadedPiece;
    private Double confidence;
    private Map<String, Object> additionalData;
    
    public TacticalEvidence() {}
    
    // Builder pattern for easy construction
    public static class Builder {
        private TacticalEvidence evidence = new TacticalEvidence();
        
        public Builder opponentReply(String opponentReply) {
            evidence.opponentReply = opponentReply;
            return this;
        }
        
        public Builder targets(List<String> targets) {
            evidence.targets = targets;
            return this;
        }
        
        public Builder seeGainCp(Integer seeGainCp) {
            evidence.seeGainCp = seeGainCp;
            return this;
        }
        
        public Builder checksInLine(Integer checksInLine) {
            evidence.checksInLine = checksInLine;
            return this;
        }
        
        public Builder mateIn(Integer mateIn) {
            evidence.mateIn = mateIn;
            return this;
        }
        
        public Builder square(String square) {
            evidence.square = square;
            return this;
        }
        
        public Builder byPiece(String byPiece) {
            evidence.byPiece = byPiece;
            return this;
        }
        
        public Builder absolutePin(Boolean absolutePin) {
            evidence.absolutePin = absolutePin;
            return this;
        }
        
        public Builder attackedSquares(List<String> attackedSquares) {
            evidence.attackedSquares = attackedSquares;
            return this;
        }
        
        public Builder lineOpened(String lineOpened) {
            evidence.lineOpened = lineOpened;
            return this;
        }
        
        public Builder defenderRemoved(String defenderRemoved) {
            evidence.defenderRemoved = defenderRemoved;
            return this;
        }
        
        public Builder overloadedPiece(String overloadedPiece) {
            evidence.overloadedPiece = overloadedPiece;
            return this;
        }
        
        public Builder confidence(Double confidence) {
            evidence.confidence = confidence;
            return this;
        }
        
        public Builder additionalData(Map<String, Object> additionalData) {
            evidence.additionalData = additionalData;
            return this;
        }
        
        public TacticalEvidence build() {
            return evidence;
        }
    }
    
    // Getters and setters
    public String getOpponentReply() { return opponentReply; }
    public void setOpponentReply(String opponentReply) { this.opponentReply = opponentReply; }
    
    public List<String> getTargets() { return targets; }
    public void setTargets(List<String> targets) { this.targets = targets; }
    
    public Integer getSeeGainCp() { return seeGainCp; }
    public void setSeeGainCp(Integer seeGainCp) { this.seeGainCp = seeGainCp; }
    
    public Integer getChecksInLine() { return checksInLine; }
    public void setChecksInLine(Integer checksInLine) { this.checksInLine = checksInLine; }
    
    public Integer getMateIn() { return mateIn; }
    public void setMateIn(Integer mateIn) { this.mateIn = mateIn; }
    
    public String getSquare() { return square; }
    public void setSquare(String square) { this.square = square; }
    
    public String getByPiece() { return byPiece; }
    public void setByPiece(String byPiece) { this.byPiece = byPiece; }
    
    public Boolean getAbsolutePin() { return absolutePin; }
    public void setAbsolutePin(Boolean absolutePin) { this.absolutePin = absolutePin; }
    
    public List<String> getAttackedSquares() { return attackedSquares; }
    public void setAttackedSquares(List<String> attackedSquares) { this.attackedSquares = attackedSquares; }
    
    public String getLineOpened() { return lineOpened; }
    public void setLineOpened(String lineOpened) { this.lineOpened = lineOpened; }
    
    public String getDefenderRemoved() { return defenderRemoved; }
    public void setDefenderRemoved(String defenderRemoved) { this.defenderRemoved = defenderRemoved; }
    
    public String getOverloadedPiece() { return overloadedPiece; }
    public void setOverloadedPiece(String overloadedPiece) { this.overloadedPiece = overloadedPiece; }
    
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    
    public Map<String, Object> getAdditionalData() { return additionalData; }
    public void setAdditionalData(Map<String, Object> additionalData) { this.additionalData = additionalData; }
}