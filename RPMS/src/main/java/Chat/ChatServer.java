package Chat;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 8080;
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    private static final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat Server is running on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientThread = new ClientHandler(socket);
                pool.execute(clientThread);
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String userId;
        private String userType;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Authentication (first message must be "type:id")
                String authMessage = in.readLine();
                String[] authParts = authMessage.split(":");
                this.userType = authParts[0];
                this.userId = authParts[1];

                clients.put(userId, this);
                System.out.println(userType + " " + userId + " connected");
                broadcastUserStatus(userId, true);

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received from " + userId + ": " + message);

                    if (message.startsWith("MSG:")) {
                        handleNewMessage(message);
                    } else if (message.startsWith("TYPING:")) {
                        forwardTypingIndicator(message);
                    } else if (message.startsWith("READ:")) {
                        forwardReadReceipt(message);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error with client " + userId + ": " + e.getMessage());
            } finally {
                try {
                    if (userId != null) {
                        clients.remove(userId);
                        broadcastUserStatus(userId, false);
                    }
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Socket close error");
                }
                System.out.println((userType != null ? userType + " " + userId : "Unknown user") + " disconnected");
            }
        }


        // In ChatServer.java
        private void handleNewMessage(String message) {
            if (message.startsWith("MSG:")) {
                String[] parts = message.substring(4).split(":", 2);
                if (parts.length == 2) {
                    String receiverId = parts[0];
                    String content = parts[1];

                    ClientHandler receiver = clients.get(receiverId);
                    if (receiver != null) {
                        // Forward message with original sender's ID
                        receiver.out.println("MSG:" + userId + ":" + content);

                        // Send delivery confirmation to sender
                        this.out.println("DELIVERED:" + receiverId);
                    } else {
                        this.out.println("ERROR:" + receiverId + " not online");
                        // Optionally store for later delivery
                    }
                }
            }
        }
        private void forwardTypingIndicator(String message) {
            // Format: TYPING:receiverId
            String receiverId = message.substring(7);
            ClientHandler receiver = clients.get(receiverId);
            if (receiver != null) {
                receiver.out.println("TYPING:" + userId);
            }
        }

        private void forwardReadReceipt(String message) {
            // Format: READ:receiverId:messageContent
            String receiverId = message.substring(5);
            ClientHandler receiver = clients.get(receiverId);
            if (receiver != null) {
                receiver.out.println("READ:" + userId);
            }
        }

        private void broadcastUserStatus(String userId, boolean isOnline) {
            for (ClientHandler client : clients.values()) {
                if (!client.userId.equals(userId)) {
                    client.out.println("STATUS:" + userId + ":" + isOnline);
                }
            }
        }
    }
}