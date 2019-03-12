package com.dicegame.chat.endpoints;

import com.dicegame.chat.content.Message;
import com.dicegame.interfaces.EventHandler;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Client extends Thread {

  private Socket clientSocket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private ObservableList<String> chatLog;
  private Map<String, EventHandler<Client>> eventHandlers;

  public Client(String ip, int port, String name) throws IOException {
    clientSocket = new Socket(ip, port);
    out = new ObjectOutputStream(clientSocket.getOutputStream());
    in = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
    chatLog = FXCollections.observableArrayList();
    eventHandlers = new HashMap<>();


    addHandler("exit", (client, body) -> {
      try {
        clientSocket.close();
      } catch (IOException e) {
        addToLog("Error: Failed to close socket");
      }
    });

    send("name", name);
  }

  public ObservableList<String> getChatLog() {
    return chatLog;
  }

  public Socket getClientSocket() {
    return clientSocket;
  }

  public void send(String command, String body) {
    try {
      out.writeObject(new Message(command, body));
    } catch (IOException e) {
      e.printStackTrace();
      addToLog("Error: Failed to send message");
    }
  }

  @Override
  public void run() {
    while (!isInterrupted()) {
      try {
        Message message = (Message) in.readObject();

        for (Map.Entry<String, EventHandler<Client>> entry : eventHandlers.entrySet()) {
          if (entry.getKey().equals(message.getCommand())) {
            entry.getValue().handle(this, message.getBody());
            return;
          }
        }

      } catch (SocketException e) {
        addToLog("Disconnected from server");
        break;
      } catch (IOException | ClassNotFoundException e) {
        addToLog("Error: Failed to receive message");
      }
    }
  }

  public void addHandler(String event, EventHandler<Client> handler) {
    eventHandlers.put(event, handler);
  }

  public void addToLog(String message) {
    Platform.runLater(() -> chatLog.add(message));
  }

  public void close() {
    if (!clientSocket.isClosed()) {
      send("exit", "");
    }
    try {
      clientSocket.close();
    } catch (IOException e) {
      addToLog("Error: Failed to close socket");
    }
    interrupt();
  }
}
