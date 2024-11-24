import java.rmi.Naming;
import java.util.Scanner;

public class ChatClientMain {

    public static void main(String[] args) {
        try {
            // Create client and connect to server
            ChatClientImpl client = new ChatClientImpl();
            ChatServer server = (ChatServer) Naming.lookup(
                "rmi://localhost:1900/chatroom"
            );
            server.registerClient(client); // Register the client
            System.out.println("Client registered with ID: " + client.getID());

            // Input for sending messages
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println(
                    "\nEnter destination client ID (or type 'exit' to quit): "
                );
                String destinationID = scanner.nextLine();
                if (destinationID.equalsIgnoreCase("exit")) {
                    break;
                }

                System.out.println("Enter your message: ");
                String messageContent = scanner.nextLine();

                // Create and send message
                Message message = new Message(
                    client.getID(),
                    destinationID,
                    messageContent
                );
                server.sendMessage(message, destinationID);

                System.out.println(
                    "Message sent to client ID: " + destinationID
                );
            }

            // Deregister client before exiting
            server.deregisterClient(client);
            System.out.println("Client deregistered. Exiting...");

            scanner.close();
        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
