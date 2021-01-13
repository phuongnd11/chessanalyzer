package com.inspireon.chessanalyzer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Getter
@Configuration
public class AppConfig {
	@Value("${chessanalyzer.openingbook.path}")
	private String openingBookPath;
	
	@Value("${chessanalyzer.gamebase.folder}")
	private String gameBaseFolder;
	
	
}
