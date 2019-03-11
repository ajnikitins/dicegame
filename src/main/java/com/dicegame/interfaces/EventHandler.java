package com.dicegame.interfaces;

@FunctionalInterface
public interface EventHandler<T> {

  void handle(T caller, String body);

}
