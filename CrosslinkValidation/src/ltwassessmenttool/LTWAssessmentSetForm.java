package ltwassessmenttool;

import java.io.File;
import java.util.Collections;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import ltwassessment.parsers.PoolerManager;
import ltwassessment.parsers.resourcesManager;

import org.jdesktop.application.Action;

/**
 * @author Darren HUANG
 */
public class LTWAssessmentSetForm extends javax.swing.JDialog {

    private resourcesManager toolRscManager = null;
    private String[] afProperty = new String[4];
    private Vector<String[]> RunTopics = new Vector<String[]>();
    private Vector<String> topicFileIDsV = new Vector<String>();

    public LTWAssessmentSetForm(java.awt.Frame parent) {
        super(parent);
        initComponents();
        this.toolRscManager = resourcesManager.getInstance();
        getRootPane().setDefaultButton(closeBtn);

        String currAFFormPath = this.toolRscManager.getPoolXMLFile();
        if (!currAFFormPath.equals("")) {
            this.assXmlSetTxtField.setText(currAFFormPath);
        }
    }

    @Action
    public void OKAssFormBox() {
        String thisAssessFormXmlPath = this.assXmlSetTxtField.getText();
        if (thisAssessFormXmlPath.equals("")) {
            String notDirMsg = "The Assessment Form XML file path cannot be empty.";
            JOptionPane.showMessageDialog(rootPane, notDirMsg);
        } else {
            File thisAssessFormXmlFile = new File(thisAssessFormXmlPath);
            if (thisAssessFormXmlFile.isFile() && thisAssessFormXmlPath.toLowerCase().endsWith(".xml")) {
                // 1) update Pool XML File Path
                this.toolRscManager.updateAFXmlFile(thisAssessFormXmlPath);
                // 2) update Others
                updatePoolerToResourceXML();
                dispose();
            } else {
                String notDirMsg = "The Assessment Form must be an XML file.";
                JOptionPane.showMessageDialog(rootPane, notDirMsg);
            }
        }
    }

    @Action
    public void CancelAssFormBox() {
        dispose();
    }

    // <editor-fold defaultstate="collapsed" desc="Collection Browse Buttons">
    String currentOpenDir = "C:\\";

