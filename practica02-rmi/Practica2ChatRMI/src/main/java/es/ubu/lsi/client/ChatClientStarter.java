package es.ubu.lsi.client;

import es.ubu.lsi.common.ChatMessage;
import es.ubu.lsi.server.ChatServer;

import java.rmi.Naming;
import java.util.Scanner;

public class ChatClientStarter {
    /**
     * Constructor ChatClientStarter
     *
     * @param args argumentos de entrada
     */
    public ChatClientStarter(String[] args) {
        String nickname = args[0];
        String serverHost = (args.length > 1) ? args[1] : "localhost";
        try {
            System.out.println("Resolviendo en: " + serverHost);
            ChatServer server = (ChatServer) Naming.lookup("//" + serverHost + "/Servidor");

            ChatClientImpl client = new ChatClientImpl(nickname);

            int clientId = server.checkIn(client);

            client.setId(clientId);

            Scanner scanner = new Scanner(System.in);
            while (true) {
                String textToSend = scanner.nextLine();
                String[] textArray = textToSend.split(" ", 2);
                if ("logout".equalsIgnoreCase(textArray[0])) {
                    server.logout(client);
                    break;
                } else if ("shutdown".equalsIgnoreCase(textArray[0])) {
                    server.shutdown(client);
                    break;
                } else {
                    ChatMessage chatMessage = new ChatMessage(client.getId(), client.getNickName(), textToSend);
                    server.publish(chatMessage);
                }
            }

        } catch (Exception e) {
            System.err.println("Excepción recogida en cliente: " + e.toString());
            e.printStackTrace();
        }

    }
}
