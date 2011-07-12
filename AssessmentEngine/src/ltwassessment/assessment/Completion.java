package ltwassessment.assessment;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;

public class Completion implements Observer {
	private JLabel lblCompletion = null;
    
    private static Completion instance = null;
    
    private int totalLinks = 0;
    private int finishedLinks = 0;
    
    public static Completion getInstance() {
    	if (instance == null)
    		instance = new Completion();
		return instance;
    }
    
//    public Completion(JLabel lblCompletion) {
//		super();
//		this.lblCompletion = lblCompletion;
//	}

	public Completion() {

	}

	@Override
	public void update(Observable o, Object arg) {
		Integer value = (Integer)arg;
		
		if (value == Bep.MARK_ASSESSED) {
			++finishedLinks;
		}
		else if (value == Bep.MARK_UNASSESSED) {
			--finishedLinks;
		}
		displayCompletion();
	}

	public int getTotalLinks() {
		return totalLinks;
	}

	public void setTotalLinks(int totalLinks) {
		this.totalLinks = totalLinks;
	}

	public JLabel getLblCompletion() {
		return lblCompletion;
	}

	public void setLblCompletion(JLabel lblCompletion) {
		this.lblCompletion = lblCompletion;
	}
   
	public void oneMoreLinkForAssessment() {
		++totalLinks;
	}
	
	public void displayCompletion() {
		if (lblCompletion != null)
			lblCompletion.setText(finishedLinks + " / " + totalLinks);		
	}
	
	public boolean isFinished() {
		return finishedLinks == totalLinks;
	}

	public void reset() {
	    totalLinks = 0;
	    finishedLinks = 0;	
	}
}
