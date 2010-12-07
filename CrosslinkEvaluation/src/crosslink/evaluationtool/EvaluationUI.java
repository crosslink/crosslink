 /*
 * EvaluationUI.java
 *
 * The main UI of evaluation tool
 */
package crosslink.evaluationtool;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.jCharts.chartData.ChartDataException;
import org.jCharts.properties.PropertyException;

import org.xml.sax.SAXException;

import crosslink.measures.metricsCalculation;
import crosslink.measures.plotsCalculation;
import crosslink.resultsetGenerator.LtwResultsetType;
import crosslink.rungenerator.InexSubmission;
import crosslink.rungenerator.TopicType;
import crosslink.util.JChartDialog;
import crosslink.util.ioFileFilter;
import crosslink.util.jfcFileFilter;
import crosslink.util.runsXMLConvertors;

/**
 * Created on 7 September 2007, 09:58
 * @author  Darren Huang
 */
public class EvaluationUI extends JFrame {

    private RunTablePanel realRunTablePanel;
    private EvaTablePanel realEvaTablePanel;
    private File[] runFileCache = null;
    // For 2008
//    private String[] resultSetArray = {"6600F2FWikiResultSet.xml", "50A2BWikiResultSet.xml", "50A2BManualResultSet.xml"};
    // For 2009
//    private String[] resultSetArray = {"5000F2FWikiResultSet.xml", "33A2BWikiResultSet.xml", "33A2BManualResultSet.xml"};
    private String[] resultSetArray = {"33A2BWikiResultSet.xml", "33A2BWikiResultSet.xml", "33A2BManualResultSet.xml"};
    private String resultFilePath = "50A2BManualResultSet.xml";
    private String schemaFilePath = "LTW06Schema.xsd";

    private static void log(Object aObject) {
        System.out.println(String.valueOf(aObject));
    }

    private static void errlog(Object aObject) {
        System.err.println(String.valueOf(aObject));
    }

    public EvaluationUI() {
        this.realRunTablePanel = new RunTablePanel();
        this.realEvaTablePanel = new EvaTablePanel();

        initComponents();
        // ------------------------------------------
        resultFilePath = getResultSetPath();
        // ------------------------------------------
    }

    private void initComponents() {
        runTbRowMenu = new javax.swing.JPopupMenu();
        jMenuItemDelete = new javax.swing.JMenuItem();
        openfilesPanel = new javax.swing.JPanel();
        filedirectoryTextField = new javax.swing.JTextField();
        openfilesButton = new javax.swing.JButton();
        filecleanButton = new javax.swing.JButton();
        uploadButton = new javax.swing.JButton();
        runtablePanel = new javax.swing.JPanel();
        fullruntableButton = new javax.swing.JButton();
        runtablecleanallButton = new javax.swing.JButton();
        runTablePanelHolder = new javax.swing.JPanel();
        this.runTablePanelHolder.add(this.realRunTablePanel);
        evatablePanel = new javax.swing.JPanel();
        fullevatableButton = new javax.swing.JButton();
        evaTablePanelHolder = new javax.swing.JPanel();
        this.evaTablePanelHolder.add(this.realEvaTablePanel);
        evatablecleanallButton = new javax.swing.JButton();
        selectAllButton = new javax.swing.JButton();
        unselectAllButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jRBIntPrecisionRecallCurve = new javax.swing.JRadioButton();
        getplotsButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jRBalltopics = new javax.swing.JRadioButton();
        jRBonlysubmitted = new javax.swing.JRadioButton();
        jRBFileToFile = new javax.swing.JRadioButton();
        jRBFileToBep = new javax.swing.JRadioButton();
        jRBAnchorToFile = new javax.swing.JRadioButton();
        jRBAnchorToBEP = new javax.swing.JRadioButton();
        jRBF2FWikirs = new javax.swing.JRadioButton();
        jRBA2BWikirs = new javax.swing.JRadioButton();
        jRBA2BManualrs = new javax.swing.JRadioButton();
        evaluateButton = new javax.swing.JButton();
        fileMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();

        jMenuItemDelete.setText("delete");
        jMenuItemDelete.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDeleteActionPerformed(evt);
            }
        });

        runTbRowMenu.add(jMenuItemDelete);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Crosslink Evaluation Tool");

        openfilesButton.setText("Open Files");
        openfilesButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openfilesButtonActionPerformed(evt);
            }
        });

        uploadButton.setText("Load");
        uploadButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadButtonActionPerformed(evt);
            }
        });

        filecleanButton.setText("Clear");
        filecleanButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filecleanButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout openfilesPanelLayout = new org.jdesktop.layout.GroupLayout(openfilesPanel);
        openfilesPanel.setLayout(openfilesPanelLayout);
        openfilesPanelLayout.setHorizontalGroup(
                openfilesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(openfilesPanelLayout.createSequentialGroup().addContainerGap().add(openfilesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, openfilesPanelLayout.createSequentialGroup().add(filedirectoryTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(openfilesButton).add(22, 22, 22)).add(openfilesPanelLayout.createSequentialGroup().addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(uploadButton).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(filecleanButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 67, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addContainerGap()))));
        openfilesPanelLayout.setVerticalGroup(
                openfilesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(openfilesPanelLayout.createSequentialGroup().addContainerGap().add(openfilesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(filedirectoryTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(openfilesButton)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(openfilesPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(uploadButton).add(filecleanButton)).addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

        runtablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Run Information", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12)));
        runtablePanel.setName("File Information");
        fullruntableButton.setText("Manage Table");
        fullruntableButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fullruntableButtonActionPerformed(evt);
            }
        });

        runtablecleanallButton.setText("Clear Run Table");
        runtablecleanallButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runtablecleanallButtonActionPerformed(evt);
            }
        });


        runTablePanelHolder.setLayout(new javax.swing.BoxLayout(runTablePanelHolder, javax.swing.BoxLayout.X_AXIS));

        org.jdesktop.layout.GroupLayout runtablePanelLayout = new org.jdesktop.layout.GroupLayout(runtablePanel);
        runtablePanel.setLayout(runtablePanelLayout);
        runtablePanelLayout.setHorizontalGroup(
                runtablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(runtablePanelLayout.createSequentialGroup().addContainerGap().add(runtablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, runtablePanelLayout.createSequentialGroup().add(fullruntableButton).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(runtablecleanallButton)).add(org.jdesktop.layout.GroupLayout.TRAILING, runTablePanelHolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE))));
        runtablePanelLayout.setVerticalGroup(
                runtablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, runtablePanelLayout.createSequentialGroup().add(runTablePanelHolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 143, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(runtablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(fullruntableButton).add(runtablecleanallButton))));

        evatablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Evaluation Sets", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12)));
        fullevatableButton.setText("Manage Table");
        fullevatableButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fullevatableButtonActionPerformed(evt);
            }
        });

        evaTablePanelHolder.setLayout(new javax.swing.BoxLayout(evaTablePanelHolder, javax.swing.BoxLayout.X_AXIS));

        evatablecleanallButton.setText("Clear Evaluation Table");
        evatablecleanallButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                evatablecleanallButtonActionPerformed(evt);
            }
        });

        selectAllButton.setText("Select All");
        selectAllButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                evatableSelectAllButtonActionPerformed(evt);
            }
        });

        unselectAllButton.setText("UnSelect All");
        unselectAllButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                evatableUnSelectAllButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout evatablePanelLayout = new org.jdesktop.layout.GroupLayout(evatablePanel);
        evatablePanel.setLayout(evatablePanelLayout);
        evatablePanelLayout.setHorizontalGroup(
                evatablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, evatablePanelLayout.createSequentialGroup().addContainerGap(298, Short.MAX_VALUE).add(fullevatableButton).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(evatablecleanallButton).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(selectAllButton).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(unselectAllButton)).add(evaTablePanelHolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE));
        evatablePanelLayout.setVerticalGroup(
                evatablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, evatablePanelLayout.createSequentialGroup().add(evaTablePanelHolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 203, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(evatablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(fullevatableButton).add(evatablecleanallButton).add(selectAllButton).add(unselectAllButton))));


