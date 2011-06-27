package ltwassessment.utility;

import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import javax.swing.JLabel;

import ltwassessment.Assessment;
import ltwassessment.assessment.Bep;

import ltwassessment.parsers.ResourcesManager;

/**
 * @author Darren HUANG
 */
public class fieldUpdateObserver implements Observer {

    private ObservableSingleton myObservableSingleton = null;
    private JLabel lblTopicTitle;
    private JLabel lblTopicID;
    private JLabel lblAnchor;
    private JLabel lblTargetID;
    private JLabel lblTargetTitle;
    private JLabel lblCompletion;

    public fieldUpdateObserver(ObservableSingleton thisOS, JLabel thisTopicTitle, JLabel thisTopicID, JLabel thisAnchor, 
            JLabel thisTargetID, JLabel thisTargetTitle, JLabel thisCompletion) {
        this.myObservableSingleton = thisOS;
        this.lblTopicTitle = thisTopicTitle;
        this.lblTopicID = thisTopicID;
        this.lblAnchor = thisAnchor;
        this.lblTargetID = thisTargetID;
        this.lblTargetTitle = thisTargetTitle;
        this.lblCompletion = thisCompletion;
    }
    
    public fieldUpdateObserver(ObservableSingleton thisOS, JLabel thisTopicTitle, JLabel thisTopicID, JLabel thisAnchor, JLabel thisTargetPageID) {
        this.myObservableSingleton = thisOS;
        this.lblTopicTitle = thisTopicTitle;
        this.lblTopicID = thisTopicID;
        this.lblAnchor = thisAnchor;
        this.lblTargetID = thisTargetPageID;
    }

    public void update(Observable ov, Object obj) {
        if (this.myObservableSingleton == ov) {
            Bep tablblFieldSet = (Bep) obj;
            renewTABFieldLabels(tablblFieldSet);
        }
    }

    private void renewTABFieldLabels(Bep bep) {
//        if (tablblFieldSet.size() >= 1) {
//            this.lblTargetTitle.setText(tablblFieldSet.elementAt(3).toString());
//        }
//        if (tablblFieldSet.size() >= 2) {
//            this.lblAnchor.setText(tablblFieldSet.elementAt(2).toString());
//        }
//        if (tablblFieldSet.size() == 6) {
        String[] outCompletionRatio = ResourcesManager.getInstance().getOutgoingCompletion();
        
        this.lblTopicTitle.setText(Assessment.getInstance().getCurrentTopic().getTitle());
        this.lblTopicID.setText(Assessment.getInstance().getCurrentTopic().getId());
        
        
        this.lblAnchor.setText(bep.getAssociatedAnchor().getName());
        this.lblTargetID.setText(bep.getFileId());
        this.lblTargetTitle.setText(bep.getTargetTitle());
//            this.lblCompletion.setText(tablblFieldSet.elementAt(5).toString());
        this.lblTargetTitle.setText(outCompletionRatio + " / " + outCompletionRatio[1]);
//        }
    }
}
