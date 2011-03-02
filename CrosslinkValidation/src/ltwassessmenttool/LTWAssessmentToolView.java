/*
 * LTWAssessmentToolView.java
 */
package ltwassessmenttool;

import java.awt.Font;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;

import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import javax.swing.text.Highlighter;
// import Classes
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import ltwassessment.AppResource;
import ltwassessment.font.AdjustFont;
import ltwassessment.parsers.FOLTXTMatcher;
import ltwassessment.parsers.Xml2Html;
import ltwassessment.parsers.assessmentFormXml;
import ltwassessment.parsers.PoolerManager;
import ltwassessment.parsers.resourcesManager;
import ltwassessment.utility.AttributiveCellRenderer;
import ltwassessment.utility.InteractiveRenderer;
import ltwassessment.utility.ObservableSingleton;
import ltwassessment.utility.TABInteractiveTableModel;
import ltwassessment.utility.TBAInteractiveTableModel;
import ltwassessment.utility.fieldUpdateObserver;
import ltwassessment.utility.highlightPainters;
import ltwassessment.utility.paneTableIndexing;
import ltwassessment.utility.paneTableManager;
import ltwassessment.utility.tabTxtPaneManager;
import ltwassessment.utility.tbaTxtPaneManager;
import ltwassessment.validation.Validator;
import ltwassessmenttool.listener.CaretListenerLabel;
import ltwassessmenttool.listener.linkPaneMouseListener;
import ltwassessmenttool.listener.paneTableMouseListener;
import ltwassessmenttool.listener.topicPaneMouseListener;

/**
 * The main frame for the Assessment Tool
 */
public class LTWAssessmentToolView extends FrameView {

    // -------------------------------------------------------------------------
    // constant variables
    private final String sysPropertyIsTABKey = "isTABKey";
    private final String sysPropertyIsTopicWikiKey = "isTopicWikipedia";
    private final String sysPropertyIsLinkWikiKey = "isLinkWikipedia";
    private ResourceMap resourceMap;
    private String textContentType = "";
    private String wikipediaTopicFileDir = "";
    private String bepIconImageFilePath = "";
    private String topicAnchorsHTPrefix = "";
    private String topicBepsHTPrefix = "";
    private String xmlSchemaFile = "";
    // -------------------------------------------------------------------------
    // Variables
    private boolean isTopicWikipedia = false;
    private boolean isLinkWikipedia = false;
    private String currTopicFilePath = "";
    private String currTopicID = "";
    // -------------------------------------------------------------------------
    // For Outgoing: [0]:Anchor Name, [1]:Offset, [2]:Length
    private Vector<String> topicFileIDsV = new Vector<String>();
    private Vector<String[]> currAnchorXMLOLPairs = new Vector<String[]>();
    private Vector<String[]> currAnchorSCROLPairs = new Vector<String[]>();
    private Vector<String[]> currBepXMLOffset = new Vector<String[]>();
    private Vector<String[]> currBepSCROffset = new Vector<String[]>();
    // -------------------------------------------------------------------------
    // For Incoming: [0]:Offset
    // -------------------------------------------------------------------------
    // Declare External Classes
    private PoolerManager myPooler;
    private FOLTXTMatcher folMatcher;
    private paneTableManager myPaneTableManager;
    private paneTableIndexing paneTableIndexing;
    private resourcesManager rscManager;
    private tabTxtPaneManager myTABTxtPaneManager;
    private tbaTxtPaneManager myTBATxtPaneManager;
    private highlightPainters painters;
    // -------------------------------------------------------------------------
    //1) including participant-id, run-id, task, collection
    //2) record Topic -> [0]:File ID & [1]:Name
    //3) record Topic (outgoing : topicFile) -> [0]:Offset & [1]:Length & [2]:Anchor_Name
    //4) record Topic (incoming : topicFile) -> [0]:Offset
    private String[] afProperty = new String[4];
    private Vector<String[]> RunTopics = new Vector<String[]>();
    private Hashtable<String, Vector<String[]>> topicAnchorsHT = new Hashtable<String, Vector<String[]>>();
    private Hashtable<String, Vector<String[]>> topicBepsHT = new Hashtable<String, Vector<String[]>>();
    private Hashtable<String, Hashtable<String, Hashtable<String, Vector<String[]>>>> poolOutgoingData = new Hashtable<String, Hashtable<String, Hashtable<String, Vector<String[]>>>>();
    private Hashtable<String, Hashtable<String, Vector<String[]>>> poolIncomingData = new Hashtable<String, Hashtable<String, Vector<String[]>>>();
    // -------------------------------------------------------------------------

    public static boolean forValidationOrAssessment = false; // false validation; true assessment
    
    private String currentTopicXmlText = null;

    static void log(Object content) {
        System.out.println(content);
    }

    static void errlog(Object content) {
        System.err.println("errlog: " + content);
    }
    public static final String[] outgoingTABColumnNames = {"Topic", "Anchor (offset_length: name)", "Target Title (fileid: title)", "BEP", "HiddenField"};
    public static final String[] incomingTBAColumnNames = {"Topic", "BEP", "Anchor", "File ID", "HiddenField"};
    protected TABInteractiveTableModel tabTableModel;
    protected TBAInteractiveTableModel tbaTableModel;
    ButtonGroup group = new ButtonGroup();

