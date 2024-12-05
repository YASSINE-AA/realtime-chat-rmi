package server;

import client.ChatClient;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import models.Message;
import models.Room;

public class ChatServerImpl extends UnicastRemoteObject implements ChatServer {

    private final List<ChatClient> clients;
    private final Map<Room, List<ChatClient>> rooms;

    public ChatServerImpl() throws RemoteException {
        super();
        clients = Collections.synchronizedList(new ArrayList<>());
        rooms = new ConcurrentHashMap<>();
    }

    @Override
    public void sendMessage(Message message, String clientID) throws RemoteException {
        synchronized (clients) {
            for (ChatClient client : clients) {
                if (client.getUsername().equals(clientID)) {
                    client.receiveMessage(message);
                    return;
                }
            }
        }
        System.out.println("Client with ID " + clientID + " not found.");
    }

    @Override
    public List<String> getOnlineClients() throws RemoteException {
        synchronized (clients) {
            List<String> result = new ArrayList<>();
            for (ChatClient client : clients) {
                result.add(client.getUsername());
            }
            return result;
        }
    }

    @Override
    public void registerClient(ChatClient client) throws RemoteException {
        synchronized (clients) {
            clients.add(client);
        }
        updateOnlineClients(client.getUsername());
        System.out.println("Client<" + client.getUsername() + "> registered.");
    }

    private void updateOnlineClients(String username) throws RemoteException {
        synchronized (clients) {
            for (ChatClient client : clients) {
                client.addOnlineClient(username);
            }
        }
    }

    @Override
    public void deregisterClient(ChatClient client) throws RemoteException {
        synchronized (clients) {
            if (clients.remove(client)) {
                System.out.println("Client<" + client.getUsername() + "> unregistered.");
            } else {
                System.out.println("Failed unregistering client (not found).");
            }
        }
    }

    @Override
    public void sendMessageToRoom(String roomID, Message message) throws RemoteException {
        rooms.forEach((room, members) -> {
            try {
                if (room.getRoomID().equals(roomID)) {
                    synchronized (members) {
                        for (ChatClient client : members) {
                            client.receiveMessage(message);
                        }
                    }
                }
            } catch (RemoteException e) {
                System.err.println("Couldn't send message to Room<" + roomID + ">.");
            }
        });
    }

    @Override
    public void removeMemberFromRoom(String roomID, ChatClient client) throws RemoteException {
        rooms.forEach((room, members) -> {
            if (room.getRoomID().equals(roomID)) {
                synchronized (members) {
                    if (members.remove(client)) {
                        try {
                            System.out.println("Removed client<" + client.getUsername() + "> from room<" + roomID + ">");
 
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean isRoomOwner(String roomID, String clientID) throws RemoteException {
        return rooms.keySet().stream().anyMatch(
            room -> room.getRoomID().equals(roomID) && room.getCreator().equals(clientID)
        );
    }

    @Override
    public void registerRoom(Room room) throws RemoteException {
        rooms.put(room, Collections.synchronizedList(new ArrayList<>()));
        System.out.println("client<" + room.getCreator() + "> registered room <" + room.getRoomID() + ">.");
    }

    @Override
    public void deregisterRoom(String roomID) throws RemoteException {
        rooms.keySet().removeIf(room -> room.getRoomID().equals(roomID));
        System.out.println("Deregistered room<" + roomID + ">");
    }

    @Override
    public boolean doesRoomExist(String roomID) throws RemoteException {
        return rooms.keySet().stream().anyMatch(room -> room.getRoomID().equals(roomID));
    }

    @Override
    public boolean isUsernameTaken(String username) throws RemoteException {
        synchronized (clients) {
            return clients.stream().anyMatch(client -> {
                try {
                    return client.getUsername().equals(username);
                } catch (Exception e) {
                    return true;
                }
            });
        }
    }

    @Override
    public void addMemberToRoom(String roomID, ChatClient member) throws RemoteException {
        rooms.forEach((room, members) -> {
            if (room.getRoomID().equals(roomID)) {
                synchronized (members) {
                    try {
                        members.add(member);
                        System.out.println("Added client<" + member.getUsername() + "> to room<" + roomID + ">");
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                  
                }
            }
        });
    }
}
