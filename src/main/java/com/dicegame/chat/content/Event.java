package com.dicegame.chat.content;

public class Event<T extends Thread> {

  private Message message;
  private T caller;

  public Event(T caller, Message message) {
    this.caller = caller;
    this.message = message;
  }

  public Event(T caller, String command, String body) {
    this.caller = caller;
    this.message = new Message(command, body);
  }

  public String getCommand() {
    return message.getCommand();
  }

  public String getBody() {
    return message.getBody();
  }

  public T getCaller() {
    return caller;
  }

  public Message getMessage() {
    return message;
  }

  @Override
  public String toString() {
    String res = getCommand();

    if (!getBody().equals("")) {
      res += " " + getBody();
    }

    if (caller != null) {
      res += " by " + caller.getName();
    }

    return res;
  }
}
