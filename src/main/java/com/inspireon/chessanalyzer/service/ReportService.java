package com.inspireon.chessanalyzer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.inspireon.chessanalyzer.cache.OpeningCache;
import com.inspireon.chessanalyzer.cache.PlayerStatCache;
import com.inspireon.chessanalyzer.datamanager.GameDataAccess;
import com.inspireon.chessanalyzer.dtos.OpeningStyle;
import com.inspireon.chessanalyzer.dtos.PlayerOverview;
import com.inspireon.chessanalyzer.dtos.PlayerOverview.PlayerOverviewBuilder;

@Service
public class ReportService {
	
	@Autowired
	private StyleAnalyzerService styleAnalyzerService;
	
	public PlayerOverview getPlayerOverview(String playerUsername) throws Exception {
		PlayerOverviewBuilder builder = PlayerOverview.builder();
		OpeningStyle openingStyle = styleAnalyzerService.analyzeOpeningStyle(playerUsername);
		String style = styleAnalyzerService.describeOpeningStyle(openingStyle);
		
		return builder.style(style).build();
	}
}
