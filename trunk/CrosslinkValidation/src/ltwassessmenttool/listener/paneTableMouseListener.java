package ltwassessmenttool.listener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.JTextPane;

import ltwassessment.assessment.Bep;
import ltwassessment.parsers.PoolerManager;
import ltwassessment.parsers.ResourcesManager;
import ltwassessment.utility.AttributiveCellRenderer;
import ltwassessment.utility.PaneTableIndexing;
import ltwassessment.utility.tabTxtPaneManager;
import ltwassessment.utility.tbaTxtPaneManager;

/**
 * @author Darren HUANG @ QUT
 */
public class paneTableMouseListener extends MouseAdapter {

    private final String sysPropertyKey = "isTABKey";
    private final String sysPropertyIsTopicWikiKey = "isTopicWikipedia";
    private final String sysPropertyIsLinkWikiKey = "isLinkWikipedia";
    private ResourcesManager rscManager;
    private tabTxtPaneManager tabSManager;
    private tbaTxtPaneManager tbaSManager;
    private PaneTableIndexing tabTableIndexing;
    private PaneTableIndexing tbaTableIndexing;
    private PoolerManager myRunsPooler;
    private JTextPane topicTxtPane;
    private JTextPane linkTxtPane;
    private JTable myPaneTable;
    private Hashtable<String, Hashtable<String, Hashtable<String, Vector<Bep>>>> poolOutgoingData;
    private Hashtable<String, Hashtable<String, Vector<String[]>>> poolIncomingData;
    private Hashtable<String, String[]> TABIndice;
    private Hashtable<String, String[]> tabNAVIndice;
    private Hashtable<String, String[]> TBAIndice;
    private Hashtable<String, String[]> tbaNAVIndice;
    private int paneUpdateLevel = 0;
    private boolean isTAB = true;

    static void log(Object content) {
        System.out.println(content);
    }
    // For All
    private boolean isTableClick = false;
    private String lastRowNu = "";
    private String lastRowTopicID = "";
    private int currClickRow = 0;
    private int currClickColumn = 0;
    // For Outgoing
    private String lastRowAnchorSet = "";      // e.g. 1794_16
    private String lastRowSubAnchorSet = "";   // e.g. 1794_8
    private String lastRowBEPSet = "";         // e.g. 1385_1296879
    // For Incoming
    private String lastRowBepOffset = "";             // e.g. 1794
    private String lastRowAnchorOL = "";        // e.g. 1794_8
    private String lastRowLinkID = "";          // e.g. 1296879

    public paneTableMouseListener(JTextPane topicTextPane, JTextPane linkTextPane, JTable paneTable) {
        this.isTableClick = false;
        this.topicTxtPane = topicTextPane;
        this.linkTxtPane = linkTextPane;
        this.myPaneTable = paneTable;
        // declare External Classes
        this.rscManager = ResourcesManager.getInstance();
        this.myRunsPooler = PoolerManager.getInstance();

        this.isTAB = Boolean.valueOf(System.getProperty(sysPropertyKey));
        // For Outgoing
        this.tabSManager = new tabTxtPaneManager();
        this.poolOutgoingData = myRunsPooler.getOutgoingPool();
        TABIndice = new Hashtable<String, String[]>();
        indexTABData(this.poolOutgoingData);
        tabTableIndexing = PaneTableIndexing.getInstance();
        tabNAVIndice = tabTableIndexing.getRowNAVIndices();
        // For Incoming
        this.tbaSManager = new tbaTxtPaneManager();
        this.poolIncomingData = myRunsPooler.getIncomingPool();
        TBAIndice = new Hashtable<String, String[]>();
        indexTBAData(this.poolIncomingData);
        tbaTableIndexing = PaneTableIndexing.getInstance();
        tabNAVIndice = tbaTableIndexing.getRowNAVIndices();
    }

