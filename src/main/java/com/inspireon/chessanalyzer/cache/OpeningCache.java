package com.inspireon.chessanalyzer.cache;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspireon.chessanalyzer.AppConfig;
import com.inspireon.chessanalyzer.io.OpeningFileAccess;
import com.inspireon.chessanalyzer.model.ChessOpening;
import com.inspireon.chessanalyzer.model.ChessTempoResult;

import lombok.Getter;

@Getter
@Component
public class OpeningCache {

	@Autowired
	private AppConfig appConfig;
	
	@Autowired
	private OpeningFileAccess openingFileAccess;
	
	private List<ChessOpening> openings;
	
	private Map<String, ChessOpening> openingMap;

	@PostConstruct
	public void init() {
		ChessTempoResult chessTempoResult = openingFileAccess.getOpenings();
		openings = chessTempoResult.getOpenings();
		openingMap = new HashMap<String, ChessOpening>();
		for (ChessOpening opening : openings) {
			openingMap.put(opening.getName(), opening);
		}
	}
}