package com.inspireon.chessanalyzer.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.inspireon.chessanalyzer.domain.datamanager.GameDataAccess;
import com.inspireon.chessanalyzer.stockfish.ComputerMove;
import com.inspireon.chessanalyzer.stockfish.StockfishClient;
import com.inspireon.chessanalyzer.stockfish.engine.enums.Option;
import com.inspireon.chessanalyzer.stockfish.engine.enums.Query;
import com.inspireon.chessanalyzer.stockfish.engine.enums.QueryType;
import com.inspireon.chessanalyzer.web.dtos.OpeningStat;
import com.inspireon.chessanalyzer.web.dtos.UserMistake;

@Service
public class OpeningAnalyzerService {
  @Autowired
  private GameDataAccess gameDataAccess;
  
  public List <UserMistake> getOpeningMistakes(String playerUsername, String openingName) throws Exception {
    List<Game> games = gameDataAccess.getGames(playerUsername);
    
    List<UserMistake> mistakes = new ArrayList<UserMistake>();
    StockfishClient client = new StockfishClient.Builder()
                .setInstances(4)
                .setOption(Option.Threads, 4) // Number of threads that Stockfish will use
                .setOption(Option.Minimum_Thinking_Time, 1000) // Minimum thinking time Stockfish will take
                .setOption(Option.Skill_Level, 20) // Stockfish skill level 0-20
                //.setVariant(Variant.BMI2) // Stockfish Variant
                .build();
    int toEvaluate = 5;
    
    for (Game game: games) {      
      
      if (mistakes.size() >= 2) {
        break;
      }
            game.loadMoveText();
            MoveList moves = game.getHalfMoves();
            Board board = new Board();
            //Replay all the moves from the game and print the final position in FEN format
            int index = 0;
            int currentScore = 0;
            int moveNum = 0;
            
            boolean evaluteForWhite = game.getWhitePlayer().equals(playerUsername);
            
            boolean skip = false;
            ComputerMove lastComputerMove = null;
  
            for (int i = 0; i < moves.size(); i+=2) {
              if (i >= moves.size()-1) break;
              //tem
              if (i > 15) break;
              
              Move whiteMove = moves.get(i);
              Move blackMove = moves.get(i+1);
              
              //System.out.println("" + i/2 + "." + whiteMove + " " + blackMove);
              
              if (!evaluteForWhite) {
                board.doMove(whiteMove);
              }
                                         
                List<String> responses = client.getBestMoves(new Query.Builder(QueryType.Best_Move)
                    .setFen(board.getFen())
                    .setMovetime(1000)
                    .build());
                
                List<ComputerMove> comMoves = getComputerMoves(responses, evaluteForWhite);
                
                String fenBefore = board.getFen();
                //move 1
                board.doMove(evaluteForWhite ? whiteMove : blackMove);
                if (i > 3) {
                  List<String> responsesAfter = client.getBestMoves(new Query.Builder(QueryType.Best_Move)
                      .setFen(board.getFen())
                      .setMovetime(1000)
                      .build());
                  
                  List<ComputerMove> comSuggestForOpp = getComputerMoves(responsesAfter, evaluteForWhite);
                  
                  ComputerMove comMove = comMoves.get(comMoves.size()-1);
                  //board.getPiece(new Square());
                  int afterScore = (-1) * comSuggestForOpp.get(comSuggestForOpp.size()-1).getScore();
                  
                  
                  if (comMove.getScore() - afterScore > 100) {
                    System.out.println("Mistake: " + comMove.getScore() + "         " + afterScore);
                    System.out.println(fenBefore);
                    System.out.println("User move: " + (evaluteForWhite ? whiteMove.toString() : blackMove.toString()) + " Computer move: " + comMove.getMove().toString());
                    mistakes.add(new UserMistake(game.getGameId(), playerUsername, fenBefore, evaluteForWhite ? whiteMove.toString() : blackMove.toString(), comMove.getMove().toString()));
                  }                           
                }
                //move 2
                if (evaluteForWhite) {
                  board.doMove(blackMove);
                }
            }         
            
        }
    return mistakes;
  }

  public TreeSet <OpeningStat> getOpenings(String playerUsername) throws Exception {
    return gameDataAccess.getOpenings(playerUsername);
  }
  
  private static List<ComputerMove> getComputerMoves(List<String> moveLines, boolean isWhite) {
      List<ComputerMove> cmoves = new ArrayList<ComputerMove>();
      for (String moveLine : moveLines) {        
        if (moveLine.contains("bestmove")) {
          break;
        }
        int score = 0;
        String move = "";
        String [] tokens = moveLine.split(" ");
        for (int i = 0; i < tokens.length; i++) {
          if (tokens[i].equalsIgnoreCase("cp")) {
            score = Integer.parseInt(tokens[i+1]);  
          }
          if (tokens[i].equalsIgnoreCase("pv")) {          
            move = tokens[i+1];
          }
        }
        String lineAfterMove = ((moveLine.indexOf(move) + move.length()) < moveLine.length() - 1) ? moveLine.substring(moveLine.indexOf(move) + move.length() + 1) : "";
        
        cmoves.add(new ComputerMove(move, lineAfterMove, score, isWhite));
      }
      
      return cmoves;
    }
  
  
}
