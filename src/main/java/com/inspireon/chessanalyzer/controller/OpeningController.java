package com.inspireon.chessanalyzer.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.game.GameResult;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import com.inspireon.chessanalyzer.application.PlayerStatCache;
import com.inspireon.chessanalyzer.dtos.OpeningStat;
import com.inspireon.chessanalyzer.dtos.UserMistake;
import com.inspireon.chessanalyzer.model.ChessOpening;
import com.inspireon.chessanalyzer.model.ChessTempoResult;
import com.inspireon.chessanalyzer.stockfish.ComputerMove;
import com.inspireon.chessanalyzer.stockfish.StockfishClient;
import com.inspireon.chessanalyzer.stockfish.engine.enums.Option;
import com.inspireon.chessanalyzer.stockfish.engine.enums.Query;
import com.inspireon.chessanalyzer.stockfish.engine.enums.QueryType;



@CrossOrigin(origins = { "http://localhost:3000"})
@RestController
public class OpeningController {
	
	private static String game_base_folder = "D:\\workspace\\stockfishjava\\Stockfish-Java\\assets\\games\\";
	
	@RequestMapping("/opening/mistakes")
	public List <UserMistake> getOpeningMistakes(@RequestParam String playerUsername, @RequestParam String openingName) throws Exception {
		List<Game> games = PlayerStatCache.getInstance().getPlayerGames().get(playerUsername + "-" + "chess.com");
		
		if (games == null || games.size() == 0) {
			index(playerUsername);
			games = PlayerStatCache.getInstance().getPlayerGames().get(playerUsername + "-" + "chess.com");
		}
		
		List<UserMistake> mistakes = new ArrayList<UserMistake>();
		StockfishClient client = new StockfishClient.Builder()
                .setInstances(4)
                .setOption(Option.Threads, 4) // Number of threads that Stockfish will use
                .setOption(Option.Minimum_Thinking_Time, 1000) // Minimum thinking time Stockfish will take
                .setOption(Option.Skill_Level, 20) // Stockfish skill level 0-20
                //.setVariant(Variant.BMI2) // Stockfish Variant
                .build();
		int toEvaluate = 5;
		
		for (Game game: games) {    	
			
			if (mistakes.size() >= 2) {
				break;
			}
            game.loadMoveText();
            MoveList moves = game.getHalfMoves();
            Board board = new Board();
            //Replay all the moves from the game and print the final position in FEN format
            int index = 0;
            int currentScore = 0;
            int moveNum = 0;
            
            boolean evaluteForWhite = game.getWhitePlayer().equals(playerUsername);
            
            boolean skip = false;
            ComputerMove lastComputerMove = null;
  
            for (int i = 0; i < moves.size(); i+=2) {
            	if (i >= moves.size()-1) break;
            	//tem
            	if (i > 15) break;
            	
            	Move whiteMove = moves.get(i);
            	Move blackMove = moves.get(i+1);
            	
            	//System.out.println("" + i/2 + "." + whiteMove + " " + blackMove);
            	
            	if (!evaluteForWhite) {
            		board.doMove(whiteMove);
            	}
            	            	             
                List<String> responses = client.getBestMoves(new Query.Builder(QueryType.Best_Move)
                		.setFen(board.getFen())
                		.setMovetime(1000)
                		.build());
                
                List<ComputerMove> comMoves = getComputerMoves(responses, evaluteForWhite);
                
                String fenBefore = board.getFen();
                //move 1
                board.doMove(evaluteForWhite ? whiteMove : blackMove);
                if (i > 3) {
	                List<String> responsesAfter = client.getBestMoves(new Query.Builder(QueryType.Best_Move)
	                		.setFen(board.getFen())
	                		.setMovetime(1000)
	                		.build());
	                
	                List<ComputerMove> comSuggestForOpp = getComputerMoves(responsesAfter, evaluteForWhite);
	                
	                ComputerMove comMove = comMoves.get(comMoves.size()-1);
	                //board.getPiece(new Square());
	                int afterScore = (-1) * comSuggestForOpp.get(comSuggestForOpp.size()-1).getScore();
	                
	                
	                if (comMove.getScore() - afterScore > 100) {
	                	System.out.println("Mistake: " + comMove.getScore() + "         " + afterScore);
	                	System.out.println(fenBefore);
	                	System.out.println("User move: " + (evaluteForWhite ? whiteMove.toString() : blackMove.toString()) + " Computer move: " + comMove.getMove().toString());
	                	mistakes.add(new UserMistake(game.getGameId(), playerUsername, fenBefore, evaluteForWhite ? whiteMove.toString() : blackMove.toString(), comMove.getMove().toString()));
	                }                           
                }
                //move 2
                if (evaluteForWhite) {
                	board.doMove(blackMove);
                }
            }         
            
        }
		return mistakes;
	}
	
