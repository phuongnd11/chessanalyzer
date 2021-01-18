package com.inspireon.chessanalyzer.web.dtos;

import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
public class PlayerOverview {
  
  private String title;
  
  private String style;
  
  private String trainingSuggestion;
  
  private String timeManagement;
  
  private String emotional;

  public PlayerOverview(String title, String style, String trainingSuggestion, String timeManagement,
      String emotional) {
    super();
    this.title = title;
    this.style = style;
    this.trainingSuggestion = trainingSuggestion;
    this.timeManagement = timeManagement;
    this.emotional = emotional;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getStyle() {
    return style;
  }

  public void setStyle(String style) {
    this.style = style;
  }

  public String getTrainingSuggestion() {
    return trainingSuggestion;
  }

  public void setTrainingSuggestion(String trainingSuggestion) {
    this.trainingSuggestion = trainingSuggestion;
  }

  public String getTimeManagement() {
    return timeManagement;
  }

  public void setTimeManagement(String timeManagement) {
    this.timeManagement = timeManagement;
  }

  public String getEmotional() {
    return emotional;
  }

  public void setEmotional(String emotional) {
    this.emotional = emotional;
  }
  
}
