package ltwassessment.utility;

import java.awt.Color;
import java.awt.Component;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import ltwassessment.parsers.resourcesManager;
import ltwassessmenttool.utility.ObservableSingleton;
import ltwassessmenttool.utility.paneTableIndexing;

/**
 * @author Darren HUANG
 */
public class AttributiveCellRenderer extends DefaultTableCellRenderer {

    private final String sysPropertyKey = "isTABKey";
    private Color backColor = Color.WHITE;
    private Color selectedFontColor = Color.BLUE;
    private Color foreColor = Color.BLACK;
    private Color currSelectedColor = Color.YELLOW;
    private Color IRRelColor = Color.PINK;
    private Color RelColor = Color.GREEN;
    private int currClickRow = 0;
    private int currClickColumn = 0;
    private String currTopicID = "";
    private String anchorXmlOffset = "";
    private String anchorXmlLength = "";
    private String bepXmlOffset = "";
    private String linkFileID = "";
    private boolean isTABTask = true;
    private boolean isTableClick = false;
    private boolean isTopicPaneOLClick = false;
    private resourcesManager myRSCManager;
    private paneTableIndexing myTableIndexing;
    private Hashtable<String, String[]> myIndicesByRowKey;
    private Vector<String> newTABFieldValues = null;
    private ObservableSingleton os = null;

    static void log(Object content) {
        System.out.println(content);
    }

    public AttributiveCellRenderer() {
    }

    public AttributiveCellRenderer(int currClickRow, int currClickColumn, boolean isTAB) {
        this.currClickRow = currClickRow;
        this.currClickColumn = currClickColumn;
        this.isTABTask = isTAB;
        this.isTableClick = true;

        this.myRSCManager = new resourcesManager();
        this.myTableIndexing = new paneTableIndexing(isTABTask);
        this.myIndicesByRowKey = this.myTableIndexing.getRowNAVIndices();

        this.newTABFieldValues = new Vector<String>();
        this.os = ObservableSingleton.getInstance();
    }

