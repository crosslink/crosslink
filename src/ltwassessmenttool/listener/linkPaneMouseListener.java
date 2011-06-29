package ltwassessmenttool.listener;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.event.MouseInputListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import ltwassessmenttool.LTWAssessmentToolControler;
import ltwassessmenttool.LTWAssessmentToolView;
import ltwassessment.AppResource;
import ltwassessment.assessment.AssessedAnchor;
import ltwassessment.assessment.Bep;
import ltwassessment.assessment.CurrentFocusedAnchor;
import ltwassessment.assessment.IndexedAnchor;
import ltwassessment.parsers.FOLTXTMatcher;
import ltwassessment.parsers.Xml2Html;
import ltwassessment.parsers.PoolerManager;
import ltwassessment.parsers.ResourcesManager;
import ltwassessment.utility.BrowserControl;
import ltwassessment.utility.ObservableSingleton;
import ltwassessment.utility.highlightPainters;
import ltwassessment.utility.PoolUpdater;
import ltwassessment.utility.tabTxtPaneManager;
import ltwassessment.utility.tbaTxtPaneManager;

/**
 * @author Darren HUANG
 */
public class linkPaneMouseListener implements MouseInputListener {


    // Declare global variables
    protected final int bepLength = 4;
    private final String sysPropertyKey = "isTABKey";

    private boolean isTAB = false;
    highlightPainters painters;
    private JTextPane myTopicPane;
    private JTextPane myLinkPane;
    // import External Classes
    private ResourcesManager myRSCManager;
    private FOLTXTMatcher myFolMatcher;
    private PoolUpdater myPoolUpdater;
    private PoolerManager myPoolManager;
    private tabTxtPaneManager myTABTxtPaneManager;
    private tbaTxtPaneManager myTBATxtPaneManager;
    private String topicID = "";
    private String currTopicName = "";
    private String paneContentType = "";
    private String afTasnCollectionErrors = "";
    private String bepIconImageFilePath = "";
    private String bepHighlightIconImageFilePath = "";
    private String bepCompletedIconImageFilePath = "";
    private String bepNonrelevantIconImageFilePath = "";
//    private Color linkPaneWhiteColor = Color.WHITE;
//    private Color linkPaneRelColor = new Color(168, 232, 177);
//    private Color linkPaneNonRelColor = new Color(255, 183, 165);

    private Hashtable<String, String[]> topicAnchorOLTSENHT = new Hashtable<String, String[]>();
    private Hashtable<String, Object> myTAnchorSEHiObj = new Hashtable<String, Object>();
    
    private boolean readyForNextLink = false;
    

    private void log(String txt) {
        System.out.println(txt);
    }

    private void logger(Object aObj) {
        try {
            String targetFile = "resources" + File.separator + "Pool" + File.separator + "T" + this.topicID + "_ltwAssessTool2009.log";
            File poolFileWithName = new File(targetFile);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
			        new FileOutputStream(poolFileWithName), "UTF8")); //new BufferedWriter(new FileWriter(poolFileWithName, true));

            DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
            Date date = new Date();
            String currentDateTime = dateFormat.format(date).toString();

            String logStr = this.topicID + " : " + currentDateTime + " : " + aObj.toString();
            bw.write(logStr);
            bw.newLine();

            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(linkPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public linkPaneMouseListener(JTextPane myTopicPane, JTextPane myLinkPane) {
        this.myTopicPane = myTopicPane;
        this.myLinkPane = myLinkPane;

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ltwassessmenttool.LTWAssessmentToolApp.class).getContext().getResourceMap(LTWAssessmentToolView.class);
        afTasnCollectionErrors = resourceMap.getString("AssFormXml.taskCollectionError");
        bepIconImageFilePath = resourceMap.getString("bepIcon.imageFilePath");
        bepHighlightIconImageFilePath = resourceMap.getString("bepHighlightIcon.imageFilePath");
        bepCompletedIconImageFilePath = resourceMap.getString("bepCompletedIcon.imageFilePath");
        bepNonrelevantIconImageFilePath = resourceMap.getString("bepNonrelevantIcon.imageFilePath");
        paneContentType = resourceMap.getString("html.content.type");

        myRSCManager = ResourcesManager.getInstance();
        myFolMatcher = FOLTXTMatcher.getInstance();
        myPoolManager = PoolerManager.getInstance();
        myPoolUpdater = myPoolManager.getPoolUpdater();
        myTABTxtPaneManager = new tabTxtPaneManager();
        myTBATxtPaneManager = new tbaTxtPaneManager();

        // ---------------------------------------------------------------------
        // In case:
        // Outgoing Anchor TXT: scrSE, String[]{O,L,TXT,S,E,num}
        // Incoming BEP: scrS, String[]{O,S, num}
//        this.topicAnchorOLTSENHT = topicOLTSEIndex;
        // ---------------------------------------------------------------------

        this.painters = new highlightPainters();

        this.topicID = myRSCManager.getTopicID();
        Vector<String[]> topicIDNameVSA = this.myPoolManager.getAllTopicsInPool();
        this.currTopicName = topicIDNameVSA.elementAt(0)[1].trim();
    }

