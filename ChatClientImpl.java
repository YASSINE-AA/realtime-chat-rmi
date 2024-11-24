import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;

public class ChatClientImpl extends UnicastRemoteObject implements ChatClient {

    private String clientID;

    public ChatClientImpl() throws RemoteException {
        super();
        setID(UUID.randomUUID().toString());
    }

    @Override
    public void receiveMessage(Message message) throws RemoteException {
        System.out.println(
            "Message from " + message.source + ": " + message.content
        );
    }

    @Override
    public void setID(String clientID) throws RemoteException {
        this.clientID = clientID;
    }

    @Override
    public String getID() throws RemoteException {
        return clientID;
    }
}
