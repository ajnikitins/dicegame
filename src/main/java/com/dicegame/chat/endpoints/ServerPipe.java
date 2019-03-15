package com.dicegame.chat.endpoints;

import com.dicegame.chat.events.EventManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerPipe extends Thread {

  private ServerSocket socket;
  private EventManager<Pipe> eventManager;
  private CopyOnWriteArrayList<Pipe> pipes;

  ServerPipe(ServerSocket socket, EventManager<Pipe> eventManager, CopyOnWriteArrayList<Pipe> pipes) {
    this.eventManager = eventManager;
    this.socket = socket;
    this.pipes = pipes;
  }

  @Override
  public void run() {
    try {
      while (!isInterrupted()) {
        final Socket clientSocket = socket.accept();

        eventManager.log("Client " + clientSocket.getRemoteSocketAddress() + " connected");

        Pipe pipe = new Pipe(clientSocket, eventManager, pipes);
        pipe.setDaemon(true);
        pipe.setName("Handler Thread " + pipes.size());
        pipe.start();
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
