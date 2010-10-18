package ltwassessmenttool.utility;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author Darren HUANG @QUT
 */
public class topicTextSeqComparator implements Comparator, Serializable {

    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof String)) {
            throw new ClassCastException();
        }
        if (!(o2 instanceof String)) {
            throw new ClassCastException();
        }
        String[] oStr1 = o1.toString().split("_");
        String[] oStr2 = o2.toString().split("_");
        int result;
        if (Integer.valueOf(oStr1[0]) > Integer.valueOf(oStr2[0])){
            result = 1;
        } else if (Integer.valueOf(oStr1[0]) < Integer.valueOf(oStr2[0])){
            result = -1;
        } else {
            result = 0;
        }
        return result;
    }

}
