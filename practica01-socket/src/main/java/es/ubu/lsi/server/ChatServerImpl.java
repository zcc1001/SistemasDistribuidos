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

/** ChatServeImpl. Clase que define la logica del servidor en el chat. */
public class ChatServerImpl implements ChatServer {

  /** Perto por defecto del serviodor */
  private static final int DEFAULT_PORT = 1500;

  /** Contador de id usuarios registrados */
  private Integer clientIdCount = 0;

  /** date format */
  private SimpleDateFormat sdf;

  /** puerto del servidor */
  private int port;

  /** flag mantener vivo el hilo */
  private boolean alive = true;

  /** Mapa para almacenar la relacion hilo clientId */
  private final Map<Integer, ServerThreadForClient> serverThreadForClientMap = new HashMap<>();

  /** Mapa para almacenar la relacion clientId - username */
  private final Map<Integer, String> clientIdAndUsername = new HashMap<>();

  /** Mapa para almacenar la lista de usuarios baneados por cada cliente */
  private final Map<String, Set<String>> usernameAndBannedUsers = new HashMap<>();

  /**
   * Constructor por defecto.
   *
   * @see #DEFAULT_PORT
   */
  public ChatServerImpl() {
    this.port = DEFAULT_PORT;
  }

  /**
   * Constructor por parametros. Inicializa el servidor en el puerto dado.
   *
   * @param port numero de puerto
   */
  public ChatServerImpl(int port) {
    this.port = port;
  }

  /** Iniciliza la comunicacion con el cliente y los streams de datos */
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

  /** Finaliza el servidor y cierra los sockets abiertos. */
  @Override
  public void shutdown() {
    alive = false;
    serverThreadForClientMap.values().forEach(ServerThreadForClient::closeClientSocket);
  }

  /**
   * Broadcast. Envia el mensaje a todos los clientes no baneados.
   *
   * @param chatMessage mensaje a enviar.
   */
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

  /**
   * Remove
   *
   * @param id id
   */
  @Override
  public void remove(int id) {}

  /**
   * ServerTheradForClient. Clase que implementa Thread y gestiona un hilo por cliente conectado.
   */
  public class ServerThreadForClient extends Thread {
    /** id del cliente */
    private int threadClientId;

    /** username */
    private String username;

    /** socket del cliente */
    private Socket threadClientSocket;

    /** stream de salida */
    private ObjectOutputStream objectOutputStream;

    /** stram de entrada */
    private ObjectInputStream objectInputStream;

    /**
     * Constructor
     *
     * @param threadClientId id del cliente
     * @param threadClientSocket socket asignado a el cliente
     */
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

    /** Ejecuta el hilo. Registra el username del usuario y escucha los mensajes recibidos. */
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

    /** Cierra el socket y stream. */
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

    /**
     * Registra un username en el servidor. Se asigna una tupla username, id a este usuario
     * concreto. Usando el primer mensaje recibido como metodo de identificacion. Usando el primer
     * mensaje recibido como metodo de identificacion.
     */
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

    /**
     * Envia mensaje. Escribe en el output stream el mensaje a enviar.
     *
     * @param chatMessage mensaje a enviar.
     */
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

    /** Logout de usuario. cierra la coneccion y streams. */
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

  /**
   * Main. Inicializa el servidor y mantiene la escucha de clientes.
   *
   * @param args parametros. El primer parametro indica el numero de puerto
   */
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