// ---------
        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Result Set Selection", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12)));

        // 6600 F2F Topics Result Set by Ground Truth
        jRBF2FWikirs.setText("F2F Ground-Truth");
        jRBF2FWikirs.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRBF2FWikirs.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRBF2FWikirs.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    f2fResultSetTypeActionPerformed(evt);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // 50 A2B Topics Result Set by Ground Truth
        jRBA2BWikirs.setText("A2B Ground-Truth");
        jRBA2BWikirs.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRBA2BWikirs.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRBA2BWikirs.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    f2fResultSetTypeActionPerformed(evt);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // 50 A2B Topics Result Set by Manual Assessment
        jRBA2BManualrs.setSelected(true);
        jRBA2BManualrs.setText("A2B Manual Assessment");
        jRBA2BManualrs.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRBA2BManualrs.setMargin(new java.awt.Insets(0, 0, 0, 0));

        //Group the radio buttons.
        ButtonGroup rsGroup = new ButtonGroup();
        rsGroup.add(jRBF2FWikirs);
        rsGroup.add(jRBA2BWikirs);
        rsGroup.add(jRBA2BManualrs);

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
                jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel6Layout.createSequentialGroup().addContainerGap().add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel6Layout.createSequentialGroup().add(jRBF2FWikirs).add(23, 23, 23).addPreferredGap(org.jdesktop.layout.GroupLayout.LEADING).add(jRBA2BWikirs).add(23, 23, 23).addPreferredGap(org.jdesktop.layout.GroupLayout.LEADING).add(jRBA2BManualrs).add(23, 23, 23))).addContainerGap()));
        jPanel6Layout.setVerticalGroup(
                jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel6Layout.createSequentialGroup().addContainerGap().add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jRBF2FWikirs).add(jRBA2BWikirs).add(jRBA2BManualrs)).addContainerGap()));

