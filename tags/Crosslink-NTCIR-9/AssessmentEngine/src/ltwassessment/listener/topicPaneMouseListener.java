package ltwassessment.listener;

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

import ltwassessment.AppResource;
import ltwassessment.assessment.AssessedAnchor;
import ltwassessment.assessment.AssessmentThread;
import ltwassessment.assessment.Bep;
import ltwassessment.assessment.CurrentFocusedAnchor;
import ltwassessment.assessment.IndexedAnchor;
import ltwassessment.assessment.LTWAssessmentToolControler;
import ltwassessment.parsers.FOLTXTMatcher;
import ltwassessment.parsers.Xml2Html;
import ltwassessment.parsers.PoolerManager;
import ltwassessment.parsers.ResourcesManager;
import ltwassessment.utility.ObservableSingleton;
import ltwassessment.utility.highlightPainters;
import ltwassessment.utility.PoolUpdater;
import ltwassessment.view.TopicHighlightManager;

/**
 * @author Darren HUANG
 */
public class topicPaneMouseListener implements MouseInputListener {

    // Constant Variables
    protected final int bepLength = 4;
    private final String sysPropertyKey = "isTABKey";
    private AssessedAnchor preTHyperOLSEStatus = null;
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
    private ObservableSingleton os = null;
    private Hashtable<String, Object> myTAnchorSEHiObj = new Hashtable<String, Object>();
    private Vector<String> currAnchorOLSet = null;
    
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
        org.jdesktop.application.ResourceMap resourceMap = AppResource.getInstance().getResourceMap();
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
        
        currAnchorOLSet = myRSCManager.getTopicAnchorsOLNameSEV();
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
    private IndexedAnchor currSCRSEName = null;
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
                this.preTHyperOLSEStatus = CurrentFocusedAnchor.getCurrentFocusedAnchor().getAnchor(); //.toArray();
                
                AssessedAnchor next = null;
                if (preTHyperOLSEStatus.getParent() == currSCRSEName) {
                	next = (AssessedAnchor) preTHyperOLSEStatus.getParent().getNext(preTHyperOLSEStatus, false);
                }
                else {
					next = (AssessedAnchor) currSCRSEName.getChildrenAnchors().get(0);
                }
	                // -------------------------------------------------------------
	                // 1) Highlight Anchor/BEP + Auto Scrolling
//	                String currAnchorO = currSCRSEName.offsetIndexToString();
//	                String currAnchorL = currSCRSEName.lengthIndexToString();
//                if (currSCRSEName != preTHyperOLSEStatus.getParent()) {
////	                String currAnchorStatus = poolerManager.getPoolAnchorStatus(this.currTopicID, currSCRSEName/*new String[]{currAnchorO, currAnchorL}*/);
////	                currSCRSEName.setStatus(Integer.parseInt(currAnchorStatus));
//	
//	                int prePAnchorStatus = Integer.parseInt(this.poolerManager.getPoolAnchorStatus(this.currTopicID, this.preTHyperOLSEStatus.getParent()/*new String[]{prePAnchorO, prePAnchorL}*/));
////	                this.preTHyperOLSEStatus.getParent().setStatus(prePAnchorStatus);
//	                int unAssCounter = 0;
//	                Vector<String> thisPALinkStatusV = this.poolerManager.getPoolAnchorAllLinkStatus(currTopicID, this.preTHyperOLSEStatus.getParent()/*new String[]{prePAnchorO, prePAnchorL}*/);
//	                for (String linkStatus : thisPALinkStatusV) {
//	                    if (linkStatus.equals("0")) {
//	                        unAssCounter++;
//	                    }
//	                }
//	                if (unAssCounter == 0) {
//	                    prePAnchorStatus = this.myRSCManager.getPoolAnchorCompletedStatus(this.currTopicID, this.preTHyperOLSEStatus.getParent()/*new String[]{prePAnchorO, prePAnchorL}*/);
//	                } else {
//	                    if (prePAnchorStatus != -1) {
//	                        // In this case, the assessor Right-Click to make PA NON-Relevant
//	                        // PA links cannot indicate the PA Status
//	//                            prePAnchorStatus = "-1";
//	//                        } else {
//	                        prePAnchorStatus = this.myRSCManager.getPoolAnchorCompletedStatus(this.currTopicID, this.preTHyperOLSEStatus.getParent()/*new String[]{prePAnchorO, prePAnchorL}*/);
//	                    }
//	                }
//	                this.preTHyperOLSEStatus.getParent().setStatus(prePAnchorStatus);
//	                this.pUpdater.updatePoolAnchorStatus(this.currTopicID, this.preTHyperOLSEStatus.getParent());
////	                preTHyperOLSEStatus.setStatus(prePAnchorStatus);
//                }
                    // ---------------------------------------------------------                   

