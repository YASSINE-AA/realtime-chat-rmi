package server;

import client.ChatClient;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.Message;
import models.Room;

public class ChatServerImpl extends UnicastRemoteObject implements ChatServer {

    public final List<ChatClient> clients;
    public final Map<Room, List<ChatClient>> rooms;

    public ChatServerImpl() throws RemoteException {
        super();
        clients = new ArrayList<>();
        rooms = new HashMap<>();
    }

    @Override
    public void sendMessage(Message message, String clientID)
            throws RemoteException {
     
        System.out.println("sending to" + clientID);
        for (ChatClient client : clients) {
            if (client.getID().equals(clientID)) {
                client.receiveMessage(message);
                return;
            }
        }
        System.out.println("Client with ID " + clientID + " not found.");
    }

    @Override
    public List<String> getOnlineClients() throws RemoteException {
        List<String> result = new ArrayList<>();
        for (ChatClient client : clients) {
            result.add(client.getID());
        }
        return result;
    }

    @Override
    public void registerClient(ChatClient client) throws RemoteException {
        clients.add(client);
        System.out.println("Client<" + client.getID() + "> registered.");
    }

    @Override
    public void deregisterClient(ChatClient client) throws RemoteException {
        if (clients.remove(client)) {
            System.out.println("Client<" + client.getID() + "> unregistered.");
        } else {
            System.out.println("Failed unregistering client (not found).");
        }
    }

    @Override
    public void sendMessageToRoom(String roomID, Message message) throws RemoteException  {
        rooms.forEach((key, value) -> {
            try {
                if(key.getRoomID().equals(roomID)) {
                    for(ChatClient client: value) {
                        client.receiveMessage(message);
                    }
                }
            } catch (RemoteException e) {
                System.err.println("Couldn't send message to Room<" +roomID+ ">.");
            }
              
           
        });
    }

    @Override
    public void registerRoom(Room room) throws RemoteException {
        rooms.put(room, new ArrayList<>());
        System.out.println("client<" + room.getCreator() + "> registered room <" + room.getRoomID() + ">.");
    }

    @Override
    public void deregisterRoom(String roomID) throws RemoteException {
        rooms.forEach((key, value) -> {
            if(key.getRoomID().equals(roomID)) rooms.remove(key);
        });
    }

    @Override
    public void addMemberToRoom(String roomID, ChatClient member) throws RemoteException {

        rooms.forEach(
                (key, value) -> {
                    try {
                        if (key.getRoomID().equals(roomID)) {
                            value.add(member);
                            System.out.println("Added client<" + member.getID() + "> to room<" + roomID + ">");
                        }
                    } catch (Exception e) {
                        System.out.println("Couldn't add client to room.");
                    }
                }
        );

    }

}
