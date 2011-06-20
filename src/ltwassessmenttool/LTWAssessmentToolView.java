/*
 * LTWAssessmentToolView.java
 */
package ltwassessmenttool;

import java.awt.Color;
import java.awt.Insets;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;

import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.text.Highlighter;
// import Classes
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import ltwassessment.AppResource;
import ltwassessment.Assessment;
import ltwassessment.font.AdjustFont;
import ltwassessment.parsers.Xml2Html;
import ltwassessmenttool.listener.CaretListenerLabel;
import ltwassessmenttool.listener.linkPaneMouseListener;
import ltwassessmenttool.listener.topicPaneMouseListener;
import ltwassessment.parsers.FOLTXTMatcher;
import ltwassessment.parsers.PoolerManager;
import ltwassessment.parsers.ResourcesManager;
import ltwassessment.utility.ObservableSingleton;
import ltwassessment.utility.fieldUpdateObserver;
import ltwassessment.utility.highlightPainters;
import ltwassessment.utility.PoolUpdater;
import ltwassessment.view.Link;
import ltwassessment.view.ScreenAnchor;
import ltwassessment.wiki.WikiArticleXml;

/**
 * The main frame for the Assessment Tool
 */
public class LTWAssessmentToolView extends FrameView {

    // -------------------------------------------------------------------------
    // constant variables
    protected final static int bepLength = 4;

    private static ResourceMap resourceMap;
    private static String textContentType = "";
//    private String wikipediaTopicFileDir = "";
//    private String topicAnchorsHTPrefix = "";
//    private String topicBepsHTPrefix = "";
    private String defaultWikipediaDirectory = "";
    private String defaultTeAraDirectory = "";
    private static String afTasnCollectionErrors = "";
    private String defaultPoolXmlFilePath = "";
    // -------------------------------------------------------------------------
    // Variables
    private boolean isTopicWikipedia = false;
    private boolean isLinkWikipedia = false;
    private String currTopicFilePath = "";
    private static String currTopicID = "";
    private static String currTopicName = "";
    // -------------------------------------------------------------------------
    private Vector<String[]> topicAnchorOLSEStatus = new Vector<String[]>();
    private Vector<String[]> topicBepScrStatus = new Vector<String[]>();
//    private Vector<String[]> currBepXMLOffset = new Vector<String[]>();
//    private Vector<String[]> currBepSCROffset = new Vector<String[]>();
    // -------------------------------------------------------------------------
    // For Incoming: [0]:Offset
    // -------------------------------------------------------------------------
    // Declare External Classes
    private static PoolerManager myPooler;
    private static ResourcesManager rscManager = null;
    private static highlightPainters painters = new highlightPainters();;
    private static ObservableSingleton os = null;
    private fieldUpdateObserver fuObserver = null;
    // -------------------------------------------------------------------------
    //1) including participant-id, run-id, task, collection
    //2) record Topic -> [0]:File ID & [1]:Name
    //3) record Topic (outgoing : topicFile) -> [0]:Offset & [1]:Length & [2]:Anchor_Name
    //4) record Topic (incoming : topicFile) -> [0]:Offset
//    private String[] afProperty = new String[4];
//    private Vector<String[]> RunTopics = new Vector<String[]>();
    private Hashtable<String, Vector<String[]>> topicAnchorsHT = new Hashtable<String, Vector<String[]>>();
    private Hashtable<String, Vector<String[]>> topicSubanchorsHT = new Hashtable<String, Vector<String[]>>();
    private Hashtable<String, Vector<String[]>> topicBepsHT = new Hashtable<String, Vector<String[]>>();
//    private Hashtable<String, String[]> topicAnchorOLTSENHT = new Hashtable<String, String[]>();
    // -------------------------------------------------------------------------
    private static Color linkPaneWhiteColor = Color.WHITE;
    private static Color linkPaneRelColor = new Color(168, 232, 177);
    private static Color linkPaneNonRelColor = new Color(255, 183, 165);
    // -------------------------------------------------------------------------
    private static String bepIconImageFilePath = "";
    private static String bepIconCompleted = "";
    private static String bepIconNonrelevant = "";
    private static String bepIconHighlight = "";
    
    private static JTextPane topicTextPane = null;
    private static JTextPane linkTextPane = null;
    
    private static PoolUpdater myPUpdater = null;

    static void log(Object content) {
        System.out.println(content);
    }

    static void errlog(Object content) {
        System.err.println("errlog: " + content);
    }
    public static final String[] outgoingTABColumnNames = {"Topic", "Pool Anchor", "Anchor", "BEP", "HiddenField"};
    public static final String[] incomingTBAColumnNames = {"Topic", "BEP", "Anchor", "File ID", "HiddenField"};
    ButtonGroup group = new ButtonGroup();
    
    //    Runnable runnable = new BasicThread2();
    // Create the thread supplying it with the runnable object
    Thread threadAssessment = new Thread(new AssessmentThread());
    private boolean assessmentLock = true; 
    
        /**
	 * @return the assessmentLock
	 */
    synchronized public boolean isAssessmentLock() {
		return assessmentLock;
	}

	/**
	 * @param assessmentLock the assessmentLock to set
	 */
    synchronized public void setAssessmentLock(boolean assessmentLock) {
		this.assessmentLock = assessmentLock;
	}

	class AssessmentThread implements Runnable {
        // This method is called when the thread runs
        public void run() {

        }
    }

    public LTWAssessmentToolView(SingleFrameApplication app) {
        super(app);
        
     	// update resource manager first thing with the app starting up
        AppResource.forValidationOrAssessment = true;
        AppResource.getInstance().setResourceMap(org.jdesktop.application.Application.getInstance(ltwassessmenttool.LTWAssessmentToolApp.class).getContext().getResourceMap(ltwassessmenttool.LTWAssessmentToolView.class));

        initComponents();

        setAnchorColorHints();
        
        topicTextPane = thisTopicTextPane;
        linkTextPane = thisLinkTextPane;
        
        group.add(outRadioBtn);
        group.add(inRadioBtn);

        os = ObservableSingleton.getInstance();
        fuObserver = new fieldUpdateObserver(os, this.lblTopicTitle, this.lblTopicID, this.lblPoolAnchor, this.lblTargetID, this.lblTargetTitle,
                this.lblCompletion);
        os.addObserver(fuObserver);
        
        // tool resource constant variables
        resourceMap = getResourceMap();
//        wikipediaTopicFileDir = resourceMap.getString("wikipedia.topics.folder");
//        topicAnchorsHTPrefix = resourceMap.getString("topicAnchorsHT.Prefix");
//        topicBepsHTPrefix = resourceMap.getString("topicBepsHT.Prefix");
        textContentType = resourceMap.getString("html.content.type");
        afTasnCollectionErrors = resourceMap.getString("AssFormXml.taskCollectionError");
        // =====================================================================
        bepIconImageFilePath = resourceMap.getString("bepIcon.imageFilePath");
        bepIconCompleted = resourceMap.getString("bepCompletedIcon.imageFilePath");
        bepIconNonrelevant = resourceMap.getString("bepNonrelevantIcon.imageFilePath");
        bepIconHighlight = resourceMap.getString("bepHighlightIcon.imageFilePath");
        // =====================================================================
        // In Assessment, Collections and Pool have been located in a default directory
        defaultWikipediaDirectory = resourceMap.getString("directory.Wikipedia");
        defaultTeAraDirectory = resourceMap.getString("directory.TeAra");
        defaultPoolXmlFilePath = resourceMap.getString("pooling.AFXmlFolder");
        // =====================================================================
        // TODO: activate/fix the Progress Bar
        progressBar.setVisible(false);
        // =====================================================================

        // =====================================================================
        // when the tool firstly starts:
        // For Link-the-Wikipedia A2B
        this.isTopicWikipedia = true;
        this.isLinkWikipedia = true;
        System.setProperty(LTWAssessmentToolControler.sysPropertyIsTopicWikiKey, "true");
        System.setProperty(LTWAssessmentToolControler.sysPropertyIsLinkWikiKey, "true");
        // =====================================================================
        // 0) Check Pool and Corpus are ready
        // 0-1) Wikipedia
        File wikiCollectionFolder = new File(defaultWikipediaDirectory);
        if (wikiCollectionFolder.exists()) {
            log("The Wikipedia Collection is ready.");
        } else {
        	defaultWikipediaDirectory = ResourcesManager.getInstance().getWikipediaCollectionFolder();
        	if (!new File(defaultWikipediaDirectory).exists())
        		LTWAssessmentToolApp.getApplication().showCorpusBox();
        	wikiCollectionFolder = new File(ResourcesManager.getInstance().getWikipediaCollectionFolder());
        }
        
        // 0-2) TeAra
        File tearaCollectionFolder = new File(defaultTeAraDirectory);
        if (tearaCollectionFolder.exists()) {
            log("The TeAra Collection is ready.");
        }
        // =====================================================================
        // assume this must be TRUE
//        boolean rightCorpusDir = corpusDirChecker(isTopicWikipedia);
//        if (rightCorpusDir) {
//        if (true) {
            log("Random XML file checking ... OK");
            // Topic and Link Panes Listeners
            // Check & Set Outgoing or Incoming
            // populate Topic & Link Panes
            // -----------------------------------------------------------------
            // new String[]{completed, total}
            // -----------------------------------------------------------------
//            if (rscManager.getLinkingMode().toLowerCase().equals("outgoing")) {
            
//            threadAssessment.start();
            Hashtable<String, File> topics4Assessment = Assessment.getInstance().getTopics();
            if (topics4Assessment.size() == 0) {
	            currTopicID = rscManager.getInstance().getTopicID();
	            if (currTopicID.length() == 0) {
	            	//TODO : fix this
	            }
	            else
	            	assess(Assessment.getPoolFile(currTopicID));
            }
            else {
        		currTopicID = null;
        		assessNextTopic();
            }

//        } 
            // we don't need incoming for crosslink
//            else if (rscManager.getLinkingMode().toLowerCase().equals("incoming")) {
//                String[] tbaCompletedRatio = this.rscManager.getTBACompletedRatio();
//                this.rscManager.updateIncomingCompletion(tbaCompletedRatio[0] + " : " + tbaCompletedRatio[1]);
//                System.setProperty(sysPropertyTBACompletedRatioKey, tbaCompletedRatio[0] + "_" + tbaCompletedRatio[1]);
//
//                painters = new highlightPainters();
//                // -------------------------------------------------------------
//                // scrS, String[]{O,S, num}
//                Hashtable<String, String[]> topicAnchorOSNHT = new Hashtable<String, String[]>();
//                topicAnchorOSNHT = populateTopicBepOSNHT();
//                // -------------------------------------------------------------
//                CaretListenerLabel caretListenerLabel = new CaretListenerLabel("Caret Status", this.topicTextPane, this.statusMessageLabel);
//                this.topicTextPane.addCaretListener(caretListenerLabel);
//                topicPaneMouseListener mtTopicPaneListener = new topicPaneMouseListener(this.topicTextPane, this.linkTextPane);
//                this.topicTextPane.addMouseListener(mtTopicPaneListener);
//                this.topicTextPane.addMouseMotionListener(mtTopicPaneListener);
//                linkPaneMouseListener myLPMListener = new linkPaneMouseListener(this.topicTextPane, this.linkTextPane);
//                this.linkTextPane.addMouseListener(myLPMListener);
//                // -------------------------------------------------------------
//                this.outRadioBtn.setSelected(false);
//                this.inRadioBtn.setSelected(true);
//                // -------------------------------------------------------------
//                setIncomingTBA();
//            } else {
//                String errInLinkingMode = "The system cannot indicate the linking mode, Outgoing or Incoming.\r\n" +
//                        " It might have been externally modified.\r\n" +
//                        "Please check the tool Resource file, \r\n" +
//                        "resources\toolResources.xml";
//                JOptionPane.showMessageDialog(LTWAssessmentToolApp.getApplication().getMainFrame(), errInLinkingMode);
//            }
//        }
    }
    
