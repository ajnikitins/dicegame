package com.dicegame.chat.endpoints;

import com.dicegame.chat.content.Message;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Client extends Thread {

  private Socket clientSocket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private ObservableList<String> chatLog;

  public Client(String ip, int port, String name) throws IOException {
    clientSocket = new Socket(ip, port);

    out = new ObjectOutputStream(clientSocket.getOutputStream());
    in = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));

    chatLog = FXCollections.observableArrayList();

    send(new Message("name", name));
  }

  public ObservableList<String> getChatLog() {
    return chatLog;
  }

  public void send(Message message) {
    try {
      out.writeObject(message);
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

        switch (message.getCommand()) {
          case "message":
            Platform.runLater(() -> chatLog.add(message.getBody()));
            break;
          case "exit":
            clientSocket.close();
            break;
          case "error":
            clientSocket.close();
            switch (message.getBody()) {
              case "ReachedMaxRoom":
                addToLog("Room is full, please try again later!");
                break;
              case "NoName":
                addToLog("No name specified, try again!");
                break;
            }
            break;
        }

      } catch (SocketException e) {
        addToLog("Disconnected from server");
        break;
      } catch (IOException | ClassNotFoundException e) {
        addToLog("Error: Failed to receive message");
      }
    }
  }

  private void addToLog(String message) {
    Platform.runLater(() -> chatLog.add(message));
  }

  public void close() {
    if (!clientSocket.isClosed()) {
      send(new Message("exit"));
    }
    try {
      clientSocket.close();
    } catch (IOException e) {
      addToLog("Error: Failed to close socket");
    }
    interrupt();
  }
}
