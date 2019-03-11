package com.dicegame.chat.content;

public class Message {

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
}
