package services;

import java.io.Serializable;
import java.time.*;
import java.util.UUID;

public class VideoConsultation implements Serializable {
    private String consultationId;
    private String patientId;
    private String doctorId;
    private LocalDateTime requestDate;
    private LocalDateTime preferredTime;
    private String platform;
    private String link;
    private boolean approved;
    private LocalDateTime approvalTime;

    // Constructor
    public VideoConsultation(String patientId, String doctorId,
                             LocalDateTime requestDate, LocalDateTime preferredTime) {
        this.consultationId = UUID.randomUUID().toString().substring(0,4);
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.requestDate = requestDate;
        this.preferredTime = preferredTime;
        this.approvalTime=requestDate;
        this.approved = false;
    }

    // Getters and Setters
    public String getConsultationId() { return consultationId; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public LocalDateTime getRequestDate() { return requestDate; }
    public LocalDateTime getPreferredTime() { return preferredTime; }
    public String getPlatform() { return platform; }
    public String getLink() { return link; }
    public boolean isApproved() { return approved; }
    public LocalDateTime getApprovalTime() { return approvalTime; }

    public void setPlatform(String platform) { this.platform = platform; }
    public void setLink(String link) { this.link = link; }
    public void setApproved(boolean approved) { this.approved = approved; }
    public void setApprovalTime(LocalDateTime approvalTime) {
        this.approvalTime = approvalTime;
    }
}