    public AttributiveCellRenderer(String currTopicID, String[] anchorXmlOL, String bepXmlOffset, String linkFileID, boolean isTAB) {
        this.currTopicID = currTopicID;
        this.anchorXmlOffset = anchorXmlOL[0];
        this.anchorXmlLength = anchorXmlOL[1];
        this.bepXmlOffset = bepXmlOffset;
        this.linkFileID = linkFileID;
        this.isTABTask = isTAB;
        this.isTopicPaneOLClick = true;

        this.myRSCManager = new resourcesManager();
        this.myTableIndexing = new paneTableIndexing(isTABTask);
        this.myIndicesByRowKey = this.myTableIndexing.getRowNAVIndices();

        this.newTABFieldValues = new Vector<String>();
        this.os = ObservableSingleton.getInstance();
    }
    private String thisTopicID = "";
    private String thisTopicTitle = "";
    private String thisAnchorOffset = "";
    private String thisAnchorLength = "";
    private String thisAnchorText = "";
    private String thisBepOffset = "";
    private String thisLinkID = "";
    private String thisLinkTitle = "";

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {


//        this.isTABTask = Boolean.valueOf(System.getProperty(sysPropertyKey));
//        log("isTABTask: " + isTABTask);
//        this.myTableIndexing = new paneTableIndexing(isTABTask);

        Component c = super.getTableCellRendererComponent(table, value,
                isSelected, hasFocus,
                row, column);
        if (isTABTask) {
            // TAB Outgoing
            // <editor-fold defaultstate="collapsed" desc="For Topic AnchorOL - Click -> BEP Links">
            if (column == 0) {
                thisTopicID = value.toString().split(" : ")[0].trim();
                thisTopicTitle = value.toString().split(" : ")[1].trim();
            } else if (column == 1) {
                String[] thisTABAnchorSet = value.toString().split(" : ");
                thisAnchorOffset = thisTABAnchorSet[0].trim().split("_")[0];
                thisAnchorLength = thisTABAnchorSet[0].trim().split("_")[1];
                thisAnchorText = thisTABAnchorSet[1].trim();
            } else if (column == 3) {
                String[] thisTABBepLinkSet = value.toString().split(" : ");
                thisBepOffset = thisTABBepLinkSet[0].split("_")[0];
                thisLinkID = thisTABBepLinkSet[0].split("_")[1];
                thisLinkTitle = thisTABBepLinkSet[1];
            }
            if (isTopicPaneOLClick) {
                if (thisTopicID.equals(currTopicID) && thisAnchorOffset.equals(anchorXmlOffset) && thisAnchorLength.equals(anchorXmlLength) && thisBepOffset.equals(bepXmlOffset) && thisLinkID.equals(linkFileID)) {
                    c.setBackground(currSelectedColor);
                    c.setForeground(selectedFontColor);
                    thisBepOffset = "";
                    thisLinkID = "";
                    // ---------------------------------------------------------
                    // Update TBA Navigation Indices
                    String[] thisIndicesSA = myIndicesByRowKey.get(String.valueOf(row));
                    String[] navIndices = new String[]{String.valueOf(row), thisIndicesSA[0], thisIndicesSA[1], thisIndicesSA[2], thisIndicesSA[3]};
                    myRSCManager.updateTABNavigationIndex(navIndices);
                    // ---------------------------------------------------------
                    newTABFieldValues.add(thisTopicTitle);
                    newTABFieldValues.add(thisTopicID);
                    newTABFieldValues.add(thisAnchorText);
                    newTABFieldValues.add(thisLinkTitle);
                    os.setTABFieldValues(newTABFieldValues);
                } else {
                    c.setBackground(backColor);
                    c.setForeground(foreColor);
                }
            } else if (isTableClick) {
                if (row == currClickRow && column == currClickColumn) {
                    c.setBackground(currSelectedColor);
                    c.setForeground(selectedFontColor);
                    // ---------------------------------------------------------
                    // Update TBA Navigation Indices
                    String[] thisIndicesSA = myIndicesByRowKey.get(String.valueOf(row));
                    String[] navIndices = new String[]{String.valueOf(row), thisIndicesSA[0], thisIndicesSA[1], thisIndicesSA[2], thisIndicesSA[3]};
                    myRSCManager.updateTABNavigationIndex(navIndices);
                } else {
                    c.setBackground(backColor);
                    c.setForeground(foreColor);
                }
                if (row == currClickRow && column == 3) {
                    newTABFieldValues.add(thisTopicTitle);
                    newTABFieldValues.add(thisTopicID);
                    newTABFieldValues.add(thisAnchorText);
                    newTABFieldValues.add(thisLinkTitle);
                    os.setTABFieldValues(newTABFieldValues);
                }
            }
        // </editor-fold>
        } else {
            // TBA Incoming
            // <editor-fold defaultstate="collapsed" desc="For Topic BEP - Click -> AnchorOL Links">
            if (column == 0) {
                thisTopicID = value.toString().split(" : ")[0].trim();
                thisTopicTitle = value.toString().split(" : ")[1].trim();
            } else if (column == 1) {
                thisBepOffset = value.toString();
            } else if (column == 2) {
                String[] thisTBAAnchorOLName = value.toString().split(" : ");
                thisAnchorOffset = thisTBAAnchorOLName[0].split("_")[0];
                thisAnchorLength = thisTBAAnchorOLName[0].split("_")[1];
                thisAnchorText = thisTBAAnchorOLName[1].trim();
            } else if (column == 3) {
                thisLinkID = value.toString().split(" : ")[0].trim();
                thisLinkTitle = value.toString().split(" : ")[1].trim();
            }
            if (isTopicPaneOLClick) {
                if (thisTopicID.equals(currTopicID) && thisAnchorOffset.equals(anchorXmlOffset) && thisAnchorLength.equals(anchorXmlLength) && thisBepOffset.equals(bepXmlOffset) && thisLinkID.equals(linkFileID)) {
                    c.setBackground(currSelectedColor);
                    c.setForeground(selectedFontColor);
                    thisAnchorOffset = "";
                    thisAnchorLength = "";
                    thisLinkID = "";
                    // ---------------------------------------------------------
                    // ---------------------------------------------------------
                    // Update TBA Navigation Indices
                    String[] thisIndicesSA = myIndicesByRowKey.get(String.valueOf(row));
                    String[] navIndices = new String[]{String.valueOf(row), thisIndicesSA[0], thisIndicesSA[1], thisIndicesSA[2], thisIndicesSA[3]};
                    myRSCManager.updateTBANavigationIndex(navIndices);
                    // ---------------------------------------------------------
                    newTABFieldValues.add(thisTopicTitle);
                    newTABFieldValues.add(thisTopicID);
                    newTABFieldValues.add(thisAnchorText);
                    newTABFieldValues.add(thisLinkTitle);
                    os.setTABFieldValues(newTABFieldValues);
                } else {
                    c.setBackground(backColor);
                    c.setForeground(foreColor);
                }
            } else if (isTableClick) {
                if (row == currClickRow && column == currClickColumn) {
                    c.setBackground(currSelectedColor);
                    c.setForeground(selectedFontColor);
                    // ---------------------------------------------------------
                    // Update TBA Navigation Indices
                    String[] thisIndicesSA = myIndicesByRowKey.get(String.valueOf(row));
                    String[] navIndices = new String[]{String.valueOf(row), thisIndicesSA[0], thisIndicesSA[1], thisIndicesSA[2], thisIndicesSA[3]};
                    myRSCManager.updateTBANavigationIndex(navIndices);
                } else {
                    c.setBackground(backColor);
                    c.setForeground(foreColor);
                }
                if (row == currClickRow && column == 3) {
                    newTABFieldValues.add(thisTopicTitle);
                    newTABFieldValues.add(thisTopicID);
                    newTABFieldValues.add(thisAnchorText);
                    newTABFieldValues.add(thisLinkTitle);
                    os.setTABFieldValues(newTABFieldValues);
                }
            }
        // </editor-fold>
        }
        return c;
    }
}
