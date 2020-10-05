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
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.game.GameResult;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import com.inspireon.chessanalyzer.dtos.OpeningStat;
import com.inspireon.chessanalyzer.model.ChessOpening;
import com.inspireon.chessanalyzer.model.ChessTempoResult;

@RestController
public class OpeningController {
	
	private static String game_base_folder = "D:\\workspace\\stockfishjava\\Stockfish-Java\\assets\\games\\";

	@RequestMapping("/opening")
	public List<OpeningStat> index(@RequestParam String playerUsername) throws Exception {
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
		List <OpeningStat> openingStats = new ArrayList<OpeningStat>();
		LocalDate localDate = LocalDate.now();
		SortedMap<String, List<Integer>> winrateByOpening = new TreeMap<String, List<Integer>>();
		
		while (true) {
			numOfMonths++;
	    	PgnHolder pgn = null;
	    	if (!new File(game_base_folder + playerUsername + "_" + localDate.getMonthValue() + ".pgn").exists() || localDate.getMonth().equals(LocalDate.now().getMonth())) {
	    		pgn = new PgnHolder(getPgnFile(playerUsername, localDate.getMonthValue()));
	    	} else {
	    		pgn = new PgnHolder(game_base_folder + playerUsername + "_" + localDate.getMonthValue() + ".pgn");
	    	}
	    	
	    	
	    	//Map<String, String> gameIdToOpening = new HashMap<String, String>();
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
	            	//System.out.println(game.getWhitePlayer() +  "-" + game.getBlackPlayer());
	            	thisGameOpening = new ChessOpening();
	            	thisGameOpening.setName("Unknown");
	            }
	            //gameIdToOpening.put(game.getGameId(), thisGameOpening.getName());
	            if (winrateByOpening.get(thisGameOpening.getName()) == null) {
	            	List<Integer> tem = new ArrayList<Integer>();
	            	tem.add(0);
	            	tem.add(0);
	            	tem.add(0);
	            	winrateByOpening.put(thisGameOpening.getName(), tem);
	            }
	
	            if ((game.getResult() == GameResult.WHITE_WON && game.getWhitePlayer().getName().equalsIgnoreCase(playerUsername))
	            		|| game.getResult() == GameResult.BLACK_WON && game.getBlackPlayer().getName().equalsIgnoreCase(playerUsername))
	            {
	            	winrateByOpening.get(thisGameOpening.getName()).set(0, winrateByOpening.get(thisGameOpening.getName()).get(0)+1);
	            } else if (game.getResult() == GameResult.DRAW) {
	            	winrateByOpening.get(thisGameOpening.getName()).set(1, winrateByOpening.get(thisGameOpening.getName()).get(1)+1);
	            } 
	            winrateByOpening.get(thisGameOpening.getName()).set(2, winrateByOpening.get(thisGameOpening.getName()).get(2)+1);  	
	        }
    
	        numOfGames += pgn.getGames().size();
	        localDate = localDate.minusMonths(1);
	        if (numOfGames >= 1000 || numOfMonths > 8) {
	        	break;
	        }
		}
		
		winrateByOpening.entrySet().forEach(gameOpenin -> {
        	if (gameOpenin.getValue().get(2) > 1) {
	        	openingStats.add(new OpeningStat(gameOpenin.getKey(), Math.round(gameOpenin.getValue().get(0)*100/gameOpenin.getValue().get(2)), gameOpenin.getValue().get(2)));
	        	System.out.println("winRate: " + gameOpenin.getValue().get(0) +
	        			"   total games : " + gameOpenin.getValue().get(2) + "   " + gameOpenin.getKey()) ;
        	}
        });
		
		return openingStats;
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
