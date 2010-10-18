package ltwassessmenttool.listener;

import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.event.MouseInputListener;

import ltwassessment.parsers.resourcesManager;
import ltwassessment.utility.AttributiveCellRenderer;
import ltwassessment.utility.paneTableIndexing;
import ltwassessment.utility.tabTxtPaneManager;
import ltwassessment.utility.tbaTxtPaneManager;

/**
 * @author Darren HUANG
 */
public class linkPaneMouseListener implements MouseInputListener {
    // Declare global variables
    private final String sysPropertyKey = "isTABKey";
    private boolean isTAB = false;
    private JTextPane myTopicPane;
    private JTextPane myLinkPane;
    private JTable myPaneTable;
    private Vector<String> topicIDsSA;
    // import External Classes
    private resourcesManager myRSCManager;
    private tabTxtPaneManager myTABTxtPaneManager;
    private tbaTxtPaneManager myTBATxtPaneManager;
    private paneTableIndexing myPaneTableIndexing;
    // Declare TAB Index Variables
    private int currRowIndex = 0;
    private int currTopicIndex = 0;
    private int currPoolAnchorIndex = 0;
    private int currBepLinkIndex = 0;
    // Declare TBA Index Variables
    private int currBepIndex = 0;
    private int currAnchorLinkIndex = 0;

    private void log(String txt) {
        System.out.println(txt);
    }

    public linkPaneMouseListener(JTextPane myTopicPane, JTextPane myLinkPane, JTable paneTable) {
        this.myTopicPane = myTopicPane;
        this.myLinkPane = myLinkPane;
        this.myPaneTable = paneTable;
        
        myRSCManager = new resourcesManager();
        myTABTxtPaneManager = new tabTxtPaneManager();
        myTBATxtPaneManager = new tbaTxtPaneManager();

        // Get Current TopicID Index
        this.topicIDsSA = myRSCManager.getTopicIDsV();
        Collections.sort(topicIDsSA);
    }