    private void assessNextTopic() {
    	if (currTopicID != null)
    		Assessment.getInstance().finishTopic(currTopicID);
    	currTopicID = Assessment.getInstance().getNextTopic();
    	assess(Assessment.getPoolFile(currTopicID), true);
    }
    
    private void resetResouceTopic(String topicLang) {
    	currTopicFilePath = AppResource.getInstance().getTopicXmlPathNameByFileID(currTopicID, topicLang); //rscManager.getTopicFilePath(currTopicID, topicLang);
    	rscManager.updateTopicID(currTopicID + ":" + topicLang);
    	rscManager.updateCurrTopicFilePath(currTopicFilePath);
    	rscManager.updateCurrAnchorFOL(null);
    }
    
    private void setupTopic() {
        currTopicFilePath = rscManager.getCurrTopicXmlFile();
		//      currTopicName = new WikiArticleXml(currTopicFilePath).getTitle();
		String topicLang = rscManager.getTopicLang();
		setTopicPaneContent(currTopicFilePath, topicLang);
		FOLTXTMatcher.getInstance().getCurrFullXmlText();
    	    	
    	FOLTXTMatcher.getInstance().getSCRAnchorPosV(thisTopicTextPane, currTopicID, topicAnchorsHT);
    	Vector<String> topicAnchorsOLNameSEVS = rscManager.getTopicAnchorsOLNameSEV();
//        int completedAnchor = 0;
//		//      int totoalAnchorNumber = Integer.valueOf(tabCompletedRatio[1]);
//		
//		int totoalAnchorNumberRecored = topicAnchorsOLNameSEVS.size();
//		
//		this.rscManager.updateOutgoingCompletion(String.valueOf(completedAnchor) + " : " + String.valueOf(totoalAnchorNumberRecored));
    }
    
    private void assess(String poolFile) {
    	assess(poolFile, false);
    }
    
    private void assess(String poolFile, boolean reset) {
    	if (!new File(poolFile).exists()) {
          String errMessage = "Cannot find pool file: " + poolFile + "\r\n";
			JOptionPane.showMessageDialog(LTWAssessmentToolApp.getApplication().getMainFrame(), errMessage);    		
    		return;
    	}
    	
        rscManager = ResourcesManager.getInstance();
        myPooler = PoolerManager.getInstance(poolFile);
        myPUpdater = myPooler.getPoolUpdater();
        
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
        CaretListenerLabel caretListenerLabel = new CaretListenerLabel("Caret Status", this.topicTextPane, this.statusMessageLabel);
        this.topicTextPane.addCaretListener(caretListenerLabel);
        topicPaneMouseListener mtTopicPaneListener = new topicPaneMouseListener(this.topicTextPane, this.linkTextPane);
        this.topicTextPane.addMouseListener(mtTopicPaneListener);
        this.topicTextPane.addMouseMotionListener(mtTopicPaneListener);
        linkPaneMouseListener myLPMListener = new linkPaneMouseListener(this.topicTextPane, this.linkTextPane);
        this.linkTextPane.addMouseListener(myLPMListener);
        
        LTWAssessmentToolControler.getInstance().setContainter(this.topicTextPane, this.linkTextPane);
        // -------------------------------------------------------------
        this.outRadioBtn.setSelected(true);
        this.inRadioBtn.setSelected(false);
        // -------------------------------------------------------------
        setOutgoingTAB();
    }

    
    private void setAnchorColorHints() {
    	jlblColorNotAssessed.setOpaque(true);
    	jlblColorIncomplete.setOpaque(true);
    	jlblColorCurrentAnchor.setOpaque(true);
    	jlblColorRelevant.setOpaque(true);
    	jlblColorIrrelevant.setOpaque(true);

        jlblColorNotAssessed.setBackground(highlightPainters.COLOR_NOT_ASSESSED);
        jlblColorIncomplete.setBackground(highlightPainters.COLOR_INCOMPLETE);
        jlblColorCurrentAnchor.setBackground(highlightPainters.COLOR_SELECTED);
        jlblColorRelevant.setBackground(highlightPainters.COLOR_RELEVENT);
        jlblColorIrrelevant.setBackground(highlightPainters.COLOR_IRREVENT);   	
    }

    private boolean corpusDirChecker(boolean topicIsWikipedia) {
        boolean rightCorpusDir = false;
        String ranTopicID = rscManager.getTopicID();
        String runFilePath = "";
        if (topicIsWikipedia) {
            runFilePath = defaultWikipediaDirectory + ranTopicID + ".xml";
        } else {
            runFilePath = defaultTeAraDirectory + ranTopicID + ".xml";
        }
        File ranXmlFile = new File(runFilePath);
        if (ranXmlFile.exists()) {
            rightCorpusDir = true;
        } else {
            rightCorpusDir = false;
            JOptionPane.showMessageDialog(mainPanel, "The XML File, " + ranTopicID + ".xml , cannot be found in specified Collection Directory, " + runFilePath + "!");
        }
        return rightCorpusDir;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jInfoPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lblTopicTitle = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lblTopicID = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lblPoolAnchor = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lblTargetID = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        lblTargetTitle = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        lblCompletion = new javax.swing.JLabel();
        jSplitPane1 = new javax.swing.JSplitPane();
        topicTextScrollPane = new javax.swing.JScrollPane();
        thisTopicTextPane = new javax.swing.JTextPane();
        linkTextScrollPane = new javax.swing.JScrollPane();
        thisLinkTextPane = new javax.swing.JTextPane();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        utilMenu = new javax.swing.JMenu();
        loadMenuItem = new javax.swing.JMenuItem();
        linkMenu = new javax.swing.JMenu();
        outRadioBtn = new javax.swing.JRadioButtonMenuItem();
        inRadioBtn = new javax.swing.JRadioButtonMenuItem();
        jMenuLang = new javax.swing.JMenu();
        jRadioButtonMenuItemZh = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItemJa = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItemKo = new javax.swing.JRadioButtonMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        btnGoBack = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jlblColorIrrelevant = new javax.swing.JLabel();
        jlblColorRelevant = new javax.swing.JLabel();
        jlblColorNotAssessed = new javax.swing.JLabel();
        jlblColorCurrentAnchor = new javax.swing.JLabel();
        statusMessageLabel = new javax.swing.JLabel();
        jlblColorIncomplete = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        buttonGroup1 = new javax.swing.ButtonGroup();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ltwassessmenttool.LTWAssessmentToolApp.class).getContext().getResourceMap(LTWAssessmentToolView.class);
        mainPanel.setBackground(resourceMap.getColor("mainPanel.background")); // NOI18N
        mainPanel.setName("mainPanel"); // NOI18N

