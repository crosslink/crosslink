package ltwassessmenttool;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import ltwassessment.parsers.ResourcesManager;

import org.jdesktop.application.Action;

/**
 * LTWAssessmentToolCorpusBox.java
 * Created on 28/07/2009, 08:57:13
 * @author Darren Huang
 */
public class LTWAssessmentToolCorpusBox extends javax.swing.JDialog {

    ResourcesManager toolRscManager = null;
//    private JLabel jLableCollection = null;

    public LTWAssessmentToolCorpusBox(java.awt.Frame parent) {
        super(parent, true);
        initComponents();
        toolRscManager = ResourcesManager.getInstance();
        getRootPane().setDefaultButton(OkBtn);

        String currWikipediaCollDir = this.toolRscManager.getWikipediaCollectionFolder();
        if (!currWikipediaCollDir.equals("")) {
            this.wikiPathTxtField.setText(currWikipediaCollDir);
        }
//        String currTeAraCollDir = this.toolRscManager.getTeAraCollectionFolder();
//        if (!currTeAraCollDir.equals("")) {
//            this.taAraPathTxtField.setText(currTeAraCollDir);
//        }
    }

//    public void setJLableCollection(JLabel lableCollection) {
//		jLableCollection = lableCollection;
//	}

	@Action
    public void OKCorpusBox() {
        String myWikipediaPath = this.wikiPathTxtField.getText();
//        String myTeAraPath = this.taAraPathTxtField.getText();
        if (myWikipediaPath.equals("")/* && myTeAraPath.equals("")*/) {
            String notDirMsg = "Please specify Wikipedia Collection Directory.";
            JOptionPane.showMessageDialog(rootPane, notDirMsg);
        } /*else if (myWikipediaPath.equals("")) {
            String notDirMsg = "Please specify Wikipedia Collection Directory.";
            JOptionPane.showMessageDialog(rootPane, notDirMsg);
        } else if (myTeAraPath.equals("")) {
            String notDirMsg = "Please specify TeAra Collection Directory.";
            JOptionPane.showMessageDialog(rootPane, notDirMsg);
        } */else {
            File myWikipediaDir = new File(myWikipediaPath);
//            File myTeAraDir = new File(myTeAraPath);
            if (!myWikipediaDir.isDirectory() /*&& !myTeAraDir.isDirectory()*/) {
                String notDirMsg = "Wikipedia Collections location must be Directory.";
                JOptionPane.showMessageDialog(rootPane, notDirMsg);
            } else if (!myWikipediaDir.isDirectory()) {
                String notDirMsg = "Wikipedia Collections location must be a Directory.";
                JOptionPane.showMessageDialog(rootPane, notDirMsg);
            } /*else if (!myTeAraDir.isDirectory()) {
                String notDirMsg = "TeAra Collection location must be a Directory.";
                JOptionPane.showMessageDialog(rootPane, notDirMsg);
            } */else {
//            	jLableCollection.setText(myWikipediaPath);
                toolRscManager.updateWikipediaCollectionDirectory(myWikipediaPath);
//                toolRscManager.updateTeAraCollectionDirectory(myTeAraPath);
                dispose();
            }
        }
    }

    @Action
    public void CancelCorpusBox() {
        dispose();
        System.exit(-1);
    }

    // <editor-fold defaultstate="collapsed" desc="Collection Browse Buttons">
    String currentOpenDir = "C:\\";

