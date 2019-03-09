package com.dicegame.players;

import com.dicegame.controllers.GameMenuController;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Client {

  public Client(String ip, int port, String displayName) throws IOException {
    this.ip = ip;
    this.port = port;
    this.displayName = displayName;

    FXMLLoader gameFxmlLoader = new FXMLLoader(getClass().getResource("gameMenu.fxml"));
    Parent gameRoot = gameFxmlLoader.load();
    Stage gameStage = new Stage();
    gameStage.setOnHiding(e -> stop());
    gameStage.setScene(new Scene(gameRoot));
    gameStage.show();

    gameMenuController = gameFxmlLoader.getController();
  }

  private String ip;
  private int port;
  private String displayName;

  private GameMenuController gameMenuController;
  private Runnable onHiding;

  public void setOnHiding(Runnable onHiding) {
    this.onHiding = onHiding;
  }

  public void stop() {
    gameMenuController.stop();
    onHiding.run();
  }
}
