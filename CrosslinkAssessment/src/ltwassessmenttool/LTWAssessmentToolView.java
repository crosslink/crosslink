/*
 * LTWAssessmentToolView.java
 */
package ltwassessmenttool;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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
import ltwassessment.assessment.AssessedAnchor;
import ltwassessment.assessment.AssessmentThread;
import ltwassessment.assessment.Bep;
import ltwassessment.assessment.CurrentFocusedAnchor;
import ltwassessment.assessment.IndexedAnchor;
import ltwassessment.assessment.LTWAssessmentToolControler;
import ltwassessment.font.AdjustFont;
import ltwassessment.listener.CaretListenerLabel;
import ltwassessment.listener.linkPaneMouseListener;
import ltwassessment.listener.topicPaneMouseListener;
import ltwassessment.parsers.Xml2Html;
import ltwassessment.parsers.FOLTXTMatcher;
import ltwassessment.parsers.PoolerManager;
import ltwassessment.parsers.ResourcesManager;
import ltwassessment.utility.ObservableSingleton;
import ltwassessment.utility.fieldUpdateObserver;
import ltwassessment.utility.highlightPainters;
import ltwassessment.utility.PoolUpdater;
import ltwassessment.view.Link;
import ltwassessment.view.ScreenAnchor;
import ltwassessment.view.TopicHighlightManager;
import ltwassessment.wiki.WikiArticleXml;

/**
 * The main frame for the Assessment Tool
 */
public class LTWAssessmentToolView extends FrameView {

    private static ResourceMap resourceMap;
    private static String textContentType = "";
//    private String wikipediaTopicFileDir = "";
//    private String topicAnchorsHTPrefix = "";
//    private String topicBepsHTPrefix = "";
    private String defaultWikipediaDirectory = Assessment.RESOURCES_PATH + "Collections" + File.separator;
    private String defaultTeAraDirectory = "";
    private static String afTasnCollectionErrors = "";
    private String defaultPoolXmlFilePath = "";
    // -------------------------------------------------------------------------
    // Variables
    private boolean isTopicWikipedia = false;
    private boolean isLinkWikipedia = false;

//    private Vector<String[]> topicBepScrStatus = new Vector<String[]>();
//    private Vector<String[]> currBepXMLOffset = new Vector<String[]>();
//    private Vector<String[]> currBepSCROffset = new Vector<String[]>();
    // -------------------------------------------------------------------------
    // For Incoming: [0]:Offset
    // -------------------------------------------------------------------------
    // Declare External Classes
    private static PoolerManager myPooler;
    private static ResourcesManager rscManager = null;
    private static highlightPainters painters = new highlightPainters();
    private static ObservableSingleton os = null;
    private fieldUpdateObserver fuObserver = null;
    // -------------------------------------------------------------------------
    //1) including participant-id, run-id, task, collection
    //2) record Topic -> [0]:File ID & [1]:Name
    //3) record Topic (outgoing : topicFile) -> [0]:Offset & [1]:Length & [2]:Anchor_Name
    //4) record Topic (incoming : topicFile) -> [0]:Offset
//    private String[] afProperty = new String[4];
//    private Vector<String[]> RunTopics = new Vector<String[]>();

//    private Hashtable<String, String[]> topicAnchorOLTSENHT = new Hashtable<String, String[]>();
    // -------------------------------------------------------------------------
//    private static Color linkPaneWhiteColor = Color.WHITE;
//    private static Color linkPaneRelColor = new Color(168, 232, 177);
//    private static Color linkPaneNonRelColor = new Color(255, 183, 165);
    // -------------------------------------------------------------------------
    private static String bepIconImageFilePath = "";
    private static String bepIconCompleted = "";
    private static String bepIconNonrelevant = "";
    private static String bepIconHighlight = "";
    
    private static JTextPane topicTextPane = null;
    private static JTextPane linkTextPane = null;
    
    private static PoolUpdater myPUpdater = null;
    
    private AssessmentThread assessmentThread = null;

