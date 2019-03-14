package com.dicegame.chat.endpoints;

import com.dicegame.chat.content.Event;
import com.dicegame.chat.content.EventHandler;
import com.dicegame.chat.content.Message;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public class Client extends Thread {

  private Socket clientSocket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private Map<String, EventHandler<Client>> eventHandlers;

  public Client(String ip, int port, String name) throws IOException {
    clientSocket = new Socket(ip, port);
    out = new ObjectOutputStream(clientSocket.getOutputStream());
    in = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
    eventHandlers = new HashMap<>();

    setDefaultEventHandlers();

    send("name", name);
  }

  public Socket getClientSocket() {
    return clientSocket;
  }

  private void setDefaultEventHandlers() {
    addHandler("log", (e) -> System.out.println(e.getBody()));
    addHandler("exit", (e) -> {
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
        handle(new Event<>(this, (Message) in.readObject()));
      } catch (SocketException e) {
        log("Disconnected from server");
        break;
      } catch (IOException | ClassNotFoundException e) {
        log("Error: Failed to receive message");
      }
    }
  }

  private void handle(Event<Client> event) {
    for (Map.Entry<String, EventHandler<Client>> entry : eventHandlers.entrySet()) {
      if (entry.getKey().equals(event.getCommand())) {
        entry.getValue().handle(event);
        return;
      }
    }
  }

  private void log(String body) {
    handle(new Event<>(null,"log", body));
  }

  public void addHandler(String event, EventHandler<Client> handler) {
    eventHandlers.put(event, handler);
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
