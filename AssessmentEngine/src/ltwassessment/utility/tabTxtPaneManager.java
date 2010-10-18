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
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import ltwassessment.AppResource;
import ltwassessment.parsers.FOLTXTMatcher;
import ltwassessment.parsers.Xml2Html;
import ltwassessment.parsers.poolerManager;
import ltwassessment.parsers.resourcesManager;
import ltwassessment.utility.highlightPainters;
import ltwassessment.utility.tabTxtPaneManager;

/**
 * @author Darren HUANG
 */
public class tabTxtPaneManager {

    protected final int bepLength = 4;
    private poolerManager myRunsPooler;
    private resourcesManager myRSCManager;
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
    // TAB
    private String rowAnchorSet = "";      // e.g. 1794_16
    private String rowSubAnchorSet = "";   // e.g. 1794_8
    private String rowBEPSet = "";         // e.g. 1385_1296879
    private String rowBEPFileID = "";
    private String rowBEPOffset = "";

    private static void log(Object text) {
        System.out.println(text);
    }

    public tabTxtPaneManager() {
        //org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ltwassessment.ltwassessmentApp.class).getContext().getResourceMap(ltwassessmentView.class);
        this.wikipediaCollTitle = AppResource.getInstance().getResourceMap().getString("collectionType.Wikipedia");
        this.teAraCollTitle = AppResource.getInstance().getResourceMap().getString("collectionType.TeAra");
        this.wikipediaTopicDirectory = AppResource.getInstance().getResourceMap().getString("wikipedia.topics.folder");
        this.bepIconImageFilePath = AppResource.getInstance().getResourceMap().getString("bepIcon.imageFilePath");
        this.bepHighlightIconImageFilePath = AppResource.getInstance().getResourceMap().getString("bepHighlightIcon.imageFilePath");
        this.contentType = AppResource.getInstance().getResourceMap().getString("html.content.type");
        this.topicBepsHTPrefix = AppResource.getInstance().getResourceMap().getString("topicBepsHT.Prefix");

        this.myRunsPooler = poolerManager.getInstance();
        this.myRSCManager = new resourcesManager();
        this.myFOLMatcher = new FOLTXTMatcher();

        this.painters = new highlightPainters();
    }