	private static List<ComputerMove> getComputerMoves(List<String> moveLines, boolean isWhite) {
    	List<ComputerMove> cmoves = new ArrayList<ComputerMove>();
    	for (String moveLine : moveLines) {    		
    		if (moveLine.contains("bestmove")) {
    			break;
    		}
    		int score = 0;
    		String move = "";
    		String [] tokens = moveLine.split(" ");
    		for (int i = 0; i < tokens.length; i++) {
    			if (tokens[i].equalsIgnoreCase("cp")) {
    				score = Integer.parseInt(tokens[i+1]);	
    			}
    			if (tokens[i].equalsIgnoreCase("pv")) {    			
    				move = tokens[i+1];
    			}
    		}
    		String lineAfterMove = ((moveLine.indexOf(move) + move.length()) < moveLine.length() - 1) ? moveLine.substring(moveLine.indexOf(move) + move.length() + 1) : "";
    		
    		cmoves.add(new ComputerMove(move, lineAfterMove, score, isWhite));
    	}
    	
    	return cmoves;
    }
	
	public static void main(String[] args) throws IOException {
		String jsonString = Files.readString(Path.of(new ClassPathResource("openingbook/response.json").getFile().getAbsolutePath()));
		ObjectMapper mapper = new ObjectMapper();
		ChessTempoResult chessTempoResult = mapper.readValue(jsonString, ChessTempoResult.class);
		Map<String, ChessOpening> openings = new HashMap<String, ChessOpening>();
		for (ChessOpening chessOpening : chessTempoResult.getOpenings()) {
			openings.put(chessOpening.getName().split(":")[0], chessOpening);
		}
		int i = 0;
		for (Entry<String, ChessOpening> entr : openings.entrySet()) {
			System.out.println(++i + entr.getKey());
		}
	}