    public synchronized boolean isReadyForNextLink() {
		return readyForNextLink;
	}

	public synchronized void setReadyForNextLink(boolean readyForNextLink) {
		this.readyForNextLink = readyForNextLink;
	}

	// <editor-fold defaultstate="collapsed" desc="Mouse Events">
    public void mouseClicked(MouseEvent me) {
        /**
         * CASE 1: Left-Click -> RELEVANT + NEXT
         * CASE 2: Double-Left-Click -> Change BEP Position
         * CASE 3: Right-Click: IRRELEVANT + NEXT
         */
        this.isTAB = Boolean.valueOf(System.getProperty(sysPropertyKey));
        activateMouseClickEvent(me);
    }

    public void mousePressed(MouseEvent e) {
        if (SINGLECLICK) {
            SINGLECLICK = false;
            singleLeftClickEventAction();
            IGNORE = false;
        }
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
    // <editor-fold defaultstate="collapsed" desc="Right/Left Mouse Events: Next/Previous">
    private Timer timer;
    private boolean IGNORE = false;
    private boolean SINGLECLICK = false;

    private void activateMouseClickEvent(MouseEvent mce) {
        int noOfClicks = mce.getClickCount();
        int button = mce.getButton();
        timer = new Timer();
        if (noOfClicks > 2) {
            return;
        } else if (noOfClicks == 2) {
            if (button == 1) {
                // ------ Double-Left-Click Event: Specify a BEP ---------------
                IGNORE = true; // to stop single click from running
                // -------------------------------------------------------------
                // 1) get the NEW BEP Start Point & Offset after Double-Click
                // 2) Update Link BEP: Add NEW + Remove OLD
                // 2) BG --> Green
                // 3) Update bepLink: SP, Offset, Status-Rel
                // 4) STAY, NOT Go Next
                doubleLeftClickEventAction();
                Thread.yield();
            }
        } else if (noOfClicks == 1) {
            if (button == 1) {
                // ----------- Single-Left-Click Event: Go NEXT --------
                IGNORE = false;
                timer.schedule(new timerTask(), 500);
            } else if (button == 3) {
                // --- Single-Right-Click Event: Irrelevance + Go NEXT -
                // Switch to "IRRELEVANT"
                // 1) BG --> Red
                // 2) Update bepLink: SP, Offset, Status-Rel
                // 3) GO NEXT
                singleRightClickEventAction();
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Utility: Link Pane Mouse-Click">
    @SuppressWarnings("static-access")
    private void singleRightClickEventAction() {
        // NON_Relevant Link
        // 1) BG Colour to RED
        // 2) Update Topic Anchor Link Status to -1
        // 3) GO NEXT
//        if (this.isTAB) {
            // For outgoing TAB
            // -----------------------------------------------------------------
//            IndexedAnchor currAnchorOLNameStatusSA = this.myRSCManager.getCurrTopicAnchorOLNameStatusSA();
//            Bep currALinkOIDSA = this.myRSCManager.getCurrTopicATargetOID(this.myLinkPane, this.topicID);
//            String currALinkID = currALinkOIDSA.getFileId(); //[1];
//            logger("outgoing_singleRightClick_" + currAnchorOLNameStatusSA.offsetToString() + "-" + currAnchorOLNameStatusSA.lengthToString() + " --> " + currALinkID);
        Bep currentLink = CurrentFocusedAnchor.getCurrentFocusedAnchor().getCurrentBep();
            // -----------------------------------------------------------------
            String currALinkStatus = this.myPoolManager.getPoolAnchorBepLinkStatus(topicID, currentLink);
            if (currALinkStatus.equals("0")) {
                String[] outCompletion = this.myRSCManager.getOutgoingCompletion();
                String completedLinkN = outCompletion[0];
                String totalLinkN = outCompletion[1];
                completedLinkN = String.valueOf(Integer.valueOf(completedLinkN) + 1);
                this.myRSCManager.updateOutgoingCompletion(completedLinkN + " : " + totalLinkN);
            }
            // -----------------------------------------------------------------
//            this.myLinkPane.setBackground(this.linkPaneNonRelColor);
//            this.myLinkPane.repaint();

            currentLink.setRel(Bep.IRRELEVANT);
            String currLinkStatus = currentLink.relString();
            this.myPoolUpdater.updateTopicAnchorLinkRel(topicID, currentLink);
            // -----------------------------------------------------------------
            AssessedAnchor previous =  currentLink.getAssociatedAnchor();
            Bep link = LTWAssessmentToolControler.getInstance().goNextLink(true, true);
            CurrentFocusedAnchor.getCurrentFocusedAnchor().setAnchor(previous, link.getAssociatedAnchor(), link);
    }
    
    private void updateRelevantCompletion(Bep link) {
//        String currALinkStatus = this.myPoolManager.getPoolAnchorBepLinkStatus(topicID, currAnchorOLNameStatusSA/*new String[]{currAnchorOLNameStatusSA[0], currAnchorOLNameStatusSA[1]}*/, currALinkID);
        if (link.getRel() ==0 ) {
            String[] outCompletion = this.myRSCManager.getOutgoingCompletion();
            String completedLinkN = outCompletion[0];
            String totalLinkN = outCompletion[1];
            completedLinkN = String.valueOf(Integer.valueOf(completedLinkN) + 1);
            this.myRSCManager.updateOutgoingCompletion(completedLinkN + " : " + totalLinkN);
        }
    }
    
    private void setRelevant(Bep currentLink, boolean setBep) {
        String currALinkStatus = this.myPoolManager.getPoolAnchorBepLinkStatus(topicID, currentLink);
        if (currALinkStatus.equals("0")) 
        	updateRelevantCompletion(currentLink);  	
        
        currentLink.setRel(Bep.RELEVANT);
        if (setBep)
        	this.myPoolUpdater.updateTopicAnchorLinkOSStatus(topicID, currentLink.getAssociatedAnchor().getParent(), currentLink);
        else
        	this.myPoolUpdater.updateTopicAnchorLinkRel(topicID,  currentLink);
        
        int status = currentLink.getAssociatedAnchor().getStatus();
        if (status != Bep.RELEVANT) {
        	currentLink.getAssociatedAnchor().setStatus(Bep.RELEVANT);
        	myPoolUpdater.updatePoolSubanchorStatus(this.topicID, currentLink.getAssociatedAnchor());
        }
        
        status = currentLink.getAssociatedAnchor().getParent().getStatus();
        if (status != Bep.RELEVANT) {
        	currentLink.getAssociatedAnchor().setStatus(Bep.RELEVANT);
        	myPoolUpdater.updatePoolAnchorStatus(this.topicID, currentLink.getAssociatedAnchor().getParent());
        }
    }

    private void doubleLeftClickEventAction() {
        // 1) get the NEW BEP Start Point & Offset after Double-Click
        // 2) Update Link BEP: Add NEW + Remove OLD
        // 2) BG --> Green
        // 3) Update bepLink: SP, Offset, Status-Rel
        // 4) STAY, NOT Go Next
//        if (this.isTAB) {
//            IndexedAnchor currAnchorOLNameStatusSA = null;
//            Bep currALinkOIDSA = null;
//            String currBepOffset = "";
//            String[] currBLinkOLIDSA = null;
//            currAnchorOLNameStatusSA = this.myRSCManager.getCurrTopicAnchorOLNameStatusSA();
//            currALinkOIDSA = this.myRSCManager.getCurrTopicATargetOID(this.myLinkPane, this.topicID);
//            String currALinkOffset = currALinkOIDSA.offsetToString(); //[0];
//            String currALinkID = currALinkOIDSA.getFileId(); //[1];
//            String currALinkLang = currALinkOIDSA.getTargetLang(); //[3];
//            // -----------------------------------------------------------------
//            logger("outgoing_doubleLeftClick_" + currAnchorOLNameStatusSA.lengthToString() + "-" + currAnchorOLNameStatusSA.lengthToString() + " --> " + currALinkID);
            // -----------------------------------------------------------------
    	Bep currentLink = CurrentFocusedAnchor.getCurrentFocusedAnchor().getCurrentBep();
    	String currALinkID = currentLink.getFileId();
            try {
                int preTBStartPoint = this.myPoolManager.getPABepLinkStartP(topicID, currentLink.getAssociatedAnchor().getParent()/*new String[]{currAnchorOLNameStatusSA[0], currAnchorOLNameStatusSA[1]}*/, currALinkID);
                int clickStartPoint = this.myLinkPane.getSelectionStart();
                String scrTxtTilStartP = this.myLinkPane.getDocument().getText(0, clickStartPoint);
                String linkBepStartP = String.valueOf(clickStartPoint);
                String linkBepOffset = this.myFolMatcher.getXmlBepOffset(currALinkID, scrTxtTilStartP, currentLink.getTargetLang());
                // -------------------------------------------------------------
                // remove OLD Bep (if there is one) & insert new BEP
                // repaint BG as RELEVANT
//                this.updateLinkPaneBepIcon(clickStartPoint, preTBStartPoint);
//                this.myLinkPane.setBackground(this.linkPaneRelColor);
//                this.myLinkPane.repaint();
                // -------------------------------------------------------------
                // Update Outgoing Completion
                setRelevant(currentLink, true);
                
                // -------------------------------------------------------------
                // Update this PAnchor BEP Link Status
//                String currLinkStatus = "1";
                
                // -------------------------------------------------------------
                // Check & Update this Pool Anchor Status/Completion
                // according to its BEP links status
                String[] thisPAnchorCRatio = this.myRSCManager.getPoolAnchorCompletedRatio(topicID, currentLink.getAssociatedAnchor().getParent()/*new String[]{currAnchorOLNameStatusSA[0], currAnchorOLNameStatusSA[1]}*/);
                if (thisPAnchorCRatio[0].equals(thisPAnchorCRatio[1])) {
//                    String poolAnchorStatus = "1";
                	currentLink.getAssociatedAnchor().getParent().setStatus(Bep.RELEVANT);
                    this.myPoolUpdater.updatePoolAnchorStatus(topicID, currentLink.getAssociatedAnchor().getParent());
                } else {
//                    String poolAnchorStatus = "0";
                	currentLink.getAssociatedAnchor().getParent().setStatus(Bep.UNASSESSED);
                    this.myPoolUpdater.updatePoolAnchorStatus(topicID, currentLink.getAssociatedAnchor().getParent());
                }
            } catch (BadLocationException ex) {
                Logger.getLogger(linkPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
            }
//        }
    }

    private void singleLeftClickEventAction() {
        // Relevant Link
        // For TAB ONLY
        // 1) If tbStartP == -1 --> DO-NOTHING
        // 2) If tbStartP > -1  -->
        //       BG Colour to RED
        //       Update Topic Anchor/BEP Link Status to 1
        //       GO NEXT
//        if (this.isTAB) {
            // For outgoing TAB
            // -----------------------------------------------------------------
//            IndexedAnchor currAnchorOLNameStatusSA = this.myRSCManager.getCurrTopicAnchorOLNameStatusSA();
//            Bep currALinkOIDSA = this.myRSCManager.getCurrTopicATargetOID(this.myLinkPane, this.topicID);
//            String currALinkOffset = currALinkOIDSA.offsetToString(); //[0];
//            String currALinkID = currALinkOIDSA.getFileId(); //[1];
//            logger("outgoing_doubleLeftClick_" + currAnchorOLNameStatusSA.lengthToString() + "-" + currAnchorOLNameStatusSA.lengthToString() + " --> " + currALinkID);
            
            // Update Outgoing Completion
            Bep currentLink = CurrentFocusedAnchor.getCurrentFocusedAnchor().getCurrentBep();
            setRelevant(currentLink, false);
            // -----------------------------------------------------------------
//            int poolABepLinkStartP = this.myPoolManager.getPABepLinkStartP(topicID, currentLink.getAssociatedAnchor().getParent()/*new String[]{currAnchorOLNameStatusSA[0], currAnchorOLNameStatusSA[1]}*/, currentLink.getFileId());
//            // -----------------------------------------------------------------
//            if (poolABepLinkStartP > -1) {
//            String currALinkStatus = this.myPoolManager.getPoolAnchorBepLinkStatus(topicID, currentLink.getAssociatedAnchor().getParent(), currentLink.getFileId());
            

            
            AssessedAnchor previous =   currentLink.getAssociatedAnchor();
            Bep link = LTWAssessmentToolControler.getInstance().goNextLink(true, true);
            CurrentFocusedAnchor.getCurrentFocusedAnchor().setAnchor(previous, link.getAssociatedAnchor(), link);
//            os.setTABFieldValues(link);
    }

    private class timerTask extends TimerTask {

        @Override
        public void run() {
            if (IGNORE) {
                timer.cancel();
                IGNORE = false;
                return;
            }
            IGNORE = true;
            SINGLECLICK = true;
            try {
                Robot robot = new Robot();
                robot.mousePress(InputEvent.BUTTON2_MASK);
                robot.mouseRelease(InputEvent.BUTTON2_MASK);
            } catch (AWTException e) {
                e.printStackTrace();
            }
            timer.cancel();
        }
    }
// </editor-fold>
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Update Anchor Highlight & BEP Icon">
    private void updateLinkPaneBepIcon(int newBepStartP, int oldStartP) {
        // TODO: "isHighlighBEP"
        // 1) YES: Remove previous BEP + Make a new BEP
        // 2) NO: INSERT BEP ICONs for this Topic
        try {
            StyledDocument styDoc = (StyledDocument) this.myLinkPane.getDocument();

            if (oldStartP > -1) {
                styDoc.remove(oldStartP, bepLength);
            }

            Style bepStyle = styDoc.addStyle("bepIcon", null);
            StyleConstants.setIcon(bepStyle, new ImageIcon(bepIconImageFilePath));
            styDoc.insertString(newBepStartP, "TBEP", bepStyle);

            this.myLinkPane.repaint();
        } catch (BadLocationException ex) {
            Logger.getLogger(topicPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    private void updatePaneBepIcon(JTextPane txtPane, Vector<String> bepSCROffset, boolean isHighlighBEP) {
//        // TODO: "isHighlighBEP"
//        // 1) YES: Remove previous Highlight BEPs + Make a new Highlight BEP
//        // 2) NO: INSERT BEP ICONs for this Topic
//        try {
//            StyledDocument styDoc = (StyledDocument) txtPane.getDocument();
//            Vector<String[]> HBepSCROffsetV = new Vector<String[]>();
//            Vector<String> bepOLListV = myRSCManager.getTopicBepsOSVS();
//            for (String thisBepOL : bepOLListV) {
//                String[] thisBepOLSA = thisBepOL.split(" : ");
//                HBepSCROffsetV.add(thisBepOLSA);
//            }
//
//            Style bepHStyle = styDoc.addStyle("bepHIcon", null);
//            StyleConstants.setIcon(bepHStyle, new ImageIcon(bepHighlightIconImageFilePath));
//            Style bepCStyle = styDoc.addStyle("bepCIcon", null);
//            StyleConstants.setIcon(bepCStyle, new ImageIcon(bepCompletedIconImageFilePath));
//            Style bepNStyle = styDoc.addStyle("bepNIcon", null);
//            StyleConstants.setIcon(bepNStyle, new ImageIcon(bepNonrelevantIconImageFilePath));
//            Style bepStyle = styDoc.addStyle("bepIcon", null);
//            StyleConstants.setIcon(bepStyle, new ImageIcon(bepIconImageFilePath));
//
//            if (isHighlighBEP) {
//                for (String[] scrBepOS : HBepSCROffsetV) {
//                    styDoc.remove(Integer.valueOf(scrBepOS[1]), bepLength);
//                    String thisPBepStatus = this.myPoolManager.getPoolBepStatus(topicID, scrBepOS[0]);
//                    if (Integer.valueOf(thisPBepStatus) == 1) {
//                        styDoc.insertString(Integer.valueOf(scrBepOS[1]), "CBEP", bepCStyle);
//                    } else if (Integer.valueOf(thisPBepStatus) == -1) {
//                        styDoc.insertString(Integer.valueOf(scrBepOS[1]), "NBEP", bepNStyle);
//                    } else if (Integer.valueOf(thisPBepStatus) == 0) {
//                        styDoc.insertString(Integer.valueOf(scrBepOS[1]), "TBEP", bepStyle);
//                    }
//
//                }
//                for (String scrOffset : bepSCROffset) {
//                    styDoc.remove(Integer.valueOf(scrOffset), bepLength);
//                    styDoc.insertString(Integer.valueOf(scrOffset), "HBEP", bepHStyle);
//                }
//
//            } else {
//                for (String scrOffset : bepSCROffset) {
//                    styDoc.insertString(Integer.valueOf(scrOffset), "TBEP", bepStyle);
//                }
//
//
//            }
//
//        } catch (BadLocationException ex) {
//            Logger.getLogger(topicPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        txtPane.repaint();
//    }
//// </editor-fold>
}
