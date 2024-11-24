import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatServer extends Remote {
    void sendMessage(Message message, String clientID) throws RemoteException;
    void registerClient(ChatClient client) throws RemoteException;
    void deregisterClient(ChatClient client) throws RemoteException;
}
