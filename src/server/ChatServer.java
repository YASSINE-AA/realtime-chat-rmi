package server;

import client.ChatClient;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import models.Message;
import models.Room;

public interface ChatServer extends Remote {


    void sendMessage(Message message, String clientID) throws RemoteException;

    void registerClient(ChatClient client) throws RemoteException;

    void deregisterClient(ChatClient client) throws RemoteException;

    List<String> getOnlineClients() throws RemoteException;

    void addMemberToRoom(String roomID, ChatClient member) throws RemoteException;

    void registerRoom(Room room) throws RemoteException;

    void sendMessageToRoom(String roomID, Message message) throws RemoteException;
    void deregisterRoom(String roomID) throws RemoteException;
}
