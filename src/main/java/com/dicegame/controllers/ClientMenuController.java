package com.dicegame.controllers;

import com.dicegame.chat.content.Message;
import com.dicegame.chat.endpoints.Client;
import com.dicegame.interfaces.Stoppable;
import com.dicegame.utils.AlertFactory;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class ClientMenuController implements Initializable, Stoppable {

  @FXML private ListView<String> messageLog;
  @FXML private TextField messageField;
  @FXML private Button rollButton;
  @FXML private Label statusLabel;

  private Client client;

  void createClient(String ip, int port, String name) {
    try {
      client = new Client(ip, port, name);
      client.setDaemon(true);
      client.setName("Client Thread");
      client.start();
      messageLog.setItems(client.getChatLog());
    } catch (IOException e) {
      System.out.println("Failed to create Client");

      AlertFactory.showAlert(
          AlertType.ERROR,
          "Can't reach host, try again!",
          () -> messageField.getScene().getWindow().hide()
      );
    }
  }

  @FXML
  public void onSend() {
    if (!messageField.getText().equals("")) {
      client.send(new Message("message", messageField.getText()));
      messageField.setText("");
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) { }

  @Override
  public void stop() {
    if (client != null) {
      client.close();
    }
  }

}
