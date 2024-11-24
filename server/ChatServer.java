package server;

import client.ChatClient;
import java.rmi.Remote;
import java.rmi.RemoteException;
import models.Message;

public interface ChatServer extends Remote {
    void sendMessage(Message message, String clientID) throws RemoteException;
    void registerClient(ChatClient client) throws RemoteException;
    void deregisterClient(ChatClient client) throws RemoteException;
}
