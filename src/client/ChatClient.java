package client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import models.Message;

public interface ChatClient extends Remote {
    void receiveMessage(Message message) throws RemoteException;
    
    void setRoom(String roomID) throws RemoteException;
    String getRoom() throws RemoteException;
    void resetRoom() throws RemoteException;
    void setUsername(String username) throws RemoteException;
    String getUsername() throws RemoteException;
    void addOnlineClient(String username) throws RemoteException;
}
