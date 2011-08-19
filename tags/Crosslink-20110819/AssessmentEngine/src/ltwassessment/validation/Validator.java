package ltwassessment.validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import ltwassessment.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Darren HUANG
 */
public class Validator {

    /** The instances of this class for use (singleton design pattern) */
    private static Map instances = null;
    /** The URL of the XML Schema for this <code>Validator</code> */
    private URL schemaURL;
    private String DTDPath = "";
    /** The constraints for this XML Schema */
    private Map constraints;

    /**
     * <p>
     *  This constructor is private so that the class cannot be instantiated
     *    directly, but instead only through <code>{@link #getInstance()}</code>.
     * </p>
     *
     * @param schemaURL <code>URL</code> to parse the schema at.
     * @throws <code>IOException</code> - when errors in parsing occur.
     */
    private Validator(URL schemaURL) throws IOException {
//        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ltwassessment.ltwassessmentApp.class).getContext().getResourceMap(Validator.class);
//        this.DTDPath = resourceMap.getString("pooling.dtd");

//        this.schemaURL = schemaURL;
        // parse the XML Schema and create the constraints
//        schemaParser parser = new schemaParser(schemaURL);
//        constraints = parser.getConstraints();
    }

    /**
     * <p>
     *  This will return the instance for the specific XML Schema URL. If a schema
     *    exists, it is returned (as parsing will already be done); otherwise,
     *    a new instance is created, and then returned.
     * </p>
     *
     * @param schemaURL <code>URL</code> of schema to validate against.
     * @return <code>Validator</code> - the instance, ready to use.
     * @throws <code>IOException</code> - when errors in parsing occur.
     */
    public static synchronized Validator getInstance(URL schemaURL) throws IOException {
        if (instances != null) {
            if (instances.containsKey(schemaURL.toString())) {
                return (Validator) instances.get(schemaURL.toString());
            } else {
                Validator validator = new Validator(schemaURL);
                instances.put(schemaURL.toString(), validator);
                return validator;
            }
        } else {
            instances = new HashMap();
            Validator validator = new Validator(schemaURL);
            instances.put(schemaURL.toString(), validator);
            return validator;
        }
    }

    public String validateSubmissionXML(ArrayList<File> runXmlList) {
        StringBuffer errMsg = new StringBuffer();
        if (!runXmlList.isEmpty()) {
            for (File runXml : runXmlList) {
                try {
                    // ---------------------------------------------------------
                    // 1) well-form
                    String runXmlPath = runXml.getAbsolutePath();
                    XMLReader reader = XMLReaderFactory.createXMLReader();
                    reader.parse(runXmlPath);
                    // ---------------------------------------------------------
                    // TODO: we need to use either Schema or DTD to validate submission XML
                    // 2) DTD
//                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//                    factory.setValidating(true);
//                    DocumentBuilder builder = factory.newDocumentBuilder();
//                    builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
//                        //To handle Fatal Errors
//                        public void fatalError(SAXParseException exception) throws SAXException {
//                            System.out.println("Line: " + exception.getLineNumber() + "\nFatal Error: " + exception.getMessage());
//                        }
//                        //To handle Errors
//                        public void error(SAXParseException e) throws SAXParseException {
//                            System.out.println("Line: " + e.getLineNumber() + "\nError: " + e.getMessage());
//                        }
//                        //To Handle warnings
//                        public void warning(SAXParseException err) throws SAXParseException {
//                            System.out.println("Line: " + err.getLineNumber() + "\nWarning: " + err.getMessage());
//                        }
//                    });
//
//                    Document xmlDocument = builder.parse(new FileInputStream(runXml.getAbsolutePath()));
//                    DOMSource source = new DOMSource(xmlDocument);
//                    StreamResult result = new StreamResult(System.out);
//                    TransformerFactory tf = TransformerFactory.newInstance();
//                    Transformer transformer = tf.newTransformer();
//                    transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, this.DTDPath);
//                    transformer.transform(source, result);

//                } catch (TransformerException ex) {
//                    return errMsg.append("Err Msg: TransformerException: " + runXml.getName() + "- ").append("\r\n Error: " + ex.getMessage()).toString();
//                } catch (ParserConfigurationException ex) {
//                    return errMsg.append("Err Msg: ParserConfigurationException: " + runXml.getName() + "- ").append("\r\n Error: " + ex.getMessage()).toString();
                } catch (SAXParseException ex) {
                    return errMsg.append("Err Msg: SAXParseException:" + runXml.getName() + "- ").append("\r\n Line: " + ex.getLineNumber()).append("\r\n Column: " + ex.getColumnNumber()).append("\r\n Error: " + ex.getMessage()).toString();
                } catch (SAXException ex) {
                    return errMsg.append("Err Msg: SAXException: " + runXml.getName() + "- ").append("\r\n Error: " + ex.getMessage()).toString();
                } catch (IOException ex) {
                    return errMsg.append("Err Msg: IOException: " + runXml.getName() + "- ").append("\r\n Error: " + ex.getMessage()).toString();
                }
            }
        }
        return errMsg.toString();
    }
    /**
     * <p>
     *  This will validate a data value (in <code>String</code> format) against a
     *    specific constraint, and return an error message if there is a problem.
     * </p>
     *
     * @param constraintName the identifier in the constraints to validate this data against.
     * @param data <code>String</code> data to validate.
     * @return <code>String</code> - Error message, or an empty String if no problems occurred.
     */
//    public String checkValidity(String constraintName, Hashtable<String, String> attrsHT, Hashtable<String, String> elmnsHT) {
//
//    }
    /**
     * <p>
     *  This will test the supplied data to see if it can be converted to the
     *    Java data type given in <code>dataType</code>.
     * </p>
     * 
     * @param data <code>String</code> to test data type of.
     * @param dataType <code>String</code> name of Java class to convert to.
     * @return <code>boolean</code> - whether conversion can occur.
     */
//    private boolean correctDataType(String data, String dataType) {
//        if ((dataType.equals("String")) || (dataType.equals("java.lang.String"))) {
//            return true;
//        }
//        if ((dataType.equals("int")) || (dataType.equals("java.lang.Integer"))) {
//            try {
//                Integer test = new Integer(data);
//                return true;
//            } catch (NumberFormatException e) {
//                return false;
//            }
//        }
//        if ((dataType.equals("long")) || (dataType.equals("java.lang.Long"))) {
//            try {
//                Long test = new Long(data);
//                return true;
//            } catch (NumberFormatException e) {
//                return false;
//            }
//        }
//        if ((dataType.equals("float")) || (dataType.equals("java.lang.Float"))) {
//            try {
//                Float test = new Float(data);
//                return true;
//            } catch (NumberFormatException e) {
//                return false;
//            }
//        }
//        if ((dataType.equals("double")) || (dataType.equals("java.lang.Double"))) {
//            try {
//                Double test = new Double(data);
//                return true;
//            } catch (NumberFormatException e) {
//                return false;
//            }
//        }
//        if (dataType.equals("java.lang.Boolean")) {
//            if ((data.equalsIgnoreCase("true")) ||
//                    (data.equalsIgnoreCase("false")) ||
//                    (data.equalsIgnoreCase("yes")) ||
//                    (data.equalsIgnoreCase("no"))) {
//                return true;
//            } else {
//                return false;
//            }
//        }
//
//        return true;
//    }
}










