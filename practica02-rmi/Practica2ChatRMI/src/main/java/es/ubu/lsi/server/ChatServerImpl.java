package es.ubu.lsi.server;

import es.ubu.lsi.client.ChatClient;
import es.ubu.lsi.common.ChatMessage;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChatServerImpl extends UnicastRemoteObject implements ChatServer {
    private static final long serialVersionUID = 1234L;
    private final Map<Integer, ChatClient> clients;
    private final Map<String, Integer> clientsNicnamesAndIds;
    private final Map<Integer, Set<String>> bannedUsersByClient;
    private int clientIdCounter = 0;

    /**
     * Constructor ChatServerImpl
     */
    public ChatServerImpl() throws RemoteException {
        super();
        this.clientsNicnamesAndIds = new HashMap<>();
        this.bannedUsersByClient = new HashMap<>();
        this.clients = new HashMap<>();
    }

    /**
     * Registra cliente en el servidor
     *
     * @param client client
     * @return client id
     * @throws RemoteException
     */
    @Override
    public int checkIn(ChatClient client) throws RemoteException {
        client.setId(clientIdCounter++);
        clients.put(client.getId(), client);
        clientsNicnamesAndIds.put(client.getNickName(), client.getId());
        bannedUsersByClient.put(client.getId(), new HashSet<>());

        System.out.printf("Client: [%s] id: [%d] registrado correctamente.%n", client.getNickName(), client.getId());
        return client.getId();
    }

    /**
     * Elimina a un cliente del servidor
     *
     * @param client current client
     * @throws RemoteException
     */
    @Override
    public void logout(ChatClient client) throws RemoteException {
        clients.entrySet().removeIf(entry -> entry.getValue().equals(client));
    }

    /**
     * Publica un mesaje en el cliente
     *
     * @param msg message
     * @throws RemoteException
     */
    @Override
    public void publish(ChatMessage msg) throws RemoteException {
        ChatClient chatClient = clients.get(msg.getId());

        // Comandos de baneo
        Set<String> clientsToBan = this.bannedUsersByClient.getOrDefault(msg.getId(), new HashSet<>());
        String[] textArray = msg.getMessage().split(" ", 2);
        if ("ban".equalsIgnoreCase(textArray[0])) {
            clientsToBan.add(textArray[1].trim());
            System.out.printf("Cliente: [%s] ha baneado a [%s]%n", chatClient.getNickName(), textArray[1]);
            return;
        } else if ("unban".equalsIgnoreCase(textArray[0])) {
            clientsToBan.remove(textArray[1].trim());
            System.out.printf("Cliente: [%s] ha des-baneado a [%s]%n", chatClient.getNickName(), textArray[1]);
            return;
        }

        // envio de mensajes
        for (ChatClient c : clients.values()) {
            Set<String> bannedForActualClient = bannedUsersByClient.get(c.getId());
            if (!bannedForActualClient.contains(msg.getNickname()) && c.getId() != msg.getId()) {
                c.receive(msg);
                System.out.printf("[%s] envia mensaje al cliente [%s]...%n", chatClient.getNickName(), c.getNickName());
            } else {
                System.out.printf("[%s] baneado [%s]...%n", chatClient.getNickName(), c.getNickName());
            }
        }
    }

    @Override
    public void shutdown(ChatClient client) throws RemoteException {
    }
}
