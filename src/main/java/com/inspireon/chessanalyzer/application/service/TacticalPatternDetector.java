package com.inspireon.chessanalyzer.application.service;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.inspireon.chessanalyzer.common.enums.TacticalTheme;
import com.inspireon.chessanalyzer.stockfish.ComputerMove;
import com.inspireon.chessanalyzer.web.dtos.TacticalEvidence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * PV-based Tactical Pattern Detector using engine analysis and SEE
 * Based on proven chess analysis techniques for accurate tactical theme identification
 */
@Service
@Slf4j
public class TacticalPatternDetector {
    
    @Autowired
    private StaticExchangeEvaluator seeEvaluator;
    
    private static final int MISTAKE_THRESHOLD_CP = 150;
    private static final int MAJOR_BLUNDER_THRESHOLD_CP = 500;
    private static final double MIN_CONFIDENCE = 0.6;
    
    /**
     * Internal class to hold tactical analysis results
     */
    private static class TacticalAnalysis {
        TacticalTheme theme;
        TacticalEvidence evidence;
        double confidence;
        
        TacticalAnalysis(TacticalTheme theme, TacticalEvidence evidence, double confidence) {
            this.theme = theme;
            this.evidence = evidence;
            this.confidence = confidence;
        }
    }
    
    /**
     * Main entry point for tactical theme detection using PV-based analysis
     */
    public TacticalTheme detectTacticalTheme(Board beforeBoard, Move userMove, Move bestMove, 
                                           String gamePhase, int scoreDrop) {
        
        log.debug("PV-based tactical analysis: userMove={}, bestMove={}, scoreDrop={}", 
            userMove, bestMove, scoreDrop);
        
        // Gate: Only analyze significant mistakes
        if (scoreDrop < MISTAKE_THRESHOLD_CP) {
            log.debug("Score drop {} below threshold {}, skipping analysis", scoreDrop, MISTAKE_THRESHOLD_CP);
            return TacticalTheme.UNKNOWN;
        }
        
        log.info("Analyzing mistake: scoreDrop={} (above threshold {})", scoreDrop, MISTAKE_THRESHOLD_CP);
        
        Board afterUserMove = beforeBoard.clone();
        afterUserMove.doMove(userMove);
        
        Side userSide = beforeBoard.getSideToMove();
        
        // Create a mock PV with the best move as opponent's reply
        List<Move> principalVariation = createMockPV(afterUserMove, bestMove);
        
        log.debug("Analyzing with PV: {}", principalVariation);
        
        // Run motif detectors in order of priority
        TacticalAnalysis analysis = runMotifDetectors(beforeBoard, afterUserMove, userMove, 
                                                     principalVariation, userSide, scoreDrop, gamePhase);
        
        if (analysis != null && analysis.confidence >= MIN_CONFIDENCE) {
            log.info("Detected tactical theme: {} with confidence {}", analysis.theme, analysis.confidence);
            return analysis.theme;
        }
        
        // Fallback based on score drop magnitude
        log.info("No high-confidence pattern detected, using fallback for scoreDrop={}, gamePhase={}", scoreDrop, gamePhase);
        
        if (scoreDrop >= MAJOR_BLUNDER_THRESHOLD_CP) {
            log.info("Fallback: TACTICAL_BLUNDER (scoreDrop >= {})", MAJOR_BLUNDER_THRESHOLD_CP);
            return TacticalTheme.TACTICAL_BLUNDER;
        } else if (gamePhase.equals("OPENING")) {
            log.info("Fallback: OPENING_PRINCIPLE");
            return TacticalTheme.OPENING_PRINCIPLE;
        } else if (gamePhase.equals("ENDGAME")) {
            log.info("Fallback: ENDGAME_TECHNIQUE");
            return TacticalTheme.ENDGAME_TECHNIQUE;
        } else {
            log.info("Fallback: POSITIONAL_BLUNDER");
            return TacticalTheme.POSITIONAL_BLUNDER;
        }
    }
    
