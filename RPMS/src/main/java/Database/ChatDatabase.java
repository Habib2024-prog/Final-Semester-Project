package Database;
import Chat.ChatMessage;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ChatDatabase {
    // Store messages with composite key: "senderId:receiverId"
    private static final Map<String, List<ChatMessage>> messages = new ConcurrentHashMap<>();

    // Track unread messages count: "receiverId:senderId" -> count
    private static final Map<String, Integer> unreadCounts = new ConcurrentHashMap<>();

    // Track online status
    private static final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    public static void addMessage(ChatMessage message) {
        String conversationKey = getConversationKey(message.getSenderId(), message.getReceiverId());
        String reverseKey = getConversationKey(message.getReceiverId(), message.getSenderId());

        // Add to both conversation directions for easy lookup
        messages.computeIfAbsent(conversationKey, k -> new ArrayList<>()).add(message);
        messages.computeIfAbsent(reverseKey, k -> new ArrayList<>()).add(message);

        // Update unread count
        if (!message.isRead()) {
            String unreadKey = message.getReceiverId() + ":" + message.getSenderId();
            unreadCounts.merge(unreadKey, 1, Integer::sum);
        }
    }

    public static List<ChatMessage> getConversation(String user1Id, String user2Id) {
        String key = getConversationKey(user1Id, user2Id);
        return messages.getOrDefault(key, Collections.emptyList())
                .stream()
                .sorted(Comparator.comparing(ChatMessage::getTimestamp))
                .collect(Collectors.toList());
    }

    public static void markMessagesAsRead(String senderId, String receiverId) {
        String key = getConversationKey(senderId, receiverId);
        messages.getOrDefault(key, Collections.emptyList())
                .forEach(msg -> {
                    if (msg.getSenderId().equals(senderId)){
                        msg.setRead(true);
                    }
                });

        // Clear unread count
        unreadCounts.remove(receiverId + ":" + senderId);
    }

    public static int getUnreadCount(String receiverId, String senderId) {
        return unreadCounts.getOrDefault(receiverId + ":" + senderId, 0);
    }

    public static void setUserOnline(String userId) {
        onlineUsers.add(userId);
    }

    public static void setUserOffline(String userId) {
        onlineUsers.remove(userId);
    }

    public static boolean isUserOnline(String userId) {
        return onlineUsers.contains(userId);
    }

    private static String getConversationKey(String user1Id, String user2Id) {
        return user1Id.compareTo(user2Id) < 0 ?
                user1Id + ":" + user2Id :
                user2Id + ":" + user1Id;
    }

    // For persistence (save to file)
    public static void saveToFile() {
        // Implement serialization to save chat history
    }

    // For initialization (load from file)
    public static void loadFromFile() {
        // Implement deserialization to load chat history
    }
}
