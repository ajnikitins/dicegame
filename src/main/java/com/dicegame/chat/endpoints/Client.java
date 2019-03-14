package com.dicegame.chat.endpoints;

import com.dicegame.chat.content.Message;
import com.dicegame.chat.events.Event;
import com.dicegame.chat.events.EventManager;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class Client extends Thread {

  private Socket clientSocket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private EventManager<Client> eventManager;

  public Client(String ip, int port, String name) throws IOException {
    this.clientSocket = new Socket(ip, port);
    this.out = new ObjectOutputStream(clientSocket.getOutputStream());
    this.in = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
    this.eventManager = new EventManager<>();

    setDefaultEventHandlers();

    send("name", name);
  }

  public Socket getClientSocket() {
    return clientSocket;
  }

  public EventManager<Client> getEventManager() {
    return eventManager;
  }

  private void setDefaultEventHandlers() {
    eventManager.addHandler("log", (e) -> System.out.println(e.getBody()));
    eventManager.addHandler("exit", (e) -> {
      try {
        clientSocket.close();
      } catch (IOException ioe) {
        log("Error: Failed to close socket");
      }
    });
  }

  public void send(String command, String body) {
    try {
      if (!clientSocket.isClosed()) {
        out.writeObject(new Message(command, body));
      }
    } catch (IOException e) {
      e.printStackTrace();
      log("Error: Failed to send message");
    }
  }

  @Override
  public void run() {
    while (!isInterrupted()) {
      try {
        eventManager.handle(new Event<>(this, (Message) in.readObject()));
      } catch (SocketException e) {
        log("Disconnected from server");
        break;
      } catch (IOException | ClassNotFoundException e) {
        log("Error: Failed to receive message");
      }
    }
  }

  private void log(String body) {
    eventManager.handle(new Event<>(null,"log", body));
  }

  public void close() {
    if (!clientSocket.isClosed()) {
      send("exit", "");
    }
    try {
      clientSocket.close();
    } catch (IOException e) {
      log("Error: Failed to close socket");
    }
    interrupt();
  }
}
