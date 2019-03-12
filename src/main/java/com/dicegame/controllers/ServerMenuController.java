package com.dicegame.controllers;

import com.dicegame.chat.content.Message;
import com.dicegame.chat.endpoints.Server;
import com.dicegame.interfaces.Stoppable;
import com.dicegame.utils.AlertFactory;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;

public class ServerMenuController implements Initializable, Stoppable {

  @FXML private ListView<String> serverLog;
  @FXML private ListView<String> clientList;

  private Server server;

  void createServer(int port, int roomSize) {
    try {
      server = new Server(port, roomSize);
    } catch (IOException e) {
      System.out.println("Error: Invalid Port");

      AlertFactory.showAlert(
          AlertType.ERROR,
          "Port is already in use, try again!",
          () -> serverLog.getScene().getWindow().hide()
      );
      return;
    }

    server.setName("Server Thread");
    server.setDaemon(true);
    server.start();
    serverLog.setItems(server.getServerLog());
    clientList.setItems(server.getClientNames());

    server.addHandler("message", (client, body) -> client.getBaseServer().toAll(
        new Message("message", client.getChatName() + "> "  + body)
    ));
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {

  }

  @Override
  public void stop() {
    if (server != null) {
      server.close();
    }
  }
}
