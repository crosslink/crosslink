package crosslink.utility;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author Darren HUANG
 */
public class InteractiveRenderer extends DefaultTableCellRenderer {


    protected int interactiveColumn;
    public InteractiveRenderer(int interactiveColumn) {
        this.interactiveColumn = interactiveColumn;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        return c;
    }

}