    // <editor-fold defaultstate="collapsed" desc="Right/Left Mouse Events: Next/Previous">
    private void activateMouseClickEvent(MouseEvent mce) {
        // 1) Set Current Status
        Hashtable<String, String[]> myRowNAVIndices = myPaneTableIndexing.getRowNAVIndices();
        // ---------------------------------------------------------------------
        // 2) TAB Update Level
        int tableUpdateLevel = 0;
        if (mce.getButton() == MouseEvent.BUTTON1) {
            // Left-Click --> Next
            // <editor-fold defaultstate="collapsed" desc="Left Click Mouse Events: Next">
            if (isTAB) {
                // <editor-fold defaultstate="collapsed" desc="For Outgoing TAB">
                String[] lastTABRowIndices = myRSCManager.getTABNavigationIndex();
                int lastTABRowNumber = Integer.valueOf(lastTABRowIndices[0]);
                String[] lastTABIndices = lastTABRowIndices[1].split(" , ");
                // -------------------------------------------------------------
                String[] thisTABIndices = new String[4];
                if (lastTABRowNumber < myRowNAVIndices.size() - 1) {
                    currRowIndex = lastTABRowNumber + 1;
                    thisTABIndices = myRowNAVIndices.get(String.valueOf(currRowIndex));
                    if (thisTABIndices[0].equals(lastTABIndices[0]) &&
                            thisTABIndices[1].equals(lastTABIndices[1]) &&
                            thisTABIndices[3].equals(lastTABIndices[3])) {
                        // Click on the Same Row: DONOTHING
                    } else if (thisTABIndices[0].equals(lastTABIndices[0]) &&
                            thisTABIndices[1].equals(lastTABIndices[1])) {
                        tableUpdateLevel = 3;
                    } else if (thisTABIndices[0].equals(lastTABIndices[0])) {
                        tableUpdateLevel = 2;
                    } else {
                        tableUpdateLevel = 1;
                    }
                } else if (lastTABRowNumber == myRowNAVIndices.size() - 1) {
                    currRowIndex = 0;
                    thisTABIndices = myRowNAVIndices.get(String.valueOf(currRowIndex));
                    tableUpdateLevel = 1;
                }
                currTopicIndex = Integer.valueOf(thisTABIndices[0]);
                currPoolAnchorIndex = Integer.valueOf(thisTABIndices[1]);
                currBepLinkIndex = Integer.valueOf(thisTABIndices[3]);
                updateToolPanesByIndices(tableUpdateLevel);
            // </editor-fold>
            } else {
                // <editor-fold defaultstate="collapsed" desc="For Incoming TBA">
                String[] lastTBARowIndices = myRSCManager.getTBANavigationIndex();
                int lastTBARowNumber = Integer.valueOf(lastTBARowIndices[0]);
                String[] lastTBAIndices = lastTBARowIndices[1].split(" , ");
                // -------------------------------------------------------------
                String[] thisTBAIndices = new String[4];
                if (lastTBARowNumber < myRowNAVIndices.size() - 1) {
                    currRowIndex = lastTBARowNumber + 1;
                    thisTBAIndices = myRowNAVIndices.get(String.valueOf(currRowIndex));
                    if (thisTBAIndices[0].equals(lastTBAIndices[0]) &&
                            thisTBAIndices[1].equals(lastTBAIndices[1]) &&
                            thisTBAIndices[3].equals(lastTBAIndices[3])) {
                        // Click on the Same Row: DONOTHING
                    } else if (thisTBAIndices[0].equals(lastTBAIndices[0]) &&
                            thisTBAIndices[1].equals(lastTBAIndices[1])) {
                        tableUpdateLevel = 3;
                    } else if (thisTBAIndices[0].equals(lastTBAIndices[0])) {
                        tableUpdateLevel = 2;
                    } else {
                        tableUpdateLevel = 1;
                    }
                } else if (lastTBARowNumber == myRowNAVIndices.size() - 1) {
                    currRowIndex = 0;
                    thisTBAIndices = myRowNAVIndices.get(String.valueOf(currRowIndex));
                    tableUpdateLevel = 1;
                }
                currTopicIndex = Integer.valueOf(thisTBAIndices[0]);
                currBepIndex = Integer.valueOf(thisTBAIndices[1]);
                currAnchorLinkIndex = Integer.valueOf(thisTBAIndices[3]);
                log(thisTBAIndices[0] + " : " + thisTBAIndices[1] + " : " + thisTBAIndices[3]);
                log("tableUpdateLevel: " + tableUpdateLevel);
                updateToolPanesByIndices(tableUpdateLevel);
            // </editor-fold>
            }
        // </editor-fold>
        } else if (mce.getButton() == MouseEvent.BUTTON3) {
            // <editor-fold defaultstate="collapsed" desc="Right Click Mouse Events: Previous">
            if (isTAB) {
                // <editor-fold defaultstate="collapsed" desc="For Outgoing TAB">
                String[] lastTABRowIndices = myRSCManager.getTABNavigationIndex();
                int lastTABRowNumber = Integer.valueOf(lastTABRowIndices[0]);
                String[] lastTABIndices = lastTABRowIndices[1].split(" , ");
                // -------------------------------------------------------------
                String[] thisTABIndices = new String[4];
                if (lastTABRowNumber > 0) {
                    currRowIndex = lastTABRowNumber - 1;
                    thisTABIndices = myRowNAVIndices.get(String.valueOf(currRowIndex));
                    if (thisTABIndices[0].equals(lastTABIndices[0]) &&
                            thisTABIndices[1].equals(lastTABIndices[1]) &&
                            thisTABIndices[3].equals(lastTABIndices[3])) {
                        // Click on the Same Row: DONOTHING
                    } else if (thisTABIndices[0].equals(lastTABIndices[0]) &&
                            thisTABIndices[1].equals(lastTABIndices[1])) {
                        tableUpdateLevel = 3;
                    } else if (thisTABIndices[0].equals(lastTABIndices[0])) {
                        tableUpdateLevel = 2;
                    } else {
                        tableUpdateLevel = 1;
                    }
                } else if (lastTABRowNumber == 0) {
                    currRowIndex = myRowNAVIndices.size() - 1;
                    thisTABIndices = myRowNAVIndices.get(String.valueOf(currRowIndex));
                    tableUpdateLevel = 1;
                }
                currTopicIndex = Integer.valueOf(thisTABIndices[0]);
                currPoolAnchorIndex = Integer.valueOf(thisTABIndices[1]);
                currBepLinkIndex = Integer.valueOf(thisTABIndices[3]);
                updateToolPanesByIndices(tableUpdateLevel);
            // </editor-fold>
            } else {
                // <editor-fold defaultstate="collapsed" desc="For Incoming TBA">
                String[] lastTBARowIndices = myRSCManager.getTBANavigationIndex();
                int lastTBARowNumber = Integer.valueOf(lastTBARowIndices[0]);
                String[] lastTBAIndices = lastTBARowIndices[1].split(" , ");
                // -------------------------------------------------------------
                String[] thisTBAIndices = new String[4];
                if (lastTBARowNumber > 0) {
                    currRowIndex = lastTBARowNumber - 1;
                    thisTBAIndices = myRowNAVIndices.get(String.valueOf(currRowIndex));
                    if (thisTBAIndices[0].equals(lastTBAIndices[0]) &&
                            thisTBAIndices[1].equals(lastTBAIndices[1]) &&
                            thisTBAIndices[3].equals(lastTBAIndices[3])) {
                        // Click on the Same Row: DONOTHING
                    } else if (thisTBAIndices[0].equals(lastTBAIndices[0]) &&
                            thisTBAIndices[1].equals(lastTBAIndices[1])) {
                        tableUpdateLevel = 3;
                    } else if (thisTBAIndices[0].equals(lastTBAIndices[0])) {
                        tableUpdateLevel = 2;
                    } else {
                        tableUpdateLevel = 1;
                    }
                } else if (lastTBARowNumber == 0) {
                    currRowIndex = myRowNAVIndices.size() - 1;
                    thisTBAIndices = myRowNAVIndices.get(String.valueOf(currRowIndex));
                    tableUpdateLevel = 1;
                }
                currTopicIndex = Integer.valueOf(thisTBAIndices[0]);
                currBepIndex = Integer.valueOf(thisTBAIndices[1]);
                currAnchorLinkIndex = Integer.valueOf(thisTBAIndices[3]);
                updateToolPanesByIndices(tableUpdateLevel);
            // </editor-fold>
            }
        // </editor-fold>
        }
    }

