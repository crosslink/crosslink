package crosslink.utility;

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

    public static final Color COLOR_NOT_ASSESSED = new Color(150, 215, 215);
    public static final Color COLOR_INCOMPLETE = new Color(0, 255, 255);
    public static final Color COLOR_SELECTED = new Color(255, 232, 0);
    public static final Color COLOR_RELEVENT = new Color(180, 218, 180);
    public static final Color COLOR_IRREVENT = new Color(224, 184, 224);
    
    public highlightPainters() {
        // Blue: Anchor Text and have not been assessed
        anchorPainter = new DefaultHighlighter.DefaultHighlightPainter(COLOR_NOT_ASSESSED);
     
        // Light Blue: have been assessed but not completed
        ongoingPainter = new DefaultHighlighter.DefaultHighlightPainter(COLOR_INCOMPLETE);
        
        // Yellow: current selected / asseeing Anchor
        currentPainter = new DefaultHighlighter.DefaultHighlightPainter(COLOR_SELECTED);
        
        // Green: completed and full relevant
        relPainter = new DefaultHighlighter.DefaultHighlightPainter(COLOR_RELEVENT);
        
        // Red: completed and full irrelevant
        irrelPainter = new DefaultHighlighter.DefaultHighlightPainter(COLOR_IRREVENT);
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
    public DefaultHighlighter.DefaultHighlightPainter getCompletePainter(){
        return relPainter;
    }
    public DefaultHighlighter.DefaultHighlightPainter getIrrelevantPainter(){
        return irrelPainter;
    }
}
