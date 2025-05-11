package Database;


import services.VideoConsultation;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class VideoConsultationDatabase {
    private static final String FILE_PATH = "VideoConsultationFile/data.dat";

    public static void saveConsultation(List<VideoConsultation> consultations) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(FILE_PATH))) {
            oos.writeObject(consultations);
        } catch (IOException e) {
            System.err.println("Error saving consultations: " + e.getMessage());
        }
    }

    public static List<VideoConsultation> loadConsultations() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(FILE_PATH))) {
            return (List<VideoConsultation>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading consultations: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}