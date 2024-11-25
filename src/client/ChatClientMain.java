package client;



import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import models.Message;
import server.ChatServer;

import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;
import models.Room;

public class ChatClientMain {

    private ChatServer server;
    private ChatClientImpl client;
    private boolean isInRoom = false;
    private List<String> onlineClients;
    private final List<String> joinedRooms = new ArrayList<>();
    private String currentRoom = null;
    private MultiWindowTextGUI gui;

    public ChatClientMain() {
        try {
            client = new ChatClientImpl();
            server = (ChatServer) Naming.lookup("rmi://localhost:1900/chatroom");
            server.registerClient(client);
            onlineClients = server.getOnlineClients();
        } catch (Exception e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            System.exit(1);
        }
    }
    private void handleIncomingMessage(String message, TextBox messagesBox) {
        gui.getGUIThread().invokeLater(() -> {
            messagesBox.addLine(message);
        });
    }
    
    public void start() {
        try {
            Screen screen = new DefaultTerminalFactory().createScreen();
            screen.startScreen();

            gui = new MultiWindowTextGUI(
                    screen,
                    new DefaultWindowManager(),
                    new EmptySpace(TextColor.ANSI.BLUE)
            );

            BasicWindow mainWindow = new BasicWindow("Chat Client");
            Panel mainPanel = new Panel(new BorderLayout());

            // Sidebar with online clients and joined rooms
            Panel sidebar = new Panel(new LinearLayout(Direction.VERTICAL));
            updateSidebar(sidebar);

            // Messages area
            TextBox messagesBox = new TextBox()
                    .setReadOnly(true)
                    .setPreferredSize(new TerminalSize(50, 20));
                    client.setOnMessageReceivedListener(message -> {
                        handleIncomingMessage(message.getContent(), messagesBox);
                    });
            mainPanel.addComponent(messagesBox, BorderLayout.Location.CENTER);

            Panel inputPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
            TextBox inputBox = new TextBox(new TerminalSize(40, 1));
            Button sendButton = new Button("Send", () -> sendMessage(inputBox.getText(), messagesBox));
            inputPanel.addComponent(inputBox);
            inputPanel.addComponent(sendButton);
            mainPanel.addComponent(inputPanel, BorderLayout.Location.BOTTOM);

            Panel managementPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
            Button createRoomButton = new Button("Create Room", () -> createRoom(messagesBox, sidebar));
            Button joinRoomButton = new Button("Join Room", () -> joinRoom(messagesBox, sidebar));
            Button leaveRoomButton = new Button("Leave Room", () -> leaveRoom(messagesBox, sidebar));
            managementPanel.addComponent(createRoomButton);
            managementPanel.addComponent(joinRoomButton);
            managementPanel.addComponent(leaveRoomButton);
            mainPanel.addComponent(managementPanel, BorderLayout.Location.TOP);

            mainPanel.addComponent(sidebar, BorderLayout.Location.LEFT);
            mainWindow.setComponent(mainPanel);

            gui.addWindowAndWait(mainWindow);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String message, TextBox messagesBox) {
        if (message == null || message.isBlank()) return;

        try {
            if (isInRoom && currentRoom != null) {
                Message msg = new Message(client.getID(), currentRoom, client.getID() + "<" + currentRoom + ">: " + message);
                server.sendMessageToRoom(currentRoom, msg);
                messagesBox.addLine("Message sent to Room<" + currentRoom + ">: " + message);
            } else {
                messagesBox.addLine("Join a room before sending messages.");
            }
        } catch (Exception e) {
            messagesBox.addLine("Error sending message: " + e.getMessage());
        }
    }

    private void createRoom(TextBox messagesBox, Panel sidebar) {
        String roomName = promptInput("Enter Room Name:");
        if (roomName == null || roomName.isBlank()) return;

        try {
            Room room = new Room(roomName, client.getID());
            server.registerRoom(room);
            server.addMemberToRoom(roomName, client);
            isInRoom = true;
            currentRoom = roomName;
            joinedRooms.add(roomName);
            updateSidebar(sidebar);
            messagesBox.addLine("Room created and joined: " + roomName);
        } catch (Exception e) {
            messagesBox.addLine("Error creating room: " + e.getMessage());
        }
    }

    private void joinRoom(TextBox messagesBox, Panel sidebar) {
        String roomName = promptInput("Enter Room Name to Join:");
        if (roomName == null || roomName.isBlank()) return;

        try {
            server.addMemberToRoom(roomName, client);
            isInRoom = true;
            currentRoom = roomName;
            joinedRooms.add(roomName);
            updateSidebar(sidebar);
            messagesBox.addLine("Joined room: " + roomName);
        } catch (Exception e) {
            messagesBox.addLine("Error joining room: " + e.getMessage());
        }
    }

    private void leaveRoom(TextBox messagesBox, Panel sidebar) {
        if (!isInRoom || currentRoom == null) {
            messagesBox.addLine("You are not in any room to leave.");
            return;
        }

        try {
            String roomToLeave = currentRoom;
            server.deregisterRoom(roomToLeave);
            joinedRooms.remove(roomToLeave);
            currentRoom = null;
            isInRoom = false;
            updateSidebar(sidebar);
            messagesBox.addLine("Left room: " + roomToLeave);
        } catch (Exception e) {
            messagesBox.addLine("Error leaving room: " + e.getMessage());
        }
    }

    private void updateSidebar(Panel sidebar) {
        sidebar.removeAllComponents();
        sidebar.addComponent(new Label("Online Clients").addStyle(SGR.BOLD));
        for (String client : onlineClients) {
            sidebar.addComponent(new Label(client));
        }
        sidebar.addComponent(new Separator(Direction.HORIZONTAL));
        sidebar.addComponent(new Label("Joined Rooms").addStyle(SGR.BOLD));
        for (String room : joinedRooms) {
            sidebar.addComponent(new Label(room));
        }
    }

    private String promptInput(String promptMessage) {
        TextInputDialog dialog = new TextInputDialog("Input Required", promptMessage);
        return dialog.showDialog(gui);
    }

    public static void main(String[] args) {
        new ChatClientMain().start();
    }
}

class TextInputDialog {
    private final String title;
    private final String promptMessage;
    private String inputText;

    public TextInputDialog(String title, String promptMessage) {
        this.title = title;
        this.promptMessage = promptMessage;
    }

    public String showDialog(MultiWindowTextGUI gui) {
        final BasicWindow dialogWindow = new BasicWindow(title);
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));

        panel.addComponent(new Label(promptMessage));
        TextBox inputBox = new TextBox();
        panel.addComponent(inputBox);

        Button okButton = new Button("OK", () -> {
            inputText = inputBox.getText();
            dialogWindow.close();
        });

        Button cancelButton = new Button("Cancel", dialogWindow::close);

        Panel buttonPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        buttonPanel.addComponent(okButton);
        buttonPanel.addComponent(cancelButton);

        panel.addComponent(buttonPanel);
        dialogWindow.setComponent(panel);

        gui.addWindowAndWait(dialogWindow);
        return inputText;
    }
}
