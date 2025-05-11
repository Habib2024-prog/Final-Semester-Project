package services;

import Model.Patients;
import utill.Vitals;

import java.util.List;

public class EmergencyAlertSystem {
    public static boolean checkForEmergency(Patients patients){

       List<Vitals> vitalsList=patients.getVitalsList();
       if(vitalsList.isEmpty()){return false;}
      // check the last vitals
        Vitals lastVitals=vitalsList.get(vitalsList.size()-1);
       return isVitalsCritical(lastVitals);

    }
    public static Boolean isVitalsCritical(Vitals vitals){

         boolean heartRate= vitals.getHeartRate()<50||vitals.getHeartRate()>120;
        boolean temperature= vitals.getTemperature()<35||vitals.getTemperature()>38.50;
        boolean OxygenLevel= vitals.getOxygenLevel()<90;
        boolean bloodPressure=false;
        try{
            String[]bp=vitals.getBloodPressure().split("/");
            // blood pressure
            int systolic=Integer.parseInt(bp[0]);
            bloodPressure=systolic<90||systolic>140;
        }catch (Exception e){
            bloodPressure=true;
        }
        return heartRate||temperature||OxygenLevel||bloodPressure;

    }











}
