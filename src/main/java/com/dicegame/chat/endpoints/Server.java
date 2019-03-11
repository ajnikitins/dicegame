package com.dicegame.chat.endpoints;

import com.dicegame.chat.content.Message;
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

        addToLog("Client " + clientSocket.getRemoteSocketAddress() + " connected");

        ServerHandler clientHandler = new ServerHandler(this, clientSocket);
        clients.add(clientHandler);
        clientHandler.setDaemon(true);
        clientHandler.setName("Client Thread " + clients.size());
        clientHandler.start();
      }
    } catch (SocketException ignored) {
    } catch (IOException e) {
      addToLog("Error: Failed to accept connection");
    }
  }

  synchronized void clientDisconnected(ServerHandler client) {
    client.close();
    clients.remove(client);
    Platform.runLater(() -> clientNames.remove(client.getChatName()));
    addToLog("Client " + client.getClientSocket().getRemoteSocketAddress() + " disconnected");
    toAll(new Message("message", client.getChatName() + " has disconnected!"));
  }

  synchronized void handle(ServerHandler client, Message message) {
    System.out.println("Received " + message);

    addToLog("From " + client.getClientSocket().getRemoteSocketAddress()
        + " received command: " + message.getCommand()
        + " with body: " + message.getBody()
    );

    switch (message.getCommand()) {
      case "exit":
        clientDisconnected(client);
        break;

      case "message":
        toAll(new Message("message", client.getChatName() + "> "  + message.getBody()));
        break;
    }
  }

  synchronized void toAll(Message message) {
    clients.forEach(client -> client.send(message));
  }

  void addToLog(String message) {
    Platform.runLater(() -> serverLog.add(message));
  }

  public void close() {
    try {
      socket.close();
      for (ServerHandler client : clients) {
        client.send(new Message("exit"));
        client.close();
      }
    } catch (IOException e) {
     addToLog("Error: Failed to close socket");
    }
    interrupt();
  }
}
