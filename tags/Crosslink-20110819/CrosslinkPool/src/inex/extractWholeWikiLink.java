package inex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Darren
 */
public class extractWholeWikiLink {

    static void log(Object obj) {
        System.out.println(obj.toString());
    }

    public static void main(String[] args) {

        String wikipediaCollectionDir = "G:\\PHD_Tools\\Wikipedia_2009_Collection\\XML";
        File wikiDir = new File(wikipediaCollectionDir);
        if (wikiDir.isDirectory()) {
            File[] wikiSubDirList = wikiDir.listFiles();
            for (File wikiSubDir : wikiSubDirList) {
                if (wikiSubDir.isDirectory()) {
                    File[] wikiFileList = wikiSubDir.listFiles();
                    for (File wikiXmlFile : wikiFileList) {
                        String wikiFileFPath = wikiXmlFile.getAbsolutePath();
                        String wikiFileFName = wikiXmlFile.getName();
                        String wikiFileID = "";
                        if (wikiFileFPath.endsWith(".xml")) {
                            wikiFileID = wikiFileFName.substring(0, wikiFileFName.lastIndexOf(".xml"));
                            log("START: " + wikiFileID);
                            populateAnchorLink(wikiFileID, wikiFileFPath);
                        }
                    }
                }
            }
        }
    }
    static String wikiLinkCollectionDir = "G:\\PHD_Tools\\Wikipedia_2009_Links_Collection\\";
    static StringBuffer htmlSB = null;
    static Vector<String> linksV = null;

    private static void populateAnchorLink(String wikiTopicID, String wikiFPath) {
        try {
            linksV = new Vector<String>();
            pageLinkExtractor(wikiFPath);
            // -----------------------------------------------------------------
            String subDirName = "";
            if (wikiTopicID.length() < 3) {
                if (wikiTopicID.length() == 1) {
                    subDirName = "00" + wikiTopicID;
                } else if (wikiTopicID.length() == 2) {
                    subDirName = "0" + wikiTopicID;
                }
            } else {
                int startPoint = wikiTopicID.length() - 3;
                subDirName = wikiTopicID.substring(startPoint, wikiTopicID.length());
            }
            // -----------------------------------------------------------------
            String wikiLinksSubDir = wikiLinkCollectionDir + subDirName;
            File subDirFile = new File(wikiLinksSubDir);
            if (!subDirFile.exists()) {
                subDirFile.mkdir();
            }
            String wikiLinksFilePath = wikiLinksSubDir + File.separator + wikiTopicID + "_links.txt";
            FileWriter fw = new FileWriter(wikiLinksFilePath);
            for (String linkID : linksV) {
                fw.write(linkID);
                fw.write("\n");
            }
            fw.close();
            log(wikiTopicID + " DONE --> " + wikiLinksFilePath + " : # of links ->" + linksV.size());
        } catch (IOException ex) {
            Logger.getLogger(extractWholeWikiLink.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private static String xmlUnicode = "utf-8";

    private static void pageLinkExtractor(String xmlPath) {
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(xmlPath));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, xmlUnicode);
            InputSource inputSource = new InputSource(inputStreamReader);

            // Parse the XML File
            DOMParser parser = new DOMParser();
            parser.parse(inputSource);
            Document myDoc = parser.getDocument();

            pageElmnFinder(myDoc, htmlSB);

            fileInputStream.close();
            inputStreamReader.close();
        } catch (SAXException ex) {
            log("***** SAXException *****");
            return;
//            Logger.getLogger(poolingMen.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            log("***** IOException *****");
            return;
//            Logger.getLogger(poolingMen.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private static boolean isLinkFlag = false;

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
                if (element.equals("link")) {
                    isLinkFlag = true;
                }
                // =============================================================
//                <link xlink:type="simple" xlink:href="../856/856.xml">
//                Apple Computer</link>
                NamedNodeMap attributes = node.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node current = attributes.item(i);
                    if (current.getNodeName().equals("xlink:href") && isLinkFlag) {
                        String linkPath = current.getNodeValue();
//                        log("linkPath: " + linkPath);
                        String linkID = "";
                        if (linkPath.lastIndexOf(".xml") > linkPath.lastIndexOf(File.separator) + 1) {
                            linkID = linkPath.substring(linkPath.lastIndexOf(File.separator) + 1, linkPath.lastIndexOf(".xml"));
//                            log("linkID: " + linkID);
                            if (!linksV.contains(linkID)) {
                                linksV.add(linkID);
                            }
                        }
                    }
                }
                // =============================================================
                // Recurse each child: e.g. TEXT ---------------------------
                NodeList children = node.getChildNodes();
                if (children != null) {
                    for (int i = 0; i < children.getLength(); i++) {
                        pageElmnFinder(children.item(i), htmlSB);
                    }
                }
                break;
            case Node.TEXT_NODE:
                if (isLinkFlag) {
                    isLinkFlag = false;
                    return;
                }
                break;
        }
    }
}
