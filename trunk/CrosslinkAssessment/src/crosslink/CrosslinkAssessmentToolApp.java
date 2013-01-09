/*
 * CrosslinkAssessmentToolApp.java
 */

package crosslink;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;


import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

import crosslink.Assessment;

/**
 * The main class of the application.
 */
public class CrosslinkAssessmentToolApp extends SingleFrameApplication {
	
	private CrosslinkAssessmentToolCorpusBox corpusBox = null;
	
	class MyWindowListener implements WindowListener {

		  public void windowClosing(WindowEvent arg0) {
			    System.exit(0);
		  }

		  public void windowOpened(WindowEvent arg0) {
		  }

		  public void windowClosed(WindowEvent arg0) {
		  }

		  public void windowIconified(WindowEvent arg0) {
		  }

		  public void windowDeiconified(WindowEvent arg0) {
		  }

		  public void windowActivated(WindowEvent arg0) {
		  }

		  public void windowDeactivated(WindowEvent arg0) {
		  }

	}

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
//        JFrame mainFrame = CrosslinkAssessmentToolApp.getApplication().getMainFrame();
//        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        mainFrame.addWindowListener(new MyWindowListener());
    	try {
        show(new CrosslinkAssessmentToolView(this));
    	}
    	catch (Exception ex) {
    		ex.printStackTrace();
    	}
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
     * @return the instance of CrosslinkAssessmentToolApp
     */
    public static CrosslinkAssessmentToolApp getApplication() {
        return Application.getInstance(CrosslinkAssessmentToolApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
    	if (args.length > 0 && args[0].equalsIgnoreCase("run"))
    		Assessment.getInstance().setAssessmentType(true);
    	Assessment.getInstance().initialize();
        launch(CrosslinkAssessmentToolApp.class, args);
    }
    
    public void showCorpusBox() {
        if (corpusBox == null) {
            JFrame mainFrame = CrosslinkAssessmentToolApp.getApplication().getMainFrame();
            
            corpusBox = new CrosslinkAssessmentToolCorpusBox(mainFrame);
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
//            ((CrosslinkAssessmentToolCorpusBox) corpusBox).setJLableCollection(this.jLabelCollection);
        }
        corpusBox.setVisible(true);
        //CrosslinkAssessmentToolApp.getApplication().show(corpusBox);
    }

	@Override
	protected void shutdown() {
		super.shutdown();
	}

	@Override
	public void quit(ActionEvent arg0) {
		// TODO Auto-generated method stub
		super.quit(arg0);
	}
}