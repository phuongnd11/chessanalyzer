package com.inspireon.chessanalyzer.application.clients;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;

import com.inspireon.chessanalyzer.AppConfig;
import com.inspireon.chessanalyzer.web.dtos.ChessApiResponse;
import com.inspireon.chessanalyzer.web.dtos.ChessApiRequest;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ChessApiClient {
    
    
    @Autowired
    private AppConfig appConfig;
    
    private ExecutorService executor;
    private RestTemplate restTemplate;
    private CloseableHttpClient httpClient;
    
    @PostConstruct
    public void init() {
        executor = Executors.newFixedThreadPool(appConfig.getChesscomRequestsLimit());
        
        // Create connection pool
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(20); // Max total connections
        connectionManager.setDefaultMaxPerRoute(10); // Max connections per route
        
        // Create HTTP client with connection pooling
        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();
        
        // Create RestTemplate with pooled HTTP client
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setConnectTimeout(5000); // 5 seconds
        requestFactory.setConnectionRequestTimeout(5000); // 5 seconds
        restTemplate = new RestTemplate(requestFactory);
        
        log.info("ChessApiClient initialized with connection pool (max total: 20, max per route: 10)");
    }
    
    @PreDestroy
    public void cleanup() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
            if (executor != null) {
                executor.shutdown();
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            }
            log.info("ChessApiClient cleanup completed");
        } catch (Exception e) {
            log.error("Error during ChessApiClient cleanup: {}", e.getMessage(), e);
        }
    }
    
    public List<ChessApiResponse> getBestMoves(String fen) throws Exception {
        return getBestMoves(fen, appConfig.getChessApiDefaultVariants(), 
                           appConfig.getChessApiDefaultDepth(), 
                           appConfig.getChessApiDefaultThinkingTime());
    }
    
    public List<ChessApiResponse> getBestMoves(String fen, int variants, int depth, int maxThinkingTime) throws Exception {
        ChessApiRequest request = new ChessApiRequest(fen, variants, depth, maxThinkingTime);
        
        Future<List<ChessApiResponse>> future = executor.submit(() -> requestAnalysis(request));
        
        long timeout = appConfig.getChesscomResponseTimeout();
        log.info("Requesting chess analysis for FEN: {} with depth: {}, variants: {}, timeout: {} ms", 
                 fen, depth, variants, timeout);
        
        try {
            List<ChessApiResponse> responses = future.get(timeout, TimeUnit.MILLISECONDS);
            log.info("Received {} chess analysis responses", responses.size());
            return responses;
        } catch (Exception e) {
            log.error("Failed to get chess analysis for FEN {}: {}", fen, e.toString(), e);
            throw e;
        }
    }
    
    private List<ChessApiResponse> requestAnalysis(ChessApiRequest request) {
        try {
            log.debug("Calling chess-api.com: {}", appConfig.getChessApiUrl());
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<ChessApiRequest> entity = new HttpEntity<>(request, headers);
            
            // Make request using connection pool
            ResponseEntity<ChessApiResponse> response = restTemplate.exchange(
                appConfig.getChessApiUrl(), 
                HttpMethod.POST, 
                entity, 
                ChessApiResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<ChessApiResponse> responses = new ArrayList<>();
                responses.add(response.getBody());
                
                log.info("Successfully received chess analysis response");
                return responses;
                
            } else {
                log.error("HTTP error response: {}", response.getStatusCode());
                return new ArrayList<>();
            }
            
        } catch (Exception e) {
            log.error("Failed to request chess analysis: {}", e.toString(), e);
            return new ArrayList<>();
        }
    }
}