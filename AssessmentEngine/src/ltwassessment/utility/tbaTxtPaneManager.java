package ltwassessment.utility;

import java.awt.Insets;
import java.io.File;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import ltwassessment.AppResource;
import ltwassessment.parsers.FOLTXTMatcher;
import ltwassessment.parsers.Xml2Html;
import ltwassessment.parsers.PoolerManager;
import ltwassessment.parsers.ResourcesManager;
import ltwassessment.utility.highlightPainters;
import ltwassessment.utility.tabTxtPaneManager;

/**
 * @author Darren HUANG
 */
public class tbaTxtPaneManager {

    protected final int bepLength = 4;
    private PoolerManager myRunsPooler;
    private ResourcesManager myRSCManager;
    private FOLTXTMatcher myFOLMatcher;
    private String wikipediaCollTitle = "";
    private String teAraCollTitle = "";
    private String wikipediaTopicDirectory = "";
    private String bepIconImageFilePath = "";
    private String bepHighlightIconImageFilePath = "";
    private String contentType = "";
    private String topicBepsHTPrefix = "";
    private JTextPane myTopicPane;
    private JTextPane myLinkPane;
    private JTable myPaneTable;
    private highlightPainters painters;
    private boolean isTopicWikipedia = true;
    private boolean isLinkWikipedia = true;
    private int updateLevel = 0;
    private String rowTopicID = "";        // e.g. 25640
    private String rowBepOffset = "";
    private String rowAnchorXmlOLSet = "";
    private String rowAnchorXmlOffset = "";
    private String rowAnchorXmlLength = "";
    private String rowAnchorFileID = "";

    private static void log(Object text) {
        System.out.println(text);
    }

    public tbaTxtPaneManager() {
        //org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ltwassessment.ltwassessmentApp.class).getContext().getResourceMap(ltwassessmentView.class);
        this.wikipediaCollTitle = AppResource.getInstance().getResourceMap().getString("collectionType.Wikipedia");
        this.teAraCollTitle = AppResource.getInstance().getResourceMap().getString("collectionType.TeAra");
        this.wikipediaTopicDirectory = AppResource.getInstance().getResourceMap().getString("wikipedia.topics.folder");
        this.bepIconImageFilePath = AppResource.getInstance().getResourceMap().getString("bepIcon.imageFilePath");
        this.bepHighlightIconImageFilePath = AppResource.getInstance().getResourceMap().getString("bepHighlightIcon.imageFilePath");
        this.contentType = AppResource.getInstance().getResourceMap().getString("html.content.type");
        this.topicBepsHTPrefix = AppResource.getInstance().getResourceMap().getString("topicBepsHT.Prefix");

        this.myRunsPooler = PoolerManager.getInstance();
        this.myRSCManager = ResourcesManager.getInstance();
        this.myFOLMatcher = FOLTXTMatcher.getInstance();

        this.painters = new highlightPainters();
    }

