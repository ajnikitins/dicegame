package com.dicegame.chat.content;

import java.io.Serializable;

public class Message implements Serializable {

  private String command;
  private String body;

  public Message(String command, String body) {
    this.command = command;
    this.body = body;
  }

  public Message(String command) {
    this.command = command;
    this.body = "";
  }

  public String getCommand() {
    return command;
  }

  public String getBody() {
    return body;
  }

  @Override
  public String toString() {
    String res = command;
    if (!body.equals("")) {
      res += " " + body;
    }
    return res;
  }
}
