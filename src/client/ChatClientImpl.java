package client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import models.Message;

public class ChatClientImpl extends UnicastRemoteObject implements ChatClient {

    private String username;
    private String joinedRoom;
    private final List<Message> messageQueue;
    private List<String> onlineClients;
    private Consumer<Message> messageListener; 
    private Consumer<String> clientsListener; 

    public ChatClientImpl() throws RemoteException {
        super();
        messageQueue = new ArrayList<>();
        onlineClients = new ArrayList<>();
    }

    @Override
    public void setRoom(String roomID) {
        this.joinedRoom = roomID;
        System.out.println("You have joined Room<" + roomID + ">.");
    }

    @Override
    public String getRoom() {
        return joinedRoom;
    }


    @Override
    public void receiveMessage(Message message) throws RemoteException {
        synchronized (messageQueue) {
            messageQueue.add(message);
        }
        if (messageListener != null) {
            messageListener.accept(message);
        }
    }

 

    @Override
    public void resetRoom() throws RemoteException {
        this.joinedRoom = null;
    }

    public void setOnMessageReceivedListener(Consumer<Message> listener) {
        this.messageListener = listener;
    }


    public void setClientsListener(Consumer<String> listener) {
        this.clientsListener = listener;
    }
    
    @Override
    public void addOnlineClient(String username) throws RemoteException {
        onlineClients.add(username);
        if (clientsListener != null) {
            clientsListener.accept(username);
        }
    }

    @Override
    public String getUsername() throws RemoteException
     {return username;}

    @Override
    public void setUsername(String username)  throws RemoteException
    {this.username = username;}
}
