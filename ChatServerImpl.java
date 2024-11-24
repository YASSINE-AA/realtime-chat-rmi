import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class ChatServerImpl extends UnicastRemoteObject implements ChatServer {

    private final List<ChatClient> clients;

    public ChatServerImpl() throws RemoteException {
        super();
        clients = new ArrayList<>();
    }

    @Override
    public void sendMessage(Message message, String clientID)
        throws RemoteException {
        for (ChatClient client : clients) {
            if (client.getID().equals(clientID)) {
                client.receiveMessage(message);
                return;
            }
        }
        System.out.println("Client with ID " + clientID + " not found.");
    }

    @Override
    public void registerClient(ChatClient client) throws RemoteException {
        clients.add(client);
        System.out.println("Client<" + client.getID() + "> registered.");
    }

    @Override
    public void deregisterClient(ChatClient client) throws RemoteException {
        if (clients.remove(client)) {
            System.out.println("Client<" + client.getID() + "> unregistered.");
        } else {
            System.out.println("Failed unregistering client (not found).");
        }
    }
}
