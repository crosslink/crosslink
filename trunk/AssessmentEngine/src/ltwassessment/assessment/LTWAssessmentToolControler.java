package ltwassessment.assessment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JTextPane;

import ltwassessment.Assessment;
import ltwassessment.assessment.Bep;
import ltwassessment.assessment.CurrentFocusedAnchor;
import ltwassessment.assessment.IndexedAnchor;
import ltwassessment.parsers.PoolerManager;
import ltwassessment.parsers.ResourcesManager;
import ltwassessment.utility.BrowserControl;
import ltwassessment.utility.PoolUpdater;

public class LTWAssessmentToolControler {
	public final static String sysPropertyIsTABKey = "isTABKey";
	public final static String sysPropertyIsTopicWikiKey = "isTopicWikipedia";
    public final static String sysPropertyIsLinkWikiKey = "isLinkWikipedia";
    public final static String sysPropertyTABCompletedRatioKey = "tabCompletedRatio";
    public final static String sysPropertyTBACompletedRatioKey = "tbaCompletedRatio";
    
    protected final String poolXmlFileName = "wikipedia_pool.xml";
    protected final String loggerFileName = "_ltwAssessTool2011.log";
    protected final String poolAndLogDir = "resources" + File.separator + "Pool";
    protected final String crosslinkURL = "http://ntcir.nii.ac.jp/CrossLink/";
    
	private int globalPoolBackupCounter = 0;
    private ResourcesManager myRSCManager = ResourcesManager.getInstance();
    private JTextPane myTopicPane = null;
    private JTextPane myLinkPane = null;
    
    private boolean showOnce = false;
	private PoolUpdater myPUpdater;
    private PoolerManager myPooler;
    private ResourcesManager rscManager = null;
    
    private static LTWAssessmentToolControler instance = null;
    
    public void setContainter(JTextPane myTopicPane, JTextPane myLinkPane) {
    	this.myTopicPane = myTopicPane;
    	this.myLinkPane = myLinkPane;
    }
    
    public static LTWAssessmentToolControler getInstance() {
        if (instance == null)
            instance = new LTWAssessmentToolControler();
        return instance;
    }
    
    public LTWAssessmentToolControler() {
        rscManager = ResourcesManager.getInstance();
        myPooler = PoolerManager.getInstance();
        myPUpdater = myPooler.getPoolUpdater();
	}

