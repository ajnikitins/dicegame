package com.dicegame.chat.endpoints;

import com.dicegame.chat.content.Message;
import com.dicegame.chat.events.Event;
import com.dicegame.chat.events.EventManager;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class Pipe extends Thread {

  private Socket socket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private EventManager<Pipe> eventManager;
  private CopyOnWriteArrayList<Pipe> pipes;
  private String pipeName = "";

  private Consumer<Pipe> onClose = (pipe) -> {};

  Pipe(Socket socket, EventManager<Pipe> eventManager) throws IOException {
    this.socket = socket;
    this.eventManager = eventManager;

    this.pipes = new CopyOnWriteArrayList<>();
    this.pipes.add(this);

    this.out = new ObjectOutputStream(socket.getOutputStream());
    this.in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
  }

  Pipe(Socket socket, EventManager<Pipe> eventManager, CopyOnWriteArrayList<Pipe> pipes) throws IOException {
    this.eventManager = eventManager;
    this.socket = socket;
    this.pipes = pipes;

    this.out = new ObjectOutputStream(socket.getOutputStream());
    this.in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
  }

  public Socket getSocket() {
    return socket;
  }

  void setPipeName(String pipeName) {
    this.pipeName = pipeName + " - " + socket.getPort();
  }

  public String getPipeName() {
    return pipeName;
  }

  void setOnClose(Consumer<Pipe> onClose) {
    this.onClose = onClose;
  }

  void sendAll(String command, String body) {
    pipes.forEach((pipe -> pipe.send(command, body)));
  }

  @Override
  public void run() {
    try {
      while (!isInterrupted()) {
        eventManager.handle(new Event<>(this, (Message) in.readObject()));
      }
    } catch (SocketException e) {
      eventManager.log("Disconnected");
    } catch (IOException | ClassNotFoundException e) {
      eventManager.log("Error: Failed to receive message");
    }
  }

  public void send(String command, String body) {
    try {
      if (!socket.isClosed()) {
        out.writeObject(new Message(command, body));
      }
    } catch (IOException e) {
      eventManager.log("Error: Failed to send message");
      e.printStackTrace();
    }
  }

  public void close() {
    onClose.accept(this);
    try {
      socket.close();
    } catch (IOException e) {
      eventManager.log("Error: Failed to close socket");
    }
    interrupt();
  }
}
