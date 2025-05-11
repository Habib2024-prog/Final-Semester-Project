package Database;

import Model.Admin;
import Model.Doctors;
import Model.Patients;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserDatabase {
    private final static String DOCTOR_FILE = "UserFile\\Doctor\\doctor.txt";
    private final static String PATIENT_FILE = "UserFile\\Patient\\patient.txt";
    private final static String ADMIN = "UserFile/Admin/admin.txt";

    public static void savePatient(Patients patients){
        List<Patients>existing=LoadPatient();
        for(Patients p:existing){
            if(p.getUserId().equals(patients.getUserId())){
                System.out.println("Patient Already Exist!");
                return;}
        }
        existing.add(patients);
        savePatientToFile(existing);
    }
    public static void saveDoctor(Doctors doctors){
        List<Doctors>existing=LoadDoctors();
        for(Doctors d:existing){
            if(d.getUserId().equals(doctors.getUserId())){
                System.out.println("Doctor Already Exist!");
                return;
            }
        }
        existing.add(doctors);
        saveDoctorToFile(existing);
    }
    public static  void removePatient(String patientId){
        List<Patients>patients=LoadPatient();
        patients.removeIf(p->p.getUserId().equals(patientId));
        savePatientToFile(patients);
    }
    public static  void removeDoctor(String doctorId){
        List<Doctors>doctors=LoadDoctors();
        doctors.removeIf(d->d.getUserId().equals(doctorId));
        saveDoctorToFile(doctors);
    }



    public static void saveDoctorToFile(List<Doctors>doctor){
        File file = new File(DOCTOR_FILE);
        if(!file.exists()){
            file.mkdirs();
        }
        try (PrintWriter out=new PrintWriter(new FileWriter(file))){
            for(Doctors d:doctor){
                out.println(d.getUserId()+"|"+d.getName()+"|"+d.getEmail()+
                        "|"+d.getContactNumber()+"|"+ d.getPasswordHash());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void savePatientToFile(List<Patients>patient){

        File file = new File(PATIENT_FILE);
        if(!file.exists()){
            file.mkdirs();
        }
        try (PrintWriter out=new PrintWriter(new FileWriter(file))){
            for(Patients p:patient){
                out.println(p.getUserId()+"|"+p.getName()+"|"+p.getEmail()+"|"+
                        p.getContactNumber()+ "|"+p.getEmergencyEmail()+"|"+p.getPasswordHash()
                        +"|"+p.getAge()+"|"+p.getGender());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static List<Patients>LoadPatient(){
        List<Patients>patient=new ArrayList<>();
        File file =new File(PATIENT_FILE);
        if(!file.exists()){return patient;}
        try(BufferedReader br=new BufferedReader(new FileReader(file))){
            String line;
            while ((line=br.readLine())!=null){
               String[]parts=line.split("\\|");
             Patients p=new Patients(parts[0],parts[1],parts[2],parts[3],
                     parts[4],parts[5],Integer.parseInt(parts[6]),parts[7],true);

             patient.add(p);
            }

        }catch (IOException e){
            e.printStackTrace();
        }
        return patient;
    }
    public static List<Doctors>LoadDoctors(){
        List<Doctors>doctor=new ArrayList<>();
        File file =new File(DOCTOR_FILE);
        if(!file.exists()){return doctor;}
        try(BufferedReader br=new BufferedReader(new FileReader(file))){
            String line;
            while ((line=br.readLine())!=null){
                String[]parts=line.split("\\|");
                Doctors d=new Doctors(parts[0],parts[1],parts[2],parts[3],parts[4],true);
                doctor.add(d);

            }

        }catch (IOException e){
            e.printStackTrace();
        }
        return doctor;
    }
    public static void saveAdminToFile(Admin admins){
        File file = new File(ADMIN);
        if(!file.exists()){
            file.mkdirs();
        }
        try (PrintWriter out=new PrintWriter(new FileWriter(file))){
                out.println(admins.getUserId()+"|"+admins.getName()+"|"+admins.getEmail()+
                        "|"+admins.getContactNumber()+"|"+ admins.getPasswordHash());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public  static List<Admin> LoadAdmin(){
        List<Admin>admins=new ArrayList<>();
        File file =new File(ADMIN);
        if(!file.exists()){return admins;}
        try(BufferedReader br=new BufferedReader(new FileReader(file))){
            String line;
            while ((line=br.readLine())!=null){
                String[]parts=line.split("\\|");
                if(parts.length==5) {
                    Admin admin = new Admin(parts[0], parts[1], parts[2], parts[3], parts[4], true);
                    admins.add(admin);
                }
            }

        }catch (IOException e){
            e.printStackTrace();
        }
        return  admins;

    }
    private final static String PATIENT_DOCTOR_FILE = "UserFile/Relationships/patient_doctor.txt";

    public static void assignDoctorToPatient(String patientId, String doctorId) {
        try {
            // Create directory if it doesn't exist
            new File("UserFile/Relationships").mkdirs();

            // Append the relationship to file
            try (PrintWriter out = new PrintWriter(new FileWriter(PATIENT_DOCTOR_FILE, true))) {
                out.println(patientId + "|" + doctorId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getDoctorsForPatient(String patientId) {
        List<String> doctorIds = new ArrayList<>();
        File file = new File(PATIENT_DOCTOR_FILE);

        if (!file.exists()) {
            return doctorIds;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 2 && parts[0].equals(patientId)) {
                    doctorIds.add(parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doctorIds;
    }

    public static Doctors getDoctorById(String doctorId) {
        List<Doctors> doctors = LoadDoctors();
        for (Doctors doctor : doctors) {
            if (doctor.getUserId().equals(doctorId)) {
                return doctor;
            }
        }
        return null;
    }


}