        jInfoPanel.setBackground(resourceMap.getColor("jInfoPanel.background")); // NOI18N
        jInfoPanel.setName("jInfoPanel"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        lblTopicTitle.setFont(resourceMap.getFont("lblTopicTitle.font")); // NOI18N
        lblTopicTitle.setForeground(resourceMap.getColor("lblTopicTitle.foreground")); // NOI18N
        lblTopicTitle.setText(resourceMap.getString("lblTopicTitle.text")); // NOI18N
        lblTopicTitle.setMaximumSize(new java.awt.Dimension(30, 14));
        lblTopicTitle.setMinimumSize(new java.awt.Dimension(30, 14));
        lblTopicTitle.setName("lblTopicTitle"); // NOI18N
        lblTopicTitle.setPreferredSize(new java.awt.Dimension(30, 14));

        jLabel4.setFont(resourceMap.getFont("jLabel4.font")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        lblTopicID.setText(resourceMap.getString("lblTopicID.text")); // NOI18N
        lblTopicID.setMaximumSize(new java.awt.Dimension(30, 14));
        lblTopicID.setMinimumSize(new java.awt.Dimension(30, 14));
        lblTopicID.setName("lblTopicID"); // NOI18N
        lblTopicID.setPreferredSize(new java.awt.Dimension(30, 14));

        jLabel6.setFont(resourceMap.getFont("jLabel6.font")); // NOI18N
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        lblPoolAnchor.setFont(resourceMap.getFont("lblPoolAnchor.font")); // NOI18N
        lblPoolAnchor.setForeground(resourceMap.getColor("lblPoolAnchor.foreground")); // NOI18N
        lblPoolAnchor.setText(resourceMap.getString("lblPoolAnchor.text")); // NOI18N
        lblPoolAnchor.setName("lblPoolAnchor"); // NOI18N

        jLabel3.setFont(resourceMap.getFont("jLabel3.font")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        lblTargetID.setText(resourceMap.getString("lblTargetID.text")); // NOI18N
        lblTargetID.setName("lblTargetID"); // NOI18N

        jLabel2.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        lblTargetTitle.setText(resourceMap.getString("lblTargetTitle.text")); // NOI18N
        lblTargetTitle.setName("lblTargetTitle"); // NOI18N

        jLabel7.setFont(resourceMap.getFont("jLabel7.font")); // NOI18N
        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        lblCompletion.setText(resourceMap.getString("lblCompletion.text")); // NOI18N
        lblCompletion.setMaximumSize(new java.awt.Dimension(30, 14));
        lblCompletion.setMinimumSize(new java.awt.Dimension(30, 14));
        lblCompletion.setName("lblCompletion"); // NOI18N
        lblCompletion.setPreferredSize(new java.awt.Dimension(30, 14));

        javax.swing.GroupLayout jInfoPanelLayout = new javax.swing.GroupLayout(jInfoPanel);
        jInfoPanel.setLayout(jInfoPanelLayout);
        jInfoPanelLayout.setHorizontalGroup(
            jInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInfoPanelLayout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(jInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jInfoPanelLayout.createSequentialGroup()
                        .addComponent(lblTopicID, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(132, 132, 132)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblPoolAnchor, javax.swing.GroupLayout.PREFERRED_SIZE, 405, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(136, 136, 136)
                        .addComponent(jLabel3))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jInfoPanelLayout.createSequentialGroup()
                        .addComponent(lblTopicTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                        .addGap(482, 482, 482)
                        .addComponent(jLabel2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jInfoPanelLayout.createSequentialGroup()
                        .addComponent(lblTargetID, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 144, Short.MAX_VALUE)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCompletion, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblTargetTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(jInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jInfoPanelLayout.createSequentialGroup()
                    .addGap(1171, 1171, 1171)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                    .addGap(147, 147, 147)))
        );
        jInfoPanelLayout.setVerticalGroup(
            jInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInfoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jInfoPanelLayout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(53, 53, 53))
                    .addGroup(jInfoPanelLayout.createSequentialGroup()
                        .addGroup(jInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblTargetID, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
                            .addComponent(lblCompletion, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblPoolAnchor, javax.swing.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblTopicID, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblTargetTitle, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
                                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE))
                            .addComponent(lblTopicTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE))
                        .addGap(27, 27, 27))))
            .addGroup(jInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jInfoPanelLayout.createSequentialGroup()
                    .addGap(33, 33, 33)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
                    .addGap(32, 32, 32)))
        );

        jSplitPane1.setBackground(resourceMap.getColor("jSplitPane1.background")); // NOI18N
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        topicTextScrollPane.setBackground(resourceMap.getColor("topicTextScrollPane.background")); // NOI18N
        topicTextScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        topicTextScrollPane.setMaximumSize(new java.awt.Dimension(1600, 1600));
        topicTextScrollPane.setName("topicTextScrollPane"); // NOI18N
        topicTextScrollPane.setPreferredSize(new java.awt.Dimension(500, 644));

        thisTopicTextPane.setBackground(resourceMap.getColor("thisTopicTextPane.background")); // NOI18N
        thisTopicTextPane.setEditable(false);
        thisTopicTextPane.setDragEnabled(true);
        thisTopicTextPane.setMaximumSize(new java.awt.Dimension(1600, 1600));
        thisTopicTextPane.setMinimumSize(new java.awt.Dimension(500, 644));
        thisTopicTextPane.setName("thisTopicTextPane"); // NOI18N
        thisTopicTextPane.setPreferredSize(new java.awt.Dimension(500, 644));
        topicTextScrollPane.setViewportView(thisTopicTextPane);

        jSplitPane1.setLeftComponent(topicTextScrollPane);

        linkTextScrollPane.setMinimumSize(new java.awt.Dimension(20, 20));
        linkTextScrollPane.setName("linkTextScrollPane"); // NOI18N
        linkTextScrollPane.setPreferredSize(new java.awt.Dimension(525, 400));

        thisLinkTextPane.setEditable(false);
        thisLinkTextPane.setName("thisLinkTextPane"); // NOI18N
        thisLinkTextPane.setPreferredSize(new java.awt.Dimension(525, 444));
        linkTextScrollPane.setViewportView(thisLinkTextPane);

