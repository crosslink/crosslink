package ltwassessmenttool.listener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.JTextPane;

import ltwassessment.parsers.resourcesManager;
import ltwassessment.utility.AttributiveCellRenderer;
import ltwassessment.utility.paneTableIndexing;
import ltwassessment.utility.tbaTxtPaneManager;

/**
 * @author Darren HUANG
 */
public class tbaTableMouseListener extends MouseAdapter {

    private resourcesManager rscManager;
    private tbaTxtPaneManager txtPManager;
    private paneTableIndexing tbaTableIndexing;
    private JTextPane topicTxtPane;
    private JTextPane linkTxtPane;
    private JTable myTBATable;
    private Hashtable<String, Hashtable<String, Vector<String[]>>> poolIncomingData;
    private Hashtable<String, String[]> TBAIndice;
    private Hashtable<String, String[]> NAVIndice;
    private int tbaUpdateLevel = 0;
    private boolean isTAB = false;

    static void log(Object content) {
        System.out.println(content);
    }
    private boolean isTableClick = false;
    private String lastRowNu = "";
    private String lastRowTopicID = "";         // e.g. 25640
    private String lastRowBepOffset = "";             // e.g. 1794
    private String lastRowAnchorOL = "";        // e.g. 1794_8
    private String lastRowLinkID = "";          // e.g. 1296879
    private int currClickRow = 0;
    private int currClickColumn = 0;

    public tbaTableMouseListener(JTextPane topicTextPane, JTextPane linkTextPane, JTable tbaTable, Hashtable<String, Hashtable<String, Vector<String[]>>> poolIncomingData) {
        // assign Passed Variables
        this.isTableClick = false;
        this.topicTxtPane = topicTextPane;
        this.linkTxtPane = linkTextPane;
        this.myTBATable = tbaTable;
        this.poolIncomingData = poolIncomingData;
        // declare External Classes
        rscManager = new resourcesManager();
        txtPManager = new tbaTxtPaneManager();
        // indexing
        TBAIndice = new Hashtable<String, String[]>();
        indexTBAData(this.poolIncomingData);
        tbaTableIndexing = new paneTableIndexing(isTAB);
        NAVIndice = tbaTableIndexing.getRowNAVIndices();

        String[] lastTBANavSA = rscManager.getTBANavigationIndex();
        lastRowNu = lastTBANavSA[0];
        String[] firstRow = TBAIndice.get(lastRowNu);
        lastRowTopicID = firstRow[0];
        lastRowBepOffset = firstRow[1];
        lastRowAnchorOL = firstRow[2];
        lastRowLinkID = firstRow[3];
    }

    private void indexTBAData(Hashtable<String, Hashtable<String, Vector<String[]>>> poolIncomingData) {
        // Indexing Format:
        // TopicID(25640), thisBepOffset(1794), thisAnchorOL(1794_8), thisLinkID(1296879)
        int rowCounter = 0;
        String thisTopicID = "";
        String thisBepOffset = "";
        String thisAnchorOL = "";
        String thisLinkID = "";
        Vector<String> myTopicIDsV = new Vector<String>();
        Hashtable<String, Vector<String>> myTopicBepOffsetHT = new Hashtable<String, Vector<String>>();
        Vector<String> myBepOffsetV = new Vector<String>();
        Enumeration topicKeyEnu = poolIncomingData.keys();
        while (topicKeyEnu.hasMoreElements()) {
            // Get Topic ID
            Object topicKeyObj = topicKeyEnu.nextElement();
            if (!myTopicIDsV.contains(topicKeyObj.toString())) {
                myTopicIDsV.add(topicKeyObj.toString());
            }
            // Get BEP Offset
            myBepOffsetV = new Vector<String>();
            Hashtable<String, Vector<String[]>> bepAnchorFileIDH = poolIncomingData.get(topicKeyObj);
            Enumeration bepOffsetKeyEnu = bepAnchorFileIDH.keys();
            while (bepOffsetKeyEnu.hasMoreElements()) {
                Object bepOffsetObj = bepOffsetKeyEnu.nextElement();
                if (!myBepOffsetV.contains(bepOffsetObj.toString())) {
                    myBepOffsetV.add(bepOffsetObj.toString());
                }
            }
            Vector<String> mySortedBepOffsetV = sortVectorNumbers(myBepOffsetV);
            myTopicBepOffsetHT.put(topicKeyObj.toString(), mySortedBepOffsetV);
        }
        Collections.sort(myTopicIDsV);
        for (String tID : myTopicIDsV) {
            thisTopicID = tID;
            Hashtable<String, Vector<String[]>> bepHT = poolIncomingData.get(thisTopicID);
            Vector<String> BepOffsetV = myTopicBepOffsetHT.get(thisTopicID);
            for (String bO : BepOffsetV) {
                thisBepOffset = bO;
                Vector<String[]> anchorFileIDHT = bepHT.get(thisBepOffset);
                for (String[] thisAIDSA : anchorFileIDHT) {
                    // [0]:Offset, [1]:Length, [2]:Anchor_Name, [3]:File ID
                    thisAnchorOL = thisAIDSA[0] + "_" + thisAIDSA[1];
                    thisLinkID = thisAIDSA[3];
                    String[] tbaSA = new String[]{thisTopicID, thisBepOffset, thisAnchorOL, thisLinkID};
                    TBAIndice.put(String.valueOf(rowCounter), tbaSA);
                    rowCounter++;
                }
            }
        }
    }