    @Action
    public void browseWikiCorpus() {
        JFileChooser fc = new JFileChooser(currentOpenDir);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setMultiSelectionEnabled(false);

        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File thisDir = fc.getSelectedFile();
            String absFilePath = thisDir.getAbsolutePath();
            if (absFilePath.lastIndexOf(File.separator) >= 0) {
                currentOpenDir = absFilePath.substring(0, absFilePath.lastIndexOf(File.separator));
            } else if (absFilePath.lastIndexOf(File.separator) >= 0) {
                currentOpenDir = absFilePath.substring(0, absFilePath.lastIndexOf(File.separator));
            }
            if (thisDir.isDirectory() && thisDir.exists()) {
                // get & Display Directory Path
                this.wikiPathTxtField.setText(absFilePath);
            } else {
                String notDirMsg = "Please select the Top Directory of the Wikipedia Collections.";
                JOptionPane.showMessageDialog(this, notDirMsg);
            }
        }
    }

//    @Action
//    public void browseTeAraCorpus() {
//        JFileChooser fc = new JFileChooser(currentOpenDir);
//        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//        fc.setMultiSelectionEnabled(false);
//
//        int returnVal = fc.showOpenDialog(this);
//        if (returnVal == JFileChooser.APPROVE_OPTION) {
//            File thisDir = fc.getSelectedFile();
//            String absFilePath = thisDir.getAbsolutePath();
//            if (absFilePath.lastIndexOf(File.separator) >= 0) {
//                currentOpenDir = absFilePath.substring(0, absFilePath.lastIndexOf(File.separator));
//            } else if (absFilePath.lastIndexOf(File.separator) >= 0) {
//                currentOpenDir = absFilePath.substring(0, absFilePath.lastIndexOf(File.separator));
//            }
//            if (thisDir.isDirectory() && thisDir.exists()) {
//                // get & Display Directory Path
//                this.taAraPathTxtField.setText(absFilePath);
//            } else {
//                String notDirMsg = "Please select the Top Directory of the TeAra 2009 Collection.";
//                JOptionPane.showMessageDialog(this, notDirMsg);
//            }
//        }
//    }
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
        OkBtn = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        wikiPathTxtField = new javax.swing.JTextField();
        wikiBBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);
        setModalExclusionType(java.awt.Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        setName("Form"); // NOI18N

        imageLabel.setName("imageLabel"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ltwassessmenttool.LTWAssessmentToolApp.class).getContext().getActionMap(LTWAssessmentToolCorpusBox.class, this);
        OkBtn.setAction(actionMap.get("OKCorpusBox")); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ltwassessmenttool.LTWAssessmentToolApp.class).getContext().getResourceMap(LTWAssessmentToolCorpusBox.class);
        OkBtn.setText(resourceMap.getString("OkBtn.text")); // NOI18N
        OkBtn.setName("OkBtn"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        wikiPathTxtField.setText(resourceMap.getString("wikiPathTxtField.text")); // NOI18N
        wikiPathTxtField.setName("wikiPathTxtField"); // NOI18N

        wikiBBtn.setAction(actionMap.get("browseWikiCorpus")); // NOI18N
        wikiBBtn.setText(resourceMap.getString("wikiBBtn.text")); // NOI18N
        wikiBBtn.setName("wikiBBtn"); // NOI18N

        cancelBtn.setAction(actionMap.get("CancelCorpusBox")); // NOI18N
        cancelBtn.setText(resourceMap.getString("cancelBtn.text")); // NOI18N
        cancelBtn.setName("cancelBtn"); // NOI18N

        jLabel4.setFont(resourceMap.getFont("jLabel4.font")); // NOI18N
        jLabel4.setForeground(resourceMap.getColor("jLabel4.foreground")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel5.setFont(resourceMap.getFont("jLabel5.font")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(imageLabel)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(wikiPathTxtField)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(wikiBBtn))
                        .addGroup(layout.createSequentialGroup()
                            .addGap(6, 6, 6)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(OkBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel5)))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                            .addComponent(cancelBtn)))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(imageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 283, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 44, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(wikiPathTxtField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(wikiBBtn))
                .addGap(60, 60, 60)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(OkBtn, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(cancelBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton OkBtn;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JButton wikiBBtn;
    private javax.swing.JTextField wikiPathTxtField;
    // End of variables declaration//GEN-END:variables
}