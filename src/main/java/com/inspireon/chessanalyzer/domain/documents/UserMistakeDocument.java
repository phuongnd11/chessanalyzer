package com.inspireon.chessanalyzer.domain.documents;

import com.inspireon.chessanalyzer.common.enums.TacticalTheme;
import com.inspireon.chessanalyzer.web.dtos.TacticalEvidence;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "user_mistakes")
public class UserMistakeDocument {

    @Id
    private String id;

    @Indexed
    @Field("username")
    private String username;

    @Indexed
    @Field("game_id")
    private String gameId;

    @Field("date_played")
    private LocalDate datePlayed;

    @Field("fen")
    private String fen;

    @Field("move_number")
    private Integer moveNumber;

    @Indexed
    @Field("game_phase")
    private String gamePhase;

    @Field("user_move")
    private String userMove;

    @Field("computer_move")
    private String computerMove;

    @Indexed
    @Field("score_drop")
    private Integer scoreDrop;

    @Indexed
    @Field("tactical_theme")
    private TacticalTheme tacticalTheme;

    @Field("evidence")
    private TacticalEvidence evidence;

    @Field("player_color")
    private String playerColor;

    @Field("white_player")
    private String whitePlayer;

    @Field("black_player")
    private String blackPlayer;

    @Field("white_elo")
    private Integer whiteElo;

    @Field("black_elo")
    private Integer blackElo;

    @Field("game_result")
    private String gameResult;

    @Field("time_control")
    private String timeControl;

    @Indexed
    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("puzzle_generated")
    private Boolean puzzleGenerated;

    @Field("puzzle_id")
    private String puzzleId;

    @Field("is_solved")
    private Boolean isSolved;

    @Field("solve_attempts")
    private Integer solveAttempts;

    @Field("solved_at")
    private LocalDateTime solvedAt;

    @Field("solved_by")
    private String solvedBy;

    public UserMistakeDocument() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.puzzleGenerated = false;
        this.isSolved = false;
        this.solveAttempts = 0;
    }

    public UserMistakeDocument(String username, String gameId, LocalDate datePlayed, String fen, 
                              String userMove, String computerMove) {
        this();
        this.username = username;
        this.gameId = gameId;
        this.datePlayed = datePlayed;
        this.fen = fen;
        this.userMove = userMove;
        this.computerMove = computerMove;
    }

}