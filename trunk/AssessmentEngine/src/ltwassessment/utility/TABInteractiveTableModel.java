package ltwassessment.utility;

import java.util.Vector;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import ltwassessment.utility.TABRecord;

/**
 * @author Darren HUANG
 */
public class TABInteractiveTableModel extends DefaultTableModel {

    public static final int TOPIC_INDEX = 0;
    public static final int ANCHOR_INDEX = 1;
    public static final int SUBANCHOR_INDEX = 2;
    public static final int BEP_INDEX = 3;
    public static final int HIDDEN_INDEX = 4;
    protected String[] columnNames;
    protected Vector dataVector;

    public TABInteractiveTableModel(String[] columnNames) {
        this.columnNames = columnNames;
        this.dataVector = new Vector();
    }

    @Override
    public String getColumnName(int colIndex) {
        return columnNames[colIndex];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        if (column == HIDDEN_INDEX) {
            return false;
        } else {
            return true;
        }
    }

    public Class getCloumnClass(int colIndex) {
        switch (colIndex) {
            case TOPIC_INDEX:
            case ANCHOR_INDEX:
            case SUBANCHOR_INDEX:
            case BEP_INDEX:
                return String.class;
            default:
                return Object.class;
        }
    }

    public int getRowCount() {
        return dataVector == null ? 0 : dataVector.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Object getValueAt(int row, int column) {
        TABRecord myRecord = (TABRecord) dataVector.get(row);
        switch (column) {
            case TOPIC_INDEX:
                return myRecord.getTopic();
            case ANCHOR_INDEX:
                return myRecord.getAnchor();
            case SUBANCHOR_INDEX:
                return myRecord.getSubAnchor();
            case BEP_INDEX:
                return myRecord.getBEP();
            case HIDDEN_INDEX:
                return myRecord.getHiddenFieldValue();
            default:
                return new Object();
        }
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        TABRecord myRecord = (TABRecord) dataVector.get(row);
        switch (column) {
            case TOPIC_INDEX:
                myRecord.setTopic((String) value);
                break;
            case ANCHOR_INDEX:
                myRecord.setAnchor((String) value);
                break;
            case SUBANCHOR_INDEX:
                myRecord.setSubAnchor((String) value);
                break;
            case BEP_INDEX:
                myRecord.setBEP((String) value);
                break;
            case HIDDEN_INDEX:
                myRecord.setHiddenFieldValue((String) value);
                break;
            default:
                System.out.println("Invalid Index");
        }
        fireTableCellUpdated(row, column);
    }

    public boolean hasEmptyRow() {
        if (dataVector.size() == 0) {
            return false;
        }
        TABRecord myRecord = (TABRecord) dataVector.get(dataVector.size() - 1);
        if (myRecord.getTopic().trim().equals("") &&
                myRecord.getAnchor().trim().equals("") &&
                myRecord.getSubAnchor().trim().equals("") &&
                myRecord.getBEP().trim().equals("")) {
            return true;
        } else {
            return false;
        }
    }

    public void addEmptyRow() {
        dataVector.add(new TABRecord());
        fireTableCellUpdated(dataVector.size() - 1, dataVector.size() - 1);
    }
}
