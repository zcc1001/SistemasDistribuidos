package es.ubu.lsi.server;

import java.rmi.Naming;

public class ChatServerStarter {

    /**
     * Constructor ChatServerStarter
     */
    public ChatServerStarter() {
        try {
            // Instancia el objeto
            ChatServer server = new ChatServerImpl();
            System.out.println("Objecto instanciado: " + server);

            // Ligado del objeto remoto en el registro
            Naming.rebind("/Servidor", server);
            System.out.println("Servidor registrado...");
        } catch (Exception e) {
            System.err.println("Excepción recogida: " + e.toString());
        }
    }
}
