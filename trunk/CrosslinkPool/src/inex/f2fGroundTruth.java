package inex;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
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
public class f2fGroundTruth {

    static void log(Object obj) {
        System.out.println(obj.toString());
    }
    private static String f2fTopicsFPath = "C:\\Users\\Darren\\Desktop\\INEX_2009\\LTW2009_TOPICS_F2F\\LTW2009_F2FTOPICS\\LTW2009_F2FTOPICS.txt";
    private static String f2fGTDir = "C:\\Users\\Darren\\Desktop\\INEX_2009\\LTW2009_TOPICS_F2F\\LTW2009_GROUND_TRUTH\\";
    private static int option = 2;

    public static void main(String[] args) {

        String wikiLinkCollection = "";

        switch (option) {
            case 0:
                try {
                    FileInputStream fstream = new FileInputStream(f2fTopicsFPath);
                    DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String strLine;
                    while ((strLine = br.readLine()) != null) {
                        String[] lineSA = strLine.split(" : ");
                        String wikiFileName = lineSA[1];
                        String wikiFileID = wikiFileName.substring(0, wikiFileName.lastIndexOf(".xml"));
                        log("wikiFileID: " + wikiFileID);
                        populateAnchorLink(wikiFileID);
                    }
                    fstream.close();
                } catch (IOException ex) {
                    Logger.getLogger(f2fGroundTruth.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case 1:
                wikiLinkCollection = "G:\\PHD_Tools\\Wikipedia_2009_Links_Collection\\";
                produceIncomingLinks(wikiLinkCollection);
                break;
            case 2:
                String a2bXmlDir = "C:\\Users\\Darren\\Desktop\\INEX_2009\\LTW2009_TOPICS\\LTW2009_Topics\\XML";
                Vector<String> a2bTopicID = getA2BTopicIDs(a2bXmlDir);
                wikiLinkCollection = "G:\\PHD_Tools\\Wikipedia_2009_Links_Collection\\";
                produceA2BGTIncomingLinks(a2bTopicID, wikiLinkCollection);
                break;
            default:
                log("This is the default option...");
        }
    }
    // =========================================================================
//    private static String incomeLinksTargetDir = "C:\\Users\\Darren\\Desktop\\INEX_2009\\LTW2009_TOPICS_F2F\\LTW2009_GROUND_TRUTH_INCOMING\\";
    private static String incomeLinksTargetDir = "C:\\Users\\Darren\\Desktop\\INEX_2009\\POOL_2009\\POOL_GROUNDTRUTH_INCOMING\\";

    private static Vector<String> getA2BTopicIDs(String a2bXmlDir) {
        Vector<String> a2bTopicIDV = new Vector<String>();
        File a2bXmlFileDir = new File(a2bXmlDir);
        if (a2bXmlFileDir.isDirectory()) {
            File[] a2bXmlFileList = a2bXmlFileDir.listFiles();
            for (File a2bXmlFile : a2bXmlFileList) {
                String a2bXmlFileFPath = a2bXmlFile.getAbsolutePath();
                if (a2bXmlFileFPath.endsWith(".xml")) {
                    String a2bXmlFileFName = a2bXmlFile.getName();
                    String a2bXmlFileID = a2bXmlFileFName.substring(0, a2bXmlFileFName.lastIndexOf(".xml"));
                    a2bTopicIDV.add(a2bXmlFileID);
                }
            }

        }
        return a2bTopicIDV;
    }

    private static void produceA2BGTIncomingLinks(Vector<String> a2bTopicID, String wikiLinkCollection) {
        // ---------------------------------------------------------------------
        // populate Topic Set
//        topicIDNameVS = new Vector<String[]>();
        topicIncomingHT = new Hashtable<String, Vector<String>>();
        for (String thisTopicID : a2bTopicID) {
            Vector<String> topicInLinkVS = new Vector<String>();
            topicIncomingHT.put(thisTopicID, topicInLinkVS);
        }
        // ---------------------------------------------------------------------
        File wikiLinkCollDir = new File(wikiLinkCollection);
        if (wikiLinkCollDir.isDirectory()) {
            File[] wikiLinkSubDirList = wikiLinkCollDir.listFiles();
            for (File wikiLinkSubDir : wikiLinkSubDirList) {
                log(wikiLinkSubDir.getAbsolutePath());
                if (wikiLinkSubDir.isDirectory()) {
                    File[] wikiLinkFileList = wikiLinkSubDir.listFiles();
                    for (File wikiLinkFile : wikiLinkFileList) {
                        String wikiLinkFileFPath = wikiLinkFile.getAbsolutePath();
                        if (wikiLinkFileFPath.endsWith(".txt")) {
                            String wikiLinkFileFName = wikiLinkFile.getName();
                            String wikiLinkFileID = wikiLinkFileFName.substring(0, wikiLinkFileFName.lastIndexOf(".txt"));
                            extractIncomingLinks(wikiLinkFileID, wikiLinkFileFPath);
                        }
                    }
                }
                log("DONE --> " + wikiLinkSubDir.getName());
            }
        }
        // ---------------------------------------------------------------------
        writeToTXT(topicIncomingHT);
    }
    // =========================================================================
//    private static String incomeLinksTargetDir = "C:\\Users\\Darren\\Desktop\\INEX_2009\\LTW2009_TOPICS_F2F\\LTW2009_GROUND_TRUTH_INCOMING\\";
    private static Vector<String[]> topicIDNameVS = null;
    private static Hashtable<String, Vector<String>> topicIncomingHT = null;

    private static void produceIncomingLinks(String wikiLinkCollection) {
        // ---------------------------------------------------------------------
        // populate Topic Set
        topicIDNameVS = new Vector<String[]>();
        topicIncomingHT = new Hashtable<String, Vector<String>>();
        try {
            FileInputStream fstream = new FileInputStream(f2fTopicsFPath);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] lineSA = strLine.split(" : ");
                String wikiFileName = lineSA[1];
                String wikiFileID = wikiFileName.substring(0, wikiFileName.lastIndexOf(".xml"));
                topicIDNameVS.add(new String[]{wikiFileID, wikiFileName});
                Vector<String> topicInLinkVS = new Vector<String>();
                topicIncomingHT.put(wikiFileID, topicInLinkVS);
            }
            fstream.close();
        } catch (IOException ex) {
            Logger.getLogger(f2fGroundTruth.class.getName()).log(Level.SEVERE, null, ex);
        }
        // ---------------------------------------------------------------------
        File wikiLinkCollDir = new File(wikiLinkCollection);
        if (wikiLinkCollDir.isDirectory()) {
            File[] wikiLinkSubDirList = wikiLinkCollDir.listFiles();
            for (File wikiLinkSubDir : wikiLinkSubDirList) {
                log(wikiLinkSubDir.getAbsolutePath());
                if (wikiLinkSubDir.isDirectory()) {
                    File[] wikiLinkFileList = wikiLinkSubDir.listFiles();
                    for (File wikiLinkFile : wikiLinkFileList) {
                        String wikiLinkFileFPath = wikiLinkFile.getAbsolutePath();
                        if (wikiLinkFileFPath.endsWith(".txt")) {
                            String wikiLinkFileFName = wikiLinkFile.getName();
                            String wikiLinkFileID = wikiLinkFileFName.substring(0, wikiLinkFileFName.lastIndexOf(".txt"));
                            extractIncomingLinks(wikiLinkFileID, wikiLinkFileFPath);
                        }
                    }
                }
                log("DONE --> " + wikiLinkSubDir.getName());
            }
        }
        // ---------------------------------------------------------------------
        writeToTXT(topicIncomingHT);
    }

