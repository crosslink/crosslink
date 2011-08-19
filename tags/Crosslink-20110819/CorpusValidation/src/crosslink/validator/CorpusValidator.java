package crosslink.validator;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class CorpusValidator {

//	static boolean good = true;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Usage: proram inputfile");
			System.exit(-1);
		}
		
		String inputfile = null;
		DocumentBuilder builder = null;
		InvalidXmlFileHander errorHandler = new InvalidXmlFileHander();
        try {
//            SAXParserFactory factory = SAXParserFactory.newInstance();
//            factory.setValidating(false);
//            factory.setNamespaceAware(true);
//
//            SAXParser parser = factory.newSAXParser();
//
//            XMLReader reader = parser.getXMLReader();
//            reader.setErrorHandler(new com.edankert.SimpleErrorHandler());
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        	factory.setValidating(false);
        	factory.setNamespaceAware(true);

        	builder = factory.newDocumentBuilder();
        	
            builder.setEntityResolver(new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId)
                throws SAXException, IOException {
                    if (systemId.contains("article.dtd")) {
                        return new InputSource(new StringReader(""));
                    } else {
                        return null;
                    }
                }
            });

        	builder.setErrorHandler(errorHandler);
        } catch (ParserConfigurationException e) {
//        	recordError(inputfile, "ParserConfigurationException");
            e.printStackTrace();     
        } 


            
//          	String[] arrFile = WildcardFiles.list(args[0]);
        int filecount = 0;
      	Stack<File> stack = null;
      	
      	for (String input : args) {
          	stack = WildcardFiles.listFilesInStack(input, true);
          	while (!stack.isEmpty())
            {
//          		good = true;
                File onefile = (File)stack.pop();
                if (onefile.isDirectory()) {
                	stack.addAll(Arrays.asList(WildcardFiles.getInstance().listSubfolderFiles(onefile)));
                	continue;
                }
                System.err.println("validating " + onefile.getAbsolutePath() + "...");
                errorHandler.setFilename(onefile.getAbsolutePath());
                ++filecount;
                try {
                	inputfile = onefile.getCanonicalPath();
                	Document document = builder.parse(new InputSource(inputfile)); //dom
//                reader.parse(new InputSource(inputfile)); //SAX
                	// do whatever you want with the input file...
                }
                catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
					//recordError(inputfile, "IOException");
					e.printStackTrace();
                } 
            }
      	}
      	
      	System.err.println("Xml file(s) validation finished: " + filecount);
	}
}
