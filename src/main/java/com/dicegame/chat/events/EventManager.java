package com.dicegame.chat.events;

import java.util.HashMap;
import java.util.Map;

public class EventManager<T> {

  private Map<String, EventHandler<T>> eventHandlers = new HashMap<>();
  private EventHandler<T> beforeHandle = (e) -> {};

  public EventManager() {
    addHandler("log", (e) -> System.out.println(e.getBody()));
  }

  public void setBeforeHandle(EventHandler<T> beforeHandle) {
    this.beforeHandle = beforeHandle;
  }

  public void addHandler(String event, EventHandler<T> handler) {
    eventHandlers.put(event, handler);
  }

  public void handle(Event<T> e) {
    beforeHandle.handle(e);
    for (Map.Entry<String, EventHandler<T>> entry : eventHandlers.entrySet()) {
      if (entry.getKey().equals(e.getCommand())) {
        entry.getValue().handle(e);
        return;
      }
    }
  }

  public void log(String body) {
    handle(new Event<>(null,"log", body));
  }
}
