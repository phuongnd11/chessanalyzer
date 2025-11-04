package com.inspireon.chessanalyzer.domain.repository;

import com.inspireon.chessanalyzer.domain.documents.UserProgressDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface UserProgressRepository extends MongoRepository<UserProgressDocument, String> {

    Optional<UserProgressDocument> findByUsername(String username);

    @Query("{ 'username': ?0 }")
    UserProgressDocument findProgressByUsername(String username);

    @Query(value = "{ 'username': ?0 }", fields = "{ 'solved_mistake_ids': 1 }")
    UserProgressDocument findSolvedMistakeIdsByUsername(String username);

    @Query(value = "{ 'username': ?0 }", fields = "{ 'solved_puzzle_ids': 1 }")
    UserProgressDocument findSolvedPuzzleIdsByUsername(String username);

    boolean existsByUsername(String username);
}