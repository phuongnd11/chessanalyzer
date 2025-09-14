package com.inspireon.chessanalyzer.web.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChessApiResponse {
    
    private String text;
    private Double eval;
    private String move;
    private String fen;
    private Integer depth;
    private Double winChance;
    private List<String> continuationArr;
    private Integer mate;
    private String centipawns;
    private String debug;
    private Object captured; // Can be boolean false or string (piece type)
    private Boolean promotion;
    
    // Move details
    private String san;
    private String lan;
    private String turn;
    private String color;
    private String piece;
    private String flags;
    private Boolean isCapture;
    private Boolean isCastling;
    private Boolean isPromotion;
    
    // Move coordinates
    private String from;
    private String to;
    private String fromNumeric;
    private String toNumeric;
    
    // Task metadata
    private String taskId;
    private Long time;
    private String type;
    
    public ChessApiResponse() {}
    
    // Getters and setters
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public Double getEval() {
        return eval;
    }
    
    public void setEval(Double eval) {
        this.eval = eval;
    }
    
    public String getMove() {
        return move;
    }
    
    public void setMove(String move) {
        this.move = move;
    }
    
    public String getFen() {
        return fen;
    }
    
    public void setFen(String fen) {
        this.fen = fen;
    }
    
    public Integer getDepth() {
        return depth;
    }
    
    public void setDepth(Integer depth) {
        this.depth = depth;
    }
    
    public Double getWinChance() {
        return winChance;
    }
    
    public void setWinChance(Double winChance) {
        this.winChance = winChance;
    }
    
    public List<String> getContinuationArr() {
        return continuationArr;
    }
    
    public void setContinuationArr(List<String> continuationArr) {
        this.continuationArr = continuationArr;
    }
    
    public Integer getMate() {
        return mate;
    }
    
    public void setMate(Integer mate) {
        this.mate = mate;
    }
    
    public String getCentipawns() {
        return centipawns;
    }
    
    public void setCentipawns(String centipawns) {
        this.centipawns = centipawns;
    }
    
    public String getSan() {
        return san;
    }
    
    public void setSan(String san) {
        this.san = san;
    }
    
    public String getLan() {
        return lan;
    }
    
    public void setLan(String lan) {
        this.lan = lan;
    }
    
    public String getTurn() {
        return turn;
    }
    
    public void setTurn(String turn) {
        this.turn = turn;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public String getPiece() {
        return piece;
    }
    
    public void setPiece(String piece) {
        this.piece = piece;
    }
    
    public String getFlags() {
        return flags;
    }
    
    public void setFlags(String flags) {
        this.flags = flags;
    }
    
    public Boolean getIsCapture() {
        return isCapture;
    }
    
    public void setIsCapture(Boolean isCapture) {
        this.isCapture = isCapture;
    }
    
    public Boolean getIsCastling() {
        return isCastling;
    }
    
    public void setIsCastling(Boolean isCastling) {
        this.isCastling = isCastling;
    }
    
    public Boolean getIsPromotion() {
        return isPromotion;
    }
    
    public void setIsPromotion(Boolean isPromotion) {
        this.isPromotion = isPromotion;
    }
    
    public String getFrom() {
        return from;
    }
    
    public void setFrom(String from) {
        this.from = from;
    }
    
    public String getTo() {
        return to;
    }
    
    public void setTo(String to) {
        this.to = to;
    }
    
    public String getFromNumeric() {
        return fromNumeric;
    }
    
    public void setFromNumeric(String fromNumeric) {
        this.fromNumeric = fromNumeric;
    }
    
    public String getToNumeric() {
        return toNumeric;
    }
    
    public void setToNumeric(String toNumeric) {
        this.toNumeric = toNumeric;
    }
    
    public String getTaskId() {
        return taskId;
    }
    
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    
    public Long getTime() {
        return time;
    }
    
    public void setTime(Long time) {
        this.time = time;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getDebug() {
        return debug;
    }
    
    public void setDebug(String debug) {
        this.debug = debug;
    }
    
    public Object getCaptured() {
        return captured;
    }
    
    public void setCaptured(Object captured) {
        this.captured = captured;
    }
    
    // Helper method to check if there was a capture
    public boolean isCaptureMade() {
        return captured instanceof String;
    }
    
    // Helper method to get captured piece type (returns null if no capture)
    public String getCapturedPieceType() {
        return captured instanceof String ? (String) captured : null;
    }
    
    public Boolean getPromotion() {
        return promotion;
    }
    
    public void setPromotion(Boolean promotion) {
        this.promotion = promotion;
    }
    
    // Helper method to get score in centipawns as integer
    public int getCentipawnsAsInt() {
        if (centipawns != null && !centipawns.isEmpty()) {
            try {
                return Integer.parseInt(centipawns);
            } catch (NumberFormatException e) {
                // Fall back to eval * 100 if centipawns can't be parsed
                return eval != null ? (int) (eval * 100) : 0;
            }
        }
        return eval != null ? (int) (eval * 100) : 0;
    }
}