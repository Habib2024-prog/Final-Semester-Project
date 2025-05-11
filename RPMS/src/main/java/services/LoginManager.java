package services;
import Database.UserDatabase;
import Model.Admin;
import Model.Doctors;
import Model.Patients;
import Model.User;

import java.util.*;

public class LoginManager {
    private Map<String, User> userMap;

    public LoginManager() {
        userMap = new HashMap<>();
        loadAllUsers(); // load from file system
    }

    private void loadAllUsers() {
        for (Doctors doc : UserDatabase.LoadDoctors()) {
            userMap.put(doc.getEmail(), doc);
        }
        for (Patients pat : UserDatabase.LoadPatient()) {
            userMap.put(pat.getEmail(), pat);
        }
        for(Admin ad: Objects.requireNonNull(UserDatabase.LoadAdmin())){
            userMap.put(ad.getEmail(),ad);
        }

    }

    public User login(String email, String password) {
        User user = userMap.get(email);
        if (user != null && user.checkPassword(password)) {
            user.login();

            // Add role-based access check
            if (user instanceof Admin) {
                return user;  // Allow admin login
            }
            // For others (doctors/patients), proceed as normal
            return user;
        }
        System.out.println("Login failed for: " + email);
        return null;
    }

    public void logout(User user) {
        if (user != null) user.logOut();
    }

    public void registerDoctor(Doctors doctor) {
        UserDatabase.saveDoctor(doctor);
        userMap.put(doctor.getEmail(), doctor);
    }

    public void registerPatient(Patients patient) {
        UserDatabase.savePatient(patient);
        userMap.put(patient.getEmail(), patient);
    }

    public User getUserByEmail(String email) {
        return userMap.get(email);
    }
}



