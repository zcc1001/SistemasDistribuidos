package es.ubu.lsi.server;

import static es.ubu.lsi.common.ChatMessage.MessageType.MESSAGE;

import es.ubu.lsi.common.ChatMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatServerImpl implements ChatServer {

  private static final int DEFAULT_PORT = 1500;
  private Integer clientIdCount = 0;
  private SimpleDateFormat sdf;
  private int port;
  private boolean alive = true;
  private final Map<Integer, ServerThreadForClient> serverThreadForClientMap = new HashMap<>();
  private final Map<Integer, String> clientIdAndUsername = new HashMap<>();
  private final Map<String, Set<String>> usernameAndBannedUsers = new HashMap<>();

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

        ServerThreadForClient threadForClient =
            new ServerThreadForClient(clientIdCount, clientSocket);
        serverThreadForClientMap.put(clientIdCount, threadForClient);
        clientIdCount++;
        threadForClient.start();

        System.out.printf("Establecida coneccion con clienteId: [%d] \n", clientIdCount);
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
    int clientId = chatMessage.getId();
    String username = clientIdAndUsername.get(clientId);
    Set<String> listOfBannedUsers =
        Optional.ofNullable(usernameAndBannedUsers.get(username)).orElseGet(HashSet::new);

    chatMessage.setMessage(username + ": " + chatMessage.getMessage());
    serverThreadForClientMap.values().stream()
        .filter(client -> !listOfBannedUsers.contains(client.getName()))
        .forEach(client -> client.sendMessage(chatMessage));
  }

  @Override
  public void remove(int id) {}

  public class ServerThreadForClient extends Thread {
    private int threadClientId;
    private String username;
    private Socket threadClientSocket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    public ServerThreadForClient(int threadClientId, Socket threadClientSocket) {
      this.threadClientId = threadClientId;
      this.threadClientSocket = threadClientSocket;
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
      try {
        while (threadClientSocket.isConnected()) {
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
        }
      } catch (IOException | ClassNotFoundException e) {
        System.out.println(Arrays.toString(e.getStackTrace()));
        closeClientSocket();
      }
    }

    private void closeClientSocket() {
      try {
        if (objectOutputStream != null) {
          objectOutputStream.close();
        }
        if (objectInputStream != null) {
          objectInputStream.close();
        }
        if (threadClientSocket != null) {
          threadClientSocket.close();
        }
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
          clientIdAndUsername.put(threadClientId, textArray[1]);
          System.out.printf(
              "cliente con id [%s] username [%s] se ha unido al chat.\n", threadClientId, username);

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
        System.out.printf("Error enviando mensaje a [%s] \n", username);
        System.out.println(e.getMessage());
        remove(threadClientId);
        closeClientSocket();
      }
    }

    private void handleLogout() {
      closeClientSocket();
    }

    private void handleMessage(ChatMessage chatMessage) {
      String[] txt = chatMessage.getMessage().split(" ");
      if (txt.length > 0) {
        if ("ban".equals(txt[0])) {
          Set<String> bannedListForCurrentUser =
              usernameAndBannedUsers.putIfAbsent(username, new HashSet<>());
          // anadimos el usuario a la lista de baneados
          bannedListForCurrentUser.add(txt[1]);
          System.out.printf("[%s] ha baneado a [%s] \n", username, txt[1]);
          return;
        } else if ("unban".equals(txt[0])) {
          Set<String> bannedListForCurrentUser =
              usernameAndBannedUsers.putIfAbsent(username, new HashSet<>());
          if (bannedListForCurrentUser.stream()
              .anyMatch(bannedUser -> bannedUser.equalsIgnoreCase(txt[1]))) {
            bannedListForCurrentUser.remove(txt[1]);
            System.out.printf("[%s] ha des-baneado a [%s] \n", username, txt[1]);
          }
          return;
        } else broadcast(chatMessage);
        return;
      }
    }
  }

  public static void main(String[] args) {
    ChatServer chatServer;

    // primer argumento indica el puerto donde se inicia el server
    if (args.length > 0) {
      System.out.printf("Iniciando servidor en puerto: [%s] ...\n", args[0]);
      chatServer = new ChatServerImpl(Integer.parseInt(args[0]));
    } else {
      // si no viene argumento usamos puerto por defecto
      System.out.printf("Iniciando servidor en puerto: [%s] ...\n", DEFAULT_PORT);
      chatServer = new ChatServerImpl();
    }
    System.out.println("Esperando clientes...");
    chatServer.starup();
  }
}
