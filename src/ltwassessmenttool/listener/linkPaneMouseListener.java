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
import ltwassessmenttool.LTWAssessmentToolView;
import ltwassessment.AppResource;
import ltwassessment.parsers.FOLTXTMatcher;
import ltwassessment.parsers.Xml2Html;
import ltwassessment.parsers.PoolerManager;
import ltwassessment.parsers.resourcesManager;
import ltwassessment.utility.BrowserControl;
import ltwassessment.utility.ObservableSingleton;
import ltwassessment.utility.highlightPainters;
import ltwassessment.utility.poolUpdater;
import ltwassessment.utility.tabTxtPaneManager;
import ltwassessment.utility.tbaTxtPaneManager;

/**
 * @author Darren HUANG
 */
public class linkPaneMouseListener implements MouseInputListener {

    protected final String poolXmlFileName = "wikipedia_pool.xml";
    protected final String loggerFileName = "_ltwAssessTool2009.log";
    protected final String poolAndLogDir = "resources" + File.separator + "Pool";
    protected final String crosslinkURL = "http://ntcir.nii.ac.jp/CrossLink/";
    // Declare global variables
    protected final int bepLength = 4;
    private final String sysPropertyKey = "isTABKey";
    private final String sysPropertyIsTopicWikiKey = "isTopicWikipedia";
    private final String sysPropertyIsLinkWikiKey = "isLinkWikipedia";
    private final String sysPropertyTABCompletedRatioKey = "tabCompletedRatio";
    private final String sysPropertyTBACompletedRatioKey = "tbaCompletedRatio";
    private final String sysPropertyCurrTopicOLSEStatusKey = "currTopicOLSE";
    private boolean isTAB = false;
    highlightPainters painters;
    private JTextPane myTopicPane;
    private JTextPane myLinkPane;
    // import External Classes
    private resourcesManager myRSCManager;
    private FOLTXTMatcher myFolMatcher;
    private poolUpdater myPoolUpdater;
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
    private Color linkPaneWhiteColor = Color.WHITE;
    private Color linkPaneRelColor = new Color(168, 232, 177);
    private Color linkPaneNonRelColor = new Color(255, 183, 165);
    private ObservableSingleton os = null;
    private Hashtable<String, String[]> topicAnchorOLTSENHT = new Hashtable<String, String[]>();
    private Hashtable<String, Object> myTAnchorSEHiObj = new Hashtable<String, Object>();
    
    private boolean showOnce = false;

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
    private int globalPoolBackupCounter = 0;