// ---------


        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Plots Computation", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12)));
        jRBIntPrecisionRecallCurve.setSelected(true);
        jRBIntPrecisionRecallCurve.setText("Interpolated Precision-Recall Curves");
        jRBIntPrecisionRecallCurve.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRBIntPrecisionRecallCurve.setMargin(new java.awt.Insets(0, 0, 0, 0));

        getplotsButton.setText("Get Plots");
        getplotsButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    getplotsButtonActionPerformed(evt);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
                jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel4Layout.createSequentialGroup().addContainerGap().add(jRBIntPrecisionRecallCurve).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 121, Short.MAX_VALUE).add(getplotsButton).addContainerGap()));
        jPanel4Layout.setVerticalGroup(
                jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel4Layout.createSequentialGroup().addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, getplotsButton).add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup().add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jRBIntPrecisionRecallCurve)).addContainerGap()))));

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Result Computation", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12)));
        jRBalltopics.setSelected(true);
        jRBalltopics.setText("Use All Topics");
        jRBalltopics.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRBalltopics.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRBonlysubmitted.setText("Use Only Submitted Topics");
        jRBonlysubmitted.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRBonlysubmitted.setMargin(new java.awt.Insets(0, 0, 0, 0));
        // jRBFileToFile
        jRBFileToFile.setSelected(true);
        jRBFileToFile.setText("FileToFile");
        jRBFileToFile.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRBFileToFile.setMargin(new java.awt.Insets(0, 0, 0, 0));

        // jRBFileToBep
        jRBFileToBep.setText("FileToBEP");
        jRBFileToBep.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRBFileToBep.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRBFileToBep.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    evaluationTypeActionPerformed(evt);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        // jRBAnchorToFile
        jRBAnchorToFile.setText("AnchorGToFile");
        jRBAnchorToFile.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRBAnchorToFile.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRBAnchorToFile.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    evaluationTypeActionPerformed(evt);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        // jRBAnchorToBEP
        jRBAnchorToBEP.setText("AnchorGToBEP");
        jRBAnchorToBEP.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jRBAnchorToBEP.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRBAnchorToBEP.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    evaluationTypeActionPerformed(evt);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        //Group the radio buttons.
        ButtonGroup group1 = new ButtonGroup();
        group1.add(jRBalltopics);
        group1.add(jRBonlysubmitted);

        ButtonGroup group2 = new ButtonGroup();
        group2.add(jRBFileToFile);
        group2.add(jRBFileToBep);
        group2.add(jRBAnchorToFile);
        group2.add(jRBAnchorToBEP);

        evaluateButton.setText("Evaluate");
        evaluateButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    evaluateButtonActionPerformed(evt);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
                jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel5Layout.createSequentialGroup().addContainerGap().add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel5Layout.createSequentialGroup().add(jRBalltopics).add(23, 23, 23).addPreferredGap(org.jdesktop.layout.GroupLayout.LEADING).add(jRBonlysubmitted).add(23, 23, 23)).add(jPanel5Layout.createSequentialGroup().add(jRBFileToFile).add(23, 23, 23).addPreferredGap(org.jdesktop.layout.GroupLayout.LEADING).add(jRBFileToBep).add(23, 23, 23).addPreferredGap(org.jdesktop.layout.GroupLayout.LEADING).add(jRBAnchorToFile).add(23, 23, 23).addPreferredGap(org.jdesktop.layout.GroupLayout.LEADING).add(jRBAnchorToBEP).add(23, 23, 23).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 247, Short.MAX_VALUE).add(evaluateButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 86, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))).addContainerGap()));
        jPanel5Layout.setVerticalGroup(
                jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel5Layout.createSequentialGroup().addContainerGap().add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jRBalltopics).add(jRBonlysubmitted)).addPreferredGap(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jRBFileToFile).add(jRBFileToBep).add(jRBAnchorToFile).add(jRBAnchorToBEP).add(evaluateButton)).addContainerGap()));

        fileMenu.setMnemonic('F');
        fileMenu.setText("File");
        aboutMenuItem.setMnemonic('A');
        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(aboutMenuItem);

        exitMenuItem.setMnemonic('E');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(exitMenuItem);

        fileMenuBar.add(fileMenu);

        setJMenuBar(fileMenuBar);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().addContainerGap().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(evatablePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(runtablePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(openfilesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(jPanel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap()));
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup().add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(openfilesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(runtablePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(evatablePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        pack();
    }

    private void evaluationTypeActionPerformed(ActionEvent evt) {
        if (!this.jRBA2BManualrs.getModel().isSelected()) {
            this.jRBFileToFile.setSelected(true);
        }
    }

    private void f2fResultSetTypeActionPerformed(ActionEvent evt) {
        this.jRBFileToFile.setSelected(true);
    }

    private void getplotsButtonActionPerformed(java.awt.event.ActionEvent evt) throws Exception {//GEN-FIRST:event_getplotsButtonActionPerformed
        try {

            boolean useAllTopics = true;
            boolean useFileToBep = false;
            boolean useAnchorToFile = false;
            boolean useAnchorToBEP = false;
            int colorColumnNo = 10;
            int linewidthColumnNo = 11;

            if (this.jRBalltopics.getModel().isSelected()) {
                useAllTopics = true;
            } else {
                useAllTopics = false;   // Only use submitted topics to calculate
            }
            if (this.jRBFileToBep.getModel().isSelected()) {
                useFileToBep = true;
            } else {
                useFileToBep = false;
            }
            if (this.jRBAnchorToFile.getModel().isSelected()) {
                useAnchorToFile = true;
            } else {
                useAnchorToFile = false;
            }
            if (this.jRBAnchorToBEP.getModel().isSelected()) {
                useAnchorToBEP = true;
            } else {
                useAnchorToBEP = false;
            }

            Vector data = this.realEvaTablePanel.getModelData();
            HashMap plotHashmap = new HashMap();
            for (Object row : data) {
                Vector aRow = (Vector) row;
                if (aRow.get(12).equals(Boolean.TRUE)) {
                    HashMap hm = (HashMap) plotHashmap.get(aRow.get(0));
                    if (hm == null) {
                        hm = new HashMap();
                        plotHashmap.put(aRow.get(0), hm);
                    }
                    Object[] ctObject = new Object[2];
                    ctObject[0] = aRow.get(colorColumnNo);
                    ctObject[1] = aRow.get(linewidthColumnNo);
                    hm.put(aRow.get(1), ctObject);   // Type & Color & Thick
                }
            }

            if (plotHashmap.isEmpty()) {

                JOptionPane.showMessageDialog(this, "Please select the plot to draw", "Message Board", JOptionPane.INFORMATION_MESSAGE);

            } else {
                resultFilePath = getResultSetPath();
                File resultFile = new File(resultFilePath);
                Vector<Object[]> incommingPlotData = new Vector<Object[]>();
                Vector<Object[]> outgoingPlotData = new Vector<Object[]>();
                Vector<Object[]> combinationPlotData = new Vector<Object[]>();
                // RESULT_TYPE_NUM: incoming --> outgoing --> combination
                for (int i = 0; i < this.runFileCache.length; i++) {
                    plotsCalculation.PRCurveResult pcr = plotsCalculation.plotCalculate(resultFile, this.runFileCache[i], useAllTopics, useFileToBep, useAnchorToFile, useAnchorToBEP);
                    if (plotHashmap.containsKey((Object) pcr.plotRunId)) {

                        HashMap hmap = (HashMap) plotHashmap.get((Object) pcr.plotRunId);
                        if (hmap.containsKey("incoming")) {
                            incommingPlotData.add(new Object[]{
                                        pcr.incomming, hmap.get("incoming"), pcr.plotRunId
                                    });
                        }
                        if (hmap.containsKey("outgoing")) {
                            outgoingPlotData.add(new Object[]{
                                        pcr.outgoing, hmap.get("outgoing"), pcr.plotRunId
                                    });
                        }
                        if (hmap.containsKey("f_score")) {
                            combinationPlotData.add(new Object[]{
                                        pcr.combination, hmap.get("f_score"), pcr.plotRunId
                                    });
                        }
                    }
                }

                Vector[] plotDatas = null;
                String[] plotTitle = null;
                if (incommingPlotData.isEmpty() && outgoingPlotData.isEmpty()) {
                    plotDatas = new Vector[]{combinationPlotData};
                    plotTitle = new String[]{"InteP-R Curve: Combination"};
                } else if (incommingPlotData.isEmpty() && combinationPlotData.isEmpty()) {
                    plotDatas = new Vector[]{outgoingPlotData};
                    plotTitle = new String[]{"InteP-R Curve: Outgoing"};
                } else if (outgoingPlotData.isEmpty() && combinationPlotData.isEmpty()) {
                    plotDatas = new Vector[]{incommingPlotData};
                    plotTitle = new String[]{"InteP-R Curve: Incoming"};
                } else if (incommingPlotData.isEmpty()) {
                    plotDatas = new Vector[]{
                                outgoingPlotData, combinationPlotData
                            };
                    plotTitle = new String[]{"InteP-R Curve: Outgoing", "InteP-R Curve: Combination"};
                } else if (outgoingPlotData.isEmpty()) {
                    plotDatas = new Vector[]{
                                incommingPlotData, combinationPlotData
                            };
                    plotTitle = new String[]{"InteP-R Curve: Incoming", "InteP-R Curve: Combination"};
                } else if (combinationPlotData.isEmpty()) {
                    plotDatas = new Vector[]{
                                incommingPlotData, outgoingPlotData
                            };
                    plotTitle = new String[]{"InteP-R Curve: Incoming", "InteP-R Curve: Outgoing"};
                } else {
                    plotDatas = new Vector[]{
                                incommingPlotData, outgoingPlotData, combinationPlotData
                            };
                    plotTitle = new String[]{"InteP-R Curve: Incoming", "InteP-R Curve: Outgoing", "InteP-R Curve: Combination"};
                }

                for (int j = 0; j < plotDatas.length; j++) {
                    Vector<Object[]> cool_data_points = (Vector<Object[]>) plotDatas[j];
                    JChartDialog jplot = new JChartDialog(this, false, plotTitle[j], cool_data_points);

                    // Export Selected Row Data to .CSV
                    this.exportPlotDatatoCSV(plotTitle[j], cool_data_points);
                    // End of Export to .CSV

                    Point ploca = this.getLocation();
//                    jplot.setLocation(ploca.x + j * 30, ploca.y + j * 30);
//                    jplot.setVisible(true);
                }
            }

        } catch (PropertyException ex) {
            ex.printStackTrace();
        } catch (ChartDataException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_getplotsButtonActionPerformed

    void exportPlotDatatoCSV(String plotType, Vector<Object[]> cool_data_points) {
        try {
            String pdFileName = plotType.substring(plotType.indexOf(":") + 1) + this.getNowTime("MMddmmss") + ".CSV";
            File csvFile = new File(pdFileName);
            BufferedWriter bw = new BufferedWriter(new FileWriter(csvFile, true));
            PrintWriter pw = new PrintWriter(bw);
            String[] columnTitle = {"Run", "0.05", "0.10", "0.15", "0.20", "0.25", "0.30", "0.35", "0.40", "0.45", "0.50",
                "0.55", "0.60", "0.65", "0.70", "0.75", "0.80", "0.85", "0.90", "0.95", "1.00"
            };
            for (int k = 0; k < columnTitle.length; k++) {
                pw.print(columnTitle[k] + ",");
            }
            pw.println("");
            String rowTitle = "";
            double[] rowData = new double[20];
            for (int i = 0; i < cool_data_points.size(); i++) {
                Object[] pd = cool_data_points.get(i);
                rowTitle = pd[2].toString();
                pw.print(rowTitle + ",");
                rowData = (double[]) pd[0];
                for (int j = 0; j < rowData.length; j++) {
                    pw.print(rowData[j] + ",");
                }
                pw.println("");
            }
            pw.close();

        } catch (Exception exc) {
            System.out.println("Error While Exporting Plot Data to CSV file: /n" + exc.toString());
        }
    }

    private String getNowTime(String dateFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());
    }

    private void fullevatableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fullevatableButtonActionPerformed
        JDialog showFullEvaTable = new FullEvaTable(this);
        ((FullEvaTable) showFullEvaTable).setTable(new EvaTablePanel(this.realEvaTablePanel));
        showFullEvaTable.setVisible(true);
    }//GEN-LAST:event_fullevatableButtonActionPerformed

    private void evatablecleanallButtonActionPerformed(java.awt.event.ActionEvent evt) {
        this.realEvaTablePanel.cleanAllEvaTable();
    }

    private void evatableSelectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {
        this.realEvaTablePanel.selectAllRunPlots();
    }

    private void evatableUnSelectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {
        this.realEvaTablePanel.unselectAllRunPlots();
    }

    private void evaluateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_evaluateButtonActionPerformed

        try {
            resultFilePath = getResultSetPath();
            File resultFile = new File(resultFilePath);
            ArrayList<Object[]> result = new ArrayList<Object[]>();

            boolean useAllTopics = false;
            boolean useFileToBep = false;
            boolean useAnchorGToFile = false;
            boolean useAnchorGToBEP = false;

            if (this.jRBalltopics.getModel().isSelected()) {
                useAllTopics = true;
            }
            if (this.jRBFileToBep.getModel().isSelected()) {
                useFileToBep = true;
            } else if (this.jRBAnchorToFile.getModel().isSelected()) {
                useAnchorGToFile = true;
            } else if (this.jRBAnchorToBEP.getModel().isSelected()) {
                useAnchorGToBEP = true;
            }

            Color[][] spColor = {
                {new Color(150, 0, 0), new Color(150, 0, 0), new Color(150, 0, 0)},
                {new Color(0, 150, 0), new Color(0, 150, 0), new Color(0, 150, 0)},
                {new Color(0, 0, 150), new Color(0, 0, 150), new Color(0, 0, 150)},
                {new Color(150, 150, 0), new Color(150, 150, 0), new Color(150, 150, 0)},
                {new Color(200, 0, 200), new Color(200, 0, 200), new Color(200, 0, 200)}
            };



            for (int i = 0; i < this.runFileCache.length; i++) {

                metricsCalculation.EvaluationResult er;

                er = metricsCalculation.calculate(resultFile, this.runFileCache[i], useAllTopics, useFileToBep, useAnchorGToFile, useAnchorGToBEP);

//                if (i < 5) {
//                    result.add(new Object[]{
//                        er.runId, "incoming", er.incomming[metricsCalculation.R_MAP],
//                        er.incomming[metricsCalculation.R_RPREC],er.incomming[metricsCalculation.R_P5],
//                        er.incomming[metricsCalculation.R_P10],
//                        er.incomming[metricsCalculation.R_P20],
//                        er.incomming[metricsCalculation.R_P30], er.incomming[metricsCalculation.R_P50],
//                        er.incomming[metricsCalculation.R_P250],
//                        spColor[i][0], Boolean.TRUE, Boolean.FALSE
//                    });
//                    result.add(new Object[]{
//                        er.runId, "outgoing", er.outgoing[metricsCalculation.R_MAP],
//                        er.outgoing[metricsCalculation.R_RPREC],er.outgoing[metricsCalculation.R_P5],
//                        er.outgoing[metricsCalculation.R_P10],
//                        er.outgoing[metricsCalculation.R_P20],
//                        er.outgoing[metricsCalculation.R_P30], er.outgoing[metricsCalculation.R_P50],
//                        er.outgoing[metricsCalculation.R_P250],
//                        spColor[i][1], Boolean.TRUE, Boolean.FALSE
//                    });
//                    result.add(new Object[]{
//                        er.runId, "f_score", er.combination[metricsCalculation.R_MAP],
//                        er.combination[metricsCalculation.R_RPREC],er.combination[metricsCalculation.R_P5],
//                        er.combination[metricsCalculation.R_P10],
//                        er.combination[metricsCalculation.R_P20],
//                        er.combination[metricsCalculation.R_P30], er.combination[metricsCalculation.R_P50],
//                        er.combination[metricsCalculation.R_P250],
//                        spColor[i][2], Boolean.TRUE, Boolean.FALSE
//                    });
//                } else {
                result.add(new Object[]{
                            er.runId, "incoming", er.incomming[metricsCalculation.R_MAP],
                            er.incomming[metricsCalculation.R_RPREC], er.incomming[metricsCalculation.R_P5],
                            er.incomming[metricsCalculation.R_P10],
                            er.incomming[metricsCalculation.R_P20],
                            er.incomming[metricsCalculation.R_P30], er.incomming[metricsCalculation.R_P50],
                            er.incomming[metricsCalculation.R_P250],
                            Color.LIGHT_GRAY, Boolean.FALSE, Boolean.FALSE
                        });
                result.add(new Object[]{
                            er.runId, "outgoing", er.outgoing[metricsCalculation.R_MAP],
                            er.outgoing[metricsCalculation.R_RPREC], er.outgoing[metricsCalculation.R_P5],
                            er.outgoing[metricsCalculation.R_P10],
                            er.outgoing[metricsCalculation.R_P20],
                            er.outgoing[metricsCalculation.R_P30], er.outgoing[metricsCalculation.R_P50],
                            er.outgoing[metricsCalculation.R_P250],
                            Color.LIGHT_GRAY, Boolean.FALSE, Boolean.FALSE
                        });
//                result.add(new Object[]{
//                            er.runId, "f_score", er.combination[metricsCalculation.R_MAP],
//                            er.combination[metricsCalculation.R_RPREC], er.combination[metricsCalculation.R_P5],
//                            er.combination[metricsCalculation.R_P10],
//                            er.combination[metricsCalculation.R_P20],
//                            er.combination[metricsCalculation.R_P30], er.combination[metricsCalculation.R_P50],
//                            er.combination[metricsCalculation.R_P250],
//                            Color.LIGHT_GRAY, Boolean.FALSE, Boolean.FALSE
//                        });
//                }
            }
            // -----------------------------------------------------------------
            // =================================================================
            // Sort the "result" by f-score value decreasingly
            Object[][] objData = new Object[result.size()][];
            for (int i = 0; i < result.size(); i++) {
                objData[i] = result.get(i);
            }
            Hashtable compareHash = new Hashtable();

            for (int j = 1; j < objData.length; j = j + 2) {
                ArrayList<Object[]> threeColl = new ArrayList<Object[]>();
//                threeColl.add(objData[j - 2]);
                threeColl.add(objData[j - 1]);
                threeColl.add(objData[j]);
                compareHash.put(objData[j][2], threeColl);
            }
            Vector v = new Vector(compareHash.keySet());
            Collections.sort(v, Collections.reverseOrder());
            Object[][] evaData = new Object[result.size()][];
            int sortIndex = 0;
            int colorCount = 0;
            for (Enumeration e = v.elements(); e.hasMoreElements();) {
                Object key = (Object) e.nextElement();
                ArrayList<Object[]> sortColl = (ArrayList<Object[]>) compareHash.get(key);
                evaData[sortIndex] = sortColl.get(0);
                evaData[sortIndex + 1] = sortColl.get(1);
//                evaData[sortIndex + 2] = sortColl.get(2);
                if (colorCount < 5) {
                    evaData[sortIndex][10] = spColor[colorCount][0];
                    evaData[sortIndex + 1][10] = spColor[colorCount][1];
//                    evaData[sortIndex + 2][10] = spColor[colorCount][2];
                    evaData[sortIndex][11] = Boolean.TRUE;
                    evaData[sortIndex + 1][11] = Boolean.TRUE;
//                    evaData[sortIndex + 2][11] = Boolean.TRUE;
                }
                colorCount++;
                sortIndex = sortIndex + 2;
            }
            // END of sort the result
            // =================================================================
            // -----------------------------------------------------------------

            this.realEvaTablePanel.evaluateRunsToTable(evaData);
        } catch (Exception ex) {
            ex.printStackTrace();
//            JOptionPane.showMessageDialog(this, ex.toString(), "Exception Board", JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_evaluateButtonActionPerformed

    private void fullruntableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fullruntableButtonActionPerformed
        JDialog showFullRunTable = new FullRunTable(this);
        ((FullRunTable) showFullRunTable).setTable(new RunTablePanel(this.realRunTablePanel));
        showFullRunTable.setVisible(true);
    }//GEN-LAST:event_fullruntableButtonActionPerformed

    private void jMenuItemDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDeleteActionPerformed
    }//GEN-LAST:event_jMenuItemDeleteActionPerformed

    private void runtablecleanallButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runtablecleanallButtonActionPerformed
        this.realRunTablePanel.cleanAllRunTable();
    }//GEN-LAST:event_runtablecleanallButtonActionPerformed

    private void uploadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadButtonActionPerformed
        // BUGS: NEED to be fixed

        String[] fl = this.filedirectoryTextField.getText().split(";");

        File[] files = new File[fl.length];
        for (int l = 0; l < files.length; l++) {
            String afile = fl[l].toString().trim();
            files[l] = new File(afile);
        }
        // -------------------------------------------------------------
        List<File> tmp = Arrays.asList(files);
        ArrayList<File> fileList = new ArrayList<File>(tmp);
        for (int k = 0; k < fileList.size(); k++) {
            try {
                JAXBContext jc = JAXBContext.newInstance("crosslink.rungenerator");
                Unmarshaller um = jc.createUnmarshaller();
                Object unmarshal = um.unmarshal(fileList.get(k));
            } catch (JAXBException ex) {
                JOptionPane.showMessageDialog(this, fileList.get(k).getAbsolutePath() + " is invalid.", "Exception Message", JOptionPane.ERROR_MESSAGE);
                fileList.remove(k);
            }
        }
        files = fileList.toArray(new File[fileList.size()]);
        this.runFileCache = files;
        // -------------------------------------------------------------

        ArrayList<String[]> result = retrieveRunData(files);
        Object[][] r = new Object[result.size()][6];
        int count = 0;
        for (String[] x : result) {
            r[count][0] = x[0];
            r[count][1] = x[1];
            r[count][2] = x[2];
            r[count][3] = x[3];
            r[count][4] = x[4];
            r[count][5] = x[5];
            count++;
        }

        this.realRunTablePanel.uploadFileToTable(r);

        // record the file name into "filedirectoryTextField"
        String strFiles = "";
        for (int i = 0; i < files.length; i++) {
            strFiles += files[i].getAbsolutePath() + "; ";
        }

        this.filedirectoryTextField.setText(strFiles);
    }//GEN-LAST:event_uploadButtonActionPerformed

    private void filecleanButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filecleanButtonActionPerformed
        filedirectoryTextField.setText("");
    }//GEN-LAST:event_filecleanButtonActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        JDialog about = new About(this);
        about.setVisible(true);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed
    String currentOpenDir = "C:\\";
    private void openfilesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openfilesButtonActionPerformed

        String newline = "\n";
        JFileChooser fc = new JFileChooser(currentOpenDir);
        // To allow both (multiple) files and directories to be selected
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(true);
        // filter XML files here
        jfcFileFilter filter = new jfcFileFilter();
        filter.addExtension("XML");
        filter.addExtension("xml");
        filter.addExtension("Xml");
        filter.setDescription("XML files only");

        fc.setFileFilter(filter);

        // Handle open files button action
        if (evt.getSource() == openfilesButton) {

            int returnVal = fc.showOpenDialog(EvaluationUI.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {

                File[] files = fc.getSelectedFiles();

//                this.runFileCache = files;  // remember to delete
                String absFilePath = files[0].getAbsolutePath();
                currentOpenDir = absFilePath.substring(0, absFilePath.lastIndexOf(File.separator));

                if (files[0].isDirectory() && files[0].exists()) {
                    // get all files in directory
                    ioFileFilter ioFilter = new ioFileFilter();
                    files = files[0].listFiles(ioFilter);
                }
                // This part is used to eliminate the RUN files that cannot be processed by JAXB
                List<File> tmp = Arrays.asList(files);
                ArrayList<File> fileList = new ArrayList<File>(tmp);
                for (int k = 0; k < fileList.size(); k++) {
                    try {
                        JAXBContext jc = JAXBContext.newInstance("crosslink.rungenerator");

                        // ----------
                        Schema vschema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new File(schemaFilePath));

                        Unmarshaller um = jc.createUnmarshaller();
//                        um.setSchema(vschema);
                        // ----------
                        Object unmarshal = um.unmarshal(fileList.get(k));
                    } catch (SAXException sex) {
//                        System.err.println(sex.getStackTrace());
                        JOptionPane.showMessageDialog(this, fileList.get(k).getAbsolutePath() + " is invalid.\n" + sex.toString(),
                                "SAXException Message", JOptionPane.ERROR_MESSAGE);
                    } catch (JAXBException ex) {
                        String jexString = StackTraceToString(ex);
                        JOptionPane.showMessageDialog(this, fileList.get(k).getAbsolutePath() + " is invalid.\n" + jexString,
                                "Exception Message", JOptionPane.ERROR_MESSAGE);
                        fileList.remove(k);
                    }
                }
                files = fileList.toArray(new File[fileList.size()]);
                this.runFileCache = files;
                // -------------------------------------------------------------

                ArrayList<String[]> result = retrieveRunData(files);
                Object[][] r = new Object[result.size()][8];
                int count = 0;
                for (String[] x : result) {
                    r[count][0] = x[0];
                    r[count][1] = x[1];
                    r[count][2] = x[2];
                    r[count][3] = x[3];
                    r[count][4] = x[4];
                    r[count][5] = x[5];
                    r[count][6] = x[6];
                    r[count][7] = x[7];
                    count++;
                }

                this.realRunTablePanel.uploadFileToTable(r);

                // record the file name into "filedirectoryTextField"
                String strFiles = "";
                for (int i = 0; i < files.length; i++) {
                    strFiles += files[i].getAbsolutePath() + "; ";
                }

                this.filedirectoryTextField.setText(strFiles);

            } else {
                JOptionPane.showMessageDialog(this, "Open command cancelled by the user", "Message Board", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_openfilesButtonActionPerformed

    private ArrayList<String[]> retrieveRunData(File[] file) {

        int runInfoColumnLength = 8;
        resultFilePath = getResultSetPath();
        log(resultFilePath);
        Hashtable resultLinks = ResultSetLinksNo(new File(resultFilePath));

        ArrayList<String[]> bigResult = new ArrayList<String[]>();
        // String[] result = null;
        for (int i = 0; i < file.length; i++) {
            String[] result = null;
            try {
                JAXBContext jc = JAXBContext.newInstance("crosslink.rungenerator");
                Unmarshaller um = jc.createUnmarshaller();
                Object unmarshal = um.unmarshal(file[i]);
                if (unmarshal instanceof InexSubmission) {
                    InexSubmission bt = (InexSubmission) unmarshal;

                    String id = bt.getParticipantId();
                    String rid = bt.getRunId();
                    for (TopicType topic : bt.getTopic()) {
                        result = new String[runInfoColumnLength];
                        result[0] = id;
                        result[1] = rid;
                        result[2] = topic.getFile();
                        result[3] = topic.getName();
                        log(result[0] + " - " + result[1] + " - " + result[2] + " - " + result[3]);
                        log(resultLinks.size());
                        // -----------------------------------------------------
                        String[] topicLinks = ((String) resultLinks.get(result[2])).split(";");
                        result[5] = topicLinks[0];
                        result[7] = topicLinks[1];
                        // -----------------------------------------------------
//                        result[3] = topic.getName();
                        if (topic.getOutgoing().getLink().isEmpty()) {
                            result[4] = "0";
                        } else {
                            if (topic.getOutgoing().getLink().get(0).getLinkto().isEmpty()) {
                                result[4] = "0";
                            } else {
                                // ============================
                                int outLinkCount = 0;
                                int outNonduplicateCount = 0;
                                List outItemsList = new ArrayList();
                                for (int j = 0; j < topic.getOutgoing().getLink().size(); j++) {
                                    List<crosslink.rungenerator.LinktoType> linkTo = topic.getOutgoing().getLink().get(j).getLinkto();
                                    for (int k = 0; k < linkTo.size(); k++) {
                                        outLinkCount++;
                                        int endop = linkTo.get(k).getFile().toLowerCase().trim().indexOf(".xml");
                                        if (endop != -1) {
                                            String outFile = linkTo.get(k).getFile().toString();
                                            if (!outItemsList.contains(outFile)) {
                                                outItemsList.add(outFile);
                                                outNonduplicateCount++;
                                            }
                                        }
                                    }
                                }
                                // ============================
                                // totalFiles:nonDuplicateFiles
//                                result[4] = "" + topic.getOutgoing().getLink().size() + "(" + outNonduplicateCount + ")";
                                result[4] = "" + outLinkCount + "(" + outNonduplicateCount + ")";
                            }
                        }
                        if (topic.getIncoming().getLink().isEmpty()) {
                            result[6] = "0";
                        } else {
                            if (topic.getIncoming().getLink().get(0).getAnchor().getFile().toString().equalsIgnoreCase("")) {
                                result[6] = "0";
                            } else {
                                // ============================
                                int inLinkCount = 0;
                                int inNonduplicateCount = 0;
                                List inItemsList = new ArrayList();
                                for (int j = 0; j < topic.getIncoming().getLink().size(); j++) {
                                    int endop = topic.getIncoming().getLink().get(j).getAnchor().getFile().toString().toLowerCase().indexOf(".xml");
                                    if (endop != -1) {
                                        String inFile = topic.getIncoming().getLink().get(j).getAnchor().getFile().toString();
                                        if (!inItemsList.contains(inFile)) {
                                            inItemsList.add(inFile);
                                            inNonduplicateCount++;
                                        }
                                    }
                                }
                                // ============================
                                // totalFiles:nonDuplicateFiles
                                result[6] = "" + topic.getIncoming().getLink().size() + "(" + inNonduplicateCount + ")";
                            }
                        }
                        bigResult.add(result);
                    }
                }
            } catch (JAXBException ex) {
                ex.printStackTrace();
            }
        }
        return bigResult;
    }

    private static Hashtable ResultSetLinksNo(File resultfiles) {
        Hashtable resultLinksTable = new Hashtable();
        try {
            JAXBContext jc;
            jc = JAXBContext.newInstance("crosslink.resultsetGenerator");
            Unmarshaller um = jc.createUnmarshaller();
            LtwResultsetType lrs = (LtwResultsetType) ((um.unmarshal(resultfiles)));

            if (lrs.getLtwTopic().size() > 0) {
                for (int i = 0; i < lrs.getLtwTopic().size(); i++) {

                    int inCount = 0;
                    int outCount = 0;

                    String topicID = lrs.getLtwTopic().get(i).getId();

                    log("topicID: " + topicID);

                    if (lrs.getLtwTopic().get(i).getOutgoingLinks().getOutLink().isEmpty()) {
                    } else {
                        String[] outLinks = new String[lrs.getLtwTopic().get(i).getOutgoingLinks().getOutLink().size()];
                        for (int j = 0; j < lrs.getLtwTopic().get(i).getOutgoingLinks().getOutLink().size(); j++) {
                            outCount++;
                        }
                    }
                    if (lrs.getLtwTopic().get(i).getIncomingLinks().getInLink().isEmpty()) {
                    } else {
                        String[] inLinks = new String[lrs.getLtwTopic().get(i).getIncomingLinks().getInLink().size()];
                        for (int k = 0; k < lrs.getLtwTopic().get(i).getIncomingLinks().getInLink().size(); k++) {
                            inCount++;
                        }
                    }

                    resultLinksTable.put(topicID + ".xml", outCount + ";" + inCount);
                    log("Outgoing Links: " + outCount);
                    log("Incoming Links: " + inCount);
                }
            }

        } catch (JAXBException ex) {
            ex.printStackTrace();
        }

        return resultLinksTable;
    }

    private static String StackTraceToString(JAXBException jaxbe) {
        try {

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            jaxbe.printStackTrace(pw);

            String retSW = sw.toString();
            String newSW = "";
            int firstPos = 0;
            int nextPos = 0;
            int endPos = 0;
            firstPos = retSW.indexOf("[");
            while (firstPos != -1) {

                endPos = retSW.indexOf("]", nextPos);
                newSW += retSW.substring(firstPos + 1, endPos) + "\n";

                nextPos = endPos + 1;
                firstPos = retSW.indexOf("[", nextPos);
            }
            sw.close();
            pw.close();

            return newSW;

        } catch (Exception e2) {
            return "Invalid Exception Catching when validating XML against Schema";
        }
    }

    public static void main(String args[]) throws Exception {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new EvaluationUI().setVisible(true);
            }
        });
    }

    private String getResultSetPath() {
        String resultSet = "";

        if (this.jRBF2FWikirs.getModel().isSelected()) {
            resultSet = resultSetArray[0];
        } else if (this.jRBA2BWikirs.getModel().isSelected()) {
            resultSet = resultSetArray[1];
        } else if (this.jRBA2BManualrs.getModel().isSelected()) {
            resultSet = resultSetArray[2];
        }

        return resultSet;
    }

    private void runsXMLConvertor(Hashtable runsHashData) {
        // To convert submitted runs from/into local XML files
        runsXMLConvertors xmlConverter = new runsXMLConvertors(runsHashData);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JPanel evaTablePanelHolder;
    private javax.swing.JButton evaluateButton;
    private javax.swing.JPanel evatablePanel;
    private javax.swing.JButton evatablecleanallButton;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuBar fileMenuBar;
    private javax.swing.JButton filecleanButton;
    private javax.swing.JTextField filedirectoryTextField;
    private javax.swing.JButton fullevatableButton;
    private javax.swing.JButton fullruntableButton;
    private javax.swing.JButton getplotsButton;
    private javax.swing.JMenuItem jMenuItemDelete;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JRadioButton jRBFileToFile;
    private javax.swing.JRadioButton jRBFileToBep;
    private javax.swing.JRadioButton jRBAnchorToFile;
    private javax.swing.JRadioButton jRBAnchorToBEP;
    private javax.swing.JRadioButton jRBalltopics;	// Use All Topics
    private javax.swing.JRadioButton jRBonlysubmitted;
    private javax.swing.JRadioButton jRBIntPrecisionRecallCurve;
    private javax.swing.JRadioButton jRBF2FWikirs;
    private javax.swing.JRadioButton jRBA2BWikirs;
    private javax.swing.JRadioButton jRBA2BManualrs;
    private javax.swing.JButton openfilesButton;
    private javax.swing.JPanel openfilesPanel;
    private javax.swing.JPanel runTablePanelHolder;
    private javax.swing.JPopupMenu runTbRowMenu;
    private javax.swing.JPanel runtablePanel;
    private javax.swing.JButton runtablecleanallButton;
    private javax.swing.JButton uploadButton;
    private javax.swing.JButton selectAllButton;
    private javax.swing.JButton unselectAllButton;
    // End of variables declaration//GEN-END:variables
}
