package es.ubu.lsi.server;

import static es.ubu.lsi.common.ChatMessage.MessageType.MESSAGE;

import es.ubu.lsi.common.ChatMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class ChatServerImpl implements ChatServer {

  private static final int DEFAULT_PORT = 1500;
  private Integer clientId = 0;
  private SimpleDateFormat sdf;
  private int port;
  private boolean alive = true;

  private final Map<Integer, ServerThreadForClient> serverThreadForClientMap = new HashMap<>();
  private final Map<String, Integer> usernameAndClientId = new HashMap<>();

  public ChatServerImpl() {
    this.port = DEFAULT_PORT;
  }

  public ChatServerImpl(int port) {
    this.port = port;
  }

  @Override
  public void starup() {
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      while (alive) {
        Socket clientSocket = serverSocket.accept(); // aceptamos la coneccion de un cliente

        ServerThreadForClient threadForClient = new ServerThreadForClient(clientId, clientSocket);
        serverThreadForClientMap.put(clientId, threadForClient);
        clientId++;
        threadForClient.start();

        System.out.printf("Establecida coneccion con clienteId: %d%n", clientId);
      }

    } catch (IOException e) {
      shutdown();
      System.out.println("Error creating server socket.");
      throw new RuntimeException(e);
    }
  }

  @Override
  public void shutdown() {
    alive = false;
    serverThreadForClientMap.values().forEach(ServerThreadForClient::closeClientSocket);
  }

  @Override
  public void broadcast(ChatMessage chatMessage) {
    serverThreadForClientMap.values().forEach(client -> client.sendMessage(chatMessage));
  }

  @Override
  public void remove(int id) {}

  public class ServerThreadForClient extends Thread {
    private int threadClientId;
    private String username;
    private Socket threadClientSocket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private final Map<String, Integer> bannedClients;

    public ServerThreadForClient(int threadClientId, Socket threadClientSocket) {
      this.threadClientId = threadClientId;
      this.threadClientSocket = threadClientSocket;
      this.bannedClients = new HashMap<>();
      try {
        objectOutputStream = new ObjectOutputStream(threadClientSocket.getOutputStream());
        objectInputStream = new ObjectInputStream(threadClientSocket.getInputStream());
      } catch (IOException e) {
        System.out.println("Error creando inputStream o outputStream");
        throw new RuntimeException(e);
      }
    }

    public void run() {
      // obtememos el username del cliente
      registerUsername();

      // recibimos los mensajes del cliente y ejecutamos la accion correspondiente
      while (threadClientSocket.isConnected()) {
        try {
          ChatMessage message = (ChatMessage) objectInputStream.readObject();
          switch (message.getType()) {
            case MESSAGE:
              handleMessage(message);
              break;
            case SHUTDOWN:
              break;
            case LOGOUT:
              handleLogout();
              break;
          }
        } catch (IOException | ClassNotFoundException e) {
          closeClientSocket();
          throw new RuntimeException(e);
        }
      }
    }

    private void closeClientSocket() {
      try {
        objectOutputStream.close();
        objectInputStream.close();
        threadClientSocket.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    private void registerUsername() {
      try {
        ChatMessage chatMessage = (ChatMessage) objectInputStream.readObject();
        String[] textArray = chatMessage.getMessage().split(" ");
        if ("username".equalsIgnoreCase(textArray[0])) {
          username = textArray[1];
          usernameAndClientId.put(textArray[1], threadClientId);
          System.out.printf("%s se ha unido al chat\n", username);

          ChatMessage confirmationMessage =
              new ChatMessage(threadClientId, MESSAGE, "cliente registrado correctamente");
          sendMessage(confirmationMessage);
        } else {
          throw new RuntimeException(
              "Mensaje inespereado, el primer mensaje debe contener el username");
        }
      } catch (IOException | ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }

    private void sendMessage(ChatMessage chatMessage) {
      try {
        objectOutputStream.writeObject(chatMessage);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    private void handleLogout() {
      closeClientSocket();
    }

    private void handleMessage(ChatMessage chatMessage) {
      String[] txt = chatMessage.getMessage().split(" ");
      if (txt.length > 0) {
        if ("ban".equals(txt[0])) {
          bannedClients.put(txt[1], chatMessage.getId());
          System.out.printf("%s ha baneado a %s \n", username, txt[1]);
          return;
        } else if ("unban".equals(txt[0])) {
          if (bannedClients.get(txt[1]) != null) {
            bannedClients.remove(txt[1]);
            System.out.printf("%s ha des-baneado a %s \n", username, txt[1]);
          }
          return;
        } else if ("broadcast".equals(txt[0])) {
          broadcast(chatMessage);
          return;
        } else {
          if (bannedClients.get(txt[0]) != null) {
            // si el cliente objetivo esta banneado
            return;
          }
          ServerThreadForClient threadForClient = serverThreadForClientMap.get(chatMessage.getId());
          if (threadForClient == null) {
            // cliente no registrado en el server
            System.out.printf(
                "El ciente %s intento enviar un mesaje a el cliente no registrado %s \n",
                username, txt[0]);
            return;
          }
          threadForClient.sendMessage(chatMessage);
          return;
        }
      }
    }
  }

  public static void main(String[] args) {
    ChatServer chatServer;

    System.out.println("Esperando clientes.");
    // primer argumento indica el puerto donde se inicia el server
    if (args.length > 0) {
      chatServer = new ChatServerImpl(Integer.parseInt(args[0]));
    } else {
      // si no viene argumento usamos puerto por defecto
      chatServer = new ChatServerImpl();
    }

    chatServer.starup();
  }
}
