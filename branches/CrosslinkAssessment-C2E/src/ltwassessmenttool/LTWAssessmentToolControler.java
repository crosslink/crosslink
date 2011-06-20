package ltwassessmenttool;

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

import ltwassessment.parsers.ResourcesManager;
import ltwassessment.utility.BrowserControl;
import ltwassessmenttool.listener.linkPaneMouseListener;

public class LTWAssessmentToolControler {
	public final static String sysPropertyIsTABKey = "isTABKey";
	public final static String sysPropertyIsTopicWikiKey = "isTopicWikipedia";
    public final static String sysPropertyIsLinkWikiKey = "isLinkWikipedia";
    public final static String sysPropertyTABCompletedRatioKey = "tabCompletedRatio";
    public final static String sysPropertyTBACompletedRatioKey = "tbaCompletedRatio";
    
    public final static String sysPropertyCurrTopicOLSEStatusKey = "currTopicOLSE";
    
    protected final String poolXmlFileName = "wikipedia_pool.xml";
    protected final String loggerFileName = "_ltwAssessTool2011.log";
    protected final String poolAndLogDir = "resources" + File.separator + "Pool";
    protected final String crosslinkURL = "http://ntcir.nii.ac.jp/CrossLink/";
    
	private int globalPoolBackupCounter = 0;
    private ResourcesManager myRSCManager = ResourcesManager.getInstance();
    private JTextPane myTopicPane = null;
    private JTextPane myLinkPane = null;
    
    private boolean showOnce = false;
    
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
    
    public void goNextLink(boolean updateCurrAnchorStatus, boolean nextUnassessed) {
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

                
                LTWAssessmentToolView.moveForwardALink(updateCurrAnchorStatus, nextUnassessed);
                // ---------------------------------------------------------------------
                // ---------------------------------------------------------------------
                // </editor-fold>
//                // </editor-fold>
        }
    }
    
    private void backupPool() {
		//      String sourcePoolFPath = "resources" + File.separator + "Pool" + File.separator + "wikipedia_pool.xml";
    	String topicID = myRSCManager.getTopicID();
		String sourcePoolFPath = ltwassessment.Assessment.getPoolFile(topicID);
		File srcFile = new File(sourcePoolFPath);
		String backupPoolDir = "resources" + File.separator + "Pool" + File.separator + "POOL_BACKUP" + File.separator;
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
		        Logger.getLogger(linkPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
		    }
		}
    }
    
    public static void setCurrentAnchorProperty(int offset, int length, int screenPosStart, int screenPosEnd, int status, int extLength) {
    	setCurrentAnchorProperty(String.valueOf(offset), String.valueOf(length), String.valueOf(screenPosStart), String.valueOf(screenPosEnd), String.valueOf(status), String.valueOf(extLength));
    }
    
    public static void setCurrentAnchorProperty(String offsetStr, String lengthStr, String screenPosStartStr, String screenPosEndStr, String statusStr, String extLengthStr) {
    	String sysPropertyValue = offsetStr + "_" + lengthStr + "_" + screenPosStartStr + "_" + screenPosEndStr + "_" + statusStr + "_" + extLengthStr;
    	System.setProperty(sysPropertyCurrTopicOLSEStatusKey, sysPropertyValue);
    }
}
