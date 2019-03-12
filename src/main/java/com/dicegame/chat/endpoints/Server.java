package com.dicegame.chat.endpoints;

import com.dicegame.chat.content.Message;
import com.dicegame.interfaces.EventHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Server extends Thread {

  private ServerSocket socket;
  private CopyOnWriteArrayList<ServerHandler> clientHandlers;
  private ObservableList<String> serverLog;
  private ObservableList<String> clientNames;
  private int roomSize;
  private Map<String, EventHandler<ServerHandler>> eventHandlers;

  public Server(int port, int roomSize) throws IOException {
    this.roomSize = roomSize;
    this.serverLog = FXCollections.observableArrayList();
    this.clientNames = FXCollections.observableArrayList();
    this.clientHandlers = new CopyOnWriteArrayList<>();
    this.socket = new ServerSocket(port);
    this.eventHandlers = new HashMap<>();

    addHandler("exit", (client, body) -> clientDisconnected(client));
  }

  public ObservableList<String> getServerLog() {
    return serverLog;
  }

  public ObservableList<String> getClientNames() {
    return clientNames;
  }

  int getRoomSize() {
    return roomSize;
  }

  CopyOnWriteArrayList<ServerHandler> getClientHandlers() {
    return clientHandlers;
  }

  @Override
  public void run() {
    try {
      while (!isInterrupted()) {
        final Socket clientSocket = socket.accept();

        addToLog("Client " + clientSocket.getRemoteSocketAddress() + " connected");

        ServerHandler clientHandler = new ServerHandler(this, clientSocket);
        clientHandlers.add(clientHandler);
        clientHandler.setDaemon(true);
        clientHandler.setName("Client Thread " + clientHandlers.size());
        clientHandler.start();
      }
    } catch (SocketException ignored) {
    } catch (IOException e) {
      addToLog("Error: Failed to accept connection");
    }
  }

  synchronized void clientDisconnected(ServerHandler client) {
    client.close();
    clientHandlers.remove(client);
    Platform.runLater(() -> clientNames.remove(client.getChatName()));
    addToLog("Client " + client.getClientSocket().getRemoteSocketAddress() + " disconnected");
    toAll("message", client.getChatName() + " has disconnected!");
  }

  synchronized void handle(ServerHandler client, Message message) {
    System.out.println("Received " + message);

    addToLog("From " + client.getClientSocket().getRemoteSocketAddress()
        + " received command: " + message.getCommand()
        + " with body: " + message.getBody()
    );

    for (Map.Entry<String, EventHandler<ServerHandler>> entry : eventHandlers.entrySet()) {
      if (entry.getKey().equals(message.getCommand())) {
        entry.getValue().handle(client, message.getBody());
        return;
      }
    }
  }

  public void addHandler(String event, EventHandler<ServerHandler> handler) {
    eventHandlers.put(event, handler);
  }

  public synchronized void toAll(String command, String body) {
    clientHandlers.forEach(client -> client.send(command, body));
  }

  private synchronized void toAll(String command, Consumer<ServerHandler> onSend) {
    clientHandlers.forEach(client -> { client.send(command, ""); onSend.accept(client);});
  }

  void addToLog(String message) {
    Platform.runLater(() -> serverLog.add(message));
  }

  public void close() {
    try {
      socket.close();
    } catch (IOException e) {
     addToLog("Error: Failed to close socket");
    }
    toAll("exit", ServerHandler::close);
    interrupt();
  }
}
