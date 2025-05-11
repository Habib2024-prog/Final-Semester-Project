package services;

import Model.Patients;
import utill.Vitals;

import java.util.List;

public class MedicationEffectiveness {

    public static boolean isPatientImproving(Patients patient) {
        List<Vitals> vitals = patient.getVitalsList();
        if (vitals.size() < 2) {
            return false; // Not enough data to compare
        }

        Vitals previous = vitals.get(vitals.size() - 2); // second last
        Vitals latest = vitals.get(vitals.size() - 1);    // most recent
         // High Heart rate means is not improving in health
        boolean heartRateImproving = latest.getHeartRate() <= previous.getHeartRate();
        boolean oxygenImproving = latest.getOxygenLevel() >= previous.getOxygenLevel();
        //patient person normally has high temperature if reduced means improving
        boolean temperatureImproving = latest.getTemperature() <= previous.getTemperature();
         // normally reducing from high BP means improving health
        int previousBP = getSystolic(previous.getBloodPressure());
        int latestBP = getSystolic(latest.getBloodPressure());
        boolean bpImproving = (latestBP <= previousBP);

        // If majority vitals are better ➔ improving
        int improvements = 0;
        if (heartRateImproving) improvements++;
        if (oxygenImproving) improvements++;
        if (temperatureImproving) improvements++;
        if (bpImproving) improvements++;

        return improvements >= 3; // at least 3 vitals must improve
    }

    private static int getSystolic(String bloodPressure) {
        try {
            String[] parts = bloodPressure.split("/");
            return Integer.parseInt(parts[0]);
        } catch (Exception e) {
            return 999; // treat invalid BP as very high
        }
    }


}
