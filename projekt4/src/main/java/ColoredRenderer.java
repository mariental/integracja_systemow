import javax.swing.*;

import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;

public class ColoredRenderer extends DefaultTableCellRenderer {

    private boolean isColored;
    private ArrayList<Integer> rowsNumbers;

    public ColoredRenderer(boolean isColored, ArrayList<Integer> rowsNumbers) {
        this.isColored = isColored;
        this.rowsNumbers = rowsNumbers;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c =  super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if(isColored & rowsNumbers.contains(row)) {
            c.setBackground(Color.RED);
        } else {
            c.setBackground(Color.WHITE);
        }
        return c;
    }
}
