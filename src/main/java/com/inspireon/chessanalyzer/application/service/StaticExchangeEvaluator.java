package com.inspireon.chessanalyzer.application.service;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Static Exchange Evaluation (SEE) calculator
 * Determines the material outcome of a capture sequence on a given square
 */
@Service
@Slf4j
public class StaticExchangeEvaluator {
    
    private static final int[] PIECE_VALUES = {
        100,  // PAWN
        320,  // KNIGHT  
        330,  // BISHOP
        500,  // ROOK
        900,  // QUEEN
        20000 // KING (effectively infinite)
    };
    
    /**
     * Calculate SEE for a move (typically a capture)
     * Returns the material gain/loss from the attacker's perspective
     */
    public int calculateSEE(Board board, Move move) {
        Square targetSquare = move.getTo();
        Piece capturedPiece = board.getPiece(targetSquare);
        
        // If no piece to capture, SEE is 0
        if (capturedPiece == Piece.NONE) {
            return 0;
        }
        
        Side attacker = board.getSideToMove();
        Piece attackingPiece = board.getPiece(move.getFrom());
        
        // Get all attackers and defenders of the target square
        List<Integer> attackers = getAttackerValues(board, targetSquare, attacker);
        List<Integer> defenders = getAttackerValues(board, targetSquare, attacker.flip());
        
        // Initial gain is the captured piece value
        int gain = getPieceValue(capturedPiece.getPieceType());
        
        // Remove the moving piece from attackers (it's used in the initial capture)
        if (!attackers.isEmpty()) {
            attackers.remove(Integer.valueOf(getPieceValue(attackingPiece.getPieceType())));
        }
        
        return gain - seeRecursive(attackers, defenders, getPieceValue(attackingPiece.getPieceType()), false);
    }
    
    /**
     * Calculate SEE for capturing a piece on a square (without a specific move)
     * Useful for determining if a piece is "hanging"
     */
    public int calculateSEEOnSquare(Board board, Square square, Side attackingSide) {
        Piece targetPiece = board.getPiece(square);
        if (targetPiece == Piece.NONE) {
            return 0;
        }
        
        List<Integer> attackers = getAttackerValues(board, square, attackingSide);
        List<Integer> defenders = getAttackerValues(board, square, attackingSide.flip());
        
        if (attackers.isEmpty()) {
            return 0; // Can't capture
        }
        
        // Use the least valuable attacker
        int leastValuableAttacker = Collections.min(attackers);
        attackers.remove(Integer.valueOf(leastValuableAttacker));
        
        int gain = getPieceValue(targetPiece.getPieceType());
        return gain - seeRecursive(defenders, attackers, leastValuableAttacker, true);
    }
    
    /**
     * Recursive SEE calculation - alternates between sides capturing
     */
    private int seeRecursive(List<Integer> attackers, List<Integer> defenders, int capturedValue, boolean defenderToMove) {
        if (attackers.isEmpty()) {
            return 0; // No more pieces to capture with
        }
        
        // Use least valuable attacker
        int leastValuable = Collections.min(attackers);
        attackers.remove(Integer.valueOf(leastValuable));
        
        // Recursive call with sides swapped
        int futureScore = seeRecursive(defenders, attackers, leastValuable, !defenderToMove);
        
        // Current side gains the captured piece but loses what happens next
        return Math.max(0, capturedValue - futureScore);
    }
    
    /**
     * Get all pieces of a given side that can attack a square, ordered by value
     */
    private List<Integer> getAttackerValues(Board board, Square square, Side side) {
        List<Integer> values = new ArrayList<>();
        
        // Check each piece type that could attack this square
        for (PieceType pieceType : PieceType.values()) {
            long attackers = board.squareAttackedByPieceType(square, side, pieceType);
            int count = Long.bitCount(attackers);
            
            // Add each attacker of this type
            for (int i = 0; i < count; i++) {
                values.add(getPieceValue(pieceType));
            }
        }
        
        Collections.sort(values); // Sort by value (least valuable first)
        return values;
    }
    
    /**
     * Get material value of a piece type in centipawns
     */
    public int getPieceValue(PieceType pieceType) {
        switch (pieceType) {
            case PAWN: return PIECE_VALUES[0];
            case KNIGHT: return PIECE_VALUES[1];
            case BISHOP: return PIECE_VALUES[2];
            case ROOK: return PIECE_VALUES[3];
            case QUEEN: return PIECE_VALUES[4];
            case KING: return PIECE_VALUES[5];
            default: return 0;
        }
    }
    
    /**
     * Check if a piece is hanging (can be captured for free or at material loss)
     */
    public boolean isPieceHanging(Board board, Square square) {
        Piece piece = board.getPiece(square);
        if (piece == Piece.NONE) {
            return false;
        }
        
        Side opponent = piece.getPieceSide().flip();
        int seeValue = calculateSEEOnSquare(board, square, opponent);
        
        // Piece is hanging if opponent gains material by capturing it
        return seeValue > 0;
    }
    
    /**
     * Get the material value that would be lost if a piece is captured
     */
    public int getMaterialLoss(Board board, Square square) {
        Piece piece = board.getPiece(square);
        if (piece == Piece.NONE) {
            return 0;
        }
        
        Side opponent = piece.getPieceSide().flip();
        int seeValue = calculateSEEOnSquare(board, square, opponent);
        
        return Math.max(0, seeValue);
    }
}