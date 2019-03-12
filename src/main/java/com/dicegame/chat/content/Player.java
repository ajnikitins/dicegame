package com.dicegame.chat.content;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Player {
  private final SimpleStringProperty name;
  private final SimpleIntegerProperty score;

  public Player(String name) {
    this.name = new SimpleStringProperty(name);
    this.score = new SimpleIntegerProperty(0);
  }

  public String getName() {
    return name.get();
  }

  public void setName(String name) {
    this.name.set(name);
  }

  public int getScore() {
    return score.get();
  }

  public void setScore(int score) {
    this.score.set(score);
  }
}