	public Bep goNextLink(boolean updateCurrAnchorStatus, boolean nextUnassessed) {
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
        String topicID = myRSCManager.getTopicID();
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
            return null;
        } /*else if (this.isTAB && tabCompletedRatio[0].equals(tabCompletedRatio[1]) && !tbaCompletedRatio[0].equals(tbaCompletedRatio[1])) {
            JOptionPane.showMessageDialog(this.myTopicPane, "The Outgoing Assessment is completed. \r\n" +
                    "Please click \"OK\" button and switch to Incoming Mode to complete the assessment.\r\n" +
                    "The progress of Incoming is " + tbaCompletedRatio[0] + " out of " + tbaCompletedRatio[1] + ".");
        } else if (!this.isTAB && !tabCompletedRatio[0].equals(tabCompletedRatio[1]) && tbaCompletedRatio[0].equals(tbaCompletedRatio[1])) {
            JOptionPane.showMessageDialog(this.myTopicPane, "The Incoming Assessment is completed. \r\n" +
                    "Please click \"OK\" button and switch to Outgoing Mode to complete the assessment.\r\n" +
                    "The progress of Outgoing is " + tabCompletedRatio[0] + " out of " + tabCompletedRatio[1] + ".");
//        } */
//        else {

                
        return moveForwardALink(updateCurrAnchorStatus, nextUnassessed);
                // ---------------------------------------------------------------------
                // ---------------------------------------------------------------------
                // </editor-fold>
//                // </editor-fold>
//        }
    }
    
    private void backupPool() {
		//      String sourcePoolFPath = "resources" + File.separator + "Pool" + File.separator + "wikipedia_pool.xml";
    	String topicID = myRSCManager.getTopicID();
		String sourcePoolFPath = ltwassessment.Assessment.getPoolFile(topicID);
		File srcFile = new File(sourcePoolFPath);
		String backupPoolDir = Assessment.ASSESSMENT_POOL_BACKUP_DIR;
		DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
		Date date = new Date();
		String currentDateTime = dateFormat.format(date).toString();
		String backupPoolFPath = backupPoolDir + topicID + "_" + currentDateTime + "_Pool.xml";
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
		        Logger.getLogger(LTWAssessmentToolControler.class.getName()).log(Level.SEVERE, null, ex);
		    }
		}
    }
    
    public Bep moveForwardALink(boolean updateCurrAnchorStatus, boolean nextUnassessed) {
        // Click the button to Go Back one Link
    	String currTopicID = rscManager.getTopicID();
            IndexedAnchor poolAnchor = CurrentFocusedAnchor.getCurrentFocusedAnchor().getAnchor().getParent();
            Bep currentBep = CurrentFocusedAnchor.getCurrentFocusedAnchor().getCurrentBep();
            if (updateCurrAnchorStatus) {
            	String currPALinkStatus = myPooler.getPoolAnchorBepLinkStatus(currTopicID, currentBep);
            	int currPAnchorStatus = Integer.parseInt(myPooler.getPoolAnchorStatus(currTopicID, poolAnchor));
            	poolAnchor.setStatus(currPAnchorStatus);
            	int poolAnchorStatus = 0;
            	if (currPALinkStatus.equals("-1")) {
            		if (currPAnchorStatus == -1) {
            			poolAnchorStatus = currPAnchorStatus;
            		} else {
            			poolAnchorStatus = rscManager.getPoolAnchorCompletedStatus(currTopicID, poolAnchor);
            		}
            	} else {
			      poolAnchorStatus = rscManager.getPoolAnchorCompletedStatus(currTopicID, poolAnchor);
            	}
            	poolAnchor.setStatus(poolAnchorStatus);
//            	if (poolAnchorStatus != 0)
            		myPUpdater.updatePoolAnchorStatus(currTopicID, poolAnchor);            	
            }
            // -------------------------------------------------------------
            // 1) Get the NEXT Anchor O, L, S, E, Status + its BEP link O, S, ID, Status
            //    With TAB Nav Update --> NEXT TAB
            Bep nextAnchorBepLinkVSA = rscManager.getNextTABWithUpdateNAV(currTopicID, currentBep, nextUnassessed);

            updateAnchorChanges(nextAnchorBepLinkVSA, currentBep);
            return nextAnchorBepLinkVSA;
    }
    
    public void updateAnchorChanges(Bep nextAnchorBepLinkVSA, Bep currALinkOIDSA) {
    	String currTopicID = rscManager.getTopicID();
    	AssessedAnchor currentAnchor = currALinkOIDSA.getAssociatedAnchor();
    	AssessedAnchor nextAnchor = nextAnchorBepLinkVSA.getAssociatedAnchor();
    	
//        updateAnchor(/*currTopicOLSEStatusSA*/, );
        int currPAnchorStatus = Integer.parseInt(myPooler.getPoolAnchorStatus(currTopicID, currentAnchor));

    if (currentAnchor.getParent() != nextAnchor.getParent()) {
        int poolAnchorStatus = 0;
        if (currPAnchorStatus != 0){
            poolAnchorStatus = currPAnchorStatus;
        } else {
            poolAnchorStatus = rscManager.getPoolAnchorCompletedStatus(currTopicID, currentAnchor);
        }
        currentAnchor.getParent().setStatus(poolAnchorStatus);
        myPUpdater.updatePoolAnchorStatus(currTopicID, currentAnchor.getParent());

    }
		CurrentFocusedAnchor.getCurrentFocusedAnchor().setAnchor(currentAnchor, nextAnchor, nextAnchorBepLinkVSA);
//        updateFields(nextAnchorBepLinkVSA);
    }
    
    private void updateFields(Bep bep) {
        // =================================================================
        // Link Pane
        // =================================================================
//        String bepXmlFilePath = myPooler.getXmlFilePathByTargetID(bep.getFileId(), bep.getTargetLang());
//        if (bepXmlFilePath.startsWith(afTasnCollectionErrors)) {
//            bepXmlFilePath = rscManager.getErrorXmlFilePath(bepXmlFilePath);
//        }
//        Xml2Html xmlParser = new Xml2Html(bepXmlFilePath, Boolean.valueOf(System.getProperty(LTWAssessmentToolControler.sysPropertyIsLinkWikiKey)));
//        String xmlHtmlText = xmlParser.getHtmlContent().toString();
//        linkTextPane.setContentType(textContentType);
//        linkTextPane.setText(xmlHtmlText);
//        linkTextPane.setCaretPosition(0);
//        // -------------------------------------------------------------
//        // -------------------------------------------------------------
//        int screenAnchorStatus = Integer.valueOf(bep.getAssociatedAnchor().getStatus());
//        int linkStatus = bep.getRel(); //Integer.valueOf(link.getStatus());
//        if (screenAnchorStatus == 1 || screenAnchorStatus == 0) {
//            if (linkStatus == 1) {
//                // Relevant --> Insert BEP --> BG = Green
//                Vector<String> bepSCROffset = new Vector<String>();
//                bepSCROffset.add(String.valueOf(bep.getStartP()));
//                boolean isTopicBEP = false;
//                updatePaneBepIcon(linkTextPane, bepSCROffset, isTopicBEP);
//                linkTextPane.getCaret().setDot(bep.getStartP());
//                linkTextPane.scrollRectToVisible(linkTextPane.getVisibleRect());
//                linkTextPane.setBackground(linkPaneRelColor);
//            } else if (linkStatus == -1) {
//                linkTextPane.setBackground(linkPaneNonRelColor);
//            } else if (linkStatus == 0) {
//                linkTextPane.setBackground(linkPaneWhiteColor);
//            }
//            linkTextPane.repaint();
//        } else if (screenAnchorStatus == -1) {
//            linkTextPane.setBackground(linkPaneNonRelColor);
//            linkTextPane.repaint();
//        }    	
        // -------------------------------------------------------------
        // -------------------------------------------------------------
//        String currAnchorName = myPooler.getPoolAnchorNameByOL(currTopicID, new String[]{String.valueOf(screenAnchor.getOffset()), String.valueOf(screenAnchor.getLength())});
//        Vector<String> newTABFieldValues = new Vector<String>();
//        newTABFieldValues.add(currTopicName);
//        newTABFieldValues.add(currTopicID);
//        newTABFieldValues.add(bep.getAssociatedAnchor().getName());
//        newTABFieldValues.add(bep.getFileId());
//        String pageTitle = "";
////        if (Boolean.valueOf(System.getProperty(sysPropertyIsLinkWikiKey))) {
//            pageTitle = rscManager.getWikipediaPageTitle(bep.getFileId());
////        } else {
////            pageTitle = rscManager.getTeAraFilePathByName(nextLinkID);
////        }
//        newTABFieldValues.add(pageTitle.trim());
//        String[] pAnchorCompletionSA = rscManager.getOutgoingCompletion();
//        newTABFieldValues.add(pAnchorCompletionSA[0] + " / " + pAnchorCompletionSA[1]);
//        os.setTABFieldValues(bep);
    }
}
