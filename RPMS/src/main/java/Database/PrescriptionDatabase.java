package Database;

import utill.FeedBack;
import utill.Prescriptions;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDatabase
{
    private static final String DIR="prescriptionFile/data";
    static {
        File directory=new File(DIR);
        if(!directory.exists()){
            directory.mkdirs();
        }
    }
    public static void savePrescription(String patientId, Prescriptions pres){
        String path = DIR + patientId + "prescription.csv";
        boolean isNew=!(new File(path).exists());
        try (PrintWriter out = new PrintWriter(new FileWriter(path,true))){
            if(isNew){
                out.println("MedicationName,Dosage,Schedule");
            }
            out.println(pres.getMedicationName()+","+pres.getDosage()+","+pres.getSchedule());

        }catch ( IOException e){
            System.err.println("Error at Saving feedback"+e.getMessage());
        }

    }
    public  static List<Prescriptions> loadPrescription(String patientId){
        List<Prescriptions> prescriptions= new ArrayList<>();
        String path = DIR + patientId + "prescription.csv";
        File file=new File(path);
        if(!file.exists()){return prescriptions;}
        try(BufferedReader bf=new BufferedReader(new FileReader(file))){
            bf.readLine();
            String line;
            int lineNo=0;
            while ((line= bf.readLine())!=null){
                String[]part=line.split(",");
                if(part.length==3) {
                    Prescriptions pres= new Prescriptions(part[0],part[1],part[2]);
                    prescriptions.add(pres);
                }

            }
        } catch (IOException |ArrayIndexOutOfBoundsException e){
            System.err.println("Error in writing feedback"+e.getMessage());
        }
        return prescriptions;
    }





}
