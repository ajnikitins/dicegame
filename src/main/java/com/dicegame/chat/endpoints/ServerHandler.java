package com.dicegame.chat.endpoints;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import javafx.application.Platform;

public class ServerHandler extends Thread {

  private Socket clientSocket;
  private Server baseServer;
  private BufferedReader in;
  private PrintWriter out;
  private String clientName;

  ServerHandler(Server baseServer, Socket clientSocket) {
    this.clientSocket = clientSocket;
    this.baseServer = baseServer;

    try {
      in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      out = new PrintWriter(clientSocket.getOutputStream(), true);
    } catch (IOException e) {
      Platform.runLater(() -> baseServer.getServerLog().add("Error: Failed to open streams"));
    }
  }

  Socket getClientSocket() {
    return clientSocket;
  }

  @Override
  public void run() {
    try {
      this.clientName = in.readLine();
      Platform.runLater(() -> baseServer.getClientNames().add(
          clientName + " - " + clientSocket.getRemoteSocketAddress()
      ));

      baseServer.toAll("message", String.format("%s - %s has joined!", clientName, clientSocket.getPort()));

      if (baseServer.getClients().size() > baseServer.getRoomSize()) {
        send("error", "ReachedMaxRoom");
        baseServer.clientDisconnected(this);
      }

      while (!isInterrupted()) {
          baseServer.handle(this, in.readLine(), in.readLine());
      }
    } catch (SocketException e) {
      baseServer.clientDisconnected(this);
    } catch (IOException e) {
      Platform.runLater(() -> baseServer.getServerLog().add("Error: Failed to read input"));
    }
  }

  void send(String command, String body) {
    out.println(command);
    out.println(body);
  }

  String getClientName() {
    return clientName;
  }

  void close() {
    try {
      clientSocket.close();
    } catch (IOException e) {
      Platform.runLater(() -> baseServer.getServerLog().add("Error: Failed to close socket"));
    }
    interrupt();
  }
}
