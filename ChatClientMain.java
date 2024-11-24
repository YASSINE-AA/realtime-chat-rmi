import java.rmi.*;

public class ChatClientMain {

    public static void main(String args[]) {
        try {
            ChatClientImpl client = new ChatClientImpl();
            ChatServer access = (ChatServer) Naming.lookup(
                "rmi://localhost:1900" + "/chatroom"
            );
            access.registerClient(client); // register current client
        } catch (Exception ae) {
            System.out.println(ae);
        }
    }
}
