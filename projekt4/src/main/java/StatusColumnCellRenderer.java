import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class StatusColumnCellRenderer extends DefaultTableCellRenderer {

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        String cellValue = table.getValueAt(row, col).toString();
        if (!cellValue.isEmpty()) {
            if(col == 7 & !isNumeric(cellValue) & !cellValue.equals("brak danych")){
                l.setBackground(Color.RED);
            } else if (col == 11 & !cellValue.equals("SSD") & !cellValue.equals("HDD") & !cellValue.equals("brak danych")){
                l.setBackground(Color.RED);
            }
            else {
                l.setBackground(Color.WHITE);
            }

        } else {
            l.setBackground(Color.RED);
        }
        return l;
    }
}