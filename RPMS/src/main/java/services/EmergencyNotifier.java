package services;

import Model.Doctors;
import Model.Patients;

public class EmergencyNotifier {

    private static Notifier emailNotifier = new EmailNotifier();
     // message...........

    public static void notifyDoctor(Patients patient, Doctors doctor) {
        String subject = "Emergency Alert for Patient: " + patient.getName();
        String message = "Critical vitals detected. Immediate attention needed for " + patient.getName() + ".";

        emailNotifier.sendNotification(doctor.getEmail(), subject, message);
    }

    public static void notifyPatientFamily(Patients patient) {
        String subject = " Emergency: Patient " + patient.getName();
        String message = "Emergency alert triggered for " + patient.getName() + ". Immediate action needed.";
        emailNotifier.sendNotification(patient.getEmergencyEmail(), subject, message);
    }

    public static void showEmergencyNotificationOnPatientDashboard(Patients patient) {
        System.out.println("[DASHBOARD] Emergency for " + patient.getName() + "!");
    }
}

