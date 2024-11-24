import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatClient extends Remote {
    void receiveMessage(Message message) throws RemoteException;
    String getID() throws RemoteException;
    void setID(String clientID) throws RemoteException;
}
