package ltwassessment.utility;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author n4275187
 */
public class toolErrorHandler implements ErrorHandler {

    public void warning(SAXParseException ex) throws SAXException {
        System.err.println("Warning:" + ex.getMessage());
    }

    public void error(SAXParseException ex) throws SAXException {
        System.err.println("Error:" + ex.getMessage());
    }

    public void fatalError(SAXParseException ex) throws SAXException {
        throw ex;
    }

}
