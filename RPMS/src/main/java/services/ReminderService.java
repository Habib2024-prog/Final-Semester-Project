package services;

import Model.Patients;
import utill.Appointment;
import utill.Prescriptions;

import java.time.LocalDateTime;
import java.util.List;

public class ReminderService {
    static Notifier emailNotifier=new EmailNotifier();
    public static void sendAppointmentReminders(Patients patient) {
        List<Appointment> appointments = patient.getAppointments();
        LocalDateTime now = LocalDateTime.now();

        for (Appointment appt : appointments) {
            if (appt.getStatus() == Appointment.Status.CONFIRMED) {
                // Send reminder if appointment is within next 24 hours
                if (appt.getAppointmentDate().isAfter(now) &&
                        appt.getAppointmentDate().isBefore(now.plusHours(24))) {
                        String message="Reminder: You have an appointment tomorrow at "+appt.getAppointmentDate().toString();
                        emailNotifier.sendNotification(patient.getEmail(),"AppointmentReminder!!",message);
                }
            }
        }
    }

    public static void sendMedicationReminders(Patients patient) {
        List<Prescriptions> prescriptions = patient.getPrescriptions();
        if (prescriptions.isEmpty()) return;

        for (Prescriptions p : prescriptions) {
            // You can enhance this based on schedule time
            String message="Reminder: Take " + p.getMedicationName() + " - " + p.getDosage() + " (" + p.getSchedule() + ")";
            emailNotifier.sendNotification(patient.getEmail(),"MedicationReminder",message);
        }
    }
}