    @Action
    public void browseAssessXMLSet() {
        JFileChooser fc = new JFileChooser(currentOpenDir);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setMultiSelectionEnabled(false);

        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File thisXmlFile = fc.getSelectedFile();
            String absFilePath = thisXmlFile.getAbsolutePath();
            currentOpenDir = absFilePath.substring(0, absFilePath.lastIndexOf(File.separator));
            if (thisXmlFile.isFile() && absFilePath.toLowerCase().endsWith(".xml")) {
                // get & Display Directory Path
                toolRscManager.updateAFXmlFile(absFilePath);
                this.assXmlSetTxtField.setText(absFilePath);
            } else {
                String notDirMsg = "The Assessment Set must be an XML file.";
                JOptionPane.showMessageDialog(this, notDirMsg);
            }
        }
    }
    // </editor-fold>

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JLabel imageLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        assXmlSetTxtField = new javax.swing.JTextField();
        assBrowseBtn = new javax.swing.JButton();
        closeBtn = new javax.swing.JButton();
        cancelbtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        imageLabel.setName("imageLabel"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ltwassessmenttool.LTWAssessmentToolApp.class).getContext().getResourceMap(LTWAssessmentSetForm.class);
        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        assXmlSetTxtField.setText(resourceMap.getString("assXmlSetTxtField.text")); // NOI18N
        assXmlSetTxtField.setEnabled(false);
        assXmlSetTxtField.setName("assXmlSetTxtField"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ltwassessmenttool.LTWAssessmentToolApp.class).getContext().getActionMap(LTWAssessmentSetForm.class, this);
        assBrowseBtn.setAction(actionMap.get("browseAssessXMLSet")); // NOI18N
        assBrowseBtn.setText(resourceMap.getString("assBrowseBtn.text")); // NOI18N
        assBrowseBtn.setName("assBrowseBtn"); // NOI18N

        closeBtn.setAction(actionMap.get("OKAssFormBox")); // NOI18N
        closeBtn.setText(resourceMap.getString("closeBtn.text")); // NOI18N
        closeBtn.setName("closeBtn"); // NOI18N

        cancelbtn.setText(resourceMap.getString("cancelbtn.text")); // NOI18N
        cancelbtn.setName("cancelbtn"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(imageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(assXmlSetTxtField, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                        .addComponent(assBrowseBtn))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(closeBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelbtn)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(imageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 177, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(assXmlSetTxtField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(assBrowseBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 63, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelbtn)
                    .addComponent(closeBtn))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton assBrowseBtn;
    private javax.swing.JTextField assXmlSetTxtField;
    private javax.swing.JButton cancelbtn;
    private javax.swing.JButton closeBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables

    private void updatePoolerToResourceXML() {
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ltwassessmenttool.LTWAssessmentToolApp.class).getContext().getResourceMap(LTWAssessmentToolView.class);
        PoolerManager myPooler = PoolerManager.getInstance();
        afProperty = myPooler.getPoolProperty();    // [0]:participant-id, [1]:run-id, [2]:task, [3]:collection
        RunTopics = myPooler.getAllTopicsInPool();  // Vector<String[]>: [0]:File, [1]:Name
        // ---------------------------------------------------------------------
        boolean isTopicWikipedia = false;
        boolean isLinkWikipedia = false;
        String myAFTask = afProperty[2].trim();
        if (myAFTask.equals(resourceMap.getString("task.ltwF2F")) || myAFTask.equals(resourceMap.getString("task.ltwA2B"))) {
            isTopicWikipedia = true;
            isLinkWikipedia = true;
        } else if (myAFTask.equals(resourceMap.getString("task.ltaraA2B"))) {
            isTopicWikipedia = false;
            isLinkWikipedia = false;
        } else if (myAFTask.equals(resourceMap.getString("task.ltaratwA2B"))) {
            isTopicWikipedia = false;
            isLinkWikipedia = true;
        }
        // ---------------------------------------------------------------------
        // 1) topic collection type --> link collection type
        if (isTopicWikipedia) {
            this.toolRscManager.updateTopicCollType(resourceMap.getString("collectionType.Wikipedia"));
        } else {
            this.toolRscManager.updateTopicCollType(resourceMap.getString("collectionType.TeAra"));
        }
        if (isLinkWikipedia) {
            this.toolRscManager.updateLinkCollType(resourceMap.getString("collectionType.Wikipedia"));
        } else {
            this.toolRscManager.updateLinkCollType(resourceMap.getString("collectionType.TeAra"));
        }
        // ---------------------------------------------------------------------
        // 2) current Topics List
        for (String[] thisTopicSA : RunTopics) {
            topicFileIDsV.add(thisTopicSA[0].trim());
        }
        Collections.sort(topicFileIDsV);
        this.toolRscManager.updateTopicList(topicFileIDsV);
        // ---------------------------------------------------------------------
        // Get Topic ID & xmlFile Path --> record them into ToolResource XML
        String currTopicFilePath = "";
        String currTopicID = topicFileIDsV.elementAt(0);
//        if (isTopicWikipedia) {
            // current Topic
            String wikipediaTopicFileDir = resourceMap.getString("wikipedia.topics.folder")  + File.separator;
            currTopicFilePath = wikipediaTopicFileDir + currTopicID + ".xml";
//        } else {
//            // need to find out from TeAra Collection Folders
//            currTopicFilePath = this.toolRscManager.getTeAraCollectionFolder() + this.toolRscManager.getTeAraFilePathByName(currTopicID + ".xml");
//        }
        this.toolRscManager.updateCurrTopicFilePath(currTopicFilePath);
    }
}
