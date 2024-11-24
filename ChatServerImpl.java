import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class ChatServerImpl extends UnicastRemoteObject implements ChatServer {

    ArrayList<ChatClientImpl> clients = new ArrayList<ChatClientImpl>();

    ChatServerImpl() throws RemoteException {}

    public void sendMessage(Message message, String clientID)
        throws RemoteException {
        for (ChatClientImpl client : clients) {
            if (client.getID() == clientID) {
                client.receiveMessage(message);
            }
        }
    }

    public void registerClient(ChatClientImpl client) throws RemoteException {
        clients.add(client);
    }

    public void deregisterClient(ChatClientImpl client) throws RemoteException {
        if (clients.remove(client)) System.out.println("Client Unregistered.");
        else System.out.println("Failed unregisterning (404).");
    }
}