    public LTWAssessmentToolView(SingleFrameApplication app) {
        super(app);
        
        // update resource manager first thing with the app starting up
        AppResource.forValidationOrAssessment = false;
        AppResource.getInstance().setResourceMap(org.jdesktop.application.Application.getInstance(ltwassessmenttool.LTWAssessmentToolApp.class).getContext().getResourceMap(ltwassessmenttool.LTWAssessmentToolView.class));

        initComponents();

        group.add(outRadioBtn);
        group.add(inRadioBtn);

        buttonGroup1.add(jRadioButtonMenuItemZh);
        buttonGroup1.add(jRadioButtonMenuItemJa);
        buttonGroup1.add(jRadioButtonMenuItemKo);

        ObservableSingleton os = ObservableSingleton.getInstance();
        fieldUpdateObserver fuObserver = new fieldUpdateObserver(os, this.lblTopicTitle, this.lblTopicID, this.lblPoolAnchor, this.lblTargetPage);
        os.addObserver(fuObserver);

        resourceMap = getResourceMap();
        //AppResource.getInstance().setResourceMap(resourceMap);
        
        wikipediaTopicFileDir = resourceMap.getString("wikipedia.topics.folder") + File.separator;
        textContentType = resourceMap.getString("html.content.type");
        bepIconImageFilePath = resourceMap.getString("bepIcon.imageFilePath");
        topicAnchorsHTPrefix = resourceMap.getString("topicAnchorsHT.Prefix");
        topicBepsHTPrefix = resourceMap.getString("topicBepsHT.Prefix");
        xmlSchemaFile = resourceMap.getString("pooling.runXmlSchema");
        // =====================================================================
//        progressBar.setVisible(false);
        // =====================================================================
        rscManager = resourcesManager.getInstance();
        // =====================================================================
        // 0) check IF Pool XML File in
        String wikiCollectionFolder = rscManager.getWikipediaCollectionFolder();
//        String tearaCollectionFolder = rscManager.getTeAraCollectionFolder();
        if (wikiCollectionFolder.equals("") /*|| tearaCollectionFolder.equals("")*/) {
            String msgTxt = "Please specify the directories of Wikipedia collections." +
                    "\r\n Then split a submission and load a single run file to validate your work.";
            JOptionPane.showMessageDialog(mainPanel, msgTxt);
        } else {
//            String msgTxt = "The directories of Wikipedia collections:\r\n" +
//                    "Wikipedia Collection: " + wikiCollectionFolder + "\r\n" +
//                   // "TeAra Collection: " + tearaCollectionFolder + "\r\n" +
//                    "Please re-specify the directories if collections have been moved.";
//            JOptionPane.showMessageDialog(mainPanel, msgTxt);
//        	jLabelCollection.setText(wikiCollectionFolder + "   " +
//                  "Please re-specify the directories if collections have been moved.");
        	jLabelCollection.setText(wikiCollectionFolder);
        }
        // =====================================================================

//        anchorBepTable.addMouseListener(new paneTableMouseListener(this.topicTextPane, this.linkTextPane, this.anchorBepTable));


        // when the tool firstly starts
        System.setProperty(sysPropertyIsTABKey, "true");
    }

