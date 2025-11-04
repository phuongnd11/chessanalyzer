package com.inspireon.chessanalyzer.domain.documents;

import com.inspireon.chessanalyzer.common.enums.TacticalTheme;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "generated_puzzles")
public class GeneratedPuzzleDocument {

    @Id
    private String id;

    @Field("title")
    private String title;

    @Indexed
    @Field("difficulty")
    private String difficulty;

    @Indexed
    @Field("rating")
    private Integer rating;

    @Field("fen")
    private String fen;

    @Field("player_to_move")
    private String playerToMove;

    @Field("original_user_move")
    private OriginalUserMove originalUserMove;

    @Field("computer_lines")
    private List<ComputerLine> computerLines;

    @Field("position_evaluations")
    private List<PositionEvaluation> positionEvaluations;

    @Indexed
    @Field("tactical_theme")
    private TacticalTheme tacticalTheme;

    @Field("secondary_themes")
    private List<TacticalTheme> secondaryThemes;

    @Indexed
    @Field("game_phase")
    private String gamePhase;

    @Field("piece_count")
    private Integer pieceCount;

    @Field("source_type")
    private String sourceType;

    @Indexed
    @Field("source_mistake_id")
    private String sourceMistakeId;

    @Field("source_game_id")
    private String sourceGameId;

    @Field("source_username")
    private String sourceUsername;

    @Field("stats")
    private PuzzleStats stats;

    @Indexed
    @Field("is_published")
    private Boolean isPublished;

    @Field("is_verified")
    private Boolean isVerified;

    @Indexed
    @Field("quality_score")
    private Integer qualityScore;

    @Indexed
    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("created_by")
    private String createdBy;

    @Field("tags")
    private List<String> tags;

    public GeneratedPuzzleDocument() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isPublished = false;
        this.isVerified = false;
        this.stats = new PuzzleStats();
    }

    @Data
    public static class OriginalUserMove {
        private String move;
        private Integer evaluation;
        private String evaluationType;
        private String description;

        public OriginalUserMove() {}

        public OriginalUserMove(String move, Integer evaluation, String evaluationType, String description) {
            this.move = move;
            this.evaluation = evaluation;
            this.evaluationType = evaluationType;
            this.description = description;
        }
    }

    @Data
    public static class ComputerLine {
        private Integer lineNumber;
        private List<String> moves;
        private Integer evaluation;
        private String evaluationType;
        private Integer depth;
        private Boolean isMainSolution;
        private String explanation;
        private List<Variation> variations;
    }

    @Data
    public static class Variation {
        private Integer fromMoveIndex;
        private List<String> moves;
        private Integer evaluation;
        private String evaluationType;
    }

    @Data
    public static class PositionEvaluation {
        private String afterMove;
        private String fen;
        private Integer evaluation;
        private String evaluationType;
        private String bestMove;
        private String comment;
    }

    @Data
    public static class PuzzleStats {
        private Integer totalAttempts = 0;
        private Integer correctSolves = 0;
        private Double averageTime = 0.0;
        private Double successRate = 0.0;
        private List<CommonWrongMove> commonWrongMoves;
        private List<UserRating> userRatings;
    }

    @Data
    public static class CommonWrongMove {
        private String move;
        private Integer count;
        private Integer evaluation;
    }

    @Data
    public static class UserRating {
        private String username;
        private Integer rating;
        private String feedback;
        private LocalDateTime date;
    }
}