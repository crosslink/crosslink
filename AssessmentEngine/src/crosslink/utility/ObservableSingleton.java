package crosslink.utility;

import java.util.Observable;
import java.util.Vector;
import javax.swing.JLabel;

import crosslink.assessment.Bep;

import crosslink.utility.ObservableSingleton;

/**
 * @author Darren Huang
 */
public class ObservableSingleton extends Observable {

    private static ObservableSingleton instance = null;
    // TAB Field Values Bep
    Bep tabFieldValuesV = new Bep();

    protected ObservableSingleton() {
    }

    public static ObservableSingleton getInstance() {
        if (instance == null) {
            instance = new ObservableSingleton();
        }
        return instance;
    }

    public void setTABFieldValues(Bep thisTABFieldValuesV) {
        // 0: topicTitle, 1: topicID, 2: anchorNameOL, 3: target File ID
        tabFieldValuesV = thisTABFieldValuesV;
        // invoke Change Event
        setChanged();
        notifyObservers(tabFieldValuesV);
    }

    public Bep getTABFieldValues() {
        return tabFieldValuesV;
    }
}
