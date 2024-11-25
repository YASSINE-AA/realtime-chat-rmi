package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class ChatServerMain {

    public static void main(String[] args) {
        try {
            ChatServerImpl server = new ChatServerImpl();
            LocateRegistry.createRegistry(1900);
            Naming.rebind("rmi://localhost:1900/chatroom", server);
            System.out.println("Chat server is ready.");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
