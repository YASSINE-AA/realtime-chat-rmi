import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatServer extends Remote {
    void sendMessage(Message message, String clientName) throws RemoteException;
    void registerClient(ChatClientImpl client) throws RemoteException;
    void deregisterClient(ChatClientImpl client) throws RemoteException;
}
