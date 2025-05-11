package Model;

import DashBoard.AdminDashboard;
import DashBoard.DoctorDashBoard;
import DashBoard.PatientDashboard;
import Database.UserDatabase;
import services.LoginManager;

import javax.swing.*;
import java.awt.*;

public class LoginSystem {
    private static LoginManager loginManager = new LoginManager();

    public static void main(String[] args) {

        if (UserDatabase.LoadAdmin().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "No admin accounts found in the system.\n" +
                            "Please contact system administrator.",
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        SwingUtilities.invokeLater(() -> {
            createAndShowLoginScreen();
        });
    }

    // LOGIN SCREEN
    public static void createAndShowLoginScreen() {
        JFrame frame = new JFrame("Remote Patient Monitoring System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout(10, 10));

        // Main panel
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel(" Remote Patient Monitoring Login System ", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        // Email Field
        gbc.gridy++;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        JTextField emailField = new JTextField(20);
        panel.add(emailField, gbc);

        // Password Field
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        // Login Button
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JButton loginButton = new JButton("Login");
        panel.add(loginButton, gbc);

        // Registration Options
        gbc.gridy++;
        JPanel registerPanel = new JPanel(new FlowLayout());
        registerPanel.add(new JLabel("No account? Register as:"));
        JButton patientRegisterBtn = new JButton("Patient");
        JButton doctorRegisterBtn = new JButton("Doctor");
        registerPanel.add(patientRegisterBtn);
        registerPanel.add(doctorRegisterBtn);
        panel.add(registerPanel, gbc);

        // Action Listeners
        loginButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            User user = loginManager.login(email, password);
            if (user != null) {
                frame.dispose();
                showDashboard(user);
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid email or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        patientRegisterBtn.addActionListener(e -> {
            frame.dispose();
            showPatientRegistrationForm();
        });

        doctorRegisterBtn.addActionListener(e -> {
            frame.dispose();
            showDoctorRegistrationForm();
        });

        frame.add(panel, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // PATIENT REGISTRATION FORM
    private static void showPatientRegistrationForm() {
        JFrame frame = new JFrame("Patient Registration");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Patient Registration", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        // Basic Info Fields
        String[] fields = {"User ID:", "Full Name:", "Email:", "Contact Number:",
                "Emergency Email:", "Age:", "Gender:"};
        JTextField[] textFields = new JTextField[fields.length];

        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i + 1;
            gbc.gridwidth = 1;
            panel.add(new JLabel(fields[i]), gbc);

            gbc.gridx = 1;
            textFields[i] = new JTextField(20);
            panel.add(textFields[i], gbc);
        }

        // Password Fields
        gbc.gridx = 0;
        gbc.gridy = fields.length + 1;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField confirmPasswordField = new JPasswordField(20);
        panel.add(confirmPasswordField, gbc);

        // Register Button
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JButton registerButton = new JButton("Register");
        panel.add(registerButton, gbc);

        // Back Button
        JButton backButton = new JButton("Back to Login");
        panel.add(backButton, gbc);

        // Action Listeners
        registerButton.addActionListener(e -> {
            // Validate passwords match
            if (!new String(passwordField.getPassword()).equals(new String(confirmPasswordField.getPassword()))) {
                JOptionPane.showMessageDialog(frame, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Create new patient
                Patients patient = new Patients(
                        textFields[0].getText(), // userId
                        textFields[1].getText(), // name
                        textFields[2].getText(), // email
                        textFields[3].getText(), // contactNumber
                        textFields[4].getText(), // emergencyEmail
                        new String(passwordField.getPassword()), // password
                        Integer.parseInt(textFields[5].getText()), // age
                        textFields[6].getText(), // gender
                        false // isHash
                );

                loginManager.registerPatient(patient);
                JOptionPane.showMessageDialog(frame, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                frame.dispose();
                createAndShowLoginScreen();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Registration Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        backButton.addActionListener(e -> {
            frame.dispose();
            createAndShowLoginScreen();
        });

        frame.add(panel, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // DOCTOR REGISTRATION FORM
    private static void showDoctorRegistrationForm() {
        JFrame frame = new JFrame("Doctor Registration");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(450, 350);
        frame.setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Doctor Registration", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        // Basic Info Fields
        String[] fields = {"User ID:", "Full Name:", "Email:", "Contact Number:"};
        JTextField[] textFields = new JTextField[fields.length];

        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i + 1;
            gbc.gridwidth = 1;
            panel.add(new JLabel(fields[i]), gbc);

            gbc.gridx = 1;
            textFields[i] = new JTextField(20);
            panel.add(textFields[i], gbc);
        }

        // Password Fields
        gbc.gridx = 0;
        gbc.gridy = fields.length + 1;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField confirmPasswordField = new JPasswordField(20);
        panel.add(confirmPasswordField, gbc);

        // Register Button
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JButton registerButton = new JButton("Register");
        panel.add(registerButton, gbc);

        // Back Button
        JButton backButton = new JButton("Back to Login");
        panel.add(backButton, gbc);

        // Action Listeners
        registerButton.addActionListener(e -> {
            if (!new String(passwordField.getPassword()).equals(new String(confirmPasswordField.getPassword()))) {
                JOptionPane.showMessageDialog(frame, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Doctors doctor = new Doctors(
                        textFields[0].getText(), // userId
                        textFields[1].getText(), // name
                        textFields[2].getText(), // email
                        textFields[3].getText(), // contactNumber
                        new String(passwordField.getPassword()), // password
                        false // isHash
                );

                loginManager.registerDoctor(doctor);
                JOptionPane.showMessageDialog(frame, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                frame.dispose();
                createAndShowLoginScreen();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Registration Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        backButton.addActionListener(e -> {
            frame.dispose();
            createAndShowLoginScreen();
        });

        frame.add(panel, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // DASHBOARD DISPLAY
    private static void showDashboard(User user) {
        JFrame frame = new JFrame("RPMS Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel panel = new JPanel(new BorderLayout());

        // Welcome message
        String welcomeMessage = String.format("Welcome, %s (%s)", user.getName(), user.getClass().getSimpleName());
        JLabel welcomeLabel = new JLabel(welcomeMessage, JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(welcomeLabel, BorderLayout.NORTH);

        // Role-specific content
        JPanel contentPanel = new JPanel();
        if (user instanceof Patients) {
             new PatientDashboard((Patients)user);
        } else if (user instanceof Doctors) {
            contentPanel.add(new JLabel("Doctor Dashboard - Patient Management"));
             new DoctorDashBoard((Doctors)user);
        } else if (user instanceof Admin) {
            contentPanel.add(new JLabel("Admin Dashboard - System Management"));
                   new AdminDashboard((Admin)user);
        }

        panel.add(contentPanel, BorderLayout.CENTER);

        // Logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            loginManager.logout(user);
            frame.dispose();
            createAndShowLoginScreen();
        });
        panel.add(logoutButton, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
