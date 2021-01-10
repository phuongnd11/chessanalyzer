package com.inspireon.chessanalyzer.application;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspireon.chessanalyzer.model.ChessOpening;
import com.inspireon.chessanalyzer.model.ChessTempoResult;

public class OpeningCache {
	
	public static List<ChessOpening> openings;
	
	public static Map<String, ChessOpening> openingMap;

	static {
		String jsonString;
		try {
			jsonString = Files.readString(Path.of(new ClassPathResource("openingbook/response.json").getFile().getAbsolutePath()));
		
			ObjectMapper mapper = new ObjectMapper();

			ChessTempoResult chessTempoResult = mapper.readValue(jsonString, ChessTempoResult.class);
			openings = chessTempoResult.getOpenings();
			openingMap = new HashMap<String, ChessOpening>();
			for (ChessOpening opening : openings) {
				openingMap.put(opening.getName(), opening);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}