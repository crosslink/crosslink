/*
 * runsParser.java
 *
 * This class is simply used to parse underlying run XML files
 * into Hashtable in the momory
 */
package org.inex.ltw.util;

import java.util.Hashtable;
/**
 * Created on September 15, 2007, 10:27 AM
 * @author Darren Huang
 */
public class runsParser {
    
    String filesdirectory;
    String[] runfiles;
    Hashtable rundata = new Hashtable();
    /** Creates a new instance of runsParser */
    public runsParser(String filesDirectory) {
        this.filesdirectory = filesDirectory;
        // then here to get all files required into a string array
        //runfiles = parseDirectory();
    }
    
    public Hashtable getRunsHash() {
        
        Object[][] runsData = {
            {new Integer(11), "qutlte_02", "12689.xml", "Einstan", new Integer(255), new Integer(255), new Boolean(false)},
            {new Integer(13), "qutlte_03", "12689.xml", "Einstan", new Integer(255), new Integer(255), new Boolean(false)},
            {new Integer(18), "qutlte_032", "12689.xml", "Einstan", new Integer(255), new Integer(255), new Boolean(true)},
            {new Integer(122), "qutlte_05", "12689.xml", "Einstan", new Integer(255), new Integer(255), new Boolean(false)},
            {new Integer(123), "qutlte_08", "12689.xml", "Einstan", new Integer(255), new Integer(255), new Boolean(false)},
            {new Integer(131), "qutlte_09", "12689.xml", "Einstan", new Integer(255), new Integer(255), new Boolean(false)}};
        
        for (int i=0; i<6; i++){
            rundata.put(runsData[i][1], runsData[i]);
        }
        return rundata;
    }
    
}