        jSplitPane1.setRightComponent(linkTextScrollPane);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jInfoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1527, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addComponent(jInfoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 634, Short.MAX_VALUE))
        );

        menuBar.setBackground(resourceMap.getColor("menuBar.background")); // NOI18N
        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setBackground(resourceMap.getColor("helpMenu.background")); // NOI18N
        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ltwassessmenttool.LTWAssessmentToolApp.class).getContext().getActionMap(LTWAssessmentToolView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        utilMenu.setBackground(resourceMap.getColor("utilMenu.background")); // NOI18N
        utilMenu.setText(resourceMap.getString("utilMenu.text")); // NOI18N
        utilMenu.setName("utilMenu"); // NOI18N

        loadMenuItem.setAction(actionMap.get("loadSubmissionXML")); // NOI18N
        loadMenuItem.setText(resourceMap.getString("loadMenuItem.text")); // NOI18N
        loadMenuItem.setName("loadMenuItem"); // NOI18N
        utilMenu.add(loadMenuItem);

        menuBar.add(utilMenu);

        linkMenu.setBackground(resourceMap.getColor("linkMenu.background")); // NOI18N
        linkMenu.setText(resourceMap.getString("linkMenu.text")); // NOI18N
        linkMenu.setName("linkMenu"); // NOI18N

        outRadioBtn.setSelected(true);
        outRadioBtn.setText(resourceMap.getString("outRadioBtn.text")); // NOI18N
        outRadioBtn.setName("outRadioBtn"); // NOI18N
        outRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outRadioBtnActionPerformed(evt);
            }
        });
        linkMenu.add(outRadioBtn);

        inRadioBtn.setText(resourceMap.getString("inRadioBtn.text")); // NOI18N
        inRadioBtn.setName("inRadioBtn"); // NOI18N
        inRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inRadioBtnActionPerformed(evt);
            }
        });
        linkMenu.add(inRadioBtn);

        menuBar.add(linkMenu);

        jMenuLang.setText(resourceMap.getString("jMenuLang.text")); // NOI18N
        jMenuLang.setName("jMenuLang"); // NOI18N

        buttonGroup1.add(jRadioButtonMenuItemZh);
        jRadioButtonMenuItemZh.setText(resourceMap.getString("jRadioButtonMenuItemZh.text")); // NOI18N
        jRadioButtonMenuItemZh.setName("jRadioButtonMenuItemZh"); // NOI18N
        jRadioButtonMenuItemZh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItemZhActionPerformed(evt);
            }
        });
        jMenuLang.add(jRadioButtonMenuItemZh);

        buttonGroup1.add(jRadioButtonMenuItemJa);
        jRadioButtonMenuItemJa.setText(resourceMap.getString("jRadioButtonMenuItemJa.text")); // NOI18N
        jRadioButtonMenuItemJa.setName("jRadioButtonMenuItemJa"); // NOI18N
        jRadioButtonMenuItemJa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItemJaActionPerformed(evt);
            }
        });
        jMenuLang.add(jRadioButtonMenuItemJa);

        buttonGroup1.add(jRadioButtonMenuItemKo);
        jRadioButtonMenuItemKo.setText(resourceMap.getString("jRadioButtonMenuItemKo.text")); // NOI18N
        jRadioButtonMenuItemKo.setName("jRadioButtonMenuItemKo"); // NOI18N
        jRadioButtonMenuItemKo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItemKoActionPerformed(evt);
            }
        });
        jMenuLang.add(jRadioButtonMenuItemKo);

        menuBar.add(jMenuLang);

        helpMenu.setBackground(resourceMap.getColor("helpMenu.background")); // NOI18N
        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setBackground(resourceMap.getColor("statusPanel.background")); // NOI18N
        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        btnGoBack.setAction(actionMap.get("btnGoBackALink")); // NOI18N
        btnGoBack.setText(resourceMap.getString("btnGoBack.text")); // NOI18N
        btnGoBack.setName("btnGoBack"); // NOI18N

        jButton1.setAction(actionMap.get("btnGoForwardALink")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setMaximumSize(new java.awt.Dimension(75, 23));
        jButton1.setMinimumSize(new java.awt.Dimension(75, 23));
        jButton1.setName("jButton1"); // NOI18N
        jButton1.setPreferredSize(new java.awt.Dimension(75, 23));

        jlblColorIrrelevant.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlblColorIrrelevant.setText(resourceMap.getString("jlblColorIrrelevant.text")); // NOI18N
        jlblColorIrrelevant.setName("jlblColorIrrelevant"); // NOI18N

        jlblColorRelevant.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlblColorRelevant.setText(resourceMap.getString("jlblColorRelevant.text")); // NOI18N
        jlblColorRelevant.setName("jlblColorRelevant"); // NOI18N

        jlblColorNotAssessed.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlblColorNotAssessed.setText(resourceMap.getString("jlblColorNotAssessed.text")); // NOI18N
        jlblColorNotAssessed.setName("jlblColorNotAssessed"); // NOI18N

        jlblColorCurrentAnchor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlblColorCurrentAnchor.setText(resourceMap.getString("jlblColorCurrentAnchor.text")); // NOI18N
        jlblColorCurrentAnchor.setName("jlblColorCurrentAnchor"); // NOI18N

        statusMessageLabel.setText(resourceMap.getString("statusMessageLabel.text")); // NOI18N
        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        jlblColorIncomplete.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jlblColorIncomplete.setText(resourceMap.getString("jlblColorIncomplete.text")); // NOI18N
        jlblColorIncomplete.setName("jlblColorIncomplete"); // NOI18N

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 1527, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 148, Short.MAX_VALUE)
                .addComponent(jlblColorCurrentAnchor, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jlblColorNotAssessed)
                .addGap(24, 24, 24)
                .addComponent(jlblColorIncomplete)
                .addGap(24, 24, 24)
                .addComponent(jlblColorRelevant, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jlblColorIrrelevant, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(273, 273, 273)
                .addComponent(statusMessageLabel)
                .addGap(18, 18, 18)
                .addComponent(btnGoBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(statusPanelLayout.createSequentialGroup()
                        .addGap(235, 235, 235)
                        .addComponent(statusAnimationLabel)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(66, 66, 66))))
        );

        statusPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jlblColorCurrentAnchor, jlblColorIncomplete, jlblColorIrrelevant, jlblColorNotAssessed, jlblColorRelevant});

        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(statusAnimationLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnGoBack)
                                .addComponent(statusMessageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(statusPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jlblColorCurrentAnchor)
                            .addComponent(jlblColorNotAssessed)
                            .addComponent(jlblColorIncomplete)
                            .addComponent(jlblColorRelevant)
                            .addComponent(jlblColorIrrelevant)
                            .addComponent(jLabel8))))
                .addContainerGap())
        );

        statusPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jlblColorCurrentAnchor, jlblColorIncomplete, jlblColorIrrelevant, jlblColorNotAssessed, jlblColorRelevant});

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents
    // <editor-fold defaultstate="collapsed" desc="Menu Bar Event Action">
    String currentOpenDir = "C:\\";

    @Action
    public void loadImages() {
        // load images for the Topic and all Links
        // using Image Loader
    }

    private static void updateAnchorChanges(Vector<String[]> nextAnchorBepLinkVSA, String[] currTopicOLSEStatusSA, String[] currPAnchorOLSA, String[] currPALinkOIDSA) {
        // Get PoolAnchor O, L, S, E, Status
        String[] nextAnchorOLSEStatusSA = nextAnchorBepLinkVSA.elementAt(0);
        String nextAnchorO = nextAnchorOLSEStatusSA[0];
        String nextAnchorL = nextAnchorOLSEStatusSA[1];
        String nextAnchorS = nextAnchorOLSEStatusSA[2];
        String nextAnchorE = nextAnchorOLSEStatusSA[3];
        String nextAnchorExt = nextAnchorOLSEStatusSA[5];
        String nextAnchorStatus = myPooler.getPoolAnchorStatus(currTopicID, new String[]{nextAnchorO, nextAnchorL});
        
    	int anchorEnd = Integer.valueOf(nextAnchorE);
    	int anchorExt = Integer.valueOf(nextAnchorExt);
        if (anchorExt > 0) {
        	anchorEnd += anchorExt;
        	nextAnchorExt = "0";
        	anchorExt = 0;
        }
    	nextAnchorE = String.valueOf(anchorEnd);
        
        // new String[]{tbOffset, tbStartP, tbFileID, tbRel}
        String[] nextAnchorLinkOSIDStatusSA = nextAnchorBepLinkVSA.elementAt(1);
        String nextLinkO = nextAnchorLinkOSIDStatusSA[0];
        String nextLinkID = nextAnchorLinkOSIDStatusSA[2];
        String nextLinkLang = nextAnchorLinkOSIDStatusSA[4];
        String nextLinkTitle = nextAnchorLinkOSIDStatusSA[5];
        String nextLinkAnchorName = nextAnchorLinkOSIDStatusSA[6];
//        String nextLinkExtLength = "0"; //nextAnchorLinkOSIDStatusSA[6];
        String nextLinkS = myPooler.getPoolAnchorBepLinkStartP(currTopicID, new String[]{nextAnchorO, nextAnchorL}, nextLinkID);
        String nextLinkStatus = myPooler.getPoolAnchorBepLinkStatus(currTopicID, new String[]{nextAnchorO, nextAnchorL}, nextLinkID);
        
        Link link = new Link();
        link.setAnchorName(nextLinkAnchorName);
        link.setBepOffset(Integer.parseInt(nextLinkO));
        link.setId(nextLinkID);
        link.setLang(nextLinkLang);
        link.setTitle(nextLinkTitle);
        link.setStartPos(Integer.parseInt(nextLinkS));
        link.setStatus(nextLinkStatus);
        
        // re-adjust the anchor offset and length
        int offset = Integer.parseInt(nextAnchorLinkOSIDStatusSA[7]);
        int length = Integer.parseInt(nextAnchorLinkOSIDStatusSA[8]);
        int anchorOffset = Integer.parseInt(nextAnchorO);
        int screenOffset = Integer.parseInt(nextAnchorS) + offset - anchorOffset;
        String nextSubanchorS = String.valueOf(screenOffset);
        String nextSubanchorE = String.valueOf(screenOffset + length);
        
        // =================================================================
        // Topic Pane
        // =================================================================
        // Update Pool Anchor Status <-- When GO NEXT Pool Anchor
        // because the PRE-Pool_Anchor might have been completed.
        ScreenAnchor screenAnchor = new ScreenAnchor();
        screenAnchor.setOffset(anchorOffset);
        screenAnchor.setLength(length);
        screenAnchor.setScreenSubPosStart(nextSubanchorS);
        screenAnchor.setScreenSubPosEnd(nextSubanchorE);
        screenAnchor.setScreenPosStart(nextAnchorS);
        screenAnchor.setScreenPosEnd(nextAnchorE);
        screenAnchor.setStatus(nextAnchorStatus);
        screenAnchor.setExtLength(anchorExt);
        
        screenAnchor.setLink(link);


        updateAnchor(currTopicOLSEStatusSA, screenAnchor);

        updateFields(screenAnchor, link);
    }
    
    private static void updateAnchor(String[] currTopicOLSEStatusSA, ScreenAnchor scrAnchor) {
        String currPAnchorO = currTopicOLSEStatusSA[0];
        String currPAnchorL = currTopicOLSEStatusSA[1];
        String[] currPAnchorOLSA = new String[]{currPAnchorO, currPAnchorL};
        String currPAnchorS = currTopicOLSEStatusSA[2];
        String currPAnchorE = currTopicOLSEStatusSA[3];
        String currPAnchorExt = currTopicOLSEStatusSA[5];

        String currPAnchorStatus = myPooler.getPoolAnchorStatus(currTopicID, currPAnchorOLSA);
        String[] targetAnchorScreenInfo = new String[]{"", scrAnchor.getScreenPosStart(), scrAnchor.getScreenPosEnd(), String.valueOf(scrAnchor.getExtLength()), scrAnchor.getScreenSubPosStart(), scrAnchor.getScreenSubPosEnd()};
        if (!targetAnchorScreenInfo[4].equals(currPAnchorS) || !targetAnchorScreenInfo[5].equals(currPAnchorE)) {
            int extLength = Integer.parseInt(currPAnchorExt);
            if (extLength > 0)
            	currPAnchorE = String.valueOf(Integer.parseInt(currPAnchorE) + extLength);
            // Highlight Anchor/BEP + Auto Scrolling
            // String[]{Name, SP, EP, extLength, subSP, subEP}
            updateTopicAnchorsHighlight(topicTextPane, new String[]{currPAnchorS, currPAnchorE, currPAnchorStatus, currPAnchorExt}, targetAnchorScreenInfo, Integer.parseInt(scrAnchor.getStatus()));
            
            if (scrAnchor.getOffset() != Integer.parseInt(currPAnchorO)) {
//          if (currPALinkStatus.equals("-1")) {
//              if (currPAnchorStatus.equals("-1")) {
//                  poolAnchorStatus = currPAnchorStatus;
//              } else {
//                  poolAnchorStatus = this.myRSCManager.getPoolAnchorCompletedStatus(topicID, currPAnchorOLSA);
//              }
//          } else {
//              poolAnchorStatus = this.myRSCManager.getPoolAnchorCompletedStatus(topicID, currPAnchorOLSA);
//          }
//          this.myPoolUpdater.updatePoolAnchorStatus(topicID, currPAnchorOLSA, poolAnchorStatus);
                String poolAnchorStatus = "";
                if (!currPAnchorStatus.equals("0")){
                    poolAnchorStatus = currPAnchorStatus;
                } else {
                    poolAnchorStatus = rscManager.getPoolAnchorCompletedStatus(currTopicID, currPAnchorOLSA);
                }
                myPUpdater.updatePoolAnchorStatus(currTopicID, currPAnchorOLSA, poolAnchorStatus);
                topicTextPane.getCaret().setDot(Integer.valueOf(scrAnchor.getScreenPosEnd()));
                topicTextPane.scrollRectToVisible(topicTextPane.getVisibleRect());
                topicTextPane.repaint();
            }
            
            // update System Property
            LTWAssessmentToolControler.setCurrentAnchorProperty(String.valueOf(scrAnchor.getOffset()), String.valueOf(scrAnchor.getLength()), scrAnchor.getScreenPosStart(), scrAnchor.getScreenPosEnd(), scrAnchor.getStatus(), String.valueOf(scrAnchor.getExtLength()));
        }
    }
    
    private static void updateFields(ScreenAnchor screenAnchor, Link link) {
        // =================================================================
        // Link Pane
        // =================================================================
        String bepXmlFilePath = myPooler.getXmlFilePathByTargetID(link.getId(), link.getLang());
        if (bepXmlFilePath.startsWith(afTasnCollectionErrors)) {
            bepXmlFilePath = rscManager.getErrorXmlFilePath(bepXmlFilePath);
        }
        Xml2Html xmlParser = new Xml2Html(bepXmlFilePath, Boolean.valueOf(System.getProperty(LTWAssessmentToolControler.sysPropertyIsLinkWikiKey)));
        String xmlHtmlText = xmlParser.getHtmlContent().toString();
        linkTextPane.setContentType(textContentType);
        linkTextPane.setText(xmlHtmlText);
        linkTextPane.setCaretPosition(0);
        // -------------------------------------------------------------
        // -------------------------------------------------------------
        int screenAnchorStatus = Integer.valueOf(screenAnchor.getStatus());
        int linkStatus = Integer.valueOf(link.getStatus());
        if (screenAnchorStatus == 1 || screenAnchorStatus == 0) {
            if (linkStatus == 1) {
                // Relevant --> Insert BEP --> BG = Green
                Vector<String> bepSCROffset = new Vector<String>();
                bepSCROffset.add(String.valueOf(link.getStartPos()));
                boolean isTopicBEP = false;
                updatePaneBepIcon(linkTextPane, bepSCROffset, isTopicBEP);
                linkTextPane.getCaret().setDot(link.getStartPos());
                linkTextPane.scrollRectToVisible(linkTextPane.getVisibleRect());
                linkTextPane.setBackground(linkPaneRelColor);
            } else if (linkStatus == -1) {
                linkTextPane.setBackground(linkPaneNonRelColor);
            } else if (linkStatus == 0) {
                linkTextPane.setBackground(linkPaneWhiteColor);
            }
            linkTextPane.repaint();
        } else if (screenAnchorStatus == -1) {
            linkTextPane.setBackground(linkPaneNonRelColor);
            linkTextPane.repaint();
        }    	
        // -------------------------------------------------------------
        // -------------------------------------------------------------
//        String currAnchorName = myPooler.getPoolAnchorNameByOL(currTopicID, new String[]{String.valueOf(screenAnchor.getOffset()), String.valueOf(screenAnchor.getLength())});
        Vector<String> newTABFieldValues = new Vector<String>();
        newTABFieldValues.add(currTopicName);
        newTABFieldValues.add(currTopicID);
        newTABFieldValues.add(link.getAnchorName());
        newTABFieldValues.add(link.getId());
        String pageTitle = "";
//        if (Boolean.valueOf(System.getProperty(sysPropertyIsLinkWikiKey))) {
            pageTitle = rscManager.getWikipediaPageTitle(link.getId());
//        } else {
//            pageTitle = rscManager.getTeAraFilePathByName(nextLinkID);
//        }
        newTABFieldValues.add(pageTitle.trim());
        String[] pAnchorCompletionSA = rscManager.getOutgoingCompletion();
        newTABFieldValues.add(pAnchorCompletionSA[0] + " / " + pAnchorCompletionSA[1]);
        os.setTABFieldValues(newTABFieldValues);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Button Click to Go Back / Forward A Link">
    @Action
    public void btnGoBackALink() {
        // Click the button to Go Back one Link
//        boolean isTABOutgoing = Boolean.valueOf(System.getProperty(sysPropertyIsTABKey));
//        if (isTABOutgoing) {
            // <editor-fold defaultstate="collapsed" desc="Update TAB Topic, Link">
            // =================================================================
            String currTopicOLSEStatus = System.getProperty(LTWAssessmentToolControler.sysPropertyCurrTopicOLSEStatusKey);
            String[] currTopicOLSEStatusSA = currTopicOLSEStatus.split("_");
            String currPAnchorO = currTopicOLSEStatusSA[0];
            String currPAnchorL = currTopicOLSEStatusSA[1];
            String[] currPAnchorOLSA = new String[]{currPAnchorO, currPAnchorL};
            // -----------------------------------------------------------------
            String[] currALinkOIDSA = rscManager.getCurrTopicATargetOID(thisLinkTextPane, currTopicID);
            String currALinkO = currALinkOIDSA[0];
            String currALinkID = currALinkOIDSA[1];
            String[] currPALinkOIDSA = new String[]{currALinkO, currALinkID};

//          String currALinkStatus = myPooler.getPoolAnchorBepLinkStatus(currTopicID, currPAnchorOLSA, currALinkID);
            // =================================================================
            // 1) Get the NEXT Anchor O, L, S, E, Status + its BEP link O, S, ID, Status
            //    With TAB Nav Update --> NEXT TAB
            Vector<String[]> nextAnchorBepLinkVSA = rscManager.getPreTABWithUpdateNAV(currTopicID, currPAnchorOLSA, currPALinkOIDSA, false);
            
            updateAnchorChanges(nextAnchorBepLinkVSA, currTopicOLSEStatusSA, currPAnchorOLSA, currPALinkOIDSA);
    }

    @Action
    public void btnGoForwardALink() {
    	moveForwardALink(false, false);
    }
    
    public static void moveForwardALink(boolean updateCurrAnchorStatus, boolean nextUnassessed) {
        // Click the button to Go Back one Link
//        PoolUpdater myPUpdater = new PoolUpdater();
//        boolean isTABOutgoing = Boolean.valueOf(System.getProperty(sysPropertyIsTABKey));
//        if (isTABOutgoing) {
            // <editor-fold defaultstate="collapsed" desc="Update TAB Topic, Link">
            String currTopicOLSEStatus = System.getProperty(LTWAssessmentToolControler.sysPropertyCurrTopicOLSEStatusKey);
            String[] currTopicOLSEStatusSA = currTopicOLSEStatus.split("_");
            String currPAnchorO = currTopicOLSEStatusSA[0];
            String currPAnchorL = currTopicOLSEStatusSA[1];
            String[] currPAnchorOLSA = new String[]{currPAnchorO, currPAnchorL};
            String[] currALinkOIDSA = rscManager.getCurrTopicATargetOID(linkTextPane, currTopicID);
            String currALinkOffset = currALinkOIDSA[0];
            String currALinkID = currALinkOIDSA[1];
            String[] currPALinkOIDSA = new String[]{currALinkOffset, currALinkID};
            
            if (updateCurrAnchorStatus) {
            	String currPALinkStatus = myPooler.getPoolAnchorBepLinkStatus(currTopicID, currPAnchorOLSA, currALinkID);
            	String currPAnchorStatus = myPooler.getPoolAnchorStatus(currTopicID, currPAnchorOLSA);
            	String poolAnchorStatus = "";
            	if (currPALinkStatus.equals("-1")) {
            		if (currPAnchorStatus.equals("-1")) {
			          poolAnchorStatus = currPAnchorStatus;
            		} else {
			        poolAnchorStatus = rscManager.getPoolAnchorCompletedStatus(currTopicID, currPAnchorOLSA);
            		}
            	} else {
			      poolAnchorStatus = rscManager.getPoolAnchorCompletedStatus(currTopicID, currPAnchorOLSA);
            	}
            	if (!poolAnchorStatus.equals("0"))
            		myPUpdater.updatePoolAnchorStatus(currTopicID, currPAnchorOLSA, poolAnchorStatus);            	
            }
            // -------------------------------------------------------------
            // 1) Get the NEXT Anchor O, L, S, E, Status + its BEP link O, S, ID, Status
            //    With TAB Nav Update --> NEXT TAB
            Vector<String[]> nextAnchorBepLinkVSA = rscManager.getNextTABWithUpdateNAV(currTopicID, currPAnchorOLSA, currPALinkOIDSA, nextUnassessed);

            updateAnchorChanges(nextAnchorBepLinkVSA, currTopicOLSEStatusSA, currPAnchorOLSA, currPALinkOIDSA);
    }

    // </editor-fold>

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = LTWAssessmentToolApp.getApplication().getMainFrame();
            aboutBox = new LTWAssessmentToolAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        LTWAssessmentToolApp.getApplication().show(aboutBox);
    }

    private void outRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outRadioBtnActionPerformed
        // populate Outgoing Links T.A.B.
        setOutgoingTAB();
    }//GEN-LAST:event_outRadioBtnActionPerformed

    private void inRadioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inRadioBtnActionPerformed
        // populate Incoming Links T.B.A.
