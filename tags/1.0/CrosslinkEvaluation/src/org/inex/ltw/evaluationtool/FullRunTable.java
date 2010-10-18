/*
 * FullRunTable.java
 *
 * This JDialog is used to display the full runs table
 * It allows users to manipulate the Run Table,
 * e.g. Sort, Print, column re-arrangement...
 */
package org.inex.ltw.evaluationtool;

import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Created on September 15, 2007, 12:45 PM
 * @author  Darren Huang
 */
public class FullRunTable extends JDialog {
    
    JFrame rtParent;
    private RunTablePanel rp;
    
    /** Creates new form FullRunTable */
    public FullRunTable(JFrame parent) {
        super(parent, true);
        initComponents();
        this.rtParent = parent;
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        fullruntablePanel = new javax.swing.JPanel();
        rtcloseButton = new javax.swing.JButton();
        rtprintButton = new javax.swing.JButton();
        runtableHolder = new javax.swing.JScrollPane();
        test = new javax.swing.JPanel();
        jButtonExportCSV = new javax.swing.JButton();
        pidfilterPanel = new javax.swing.JPanel();
        rtpidfilterTextField = new javax.swing.JTextField();
        this.rtpidfilterTextField.getDocument().addDocumentListener(
            new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    FullRunTable.this.rp.newFilter(FullRunTable.this.rtpidfilterTextField.getText().trim());
                }
                public void insertUpdate(DocumentEvent e) {
                    FullRunTable.this.rp.newFilter(FullRunTable.this.rtpidfilterTextField.getText().trim());
                }
                public void removeUpdate(DocumentEvent e) {
                    FullRunTable.this.rp.newFilter(FullRunTable.this.rtpidfilterTextField.getText().trim());
                }
            });
            pidfilterLabel = new javax.swing.JLabel();
            runtableMenuBar = new javax.swing.JMenuBar();
            runtableMenu = new javax.swing.JMenu();
            runtableMenuItemExit = new javax.swing.JMenuItem();

            setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            setTitle("Full Run Table");
            rtcloseButton.setText("Close");
            rtcloseButton.setMaximumSize(new java.awt.Dimension(55, 23));
            rtcloseButton.setMinimumSize(new java.awt.Dimension(55, 23));
            rtcloseButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    rtcloseButtonActionPerformed(evt);
                }
            });

            rtprintButton.setText("Print");
            rtprintButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    rtprintButtonActionPerformed(evt);
                }
            });

            test.setLayout(new javax.swing.BoxLayout(test, javax.swing.BoxLayout.X_AXIS));

            runtableHolder.setViewportView(test);

            jButtonExportCSV.setText("Export to CSV");
            jButtonExportCSV.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButtonExportCSVActionPerformed(evt);
                }
            });

            javax.swing.GroupLayout fullruntablePanelLayout = new javax.swing.GroupLayout(fullruntablePanel);
            fullruntablePanel.setLayout(fullruntablePanelLayout);
            fullruntablePanelLayout.setHorizontalGroup(
                fullruntablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, fullruntablePanelLayout.createSequentialGroup()
                    .addContainerGap(345, Short.MAX_VALUE)
                    .addComponent(jButtonExportCSV)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(rtprintButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(rtcloseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
                .addComponent(runtableHolder, javax.swing.GroupLayout.DEFAULT_SIZE, 621, Short.MAX_VALUE)
            );
            fullruntablePanelLayout.setVerticalGroup(
                fullruntablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, fullruntablePanelLayout.createSequentialGroup()
                    .addComponent(runtableHolder, javax.swing.GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(fullruntablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(rtprintButton)
                        .addComponent(rtcloseButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonExportCSV))
                    .addContainerGap())
            );

            pidfilterLabel.setText("Filter: ");

            javax.swing.GroupLayout pidfilterPanelLayout = new javax.swing.GroupLayout(pidfilterPanel);
            pidfilterPanel.setLayout(pidfilterPanelLayout);
            pidfilterPanelLayout.setHorizontalGroup(
                pidfilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pidfilterPanelLayout.createSequentialGroup()
                    .addGap(36, 36, 36)
                    .addComponent(pidfilterLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(rtpidfilterTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 493, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(57, Short.MAX_VALUE))
            );
            pidfilterPanelLayout.setVerticalGroup(
                pidfilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pidfilterPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(pidfilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(pidfilterLabel)
                        .addComponent(rtpidfilterTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );

            runtableMenu.setText("File");
            runtableMenuItemExit.setText("Exit");
            runtableMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    runtableMenuItemExitActionPerformed(evt);
                }
            });

            runtableMenu.add(runtableMenuItemExit);

            runtableMenuBar.add(runtableMenu);

            setJMenuBar(runtableMenuBar);

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(fullruntablePanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(pidfilterPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(pidfilterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(fullruntablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap())
            );
            pack();
        }// </editor-fold>//GEN-END:initComponents
        
    private void jButtonExportCSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExportCSVActionPerformed
        this.rp.exportRunTableToCSV();
    }//GEN-LAST:event_jButtonExportCSVActionPerformed
    
    private void rtprintButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rtprintButtonActionPerformed
        this.rp.printRunTable();
    }//GEN-LAST:event_rtprintButtonActionPerformed
    
    private void rtcloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rtcloseButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_rtcloseButtonActionPerformed
    
    private void runtableMenuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runtableMenuItemExitActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_runtableMenuItemExitActionPerformed
    
    void setTable(RunTablePanel tp){
        this.test.add(tp);
        this.rp = tp;
        this.repaint();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel fullruntablePanel;
    private javax.swing.JButton jButtonExportCSV;
    private javax.swing.JLabel pidfilterLabel;
    private javax.swing.JPanel pidfilterPanel;
    private javax.swing.JButton rtcloseButton;
    private javax.swing.JTextField rtpidfilterTextField;
    private javax.swing.JButton rtprintButton;
    private javax.swing.JScrollPane runtableHolder;
    private javax.swing.JMenu runtableMenu;
    private javax.swing.JMenuBar runtableMenuBar;
    private javax.swing.JMenuItem runtableMenuItemExit;
    private javax.swing.JPanel test;
    // End of variables declaration//GEN-END:variables
    
}
