package DashBoard;


import Chat.PatientChatPanel;
import Database.UserDatabase;
import Database.VideoConsultationDatabase;
import Model.Doctors;
import Model.LoginSystem;
import Model.Patients;
import services.EmergencyNotifier;
import services.ReminderService;
import services.ReportGenerator;
import services.VideoConsultation;
import utill.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PatientDashboard {
    private final Patients patient;
    private final JFrame frame;
    private DefaultTableModel vitalsModel;
    private DefaultTableModel appointmentsModel;
    private DefaultTableModel videoConsultationsModel;

    public PatientDashboard(Patients patient) {
        this.patient = patient;
        this.frame = new JFrame("Patient Dashboard - " + patient.getName());
        frame.setSize(1200, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        initializeModels();
        createUI();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void initializeModels() {
        // Vitals Table Model
        String[] vitalsColumns = {"Date", "Blood Pressure", "Heart Rate", "Temperature", "Oxygen Level", "Notes"};
        vitalsModel = new DefaultTableModel(vitalsColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        refreshVitalsTable();

        // Appointments Table Model
        String[] appointmentsColumns = {"ID", "Doctor", "Date/Time", "Description", "Status"};
        appointmentsModel = new DefaultTableModel(appointmentsColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        refreshAppointmentsTable();

        // Video Consultations Table Model
        String[] videoColumns = {"Doctor", "Date/Time", "Platform", "Status", "Link"};
        videoConsultationsModel = new DefaultTableModel(videoColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only link column is clickable
            }
        };
        refreshVideoConsultations();
    }

    private void createUI() {
        // Top Panel with Welcome and Logout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel welcomeLabel = new JLabel("Welcome, " + patient.getName(), JLabel.LEFT);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(logoutButton, BorderLayout.EAST);
        frame.add(topPanel, BorderLayout.NORTH);

        // Main Tabbed Interface
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: Health Overview
        tabbedPane.addTab("Health Overview", createHealthOverviewTab());

        // Tab 2: Appointments
        tabbedPane.addTab("Appointments", createAppointmentsTab());

        // Tab 3: Video Consultations
        tabbedPane.addTab("Video Consultations", createVideoConsultationsTab());

        // Tab 4: Emergency
        tabbedPane.addTab("Emergency", createEmergencyTab());
        // Tab 5: trend view
        tabbedPane.addTab("TrendView",TrendTab());
        // tan 6; chat
        tabbedPane.addTab("Chat with Doctor",createChatPanel());

        frame.add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createHealthOverviewTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Vitals Panel
        JPanel vitalsPanel = new JPanel(new BorderLayout());
        vitalsPanel.setBorder(BorderFactory.createTitledBorder("Vital Signs"));

        JTable vitalsTable = new JTable(vitalsModel);
        vitalsTable.setAutoCreateRowSorter(true);
        JScrollPane vitalsScrollPane = new JScrollPane(vitalsTable);
        vitalsScrollPane.setPreferredSize(new Dimension(0, 200));

        // Vitals Buttons
        JPanel vitalsButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addVitalsBtn = new JButton("Add New Vitals");
        addVitalsBtn.addActionListener(e -> showAddVitalsDialog());
        JButton uploadVitalsBtn = new JButton("Upload from CSV");
        uploadVitalsBtn.addActionListener(e -> uploadVitalsFromCSV());
        vitalsButtonPanel.add(addVitalsBtn);
        vitalsButtonPanel.add(uploadVitalsBtn);
        JButton generateReportBtn = new JButton(" Generate Your Health Report");
        generateReportBtn.addActionListener(e -> {
            ReportGenerator.generatePatientReport(patient);
        });
        vitalsButtonPanel.add(generateReportBtn);
        vitalsPanel.add(vitalsScrollPane, BorderLayout.CENTER);
        vitalsPanel.add(vitalsButtonPanel, BorderLayout.SOUTH);

        // Prescriptions Panel
        JPanel prescriptionsPanel = new JPanel(new BorderLayout());
        prescriptionsPanel.setBorder(BorderFactory.createTitledBorder("Prescriptions"));

        String[] presColumns = {"Medication", "Dosage", "Frequency", "Start Date", "End Date"};
        DefaultTableModel presModel = new DefaultTableModel(presColumns, 0);
        for (Prescriptions p : patient.getPrescriptions()) {
            presModel.addRow(new Object[]{
                    p.getMedicationName(), p.getDosage(), p.getSchedule()
            });
        }

        JTable presTable = new JTable(presModel);
        prescriptionsPanel.add(new JScrollPane(presTable), BorderLayout.CENTER);

        // Feedback Panel
        JPanel feedbackPanel = new JPanel(new BorderLayout());
        feedbackPanel.setBorder(BorderFactory.createTitledBorder("Doctor Feedback"));

        String[] feedbackColumns = {"Date", "Feedback"};
        DefaultTableModel feedbackModel = new DefaultTableModel(feedbackColumns, 0);
        for (FeedBack fb : patient.getDoctorFeedback()) {
            feedbackModel.addRow(new Object[]{
                    fb.getFeedBackDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    fb.getFeedBackText()
            });
        }

        JTable feedbackTable = new JTable(feedbackModel);
        feedbackPanel.add(new JScrollPane(feedbackTable), BorderLayout.CENTER);

        // Combine panels
        JPanel upperPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        upperPanel.add(prescriptionsPanel);
        upperPanel.add(feedbackPanel);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.add(vitalsPanel, BorderLayout.NORTH);
        mainPanel.add(upperPanel, BorderLayout.CENTER);

        // Medical History Button
        JButton historyBtn = new JButton("View Complete Medical History");
        historyBtn.addActionListener(e -> showMedicalHistory());
        mainPanel.add(historyBtn, BorderLayout.SOUTH);

        panel.add(mainPanel, BorderLayout.CENTER);
        return panel;
    }
    private JPanel TrendTab() {
        JPanel trendsPanel = new JPanel(new BorderLayout(10, 10));
        trendsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create a container panel for the tabbed pane and refresh button
        JPanel containerPanel = new JPanel(new BorderLayout());

        // Create the tabbed pane
        JTabbedPane trendsTabPane = new JTabbedPane();
        updateTrendCharts(trendsTabPane); // Initial chart loading
        // Add refresh button
        JButton refreshButton = new JButton("Refresh Trends");
        refreshButton.addActionListener(e -> {
            updateTrendCharts(trendsTabPane);
            JOptionPane.showMessageDialog(trendsPanel,
                    "Trend data refreshed successfully!",
                    "Refresh Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        // Create a panel for the button (for better positioning)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(refreshButton);

        // Add components to container
        containerPanel.add(trendsTabPane, BorderLayout.CENTER);
        containerPanel.add(buttonPanel, BorderLayout.SOUTH);

        trendsPanel.add(containerPanel, BorderLayout.CENTER);

        return trendsPanel;
    }
    // In DashBoard/PatientDashboard.java
    private JPanel createChatPanel() {
        return new PatientChatPanel(patient.getUserId());
    }



    private JPanel createAppointmentsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Appointments Table
        JTable appointmentsTable = new JTable(appointmentsModel);
        appointmentsTable.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Appointment Management Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton newAppointmentBtn = new JButton("Request New Appointment");
        newAppointmentBtn.addActionListener(e -> showNewAppointmentDialog());
        buttonPanel.add(newAppointmentBtn);

        JButton confirmedBtn = new JButton("View Confirmed Appointments");
        confirmedBtn.addActionListener(e -> showConfirmedAppointments());
        buttonPanel.add(confirmedBtn);

        JButton reminderBtn = new JButton("Send Appointment Reminder");
        reminderBtn.addActionListener(e -> {
            ReminderService.sendAppointmentReminders(patient);
            JOptionPane.showMessageDialog(frame, "Reminders sent to your registered contact!");
        });
        buttonPanel.add(reminderBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createVideoConsultationsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Video Consultations Table
        JTable videoTable = new JTable(videoConsultationsModel);
        videoTable.setAutoCreateRowSorter(true);

        // Make links clickable
        videoTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column);
                if (column == 4 && value != null) { // Link column
                    c.setForeground(Color.BLUE);
                    ((JLabel)c).setText("<html><u>" + value + "</u></html>");
                }
                return c;
            }
        });

        videoTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                int row = videoTable.rowAtPoint(evt.getPoint());
                int col = videoTable.columnAtPoint(evt.getPoint());
                if (row >= 0 && col == 4) { // Clicked on link column
                    String link = (String) videoTable.getValueAt(row, col);
                    if (link != null && !link.isEmpty()) {
                        openVideoLink(link);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(videoTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Video Consultation Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton requestBtn = new JButton("Request New Consultation");
        requestBtn.addActionListener(e -> requestVideoConsultation());
        buttonPanel.add(requestBtn);

        JButton refreshBtn = new JButton("Refresh List");
        refreshBtn.addActionListener(e -> refreshVideoConsultations());
        buttonPanel.add(refreshBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createEmergencyTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Emergency Panel
        JPanel emergencyPanel = new JPanel(new GridLayout(2, 1, 15, 15));
        emergencyPanel.setBorder(BorderFactory.createTitledBorder("Emergency Features"));

        // Panic Button
        JPanel panicPanel = new JPanel(new BorderLayout());
        JButton panicBtn = new JButton("EMERGENCY PANIC BUTTON");
        panicBtn.setBackground(Color.RED);
        panicBtn.setForeground(Color.WHITE);
        panicBtn.setFont(new Font("Arial", Font.BOLD, 18));
        panicBtn.addActionListener(e -> triggerEmergency());
        panicPanel.add(panicBtn, BorderLayout.CENTER);

        // Emergency Contact Info
        JPanel contactPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        contactPanel.setBorder(BorderFactory.createTitledBorder("Emergency Contacts"));
        contactPanel.add(new JLabel("Emergency Email: " + patient.getEmergencyEmail()));

        Doctors primaryDoctor = findPrimaryDoctor(patient.getUserId());
        contactPanel.add(new JLabel("Primary Doctor: " +
                (primaryDoctor != null ? primaryDoctor.getName() : "Not assigned")));

        panicPanel.add(contactPanel, BorderLayout.SOUTH);
        emergencyPanel.add(panicPanel);

        // Medication Reminders Panel
        JPanel reminderPanel = new JPanel(new BorderLayout());
        reminderPanel.setBorder(BorderFactory.createTitledBorder("Medication Reminders"));

        JButton medReminderBtn = new JButton("Send Medication Reminders");
        medReminderBtn.addActionListener(e -> {
            ReminderService.sendMedicationReminders(patient);
            JOptionPane.showMessageDialog(frame, "Medication reminders sent!");
        });
        reminderPanel.add(medReminderBtn, BorderLayout.CENTER);

        emergencyPanel.add(reminderPanel);
        panel.add(emergencyPanel, BorderLayout.CENTER);

        return panel;
    }

    private void refreshVitalsTable() {
        vitalsModel.setRowCount(0);
        for (Vitals v : patient.getVitalsList()) {
            vitalsModel.addRow(new Object[]{
                    v.getTimestamp(),
                    v.getBloodPressure(),
                    v.getHeartRate(),
                    v.getTemperature(),
                    v.getOxygenLevel(),
            });
        }
    }

    private void refreshAppointmentsTable() {
        appointmentsModel.setRowCount(0);
        for (Appointment a : patient.getAppointments()) {
            appointmentsModel.addRow(new Object[]{
                    a.getAppointmentId(),
                    a.getDoctorId(),
                    a.getAppointmentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    a.getDescription(),
                    a.getStatus().toString()
            });
        }
    }

    private void refreshVideoConsultations() {
        videoConsultationsModel.setRowCount(0);
        List<VideoConsultation> consultations = VideoConsultationDatabase.loadConsultations();

        for (VideoConsultation vc : consultations) {
            if (vc.getPatientId().equals(patient.getUserId())) {
                videoConsultationsModel.addRow(new Object[]{
                        vc.getDoctorId(),
                        vc.getApprovalTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        vc.getPlatform(),
                        vc.isApproved() ? "Approved" : "Pending",
                        vc.isApproved() ? vc.getLink() : "Not available"
                });
            }
        }
    }

    private void showAddVitalsDialog() {
        JDialog dialog = new JDialog(frame, "Add New Vitals", true);
        dialog.setLayout(new GridLayout(6, 2, 10, 10));
        dialog.setSize(400, 300);

        JTextField bpField = new JTextField();
        JTextField hrField = new JTextField();
        JTextField tempField = new JTextField();
        JTextField oxyField = new JTextField();

        dialog.add(new JLabel("Blood Pressure (mmHg):"));
        dialog.add(bpField);
        dialog.add(new JLabel("Heart Rate (bpm):"));
        dialog.add(hrField);
        dialog.add(new JLabel("Temperature (°C):"));
        dialog.add(tempField);
        dialog.add(new JLabel("Oxygen Level (%):"));
        dialog.add(oxyField);


        JButton submitBtn = new JButton("Submit");
        submitBtn.addActionListener(e -> {
            try {
                Vitals newVital = new Vitals(
                        Double.parseDouble(hrField.getText()),
                        Double.parseDouble(tempField.getText()),
                        Double.parseDouble(oxyField.getText()),
                        bpField.getText(),
                        LocalDateTime.now()
                );

                Doctors doctor = findPrimaryDoctor(patient.getUserId());
                if (doctor != null) {
                    patient.addVitalSign(newVital, doctor);
                    refreshVitalsTable();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "No primary doctor assigned!",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException | VitalException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter valid numbers for all fields",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(submitBtn);
        dialog.add(cancelBtn);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void uploadVitalsFromCSV() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            Doctors doctor = findPrimaryDoctor(patient.getUserId());
            if (doctor != null) {
                patient.uploadVitalsFromFile(file.getAbsolutePath(), doctor);
                refreshVitalsTable();
                JOptionPane.showMessageDialog(frame,
                        "Vitals uploaded successfully from CSV!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame,
                        "No primary doctor assigned!",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showMedicalHistory() {
        JDialog historyDialog = new JDialog(frame, "Medical History", true);
        historyDialog.setSize(800, 600);

        JTabbedPane historyTabs = new JTabbedPane();

        // Feedback History
        JTextArea feedbackArea = new JTextArea();
        feedbackArea.setEditable(false);
        for (FeedBack fb : patient.getDoctorFeedback()) {
            feedbackArea.append(fb.toString() + "\n\n");
        }
        historyTabs.addTab("Feedback History", new JScrollPane(feedbackArea));

        // Prescription History
        JTextArea presArea = new JTextArea();
        presArea.setEditable(false);
        for (Prescriptions p : patient.getPrescriptions()) {
            presArea.append(p.toString() + "\n\n");
        }
        historyTabs.addTab("Prescription History", new JScrollPane(presArea));

        historyDialog.add(historyTabs);
        historyDialog.setLocationRelativeTo(frame);
        historyDialog.setVisible(true);
    }

    private void showNewAppointmentDialog() {
        JDialog dialog = new JDialog(frame, "New Appointment", true);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.setSize(400, 300);

        JTextField doctorIdField = new JTextField();
        JTextField dateField = new JTextField(LocalDateTime.now().plusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        JTextArea descArea = new JTextArea(3, 20);

        dialog.add(new JLabel("Doctor ID:"));
        dialog.add(doctorIdField);
        dialog.add(new JLabel("Date/Time (yyyy-MM-dd HH:mm):"));
        dialog.add(dateField);
        dialog.add(new JLabel("Description:"));
        dialog.add(new JScrollPane(descArea));

        JButton submitBtn = new JButton("Submit");
        submitBtn.addActionListener(e -> {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(dateField.getText(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                Appointment appt = patient.requestAppointment(
                        doctorIdField.getText(),
                        descArea.getText(),
                        dateTime
                );

                refreshAppointmentsTable();
                JOptionPane.showMessageDialog(dialog,
                        "Appointment requested! ID: " + appt.getAppointmentId());
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Invalid date format or other error",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(submitBtn);
        dialog.add(cancelBtn);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void showConfirmedAppointments() {
        StringBuilder confirmed = new StringBuilder("Confirmed Appointments:\n\n");
        for (Appointment a : patient.getAppointments()) {
            if (a.getStatus() == Appointment.Status.CONFIRMED) {
                confirmed.append(String.format(
                        "ID: %s\nDoctor: %s\nTime: %s\nDescription: %s\n\n",
                        a.getAppointmentId(),
                        a.getDoctorId(),
                        a.getAppointmentDate().format(DateTimeFormatter.ofPattern("MMM dd, hh:mm a")),
                        a.getDescription()
                ));
            }
        }

        if (confirmed.length() == "Confirmed Appointments:\n\n".length()) {
            confirmed.append("No confirmed appointments found.");
        }

        JOptionPane.showMessageDialog(frame, confirmed.toString(),
                "Confirmed Appointments", JOptionPane.INFORMATION_MESSAGE);
    }

    private void triggerEmergency() {
        int confirm = JOptionPane.showConfirmDialog(frame,
                "This will immediately notify your doctor and emergency contacts!\nContinue?",
                "Confirm Emergency", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            Doctors doctor = findPrimaryDoctor(patient.getUserId());
            if (doctor != null) {
                EmergencyNotifier.notifyDoctor(patient, doctor);
                EmergencyNotifier.notifyPatientFamily(patient);
                JOptionPane.showMessageDialog(frame,
                        "Emergency alerts sent to Dr. " + doctor.getName() +
                                " and your emergency contacts!",
                        "Emergency Triggered", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame,
                        "No primary doctor assigned!",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void requestVideoConsultation() {
        JDialog dialog = new JDialog(frame, "Request Video Consultation", true);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));
        dialog.setSize(400, 200);

        JTextField doctorIdField = new JTextField();
        JTextField timeField = new JTextField(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        dialog.add(new JLabel("Doctor ID:"));
        dialog.add(doctorIdField);
        dialog.add(new JLabel("Preferred Time (yyyy-MM-dd HH:mm):"));
        dialog.add(timeField);

        JButton submitBtn = getJButton(timeField, doctorIdField, dialog);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(submitBtn);
        dialog.add(cancelBtn);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private JButton getJButton(JTextField timeField, JTextField doctorIdField, JDialog dialog) {
        JButton submitBtn = new JButton("Submit");
        submitBtn.addActionListener(e -> {
            try {
                // Parse input in "yyyy-MM-dd HH:mm" format
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime dateTime = LocalDateTime.parse(timeField.getText(), inputFormatter);

                // Convert to ISO-8601 format (commonly required by APIs)
                 //dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                // Send to backend
                patient.requestVideoConsultation(doctorIdField.getText(),dateTime);
                refreshVideoConsultations();
                JOptionPane.showMessageDialog(dialog, "Video consultation requested!");
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "Invalid date format! Use: yyyy-MM-dd HH:mm (e.g., 2024-05-06 14:30)",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
        return submitBtn;
    }

    private void openVideoLink(String link) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(link));
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Cannot open link automatically. Please copy and paste this link:\n" + link,
                        "Open Link", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException | URISyntaxException e) {
            JOptionPane.showMessageDialog(frame,
                    "Could not open video link: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Doctors findPrimaryDoctor(String PId) {
        String DID="";
        for(String Did:UserDatabase.getDoctorsForPatient(PId)){
            DID=Did;
        }
        return UserDatabase.getDoctorById(DID);
    }
    private void updateTrendCharts(JTabbedPane trendsTabPane) {
        // Remove all existing tabs
        trendsTabPane.removeAll();

        // Add refreshed charts
        trendsTabPane.addTab("Heart Rate", TrendAnalyzer.createHeartRateChart(patient));
        trendsTabPane.addTab("Blood Pressure", TrendAnalyzer.createBloodPressureChart(patient));
        trendsTabPane.addTab("Oxygen Level", TrendAnalyzer.createOxygenChart(patient));
        trendsTabPane.addTab("Temperature", TrendAnalyzer.createTemperatureChart(patient));

        // Optional: Force UI update
        trendsTabPane.revalidate();
        trendsTabPane.repaint();
    }
    private Doctors getDoctor(String Id){
        for(Doctors d:UserDatabase.LoadDoctors()){
            if(Id.equals(d.getUserId())){
                 return d;
            }
        }

        return null;
    }


    private void logout() {
        patient.logOut();
        frame.dispose();
        SwingUtilities.invokeLater(() -> new LoginSystem().createAndShowLoginScreen());
    }
}