package crosslink.validator;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class CorpusValidator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);

            SAXParser parser = factory.newSAXParser();

            XMLReader reader = parser.getXMLReader();
            reader.setErrorHandler(new com.edankert.SimpleErrorHandler());
            
          	String[] arrFile = WildcardFiles.list(args[0]);
            for (String onefile : arrFile) 
            {
            	String inputfile = WildcardFiles.getDirectory() + File.separator + onefile;
                reader.parse(new InputSource(inputfile));
                	// do whatever you want with the input file...
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

	}
}