    private void backupPool() {
        String sourcePoolFPath = "resources" + File.separator + "Pool" + File.separator + "wikipedia_pool.xml";
        File srcFile = new File(sourcePoolFPath);
        String backupPoolDir = "resources" + File.separator + "Pool" + File.separator + "POOL_BACKUP" + File.separator;
        DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
        Date date = new Date();
        String currentDateTime = dateFormat.format(date).toString();
        String backupPoolFPath = backupPoolDir + this.topicID + "_" + currentDateTime + "_Pool.xml";
        File destFile = new File(backupPoolFPath);
        InputStream in = null;
        OutputStream out = null;
        if (!destFile.exists() && srcFile.exists()) {
            try {
                destFile.createNewFile();
                in = new FileInputStream(srcFile);
                out = new FileOutputStream(destFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(linkPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
            }
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

        myRSCManager = resourcesManager.getInstance();
        myFolMatcher = FOLTXTMatcher.getInstance();
        myPoolUpdater = new poolUpdater();
        myPoolManager = PoolerManager.getInstance();
        myTABTxtPaneManager = new tabTxtPaneManager();
        myTBATxtPaneManager = new tbaTxtPaneManager();
        this.os = ObservableSingleton.getInstance();
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
            String[] currAnchorOLNameStatusSA = this.myRSCManager.getCurrTopicAnchorOLNameStatusSA();
            String[] currALinkOIDSA = this.myRSCManager.getCurrTopicATargetOID(this.myLinkPane, this.topicID);
            String currALinkID = currALinkOIDSA[1];
            logger("outgoing_singleRightClick_" + currAnchorOLNameStatusSA[0] + "-" + currAnchorOLNameStatusSA[1] + " --> " + currALinkID);
            // -----------------------------------------------------------------
            String currALinkStatus = this.myPoolManager.getPoolAnchorBepLinkStatus(topicID, new String[]{currAnchorOLNameStatusSA[0], currAnchorOLNameStatusSA[1]}, currALinkID);
            if (currALinkStatus.equals("0")) {
                String[] outCompletion = this.myRSCManager.getOutgoingCompletion();
                String completedLinkN = outCompletion[0];
                String totalLinkN = outCompletion[1];
                completedLinkN = String.valueOf(Integer.valueOf(completedLinkN) + 1);
                this.myRSCManager.updateOutgoingCompletion(completedLinkN + " : " + totalLinkN);
            }
            // -----------------------------------------------------------------
            this.myLinkPane.setBackground(this.linkPaneNonRelColor);
            this.myLinkPane.repaint();
            String currLinkStatus = "-1";
            this.myPoolUpdater.updateTopicAnchorLinkRel(topicID, new String[]{currAnchorOLNameStatusSA[0], currAnchorOLNameStatusSA[1]},
                    currALinkID, currLinkStatus);
            // -----------------------------------------------------------------
            goNextLink();
//        } else {
//            // For incoming TBA
//            // -----------------------------------------------------------------
//            String currBepOffset = this.myRSCManager.getCurrTopicBepOffset();
//            String[] currBLinkOLIDSA = this.myRSCManager.getCurrTopicBTargetOLID(this.myLinkPane, this.topicID);
//            logger("incoming_singleRightClick_" + currBepOffset + " --> " + currBLinkOLIDSA[2]);
//            // -----------------------------------------------------------------
//            String currBLinkStatus = this.myPoolManager.getPoolBepAnchorLinkStatus(topicID, currBepOffset, currBLinkOLIDSA);
//            if (currBLinkStatus.equals("0")) {
//                String[] inCompletion = this.myRSCManager.getIncomingCompletion();
//                String completedLinkN = inCompletion[0];
//                String totalLinkN = inCompletion[1];
//                completedLinkN = String.valueOf(Integer.valueOf(completedLinkN) + 1);
//                this.myRSCManager.updateIncomingCompletion(completedLinkN + " : " + totalLinkN);
//            }
//            // -----------------------------------------------------------------
//            this.myLinkPane.setBackground(this.linkPaneNonRelColor);
//            this.myLinkPane.repaint();
//            // -----------------------------------------------------------------
//            String currLinkStatus = "-1";
//            this.myPoolUpdater.updateTopicBepLinkRel(topicID, currBepOffset,
//                    currBLinkOLIDSA, currLinkStatus);
//            // -----------------------------------------------------------------
////            log("START NEXT LINK ... ");
//            goNextLink();
//        }
    }
    
    private void updateRelevantCompletion(String[] currAnchorOLNameStatusSA, String currALinkID) {
        String currALinkStatus = this.myPoolManager.getPoolAnchorBepLinkStatus(topicID, new String[]{currAnchorOLNameStatusSA[0], currAnchorOLNameStatusSA[1]}, currALinkID);
        if (currALinkStatus.equals("0")) {
            String[] outCompletion = this.myRSCManager.getOutgoingCompletion();
            String completedLinkN = outCompletion[0];
            String totalLinkN = outCompletion[1];
            completedLinkN = String.valueOf(Integer.valueOf(completedLinkN) + 1);
            this.myRSCManager.updateOutgoingCompletion(completedLinkN + " : " + totalLinkN);
        }
    }

    private void doubleLeftClickEventAction() {
        // 1) get the NEW BEP Start Point & Offset after Double-Click
        // 2) Update Link BEP: Add NEW + Remove OLD
        // 2) BG --> Green
        // 3) Update bepLink: SP, Offset, Status-Rel
        // 4) STAY, NOT Go Next
        if (this.isTAB) {
            String[] currAnchorOLNameStatusSA = null;
            String[] currALinkOIDSA = null;
            String currBepOffset = "";
            String[] currBLinkOLIDSA = null;
            currAnchorOLNameStatusSA = this.myRSCManager.getCurrTopicAnchorOLNameStatusSA();
            currALinkOIDSA = this.myRSCManager.getCurrTopicATargetOID(this.myLinkPane, this.topicID);
            String currALinkOffset = currALinkOIDSA[0];
            String currALinkID = currALinkOIDSA[1];
            String currALinkLang = currALinkOIDSA[3];
            // -----------------------------------------------------------------
            logger("outgoing_doubleLeftClick_" + currAnchorOLNameStatusSA[0] + "-" + currAnchorOLNameStatusSA[1] + " --> " + currALinkID);
            // -----------------------------------------------------------------
            try {
                int preTBStartPoint = this.myPoolManager.getPABepLinkStartP(topicID, new String[]{currAnchorOLNameStatusSA[0], currAnchorOLNameStatusSA[1]}, currALinkID);
                int clickStartPoint = this.myLinkPane.getSelectionStart();
                String scrTxtTilStartP = this.myLinkPane.getDocument().getText(0, clickStartPoint);
                String linkBepStartP = String.valueOf(clickStartPoint);
                String linkBepOffset = this.myFolMatcher.getXmlBepOffset(currALinkID, scrTxtTilStartP, currALinkLang);
                // -------------------------------------------------------------
                // remove OLD Bep (if there is one) & insert new BEP
                // repaint BG as RELEVANT
                this.updateLinkPaneBepIcon(clickStartPoint, preTBStartPoint);
                this.myLinkPane.setBackground(this.linkPaneRelColor);
                this.myLinkPane.repaint();
                // -------------------------------------------------------------
                // Update Outgoing Completion
                updateRelevantCompletion(currAnchorOLNameStatusSA, currALinkID);
                
                // -------------------------------------------------------------
                // Update this PAnchor BEP Link Status
                String currLinkStatus = "1";
                this.myPoolUpdater.updateTopicAnchorLinkOSStatus(topicID, new String[]{currAnchorOLNameStatusSA[0], currAnchorOLNameStatusSA[1]},
                        new String[]{linkBepOffset, linkBepStartP, currALinkID},
                        currLinkStatus);
                // -------------------------------------------------------------
                // Check & Update this Pool Anchor Status/Completion
                // according to its BEP links status
                String[] thisPAnchorCRatio = this.myRSCManager.getPoolAnchorCompletedRatio(topicID, new String[]{currAnchorOLNameStatusSA[0], currAnchorOLNameStatusSA[1]});
                if (thisPAnchorCRatio[0].equals(thisPAnchorCRatio[1])) {
                    String poolAnchorStatus = "1";
                    this.myPoolUpdater.updatePoolAnchorStatus(topicID, new String[]{currAnchorOLNameStatusSA[0], currAnchorOLNameStatusSA[1]},
                            poolAnchorStatus);
                } else {
                    String poolAnchorStatus = "0";
                    this.myPoolUpdater.updatePoolAnchorStatus(topicID, new String[]{currAnchorOLNameStatusSA[0], currAnchorOLNameStatusSA[1]},
                            poolAnchorStatus);
                }
            } catch (BadLocationException ex) {
                Logger.getLogger(linkPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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
            String[] currAnchorOLNameStatusSA = this.myRSCManager.getCurrTopicAnchorOLNameStatusSA();
            String[] currALinkOIDSA = this.myRSCManager.getCurrTopicATargetOID(this.myLinkPane, this.topicID);
            String currALinkOffset = currALinkOIDSA[0];
            String currALinkID = currALinkOIDSA[1];
            logger("outgoing_singleRightClick_" + currAnchorOLNameStatusSA[0] + "-" + currAnchorOLNameStatusSA[1] + " --> " + currALinkID);
            
            // Update Outgoing Completion
            updateRelevantCompletion(currAnchorOLNameStatusSA, currALinkID);
            
            // -----------------------------------------------------------------
            int poolABepLinkStartP = this.myPoolManager.getPABepLinkStartP(topicID, new String[]{currAnchorOLNameStatusSA[0], currAnchorOLNameStatusSA[1]}, currALinkID);
            // -----------------------------------------------------------------
            if (poolABepLinkStartP > -1) {
                this.myLinkPane.setBackground(this.linkPaneNonRelColor);
                // -------------------------------------------------------------
                String currLinkStatus = "1";
                this.myPoolUpdater.updateTopicAnchorLinkRel(topicID, new String[]{currAnchorOLNameStatusSA[0], currAnchorOLNameStatusSA[1]},
                        currALinkID, currLinkStatus);
                // -------------------------------------------------------------
                goNextLink();
            }
//        } else {
//            // For incoming TBA
//            // -----------------------------------------------------------------
//            String currBepOffset = this.myRSCManager.getCurrTopicBepOffset();
//            String[] currBLinkOLIDSA = this.myRSCManager.getCurrTopicBTargetOLID(this.myLinkPane, this.topicID);
//            logger("incoming_singleRightClick_" + currBepOffset + " --> " + currBLinkOLIDSA[2]);
//            // -----------------------------------------------------------------
//            this.myLinkPane.setBackground(this.linkPaneNonRelColor);
//            // -----------------------------------------------------------------
//            String currBLinkStatus = this.myPoolManager.getPoolBepAnchorLinkStatus(topicID, currBepOffset, currBLinkOLIDSA);
//            if (currBLinkStatus.equals("0")) {
//                String[] inCompletion = this.myRSCManager.getIncomingCompletion();
//                String completedLinkN = inCompletion[0];
//                String totalLinkN = inCompletion[1];
//                completedLinkN = String.valueOf(Integer.valueOf(completedLinkN) + 1);
//                this.myRSCManager.updateIncomingCompletion(completedLinkN + " : " + totalLinkN);
//            }
//            // -----------------------------------------------------------------
//            String currLinkStatus = "1";
//            this.myPoolUpdater.updateTopicBepLinkRel(topicID, currBepOffset,
//                    currBLinkOLIDSA, currLinkStatus);
//            // -----------------------------------------------------------------
//            goNextLink();
//        }
    }

    private void goNextLink() {
        // ---------------------------------------------------------------------
        globalPoolBackupCounter++;
        // Pool Assessment Result Back up
        if (globalPoolBackupCounter % 10 == 0) {
            backupPool();
        }
        // ---------------------------------------------------------------------
        // 1) check Whether the assessment is Completed
        //    If Yes, pop-up Survey Questionaire
        //           --> Then Link for Upload Result XML, Logger & Questionaires
        // 2) If Not, Go Next
//        log("COMPLETION ... ");
        String[] tabCompletedRatio = this.myRSCManager.getOutgoingCompletion();
//        String[] tbaCompletedRatio = this.myRSCManager.getIncomingCompletion();
        if (Integer.parseInt(tabCompletedRatio[0]) > 0 && tabCompletedRatio[0].equals(tabCompletedRatio[1])  && !showOnce /* && tbaCompletedRatio[0].equals(tbaCompletedRatio[1])*/) {
            int option = JOptionPane.showConfirmDialog(this.myTopicPane, "The Assessment is completed.\r\n" +
                    "Please zip the result file and log together \r\n" +
                    "and email it to the Crosslink organisers or other alternative way provided. \r\n" +
                    "The files, " + poolXmlFileName + " & T" + topicID + loggerFileName + " are located in the following directory: \r\n" +
                    poolAndLogDir, "Assessment Completion",
                    JOptionPane.OK_OPTION);
            if (option == JOptionPane.OK_OPTION || option == JOptionPane.CLOSED_OPTION) {
                BrowserControl openBrowser = new BrowserControl();
                openBrowser.displayURL(crosslinkURL);
                javax.swing.SwingUtilities.getWindowAncestor(this.myLinkPane).setVisible(false);
                javax.swing.SwingUtilities.getWindowAncestor(this.myLinkPane).dispose();
                System.exit(0);
            }
            showOnce = true;
            return;
        } /*else if (this.isTAB && tabCompletedRatio[0].equals(tabCompletedRatio[1]) && !tbaCompletedRatio[0].equals(tbaCompletedRatio[1])) {
            JOptionPane.showMessageDialog(this.myTopicPane, "The Outgoing Assessment is completed. \r\n" +
                    "Please click \"OK\" button and switch to Incoming Mode to complete the assessment.\r\n" +
                    "The progress of Incoming is " + tbaCompletedRatio[0] + " out of " + tbaCompletedRatio[1] + ".");
        } else if (!this.isTAB && !tabCompletedRatio[0].equals(tabCompletedRatio[1]) && tbaCompletedRatio[0].equals(tbaCompletedRatio[1])) {
            JOptionPane.showMessageDialog(this.myTopicPane, "The Incoming Assessment is completed. \r\n" +
                    "Please click \"OK\" button and switch to Outgoing Mode to complete the assessment.\r\n" +
                    "The progress of Outgoing is " + tabCompletedRatio[0] + " out of " + tabCompletedRatio[1] + ".");
        } */else {
//            if (this.isTAB) {
                // <editor-fold defaultstate="collapsed" desc="Update TAB Topic, Link">
//                String currTopicOLSEStatus = System.getProperty(sysPropertyCurrTopicOLSEStatusKey);
//                String[] currTopicOLSEStatusSA = currTopicOLSEStatus.split("_");
//                String currPAnchorO = currTopicOLSEStatusSA[0];
//                String currPAnchorL = currTopicOLSEStatusSA[1];
//                String currPAnchorS = currTopicOLSEStatusSA[2];
//                String currPAnchorE = currTopicOLSEStatusSA[3];
//                String currPAnchorExt = currTopicOLSEStatusSA[4];
//                String[] currPAnchorOLSA = new String[]{currPAnchorO, currPAnchorL};
//                String currPAnchorStatus = this.myPoolManager.getPoolAnchorStatus(topicID, currPAnchorOLSA);
//                String[] currALinkOIDSA = this.myRSCManager.getCurrTopicATargetOID(this.myLinkPane, this.topicID);
//                String currALinkOffset = currALinkOIDSA[0];
//                String currALinkID = currALinkOIDSA[1];
//                String[] currPALinkOIDSA = new String[]{currALinkOffset, currALinkID};
//                String currPALinkStatus = this.myPoolManager.getPoolAnchorBepLinkStatus(topicID, currPAnchorOLSA, currALinkID);
//                // -------------------------------------------------------------
//                // 1) Get the NEXT Anchor O, L, S, E, Status + its BEP link O, S, ID, Status
//                //    With TAB Nav Update --> NEXT TAB
//                Vector<String[]> nextAnchorBepLinkVSA = this.myRSCManager.getNextUNAssTABWithUpdateNAV(topicID, currPAnchorOLSA, currPALinkOIDSA);
//                // Get PoolAnchor O, L, S, E, Status
//                String[] nextAnchorOLSEStatusSA = nextAnchorBepLinkVSA.elementAt(0);
//                String nextAnchorO = nextAnchorOLSEStatusSA[0];
//                String nextAnchorL = nextAnchorOLSEStatusSA[1];
//                String nextAnchorS = nextAnchorOLSEStatusSA[2];
//                String nextAnchorE = nextAnchorOLSEStatusSA[3];
//                String nextAnchorStatus = this.myPoolManager.getPoolAnchorStatus(topicID, new String[]{nextAnchorO, nextAnchorL});
//                // new String[]{tbOffset, tbStartP, tbFileID, tbRel}
//                String[] nextAnchorLinkOSIDStatusSA = nextAnchorBepLinkVSA.elementAt(1);
//                String nextLinkO = nextAnchorLinkOSIDStatusSA[0];
//                String nextLinkID = nextAnchorLinkOSIDStatusSA[2];
//                String nextLinkLang = nextAnchorLinkOSIDStatusSA[4];
//                String nextLinkTitle = nextAnchorLinkOSIDStatusSA[5];
//                if (nextLinkID.endsWith("\"")) {
//                    nextLinkID = nextLinkID.substring(0, nextLinkID.length() - 1);
//                }
//                String nextLinkS = this.myPoolManager.getPoolAnchorBepLinkStartP(topicID, new String[]{nextAnchorO, nextAnchorL}, nextLinkID);
//                String nextLinkStatus = this.myPoolManager.getPoolAnchorBepLinkStatus(topicID, new String[]{nextAnchorO, nextAnchorL}, nextLinkID);
//                
//                String nextLinkExtLength = nextAnchorLinkOSIDStatusSA[6];
//                
//                // re-adjust the anchor offset and length
//                int offset = Integer.parseInt(nextAnchorLinkOSIDStatusSA[6]);
//                int length = Integer.parseInt(nextAnchorLinkOSIDStatusSA[7]);
//                int anchorOffset = Integer.parseInt(nextAnchorO);
//                int screenOffset = Integer.parseInt(nextAnchorS) + offset - anchorOffset;
//                String nextSubanchorS = String.valueOf(screenOffset);
//                String nextSubanchorE = String.valueOf(screenOffset + length);
//                
//                int extLength = Integer.parseInt(currPAnchorExt);
//                if (extLength > 0)
//                	currPAnchorE = String.valueOf(Integer.parseInt(currPAnchorE) + extLength);
//                // -------------------------------------------------------------
//                // Update Pool Anchor Status <-- When GO NEXT Pool Anchor
//                // because the PRE-Pool_Anchor might have been completed.
//                if (!nextAnchorO.equals(currPAnchorO)) {
//                    // ---------------------------------------------------------
//                    // Update Pool Anchor Status
//                    String poolAnchorStatus = "";
//                    if (currPALinkStatus.equals("-1")) {
//                        if (currPAnchorStatus.equals("-1")) {
//                            poolAnchorStatus = currPAnchorStatus;
//                        } else {
//                            poolAnchorStatus = this.myRSCManager.getPoolAnchorCompletedStatus(topicID, currPAnchorOLSA);
//                        }
//                    } else {
//                        poolAnchorStatus = this.myRSCManager.getPoolAnchorCompletedStatus(topicID, currPAnchorOLSA);
//                    }
//                    this.myPoolUpdater.updatePoolAnchorStatus(topicID, currPAnchorOLSA, poolAnchorStatus);
//                    // ---------------------------------------------------------
//                    // Highlight Anchor/BEP + Auto Scrolling
//                    String updatedPAnchorStatus = this.myPoolManager.getPoolAnchorStatus(topicID, currPAnchorOLSA);
//                    LTWAssessmentToolView.updateTopicAnchorsHighlight(this.myTopicPane, new String[]{currPAnchorS, currPAnchorE, updatedPAnchorStatus}, new String[]{"", nextAnchorS, nextAnchorE, nextLinkExtLength, nextSubanchorS, nextSubanchorE}, Integer.parseInt(nextLinkStatus));
//                    this.myTopicPane.getCaret().setDot(Integer.valueOf(nextAnchorE));
//                    this.myTopicPane.scrollRectToVisible(this.myTopicPane.getVisibleRect());
//                    this.myTopicPane.repaint();
//                    // ---------------------------------------------------------
//                    // set up Property
//                    String sysPropertyValue = nextAnchorO + "_" + nextAnchorL + "_" + nextAnchorS + "_" + nextAnchorE + "_" + nextAnchorStatus;
//                    System.setProperty(sysPropertyCurrTopicOLSEStatusKey, sysPropertyValue);
//                }
//                String bepXmlFilePath = this.myPoolManager.getXmlFilePathByTargetID(nextLinkID, nextLinkLang);
//                if (bepXmlFilePath.startsWith(afTasnCollectionErrors)) {
//                    bepXmlFilePath = myRSCManager.getErrorXmlFilePath(bepXmlFilePath);
//                }
//                Xml2Html xmlParser = new Xml2Html(bepXmlFilePath, Boolean.valueOf(System.getProperty(sysPropertyIsLinkWikiKey)));
//                String xmlHtmlText = xmlParser.getHtmlContent().toString();
//                this.myLinkPane.setContentType(paneContentType);
//                this.myLinkPane.setText(xmlHtmlText);
//                this.myLinkPane.setCaretPosition(0);
//                // -------------------------------------------------------------
//                // -------------------------------------------------------------
//                if (Integer.valueOf(nextAnchorStatus) == 1 || Integer.valueOf(nextAnchorStatus) == 0) {
//                    if (Integer.valueOf(nextLinkStatus) == 1) {
//                        // Relevant --> Insert BEP --> BG = Green
//                        Vector<String> bepSCROffset = new Vector<String>();
//                        bepSCROffset.add(nextLinkS);
//                        boolean isTopicBEP = false;
//                        updatePaneBepIcon(this.myLinkPane, bepSCROffset, isTopicBEP);
//                        this.myLinkPane.getCaret().setDot(Integer.valueOf(nextLinkS));
//                        this.myLinkPane.scrollRectToVisible(this.myLinkPane.getVisibleRect());
//                        this.myLinkPane.setBackground(this.linkPaneRelColor);
//                    } else if (Integer.valueOf(nextLinkStatus) == -1) {
//                        this.myLinkPane.setBackground(this.linkPaneNonRelColor);
//                    } else if (Integer.valueOf(nextLinkStatus) == 0) {
//                        this.myLinkPane.setBackground(this.linkPaneWhiteColor);
//                    }
//                    this.myLinkPane.repaint();
//                } else if (Integer.valueOf(nextAnchorStatus) == -1) {
//                    this.myLinkPane.setBackground(this.linkPaneNonRelColor);
//                    this.myLinkPane.repaint();
//                }
//                // -------------------------------------------------------------
//                // -------------------------------------------------------------
//                String currAnchorName = this.myPoolManager.getPoolAnchorNameByOL(this.topicID, new String[]{nextAnchorO, nextAnchorL});
//                Vector<String> newTABFieldValues = new Vector<String>();
//                newTABFieldValues.add(this.currTopicName);
//                newTABFieldValues.add(this.topicID);
//                newTABFieldValues.add(currAnchorName);
//                newTABFieldValues.add(nextLinkID);
//                String pageTitle = "";
//                pageTitle = this.myRSCManager.getWikipediaPageTitle(nextLinkID);
//                newTABFieldValues.add(pageTitle.trim());
//                String[] pAnchorCompletionSA = this.myRSCManager.getOutgoingCompletion();
//                newTABFieldValues.add(pAnchorCompletionSA[0] + " / " + pAnchorCompletionSA[1]);
//                os.setTABFieldValues(newTABFieldValues);
                
                LTWAssessmentToolView.moveForwardALink(true);
                // ---------------------------------------------------------------------
                // ---------------------------------------------------------------------
                // </editor-fold>
//            } else {
//                // <editor-fold defaultstate="collapsed" desc="Update TBA Topic, Link">
//                String currTopicOLSEStatus = System.getProperty(sysPropertyCurrTopicOLSEStatusKey);
//                String[] currTopicOLSEStatusSA = currTopicOLSEStatus.split("_");
//                String currBepOffset = currTopicOLSEStatusSA[0];
//                String currBepSPoint = currTopicOLSEStatusSA[2];
//                String[] currBLinkOLIDSA = this.myRSCManager.getCurrTopicBTargetOLID(this.myLinkPane, this.topicID);
//                // -------------------------------------------------------------
//                // 1) Get the NEXT Anchor O, L, S, E, Status + its BEP link O, S, ID, Status
//                //    With TAB Nav Update --> NEXT TAB
////                log("START NEXT TBA ... ");
//                Vector<String[]> nextBepAnchorLinkVSA = this.myRSCManager.getNextUNAssTBAWithUpdateNAV(topicID, currBepOffset, currBLinkOLIDSA);
////                log("END NEXT TBA ... ");
//                // Get Pool Bep O, S, Status
//                String[] nextBepOSStatus = nextBepAnchorLinkVSA.elementAt(0);
//                String nextBepO = nextBepOSStatus[0];
//                String nextBepS = nextBepOSStatus[1];
//                String nextBepStatus = this.myPoolManager.getPoolBepStatus(topicID, currBepOffset);
//                // Get Pool Bep - AnchorLink O, L, ID, Status
//                String[] nextLinkOLStatusStatus = nextBepAnchorLinkVSA.elementAt(1);
//                String nextLinkO = nextLinkOLStatusStatus[0];
//                String nextLinkL = nextLinkOLStatusStatus[1];
//                String nextLinkID = nextLinkOLStatusStatus[2];
//                
//                String nextLinkStatus = this.myPoolManager.getPoolBepAnchorLinkStatus(topicID, currBepOffset, new String[]{nextLinkO, nextLinkL, nextLinkID});
////                log(nextLinkO + " - " + nextLinkL + " - " + nextLinkID);
////                log("nextLinkStatus: " + nextLinkStatus);
//                // -------------------------------------------------------------
//                // Update Pool Anchor Status <-- When GO NEXT Pool Anchor
//                // because the PRE-Pool_Anchor might have been completed.
//                if (!nextBepO.equals(currBepOffset)) {
//                    // Update Pool BEP Status
//                    String poolBepStatus = this.myRSCManager.getPoolBepCompletedStatus(topicID, currBepOffset);
//                    this.myPoolUpdater.updatePoolBepStatus(topicID, currBepOffset, poolBepStatus);
//                    // Highlight Anchor/BEP + Auto Scrolling
//                    Vector<String> bepSCROffset = new Vector<String>();
//                    bepSCROffset.add(nextBepS);
//                    boolean isTopicBEP = true;
//                    updatePaneBepIcon(this.myTopicPane, bepSCROffset, isTopicBEP);
//                    this.myTopicPane.getCaret().setDot(Integer.valueOf(nextBepS));
//                    this.myTopicPane.scrollRectToVisible(this.myTopicPane.getVisibleRect());
//                    this.myTopicPane.repaint();
//                    // update System Property
//                    String currTopicOLSEStatusKey = nextBepO + "_" + bepLength + "_" + nextBepS + "_" + String.valueOf(Integer.valueOf(nextBepS) + Integer.valueOf(bepLength)) + "_" + nextBepStatus;
//                    System.setProperty(this.sysPropertyCurrTopicOLSEStatusKey, currTopicOLSEStatusKey);
//                }
//                // -------------------------------------------------------------
//                String anchorXmlFilePath = this.myPoolManager.getXmlFilePathByTargetID(nextLinkID);
//                if (anchorXmlFilePath.startsWith(afTasnCollectionErrors)) {
//                    anchorXmlFilePath = myRSCManager.getErrorXmlFilePath(anchorXmlFilePath);
//                }
//                Xml2Html xmlParser = new Xml2Html(anchorXmlFilePath, Boolean.valueOf(System.getProperty(sysPropertyIsLinkWikiKey)));
//                String xmlHtmlText = xmlParser.getHtmlContent().toString();
//                this.myLinkPane.setContentType(paneContentType);
//                this.myLinkPane.setText(xmlHtmlText);
//                this.myLinkPane.setCaretPosition(0);
//                // -------------------------------------------------------------
//                String[] nextLinkSE = this.myFolMatcher.getSCRAnchorNameSESA(this.myLinkPane, nextLinkID, new String[]{nextLinkO, nextLinkL, ""});
//                String nextLinkAnchorName = nextLinkSE[0];
//                String nextLinkS = nextLinkSE[1];
//                String nextLinkE = nextLinkSE[2];
//                // Highlight Anchor Txt in JTextPane
//                updateLinkAnchorHighlight(this.myLinkPane, new String[]{nextLinkS, nextLinkE});
//                // ---------------------------------------------------------
//                // Renew Link Text Pane Listener for Detecting Anchor Text & Right/Left Click
//                this.myLinkPane.getCaret().setDot(Integer.valueOf(nextLinkE));
//                this.myLinkPane.scrollRectToVisible(this.myLinkPane.getVisibleRect());
//                if (Integer.valueOf(nextBepStatus) == 1 || Integer.valueOf(nextBepStatus) == 0) {
//                    if (Integer.valueOf(nextLinkStatus) == 1) {
//                        this.myLinkPane.setBackground(this.linkPaneRelColor);
//                    } else if (Integer.valueOf(nextLinkStatus) == -1) {
//                        this.myLinkPane.setBackground(this.linkPaneNonRelColor);
//                    } else if (Integer.valueOf(nextLinkStatus) == 0) {
//                        this.myLinkPane.setBackground(this.linkPaneWhiteColor);
//                    }
//                } else if (Integer.valueOf(nextBepStatus) == -1) {
//                    this.myLinkPane.setBackground(this.linkPaneNonRelColor);
//                }
//                this.myLinkPane.repaint();
//                // -------------------------------------------------------------
//                Vector<String> newTABFieldValues = new Vector<String>();
//                newTABFieldValues.add(this.currTopicName);
//                newTABFieldValues.add(this.topicID);
//                newTABFieldValues.add(nextLinkAnchorName);
//                newTABFieldValues.add(nextLinkID);
//                String pageTitle = "";
////                if (Boolean.valueOf(System.getProperty(sysPropertyIsLinkWikiKey))) {
//                    pageTitle = this.myRSCManager.getWikipediaPageTitle(nextLinkID);
////                } else {
////                    pageTitle = this.myRSCManager.getTeAraFilePathByName(nextLinkID);
////                }
//                newTABFieldValues.add(pageTitle.trim());
//                String[] pAnchorCompletionSA = this.myRSCManager.getIncomingCompletion();
//                newTABFieldValues.add(pAnchorCompletionSA[0] + " / " + pAnchorCompletionSA[1]);
//                os.setTABFieldValues(newTABFieldValues);
//                // </editor-fold>
//            }
        }
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

    private void updateLinkAnchorHighlight(JTextPane linkPane, String[] anchorSEPosSA) {
        // This might need to handle: isAssessment
        // 1) YES:
        // 2) NO: ONLY highlight Anchor Text
        try {
            Highlighter txtPaneHighlighter = linkPane.getHighlighter();
            int[] achorSCRPos = new int[]{Integer.valueOf(anchorSEPosSA[0]), Integer.valueOf(anchorSEPosSA[1])};
            Object aHighlightRef = txtPaneHighlighter.addHighlight(achorSCRPos[0], achorSCRPos[1], painters.getAnchorPainter());
            // -----------------------------------------------------------------
            Document myDoc = linkPane.getDocument();
            String myContent = myDoc.getText(0, myDoc.getLength()).toLowerCase();
            String hiltTxt = this.currTopicName.trim();
            int endIndex = 0;
            int wordLength = hiltTxt.length();
            while ((endIndex = myContent.indexOf(hiltTxt, endIndex)) != -1) {
                aHighlightRef = txtPaneHighlighter.addHighlight(endIndex, endIndex + wordLength, painters.getIrrelevantPainter());
                log("Topic Title Matched: " + endIndex);
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(topicPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    private void updateTopicAnchorsHighlight(JTextPane topicPane, String[] preAnchorSEStatus, String[] currAnchorSE) {
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
//            for (int i = 0; i < highlights.length; i++) {
//                int sPos = highlights[i].getStartOffset();
//                int ePos = highlights[i].getEndOffset();
//                if (achorSCRPos[0] == sPos && achorSCRPos[1] == ePos) {
//                    txtPaneHighlighter.removeHighlight(highlights[i]);
//                    anchorHighlightRef = txtPaneHighlighter.addHighlight(sPos, ePos, painters.getSelectedPainter());
//                    topicPane.repaint();
//                    currDONEFlag = true;
//                    if (currDONEFlag && preDONEFlag) {
//                        break;
//                    }
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
//                        topicPane.repaint();
//                        preDONEFlag = true;
//                        if (currDONEFlag && preDONEFlag) {
//                            break;
//                        }
//                    }
//                }
//            }
//        } catch (BadLocationException ex) {
//            Logger.getLogger(topicPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

    private void updatePaneBepIcon(JTextPane txtPane, Vector<String> bepSCROffset, boolean isHighlighBEP) {
        // TODO: "isHighlighBEP"
        // 1) YES: Remove previous Highlight BEPs + Make a new Highlight BEP
        // 2) NO: INSERT BEP ICONs for this Topic
        try {
            StyledDocument styDoc = (StyledDocument) txtPane.getDocument();
            Vector<String[]> HBepSCROffsetV = new Vector<String[]>();
            Vector<String> bepOLListV = myRSCManager.getTopicBepsOSVS();
            for (String thisBepOL : bepOLListV) {
                String[] thisBepOLSA = thisBepOL.split(" : ");
                HBepSCROffsetV.add(thisBepOLSA);
            }

            Style bepHStyle = styDoc.addStyle("bepHIcon", null);
            StyleConstants.setIcon(bepHStyle, new ImageIcon(bepHighlightIconImageFilePath));
            Style bepCStyle = styDoc.addStyle("bepCIcon", null);
            StyleConstants.setIcon(bepCStyle, new ImageIcon(bepCompletedIconImageFilePath));
            Style bepNStyle = styDoc.addStyle("bepNIcon", null);
            StyleConstants.setIcon(bepNStyle, new ImageIcon(bepNonrelevantIconImageFilePath));
            Style bepStyle = styDoc.addStyle("bepIcon", null);
            StyleConstants.setIcon(bepStyle, new ImageIcon(bepIconImageFilePath));

            if (isHighlighBEP) {
                for (String[] scrBepOS : HBepSCROffsetV) {
                    styDoc.remove(Integer.valueOf(scrBepOS[1]), bepLength);
                    String thisPBepStatus = this.myPoolManager.getPoolBepStatus(topicID, scrBepOS[0]);
                    if (Integer.valueOf(thisPBepStatus) == 1) {
                        styDoc.insertString(Integer.valueOf(scrBepOS[1]), "CBEP", bepCStyle);
                    } else if (Integer.valueOf(thisPBepStatus) == -1) {
                        styDoc.insertString(Integer.valueOf(scrBepOS[1]), "NBEP", bepNStyle);
                    } else if (Integer.valueOf(thisPBepStatus) == 0) {
                        styDoc.insertString(Integer.valueOf(scrBepOS[1]), "TBEP", bepStyle);
                    }

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
// </editor-fold>
}
