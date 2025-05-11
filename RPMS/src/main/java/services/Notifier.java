package services;

public interface Notifier {
    void sendNotification(String to,String subject,String message);
}
