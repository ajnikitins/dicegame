package com.dicegame.chat.content;

@FunctionalInterface
public interface EventHandler<T extends Thread> {

  // TODO: Add next() functionality

  void handle(Event<T> event);
}
