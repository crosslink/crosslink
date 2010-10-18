package ltwassessment.utility;

import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import javax.swing.JLabel;

import ltwassessmenttool.utility.ObservableSingleton;

/**
 * @author Darren HUANG
 */
public class fieldUpdateObserver implements Observer {

    private ObservableSingleton myObservableSingleton = null;
    private JLabel lblTopicTitle;
    private JLabel lblTopicID;
    private JLabel lblAnchor;
    private JLabel lblTargetPage;

    public fieldUpdateObserver(ObservableSingleton thisOS, JLabel thisTopicTitle, JLabel thisTopicID, JLabel thisAnchor, JLabel thisTargetPageID) {
        this.myObservableSingleton = thisOS;
        this.lblTopicTitle = thisTopicTitle;
        this.lblTopicID = thisTopicID;
        this.lblAnchor = thisAnchor;
        this.lblTargetPage = thisTargetPageID;
    }

    public void update(Observable ov, Object obj) {
        if (this.myObservableSingleton == ov) {
            Vector<String> tablblFieldSet = (Vector<String>) obj;
            renewTABFieldLabels(tablblFieldSet);
        }
    }

    private void renewTABFieldLabels(Vector<String> tablblFieldSet) {
        if (tablblFieldSet.size() >= 1) {
            this.lblTargetPage.setText(tablblFieldSet.elementAt(3).toString());
        }
        if (tablblFieldSet.size() >= 2) {
            this.lblAnchor.setText(tablblFieldSet.elementAt(2).toString());
        }
        if (tablblFieldSet.size() == 4) {
            this.lblTopicTitle.setText(tablblFieldSet.elementAt(0).toString());
            this.lblTopicID.setText(tablblFieldSet.elementAt(1).toString());
        }
    }
}