                topicAnchorClickToLink(next);
                
//                LTWAssessmentToolView.updateTopicAnchorsHighlight(this.topicTextPane, preTHyperOLSEStatus/*preAnchorSEStatus*/, next, Integer.parseInt(currAnchorStatus));
//                TopicHighlightManager.getInstance().update(preTHyperOLSEStatus, next);
                
            }
            // </editor-fold>
        } else if (mce.getButton() == MouseEvent.BUTTON3) {
            // <editor-fold defaultstate="collapsed" desc="Right-Click to Toggle Irelevance/Back">
            if (withinTarget) {
                logger("rightTopicAnchorClickToggle" + "_" + currSCRSEName.screenPosStartToString() + "_" + currSCRSEName.screenPosStartToString());

                this.preTHyperOLSEStatus = CurrentFocusedAnchor.getCurrentFocusedAnchor().getAnchor(); //.toArray();
                this.topicTextPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                // 1) Check the Status of the Anchor
                // 2) Toggle the Anchor/BEP Highlight - Selected/NONRelevant
                // 3) Highlight/DIS-H Current Link Pane
                // 4) Update Result XML
//                if (this.isOutgoingTAB) {
                    topicAnchorClickToToggle(mce.isControlDown());
//                } else {
//                    topicBepClickToToggle();
//                }
            }
            // </editor-fold>
        }
    }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Activate Topic Pane TAB/TBA Mouse Listeners">
    private void topicAnchorClickToLink(AssessedAnchor next) {
        // <editor-fold defaultstate="collapsed" desc="Topic Anchor Click to open 1st BEP Link">
        // pAnchorOL, V<String[]{bepOffset, bepID}>
//        Hashtable<String, Vector<Bep>> poolAnchorBepLinksHT = myRSCManager.getPoolAnchorBepLinksHashtable();

//        String pAnchorStatus = this.poolerManager.getPoolAnchorStatus(currTopicID, currSCRSEName/*new String[]{currPAnchorOLStatus[0], currPAnchorOLStatus[1]}*/);

    	int i = 0;
    	Bep bepInfo = next.getBeps().get(i++);
    	while (bepInfo.getRel() != 0 && i < next.getBeps().size()) {
    		bepInfo = next.getBeps().get(i++);
    	}
    	
    	
//    	int bepRel = bepInfo.getRel();
//		//    	String bepID = bepInfo.getFileId();
////        String bepRel = this.poolerManager.getPoolAnchorBepLinkStatus(this.currTopicID, currSCRSEName, bepID);
////        String bepXmlFilePath = myRSCManager.getWikipediaFilePathByName(bepID + ".xml", bepInfo.getTargetLang());
////        String bepStartp = this.poolerManager.getPoolAnchorBepLinkStartP(this.currTopicID, currSCRSEName, bepID);
////        if (bepXmlFilePath.startsWith(afTasnCollectionErrors)) {
////            bepXmlFilePath = myRSCManager.getErrorXmlFilePath(bepXmlFilePath);
////        }
////        Xml2Html xmlParser = new Xml2Html(bepXmlFilePath, Boolean.valueOf(System.getProperty(LTWAssessmentToolControler.sysPropertyIsLinkWikiKey)));
////        String xmlHtmlText = xmlParser.getHtmlContent().toString();
////        this.linkTextPane.setContentType(paneContentType);
////        this.linkTextPane.setText(xmlHtmlText);
////        this.linkTextPane.setCaretPosition(0);
//        // ---------------------------------------------------------------------
//        if (Integer.valueOf(pAnchorStatus) == 0) {
//            if (bepRel  == 0) {
//                // The 1st Link is unassessed so NO need to find
////                this.linkTextPane.setBackground(this.linkPaneWhiteColor);
////                this.linkTextPane.repaint();
//            } else {
//                bepInfo = this.myRSCManager.getNextUNAssTABWithUpdateNAV(this.currTopicID, currSCRSEName.getChildrenAnchors().get(0), bepInfo); //getCurrPAnchorUNAssBLinkWithUpdateNAV(this.currTopicID, new String[]{pAnchorO, pAnchorL}, new String[]{bepOffset, bepID});
////                String[] unAssLinkOID = pAUNAssBLinkVSA.elementAt(1);
//                // new String[]{tbOffset, tbStartP, tbFileID, tbRel}
////                String unAssLinkID = bepInfo.getFileId(); //unAssLinkOID[2];
////                String unAssLinkFPath = myRSCManager.getWikipediaFilePathByName(unAssLinkID + ".xml", AppResource.targetLang);
////                if (unAssLinkFPath.startsWith(afTasnCollectionErrors)) {
////                    unAssLinkFPath = myRSCManager.getErrorXmlFilePath(unAssLinkFPath);
////                }
////                Xml2Html unAssXmlParser = new Xml2Html(unAssLinkFPath, Boolean.valueOf(System.getProperty(LTWAssessmentToolControler.sysPropertyIsLinkWikiKey)));
////                String unAssXmlHtmlText = unAssXmlParser.getHtmlContent().toString();
////                this.linkTextPane.setContentType(paneContentType);
////                this.linkTextPane.setText(unAssXmlHtmlText);
////                this.linkTextPane.setCaretPosition(0);
////                this.linkTextPane.setBackground(this.linkPaneWhiteColor);
////                this.linkTextPane.repaint();
//            }
//        } 
//        else if (Integer.valueOf(pAnchorStatus) == 1) {
//            if (Integer.valueOf(bepRel) == 1) {
//                // Relevant --> Insert BEP --> BG = Green
//                Vector<String> bepSCRSPoint = new Vector<String>();
//                bepSCRSPoint.add(bepStartp);
//                boolean isTopicBEP = false;
//                updatePaneBepIcon(this.linkTextPane, bepSCRSPoint, isTopicBEP);
//                this.linkTextPane.getCaret().setDot(Integer.valueOf(bepStartp));
//                this.linkTextPane.scrollRectToVisible(this.linkTextPane.getVisibleRect());
//                this.linkTextPane.setBackground(this.linkPaneRelColor);
//            } else if (Integer.valueOf(bepRel) == -1) {
//                this.linkTextPane.setBackground(this.linkPaneNonRelColor);
//            } else if (Integer.valueOf(bepRel) == 0) {
//                this.linkTextPane.setBackground(this.linkPaneWhiteColor);
//            }
//            this.linkTextPane.repaint();
//        } else if (Integer.valueOf(pAnchorStatus) == -1) {
//            this.linkTextPane.setBackground(this.linkPaneNonRelColor);
//            this.linkTextPane.repaint();
//        }
        // ---------------------------------------------------------------------
        // ---------------------------------------------------------------------
        // 3) update NAV Indices in toolResources XML
        this.myRSCManager.updateTABNavIndex(this.currTopicID, bepInfo.getAssociatedAnchor().getParent(), bepInfo);
        CurrentFocusedAnchor.getCurrentFocusedAnchor().setAnchor(preTHyperOLSEStatus, (AssessedAnchor)bepInfo.getAssociatedAnchor(), bepInfo);
//        CurrentFocusedAnchor.getCurrentFocusedAnchor().setCurrentAnchorProperty(pAnchorO, pAnchorOL, currSCRSEName.screenPosStartToString(), currSCRSEName.screenPosStartToString(), pAnchorStatus, currSCRSEName.extendedLengthToString()/*, currSCRSEName.offsetIndexToString()*/);
        // ---------------------------------------------------------------------

//        os.setTABFieldValues(bepInfo);
        // ---------------------------------------------------------------------
        // ---------------------------------------------------------------------
        // </editor-fold>
    }

    private void topicAnchorClickToToggle(boolean ctrlKeyDown) {
    	if (!AssessmentThread.isReadyForNextLink()) {
    		return;
    	}
    	AssessmentThread.setCtrlKeyDown(ctrlKeyDown);
        // ---------------------------------------------------------------------
        if (preTHyperOLSEStatus.getParent() == currSCRSEName) {
            log("CURR PAnchor is PRE PAnchor...");
            // CURR PAnchor is PRE PAnchor     
            AssessmentThread.setProcessingAnchor(preTHyperOLSEStatus);
            AssessmentThread.setTask(AssessmentThread.EVENT_SET_SUBANCHOR_IRRELEVANT);
//            LTWAssessmentToolControler.getInstance().setSubanchorIrrelevant(preTHyperOLSEStatus);
//            LTWAssessmentToolControler.getInstance().goNextLink(true, true);
            
        } else {
            log("CURR PAnchor is NOT PRE PAnchor...");
            
            AssessmentThread.setProcessingAnchor((AssessedAnchor) currSCRSEName.getChildrenAnchors().firstElement());
            AssessmentThread.setTask(AssessmentThread.EVENT_SET_SUBANCHORS_IRRELEVANT);
            // CURR PAnchor is NOT PRE PAnchor
            // -----------------------------------------------------------------
            // Topic Pane
            // re-Store PRE PAnchor
            // Originally, "Highlight CURR PAnchor as YELLOW", but we don't need that
            // -----------------------------------------------------------------
//            for (AssessedAnchor subanchor : currSCRSEName.getChildrenAnchors())
//            	LTWAssessmentToolControler.getInstance().setSubanchorIrrelevant(subanchor);
//            
//            currSCRSEName.statusCheck();
//    		TopicHighlightManager.getInstance().update(currSCRSEName);
        }
        
        // </editor-fold>
    }

    private void updatePaneBepIcon(JTextPane txtPane, Vector<String> bepSCROffset, boolean isTopicBEP) {
        // TODO: "isHighlighBEP"
        // 1) YES: Remove previous Highlight BEPs + Make a new Highlight BEP
        // 2) NO: INSERT BEP ICONs for this Topic
        try {
            // Get Pre-BEP data
//            String[] preBepOLSEStatusSA = this.preTHyperOLSEStatus;
//            String prePBepO = preBepOLSEStatusSA[0];
//            String prePBepL = preBepOLSEStatusSA[1];
//            String prePBepS = preBepOLSEStatusSA[2];
//            String prePBepE = preBepOLSEStatusSA[3];
            String prePBepO = this.preTHyperOLSEStatus.offsetToString(); //preAnchorOLSEStatusSA[0];
            String prePBepL = this.preTHyperOLSEStatus.lengthToString(); //preAnchorOLSEStatusSA[1];
            String prePBepS = this.preTHyperOLSEStatus.screenPosStartToString(); //preAnchorOLSEStatusSA[2];
            String prePBepE = this.preTHyperOLSEStatus.screenPosEndToString(); //preAnchorOLSEStatusSA[3];
            String prePBepStatus = this.preTHyperOLSEStatus.statusToString(); //preAnchorOLSEStatusSA[4];
//            String prePAnchorSE = prePAnchorS + "_" + prePAnchorE;
//            String prePBepStatus = preBepOLSEStatusSA[4];
            
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

    private IndexedAnchor getAnchorSENameByDot(int thisdot) {
//        String[] selectedAnchorSEName = null;
        Vector<String[]> currAnchorSCRPos = new Vector<String[]>();
        IndexedAnchor hoveredAnchor = null;
        
        for (int i = 0; i < currAnchorOLSet.size(); ++i) {
        	String thisAnchorSet = currAnchorOLSet.get(i);
            String[] thisAnchorSA = thisAnchorSet.split(" : ");
            currAnchorSCRPos.add(new String[]{thisAnchorSA[2], thisAnchorSA[3], thisAnchorSA[4]});

        	int offset = Integer.valueOf(thisAnchorSA[0]);
        	int length = Integer.valueOf(thisAnchorSA[1]);
        	
        	// this is screen pos
            int aStartPoint = Integer.valueOf(thisAnchorSA[3]);
            int aEndPoint = Integer.valueOf(thisAnchorSA[4]);
            int extLength = 0;
            
            if (thisAnchorSA[5] != null && !thisAnchorSA[5].equals("null"))
            	extLength = Integer.valueOf(thisAnchorSA[5]);
            
            if (extLength > 0)
            	aEndPoint += extLength;
            if (aStartPoint <= thisdot && thisdot <= aEndPoint) {
            	
                this.topicTextPane.setSelectionStart(Integer.valueOf(aStartPoint));
                this.topicTextPane.setSelectionEnd(Integer.valueOf(aEndPoint));
                currAnchorSCRName =
                        thisAnchorSA[2].toString().trim();
                currAnchorSCRName =
                        this.topicTextPane.getSelectedText();
                hoveredAnchor = myRSCManager.getBepsPoolByAnchor(i);
//                return hoveredAnchorSEName = new String[]{String.valueOf(aStartPoint), String.valueOf(aEndPoint), currAnchorSCRName, thisAnchorSA[5]};
//                hoveredAnchorSEName = new String[]{thisAnchorSA[3], thisAnchorSA[4], currAnchorSCRName, thisAnchorSA[5], thisAnchorSA[0], thisAnchorSA[1]};
//                                                      screenOffset    screenEnd        screenAnchorName,   extension length  offset      offset length
                
//                hoveredAnchor = new IndexedAnchor();
//                hoveredAnchor.setCurrentAnchorProperty(offset, length, aStartPoint, aEndPoint, 0, extLength);
//                
//                hoveredAnchor.setName(currAnchorSCRName);
                
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
        return hoveredAnchor; //.toArray(); //selectedAnchorSEName;
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

















