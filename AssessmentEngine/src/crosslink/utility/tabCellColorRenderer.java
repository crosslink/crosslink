package crosslink.utility;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author Darren HUANG
 */
public class tabCellColorRenderer extends DefaultTableCellRenderer {

    static void log(Object content) {
        System.out.println(content);
    }

    private Color foreColor = Color.BLACK;
    private Color backColor = Color.WHITE;
    private Color currSelectedROW = Color.YELLOW;
    private Color IRRelROW = Color.PINK;
    private Color RelROW = Color.GREEN;
    private int[] selectedRow = null;
    private int[] selectedColumn = null;
    private boolean isTableClick = false;
    private JTable tabTable;

    public tabCellColorRenderer(JTable tabTable, boolean isTableClick) {
        this.isTableClick = isTableClick;
        this.tabTable = tabTable;
    }

    public void tabIndices(int rowIndex, int columnIndex, boolean isTableClick) {
        selectedRow = new int[]{rowIndex};
        selectedColumn = new int[]{0, 1, 2, 3};
        this.isTableClick = isTableClick;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
        Component c = super.getTableCellRendererComponent(table, value,
                isSelected, hasFocus,
                row, column);

        if (isTableClick) {
            if (isSelected) {
                c.setBackground(currSelectedROW);
            } else {
                c.setBackground(backColor);
            }
        } else {
            if (row == selectedRow[0]){
                c.setBackground(currSelectedROW);
            }
        }
        return c;
    }
}
