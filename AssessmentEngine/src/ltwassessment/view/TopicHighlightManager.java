package ltwassessment.view;

import java.util.Vector;

import javax.swing.JTextPane;
import javax.swing.text.Highlighter;

import ltwassessment.assessment.AssessedAnchor;
import ltwassessment.assessment.IndexedAnchor;

public class TopicHighlightManager {
	private Highlighter txtPaneHighlighter = null;
	private JTextPane pane = null;
	
	private static TopicHighlightManager instance = null;
	
	public static TopicHighlightManager getInstance() {
		if (instance == null)
			instance = new TopicHighlightManager();
		return instance;
	}

	public void initializeHighlighter(Vector<IndexedAnchor> anchors) {
		
	}
	
	public void update(AssessedAnchor preAnchorSEStatus, AssessedAnchor currAnchorSE) {
		
	}
}
