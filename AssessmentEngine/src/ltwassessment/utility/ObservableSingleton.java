package ltwassessment.utility;

import java.util.Observable;
import java.util.Vector;
import javax.swing.JLabel;

/**
 * @author Darren Huang
 */
public class ObservableSingleton extends Observable {

    private static ObservableSingleton instance = null;
    // TAB Field Values Vector<String>
    Vector<String> tabFieldValuesV = new Vector<String>();

    protected ObservableSingleton() {
    }

    public static ObservableSingleton getInstance() {
        if (instance == null) {
            instance = new ObservableSingleton();
        }
        return instance;
    }

    public void setTABFieldValues(Vector<String> thisTABFieldValuesV) {
        tabFieldValuesV.removeAllElements();
        // 0: topicTitle, 1: topicID, 2: anchorNameOL, 3: target File ID
        tabFieldValuesV = thisTABFieldValuesV;
        // invoke Change Event
        setChanged();
        notifyObservers(tabFieldValuesV);
    }

    public Vector<String> getTABFieldValues() {
        return tabFieldValuesV;
    }
}
