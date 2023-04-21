import javax.swing.*;

import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;

public class RowRenderer extends DefaultTableCellRenderer {

    private boolean isColored;
    private ArrayList<Integer> duplicatesRowsNumbers;
    private ArrayList<Integer> editedRowsNumbers;

    public RowRenderer(boolean isColored, ArrayList<Integer> duplicatesRowsNumbers, ArrayList<Integer> editedRowsNumbers) {
        this.isColored = isColored;
        this.duplicatesRowsNumbers = duplicatesRowsNumbers;
        this.editedRowsNumbers = editedRowsNumbers;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c =  super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if(isColored & duplicatesRowsNumbers.contains(row)) {
            c.setBackground(Color.RED);
        } else if (isColored & editedRowsNumbers.contains(row)) {
            c.setBackground(Color.WHITE);
        }
        else {
            c.setBackground(Color.GRAY);
        }
        return c;
    }
}
