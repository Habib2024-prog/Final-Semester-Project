package Model;

import Database.UserDatabase;
import services.ReportGenerator;

import java.util.ArrayList;
import java.util.List;

public class Admin extends User{

    public Admin(String id, String name, String email, String contactNumber, String password ,boolean isHash) {
        super(id, name, email, contactNumber, password,isHash);
    }


    //  Add a new doctor
    public void addDoctor(Doctors doctor) {
        UserDatabase.saveDoctor(doctor);
    }

    //  Remove a doctor
    public void removeDoctor(String doctorId) {
        UserDatabase.removeDoctor(doctorId);

    }
    public Doctors findDoctorById(String id) {
        for (Doctors d : UserDatabase.LoadDoctors()) {
            if (d.getUserId().equals(id)) return d;
        }
        return null;
    }

    // view list of all doctor
    public List<Doctors>ViewAllDoctors(){
        return UserDatabase.LoadDoctors();
    }

    //  Add a new patient
    public void addPatient(Patients patient) {
        UserDatabase.savePatient(patient);
    }
    //  Remove a patient
    public void removePatient(String patientId) {
        UserDatabase.removePatient(patientId);
    }

    public Patients findPatientById(String id) {
        for (Patients p : UserDatabase.LoadPatient()) {
            if (p.getUserId().equals(id)) return p;
        }
        return null;
    }

    //  View all patients
    public List<Patients> AllPatients() {
        return  UserDatabase.LoadPatient();
    }
    // managing report
    public void generatePatientReport(Patients patient) {
        ReportGenerator.generatePatientReport(patient);
    }

    @Override
    public void login() {
        System.out.println(" Admin " + getName() + " logged in successfully!");
    }

    @Override
    public void logOut() {
        System.out.println(" Admin " + getName() + " logged out!");
    }

}























