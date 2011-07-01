package ltwassessment.assessment;

import java.awt.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;

import ltwassessment.AppResource;
import ltwassessment.Assessment;
import ltwassessment.assessment.Bep;
import ltwassessment.assessment.CurrentFocusedAnchor;
import ltwassessment.assessment.IndexedAnchor;
import ltwassessment.font.AdjustFont;
import ltwassessment.listener.CaretListenerLabel;
import ltwassessment.listener.linkPaneMouseListener;
import ltwassessment.listener.topicPaneMouseListener;
import ltwassessment.parsers.FOLTXTMatcher;
import ltwassessment.parsers.PoolerManager;
import ltwassessment.parsers.ResourcesManager;
import ltwassessment.parsers.Xml2Html;
import ltwassessment.utility.BrowserControl;
import ltwassessment.utility.PoolUpdater;
import ltwassessment.view.TopicHighlightManager;

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
    private JTextPane myTopicPane = null;
    private JTextPane myLinkPane = null;
    
    private String currTopicFilePath = "";
    
    private boolean showOnce = false;
	private PoolUpdater myPUpdater;
    private PoolerManager myPooler;
    private ResourcesManager rscManager = null;
    private JFrame mainFrame = null;
    
    private Hashtable<String, Vector<IndexedAnchor>> topicAnchorsHT = new Hashtable<String, Vector<IndexedAnchor>>();
    private Hashtable<String, Vector<String[]>> topicSubanchorsHT = new Hashtable<String, Vector<String[]>>();
    private Hashtable<String, Vector<String[]>> topicBepsHT = new Hashtable<String, Vector<String[]>>();
    
    private static String currTopicID = "";
    private static String currTopicName = "";
    // -------------------------------------------------------------------------
    private Vector<IndexedAnchor> topicAnchorOLSEStatus; // = new Vector<String[]>();
	private JComponent lblTargetTitle;
	private JLabel statusMessageLabel;
	private JComponent lblPoolAnchor; // subanchor
	private JComponent lblAnchor;
	private JComponent lblTopicTitle;
	private ResourceMap resourceMap;
    
    private static String textContentType = "";
    
    private static String bepIconImageFilePath = "";
    private static String bepIconCompleted = "";
    private static String bepIconNonrelevant = "";
    private static String bepIconHighlight = "";

	private static LTWAssessmentToolControler instance = null;
    
    public void setContainter(JFrame mainView, JTextPane myTopicPane, JTextPane myLinkPane) {
    	this.mainFrame = mainView;
    	this.myTopicPane = myTopicPane;
    	this.myLinkPane = myLinkPane;
    }
    
    public static void setCurrTopicID(String currTopicID) {
		LTWAssessmentToolControler.currTopicID = currTopicID;
	}

	public static void setTextContentType(String textContentType) {
		LTWAssessmentToolControler.textContentType = textContentType;
	}
    
    public void setLblTargetTitle(JComponent lblTargetTitle) {
		this.lblTargetTitle = lblTargetTitle;
	}

	public void setStatusMessageLabel(JLabel statusMessageLabel) {
		this.statusMessageLabel = statusMessageLabel;
	}

	public void setLblPoolAnchor(JComponent lblPoolAnchor) {
		this.lblPoolAnchor = lblPoolAnchor;
	}
	
	public void setLblAnchor(JComponent lblAnchor) {
		this.lblAnchor = lblAnchor;
	}

	public void setLblTopicTitle(JComponent lblTopicTitle) {
		this.lblTopicTitle = lblTopicTitle;
	}

	public static LTWAssessmentToolControler getInstance() {
        if (instance == null)
            instance = new LTWAssessmentToolControler();
        return instance;
    }
    
    public LTWAssessmentToolControler() {
    	resourceMap = AppResource.getInstance().getResourceMap();
    	
        rscManager = ResourcesManager.getInstance();
        myPooler = PoolerManager.getInstance();
        myPUpdater = myPooler.getPoolUpdater();
        
        bepIconImageFilePath = resourceMap.getString("bepIcon.imageFilePath");
        bepIconCompleted = resourceMap.getString("bepCompletedIcon.imageFilePath");
        bepIconNonrelevant = resourceMap.getString("bepNonrelevantIcon.imageFilePath");
        bepIconHighlight = resourceMap.getString("bepHighlightIcon.imageFilePath");
	}
    
    public void assessNextTopic() {
//    	currTopicID = rscManager.getTopicID();
    	if (currTopicID != null && currTopicID.length() > 0)
    		Assessment.getInstance().finishTopic(currTopicID);
    	currTopicID = Assessment.getInstance().getNextTopic();
    	assess(Assessment.getPoolFile(currTopicID), true);
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
        String[] tabCompletedRatio = this.rscManager.getOutgoingCompletion();
        String topicID = rscManager.getTopicID();
//        String[] tbaCompletedRatio = this.myRSCManager.getIncomingCompletion();
        if (Integer.parseInt(tabCompletedRatio[0]) > 0 && tabCompletedRatio[0].equals(tabCompletedRatio[1])  && !showOnce /* && tbaCompletedRatio[0].equals(tbaCompletedRatio[1])*/) {
            int option = JOptionPane.showConfirmDialog(this.myTopicPane, "The Assessment is completed.\r\n" +
                    "Please zip the result file and log together \r\n" +
                    "and email it to the Crosslink organisers or other alternative way provided. \r\n" +
                    "The files, " + poolXmlFileName + " & T" + topicID + loggerFileName + " are located in the following directory: \r\n" +
                    poolAndLogDir, "Assessment Completion",
                    JOptionPane.OK_OPTION);
            if (option == JOptionPane.OK_OPTION || option == JOptionPane.CLOSED_OPTION) {
//                BrowserControl openBrowser = new BrowserControl();
//                openBrowser.displayURL(crosslinkURL);
//                javax.swing.SwingUtilities.getWindowAncestor(this.myLinkPane).setVisible(false);
//                javax.swing.SwingUtilities.getWindowAncestor(this.myLinkPane).dispose();
                assessNextTopic();
//                System.exit(0);
            }
            showOnce = true;
//            return null;
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

                
        moveForwardALink(updateCurrAnchorStatus, nextUnassessed);
                // ---------------------------------------------------------------------
                // ---------------------------------------------------------------------
                // </editor-fold>
//                // </editor-fold>
//        }
    }
    
	public void setSubanchorIrrelevant(AssessedAnchor anchor) {
        int newCompletedCounter = 0;
        int prePAnchorStatus = anchor.getStatus();
        String currTopicID = rscManager.getTopicID();
        
        for (Bep aBep : anchor.getBeps())
        	if (aBep.getRel() != -1) {
        		if (aBep.getRel() == 0)
        			newCompletedCounter++;
        		
        		aBep.setRel(-1);
        	}
        if (prePAnchorStatus == 1 || prePAnchorStatus == 0) {
            // <editor-fold defaultstate="collapsed" desc="Toggle to NONRelevant -1">
            // Toggle to NONRelevant -1, because it was 1 or 0
        	prePAnchorStatus = -1;
            
            anchor.setStatus(prePAnchorStatus);
            myPUpdater.updatePoolSubanchorStatus(currTopicID, anchor);
            
            // updare Pool XML

            // </editor-fold>
        } else if (prePAnchorStatus == -1) {
            // <editor-fold defaultstate="collapsed" desc="Toggle to PRE-STATUS: 1 or 0">
            // Toggle to PRE-STATUS: 1 or 0
            // b) Set Link Pane BG back to Pre-Status
            // -------------------------------------------------------------
            // update Completion Ratio
            int unAssCounter = 0;
            int nonRelCounter = 0;
            Vector<String> pAnchorAllLinkStatus = myPooler.getPoolSubanchorAllLinkStatus(currTopicID, anchor);
            for (String pAnchorLinkStatus : pAnchorAllLinkStatus) {
                if (pAnchorLinkStatus.equals("0")) {
                    unAssCounter++;
                } else if (pAnchorLinkStatus.equals("-1")) {
                    nonRelCounter++;
                }

            }
            int toPAnchorStatus = Bep.UNASSESSED;
            if (nonRelCounter == pAnchorAllLinkStatus.size()) {
                toPAnchorStatus = -1;
            } else if (unAssCounter > 0) {
                toPAnchorStatus = 0;
            } else {
                toPAnchorStatus = 1;
            }


			anchor.setStatus(toPAnchorStatus);
			
			newCompletedCounter -= unAssCounter;
			myPUpdater.updatePoolSubanchorStatus(currTopicID, anchor);
        }
        String[] outCompletionRatio = rscManager.getOutgoingCompletion();
        String outCompletedLinks = String.valueOf(Integer.valueOf(outCompletionRatio[0]) + newCompletedCounter);
        rscManager.updateOutgoingCompletion(outCompletedLinks + " : " + outCompletionRatio[1]);  
	}
	
    private void backupPool() {
		//      String sourcePoolFPath = "resources" + File.separator + "Pool" + File.separator + "wikipedia_pool.xml";
    	String topicID = rscManager.getTopicID();
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
    
    public void moveForwardALink(boolean updateCurrAnchorStatus, boolean nextUnassessed) {
        // Click the button to Go Back one Link
    	String currTopicID = rscManager.getTopicID();
            IndexedAnchor poolAnchor = CurrentFocusedAnchor.getCurrentFocusedAnchor().getAnchor().getParent();
            Bep currentBep = CurrentFocusedAnchor.getCurrentFocusedAnchor().getCurrentBep();
//            if (updateCurrAnchorStatus) {
//            	String currPALinkStatus = myPooler.getPoolAnchorBepLinkStatus(currTopicID, currentBep);
//            	int currPAnchorStatus = Integer.parseInt(myPooler.getPoolAnchorStatus(currTopicID, poolAnchor));
//            	poolAnchor.setStatus(currPAnchorStatus);
//            	int poolAnchorStatus = 0;
//            	if (currPALinkStatus.equals("-1")) {
//            		if (currPAnchorStatus == -1) {
//            			poolAnchorStatus = currPAnchorStatus;
//            		} else {
//            			poolAnchorStatus = rscManager.getPoolAnchorCompletedStatus(currTopicID, poolAnchor);
//            		}
//            	} else {
//			      poolAnchorStatus = rscManager.getPoolAnchorCompletedStatus(currTopicID, poolAnchor);
//            	}
//            	poolAnchor.setStatus(poolAnchorStatus);
////            	if (poolAnchorStatus != 0)
//            		myPUpdater.updatePoolAnchorStatus(currTopicID, poolAnchor);            	
//            }
            // -------------------------------------------------------------
            // 1) Get the NEXT Anchor O, L, S, E, Status + its BEP link O, S, ID, Status
            //    With TAB Nav Update --> NEXT TAB
            Bep nextAnchorBepLinkVSA = rscManager.getNextTABWithUpdateNAV(currTopicID, currentBep, nextUnassessed);
            
            if (updateCurrAnchorStatus && (nextAnchorBepLinkVSA.getAssociatedAnchor().getParent() != currentBep.getAssociatedAnchor().getParent()))
            	currentBep.getAssociatedAnchor().getParent().statusCheck();

//            updateAnchorChanges(nextAnchorBepLinkVSA, currentBep);
//            return nextAnchorBepLinkVSA;
            CurrentFocusedAnchor.getCurrentFocusedAnchor().setAnchor(currentBep.getAssociatedAnchor(), nextAnchorBepLinkVSA.getAssociatedAnchor(), nextAnchorBepLinkVSA);
    }
    
//    public void updateAnchorChanges(Bep nextAnchorBepLinkVSA, Bep currALinkOIDSA) {
//    	String currTopicID = rscManager.getTopicID();
//    	AssessedAnchor currentAnchor = currALinkOIDSA.getAssociatedAnchor();
//    	AssessedAnchor nextAnchor = nextAnchorBepLinkVSA.getAssociatedAnchor();
//    	
////        updateAnchor(/*currTopicOLSEStatusSA*/, );
//        int currPAnchorStatus = Integer.parseInt(myPooler.getPoolAnchorStatus(currTopicID, currentAnchor));
//
//    if (currentAnchor.getParent() != nextAnchor.getParent()) {
//        int poolAnchorStatus = 0;
//        if (currPAnchorStatus != 0){
//            poolAnchorStatus = currPAnchorStatus;
//        } else {
//            poolAnchorStatus = rscManager.getPoolAnchorCompletedStatus(currTopicID, currentAnchor);
//        }
//        currentAnchor.getParent().setStatus(poolAnchorStatus);
//        myPUpdater.updatePoolAnchorStatus(currTopicID, currentAnchor.getParent());
//
//    }
//		CurrentFocusedAnchor.getCurrentFocusedAnchor().setAnchor(currentAnchor, nextAnchor, nextAnchorBepLinkVSA);
////        updateFields(nextAnchorBepLinkVSA);
//    }
    
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
    
    public void assess(String poolFile) {
    	assess(poolFile, false);
    }
    
    private void assess(String poolFile, boolean reset) {
    	if (!new File(poolFile).exists()) {
          String errMessage = "Cannot find pool file: " + poolFile + "\r\n";
			JOptionPane.showMessageDialog(mainFrame, errMessage);    		
    		return;
    	}
    	
        rscManager = ResourcesManager.getInstance();
        myPooler = PoolerManager.getInstance(poolFile);
        myPUpdater = myPooler.getPoolUpdater();
        
        AssessmentThread.setMyPoolManager(myPooler);
        AssessmentThread.setMyRSCManager(rscManager);
        AssessmentThread.setMyPoolUpdater(myPUpdater);
        
        topicAnchorsHT = myPooler.getTopicAllAnchors();
       
        if (reset) {
        	resetResouceTopic(AppResource.sourceLang);
        } 
        else {
	        String id = rscManager.getTopicID(); 
	        if (!id.equals(currTopicID)) {
		        Vector<String[]> topicIDNameVSA = this.myPooler.getAllTopicsInPool();
		    	currTopicID = topicIDNameVSA.elementAt(0)[0].trim();
		    	
		        String topicLang = topicIDNameVSA.elementAt(0)[2];
		        resetResouceTopic(topicLang);
	        }
        }

        setupTopic();        
        setupComponentFont();
        
        Assessment.getInstance().setCurrentTopicWithId(currTopicID);
        currTopicName = Assessment.getInstance().getCurrentTopic().getTitle();
        rscManager.pullPoolData();
    	
        // -------------------------------------------------------------
        // scrSE, String[]{O,L,TXT,S,E,num}
//      topicAnchorOLTSENHT = populateTopicAnchorOLTSENHT();
        // -------------------------------------------------------------
        CaretListenerLabel caretListenerLabel = new CaretListenerLabel("Caret Status", myTopicPane, statusMessageLabel);
        myTopicPane.addCaretListener(caretListenerLabel);
        topicPaneMouseListener mtTopicPaneListener = new topicPaneMouseListener(myTopicPane, myLinkPane);
        myTopicPane.addMouseListener(mtTopicPaneListener);
        myTopicPane.addMouseMotionListener(mtTopicPaneListener);
        linkPaneMouseListener myLPMListener = new linkPaneMouseListener(myTopicPane, myLinkPane);
        myLinkPane.addMouseListener(myLPMListener);
        // -------------------------------------------------------------
//        this.outRadioBtn.setSelected(true);
//        this.inRadioBtn.setSelected(false);
        // -------------------------------------------------------------
        setOutgoingTAB();
    }
    
    private void setOutgoingTAB() {
        // 0) set System property to TAB Outgoing
        // 1) populate Topic Pane <-- only one Topic
        // 2) --> Check AnchorOL Status
        // 3) highlight Topic Pane Anchors
        // 4) indciate CURRENT Topic Anchor Text & Target Link
        System.setProperty(LTWAssessmentToolControler.sysPropertyIsTABKey, "true");
        rscManager.updateLinkingMode("outgoing");
        // ---------------------------------------------------------------------
        // Get Pool Properties
        // Hashtable<outgoing : topicFileID>: [0]:Offset, [1]:Length, [2]:Anchor_Name
//        topicSubanchorsHT = myPooler.getTopicAllSubanchors();
        // ---------------------------------------------------------------------
        // 1) Get Topic ID & xmlFile Path
        //    SET Topic Text Pane Content
        // 2) Get all Anchor Text SCR SE + Status
        //    SET Highlighter + Curr Anchor Text
        // 3) Get Target Link
        //    1st un-assessed Link, belonging to Curr Anchor Text

        //currTopicName = topicIDNameVSA.elementAt(0)[1].trim();
        String currTopicID = rscManager.getTopicID();
        String currTopicFilePath = rscManager.getCurrTopicXmlFile();
//        currTopicName = new WikiArticleXml(currTopicFilePath).getTitle();
        String topicLang = rscManager.getTopicLang();
        setTopicPaneContent(currTopicFilePath, topicLang);

        // ---------------------------------------------------------------------
        // For 1st time to get the ANCHOR OL name SE
//        folMatcher = FOLTXTMatcher.getInstance();
        Vector<String> topicAnchorsOLNameSEVS = rscManager.getTopicAnchorsOLNameSEV();
        if (topicAnchorsOLNameSEVS.size() == 0) {
        	FOLTXTMatcher.getInstance().getSCRAnchorPosV(myTopicPane, currTopicID, topicAnchorsHT);
        	topicAnchorsOLNameSEVS = rscManager.getTopicAnchorsOLNameSEV();
        }
        
        rscManager.checkAnchorStatus();
        
        /*************************************************************************
         * Step 2
         * 
         * setup the screen start and end position of anchors
         *************************************************************************/
        rscManager.setTopicAnchorOLStatusBySE();
        topicAnchorOLSEStatus = rscManager.getPoolAnchorsOLNameStatusV();
        
        /*************************************************************************
         * Step 3
         * 
         ************************************************************************/
        TopicHighlightManager.getInstance().initializeHighlighter(topicAnchorOLSEStatus);
        
        String[] tabCompletedRatio = this.rscManager.getTABCompletedRatio();
        this.rscManager.updateOutgoingCompletion(tabCompletedRatio[0] + " : " + tabCompletedRatio[1]);
        System.setProperty(LTWAssessmentToolControler.sysPropertyTABCompletedRatioKey, String.valueOf(tabCompletedRatio[0]) + "_" + String.valueOf(tabCompletedRatio[1]));
        // ---------------------------------------------------------------------
        
        // String[]{Anchor_O, L, SP, EP, Status}
//        topicAnchorOLSEStatus = rscManager.getTopicAnchorOLSEStatusVSA();
        // String[]{Anchor_O, L, Name, SP, EP, Status}
        String[] currTopicOLNameSEStatus = rscManager.getCurrTopicAnchorOLNameSEStatusSA(myTopicPane, currTopicID, topicAnchorsOLNameSEVS);
        String currTopicPAnchorStatus = currTopicOLNameSEStatus[5];
        // ---------------------------------------------------------------------
        // Get current Link file ID & SCR BEP S, lang, title
        Bep CurrTopicATargetOID = rscManager.getCurrTopicATargetOID(myLinkPane, currTopicID);
        String currTargetOffset = CurrTopicATargetOID.offsetToString(); //[0];
        String currTargetID = CurrTopicATargetOID.getFileId(); //[1];
        String currTargetLang = CurrTopicATargetOID.getTargetLang(); //[2];
        String pageTitle = CurrTopicATargetOID.getTargetTitle(); //[3];;
        if (currTargetID.endsWith("\"")) {
            currTargetID = currTargetID.substring(0, currTargetID.length() - 1);
        }
        String currTargetFilePath = rscManager.getWikipediaFilePathByName(currTargetID + ".xml", currTargetLang);
        
//        os.setTABFieldValues(CurrTopicATargetOID);
        
        /*************************************************************************
         * Step 5
         * 
         ************************************************************************/
//        TopicHighlightManager.getInstance().update(null, CurrTopicATargetOID.getAssociatedAnchor());
        CurrentFocusedAnchor.getCurrentFocusedAnchor().setAnchor(null, CurrTopicATargetOID.getAssociatedAnchor(), CurrTopicATargetOID);

        setTABLinkPaneContent(currTargetFilePath, currTargetLang);
        // bep_Offset, linkID, Status
        String[] CurrTopicATargetSIDStatus = rscManager.getCurrTopicABepSIDStatusSA(myLinkPane, currTopicID);
        setLinkBEPIcon(currTopicPAnchorStatus, CurrTopicATargetSIDStatus);

    }
    
    // Topic Pane: Anchor
    private void setTopicPaneContent(String xmlFilePath, String lang) {
    	AdjustFont.setComponentFont(myTopicPane, lang);
        if (!xmlFilePath.equals("")) {
            createTopicTextPane(xmlFilePath);
        } else {
            myTopicPane.setContentType(textContentType);
            myTopicPane.setText("<b>The topic file is missing or specified wrongly!!!</b>");
        }
    }
    
    private void setupComponentFont() {
    	AdjustFont.setComponentFont(lblTopicTitle, AppResource.sourceLang);
    	AdjustFont.setComponentFont(lblPoolAnchor, AppResource.sourceLang);
    	AdjustFont.setComponentFont(lblAnchor, AppResource.sourceLang);
    }
    
    private void setupTopic() {
        currTopicFilePath = rscManager.getCurrTopicXmlFile();
		//      currTopicName = new WikiArticleXml(currTopicFilePath).getTitle();
		String topicLang = rscManager.getTopicLang();
		setTopicPaneContent(currTopicFilePath, topicLang);
		FOLTXTMatcher.getInstance().getCurrFullXmlText();
    	    	
    	FOLTXTMatcher.getInstance().getSCRAnchorPosV(myTopicPane, currTopicID, topicAnchorsHT);
//    	Vector<String> topicAnchorsOLNameSEVS = rscManager.getTopicAnchorsOLNameSEV();
//        int completedAnchor = 0;
//		//      int totoalAnchorNumber = Integer.valueOf(tabCompletedRatio[1]);
//		
//		int totoalAnchorNumberRecored = topicAnchorsOLNameSEVS.size();
//		
//		this.rscManager.updateOutgoingCompletion(String.valueOf(completedAnchor) + " : " + String.valueOf(totoalAnchorNumberRecored));
    }
    
    private void resetResouceTopic(String topicLang) {
    	currTopicFilePath = AppResource.getInstance().getTopicXmlPathNameByFileID(currTopicID, topicLang); //rscManager.getTopicFilePath(currTopicID, topicLang);
    	rscManager.updateTopicID(currTopicID + ":" + topicLang);
    	rscManager.updateCurrTopicFilePath(currTopicFilePath);
    	rscManager.updateCurrAnchorFOL(null);
    	rscManager.updateTABNavigationIndex(new String[]{"0", "0", "0", "0", "0"});
    }
    
    private void createLinkTextPane(String xmlFilePath, String lang) {
    	AdjustFont.getInstance().setComponentFont(myLinkPane, lang);
    	AdjustFont.getInstance().setComponentFont(lblTargetTitle, lang);
        myLinkPane.setCaretPosition(0);
        myLinkPane.setMargin(new Insets(5, 5, 5, 5));
        initLinkDocument(xmlFilePath);
        myLinkPane.setCaretPosition(0);
    }
    
    private void setTABLinkPaneContent(String xmlFilePath, String lang) {
        if (!xmlFilePath.equals("")) {
            createLinkTextPane(xmlFilePath, lang);
        } else {
            myLinkPane.setContentType(textContentType);
            myLinkPane.setText("<b>The target file is missing or specified wrongly!!!</b>");
        }
    }
    
    private void createTopicTextPane(String xmlFilePath) {
        myTopicPane.setCaretPosition(0);
        myTopicPane.setMargin(new Insets(5, 5, 5, 5));
        initTopicDocument(xmlFilePath);
        myTopicPane.setCaretPosition(0);
    }

    private void initTopicDocument(String xmlFilePath) {
        Xml2Html xmlParser = new Xml2Html(xmlFilePath, true);
        myTopicPane.setContentType(textContentType);
        myTopicPane.setText(xmlParser.getHtmlContent().toString());
    }
    

    private void initLinkDocument(String xmlFilePath) {
        Xml2Html xmlParser = new Xml2Html(xmlFilePath, true);
        myLinkPane.setContentType(textContentType);
        myLinkPane.setText(xmlParser.getHtmlContent().toString());
    }
    
    private void setLinkBEPIcon(String currTopicPAnchorStatus, String[] CurrTopicATargetSIDStatus) {
        // Insert into Doc in JTextPane
        ImageIcon bepIcon = new ImageIcon(bepIconImageFilePath);
        myLinkPane.insertIcon(bepIcon);
        myLinkPane.repaint();
        // bepSP: -1 or > -1
        String bepSP = CurrTopicATargetSIDStatus[0].trim();
        // bepStatus: 0 , -1 , 1
        String bepStatus = CurrTopicATargetSIDStatus[2].trim();
        if (Integer.valueOf(bepSP) > -1) {
            StyledDocument styDoc = (StyledDocument) myLinkPane.getDocument();
            Style bepStyle = styDoc.addStyle("bepIcon", null);
            StyleConstants.setIcon(bepStyle, new ImageIcon(bepIconImageFilePath));
            try {
                styDoc.insertString(Integer.valueOf(bepSP), "TBEP", bepStyle);
            } catch (BadLocationException ex) {
                Logger.getLogger(topicPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
