import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;

public class ChatClientImpl extends UnicastRemoteObject implements ChatClient {

    private String clientID;

    ChatClientImpl() throws RemoteException {
        // Generate random ID
        setID(UUID.randomUUID().toString());
    }

    public void receiveMessage(Message message) throws RemoteException {
        System.out.println(message);
    }

    public void setID(String clientID) throws RemoteException {
        this.clientID = clientID;
    }

    public String getID() throws RemoteException {
        return clientID;
    }
}
