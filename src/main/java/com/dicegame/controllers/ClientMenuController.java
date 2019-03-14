package com.dicegame.controllers;

import com.dicegame.chat.content.Player;
import com.dicegame.chat.endpoints.Client;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ClientMenuController implements Initializable, Stoppable {

  @FXML private ListView<String> chatLogList;
  @FXML private TextField messageField;
  @FXML private Button rollButton;
  @FXML private Label statusLabel;
  @FXML private TableView<Player> playerList;
  @FXML private TableColumn<Player, String> nameColumn;
  @FXML private TableColumn<Player, Integer> scoreColumn;

  private Client client;
  private ObservableList<String> chatLog;
  private ObservableList<Player> clientList;

  void createClient(String ip, int port, String name) {
    try {
      client = new Client(ip, port, name);
    } catch (IOException e) {
      System.out.println("Failed to create Client");

      AlertFactory.showAlert(
          AlertType.ERROR,
          "Can't reach host, try again!",
          () -> messageField.getScene().getWindow().hide()
      );
      return;
    }

    client.setDaemon(true);
    client.setName("Client Thread");
    client.start();

    client.addHandler("message", (e) -> Platform.runLater(() -> chatLog.add(e.getBody())));

    client.addHandler("join", (e) -> Platform.runLater(() -> {
      addPlayer(e.getBody());
      chatLog.add(e.getBody() + " has joined!");
    }));

    client.addHandler("leave", (e) -> Platform.runLater(() -> {
      removeByName(e.getBody());
      chatLog.add(e.getBody() + " has disconnected!");
    }));

    client.addHandler("error", (e) -> Platform.runLater(() -> {
      try {
        e.getCaller().getClientSocket().close();
      } catch (IOException ioe) {
        chatLog.add("Error: Failed to close socket");
      }

      switch (e.getBody()) {
        case "ReachedMaxRoom":
          chatLog.add("Room is full, please try again later!");
          break;
        case "NoName":
          chatLog.add("No name specified, try again!");
          break;
      }
    }));
  }

  @FXML
  public void onSend() {
    if (!messageField.getText().equals("")) {
      client.send("message", messageField.getText());
      messageField.setText("");
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    clientList = FXCollections.observableArrayList();

    nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));

    playerList.setItems(clientList);

    chatLog = FXCollections.observableArrayList();
    chatLogList.setItems(chatLog);
  }

  private void addPlayer(String name) {
    clientList.add(new Player(name));
  }

  private void removeByName(String name) {
    for (Player player : clientList) {
      if (player.getName().equals(name)) {
        clientList.remove(player);
        return;
      }
    }
  }


  @Override
  public void stop() {
    if (client != null) {
      client.close();
    }
  }

}