    private static void writeToTXT(Hashtable<String, Vector<String>> topicIncomingHT) {
        Enumeration tKeyEnu = topicIncomingHT.keys();
        while (tKeyEnu.hasMoreElements()) {
            try {
                Object tKeyObj = tKeyEnu.nextElement();
                String topicID = tKeyObj.toString();
                String targetIncomeFile = incomeLinksTargetDir + topicID + "_gtIncoming.txt";
                File inFile = new File(targetIncomeFile);
                FileWriter fw = new FileWriter(inFile);
                Vector<String> incomingLinksVS = topicIncomingHT.get(tKeyObj.toString());
                for (String thisInLinkID : incomingLinksVS) {
                    fw.append(thisInLinkID);
                    fw.append("\n");
                }
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(f2fGroundTruth.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void extractIncomingLinks(String wikiLinkFileID, String wikiLinkFileFPath) {
        FileInputStream fstream = null;
        try {
            fstream = new FileInputStream(wikiLinkFileFPath);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                String linkID = strLine.trim();
                if (isInNumber(linkID)) {
                    if (topicIncomingHT.containsKey(linkID)) {
                        Vector<String> thisIncomingVS = topicIncomingHT.get(linkID);
                        if (!thisIncomingVS.contains(wikiLinkFileID)) {
                            thisIncomingVS.add(wikiLinkFileID);
                        }
                        topicIncomingHT.remove(linkID);
                        topicIncomingHT.put(linkID, thisIncomingVS);
                    }
                }
            }
            fstream.close();
        } catch (IOException ex) {
            Logger.getLogger(f2fGroundTruth.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    // =========================================================================
    // <editor-fold defaultstate="collapsed" desc="Wikipedia Page Link Finder">
    static StringBuffer htmlSB = null;
    static Vector<String> linksV = null;

    private static void populateAnchorLink(String myTopicID) {
        try {
            linksV = new Vector<String>();
            String xmlPath = getWikipediaFilePathByName(myTopicID + ".xml");
            pageLinkExtractor(xmlPath);
            // ---------------------------------------------------------------------
            String topicF2FLinksTXTFPath = f2fGTDir + myTopicID + "_F2FGT.txt";
            log("============= " + topicF2FLinksTXTFPath);
            FileWriter fw = new FileWriter(topicF2FLinksTXTFPath);
            for (String linkID : linksV) {
                fw.write(linkID);
                fw.write("\n");
            }
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(f2fGroundTruth.class.getName()).log(Level.SEVERE, null, ex);
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
            return;
//            Logger.getLogger(poolingMen.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
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
                        log("linkPath: " + linkPath);
                        String linkID = "";
                        if (linkPath.lastIndexOf(".xml") > linkPath.lastIndexOf(File.separator) + 1) {
                            linkID = linkPath.substring(linkPath.lastIndexOf(File.separator) + 1, linkPath.lastIndexOf(".xml"));
                            log("linkID: " + linkID);
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
    private static String wikipediaXmlDir = "G:\\PHD_Tools\\Wikipedia_2009_Collection\\XML\\";

    public static String getWikipediaFilePathByName(String fileName) {
        String WikipediaPathFile = "";
        String WikipediaDir = "";
        String wikipediaFilePath = getTargetFilePathByFileName(WikipediaDir, WikipediaPathFile, fileName);
        return wikipediaFilePath;
    }

    private static String getTargetFilePathByFileName(String topDir, String pathCollectionFile, String fileName) {
        String thisFileFullPath = "";
        int startPoint = 0;
        String subFolder = "";
        if (fileName.length() < 7) {
            startPoint = 0;
            subFolder = fileName.substring(0, fileName.lastIndexOf(".xml"));
            if (subFolder.length() == 1) {
                subFolder = "00" + subFolder;
            } else if (subFolder.length() == 2) {
                subFolder = "0" + subFolder;
            }
        } else {
            startPoint = fileName.length() - 7;
            subFolder = fileName.substring(startPoint, fileName.lastIndexOf(".xml"));
        }
        return thisFileFullPath = wikipediaXmlDir + subFolder + File.separator + fileName;
    }
    // </editor-fold>

    public static boolean isInNumber(String num) {
        try {
            int intI = Integer.parseInt(num);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
