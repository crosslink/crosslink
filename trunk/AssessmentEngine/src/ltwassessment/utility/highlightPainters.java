package ltwassessment.utility;

import java.awt.Color;
import javax.swing.text.DefaultHighlighter;

/**
 * @author Darren HUANG
 */
public class highlightPainters {

    private DefaultHighlighter.DefaultHighlightPainter anchorPainter;
    private DefaultHighlighter.DefaultHighlightPainter ongoingPainter;
    private DefaultHighlighter.DefaultHighlightPainter currentPainter;
    private DefaultHighlighter.DefaultHighlightPainter relPainter;
    private DefaultHighlighter.DefaultHighlightPainter irrelPainter;

    public highlightPainters() {
        // Blue: Anchor Text and have not been assessed
        anchorPainter = new DefaultHighlighter.DefaultHighlightPainter(new Color(150, 215, 215));
        // Light Blue: have been assessed but not completed
        ongoingPainter = new DefaultHighlighter.DefaultHighlightPainter(new Color(0, 255, 255));
        // Yellow: current selected / asseeing Anchor
        currentPainter = new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 232, 0));
        // Green: completed and full relevant
        relPainter = new DefaultHighlighter.DefaultHighlightPainter(new Color(180, 218, 180));
        // Red: completed and full irrelevant
        irrelPainter = new DefaultHighlighter.DefaultHighlightPainter(new Color(224, 184, 224));
    }

    public DefaultHighlighter.DefaultHighlightPainter getAnchorPainter(){
        return anchorPainter;
    }
    public DefaultHighlighter.DefaultHighlightPainter getOngoingPainter(){
        return ongoingPainter;
    }
    public DefaultHighlighter.DefaultHighlightPainter getSelectedPainter(){
        return currentPainter;
    }
    public DefaultHighlighter.DefaultHighlightPainter getRelevantPainter(){
        return relPainter;
    }
    public DefaultHighlighter.DefaultHighlightPainter getIrrelevantPainter(){
        return irrelPainter;
    }
}
