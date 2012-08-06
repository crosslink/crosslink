package crosslink.utility;

import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import javax.swing.JLabel;

import crosslink.Assessment;
import crosslink.assessment.Bep;
import crosslink.parsers.ResourcesManager;


import crosslink.utility.ObservableSingleton;

/**
 * @author Darren HUANG
 */
public class fieldUpdateObserver implements Observer {

    private ObservableSingleton myObservableSingleton = null;
    private JLabel lblTopicTitle;
    private JLabel lblTopicID;
    private JLabel lblAnchor;
    private JLabel lblSubanchor;
    private JLabel lblTargetID;
    private JLabel lblTargetTitle;
//    private JLabel lblCompletion;

    public fieldUpdateObserver(ObservableSingleton thisOS, JLabel thisTopicTitle, JLabel thisTopicID, JLabel thisAnchor, JLabel thisSubanchor,
            JLabel thisTargetID, JLabel thisTargetTitle/*, JLabel thisCompletion*/) {
        this.myObservableSingleton = thisOS;
        this.lblTopicTitle = thisTopicTitle;
        this.lblTopicID = thisTopicID;
        this.lblAnchor = thisAnchor;
        this.lblSubanchor = thisSubanchor; 
        this.lblTargetID = thisTargetID;
        this.lblTargetTitle = thisTargetTitle;
//        this.lblCompletion = thisCompletion;
    }
    
    public fieldUpdateObserver(ObservableSingleton thisOS, JLabel thisTopicTitle, JLabel thisTopicID, JLabel thisAnchor, JLabel thisSubanchor, JLabel thisTargetPageID) {
        this.myObservableSingleton = thisOS;
        this.lblTopicTitle = thisTopicTitle;
        this.lblTopicID = thisTopicID;
        this.lblAnchor = thisAnchor;
        this.lblSubanchor = thisSubanchor; 
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
//        String[] outCompletionRatio = ResourcesManager.getInstance().getOutgoingCompletion();
        
        this.lblTopicTitle.setText(Assessment.getInstance().getCurrentTopic().getTitle());
        this.lblTopicID.setText(Assessment.getInstance().getCurrentTopic().getId());
        
        
        this.lblAnchor.setText(bep.getAssociatedAnchor().getParent().getName());
        this.lblSubanchor.setText(bep.getAssociatedAnchor().getName());
        this.lblTargetID.setText(bep.getFileId());
        this.lblTargetTitle.setText(bep.getTargetTitle());
//            this.lblCompletion.setText(tablblFieldSet.elementAt(5).toString());
//        this.lblCompletion.setText(outCompletionRatio[0] + " / " + outCompletionRatio[1]);
//        }
    }
}
