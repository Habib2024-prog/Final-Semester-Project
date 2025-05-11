package utill;

public class Prescriptions {

    private String medicationName;
    private String dosage;
    private String schedule;
    //Constructor
    public Prescriptions(String medicationName, String dosage, String schedule) {
         this.medicationName=medicationName;
         this.dosage=dosage;
         this.schedule=schedule;
    }
    //getter
    public String getMedicationName(){return  medicationName;}
    public String getDosage(){return  dosage;}
    public String getSchedule(){return schedule;}
    public void  setMedicationName(String medicationName){
        this.medicationName=medicationName;
    }
    public void setDosage(String dosage){
        this.dosage= dosage;
    }
    public void setSchedule(String schedule){
        this.schedule=schedule;
    }
    @Override
    public String toString(){
        return String.format("MedicationName: %s\nDosage: %s\nSchedule: %s",medicationName,dosage,schedule);
    }


}