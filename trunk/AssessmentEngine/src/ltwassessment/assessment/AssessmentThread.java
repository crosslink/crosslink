package ltwassessment.assessment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import ltwassessment.AppResource;
import ltwassessment.parsers.FOLTXTMatcher;
import ltwassessment.parsers.PoolerManager;
import ltwassessment.parsers.ResourcesManager;
import ltwassessment.utility.PoolUpdater;
import ltwassessment.utility.highlightPainters;
import ltwassessment.utility.tabTxtPaneManager;
import ltwassessment.utility.tbaTxtPaneManager;
import ltwassessment.view.TopicHighlightManager;



public class AssessmentThread extends Thread {
    private JTextPane myTopicPane;
    private JTextPane myLinkPane;
    
	private static ResourcesManager myRSCManager;
    private static PoolUpdater myPoolUpdater;
    private static PoolerManager myPoolManager;
    
    private static AssessedAnchor processingAnchor = null;
    
    private FOLTXTMatcher myFolMatcher;
    
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

    public static final int EVENT_SET_NOTHING = -1;
    public static final int EVENT_SET_BEP = 0;
    public static final int EVENT_SET_RELEVANT = 1;
    public static final int EVENT_SET_IRRELEVANT = 2;
    public static final int EVENT_SET_SUBANCHOR_IRRELEVANT = 3;
    public static final int EVENT_SET_SUBANCHORS_IRRELEVANT = 4;
    public static final int EVENT_NEW_ASSESSMENT = 5;
    
    private static boolean readyForNextLink = true;
    private static int task = EVENT_SET_NOTHING;

