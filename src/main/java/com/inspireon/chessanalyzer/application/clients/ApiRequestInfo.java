package com.inspireon.chessanalyzer.application.clients;

import java.io.InputStream;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class ApiRequestInfo {
	private String threadId;
	private String url;
	private InputStream inputStream;
	private boolean done;
}
