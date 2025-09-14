# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Chess Analyzer is a Spring Boot web application that analyzes chess games and provides statistics. It integrates with Chess.com API, uses Stockfish engine for analysis, and serves REST endpoints for chess data analysis.

## Build & Development Commands

### Building the Application
```bash
./gradlew build          # Full build including tests
./gradlew bootWar        # Create WAR file for deployment
./gradlew clean build    # Clean and rebuild
```

### Running Tests
```bash
./gradlew test           # Run all tests
./gradlew test --tests OpeningIndexerTest  # Run specific test
```

### Running the Application
```bash
./gradlew bootRun        # Run Spring Boot application locally
```

## Architecture Overview

### Package Structure
- `web/controller/` - REST controllers (ReportController, OpeningController)
- `application/` - Services and business logic layer
  - `clients/` - External API clients (Chess.com)
  - `service/` - Core services including WeeklyAnalysisService
- `domain/` - Domain logic and data management
  - `datamanager/` - Game data access and processing (GameDataAccess, OpeningIndexer)
  - `cache/` - Caching layer (PlayerStatCache, OpeningCache)
- `common/` - Shared utilities and I/O operations
- `stockfish/` - Stockfish chess engine integration

### Key Components
- **GameDataAccess**: Handles PGN file processing and Chess.com API integration
- **OpeningIndexer**: Indexes and analyzes chess openings
- **UCIEngine**: Abstract base for Stockfish UCI protocol communication
- **ReportService**: Generates player statistics and analysis reports

### Configuration
- Main config in `src/main/resources/application.properties`
- Key settings: Chess.com API limits, Stockfish path, game folders
- CORS configured for `http://chessstats.io` and `http://localhost:3000`

### Dependencies
- Spring Boot 3.2.0 with Java 21
- Chess library: `com.github.phuongnd11:chesslib:2.0.5`
- Uses Lombok for boilerplate reduction
- WAR packaging for deployment

### External Dependencies
- Stockfish engine binary (configurable path in application.properties)
- Chess.com API for game data retrieval
- Local file system for PGN game storage