//        setIncomingTBA();
    }//GEN-LAST:event_inRadioBtnActionPerformed

    private void jRadioButtonMenuItemZhActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemZhActionPerformed
        AppResource.targetLang = "zh";
}//GEN-LAST:event_jRadioButtonMenuItemZhActionPerformed

    private void jRadioButtonMenuItemJaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemJaActionPerformed
        AppResource.targetLang = "ja";
}//GEN-LAST:event_jRadioButtonMenuItemJaActionPerformed

    private void jRadioButtonMenuItemKoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMenuItemKoActionPerformed
        AppResource.targetLang = "ko";
}//GEN-LAST:event_jRadioButtonMenuItemKoActionPerformed
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Pre-Variables-Declaration">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGoBack;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JRadioButtonMenuItem inRadioBtn;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jInfoPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JMenu jMenuLang;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemJa;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemKo;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemZh;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel jlblColorCurrentAnchor;
    private javax.swing.JLabel jlblColorIncomplete;
    private javax.swing.JLabel jlblColorIrrelevant;
    private javax.swing.JLabel jlblColorNotAssessed;
    private javax.swing.JLabel jlblColorRelevant;
    private javax.swing.JLabel lblCompletion;
    private javax.swing.JLabel lblPoolAnchor;
    private javax.swing.JLabel lblTargetID;
    private javax.swing.JLabel lblTargetTitle;
    private javax.swing.JLabel lblTopicID;
    private javax.swing.JLabel lblTopicTitle;
    private javax.swing.JMenu linkMenu;
    private javax.swing.JScrollPane linkTextScrollPane;
    private javax.swing.JMenuItem loadMenuItem;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JRadioButtonMenuItem outRadioBtn;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JTextPane thisLinkTextPane;
    private javax.swing.JTextPane thisTopicTextPane;
    private javax.swing.JScrollPane topicTextScrollPane;
    private javax.swing.JMenu utilMenu;
    // End of variables declaration//GEN-END:variables
    // Pre-implemented Variables declaration
    private JDialog aboutBox;
    private JDialog corpusBox;
    private JDialog assessmentSetBox;

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Set Outgoing TAB & Incoming TBA">
    // =========================================================================
    // Outgoing Links: T.A.B.: populate Topic and Link Pane
    // =========================================================================
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
        currTopicID = rscManager.getTopicID();
        currTopicFilePath = rscManager.getCurrTopicXmlFile();
//        currTopicName = new WikiArticleXml(currTopicFilePath).getTitle();
        String topicLang = rscManager.getTopicLang();
        setTopicPaneContent(currTopicFilePath, topicLang);

        // ---------------------------------------------------------------------
        // For 1st time to get the ANCHOR OL name SE
//        folMatcher = FOLTXTMatcher.getInstance();
        Vector<String> topicAnchorsOLNameSEVS = rscManager.getTopicAnchorsOLNameSEV();
        if (topicAnchorsOLNameSEVS.size() == 0) {
        	FOLTXTMatcher.getInstance().getSCRAnchorPosV(thisTopicTextPane, currTopicID, topicAnchorsHT);
        	topicAnchorsOLNameSEVS = rscManager.getTopicAnchorsOLNameSEV();
        }     
        String[] tabCompletedRatio = this.rscManager.getTABCompletedRatio();
        this.rscManager.updateOutgoingCompletion(tabCompletedRatio[0] + " : " + tabCompletedRatio[1]);
        System.setProperty(LTWAssessmentToolControler.sysPropertyTABCompletedRatioKey, String.valueOf(tabCompletedRatio[0]) + "_" + String.valueOf(tabCompletedRatio[1]));
        // ---------------------------------------------------------------------
        
        // String[]{Anchor_O, L, SP, EP, Status}
        topicAnchorOLSEStatus = rscManager.getTopicAnchorOLSEStatusVSA();
        // String[]{Anchor_O, L, Name, SP, EP, Status}
        String[] currTopicOLNameSEStatus = rscManager.getCurrTopicAnchorOLNameSEStatusSA(thisTopicTextPane, currTopicID, topicAnchorsOLNameSEVS);
        String currTopicPAnchorStatus = currTopicOLNameSEStatus[5];
        // ---------------------------------------------------------------------
        // Get current Link file ID & SCR BEP S, lang, title
        String[] CurrTopicATargetOID = rscManager.getCurrTopicATargetOID(thisLinkTextPane, currTopicID);
        String currTargetOffset = CurrTopicATargetOID[0];
        String currTargetID = CurrTopicATargetOID[1];
        String currTargetLang = CurrTopicATargetOID[2];
        String pageTitle = CurrTopicATargetOID[3];;
        if (currTargetID.endsWith("\"")) {
            currTargetID = currTargetID.substring(0, currTargetID.length() - 1);
        }
        String currTargetFilePath = rscManager.getWikipediaFilePathByName(currTargetID + ".xml", currTargetLang);
        
        // re-adjust the anchor offset and length
        int offset = Integer.parseInt(CurrTopicATargetOID[5]);
        int length = Integer.parseInt(CurrTopicATargetOID[6]);
        
        int anchorOffset = Integer.parseInt(currTopicOLNameSEStatus[0]);   
        