    private void updateToolPanesByIndices(int paneUpdateLevel) {
        // 1) Update Topic & Link Txt Panes
        // 2) Highlight paneTable ROW: 1st BEP link of the Selected Anchor
        if (paneUpdateLevel > 0 && paneUpdateLevel <= 3) {
            if (isTAB) {
                String[] thisRowSA = this.myPaneTableIndexing.rowTABSAByIndices(currTopicIndex, currPoolAnchorIndex, currBepLinkIndex);
                this.myTABTxtPaneManager.updateTXTPanes(this.myTopicPane, this.myLinkPane, this.myPaneTable, thisRowSA, paneUpdateLevel);
                String topicID = thisRowSA[0];
                String[] anchorXmlOL = thisRowSA[1].split("_");
                String bepOffset = thisRowSA[3].split("_")[0];
                String bepLinkID = thisRowSA[3].split("_")[1];
                this.myPaneTable.setDefaultRenderer(Object.class, new AttributiveCellRenderer(topicID, anchorXmlOL, bepOffset, bepLinkID, isTAB));
                this.myPaneTable.repaint();
            } else {
                String[] thisRowSA = this.myPaneTableIndexing.rowTBASAByIndices(currTopicIndex, currBepIndex, currAnchorLinkIndex);
                this.myTBATxtPaneManager.updateTXTPanes(this.myTopicPane, this.myLinkPane, this.myPaneTable, thisRowSA, paneUpdateLevel);
                String topicID = thisRowSA[0];
                String bepOffset = thisRowSA[1];
                String[] anchorXmlOL = thisRowSA[2].split("_");
                String anchorLinkID = thisRowSA[3];
                this.myPaneTable.setDefaultRenderer(Object.class, new AttributiveCellRenderer(topicID, anchorXmlOL, bepOffset, anchorLinkID, isTAB));
                this.myPaneTable.repaint();
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Mouse Events">
    public void mouseClicked(MouseEvent me) {
        /**
         * CASE 1: Left-Click -> NEXT Link ; Right-Click: PREVIOUS Link
         * CASE 2: Left-Click -> RELEVANT + NEXT ; Right-Click: IRRELEVANT + NEXT
         * CASE 3: Double-Left-Click -> Change BEP Position
         */
        this.isTAB = Boolean.valueOf(System.getProperty(sysPropertyKey));
        this.myPaneTableIndexing = new paneTableIndexing(this.isTAB);
        activateMouseClickEvent(me);
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }
    // </editor-fold>
}
