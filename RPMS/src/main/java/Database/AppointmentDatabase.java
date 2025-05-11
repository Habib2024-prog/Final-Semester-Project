package Database;

import utill.Appointment;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDatabase {
    // Patient directory path
    private static final String PATIENT_DIR="AppointmentFile/appt/patient/";
    // Doctor directory path
    private static final String DOCTOR_DIR="AppointmentFile/appt/doctor/";

    static {
        new File(PATIENT_DIR).mkdirs();
        new File(DOCTOR_DIR).mkdirs();

    }
    // method to save Patients appointment in their own file
    public static void savePatientAppointment(String Id,List<Appointment>apptlist){
        String path=PATIENT_DIR+Id+"_appointment.txt";
        saveAppointment(path,apptlist);
    }
    // method to save doctor appointment in their own file
    public static void saveDoctorAppointment(String Id,List<Appointment>apptlist){
        String path=DOCTOR_DIR+Id+"_appointment.txt";
        saveAppointment(path,apptlist);
    }
    // method to load patient appointment from their file
    public static List<Appointment>loadPatientAppointment(String patientId){
        String path=PATIENT_DIR+patientId+"_appointment.txt";
        return loadAppointment(path);
    }
    // method to load Doctor appointment from their file
    public static List<Appointment>loadDoctorAppointment(String doctorId){
        String path=DOCTOR_DIR+doctorId+"_appointment.txt";
        return loadAppointment(path);
    }
    // method to save appointment in file
    public static  void saveAppointment(String filePath, List<Appointment> appointmentList){
        try(PrintWriter out=new PrintWriter(new FileWriter(filePath))){
            for(Appointment app:appointmentList){
                 out.println(app.getAppointmentId()+"|"+
                         app.getPatientId()+"|"+app.getDoctorId()+
                         "|"+app.getAppointmentDate()+"|"+
                         app.getDescription()+"|"+app.getStatus());
            }

        }catch (IOException e){
            System.err.println("Error in Saving appointment!"+e.getMessage());
        }
    }
    //method to load appointment from file
    public  static  List<Appointment> loadAppointment(String filePath){
         List <Appointment>appt =new ArrayList<>();
         File file=new File(filePath);
         if(!file.exists()){return appt;}
         try(BufferedReader br=new BufferedReader(new FileReader(file))){
             String line;
             while ((line= br.readLine())!=null){
                 String[]parts=line.split("\\|");
                 if(parts.length==6){
                     Appointment a=new Appointment(parts[0],parts[1]
                             ,parts[2],java.time.LocalDateTime.parse(parts[3]),parts[4]
                             ,Appointment.Status.valueOf(parts[5]));

                     appt.add(a);
                 }
             }
         }catch (IOException e){
             System.err.println("Error in Reading appointment!"+e.getMessage());
         }
         return appt;
    }



}
