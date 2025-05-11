package Database;

import utill.FeedBack;

import javax.swing.*;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FeedBackDatabase {
    private static final String DIR="feedbackFile/data";
     static {
         File directory=new File(DIR);
         if(!directory.exists()){
             directory.mkdirs();
         }
     }
     public static void saveFeedback(String patientId,FeedBack feedBack){
         String path = DIR + patientId + "feedback.txt";
         try (PrintWriter out = new PrintWriter(new FileWriter(path,true))){
             out.println(feedBack.getFeedBackText()+"|"+feedBack.getFeedBackDate());
         }catch ( IOException e){
             System.err.println("Error at Saving feedback"+e.getMessage());
         }

     }
     public  static List<FeedBack> loadFeedback(String patientId){
         List<FeedBack> feedBacks= new ArrayList<>();
         String path=DIR+patientId+"feedback.txt";
         File file=new File(path);
         if(!file.exists()){return feedBacks;}
         try(BufferedReader bf=new BufferedReader(new FileReader(file))){
             String line;
             while ((line= bf.readLine())!=null){
                 String[]part=line.split("\\|",2);
                 if(part.length==2) {
                     FeedBack fed = new FeedBack(part[0], LocalDate.parse(part[1]));
                     feedBacks.add(fed);
                 }

             }
         } catch (IOException e){
             System.err.println("Error in writing feedback"+e.getMessage());
         }
         return feedBacks;
     }


}
