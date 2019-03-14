package com.dicegame.chat.events;

import java.util.HashMap;
import java.util.Map;

public class EventManager<T>{
  private Map<String, EventHandler<T>> eventHandlers = new HashMap<>();
  private EventHandler<T> beforeHandle;

  public void setBeforeHandle(EventHandler<T> beforeHandle) {
    this.beforeHandle = beforeHandle;
  }

  public void addHandler(String event, EventHandler<T> handler) {
    eventHandlers.put(event, handler);
  }

  public void handle(Event<T> e) {
    if (beforeHandle != null) {
      beforeHandle.handle(e);
    }
    for (Map.Entry<String, EventHandler<T>> entry : eventHandlers.entrySet()) {
      if (entry.getKey().equals(e.getCommand())) {
        entry.getValue().handle(e);
        return;
      }
    }
  }
}
