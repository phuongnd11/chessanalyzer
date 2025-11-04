package com.inspireon.chessanalyzer.config;

import com.inspireon.chessanalyzer.common.enums.TacticalTheme;
import com.inspireon.chessanalyzer.domain.documents.UserMistakeDocument;
import com.inspireon.chessanalyzer.application.service.UserMistakeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.bson.Document;

import java.time.LocalDate;

@Component
public class MongoConnectionTest implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MongoConnectionTest.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserMistakeService userMistakeService;

    @Override
    public void run(String... args) throws Exception {
        try {
            logger.info("Testing MongoDB connection...");
            
            // Test connection with ping
            Document pingResult = mongoTemplate.getDb().runCommand(new Document("ping", 1));
            logger.info("MongoDB ping result: {}", pingResult.toJson());
            
            // Test creating a sample document
            UserMistakeDocument testMistake = new UserMistakeDocument();
            testMistake.setUsername("testuser");
            testMistake.setGameId("test_game_123");
            testMistake.setDatePlayed(LocalDate.now());
            testMistake.setFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            testMistake.setUserMove("e4");
            testMistake.setComputerMove("d4");
            testMistake.setGamePhase("opening");
            testMistake.setMoveNumber(1);
            testMistake.setScoreDrop(50);
            testMistake.setTacticalTheme(TacticalTheme.OPENING_PRINCIPLE);
            
            // Save test document
            UserMistakeDocument saved = userMistakeService.saveMistake(testMistake);
            logger.info("Successfully saved test mistake with ID: {}", saved.getId());
            
            // Retrieve and verify
            var retrieved = userMistakeService.findById(saved.getId());
            if (retrieved.isPresent()) {
                logger.info("Successfully retrieved test mistake: {}", retrieved.get().getUsername());
                
                // Clean up test data
                userMistakeService.deleteById(saved.getId());
                logger.info("Test data cleaned up successfully");
            }
            
            logger.info("MongoDB connection test completed successfully!");
            
        } catch (Exception e) {
            logger.error("MongoDB connection test failed: ", e);
            throw new RuntimeException("Failed to connect to MongoDB", e);
        }
    }
}