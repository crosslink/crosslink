package ltwassessmenttool.listener;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.event.MouseInputListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.Position;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import ltwassessmenttool.LTWAssessmentToolControler;
import ltwassessmenttool.LTWAssessmentToolView;
import ltwassessment.AppResource;
import ltwassessment.assessment.CurrentFocusedAnchor;
import ltwassessment.parsers.FOLTXTMatcher;
import ltwassessment.parsers.Xml2Html;
import ltwassessment.parsers.PoolerManager;
import ltwassessment.parsers.ResourcesManager;
import ltwassessment.utility.ObservableSingleton;
import ltwassessment.utility.highlightPainters;
import ltwassessment.utility.PoolUpdater;

/**
 * @author Darren HUANG
 */
public class topicPaneMouseListener implements MouseInputListener {

    // Constant Variables
    protected final int bepLength = 4;
    private final String sysPropertyKey = "isTABKey";
    private String preTHyperOLSEStatus[] = null;
    private String paneContentType = "";
    private JTextPane topicTextPane;
    private JTextPane linkTextPane;
    private String currTopicXmlPath = "";
    private String currTopicName = "";
    private String currTopicID = "";
    private String currAnchorSCRName = "";
    private String currBepSCRTitle = "";
//    private Vector<String[]> anchorSCRSEPosSet;
//    private Vector<String[]> bepSCRSEPosSet;
//    private int cdot = 0;
//    private Document doc;
    private String afTasnCollectionErrors = "";
    private String bepIconImageFilePath = "";
    private String bepHighlightIconImageFilePath = "";
    private String bepCompletedIconImageFilePath = "";
    private String bepNonrelevantIconImageFilePath = "";
    private boolean isWikipedia = false;
    private boolean isOutgoingTAB = false;
    private Hashtable<String, String[]> topicAnchorOLTSENHT = new Hashtable<String, String[]>();
    // Declair Anchor Status HashMap & Links HashMap
    private HashMap<String, Vector<String[]>> bepFileSAVBySCRAnchorOLHM = new HashMap<String, Vector<String[]>>();
    // Declair BEP Status HashMap & Links HashMap
    private HashMap<String, String> bepSEPosAssStatusHM = new HashMap<String, String>();
    private HashMap<String, Vector<String[]>> anchorFileSAVBySCRBepOLHM = new HashMap<String, Vector<String[]>>();
    // Import Extenal Classes
    private PoolerManager poolerManager = PoolerManager.getInstance();
    private PoolUpdater pUpdater = poolerManager.getPoolUpdater();
    private ResourcesManager myRSCManager = ResourcesManager.getInstance();
    private FOLTXTMatcher myFOLMatcher = FOLTXTMatcher.getInstance();
    // Declair Highlight Painter
    private highlightPainters painters;
    // Color c = new Color(255, 255, 240);
    private Color linkPaneWhiteColor = Color.WHITE;
    private Color linkPaneRelColor = new Color(168, 232, 177);
    private Color linkPaneNonRelColor = new Color(255, 183, 165);
    private ObservableSingleton os = null;
    private Hashtable<String, Object> myTAnchorSEHiObj = new Hashtable<String, Object>();

    private void log(Object txt) {
        System.out.println(String.valueOf(txt));
    }

    private void logger(Object aObj) {
        try {
            String targetFile = "resources" + File.separator + "Pool" + File.separator + "T" + this.currTopicID + "_ltwAssessTool2009.log";
            File poolFileWithName = new File(targetFile);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
			        new FileOutputStream(poolFileWithName), "UTF8")); //new BufferedWriter(new FileWriter(poolFileWithName, true));

            DateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmss");
            Date date = new Date();
            String currentDateTime = dateFormat.format(date).toString();

            String logStr = this.currTopicID + " : " + currentDateTime + " : " + aObj.toString();
            bw.write(logStr);
            bw.newLine();

            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(linkPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public topicPaneMouseListener(JTextPane topicPane, JTextPane linkPane) {
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ltwassessmenttool.LTWAssessmentToolApp.class).getContext().getResourceMap(LTWAssessmentToolView.class);
        afTasnCollectionErrors = resourceMap.getString("AssFormXml.taskCollectionError");
        bepIconImageFilePath = resourceMap.getString("bepIcon.imageFilePath");
        bepHighlightIconImageFilePath = resourceMap.getString("bepHighlightIcon.imageFilePath");
        bepCompletedIconImageFilePath = resourceMap.getString("bepCompletedIcon.imageFilePath");
        bepNonrelevantIconImageFilePath = resourceMap.getString("bepNonrelevantIcon.imageFilePath");
        paneContentType = resourceMap.getString("html.content.type");
        // ---------------------------------------------------------------------
        this.painters = new highlightPainters();
        this.topicTextPane = topicPane;
        this.linkTextPane = linkPane;
        this.os = ObservableSingleton.getInstance();
        // ---------------------------------------------------------------------
        // In case:
        // Outgoing Anchor TXT: scrSE, String[]{O,L,TXT,S,E,num}
        // Incoming BEP: scrS, String[]{O,S, num}
//        this.topicAnchorOLTSENHT = topicOLTSEIndex;
        // ---------------------------------------------------------------------
        Vector<String[]> topicIDNameVSA = this.poolerManager.getAllTopicsInPool();
        this.currTopicName = topicIDNameVSA.elementAt(0)[1].trim();
        // Only 1 Topic per Package so it can be drclared here
        this.currTopicXmlPath = myRSCManager.getCurrTopicXmlFile();
        if (currTopicXmlPath.lastIndexOf("/") >= 0) {
            this.currTopicID = currTopicXmlPath.substring(currTopicXmlPath.lastIndexOf("/") + 1, currTopicXmlPath.length() - 4);
        } else if (currTopicXmlPath.lastIndexOf("\\") >= 0) {
            this.currTopicID = currTopicXmlPath.substring(currTopicXmlPath.lastIndexOf("\\") + 1, currTopicXmlPath.length() - 4);
        }
    }

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
    // <editor-fold defaultstate="collapsed" desc="Mouse Event Action">
    // Get Caret Event when Mouse Click
    private CurrentFocusedAnchor currSCRSEName = null;
    private boolean withinTarget = false;

