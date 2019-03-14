package com.dicegame.chat.endpoints;

import com.dicegame.chat.events.Event;
import com.dicegame.chat.content.Message;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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
      baseServer.log("Error: Failed to open streams");
    }
  }

  Socket getClientSocket() {
    return clientSocket;
  }

  void setClientName(String clientName) {
    this.clientName =  clientName + " - " + clientSocket.getPort();
  }

  public String getClientName() {
    return clientName;
  }

  Server getBaseServer() {
    return baseServer;
  }

  @Override
  public void run() {
    try {
      while (!isInterrupted()) {
        baseServer.getEventManager().handle(new Event<>(this, (Message) in.readObject()));
      }
    } catch (IOException | ClassNotFoundException e) {
      baseServer.log("Error: Failed to read input");
    }
  }

  void send(String command, String body) {
    try {
      if (!clientSocket.isClosed()) {
        out.writeObject(new Message(command, body));
      }
    } catch (IOException e) {
      baseServer.log("Error: Failed to send message");
      e.printStackTrace();
    }
  }

  void close() {
    try {
      clientSocket.close();
    } catch (IOException e) {
      baseServer.log("Error: Failed to close socket");
    }
    interrupt();
  }
}
