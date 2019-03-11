package com.dicegame.controllers;

import com.dicegame.chat.endpoints.Server;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

public class ServerMenuController implements Initializable {

  @FXML private ListView<String> serverLog;
  @FXML private ListView<String> clientList;

  private Server server;

  void createServer(int port, int roomSize) {
    try {
      server = new Server(port, roomSize);
      server.setName("Server Thread");
      server.setDaemon(true);
      server.start();
      serverLog.setItems(server.getServerLog());
      clientList.setItems(server.getClientNames());
    } catch (IOException e) {
      server.getServerLog().add("Error: Invalid Port");
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {

  }

  void stop() {
    server.close();
  }
}