    static void log(Object content) {
        System.out.println(content);
    }

    static void errlog(Object content) {
        System.err.println("errlog: " + content);
    }
    public static final String[] outgoingTABColumnNames = {"Topic", "Pool Anchor", "Anchor", "BEP", "HiddenField"};
    public static final String[] incomingTBAColumnNames = {"Topic", "BEP", "Anchor", "File ID", "HiddenField"};
    ButtonGroup group = new ButtonGroup();

	private static String currTopicID;
    
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
        fuObserver = new fieldUpdateObserver(os, this.lblTopicTitle, this.lblTopicID, this.lblAnchor, this.lblPoolAnchor, this.lblTargetID, this.lblTargetTitle);
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
//        defaultWikipediaDirectory = resourceMap.getString("directory.Wikipedia");
//        defaultTeAraDirectory = resourceMap.getString("directory.TeAra");
//        defaultPoolXmlFilePath = resourceMap.getString("pooling.AFXmlFolder");
        if (Assessment.getInstance().assessingGroundTruthLinks())
        	defaultWikipediaDirectory = Assessment.RESOURCES_PATH + "Collections.gt" + File.separator;
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
        	ResourcesManager.getInstance().updateWikipediaCollectionDirectory(wikiCollectionFolder.getAbsolutePath());
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
        
        setupOthers(app);
        
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
            LTWAssessmentToolControler.getInstance().start();
            
