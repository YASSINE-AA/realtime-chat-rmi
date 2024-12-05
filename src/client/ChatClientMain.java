package client;

import java.awt.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import models.Message;
import models.Room;
import server.ChatServer;

public class ChatClientMain {

    private ChatServer server;
    private ChatClientImpl client;
    private boolean isInRoom = false;
    private final List<String> onlineClients = new ArrayList<>();
    private final List<String> joinedRooms = new ArrayList<>();
    private String currentRoom = null;
    private String privateRecipient = null;
    private JTextArea messagesArea;
    private DefaultListModel<String> onlineClientsModel;
    private DefaultListModel<String> joinedRoomsModel;

    public ChatClientMain() {
        try {
            client = new ChatClientImpl();
            server = (ChatServer) Naming.lookup("rmi://localhost:1900/chat");
            onlineClients.addAll(server.getOnlineClients());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                null,
                "Failed to connect to server: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        }
    }

    public void start() {
        JFrame frame = new JFrame("Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        
        // choose username
        setUsername(frame);
        JPanel mainPanel = new JPanel(new BorderLayout());

        messagesArea = new JTextArea();
        messagesArea.setEditable(false);
        JScrollPane messagesScrollPane = new JScrollPane(messagesArea);
        mainPanel.add(messagesScrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField inputField = new JTextField();
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage(inputField.getText()));
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        JPanel sidebarPanel = new JPanel(new BorderLayout());

        onlineClientsModel = new DefaultListModel<>();
        onlineClients.forEach(onlineClientsModel::addElement);
        JList<String> onlineClientsList = new JList<>(onlineClientsModel);
        onlineClientsList.setBorder(
            BorderFactory.createTitledBorder("Online Clients")
        );
        onlineClientsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedClient = onlineClientsList.getSelectedValue();
                try {
                    if (selectedClient != null) switchToPrivateMessaging(
                        selectedClient
                    );
                } catch (Exception ex) {}
            }
        });
        sidebarPanel.add(
            new JScrollPane(onlineClientsList),
            BorderLayout.NORTH
        );

