package ltwassessment.view;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;

import ltwassessment.assessment.AssessedAnchor;
import ltwassessment.assessment.IndexedAnchor;
import ltwassessment.utility.highlightPainters;

public class TopicHighlightManager {
	private Highlighter txtPaneHighlighter = null;
	private JTextPane pane = null;
    private static highlightPainters painters = new highlightPainters();
	
	private static TopicHighlightManager instance = null;
	
	public static TopicHighlightManager getInstance() {
		if (instance == null)
			instance = new TopicHighlightManager();
		return instance;
	}

	public JTextPane getPane() {
		return pane;
	}

	public void setPane(JTextPane pane) {
		this.pane = pane;
		
		txtPaneHighlighter = pane.getHighlighter();
	}

	public void initializeHighlighter(Vector<IndexedAnchor> anchors) {
//    	txtPaneHighlighter.removeAllHighlights();

        for (IndexedAnchor anchor : anchors)
        	anchor.setHighlighter(txtPaneHighlighter, painters);
	}
	
	public void update(AssessedAnchor preAnchor, AssessedAnchor currAnchor) {
		if (preAnchor != null) {
			preAnchor.setHighlighter(txtPaneHighlighter, painters);
		}
		
		currAnchor.setToCurrentAnchor(txtPaneHighlighter, painters, currAnchor);
		
        pane.getCaret().setDot(currAnchor.getScreenPosEnd());
        pane.scrollRectToVisible(pane.getVisibleRect());
        pane.repaint();
	}
}
