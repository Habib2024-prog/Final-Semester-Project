package DashBoard;

import Model.Admin;
import Model.Doctors;
import Model.LoginSystem;
import Model.Patients;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import static Database.UserDatabase.assignDoctorToPatient;

public class AdminDashboard {
    private final Admin admin;
    private final JFrame frame;
    private DefaultTableModel doctorsModel;
    private DefaultTableModel patientsModel;

    public AdminDashboard(Admin admin) {
        if (admin == null) {
            throw new SecurityException("Unauthorized access attempt to AdminDashboard");
        }
        this.admin = admin;
        this.frame = new JFrame("Admin Dashboard - " + admin.getName());
        frame.setSize(1200, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        initializeModels();
        createUI();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void initializeModels() {
        // Doctors Table Model with Password column
        String[] doctorsColumns = {"ID", "Name", "Email", "Contact Number", "Password"};
        doctorsModel = new DefaultTableModel(doctorsColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Patients Table Model with Password column
        String[] patientsColumns = {"ID", "Name", "Email", "Contact", "Emergency Email", "Age", "Gender", "Password"};
        patientsModel = new DefaultTableModel(patientsColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };



        refreshDoctorsTable();
        refreshPatientsTable();

    }

    private void createUI() {
        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel welcomeLabel = new JLabel("Welcome, " + admin.getName() + " (Admin)", JLabel.LEFT);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(logoutButton, BorderLayout.EAST);
        frame.add(topPanel, BorderLayout.NORTH);

        // Main Tabbed Interface
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Doctor Management", createDoctorsTab());
        tabbedPane.addTab("Patient Management", createPatientsTab());
        tabbedPane.addTab("Assign Doctor For Patient",createAssignmentPanel());
        frame.add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createDoctorsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Doctors Table
        JTable doctorsTable = new JTable(doctorsModel);
        doctorsTable.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(doctorsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Doctor Management Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addDoctorBtn = new JButton("Add New Doctor");
        addDoctorBtn.addActionListener(e -> showAddDoctorDialog());
        buttonPanel.add(addDoctorBtn);

        JButton removeDoctorBtn = new JButton("Remove Doctor");
        removeDoctorBtn.addActionListener(e -> removeSelectedDoctor(doctorsTable));
        buttonPanel.add(removeDoctorBtn);

        JButton changePasswordBtn = new JButton("Change Password");
        changePasswordBtn.addActionListener(e -> changeDoctorPassword(doctorsTable));
        buttonPanel.add(changePasswordBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshDoctorsTable());
        buttonPanel.add(refreshBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createPatientsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Patients Table
        JTable patientsTable = new JTable(patientsModel);
        patientsTable.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(patientsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Patient Management Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addPatientBtn = new JButton("Add New Patient");
        addPatientBtn.addActionListener(e -> showAddPatientDialog());
        buttonPanel.add(addPatientBtn);

        JButton removePatientBtn = new JButton("Remove Patient");
        removePatientBtn.addActionListener(e -> removeSelectedPatient(patientsTable));
        buttonPanel.add(removePatientBtn);

        JButton changePasswordBtn = new JButton("Change Password");
        changePasswordBtn.addActionListener(e -> changePatientPassword(patientsTable));
        buttonPanel.add(changePasswordBtn);

        JButton generateReportBtn = new JButton("Generate Report");
        generateReportBtn.addActionListener(e -> generatePatientReport(patientsTable));
        buttonPanel.add(generateReportBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshPatientsTable());
        buttonPanel.add(refreshBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }
    private JPanel createAssignmentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(new JLabel("Assign Patient to Doctor"), gbc);

        // Patient ID
        gbc.gridy++;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Patient ID:"), gbc);

        JTextField patientIdField = new JTextField(15);
        gbc.gridx = 1;
        panel.add(patientIdField, gbc);

        // Doctor ID
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Doctor ID:"), gbc);

        JTextField doctorIdField = new JTextField(15);
        gbc.gridx = 1;
        panel.add(doctorIdField, gbc);

        // Assign Button
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        JButton assignButton = new JButton("Assign");
        panel.add(assignButton, gbc);

        // Status Label
        gbc.gridy++;
        JLabel statusLabel = new JLabel(" ");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(statusLabel, gbc);

        // Button action
        assignButton.addActionListener(e -> {
            String patientId = patientIdField.getText().trim();
            String doctorId = doctorIdField.getText().trim();

            if (patientId.isEmpty() || doctorId.isEmpty()) {
                statusLabel.setText("Please enter both Patient ID and Doctor ID");
                statusLabel.setForeground(Color.RED);
                return;
            }

            assignDoctorToPatient(patientId, doctorId);

            // Clear fields after assignment
            patientIdField.setText("");
            doctorIdField.setText("");

            statusLabel.setText("Patient " + patientId + " assigned to Doctor " + doctorId);
            statusLabel.setForeground(new Color(0, 100, 0)); // Dark green
        });

        return panel;
    }

    private void refreshDoctorsTable() {
        doctorsModel.setRowCount(0);
        List<Doctors> doctors = admin.ViewAllDoctors();
        for (Doctors doctor : doctors) {
            doctorsModel.addRow(new Object[]{
                    doctor.getUserId(),
                    doctor.getName(),
                    doctor.getEmail(),
                    doctor.getContactNumber(),
                    doctor.getPasswordHash() // Displaying the password
            });
        }
    }

    private void refreshPatientsTable() {
        patientsModel.setRowCount(0);
        List<Patients> patients = admin.AllPatients();
        for (Patients patient : patients) {
            patientsModel.addRow(new Object[]{
                    patient.getUserId(),
                    patient.getName(),
                    patient.getEmail(),
                    patient.getContactNumber(),
                    patient.getEmergencyEmail(),
                    patient.getAge(),
                    patient.getGender(),
                    patient.getPasswordHash()// Displaying the password
            });
        }
    }


    private void showAddDoctorDialog() {
        JDialog dialog = new JDialog(frame, "Add New Doctor", true);
        dialog.setLayout(new GridLayout(5, 2, 10, 10));
        dialog.setSize(400, 300);

        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField contactField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        dialog.add(new JLabel("Doctor ID:"));
        dialog.add(idField);
        dialog.add(new JLabel("Full Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Email:"));
        dialog.add(emailField);
        dialog.add(new JLabel("Contact Number:"));
        dialog.add(contactField);
        dialog.add(new JLabel("Password:"));
        dialog.add(passwordField);

        JButton submitBtn = new JButton("Submit");
        submitBtn.addActionListener(e -> {
            Doctors doctor = new Doctors(
                    idField.getText(),
                    nameField.getText(),
                    emailField.getText(),
                    contactField.getText(),
                    new String(passwordField.getPassword()),
                    false
            );

            admin.addDoctor(doctor);
            refreshDoctorsTable();
            JOptionPane.showMessageDialog(dialog, "Doctor added successfully!");
            dialog.dispose();
        });

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(submitBtn);
        dialog.add(cancelBtn);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void removeSelectedDoctor(JTable doctorsTable) {
        int selectedRow = doctorsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String doctorId = (String) doctorsTable.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Are you sure you want to remove this doctor?",
                    "Confirm Removal", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                admin.removeDoctor(doctorId);
                refreshDoctorsTable();
                JOptionPane.showMessageDialog(frame, "Doctor removed successfully!");
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a doctor to remove",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showAddPatientDialog() {
        JDialog dialog = new JDialog(frame, "Add New Patient", true);
        dialog.setLayout(new GridLayout(9, 2, 10, 10));
        dialog.setSize(500, 450);

        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField contactField = new JTextField();
        JTextField emergencyEmailField = new JTextField();
        JTextField ageField = new JTextField();
        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        JPasswordField passwordField = new JPasswordField();

        dialog.add(new JLabel("Patient ID:"));
        dialog.add(idField);
        dialog.add(new JLabel("Full Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Email:"));
        dialog.add(emailField);
        dialog.add(new JLabel("Contact Number:"));
        dialog.add(contactField);
        dialog.add(new JLabel("Emergency Email:"));
        dialog.add(emergencyEmailField);
        dialog.add(new JLabel("Age:"));
        dialog.add(ageField);
        dialog.add(new JLabel("Gender:"));
        dialog.add(genderCombo);
        dialog.add(new JLabel("Password:"));
        dialog.add(passwordField);

        JButton submitBtn = new JButton("Submit");
        submitBtn.addActionListener(e -> {
            try {
                Patients patient = new Patients(
                        idField.getText(),
                        nameField.getText(),
                        emailField.getText(),
                        contactField.getText(),
                        emergencyEmailField.getText(),
                        new String(passwordField.getPassword()),
                        Integer.parseInt(ageField.getText()),
                        (String) genderCombo.getSelectedItem(),
                        false
                );

                admin.addPatient(patient);
                refreshPatientsTable();
                JOptionPane.showMessageDialog(dialog, "Patient added successfully!");
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid age",
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

    private void removeSelectedPatient(JTable patientsTable) {
        int selectedRow = patientsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String patientId = (String) patientsTable.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Are you sure you want to remove this patient?",
                    "Confirm Removal", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                admin.removePatient(patientId);
                refreshPatientsTable();
                JOptionPane.showMessageDialog(frame, "Patient removed successfully!");
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a patient to remove",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void generatePatientReport(JTable patientsTable) {
        int selectedRow = patientsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String patientId = (String) patientsTable.getValueAt(selectedRow, 0);
            Patients patient = admin.findPatientById(patientId);
            if (patient != null) {
                admin.generatePatientReport(patient);
                JOptionPane.showMessageDialog(frame,
                        "Report generated successfully!",
                        "Report Generated", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Patient not found",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a patient to generate report",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    private void changeDoctorPassword(JTable doctorsTable) {
        int selectedRow = doctorsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String doctorId = (String) doctorsTable.getValueAt(selectedRow, 0);
            Doctors doctor = admin.findDoctorById(doctorId);

            if (doctor != null) {
                JPasswordField newPasswordField = new JPasswordField();
                JPasswordField confirmPasswordField = new JPasswordField();

                JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
                panel.add(new JLabel("New Password:"));
                panel.add(newPasswordField);
                panel.add(new JLabel("Confirm Password:"));
                panel.add(confirmPasswordField);

                int result = JOptionPane.showConfirmDialog(
                        frame,
                        panel,
                        "Change Password for " + doctor.getName(),
                        JOptionPane.OK_CANCEL_OPTION
                );

                if (result == JOptionPane.OK_OPTION) {
                    String newPassword = new String(newPasswordField.getPassword());
                    String confirmPassword = new String(confirmPasswordField.getPassword());

                    if (newPassword.equals(confirmPassword)) {
                        doctor.setPassword(newPassword);
                        admin.addDoctor(doctor);
                        refreshDoctorsTable();
                        JOptionPane.showMessageDialog(
                                frame,
                                "Password changed successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                                frame,
                                "Passwords do not match!",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(
                    frame,
                    "Please select a doctor to change password",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void changePatientPassword(JTable patientsTable) {
        int selectedRow = patientsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String patientId = (String) patientsTable.getValueAt(selectedRow, 0);
            Patients patient = admin.findPatientById(patientId);

            if (patient != null) {
                JPasswordField newPasswordField = new JPasswordField();
                JPasswordField confirmPasswordField = new JPasswordField();

                JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
                panel.add(new JLabel("New Password:"));
                panel.add(newPasswordField);
                panel.add(new JLabel("Confirm Password:"));
                panel.add(confirmPasswordField);

                int result = JOptionPane.showConfirmDialog(
                        frame,
                        panel,
                        "Change Password for " + patient.getName(),
                        JOptionPane.OK_CANCEL_OPTION
                );

                if (result == JOptionPane.OK_OPTION) {
                    String newPassword = new String(newPasswordField.getPassword());
                    String confirmPassword = new String(confirmPasswordField.getPassword());

                    if (newPassword.equals(confirmPassword)) {
                        patient.setPassword(newPassword);
                        admin.addPatient(patient);
                        refreshPatientsTable();
                        JOptionPane.showMessageDialog(
                                frame,
                                "Password changed successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                                frame,
                                "Passwords do not match!",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(
                    frame,
                    "Please select a patient to change password",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void logout() {
        admin.logOut();
        frame.dispose();
        SwingUtilities.invokeLater(() -> new LoginSystem().createAndShowLoginScreen());
    }
}