        joinedRoomsModel = new DefaultListModel<>();
        JList<String> joinedRoomsList = new JList<>(joinedRoomsModel);
        joinedRoomsList.setBorder(
            BorderFactory.createTitledBorder("Joined Rooms")
        );
        joinedRoomsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedRoom = joinedRoomsList.getSelectedValue();
                if (selectedRoom != null) switchToRoomMessaging(selectedRoom);
            }
        });
        sidebarPanel.add(new JScrollPane(joinedRoomsList), BorderLayout.CENTER);

        JPanel roomManagementPanel = new JPanel(new GridLayout(1, 3));
        JButton createRoomButton = new JButton("Create Room");
        createRoomButton.addActionListener(e -> createRoom());
        JButton joinRoomButton = new JButton("Join Room");
        joinRoomButton.addActionListener(e -> joinRoom(frame));
        JButton leaveRoomButton = new JButton("Leave Room");
        leaveRoomButton.addActionListener(e -> leaveRoom());
        roomManagementPanel.add(createRoomButton);
        roomManagementPanel.add(joinRoomButton);
        roomManagementPanel.add(leaveRoomButton);
        sidebarPanel.add(roomManagementPanel, BorderLayout.SOUTH);
        JButton deleteRoomButton = new JButton("Delete Room");
        deleteRoomButton.addActionListener(e -> deleteRoom());
        roomManagementPanel.add(deleteRoomButton);

        mainPanel.add(sidebarPanel, BorderLayout.WEST);

        frame.add(mainPanel);
        frame.setVisible(true);

        client.setOnMessageReceivedListener(message ->
            handleIncomingMessage(message.getContent())
        );

        client.setClientsListener(message ->{
            System.out.println(message);
            onlineClientsModel.addElement(message);
        }
        
    );

    }

    private void handleIncomingMessage(String message) {
        SwingUtilities.invokeLater(() -> messagesArea.append(message + "\n"));
    }

    private void sendMessage(String message) {
        if (message == null || message.isBlank()) return;

        try {
            if (privateRecipient != null) {
                Message msg = new Message(
                    client.getUsername(),
                    privateRecipient,
                    client.getUsername() + " (private): " + message
                );
                server.sendMessage(msg, privateRecipient);
                messagesArea.append(
                    "You (to " + privateRecipient + "): " + message + "\n"
                );
            } else if (isInRoom && currentRoom != null) {
                Message msg = new Message(
                    client.getUsername(),
                    currentRoom,
                    client.getUsername() + "<" + currentRoom + ">: " + message
                );
                server.sendMessageToRoom(currentRoom, msg);
            } else {
                messagesArea.append(
                    "Join a room or select a user to send messages.\n"
                );
            }
        } catch (Exception e) {
            messagesArea.append(
                "Error sending message: " + e.getMessage() + "\n"
            );
        }
    }

    private void switchToPrivateMessaging(String recipient)
        throws RemoteException {
        privateRecipient = recipient;
        currentRoom = null;
        isInRoom = false;
        client.resetRoom();
        updateSidebarLabels();
        messagesArea.setText("");
        messagesArea.append("You are now chatting with: " + recipient + "\n");
    }

    private void switchToRoomMessaging(String room) {
        privateRecipient = null;
        isInRoom = true;
        currentRoom = room;
        client.setRoom(room);
        updateSidebarLabels();
        messagesArea.setText("");
        messagesArea.append("You are now in room: " + room + "\n");
    }

    private void setUsername(JFrame frame) {
        String username = JOptionPane.showInputDialog("Type a username:");
        if (username == null || username.isBlank()) return;
        try {
            if (server.isUsernameTaken(username)) {
                JOptionPane.showMessageDialog(frame, "Username taken.");
                System.exit(0);
                return;
            }
            // set username
            client.setUsername(username);
            // register client in server
            server.registerClient(client);
        } catch (Exception e) {
            messagesArea.append("Error joining room: " + e.getMessage() + "\n");
        }
    }

    private void updateSidebarLabels() {
        for (int i = 0; i < onlineClientsModel.size(); i++) {
            String client = onlineClientsModel
                .getElementAt(i)
                .replace(" (opened)", "");
            if (client.equals(privateRecipient)) {
                onlineClientsModel.set(i, client + " (opened)");
            } else {
                onlineClientsModel.set(i, client);
            }
        }
        for (int i = 0; i < joinedRoomsModel.size(); i++) {
            String room = joinedRoomsModel
                .getElementAt(i)
                .replace(" (opened)", "");
            if (room.equals(currentRoom)) {
                joinedRoomsModel.set(i, room + " (opened)");
            } else {
                joinedRoomsModel.set(i, room);
            }
        }
    }

    private void createRoom() {
        String roomName = JOptionPane.showInputDialog("Enter Room Name:");
        if (roomName == null || roomName.isBlank()) return;

        try {
            Room room = new Room(roomName, client.getUsername());
            server.registerRoom(room);
            server.addMemberToRoom(roomName, client);
            isInRoom = true;
            currentRoom = roomName;
            joinedRooms.add(roomName);
            client.setRoom(roomName);
            joinedRoomsModel.addElement(roomName);
            switchToRoomMessaging(roomName);
        } catch (Exception e) {
            messagesArea.append(
                "Error creating room: " + e.getMessage() + "\n"
            );
        }
    }

    private void joinRoom(JFrame frame) {
        String roomName = JOptionPane.showInputDialog(
            "Enter Room Name to Join:"
        );
        if (roomName == null || roomName.isBlank()) return;
        try {
            if (!server.doesRoomExist(roomName)) {
                JOptionPane.showMessageDialog(frame, "Room does not exist.");
                return;
            }
            server.addMemberToRoom(roomName, client);
            isInRoom = true;
            currentRoom = roomName;
            client.setRoom(roomName);

            joinedRooms.add(roomName);
            joinedRoomsModel.addElement(roomName);
            switchToRoomMessaging(roomName);
        } catch (Exception e) {
            messagesArea.append("Error joining room: " + e.getMessage() + "\n");
        }
    }

    private void leaveRoom() {
        if (!isInRoom || currentRoom == null) {
            messagesArea.append("You are not in any room to leave.\n");
            return;
        }

        try {
            String roomToLeave = currentRoom;
            server.removeMemberFromRoom(roomToLeave, client);
            joinedRooms.remove(roomToLeave);

            SwingUtilities.invokeLater(() -> {
                joinedRoomsModel.removeElement(roomToLeave); // Update the UI safely
            });

            currentRoom = null;
            isInRoom = false;
            client.resetRoom();
            updateSidebarLabels();
            messagesArea.append("Left room: " + roomToLeave + "\n");
        } catch (Exception e) {
            messagesArea.append("Error leaving room: " + e.getMessage() + "\n");
        }
    }

    private void deleteRoom() {
        if (!isInRoom || currentRoom == null) {
            messagesArea.append("You are not in any room to delete.\n");
            return;
        }

        try {
            String roomToDelete = currentRoom;
            if (!server.isRoomOwner(roomToDelete, client.getUsername())) {
                messagesArea.append(
                    "You are not the owner of this room and cannot delete it.\n"
                );
                return;
            }

            server.deregisterRoom(roomToDelete);
            joinedRooms.remove(roomToDelete);

            SwingUtilities.invokeLater(() -> {
                joinedRoomsModel.removeElement(roomToDelete); // Update the UI safely
            });

            currentRoom = null;
            isInRoom = false;
            client.resetRoom();
            updateSidebarLabels();
            messagesArea.append("Deleted room: " + roomToDelete + "\n");
        } catch (Exception e) {
            messagesArea.append(
                "Error deleting room: " + e.getMessage() + "\n"
            );
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatClientMain().start());
    }
}
