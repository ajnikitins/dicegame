package com.dicegame.endpoints;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Server extends Thread {

  private ServerSocket socket;
  private ArrayList<ServerHandler> clients;
  private ObservableList<String> serverLog;
  private ObservableList<String> clientNames;
  private int roomSize;

  public Server(int port, int roomSize) throws IOException {
    this.roomSize = roomSize;
    this.serverLog = FXCollections.observableArrayList();
    this.clientNames = FXCollections.observableArrayList();
    this.clients = new ArrayList<>();
    this.socket = new ServerSocket(port);
  }

  public ObservableList<String> getServerLog() {
    return serverLog;
  }

  public ObservableList<String> getClientNames() {
    return clientNames;
  }

  public ServerSocket getSocket() {
    return socket;
  }

  int getRoomSize() {
    return roomSize;
  }

  ArrayList<ServerHandler> getClients() {
    return clients;
  }

  @Override
  public void run() {
    try {
      while (!isInterrupted()) {
        final Socket clientSocket = socket.accept();

        Platform.runLater(() -> serverLog.add(
          "Client "
          + clientSocket.getRemoteSocketAddress()
          + " connected"
        ));
        ServerHandler clientHandler = new ServerHandler(this, clientSocket);
        clients.add(clientHandler);
        clientHandler.setDaemon(true);
        clientHandler.setName("Client Thread " + clients.size());
        clientHandler.start();
      }
    } catch (SocketException ignored) {
    } catch (IOException e) {
      Platform.runLater(() -> serverLog.add("Error: Failed to accept connection"));
    }
  }

  void clientDisconnected(ServerHandler client) {
    client.close();
    Platform.runLater(() -> {
      serverLog.add("Client " + client.getClientSocket().getRemoteSocketAddress() + " disconnected");
      toAll("message", String.format("%s - %s has disconnected!", client.getClientName(), client.getClientSocket().getPort()));
      clientNames.remove(clients.indexOf(client));
      clients.remove(client);
    });
  }

  synchronized void handle(ServerHandler client, String command, String body) {
    System.out.println("Received " + command + " " + body);
    Platform.runLater(() -> serverLog.add(
      "From " + client.getClientSocket().getRemoteSocketAddress()
        + " received command: " + command
        + " with body: " + body));

    switch (command) {
      case "exit":
        clientDisconnected(client);
        break;

      case "message":
        toAll(command, String.format("%s - %s> %s", client.getClientName(), client.getClientSocket().getPort(), body));
        break;
    }
  }

  synchronized void toAll(String command, String body) {
    clients.forEach(client -> client.send(command, body));
  }

  public void close() {
    try {
      socket.close();
      for (ServerHandler client : clients) {
        client.send("exit", "");
        client.close();
      }
    } catch (IOException e) {
      Platform.runLater(() -> serverLog.add("Error: Failed to close socket"));
    }
    interrupt();
  }
}
