package ltwassessmenttool.listener;

import java.awt.Rectangle;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;

import ltwassessment.parsers.FOLTXTMatcher;

/**
 * @author Darren HUANG
 */
public class CaretListenerLabel implements CaretListener {

    private String newline = "\n";
    private JTextPane textPane;
    private JLabel statusLabel;
    private FOLTXTMatcher myMatcher;

    public CaretListenerLabel(String text, JTextPane textpane, JLabel label) {
        this.textPane = textpane;
//        this.textPane = (JTextPane)
        this.statusLabel = label;

        this.myMatcher = new FOLTXTMatcher();
    }

    public void caretUpdate(CaretEvent e) {
        displaySelectionInfo(e.getDot(), e.getMark());
    }

    private void displaySelectionInfo(final int dot, final int mark) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
//                try {
                    if (dot == mark) {
                        // no selection
//                        Rectangle caretCoords = textPane.modelToView(dot);
                        // convert it to View Coordinates
//                        statusLabel.setText("caret: text position: " + dot + ", view location = [" + caretCoords.x + ", " + caretCoords.y + "]" + newline);
                    } else if (dot < mark) {
//                        String selectedText = textPane.getText(dot, mark - dot);
//                        String srTxtTilMark = textPane.getText(0, mark);
//                        String[] xmlOLSA = myMatcher.getXMLOffsetLength(dot, mark - dot, srTxtTilMark);
//                        statusLabel.setText("selection from: " + dot + " to " + mark + " -> " + selectedText + "; TXT Offset: " + xmlOLSA[0] + ", Length: " + xmlOLSA[1] + ", -> " + xmlOLSA[2]);
                    } else if (dot > mark) {
//                        String selectedText = textPane.getText(mark, dot - mark);
//                        String srTxtTilDot = textPane.getText(0, dot);
//                        String[] xmlOLSA = myMatcher.getXMLOffsetLength(mark, dot - mark, srTxtTilDot);
//                        statusLabel.setText("selection from: " + mark + " to " + dot + " -> " + selectedText + "; TXT Offset: " + xmlOLSA[0] + ", Length: " + xmlOLSA[1] + ", -> " + xmlOLSA[2]);
                    }
//                } catch (BadLocationException ex) {
//                    statusLabel.setText("caret event error: " + ex.getMessage());
//                }
            }
        });
    }
}