//      byte[] bytes = FOLTXTMatcher.getInstance().getFullXmlTxt().getBytes();
//		offset = FOLTXTMatcher.byteOffsetToTextOffset(bytes, offset);
//		length = FOLTXTMatcher.textLength(bytes, offset, length);

//        anchorOffset = FOLTXTMatcher.byteOffsetToTextOffset(bytes, anchorOffset);
        int screenOffset = Integer.parseInt(currTopicOLNameSEStatus[3]) + offset - anchorOffset;
        String currAnchorS = String.valueOf(screenOffset);
        String currAnchorE = String.valueOf(screenOffset + length);      
        
        // String[]{Name, SP, EP, extLength, subSP, subEP}
        String[] currTopicAnchorNameSE = new String[]{currTopicOLNameSEStatus[2], currTopicOLNameSEStatus[3], currTopicOLNameSEStatus[4], currTopicOLNameSEStatus[6], currAnchorS, currAnchorE};        
        setTopicTextHighlighter(topicAnchorOLSEStatus, currTopicAnchorNameSE);
        thisTopicTextPane.getCaret().setDot(Integer.valueOf(currTopicAnchorNameSE[1]));
        thisTopicTextPane.scrollRectToVisible(thisTopicTextPane.getVisibleRect());
        thisTopicTextPane.repaint();

        setTABLinkPaneContent(currTargetFilePath, currTargetLang);
        // bep_Offset, linkID, Status
        String[] CurrTopicATargetSIDStatus = rscManager.getCurrTopicABepSIDStatusSA(thisLinkTextPane, currTopicID);
        setLinkBEPIcon(currTopicPAnchorStatus, CurrTopicATargetSIDStatus);
        // ---------------------------------------------------------------------
        LTWAssessmentToolControler.setCurrentAnchorProperty(currTopicOLNameSEStatus[0], currTopicOLNameSEStatus[1], currTopicOLNameSEStatus[3], currTopicOLNameSEStatus[4], currTopicOLNameSEStatus[5], currTopicOLNameSEStatus[6]);
//        String currTopicOLSEStatusKey = currTopicOLNameSEStatus[0] + "_" + currTopicOLNameSEStatus[1] + "_" + currTopicOLNameSEStatus[3] + "_" + currTopicOLNameSEStatus[4] + "_" + currTopicOLNameSEStatus[5] + "_" + currTopicOLNameSEStatus[6];
//        System.setProperty(sysPropertyCurrTopicOLSEStatusKey, currTopicOLSEStatusKey);
        // ---------------------------------------------------------------------
        String currAnchorName = myPooler.getPoolAnchorNameByOL(currTopicID, new String[]{currTopicOLNameSEStatus[0], currTopicOLNameSEStatus[1]});
        Vector<String> newTABFieldValues = new Vector<String>();
        newTABFieldValues.add(currTopicName);
        newTABFieldValues.add(currTopicID);
        newTABFieldValues.add(currAnchorName);
        newTABFieldValues.add(currTargetID);
//        if (Boolean.valueOf(System.getProperty(sysPropertyIsLinkWikiKey))) {
        //    pageTitle = rscManager.getWikipediaPageTitle(currTargetID);
//        } else {
//            pageTitle = rscManager.getTeAraFilePathByName(currTargetID);
//        }
        newTABFieldValues.add(pageTitle.trim());
        String[] pAnchorCompletionSA = rscManager.getOutgoingCompletion();
        newTABFieldValues.add(pAnchorCompletionSA[0] + " / " + pAnchorCompletionSA[1]);
        os.setTABFieldValues(newTABFieldValues);
    }
    // =========================================================================
    // Incoming Links: T.B.A.
    // =========================================================================