    private boolean corpusDirChecker(boolean topicIsWikipedia) {
        boolean rightCorpusDir = false;
        if (rscManager.getWikipediaCollectionFolder().trim().equals("") /*|| rscManager.getTeAraCollectionFolder().trim().equals("") */|| rscManager.getWikipediaCollectionFolder() == null/* || rscManager.getTeAraCollectionFolder() == null*/) {
            rightCorpusDir = false;
            JOptionPane.showMessageDialog(mainPanel, "The Collection Directory is Empty!");
        } else {
            Random numGenerator = new Random();
            Vector<String> topicIDsV = rscManager.getTopicIDsV();
            int myTopicNum = numGenerator.nextInt(topicIDsV.size());
            String ranTopicID = topicIDsV.elementAt(myTopicNum);
            String runFilePath = "";
            //String collectionFolder = "";
//            if (topicIsWikipedia) {
                //collectionFolder = rscManager.getWikipediaCollectionFolder();
//            String topicFolder = AppResource.getTopicDirectoryh();
//                runFilePath = rscManager.getWikipediaCollectionFolder() + rscManager.getWikipediaFilePathByName(ranTopicID + ".xml");
              runFilePath =  AppResource.getTopicXmlPathNameByFileID(ranTopicID);
//            } else {
//                collectionFolder = rscManager.getTeAraCollectionFolder();
//                runFilePath = rscManager.getTeAraCollectionFolder() + rscManager.getTeAraFilePathByName(ranTopicID + ".xml");
//            }
            File ranXmlFile = new File(runFilePath);
            if (ranXmlFile.exists()) {
                rightCorpusDir = true;
            } else {
                rightCorpusDir = false;
                JOptionPane.showMessageDialog(mainPanel, "Cannot find topic  " + runFilePath + "!");
            }
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
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        topicTextScrollPane = new javax.swing.JScrollPane();
        topicTextPane = new javax.swing.JTextPane();
        rightSplitPane = new javax.swing.JSplitPane();
        linkTextScrollPane = new javax.swing.JScrollPane();
        linkTextPane = new javax.swing.JTextPane();
        anchorBepTablePane = new javax.swing.JScrollPane();
        anchorBepTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jLabelTopicTitle = new javax.swing.JLabel();
        lblTargetPage2 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lblTopicTitle = new javax.swing.JLabel();
        jLabelTargetPage = new javax.swing.JLabel();
        lblTargetPage = new javax.swing.JLabel();
        lblTopicID = new javax.swing.JLabel();
        jLabelAnchor = new javax.swing.JLabel();
        lblPoolAnchor = new javax.swing.JLabel();
        lblTargetPage1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabelCollection = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        utilMenu = new javax.swing.JMenu();
        corpusMenuItem = new javax.swing.JMenuItem();
        loadMenuItem = new javax.swing.JMenuItem();
        splitMenuItem = new javax.swing.JMenuItem();
        linkMenu = new javax.swing.JMenu();
        outRadioBtn = new javax.swing.JRadioButtonMenuItem();
        inRadioBtn = new javax.swing.JRadioButtonMenuItem();
        jMenuLang = new javax.swing.JMenu();
        jRadioButtonMenuItemZh = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItemJa = new javax.swing.JRadioButtonMenuItem();
        jRadioButtonMenuItemKo = new javax.swing.JRadioButtonMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        buttonGroup1 = new javax.swing.ButtonGroup();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ltwassessmenttool.LTWAssessmentToolApp.class).getContext().getResourceMap(LTWAssessmentToolView.class);
        mainPanel.setBackground(resourceMap.getColor("mainPanel.background")); // NOI18N
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setPreferredSize(new java.awt.Dimension(1032, 780));

        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setName("jSplitPane2"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        jSplitPane1.setBackground(resourceMap.getColor("jSplitPane1.background")); // NOI18N
        jSplitPane1.setBorder(null);
        jSplitPane1.setMinimumSize(new java.awt.Dimension(254, 350));
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        topicTextScrollPane.setBackground(resourceMap.getColor("topicTextScrollPane.background")); // NOI18N
        topicTextScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        topicTextScrollPane.setMaximumSize(new java.awt.Dimension(1600, 1600));
        topicTextScrollPane.setName("topicTextScrollPane"); // NOI18N
        topicTextScrollPane.setPreferredSize(new java.awt.Dimension(500, 644));

        topicTextPane.setBackground(resourceMap.getColor("topicTextPane.background")); // NOI18N
        topicTextPane.setEditable(false);
        topicTextPane.setDragEnabled(true);
        topicTextPane.setMaximumSize(new java.awt.Dimension(1600, 1600));
        topicTextPane.setMinimumSize(new java.awt.Dimension(256, 430));
        topicTextPane.setName("topicTextPane"); // NOI18N
        topicTextPane.setPreferredSize(new java.awt.Dimension(500, 644));
        topicTextScrollPane.setViewportView(topicTextPane);

        jSplitPane1.setLeftComponent(topicTextScrollPane);

        rightSplitPane.setBackground(resourceMap.getColor("rightSplitPane.background")); // NOI18N
        rightSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        rightSplitPane.setName("rightSplitPane"); // NOI18N
        rightSplitPane.setOneTouchExpandable(true);

        linkTextScrollPane.setMinimumSize(new java.awt.Dimension(20, 20));
        linkTextScrollPane.setName("linkTextScrollPane"); // NOI18N
        linkTextScrollPane.setPreferredSize(new java.awt.Dimension(525, 400));

        linkTextPane.setBackground(resourceMap.getColor("linkTextPane.background")); // NOI18N
        linkTextPane.setEditable(false);
        linkTextPane.setFont(resourceMap.getFont("linkTextPane.font")); // NOI18N
        linkTextPane.setMinimumSize(new java.awt.Dimension(125, 244));
        linkTextPane.setName("linkTextPane"); // NOI18N
        linkTextPane.setPreferredSize(new java.awt.Dimension(525, 444));
        linkTextScrollPane.setViewportView(linkTextPane);

        rightSplitPane.setTopComponent(linkTextScrollPane);

        anchorBepTablePane.setName("anchorBepTablePane"); // NOI18N

        anchorBepTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        anchorBepTable.setMaximumSize(new java.awt.Dimension(2147, 720));
        anchorBepTable.setMinimumSize(new java.awt.Dimension(10, 32));
        anchorBepTable.setName("anchorBepTable"); // NOI18N
        anchorBepTable.setPreferredSize(new java.awt.Dimension(10, 72));
        anchorBepTablePane.setViewportView(anchorBepTable);

        rightSplitPane.setRightComponent(anchorBepTablePane);

        jSplitPane1.setRightComponent(rightSplitPane);

        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setPreferredSize(new java.awt.Dimension(1031, 59));

        jLabelTopicTitle.setFont(resourceMap.getFont("jLabelTopicTitle.font")); // NOI18N
        jLabelTopicTitle.setText(resourceMap.getString("jLabelTopicTitle.text")); // NOI18N
        jLabelTopicTitle.setName("jLabelTopicTitle"); // NOI18N

        lblTargetPage2.setAlignmentX(0.5F);
        lblTargetPage2.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        lblTargetPage2.setMaximumSize(new java.awt.Dimension(255555555, 25555555));
        lblTargetPage2.setMinimumSize(new java.awt.Dimension(255, 184));
        lblTargetPage2.setName("lblTargetPage2"); // NOI18N
        lblTargetPage2.setPreferredSize(new java.awt.Dimension(255, 184));

        jLabel2.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        lblTopicTitle.setText(resourceMap.getString("lblTopicTitle.text")); // NOI18N
        lblTopicTitle.setMaximumSize(new java.awt.Dimension(50, 14));
        lblTopicTitle.setMinimumSize(new java.awt.Dimension(50, 14));
        lblTopicTitle.setName("lblTopicTitle"); // NOI18N
        lblTopicTitle.setPreferredSize(new java.awt.Dimension(50, 14));

        jLabelTargetPage.setFont(resourceMap.getFont("jLabelTargetPage.font")); // NOI18N
        jLabelTargetPage.setText(resourceMap.getString("jLabelTargetPage.text")); // NOI18N
        jLabelTargetPage.setName("jLabelTargetPage"); // NOI18N

        lblTargetPage.setText(resourceMap.getString("lblTargetPage.text")); // NOI18N
        lblTargetPage.setName("lblTargetPage"); // NOI18N

        lblTopicID.setText(resourceMap.getString("lblTopicID.text")); // NOI18N
        lblTopicID.setMaximumSize(new java.awt.Dimension(30, 14));
        lblTopicID.setMinimumSize(new java.awt.Dimension(30, 14));
        lblTopicID.setName("lblTopicID"); // NOI18N
        lblTopicID.setPreferredSize(new java.awt.Dimension(30, 14));

        jLabelAnchor.setFont(resourceMap.getFont("jLabelAnchor.font")); // NOI18N
        jLabelAnchor.setText(resourceMap.getString("jLabelAnchor.text")); // NOI18N
        jLabelAnchor.setName("jLabelAnchor"); // NOI18N

        lblPoolAnchor.setText(resourceMap.getString("lblPoolAnchor.text")); // NOI18N
        lblPoolAnchor.setName("lblPoolAnchor"); // NOI18N

        lblTargetPage1.setName("lblTargetPage1"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabelAnchor, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPoolAnchor, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelTargetPage, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(lblTargetPage2, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelTopicTitle)
                .addGap(18, 18, 18)
                .addComponent(lblTopicTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTopicID, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(76, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(2156, 2156, 2156)
                    .addComponent(lblTargetPage1, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblTargetPage, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTopicID, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                    .addComponent(lblTopicTitle, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                    .addComponent(jLabelTopicTitle, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                    .addComponent(jLabelTargetPage, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                    .addComponent(lblTargetPage2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 36, Short.MAX_VALUE)
                    .addComponent(lblPoolAnchor, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                    .addComponent(jLabelAnchor, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(lblTargetPage, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
                        .addComponent(lblTargetPage1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE))
                    .addContainerGap(13, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 1385, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1397, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(354, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                    .addGap(64, 64, 64)
                    .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)))
        );

        jSplitPane2.setLeftComponent(jPanel1);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextPane1.setBackground(resourceMap.getColor("jTextPane1.background")); // NOI18N
        jTextPane1.setName("jTextPane1"); // NOI18N
        jScrollPane1.setViewportView(jTextPane1);

        jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel4.setName("jPanel4"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabelCollection.setText(resourceMap.getString("jLabelCollection.text")); // NOI18N
        jLabelCollection.setName("jLabelCollection"); // NOI18N

        jLabel3.setFont(resourceMap.getFont("jLabel3.font")); // NOI18N
        jLabel3.setForeground(resourceMap.getColor("jLabel3.foreground")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 434, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelCollection, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabelCollection, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel3))
        );

        jPanel3.add(jPanel4, java.awt.BorderLayout.PAGE_START);

        jSplitPane2.setRightComponent(jPanel3);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1397, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
                .addContainerGap())
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

        corpusMenuItem.setAction(actionMap.get("showCorpusBox")); // NOI18N
        corpusMenuItem.setText(resourceMap.getString("corpusMenuItem.text")); // NOI18N
        corpusMenuItem.setName("corpusMenuItem"); // NOI18N
        utilMenu.add(corpusMenuItem);

        loadMenuItem.setAction(actionMap.get("loadSubmissionXML")); // NOI18N
        loadMenuItem.setText(resourceMap.getString("loadMenuItem.text")); // NOI18N
        loadMenuItem.setName("loadMenuItem"); // NOI18N
        utilMenu.add(loadMenuItem);

        splitMenuItem.setAction(actionMap.get("splitSubmissionIntoTopics")); // NOI18N
        splitMenuItem.setText(resourceMap.getString("splitMenuItem.text")); // NOI18N
        splitMenuItem.setName("splitMenuItem"); // NOI18N
        utilMenu.add(splitMenuItem);

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
        inRadioBtn.setEnabled(false);
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

        jRadioButtonMenuItemZh.setSelected(true);
        jRadioButtonMenuItemZh.setText(resourceMap.getString("jRadioButtonMenuItemZh.text")); // NOI18N
        jRadioButtonMenuItemZh.setName("jRadioButtonMenuItemZh"); // NOI18N
        jRadioButtonMenuItemZh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItemZhActionPerformed(evt);
            }
        });
        jMenuLang.add(jRadioButtonMenuItemZh);

        jRadioButtonMenuItemJa.setSelected(true);
        jRadioButtonMenuItemJa.setText(resourceMap.getString("jRadioButtonMenuItemJa.text")); // NOI18N
        jRadioButtonMenuItemJa.setName("jRadioButtonMenuItemJa"); // NOI18N
        jRadioButtonMenuItemJa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMenuItemJaActionPerformed(evt);
            }
        });
        jMenuLang.add(jRadioButtonMenuItemJa);

        jRadioButtonMenuItemKo.setSelected(true);
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

        setComponent(mainPanel);
        setMenuBar(menuBar);
    }// </editor-fold>//GEN-END:initComponents

