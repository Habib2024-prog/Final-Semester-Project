package Database;

import utill.Vitals;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class VitalsDatabase {
    private static final String DATA_DIR="vital/data";
    // ensuring of existing file
    static {
        File directory=new File(DATA_DIR);
        if(!directory.exists()){
            directory.mkdirs();
        }
    }
    public  static void appendVital(String patientId, Vitals vitals){
          String path= DATA_DIR+patientId+"vital.csv";
          boolean isNew=!(new File(path).exists());
          try (PrintWriter out=new PrintWriter(new BufferedWriter(new FileWriter(path,true)))) {
            if(isNew){
                out.println("timestamp,heartRate,oxygenLevel,temperature,bloodPressure");
            }
            out.println(vitals.toCSVLine());
          }
          catch (IOException e){
              System.err.println("Error writing vitals "+patientId+":"+e.getMessage());
          }
    }
    public static List<Vitals> loadVitals(String patientId){
        List<Vitals> list=new ArrayList<>();
        String path=DATA_DIR+patientId+"vital.csv";
        File file=new File(path);
        if(!file.exists()){return list;}
        try(BufferedReader bf=new BufferedReader(new FileReader(file))){
            bf.readLine();
            String line;
            int lineNo=0;
            while ((line=bf.readLine())!=null){
                lineNo++;
                try{
                    list.add(Vitals.fromCSVLine(line));
                } catch (Exception esx){
                    System.err.println("invalid csv line line no:"+lineNo+":"+esx.getMessage());
                }
            }


        }catch (IOException e){
            System.err.println("Error loading vitals for"+patientId+":"+e.getMessage());
        }


        return  list;
    }










}
