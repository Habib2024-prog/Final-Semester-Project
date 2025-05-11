package utill;

import javax.sound.sampled.Line;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Vitals {
    static DateTimeFormatter formatter=DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private double heartRate;
    private  double temperature;
    private  double oxygenLevel;
    private  String bloodPressure;
    private LocalDateTime timestamp;

    // primary constructor
    public Vitals(double heartRate,double temperature,
                  double oxygenLevel,String bloodPressure) throws VitalException {
        this(heartRate,temperature,oxygenLevel,bloodPressure,LocalDateTime.now());

    }
    //constructor for CSV line
    public Vitals(double heartRate,double temperature,
                  double oxygenLevel,String bloodPressure, LocalDateTime timestamp) throws VitalException {
        ValidateVitals.ValidateVitalsSign(heartRate,oxygenLevel,temperature,bloodPressure);
        this.bloodPressure=bloodPressure;
        this.oxygenLevel=oxygenLevel;
        this.heartRate= heartRate;
        this.timestamp=timestamp;
        this.temperature=temperature;

    }
    //getter
    public double getHeartRate(){return  heartRate;}
    public double getTemperature(){return  temperature;}
    public double getOxygenLevel(){return  oxygenLevel;}
    public String getBloodPressure(){return  bloodPressure;}
    public LocalDateTime getTimestamp(){return  timestamp;}
   // setter
    public  void setHeartRate(double heartRate){
        this.heartRate=heartRate;
    }
    public  void setTemperature(double temperature){
        this.temperature= temperature;
    }
    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setOxygenLevel(double oxygenLevel) {
        this.oxygenLevel = oxygenLevel;
    }

   // method to make line in the formate of CSV file
    public String toCSVLine(){
        return String.format("%s,%.1f,%.1f,%.1f,%s",
                timestamp.format(formatter),heartRate,oxygenLevel,temperature,bloodPressure);

    }
    // method return vitals from a CSV file
  public static Vitals fromCSVLine(String line) throws VitalException {
        String[]colunm= line.split(",");
        LocalDateTime timeStamp=LocalDateTime.parse(colunm[0],formatter);
        double heartRate=Double.parseDouble(colunm[1]);
        double oxygenLevel=Double.parseDouble(colunm[2]);
        double temperature=Double.parseDouble(colunm[3]);
        String bloodPressure=colunm[4];
        return new Vitals(heartRate,temperature,oxygenLevel,bloodPressure,timeStamp);
  }
  @Override
  public String toString(){
        return String.format("HearRate: %.1f\nBloodPressure: %s\nTemperature: %.1f\nOxygenLevel: %.1f\nTimeStamp: %s",
        heartRate, bloodPressure, temperature,oxygenLevel,timestamp.format(formatter));
  }





















}