    // <editor-fold defaultstate="collapsed" desc="Menu Bar Event Action">
    String currentOpenDir = "C:\\";

    private String splitSubmissionByTopics(String runFileAbsPath) {
        String isSplitOK = "Error Msg: ";
        StringBuffer sb = new StringBuffer();
        FileInputStream fstream = null;
        try {
            // 0) get the ROOT directory and create a sub folder 
            //    underneath the ROOT to store sub-division topic runs
            String submissionFileName = "";
            String subDirectory = "";
            String rootDirectory = "";
            if (runFileAbsPath.lastIndexOf(File.separator) >= 0) {
                submissionFileName = runFileAbsPath.substring(runFileAbsPath.lastIndexOf(File.separator) + 1, runFileAbsPath.toLowerCase().lastIndexOf(".xml"));
                rootDirectory = runFileAbsPath.substring(0, runFileAbsPath.lastIndexOf(File.separator) + 1);
                subDirectory = rootDirectory + submissionFileName + File.separator;
            } else if (runFileAbsPath.lastIndexOf(File.separator) >= 0) {
                submissionFileName = runFileAbsPath.substring(runFileAbsPath.lastIndexOf(File.separator) + 1, runFileAbsPath.toLowerCase().lastIndexOf(".xml"));
                rootDirectory = runFileAbsPath.substring(0, runFileAbsPath.lastIndexOf(File.separator) + 1);
                subDirectory = rootDirectory + submissionFileName + File.separator;
            }
            
            File thisSubFolder = new File(subDirectory);
            boolean subDirOK = false;
            if (!thisSubFolder.exists()){
                if (thisSubFolder.mkdir()){
                    subDirOK = true;
                } else {
                    subDirOK = false;
                }
            } else {
                subDirOK = true;
            }
            if (subDirOK) {
                // -----------------------------------------------------------------
                fstream = new FileInputStream(runFileAbsPath);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine;
                while ((strLine = br.readLine()) != null) {
                    sb.append(strLine);
                }
                // -----------------------------------------------------------------
                String runProperty = "";
                String endTag = "</crosslink-submission>";
                Vector<String> topicBodyV = new Vector<String>();
                Vector<String> topicFileIDV = new Vector<String>();
                String thisRunContent = sb.toString();
                // 1) get run Property
                String root = "<crosslink-submission";
                runProperty = thisRunContent.substring(thisRunContent.indexOf(root), thisRunContent.indexOf("</collections>") + 14);
                boolean loopFlag = true;
                int fromCounter = 0;
                while (loopFlag) {
                    int startPoint = thisRunContent.indexOf("<topic", fromCounter);
                    int endPoint = thisRunContent.indexOf("</topic>", startPoint);
                    int fileIDSP = thisRunContent.indexOf("file=", startPoint + 6);
                    int fileIDEP = thisRunContent.indexOf("\"", fileIDSP + 6);
                    String topicFileIDS = thisRunContent.substring(fileIDSP + 6, fileIDEP);
                    topicFileIDV.add(topicFileIDS);
                    String topicBodyS = thisRunContent.substring(startPoint, endPoint + 8);
                    topicBodyV.add(topicBodyS);
                    fromCounter = endPoint;
                    int checkPoint = thisRunContent.indexOf("<topic", fromCounter);
                    if (checkPoint < 0) {
                        loopFlag = false;
                    }
                }
                // -----------------------------------------------------------------
                if (topicBodyV.size() == topicFileIDV.size()) {
                    for (int i = 0; i < topicBodyV.size(); i++) {
                        String thisTopic = topicBodyV.elementAt(i);
                        String thisTopicID = topicFileIDV.elementAt(i);
                        StringBuffer topicSB = new StringBuffer();
                        topicSB.append(runProperty);
                        topicSB.append(thisTopic);
                        topicSB.append(endTag);
                        String thisTopicRun = subDirectory + thisTopicID + ".xml";
                        FileWriter fw = new FileWriter(thisTopicRun);
                        BufferedWriter out = new BufferedWriter(fw);
                        out.write(topicSB.toString());
                        out.close();
                    }
                } else {
                    return isSplitOK + "Topics might be dupicated! Please check...";
                }
                // -----------------------------------------------------------------
                fstream.close();
                in.close();
                return subDirectory;
            } else {
                return isSplitOK + "Create Folder False: " + subDirectory + ".\r\n Check Directory status!";
            }

        } catch (IOException ex) {
            return isSplitOK + ex.getMessage();
        }
    }

