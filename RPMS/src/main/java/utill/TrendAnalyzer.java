package utill;

import Model.Patients;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class TrendAnalyzer {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm");

    public static ChartPanel createHeartRateChart(Patients patient) {
        return createChartPanel(
                createHeartRateDataset(patient),
                "Heart Rate Trend",
                "Heart Rate (bpm)",
                Color.RED
        );
    }

    public static ChartPanel createBloodPressureChart(Patients patient) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();

        TimeSeries systolicSeries = new TimeSeries("Systolic");
        TimeSeries diastolicSeries = new TimeSeries("Diastolic");

        for (Vitals v : patient.getVitalsList()) {
            String[] bp = v.getBloodPressure().split("/");
            if (bp.length == 2) {
                try {
                    Second time = new Second(toDate(v.getTimestamp()));
                    systolicSeries.add(time, Double.parseDouble(bp[0].trim()));
                    diastolicSeries.add(time, Double.parseDouble(bp[1].trim()));
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing BP values: " + v.getBloodPressure());
                }
            }
        }

        dataset.addSeries(systolicSeries);
        dataset.addSeries(diastolicSeries);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Blood Pressure Trend",
                "Time",
                "mmHg",
                dataset,
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();
        customizePlot(plot);
        plot.getRenderer().setSeriesPaint(0, Color.RED);    // Systolic
        plot.getRenderer().setSeriesPaint(1, Color.BLUE);   // Diastolic

        return createChartPanel(chart);
    }

    public static ChartPanel createOxygenChart(Patients patient) {
        return createChartPanel(
                createOxygenDataset(patient),
                "Oxygen Saturation Trend",
                "Oxygen Level (%)",
                Color.GREEN
        );
    }

    public static ChartPanel createTemperatureChart(Patients patient) {
        return createChartPanel(
                createTemperatureDataset(patient),
                "Body Temperature Trend",
                "Temperature (°C)",
                Color.ORANGE
        );
    }

    private static XYDataset createHeartRateDataset(Patients patient) {
        TimeSeries series = new TimeSeries("Heart Rate");
        for (Vitals v : patient.getVitalsList()) {
            series.add(new Second(toDate(v.getTimestamp())), v.getHeartRate());
        }
        return new TimeSeriesCollection(series);
    }

    private static XYDataset createOxygenDataset(Patients patient) {
        TimeSeries series = new TimeSeries("Oxygen Level");
        for (Vitals v : patient.getVitalsList()) {
            series.add(new Second(toDate(v.getTimestamp())), v.getOxygenLevel());
        }
        return new TimeSeriesCollection(series);
    }

    private static XYDataset createTemperatureDataset(Patients patient) {
        TimeSeries series = new TimeSeries("Temperature");
        for (Vitals v : patient.getVitalsList()) {
            series.add(new Second(toDate(v.getTimestamp())), v.getTemperature());
        }
        return new TimeSeriesCollection(series);
    }

    private static ChartPanel createChartPanel(XYDataset dataset, String title, String yLabel, Color color) {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title,
                "Time",
                yLabel,
                dataset,
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();
        customizePlot(plot);
        plot.getRenderer().setSeriesPaint(0, color);

        return createChartPanel(chart);
    }

    private static ChartPanel createChartPanel(JFreeChart chart) {
        DateAxis axis = (DateAxis) chart.getXYPlot().getDomainAxis();
        axis.setDateFormatOverride(dateFormat);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 400));
        return chartPanel;
    }

    private static void customizePlot(XYPlot plot) {
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
    }

    private static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}