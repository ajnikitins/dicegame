package com.dicegame.chat.endpoints;

import com.dicegame.chat.events.EventManager;
import java.io.IOException;
import java.net.Socket;

public class Client {

  private EventManager<Pipe> eventManager;
  private Pipe pipe;

  public Client(String ip, int port, String name) throws IOException {
    this.eventManager = new EventManager<>();
    setDefaultEventHandlers();

    this.pipe = new Pipe(new Socket(ip, port), eventManager);

    pipe.setDaemon(true);
    pipe.setName("Client Thread");

    pipe.setOnClose((pipe) -> {
      if (!pipe.getSocket().isClosed()) {
        pipe.send("exit", "");
      }
    });

    pipe.start();
    pipe.send("name", name);
  }

  public EventManager<Pipe> getEventManager() {
    return eventManager;
  }

  public Pipe getPipe() {
    return pipe;
  }

  private void setDefaultEventHandlers() {
    eventManager.addHandler("exit", (e) -> {
      try {
        e.getCaller().getSocket().close();
      } catch (IOException ioe) {
        eventManager.log("Error: Failed to close socket");
      }
    });
  }
}