    /**
     * Create a mock PV from the best move (simulating engine PV)
     */
    private List<Move> createMockPV(Board afterUserMove, Move bestMove) {
        List<Move> pv = new ArrayList<>();
        
        try {
            // First move in PV is the best reply to user's move
            pv.add(new Move(bestMove.getFrom(), bestMove.getTo()));
            
            // For now, we only use the first move of PV
            // In a real implementation, you'd get the full PV from the engine
            
        } catch (Exception e) {
            log.warn("Error creating mock PV: {}", e.getMessage());
        }
        
        return pv;
    }
    
    /**
     * Run all motif detectors in priority order
     */
    private TacticalAnalysis runMotifDetectors(Board beforeBoard, Board afterUserMove, Move userMove,
                                             List<Move> pv, Side userSide, int scoreDrop, String gamePhase) {
        
        if (pv.isEmpty()) {
            log.warn("Empty PV, cannot run motif detectors");
            return null;
        }
        
        Move opponentReply = pv.get(0);
        log.debug("Running motif detectors with opponentReply: {}", opponentReply);
        
        // Run detectors in order of priority (stop at first strong match)
        
        // 0. Mate threats
        TacticalAnalysis mateAnalysis = detectMateThreats(beforeBoard, afterUserMove, opponentReply, userSide, scoreDrop);
        if (mateAnalysis != null) return mateAnalysis;
        
        // 1. Hanging pieces
        TacticalAnalysis hangingAnalysis = detectHangingPiece(beforeBoard, afterUserMove, userMove, userSide);
        if (hangingAnalysis != null) return hangingAnalysis;
        
        // 2. Forks
        TacticalAnalysis forkAnalysis = detectFork(afterUserMove, opponentReply, userSide, scoreDrop);
        if (forkAnalysis != null) return forkAnalysis;
        
        // 3. Skewers
        TacticalAnalysis skewerAnalysis = detectSkewer(afterUserMove, opponentReply, userSide);
        if (skewerAnalysis != null) return skewerAnalysis;
        
        // 4. Pins
        TacticalAnalysis pinAnalysis = detectPin(beforeBoard, afterUserMove, opponentReply, userSide);
        if (pinAnalysis != null) return pinAnalysis;
        
        // 5. Back rank weaknesses
        TacticalAnalysis backRankAnalysis = detectBackRankWeakness(afterUserMove, opponentReply, userSide);
        if (backRankAnalysis != null) return backRankAnalysis;
        
        return null; // No strong pattern detected
    }
    
    /**
     * 0. Detect mate threats / mating net
     */
    private TacticalAnalysis detectMateThreats(Board beforeBoard, Board afterUserMove, Move opponentReply, 
                                             Side userSide, int scoreDrop) {
        
        // Apply opponent's reply to see if it creates mate threats
        Board afterOpponentReply = afterUserMove.clone();
        afterOpponentReply.doMove(opponentReply);
        
        // Check if opponent now has mate threats
        if (afterOpponentReply.isMated()) {
            TacticalEvidence evidence = new TacticalEvidence.Builder()
                .opponentReply(opponentReply.toString())
                .mateIn(1)
                .confidence(0.95)
                .build();
                
            return new TacticalAnalysis(TacticalTheme.CHECKMATE_MISSED, evidence, 0.95);
        }
        
        // Check for mate-in-N patterns by looking at severe score drops
        if (scoreDrop > 800) {
            TacticalEvidence evidence = new TacticalEvidence.Builder()
                .opponentReply(opponentReply.toString())
                .mateIn(2)
                .confidence(0.8)
                .build();
                
            return new TacticalAnalysis(TacticalTheme.BACK_RANK_MATE, evidence, 0.8);
        }
        
        return null;
    }
    
