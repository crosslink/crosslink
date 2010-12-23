package crosslink.validator;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class CorpusValidator {

//	static boolean good = true;
	
	public static void recordError(String inputfile, String errorType) {
//		good = false;
		System.out.println(errorType + ":" + inputfile);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: proram inputfile");
			System.exit(-1);
		}
		
		String inputfile = null;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);

            SAXParser parser = factory.newSAXParser();

            XMLReader reader = parser.getXMLReader();
            reader.setErrorHandler(new com.edankert.SimpleErrorHandler());
            
//          	String[] arrFile = WildcardFiles.list(args[0]);
          	Stack stack = WildcardFiles.listFilesInStack(args[0], true);
          	while (!stack.isEmpty())
            {
//          		good = true;
                File onefile = (File)stack.pop();
            	inputfile = onefile.getCanonicalPath();
                reader.parse(new InputSource(inputfile));
                	// do whatever you want with the input file...
            }
        } catch (ParserConfigurationException e) {
        	recordError(inputfile, "ParserConfigurationException");
            e.printStackTrace();     
        } catch (SAXException e) {
            recordError(inputfile, "SAXException");
            e.printStackTrace();
        } catch (IOException e) {
            recordError(inputfile, "IOException");
            e.printStackTrace();
        } 
	}
}
