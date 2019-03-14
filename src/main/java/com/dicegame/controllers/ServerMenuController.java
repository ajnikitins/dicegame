package com.dicegame.controllers;

import com.dicegame.chat.content.Player;
import com.dicegame.chat.endpoints.Server;
import com.dicegame.utils.AlertFactory;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ServerMenuController implements Initializable, Stoppable {

  @FXML private ListView<String> serverLogList;
  @FXML private TableView<Player> clientList;
  @FXML private TableColumn<Player, String> nameColumn;
  @FXML private TableColumn<Player, Integer> scoreColumn;
  @FXML private ObservableList<Player> playerList;

  private ObservableList<String> serverLog;

  private Server server;

  void createServer(int port, int roomSize) {
    try {
      server = new Server(port, roomSize);
    } catch (IOException e) {
      System.out.println("Error: Invalid Port");

      AlertFactory.showAlert(
          AlertType.ERROR,
          "Port is already in use, try again!",
          () -> serverLogList.getScene().getWindow().hide()
      );
      return;
    }

    server.setName("Server Thread");
    server.setDaemon(true);
    server.start();

    server.addHandler("log", (e) -> Platform.runLater(() -> serverLog.add(e.getBody())));

    server.addHandler("join", (e) -> Platform.runLater(() -> playerList.add(new Player(e.getBody()))));

    server.addHandler("leave", (e) -> {
      for (Player player : playerList) {
        if (player.getName().equals(e.getCaller().getClientName())) {
          Platform.runLater(() -> playerList.remove(player));
          return;
        }
      }
    });
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    playerList = FXCollections.observableArrayList();

    nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));

    clientList.setItems(playerList);

    serverLog = FXCollections.observableArrayList();
    serverLogList.setItems(serverLog);
  }



  @Override
  public void stop() {
    if (server != null) {
      server.close();
    }
  }
}
