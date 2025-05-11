package Chat;

import javax.swing.*;
import java.awt.*;

public class DoctorListRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof String) {
            // Value is in format "doctorId|doctorName"
            String[] parts = ((String)value).split("\\|");
            if (parts.length >= 2) {
                setText("Dr. " + parts[1]); // Display as "Dr. Smith"
            }
        }
        return this;
    }
}