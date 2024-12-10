package server;

import client.ChatClient;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import models.Message;
import models.Room;

public class ChatServerImpl extends UnicastRemoteObject implements ChatServer {

    private final List<ChatClient> clients;
    private final Map<Room, List<ChatClient>> rooms;
    private final ExecutorService clientThreadPool;

    public ChatServerImpl() throws RemoteException {
        super();
        clients = Collections.synchronizedList(new ArrayList<>());
        rooms = new ConcurrentHashMap<>();
        clientThreadPool = Executors.newCachedThreadPool(); 
    }

    @Override
    public void registerClient(ChatClient client) throws RemoteException {
        synchronized (clients) {
            clients.add(client);
        }
        updateOnlineClients(client.getUsername());
        System.out.println("Client<" + client.getUsername() + "> connecté.");

        clientThreadPool.submit(() -> handleClient(client));
    }

    private void handleClient(ChatClient client) {
        try {
            String username = client.getUsername();
            System.out.println("Lancement du thread pour le client<" + username + ">.");
            while (clients.contains(client)) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la gestion du client : " + e.getMessage());
        } finally {
            try {
                deregisterClient(client);
            } catch (RemoteException e) {
                System.err.println("Erreur lors de la désinscription du client : " + e.getMessage());
            }
        }
    }

    @Override
    public void deregisterClient(ChatClient client) throws RemoteException {
        synchronized (clients) {
            if (clients.remove(client)) {
                System.out.println("Client<" + client.getUsername() + "> désinscrit.");
            } else {
                System.out.println("Échec de la désinscription du client (non trouvé).");
            }
        }
    }

    @Override
    public void sendMessage(Message message, String clientID) throws RemoteException {
        clientThreadPool.submit(() -> {
            synchronized (clients) {
                for (ChatClient client : clients) {
                    try {
                        if (client.getUsername().equals(clientID)) {
                            client.receiveMessage(message);
                            System.out.println("Message envoyé au client<" + clientID + ">.");
                            return;
                        }
                    } catch (RemoteException e) {
                        System.err.println("Erreur lors de l'envoi du message au client<" + clientID + "> : " + e.getMessage());
                    }
                }
            }
            System.out.println("Client avec l'ID " + clientID + " introuvable.");
        });
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

    private void updateOnlineClients(String username) throws RemoteException {
        clientThreadPool.submit(() -> {
            synchronized (clients) {
                for (ChatClient client : clients) {
                    try {
                        client.addOnlineClient(username);
                    } catch (RemoteException e) {
                        System.err.println("Erreur lors de la mise à jour du client en ligne<" + username + "> : " + e.getMessage());
                    }
                }
            }
        });
    }

    @Override
    public void sendMessageToRoom(String roomID, Message message) throws RemoteException {
        clientThreadPool.submit(() -> {
            rooms.forEach((room, members) -> {
                if (room.getRoomID().equals(roomID)) {
                    synchronized (members) {
                        for (ChatClient client : members) {
                            try {
                                client.receiveMessage(message);
                            } catch (RemoteException e) {
                                System.err.println("Erreur lors de l'envoi du message à la salle<" + roomID + "> : " + e.getMessage());
                            }
                        }
                    }
                }
            });
        });
    }

    @Override
    public void addMemberToRoom(String roomID, ChatClient member) throws RemoteException {
        clientThreadPool.submit(() -> {
            rooms.forEach((room, members) -> {
                if (room.getRoomID().equals(roomID)) {
                    synchronized (members) {
                        members.add(member);
                        try {
                            System.out.println("Client<" + member.getUsername() + "> ajouté à la salle<" + roomID + ">.");
                        } catch (RemoteException e) {
                            System.err.println("Erreur lors de la récupération du nom d'utilisateur pour le membre : " + e.getMessage());
                        }
                    }
                }
            });
        });
    }

    @Override
    public void removeMemberFromRoom(String roomID, ChatClient client) throws RemoteException {
        clientThreadPool.submit(() -> {
            rooms.forEach((room, members) -> {
                if (room.getRoomID().equals(roomID)) {
                    synchronized (members) {
                        if (members.remove(client)) {
                            try {
                                System.out.println("Client<" + client.getUsername() + "> retiré de la salle<" + roomID + ">.");
                            } catch (RemoteException e) {
                                System.err.println("Erreur lors de la récupération du nom d'utilisateur pour le client : " + e.getMessage());
                            }
                        }
                    }
                }
            });
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
        synchronized (rooms) {
            rooms.put(room, Collections.synchronizedList(new ArrayList<>()));
            System.out.println("Client<" + room.getCreator() + "> a enregistré la salle<" + room.getRoomID() + ">.");
        }
    }

    @Override
    public void deregisterRoom(String roomID) throws RemoteException {
        synchronized (rooms) {
            rooms.keySet().removeIf(room -> room.getRoomID().equals(roomID));
            System.out.println("Salle<" + roomID + "> désenregistrée.");
        }
    }

    @Override
    public boolean doesRoomExist(String roomID) throws RemoteException {
        synchronized (rooms) {
            return rooms.keySet().stream().anyMatch(room -> room.getRoomID().equals(roomID));
        }
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

    public void shutdown() {
        clientThreadPool.shutdown();
        System.out.println("Arrêt du serveur...");
    }
}
