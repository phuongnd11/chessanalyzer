package com.inspireon.chessanalyzer.stockfish;

public class ComputerMove {
	
	private String move;
	private String lineAfterMove;
	private Integer score;
	private boolean isWhite;
	
	public ComputerMove(String move, String lineAfterMove, Integer score, boolean isWhite) {
		super();
		this.move = move;
		this.lineAfterMove = lineAfterMove;
		this.score = score;
		this.isWhite = isWhite;
	}
	
	public String getMove() {
		return move;
	}
	public void setMove(String move) {
		this.move = move;
	}
	public String getLineAfterMove() {
		return lineAfterMove;
	}
	public void setLineAfterMove(String lineAfterMove) {
		this.lineAfterMove = lineAfterMove;
	}
	public Integer getScore() {
		return score;
	}
	public void setScore(Integer score) {
		this.score = score;
	}
	public boolean isWhite() {
		return isWhite;
	}
	public void setWhite(boolean isWhite) {
		this.isWhite = isWhite;
	}

	@Override
	public String toString() {
		return "ComputerMove [move=" + move + ", lineAfterMove=" + lineAfterMove + ", score=" + score + ", isWhite="
				+ isWhite + "]";
	}

}