    @Action
    public void splitSubmissionIntoTopics() {
        // The user can only load one submission at a time to validate it.
        JFileChooser fc = new JFileChooser(currentOpenDir);
        // Load only 1 XML file
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setMultiSelectionEnabled(false);
        int returnVal = fc.showOpenDialog(this.mainPanel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File thisXMLFile = fc.getSelectedFile();
            String absFilePath = thisXMLFile.getAbsolutePath();
            File[] xmlFiles = new File[]{thisXMLFile};
            List<File> tmp = Arrays.asList(xmlFiles);
            ArrayList<File> fileList = new ArrayList<File>(tmp);
            if (absFilePath.lastIndexOf(File.separator) >= 0) {
                currentOpenDir = absFilePath.substring(0, absFilePath.lastIndexOf(File.separator));
            } else if (absFilePath.lastIndexOf(File.separator) >= 0) {
                currentOpenDir = absFilePath.substring(0, absFilePath.lastIndexOf(File.separator));
            }
            if (thisXMLFile.isFile() && absFilePath.toLowerCase().endsWith(".xml")) {
                try {
                    // 1) validate the XML file: well-form only
                    // TODO: update to against schema or DTD
                    // 2) convert submission XML into Pool XML for validation
                    // TODO: We kept this form just because it already have been done.
                    File schemaFile = new File(xmlSchemaFile);
                    Validator validator = Validator.getInstance(schemaFile.toURI().toURL());
                    String msgFromValidation = validator.validateSubmissionXML(fileList);
                    if (msgFromValidation.trim().startsWith("Err Msg:")) {
                        // Errors: well-form or xml data
                        JOptionPane.showMessageDialog(LTWAssessmentToolApp.getApplication().getMainFrame(), msgFromValidation);
                    } else {
                        // split the submission run into different XML files by each Topic
                        String backMsg = splitSubmissionByTopics(absFilePath);
                        if (backMsg.startsWith("Error Msg:")) {
                            JOptionPane.showMessageDialog(LTWAssessmentToolApp.getApplication().getMainFrame(), backMsg);
                        } else {
                            String subRunDirectory = "Submission has been splited into the directory, \r\n" + backMsg + "\r\n Please load these subdivided runs one by one using the tool.";
                            JOptionPane.showMessageDialog(LTWAssessmentToolApp.getApplication().getMainFrame(), subRunDirectory);
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(LTWAssessmentToolView.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                String notXMLMsg = "Error Msg: The submission run must be an XML file.\r\n" +
                        "The file selected, " + thisXMLFile.getName() + "is invalid.";
                JOptionPane.showMessageDialog(this.mainPanel, notXMLMsg);
            }
        }
    }

    @Action
    public void loadSubmissionXML() {
        // The user can only load one submission at a time to validate it.
        JFileChooser fc = new JFileChooser(currentOpenDir);
        // Load only 1 XML file
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setMultiSelectionEnabled(false);
        int returnVal = fc.showOpenDialog(this.mainPanel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File thisXMLFile = fc.getSelectedFile();
            String absFilePath = thisXMLFile.getAbsolutePath();
            File[] xmlFiles = new File[]{thisXMLFile};
            List<File> tmp = Arrays.asList(xmlFiles);
            ArrayList<File> fileList = new ArrayList<File>(tmp);
            if (absFilePath.lastIndexOf(File.separator) >= 0) {
                currentOpenDir = absFilePath.substring(0, absFilePath.lastIndexOf(File.separator));
            } else if (absFilePath.lastIndexOf(File.separator) >= 0) {
                currentOpenDir = absFilePath.substring(0, absFilePath.lastIndexOf(File.separator));
            }
            if (thisXMLFile.isFile() && absFilePath.toLowerCase().endsWith(".xml")) {
                try {
                    // 1) validate the XML file: well-form only
                    // TODO: update to against schema or DTD
                    // 2) convert submission XML into Pool XML for validation
                    // TODO: We kept this form just because it already have been done.
                	
                    File schemaFile = new File(xmlSchemaFile);
                    Validator validator = Validator.getInstance(schemaFile.toURI().toURL());
                    String msgFromValidation = validator.validateSubmissionXML(fileList);
                    if (msgFromValidation.startsWith("Err Msg:")) {
                        // Errors: well-form or xml data
                        JOptionPane.showMessageDialog(LTWAssessmentToolApp.getApplication().getMainFrame(), msgFromValidation);
                    } else {
                        // Get returned valid file
                        assessmentFormXml toPooling = new assessmentFormXml(fileList);
                        String poolingMsg = "Submission for topic" + thisXMLFile.getName() + " is successful loaded: \r\n" + toPooling.getPoolingMsg() + "\r\n Please be patient, it may take a few minutes to discovery all the links";
                        JOptionPane.showMessageDialog(LTWAssessmentToolApp.getApplication().getMainFrame(), poolingMsg);
                        // =====================================================
                        updatePoolerToResourceXML(thisXMLFile.getAbsolutePath());
                        
                    	// set UI font
                    	AdjustFont.setComponentFont(anchorBepTable, AppResource.targetLang);
                    	AdjustFont.setComponentFont(linkTextPane, AppResource.targetLang);
                    	
                        myTABTxtPaneManager = new tabTxtPaneManager();
                        myTBATxtPaneManager = new tbaTxtPaneManager();
                        // =====================================================
                        boolean rightCorpusDir = true; //corpusDirChecker(isTopicWikipedia);
                        if (rightCorpusDir) {
                            if (outRadioBtn.isSelected()) {
                                setOutgoingTAB();
                            } else if (inRadioBtn.isSelected()) {
                                setIncomingTBA();
                            }
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(LTWAssessmentToolView.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception e) {
					e.printStackTrace();
				}
            } else {
                String notXMLMsg = "Error Msg: The submission run must be an XML file.\r\n" +
                        "The file selected, " + thisXMLFile.getName() + "is invalid.";
                JOptionPane.showMessageDialog(this.mainPanel, notXMLMsg);
            }
        }
    }

    @Action
    public void showCorpusBox() {
        if (corpusBox == null) {
            JFrame mainFrame = LTWAssessmentToolApp.getApplication().getMainFrame();
            corpusBox = new LTWAssessmentToolCorpusBox(mainFrame);
            corpusBox.setLocationRelativeTo(mainFrame);
            ((LTWAssessmentToolCorpusBox) corpusBox).setJLableCollection(this.jLabelCollection);
        }
        LTWAssessmentToolApp.getApplication().show(corpusBox);
    }

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
        setIncomingTBA();
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

    public void highlightLastRow(int row) {
        int lastRow = tabTableModel.getRowCount();
        if (row == lastRow - 1) {
        	anchorBepTable.setRowSelectionInterval(lastRow - 1, lastRow - 1);
        } else {
            anchorBepTable.setRowSelectionInterval(row + 1, row + 1);
        }
        anchorBepTable.setRowSelectionInterval(0, 0);
    }

    @Action
    public void showAssessmentSetBox() {
        if (assessmentSetBox == null) {
            JFrame mainFrame = LTWAssessmentToolApp.getApplication().getMainFrame();
            assessmentSetBox = new LTWAssessmentSetForm(mainFrame);
            assessmentSetBox.setLocationRelativeTo(mainFrame);
        }
        LTWAssessmentToolApp.getApplication().show(assessmentSetBox);
    }

    private void updatePoolerToResourceXML(String poolFile) {
		try {
	    	if (poolFile == null || poolFile.length() == 0)
				myPooler = PoolerManager.getInstance();
			else {
	    		myPooler = PoolerManager.getInstance(poolFile);
	    		resourcesManager.pooler = myPooler;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
        afProperty = myPooler.getPoolProperty();    // [0]:participant-id, [1]:run-id, [2]:task, [3]:collection
        RunTopics = myPooler.getAllTopicsInPool();  // Vector<String[]>: [0]:File, [1]:Name
        // ---------------------------------------------------------------------
        String myAFTask = afProperty[2].trim();
//        if (myAFTask.equals(resourceMap.getString("task.ltwF2F")) || myAFTask.equals(resourceMap.getString("task.ltwA2B"))) {
            isTopicWikipedia = true;
            isLinkWikipedia = true;
            System.setProperty(sysPropertyIsTopicWikiKey, "true");
            System.setProperty(sysPropertyIsLinkWikiKey, "true");
//        } else if (myAFTask.equals(resourceMap.getString("task.ltaraA2B"))) {
//            isTopicWikipedia = false;
//            isLinkWikipedia = false;
//            System.setProperty(sysPropertyIsTopicWikiKey, "false");
//            System.setProperty(sysPropertyIsLinkWikiKey, "false");
//        } else if (myAFTask.equals(resourceMap.getString("task.ltaratwA2B"))) {
//            isTopicWikipedia = false;
//            isLinkWikipedia = true;
//            System.setProperty(sysPropertyIsTopicWikiKey, "false");
//            System.setProperty(sysPropertyIsLinkWikiKey, "true");
//        }
        // ---------------------------------------------------------------------
        // 1) topic collection type --> link collection type
//        if (isTopicWikipedia) {
            this.rscManager.updateTopicCollType(resourceMap.getString("collectionType.Wikipedia"));
//        } else {
//            this.rscManager.updateTopicCollType(resourceMap.getString("collectionType.TeAra"));
//        }
//        if (isLinkWikipedia) {
            this.rscManager.updateLinkCollType(resourceMap.getString("collectionType.Wikipedia"));
//        } else {
//            this.rscManager.updateLinkCollType(resourceMap.getString("collectionType.TeAra"));
//        }
        /*
         * TODO if it is loading action, we need to clear up the old topic list
         */
        
        // ---------------------------------------------------------------------
        // 2) current Topics List
        topicFileIDsV.removeAllElements();
        for (String[] thisTopicSA : RunTopics) {
            topicFileIDsV.add(thisTopicSA[0].trim());
        }
        Collections.sort(topicFileIDsV);
        this.rscManager.updateTopicList(topicFileIDsV);
        // ---------------------------------------------------------------------
        // Get Topic ID & xmlFile Path --> record them into ToolResource XML
        currTopicID = topicFileIDsV.elementAt(0);
//        if (isTopicWikipedia) {
            // current Topic
//            String wikipediaTopicFileDir = resourceMap.getString("wikipedia.topics.folder");
            //currTopicFilePath = wikipediaTopicFileDir + currTopicID + ".xml";
            currTopicFilePath = "resources" + File.separator + "Topics" + File.separator + currTopicID + ".xml";
//        } else {
//            // need to find out from TeAra Collection Folders
//            currTopicFilePath = this.rscManager.getTeAraCollectionFolder() + this.rscManager.getTeAraFilePathByName(currTopicID + ".xml");
//        }
        this.rscManager.updateCurrTopicID(currTopicFilePath);
        // ---------------------------------------------------------------------
        // make sure the NAV to be ZERO every time when a new Submission is loaded
        String[] navIndices = new String[]{"0", "0", "0", "0", "0"};
        this.rscManager.updateTABNavigationIndex(navIndices);
        this.rscManager.updateTBANavigationIndex(navIndices);
    }

    // <editor-fold defaultstate="collapsed" desc="Pre-Variables-Declaration">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable anchorBepTable;
    private javax.swing.JScrollPane anchorBepTablePane;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JMenuItem corpusMenuItem;
    private javax.swing.JRadioButtonMenuItem inRadioBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelAnchor;
    private javax.swing.JLabel jLabelCollection;
    private javax.swing.JLabel jLabelTargetPage;
    private javax.swing.JLabel jLabelTopicTitle;
    private javax.swing.JMenu jMenuLang;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemJa;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemKo;
    private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItemZh;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JLabel lblPoolAnchor;
    private javax.swing.JLabel lblTargetPage;
    private javax.swing.JLabel lblTargetPage1;
    private javax.swing.JLabel lblTargetPage2;
    private javax.swing.JLabel lblTopicID;
    private javax.swing.JLabel lblTopicTitle;
    private javax.swing.JMenu linkMenu;
    private javax.swing.JTextPane linkTextPane;
    private javax.swing.JScrollPane linkTextScrollPane;
    private javax.swing.JMenuItem loadMenuItem;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JRadioButtonMenuItem outRadioBtn;
    private javax.swing.JSplitPane rightSplitPane;
    private javax.swing.JMenuItem splitMenuItem;
    private javax.swing.JTextPane topicTextPane;
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
    // Incoming Links: T.B.A.
    // =========================================================================
    private void setIncomingTBA() {
        System.setProperty(sysPropertyIsTABKey, "false");
        // Hashtable<incoming : topicFile>: [0]:Offset
        // Hashtable<String, Hashtable<String, Vector<String[]>>>
        topicBepsHT = myPooler.getTopicAllBeps();
        poolIncomingData = myPooler.getIncomingPool();
        // ---------------------------------------------------------------------
        // Get Last Indices
        String[] lastTBAIndices = rscManager.getTBANavigationIndex();
        String lastRowIndex = lastTBAIndices[0];
        String[] lastTBAIndicesSA = lastTBAIndices[1].split(" , ");
        String lastTopicIDIndex = lastTBAIndicesSA[0];
        // Get Topic ID & xmlFile Path --> record them into ToolResource XML
        currTopicID = topicFileIDsV.elementAt(Integer.valueOf(lastTopicIDIndex));
//        if (isTopicWikipedia) {
            // current Topic
            currTopicFilePath = wikipediaTopicFileDir + currTopicID + ".xml";
//        } else {
//            // need to find out from TeAra Collection Folders
//            currTopicFilePath = rscManager.getTeAraCollectionFolder() + rscManager.getTeAraFilePathByName(currTopicID + ".xml");
//        }
        rscManager.updateCurrTopicID(currTopicFilePath);
        // ---------------------------------------------------------------------
        // init Topic Text Pane Content
        setTextPaneContent(currTopicFilePath);
        // Get BEP Screen Position
        // --> Convert FOL to Screen Position --> record INTO toolResource.XML
        currBepXMLOffset = topicBepsHT.get(topicBepsHTPrefix + currTopicID);
        folMatcher = FOLTXTMatcher.getInstance();
        currBepSCROffset = folMatcher.getSCRBepPosV(this.topicTextPane, currTopicID, currBepXMLOffset, isTopicWikipedia);
        // set BEP ICONs
        setTopicBEPIcon(currBepSCROffset);
        // ---------------------------------------------------------------------
        // populate SRC JTable
        tbaTableModel = new TBAInteractiveTableModel(incomingTBAColumnNames);
        anchorBepTable.setModel(tbaTableModel);
        anchorBepTable.repaint();
        anchorBepTable.setSurrendersFocusOnKeystroke(true);
        anchorBepTable.setEnabled(false);
        anchorBepTable.setCellSelectionEnabled(true);
        anchorBepTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        anchorBepTable.addMouseListener(new paneTableMouseListener(this.topicTextPane, this.linkTextPane, this.anchorBepTable));
        // populate TAB Table, including Hidden Field
        boolean isTAB = false;
        paneTableIndexing = paneTableIndexing.getInstance();
        myPaneTableManager = new paneTableManager(paneTableIndexing);
        myPaneTableManager.populateTBATable(tbaTableModel);
        // Hidden Field Column
        TableColumn hiddenTC = anchorBepTable.getColumnModel().getColumn(TBAInteractiveTableModel.HIDDEN_INDEX);
        hiddenTC.setMinWidth(2);
        hiddenTC.setPreferredWidth(2);
        hiddenTC.setMaxWidth(2);
        hiddenTC.setCellRenderer(new InteractiveRenderer(TBAInteractiveTableModel.HIDDEN_INDEX));
        // ---------------------------------------------------------------------
        // initialize Topic Panel "BEP" Listener
        // create Caret the status pane
//        CaretListenerLabel caretListenerLabel = new CaretListenerLabel("Caret Status", this.topicTextPane, this.statusMessageLabel);
//        this.topicTextPane.addCaretListener(caretListenerLabel);
        topicPaneMouseListener mtTopicPaneListener = new topicPaneMouseListener(this.topicTextPane, this.linkTextPane, this.currBepSCROffset, this.anchorBepTable, this.paneTableIndexing);
        this.topicTextPane.addMouseListener(mtTopicPaneListener);
        this.topicTextPane.addMouseMotionListener(mtTopicPaneListener);
        // ---------------------------------------------------------------------
        this.topicTextPane.getCaret().setDot(Integer.valueOf(currBepSCROffset.elementAt(0)[0]));
        this.topicTextPane.scrollRectToVisible(this.topicTextPane.getVisibleRect());
        this.topicTextPane.repaint();
        // ---------------------------------------------------------------------
        // set up linkPaneMouseListener();
        linkPaneMouseListener myLPMListener = new linkPaneMouseListener(this.topicTextPane, this.linkTextPane, this.anchorBepTable);
        this.linkTextPane.addMouseListener(myLPMListener);
        // ---------------------------------------------------------------------
        // According to TAB-Navigation-Indices
        // TODO: populate the "Last" BEP Link & Highlight Table Row
        String[] thisRowValuesSA = paneTableIndexing.getTableValuesByRowIndexWOText().elementAt(Integer.valueOf(lastRowIndex));
        int updateLevel = 2;
        if (isTAB) {
            this.myTABTxtPaneManager.updateTXTPanes(topicTextPane, linkTextPane, anchorBepTable, thisRowValuesSA, updateLevel);
        } else {
            this.myTBATxtPaneManager.updateTXTPanes(topicTextPane, linkTextPane, anchorBepTable, thisRowValuesSA, updateLevel);
        }
        // ---------------------------------------------------------------------
        anchorBepTable.setDefaultRenderer(Object.class, new AttributiveCellRenderer(Integer.valueOf(lastRowIndex), 3, isTAB));
        anchorBepTable.repaint();
    }
    // =========================================================================
    // Outgoing Links: T.A.B.
    // =========================================================================
    // tagert, anchor and bep?

    private void setOutgoingTAB() {
        System.setProperty(sysPropertyIsTABKey, "true");
        // ---------------------------------------------------------------------
        // Get Pool-AF Properties
        // Hashtable<outgoing : topicFileID>: [0]:Offset, [1]:Length, [2]:Anchor_Name
        // Topic --> outgoing --> anchor --> subanchor --> BEPs
        topicAnchorsHT = myPooler.getTopicAllAnchors();
        poolOutgoingData = myPooler.getOutgoingPool();
        // ---------------------------------------------------------------------
        // Get Last Indices
        String[] lastTABIndices = rscManager.getTABNavigationIndex();
        String lastRowIndex = lastTABIndices[0] == null ? "0" : lastTABIndices[0];
        String[] lastTABIndicesSA = lastTABIndices[1].split(" , ");
        String lastTopicIDIndex = lastTABIndicesSA[0];
        // ---------------------------------------------------------------------
        // Get Topic ID & xmlFile Path --> record them into ToolResource XML
        currTopicID = topicFileIDsV.elementAt(Integer.valueOf(lastTopicIDIndex));
//        if (isTopicWikipedia) {
            // current Topic
            currTopicFilePath = wikipediaTopicFileDir + AppResource.sourceLang + File.separator +  currTopicID + ".xml";
//        } else {
//            // need to find out from TeAra Collection Folders
//            currTopicFilePath = rscManager.getTeAraCollectionFolder() + rscManager.getTeAraFilePathByName(currTopicID + ".xml");
//        }
        rscManager.updateCurrTopicID(currTopicFilePath);
        // ---------------------------------------------------------------------
        // SET Topic Text Pane Content
        setTextPaneContent(currTopicFilePath);
        // Get Anchor Position from Pool XML
        // --> Convert FOL to Screen Position --> record INTO toolResource.XML
        currAnchorXMLOLPairs = topicAnchorsHT.get(topicAnchorsHTPrefix + currTopicID);
        folMatcher = FOLTXTMatcher.getInstance();
        currAnchorSCROLPairs = folMatcher.getSCRAnchorPosV(this.topicTextPane, currTopicID, topicAnchorsHT);
        // create the status pane
//        CaretListenerLabel caretListenerLabel = new CaretListenerLabel("Caret Status", this.topicTextPane, this.statusMessageLabel);
        // initialize HighlightPainter
        painters = new highlightPainters();
        setTopicTextHighlighter(currAnchorSCROLPairs);
        // ---------------------------------------------------------------------
        // Clean JTable
        anchorBepTable.removeAll();
        // populate SRC JTable
        tabTableModel = new TABInteractiveTableModel(outgoingTABColumnNames);
        anchorBepTable.setModel(tabTableModel);
        anchorBepTable.setSurrendersFocusOnKeystroke(true);
        anchorBepTable.setEnabled(false);
        anchorBepTable.setCellSelectionEnabled(true);
        anchorBepTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        anchorBepTable.addMouseListener(new paneTableMouseListener(this.topicTextPane, this.linkTextPane, this.anchorBepTable));
        // populate TAB Table, including Hidden Field
        boolean isTAB = true;
        paneTableIndexing = paneTableIndexing.getInstance();
        myPaneTableManager = new paneTableManager(paneTableIndexing);
        myPaneTableManager.populateTABTable(tabTableModel);
        // Hidden Field Column
        TableColumn hiddenTC = anchorBepTable.getColumnModel().getColumn(TABInteractiveTableModel.HIDDEN_INDEX);
        hiddenTC.setMinWidth(2);
        hiddenTC.setPreferredWidth(2);
        hiddenTC.setMaxWidth(2);
        hiddenTC.setCellRenderer(new InteractiveRenderer(TABInteractiveTableModel.HIDDEN_INDEX));
        // ---------------------------------------------------------------------
        // initialize Topic and Link Content Panels
//        this.topicTextPane.addCaretListener(caretListenerLabel);
        topicPaneMouseListener mtTopicPaneListener = new topicPaneMouseListener(this.topicTextPane, this.linkTextPane, this.currAnchorSCROLPairs, this.anchorBepTable, this.paneTableIndexing);
        this.topicTextPane.addMouseListener(mtTopicPaneListener);
        this.topicTextPane.addMouseMotionListener(mtTopicPaneListener);
        // ---------------------------------------------------------------------
        // set up linkPaneMouseListener()
        linkPaneMouseListener myLPMListener = new linkPaneMouseListener(this.topicTextPane, this.linkTextPane, this.anchorBepTable);
        this.linkTextPane.addMouseListener(myLPMListener);
        // ---------------------------------------------------------------------
        // According to TAB-Navigation-Indices
        // TODO: populate the "Last" BEP Link & Highlight Table Row
        Vector<String[]> paneTableRowIndexWOText = paneTableIndexing.getTableValuesByRowIndexWOText();
        if (paneTableRowIndexWOText.size() > 0) {
	        String[] thisRowValuesSA = paneTableRowIndexWOText.elementAt(Integer.valueOf(lastRowIndex));
	        int updateLevel = 2;
	        if (isTAB) {
	            this.myTABTxtPaneManager.updateTXTPanes(this.topicTextPane, this.linkTextPane, this.anchorBepTable, thisRowValuesSA, updateLevel);
	        } else {
	            this.myTBATxtPaneManager.updateTXTPanes(this.topicTextPane, this.linkTextPane, this.anchorBepTable, thisRowValuesSA, updateLevel);
	        }
        }
        // ---------------------------------------------------------------------
        anchorBepTable.setDefaultRenderer(Object.class, new AttributiveCellRenderer(Integer.valueOf(lastRowIndex), 3, isTAB));
        anchorBepTable.repaint();
    }
    // =========================================================================
    // =========================================================================
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Set Text Pane & Highlight Anchor">
    private void setTextPaneContent(String xmlFilePath) {
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
//        try {
        Xml2Html xmlParser = new Xml2Html(xmlFilePath, isTopicWikipedia);
        this.topicTextPane.setContentType(textContentType);
////        this.topicTextPane.setFont(resourceMap.getFont("topicTextPane.font"));
//        this.topicTextPane.setFont(new Font("WenQuanYi Zen Hei", Font.PLAIN, 10)/*resourceMap.getFont("topicTextPane.font")*/);
//        this.topicTextPane.putClientProperty(topicTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        this.topicTextPane.setText(xmlParser.getHtmlContent().toString());
//            java.net.URL htmlFileURL = new File(xmlParser.getHtmlPath().toString()).toURI().toURL();
//            this.topicTextPane.setPage(htmlFileURL);
//        } catch (IOException ex) {
//            Logger.getLogger(LTWAssessmentToolView.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    private void setTopicTextHighlighter(Vector<String[]> anchorLPairs) {
        try {
            // 1) get Anchor Text Offset & Length from the XML file
            Highlighter highlighter = this.topicTextPane.getHighlighter();
            Object anchorHighlightReference;
            for (String[] alPair : anchorLPairs) {
                // Active faults_669_682
                anchorHighlightReference = highlighter.addHighlight(Integer.valueOf(alPair[1]), Integer.valueOf(alPair[2]), painters.getAnchorPainter());
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(LTWAssessmentToolView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setTopicBEPIcon(Vector<String[]> currBepSCRO) {
        // Insert into Doc in JTextPane
        ImageIcon bepIcon = new ImageIcon(bepIconImageFilePath);
        this.topicTextPane.insertIcon(bepIcon);
        this.topicTextPane.repaint();

        StyledDocument styDoc = (StyledDocument) this.topicTextPane.getDocument();
        Style bepStyle = styDoc.addStyle("bepIcon", null);
        StyleConstants.setIcon(bepStyle, new ImageIcon(bepIconImageFilePath));
        try {
            for (String[] bepSCROffsetSA : currBepSCRO) {
                // ??? Does the naming of "TBEP" affect the character count???
                styDoc.insertString(Integer.valueOf(bepSCROffsetSA[0]), "TBEP", bepStyle);
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(topicPaneMouseListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.topicTextPane.repaint();
    }
    // </editor-fold>
}




