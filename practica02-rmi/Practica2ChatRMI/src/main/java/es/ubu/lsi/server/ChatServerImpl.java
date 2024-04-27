package es.ubu.lsi.server;

import es.ubu.lsi.client.ChatClient;
import es.ubu.lsi.common.ChatMessage;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class ChatServerImpl implements ChatServer {
    private final Map<Integer, ChatClient> clients;
    private int clientIdCounter = 0;

    /**
     * Constructor ChatServerImpl
     */
    public ChatServerImpl() throws RemoteException {
        super();
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

        System.out.println(
                String.format("Client: [%s] id: [%d] registrado correctamente.  \n \n", client.getNickName(), client.getId()));
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
        for (ChatClient c : clients.values()) {
            if (c.getId() != msg.getId()) {
                c.receive(msg);
            }
            System.out.println(String.format("Enviando mensaje al cliente [%s]...", c.getNickName()));
        }
    }

    @Override
    public void shutdown(ChatClient client) throws RemoteException {
    }
}
