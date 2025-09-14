package com.inspireon.chessanalyzer.common.enums;

public enum TacticalTheme {
    FORK("Fork - attacking multiple pieces simultaneously"),
    PIN("Pin - restricting piece movement"),
    SKEWER("Skewer - forcing valuable piece to move and exposing lesser piece"),
    DISCOVERED_ATTACK("Discovered Attack - moving piece reveals attack from another piece"),
    DOUBLE_ATTACK("Double Attack - attacking multiple targets"),
    BACK_RANK_MATE("Back Rank Mate - checkmate threat on back rank"),
    DEFLECTION("Deflection - forcing piece away from important duty"),
    DECOY("Decoy - luring piece to unfavorable square"),
    HANGING_PIECE("Hanging Piece - leaving piece undefended"),
    TACTICAL_BLUNDER("Tactical Blunder - missing immediate tactical threat"),
    SACRIFICE_MISSED("Sacrifice Missed - failing to make winning sacrifice"),
    COUNTER_ATTACK_MISSED("Counter Attack Missed - missing counter-attacking opportunity"),
    CHECKMATE_MISSED("Checkmate Missed - failing to deliver checkmate"),
    MATERIAL_LOSS("Material Loss - losing material unnecessarily"),
    POSITIONAL_BLUNDER("Positional Blunder - poor positional understanding"),
    TIME_TROUBLE("Time Trouble - mistake due to time pressure"),
    CALCULATION_ERROR("Calculation Error - miscalculating variations"),
    OPENING_PRINCIPLE("Opening Principle - violating opening principles"),
    ENDGAME_TECHNIQUE("Endgame Technique - poor endgame understanding"),
    UNKNOWN("Unknown - theme not identified");
    
    private final String description;
    
    TacticalTheme(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}