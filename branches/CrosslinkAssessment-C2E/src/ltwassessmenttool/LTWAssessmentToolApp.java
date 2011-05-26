/*
 * LTWAssessmentToolApp.java
 */

package ltwassessmenttool;

import java.awt.Dialog.ModalityType;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class LTWAssessmentToolApp extends SingleFrameApplication {
	
	private LTWAssessmentToolCorpusBox corpusBox = null;

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        show(new LTWAssessmentToolView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of LTWAssessmentToolApp
     */
    public static LTWAssessmentToolApp getApplication() {
        return Application.getInstance(LTWAssessmentToolApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(LTWAssessmentToolApp.class, args);
    }
    
    public void showCorpusBox() {
        if (corpusBox == null) {
            JFrame mainFrame = LTWAssessmentToolApp.getApplication().getMainFrame();
            corpusBox = new LTWAssessmentToolCorpusBox(mainFrame);
            //corpusBox.setModal(true);
//            corpusBox.setModalityType(ModalityType.APPLICATION_MODAL);
//            JOptionPane optionPane = new JOptionPane(
//                    "The only way to close this dialog is by\n"
//                        + "pressing one of the following buttons.\n"
//                        + "Do you understand?", JOptionPane.QUESTION_MESSAGE,
//                    JOptionPane.YES_NO_OPTION);
//            corpusBox.setContentPane(optionPane);
            corpusBox.setLocationRelativeTo(mainFrame);
            corpusBox.pack();
//            ((LTWAssessmentToolCorpusBox) corpusBox).setJLableCollection(this.jLabelCollection);
        }
        corpusBox.setVisible(true);
        //LTWAssessmentToolApp.getApplication().show(corpusBox);
    }
}
