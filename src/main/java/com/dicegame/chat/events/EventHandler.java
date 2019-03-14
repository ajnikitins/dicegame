package com.dicegame.chat.events;

@FunctionalInterface
public interface EventHandler<T> {

  // TODO: Add next() functionality

  void handle(Event<T> event);
}
