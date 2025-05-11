package utill;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static utill.Vitals.formatter;

public class VitalsUploader {

 public static List<Vitals>readVitalsFromCSV(String filepath){
     List <Vitals>vitals=new ArrayList<>();
     try(BufferedReader bf=new BufferedReader(new FileReader(filepath))){
         bf.readLine();
         String line;
         int lineNo=0;
         while ((line=bf.readLine())!=null){
             lineNo++;
             try{
                 String[]column=line.split(",");

                 double hr=Double.parseDouble(column[0]);
                 double oxy=Double.parseDouble(column[1]);
                 double tem=Double.parseDouble(column[2]);
                 String bp=column[3];
                 ValidateVitals.ValidateVitalsSign(hr,oxy,tem,bp);
                 vitals.add(new Vitals(hr,tem,oxy,bp));
             } catch (NumberFormatException | ArrayIndexOutOfBoundsException|VitalException es){
                 System.err.println("invalid csv line no:"+lineNo+":"+es.getMessage());
             }
         }


     }catch (IOException e){
         System.err.println("Error failed to read file "+e.getMessage());
     }


     return vitals ;


 }










}