    /**
     * 1. Detect hanging pieces using SEE
     */
    private TacticalAnalysis detectHangingPiece(Board beforeBoard, Board afterUserMove, Move userMove, Side userSide) {
        
        // Check if the moved piece is now hanging
        Square movedTo = userMove.getTo();
        if (seeEvaluator.isPieceHanging(afterUserMove, movedTo)) {
            int materialLoss = seeEvaluator.getMaterialLoss(afterUserMove, movedTo);
            
            TacticalEvidence evidence = new TacticalEvidence.Builder()
                .square(movedTo.toString())
                .seeGainCp(materialLoss)
                .confidence(0.9)
                .build();
                
            return new TacticalAnalysis(TacticalTheme.HANGING_PIECE, evidence, 0.9);
        }
        
        // Check if any other user pieces are now hanging due to discovered attacks, etc.
        for (Square square : Square.values()) {
            Piece piece = afterUserMove.getPiece(square);
            if (piece != Piece.NONE && piece.getPieceSide() == userSide) {
                if (seeEvaluator.isPieceHanging(afterUserMove, square)) {
                    int materialLoss = seeEvaluator.getMaterialLoss(afterUserMove, square);
                    if (materialLoss > 100) { // Only report significant material loss
                        
                        TacticalEvidence evidence = new TacticalEvidence.Builder()
                            .square(square.toString())
                            .seeGainCp(materialLoss)
                            .confidence(0.85)
                            .build();
                            
                        return new TacticalAnalysis(TacticalTheme.HANGING_PIECE, evidence, 0.85);
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * 2. Detect forks (double attacks)
     */
    private TacticalAnalysis detectFork(Board afterUserMove, Move opponentReply, Side userSide, int scoreDrop) {
        
        // Apply opponent's reply
        Board afterOpponentReply = afterUserMove.clone();
        afterOpponentReply.doMove(opponentReply);
        
        Square opponentPieceSquare = opponentReply.getTo();
        Piece attackingPiece = afterOpponentReply.getPiece(opponentPieceSquare);
        
        if (attackingPiece == Piece.NONE) {
            return null;
        }
        
        // Count valuable targets attacked by the opponent's piece
        List<String> attackedTargets = new ArrayList<>();
        boolean attacksKing = false;
        
        for (Square square : Square.values()) {
            Piece targetPiece = afterOpponentReply.getPiece(square);
            if (targetPiece != Piece.NONE && targetPiece.getPieceSide() == userSide) {
                
                // Check if this square is attacked by the opponent's new piece
                if (isSquareAttackedByPiece(afterOpponentReply, square, opponentPieceSquare, attackingPiece)) {
                    PieceType targetType = targetPiece.getPieceType();
                    
                    if (targetType == PieceType.KING) {
                        attacksKing = true;
                        attackedTargets.add("K");
                    } else if (seeEvaluator.getPieceValue(targetType) >= 300) { // Knight, Bishop, Rook, Queen
                        attackedTargets.add(targetType.name().substring(0, 1));
                    }
                }
            }
        }
        
        // Fork: attacks 2+ valuable pieces, or King + another piece
        if (attackedTargets.size() >= 2 || (attacksKing && attackedTargets.size() >= 2)) {
            
            TacticalEvidence evidence = new TacticalEvidence.Builder()
                .opponentReply(opponentReply.toString())
                .targets(attackedTargets)
                .byPiece(attackingPiece.getPieceType().name())
                .confidence(0.85)
                .build();
                
            return new TacticalAnalysis(TacticalTheme.FORK, evidence, 0.85);
        }
        
        return null;
    }
    
    // === STUB IMPLEMENTATIONS FOR OTHER DETECTORS ===
    // These would be fully implemented in production
    
    private TacticalAnalysis detectSkewer(Board afterUserMove, Move opponentReply, Side userSide) {
        // TODO: Implement skewer detection
        return null;
    }
    
    private TacticalAnalysis detectPin(Board beforeBoard, Board afterUserMove, Move opponentReply, Side userSide) {
        // TODO: Implement pin detection
        return null;
    }
    
    private TacticalAnalysis detectBackRankWeakness(Board afterUserMove, Move opponentReply, Side userSide) {
        // TODO: Implement back rank weakness detection
        return null;
    }
    
    // === GEOMETRIC HELPER METHODS ===
    
    /**
     * Helper method to check if a square is attacked by a specific piece
     */
    private boolean isSquareAttackedByPiece(Board board, Square targetSquare, Square attackerSquare, Piece attackingPiece) {
        PieceType pieceType = attackingPiece.getPieceType();
        
        switch (pieceType) {
            case KNIGHT:
                return isKnightAttack(attackerSquare, targetSquare);
            case BISHOP:
                return isDiagonalAttack(board, attackerSquare, targetSquare);
            case ROOK:
                return isRankFileAttack(board, attackerSquare, targetSquare);
            case QUEEN:
                return isDiagonalAttack(board, attackerSquare, targetSquare) || 
                       isRankFileAttack(board, attackerSquare, targetSquare);
            case PAWN:
                return isPawnAttack(attackerSquare, targetSquare, attackingPiece.getPieceSide());
            case KING:
                return isAdjacentSquare(attackerSquare, targetSquare);
            default:
                return false;
        }
    }
    
    private boolean isKnightAttack(Square from, Square to) {
        int fileDistance = Math.abs(from.getFile().ordinal() - to.getFile().ordinal());
        int rankDistance = Math.abs(from.getRank().ordinal() - to.getRank().ordinal());
        return (fileDistance == 2 && rankDistance == 1) || (fileDistance == 1 && rankDistance == 2);
    }
    
    private boolean isDiagonalAttack(Board board, Square from, Square to) {
        int fileDistance = Math.abs(from.getFile().ordinal() - to.getFile().ordinal());
        int rankDistance = Math.abs(from.getRank().ordinal() - to.getRank().ordinal());
        
        if (fileDistance != rankDistance || fileDistance == 0) {
            return false;
        }
        
        int fileDirection = Integer.compare(to.getFile().ordinal(), from.getFile().ordinal());
        int rankDirection = Integer.compare(to.getRank().ordinal(), from.getRank().ordinal());
        
        return isPathClear(board, from, to, fileDirection, rankDirection);
    }
    
    private boolean isRankFileAttack(Board board, Square from, Square to) {
        int fileDistance = Math.abs(from.getFile().ordinal() - to.getFile().ordinal());
        int rankDistance = Math.abs(from.getRank().ordinal() - to.getRank().ordinal());
        
        if (fileDistance != 0 && rankDistance != 0) {
            return false;
        }
        
        if (fileDistance == 0 && rankDistance == 0) {
            return false;
        }
        
        int fileDirection = Integer.compare(to.getFile().ordinal(), from.getFile().ordinal());
        int rankDirection = Integer.compare(to.getRank().ordinal(), from.getRank().ordinal());
        
        return isPathClear(board, from, to, fileDirection, rankDirection);
    }
    
    private boolean isPathClear(Board board, Square from, Square to, int fileDirection, int rankDirection) {
        int currentFile = from.getFile().ordinal() + fileDirection;
        int currentRank = from.getRank().ordinal() + rankDirection;
        
        while (currentFile != to.getFile().ordinal() || currentRank != to.getRank().ordinal()) {
            Square currentSquare = Square.encode(
                com.github.bhlangonijr.chesslib.Rank.allRanks[currentRank],
                com.github.bhlangonijr.chesslib.File.allFiles[currentFile]
            );
            
            if (board.getPiece(currentSquare) != Piece.NONE) {
                return false;
            }
            
            currentFile += fileDirection;
            currentRank += rankDirection;
        }
        
        return true;
    }
    
    private boolean isPawnAttack(Square from, Square to, Side side) {
        int fileDistance = Math.abs(from.getFile().ordinal() - to.getFile().ordinal());
        int rankDirection = (side == Side.WHITE) ? 1 : -1;
        int expectedRank = from.getRank().ordinal() + rankDirection;
        
        return fileDistance == 1 && to.getRank().ordinal() == expectedRank;
    }
    
    private boolean isAdjacentSquare(Square from, Square to) {
        int fileDistance = Math.abs(from.getFile().ordinal() - to.getFile().ordinal());
        int rankDistance = Math.abs(from.getRank().ordinal() - to.getRank().ordinal());
        return fileDistance <= 1 && rankDistance <= 1 && (fileDistance + rankDistance > 0);
    }
}