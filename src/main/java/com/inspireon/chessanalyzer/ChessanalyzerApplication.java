package com.inspireon.chessanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
public class ChessanalyzerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ChessanalyzerApplication.class, args);
  }

}