    private void mouseHoverCaretEvent(MouseEvent me) {
//        currSCRSEName =;
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
//            if (this.isOutgoingTAB) {
                currSCRSEName = getAnchorSENameByDot(thisCaret);
//            } else {
//                currSCRSEName = getBepSETitleByDot(thisCaret);
//            }
            if (currSCRSEName != null/* && currSCRSEName.length == 3*/) {
                withinTarget = true;
                this.topicTextPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                withinTarget = false;
                this.topicTextPane.setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    private void activateTopicPaneMouseClickEvent(MouseEvent mce) {
        this.isOutgoingTAB = Boolean.valueOf(System.getProperty(sysPropertyKey));
        Point point = mce.getPoint();
        if (mce.getButton() == MouseEvent.BUTTON1) {
            // <editor-fold defaultstate="collapsed" desc="Left-Click to Open Target Links">
            if (withinTarget) {
                logger("leftTopicAnchorClick" + "_" + currSCRSEName.screenPosStartToString() + "_" + currSCRSEName.screenPosEndToString());

                this.topicTextPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                // -------------------------------------------------------------
                // Get CURR Topic OLSEStatus before we go to NEXT Pool Anchor
                this.preTHyperOLSEStatus = CurrentFocusedAnchor.getCurrentFocusedAnchor().toArray();
                // -------------------------------------------------------------
                // 1) Highlight Anchor/BEP + Auto Scrolling
                String currAnchorO = currSCRSEName.offsetIndexToString();
                String currAnchorL = currSCRSEName.lengthIndexToString();
                String currAnchorStatus = poolerManager.getPoolAnchorStatus(this.currTopicID, new String[]{currAnchorO, currAnchorL});
                String[] scrSEPosKey = new String[]{currSCRSEName.getName(), currSCRSEName.screenPosStartToString(), currSCRSEName.screenPosEndToString(), currSCRSEName.extendedLengthToString(), currSCRSEName.screenPosStartToString(), currSCRSEName.screenPosEndToString()};
            	int anchorEnd = Integer.valueOf(scrSEPosKey[2]) + Integer.valueOf(scrSEPosKey[3]);
            	scrSEPosKey[2] = String.valueOf(anchorEnd);
//                String[] scrSEPosKey = new String[]{currSCRSEName.screenPosStartToString(), currSCRSEName.screenPosEndToString(), currSCRSEName.extendedLengthToString()};
                String[] preAnchorOLSEStatus = this.preTHyperOLSEStatus;
                String[] preAnchorSEStatus = new String[]{preAnchorOLSEStatus[2], preAnchorOLSEStatus[3], preAnchorOLSEStatus[4]};
//
                LTWAssessmentToolView.updateTopicAnchorsHighlight(this.topicTextPane, preAnchorSEStatus, scrSEPosKey, Integer.parseInt(currAnchorStatus));
//                LTWAssessmentToolView.updateTopicAnchorsHighlight(this.topicTextPane, scrSEPosKey, preAnchorSEStatus, bepLength);
                this.topicTextPane.getCaret().setDot(Integer.valueOf(currSCRSEName.screenPosEndToString()));
                this.topicTextPane.scrollRectToVisible(this.topicTextPane.getVisibleRect());
                this.topicTextPane.repaint();
                // -------------------------------------------------------------
                // 2-1) Get SCR-Anchor/BEP-SE, V(String[]{fileID, Offset})
                // 2-2) Open 1st Link
                if (this.isOutgoingTAB) {
                    // ---------------------------------------------------------
                    // Update PRE Pool Anchor Status <-- When GO NEXT Pool Anchor by AnchorText Click
                    // because the PRE-Pool_Anchor might have been completed.
                    String[] preAnchorOLSEStatusSA = this.preTHyperOLSEStatus;
                    String prePAnchorO = preAnchorOLSEStatusSA[0];
                    String prePAnchorL = preAnchorOLSEStatusSA[1];

                    String prePAnchorStatus = this.poolerManager.getPoolAnchorStatus(this.currTopicID, new String[]{prePAnchorO, prePAnchorL});
                    int unAssCounter = 0;
                    Vector<String> thisPALinkStatusV = this.poolerManager.getPoolAnchorAllLinkStatus(currTopicID, new String[]{prePAnchorO, prePAnchorL});
                    for (String linkStatus : thisPALinkStatusV) {
                        if (linkStatus.equals("0")) {
                            unAssCounter++;
                        }
                    }
                    if (unAssCounter == 0) {
                        prePAnchorStatus = this.myRSCManager.getPoolAnchorCompletedStatus(this.currTopicID, new String[]{prePAnchorO, prePAnchorL});
                    } else {
                        if (prePAnchorStatus.equals("-1")) {
                            // In this case, the assessor Right-Click to make PA NON-Relevant
                            // PA links cannot indicate the PA Status
                            prePAnchorStatus = "-1";
                        } else {
                            prePAnchorStatus = this.myRSCManager.getPoolAnchorCompletedStatus(this.currTopicID, new String[]{prePAnchorO, prePAnchorL});
                        }
                    }
                    this.pUpdater.updatePoolAnchorStatus(this.currTopicID, new String[]{prePAnchorO, prePAnchorL}, prePAnchorStatus);
                    // ---------------------------------------------------------
                    topicAnchorClickToLink();
                } else {
                    // ---------------------------------------------------------
                    // Update Pool BEP Status <-- When GO NEXT Pool BEP
                    // because the PRE-Pool_Bep might have been completed.
                    String[] preBepOLSEStatusSA = this.preTHyperOLSEStatus;
                    String prePBepO = preBepOLSEStatusSA[0];
                    String poolBepStatus = this.myRSCManager.getPoolBepCompletedStatus(this.currTopicID, prePBepO);
                    this.pUpdater.updatePoolBepStatus(this.currTopicID, prePBepO, poolBepStatus);
                    // ---------------------------------------------------------
                    topicBepClickToLink();
                }
            }
            // </editor-fold>
        } else if (mce.getButton() == MouseEvent.BUTTON3) {
            // <editor-fold defaultstate="collapsed" desc="Right-Click to Toggle Irelevance/Back">
            if (withinTarget) {
                logger("rightTopicAnchorClickToggle" + "_" + currSCRSEName.screenPosStartToString() + "_" + currSCRSEName.screenPosEndToString());

                this.preTHyperOLSEStatus = CurrentFocusedAnchor.getCurrentFocusedAnchor().toArray();
                this.topicTextPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                // 1) Check the Status of the Anchor
                // 2) Toggle the Anchor/BEP Highlight - Selected/NONRelevant
                // 3) Highlight/DIS-H Current Link Pane
                // 4) Update Result XML
                if (this.isOutgoingTAB) {
                    topicAnchorClickToToggle();
                } else {
//                    topicBepClickToToggle();
                }
            }
            // </editor-fold>
        }
    }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Activate Topic Pane TAB/TBA Mouse Listeners">
    private void topicAnchorClickToLink() {
        // <editor-fold defaultstate="collapsed" desc="Topic Anchor Click to open 1st BEP Link">
        // pAnchorOL, V<String[]{bepOffset, bepID}>
        Hashtable<String, Vector<String[]>> poolAnchorBepLinksHT = myRSCManager.getPoolAnchorBepLinksHT();
        String[] currPAnchorOLStatus = myRSCManager.getTopicAnchorOLStatusBySE(currTopicID, currSCRSEName);
        String pAnchorO = currPAnchorOLStatus[0];
        String pAnchorL = currPAnchorOLStatus[1];
        String pAnchorOL = pAnchorO + "_" + pAnchorL;
        String pAnchorStatus = this.poolerManager.getPoolAnchorStatus(currTopicID, new String[]{currPAnchorOLStatus[0], currPAnchorOLStatus[1]});
        Vector<String[]> pABepLinksVSA = poolAnchorBepLinksHT.get(pAnchorOL);
        String bepOffset = "";
        String bepID = "";
        String bepLang = "";
        String bepTitle = "";
        String[] bepInfo = pABepLinksVSA.get(0);
        bepOffset = bepInfo[0];
        bepID = bepInfo[1];
        bepLang = bepInfo[3];
        bepTitle = bepInfo[4];
        if (bepInfo[6] != pAnchorO || Integer.parseInt(bepInfo[8]) != 0)
	        for (int i = 1; i < pABepLinksVSA.size(); ++i) {
	        	bepInfo = pABepLinksVSA.get(i);
	        	if (bepInfo[6].equals(pAnchorO)) {
			        bepOffset = bepInfo[0];
			        bepID = bepInfo[1];
			        bepLang = bepInfo[3];
			        bepTitle = bepInfo[4];
			        if (bepID.endsWith("\"")) {
			            bepID = bepID.substring(0, bepID.length() - 1);
			        }
			        
			        if (Integer.parseInt(bepInfo[8]) == 0)
			        	break;
	        	}
	        }
        String bepRel = this.poolerManager.getPoolAnchorBepLinkStatus(this.currTopicID, currPAnchorOLStatus, bepID);
        String bepXmlFilePath = myRSCManager.getWikipediaFilePathByName(bepID + ".xml", bepLang);
        String bepStartp = this.poolerManager.getPoolAnchorBepLinkStartP(this.currTopicID, currPAnchorOLStatus, bepID);
        if (bepXmlFilePath.startsWith(afTasnCollectionErrors)) {
            bepXmlFilePath = myRSCManager.getErrorXmlFilePath(bepXmlFilePath);
        }
        Xml2Html xmlParser = new Xml2Html(bepXmlFilePath, Boolean.valueOf(System.getProperty(LTWAssessmentToolControler.sysPropertyIsLinkWikiKey)));
        String xmlHtmlText = xmlParser.getHtmlContent().toString();
        this.linkTextPane.setContentType(paneContentType);
        this.linkTextPane.setText(xmlHtmlText);
        this.linkTextPane.setCaretPosition(0);
        // ---------------------------------------------------------------------
        if (Integer.valueOf(pAnchorStatus) == 0) {
            if (Integer.valueOf(bepRel) == 0) {
                // The 1st Link is unassessed so NO need to find
                this.linkTextPane.setBackground(this.linkPaneWhiteColor);
                this.linkTextPane.repaint();
            } else {
                Vector<String[]> pAUNAssBLinkVSA = this.myRSCManager.getCurrPAnchorUNAssBLinkWithUpdateNAV(this.currTopicID, new String[]{pAnchorO, pAnchorL}, new String[]{bepOffset, bepID});
                String[] unAssLinkOID = pAUNAssBLinkVSA.elementAt(1);
                // new String[]{tbOffset, tbStartP, tbFileID, tbRel}
                String unAssLinkID = unAssLinkOID[2];
                String unAssLinkFPath = myRSCManager.getWikipediaFilePathByName(unAssLinkID + ".xml", AppResource.targetLang);
                if (unAssLinkFPath.startsWith(afTasnCollectionErrors)) {
                    unAssLinkFPath = myRSCManager.getErrorXmlFilePath(unAssLinkFPath);
                }
                Xml2Html unAssXmlParser = new Xml2Html(unAssLinkFPath, Boolean.valueOf(System.getProperty(LTWAssessmentToolControler.sysPropertyIsLinkWikiKey)));
                String unAssXmlHtmlText = unAssXmlParser.getHtmlContent().toString();
                this.linkTextPane.setContentType(paneContentType);
                this.linkTextPane.setText(unAssXmlHtmlText);
                this.linkTextPane.setCaretPosition(0);
                this.linkTextPane.setBackground(this.linkPaneWhiteColor);
                this.linkTextPane.repaint();
            }
        } else if (Integer.valueOf(pAnchorStatus) == 1) {
            if (Integer.valueOf(bepRel) == 1) {
                // Relevant --> Insert BEP --> BG = Green
                Vector<String> bepSCRSPoint = new Vector<String>();
                bepSCRSPoint.add(bepStartp);
                boolean isTopicBEP = false;
                updatePaneBepIcon(this.linkTextPane, bepSCRSPoint, isTopicBEP);
                this.linkTextPane.getCaret().setDot(Integer.valueOf(bepStartp));
                this.linkTextPane.scrollRectToVisible(this.linkTextPane.getVisibleRect());
                this.linkTextPane.setBackground(this.linkPaneRelColor);
            } else if (Integer.valueOf(bepRel) == -1) {
                this.linkTextPane.setBackground(this.linkPaneNonRelColor);
            } else if (Integer.valueOf(bepRel) == 0) {
                this.linkTextPane.setBackground(this.linkPaneWhiteColor);
            }
            this.linkTextPane.repaint();
        } else if (Integer.valueOf(pAnchorStatus) == -1) {
            this.linkTextPane.setBackground(this.linkPaneNonRelColor);
            this.linkTextPane.repaint();
        }
        // ---------------------------------------------------------------------
        // ---------------------------------------------------------------------
        // 3) update NAV Indices in toolResources XML
        this.myRSCManager.updateTABNavIndex(this.currTopicID, currPAnchorOLStatus, new String[]{bepOffset, bepID});
        CurrentFocusedAnchor.getCurrentFocusedAnchor().setCurrentAnchorProperty(pAnchorOL, currSCRSEName.screenPosStartToString(), currSCRSEName.screenPosEndToString(), pAnchorStatus, currSCRSEName.extendedLengthToString(), currSCRSEName.offsetIndexToString());
        // ---------------------------------------------------------------------
        String currAnchorName = this.poolerManager.getPoolAnchorNameByOL(this.currTopicID, currPAnchorOLStatus);
        Vector<String> newTABFieldValues = new Vector<String>();
        newTABFieldValues.add(this.currTopicName);
        newTABFieldValues.add(this.currTopicID);
        newTABFieldValues.add(currAnchorName);
        newTABFieldValues.add(bepID);
        String pageTitle = "";
        pageTitle = this.myRSCManager.getWikipediaPageTitle(bepID);

        newTABFieldValues.add(pageTitle.trim());
        String[] pAnchorCompletionSA = this.myRSCManager.getOutgoingCompletion();
        newTABFieldValues.add(pAnchorCompletionSA[0] + " / " + pAnchorCompletionSA[1]);
        os.setTABFieldValues(newTABFieldValues);
        // ---------------------------------------------------------------------
        // ---------------------------------------------------------------------
        // </editor-fold>
    }

    private void topicBepClickToLink() {
        // <editor-fold defaultstate="collapsed" desc="Topic BEP Click to open 1st Anchor Link">
        // Bep(Offset:1114), Vector<String[]{Offset:1538, Length:9, Name:TITLE, ID:123017, Status}+>
        HashMap<String, Vector<String[]>> bepAnchorLinksHM = poolerManager.getAnchorFileSetByBep(this.currTopicID);
        // String[]{Bep_Offset, S, Status}
        String[] currPBepOStatus = myRSCManager.getTopicBepOStatusBySE(currTopicID, currSCRSEName);
        String currPBepOffset = currPBepOStatus[0];
        String currPBepOL = currPBepOStatus[0] + "_" + String.valueOf(bepLength);
        String currPBepStatus = this.poolerManager.getPoolBepStatus(currTopicID, currPBepOffset);
        Vector<String[]> bepAnchorLinks = bepAnchorLinksHM.get(currPBepOffset);

        String anchorOffset = bepAnchorLinks.elementAt(0)[0];
        String anchorLength = bepAnchorLinks.elementAt(0)[1];
        String anchorName = bepAnchorLinks.elementAt(0)[2];
        String anchorFileID = bepAnchorLinks.elementAt(0)[3];
        String anchorStatus = this.poolerManager.getPoolBepAnchorLinkStatus(currTopicID, currPBepOffset, new String[]{anchorOffset, anchorLength, anchorFileID});
        String anchorXmlFilePath = poolerManager.getXmlFilePathByTargetID(anchorFileID, AppResource.targetLang);
        // When Errors:
        if (anchorXmlFilePath.startsWith(afTasnCollectionErrors)) {
            anchorXmlFilePath = myRSCManager.getErrorXmlFilePath(anchorXmlFilePath);
        }

        Xml2Html xmlParser = new Xml2Html(anchorXmlFilePath, Boolean.valueOf(System.getProperty(LTWAssessmentToolControler.sysPropertyIsLinkWikiKey)));
        String xmlHtmlText = xmlParser.getHtmlContent().toString();
        this.linkTextPane.setContentType(paneContentType);
        this.linkTextPane.setText(xmlHtmlText);
        this.linkTextPane.setCaretPosition(0);
        // --------------------------------------------------------- CHECK HERE
        // get SCR Anchor Offset-Length
        String[] mySCRAnchorSEPos = myFOLMatcher.getSCRAnchorNameSESA(linkTextPane, anchorFileID, new String[]{anchorOffset, anchorLength, anchorName}, AppResource.sourceLang);
        // Highlight Anchor Txt in JTextPane
        updateLinkAnchorHighlight(linkTextPane, mySCRAnchorSEPos);
        // ---------------------------------------------------------
        // Renew Link Text Pane Listener for Detecting Anchor Text & Right/Left Click
        this.linkTextPane.getCaret().setDot(Integer.valueOf(mySCRAnchorSEPos[1].trim()));
        this.linkTextPane.scrollRectToVisible(this.linkTextPane.getVisibleRect());

        if (Integer.valueOf(currPBepStatus) == 1 || Integer.valueOf(currPBepStatus) == 0) {
            if (Integer.valueOf(anchorStatus) == 1) {
                this.linkTextPane.setBackground(this.linkPaneRelColor);
            } else if (Integer.valueOf(anchorStatus) == -1) {
                this.linkTextPane.setBackground(this.linkPaneNonRelColor);
            } else if (Integer.valueOf(anchorStatus) == 0) {
                this.linkTextPane.setBackground(this.linkPaneWhiteColor);
            }

        } else if (Integer.valueOf(currPBepStatus) == -1) {
            this.linkTextPane.setBackground(this.linkPaneNonRelColor);
        }

        this.linkTextPane.repaint();
        // ---------------------------------------------------------
        this.myRSCManager.updateTBANavIndex(currTopicID, currPBepOffset, new String[]{anchorOffset, anchorLength, anchorFileID});
//        System.setProperty(sysPropertyCurrTopicOLSEStatusKey, currPBepOL + "_" + currSCRSEName.screenPosStartToString() + "_" + currSCRSEName.screenPosEndToString() + "_" + currPBepStatus);
        CurrentFocusedAnchor.getCurrentFocusedAnchor().setCurrentAnchorProperty(currPBepOL, currSCRSEName.screenPosStartToString(), currSCRSEName.screenPosEndToString(), currPBepStatus, currSCRSEName.extendedLengthToString(), currSCRSEName.offsetIndexToString());
        // ---------------------------------------------------------------------
        Vector<String> newTABFieldValues = new Vector<String>();
        newTABFieldValues.add(this.currTopicName);
        newTABFieldValues.add(this.currTopicID);
        newTABFieldValues.add(anchorName);
        newTABFieldValues.add(anchorFileID);
        String pageTitle = "";
//        if (Boolean.valueOf(System.getProperty(sysPropertyIsLinkWikiKey))) {
            pageTitle = this.myRSCManager.getWikipediaPageTitle(anchorFileID);
//        } else {
//            pageTitle = this.myRSCManager.getTeAraFilePathByName(anchorFileID);
//        }

        newTABFieldValues.add(pageTitle.trim());
        String[] pAnchorCompletionSA = this.myRSCManager.getIncomingCompletion();
        newTABFieldValues.add(pAnchorCompletionSA[0] + " / " + pAnchorCompletionSA[1]);
        os.setTABFieldValues(newTABFieldValues);
        // </editor-fold>
    }

    private void topicAnchorClickToToggle() {
        // <editor-fold defaultstate="collapsed" desc="Topic Anchor Click to toggle btw NONRel & Back">
        // ---------------------------------------------------------------------
        // GET CURR-Clicked Anchor + BEP Data
        // pAnchorOL, V<String[]{bepOffset, bepID}>
        Hashtable<String, Vector<String[]>> poolAnchorBepLinksHT = myRSCManager.getPoolAnchorBepLinksHT();
        String[] currPAnchorOLStatus = myRSCManager.getTopicAnchorOLStatusBySE(currTopicID, currSCRSEName);
        String pAnchorO = currPAnchorOLStatus[0];
        String pAnchorL = currPAnchorOLStatus[1];
        String pAnchorS = currSCRSEName.screenPosStartToString();
        String pAnchorE = currSCRSEName.screenPosEndToString();
        String pAnchorOL = pAnchorO + "_" + pAnchorL;
        String pAnchorStatus = this.poolerManager.getPoolAnchorStatus(currTopicID, new String[]{pAnchorO, pAnchorL});

        Vector<String[]> pABepLinksVSA = poolAnchorBepLinksHT.get(pAnchorOL);
        String bepOffset = pABepLinksVSA.elementAt(0)[0];
        String bepID = pABepLinksVSA.elementAt(0)[1];
        String bepRel = this.poolerManager.getPoolAnchorBepLinkStatus(this.currTopicID, currPAnchorOLStatus, bepID);
        String bepXmlFilePath = poolerManager.getXmlFilePathByTargetID(bepID, AppResource.targetLang);
        String bepStartp = this.poolerManager.getPoolAnchorBepLinkStartP(this.currTopicID, currPAnchorOLStatus, bepID);

        // In the case of Errors --> Refresh Link Pane
        if (bepXmlFilePath.startsWith(afTasnCollectionErrors)) {
            bepXmlFilePath = myRSCManager.getErrorXmlFilePath(bepXmlFilePath);
        }
// =====================================================================
// CHECK: Pre-Highlight-Anchor might be the Curr-Anchor?

        String[] preAnchorOLSEStatusSA = this.preTHyperOLSEStatus;
        String prePAnchorO = preAnchorOLSEStatusSA[0];
        String prePAnchorL = preAnchorOLSEStatusSA[1];
        String prePAnchorS = preAnchorOLSEStatusSA[2];
        String prePAnchorE = preAnchorOLSEStatusSA[3];
        String prePAnchorStatus = preAnchorOLSEStatusSA[4];
        String prePAnchorSE = prePAnchorS + "_" + prePAnchorE;
        // ---------------------------------------------------------------------
//        Xml2Html xmlParser = new Xml2Html(bepXmlFilePath, Boolean.valueOf(System.getProperty(sysPropertyIsLinkWikiKey)));
//        String xmlHtmlText = xmlParser.getHtmlContent().toString();
//        this.linkTextPane.setContentType(paneContentType);
//        this.linkTextPane.setText(xmlHtmlText);
//        this.linkTextPane.setCaretPosition(0);
        // =====================================================================
        if (pAnchorS.equals(prePAnchorS)) {
            log("CURR PAnchor is PRE PAnchor...");
            // CURR PAnchor is PRE PAnchor
            // -----------------------------------------------------------------
            // Topic Pane
            // DO-NOTHING for Topic Pane Anchor
            // It is still highlighted as YELLOW
            // -----------------------------------------------------------------
            if (prePAnchorStatus.equals("1") || prePAnchorStatus.equals("0")) {
                // <editor-fold defaultstate="collapsed" desc="Toggle to NONRelevant -1">
                // Toggle to NONRelevant -1, because it was 1 or 0
                String toPAnchorStatus = "-1";
                // Set Link Pane BG as RED
                this.linkTextPane.setBackground(this.linkPaneNonRelColor);
                this.linkTextPane.repaint();
                // -----------------------------------------------------------------
                // update Completion Ratio
                int newCompletedCounter = 0;
                Vector<String> pAnchorAllLinkStatus = this.poolerManager.getPoolAnchorAllLinkStatus(this.currTopicID, new String[]{pAnchorO, pAnchorL});
                for (String pAnchorLinkStatus : pAnchorAllLinkStatus) {
                    if (pAnchorLinkStatus.equals("0")) {
                        newCompletedCounter++;
                    }

                }
                String[] outCompletionRatio = this.myRSCManager.getOutgoingCompletion();
                String outCompletedLinks = String.valueOf(Integer.valueOf(outCompletionRatio[0]) + newCompletedCounter);
                this.myRSCManager.updateOutgoingCompletion(outCompletedLinks + " : " + outCompletionRatio[1]);
                // updare Pool XML
                this.pUpdater.updatePoolAnchorStatus(this.currTopicID, currPAnchorOLStatus, toPAnchorStatus);
                // update System Property
                CurrentFocusedAnchor.getCurrentFocusedAnchor().setCurrentAnchorProperty(pAnchorOL, currSCRSEName.screenPosStartToString(), currSCRSEName.screenPosEndToString(), pAnchorStatus, currSCRSEName.extendedLengthToString(), currSCRSEName.offsetIndexToString());
                // -------------------------------------------------------------
                // -------------------------------------------------------------
                String currAnchorName = this.poolerManager.getPoolAnchorNameByOL(this.currTopicID, currPAnchorOLStatus);
                Vector<String> newTABFieldValues = new Vector<String>();
                newTABFieldValues.add(this.currTopicName);
                newTABFieldValues.add(this.currTopicID);
                newTABFieldValues.add(currAnchorName);
                newTABFieldValues.add(bepID);
                String pageTitle = this.myRSCManager.getWikipediaPageTitle(bepID);
                newTABFieldValues.add(pageTitle.trim());
                newTABFieldValues.add(outCompletedLinks + " / " + outCompletionRatio[1]);
                os.setTABFieldValues(newTABFieldValues);
                // </editor-fold>
            } else if (prePAnchorStatus.equals("-1")) {
                // <editor-fold defaultstate="collapsed" desc="Toggle to PRE-STATUS: 1 or 0">
                // Toggle to PRE-STATUS: 1 or 0
                // b) Set Link Pane BG back to Pre-Status
                if (Integer.valueOf(bepRel) == 1) {
                    this.linkTextPane.setBackground(this.linkPaneRelColor);
                } else if (Integer.valueOf(bepRel) == -1) {
                    this.linkTextPane.setBackground(this.linkPaneNonRelColor);
                } else if (Integer.valueOf(bepRel) == 0) {
                    this.linkTextPane.setBackground(this.linkPaneWhiteColor);
                }

                this.linkTextPane.repaint();
                // -------------------------------------------------------------
                // update Completion Ratio
                int unAssCounter = 0;
                int nonRelCounter = 0;
                Vector<String> pAnchorAllLinkStatus = this.poolerManager.getPoolAnchorAllLinkStatus(this.currTopicID, new String[]{pAnchorO, pAnchorL});
                for (String pAnchorLinkStatus : pAnchorAllLinkStatus) {
                    if (pAnchorLinkStatus.equals("0")) {
                        unAssCounter++;
                    } else if (pAnchorLinkStatus.equals("-1")) {
                        nonRelCounter++;
                    }

                }
                String toPAnchorStatus = "";
                if (nonRelCounter == pAnchorAllLinkStatus.size()) {
                    toPAnchorStatus = "-1";
                } else if (unAssCounter > 0) {
                    toPAnchorStatus = "0";
                } else {
                    toPAnchorStatus = "1";
                }

                String[] outCompletionRatio = this.myRSCManager.getOutgoingCompletion();
                String outCompletedLinks = String.valueOf(Integer.valueOf(outCompletionRatio[0]) - unAssCounter);
                this.myRSCManager.updateOutgoingCompletion(outCompletedLinks + " : " + outCompletionRatio[1]);
                // updare Pool XML
                this.pUpdater.updatePoolAnchorStatus(this.currTopicID, currPAnchorOLStatus, toPAnchorStatus);
                // update System Property
                CurrentFocusedAnchor.getCurrentFocusedAnchor().setCurrentAnchorProperty(pAnchorOL, currSCRSEName.screenPosStartToString(), currSCRSEName.screenPosEndToString(), pAnchorStatus, currSCRSEName.extendedLengthToString(), currSCRSEName.offsetIndexToString());
                // -------------------------------------------------------------
                // -------------------------------------------------------------
                String currAnchorName = this.poolerManager.getPoolAnchorNameByOL(this.currTopicID, currPAnchorOLStatus);
                Vector<String> newTABFieldValues = new Vector<String>();
                newTABFieldValues.add(this.currTopicName);
                newTABFieldValues.add(this.currTopicID);
                newTABFieldValues.add(currAnchorName);
                newTABFieldValues.add(bepID);
                String pageTitle = this.myRSCManager.getWikipediaPageTitle(bepID);
                newTABFieldValues.add(pageTitle.trim());
                newTABFieldValues.add(outCompletedLinks + " / " + outCompletionRatio[1]);
                os.setTABFieldValues(newTABFieldValues);
                // </editor-fold>
            }
            
        } else {
            log("CURR PAnchor is NOT PRE PAnchor...");
            // CURR PAnchor is NOT PRE PAnchor
            // -----------------------------------------------------------------
            // Topic Pane
            // re-Store PRE PAnchor
            // Highlight CURR PAnchor as YELLOW
            toggleTopicAnchorColor(new String[]{prePAnchorS, prePAnchorE, prePAnchorStatus}, new String[]{pAnchorS, pAnchorE});
            this.topicTextPane.getCaret().setDot(Integer.valueOf(currSCRSEName.screenPosEndToString()));
            this.topicTextPane.scrollRectToVisible(this.topicTextPane.getVisibleRect());
            this.topicTextPane.repaint();
            // -----------------------------------------------------------------
            Xml2Html xmlParser = new Xml2Html(bepXmlFilePath, Boolean.valueOf(System.getProperty(LTWAssessmentToolControler.sysPropertyIsLinkWikiKey)));
            String xmlHtmlText = xmlParser.getHtmlContent().toString();
            this.linkTextPane.setContentType(paneContentType);
            this.linkTextPane.setText(xmlHtmlText);
            this.linkTextPane.setCaretPosition(0);
            if (Integer.valueOf(bepRel) == 1) {
                Vector<String> bepSCRSPoint = new Vector<String>();
                bepSCRSPoint.add(bepStartp);
                boolean isTopicBEP = false;
                updatePaneBepIcon(this.linkTextPane, bepSCRSPoint, isTopicBEP);
                this.linkTextPane.getCaret().setDot(Integer.valueOf(bepStartp));
                this.linkTextPane.scrollRectToVisible(this.linkTextPane.getVisibleRect());
            }

            this.linkTextPane.repaint();
            // -----------------------------------------------------------------
            if (pAnchorStatus.equals("1") || pAnchorStatus.equals("0")) {
                // <editor-fold defaultstate="collapsed" desc="Toggle to NONRelevant -1">
                // Toggle to NONRelevant -1, because it was 1 or 0
                String toPAnchorStatus = "-1";
                // -------------------------------------------------------------
                // Set Link Pane BG as RED
                this.linkTextPane.setBackground(this.linkPaneNonRelColor);
                this.linkTextPane.repaint();
                // -----------------------------------------------------------------
                // update Completion Ratio
                int newCompletedCounter = 0;
                Vector<String> pAnchorAllLinkStatus = this.poolerManager.getPoolAnchorAllLinkStatus(this.currTopicID, new String[]{pAnchorO, pAnchorL});
                for (String pAnchorLinkStatus : pAnchorAllLinkStatus) {
                    if (pAnchorLinkStatus.equals("0")) {
                        newCompletedCounter++;
                    }

                }
                String[] outCompletionRatio = this.myRSCManager.getOutgoingCompletion();
                String outCompletedLinks = String.valueOf(Integer.valueOf(outCompletionRatio[0]) + newCompletedCounter);
                this.myRSCManager.updateOutgoingCompletion(outCompletedLinks + " : " + outCompletionRatio[1]);
                // updare Pool XML
                log("Before Update POOL: " + pAnchorO + " - " + pAnchorL + " - " + toPAnchorStatus);
                this.pUpdater.updatePoolAnchorStatus(this.currTopicID, new String[]{pAnchorO, pAnchorL}, toPAnchorStatus);
                // update System Property
                CurrentFocusedAnchor.getCurrentFocusedAnchor().setCurrentAnchorProperty(pAnchorOL, currSCRSEName.screenPosStartToString(), currSCRSEName.screenPosEndToString(), pAnchorStatus, currSCRSEName.extendedLengthToString(), currSCRSEName.offsetIndexToString());
                // -------------------------------------------------------------
                // -------------------------------------------------------------
                String currAnchorName = this.poolerManager.getPoolAnchorNameByOL(this.currTopicID, currPAnchorOLStatus);
                Vector<String> newTABFieldValues = new Vector<String>();
                newTABFieldValues.add(this.currTopicName);
                newTABFieldValues.add(this.currTopicID);
                newTABFieldValues.add(currAnchorName);
                newTABFieldValues.add(bepID);
                String pageTitle = this.myRSCManager.getWikipediaPageTitle(bepID);
                newTABFieldValues.add(pageTitle.trim());
                newTABFieldValues.add(outCompletedLinks + " / " + outCompletionRatio[1]);
                os.setTABFieldValues(newTABFieldValues);
                // </editor-fold>
            } else if (pAnchorStatus.equals("-1")) {
                // <editor-fold defaultstate="collapsed" desc="Toggle to PRE-STATUS: 1 or 0">
                // Toggle to PRE-STATUS: 1 or 0 or -1
                int unAssCounter = 0;
                int nonRelCounter = 0;
                Vector<String> pAnchorAllLinkStatus = this.poolerManager.getPoolAnchorAllLinkStatus(this.currTopicID, new String[]{pAnchorO, pAnchorL});
                for (String pAnchorLinkStatus : pAnchorAllLinkStatus) {
                    if (pAnchorLinkStatus.equals("0")) {
                        unAssCounter++;
                    } else if (pAnchorLinkStatus.equals("-1")) {
                        nonRelCounter++;
                    }

                }
                String toPAnchorStatus = "";
                if (nonRelCounter == pAnchorAllLinkStatus.size()) {
                    toPAnchorStatus = "-1";
                } else if (unAssCounter > 0) {
                    toPAnchorStatus = "0";
                } else {
                    toPAnchorStatus = "1";
                }
// -------------------------------------------------------------

                if (unAssCounter > 0) {
                    // if (unAssCounter > 0) --> Go to 1st UN-Assessed Link
                    // ---------------------------------------------------------
                    // RETURN V[String[]{anchor_O, L}, String[]{BepLink_O, ID}]
                    Vector<String[]> nextUnAssTAB = this.myRSCManager.getCurrPAnchorUNAssBLinkWithUpdateNAV(this.currTopicID, new String[]{pAnchorO, pAnchorL}, new String[]{bepOffset, bepID});
                    String[] unAssBEPOID = nextUnAssTAB.elementAt(1);
                    String unAssBepID = unAssBEPOID[2];
                    String unAssBepFilePath = poolerManager.getXmlFilePathByTargetID(unAssBepID, AppResource.targetLang);
                    if (unAssBepFilePath.startsWith(afTasnCollectionErrors)) {
                        unAssBepFilePath = myRSCManager.getErrorXmlFilePath(unAssBepFilePath);
                    }

                    Xml2Html thisXmlParser = new Xml2Html(unAssBepFilePath, Boolean.valueOf(System.getProperty(LTWAssessmentToolControler.sysPropertyIsLinkWikiKey)));
                    String thisXmlHtmlText = thisXmlParser.getHtmlContent().toString();
                    this.linkTextPane.setContentType(paneContentType);
                    this.linkTextPane.setText(thisXmlHtmlText);
                    this.linkTextPane.setCaretPosition(0);
                    // ---------------------------------------------------------
                } else {
                    if (bepRel.equals("1")) {
                        this.linkTextPane.setBackground(this.linkPaneRelColor);
                    } else if (bepRel.equals("-1")) {
                        this.linkTextPane.setBackground(this.linkPaneNonRelColor);
                    }

                }
                this.linkTextPane.repaint();
                // -------------------------------------------------------------
                String[] outCompletionRatio = this.myRSCManager.getOutgoingCompletion();
                String outCompletedLinks = String.valueOf(Integer.valueOf(outCompletionRatio[0]) - unAssCounter);
                this.myRSCManager.updateOutgoingCompletion(outCompletedLinks + " : " + outCompletionRatio[1]);
                // updare Pool XML
                this.pUpdater.updatePoolAnchorStatus(this.currTopicID, currPAnchorOLStatus, toPAnchorStatus);
                // update System Property
                CurrentFocusedAnchor.getCurrentFocusedAnchor().setCurrentAnchorProperty(pAnchorOL, currSCRSEName.screenPosStartToString(), currSCRSEName.screenPosEndToString(), pAnchorStatus, currSCRSEName.extendedLengthToString(), currSCRSEName.offsetIndexToString());
                // -------------------------------------------------------------
                // -------------------------------------------------------------
                String currAnchorName = this.poolerManager.getPoolAnchorNameByOL(this.currTopicID, currPAnchorOLStatus);
                Vector<String> newTABFieldValues = new Vector<String>();
                newTABFieldValues.add(this.currTopicName);
                newTABFieldValues.add(this.currTopicID);
                newTABFieldValues.add(currAnchorName);
                newTABFieldValues.add(bepID);
                String pageTitle = this.myRSCManager.getWikipediaPageTitle(bepID);
                newTABFieldValues.add(pageTitle.trim());
                newTABFieldValues.add(outCompletedLinks + " / " + outCompletionRatio[1]);
                os.setTABFieldValues(newTABFieldValues);
                // </editor-fold>
            }

        }
        
        LTWAssessmentToolControler.getInstance().goNextLink(false, true);
        // </editor-fold>
    }

    private void topicBepClickToToggle() {
        // <editor-fold defaultstate="collapsed" desc="Topic BEP Click to toggle btw NONRel & Back">
        // 1) Check the Status of the BEP
        // 2) Toggle Curr-BEP Highlight + Back-BEP Pre-Anchor
        // 3) Toggle Link Pane to Status
        // 4) Update Pool XML + Resource XML
        // ---------------------------------------------------------------------
        // Bep(Offset:1114), Vector<String[]{Offset:1538, Length:9, Name:TITLE, ID:123017, Status}+>
        HashMap<String, Vector<String[]>> bepAnchorLinksHM = poolerManager.getAnchorFileSetByBep(this.currTopicID);
        // String[]{Bep_Offset, S, Status}
        String[] currPBepOStatus = myRSCManager.getTopicBepOStatusBySE(currTopicID, currSCRSEName);
        String currPBepOffset = currPBepOStatus[0];
        String currPBepStatus = this.poolerManager.getPoolBepStatus(currTopicID, currPBepOffset);
        String pBEPOL = currPBepOffset + "_" + String.valueOf(bepLength);

        Vector<String[]> bepAnchorLinkVSA = bepAnchorLinksHM.get(currPBepOffset);
        String anchorOffset = bepAnchorLinkVSA.elementAt(0)[0];
        String anchorLength = bepAnchorLinkVSA.elementAt(0)[1];
        String anchorName = bepAnchorLinkVSA.elementAt(0)[2];
        String anchorFileID = bepAnchorLinkVSA.elementAt(0)[3];
        String anchorStatus = this.poolerManager.getPoolBepAnchorLinkStatus(currTopicID, currPBepOffset, new String[]{anchorOffset, anchorLength, anchorFileID});
        String anchorXmlFilePath = poolerManager.getXmlFilePathByTargetID(anchorFileID, AppResource.targetLang);
        // In the case of Errors --> Refresh Link Pane
        // populate Link Pane Content
        if (anchorXmlFilePath.startsWith(afTasnCollectionErrors)) {
            anchorXmlFilePath = myRSCManager.getErrorXmlFilePath(anchorXmlFilePath);
        }

        Xml2Html xmlParser = new Xml2Html(anchorXmlFilePath, Boolean.valueOf(System.getProperty(LTWAssessmentToolControler.sysPropertyIsLinkWikiKey)));
        String xmlHtmlText = xmlParser.getHtmlContent().toString();
        this.linkTextPane.setContentType(paneContentType);
        this.linkTextPane.setText(xmlHtmlText);
        this.linkTextPane.setCaretPosition(0);
        // ---------------------------------------------------------------------
        // =====================================================================
        // CHECK: Pre-BEP might be the Curr-BEP?
        String[] preBepOLSEStatusSA = this.preTHyperOLSEStatus;
        String prePBepS = preBepOLSEStatusSA[2];
        String prePBepE = preBepOLSEStatusSA[3];
        String prePBepStatus = preBepOLSEStatusSA[4];
        String prePBepSE = prePBepS + "_" + prePBepE;
        //
        if (Integer.valueOf(currPBepStatus) == 1 || Integer.valueOf(currPBepStatus) == 0) {
            // Toggle to NONRelevant -1, because it was 1 or 0
            // -----------------------------------------------------------------
            // <editor-fold defaultstate="collapsed" desc="Toggle to NONRelevant -1">
            // a) highlight toggled Anchor & back-highlight Pre-Anchor
            // turn currPAnchor to RED
            String scrSEPosKey = currSCRSEName.screenPosStartToString() + "_" + currSCRSEName.screenPosEndToString();
            String toBepStatus = "-1";
            if (prePBepSE.equals(scrSEPosKey)) {
                // DO-NOTHING for Topic Pane BEP
            } else {
                String currBepStatus = "0";
                toggleTopicBepIcon(scrSEPosKey, currBepStatus);
                this.topicTextPane.getCaret().setDot(Integer.valueOf(currSCRSEName.screenPosEndToString()));
                this.topicTextPane.scrollRectToVisible(this.topicTextPane.getVisibleRect());
                this.topicTextPane.repaint();
                // + back-highlight Pre-PoolAnchor
                restorePrePoolBepIcon(prePBepS, prePBepStatus);
                // change INFO
                Vector<String> newTABFieldValues = new Vector<String>();
                newTABFieldValues.add(this.currTopicName);
                newTABFieldValues.add(this.currTopicID);
                newTABFieldValues.add(anchorName);
                newTABFieldValues.add(anchorFileID);
                String pageTitle = "";
//                if (Boolean.valueOf(System.getProperty(sysPropertyIsLinkWikiKey))) {
                    pageTitle = this.myRSCManager.getWikipediaPageTitle(anchorFileID);
//                } else {
//                    pageTitle = this.myRSCManager.getTeAraFilePathByName(anchorFileID);
//                }

                newTABFieldValues.add(pageTitle.trim());
                String[] pAnchorCompletionSA = this.myRSCManager.getIncomingCompletion();
                newTABFieldValues.add(pAnchorCompletionSA[0] + " / " + pAnchorCompletionSA[1]);
                os.setTABFieldValues(newTABFieldValues);
            }
// -----------------------------------------------------------------
// b) highlight link anchor --> Set Link Pane BG as RED

            String[] linkAnchorOLName = new String[]{anchorOffset, anchorLength, anchorName};
            // getSCRAnchorPosSA return String[]{Anchor_Name, StartP, EndP}
            String[] linkAnchorSESA = this.myFOLMatcher.getSCRAnchorNameSESA(this.linkTextPane, anchorFileID, linkAnchorOLName, AppResource.targetLang);
//            this.updateLinkAnchorHighlight(this.linkTextPane, linkAnchorSESA);
            this.linkTextPane.getCaret().setDot(Integer.valueOf(linkAnchorSESA[2]));
            this.linkTextPane.scrollRectToVisible(this.linkTextPane.getVisibleRect());
            this.linkTextPane.setBackground(this.linkPaneNonRelColor);
            this.linkTextPane.repaint();
            // </editor-fold>
            // -----------------------------------------------------------------
            // updare Pool XML
            this.pUpdater.updatePoolBepStatus(this.currTopicID, currPBepOffset, toBepStatus);
//            System.setProperty(sysPropertyCurrTopicOLSEStatusKey, pBEPOL + "_" + currSCRSEName.screenPosStartToString() + "_" + currSCRSEName.screenPosEndToString() + "_" + toBepStatus);
            CurrentFocusedAnchor.getCurrentFocusedAnchor().setCurrentAnchorProperty(pBEPOL, currSCRSEName.screenPosStartToString(), currSCRSEName.screenPosEndToString(), toBepStatus, currSCRSEName.extendedLengthToString(), currSCRSEName.offsetIndexToString());
        } else if (Integer.valueOf(currPBepStatus) == -1) {
            // Toggle to PRE-STATUS: -1, 1 or 0
            // -----------------------------------------------------------------
            // <editor-fold defaultstate="collapsed" desc="Toggle to PRE-STATUS: -1, 1 or 0">
            // a) Check Curr-Anchor Pre-Status:
            String scrSEPosKey = currSCRSEName.screenPosStartToString() + "_" + currSCRSEName.screenPosEndToString();
            if (prePBepSE.equals(scrSEPosKey)) {
                // DO-NOTHING for Topic Pane BEP
            } else {
                String currBepStatus = "0";
                this.toggleTopicBepIcon(scrSEPosKey, currBepStatus);
                this.topicTextPane.getCaret().setDot(Integer.valueOf(currSCRSEName.screenPosEndToString()));
                this.topicTextPane.scrollRectToVisible(this.topicTextPane.getVisibleRect());
                this.topicTextPane.repaint();
                // + back-highlight Pre-PoolAnchor
                restorePrePoolBepIcon(prePBepS, prePBepStatus);
                // change INFO
                Vector<String> newTABFieldValues = new Vector<String>();
                newTABFieldValues.add(this.currTopicName);
                newTABFieldValues.add(this.currTopicID);
                newTABFieldValues.add(anchorName);
                newTABFieldValues.add(anchorFileID);
                String pageTitle = "";
//                if (Boolean.valueOf(System.getProperty(sysPropertyIsLinkWikiKey))) {
                    pageTitle = this.myRSCManager.getWikipediaPageTitle(anchorFileID);
//                } else {
//                    pageTitle = this.myRSCManager.getTeAraFilePathByName(anchorFileID);
//                }

                newTABFieldValues.add(pageTitle.trim());
                String[] pAnchorCompletionSA = this.myRSCManager.getIncomingCompletion();
                newTABFieldValues.add(pAnchorCompletionSA[0] + " / " + pAnchorCompletionSA[1]);
                os.setTABFieldValues(newTABFieldValues);
            }
// -----------------------------------------------------------------
// b) Set Link Pane BG back to Pre-Status

            String[] linkAnchorOLName = new String[]{anchorOffset, anchorLength, anchorName};
            // getSCRAnchorPosSA return String[]{Anchor_Name, StartP, EndP}
            String[] linkAnchorSESA = this.myFOLMatcher.getSCRAnchorNameSESA(this.linkTextPane, anchorFileID, linkAnchorOLName, AppResource.targetLang);
            this.updateLinkAnchorHighlight(this.linkTextPane, linkAnchorSESA);
            this.linkTextPane.getCaret().setDot(Integer.valueOf(linkAnchorSESA[2]));
            this.linkTextPane.scrollRectToVisible(this.linkTextPane.getVisibleRect());
            if (Integer.valueOf(anchorStatus) == 1) {
                this.linkTextPane.setBackground(this.linkPaneRelColor);
            } else if (Integer.valueOf(anchorStatus) == -1) {
                this.linkTextPane.setBackground(this.linkPaneNonRelColor);
            } else if (Integer.valueOf(anchorStatus) == 0) {
                this.linkTextPane.setBackground(this.linkPaneWhiteColor);
            }

            this.linkTextPane.repaint();
            // </editor-fold>
            // -----------------------------------------------------------------
            int nonRelCounter = 0;
            String currBepPreStatus = "1";
            for (int i = 0; i <
                    bepAnchorLinkVSA.size(); i++) {
                String[] thisBEPLinksSA = bepAnchorLinkVSA.elementAt(i);
                String thisLinkAnchorStatus = thisBEPLinksSA[4];
                if (Integer.valueOf(thisLinkAnchorStatus) == 0 || thisLinkAnchorStatus.equals("")) {
                    currBepPreStatus = "0";
                } else if (Integer.valueOf(thisLinkAnchorStatus) == -1) {
                    nonRelCounter++;
                }

            }
            if (Integer.valueOf(currBepPreStatus) != 0) {
                if (nonRelCounter == bepAnchorLinkVSA.size()) {
                    currBepPreStatus = "-1";
                }

            }
            // updare Pool XML
            this.pUpdater.updatePoolBepStatus(this.currTopicID, currPBepOffset, currBepPreStatus);
            CurrentFocusedAnchor.getCurrentFocusedAnchor().setCurrentAnchorProperty(pBEPOL, currSCRSEName.screenPosStartToString(), currSCRSEName.screenPosEndToString(), currBepPreStatus, currSCRSEName.extendedLengthToString(), currSCRSEName.offsetIndexToString());
//            System.setProperty(sysPropertyCurrTopicOLSEStatusKey, pBEPOL + "_" + currSCRSEName.screenPosStartToString() + "_" + currSCRSEName.screenPosEndToString() + "_" + currBepPreStatus);
        }
// </editor-fold>

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Utilities: UPDATE & GET">
    private void updateLinkAnchorHighlight(JTextPane linkPane, String[] anchorSEPosSA) {
        // This might need to handle: isAssessment
        // 1) YES:
        // 2) NO: ONLY highlight Anchor Text
        try {
            Highlighter txtPaneHighlighter = linkPane.getHighlighter();
            int[] achorSCRPos = new int[]{Integer.valueOf(anchorSEPosSA[1]), Integer.valueOf(anchorSEPosSA[2])};
            Object aHighlightRef = txtPaneHighlighter.addHighlight(achorSCRPos[0], achorSCRPos[1], painters.getAnchorPainter());
        } catch (BadLocationException ex) {
            Logger.getLogger(topicPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    private void updateTopicAnchorsHighlight(JTextPane topicPane, String[] currAnchorSE, String[] preAnchorSEStatus) {
//        // This might need to handle: isAssessment
//        // 1) YES: Highlight Curr Selected Anchor Text, keep others remaining as THEIR Colors
//        // 2) NO: Highlight Curr Selected Anchor Text, keep others remaining as Anchor Text Color
//        try {
//            boolean preDONEFlag = false;
//            boolean currDONEFlag = false;
//            Highlighter txtPaneHighlighter = topicPane.getHighlighter();
//            Highlight[] highlights = txtPaneHighlighter.getHighlights();
//            Object anchorHighlightRef = null;
//            int[] achorSCRPos = new int[]{Integer.valueOf(currAnchorSE[0]), Integer.valueOf(currAnchorSE[1])};
//            for (int i = 0; i <
//                    highlights.length; i++) {
//                int sPos = highlights[i].getStartOffset();
//                int ePos = highlights[i].getEndOffset();
//                if (achorSCRPos[0] == sPos && achorSCRPos[1] == ePos) {
//                    txtPaneHighlighter.removeHighlight(highlights[i]);
//                    anchorHighlightRef =
//                            txtPaneHighlighter.addHighlight(sPos, ePos, painters.getSelectedPainter());
//                    topicPane.repaint();
//                    currDONEFlag =
//                            true;
//                    if (currDONEFlag && preDONEFlag) {
//                        break;
//                    }
//
//                } else {
//                    if (Integer.valueOf(preAnchorSEStatus[0]) == sPos && Integer.valueOf(preAnchorSEStatus[1]) == ePos) {
//                        txtPaneHighlighter.removeHighlight(highlights[i]);
//                        String pAnchorStatus = preAnchorSEStatus[2];
//                        if (Integer.valueOf(pAnchorStatus) == 1) {
//                            anchorHighlightRef = txtPaneHighlighter.addHighlight(sPos, ePos, painters.getCompletePainter());
//                        } else if (Integer.valueOf(pAnchorStatus) == -1) {
//                            anchorHighlightRef = txtPaneHighlighter.addHighlight(sPos, ePos, painters.getIrrelevantPainter());
//                        } else {
//                            anchorHighlightRef = txtPaneHighlighter.addHighlight(sPos, ePos, painters.getAnchorPainter());
//                        }
//
//                        topicPane.repaint();
//                        preDONEFlag =
//                                true;
//                        if (currDONEFlag && preDONEFlag) {
//                            break;
//                        }
//
//
//                    }
//
//                }
//            }
//        } catch (BadLocationException ex) {
//            Logger.getLogger(topicPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

    private void updatePaneBepIcon(JTextPane txtPane, Vector<String> bepSCROffset, boolean isTopicBEP) {
        // TODO: "isHighlighBEP"
        // 1) YES: Remove previous Highlight BEPs + Make a new Highlight BEP
        // 2) NO: INSERT BEP ICONs for this Topic
        try {
            // Get Pre-BEP data
            String[] preBepOLSEStatusSA = this.preTHyperOLSEStatus;
            String prePBepO = preBepOLSEStatusSA[0];
            String prePBepL = preBepOLSEStatusSA[1];
            String prePBepS = preBepOLSEStatusSA[2];
            String prePBepE = preBepOLSEStatusSA[3];
            String prePBepStatus = preBepOLSEStatusSA[4];
            // set up BEP Icon
            StyledDocument styDoc = (StyledDocument) txtPane.getDocument();

            Style bepHStyle = styDoc.addStyle("bepHIcon", null);
            StyleConstants.setIcon(bepHStyle, new ImageIcon(bepHighlightIconImageFilePath));
            Style bepCStyle = styDoc.addStyle("bepCIcon", null);
            StyleConstants.setIcon(bepCStyle, new ImageIcon(bepCompletedIconImageFilePath));
            Style bepNStyle = styDoc.addStyle("bepNIcon", null);
            StyleConstants.setIcon(bepNStyle, new ImageIcon(bepNonrelevantIconImageFilePath));
            Style bepStyle = styDoc.addStyle("bepIcon", null);
            StyleConstants.setIcon(bepStyle, new ImageIcon(bepIconImageFilePath));

            if (isTopicBEP) {
                // PRE-BEP Icon back to its ORIGINAL
                if (prePBepStatus.equals("0")) {
                    styDoc.remove(Integer.valueOf(prePBepS), bepLength);
                    styDoc.insertString(Integer.valueOf(prePBepS), "TBEP", bepStyle);
                } else if (prePBepStatus.equals("1")) {
                    styDoc.remove(Integer.valueOf(prePBepS), bepLength);
                    styDoc.insertString(Integer.valueOf(prePBepS), "CBEP", bepCStyle);
                } else if (prePBepStatus.equals("-1")) {
                    styDoc.remove(Integer.valueOf(prePBepS), bepLength);
                    styDoc.insertString(Integer.valueOf(prePBepS), "NBEP", bepNStyle);
                }

// Curr Highlight BEP Icon
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
        // Bep Offset, SP
        Vector<String> currBepSCRPos = myRSCManager.getTopicBepsOSVS();
        // -----------------------------------------------------------------
        String[] thisBepSet = null;
        for (String thisBepOS : currBepSCRPos) {
            String[] aSet = thisBepOS.split(" : ");
            int aStartPoint = Integer.valueOf(aSet[1]);
            int aEndPoint = Integer.valueOf(aSet[1]) + 4;
            if (aStartPoint <= thisdot && thisdot <= aEndPoint) {
                this.topicTextPane.setSelectionStart(Integer.valueOf(aStartPoint));
                this.topicTextPane.setSelectionEnd(Integer.valueOf(aEndPoint));
                currBepSCRTitle =
                        "TBEP";
                return thisBepSet = new String[]{String.valueOf(aStartPoint), String.valueOf(aEndPoint), currBepSCRTitle};
            }

        }
        return thisBepSet;
    }

    private CurrentFocusedAnchor getAnchorSENameByDot(int thisdot) {
//        String[] selectedAnchorSEName = null;
        Vector<String[]> currAnchorSCRPos = new Vector<String[]>();
        CurrentFocusedAnchor selectedAnchor = null;
        Vector<String> currAnchorOLSet = myRSCManager.getTopicAnchorsOLNameSEV();
        for (String thisAnchorSet : currAnchorOLSet) {
            String[] thisAnchorSA = thisAnchorSet.split(" : ");
            currAnchorSCRPos.add(new String[]{thisAnchorSA[2], thisAnchorSA[3], thisAnchorSA[4]});

        	int offset = Integer.valueOf(thisAnchorSA[0]);
        	int length = Integer.valueOf(thisAnchorSA[1]);
        	
        	// this is screen pos
            int aStartPoint = Integer.valueOf(thisAnchorSA[3]);
            int aEndPoint = Integer.valueOf(thisAnchorSA[4]);
            int extLength = Integer.valueOf(thisAnchorSA[5]);
            
            if (extLength > 0)
            	aEndPoint += extLength;
            if (aStartPoint <= thisdot && thisdot <= aEndPoint) {
            	selectedAnchor = new CurrentFocusedAnchor();
                this.topicTextPane.setSelectionStart(Integer.valueOf(aStartPoint));
                this.topicTextPane.setSelectionEnd(Integer.valueOf(aEndPoint));
                currAnchorSCRName =
                        thisAnchorSA[2].toString().trim();
                currAnchorSCRName =
                        this.topicTextPane.getSelectedText();
//                return selectedAnchorSEName = new String[]{String.valueOf(aStartPoint), String.valueOf(aEndPoint), currAnchorSCRName, thisAnchorSA[5]};
//                selectedAnchorSEName = new String[]{thisAnchorSA[3], thisAnchorSA[4], currAnchorSCRName, thisAnchorSA[5], thisAnchorSA[0], thisAnchorSA[1]};
//                                                      screenOffset    screenEnd        screenAnchorName,   extension length  offset      offset length
                
                selectedAnchor.setCurrentAnchorProperty(offset, length, aStartPoint, aEndPoint, 0, extLength);
                
                selectedAnchor.setName(currAnchorSCRName);
//                selectedAnchor.setScreenPosEnd();
//                CurrentFocusedAnchor.getCurrentFocusedAnchor().setIndexOffset(Integer.parseInt(thisAnchorSA[3]), Integer.parseInt(thisAnchorSA[4]));
                break;
            }

        }
        // ---------------------------------------------------------------------
//        String[] thisAnchorSet = null;
//        for (String[] thisSA : currAnchorSCRPos) {
//            String[] aSet = thisSA;
//            int aStartPoint = Integer.valueOf(aSet[1]);
//            int aEndPoint = Integer.valueOf(aSet[2]);
//            if (aStartPoint <= thisdot && thisdot <= aEndPoint) {
//                this.topicTextPane.setSelectionStart(Integer.valueOf(aStartPoint));
//                this.topicTextPane.setSelectionEnd(Integer.valueOf(aEndPoint));
//                currAnchorSCRName = aSet[0].toString().trim();
//                return thisAnchorSet = new String[]{String.valueOf(aStartPoint), String.valueOf(aEndPoint), currAnchorSCRName};
//            }
//        }
        return selectedAnchor; //.toArray(); //selectedAnchorSEName;
    }

    private void toggleTopicAnchorColor(String[] prePAnchorSEStatus, String[] currPAnchorSE) {
        try {
            boolean preDONEFlag = false;
            boolean currDONEFlag = false;
            Highlighter txtPaneHighlighter = this.topicTextPane.getHighlighter();
            Highlight[] highlights = txtPaneHighlighter.getHighlights();

            Object anchorHighlightRef = null;
            int[] currAnchorSE = new int[]{Integer.valueOf(currPAnchorSE[0]), Integer.valueOf(currPAnchorSE[1])};

            for (int i = 0; i <
                    highlights.length; i++) {
                int sPos = highlights[i].getStartOffset();
                int ePos = highlights[i].getEndOffset();
                if (currAnchorSE[0] == sPos && currAnchorSE[1] == ePos) {
                    txtPaneHighlighter.removeHighlight(highlights[i]);
                    anchorHighlightRef =
                            txtPaneHighlighter.addHighlight(sPos, ePos, painters.getSelectedPainter());
                    this.topicTextPane.repaint();
                    currDONEFlag =
                            true;
                    if (currDONEFlag && preDONEFlag) {
                        break;
                    }

                } else {
                    int[] preAnchorSE = new int[]{Integer.valueOf(prePAnchorSEStatus[0]), Integer.valueOf(prePAnchorSEStatus[1])};
                    if (preAnchorSE[0] == sPos && preAnchorSE[1] == ePos) {
                        txtPaneHighlighter.removeHighlight(highlights[i]);
                        String preAnchorStatus = prePAnchorSEStatus[2];
                        if (preAnchorStatus.equals("0")) {
                            anchorHighlightRef = txtPaneHighlighter.addHighlight(sPos, ePos, painters.getAnchorPainter());
                        } else if (preAnchorStatus.equals("1")) {
                            anchorHighlightRef = txtPaneHighlighter.addHighlight(sPos, ePos, painters.getCompletePainter());
                        } else if (preAnchorStatus.equals("-1")) {
                            anchorHighlightRef = txtPaneHighlighter.addHighlight(sPos, ePos, painters.getIrrelevantPainter());
                        }

                        this.topicTextPane.repaint();
                        preDONEFlag =
                                true;
                        if (currDONEFlag && preDONEFlag) {
                            break;
                        }


                    }

                }
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(topicPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void restorePrePoolAnchorHighlight(String prePAnchorS, String prePAnchorE, String prePAnchorStatus) {
        try {
            Highlighter txtPaneHighlighter = this.topicTextPane.getHighlighter();
            Highlight[] highlights = txtPaneHighlighter.getHighlights();
            Object anchorHighlightRef = null;
            int[] preAnchorSEPos = new int[]{Integer.valueOf(prePAnchorS), Integer.valueOf(prePAnchorE)};
            for (int i = 0; i <
                    highlights.length; i++) {
                int sPos = highlights[i].getStartOffset();
                int ePos = highlights[i].getEndOffset();
                if (preAnchorSEPos[0] == sPos && preAnchorSEPos[1] == ePos) {
                    txtPaneHighlighter.removeHighlight(highlights[i]);
                    if (Integer.valueOf(prePAnchorStatus) == 0) {
                        anchorHighlightRef = txtPaneHighlighter.addHighlight(sPos, ePos, painters.getAnchorPainter());
                    } else if (Integer.valueOf(prePAnchorStatus) == 1) {
                        anchorHighlightRef = txtPaneHighlighter.addHighlight(sPos, ePos, painters.getCompletePainter());
                    } else if (Integer.valueOf(prePAnchorStatus) == -1) {
                        anchorHighlightRef = txtPaneHighlighter.addHighlight(sPos, ePos, painters.getIrrelevantPainter());
                    }


                }

            }
        } catch (BadLocationException ex) {
            Logger.getLogger(topicPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void toggleTopicBepIcon(String scrSEPosKey, String toBepStatus) {
        // change that position BEP to "toBepStatus"
        try {
            String[] scrSESA = scrSEPosKey.split("_");
            String scrStartP = scrSESA[0].trim();
            StyledDocument styDoc = (StyledDocument) this.topicTextPane.getDocument();
            Style bepNStyle = styDoc.addStyle("bepHIcon", null);
            StyleConstants.setIcon(bepNStyle, new ImageIcon(bepNonrelevantIconImageFilePath));
            styDoc.remove(Integer.valueOf(scrStartP), bepLength);
            styDoc.insertString(Integer.valueOf(scrStartP), "NBEP", bepNStyle);
        } catch (BadLocationException ex) {
            Logger.getLogger(topicPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.topicTextPane.repaint();
    }

    private void restorePrePoolBepIcon(String prePBepS, String prePBepStatus) {
        // change that position BEP to "toBepStatus"
        try {
            String scrStartP = prePBepS.trim();
            StyledDocument styDoc = (StyledDocument) this.topicTextPane.getDocument();
            if (Integer.valueOf(prePBepStatus) == 0) {
                Style bepTStyle = styDoc.addStyle("bepTIcon", null);
                StyleConstants.setIcon(bepTStyle, new ImageIcon(bepIconImageFilePath));
                styDoc.remove(Integer.valueOf(scrStartP), bepLength);
                styDoc.insertString(Integer.valueOf(scrStartP), "TBEP", bepTStyle);
            } else if (Integer.valueOf(prePBepStatus) == 1) {
                Style bepCStyle = styDoc.addStyle("bepCIcon", null);
                StyleConstants.setIcon(bepCStyle, new ImageIcon(bepCompletedIconImageFilePath));
                styDoc.remove(Integer.valueOf(scrStartP), bepLength);
                styDoc.insertString(Integer.valueOf(scrStartP), "CBEP", bepCStyle);
            } else if (Integer.valueOf(prePBepStatus) == -1) {
                Style bepNStyle = styDoc.addStyle("bepHIcon", null);
                StyleConstants.setIcon(bepNStyle, new ImageIcon(bepNonrelevantIconImageFilePath));
                styDoc.remove(Integer.valueOf(scrStartP), bepLength);
                styDoc.insertString(Integer.valueOf(scrStartP), "NBEP", bepNStyle);
            }

            this.topicTextPane.repaint();
        } catch (BadLocationException ex) {
            Logger.getLogger(topicPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
// </editor-fold>
}

















