package com.dicegame.chat.endpoints;

import com.dicegame.chat.content.Message;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import javafx.application.Platform;

public class ServerHandler extends Thread {

  private Socket clientSocket;
  private Server baseServer;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private String clientName;

  ServerHandler(Server baseServer, Socket clientSocket) {
    this.clientSocket = clientSocket;
    this.baseServer = baseServer;

    try {
      out = new ObjectOutputStream(clientSocket.getOutputStream());
      in = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
    } catch (IOException e) {
      baseServer.addToLog("Error: Failed to open streams");
    }
  }

  Socket getClientSocket() {
    return clientSocket;
  }

  public String getChatName() {
    return clientName + " - " + clientSocket.getPort();
  }

  public Server getBaseServer() {
    return baseServer;
  }

  @Override
  public void run() {
    try {
      Message nameMessage = (Message) in.readObject();

      if (!nameMessage.getCommand().equals("name") || nameMessage.getBody().equals("")) {
        baseServer.addToLog("Error: Invalid name command");
        send(new Message("error", "NoName"));
        throw new SocketException();
      }

      this.clientName = nameMessage.getBody();
      Platform.runLater(() -> baseServer.getClientNames().add(getChatName()));

      baseServer.toAll(new Message("message", getChatName() + " has joined!"));

      if (baseServer.getClientHandlers().size() > baseServer.getRoomSize()) {
        send(new Message("error", "ReachedMaxRoom"));
        baseServer.clientDisconnected(this);
      }

      while (!isInterrupted()) {
        baseServer.handle(this, (Message) in.readObject());
      }
    } catch (SocketException e) {
      baseServer.clientDisconnected(this);
    } catch (IOException | ClassNotFoundException e) {
      baseServer.addToLog("Error: Failed to read input");
    }
  }

  void send(Message message) {
    try {
      if (!clientSocket.isClosed()) {
        out.writeObject(message);
      }
    } catch (IOException e) {
      baseServer.addToLog("Error: Failed to send message");
      e.printStackTrace();
    }
  }

  void close() {
    try {
      clientSocket.close();
    } catch (IOException e) {
      baseServer.addToLog("Error: Failed to close socket");
    }
    interrupt();
  }
}
