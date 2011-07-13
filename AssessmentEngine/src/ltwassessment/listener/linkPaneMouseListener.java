package ltwassessment.listener;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JTextPane;
import javax.swing.event.MouseInputListener;
import ltwassessment.assessment.AssessmentThread;
import ltwassessment.utility.highlightPainters;

/**
 * @author Darren HUANG
 */
public class linkPaneMouseListener implements MouseInputListener {


    // Declare global variables
    protected final int bepLength = 4;
    private final String sysPropertyKey = "isTABKey";

    private boolean isTAB = false;
    highlightPainters painters;
    private JTextPane myTopicPane;
    private JTextPane myLinkPane;
  
    public linkPaneMouseListener(JTextPane myTopicPane, JTextPane myLinkPane) {
        this.myTopicPane = myTopicPane;
        this.myLinkPane = myLinkPane;

    }

    // <editor-fold defaultstate="collapsed" desc="Mouse Events">
    public void mouseClicked(MouseEvent me) {
        /**
         * CASE 1: Left-Click -> RELEVANT + NEXT
         * CASE 2: Double-Left-Click -> Change BEP Position
         * CASE 3: Right-Click: IRRELEVANT + NEXT
         */
        this.isTAB = Boolean.valueOf(System.getProperty(sysPropertyKey));
        activateMouseClickEvent(me);
    }

    public void mousePressed(MouseEvent e) {
//        if (SINGLECLICK) {
//            SINGLECLICK = false;
////            singleLeftClickEventAction();
//            AssessmentThread.setTask(AssessmentThread.EVENT_SET_RELEVANT);
//            IGNORE = false;
//        }
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Right/Left Mouse Events: Next/Previous">
    private Timer timer;
    private boolean IGNORE = false;
    private boolean SINGLECLICK = false;

    private void activateMouseClickEvent(MouseEvent mce) {
    	if (!AssessmentThread.isReadyForNextLink()) {
    		return;
    	}
    	
    	AssessmentThread.setCtrlKeyDown(mce.isControlDown());
        int noOfClicks = mce.getClickCount();
        int button = mce.getButton();
//        timer = new Timer();
        if (noOfClicks > 2) {
            return;
        } else if (noOfClicks == 2) {
            if (button == 1) {
                // ------ Double-Left-Click Event: Specify a BEP ---------------
//                IGNORE = true; // to stop single click from running
                // -------------------------------------------------------------
                // 1) get the NEW BEP Start Point & Offset after Double-Click
                // 2) Update Link BEP: Add NEW + Remove OLD
                // 2) BG --> Green
                // 3) Update bepLink: SP, Offset, Status-Rel
                // 4) STAY, NOT Go Next
                //doubleLeftClickEventAction();
                AssessmentThread.setTask(AssessmentThread.EVENT_SET_BEP);
                Thread.yield();
            }
        } else if (noOfClicks == 1) {
            if (button == 1) {
                // ----------- Single-Left-Click Event: Go NEXT --------
//                IGNORE = false;
//                timer.schedule(new timerTask(), 500);
                AssessmentThread.setTask(AssessmentThread.EVENT_SET_RELEVANT);
            } else if (button == 3) {
                // --- Single-Right-Click Event: Irrelevance + Go NEXT -
                // Switch to "IRRELEVANT"
                // 1) BG --> Red
                // 2) Update bepLink: SP, Offset, Status-Rel
                // 3) GO NEXT
                //singleRightClickEventAction();
            	AssessmentThread.setTask(AssessmentThread.EVENT_SET_IRRELEVANT);
            }
        }
    }


    private class timerTask extends TimerTask {

        @Override
        public void run() {
            if (IGNORE) {
                timer.cancel();
                IGNORE = false;
                return;
            }
            IGNORE = true;
            SINGLECLICK = true;
            try {
                Robot robot = new Robot();
                robot.mousePress(InputEvent.BUTTON2_MASK);
                robot.mouseRelease(InputEvent.BUTTON2_MASK);
            } catch (AWTException e) {
                e.printStackTrace();
            }
            timer.cancel();
        }
    }
// </editor-fold>
// </editor-fold>



}