    private void updateTBAPanes() {
        if (currClickRow == Integer.valueOf(lastRowNu)) {
        } else {
            // Check & Update Topic / Link Content Pane
            String[] thisRowSA = TBAIndice.get(String.valueOf(currClickRow));
            String thisTopicID = thisRowSA[0];
            if (!thisTopicID.equals(lastRowTopicID)) {
                // update Topic Pane & Anchor OL highlight
                //      & 1st Link/Pane & BEP Offset
                tbaUpdateLevel = 1;
                txtPManager.updateTXTPanes(topicTxtPane, linkTxtPane, myTBATable, thisRowSA, tbaUpdateLevel);
            } else {
                String thisBepOffset = thisRowSA[1];
                if (!thisBepOffset.equals(lastRowBepOffset)) {
                    // update Anchor OL highlight
                    //      & 1st Link/Pane & BEP Offset
                    tbaUpdateLevel = 2;
                    txtPManager.updateTXTPanes(topicTxtPane, linkTxtPane, myTBATable, thisRowSA, tbaUpdateLevel);
                } else {
                    String thisAnchorOL = thisRowSA[2];
                    String thisFileID = thisRowSA[3];
                    log(thisAnchorOL + "--" + thisFileID);
                    if (!thisAnchorOL.equals(lastRowAnchorOL) && !thisFileID.equals(lastRowLinkID)) {
                        // update 1st Link/Pane & BEP Offset
                        tbaUpdateLevel = 3;
                        txtPManager.updateTXTPanes(topicTxtPane, linkTxtPane, myTBATable, thisRowSA, tbaUpdateLevel);
                    }
                }
            }
            // -----------------------------------------------------------------
            // -----------------------------------------------------------------
            lastRowNu = String.valueOf(currClickRow);
            lastRowTopicID = thisRowSA[0];
            lastRowBepOffset = thisRowSA[1];
            lastRowAnchorOL = thisRowSA[2];
            lastRowLinkID = thisRowSA[3];
        }
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        super.mouseClicked(me);
        this.myTBATable = (JTable) me.getSource();
        if (me.getButton() == MouseEvent.BUTTON1) {
            // -----------------------------------------------------------------
            // must renew current Table Row Status
            String[] lastTBANavSA = rscManager.getTBANavigationIndex();
            lastRowNu = lastTBANavSA[0];
            String[] firstRow = TBAIndice.get(lastRowNu);
            lastRowTopicID = firstRow[0];
            lastRowBepOffset = firstRow[1];
            lastRowAnchorOL = firstRow[2];
            lastRowLinkID = firstRow[3];
            // -----------------------------------------------------------------

            currClickRow = myTBATable.rowAtPoint(me.getPoint());
            currClickColumn = myTBATable.columnAtPoint(me.getPoint());
            myTBATable.changeSelection(currClickRow, currClickColumn, false, false);

            myTBATable.setDefaultRenderer(Object.class, new AttributiveCellRenderer(currClickRow, currClickColumn, isTAB));
            myTBATable.repaint();

            updateTBAPanes();
        }
    }

    private Vector<String> sortVectorNumbers(Vector<String> myNumbersV) {
        int[] thisIntA = new int[myNumbersV.size()];
        for (int i = 0; i < myNumbersV.size(); i++) {
            thisIntA[i] = Integer.valueOf(myNumbersV.elementAt(i));
        }
        for (int i = 0; i < thisIntA.length; i++) {
            for (int j = i + 1; j < thisIntA.length; j++) {
                if (thisIntA[i] > thisIntA[j]) {
                    int temp = thisIntA[i];
                    thisIntA[i] = thisIntA[j];
                    thisIntA[j] = temp;
                }
            }
        }
        Vector<String> mySortedNumbersV = new Vector<String>();
        for (int i = 0; i < thisIntA.length; i++) {
            mySortedNumbersV.add(String.valueOf(thisIntA[i]));
        }
        return mySortedNumbersV;
    }
}
