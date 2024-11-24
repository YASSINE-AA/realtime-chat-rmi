import java.rmi.*;
import java.rmi.registry.*;
import java.util.ArrayList;

public class ChatServerMain {

    ArrayList<ChatClient> clients = new ArrayList<ChatClient>();

    public static void main(String args[]) {
        try {
            ChatServerImpl obj = new ChatServerImpl();

            LocateRegistry.createRegistry(1900);

            Naming.rebind("rmi://localhost:1900" + "/chatroom", obj);
        } catch (Exception ae) {
            System.out.println(ae);
        }
    }
}