    public void updateTXTPanes(JTextPane topicTxtPane, JTextPane linkTxtPane, JTable paneJTable, String[] thisRowSA, int paneUpdateLevel) {
        this.myTopicPane = topicTxtPane;
        this.myLinkPane = linkTxtPane;
        this.myPaneTable = paneJTable;
        this.rowTopicID = thisRowSA[0];

        this.rowBepOffset = thisRowSA[1];
        this.rowAnchorXmlOLSet = thisRowSA[2];
        this.rowAnchorFileID = thisRowSA[3];

        this.updateLevel = paneUpdateLevel;

        // 1) Update Topic TXT Pane
        if (paneUpdateLevel == 1) {
            updateTopicPaneStatus(this.rowTopicID, AppResource.sourceLang);
        }
        // 1) + 2) Update BEP highlight on Topic TXT Pane &
        //    and Auto scrolling to Curr BEP Position
        if (paneUpdateLevel == 1 || paneUpdateLevel == 2) {
            String currBepXmlOffset = this.rowBepOffset;
            updateTopicPaneBepStatus(this.rowTopicID, currBepXmlOffset);
        }
        // 1) + 2) + 3) Update New Link Document
        if (paneUpdateLevel == 1 || paneUpdateLevel == 2 || paneUpdateLevel == 3) {
            String[] anchorOLSA = rowAnchorXmlOLSet.split("_");
            rowAnchorXmlOffset = anchorOLSA[0];
            rowAnchorXmlLength = anchorOLSA[1];
            updateLinkPaneAnchorStatus(anchorOLSA, rowAnchorFileID, thisRowSA[4]);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Update T.B.A. Status: Level 1, 2, 3">
    // Update Topic Pane & Anchor OL highlight & 1st Link/Pane & BEP Offset
    // 1) check Topic Type & get Topic XML Path
    // 2) Update toolResources XML: currTopicXmlFile & currTopicAnchors
    // 3) Update Topic Text Pane
    private void updateTopicPaneStatus(String myTopicID, String lang) {
        String topicXmlPath = "";
//        if (myRSCManager.getTopicCollType().equals(wikipediaCollTitle)) {
            isTopicWikipedia = true;
            topicXmlPath = wikipediaTopicDirectory + myTopicID + ".xml";
//        } else if (myRSCManager.getTopicCollType().equals(teAraCollTitle)) {
//            isTopicWikipedia = false;
//            String subPath = myRSCManager.getTeAraFilePathByName(myTopicID + ".xml");
//            if (subPath.equals("FileNotFound.xml")){
//                topicXmlPath = "resources" + File.separator + "Tool_Resources" + File.separator + subPath;
//            } else {
//                topicXmlPath = myRSCManager.getTeAraCollectionFolder() + subPath;
//            }
////            topicXmlPath = myRSCManager.getTeAraCollectionFolder() + myRSCManager.getTeAraFilePathByName(myTopicID + ".xml");
//        }
        myRSCManager.updateCurrTopicFilePath(topicXmlPath);
        // Set Topic Content Text
        boolean isTopicPane = true;
        setTextPaneContent(topicXmlPath, isTopicPane, lang);
        // INSERT BEP Icons
        Hashtable<String, Vector<String[]>> topicBepsHT = myRunsPooler.getTopicAllBeps();
        Vector<String[]> thisTopicBepsV = topicBepsHT.get(topicBepsHTPrefix + myTopicID);
        Vector<String[]> bepSCRPosV = myFOLMatcher.getSCRBepPosV(this.myTopicPane, myTopicID, thisTopicBepsV, isTopicWikipedia);
        Vector<String> myBepSCROffsetV = new Vector<String>();
        for (String[] bepSCRPos : bepSCRPosV) {
            myBepSCROffsetV.add(bepSCRPos[0]);
        }
        boolean isHighlighBEP = false;
        updateBepIconStatus(this.myTopicPane, myBepSCROffsetV, isHighlighBEP);
    }
    // 4) Highlight Anchor and Selected Anchor
    //    Highlight BEP

    private void updateTopicPaneBepStatus(String topicID, String selectedBepOffset) {
        // currAnchorSCRSet: [0]:Anchor_Name, [1]:Offset, [2]:Offset + Length
        Vector<String> bepOLListV = myRSCManager.getCurrBepsOV();
        String sPos = "0";
        for (String thisOLList : bepOLListV) {
            String[] thisOLSA = thisOLList.split(" : ");
            if (thisOLSA[0].equals(selectedBepOffset)) {
                sPos = thisOLSA[1];
            }
        }
        String ePos = String.valueOf(Integer.valueOf(sPos) + 4);
        updateTopicHighlighter(sPos + "_" + ePos);
        // Auto Scrolling to Anchor Position
        this.myTopicPane.getCaret().setDot(Integer.valueOf(ePos));
        this.myTopicPane.scrollRectToVisible(this.myTopicPane.getVisibleRect());
        this.myTopicPane.repaint();
    }
    // 5) Update Link Text Pane & BEP

    private void updateLinkPaneAnchorStatus(String[] anchorOLSA, String rowAnchorFileID, String lang) {
        String thisFileID = rowAnchorFileID;
        String linkXmlPath = "";
//        if (myRSCManager.getLinkCollType().equals(wikipediaCollTitle)) {
            isLinkWikipedia = true;
            String subPath = myRSCManager.getWikipediaFilePathByName(thisFileID + ".xml", lang);
            if (subPath.equals("FileNotFound.xml")){
                linkXmlPath = "resources" + File.separator + "Tool_Resources" + File.separator + subPath;
            } else {
                linkXmlPath = /*myRSCManager.getWikipediaFileFolder(AppResource.targetLang) + */subPath;
            }
//        } else if (myRSCManager.getLinkCollType().equals(teAraCollTitle)) {
//            isLinkWikipedia = false;
//            String subPath = myRSCManager.getTeAraFilePathByName(thisFileID + ".xml");
//            if (subPath.equals("FileNotFound.xml")){
//                linkXmlPath = "resources" + File.separator + "Tool_Resources" + File.separator + subPath;
//            } else {
//                linkXmlPath = myRSCManager.getTeAraCollectionFolder() + subPath;
//            }
//        }
        boolean isTopicPane = false;
        setTextPaneContent(linkXmlPath, isTopicPane, lang);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Set Topic/Link Pane TXT Content & Anchor Highlight">
    private void updateTopicHighlighter(String currSCRSEPos) {
        String[] scrPosSA = currSCRSEPos.split("_");
        // Incoming: Change BEP Icon
        Vector<String> bepSCROffset = new Vector<String>();
        bepSCROffset.add(scrPosSA[0]);
        boolean isHighlighBEP = true;
        updateBepIconStatus(this.myTopicPane, bepSCROffset, isHighlighBEP);
        this.myTopicPane.repaint();
    }

    private void updateBepIconStatus(JTextPane txtPane, Vector<String> bepSCROffset, boolean isHighlighBEP) {
        try {
            StyledDocument styDoc = (StyledDocument) txtPane.getDocument();
            Vector<String> HBepSCROffsetV = new Vector<String>();
            Vector<String> bepOLListV = myRSCManager.getCurrBepsOV();
            for (String thisBepOL : bepOLListV) {
                String[] thisBepOLSA = thisBepOL.split(" : ");
                String thisBEPTxt = styDoc.getText(Integer.valueOf(thisBepOLSA[1]), bepLength);
                if (thisBEPTxt.equals("HBEP")) {
                    HBepSCROffsetV.add(thisBepOLSA[1]);
                }
            }

            Style bepHStyle = styDoc.addStyle("bepHIcon", null);
            StyleConstants.setIcon(bepHStyle, new ImageIcon(bepHighlightIconImageFilePath));
            Style bepStyle = styDoc.addStyle("bepIcon", null);
            StyleConstants.setIcon(bepStyle, new ImageIcon(bepIconImageFilePath));

            if (isHighlighBEP) {
                for (String scrHOffset : HBepSCROffsetV) {
                    styDoc.remove(Integer.valueOf(scrHOffset), bepLength);
                    styDoc.insertString(Integer.valueOf(scrHOffset), "TBEP", bepStyle);
                }
                for (String scrOffset : bepSCROffset) {
                    styDoc.remove(Integer.valueOf(scrOffset), bepLength);
                    styDoc.insertString(Integer.valueOf(scrOffset), "HBEP", bepHStyle);
                }
            } else {
                for (String scrOffset : bepSCROffset) {
                    styDoc.insertString(Integer.valueOf(scrOffset), "TBEP", bepStyle);
                }
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(tabTxtPaneManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        txtPane.repaint();
    }

    private void setTextPaneContent(String xmlFilePath, boolean isTopicText, String lang) {
        if (!xmlFilePath.equals("")) {
            if (isTopicText) {
                createTopicTextPane(xmlFilePath);
            } else {
                createLinkTextPane(xmlFilePath);
            }
        } else {
            this.myTopicPane.setContentType(contentType);
            this.myTopicPane.setText("<b>The topic file is missing or specified wrongly!!!</b>");
        }
    }

    private void createTopicTextPane(String xmlFilePath) {
        this.myTopicPane.setCaretPosition(0);
        this.myTopicPane.setMargin(new Insets(5, 5, 5, 5));
        Xml2Html xmlParser = new Xml2Html(xmlFilePath, isTopicWikipedia);
        this.myTopicPane.setContentType(contentType);
        this.myTopicPane.setText(xmlParser.getHtmlContent().toString());
        this.myTopicPane.setCaretPosition(0);
    }

    private void createLinkTextPane(String xmlFilePath) {
        this.myLinkPane.setCaretPosition(0);
        this.myLinkPane.setMargin(new Insets(5, 5, 5, 5));
        Xml2Html xmlParser = new Xml2Html(xmlFilePath, isLinkWikipedia);
        this.myLinkPane.setContentType(contentType);
        this.myLinkPane.setText(xmlParser.getHtmlContent().toString());
        this.myLinkPane.setCaretPosition(0);
//        try {
//            String[] mySCRAnchorSEPos = myFOLMatcher.getSCRAnchorPosSA(myLinkPane, rowAnchorFileID, new String[]{rowAnchorXmlOffset, rowAnchorXmlLength, ""}, AppResource.targetLang);
//            Highlighter txtPaneHighlighter = this.myLinkPane.getHighlighter();
//            int[] achorSCRPos = new int[]{Integer.valueOf(mySCRAnchorSEPos[1]), Integer.valueOf(mySCRAnchorSEPos[2])};
//            Object anchorHighlightRef = txtPaneHighlighter.addHighlight(achorSCRPos[0], achorSCRPos[1], painters.getAnchorPainter());
//            this.myLinkPane.getCaret().setDot(Integer.valueOf(mySCRAnchorSEPos[2]));
//        } catch (BadLocationException ex) {
//            Logger.getLogger(tabTxtPaneManager.class.getName()).log(Level.SEVERE, null, ex);
//        }
        this.myLinkPane.scrollRectToVisible(this.myLinkPane.getVisibleRect());
        // Renew Link Text Pane Listener
        this.myLinkPane.repaint();
    }
    // </editor-fold>
}
