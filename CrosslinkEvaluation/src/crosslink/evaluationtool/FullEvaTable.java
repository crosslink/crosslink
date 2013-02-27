/*
 * FullEvaTable.java
 *
 *
 */
package crosslink.evaluationtool;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
/**
 * Created on 17 September 2007, 10:25
 * @author  Darren Huang
 */
public class FullEvaTable extends JDialog {
    
    JFrame etParent;
    private EvaTablePanel ep;
    
    /** Creates new form FullEvaTable */
    public FullEvaTable(JFrame parent) {
        super(parent, true);
        initComponents();
        this.etParent = parent;
        
        jButtonExportCSV.setVisible(false);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ridfilterPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        etrifilterTextField = new javax.swing.JTextField();
        this.etrifilterTextField.getDocument().addDocumentListener(
            new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    FullEvaTable.this.ep.newFilter(FullEvaTable.this.etrifilterTextField.getText().trim());
                }
                public void insertUpdate(DocumentEvent e) {
                    FullEvaTable.this.ep.newFilter(FullEvaTable.this.etrifilterTextField.getText().trim());
                }
                public void removeUpdate(DocumentEvent e) {
                    FullEvaTable.this.ep.newFilter(FullEvaTable.this.etrifilterTextField.getText().trim());
                }
            });
            fullevatablePanel = new javax.swing.JPanel();
            evatableHolder = new javax.swing.JScrollPane();
            eHolderPanel = new javax.swing.JPanel();
            etcloseButton = new javax.swing.JButton();
            etprintButton = new javax.swing.JButton();
            jButtonExportCSV = new javax.swing.JButton();
            jButton1 = new javax.swing.JButton();
            evatableMenuBar = new javax.swing.JMenuBar();
            evatableMenu = new javax.swing.JMenu();
            evatableMenuItemExit = new javax.swing.JMenuItem();

            setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

            jLabel1.setText("Filter:");

            javax.swing.GroupLayout ridfilterPanelLayout = new javax.swing.GroupLayout(ridfilterPanel);
            ridfilterPanel.setLayout(ridfilterPanelLayout);
            ridfilterPanelLayout.setHorizontalGroup(
                ridfilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ridfilterPanelLayout.createSequentialGroup()
                    .addGap(28, 28, 28)
                    .addComponent(jLabel1)
                    .addGap(13, 13, 13)
                    .addComponent(etrifilterTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 490, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(81, Short.MAX_VALUE))
            );
            ridfilterPanelLayout.setVerticalGroup(
                ridfilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ridfilterPanelLayout.createSequentialGroup()
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(ridfilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(etrifilterTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap())
            );

            eHolderPanel.setLayout(new javax.swing.BoxLayout(eHolderPanel, javax.swing.BoxLayout.LINE_AXIS));
            evatableHolder.setViewportView(eHolderPanel);

            etcloseButton.setText("Close");
            etcloseButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    etcloseButtonActionPerformed(evt);
                }
            });

            etprintButton.setText("Print");
            etprintButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    etprintButtonActionPerformed(evt);
                }
            });

            jButtonExportCSV.setText("Export to CSV");
            jButtonExportCSV.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButtonExportCSVActionPerformed(evt);
                }
            });

            jButton1.setText("Get Plots");
            jButton1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton1ActionPerformed(evt);
                }
            });

            javax.swing.GroupLayout fullevatablePanelLayout = new javax.swing.GroupLayout(fullevatablePanel);
            fullevatablePanel.setLayout(fullevatablePanelLayout);
            fullevatablePanelLayout.setHorizontalGroup(
                fullevatablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(evatableHolder, javax.swing.GroupLayout.DEFAULT_SIZE, 647, Short.MAX_VALUE)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, fullevatablePanelLayout.createSequentialGroup()
                    .addContainerGap(274, Short.MAX_VALUE)
                    .addComponent(jButton1)
                    .addGap(18, 18, 18)
                    .addComponent(jButtonExportCSV)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(etprintButton, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(etcloseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
            );
            fullevatablePanelLayout.setVerticalGroup(
                fullevatablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(fullevatablePanelLayout.createSequentialGroup()
                    .addComponent(evatableHolder, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(fullevatablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(etcloseButton)
                        .addComponent(etprintButton)
                        .addComponent(jButtonExportCSV)
                        .addComponent(jButton1))
                    .addContainerGap())
            );

            evatableMenu.setText("File");

            evatableMenuItemExit.setText("Item");
            evatableMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    evatableMenuItemExitActionPerformed(evt);
                }
            });
            evatableMenu.add(evatableMenuItemExit);

            evatableMenuBar.add(evatableMenu);

            setJMenuBar(evatableMenuBar);

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(fullevatablePanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(ridfilterPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(ridfilterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(fullevatablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap())
            );

            pack();
        }// </editor-fold>//GEN-END:initComponents
        
    private void jButtonExportCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExportCSVActionPerformed
        this.ep.exportEvaTableToCSV();
    }//GEN-LAST:event_jButtonExportCSVActionPerformed
    
    private void etprintButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_etprintButtonActionPerformed
        this.ep.printEvaluationTable();
    }//GEN-LAST:event_etprintButtonActionPerformed
    
    private void evatableMenuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_evatableMenuItemExitActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_evatableMenuItemExitActionPerformed
    
    private void etcloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_etcloseButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_etcloseButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        ((EvaluationUI3)etParent).createPlot();
    }//GEN-LAST:event_jButton1ActionPerformed
    
    void setTable(EvaTablePanel etp){
        this.eHolderPanel.add(etp);
        this.ep = etp;
        this.repaint();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FullEvaTable(new javax.swing.JFrame()).setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel eHolderPanel;
    private javax.swing.JButton etcloseButton;
    private javax.swing.JButton etprintButton;
    private javax.swing.JTextField etrifilterTextField;
    private javax.swing.JScrollPane evatableHolder;
    private javax.swing.JMenu evatableMenu;
    private javax.swing.JMenuBar evatableMenuBar;
    private javax.swing.JMenuItem evatableMenuItemExit;
    private javax.swing.JPanel fullevatablePanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonExportCSV;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel ridfilterPanel;
    // End of variables declaration//GEN-END:variables
    
}
