package com.dicegame.chat.endpoints;

import com.dicegame.chat.events.Event;
import com.dicegame.chat.events.EventManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {

  private ServerPipe serverPipe;
  private int roomSize;
  private CopyOnWriteArrayList<Pipe> pipes;
  private EventManager<Pipe> eventManager;

  public Server(int roomSize) {
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

        eventManager.log("From "
            + (
                e.getCaller().getPipeName().equals("")
                ? e.getCaller().getSocket().getRemoteSocketAddress()
                : e.getCaller().getPipeName()
              )
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

  public ServerPipe getServerPipe() {
    return serverPipe;
  }

  private void setDefaultEventHandlers() {
    eventManager.addHandler("exit", (e) -> clientDisconnected(e.getCaller()));

    eventManager.addHandler("message", (e) -> e.getCaller().sendAll(
        "message", e.getCaller().getPipeName() + "> "  + e.getBody()
    ));

    eventManager.addHandler("name", (e) -> {
      if (e.getBody().equals("")) {
        e.getCaller().send("error", "EmptyName");
        serverPipe.close();
        return;
      }

      if (pipes.size() > roomSize) {
        e.getCaller().send("error", "ReachedMaxRoom");
        serverPipe.close();
        return;
      }

      e.getCaller().setPipeName(e.getBody());
      pipes.add(e.getCaller());
      eventManager.handle(new Event<>(e.getCaller(),"join", e.getCaller().getPipeName()));
      e.getCaller().sendAll("join", e.getCaller().getPipeName());
    });
  }

  public void start(int port) throws IOException {
    this.serverPipe = new ServerPipe(new ServerSocket(port), eventManager, pipes);
    serverPipe.setDaemon(true);
    serverPipe.setName("Server Thread");
    serverPipe.start();
  }

  private void clientDisconnected(Pipe pipe) {
    pipe.close();
    pipes.remove(pipe);
    eventManager.handle(new Event<>(pipe,"leave", pipe.getPipeName()));
    eventManager.log("Client " + pipe.getSocket().getRemoteSocketAddress() + " disconnected");
    pipe.sendAll("leave", pipe.getPipeName());
  }
}
