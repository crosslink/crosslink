package inex;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author DHuang
 */
public class findPageTitle {

    private static String xmlUnicode = "utf-8";
    private static StringBuffer htmlSB = new StringBuffer();
    private static boolean isTitleFlag = false;

    static void log(Object obj) {
        System.out.println(obj.toString());
    }

    public static void main(String[] args) {

        // title: <title>1985 BDO World Darts Championship</title>
        String xmlPath = "resources\\collection_Wikipedia\\017\\7483017.xml";
        pageTitleExtractor(xmlPath);

    }

    private static void pageTitleExtractor(String xmlPath) {
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(xmlPath));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, findPageTitle.xmlUnicode);
            InputSource inputSource = new InputSource(inputStreamReader);

            // Parse the XML File
            DOMParser parser = new DOMParser();
            parser.parse(inputSource);
            Document doc = parser.getDocument();

            pageElmnFinder(doc, htmlSB);
            log("myPageTitle: " + htmlSB.toString());

            fileInputStream.close();
            inputStreamReader.close();
        } catch (SAXException ex) {
            Logger.getLogger(findPageTitle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(findPageTitle.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void pageElmnFinder(Node node, StringBuffer htmlSB) {

        switch (node.getNodeType()) {
            case Node.DOCUMENT_NODE:
                // recurse on each child
                NodeList nodes = node.getChildNodes();
                if (nodes != null) {
                    for (int i = 0; i < nodes.getLength(); i++) {
                        pageElmnFinder(nodes.item(i), htmlSB);
                    }
                }
                break;
            case Node.ELEMENT_NODE:
                String element = node.getNodeName();
                // =============================================================
                // Start of Tag: -----------------------------------------------
                if (element.equals("title")) {
                    isTitleFlag = true;
                }
                // Recurse each child: e.g. TEXT ---------------------------
                NodeList children = node.getChildNodes();
                if (children != null) {
                    for (int i = 0; i < children.getLength(); i++) {
                        pageElmnFinder(children.item(i), htmlSB);
                    }
                }
                break;
            case Node.TEXT_NODE:
                if (isTitleFlag) {
                    isTitleFlag = false;
                    htmlSB.append(node.getNodeValue());
                }
                break;
        }
    }
}
