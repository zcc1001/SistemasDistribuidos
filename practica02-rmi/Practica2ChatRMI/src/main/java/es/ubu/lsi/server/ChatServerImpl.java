package es.ubu.lsi.server;

import es.ubu.lsi.client.ChatClient;
import es.ubu.lsi.common.ChatMessage;

import java.rmi.RemoteException;

public class ChatServerImpl implements ChatServer {

    @Override
    public int checkIn(ChatClient client) throws RemoteException {
        return 0;
    }

    @Override
    public void logout(ChatClient client) throws RemoteException {

    }

    @Override
    public void publish(ChatMessage msg) throws RemoteException {

    }

    @Override
    public void shutdown(ChatClient client) throws RemoteException {

    }
}
