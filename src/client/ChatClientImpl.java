package client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import models.Message;

public class ChatClientImpl extends UnicastRemoteObject implements ChatClient {

    private String clientID;
    private String joinedRoom;
    private final List<Message> messageQueue;
    private Consumer<Message> messageListener; 

    public ChatClientImpl() throws RemoteException {
        super();
        messageQueue = new ArrayList<>();
        setID("Client-" + System.currentTimeMillis());
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
    public void setID(String clientID) throws RemoteException {
        this.clientID = clientID;
    }

    @Override
    public String getID() throws RemoteException {
        return clientID;
    }

    @Override
    public void resetRoom() throws RemoteException {
        this.joinedRoom = null;
    }

    public void setOnMessageReceivedListener(Consumer<Message> listener) {
        this.messageListener = listener;
    }
}
