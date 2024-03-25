package es.ubu.lsi.client;

import static es.ubu.lsi.common.ChatMessage.MessageType.*;

import es.ubu.lsi.common.ChatMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ChatClientImpl implements ChatClient {
  private static final int DEFAULT_PORT = 1500;
  private String server;
  private String username;
  private int port;
  private boolean carryOn = true;
  private int id;

  private Socket socket;

  private ObjectInputStream in;
  private ObjectOutputStream out;

  public ChatClientImpl(String server, int port, String username) {
    this.server = server;
    this.port = port;
    this.username = username;
  }

  @Override
  public boolean start() {
    try {
      socket = new Socket(server, port);
      out = new ObjectOutputStream(socket.getOutputStream());
      in = new ObjectInputStream(socket.getInputStream());

      // identificacion con el servidor
      ChatMessage registerUsernameMessage =
          new ChatMessage(0, MESSAGE, String.format("username %s", username));
      sendMessage(registerUsernameMessage);

      // obtenemos el id del cliente dado por el servidor
      ChatMessage confirmationMessage = (ChatMessage) in.readObject();
      id = confirmationMessage.getId();

      // mantenemos la escucha en otro thead
      ChatClientListener chatClientListener = new ChatClientListener();
      Thread thread = new Thread(chatClientListener);
      thread.start();

      return true;
    } catch (Exception e) {
      disconnect();
      System.out.println("ERROR:" + e.getMessage());
      return false;
    }
  }

  @Override
  public void sendMessage(ChatMessage chatMessage) {
    try {
      out.writeObject(chatMessage);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void disconnect() {
    try {
      if (out != null) {
        out.close();
      }
      if (in != null) {
        in.close();
      }
      if (socket != null) {
        socket.close();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public class ChatClientListener implements Runnable {
    @Override
    public void run() {

      while (socket.isConnected()) {
        try {
          ChatMessage chatMessage = (ChatMessage) in.readObject();
          if (chatMessage.getType() == MESSAGE) {
            System.out.println(chatMessage.getMessage());
          }
        } catch (IOException | ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  public static void main(String[] args) {

    // Valores por defecto de la coneccion
    String server = "localhost";
    int port = DEFAULT_PORT;
    String username;

    if (args.length == 0) {
      throw new RuntimeException(
          "ERROR: Se debe indicar al menos un argumento para iniciar el cliente");
    } else if (args.length == 1) {
      // si solo se indica un arg asumimos que es el nickname
      username = args[0];
    } else {
      // si se indican 2+ args reemplazamos los valores por defecto
      // primer argumento indica el server y el segundo el nickname
      server = args[0];
      username = args[1];
    }

    System.out.printf("Bienvenido: %s \n", username);
    System.out.printf("Conectando con server: %s puerto: %d ...\n", server, port);

    ChatClient client = new ChatClientImpl(server, port, username);
    boolean isStarted = client.start();

    if (!isStarted) {
      System.out.println("No se pudo establecer conneccion con el servidor.");
      return;
    }

    System.out.println("Conneccion establecida con el servidor.");
    System.out.println("Introdusca su mensaje:");

    Scanner scanner = new Scanner(System.in);
    while (true) {
      String textToSend = scanner.nextLine();
      String[] textArray = textToSend.split(" ", 2);
      ChatMessage.MessageType messageType = null;
      if ("logout".equalsIgnoreCase(textArray[0])) {
        messageType = LOGOUT;
      } else if ("shutdown".equalsIgnoreCase(textArray[0])) {
        messageType = SHUTDOWN;
      } else {
        messageType = MESSAGE;
      }
      ChatMessage chatMessage = new ChatMessage(0, messageType, textToSend);
      client.sendMessage(chatMessage);

      if (messageType == LOGOUT) {
        client.disconnect();
        break;
      }
    }
  }
}
