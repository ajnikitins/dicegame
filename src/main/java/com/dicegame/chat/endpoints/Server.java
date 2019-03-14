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
  private CopyOnWriteArrayList<ServerHandler> clientHandlers;
  private EventManager<ServerHandler> eventManager;

  public Server(int port, int roomSize) throws IOException {
    this.socket = new ServerSocket(port);

    this.roomSize = roomSize;
    this.clientHandlers = new CopyOnWriteArrayList<>();
    this.eventManager = new EventManager<>();

    eventManager.setBeforeHandle((e) -> {
      if (e.getCaller() != null) {
        if (!clientHandlers.contains(e.getCaller())) {
          if (!e.getCommand().equals("name")) {
            e.getCaller().send("error", "NoName");
            e.getCaller().close();
            return;
          }
        }

        log("From " + e.getCaller().getClientSocket().getRemoteSocketAddress()
            + " received command: " + e.getCommand()
            + " with body: " + e.getBody()
        );
      }
    });
    setDefaultEventHandlers();
  }

  public EventManager<ServerHandler> getEventManager() {
    return eventManager;
  }

  private void setDefaultEventHandlers() {
    eventManager.addHandler("log", (e) -> System.out.println(e.getBody()));

    eventManager.addHandler("exit", (e) -> clientDisconnected(e.getCaller()));

    eventManager.addHandler("message", (e) -> e.getCaller().getBaseServer().toAll(
        "message", e.getCaller().getClientName() + "> "  + e.getBody()
    ));

    eventManager.addHandler("name", (e) -> {
      if (e.getBody().equals("")) {
        e.getCaller().send("error", "EmptyName");
        close();
        return;
      }

      if (clientHandlers.size() > roomSize) {
        e.getCaller().send("error", "ReachedMaxRoom");
        close();
        return;
      }

      e.getCaller().setClientName(e.getBody());
      clientHandlers.add(e.getCaller());
      eventManager.handle(new Event<>(e.getCaller(),"join", e.getBody()));
      toAll("join", e.getBody());
    });
  }

  @Override
  public void run() {
    try {
      while (!isInterrupted()) {
        final Socket clientSocket = socket.accept();

        log("Client " + clientSocket.getRemoteSocketAddress() + " connected");

        ServerHandler clientHandler = new ServerHandler(this, clientSocket);
        clientHandler.setDaemon(true);
        clientHandler.setName("Client Thread " + clientHandlers.size());
        clientHandler.start();
      }
    } catch (IOException e) {
      log("Error: Failed to accept connection");
    }
  }

  private synchronized void clientDisconnected(ServerHandler client) {
    client.close();
    clientHandlers.remove(client);
    eventManager.handle(new Event<>(client,"leave", client.getClientName()));
    log("Client " + client.getClientSocket().getRemoteSocketAddress() + " disconnected");
    toAll("leave", client.getClientName());
  }

  private void toAll(String command, String body) {
    clientHandlers.forEach(client -> client.send(command, body));
  }

  private void closeAll() {
    clientHandlers.forEach(ServerHandler::close);
  }

  void log(String body) {
    eventManager.handle(new Event<>(null,"log", body));
  }

  public void close() {
    try {
      socket.close();
    } catch (IOException e) {
     log("Error: Failed to close socket");
    }
    toAll("exit", "");
    closeAll();
    interrupt();
  }
}