    private void indexTABData(Hashtable<String, Hashtable<String, Hashtable<String, Vector<Bep>>>> poolOutgoingData2) {
        // Indexing Format:
        // TopicID(25640), thisPoolAnchorSet(1794_16), thisAnchorSet(1794_8), thisBEPSet(1385_1296879)
        int rowCounter = 0;
        String thisTopicID = "";
        String thisPoolAnchorSet = "";
        String thisAnchorSet = "";
        String thisBEPSet = "";

        Vector<String> myTopicIDsV = new Vector<String>();
        Hashtable<String, Vector<String>> myTopicAnchorOLHT = new Hashtable<String, Vector<String>>();
        Vector<String> myAnchorOLV = new Vector<String>();
        Enumeration topicKeyEnu = poolOutgoingData2.keys();
        while (topicKeyEnu.hasMoreElements()) {
            // Get Topic ID
            Object topicKeyObj = topicKeyEnu.nextElement();
            if (!myTopicIDsV.contains(topicKeyObj.toString())) {
                myTopicIDsV.add(topicKeyObj.toString());
            }
            // Get Anchor OL
            myAnchorOLV = new Vector<String>();
            Hashtable<String, Hashtable<String, Vector<Bep>>> anchorBepsH = poolOutgoingData2.get(topicKeyObj);
            Enumeration anchorKeyEnu = anchorBepsH.keys();
            while (anchorKeyEnu.hasMoreElements()) {
                Object anchorOLObj = anchorKeyEnu.nextElement();
                if (!myAnchorOLV.contains(anchorOLObj.toString())) {
                    myAnchorOLV.add(anchorOLObj.toString());
                }
            }
            Vector<String> mySortedAnchorOLV = sortOLVectorNumbers(myAnchorOLV);
            myTopicAnchorOLHT.put(topicKeyObj.toString(), mySortedAnchorOLV);
        }
        Collections.sort(myTopicIDsV);
        for (String tID : myTopicIDsV) {
            thisTopicID = tID;
            Hashtable<String, Hashtable<String, Vector<Bep>>> anchorHT = poolOutgoingData2.get(thisTopicID);
            Vector<String> anchorOLV = myTopicAnchorOLHT.get(thisTopicID);
            for (String aOL : anchorOLV) {
                thisPoolAnchorSet = aOL;
                Hashtable<String, Vector<Bep>> subAnchorHT = anchorHT.get(thisPoolAnchorSet);
                Enumeration subAnchorKeyEnu = subAnchorHT.keys();
                while (subAnchorKeyEnu.hasMoreElements()) {
                    Object subAnchorKeyObj = subAnchorKeyEnu.nextElement();
                    thisAnchorSet = subAnchorKeyObj.toString().trim();
                    Vector<Bep> bepV = subAnchorHT.get(subAnchorKeyObj);
                    for (Bep thisBepSA : bepV) {
                        thisBEPSet = thisBepSA.offsetToString() + "_" + thisBepSA.startPToString();
                        String[] tabSA = new String[]{thisTopicID, thisPoolAnchorSet, thisAnchorSet, thisBEPSet};
                        TABIndice.put(String.valueOf(rowCounter), tabSA);
                        rowCounter++;
                    }
                }
            }
        }
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

    private void updateTABPanes() {
        if (currClickRow == Integer.valueOf(lastRowNu)) {
        } else {
            // Check & Update Topic / Link Content Pane
            String[] thisRowSA = TABIndice.get(String.valueOf(currClickRow));
            String thisTopicID = thisRowSA[0];
            if (!thisTopicID.equals(lastRowTopicID)) {
                // update Topic Pane & Anchor OL highlight
                //      & 1st Link/Pane & BEP Offset
                paneUpdateLevel = 1;
                tabSManager.updateTXTPanes(topicTxtPane, linkTxtPane, myPaneTable, thisRowSA, paneUpdateLevel);
            } else {
                String thisAnchorSet = thisRowSA[1];
                if (!thisAnchorSet.equals(lastRowAnchorSet)) {
                    // update Anchor OL highlight
                    //      & 1st Link/Pane & BEP Offset
                    paneUpdateLevel = 2;
                    tabSManager.updateTXTPanes(topicTxtPane, linkTxtPane, myPaneTable, thisRowSA, paneUpdateLevel);
                } else {
                    String thisBepSet = thisRowSA[3];
                    if (!thisBepSet.equals(lastRowBEPSet)) {
                        // update 1st Link/Pane & BEP Offset
                        paneUpdateLevel = 3;
                        tabSManager.updateTXTPanes(topicTxtPane, linkTxtPane, myPaneTable, thisRowSA, paneUpdateLevel);
                    }
                }
            }
            // -----------------------------------------------------------------
            lastRowNu = String.valueOf(currClickRow);
            lastRowTopicID = thisRowSA[0];
            lastRowAnchorSet = thisRowSA[1];
            lastRowSubAnchorSet = thisRowSA[2];
            lastRowBEPSet = thisRowSA[3];
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
                paneUpdateLevel = 1;
                tbaSManager.updateTXTPanes(topicTxtPane, linkTxtPane, myPaneTable, thisRowSA, paneUpdateLevel);
            } else {
                String thisBepOffset = thisRowSA[1];
                if (!thisBepOffset.equals(lastRowBepOffset)) {
                    // update Anchor OL highlight
                    //      & 1st Link/Pane & BEP Offset
                    paneUpdateLevel = 2;
                    tbaSManager.updateTXTPanes(topicTxtPane, linkTxtPane, myPaneTable, thisRowSA, paneUpdateLevel);
                } else {
                    String thisAnchorOL = thisRowSA[2];
                    String thisFileID = thisRowSA[3];
                    if (!thisAnchorOL.equals(lastRowAnchorOL) && !thisFileID.equals(lastRowLinkID)) {
                        // update 1st Link/Pane & BEP Offset
                        paneUpdateLevel = 3;
                        tbaSManager.updateTXTPanes(topicTxtPane, linkTxtPane, myPaneTable, thisRowSA, paneUpdateLevel);
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
//        super.mouseClicked(me);
        this.myPaneTable = (JTable) me.getSource();
        if (me.getButton() == MouseEvent.BUTTON1) {
            this.isTAB = Boolean.valueOf(System.getProperty(sysPropertyKey));
            if (this.isTAB) {
                // For Outgoing
                this.myRunsPooler = PoolerManager.getInstance();
                this.tabSManager = new tabTxtPaneManager();
                this.poolOutgoingData = myRunsPooler.getOutgoingPool();
                TABIndice = new Hashtable<String, String[]>();
                indexTABData(this.poolOutgoingData);
                tabTableIndexing = PaneTableIndexing.getInstance();
                tabNAVIndice = tabTableIndexing.getRowNAVIndices();

                // -----------------------------------------------------------------
                // must renew current Table Row Status
                String[] lastTABNavSA = rscManager.getTABNavigationIndex();
                lastRowNu = lastTABNavSA[0];
                String[] firstRow = TABIndice.get(lastRowNu);
                lastRowTopicID = firstRow[0];
                lastRowAnchorSet = firstRow[1];
                lastRowSubAnchorSet = firstRow[2];
                lastRowBEPSet = firstRow[3];
                // -----------------------------------------------------------------
                currClickRow = myPaneTable.rowAtPoint(me.getPoint());
                currClickColumn = myPaneTable.columnAtPoint(me.getPoint());
                myPaneTable.changeSelection(currClickRow, currClickColumn, false, false);

                myPaneTable.setDefaultRenderer(Object.class, new AttributiveCellRenderer(currClickRow, currClickColumn, isTAB));
                myPaneTable.repaint();

                updateTABPanes();
            } else {
                // For Incoming
                this.myRunsPooler = PoolerManager.getInstance();
                this.tbaSManager = new tbaTxtPaneManager();
                this.poolIncomingData = myRunsPooler.getIncomingPool();
                TBAIndice = new Hashtable<String, String[]>();
                indexTBAData(this.poolIncomingData);
                tbaTableIndexing = PaneTableIndexing.getInstance();
                tabNAVIndice = tbaTableIndexing.getRowNAVIndices();

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
                currClickRow = myPaneTable.rowAtPoint(me.getPoint());
                currClickColumn = myPaneTable.columnAtPoint(me.getPoint());
                myPaneTable.changeSelection(currClickRow, currClickColumn, false, false);

                myPaneTable.setDefaultRenderer(Object.class, new AttributiveCellRenderer(currClickRow, currClickColumn, isTAB));
                myPaneTable.repaint();

                updateTBAPanes();
            }
        }
    }

    private Vector<String> sortOLVectorNumbers(Vector<String> myOLNumbersV) {
        int[] thisIntA = new int[myOLNumbersV.size()];
        for (int i = 0; i < myOLNumbersV.size(); i++) {
            String[] myOLNo = myOLNumbersV.elementAt(i).split("_");
            thisIntA[i] = Integer.valueOf(myOLNo[0]);
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
        Vector<String> mySortedOLNumbersV = new Vector<String>();
        for (int i = 0; i < thisIntA.length; i++) {
            String thisINT = String.valueOf(thisIntA[i]);
            // ------------------------------------------
            for (String myOL : myOLNumbersV) {
                String[] myOLSA = myOL.split("_");
                if (myOLSA[0].equals(thisINT)) {
                    mySortedOLNumbersV.add(myOL);
                    break;
                }
            }
        }
        return mySortedOLNumbersV;
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
