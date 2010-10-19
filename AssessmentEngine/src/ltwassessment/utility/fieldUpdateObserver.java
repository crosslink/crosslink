package ltwassessment.utility;

import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import javax.swing.JLabel;

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

    public void update(Observable ov, Object obj) {
        if (this.myObservableSingleton == ov) {
            Vector<String> tablblFieldSet = (Vector<String>) obj;
            renewTABFieldLabels(tablblFieldSet);
        }
    }

    private void renewTABFieldLabels(Vector<String> tablblFieldSet) {
//        if (tablblFieldSet.size() >= 1) {
//            this.lblTargetTitle.setText(tablblFieldSet.elementAt(3).toString());
//        }
//        if (tablblFieldSet.size() >= 2) {
//            this.lblAnchor.setText(tablblFieldSet.elementAt(2).toString());
//        }
        if (tablblFieldSet.size() == 6) {
            this.lblTopicTitle.setText(tablblFieldSet.elementAt(0).toString());
            this.lblTopicID.setText(tablblFieldSet.elementAt(1).toString());
            this.lblAnchor.setText(tablblFieldSet.elementAt(2).toString());
            this.lblTargetID.setText(tablblFieldSet.elementAt(3).toString());
            this.lblTargetTitle.setText(tablblFieldSet.elementAt(4).toString());
            this.lblCompletion.setText(tablblFieldSet.elementAt(5).toString());
        }
    }
}
