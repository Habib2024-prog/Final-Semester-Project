package Chat;

import Model.Doctors;
import Model.Patients;
import Database.UserDatabase;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class PatientChatPanel extends JPanel {
    // Network components
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ExecutorService executor;

    // UI components
    private JTextArea chatArea;
    private JTextField inputField;
    private JComboBox<String> doctorCombo;
    private JLabel statusLabel;

    // Data
    private String patientId;
    private String currentDoctorId;
    private Map<String, StringBuilder> conversationHistory = new HashMap<>();

    public PatientChatPanel(String patientId) {
        this.patientId = patientId;
        this.executor = Executors.newSingleThreadExecutor();

        // Initialize UI with doctor list
        initializeUI();

        // Connect to chat server
        connectToServer();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel - Doctor selection
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));

        // Doctor selection combo box
        doctorCombo = new JComboBox<>();
        doctorCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof String) {
                    String[] parts = ((String)value).split("\\|");
                    if (parts.length >= 2) {
                        setText("Dr. " + parts[1]); // Display as "Dr. Smith"
                    }
                }
                return this;
            }
        });

        // Load available doctors
        loadAvailableDoctors();

        // Doctor selection handler
        doctorCombo.addActionListener(e -> {
            String selected = (String) doctorCombo.getSelectedItem();
            if (selected != null) {
                currentDoctorId = selected.split("\\|")[0]; // Extract doctor ID
                loadConversation(currentDoctorId);
            }
        });

        statusLabel = new JLabel("Select a doctor");
        statusLabel.setForeground(Color.GRAY);

        topPanel.add(new JLabel("Select Doctor:"), BorderLayout.WEST);
        topPanel.add(doctorCombo, BorderLayout.CENTER);
        topPanel.add(statusLabel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Chat display area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane chatScroll = new JScrollPane(chatArea);
        add(chatScroll, BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputField = new JTextField();
        inputField.addActionListener(e -> sendMessage());

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);
    }

    private void loadAvailableDoctors() {
        // Clear existing items
        doctorCombo.removeAllItems();

        // Get assigned doctors for this patient
        List<String> doctorIds = UserDatabase.getDoctorsForPatient(patientId);

        // Load doctor details and populate combo box
        for (String doctorId : doctorIds) {
            Doctors doctor = UserDatabase.getDoctorById(doctorId);
            if (doctor != null) {
                doctorCombo.addItem(doctorId + "|" + doctor.getName());
            }
        }

        // Select first doctor by default if available
        if (doctorCombo.getItemCount() > 0) {
            doctorCombo.setSelectedIndex(0);
            currentDoctorId = ((String)doctorCombo.getSelectedItem()).split("\\|")[0];
        } else {
            JOptionPane.showMessageDialog(this,
                    "No doctors are currently assigned to you",
                    "No Doctors Available",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void connectToServer() {
        executor.execute(() -> {
            try {
                socket = new Socket("localhost", 8080);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Authenticate as patient
                out.println("patient:" + patientId);

                // Start message listener
                String message;
                while ((message = in.readLine()) != null) {
                    processIncomingMessage(message);
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this,
                                "Connection error: " + e.getMessage(),
                                "Connection Error",
                                JOptionPane.ERROR_MESSAGE));
            }
        });
    }

    private void processIncomingMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                if (message.startsWith("MSG:")) {
                    String[] parts = message.substring(4).split(":", 2);
                    if (parts.length == 2) {
                        String senderId = parts[0];
                        String content = parts[1];

                        // Store in conversation history
                        conversationHistory
                                .computeIfAbsent(senderId, k -> new StringBuilder())
                                .append("Dr. " + senderId + ": " + content + "\n");

                        // Update UI if this is the current doctor
                        if (senderId.equals(currentDoctorId)) {
                            chatArea.append("Dr. " + senderId + ": " + content + "\n");
                            chatArea.setCaretPosition(chatArea.getDocument().getLength());
                            out.println("READ:" + senderId); // Send read receipt
                        } else {
                            showNewMessageNotification(senderId);
                        }

                        Toolkit.getDefaultToolkit().beep();
                    }
                }
                else if (message.startsWith("STATUS:")) {
                    String[] parts = message.substring(7).split(":");
                    if (parts.length == 2) {
                        String doctorId = parts[0];
                        boolean isOnline = Boolean.parseBoolean(parts[1]);
                        updateDoctorStatus(doctorId, isOnline);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error processing message: " + e.getMessage());
            }
        });
    }

    private void updateDoctorStatus(String doctorId, boolean isOnline) {
        for (int i = 0; i < doctorCombo.getItemCount(); i++) {
            String item = (String) doctorCombo.getItemAt(i);
            if (item.startsWith(doctorId)) {
                String newText = item.replace(" (offline)", "").replace(" (online)", "");
                newText += isOnline ? " (online)" : " (offline)";
                doctorCombo.removeItemAt(i);
                doctorCombo.insertItemAt(newText, i);

                if (doctorId.equals(currentDoctorId)) {
                    statusLabel.setText(isOnline ? "Online" : "Offline");
                    statusLabel.setForeground(isOnline ? Color.GREEN : Color.RED);
                }
                break;
            }
        }
    }

    private void loadConversation(String doctorId) {
        chatArea.setText("-- Conversation with Dr. " + doctorId + " --\n");
        if (conversationHistory.containsKey(doctorId)) {
            chatArea.append(conversationHistory.get(doctorId).toString());
        }
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void showNewMessageNotification(String doctorId) {
        for (int i = 0; i < doctorCombo.getItemCount(); i++) {
            String item = (String) doctorCombo.getItemAt(i);
            if (item.startsWith(doctorId) && !item.contains("(new)")) {
                String newText = item + " (new)";
                doctorCombo.removeItemAt(i);
                doctorCombo.insertItemAt(newText, i);
                break;
            }
        }
    }

    private void sendMessage() {
        if (currentDoctorId == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a doctor first",
                    "No Doctor Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            // Store in conversation history
            conversationHistory
                    .computeIfAbsent(currentDoctorId, k -> new StringBuilder())
                    .append("You: " + message + "\n");

            // Send to server
            out.println("MSG:" + currentDoctorId + ":" + message);

            // Update UI
            chatArea.append("You: " + message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
            inputField.setText("");
        }
    }

    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
            executor.shutdownNow();
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }
}