    public void updateTXTPanes(JTextPane topicTxtPane, JTextPane linkTxtPane, JTable paneJTable, String[] thisRowSA, int paneUpdateLevel) {

        this.myTopicPane = topicTxtPane;
        this.myLinkPane = linkTxtPane;
        this.myPaneTable = paneJTable;
        this.rowTopicID = thisRowSA[0];
        this.rowAnchorSet = thisRowSA[1];
        this.rowSubAnchorSet = thisRowSA[2];    // this may be EMPTY "", so must NOT be used
        this.rowBEPSet = thisRowSA[3];
        
        this.updateLevel = paneUpdateLevel;

        if (paneUpdateLevel == 1) {
            updateTopicPaneStatus(this.rowTopicID);
        }
        // update Anchor OL highlight & Auto scrolling to Curr Anchor Position
        if (paneUpdateLevel == 1 || paneUpdateLevel == 2) {
            String[] currAnchorXmlSet = this.rowAnchorSet.split("_");
            updateTopicPaneAnchorStatus(this.rowTopicID, new String[]{currAnchorXmlSet[0], currAnchorXmlSet[1], ""});
        }
        // update 1st Link/Pane & BEP Offset
        if (paneUpdateLevel == 1 || paneUpdateLevel == 2 || paneUpdateLevel == 3) {
            String[] bepSet = this.rowBEPSet.split("_");
            rowBEPOffset = bepSet[0];
            rowBEPFileID = bepSet[1];
            updateLinkPaneBepStatus(bepSet);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Update T.A.B. Status: Level 1, 2, 3">
    // Update Topic Pane & Anchor OL highlight & 1st Link/Pane & BEP Offset
    // 1) check Topic Type & get Topic XML Path
    // 2) Update toolResources XML: currTopicXmlFile & currTopicAnchors
    // 3) Update Topic Text Pane
    private void updateTopicPaneStatus(String myTopicID) {
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
        myRSCManager.updateCurrTopicID(topicXmlPath);
        boolean isTopicText = true;
        // Set Topic Content Text
        setTextPaneContent(topicXmlPath, isTopicText);
        // Highlight Anchor Texts
        Hashtable<String, Vector<String[]>> topicAnchorsHT = myRunsPooler.getTopicAllAnchors();   // Upadte AnchorSCROL into toolResources XML
        Vector<String[]> currAnchorScreenOLPairs = myFOLMatcher.getSCRAnchorPosV(this.myTopicPane, myTopicID, topicAnchorsHT);
        setTopicTextHighlighter(currAnchorScreenOLPairs);
    }
    // 4) Highlight Anchor and Selected Anchor

    private void updateTopicPaneAnchorStatus(String topicID, String[] selectedAnchorOLNameSA) {
        // currAnchorSCRSet: [0]:Anchor_Name, [1]:Offset, [2]:Offset + Length
        String[] currAnchorSCRSet = myFOLMatcher.getSCRAnchorPosSA(this.myTopicPane, topicID, selectedAnchorOLNameSA, true);
        String sPos = currAnchorSCRSet[1];
        String ePos = currAnchorSCRSet[2];
        updateTopicHighlighter(sPos + "_" + ePos);
        // Auto Scrolling to Anchor Position
        this.myTopicPane.getCaret().setDot(Integer.valueOf(ePos));
        this.myTopicPane.scrollRectToVisible(this.myTopicPane.getVisibleRect());
        this.myTopicPane.repaint();
    }
    // 5) Update Link Text Pane & BEP

    private void updateLinkPaneBepStatus(String[] BepOIDSA) {
        String thisBepOffset = BepOIDSA[0];
        String thisBepFileID = BepOIDSA[1];
        String linkXmlPath = "";
//        if (myRSCManager.getLinkCollType().equals(wikipediaCollTitle)) {
            isLinkWikipedia = true;
            String subPath = myRSCManager.getWikipediaFilePathByName(thisBepFileID + ".xml");
            if (subPath.equals("FileNotFound.xml")){
                linkXmlPath = "resources" + File.separator + "Tool_Resources" + File.separator + subPath;
            } else {
                linkXmlPath = myRSCManager.getWikipediaCollectionFolder() + subPath;
            }
//            linkXmlPath = myRSCManager.getWikipediaCollectionFolder() + myRSCManager.getWikipediaFilePathByName(thisBepFileID + ".xml");
//        } else if (myRSCManager.getLinkCollType().equals(teAraCollTitle)) {
//            isLinkWikipedia = false;
//            String subPath = myRSCManager.getTeAraFilePathByName(thisBepFileID + ".xml");
//            if (subPath.equals("FileNotFound.xml")){
//                linkXmlPath = "resources" + File.separator + "Tool_Resources" + File.separator + subPath;
//            } else {
//                linkXmlPath = myRSCManager.getTeAraCollectionFolder() + subPath;
//            }
////            linkXmlPath = myRSCManager.getTeAraCollectionFolder() + myRSCManager.getTeAraFilePathByName(thisBepFileID + ".xml");
//        }
        boolean isTopicText = false;
        setTextPaneContent(linkXmlPath, isTopicText);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Set Topic/Link Pane TXT Content & Anchor Highlight">
    // TAB Outgoing
    private void setTopicTextHighlighter(Vector<String[]> anchorLPairs) {
        try {
            // 1) get Anchor Text Offset & Length from the XML file
            Highlighter highlighter = this.myTopicPane.getHighlighter();
            Object anchorHighlightReference;
            for (String[] alPair : anchorLPairs) {
                // Active faults_669_682
                anchorHighlightReference = highlighter.addHighlight(Integer.valueOf(alPair[1]), Integer.valueOf(alPair[2]), painters.getAnchorPainter());
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(tabTxtPaneManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updateTopicHighlighter(String currSCRSEPos) {
        String[] scrPosSA = currSCRSEPos.split("_");
        Highlighter topicHighlighter = this.myTopicPane.getHighlighter();
        Highlight[] highlights = topicHighlighter.getHighlights();
        int[] achorSCRPos = new int[]{Integer.valueOf(scrPosSA[0]), Integer.valueOf(scrPosSA[1])};
        for (int i = 0; i < highlights.length; i++) {
            try {
                int sPos = highlights[i].getStartOffset();
                int ePos = highlights[i].getEndOffset();
                if (achorSCRPos[0] == sPos && achorSCRPos[1] == ePos) {
                    topicHighlighter.removeHighlight(highlights[i]);
                    Object anchorHighlightRef = topicHighlighter.addHighlight(sPos, ePos, painters.getSelectedPainter());
                } else {
                    topicHighlighter.removeHighlight(highlights[i]);
                    Object anchorHighlightRef = topicHighlighter.addHighlight(sPos, ePos, painters.getAnchorPainter());
                }
            } catch (BadLocationException ex) {
                Logger.getLogger(tabTxtPaneManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.myTopicPane.repaint();
    }

    private void setTextPaneContent(String xmlFilePath, boolean isTopicText) {
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

        try {
            String mySCRBepOffset = myFOLMatcher.getScreenBepOffset(this.myLinkPane, rowBEPFileID, rowBEPOffset, isLinkWikipedia);
            StyledDocument styDoc = (StyledDocument) this.myLinkPane.getDocument();
            Style bepStyle = styDoc.addStyle("bepIcon", null);
            StyleConstants.setIcon(bepStyle, new ImageIcon(bepIconImageFilePath));
            styDoc.insertString(Integer.valueOf(mySCRBepOffset), "LBEP", bepStyle);
            this.myLinkPane.getCaret().setDot(Integer.valueOf(mySCRBepOffset));
        } catch (BadLocationException ex) {
            Logger.getLogger(tabTxtPaneManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.myLinkPane.scrollRectToVisible(this.myLinkPane.getVisibleRect());
        // Renew Link Text Pane Listener
        this.myLinkPane.repaint();
    }
    // </editor-fold>
}
