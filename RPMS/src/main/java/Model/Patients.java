package Model;

import Database.*;
import services.EmergencyAlertSystem;
import services.EmergencyNotifier;
import services.ReminderService;
import services.VideoConsultation;
import utill.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Patients  extends User{
    private int age;
    private String gender;
    private String emergencyEmail;
    private List<Vitals>vitalsList;
    private List<FeedBack>doctorFeedback;
    private List<Prescriptions>prescriptions;
    private List<Appointment> appointments;
    private List<VideoConsultation>videoConsultations;
    private List<String>availableDoctor;

    public Patients(String userId,String name,String email,String contactNumber,String
            emergencyEmail,String password,int age,String gender,boolean isHash){
        super(userId,name,email,contactNumber,password,isHash);
        if(age<0||age>180)throw new IllegalArgumentException("Age is invalid");
        if(!emergencyEmail.contains("@gmail.com"))throw new IllegalArgumentException("Email must be Correct");
        this.emergencyEmail=emergencyEmail;
        this.age=age;
        this.gender=gender;
        this.vitalsList=VitalsDatabase.loadVitals(getUserId());
        this.doctorFeedback=FeedBackDatabase.loadFeedback(getUserId());
        this.prescriptions=PrescriptionDatabase.loadPrescription(getUserId());
        this.appointments=AppointmentDatabase.loadPatientAppointment(getUserId());
        this.videoConsultations= VideoConsultationDatabase.loadConsultations();
        this.availableDoctor=new ArrayList<>();
    }

    // getter

    public String getEmergencyEmail() {return emergencyEmail;}
    public int getAge() {return age;}
    public String getGender() {return gender;}
    public List<FeedBack> getDoctorFeedback(){return  doctorFeedback;}
    public List<Vitals>getVitalsList(){return  vitalsList;}
    public List<Prescriptions> getPrescriptions() {return prescriptions;}
    public List<Appointment> getAppointments() {return appointments;}
    public List<String>getAvailableDoctor(){return UserDatabase.getDoctorsForPatient(this.getUserId());}

    public void addAvailableDoctor(String doctorId){
        if(!availableDoctor.contains(doctorId)){
            availableDoctor.add(doctorId);
        }
    }
    public void addVitalSign(Vitals vitals,Doctors assignedDoctor){
        vitalsList.add(vitals);
        if(EmergencyAlertSystem.isVitalsCritical(vitals)){
            EmergencyNotifier.notifyDoctor(this,assignedDoctor);
            EmergencyNotifier.notifyPatientFamily(this);
            EmergencyNotifier.showEmergencyNotificationOnPatientDashboard(this);
        }
        VitalsDatabase.appendVital(getUserId(),vitals);
    }
    public void addFeedBack(FeedBack feedBack){
        doctorFeedback.add(feedBack);
        FeedBackDatabase.saveFeedback(getUserId(),feedBack);
    }
    public void addPrescription(Prescriptions pres){
        prescriptions.add(pres);
        PrescriptionDatabase.savePrescription(getUserId(),pres);
    }
    public void uploadVitalsFromFile(String filepath,Doctors assignedDoctor){
       for(Vitals vitals:VitalsUploader.readVitalsFromCSV(filepath)){
           addVitalSign(vitals,assignedDoctor);
       }
    }
    public void viewDoctorFeedBack(){
        for(FeedBack feed:doctorFeedback){
            System.out.println(feed.toString());
        }
    }
    //medical History of patient
    public void medicalHistory(){
        System.out.println("----<Feedback History>-----");
        System.out.println("PatientName:"+ getName());
     for(FeedBack feed:doctorFeedback){
         System.out.println(feed.toString());
     }
        System.out.println("----<Prescription History>-----");
        System.out.println("PatientName:"+ getName());
     for(Prescriptions pres:prescriptions){
         System.out.println(pres.toString());
     }
    }
    // requesting appointment
    public Appointment requestAppointment(String doctorId, String description, LocalDateTime dateTime){
        Appointment appt=new Appointment(this.getUserId(),doctorId,dateTime,description);
        appointments.add(appt);
        AppointmentDatabase.savePatientAppointment(getUserId(),appointments);
        // adding this request to doctor appointment list
        List<Appointment>doctorAppointment=AppointmentDatabase.loadDoctorAppointment(doctorId);
        doctorAppointment.add(appt);
        AppointmentDatabase.saveDoctorAppointment(doctorId,doctorAppointment);
        return appt;
    }
    public void ConfirmedAppointment(){
        for(Appointment app:appointments){
            if(app.getStatus()== Appointment.Status.CONFIRMED){
                System.out.println(app.toString());
            }
        }
    }
    public void requestVideoConsultation(String doctorId,LocalDateTime time){
        VideoConsultation vd =new VideoConsultation(getUserId(),doctorId,time,time);
        videoConsultations.add(vd);
        VideoConsultationDatabase.saveConsultation(videoConsultations);
    }

    public void viewApprovedVideoConsultation(){
        for(VideoConsultation vd:VideoConsultationDatabase.loadConsultations()){
            if(vd.isApproved()){
                System.out.println("Link: "+vd.getLink());
                System.out.println("Time: "+vd.getPreferredTime());
            }
        }
    }

    public void panicButton(Doctors emergencyDoctor) {
        System.out.println(" Patient " + getName() + " triggered manual Emergency Panic Button!");
        EmergencyNotifier.notifyDoctor(this, emergencyDoctor);
        EmergencyNotifier.notifyPatientFamily(this);
        EmergencyNotifier.showEmergencyNotificationOnPatientDashboard(this);

    }
    public void sendAppointmentReminder(){
        ReminderService.sendAppointmentReminders(this);
    }
    public void sendMedicationReminder(){
        ReminderService.sendMedicationReminders(this);
    }
    @Override
    public void login() {
        System.out.println(" Patient " + getName() + " logged in successfully!");
    }

    @Override
    public void logOut() {
        System.out.println(" Patient " + getName() + " logged out");
    }
}