	@RequestMapping("/opening")
	public TreeSet <OpeningStat> index(@RequestParam String playerUsername) throws Exception {
		TreeSet<OpeningStat> openingStatCache = PlayerStatCache.getInstance().getPlayerOpeningStats().get(playerUsername + "-" + "chess.com");
		if (openingStatCache != null && openingStatCache.size() > 0) {
			return openingStatCache;
		}
		System.out.println("Chess.com player: " + playerUsername);
		String jsonString = Files.readString(Path.of(new ClassPathResource("openingbook/response.json").getFile().getAbsolutePath()));
		ObjectMapper mapper = new ObjectMapper();
		ChessTempoResult chessTempoResult = mapper.readValue(jsonString, ChessTempoResult.class);
		
		Map<String, ChessOpening> openings = new HashMap<String, ChessOpening>();
		
		for (ChessOpening chessOpening : chessTempoResult.getOpenings()) {
			openings.put(chessOpening.getLast_pos().split(" ")[0], chessOpening);
		}
		
		int numOfGames = 0;
		int numOfMonths = 0;
		TreeSet <OpeningStat> openingStats = new TreeSet<OpeningStat>();
		LocalDate localDate = LocalDate.now();
		SortedMap<String, OpeningStat> winrateByOpening = new TreeMap<String, OpeningStat>();
		
		while (true) {
			numOfMonths++;
	    	PgnHolder pgn = null;
	    	if (!new File(game_base_folder + playerUsername + "_" + localDate.getMonthValue() + ".pgn").exists() || localDate.getMonth().equals(LocalDate.now().getMonth())) {
	    		pgn = new PgnHolder(getPgnFile(playerUsername, localDate.getMonthValue()));
	    	} else {
	    		pgn = new PgnHolder(game_base_folder + playerUsername + "_" + localDate.getMonthValue() + ".pgn");
	    	}
	    	
	        pgn.loadPgn(); 
	        
	        for (Game game: pgn.getGames()) {
	        	
	        	game.loadMoveText();
	            MoveList moves = game.getHalfMoves();
	            if (moves.size() < 4) continue;
	            Board board = new Board();
	            ChessOpening thisGameOpening = null;
	            for (int i = 0; i < moves.size(); i++) {
	            	board.doMove(moves.get(i));
	            	if (openings.get(board.getFen().split(" ")[0]) != null)
	            		thisGameOpening = openings.get(board.getFen().split(" ")[0]);
	            	//System.out.println(board.getFen().split(" ")[0]);
	            	if (i == 15) break;
	            }
	            if (thisGameOpening == null) {
	            	thisGameOpening = new ChessOpening();
	            	thisGameOpening.setName("Unknown");
	            }
	            
	            if (winrateByOpening.get(thisGameOpening.getName()) == null) {
	            	OpeningStat openStat = new OpeningStat(thisGameOpening.getName(), 0, 0, 0, null);
	            	winrateByOpening.put(thisGameOpening.getName(), openStat);
	            }
	            boolean isWhite = game.getWhitePlayer().getName().equalsIgnoreCase(playerUsername);
	            if (game.getResult() == GameResult.WHITE_WON && isWhite)
	            {
	            	winrateByOpening.get(thisGameOpening.getName()).addOneWin(isWhite);
	            } else if (game.getResult() == GameResult.BLACK_WON && !isWhite) {
	            	winrateByOpening.get(thisGameOpening.getName()).addOneWin(isWhite);
	            } else if (game.getResult() == GameResult.DRAW) {
	            	winrateByOpening.get(thisGameOpening.getName()).addOneDraw(isWhite);
	            } 
	            winrateByOpening.get(thisGameOpening.getName()).addTotalGames(isWhite);
	            winrateByOpening.get(thisGameOpening.getName()).getGameIds().add(game.getGameId());
	        }
	        
	        numOfGames += pgn.getGames().size();
	        localDate = localDate.minusMonths(1);
	        if (numOfGames >= 1000 || numOfMonths > 24) {
	        	break;
	        }
	        PlayerStatCache.getInstance().reloadGames(playerUsername, "chess.com", pgn.getGames());
		}
		
		winrateByOpening.entrySet().forEach(gameOpenin -> {
        	if (gameOpenin.getValue().getTotalGames() > 2) {
	        	System.out.println("winRate: " + Math.round(gameOpenin.getValue().getWon()*100/gameOpenin.getValue().getTotalGames()) +
	        			"   total games : " + gameOpenin.getValue().getTotalGames() + "   " + gameOpenin.getKey()) ;
	        		openingStats.add(gameOpenin.getValue());
        	}
        });
		PlayerStatCache.getInstance().reloadOpeningStats(playerUsername, "chess.com", openingStats);
		return PlayerStatCache.getInstance().getPlayerOpeningStats().get(playerUsername + "-" + "chess.com");
	}

	
	 private static String getPgnFile(String playerUserName, int month) throws MalformedURLException, IOException {	
    	System.out.println("Calling chess.com api");
    	BufferedInputStream in = new BufferedInputStream(new URL("https://api.chess.com/pub/player/" + playerUserName + "/games/2020/" + month + "/pgn").openStream());
    
		FileOutputStream fileOutputStream = new FileOutputStream(game_base_folder + playerUserName + "_" + month + ".pgn"); 
	    byte dataBuffer[] = new byte[1024];
	    int bytesRead;
	    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
	        fileOutputStream.write(dataBuffer, 0, bytesRead);
	    }
		
    	return game_base_folder + playerUserName + "_" + month + ".pgn";
    }
}
