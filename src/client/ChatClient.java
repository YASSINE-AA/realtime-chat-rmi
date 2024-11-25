package client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import models.Message;

public interface ChatClient extends Remote {
    void receiveMessage(Message message) throws RemoteException;
    String getID() throws RemoteException;
    void setID(String clientID) throws RemoteException;
    void setRoom(String roomID) throws RemoteException;
    String getRoom() throws RemoteException;
    void resetRoom() throws RemoteException;
}
