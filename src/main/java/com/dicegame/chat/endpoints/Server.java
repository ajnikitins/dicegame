package com.dicegame.chat.endpoints;

import com.dicegame.chat.events.Event;
import com.dicegame.chat.events.EventManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server extends Thread {

  private ServerSocket socket;
  private int roomSize;
  private CopyOnWriteArrayList<Pipe> pipes;
  private EventManager<Pipe> eventManager;

  public Server(int port, int roomSize) throws IOException {
    this.socket = new ServerSocket(port);

    this.roomSize = roomSize;
    this.pipes = new CopyOnWriteArrayList<>();
    this.eventManager = new EventManager<>();

    eventManager.setBeforeHandle((e) -> {
      if (e.getCaller() != null) {
        if (!pipes.contains(e.getCaller())) {
          if (!e.getCommand().equals("name")) {
            e.getCaller().send("error", "NoName");
            e.getCaller().close();
            return;
          }
        }

        eventManager.log("From " + e.getCaller().getSocket().getRemoteSocketAddress()
            + " received command: " + e.getCommand()
            + " with body: " + e.getBody()
        );
      }
    });
    setDefaultEventHandlers();
  }

  public EventManager<Pipe> getEventManager() {
    return eventManager;
  }

  private void setDefaultEventHandlers() {
    eventManager.addHandler("exit", (e) -> clientDisconnected(e.getCaller()));

    eventManager.addHandler("message", (e) -> e.getCaller().sendAll(
        "message", e.getCaller().getPipeName() + "> "  + e.getBody()
    ));

    eventManager.addHandler("name", (e) -> {
      if (e.getBody().equals("")) {
        e.getCaller().send("error", "EmptyName");
        close();
        return;
      }

      if (pipes.size() > roomSize) {
        e.getCaller().send("error", "ReachedMaxRoom");
        close();
        return;
      }

      e.getCaller().setPipeName(e.getBody());
      pipes.add(e.getCaller());
      eventManager.handle(new Event<>(e.getCaller(),"join", e.getBody()));
      e.getCaller().sendAll("join", e.getBody());
    });
  }

  private void clientDisconnected(Pipe pipe) {
    pipe.close();
    pipes.remove(pipe);
    eventManager.handle(new Event<>(pipe,"leave", pipe.getPipeName()));
    eventManager.log("Client " + pipe.getSocket().getRemoteSocketAddress() + " disconnected");
    pipe.sendAll("leave", pipe.getPipeName());
  }

  @Override
  public void run() {
    try {
      while (!isInterrupted()) {
        final Socket clientSocket = socket.accept();

        eventManager.log("Client " + clientSocket.getRemoteSocketAddress() + " connected");

        Pipe clientHandler = new Pipe(clientSocket, eventManager, pipes);
        clientHandler.setDaemon(true);
        clientHandler.setName("Handler Thread " + pipes.size());
        clientHandler.start();
      }
    } catch (IOException e) {
      eventManager.log("Error: Failed to accept connection");
    }
  }

  public void close() {
    closeAll();
    try {
      socket.close();
    } catch (IOException e) {
      eventManager.log("Error: Failed to close socket");
    }
    interrupt();
  }

  private void closeAll() {
    pipes.forEach((pipe -> {
      pipe.send("exit", "");
      pipe.close();
    }));
  }
}
