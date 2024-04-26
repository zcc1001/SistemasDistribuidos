package es.ubu.lsi.client;

import es.ubu.lsi.common.ChatMessage;

import java.rmi.RemoteException;

public class ChatClientImpl implements ChatClient {

    private int id;
    private final String nickName;

    /**
     * Constructor ChatClientImpl
     *
     * @param nickName nickname de usuario
     */
    public ChatClientImpl(String nickName) {
        this.nickName = nickName;
    }

    /**
     * Establece el id del cliente
     *
     * @return id
     * @throws RemoteException
     */
    @Override
    public int getId() throws RemoteException {
        return id;
    }

    /**
     * Establece el id del cliente
     *
     * @param id id
     * @throws RemoteException
     */
    @Override
    public void setId(int id) throws RemoteException {
        this.id = id;
    }

    /**
     * Recibe el mensaje
     *
     * @param msg message
     * @throws RemoteException
     */
    @Override
    public void receive(ChatMessage msg) throws RemoteException {
        System.out.printf("%s: %s", msg.getNickname(), msg.getMessage());
    }

    /**
     * Obtiene el nickname del cliente
     *
     * @return nickname
     * @throws RemoteException
     */
    @Override
    public String getNickName() throws RemoteException {
        return nickName;
    }
}
