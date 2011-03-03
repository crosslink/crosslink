package ltwassessmenttool.listener;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.event.MouseInputListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.Position;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import ltwassessment.AppResource;
import ltwassessment.parsers.FOLTXTMatcher;
import ltwassessment.parsers.Xml2Html;
import ltwassessment.parsers.PoolerManager;
import ltwassessment.parsers.resourcesManager;
import ltwassessment.utility.AttributiveCellRenderer;
import ltwassessment.utility.highlightPainters;
import ltwassessment.utility.PaneTableIndexing;
import ltwassessmenttool.LTWAssessmentToolView;

/**
 * @author Darren HUANG
 */
public class topicPaneMouseListener implements MouseInputListener {

    // Constant Variables
    private final String sysPropertyKey = "isTABKey";
    private final String sysPropertyIsTopicWikiKey = "isTopicWikipedia";
    private final String sysPropertyIsLinkWikiKey = "isLinkWikipedia";
    protected final int bepLength = 4;
    private String paneContentType = "";
    private JTextPane topicTextPane;
    private JTextPane linkTextPane;
    private String currTopicXmlPath = "";
    private String currTopicID = "";
    private String currAnchorSCRName = "";
    private String currBepSCRTitle = "";
    private Vector<String[]> anchorSCRSEPosSet;
    private Vector<String[]> bepSCRSEPosSet;
    private int cdot = 0;
    private Document doc;
    private JTable myPaneTable;
    private String afTasnCollectionErrors = "";
    private String bepIconImageFilePath = "";
    private String bepHighlightIconImageFilePath = "";
    private boolean isWikipedia = false;
    private boolean isOutgoingTAB = false;
    // Declair Anchor Status HashMap & Links HashMap
    HashMap<String, String> anchorSEPosAssStatusHM = new HashMap<String, String>();
    HashMap<String, Vector<String[]>> bepFileSAVBySCRAnchorOLHM = new HashMap<String, Vector<String[]>>();
    // Declair BEP Status HashMap & Links HashMap
    HashMap<String, String> bepSEPosAssStatusHM = new HashMap<String, String>();
    HashMap<String, Vector<String[]>> anchorFileSAVBySCRBepOLHM = new HashMap<String, Vector<String[]>>();
    // Import Extenal Classes
    private PaneTableIndexing myPaneTableIndexing;
    private PoolerManager myPooler = PoolerManager.getInstance();
    private resourcesManager myRSCManager = resourcesManager.getInstance();
    private FOLTXTMatcher myFOLMatcher = FOLTXTMatcher.getInstance();
    // Declair Highlight Painter
    highlightPainters painters = new highlightPainters();;

    private void log(Object txt) {
        System.out.println(String.valueOf(txt));
    }

