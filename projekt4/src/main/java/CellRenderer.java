import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;

public class CellRenderer extends DefaultTableCellRenderer {

    private static int editedRow;
    private static ArrayList<Integer> duplicatesRowsNumbers;
    private static ArrayList<Integer> editedRowsNumbers;

    public CellRenderer (int editedRow, ArrayList<Integer> duplicatesRowsNumbers, ArrayList<Integer> editedRowsNumbers) {
        this.editedRow = editedRow;
        this.duplicatesRowsNumbers = duplicatesRowsNumbers;
        this.editedRowsNumbers = editedRowsNumbers;
    }

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
        if(row == editedRow) {
            String cellValue = table.getModel().getValueAt(editedRow, col).toString();
            if (!cellValue.isEmpty()) {
                if(col == 7 & !isNumeric(cellValue) & !cellValue.equals("brak danych")){
                    l.setBackground(Color.BLUE);
                } else if (col == 11 & !cellValue.equals("SSD") & !cellValue.equals("HDD") & !cellValue.equals("brak danych")){
                    l.setBackground(Color.BLUE);
                }
                else {
                    if(duplicatesRowsNumbers.contains(row)){
                        l.setBackground(Color.RED);
                    } else if (editedRowsNumbers.contains(row)) {
                        l.setBackground(Color.WHITE);
                    } else {
                        l.setBackground(Color.GRAY);
                    }
                }
            } else {
                l.setBackground(Color.BLUE);
            }
        } else if(duplicatesRowsNumbers.contains(row)){
            l.setBackground(Color.RED);
        } else if (editedRowsNumbers.contains(row)) {
            l.setBackground(Color.WHITE);
        } else {
            l.setBackground(Color.GRAY);
        }
        return l;
    }
}