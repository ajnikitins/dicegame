package com.dicegame.chat.endpoints;

import com.dicegame.chat.content.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Client extends Thread {

  private Socket clientSocket;
  private BufferedReader in;
  private PrintWriter out;
  private ObservableList<String> chatLog;

  public Client(String ip, int port, String name) throws IOException {
    clientSocket = new Socket(ip, port);

    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    out = new PrintWriter(clientSocket.getOutputStream(), true);

    chatLog = FXCollections.observableArrayList();

    out.println(name);
  }

  public ObservableList<String> getChatLog() {
    return chatLog;
  }

  public void send(Message message) {
    out.println(message.getCommand());
    out.println(message.getBody());
  }

  @Override
  public void run() {
    while (!isInterrupted()) {
      try {
        Message message = new Message(in.readLine(), in.readLine());

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
                Platform.runLater(() -> chatLog.add("Room is full, please try again later!"));
                break;
            }
            break;
        }

      } catch (SocketException e) {
        Platform.runLater(() -> chatLog.add("Disconnected from server"));
        break;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void close() {
    send(new Message("error"));
    try {
      clientSocket.close();
    } catch (IOException e) {
      Platform.runLater(() -> chatLog.add("Error: Failed to close socket"));
    }
    interrupt();
  }
}
