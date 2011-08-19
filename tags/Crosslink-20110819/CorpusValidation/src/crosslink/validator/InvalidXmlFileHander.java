package crosslink.validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

public class InvalidXmlFileHander implements ErrorHandler {
	private String filename;

    public void warning(SAXParseException e) {
        System.err.println(filename + ":" + e.getMessage());
//    	recordError(filename, e.getMessage());
    }

    public void error(SAXParseException e) {
    	//        System.err.println(e.getMessage());
        recordError(filename, e.getMessage());
    }

    public void fatalError(SAXParseException e) {
//        System.err.println(e.getMessage());
    	recordError(filename, e.getMessage());
    }

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public static void recordError(String inputfile, String errorType) {
//		good = false;
		System.out.println(inputfile + ":" + errorType);
	}
}
