package services;

import Model.Patients;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import utill.Vitals;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.jfree.chart.ChartUtils;
import utill.FeedBack;
import utill.Prescriptions;
import utill.TrendAnalyzer;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportGenerator {
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.DARK_GRAY);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public static void generatePatientReport(Patients patient) {
        try {
            // Create report directory
            Path reportDir = Path.of("Report/patient" + patient.getUserId());
            Files.createDirectories(reportDir);

            // PDF file path
            String pdfPath = reportDir + "/patient_report_" + System.currentTimeMillis() + ".pdf";

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfPath));
            document.open();

            addTitlePage(document, patient);
            addPatientInfo(document, patient);
            addVitalSigns(document, patient);
            addTrendCharts(document, patient); // New trend charts section
            addFeedback(document, patient);
            addPrescriptions(document, patient);
            addMedicationAnalysis(document, patient);
            addFooter(document);

            document.close();
            System.out.println("Report generated: " + pdfPath);

            // Open the PDF automatically (optional)
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(pdfPath));
            }
        } catch (Exception e) {
            System.err.println("Error generating report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addTitlePage(Document document, Patients patient) throws DocumentException {
        Paragraph title = new Paragraph("PATIENT HEALTH REPORT\n\n", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph(
                "Patient: " + patient.getName() + "\n" +
                        "Report Date: " + LocalDateTime.now().format(DATE_FORMATTER) + "\n\n",
                NORMAL_FONT
        );
        subtitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitle);

        document.add(new Chunk("\n"));
    }

    private static void addPatientInfo(Document document, Patients patient) throws DocumentException {
        document.add(new Paragraph("PATIENT INFORMATION", SECTION_FONT));
        document.add(createInfoTable(
                "Patient Name", patient.getName(),
                "Patient ID", patient.getUserId(),
                "Age", String.valueOf(patient.getAge()),
                "Gender", patient.getGender(),
                "Contact", patient.getContactNumber(),
                "Email", patient.getEmail()
        ));
        document.add(new Chunk("\n"));
    }

    private static void addVitalSigns(Document document, Patients patient) throws DocumentException {
        document.add(new Paragraph("VITAL SIGNS HISTORY", SECTION_FONT));

        if (patient.getVitalsList().isEmpty()) {
            document.add(new Paragraph("No vital signs recorded.", NORMAL_FONT));
            return;
        }

        // Create table for vitals
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        // Table headers
        table.addCell(createHeaderCell("Date/Time"));
        table.addCell(createHeaderCell("Blood Pressure"));
        table.addCell(createHeaderCell("Heart Rate"));
        table.addCell(createHeaderCell("Oxygen Level"));
        table.addCell(createHeaderCell("Temperature"));

        // Table data
        for (Vitals v : patient.getVitalsList()) {
            table.addCell(createCell(v.getTimestamp().format(DATE_FORMATTER)));
            table.addCell(createCell(v.getBloodPressure()));
            table.addCell(createCell(String.valueOf(v.getHeartRate())));
            table.addCell(createCell(v.getOxygenLevel() + "%"));
            table.addCell(createCell(v.getTemperature() + "°C"));
        }

        document.add(table);
    }

    private static void addTrendCharts(Document document, Patients patient) throws Exception {
        document.add(new Paragraph("HEALTH TRENDS", SECTION_FONT));

        if (patient.getVitalsList().isEmpty()) {
            document.add(new Paragraph("No data available for trends", NORMAL_FONT));
            return;
        }

        // Create temporary directory for charts
        Path chartsDir = Path.of("temp_charts");
        Files.createDirectories(chartsDir);

        // Generate charts
        String hrChart = chartsDir + "/hr_chart.png";
        String bpChart = chartsDir + "/bp_chart.png";
        String oxChart = chartsDir + "/ox_chart.png";
        String tempChart = chartsDir + "/temp_chart.png";

        ChartUtils.saveChartAsPNG(new File(hrChart), TrendAnalyzer.createHeartRateChart(patient).getChart(), 600, 400);
        ChartUtils.saveChartAsPNG(new File(bpChart), TrendAnalyzer.createBloodPressureChart(patient).getChart(), 600, 400);
        ChartUtils.saveChartAsPNG(new File(oxChart), TrendAnalyzer.createOxygenChart(patient).getChart(), 600, 400);
        ChartUtils.saveChartAsPNG(new File(tempChart), TrendAnalyzer.createTemperatureChart(patient).getChart(), 600, 400);

        // Add charts to PDF
        document.add(new Paragraph("Heart Rate Trend:", NORMAL_FONT));
        document.add(createImage(hrChart));

        document.add(new Paragraph("Blood Pressure Trend:", NORMAL_FONT));
        document.add(createImage(bpChart));

        document.add(new Paragraph("Oxygen Level Trend:", NORMAL_FONT));
        document.add(createImage(oxChart));

        document.add(new Paragraph("Temperature Trend:", NORMAL_FONT));
        document.add(createImage(tempChart));

        // Clean up temporary files
        Files.deleteIfExists(Path.of(hrChart));
        Files.deleteIfExists(Path.of(bpChart));
        Files.deleteIfExists(Path.of(oxChart));
        Files.deleteIfExists(Path.of(tempChart));
        Files.deleteIfExists(chartsDir);
    }

    private static void addFeedback(Document document, Patients patient) throws DocumentException {
        document.add(new Paragraph("DOCTOR FEEDBACK", SECTION_FONT));

        if (patient.getDoctorFeedback().isEmpty()) {
            document.add(new Paragraph("No feedback recorded.", NORMAL_FONT));
            return;
        }

        for (FeedBack f : patient.getDoctorFeedback()) {
            Paragraph feedback = new Paragraph();
            feedback.add(new Chunk(f.getFeedBackDate() + ": ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            feedback.add(new Chunk(f.getFeedBackText(), NORMAL_FONT));
            document.add(feedback);
        }
        document.add(new Chunk("\n"));
    }

    private static void addPrescriptions(Document document, Patients patient) throws DocumentException {
        document.add(new Paragraph("PRESCRIPTION HISTORY", SECTION_FONT));

        if (patient.getPrescriptions().isEmpty()) {
            document.add(new Paragraph("No prescriptions issued.", NORMAL_FONT));
            return;
        }

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        // Headers
        table.addCell(createHeaderCell("Medication"));
        table.addCell(createHeaderCell("Dosage"));
        table.addCell(createHeaderCell("Schedule"));

        // Data
        for (Prescriptions p : patient.getPrescriptions()) {
            table.addCell(createCell(p.getMedicationName()));
            table.addCell(createCell(p.getDosage()));
            table.addCell(createCell(p.getSchedule()));
        }

        document.add(table);
    }

    private static void addMedicationAnalysis(Document document, Patients patient) throws DocumentException {
        document.add(new Paragraph("MEDICATION EFFECTIVENESS", SECTION_FONT));

        String analysis = MedicationEffectiveness.isPatientImproving(patient)
                ? "Patient vitals show improvement after recent treatment."
                : "No significant improvement detected in patient vitals.";

        document.add(new Paragraph(analysis, NORMAL_FONT));
    }

    private static void addFooter(Document document) throws DocumentException {
        document.add(new Paragraph("\n"));
        Paragraph footer = new Paragraph(
                "Report generated by Remote Patient Monitoring System\n" +
                        LocalDateTime.now().format(DATE_FORMATTER),
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY)
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    // Helper methods
    private static PdfPTable createInfoTable(String... labelsAndValues) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        for (int i = 0; i < labelsAndValues.length; i += 2) {
            table.addCell(createCell(labelsAndValues[i], true));
            table.addCell(createCell(labelsAndValues[i+1], false));
        }

        return table;
    }

    private static PdfPCell createCell(String text) {
        return createCell(text, false);
    }

    private static PdfPCell createCell(String text, boolean isLabel) {
        PdfPCell cell = new PdfPCell(new Phrase(text, isLabel ?
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12) : NORMAL_FONT));
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        return cell;
    }

    private static PdfPCell createHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE)));
        cell.setBackgroundColor(BaseColor.DARK_GRAY);
        cell.setPadding(5);
        return cell;
    }

    private static Image createImage(String path) throws Exception {
        Image img = Image.getInstance(path);
        img.scaleToFit(500, 300);
        img.setAlignment(Element.ALIGN_CENTER);
        return img;
    }
}