//    private void setIncomingTBA() {
//        System.setProperty(sysPropertyIsTABKey, "false");
//        rscManager.updateLinkingMode("incoming");
//        // Hashtable<incoming : topicFile>: [0]:Offset
//        // Hashtable<String, Hashtable<String, Vector<String[]>>>
//        topicBepsHT = myPooler.getTopicAllBeps();
//        // ---------------------------------------------------------------------
//        // 1) Get Topic ID & xmlFile Path
//        //    SET Topic Text Pane Content
//        // 2) Get all Anchor Text SCR SE + Status
//        //    SET Highlighter + Curr Anchor Text
//        // 3) Get Target Link
//        //    1st un-assessed Link, belonging to Curr Anchor Text
//        currTopicID = rscManager.getTopicID();
//        currTopicFilePath = rscManager.getCurrTopicXmlFile();
//        setTopicPaneContent(currTopicFilePath);
//
//
//        // ---------------------------------------------------------------------
//        // For 1st time to get the ANCHOR OL name SE
////        String topicBepsHTPrefix = "incoming : ";
////        Vector<String[]> currBepXMLOffset = topicBepsHT.get(topicBepsHTPrefix + currTopicID);
////        FOLTXTMatcher folMatcher = FOLTXTMatcher.getInstance();
////        folMatcher.getBepSCRSPVS(this.topicTextPane, currTopicID, currBepXMLOffset, this.isTopicWikipedia);
//        // ---------------------------------------------------------------------
//
//
//        // set Topic Bep ICONs
//        // V - String[]{Bep_Offset, S, Status}
//        topicBepScrStatus = rscManager.getTopicBepOSStatusVSA();
//        String currBepStartP = rscManager.getCurrTopicBepSCRS(this.topicTextPane, currTopicID);
//        // String[]{Bep_Offset, Status}
//        String[] currBepOStatus = rscManager.getCurrTopicBepOffsetStatus().split("_");
//        String topicBepStatus = currBepOStatus[1];
//        setTopicBEPIcon(topicBepScrStatus, currBepOStatus[0]);
//        // ---------------------------------------------------------------------
//        // Get current Link file ID & SCR Anchor S-E
//        String[] CurrTopicBTargetOLID = rscManager.getCurrTopicBTargetOLID(linkTextPane, currTopicID);
//        String currTargetOffset = CurrTopicBTargetOLID[0];
//        String currTargetLength = CurrTopicBTargetOLID[1];
//        String currTargetID = CurrTopicBTargetOLID[2];
//        String currTargetFilePath = "";
////        if (this.isLinkWikipedia) {
//            currTargetFilePath = rscManager.getWikipediaFilePathByName(currTargetID + ".xml");
////        } else {
////            currTargetFilePath = rscManager.getTeAraFilePathByName(currTargetID + ".xml");
////        }
//        String currBALinkStatus = this.myPooler.getPoolBepAnchorLinkStatus(currTopicID, currBepOStatus[0], new String[]{currTargetOffset, currTargetLength, currTargetID});
//        String toBALinkStatus = "";
//        if (topicBepStatus.equals("-1")) {
//            toBALinkStatus = "-1";
//        } else {
//            toBALinkStatus = currBALinkStatus;
//        }
//        setTBALinkPaneContent(currTargetFilePath, toBALinkStatus);
//        // String[]{Anchor_SP, EP, File_ID, Status}
//        String[] CurrTopicBTargetSEIDStatus = rscManager.getCurrTopicBAnchorSEIDStatus(linkTextPane, currTopicID);
//        setLinkAnchorHighlighter(CurrTopicBTargetSEIDStatus);
//        // ---------------------------------------------------------------------
//        String currTopicOLSEStatusKey = currBepOStatus[0] + "_" + bepLength + "_" + currBepStartP + "_" + String.valueOf(Integer.valueOf(currBepStartP) + Integer.valueOf(bepLength)) + "_" + currBepOStatus[1];
//        System.setProperty(this.sysPropertyCurrTopicOLSEStatusKey, currTopicOLSEStatusKey);
//        // ---------------------------------------------------------------------
//        String linkAnchorName = this.myPooler.getPoolBepLinkAnchorName(currTopicID, currBepOStatus[0], new String[]{currTargetOffset, currTargetLength, currTargetID});
//        Vector<String> newTABFieldValues = new Vector<String>();
//        newTABFieldValues.add(this.currTopicName);
//        newTABFieldValues.add(this.currTopicID);
//        newTABFieldValues.add(linkAnchorName);
//        newTABFieldValues.add(currTargetID);
//        String pageTitle = "";
////        if (Boolean.valueOf(System.getProperty(sysPropertyIsLinkWikiKey))) {
//            pageTitle = this.rscManager.getWikipediaPageTitle(currTargetID);
////        } else {
////            pageTitle = this.rscManager.getTeAraFilePathByName(currTargetID);
////        }
//        newTABFieldValues.add(pageTitle.trim());
//        String[] pAnchorCompletionSA = this.rscManager.getIncomingCompletion();
//        newTABFieldValues.add(pAnchorCompletionSA[0] + " / " + pAnchorCompletionSA[1]);
//        os.setTABFieldValues(newTABFieldValues);
//    }
    // =========================================================================
    // =========================================================================
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Set Text Pane & Highlight Anchor">
    // Topic Pane: Anchor
    private void setTopicPaneContent(String xmlFilePath, String lang) {
    	AdjustFont.setComponentFont(thisTopicTextPane, lang);
        if (!xmlFilePath.equals("")) {
            createTopicTextPane(xmlFilePath);
        } else {
            this.topicTextPane.setContentType(textContentType);
            this.topicTextPane.setText("<b>The topic file is missing or specified wrongly!!!</b>");
        }
    }

    private void createTopicTextPane(String xmlFilePath) {
        this.topicTextPane.setCaretPosition(0);
        this.topicTextPane.setMargin(new Insets(5, 5, 5, 5));
        initTopicDocument(xmlFilePath);
        this.topicTextPane.setCaretPosition(0);
    }

    private void initTopicDocument(String xmlFilePath) {
        Xml2Html xmlParser = new Xml2Html(xmlFilePath, this.isTopicWikipedia);
        this.topicTextPane.setContentType(textContentType);
        this.topicTextPane.setText(xmlParser.getHtmlContent().toString());
    }

    private static void updateSelectedAnchorHightlighter(JTextPane topicTextPane, String[] currTopicAnchorSCRSE, int thisAnchorStatus) {

//        int curr_ext_length = Integer.parseInt(currTopicAnchorSCRSE[3]);
    	int subanchorScreenStart = Integer.valueOf(currTopicAnchorSCRSE[4]);
    	int subanchorScreenEnd = Integer.valueOf(currTopicAnchorSCRSE[5]);
    	Highlighter highlighter = topicTextPane.getHighlighter();
        Object anchorHighlightReference = null;
//        String currAnchorSP = currTopicAnchorSCRSE[1].trim();
        
    	int sp = Integer.valueOf(currTopicAnchorSCRSE[1]);
    	int se = Integer.valueOf(currTopicAnchorSCRSE[2]);
    	
        int sp1 = 0, se1 = 0;
        int sp2 = 0, se2 = 0;
    	if (subanchorScreenStart != sp || subanchorScreenEnd != se) {
    		
    		if (subanchorScreenStart > sp) {
    			sp1 = sp;
    			se1 = subanchorScreenStart; 			
    		}
    		
    		if (subanchorScreenEnd < se) {
    			sp2 = subanchorScreenEnd;
    			se2 = se; 
    		}
//            if (thisAnchorSP.equals(currAnchorSP)) {
//            	if (curr_ext_length < 0) {
//                	sp1 = sp1 + curr_ext_length;
//                	se1 = se + curr_ext_length;                		
//            	}
//            	else if (curr_ext_length > 0) {
//                	sp1 = se;
//                	se1 = se + curr_ext_length;
//                }
//                else {
//                	se1 = sp1;
////                	sp1 = sp;
//                }
        } 
//            else {
//                if (ext_length > 0) {
//                	sp1 = sp;
//                	se1 = se + ext_length;
//                }
//            }	
    	try {
			anchorHighlightReference = highlighter.addHighlight(subanchorScreenStart, subanchorScreenEnd, painters.getSelectedPainter());

	        if (se1 > sp1) {
	            if (thisAnchorStatus == 0) {
	                anchorHighlightReference = highlighter.addHighlight(sp1, se1, painters.getAnchorPainter());
	            } else if (thisAnchorStatus == 1) {
	                anchorHighlightReference = highlighter.addHighlight(sp1, se1, painters.getCompletePainter());
	            } else if (thisAnchorStatus == -1) {
	                anchorHighlightReference = highlighter.addHighlight(sp1, se1, painters.getIrrelevantPainter());
	            }
	        }
	        
	        if (se2 > sp2) {
	            if (thisAnchorStatus == 0) {
	                anchorHighlightReference = highlighter.addHighlight(sp2, se2, painters.getAnchorPainter());
	            } else if (thisAnchorStatus == 1) {
	                anchorHighlightReference = highlighter.addHighlight(sp2, se2, painters.getCompletePainter());
	            } else if (thisAnchorStatus == -1) {
	                anchorHighlightReference = highlighter.addHighlight(sp2, se2, painters.getIrrelevantPainter());
	            }
	        }		
        } catch (BadLocationException e) {
        	Logger.getLogger(LTWAssessmentToolView.class.getName()).log(Level.SEVERE, null, e);
		} 
    }
    
    private void setTopicTextHighlighter(Vector<String[]> topicAnchorOLSEStatusVSA, String[] currTopicAnchorSCRSE) {
        /**
         * Vector<String[]> topicAnchorSEVSA :
         *     String[]{Anchor_Offset, Length, S, E, Status}
         * String[] currTopicAnchorSCRSE :
         *     String[]{AnchorName, SP, EP}
         */
        String currAnchorSP = currTopicAnchorSCRSE[1].trim();
        try {
            Highlighter highlighter = this.topicTextPane.getHighlighter();
            highlighter.removeAllHighlights();
            Object anchorHighlightReference = null;

            for (String[] thisAnchorSA : topicAnchorOLSEStatusVSA) {
                String thisAnchorSP = thisAnchorSA[2];
                int thisAnchorStatus = Integer.valueOf(thisAnchorSA[4]);
                int ext_length = Integer.valueOf(thisAnchorSA[5]);
            	int sp = Integer.valueOf(thisAnchorSA[2]);
            	int se = Integer.valueOf(thisAnchorSA[3]);
                
            	if (ext_length > 0)
            		se += ext_length;
            	
                if (thisAnchorSP.equals(currAnchorSP)) {
                	String [] currAnchorScreenInfo = new String[currTopicAnchorSCRSE.length];
                	System.arraycopy(currTopicAnchorSCRSE, 0, currAnchorScreenInfo, 0, currTopicAnchorSCRSE.length);
                	int anchorEnd = Integer.valueOf(currAnchorScreenInfo[2]) + Integer.valueOf(currAnchorScreenInfo[3]);
                	currAnchorScreenInfo[2] = String.valueOf(anchorEnd);
                	updateSelectedAnchorHightlighter(this.topicTextPane, currAnchorScreenInfo, thisAnchorStatus);
                    //anchorHighlightReference = highlighter.addHighlight(sp, se, painters.getSelectedPainter()); 
                }
                else {
	                if (thisAnchorStatus == 0) {
	                    anchorHighlightReference = highlighter.addHighlight(sp, se, painters.getAnchorPainter());
	                } else if (thisAnchorStatus == 1) {
	                    anchorHighlightReference = highlighter.addHighlight(sp, se, painters.getCompletePainter());
	                } else if (thisAnchorStatus == -1) {
	                    anchorHighlightReference = highlighter.addHighlight(sp, se, painters.getIrrelevantPainter());
	                }
                }
                 
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(LTWAssessmentToolView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    // -------------------------------------------------------------------------
    // Topic Pane: BEP : need to be FIXED

    private void setTopicBEPIcon(Vector<String[]> currBepSCRO, String currBepScrSP) {
        // Insert into Doc in JTextPane
        ImageIcon bepIcon = new ImageIcon(bepIconImageFilePath);
        StyledDocument styDoc = (StyledDocument) this.topicTextPane.getDocument();
        Style bepStyle = styDoc.addStyle("bepIcon", null);
        StyleConstants.setIcon(bepStyle, new ImageIcon(this.bepIconImageFilePath));
        Style bepCStyle = styDoc.addStyle("bepIconC", null);
        StyleConstants.setIcon(bepCStyle, new ImageIcon(this.bepIconCompleted));
        Style bepHStyle = styDoc.addStyle("bepIconH", null);
        StyleConstants.setIcon(bepHStyle, new ImageIcon(this.bepIconHighlight));
        Style bepNRStyle = styDoc.addStyle("bepIconNR", null);
        StyleConstants.setIcon(bepNRStyle, new ImageIcon(this.bepIconNonrelevant));
        try {
            // String[]{Bep_Offset, S, Status}
            for (String[] bepSCROffsetSA : currBepSCRO) {
                String thisBepSP = bepSCROffsetSA[0];
                String thisBepStatus = bepSCROffsetSA[2];
                if (thisBepSP.equals(currBepScrSP)) {
                    styDoc.insertString(Integer.valueOf(bepSCROffsetSA[1]), "HBEP", bepHStyle);
                } else {
                    if (Integer.valueOf(thisBepStatus.trim()) == 0) {
                        styDoc.insertString(Integer.valueOf(bepSCROffsetSA[1]), "TBEP", bepStyle);
                    } else if (Integer.valueOf(thisBepStatus.trim()) == 1) {
                        styDoc.insertString(Integer.valueOf(bepSCROffsetSA[1]), "CBEP", bepCStyle);
                    } else if (Integer.valueOf(thisBepStatus.trim()) == -1) {
                        styDoc.insertString(Integer.valueOf(bepSCROffsetSA[1]), "NBEP", bepNRStyle);
                    }
                }
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(topicPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.topicTextPane.repaint();
    }
    // =========================================================================
    // Link Pane: BEP

    private void setTABLinkPaneContent(String xmlFilePath, String lang) {
        if (!xmlFilePath.equals("")) {
            createLinkTextPane(xmlFilePath, lang);
        } else {
            this.linkTextPane.setContentType(textContentType);
            this.linkTextPane.setText("<b>The target file is missing or specified wrongly!!!</b>");
        }
    }

    private void setTBALinkPaneContent(String xmlFilePath, String bgStatus, String lang) {
        if (!xmlFilePath.equals("")) {
            createLinkTextPane(xmlFilePath, lang);
            if (bgStatus.equals("-1")) {
                this.linkTextPane.setBackground(this.linkPaneNonRelColor);
            } else if (bgStatus.equals("0")) {
                this.linkTextPane.setBackground(this.linkPaneWhiteColor);
            } else if (bgStatus.equals("1")) {
                this.linkTextPane.setBackground(this.linkPaneRelColor);
            }
        } else {
            this.linkTextPane.setContentType(textContentType);
            this.linkTextPane.setText("<b>The target file is missing or specified wrongly!!!</b>");
        }
    }

    private void createLinkTextPane(String xmlFilePath, String lang) {
    	AdjustFont.getInstance().setComponentFont(this.linkTextPane, lang);
    	AdjustFont.getInstance().setComponentFont(this.lblTargetTitle, lang);
        this.linkTextPane.setCaretPosition(0);
        this.linkTextPane.setMargin(new Insets(5, 5, 5, 5));
        initLinkDocument(xmlFilePath);
        this.linkTextPane.setCaretPosition(0);
    }

    private void initLinkDocument(String xmlFilePath) {
        Xml2Html xmlParser = new Xml2Html(xmlFilePath, this.isLinkWikipedia);
        this.linkTextPane.setContentType(textContentType);
        this.linkTextPane.setText(xmlParser.getHtmlContent().toString());
    }

    private void setLinkBEPIcon(String currTopicPAnchorStatus, String[] CurrTopicATargetSIDStatus) {
        // Insert into Doc in JTextPane
        ImageIcon bepIcon = new ImageIcon(bepIconImageFilePath);
        this.linkTextPane.insertIcon(bepIcon);
        this.linkTextPane.repaint();
        // bepSP: -1 or > -1
        String bepSP = CurrTopicATargetSIDStatus[0].trim();
        // bepStatus: 0 , -1 , 1
        String bepStatus = CurrTopicATargetSIDStatus[2].trim();
        if (Integer.valueOf(bepSP) > -1) {
            StyledDocument styDoc = (StyledDocument) this.linkTextPane.getDocument();
            Style bepStyle = styDoc.addStyle("bepIcon", null);
            StyleConstants.setIcon(bepStyle, new ImageIcon(bepIconImageFilePath));
            try {
                styDoc.insertString(Integer.valueOf(bepSP), "TBEP", bepStyle);
            } catch (BadLocationException ex) {
                Logger.getLogger(topicPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (Integer.valueOf(currTopicPAnchorStatus) == -1) {
            this.linkTextPane.setBackground(this.linkPaneNonRelColor);
        } else {
            if (Integer.valueOf(bepStatus) == -1) {
                this.linkTextPane.setBackground(this.linkPaneNonRelColor);
            } else if (Integer.valueOf(bepStatus) == 1) {
                this.linkTextPane.setBackground(this.linkPaneRelColor);
            }
        }
        this.linkTextPane.repaint();
    }
    // -------------------------------------------------------------------------
    // Link Pane: Anchor

    private void setLinkAnchorHighlighter(String[] CurrTopicBTargetSEIDStatus) {
        /**
         * String[] topicAnchorSEVSA :
         *     String[]{Anchor_SP, EP, File_ID, Status}
         */
        try {
            Highlighter highlighter = this.linkTextPane.getHighlighter();
            highlighter.removeAllHighlights();
            Object anchorHighlightReference;
            int thisAnchorStatus = Integer.valueOf(CurrTopicBTargetSEIDStatus[3]);
            if (thisAnchorStatus == 0) {
                anchorHighlightReference = highlighter.addHighlight(Integer.valueOf(CurrTopicBTargetSEIDStatus[0]), Integer.valueOf(CurrTopicBTargetSEIDStatus[1]), painters.getAnchorPainter());
            } else if (thisAnchorStatus == 1) {
                anchorHighlightReference = highlighter.addHighlight(Integer.valueOf(CurrTopicBTargetSEIDStatus[0]), Integer.valueOf(CurrTopicBTargetSEIDStatus[1]), painters.getCompletePainter());
            } else if (thisAnchorStatus == -1) {
                anchorHighlightReference = highlighter.addHighlight(Integer.valueOf(CurrTopicBTargetSEIDStatus[0]), Integer.valueOf(CurrTopicBTargetSEIDStatus[1]), painters.getIrrelevantPainter());
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(LTWAssessmentToolView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Update Anchor Highlight & BEP Icon">
    private void updateLinkAnchorHighlight(JTextPane linkPane, String[] anchorSEPosSA) {
        // This might need to handle: isAssessment
        // 1) YES:
        // 2) NO: ONLY highlight Anchor Text
        try {
            Highlighter txtPaneHighlighter = linkPane.getHighlighter();
            int[] achorSCRPos = new int[]{Integer.valueOf(anchorSEPosSA[0]), Integer.valueOf(anchorSEPosSA[1])};
            Object aHighlightRef = txtPaneHighlighter.addHighlight(achorSCRPos[0], achorSCRPos[1], painters.getAnchorPainter());
        } catch (BadLocationException ex) {
            Logger.getLogger(topicPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void updateTopicAnchorsHighlight(JTextPane topicPane, String[] preAnchorSEStatus, String[] currAnchorSE, int currLinkStatus) {
        // This might need to handle: isAssessment
        // 1) YES: Highlight Curr Selected Anchor Text, keep others remaining as THEIR Colors
        // 2) NO: Highlight Curr Selected Anchor Text, keep others remaining as Anchor Text Color
        try {
            boolean preDONEFlag = false;
            boolean currDONEFlag = false;
            Highlighter txtPaneHighlighter = topicPane.getHighlighter();
            Highlight[] highlights = txtPaneHighlighter.getHighlights();
            Object anchorHighlightRef = null;
            int[] achorSCRPos = new int[]{Integer.valueOf(currAnchorSE[1]), Integer.valueOf(currAnchorSE[2])};
            log("PRE: " + preAnchorSEStatus[0] + " - " + preAnchorSEStatus[1] + " - " + preAnchorSEStatus[2]);
            log("CURR: " + currAnchorSE[1] + " - " + currAnchorSE[2]);
            
            int preScreenS = Integer.valueOf(preAnchorSEStatus[0]);
            int preScreenE = Integer.valueOf(preAnchorSEStatus[1]);
//            int extLength = Integer.parseInt(preAnchorSEStatus[3]);
//            int sp, se;
            for (int i = 0; i < highlights.length; i++) {
                int sPos = highlights[i].getStartOffset();
                int ePos = highlights[i].getEndOffset();
                if (sPos >= achorSCRPos[0] &&  ePos <= achorSCRPos[1]) {
                    txtPaneHighlighter.removeHighlight(highlights[i]);
                    // String[]{Name, SP, EP, extLength, subSP, subEP}
//                    currDONEFlag = true;
//                    if (currDONEFlag && preDONEFlag) {
//                        break;
//                    }
                } else {
                    if (sPos >= preScreenS && ePos <= preScreenE) {
                        txtPaneHighlighter.removeHighlight(highlights[i]);

//                        topicPane.repaint();
//                        preDONEFlag = true;
//                        if (currDONEFlag && preDONEFlag) {
//                            break;
//                        }
                    }
//                    else if (Integer.valueOf(preAnchorSEStatus[1])) {
//                    	txtPaneHighlighter.removeHighlight(highlights[i]);
//                    }
                }
            }
            
            updateSelectedAnchorHightlighter(topicPane, currAnchorSE, currLinkStatus);
            //anchorHighlightRef = txtPaneHighlighter.addHighlight(sPos, ePos, painters.getSelectedPainter());

            String pAnchorStatus = preAnchorSEStatus[2];
            if (Integer.valueOf(pAnchorStatus) == 1) {
                anchorHighlightRef = txtPaneHighlighter.addHighlight(preScreenS, preScreenE, painters.getCompletePainter());
            } else if (Integer.valueOf(pAnchorStatus) == -1) {
                anchorHighlightRef = txtPaneHighlighter.addHighlight(preScreenS, preScreenE, painters.getIrrelevantPainter());
            } else {
                anchorHighlightRef = txtPaneHighlighter.addHighlight(preScreenS, preScreenE, painters.getAnchorPainter());
            }
            topicPane.repaint();
        } catch (BadLocationException ex) {
            Logger.getLogger(topicPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void updatePaneBepIcon(JTextPane txtPane, Vector<String> bepSCROffset, boolean isHighlighBEP) {
        // TODO: "isHighlighBEP"
        // 1) YES: Remove previous Highlight BEPs + Make a new Highlight BEP
        // 2) NO: INSERT BEP ICONs for this Topic
        try {
            StyledDocument styDoc = (StyledDocument) txtPane.getDocument();
            Vector<String[]> HBepSCROffsetV = new Vector<String[]>();
            Vector<String> bepOLListV = rscManager.getTopicBepsOSVS();
            for (String thisBepOL : bepOLListV) {
                String[] thisBepOLSA = thisBepOL.split(" : ");
                HBepSCROffsetV.add(thisBepOLSA);
            }

            Style bepHStyle = styDoc.addStyle("bepHIcon", null);
            StyleConstants.setIcon(bepHStyle, new ImageIcon(bepIconHighlight));
            Style bepCStyle = styDoc.addStyle("bepCIcon", null);
            StyleConstants.setIcon(bepCStyle, new ImageIcon(bepIconCompleted));
            Style bepNStyle = styDoc.addStyle("bepNIcon", null);
            StyleConstants.setIcon(bepNStyle, new ImageIcon(bepIconNonrelevant));
            Style bepStyle = styDoc.addStyle("bepIcon", null);
            StyleConstants.setIcon(bepStyle, new ImageIcon(bepIconImageFilePath));

            if (isHighlighBEP) {
                for (String[] scrBepOS : HBepSCROffsetV) {
                    styDoc.remove(Integer.valueOf(scrBepOS[1]), bepLength);
                    String thisPBepStatus = myPooler.getPoolBepStatus(currTopicID, scrBepOS[0]);
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

    private Hashtable<String, String[]> populateTopicAnchorOLTSENHT() {
        Hashtable<String, String[]> thisAnchorOLTSENHT = new Hashtable<String, String[]>();
        Vector<String> topicAnchorsOLNameSEVS = this.rscManager.getTopicAnchorsOLNameSEV();
        for (int i = 0; i < topicAnchorsOLNameSEVS.size(); i++) {
            String topicAnchorsOLNameSE = topicAnchorsOLNameSEVS.elementAt(i);
            String[] topicAnchorsOLNameSESA = topicAnchorsOLNameSE.split(" : ");
            String tASE = topicAnchorsOLNameSESA[3] + "_" + topicAnchorsOLNameSESA[4];
            thisAnchorOLTSENHT.put(tASE, new String[]{topicAnchorsOLNameSESA[0], topicAnchorsOLNameSESA[1],
                        topicAnchorsOLNameSESA[2], topicAnchorsOLNameSESA[3], topicAnchorsOLNameSESA[4], String.valueOf(i)});
        }
        return thisAnchorOLTSENHT;
    }

    private Hashtable<String, String[]> populateTopicBepOSNHT() {
        Hashtable<String, String[]> topicBepOSNHT = new Hashtable<String, String[]>();
        Vector<String> topicBepsOSVS = this.rscManager.getTopicBepsOSVS();
        for (int i = 0; i < topicBepsOSVS.size(); i++) {
            String topicBepsOS = topicBepsOSVS.elementAt(i);
            String[] topicBepsOSSA = topicBepsOS.split(" : ");
            String tBS = topicBepsOSSA[1];
            topicBepOSNHT.put(tBS, new String[]{topicBepsOSSA[0], topicBepsOSSA[1], String.valueOf(i)});
        }
        return topicBepOSNHT;
    }
    
    private void setupComponentFont() {
    	
    	AdjustFont.setComponentFont(lblTopicTitle, AppResource.sourceLang);
    	AdjustFont.setComponentFont(lblPoolAnchor, AppResource.sourceLang);
    	
    }
// </editor-fold>
}



