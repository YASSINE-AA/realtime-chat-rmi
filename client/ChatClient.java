package client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import models.Message;

public interface ChatClient extends Remote {
    void receiveMessage(Message message) throws RemoteException;
    String getID() throws RemoteException;
    void setID(String clientID) throws RemoteException;
}
