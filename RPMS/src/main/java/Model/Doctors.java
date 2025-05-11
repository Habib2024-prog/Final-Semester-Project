package Model;

import Database.AppointmentDatabase;
import Database.VideoConsultationDatabase;
import Database.VitalsDatabase;
import services.VideoConsultation;
import utill.Appointment;
import utill.FeedBack;
import utill.Prescriptions;
import utill.Vitals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Doctors extends User {
     List<Appointment>appointments;
     List<VideoConsultation>videoConsultationList;

    public Doctors(String userId,String name,String email,String contactNumber,String password,boolean isHash){
        super(userId,name,email,contactNumber,password,isHash);
        this.appointments=AppointmentDatabase.loadDoctorAppointment(getUserId());
        this.videoConsultationList=VideoConsultationDatabase.loadConsultations();
    }

    //getter get all appointment
    public List<Appointment> getAppointmentList() {
        return appointments;
    }

    public List<VideoConsultation> getVideoConsultationList() {
        return videoConsultationList;
    }

    public void giveFeedBack(Patients patients, String feedBackText){
        patients.addFeedBack(new FeedBack(feedBackText));
    }
    public void ViewPatientVitals(Patients patients){
        System.out.println("Patient Name:"+patients.getName());
       for( Vitals v:VitalsDatabase.loadVitals(patients.getUserId())){
           System.out.println(v.toString());
       }
    }
    public void givePrescription(Patients patients, Prescriptions pres){
        patients.addPrescription(pres);
    }
    public void viewPatientHistory(Patients patients){
        patients.medicalHistory();
    }
    public List<Appointment> getPendingAppointment(){
        List <Appointment>pending=new ArrayList<>();
        for(Appointment app:AppointmentDatabase.loadDoctorAppointment(getUserId())){
            if(app.getStatus()==Appointment.Status.PENDING){
                pending.add(app);
            }
        }
        return pending;
    }
    public boolean approveAppointment(String appointmentId) {
        for (Appointment appt : appointments) {
            if (appt.getAppointmentId().equals(appointmentId) && appt.getStatus() == Appointment.Status.PENDING) {
                appt.approve(); // Only change status
                // Save updated list (no adding new appointments!)
                AppointmentDatabase.saveDoctorAppointment(this.getUserId(), appointments);

                // Also update patient's side
                List<Appointment> patientAppointments = AppointmentDatabase.loadPatientAppointment(appt.getPatientId());
                for (Appointment pAppt : patientAppointments) {
                    if (pAppt.getAppointmentId().equals(appointmentId)) {
                        pAppt.approve();
                        break;
                    }
                }
                AppointmentDatabase.savePatientAppointment(appt.getPatientId(), patientAppointments);

                return true;
            }
        }
        return false;
    }
    public boolean rejectAppointment(String appointmentId) {
        for (Appointment appt : appointments) {
            if (appt.getAppointmentId().equals(appointmentId) && appt.getStatus() == Appointment.Status.PENDING) {
                appt.reject(); // Only change status
                AppointmentDatabase.saveDoctorAppointment(this.getUserId(), appointments);

                List<Appointment> patientAppointments = AppointmentDatabase.loadPatientAppointment(appt.getPatientId());
                for (Appointment pAppt : patientAppointments) {
                    if (pAppt.getAppointmentId().equals(appointmentId)) {
                        pAppt.reject();
                        break;
                    }
                }
                AppointmentDatabase.savePatientAppointment(appt.getPatientId(), patientAppointments);

                return true;
            }
        }
        return false;
    }
    public List<VideoConsultation> viewVideoConsultationRequest(){
        List <VideoConsultation>list=new ArrayList<>();
        for(VideoConsultation vd:VideoConsultationDatabase.loadConsultations()){
            if(vd.getDoctorId().equals(getUserId())){
                list.add(vd);
            }
        }
        return list;
    }

    public boolean approveVideoConsultation(String patientId, String platform, String link) {
        for(VideoConsultation vd : videoConsultationList) {
            if(vd.getDoctorId().equals(getUserId()) &&
                    vd.getPatientId().equals(patientId) &&
                    !vd.isApproved()) {
                vd.setPlatform(platform);
                vd.setLink(link);
                vd.setApproved(true);
                vd.setApprovalTime(LocalDateTime.now()); // Add timestamp
                VideoConsultationDatabase.saveConsultation(videoConsultationList);
                return true; // Success
            }
        }
        return false; // Failed to find matching consultation
    }

    @Override
    public void login() {
        System.out.println(" Doctor " + getName() + " logged in successfully!");
    }

    @Override
    public void logOut() {
        System.out.println(" Doctor " + getName() + " logged out");
    }
}

