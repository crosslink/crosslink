/*
 * RunTablePanel.java
 */
package org.inex.ltw.evaluationtool;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Created on September 15, 2007, 2:39 PM
 * @author  Darren Huang
 */
public class RunTablePanel extends JPanel {
    
    private TableRowSorter<DefaultTableModel> sorter;
    
    /** Creates new form RunTablePanel */
    public RunTablePanel() {
        initComponents();
        this.uploadFileToTable(new Object[][]{});
    }
    
    public RunTablePanel(RunTablePanel copy){
        this();
        this.runsTable.setModel(copy.runsTable.getModel());
        this.sorter = new TableRowSorter<DefaultTableModel>((DefaultTableModel)this.runsTable.getModel());
        this.runsTable.setRowSorter(this.sorter);
    }
    
    /** This method is called from within the constructor to
     *  initialize the form.
     *  WARNING: Do NOT modify this code. The content of this method is
     *  always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        rtpPopupMenu = new javax.swing.JPopupMenu();
        rtpMenuItemDelete = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        runsTable = new javax.swing.JTable();
        this.runsTable.addMouseListener(new PopupListener());

        rtpMenuItemDelete.setText("Delete");
        rtpMenuItemDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rtpMenuItemDeleteActionPerformed(evt);
            }
        });

        rtpPopupMenu.add(rtpMenuItemDelete);

        setMinimumSize(new java.awt.Dimension(200, 200));
        runsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "", "", "", "", "", "", "", ""
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        runsTable.setFillsViewportHeight(true);
        runsTable.setSelectionBackground(new java.awt.Color(102, 255, 102));
        jScrollPane1.setViewportView(runsTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void rtpMenuItemDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rtpMenuItemDeleteActionPerformed
        int[] selectedRows = this.runsTable.getSelectedRows();
        if(selectedRows.length!=0){
            javax.swing.table.DefaultTableModel theModel = (javax.swing.table.DefaultTableModel)this.runsTable.getModel();
            int reduceCount = 0;
            for (int i=0;i<selectedRows.length;i++) {
                theModel.removeRow(this.runsTable.convertRowIndexToModel(selectedRows[i]-reduceCount));
                reduceCount++;  // the row number will keep changing when each row is deleted
            }
        }
    }//GEN-LAST:event_rtpMenuItemDeleteActionPerformed
    
    void uploadFileToTable(Object[][] runData){
        TableModel model = RunTablePanel.createCustomTableModel(runData);
        this.runsTable.setModel(model);
        this.repaint();
    }
    
    void newFilter(String reg) {
        RowFilter<DefaultTableModel, Object> rf = null;
        // If current expression doesn't parse, don't update.
        try {
            String firstCol = this.runsTable.getColumnModel().getColumn(0).getHeaderValue().toString();
            if (firstCol.equalsIgnoreCase("Participant ID")) {
                rf = RowFilter.regexFilter(reg, 0); // Participant ID
            } else if (firstCol.equalsIgnoreCase("Run ID")) {
                rf = RowFilter.regexFilter(reg, 1); // Run ID
            } else if (firstCol.equalsIgnoreCase("Topic")) {
                rf = RowFilter.regexFilter(reg, 2); // Topic
            } else if (firstCol.equalsIgnoreCase("Name")) {
                rf = RowFilter.regexFilter(reg, 3); // Name
            } else {
                rf = RowFilter.regexFilter(reg, 5); // Participant ID
            }
            
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        sorter.setRowFilter(rf);
    }
    
    void cleanAllRunTable(){
        Object[][] runData = {{"", "", "", "", "", "", "", ""}};
        this.uploadFileToTable(runData);
    }
    
    void printRunTable() {
        MessageFormat header = new MessageFormat("Page {0,number,integer}");
        try {
            this.runsTable.print(JTable.PrintMode.FIT_WIDTH, header, null);
        } catch (PrinterException ex) {
            ex.printStackTrace();
        }
    }
    
    void exportRunTableToCSV() {
        try {
            String fileName = "RunTable_" + this.getNow("MMddmmss") + ".CSV";
            File csvFile = new File(fileName);
            String[] columnTitle = new String[this.runsTable.getColumnCount()];
            for (int i=0; i<this.runsTable.getColumnCount(); i++) {
                columnTitle[i] = this.runsTable.getColumnModel().getColumn(i).getHeaderValue().toString();
            }
            String rowdata;
            BufferedWriter bw = new BufferedWriter(new FileWriter(csvFile, true));
            PrintWriter pw = new PrintWriter(bw);
            for (int j=0; j<this.runsTable.getColumnCount(); j++) {
                pw.print(columnTitle[j] + ",");
            }
            pw.println("");
            for (int k=0; k<this.runsTable.getRowCount(); k++) {
                for (int l=0; l<this.runsTable.getColumnCount(); l++) {
                    rowdata = this.runsTable.getValueAt(k, l).toString();
                    pw.print(rowdata + ",");
                }
                pw.println("");
            }
            pw.close();
            
        } catch (Exception E) {
            System.out.println("Error While Exporting Run Table "+E);
        }
    }
    
    private String getNow(String dateFormat) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());
    }
    
    private final static DefaultTableModel createCustomTableModel(Object[][] runData){
        
        String[] columnNames = {"Participant ID",
        "Run ID",
        "Topic",
        "Name",
        "# of Outgoing",
        "Outgoing Result",
        "# of Incoming",
        "Incoming Result"
        };
        
        return new DefaultTableModel(runData, columnNames){
            
            /*
             * JTable uses this method to determine the default renderer/
             * editor for each cell.  If we didn't implement this method,
             * then the last column would contain text ("true"/"false"),
             * rather than a check box.
             */
            @Override
            public Class getColumnClass(int c) {
                return getValueAt(0, c).getClass();
            }
        };
        
    }
    
    private final class PopupListener extends MouseAdapter {
        
        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                if(RunTablePanel.this.runsTable.getSelectedRow()!=-1){
                    RunTablePanel.this.rtpPopupMenu.show(e.getComponent(),e.getX(), e.getY());
                }
            }
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JMenuItem rtpMenuItemDelete;
    private javax.swing.JPopupMenu rtpPopupMenu;
    private javax.swing.JTable runsTable;
    // End of variables declaration//GEN-END:variables
    
}
