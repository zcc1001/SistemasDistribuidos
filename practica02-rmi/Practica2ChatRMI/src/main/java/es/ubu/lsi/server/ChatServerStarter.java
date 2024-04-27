package es.ubu.lsi.server;

import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.server.RMIClassLoader;
import java.util.Properties;

public class ChatServerStarter {

    /**
     * Constructor ChatServerStarter
     */
    public ChatServerStarter() {
        try {
            Properties properties = System.getProperties();
            // lee el goodbyes
            String url = properties.getProperty("java.rmi.server.codebase");
            // Cargador de clases dinámico ...
            Class<?> serverclass = RMIClassLoader.loadClass(url, "es.ubu.lsi.server.ChatServerImpl");
            // Ligado del objeto remoto en el registro
            Naming.rebind("/Servidor", (Remote) serverclass.newInstance());
            System.out.println("Servidor registrado...");
        } catch (Exception e) {
            System.err.println("Excepción recogida: " + e.toString());
        }
    }
}
