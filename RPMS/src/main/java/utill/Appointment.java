package utill;

import Model.Doctors;
import Model.Patients;

import java.time.LocalDateTime;
import java.util.UUID;

public class Appointment {
    public enum Status {
        CONFIRMED,PENDING,REJECTED
    }

    private String appointmentId;
    private String patientId;
    private String doctorId;
    private LocalDateTime appointmentDate;
    private String description;// why appointment scheduled
    private Status status;

    public Appointment(String patientId,String doctorId,LocalDateTime date,String description){
        this.patientId=patientId;
        this.appointmentDate=date;
        this.doctorId=doctorId;
        this.appointmentId= UUID.randomUUID().toString().substring(0,4);//randomly generate appointmentId
        this.status=Status.PENDING;
        this.description=description;

    }
        //constructor for manually you want to set appointment id
    public Appointment(String appointmentId,String patientId,String doctorId,LocalDateTime date,String description,Status status){
        this.appointmentId = appointmentId;
        this.appointmentDate = date;
        this.description = description;
        this.status = status;
        this.doctorId = doctorId;
        this.patientId = patientId;
    }
    // getter methods


    public String getAppointmentId(){return appointmentId;}
    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }
    public Status getStatus() {
        return status;
    }
    public String getDescription() {
        return description;
    }
    public String getDoctorId() {
        return doctorId;
    }
    public String getPatientId() {
        return patientId;
    }
    // setter methods as need in System
    public void setStatus(Status status) {
        this.status = status;
    }



    public void approve(){
      this.status=Status.CONFIRMED;
    }
    public void reject(){
        this.status=Status.REJECTED;
    }
    @Override
    public String toString() {
        return "Appointment ID: " + appointmentId +
                "\nPatient ID: " + patientId +
                "\nDoctor ID: " + doctorId +
                "\nDate & Time: " + appointmentDate +
                "\nDescription: " + description +
                "\nStatus: " + status;

    }
}
