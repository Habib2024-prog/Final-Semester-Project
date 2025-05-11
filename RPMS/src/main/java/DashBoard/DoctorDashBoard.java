package DashBoard;

import Chat.DoctorChatPanel;
import Database.UserDatabase;
import Model.Doctors;
import Model.LoginSystem;
import Model.Patients;
import services.ReportGenerator;
import services.VideoConsultation;
import utill.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DoctorDashBoard {
    private final Doctors doctor;
    private final JFrame frame;
    private DefaultTableModel appointmentsModel;
    private DefaultTableModel pendingAppointmentsModel;
    private DefaultTableModel prescriptionsModel;
    private DefaultTableModel patientsModel;
    private DefaultTableModel videoConsultationsModel;
    private List<Patients> allPatients;
    private DefaultTableModel pendingConsultationsModel;


    public DoctorDashBoard(Doctors doctor) {
        if (doctor == null) {
            throw new IllegalArgumentException("Doctor cannot be null");
        }

        this.doctor = doctor;
        this.frame = new JFrame("Doctor Dashboard - Dr. " + doctor.getName());
        frame.setSize(1200, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Initialize components safely
        try {
            this.allPatients = UserDatabase.LoadPatient();
            initializeModels();
            createUI();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Failed to initialize dashboard: " + e.getMessage(),
                    "Initialization Error",
                    JOptionPane.ERROR_MESSAGE);
            System.err.println("Dashboard initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void initializeModels() {
        // Initialize with proper column names and empty data
        appointmentsModel = new DefaultTableModel(
                new String[]{"ID", "Patient", "Date/Time", "Description", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        pendingAppointmentsModel = new DefaultTableModel(
                new String[]{"ID", "Patient", "Date/Time", "Description"}, 0);

        patientsModel = new DefaultTableModel(
                new String[]{"ID", "Name", "Age", "Gender", "Contact", "Emergency Email"}, 0);

        prescriptionsModel = new DefaultTableModel(
                new String[]{"Patient", "Medication", "Dosage", "Schedule"}, 0);

        // Initialize videoConsultationsModel with proper columns
        videoConsultationsModel = new DefaultTableModel(
                new String[]{"Patient", "Request Date", "Preferred Time", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        refreshAllData();



    }

    private void refreshAllData() {
        try {
            refreshAppointmentsTable();
            refreshPendingAppointments();
            refreshPatientsTable();
            refreshPrescriptionsTable();
            refreshVideoConsultations();

            // Show success message
            JOptionPane.showMessageDialog(frame,
                    "All data refreshed successfully!",
                    "Refresh Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame,
                    "Error refreshing data: " + e.getMessage(),
                    "Refresh Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void createUI() {
        // Top Panel with Welcome and Logout
        // Top Panel with Welcome and Logout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel welcomeLabel = new JLabel("Welcome, Dr. " + doctor.getName(), JLabel.LEFT);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));

        // Create button panel for right side
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Add refresh button
        JButton refreshButton = new JButton("Refresh All");
        refreshButton.setToolTipText("Refresh all tables and data");
        refreshButton.addActionListener(e -> refreshAllData());
        buttonPanel.add(refreshButton);

        // Add logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        buttonPanel.add(logoutButton);

        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        frame.add(topPanel, BorderLayout.NORTH);

        // Main Tabbed Interface
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: Appointments
        tabbedPane.addTab("Appointments", createAppointmentsTab());

        // Tab 2: Patient Management
        tabbedPane.addTab("Patients", createPatientsTab());

        // Tab 3: Video Consultations
        tabbedPane.addTab("Video Consultations", createVideoConsultationsTab());
        //Tab 4 chat
        tabbedPane.addTab("Patient Chat",createChatPanel());


        frame.add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createAppointmentsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Pending Appointments Panel
        JPanel pendingPanel = new JPanel(new BorderLayout());
        pendingPanel.setBorder(BorderFactory.createTitledBorder("Pending Appointments"));

        // Initialize the table model if null
        if (pendingAppointmentsModel == null) {
            pendingAppointmentsModel = new DefaultTableModel(
                    new String[]{"ID", "Patient", "Date/Time", "Description"}, 0);
        }

        JTable pendingTable = new JTable(pendingAppointmentsModel);
        pendingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Ensure single selection
        JScrollPane pendingScrollPane = new JScrollPane(pendingTable);
        pendingScrollPane.setPreferredSize(new Dimension(0, 150));

        // Appointment Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton approveBtn = new JButton("Approve Appointment");
        approveBtn.addActionListener(e -> {
            int selectedRow = pendingTable.getSelectedRow();
            if (selectedRow >= 0) {
                String appointmentId = (String) pendingTable.getValueAt(selectedRow, 0);
                if (doctor.approveAppointment(appointmentId)) {
                    refreshAppointmentsTable();
                    refreshPendingAppointments();
                    JOptionPane.showMessageDialog(frame, "Appointment approved!");
                } else {
                    JOptionPane.showMessageDialog(frame,
                            "Failed to approve appointment",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Please select an appointment first",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton rejectBtn = new JButton("Reject Appointment");
        rejectBtn.addActionListener(e -> {
            int selectedRow = pendingTable.getSelectedRow();
            if (selectedRow >= 0) {
                String appointmentId = (String) pendingTable.getValueAt(selectedRow, 0);
                if (doctor.rejectAppointment(appointmentId)) {
                    refreshAppointmentsTable();
                    refreshPendingAppointments();
                    JOptionPane.showMessageDialog(frame, "Appointment rejected!");
                } else {
                    JOptionPane.showMessageDialog(frame,
                            "Failed to reject appointment",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Please select an appointment first",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        // Add refresh button specifically for this tab
        JButton refreshBtn = new JButton("Refresh Pending");
        refreshBtn.addActionListener(e -> {
            refreshPendingAppointments();
            if (pendingAppointmentsModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(frame,
                        "No pending appointments found",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        actionPanel.add(approveBtn);
        actionPanel.add(rejectBtn);
        actionPanel.add(refreshBtn);

        pendingPanel.add(pendingScrollPane, BorderLayout.CENTER);
        pendingPanel.add(actionPanel, BorderLayout.SOUTH);

        // All Appointments Panel
        JPanel allAppointmentsPanel = new JPanel(new BorderLayout());
        allAppointmentsPanel.setBorder(BorderFactory.createTitledBorder("All Appointments"));

        JTable appointmentsTable = new JTable(appointmentsModel);
        appointmentsTable.setAutoCreateRowSorter(true);
        allAppointmentsPanel.add(new JScrollPane(appointmentsTable), BorderLayout.CENTER);

        // Combine panels
        panel.add(pendingPanel, BorderLayout.NORTH);
        panel.add(allAppointmentsPanel, BorderLayout.CENTER);

        // Load initial data
        refreshPendingAppointments();

        return panel;
    }
    private JPanel createPatientsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Main container panel that will hold everything
        JPanel mainContainer = new JPanel(new BorderLayout());

        // Patients Table Panel
        JPanel patientsTablePanel = new JPanel(new BorderLayout());
        patientsTablePanel.setBorder(BorderFactory.createTitledBorder("Patient List"));

        JTable patientsTable = new JTable(patientsModel);
        patientsTable.setAutoCreateRowSorter(true);
        JScrollPane patientsScrollPane = new JScrollPane(patientsTable);
        patientsScrollPane.setPreferredSize(new Dimension(0, 300)); // Set preferred height

        // Action buttons panel now with GridLayout for better button arrangement
        JPanel patientActionPanel = new JPanel(new GridLayout(2, 3, 5, 5)); // 2 rows, 3 columns
        patientActionPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Create buttons
        JButton viewVitalsBtn = new JButton("View Vitals");
        viewVitalsBtn.addActionListener(e -> viewPatientVitals(patientsTable));

        JButton viewHistoryBtn = new JButton("View Medical History");
        viewHistoryBtn.addActionListener(e -> viewPatientHistory(patientsTable));

        JButton generateReportBtn = new JButton("Generate Report");
        generateReportBtn.addActionListener(e -> {
            int selectedRow = patientsTable.getSelectedRow();
            if (selectedRow >= 0) {
                String patientId = (String) patientsTable.getValueAt(selectedRow, 0);
                Patients patient = findPatientById(patientId);
                if (patient != null) {
                    ReportGenerator.generatePatientReport(patient);
                }
            }
        });

        JButton newPrescriptionBtn = new JButton("New Prescription");
        newPrescriptionBtn.addActionListener(e -> createNewPrescription(patientsTable));

        JButton addFeedbackBtn = new JButton("Add Feedback");
        addFeedbackBtn.addActionListener(e -> addFeedback(patientsTable));

        JButton refreshPatientsBtn = new JButton("Refresh");
        refreshPatientsBtn.addActionListener(e -> refreshPatientsTable());

        // Add buttons to the panel
        patientActionPanel.add(viewVitalsBtn);
        patientActionPanel.add(viewHistoryBtn);
        patientActionPanel.add(generateReportBtn);
        patientActionPanel.add(newPrescriptionBtn);
        patientActionPanel.add(addFeedbackBtn);
        patientActionPanel.add(refreshPatientsBtn);

        // Add components to patients table panel
        patientsTablePanel.add(patientsScrollPane, BorderLayout.CENTER);
        patientsTablePanel.add(patientActionPanel, BorderLayout.SOUTH);

        // Prescriptions Panel
        JPanel prescriptionsPanel = new JPanel(new BorderLayout());
        prescriptionsPanel.setBorder(BorderFactory.createTitledBorder("Recent Prescriptions"));

        JTable prescriptionsTable = new JTable(prescriptionsModel);
        prescriptionsTable.setAutoCreateRowSorter(true);
        JScrollPane prescriptionsScrollPane = new JScrollPane(prescriptionsTable);
        prescriptionsScrollPane.setPreferredSize(new Dimension(0, 300)); // Set preferred height

        prescriptionsPanel.add(prescriptionsScrollPane, BorderLayout.CENTER);

        // Combine panels using a split pane for better resizing
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, patientsTablePanel, prescriptionsPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(0.5);

        mainContainer.add(splitPane, BorderLayout.CENTER);
        panel.add(mainContainer, BorderLayout.CENTER);

        return panel;
    }



    private JPanel createVideoConsultationsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Use the class-level videoConsultationsModel instead of creating a new one
        JTable pendingTable = new JTable(videoConsultationsModel);
        pendingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Input Panel for approval details
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField platformField = new JTextField("Zoom");
        JTextField linkField = new JTextField();

        inputPanel.add(new JLabel("Platform (Zoom/Meet):"));
        inputPanel.add(platformField);
        inputPanel.add(new JLabel("Meeting Link:"));
        inputPanel.add(linkField);

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton approveButton = new JButton("Approve Consultation");
        approveButton.addActionListener(e -> approveVideoConsultation(
                pendingTable,
                platformField.getText(),
                linkField.getText()
        ));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshVideoConsultations());

        buttonPanel.add(refreshButton);
        buttonPanel.add(approveButton);

        // Main layout
        JPanel approvalPanel = new JPanel(new BorderLayout());
        approvalPanel.setBorder(BorderFactory.createTitledBorder("Pending Video Consultations"));
        approvalPanel.add(new JScrollPane(pendingTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        approvalPanel.add(bottomPanel, BorderLayout.SOUTH);
        panel.add(approvalPanel, BorderLayout.CENTER);

        // Load initial data
        refreshVideoConsultations();

        return panel;
    }
    private JPanel createChatPanel() {
        return new DoctorChatPanel(doctor.getUserId());
    }

    private void refreshAppointmentsTable() {
        appointmentsModel.setRowCount(0);
        for (Appointment appt : doctor.getAppointmentList()) {
            Patients patient = findPatientById(appt.getPatientId());
            String patientName = patient != null ? patient.getName() : appt.getPatientId();
            appointmentsModel.addRow(new Object[]{
                    appt.getAppointmentId(),
                    patientName,
                    appt.getAppointmentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    appt.getDescription(),
                    appt.getStatus()
            });
        }
    }

    private void refreshPendingAppointments() {
        pendingAppointmentsModel.setRowCount(0); // Clear existing data

        // Force reload of pending appointments from database
        doctor.getPendingAppointment();

        for (Appointment appt : doctor.getPendingAppointment()) {
            Patients patient = findPatientById(appt.getPatientId());
            String patientName = patient != null ? patient.getName() : appt.getPatientId();
            pendingAppointmentsModel.addRow(new Object[]{
                    appt.getAppointmentId(),
                    patientName,
                    appt.getAppointmentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    appt.getDescription()
            });
        }
    }

    private void refreshPatientsTable() {
        patientsModel.setRowCount(0);
        for (Patients patient : allPatients) {
            patientsModel.addRow(new Object[]{
                    patient.getUserId(),
                    patient.getName(),
                    patient.getAge(),
                    patient.getGender(),
                    patient.getContactNumber(),
                    patient.getEmergencyEmail()
            });
        }
    }

    private void refreshPrescriptionsTable() {
        prescriptionsModel.setRowCount(0);
        // Show prescriptions for all patients
        for (Patients patient : allPatients) {
            for (Prescriptions p : patient.getPrescriptions()) {
                prescriptionsModel.addRow(new Object[]{
                        patient.getName(),
                        p.getMedicationName(),
                        p.getDosage(),
                        p.getSchedule()
                });
            }
        }
    }

    private void refreshVideoConsultations() {
        // Clear existing data
        videoConsultationsModel.setRowCount(0);

        // Safely load and display consultations
        try {
            List<VideoConsultation> consultations = doctor.getVideoConsultationList();
            if (consultations != null) {
                for (VideoConsultation vc : consultations) {
                    if (vc != null && !vc.isApproved()) {
                        Patients patient = findPatientById(vc.getPatientId());
                        String patientName = patient != null ? patient.getName() : vc.getPatientId();
                        String status = vc.isApproved() ? "Approved" : "Pending";

                        videoConsultationsModel.addRow(new Object[]{
                                patientName,
                                vc.getRequestDate() != null ?
                                        vc.getRequestDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "N/A",
                                vc.getPreferredTime() != null ?
                                        vc.getPreferredTime() : "N/A",
                                status
                        });
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error refreshing video consultations: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void approveAppointment(JTable pendingTable) {
        int selectedRow = pendingTable.getSelectedRow();
        if (selectedRow >= 0) {
            String appointmentId = (String) pendingTable.getValueAt(selectedRow, 0);
            if (doctor.approveAppointment(appointmentId)) {
                refreshAppointmentsTable();
                refreshPendingAppointments();
                JOptionPane.showMessageDialog(frame, "Appointment approved!");
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to approve appointment", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select an appointment", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void rejectAppointment(JTable pendingTable) {
        int selectedRow = pendingTable.getSelectedRow();
        if (selectedRow >= 0) {
            String appointmentId = (String) pendingTable.getValueAt(selectedRow, 0);
            if (doctor.rejectAppointment(appointmentId)) {
                refreshAppointmentsTable();
                refreshPendingAppointments();
                JOptionPane.showMessageDialog(frame, "Appointment rejected.");
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to reject appointment", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select an appointment", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    private void viewPatientVitals(JTable patientsTable) {
        int selectedRow = patientsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(frame,
                    "Please select a patient",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String patientId = (String) patientsTable.getValueAt(selectedRow, 0);
        Patients patient = findPatientById(patientId);

        if (patient == null || patient.getVitalsList().isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "No vitals data available for selected patient",
                    "No Data",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Create dialog with tabs for table and trends
        JDialog vitalsDialog = new JDialog(frame, "Patient Vitals - " + patient.getName(), true);
        vitalsDialog.setSize(1200, 700);
        vitalsDialog.setLayout(new BorderLayout());

        // Tabbed pane for different views
        JTabbedPane tabbedPane = new JTabbedPane();

        // 1. Data Table Tab
        String[] columns = {"Timestamp", "Blood Pressure", "Heart Rate", "Oxygen Level", "Temperature"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        for (Vitals v : patient.getVitalsList()) {
            model.addRow(new Object[]{
                    v.getTimestamp().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                    v.getBloodPressure(),
                    v.getHeartRate(),
                    v.getOxygenLevel() + "%",
                    v.getTemperature() + "°C"
            });
        }

        JTable vitalsTable = new JTable(model);
        tabbedPane.addTab("Data View", new JScrollPane(vitalsTable));

        // 2. Trends Tab
        JPanel trendsPanel = new JPanel(new BorderLayout());

        // Create tabbed pane for different trend charts
        JTabbedPane trendsTabPane = new JTabbedPane();

        // Heart Rate Trend
        trendsTabPane.addTab("Heart Rate", TrendAnalyzer.createHeartRateChart(patient));

        // Blood Pressure Trend (with both systolic and diastolic)
        trendsTabPane.addTab("Blood Pressure", TrendAnalyzer.createBloodPressureChart(patient));

        // Oxygen Level Trend
        trendsTabPane.addTab("Oxygen Level", TrendAnalyzer.createOxygenChart(patient));

        // Temperature Trend
        trendsTabPane.addTab("Temperature", TrendAnalyzer.createTemperatureChart(patient));

        trendsPanel.add(trendsTabPane, BorderLayout.CENTER);
        tabbedPane.addTab("Trends View", trendsPanel);

        vitalsDialog.add(tabbedPane, BorderLayout.CENTER);
        vitalsDialog.setLocationRelativeTo(frame);
        vitalsDialog.setVisible(true);
    }



    private void viewPatientHistory(JTable patientsTable) {
        int selectedRow = patientsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String patientId = (String) patientsTable.getValueAt(selectedRow, 0);
            Patients patient = findPatientById(patientId);
            if (patient != null) {
                JDialog historyDialog = new JDialog(frame, "Medical History for " + patient.getName(), true);
                historyDialog.setSize(800, 600);

                JTabbedPane tabs = new JTabbedPane();

                // Feedback Tab
                JTextArea feedbackArea = new JTextArea();
                feedbackArea.setEditable(false);
                for (FeedBack fb : patient.getDoctorFeedback()) {
                    feedbackArea.append(fb.toString() + "\n\n");
                }
                tabs.addTab("Feedback", new JScrollPane(feedbackArea));

                // Prescriptions Tab
                JTextArea prescriptionsArea = new JTextArea();
                prescriptionsArea.setEditable(false);
                for (Prescriptions p : patient.getPrescriptions()) {
                    prescriptionsArea.append(p.toString() + "\n\n");
                }
                tabs.addTab("Prescriptions", new JScrollPane(prescriptionsArea));

                historyDialog.add(tabs);
                historyDialog.setLocationRelativeTo(frame);
                historyDialog.setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a patient", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void createNewPrescription(JTable patientsTable) {
        int selectedRow = patientsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String patientId = (String) patientsTable.getValueAt(selectedRow, 0);
            Patients patient = findPatientById(patientId);
            if (patient != null) {
                JDialog dialog = new JDialog(frame, "New Prescription for " + patient.getName(), true);
                dialog.setLayout(new GridLayout(4, 2, 10, 10));
                dialog.setSize(400, 250);

                JTextField medicationField = new JTextField();
                JTextField dosageField = new JTextField();
                JTextField scheduleField = new JTextField();

                dialog.add(new JLabel("Medication:"));
                dialog.add(medicationField);
                dialog.add(new JLabel("Dosage:"));
                dialog.add(dosageField);
                dialog.add(new JLabel("Schedule:"));
                dialog.add(scheduleField);

                JButton submitBtn = new JButton("Submit");
                submitBtn.addActionListener(e -> {
                    Prescriptions prescription = new Prescriptions(
                            medicationField.getText(),
                            dosageField.getText(),
                            scheduleField.getText()
                    );

                    patient.addPrescription(prescription);
                    refreshPrescriptionsTable();
                    JOptionPane.showMessageDialog(dialog, "Prescription added!");
                    dialog.dispose();
                });

                JButton cancelBtn = new JButton("Cancel");
                cancelBtn.addActionListener(e -> dialog.dispose());

                dialog.add(submitBtn);
                dialog.add(cancelBtn);
                dialog.setLocationRelativeTo(frame);
                dialog.setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a patient", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void addFeedback(JTable patientsTable) {
        int selectedRow = patientsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String patientId = (String) patientsTable.getValueAt(selectedRow, 0);
            Patients patient = findPatientById(patientId);
            if (patient != null) {
                JDialog dialog = new JDialog(frame, "Add Feedback for " + patient.getName(), true);
                dialog.setLayout(new BorderLayout());
                dialog.setSize(500, 300);

                JTextArea feedbackArea = new JTextArea();
                feedbackArea.setLineWrap(true);

                JButton submitBtn = new JButton("Submit Feedback");
                submitBtn.addActionListener(e -> {
                    if (!feedbackArea.getText().isEmpty()) {
                        patient.addFeedBack(new FeedBack(feedbackArea.getText()));
                        JOptionPane.showMessageDialog(dialog, "Feedback submitted!");
                        dialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Please enter feedback text", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });

                dialog.add(new JScrollPane(feedbackArea), BorderLayout.CENTER);
                dialog.add(submitBtn, BorderLayout.SOUTH);
                dialog.setLocationRelativeTo(frame);
                dialog.setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a patient", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void approveVideoConsultation(JTable pendingTable, String platform, String link) {
        int selectedRow = pendingTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(frame,
                    "Please select a consultation request first",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (platform.trim().isEmpty() || link.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "Platform and meeting link are required",
                    "Input Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get selected consultation data
        String patientName = (String) pendingTable.getValueAt(selectedRow, 0);
        Patients patient = findPatientByName(patientName);

        if (patient != null && doctor.approveVideoConsultation(patient.getUserId(), platform, link)) {
            // Remove the approved consultation from the model
            videoConsultationsModel.removeRow(selectedRow);

            JOptionPane.showMessageDialog(frame,
                    "Successfully approved consultation for " + patientName + "\n" +
                            "Platform: " + platform + "\n" +
                            "Link: " + link,
                    "Approval Successful",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame,
                    "Failed to approve consultation",
                    "Approval Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private Patients findPatientById(String patientId) {
        for (Patients p : allPatients) {
            if (p.getUserId().equals(patientId)) {
                return p;
            }
        }
        return null;
    }

    private void logout() {
        doctor.logOut();
        frame.dispose();
        SwingUtilities.invokeLater(() -> new LoginSystem().createAndShowLoginScreen());
    }
    private Patients findPatientByName(String name) {
        for (Patients p : allPatients) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }
}