    private Hashtable<String, String[]> topicAnchorOLTSENHT = new Hashtable<String, String[]>();
    private Hashtable<String, Object> myTAnchorSEHiObj = new Hashtable<String, Object>();
    
//    private static AssessmentThread instance = null;   

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
            Logger.getLogger(AssessmentThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public AssessmentThread(JTextPane myTopicPane, JTextPane myLinkPane) {
		super();
        this.myTopicPane = myTopicPane;
        this.myLinkPane = myLinkPane;
        
        org.jdesktop.application.ResourceMap resourceMap = AppResource.getInstance().getResourceMap();
        afTasnCollectionErrors = resourceMap.getString("AssFormXml.taskCollectionError");
        bepIconImageFilePath = resourceMap.getString("bepIcon.imageFilePath");
        bepHighlightIconImageFilePath = resourceMap.getString("bepHighlightIcon.imageFilePath");
        bepCompletedIconImageFilePath = resourceMap.getString("bepCompletedIcon.imageFilePath");
        bepNonrelevantIconImageFilePath = resourceMap.getString("bepNonrelevantIcon.imageFilePath");
        paneContentType = resourceMap.getString("html.content.type");

        myRSCManager = ResourcesManager.getInstance();
        myFolMatcher = FOLTXTMatcher.getInstance();
        myTABTxtPaneManager = new tabTxtPaneManager();
        myTBATxtPaneManager = new tbaTxtPaneManager();

        // ---------------------------------------------------------------------
        // In case:
        // Outgoing Anchor TXT: scrSE, String[]{O,L,TXT,S,E,num}
        // Incoming BEP: scrS, String[]{O,S, num}
//        this.topicAnchorOLTSENHT = topicOLTSEIndex;
        // ---------------------------------------------------------------------

//        Vector<String[]> topicIDNameVSA = AssessmentThread.myPoolManager.getAllTopicsInPool();
//        this.currTopicName = topicIDNameVSA.elementAt(0)[1].trim();
	}

    private void newAssessment() {
        this.topicID = myRSCManager.getTopicID();
        myPoolManager = PoolerManager.getInstance();
        myPoolUpdater = PoolerManager.getPoolUpdater();
    }
    
    public static void setMyRSCManager(ResourcesManager myRSCManager) {
		AssessmentThread.myRSCManager = myRSCManager;
	}

	public static void setMyPoolUpdater(PoolUpdater myPoolUpdater) {
		AssessmentThread.myPoolUpdater = myPoolUpdater;
	}

	public static void setMyPoolManager(PoolerManager myPoolManager) {
		AssessmentThread.myPoolManager = myPoolManager;
	}
	
    public static AssessedAnchor getProcessingAnchor() {
		return processingAnchor;
	}

	public static void setProcessingAnchor(AssessedAnchor anchor) {
		processingAnchor = anchor;
	}

	public static boolean isReadyForNextLink() {
		return readyForNextLink;
	}

	private static void setReadyForNextLink(boolean ready) {
		readyForNextLink = ready;
	}
	
	public static synchronized int getTask() {
		return task;
	}

	public static synchronized void setTask(int aTask) {
		task = aTask;
	}

	public void run() {
		while (true) {
			if (getTask() != EVENT_SET_NOTHING) {
				try {
					setReadyForNextLink(false);
					switch (task) {
					case EVENT_SET_BEP:
						this.doubleLeftClickEventAction();
						break;
					case EVENT_SET_RELEVANT:
						this.singleLeftClickEventAction();
						break;
					case EVENT_SET_IRRELEVANT:
						this.singleRightClickEventAction();
						break;
					case EVENT_SET_SUBANCHOR_IRRELEVANT:
						this.markCurrentSubanchorIrrelevant(true);
						break;
					case EVENT_SET_SUBANCHORS_IRRELEVANT:
						this.markCurrentSubanchorIrrelevant(false);
						break;
					default:
						break;
					}
					
					Thread.sleep(100);
					Thread.sleep(1000);
				}
				catch (InterruptedException e) {
						e.printStackTrace();
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
				finally {
					setTask(EVENT_SET_NOTHING);
					setReadyForNextLink(true);
				}

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
//            String currALinkStatus = this.myPoolManager.getPoolAnchorBepLinkStatus(topicID, currentLink);
//            if (currALinkStatus.equals("0")) {
//                String[] outCompletion = this.myRSCManager.getOutgoingCompletion();
//                String completedLinkN = outCompletion[0];
//                String totalLinkN = outCompletion[1];
//                completedLinkN = String.valueOf(Integer.valueOf(completedLinkN) + 1);
//                this.myRSCManager.updateOutgoingCompletion(completedLinkN + " : " + totalLinkN);
//            }
            // -----------------------------------------------------------------
//            this.myLinkPane.setBackground(this.linkPaneNonRelColor);
//            this.myLinkPane.repaint();

            currentLink.setRel(Bep.IRRELEVANT);
            String currLinkStatus = currentLink.relString();
            this.myPoolUpdater.updateTopicAnchorLinkRel(topicID, currentLink);
            // -----------------------------------------------------------------
            AssessedAnchor previous =  currentLink.getAssociatedAnchor();
            LTWAssessmentToolControler.getInstance().goNextLink(true, true);
//            CurrentFocusedAnchor.getCurrentFocusedAnchor().setAnchor(previous, link.getAssociatedAnchor(), link);
    }
    
//    private void updateRelevantCompletion(Bep link) {
////        String currALinkStatus = this.myPoolManager.getPoolAnchorBepLinkStatus(topicID, currAnchorOLNameStatusSA/*new String[]{currAnchorOLNameStatusSA[0], currAnchorOLNameStatusSA[1]}*/, currALinkID);
//        if (link.getRel() ==0 ) {
//            String[] outCompletion = AssessmentThread.myRSCManager.getOutgoingCompletion();
//            String completedLinkN = outCompletion[0];
//            String totalLinkN = outCompletion[1];
//            completedLinkN = String.valueOf(Integer.valueOf(completedLinkN) + 1);
//            AssessmentThread.myRSCManager.updateOutgoingCompletion(completedLinkN + " : " + totalLinkN);
//        }
//    }
    
    private void setRelevant(Bep currentLink, boolean setBep) {
        String currALinkStatus = AssessmentThread.myPoolManager.getPoolAnchorBepLinkStatus(topicID, currentLink);
//        if (currALinkStatus.equals("0")) 
//        	updateRelevantCompletion(currentLink);  	
        
        currentLink.setRel(Bep.RELEVANT);
        if (setBep)
        	AssessmentThread.myPoolUpdater.updateTopicAnchorLinkOSStatus(topicID, currentLink.getAssociatedAnchor().getParent(), currentLink);
        else
        	AssessmentThread.myPoolUpdater.updateTopicAnchorLinkRel(topicID,  currentLink);
        
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
                int preTBStartPoint = AssessmentThread.myPoolManager.getPABepLinkStartP(topicID, currentLink.getAssociatedAnchor().getParent()/*new String[]{currAnchorOLNameStatusSA[0], currAnchorOLNameStatusSA[1]}*/, currALinkID);
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
                String[] thisPAnchorCRatio = AssessmentThread.myRSCManager.getPoolAnchorCompletedRatio(topicID, currentLink.getAssociatedAnchor().getParent()/*new String[]{currAnchorOLNameStatusSA[0], currAnchorOLNameStatusSA[1]}*/);
                if (thisPAnchorCRatio[0].equals(thisPAnchorCRatio[1])) {
//                    String poolAnchorStatus = "1";
                	currentLink.getAssociatedAnchor().getParent().setStatus(Bep.RELEVANT);
                    AssessmentThread.myPoolUpdater.updatePoolAnchorStatus(topicID, currentLink.getAssociatedAnchor().getParent());
                } else {
//                    String poolAnchorStatus = "0";
                	currentLink.getAssociatedAnchor().getParent().setStatus(Bep.UNASSESSED);
                    AssessmentThread.myPoolUpdater.updatePoolAnchorStatus(topicID, currentLink.getAssociatedAnchor().getParent());
                }
            } catch (BadLocationException ex) {
                Logger.getLogger(AssessmentThread.class.getName()).log(Level.SEVERE, null, ex);
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
            LTWAssessmentToolControler.getInstance().goNextLink(true, true);
//            CurrentFocusedAnchor.getCurrentFocusedAnchor().setAnchor(previous, link.getAssociatedAnchor(), link);
//            os.setTABFieldValues(link);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Update Anchor Highlight & BEP Icon">
//    private void updateLinkPaneBepIcon(int newBepStartP, int oldStartP) {
//        // TODO: "isHighlighBEP"
//        // 1) YES: Remove previous BEP + Make a new BEP
//        // 2) NO: INSERT BEP ICONs for this Topic
//        try {
//            StyledDocument styDoc = (StyledDocument) this.myLinkPane.getDocument();
//
//            if (oldStartP > -1) {
//                styDoc.remove(oldStartP, bepLength);
//            }
//
//            Style bepStyle = styDoc.addStyle("bepIcon", null);
//            StyleConstants.setIcon(bepStyle, new ImageIcon(bepIconImageFilePath));
//            styDoc.insertString(newBepStartP, "TBEP", bepStyle);
//
//            this.myLinkPane.repaint();
//        } catch (BadLocationException ex) {
//            Logger.getLogger(topicPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
    
    public void markCurrentSubanchorIrrelevant(boolean onlyCurrentAnchor) {
        if (onlyCurrentAnchor) {
        	LTWAssessmentToolControler.getInstance().setSubanchorIrrelevant(processingAnchor);
            LTWAssessmentToolControler.getInstance().goNextLink(true, true);
        }
        else {
        	IndexedAnchor parent = processingAnchor.getParent();
            for (AssessedAnchor subanchor : parent.getChildrenAnchors())
            	LTWAssessmentToolControler.getInstance().setSubanchorIrrelevant(subanchor);
            
            parent.statusCheck();
    		TopicHighlightManager.getInstance().update(parent);
        }
        setProcessingAnchor(null);
    }
}
