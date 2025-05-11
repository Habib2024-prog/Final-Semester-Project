package Chat;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import Model.Doctors;
import Database.UserDatabase;
import Model.Patients;

public class DoctorChatPanel extends JPanel {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String doctorId;
    private JTextArea chatArea;
    private JTextField inputField;
    private JComboBox<String> patientCombo;
    private ExecutorService executor;
    private String currentPatientId;
    private JLabel statusLabel;
    private JLabel unreadLabel;
    private Map<String, Boolean> typingStatus = new HashMap<>();
    private Map<String, StringBuilder> conversationHistory = new HashMap<>();

    public DoctorChatPanel(String doctorId) {
        this.doctorId = doctorId;
        executor = Executors.newSingleThreadExecutor();
        initializeUI();
        connectToServer();
        loadPatientList();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel - Patient selection and status
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));

        // Patient selection
        JPanel selectionPanel = new JPanel(new BorderLayout(5, 5));
        selectionPanel.add(new JLabel("Select Patient:"), BorderLayout.WEST);

        patientCombo = new JComboBox<>();
        patientCombo.setRenderer(new PatientListRenderer());
        patientCombo.addActionListener(e -> {
            String selected = (String) patientCombo.getSelectedItem();
            if (selected != null && !selected.isEmpty()) {
                currentPatientId = selected.split(" - ")[0];
                loadChatHistory(currentPatientId);
                clearUnreadStatus(currentPatientId);
            }
        });

        JButton refreshBtn = new JButton(new ImageIcon("resources/refresh.png"));
        refreshBtn.setToolTipText("Refresh patient list");
        refreshBtn.addActionListener(e -> loadPatientList());

        selectionPanel.add(patientCombo, BorderLayout.CENTER);
        selectionPanel.add(refreshBtn, BorderLayout.EAST);

        // Status indicators
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.GRAY);

        unreadLabel = new JLabel(" ");
        unreadLabel.setForeground(Color.RED);
        unreadLabel.setFont(unreadLabel.getFont().deriveFont(Font.BOLD));

        statusPanel.add(unreadLabel);
        statusPanel.add(statusLabel);

        topPanel.add(selectionPanel, BorderLayout.CENTER);
        topPanel.add(statusPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Chat display area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setPreferredSize(new Dimension(500, 300));
        add(chatScroll, BorderLayout.CENTER);

        // Input panel with typing indicator
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));

        // Typing indicator
        JLabel typingIndicator = new JLabel(" ");
        typingIndicator.setForeground(new Color(100, 100, 100));

        inputField = new JTextField();
        inputField.addActionListener(e -> sendMessage());
        inputField.getDocument().addDocumentListener(new DocumentListener() {
            private Timer typingTimer = new Timer(2000, e -> {
                sendTypingStatus(false);
                typingIndicator.setText(" ");
            });

            public void insertUpdate(DocumentEvent e) { handleTyping(); }
            public void removeUpdate(DocumentEvent e) { handleTyping(); }
            public void changedUpdate(DocumentEvent e) {}

            private void handleTyping() {
                if (currentPatientId != null) {
                    sendTypingStatus(true);
                    typingIndicator.setText("Typing...");
                    typingTimer.restart();
                }
            }
        });

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(typingIndicator, BorderLayout.NORTH);

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        southPanel.add(inputPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    private void loadPatientList() {
        SwingWorker<List<Patients>, String> worker = new SwingWorker<>() {
            @Override
            protected List<Patients> doInBackground() throws Exception {
                return UserDatabase.LoadPatient();
            }

            @Override
            protected void done() {
                try {
                    patientCombo.removeAllItems();
                    for (Patients patient : get()) {
                        String displayText = patient.getUserId() + " - " + patient.getName();
                        if (typingStatus.containsKey(patient.getUserId())){
                            displayText += typingStatus.get(patient.getUserId()) ? " (typing...)" : "";
                        }
                        patientCombo.addItem(displayText);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DoctorChatPanel.this,
                            "Error loading patients: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void connectToServer() {
        executor.execute(() -> {
            try {
                socket = new Socket("localhost", 8080);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Authenticate as doctor
                out.println("doctor:" + doctorId);

                // Start message listener
                String message;
                while ((message = in.readLine()) != null) {
                    processIncomingMessage(message);
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(DoctorChatPanel.this,
                                "Connection error: " + e.getMessage(),
                                "Connection Error", JOptionPane.ERROR_MESSAGE));
            }
        });
    }
    // In DoctorChatPanel.java
    private void processIncomingMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            if (message.startsWith("MSG:")) {
                String[] parts = message.substring(4).split(":", 2);
                if (parts.length == 2) {
                    String patientId = parts[0];
                    String content = parts[1];

                    // Store in conversation history

                    conversationHistory
                            .computeIfAbsent(patientId, k -> new StringBuilder())
                            .append("Patient " + patientId + ": " + content + "\n");

                    // Update UI if this is the current patient
                    if (patientId.equals(currentPatientId)) {
                        chatArea.append("Patient " + patientId + ": " + content + "\n");
                        chatArea.setCaretPosition(chatArea.getDocument().getLength());
                        out.println("READ:" + patientId); // Send read receipt
                    } else {
                        showNewMessageNotification(patientId);
                    }

                    Toolkit.getDefaultToolkit().beep();
                }
            }

        });
    }


    private void updatePatientStatus(String patientId, boolean isOnline) {
        for (int i = 0; i < patientCombo.getItemCount(); i++) {
            String item = patientCombo.getItemAt(i);
            if (item.startsWith(patientId)) {
                String newText = item.replace(" (offline)", "").replace(" (online)", "");
                newText += isOnline ? " (online)" : " (offline)";
                if (typingStatus.containsKey(patientId) && typingStatus.get(patientId)) {
                    newText += " (typing...)";
                }
                patientCombo.removeItemAt(i);
                patientCombo.insertItemAt(newText, i);

                if (patientId.equals(currentPatientId)) {
                    statusLabel.setText(isOnline ? "Online" : "Offline");
                    statusLabel.setForeground(isOnline ? Color.GREEN : Color.RED);
                }
                break;
            }
        }
    }

    private void updateUnreadCount(String patientId, int count) {
        if (count > 0 && !patientId.equals(currentPatientId)) {
            unreadLabel.setText(count + " unread");
        } else {
            unreadLabel.setText(" ");
        }
    }

    private void showNewMessageNotification(String patientId) {
        // Highlight in patient list
        for (int i = 0; i < patientCombo.getItemCount(); i++) {
            String item = patientCombo.getItemAt(i);
            if (item.startsWith(patientId)) {
                patientCombo.setForeground(Color.RED);
                break;
            }
        }

        // Show notification if window not focused
        if (!DoctorChatPanel.this.isShowing() || !DoctorChatPanel.this.hasFocus()) {
            JOptionPane.showMessageDialog(null,
                    "New message from patient " + patientId,
                    "New Message",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void loadChatHistory(String patientId) {
        // Would normally load from database
        chatArea.setText("");
        chatArea.append("-- Chat history with Patient " + patientId + " --\n");
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty() && currentPatientId != null) {
            out.println("MSG:" + currentPatientId + ":" + message);
            chatArea.append("You: " + message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
            inputField.setText("");
        }
    }

    private void sendTypingStatus(boolean isTyping) {
        if (currentPatientId != null) {
            out.println("TYPING:" + currentPatientId + ":" + isTyping);
        }
    }

    private void clearUnreadStatus(String patientId) {
        out.println("CLEAR_UNREAD:" + patientId);
    }

    private void updatePatientList() {
        for (int i = 0; i < patientCombo.getItemCount(); i++) {
            String item = patientCombo.getItemAt(i);
            String patientId = item.split(" - ")[0];
            if (typingStatus.containsKey(patientId)) {
                String newText = item.replace(" (typing...)", "");
                if (typingStatus.get(patientId)) {
                    newText += " (typing...)";
                }
                patientCombo.removeItemAt(i);
                patientCombo.insertItemAt(newText, i);
            }
        }
    }

    public void disconnect() {
        try {
            if (socket != null) {
                out.println("LOGOUT:" + doctorId);
                socket.close();
            }
            executor.shutdownNow();
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    // Custom renderer for patient list with icons
    private class PatientListRenderer extends DefaultListCellRenderer {
        private Icon onlineIcon = new ImageIcon("resources/online.png");
        private Icon offlineIcon = new ImageIcon("resources/offline.png");
        private Icon typingIcon = new ImageIcon("resources/typing.png");

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            String text = (String) value;
            String patientId = text.split(" - ")[0];

            if (text.contains("(online)")) {
                setIcon(onlineIcon);
            } else if (text.contains("(offline)")) {
                setIcon(offlineIcon);
            }

            if (text.contains("(typing...)")) {
                setIcon(typingIcon);
            }

            if (text.contains("*UNREAD*")) {
                setFont(getFont().deriveFont(Font.BOLD));
                setForeground(Color.RED);
            }

            return this;
        }
    }
}