            assessmentThread = new AssessmentThread(thisTopicTextPane, thisLinkTextPane);
            assessmentThread.start();
    }
       
    private void setupOthers(SingleFrameApplication app) {
    	TopicHighlightManager.getInstance().setPane(thisTopicTextPane);
    	TopicHighlightManager.getInstance().setLinkPane(thisLinkTextPane);
    	
    	LTWAssessmentToolControler.getInstance().setContainter(app.getMainFrame(), thisTopicTextPane, thisLinkTextPane);
    	LTWAssessmentToolControler.getInstance().setStatusMessageLabel(statusMessageLabel);
    	LTWAssessmentToolControler.getInstance().setLblPoolAnchor(lblAnchor);
    	LTWAssessmentToolControler.getInstance().setLblAnchor(lblPoolAnchor);
    	LTWAssessmentToolControler.getInstance().setLblTargetTitle(lblTargetTitle);
    	LTWAssessmentToolControler.getInstance().setLblTopicTitle(lblTopicTitle);
    	LTWAssessmentToolControler.getInstance().setLblCompletion(lblCompletion);
    	
    	LTWAssessmentToolControler.getInstance().setTextContentType(textContentType);
		
//    	this.getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	this.getFrame().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    	WindowAdapter windowListener = new WindowAdapter()
    	   {
    	   // anonymous WindowAdapter class
    	   public void windowClosing ( WindowEvent w )
    	      {
    		   	LTWAssessmentToolControler.getInstance().backupPool(null);
    		   	System.exit(0);
    	      } // end windowClosing
    	   };// end anonymous class
    	this.getFrame().addWindowListener( windowListener );
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

//    private boolean corpusDirChecker(boolean topicIsWikipedia) {
//        boolean rightCorpusDir = false;
//        String ranTopicID = rscManager.getTopicID();
//        String runFilePath = "";
//        if (topicIsWikipedia) {
//            runFilePath = defaultWikipediaDirectory + ranTopicID + ".xml";
//        } else {
//            runFilePath = defaultTeAraDirectory + ranTopicID + ".xml";
//        }
//        File ranXmlFile = new File(runFilePath);
//        if (ranXmlFile.exists()) {
//            rightCorpusDir = true;
//        } else {
//            rightCorpusDir = false;
//            JOptionPane.showMessageDialog(mainPanel, "The XML File, " + ranTopicID + ".xml , cannot be found in specified Collection Directory, " + runFilePath + "!");
//        }
//        return rightCorpusDir;
//    }

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
        anchorInfoPane = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        lblAnchor = new javax.swing.JLabel();
        lblPoolAnchor = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        docInfoPane = new javax.swing.JPanel();
        sourceDocPanel = new javax.swing.JPanel();
        lblTopicTitle = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        lblTopicID = new javax.swing.JLabel();
        targetDocPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        lblTargetTitle = new javax.swing.JLabel();
        lblTargetID = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        completionInfoPane = new javax.swing.JPanel();
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
        mainPanel.setAutoscrolls(true);
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setPreferredSize(new java.awt.Dimension(726, 644));

        jInfoPanel.setBackground(resourceMap.getColor("jInfoPanel.background")); // NOI18N
        jInfoPanel.setName("jInfoPanel"); // NOI18N

        anchorInfoPane.setBackground(resourceMap.getColor("anchorInfoPane.background")); // NOI18N
        anchorInfoPane.setName("anchorInfoPane"); // NOI18N

        jLabel6.setFont(resourceMap.getFont("jLabel6.font")); // NOI18N
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        lblAnchor.setFont(resourceMap.getFont("lblAnchor.font")); // NOI18N
        lblAnchor.setForeground(resourceMap.getColor("lblAnchor.foreground")); // NOI18N
        lblAnchor.setText(resourceMap.getString("lblAnchor.text")); // NOI18N
        lblAnchor.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblAnchor.setName("lblAnchor"); // NOI18N

        lblPoolAnchor.setFont(resourceMap.getFont("lblPoolAnchor.font")); // NOI18N
        lblPoolAnchor.setForeground(resourceMap.getColor("lblPoolAnchor.foreground")); // NOI18N
        lblPoolAnchor.setText(resourceMap.getString("lblPoolAnchor.text")); // NOI18N
        lblPoolAnchor.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblPoolAnchor.setName("lblPoolAnchor"); // NOI18N

        jLabel9.setFont(resourceMap.getFont("jLabel9.font")); // NOI18N
        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        javax.swing.GroupLayout anchorInfoPaneLayout = new javax.swing.GroupLayout(anchorInfoPane);
        anchorInfoPane.setLayout(anchorInfoPaneLayout);
        anchorInfoPaneLayout.setHorizontalGroup(
            anchorInfoPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(anchorInfoPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(anchorInfoPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPoolAnchor, javax.swing.GroupLayout.DEFAULT_SIZE, 489, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(anchorInfoPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(lblAnchor, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        anchorInfoPaneLayout.setVerticalGroup(
            anchorInfoPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, anchorInfoPaneLayout.createSequentialGroup()
                .addGroup(anchorInfoPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(anchorInfoPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(anchorInfoPaneLayout.createSequentialGroup()
                        .addComponent(lblAnchor, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
                        .addGap(19, 19, 19))
                    .addGroup(anchorInfoPaneLayout.createSequentialGroup()
                        .addComponent(lblPoolAnchor, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        anchorInfoPaneLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {lblAnchor, lblPoolAnchor});

        docInfoPane.setBackground(resourceMap.getColor("docInfoPane.background")); // NOI18N
        docInfoPane.setName("docInfoPane"); // NOI18N
        docInfoPane.setPreferredSize(new java.awt.Dimension(55, 143));

        sourceDocPanel.setBackground(resourceMap.getColor("sourceDocPanel.background")); // NOI18N
        sourceDocPanel.setName("sourceDocPanel"); // NOI18N

        lblTopicTitle.setFont(resourceMap.getFont("lblTopicTitle.font")); // NOI18N
        lblTopicTitle.setForeground(resourceMap.getColor("lblTopicTitle.foreground")); // NOI18N
        lblTopicTitle.setText(resourceMap.getString("lblTopicTitle.text")); // NOI18N
        lblTopicTitle.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblTopicTitle.setMaximumSize(new java.awt.Dimension(30, 14));
        lblTopicTitle.setMinimumSize(new java.awt.Dimension(30, 14));
        lblTopicTitle.setName("lblTopicTitle"); // NOI18N
        lblTopicTitle.setPreferredSize(new java.awt.Dimension(30, 14));

        jLabel4.setFont(resourceMap.getFont("jLabel4.font")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        lblTopicID.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTopicID.setText(resourceMap.getString("lblTopicID.text")); // NOI18N
        lblTopicID.setMaximumSize(new java.awt.Dimension(30, 14));
        lblTopicID.setMinimumSize(new java.awt.Dimension(30, 14));
        lblTopicID.setName("lblTopicID"); // NOI18N
        lblTopicID.setPreferredSize(new java.awt.Dimension(30, 14));

        javax.swing.GroupLayout sourceDocPanelLayout = new javax.swing.GroupLayout(sourceDocPanel);
        sourceDocPanel.setLayout(sourceDocPanelLayout);
        sourceDocPanelLayout.setHorizontalGroup(
            sourceDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sourceDocPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sourceDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(sourceDocPanelLayout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTopicID, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTopicTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE))
                .addContainerGap())
        );
        sourceDocPanelLayout.setVerticalGroup(
            sourceDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sourceDocPanelLayout.createSequentialGroup()
                .addGroup(sourceDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(lblTopicID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(lblTopicTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        targetDocPanel.setBackground(resourceMap.getColor("targetDocPanel.background")); // NOI18N
        targetDocPanel.setName("targetDocPanel"); // NOI18N
        targetDocPanel.setVerifyInputWhenFocusTarget(false);

        jLabel3.setFont(resourceMap.getFont("jLabel3.font")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        lblTargetTitle.setText(resourceMap.getString("lblTargetTitle.text")); // NOI18N
        lblTargetTitle.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblTargetTitle.setName("lblTargetTitle"); // NOI18N

        lblTargetID.setText(resourceMap.getString("lblTargetID.text")); // NOI18N
        lblTargetID.setName("lblTargetID"); // NOI18N

        jLabel2.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout targetDocPanelLayout = new javax.swing.GroupLayout(targetDocPanel);
        targetDocPanel.setLayout(targetDocPanelLayout);
        targetDocPanelLayout.setHorizontalGroup(
            targetDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(targetDocPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(targetDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTargetTitle, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                    .addGroup(targetDocPanelLayout.createSequentialGroup()
                        .addGroup(targetDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTargetID, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        targetDocPanelLayout.setVerticalGroup(
            targetDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(targetDocPanelLayout.createSequentialGroup()
                .addGroup(targetDocPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(targetDocPanelLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(10, 10, 10)
                        .addComponent(jLabel2))
                    .addComponent(lblTargetID, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTargetTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout docInfoPaneLayout = new javax.swing.GroupLayout(docInfoPane);
        docInfoPane.setLayout(docInfoPaneLayout);
        docInfoPaneLayout.setHorizontalGroup(
            docInfoPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(docInfoPaneLayout.createSequentialGroup()
                .addComponent(sourceDocPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(targetDocPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        docInfoPaneLayout.setVerticalGroup(
            docInfoPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(docInfoPaneLayout.createSequentialGroup()
                .addGroup(docInfoPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sourceDocPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(targetDocPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        completionInfoPane.setBackground(resourceMap.getColor("completionInfoPane.background")); // NOI18N
        completionInfoPane.setName("completionInfoPane"); // NOI18N

        jLabel7.setFont(resourceMap.getFont("jLabel7.font")); // NOI18N
        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        lblCompletion.setFont(resourceMap.getFont("lblCompletion.font")); // NOI18N
        lblCompletion.setForeground(resourceMap.getColor("lblCompletion.foreground")); // NOI18N
        lblCompletion.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCompletion.setText(resourceMap.getString("lblCompletion.text")); // NOI18N
        lblCompletion.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblCompletion.setMaximumSize(new java.awt.Dimension(30, 14));
        lblCompletion.setMinimumSize(new java.awt.Dimension(30, 14));
        lblCompletion.setName("lblCompletion"); // NOI18N
        lblCompletion.setPreferredSize(new java.awt.Dimension(30, 14));

        javax.swing.GroupLayout completionInfoPaneLayout = new javax.swing.GroupLayout(completionInfoPane);
        completionInfoPane.setLayout(completionInfoPaneLayout);
        completionInfoPaneLayout.setHorizontalGroup(
            completionInfoPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(completionInfoPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(completionInfoPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(completionInfoPaneLayout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addComponent(lblCompletion, javax.swing.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)))
        );
        completionInfoPaneLayout.setVerticalGroup(
            completionInfoPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(completionInfoPaneLayout.createSequentialGroup()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCompletion, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jInfoPanelLayout = new javax.swing.GroupLayout(jInfoPanel);
        jInfoPanel.setLayout(jInfoPanelLayout);
        jInfoPanelLayout.setHorizontalGroup(
            jInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInfoPanelLayout.createSequentialGroup()
                .addComponent(docInfoPane, javax.swing.GroupLayout.PREFERRED_SIZE, 607, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(completionInfoPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(anchorInfoPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(679, Short.MAX_VALUE))
        );
        jInfoPanelLayout.setVerticalGroup(
            jInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jInfoPanelLayout.createSequentialGroup()
                .addGroup(jInfoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(docInfoPane, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(completionInfoPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jInfoPanelLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(anchorInfoPane, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jSplitPane1.setBackground(resourceMap.getColor("jSplitPane1.background")); // NOI18N
        jSplitPane1.setMinimumSize(new java.awt.Dimension(1, 1));
        jSplitPane1.setName("jSplitPane1"); // NOI18N
        jSplitPane1.setPreferredSize(new java.awt.Dimension(732, 644));

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
        linkTextScrollPane.setPreferredSize(new java.awt.Dimension(226, 445));
        linkTextScrollPane.setRequestFocusEnabled(false);

        thisLinkTextPane.setEditable(false);
        thisLinkTextPane.setMaximumSize(new java.awt.Dimension(300, 400));
        thisLinkTextPane.setName("thisLinkTextPane"); // NOI18N
        thisLinkTextPane.setPreferredSize(new java.awt.Dimension(225, 644));
        linkTextScrollPane.setViewportView(thisLinkTextPane);

        jSplitPane1.setRightComponent(linkTextScrollPane);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jInfoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 2347, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(jInfoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 731, Short.MAX_VALUE))
        );

        menuBar.setBackground(resourceMap.getColor("menuBar.background")); // NOI18N
        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setBackground(resourceMap.getColor("helpMenu.background")); // NOI18N
        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ltwassessmenttool.LTWAssessmentToolApp.class).getContext().getActionMap(LTWAssessmentToolView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
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
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 2347, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                .addContainerGap(916, Short.MAX_VALUE)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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


    
//    private static void updateAnchor(AssessedAnchor currentAnchor, AssessedAnchor nextAnchor) {
//
////            CurrentFocusedAnchor.getCurrentFocusedAnchor().setCurrentAnchorProperty(indexedAnchor.getOffset(), indexedAnchor.getLength(), Integer.parseInt(indexedAnchor.getScreenPosStart()), Integer.parseInt(indexedAnchor.getScreenPosEnd()), Integer.parseInt(indexedAnchor.getStatus()), indexedAnchor.getExtLength());
////        }
//    }
    
    
    // <editor-fold defaultstate="collapsed" desc="Button Click to Go Back / Forward A Link">
    @Action
    public void btnGoBackALink() {
        // Click the button to Go Back one Link
        
        LTWAssessmentToolControler.getInstance().moveBackwardALink();
    }

    @Action
    public void btnGoForwardALink() {
    	LTWAssessmentToolControler.getInstance().moveForwardALink(false, false);
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
//        setOutgoingTAB();
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

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
//        System.exit(0);
        LTWAssessmentToolControler.getInstance().finishAssessment();
    }//GEN-LAST:event_exitMenuItemActionPerformed
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Pre-Variables-Declaration">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel anchorInfoPane;
    private javax.swing.JButton btnGoBack;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JPanel completionInfoPane;
    private javax.swing.JPanel docInfoPane;
    private javax.swing.JRadioButtonMenuItem inRadioBtn;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jInfoPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
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
    private javax.swing.JLabel lblAnchor;
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
    private javax.swing.JPanel sourceDocPanel;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JPanel targetDocPanel;
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

    // <editor-fold defaultstate="collapsed" desc="Set Text Pane & Highlight Anchor">

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

//    private void setTBALinkPaneContent(String xmlFilePath, String bgStatus, String lang) {
//        if (!xmlFilePath.equals("")) {
//            createLinkTextPane(xmlFilePath, lang);
////            if (bgStatus.equals("-1")) {
////                this.linkTextPane.setBackground(this.linkPaneNonRelColor);
////            } else if (bgStatus.equals("0")) {
////                this.linkTextPane.setBackground(this.linkPaneWhiteColor);
////            } else if (bgStatus.equals("1")) {
////                this.linkTextPane.setBackground(this.linkPaneRelColor);
////            }
//        } else {
//            this.linkTextPane.setContentType(textContentType);
//            this.linkTextPane.setText("<b>The target file is missing or specified wrongly!!!</b>");
//        }
//    }

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
//        if (Integer.valueOf(currTopicPAnchorStatus) == -1) {
//            this.linkTextPane.setBackground(this.linkPaneNonRelColor);
//        } else {
//            if (Integer.valueOf(bepStatus) == -1) {
//                this.linkTextPane.setBackground(this.linkPaneNonRelColor);
//            } else if (Integer.valueOf(bepStatus) == 1) {
//                this.linkTextPane.setBackground(this.linkPaneRelColor);
//            }
//        }
//        this.linkTextPane.repaint();
    }
    // -------------------------------------------------------------------------
    // Link Pane: Anchor

//    private void setLinkAnchorHighlighter(String[] CurrTopicBTargetSEIDStatus) {
//        /**
//         * String[] topicAnchorSEVSA :
//         *     String[]{Anchor_SP, EP, File_ID, Status}
//         */
//        try {
//            Highlighter highlighter = this.linkTextPane.getHighlighter();
//            highlighter.removeAllHighlights();
//            Object anchorHighlightReference;
//            int thisAnchorStatus = Integer.valueOf(CurrTopicBTargetSEIDStatus[3]);
//            if (thisAnchorStatus == 0) {
//                anchorHighlightReference = highlighter.addHighlight(Integer.valueOf(CurrTopicBTargetSEIDStatus[0]), Integer.valueOf(CurrTopicBTargetSEIDStatus[1]), painters.getAnchorPainter());
//            } else if (thisAnchorStatus == 1) {
//                anchorHighlightReference = highlighter.addHighlight(Integer.valueOf(CurrTopicBTargetSEIDStatus[0]), Integer.valueOf(CurrTopicBTargetSEIDStatus[1]), painters.getCompletePainter());
//            } else if (thisAnchorStatus == -1) {
//                anchorHighlightReference = highlighter.addHighlight(Integer.valueOf(CurrTopicBTargetSEIDStatus[0]), Integer.valueOf(CurrTopicBTargetSEIDStatus[1]), painters.getIrrelevantPainter());
//            }
//        } catch (BadLocationException ex) {
//            Logger.getLogger(LTWAssessmentToolView.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Update Anchor Highlight & BEP Icon">
//    private void updateLinkAnchorHighlight(JTextPane linkPane, String[] anchorSEPosSA) {
//        // This might need to handle: isAssessment
//        // 1) YES:
//        // 2) NO: ONLY highlight Anchor Text
//        try {
//            Highlighter txtPaneHighlighter = linkPane.getHighlighter();
//            int[] achorSCRPos = new int[]{Integer.valueOf(anchorSEPosSA[0]), Integer.valueOf(anchorSEPosSA[1])};
//            Object aHighlightRef = txtPaneHighlighter.addHighlight(achorSCRPos[0], achorSCRPos[1], painters.getAnchorPainter());
//        } catch (BadLocationException ex) {
//            Logger.getLogger(topicPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

//    public static void updateTopicAnchorsHighlight(JTextPane topicPane, AssessedAnchor preAnchorSEStatus, AssessedAnchor currAnchorSE/*String[] currAnchorSE*/, int currLinkStatus) {
//        // This might need to handle: isAssessment
//        // 1) YES: Highlight Curr Selected Anchor Text, keep others remaining as THEIR Colors
//        // 2) NO: Highlight Curr Selected Anchor Text, keep others remaining as Anchor Text Color
//        try {
//            boolean preDONEFlag = false;
//            boolean currDONEFlag = false;
//            Highlighter txtPaneHighlighter = topicPane.getHighlighter();
//            Highlight[] highlights = txtPaneHighlighter.getHighlights();
//            Object anchorHighlightRef = null;
//            int[] achorSCRPos = new int[]{currAnchorSE.getScreenPosStart(), currAnchorSE.getScreenPosEnd()/*Integer.valueOf(currAnchorSE[1]), Integer.valueOf(currAnchorSE[2])*/};
//            log("PRE: " + preAnchorSEStatus.screenPosStartToString()/*[0]*/ + " - " + preAnchorSEStatus.screenPosEndToString()/*[1]*/ + " - " + preAnchorSEStatus.getName()/*[2]*/);
//            log("CURR: " + currAnchorSE.screenPosStartToString() + " - " + currAnchorSE.screenPosEndToString());
//            
//            int preScreenS = preAnchorSEStatus.getScreenPosStart(); //Integer.valueOf(preAnchorSEStatus[0]);
//            int preScreenE = preAnchorSEStatus.getScreenPosEnd(); //Integer.valueOf(preAnchorSEStatus[1]);
////            int extLength = Integer.parseInt(preAnchorSEStatus[3]);
////            int sp, se;
//            for (int i = 0; i < highlights.length; i++) {
//                int sPos = highlights[i].getStartOffset();
//                int ePos = highlights[i].getEndOffset();
//                if (sPos >= achorSCRPos[0] &&  ePos <= achorSCRPos[1]) {
//                    txtPaneHighlighter.removeHighlight(highlights[i]);
//                    // String[]{Name, SP, EP, extLength, subSP, subEP}
////                    currDONEFlag = true;
////                    if (currDONEFlag && preDONEFlag) {
////                        break;
////                    }
//                } else {
//                    if (sPos >= preScreenS && ePos <= preScreenE) {
//                        txtPaneHighlighter.removeHighlight(highlights[i]);
//
////                        topicPane.repaint();
////                        preDONEFlag = true;
////                        if (currDONEFlag && preDONEFlag) {
////                            break;
////                        }
//                    }
////                    else if (Integer.valueOf(preAnchorSEStatus[1])) {
////                    	txtPaneHighlighter.removeHighlight(highlights[i]);
////                    }
//                }
//            }
//
//            int pAnchorStatus = preAnchorSEStatus.getStatus(); //preAnchorSEStatus[2];
//            if (pAnchorStatus == 1) {
//                anchorHighlightRef = txtPaneHighlighter.addHighlight(preScreenS, preScreenE, painters.getCompletePainter());
//            } else if (pAnchorStatus == -1) {
//                anchorHighlightRef = txtPaneHighlighter.addHighlight(preScreenS, preScreenE, painters.getIrrelevantPainter());
//            } else {
//                anchorHighlightRef = txtPaneHighlighter.addHighlight(preScreenS, preScreenE, painters.getAnchorPainter());
//            }
//            
//            
//            updateSelectedAnchorHightlighter(topicPane, currAnchorSE, currLinkStatus);
//            //anchorHighlightRef = txtPaneHighlighter.addHighlight(sPos, ePos, painters.getSelectedPainter());
//            
//            topicPane.repaint();
//        } catch (BadLocationException ex) {
//            Logger.getLogger(topicPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }



// </editor-fold>
}




