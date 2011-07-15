/*
 * ioFileFilter.java
 *
 */
package crosslink.util;

import java.io.File;
import java.io.FileFilter;
/**
 * Created on 23 September 2007, 11:07
 * @author Darren Huang
 */
public class ioFileFilter implements FileFilter {

    public boolean accept(File f) {
        
        if (f.isDirectory()) 
            return true;
        
        String name = f.getName().toLowerCase();
        
        return name.endsWith("xml") || name.endsWith("XML") || name.endsWith("Xml");
    }

}
