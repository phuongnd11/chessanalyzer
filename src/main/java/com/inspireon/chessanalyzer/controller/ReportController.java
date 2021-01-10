package com.inspireon.chessanalyzer.controller;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inspireon.chessanalyzer.application.OpeningCache;
import com.inspireon.chessanalyzer.application.PlayerStatCache;
import com.inspireon.chessanalyzer.dtos.OpeningStat;
import com.inspireon.chessanalyzer.dtos.PlayerOverview;
import com.inspireon.chessanalyzer.dtos.PlayerOverview.PlayerOverviewBuilder;
import com.inspireon.chessanalyzer.model.ChessOpening;
import com.inspireon.chessanalyzer.stockfish.ComputerMove;



@CrossOrigin(origins = { "http://localhost:3000"})
@RestController
public class ReportController {
	
	private static String game_base_folder = "D:\\workspace\\stockfishjava\\Stockfish-Java\\assets\\games\\";
	
	@RequestMapping("/report/overview")
	public PlayerOverview getPlayerOverview(@RequestParam(value="playerUsername") String playerUsername) throws Exception {
		PlayerOverviewBuilder builder = PlayerOverview.builder();
		StringBuilder style = new StringBuilder();
		TreeSet<OpeningStat> openingStats = PlayerStatCache.getInstance().getPlayerOpeningStats().get(playerUsername + "-" + "chess.com");
		
		if (openingStats == null || openingStats.size() == 0) {
			new OpeningController().index(playerUsername);
			openingStats = PlayerStatCache.getInstance().getPlayerOpeningStats().get(playerUsername + "-" + "chess.com");
		}
		OpeningStat [] openingStatArray = new OpeningStat[openingStats.size()];
		openingStats.toArray(openingStatArray);
		
		Arrays.sort(openingStatArray, new Comparator<OpeningStat>() {
			@Override
			public int compare(OpeningStat o1, OpeningStat o2) {
				return o2.getTotalWhite().compareTo(o1.getTotalWhite());
			}			
		});
		
		ChessOpening mostPopularAsWhite = OpeningCache.openingMap.get(openingStatArray[0].getName());
		
		OpeningStat mainWeaponAsWhite = null;
		
		OpeningStat mainWeaknessAsWhite = null;
		
		for (int i = 0; i < openingStatArray.length; i++) {
			if (mainWeaponAsWhite == null || openingStatArray[i].getWinRateAsWhite() > mainWeaponAsWhite.getWinRateAsWhite()) {
				if (openingStatArray[i].getTotalWhite() > 10)
					mainWeaponAsWhite = openingStatArray[i];
			}
			if (mainWeaknessAsWhite == null || openingStatArray[i].getWinRateAsWhite() < mainWeaknessAsWhite.getWinRateAsWhite()) {
				if (openingStatArray[i].getTotalWhite() > 10)
					mainWeaknessAsWhite = openingStatArray[i];
			}
		}
		
		if (mostPopularAsWhite.getMoves_lalg().get(0).equals("e2e4")) {
			style.append("You like to get an open position out of the opening as White");
		} else if (mostPopularAsWhite.getMoves_lalg().get(0).equals("d2d4")) {
			style.append("You like to start the game in a closed position as White");
		} else {
			style.append("You like to attack the center from the flank as White");
		}
		
		Arrays.sort(openingStatArray, new Comparator<OpeningStat>() {
			@Override
			public int compare(OpeningStat o1, OpeningStat o2) {
				return o2.getTotalBlack().compareTo(o1.getTotalBlack());
			}			
		});
		
		OpeningStat mainWeaponAsBlack = null;
		
		OpeningStat mainWeaknessAsBlack = null;
		
		for (int i = 0; i < openingStatArray.length; i++) {
			if (mainWeaponAsBlack == null || openingStatArray[i].getWinRateAsBlack() > mainWeaponAsBlack.getWinRateAsBlack()) {
				if (openingStatArray[i].getTotalBlack() > 10) 
					mainWeaponAsBlack = openingStatArray[i];
			}
			if (mainWeaknessAsBlack == null || openingStatArray[i].getWinRateAsBlack() < mainWeaknessAsBlack.getWinRateAsBlack()) {
				if (openingStatArray[i].getTotalBlack() > 10) 
					mainWeaknessAsBlack = openingStatArray[i];
			}
		}
		
		ChessOpening mostPopularAgainstE4 = null;
		ChessOpening mostPopularAgainstD4 = null;
		
		for (int i = 0; i < openingStatArray.length; i++) {
			if (OpeningCache.openingMap.get(openingStatArray[i].getName()).getMoves_lalg().get(0).equals("e2e4")) {
				mostPopularAgainstE4 =  OpeningCache.openingMap.get(openingStatArray[i].getName());
			} else if (OpeningCache.openingMap.get(openingStatArray[i].getName()).getMoves_lalg().get(0).equals("d2d4")) {
				mostPopularAgainstD4 =  OpeningCache.openingMap.get(openingStatArray[i].getName());
			}
		}
		
		boolean semi = false;
		if (!mostPopularAgainstE4.getMoves_lalg().get(1).equals("e7e5")) {
			style.append(" and semi-open");
			semi = true;
		}
		if (!mostPopularAgainstD4.getMoves_lalg().get(1).equals("d7d5")) {
			style.append(" and semi-closed");
			semi = true;
		}
		if (semi) {
			style.append(" position as Black");
		} else {
			style.append(" and symmetrical position as Black");
		}
		
		if (mainWeaponAsWhite != null) {
			style.append(". Your main weapon is " + mainWeaponAsWhite.getName());
		} if (mainWeaponAsBlack != null) {
			if (style.toString().contains("Your main weapon is")) {
				style.append(" and " + mainWeaponAsBlack.getName());
			} else {
				style.append(". Your main weapon is " + mainWeaponAsBlack.getName());
			}
		}
		if (mainWeaponAsWhite != null || mainWeaponAsBlack != null) {
			style.append(".");
		}
		
		if (mainWeaknessAsWhite != null) {
			style.append(" Your weakness lies in " + mainWeaknessAsWhite.getName());
		} if (mainWeaknessAsBlack != null) {
			if (style.toString().contains("Your weakness lies in")) {
				style.append(" and " + mainWeaknessAsBlack.getName());
			} else {
				style.append(". Your weakness lies in " + mainWeaknessAsBlack.getName());
			}
		}
		if (mainWeaknessAsWhite != null || mainWeaknessAsBlack != null) {
			style.append(".");
		}
		
		return builder.style(style.toString()).build();
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
