# Repository Guidelines

This guide helps contributors work effectively on ChessAnalyzer, a Spring Boot application packaged as a WAR and built with Gradle.

## Project Structure & Module Organization
- `src/main/java`: Application code under `com.inspireon.chessanalyzer`.
- `src/main/resources`: App config and assets (e.g., `application.properties`, `openingbook/response.json`, sample PGNs in `games/`).
- `src/test/java`: JUnit 5 tests (e.g., `ChessanalyzerApplicationTests`).
- `src/test/resources/games`: Test PGN fixtures.
- Build outputs land in `build/` (e.g., `build/libs/*.war`).

## Build, Test, and Development Commands
- `./gradlew clean build`: Compile, run tests, and produce a WAR.
- `./gradlew test`: Run JUnit 5 tests with detailed logging.
- `./gradlew bootWar` or `./gradlew war`: Create deployable WAR in `build/libs/`.
- Run locally: deploy the WAR to a servlet container (e.g., Tomcat). Note: the project excludes embedded Tomcat at compile time; `bootRun` is not supported.

## Coding Style & Naming Conventions
- Language: Java (targeting modern JDK; see `build.gradle`). Use 4‑space indentation.
- Packages: `com.inspireon.chessanalyzer`; classes `PascalCase`, methods/fields `camelCase`.
- Lombok is used; annotate consistently (e.g., `@Getter`). Avoid adding boilerplate manually.
- Keep controllers/config in coherent packages; keep resources under `src/main/resources`.

## Testing Guidelines
- Framework: JUnit 5 (`useJUnitPlatform`). Place tests under `src/test/java` mirroring package structure.
- Naming: `*Tests.java` with clear, behavior‑focused method names.
- Data: put PGN fixtures in `src/test/resources/games`.
- Run: `./gradlew test`. Aim to cover core services/config; no strict coverage gate yet.

## Commit & Pull Request Guidelines
- Commits: concise, imperative subject (<= 72 chars), optional body for context (why > what).
  - Examples: `Fix CORS mapping for production`, `Add PGN fixtures for rating stats`.
- PRs: include purpose, summary of changes, test notes, and any related issue IDs. Add screenshots for UI/JSON examples when helpful.

## Security & Configuration Tips
- App settings live in `src/main/resources/application.properties` (keys `chessanalyzer.*`). Prefer environment overrides for secrets/paths.
- CORS: check `WebConfig`/`ChessanalyzerApplication` for allowed origins before deploying.