    public topicPaneMouseListener(JTextPane topicPane, JTextPane linkPane, Vector<String[]> scrSEPosVSA, JTable paneTable, PaneTableIndexing paneTableIndexing) {
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ltwassessmenttool.LTWAssessmentToolApp.class).getContext().getResourceMap(LTWAssessmentToolView.class);
        afTasnCollectionErrors = resourceMap.getString("AssFormXml.taskCollectionError");
        bepIconImageFilePath = resourceMap.getString("bepIcon.imageFilePath");
        bepHighlightIconImageFilePath = resourceMap.getString("bepHighlightIcon.imageFilePath");
        paneContentType = resourceMap.getString("html.content.type");
        // ---------------------------------------------------------------------
        this.topicTextPane = topicPane;
        this.linkTextPane = linkPane;
        this.myPaneTable = paneTable;
        this.myPaneTableIndexing = paneTableIndexing;
        // ---------------------------------------------------------------------
        this.currTopicXmlPath = myRSCManager.getCurrTopicXmlFile();
        if (Boolean.valueOf(System.getProperty(sysPropertyIsTopicWikiKey))) {
            this.currTopicID = currTopicXmlPath.substring(currTopicXmlPath.lastIndexOf(File.separator) + 1, currTopicXmlPath.length() - 4);
        } else {
            this.currTopicID = currTopicXmlPath.substring(currTopicXmlPath.lastIndexOf(File.separator) + 1, currTopicXmlPath.length() - 4);
        }
        // ---------------------------------------------------------------------
        this.isOutgoingTAB = Boolean.valueOf(System.getProperty(sysPropertyKey));
        if (this.isOutgoingTAB) {
            // Topic Anchor Offset Event
            // Vector<String[]> String[]{Name, SPos, EPos}
            Vector<String[]> currAnchorSCRPos = new Vector<String[]>();
            Vector<String> currAnchorOLSet = myRSCManager.getCurrAnchorsOLV();
            for (String thisAnchorSet : currAnchorOLSet) {
                String[] thisAnchorSA = thisAnchorSet.split(" : ");
                currAnchorSCRPos.add(new String[]{thisAnchorSA[2], thisAnchorSA[3], thisAnchorSA[4]});
            }
            // -----------------------------------------------------------------
            this.anchorSCRSEPosSet = currAnchorSCRPos;
            activateTopicAnchorMouseListener();
        } else {
            // Topic BEP Offset-Length Event
            Vector<String[]> currBepSCRPos = new Vector<String[]>();
            Vector<String> currBepOLSet = myRSCManager.getCurrBepsOV();
            for (String thisBepSet : currBepOLSet) {
                String[] thisBepSA = thisBepSet.split(" : ");
                currBepSCRPos.add(new String[]{thisBepSA[1], String.valueOf(Integer.valueOf(thisBepSA[1]) + 4)});
            }
            // -----------------------------------------------------------------
            this.bepSCRSEPosSet = currBepSCRPos;
            activateTopicBepMouseListener();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Mouse Event Action">
    // Get Caret Event when Mouse Click
    private void activateTopicPaneMouseClickEvent(MouseEvent mce) {
//        this.isOutgoingTAB = Boolean.valueOf(System.getProperty(sysPropertyKey));
        Point point = mce.getPoint();
        if (mce.getButton() == MouseEvent.BUTTON1) {
            // <editor-fold defaultstate="collapsed" desc="Left-Click to Open Target Links">
            cdot = this.topicTextPane.getCaret().getDot();
            String[] SEPosTextSet = new String[3];
            if (isOutgoingTAB) {
                // Outgoing: Anchor SE Pos in Topic
                SEPosTextSet = getAnchorSENameByDot(cdot);
            } else {
                // Incoming: BEP SE Pos in Topic
                SEPosTextSet = getBepSETitleByDot(cdot);
            }
            if (SEPosTextSet != null && SEPosTextSet.length == 3) {
                this.topicTextPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                // -------------------------------------------------------------
                // 0) Highlight Anchor Text or BEP <-- Change Icon
                //    Auto Scrolling to Anchor/BEP Position
                String scrSEPosKey = SEPosTextSet[0] + "_" + SEPosTextSet[1];
                updateTopicHighlight(scrSEPosKey, isOutgoingTAB);
                this.topicTextPane.getCaret().setDot(Integer.valueOf(SEPosTextSet[1]));
                this.topicTextPane.scrollRectToVisible(this.topicTextPane.getVisibleRect());
                this.topicTextPane.repaint();
                // -------------------------------------------------------------
//                if (isOutgoingTAB) {
                    activateTopicAnchorMouseListener();
                    // Outgoing: Detect Anchor Text
                    // <editor-fold defaultstate="collapsed" desc="Click to Open LinkPane & Update TAB Table">
                    Vector<String[]> bepLinkIDOV = (Vector<String[]>) bepFileSAVBySCRAnchorOLHM.get(scrSEPosKey);
                    String bepFileID = bepLinkIDOV.elementAt(0)[0];
                    String bepFileOffset = bepLinkIDOV.elementAt(0)[1];
                    String bepXmlFilePath = myPooler.getXmlFilePathByTargetID(bepFileID, AppResource.targetLang);
                    // When Errors:
                    // bepXmlPath = afTasnCollectionErrors + " : " + myAFTask + " - " + myAFCollection
                    if (bepXmlFilePath.startsWith(afTasnCollectionErrors)) {
                        bepXmlFilePath = myRSCManager.getErrorXmlFilePath(bepXmlFilePath);
                    }
//                    if (bepXmlFilePath.startsWith(myRSCManager.getWikipediaCollectionFolder())) {
                        isWikipedia = true;
//                    } else if (bepXmlFilePath.startsWith(myRSCManager.getTeAraCollectionFolder())) {
//                        isWikipedia = false;
//                    }
                    Xml2Html xmlParser = new Xml2Html(bepXmlFilePath, isWikipedia);
                    String xmlHtmlText = xmlParser.getHtmlContent().toString();
                    this.linkTextPane.setContentType(paneContentType);
                    this.linkTextPane.setText(xmlHtmlText);
                    this.linkTextPane.setCaretPosition(0);
                    // get SCR BEP Offset
                    String mySCRBepOffset = myFOLMatcher.getScreenBepOffset(linkTextPane, bepFileID, bepFileOffset, isWikipedia);
                    // Insert into Doc in JTextPane
                    Vector<String> bepSCROffset = new Vector<String>();
                    bepSCROffset.add(mySCRBepOffset);
                    boolean isHighlightBEP = false;
                    updatePaneBepIconHighlight(this.linkTextPane, bepSCROffset, isHighlightBEP);
                    // -------------------------------------------------------------
                    // -------------------------------------------------------------
//                    System.setProperty(sysPropertyKey, String.valueOf(isOutgoingTAB));
                    this.linkTextPane.getCaret().setDot(Integer.valueOf(mySCRBepOffset));
                    this.linkTextPane.scrollRectToVisible(this.linkTextPane.getVisibleRect());
                    this.linkTextPane.repaint();
                    // -------------------------------------------------------------
                    // -------------------------------------------------------------
                    this.currTopicXmlPath = myRSCManager.getCurrTopicXmlFile();
                    if (Boolean.valueOf(System.getProperty(sysPropertyIsTopicWikiKey))) {
                        this.currTopicID = currTopicXmlPath.substring(currTopicXmlPath.lastIndexOf(File.separator) + 1, currTopicXmlPath.length() - 4);
                    } else {
                        this.currTopicID = currTopicXmlPath.substring(currTopicXmlPath.lastIndexOf(File.separator) + 1, currTopicXmlPath.length() - 4);
                    }
                    // Highlight TABTable ROW: 1st BEP link of the Selected Anchor
                    String[] currAnchorXmlOL = myRSCManager.getAnchorXmlOLBySCRSet(new String[]{SEPosTextSet[0], SEPosTextSet[1]});
                    myPaneTable.setDefaultRenderer(Object.class, new AttributiveCellRenderer(currTopicID, currAnchorXmlOL, bepFileOffset, bepFileID, isOutgoingTAB));
                    myPaneTable.repaint();
                // </editor-fold>
//                } else {
//                    activateTopicBepMouseListener();
//                    // Click on Incoming Topic Pane:
//                    // 0) Highlight BEP <-- Change Icon
//                    // 1) Get Anchor XML OL Set for this Clicked BEP ICON
//                    // 2) Set link Txt Pane Content
//                    // <editor-fold defaultstate="collapsed" desc="Click to Open LinkPane & Update TBA Table">
//                    // Vector<String[]{ID:123017, Offset:1538, Length:9, Name:TITLE}+>
//                    log("anchorFileSAVBySCRBepOLHM.size(): " + anchorFileSAVBySCRBepOLHM.size());
//                    log("scrSEPosKey: " + scrSEPosKey);
//                    Vector<String[]> anchorOLLinkIDV = (Vector<String[]>) anchorFileSAVBySCRBepOLHM.get(scrSEPosKey);
//                    String anchorOffset = anchorOLLinkIDV.elementAt(0)[0];
//                    String anchorLength = anchorOLLinkIDV.elementAt(0)[1];
//                    String anchorName = anchorOLLinkIDV.elementAt(0)[2];
//                    String anchorFileID = anchorOLLinkIDV.elementAt(0)[3];
//                    String anchorXmlFilePath = myPooler.getXmlFilePathByTargetID(anchorFileID, AppResource.sourceLang);
//                    // ---------------------------------------------------------
//                    // When Errors:
//                    // bepXmlPath = afTasnCollectionErrors + " : " + myAFTask + " - " + myAFCollection
//                    if (anchorXmlFilePath.startsWith(afTasnCollectionErrors)) {
//                        anchorXmlFilePath = myRSCManager.getErrorXmlFilePath(anchorXmlFilePath);
//                    }
////                    if (anchorXmlFilePath.startsWith(myRSCManager.getWikipediaCollectionFolder())) {
//                        isWikipedia = true;
////                    } else if (anchorXmlFilePath.startsWith(myRSCManager.getTeAraCollectionFolder())) {
////                        isWikipedia = false;
////                    }
//                    Xml2Html xmlParser = new Xml2Html(anchorXmlFilePath, isWikipedia);
//                    String xmlHtmlText = xmlParser.getHtmlContent().toString();
//                    this.linkTextPane.setContentType(paneContentType);
//                    this.linkTextPane.setText(xmlHtmlText);
//                    this.linkTextPane.setCaretPosition(0);
//                    // ---------------------------------------------------------
//                    // get SCR Anchor Offset-Length
//                    String[] mySCRAnchorSEPos = myFOLMatcher.getSCRAnchorPosSA(linkTextPane, anchorFileID, new String[]{anchorOffset, anchorLength, anchorName});
//                    // Highlight Anchor Txt in JTextPane
//                    boolean isAssessment = false;
//                    updateLinkAnchorTxtsHighlight(linkTextPane, mySCRAnchorSEPos, isAssessment);
//                    // ---------------------------------------------------------
//                    // Renew Link Text Pane Listener for Detecting Anchor Text & Right/Left Click
//                    System.setProperty(sysPropertyKey, String.valueOf(isOutgoingTAB));
////                    linkPaneMouseListener myLPMListener = new linkPaneMouseListener(this.topicTextPane, this.linkTextPane, this.myPaneTable);
////                    this.linkTextPane.addMouseListener(myLPMListener);
//                    this.linkTextPane.getCaret().setDot(Integer.valueOf(mySCRAnchorSEPos[1].trim()));
//                    this.linkTextPane.scrollRectToVisible(this.linkTextPane.getVisibleRect());
//                    this.linkTextPane.repaint();
//                    // -------------------------------------------------------------
//                    // -------------------------------------------------------------
//                    // Highlight TBATable ROW:
//                    String[] linkAnchorXmlOL = new String[]{anchorOffset, anchorLength};
//                    Vector<String> bepOLListV = myRSCManager.getCurrBepsOV();
//                    String bepSCROffset = SEPosTextSet[0];
//                    String bepXmlOffset = "";
//                    for (String thisBepOL : bepOLListV) {
//                        String[] bepOLSA = thisBepOL.split(" : ");
//                        if (bepOLSA[1].equals(bepSCROffset)) {
//                            bepXmlOffset = bepOLSA[0];
//                        }
//                    }
//                    this.currTopicXmlPath = myRSCManager.getCurrTopicXmlFile();
//                    if (Boolean.valueOf(System.getProperty(sysPropertyIsTopicWikiKey))) {
//                        this.currTopicID = currTopicXmlPath.substring(currTopicXmlPath.lastIndexOf(File.separator) + 1, currTopicXmlPath.length() - 4);
//                    } else {
//                        this.currTopicID = currTopicXmlPath.substring(currTopicXmlPath.lastIndexOf(File.separator) + 1, currTopicXmlPath.length() - 4);
//                    }
//                    this.myPaneTable.setDefaultRenderer(Object.class, new AttributiveCellRenderer(currTopicID, linkAnchorXmlOL, bepXmlOffset, anchorFileID, isOutgoingTAB));
//                    this.myPaneTable.repaint();
//                // </editor-fold>
//                }
            }
        // </editor-fold>
        }
    }

    private void mouseHoverCaretEvent(MouseEvent me) {
        this.isOutgoingTAB = Boolean.valueOf(System.getProperty(sysPropertyKey));
        // Move Caret with mouse pointer
        Point pt = me.getPoint();
        Position.Bias[] biasRet = new Position.Bias[1];
        int pos = this.topicTextPane.getUI().viewToModel(this.topicTextPane, pt, biasRet);
        if (biasRet[0] == null) {
            biasRet[0] = Position.Bias.Forward;
        }
        if (pos >= 0) {
            ((DefaultCaret) this.topicTextPane.getCaret()).setDot(pos, biasRet[0]);
            int thisCaret = this.topicTextPane.getCaretPosition();
            String[] thisSCRSESet = null;
            if (isOutgoingTAB) {
                thisSCRSESet = getAnchorSENameByDot(thisCaret);
            } else {
                thisSCRSESet = getBepSETitleByDot(thisCaret);
            }
            if (thisSCRSESet != null && thisSCRSESet.length == 3) {
                this.topicTextPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                this.topicTextPane.setCursor(Cursor.getDefaultCursor());
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Utilities: UPDATE & GET">
    private void updateTopicHighlight(String currSCRPos, boolean isOutgoing) {
        String[] scrSEPosSA = currSCRPos.split("_");
        if (isOutgoing) {
            // Outgoing: Highlight Anchor Text
            String[] anchorSCRPosSA = new String[]{"", scrSEPosSA[0], scrSEPosSA[1]};
            boolean isAssessment = false;
            updateTopicAnchorTxtsHighlight(this.topicTextPane, anchorSCRPosSA, isAssessment);
        } else {
            // Incoming: Change BEP Icon
            Vector<String> bepSCROffset = new Vector<String>();
            bepSCROffset.add(scrSEPosSA[0]);
            boolean isHighlighBEP = true;
            updatePaneBepIconHighlight(this.topicTextPane, bepSCROffset, isHighlighBEP);
        }
    }

    private void updateLinkAnchorTxtsHighlight(JTextPane txtPane, String[] anchorSEPosSA, boolean isAssessment) {
        // This might need to handle: isAssessment
        // 1) YES:
        // 2) NO: ONLY highlight Anchor Text
        try {
            Highlighter txtPaneHighlighter = txtPane.getHighlighter();
            int[] achorSCRPos = new int[]{Integer.valueOf(anchorSEPosSA[1]), Integer.valueOf(anchorSEPosSA[2])};
            Object anchorHighlightRef = txtPaneHighlighter.addHighlight(achorSCRPos[0], achorSCRPos[1], painters.getAnchorPainter());
        } catch (BadLocationException ex) {
            Logger.getLogger(topicPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updateTopicAnchorTxtsHighlight(JTextPane txtPane, String[] anchorSEPosSA, boolean isAssessment) {
        // This might need to handle: isAssessment
        // 1) YES: Highlight Curr Selected Anchor Text, keep others remaining as THEIR Colors
        // 2) NO: Highlight Curr Selected Anchor Text, keep others remaining as Anchor Text Color
        try {
            Highlighter txtPaneHighlighter = txtPane.getHighlighter();
            Highlight[] highlights = txtPaneHighlighter.getHighlights();
            int[] achorSCRPos = new int[]{Integer.valueOf(anchorSEPosSA[1]), Integer.valueOf(anchorSEPosSA[2])};
            for (int i = 0; i < highlights.length; i++) {
                int sPos = highlights[i].getStartOffset();
                int ePos = highlights[i].getEndOffset();
                txtPaneHighlighter.removeHighlight(highlights[i]);
                if (achorSCRPos[0] == sPos && achorSCRPos[1] == ePos) {
                    Object anchorHighlightRef = txtPaneHighlighter.addHighlight(sPos, ePos, painters.getSelectedPainter());
                } else {
                    if (isAssessment) {
                        if (highlights[i].getPainter().equals(painters.getSelectedPainter())) {
                            // repaint it as ITS Color
                        } else {
                            // DO NOTHING ???
                        }
                    } else {
                        Object anchorHighlightRef = txtPaneHighlighter.addHighlight(sPos, ePos, painters.getAnchorPainter());
                    }
                }

            }
        } catch (BadLocationException ex) {
            Logger.getLogger(topicPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void updatePaneBepIconHighlight(JTextPane txtPane, Vector<String> bepSCROffset, boolean isHighlighBEP) {
        // TODO: "isHighlighBEP"
        // 1) YES: Remove previous Highlight BEPs + Make a new Highlight BEP
        // 2) NO: INSERT BEP ICONs for this Topic
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
            Logger.getLogger(topicPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        txtPane.repaint();
    }

    private String[] getBepSETitleByDot(int thisdot) {
        Vector<String[]> currBepSCRPos = new Vector<String[]>();
        Vector<String> currBepOLSet = myRSCManager.getCurrBepsOV();
        int counter = 0;
        for (String thisBepSet : currBepOLSet) {
            String[] thisBepSA = thisBepSet.split(" : ");
            int thisBepOffset = Integer.valueOf(thisBepSA[1]);
            currBepSCRPos.add(new String[]{String.valueOf(thisBepOffset), String.valueOf(thisBepOffset + 4)});
            counter++;
        }
        // -----------------------------------------------------------------
        String[] thisBepSet = null;
        for (String[] thisSA : currBepSCRPos) {
            String[] aSet = thisSA;
            int aStartPoint = Integer.valueOf(aSet[0]);
            int aEndPoint = Integer.valueOf(aSet[1]);
            if (aStartPoint <= thisdot && thisdot <= aEndPoint) {
                this.topicTextPane.setSelectionStart(Integer.valueOf(aStartPoint));
                this.topicTextPane.setSelectionEnd(Integer.valueOf(aEndPoint));
                currBepSCRTitle = "TBEP";
                return thisBepSet = new String[]{String.valueOf(aStartPoint), String.valueOf(aEndPoint), currBepSCRTitle};
            }
        }
        return thisBepSet;
    }

    private String[] getAnchorSENameByDot(int thisdot) {
        Vector<String[]> currAnchorSCRPos = new Vector<String[]>();
        Vector<String> currAnchorOLSet = myRSCManager.getCurrAnchorsOLV();
        for (String thisAnchorSet : currAnchorOLSet) {
            String[] thisAnchorSA = thisAnchorSet.split(" : ");
            currAnchorSCRPos.add(new String[]{thisAnchorSA[2], thisAnchorSA[3], thisAnchorSA[4]});
        }
        // ---------------------------------------------------------------------
        String[] thisAnchorSet = null;
        for (String[] thisSA : currAnchorSCRPos) {
            String[] aSet = thisSA;
            int aStartPoint = Integer.valueOf(aSet[1]);
            int aEndPoint = Integer.valueOf(aSet[2]);
            if (aStartPoint <= thisdot && thisdot <= aEndPoint) {
                this.topicTextPane.setSelectionStart(Integer.valueOf(aStartPoint));
                this.topicTextPane.setSelectionEnd(Integer.valueOf(aEndPoint));
                currAnchorSCRName = aSet[0].toString().trim();
                return thisAnchorSet = new String[]{String.valueOf(aStartPoint), String.valueOf(aEndPoint), currAnchorSCRName};
            }
        }
        return thisAnchorSet;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Activate Topic Pane TAB/TBA Mouse Listeners">
    private void activateTopicBepMouseListener() {
        // 1) bepSEPosAssStatusHM
        // 2) anchorFileSAVBySCRBepOLHM
        // ---------------------------------------------------------------------
        // initialize HighlightPainter
//        painters = new highlightPainters();
        // The Length of a BEP ICON is 4 because of the setting of "TBEP"
        // initialize BEP Status: "669_673=669+4", "true" <-- default
        // Topic BEP Offset-Length Event
        Vector<String[]> currBepSCRPos = new Vector<String[]>();
        Vector<String> currBepOLSet = myRSCManager.getCurrBepsOV();
        for (String thisBepSet : currBepOLSet) {
            String[] thisBepSA = thisBepSet.split(" : ");
            currBepSCRPos.add(new String[]{thisBepSA[1], String.valueOf(Integer.valueOf(thisBepSA[1]) + 4)});
        }
        // -----------------------------------------------------------------
        this.bepSCRSEPosSet = currBepSCRPos;
        Vector<String> bepOffsetKey = new Vector<String>();
        for (String[] bepSCROffsetSA : bepSCRSEPosSet) {
            bepOffsetKey.add(bepSCROffsetSA[0] + "_" + (Integer.valueOf(bepSCROffsetSA[0]) + bepLength));
        }
        for (String thisOKey : bepOffsetKey) {
            bepSEPosAssStatusHM.put(thisOKey, "true");
        }
        // ---------------------------------------------------------------------
        // Get Bep --> Links:
        // Find Bep(Offset:1114), Vector<String[]{Offset:1538, Length:9, Name:TITLE, ID:123017}+>
        this.currTopicXmlPath = myRSCManager.getCurrTopicXmlFile();
        if (Boolean.valueOf(System.getProperty(sysPropertyIsTopicWikiKey))) {
            this.currTopicID = currTopicXmlPath.substring(currTopicXmlPath.lastIndexOf(File.separator) + 1, currTopicXmlPath.length() - 4);
        } else {
            this.currTopicID = currTopicXmlPath.substring(currTopicXmlPath.lastIndexOf(File.separator) + 1, currTopicXmlPath.length() - 4);
        }
        HashMap<String, Vector<String[]>> anchorFileSetByBepOffsetKeyHM = myPooler.getAnchorFileSetByBep(currTopicID);
        Set keySet = anchorFileSetByBepOffsetKeyHM.keySet();
        Iterator thisKey = keySet.iterator();
        while (thisKey.hasNext()) {
            Object keyObj = thisKey.next();
            String xmlBOffset = keyObj.toString();
            String scrBOffset = "";
            Vector<String[]> myVSA = anchorFileSetByBepOffsetKeyHM.get(keyObj);
            Vector<String> tBepsListSA = myRSCManager.getCurrBepsOV();
            for (String thisBOSet : tBepsListSA) {
                String[] thisBOSA = thisBOSet.split(" : ");
                if (thisBOSA[0].trim().equals(xmlBOffset)) {
                    scrBOffset = thisBOSA[1].trim();
                }
            }
            String mySCRBepPos = scrBOffset + "_" + (Integer.valueOf(scrBOffset) + bepLength);
            log("mySCRBepPos: " + mySCRBepPos);
            anchorFileSAVBySCRBepOLHM.put(mySCRBepPos, myVSA);
        }
    }

    private void activateTopicAnchorMouseListener() {
//        // initialize HighlightPainter
//        painters = new highlightPainters();
        // -----------------------------------------------------------------
        // initialize Anchor Status: "669_682", "true" <-- default
        Vector<String> anchorOLKey = myRSCManager.getCurrAnchorsSCROL();
        for (String thisOLKey : anchorOLKey) {
            anchorSEPosAssStatusHM.put(thisOLKey, "true");
        }
        // Get Anchor --> Links:
        this.currTopicXmlPath = myRSCManager.getCurrTopicXmlFile();
        if (Boolean.valueOf(System.getProperty(sysPropertyIsTopicWikiKey))) {
            this.currTopicID = currTopicXmlPath.substring(currTopicXmlPath.lastIndexOf(File.separator) + 1, currTopicXmlPath.length() - 4);
        } else {
            this.currTopicID = currTopicXmlPath.substring(currTopicXmlPath.lastIndexOf(File.separator) + 1, currTopicXmlPath.length() - 4);
        }
        HashMap<String, Vector<String[]>> aLinksHM = myPooler.getBepSetByAnchor(currTopicID);
        // Find screen OL from Xml OL for all Anchors here
        Set keySet = aLinksHM.keySet();
        Iterator thisKey = keySet.iterator();
        while (thisKey.hasNext()) {
            Object keyObj = thisKey.next();
            Vector<String[]> myVSA = aLinksHM.get(keyObj);
            String[] keySA = keyObj.toString().split("_");
            String[] thisAnchorSet = new String[]{keySA[0], String.valueOf(Integer.valueOf(keySA[1]) - Integer.valueOf(keySA[0])), keySA[2]};
            String[] scrAnchorPosSA = myFOLMatcher.getSCRAnchorPosSA(topicTextPane, currTopicID, thisAnchorSet, true, AppResource.sourceLang);
            String mySCRAnchorPos = scrAnchorPosSA[1] + "_" + scrAnchorPosSA[2];
            bepFileSAVBySCRAnchorOLHM.put(mySCRAnchorPos, myVSA);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Mouse Events">
    public void mouseClicked(MouseEvent mce) {
        activateTopicPaneMouseClickEvent(mce);
    }

    public void mouseMoved(MouseEvent me) {
        /**
         * TODO:
         *      1) To detect the Anchor Text Position to
         *      1-1) get Selection for direct Drag
         *      1-2) change Cursor Type
         */
        mouseHoverCaretEvent(me);
    }

    public void mousePressed(MouseEvent mpe) {
    }

    public void mouseDragged(MouseEvent mde) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent mee) {
    }

    public void mouseExited(MouseEvent e) {
    }
    // </editor-fold>
}

















