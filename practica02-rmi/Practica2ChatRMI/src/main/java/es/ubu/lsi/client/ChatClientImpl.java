package es.ubu.lsi.client;

import es.ubu.lsi.common.ChatMessage;

import java.rmi.RemoteException;

public class ChatClientImpl implements ChatClient {
    @Override
    public int getId() throws RemoteException {
        return 0;
    }

    @Override
    public void setId(int id) throws RemoteException {

    }

    @Override
    public void receive(ChatMessage msg) throws RemoteException {

    }

    @Override
    public String getNickName() throws RemoteException {
        return "";
    }
}
