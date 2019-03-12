package com.dicegame.chat.content;

@FunctionalInterface
public interface EventHandler<T> {

  void handle(T caller, String body);

}
