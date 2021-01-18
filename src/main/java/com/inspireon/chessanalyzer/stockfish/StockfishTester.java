/* Copyright 2018 David Cai Wang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.inspireon.chessanalyzer.stockfish;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.game.GameResult;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import com.inspireon.chessanalyzer.domain.model.ChessOpening;
import com.inspireon.chessanalyzer.domain.model.ChessTempoResult;
import com.inspireon.chessanalyzer.stockfish.engine.enums.Option;
import com.inspireon.chessanalyzer.stockfish.engine.enums.Query;
import com.inspireon.chessanalyzer.stockfish.engine.enums.QueryType;

public class StockfishTester {
  
  private static String playerUsername = "phuongdang";
  
  private static int numOfGameToAnalyze = 100;
  
    public static void main(String[] args) throws Exception {
      
      String jsonString = new String(Files.readAllBytes(Paths.get("assets/openingbook/response.json")));
    ObjectMapper mapper = new ObjectMapper();
    ChessTempoResult chessTempoResult = mapper.readValue(jsonString, ChessTempoResult.class);
    
    Map<String, ChessOpening> openings = new HashMap<String, ChessOpening>();
    
    for (ChessOpening chessOpening : chessTempoResult.getOpenings()) {
      openings.put(chessOpening.getLast_pos().split(" ")[0], chessOpening);
    }
      
      StockfishClient client = new StockfishClient.Builder()
                .setInstances(4)
                .setOption(Option.Threads, 4) // Number of threads that Stockfish will use
                .setOption(Option.Minimum_Thinking_Time, 1000) // Minimum thinking time Stockfish will take
                .setOption(Option.Skill_Level, 20) // Stockfish skill level 0-20
                //.setVariant(Variant.BMI2) // Stockfish Variant
                .build();
      
      File f = new File("D:\\workspace\\stockfishjava\\Stockfish-Java\\assets\\games\\" + playerUsername + ".pgn");
      PgnHolder pgn = null;
      if (!f.exists()) {
        pgn = new PgnHolder(getPgnFile(playerUsername));
      } else {
        pgn = new PgnHolder("D:\\workspace\\stockfishjava\\Stockfish-Java\\assets\\games\\" + playerUsername + ".pgn");
      }
      
      SortedMap<String, List<Integer>> winrateByOpening = new TreeMap<String, List<Integer>>();
      Map<String, String> gameIdToOpening = new HashMap<String, String>();
        pgn.loadPgn();
        
        System.out.println("Chess.com player: " + playerUsername);
        System.out.println("September 2020");
        
        for (Game game: pgn.getGames()) {
          
          game.loadMoveText();
            MoveList moves = game.getHalfMoves();
            if (moves.size() < 4) continue;
            Board board = new Board();
            ChessOpening thisGameOpening = null;
            for (int i = 0; i < moves.size(); i++) {
              board.doMove(moves.get(i));
              if (openings.get(board.getFen().split(" ")[0]) != null)
                thisGameOpening = openings.get(board.getFen().split(" ")[0]);
              //System.out.println(board.getFen().split(" ")[0]);
              if (i == 15) break;
            }
            if (thisGameOpening == null) {
              //System.out.println(game.getWhitePlayer() +  "-" + game.getBlackPlayer());
              thisGameOpening = new ChessOpening();
              thisGameOpening.setName("Unknown");
            }
            gameIdToOpening.put(game.getGameId(), thisGameOpening.getName());
            if (winrateByOpening.get(thisGameOpening.getName()) == null) {
              List<Integer> tem = new ArrayList<Integer>();
              tem.add(0);
              tem.add(0);
              tem.add(0);
              winrateByOpening.put(thisGameOpening.getName(), tem);
            }
            
            if ((game.getResult() == GameResult.WHITE_WON && game.getWhitePlayer().getName().equals(playerUsername))
                || game.getResult() == GameResult.BLACK_WON && game.getBlackPlayer().getName().equals(playerUsername))
            {
              winrateByOpening.get(thisGameOpening.getName()).set(0, winrateByOpening.get(thisGameOpening.getName()).get(0)+1);
            } else if (game.getResult() == GameResult.DRAW) {
              winrateByOpening.get(thisGameOpening.getName()).set(1, winrateByOpening.get(thisGameOpening.getName()).get(1)+1);
            } 
            winrateByOpening.get(thisGameOpening.getName()).set(2, winrateByOpening.get(thisGameOpening.getName()).get(2)+1);    
        }
        
        winrateByOpening.entrySet().forEach(gameOpenin -> {
       
          System.out.println("winRate: " + (gameOpenin.getValue().get(0) == 0 ? "0" : 
            String.valueOf(Math.round(gameOpenin.getValue().get(0)*100/gameOpenin.getValue().get(2)))) + "%" + 
              "   total games : " + gameOpenin.getValue().get(2) + "   " + gameOpenin.getKey()) ;
        });
        int k = 0;
        for (Game game: pgn.getGames()) {      
          
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
            //tem
      /*
       * if (gameIdToOpening.get(game.getGameId()) == null ||
       * !gameIdToOpening.get(game.getGameId()).startsWith("Gruenfeld Defense")) {
       * continue; }
       */
            System.out.println("Game: " + k++);
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
                  }                           
                }
                //move 2
                if (evaluteForWhite) {
                  board.doMove(blackMove);
                }
                System.out.println("");
            }         
            
        }
       
    }
    
    
    public static String getPgnFile(String playerUserName) throws MalformedURLException, IOException {  
      
      BufferedInputStream in = new BufferedInputStream(new URL("https://api.chess.com/pub/player/" + playerUserName + "/games/2020/09/pgn").openStream());
    FileOutputStream fileOutputStream = new FileOutputStream("assets/games/" + playerUserName + ".pgn"); 
      byte dataBuffer[] = new byte[1024];
      int bytesRead;
      while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
          fileOutputStream.write(dataBuffer, 0, bytesRead);
      }
    
      
      return "assets/games/" + playerUserName + ".pgn";
    }
    
    public static List<ComputerMove> getComputerMoves(List<String> moveLines, boolean isWhite) {
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
