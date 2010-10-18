package ltwassessmenttool.parsers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import ltwassessmenttool.LTWAssessmentToolView;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Darren HUANG
 */
public class resourcesManager {

    private final String sysPropertyKey = "isTABKey";
    private final String sysPropertyIsTopicWikiKey = "isTopicWikipedia";
    private final String sysPropertyIsLinkWikiKey = "isLinkWikipedia";
    private final String bepLength = "4";
    // constants for this Class
    private String resourceXMLFile = "";
    private String afTasnCollectionErrors = "";
    private String afErrorsFilePath = "";
    // constants for tool setting up property
    private String fileNotFoundXmlPath = "";
    private String defaultWikipediaDirectory = "";
    private String defaultTeAraDirectory = "";
    private String wikipediaPool = "";
    private String teAraPool = "";
    // constants for toolResource XML
    private String afTitleTag = "toolResources";
    private String afLinkingModeTag = "linkingMode";
    private String afTopicCollTypeTag = "topicCollectionType";
    private String afLinkCollTypeTag = "linkCollectionType";
    private String afWikipediaCollTag = "wikipediaCollectionPath";
    private String afTeAraCollTag = "tearaCollectionPath";
    private String afCurrTopicTag = "currTopicXmlFile";
    private String afltwTopicsTag = "ltwTopics";
    private String afTeAraFilePathTag = "tearaFilePath";
    private String afAnchorFOLTag = "currTopicAnchors";
    private String afAnchorFOLPrefixTag = "anchor";
    private String afBepOffsetTag = "currTopicBeps";
    private String afBepOffsetPrefixTag = "bep";
    private String afTABNavigationIndexTag = "tabNavigationIndex";
    private String afTBANavigationIndexTag = "tbaNavigationIndex";
    private Hashtable<String, Vector<String[]>> topicAllAnchors = new Hashtable<String, Vector<String[]>>();
    private Hashtable<String, Hashtable<String, Hashtable<String, Vector<String[]>>>> poolOutgoingData = new Hashtable<String, Hashtable<String, Hashtable<String, Vector<String[]>>>>();
    private Hashtable<String, Vector<String[]>> topicAllBEPs = new Hashtable<String, Vector<String[]>>();
    private Hashtable<String, Hashtable<String, Vector<String[]>>> poolIncomingData = new Hashtable<String, Hashtable<String, Vector<String[]>>>();
    private poolerManager pooler = null;

    private void log(Object text) {
        System.out.println(text);
    }

    public resourcesManager() {
        // get toolResources XML file Location/Path
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ltwassessmenttool.LTWAssessmentToolApp.class).getContext().getResourceMap(LTWAssessmentToolView.class);
        // resource constants for this Class
        resourceXMLFile = resourceMap.getString("ResourceXMLFilePath");
        afTasnCollectionErrors = resourceMap.getString("AssFormXml.taskCollectionError");
        afErrorsFilePath = resourceMap.getString("bepFile.ErrorXmlPath");
        // resource constants for tool setting up property
        fileNotFoundXmlPath = resourceMap.getString("fileNotFound.ReplacedXmlPath");
        defaultWikipediaDirectory = resourceMap.getString("directory.Wikipedia");
        defaultTeAraDirectory = resourceMap.getString("directory.TeAra");
        wikipediaPool = resourceMap.getString("pool.wikipedia");
        teAraPool = resourceMap.getString("pool.teara");

        pooler = new poolerManager();
        // ---------------------------------------------------------------------
        // Hashtable<String, Hashtable<String, Hashtable<String, Vector<String[]>>>>
        // topicFileID, <PoolAnchor_OL, <Anchor_OL, V<String[]{Offset, fileID, tbrel}
        poolOutgoingData = pooler.getOutgoingPool();
        // Hashtable<String, Vector<String[]>>
        // incoming : topicID, V<String[]{bep Offset, borel}>
        topicAllBEPs = pooler.getTopicAllBeps();
        // Hashtable<String, Hashtable<String, Vector<String[]>>>
        // topifFileID, <BEP_Offset, V<String[]{Offset, Length, AName, fileID, barel}
        poolIncomingData = pooler.getIncomingPool();
    }

    // =========================================================================
    // =========================================================================
    // <editor-fold defaultstate="collapsed" desc="Update RSC">
    public void updateOutgoingCompletion(String outgoingCompletion) {
        try {
            Document doc = readingXMLFromFile(resourceXMLFile);

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer tformer = tFactory.newTransformer();
            Source source = new DOMSource(doc);
            Result result = new StreamResult(new FileWriter(resourceXMLFile));

            NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
            for (int i = 0; i < titleNodeList.getLength(); i++) {
                Element titleElmn = (Element) titleNodeList.item(i);
                NodeList subNodeList = titleElmn.getElementsByTagName("outgoingCompletion");
                Element subElmn = (Element) subNodeList.item(0);
                // add NEW
                Element clonedElmn = (Element) doc.createElement("outgoingCompletion");
                subElmn.getParentNode().insertBefore(clonedElmn, subElmn.getNextSibling());
                clonedElmn.appendChild(doc.createTextNode(outgoingCompletion));
                // remove OLD element
                subElmn.getParentNode().removeChild(subElmn);
                tformer.transform(source, result);
            }
        } catch (IOException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateIncomingCompletion(String incomingCompletion) {
        try {
            Document doc = readingXMLFromFile(resourceXMLFile);

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer tformer = tFactory.newTransformer();
            Source source = new DOMSource(doc);
            Result result = new StreamResult(new FileWriter(resourceXMLFile));

            NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
            for (int i = 0; i < titleNodeList.getLength(); i++) {
                Element titleElmn = (Element) titleNodeList.item(i);
                NodeList subNodeList = titleElmn.getElementsByTagName("incomingCompletion");
                Element subElmn = (Element) subNodeList.item(0);
                // add NEW
                Element clonedElmn = (Element) doc.createElement("incomingCompletion");
                subElmn.getParentNode().insertBefore(clonedElmn, subElmn.getNextSibling());
                clonedElmn.appendChild(doc.createTextNode(incomingCompletion));
                // remove OLD element
                subElmn.getParentNode().removeChild(subElmn);
                tformer.transform(source, result);
            }
        } catch (IOException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateTABNavIndex(String topicID, String[] pAnchorOLSA, String[] linkBepOID) {
        // Format: new String[]{0, 1_2_0_0}
        String pAnchorO = pAnchorOLSA[0];
        String pAnchorL = pAnchorOLSA[1];
        String linkBepID = linkBepOID[1];
        // String[]{anchor Offset, Length, Name, arel}
        int pAnchorIndex = 0;
        int pABepIndex = 0;
        int pAnchorCounter = 0;
        Vector<String[]> poolAnchorsOLNameStatusVSA = this.getPoolAnchorsOLNameStatusV();
        for (String[] pAnchorOLNameStatus : poolAnchorsOLNameStatusVSA) {
            if (pAnchorO.equals(pAnchorOLNameStatus[0])) {
                pAnchorIndex = pAnchorCounter;
                // -------------------------------------------------------------
                int pABepCounter = 0;
//                HashMap<String, Vector<String[]>> anchorBepLinksHM = pooler.getBepSetByAnchor(topicID);
                Hashtable<String, Vector<String[]>> anchorBepLinksHM = this.getPoolAnchorBepLinksHT();
                Vector<String[]> bepLinksVSA = anchorBepLinksHM.get(pAnchorO + "_" + pAnchorL);
                for (String[] bepLinksOSIDStatus : bepLinksVSA) {
                    if (linkBepID.equals(bepLinksOSIDStatus[2])) {
                        pABepIndex = pABepCounter;
                    }
                    pABepCounter++;
                }
            }
            pAnchorCounter++;
        }
        this.updateTABNavigationIndex(new String[]{"0", String.valueOf(pAnchorIndex), String.valueOf(pABepIndex), "0", "0"});
    }

    public void updateTBANavIndex(String topicID, String pBepOSA, String[] linkAnchorOLID) {
        // Format: new String[]{0, 1_2_0_0}
        String pBepO = pBepOSA;
        String linkAnchorO = linkAnchorOLID[0];
        String linkAnchorL = linkAnchorOLID[1];
        String linkAnchorID = linkAnchorOLID[2];
        // String[]{anchor Offset, Length, Name, arel}
        int pBepIndex = 0;
        int pBAnchorIndex = 0;
        int pBepCounter = 0;
        Vector<String[]> poolBepOStatusVSA = this.getPoolBEPsOStatusV();
        for (String[] pBepOStatus : poolBepOStatusVSA) {
            if (pBepO.equals(pBepOStatus[0])) {
                pBepIndex = pBepCounter;
                // -------------------------------------------------------------
                int pBAnchorCounter = 0;
                HashMap<String, Vector<String[]>> bepAnchorLinksHM = pooler.getAnchorFileSetByBep(topicID);
                Vector<String[]> anchorLinksVSA = bepAnchorLinksHM.get(pBepO);
                // new String[]{anchor_O, L, Name, ID, Status}
                for (String[] anchorLinksOSIDStatus : anchorLinksVSA) {
                    if (linkAnchorID.equals(anchorLinksOSIDStatus[3]) &&
                            linkAnchorO.equals(anchorLinksOSIDStatus[0])) {
                        pBAnchorIndex = pBAnchorCounter;
                    }
                    pBAnchorCounter++;
                }
            }
            pBepCounter++;
        }
        this.updateTBANavigationIndex(new String[]{"0", String.valueOf(pBepIndex), String.valueOf(pBAnchorIndex), "0", "0"});
    }

    public void updateLinkingMode(String linkingMode) {
        try {
            Document doc = readingXMLFromFile(resourceXMLFile);

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer tformer = tFactory.newTransformer();
            Source source = new DOMSource(doc);
            Result result = new StreamResult(new FileWriter(resourceXMLFile));

            NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
            for (int i = 0; i < titleNodeList.getLength(); i++) {
                Element titleElmn = (Element) titleNodeList.item(i);
                NodeList subNodeList = titleElmn.getElementsByTagName(afLinkingModeTag);
                Element subElmn = (Element) subNodeList.item(0);
                // add NEW
                Element clonedElmn = (Element) doc.createElement(afLinkingModeTag);
                subElmn.getParentNode().insertBefore(clonedElmn, subElmn.getNextSibling());
                clonedElmn.appendChild(doc.createTextNode(linkingMode));
                // remove OLD element
                subElmn.getParentNode().removeChild(subElmn);

                tformer.transform(source, result);
            }

        } catch (IOException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateTBANavigationIndex(String[] navIndices) {
        try {
            Document doc = readingXMLFromFile(resourceXMLFile);

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer tformer = tFactory.newTransformer();
            Source source = new DOMSource(doc);
            Result result = new StreamResult(new FileWriter(resourceXMLFile));

            NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
            for (int i = 0; i < titleNodeList.getLength(); i++) {
                Element titleElmn = (Element) titleNodeList.item(i);
                NodeList subNodeList = titleElmn.getElementsByTagName(afTBANavigationIndexTag);
                Element subElmn = (Element) subNodeList.item(0);
                // add NEW
                Element clonedElmn = (Element) doc.createElement(afTBANavigationIndexTag);
                subElmn.getParentNode().insertBefore(clonedElmn, subElmn.getNextSibling());
                String navIndexLine = navIndices[0] + " : " + navIndices[1] + " , " + navIndices[2] + " , " + navIndices[3] + " , " + navIndices[4];
                clonedElmn.appendChild(doc.createTextNode(navIndexLine));
                // remove OLD element
                subElmn.getParentNode().removeChild(subElmn);
                tformer.transform(source, result);
            }
        } catch (IOException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateTABNavigationIndex(String[] navIndices) {
        try {
            Document doc = readingXMLFromFile(resourceXMLFile);

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer tformer = tFactory.newTransformer();
            Source source = new DOMSource(doc);
            Result result = new StreamResult(new FileWriter(resourceXMLFile));

            NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
            for (int i = 0; i < titleNodeList.getLength(); i++) {
                Element titleElmn = (Element) titleNodeList.item(i);
                NodeList subNodeList = titleElmn.getElementsByTagName(afTABNavigationIndexTag);
                Element subElmn = (Element) subNodeList.item(0);
                // add NEW
                Element clonedElmn = (Element) doc.createElement(afTABNavigationIndexTag);
                subElmn.getParentNode().insertBefore(clonedElmn, subElmn.getNextSibling());
                String navIndexLine = navIndices[0] + " : " + navIndices[1] + " , " + navIndices[2] + " , " + navIndices[3] + " , " + navIndices[4];
                clonedElmn.appendChild(doc.createTextNode(navIndexLine));
                // remove OLD element
                subElmn.getParentNode().removeChild(subElmn);
                tformer.transform(source, result);
            }
        } catch (IOException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateTopicCollType(String collType) {
        try {
            Document doc = readingXMLFromFile(resourceXMLFile);

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer tformer = tFactory.newTransformer();
            Source source = new DOMSource(doc);
            Result result = new StreamResult(new FileWriter(resourceXMLFile));

            NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
            for (int i = 0; i < titleNodeList.getLength(); i++) {
                Element titleElmn = (Element) titleNodeList.item(i);
                NodeList subNodeList = titleElmn.getElementsByTagName(afTopicCollTypeTag);
                Element subElmn = (Element) subNodeList.item(0);
                // add NEW
                Element clonedElmn = (Element) doc.createElement(afTopicCollTypeTag);
                subElmn.getParentNode().insertBefore(clonedElmn, subElmn.getNextSibling());
                clonedElmn.appendChild(doc.createTextNode(collType));
                // remove OLD element
                subElmn.getParentNode().removeChild(subElmn);

                tformer.transform(source, result);
            }

        } catch (IOException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateLinkCollType(String collType) {
        try {
            Document doc = readingXMLFromFile(resourceXMLFile);

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer tformer = tFactory.newTransformer();
            Source source = new DOMSource(doc);
            Result result = new StreamResult(new FileWriter(resourceXMLFile));

            NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
            for (int i = 0; i < titleNodeList.getLength(); i++) {
                Element titleElmn = (Element) titleNodeList.item(i);
                NodeList subNodeList = titleElmn.getElementsByTagName(afLinkCollTypeTag);
                Element subElmn = (Element) subNodeList.item(0);
                // add NEW
                Element clonedElmn = (Element) doc.createElement(afLinkCollTypeTag);
                subElmn.getParentNode().insertBefore(clonedElmn, subElmn.getNextSibling());
                clonedElmn.appendChild(doc.createTextNode(collType));
                // remove OLD element
                subElmn.getParentNode().removeChild(subElmn);

                tformer.transform(source, result);
            }

        } catch (IOException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateCurrTopicID(String currTopicPath) {
        try {
            Document doc = readingXMLFromFile(resourceXMLFile);

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer tformer = tFactory.newTransformer();
            Source source = new DOMSource(doc);
            Result result = new StreamResult(new FileWriter(resourceXMLFile));

            NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
            for (int i = 0; i < titleNodeList.getLength(); i++) {
                Element titleElmn = (Element) titleNodeList.item(i);
                NodeList subNodeList = titleElmn.getElementsByTagName(afCurrTopicTag);
                Element subElmn = (Element) subNodeList.item(0);
                // add NEW
                Element clonedElmn = (Element) doc.createElement(afCurrTopicTag);
                subElmn.getParentNode().insertBefore(clonedElmn, subElmn.getNextSibling());
                clonedElmn.appendChild(doc.createTextNode(currTopicPath));
                // remove OLD element
                subElmn.getParentNode().removeChild(subElmn);

                tformer.transform(source, result);
            }

        } catch (IOException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateCurrAnchorFOL(Vector<String> currAnchorFOLV) {
        try {
            Document doc = readingXMLFromFile(resourceXMLFile);
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer tformer = tFactory.newTransformer();
            Source source = new DOMSource(doc);
            Result result = new StreamResult(new FileWriter(resourceXMLFile));
            NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
            Vector<String> filterElementsV = new Vector<String>();
            for (int i = 0; i < titleNodeList.getLength(); i++) {
                Element titleElmn = (Element) titleNodeList.item(i);
                NodeList subNodeList = titleElmn.getElementsByTagName(afAnchorFOLTag);
                Element subElmn = (Element) subNodeList.item(0);
                NodeList subElmnNodes = subElmn.getChildNodes();
                // Get all Elements
                for (int j = 0; j < subElmnNodes.getLength(); j++) {
                    Node subElmnNode = subElmnNodes.item(j);
                    if (subElmnNode.getNodeType() == Node.ELEMENT_NODE) {
                        String thisSElmnNode = subElmnNodes.item(j).getNodeName();
                        if (!filterElementsV.contains(thisSElmnNode)) {
                            filterElementsV.add(thisSElmnNode);
                        }
                    }
                }
                // Remove These Elements
                for (String thisElmn : filterElementsV) {
                    filterElements(doc, thisElmn);
                }
                for (int j = 0; j < currAnchorFOLV.size(); j++) {
                    String thisAnchorList = currAnchorFOLV.elementAt(j);
                    Element newElmn = (Element) doc.createElement(afAnchorFOLPrefixTag + (j + 1));
                    subElmn.appendChild(newElmn);
                    newElmn.appendChild(doc.createTextNode(thisAnchorList));
                }
                tformer.transform(source, result);
            }
            condenceXml(resourceXMLFile);

        } catch (IOException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateCurrBepOffsetList(Vector<String> bepSetV) {
        try {
            Document doc = readingXMLFromFile(resourceXMLFile);
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer tformer = tFactory.newTransformer();
            Source source = new DOMSource(doc);
            Result result = new StreamResult(new FileWriter(resourceXMLFile));
            NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
            Vector<String> filterElementsV = new Vector<String>();
            for (int i = 0; i < titleNodeList.getLength(); i++) {
                Element titleElmn = (Element) titleNodeList.item(i);
                NodeList subNodeList = titleElmn.getElementsByTagName(afBepOffsetTag);
                Element subElmn = (Element) subNodeList.item(0);
                NodeList subElmnNodes = subElmn.getChildNodes();
                // Get all Elements
                for (int j = 0; j < subElmnNodes.getLength(); j++) {
                    Node subElmnNode = subElmnNodes.item(j);
                    if (subElmnNode.getNodeType() == Node.ELEMENT_NODE) {
                        String thisSElmnNode = subElmnNodes.item(j).getNodeName();
                        if (!filterElementsV.contains(thisSElmnNode)) {
                            filterElementsV.add(thisSElmnNode);
                        }
                    }
                }
                // Remove These Elements
                for (String thisElmn : filterElementsV) {
                    filterElements(doc, thisElmn);
                }
                for (int j = 0; j < bepSetV.size(); j++) {
                    String thisBepList = bepSetV.elementAt(j);
                    Element newElmn = (Element) doc.createElement(afBepOffsetPrefixTag + (j + 1));
                    subElmn.appendChild(newElmn);
                    newElmn.appendChild(doc.createTextNode(thisBepList));
                }
                tformer.transform(source, result);
            }
            condenceXml(resourceXMLFile);

        } catch (IOException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void condenceXml(String xmlFile) {
        try {
            String xmlContent = "";
            BufferedReader br = new BufferedReader(new FileReader(xmlFile));
            String thisLine = "";
            while ((thisLine = br.readLine()) != null) {
                if (thisLine.trim().startsWith("\\n")) {
                    xmlContent = xmlContent + "";
                } else {
                    xmlContent = xmlContent + thisLine.trim();
                }
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(xmlFile));
            bw.write(xmlContent);
            bw.close();
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void filterElements(Node parent, String filter) {
        NodeList children = parent.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (child.getNodeName().equals(filter)) {
                    parent.removeChild(child);
                } else {
                    filterElements(child, filter);
                }
            }
        }
    }
    // </editor-fold>
    // =========================================================================
    // =========================================================================
    // <editor-fold defaultstate="collapsed" desc="Get RSC">
    // -------------------------------------------------------------------------
    private StringBuffer htmlSB = null;

    public String getWikipediaPageTitle(String xmlFileID) {
        String thisPageTitle = "";
        htmlSB = new StringBuffer();
        String xmlPath = this.getWikipediaFilePathByName(xmlFileID + ".xml");
        pageTitleExtractor(xmlPath);
        return thisPageTitle = htmlSB.toString();
    }
    // <editor-fold defaultstate="collapsed" desc="Wikipedia Page Title Finder">
    private String xmlUnicode = "utf-8";

    private void pageTitleExtractor(String xmlPath) {
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(xmlPath));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, xmlUnicode);
            InputSource inputSource = new InputSource(inputStreamReader);

            // Parse the XML File
            DOMParser parser = new DOMParser();
            parser.parse(inputSource);
            Document doc = parser.getDocument();

            pageElmnFinder(doc, htmlSB);

            fileInputStream.close();
            inputStreamReader.close();
        } catch (SAXException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private boolean isTitleFlag = false;

    private void pageElmnFinder(Node node, StringBuffer htmlSB) {

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
    // </editor-fold>
    // TAB/TBA indices

    // <editor-fold defaultstate="collapsed" desc="Get NEXT CURR UNAssessed TAB/TBA & Update NAV Indice">
    public Vector<String[]> getCurrPAnchorUNAssBLinkWithUpdateNAV(String topicID, String[] pAnchorOLSA, String[] linkBepOID) {
        // RETURN V[String[]{anchor_O, L}, String[]{BepLink_O, ID}]
        Vector<String[]> nextTAB = new Vector<String[]>();
        String[] nextTAnchorOL = null;
        String[] nextTABepOID = null;
        // Format: new String[]{0, 1_2_0_0}
        String pAnchorO = pAnchorOLSA[0];
        String pAnchorL = pAnchorOLSA[1];
        String linkBepID = linkBepOID[1];
        // String[]{anchor Offset, Length, Name, arel}
        int pAnchorNewIndex = 0;
        int pABepNewIndex = 0;
        int pAnchorIndex = 0;
        int pABepIndex = 0;
        int pAnchorCounter = 0;
        Vector<String[]> poolAnchorsOLNameStatusVSA = this.getPoolAnchorsOLNameStatusV();
        for (String[] pAnchorOLNameStatus : poolAnchorsOLNameStatusVSA) {
            if (pAnchorO.equals(pAnchorOLNameStatus[0])) {
                pAnchorIndex = pAnchorCounter;
                // -------------------------------------------------------------
                int pABepCounter = 0;
                HashMap<String, Vector<String[]>> anchorBepLinksHM = pooler.getBepSetByAnchor(topicID);
                Vector<String[]> bepLinksOSIDStatusVSA = anchorBepLinksHM.get(pAnchorO + "_" + pAnchorL);
                Hashtable<String, Vector<String[]>> anchorBepLinksOIDStatus = this.getPoolAnchorBepLinksHT();
                Vector<String[]> bepLinksVSA = anchorBepLinksOIDStatus.get(pAnchorO + "_" + pAnchorL);
                for (String[] bepLinksOSIDStatus : bepLinksVSA) {
                    if (linkBepID.equals(bepLinksOSIDStatus[1])) {
                        pABepIndex = pABepCounter;
                        // =====================================================
                        pAnchorNewIndex = pAnchorIndex;
                        pABepNewIndex = pABepIndex;
                        boolean stillFindingFlag = true;
                        do {
                            String[] thisPAOLNameStatus = poolAnchorsOLNameStatusVSA.elementAt(pAnchorNewIndex);
                            String thisPAnchorStatus = this.pooler.getPoolAnchorStatus(topicID, new String[]{thisPAOLNameStatus[0], thisPAOLNameStatus[1]});
                            if (thisPAnchorStatus.equals("0")) {
                                Vector<String[]> thisPABepLinksVSA = anchorBepLinksOIDStatus.get(thisPAOLNameStatus[0] + "_" + thisPAOLNameStatus[1]);
                                String[] thisPABepLinkSet = thisPABepLinksVSA.elementAt(pABepNewIndex);
                                String nextLinkStatus = this.pooler.getPoolAnchorBepLinkStatus(topicID, new String[]{thisPAOLNameStatus[0], thisPAOLNameStatus[1]}, thisPABepLinkSet[1]);
                                if (nextLinkStatus.equals("0")) {
                                    stillFindingFlag = false;
                                }
                            }
                            // -------------------------------------------------
                            if (stillFindingFlag) {
                                if (pABepNewIndex == bepLinksVSA.size() - 1) {
                                    pABepNewIndex = 0;
                                    stillFindingFlag = false;
                                } else if (pABepNewIndex < bepLinksVSA.size() - 1) {
                                    pABepNewIndex = pABepNewIndex + 1;
                                }
                            }
                        } while (stillFindingFlag);
                        // =====================================================
                        // Get PoolAnchor O, L, S, E, Status
                        String[] thisPAnchorOLNameStatus = poolAnchorsOLNameStatusVSA.elementAt(pAnchorIndex);
//                        String[] thisPAnchorSEStatus = this.getTopicAnchorSEStatusByOL(topicID, new String[]{thisPAnchorOLNameStatus[0], thisPAnchorOLNameStatus[1]});
//                        nextTAnchorOL = new String[]{thisPAnchorOLNameStatus[0], thisPAnchorOLNameStatus[1], thisPAnchorSEStatus[0], thisPAnchorSEStatus[1], thisPAnchorOLNameStatus[3]};
                        // Get TargetBepLink
                        // new String[]{tbOffset, tbStartP, tbFileID, tbRel}
                        Vector<String[]> thisBepLinksVSA = anchorBepLinksOIDStatus.get(thisPAnchorOLNameStatus[0] + "_" + thisPAnchorOLNameStatus[1]);
                        String[] thisPABepLinkSet = thisBepLinksVSA.elementAt(pABepNewIndex);
                        String bepLinkStartP = "";
                        for (String[] bepLinksOSIDS : bepLinksOSIDStatusVSA) {
                            if (bepLinksOSIDS[0].equals(thisPABepLinkSet[0])) {
                                bepLinkStartP = bepLinksOSIDS[1];
                            }
                        }
                        String thisPABepLinkStatus = this.pooler.getPoolAnchorBepLinkStatus(topicID, pAnchorOLSA, thisPABepLinkSet[1]);
                        // new String[]{Bep_Offset, StartPoint, ID, Status}
                        nextTABepOID = new String[]{thisPABepLinkSet[0], bepLinkStartP, thisPABepLinkSet[1], thisPABepLinkStatus};

                        nextTAB.add(pAnchorOLSA);
                        nextTAB.add(nextTABepOID);
                    }
                    pABepCounter++;
                }
            }
            pAnchorCounter++;
        }
        this.updateTABNavigationIndex(new String[]{"0", String.valueOf(pAnchorNewIndex), String.valueOf(pABepNewIndex), "0", "0"});

        return nextTAB;
    }

    public Vector<String[]> getCurrPBepUNAssALinkWithUpdateNAV(String topicID, String pBepOSA, String[] linkAnchorOLID) {
        // RETURN V[String[]{anchor_O}, String[]{AnchorLink_O, L, ID}]
        Vector<String[]> nextTBA = new Vector<String[]>();
        String[] nextTBepO = new String[1];
        String[] nextTBAnchorOLID = new String[3];
        // Format: new String[]{0, 1_2_0_0}
        String pBepO = pBepOSA;
        String linkAnchorO = linkAnchorOLID[0];
        String linkAnchorL = linkAnchorOLID[1];
        String linkAnchorID = linkAnchorOLID[2];
        // String[]{anchor Offset, Length, Name, arel}
        int pBepNewIndex = 0;
        int pBAnchorNewIndex = 0;
        int pBepIndex = 0;
        int pBAnchorIndex = 0;
        int pBepCounter = 0;
        Vector<String[]> poolBepOStatusVSA = this.getPoolBEPsOStatusV();
        for (String[] pBepOStatus : poolBepOStatusVSA) {
            if (pBepO.equals(pBepOStatus[0])) {
                pBepIndex = pBepCounter;
                // -------------------------------------------------------------
                int pBAnchorCounter = 0;
                HashMap<String, Vector<String[]>> bepAnchorLinksHM = pooler.getAnchorFileSetByBep(topicID);
                Vector<String[]> anchorLinksVSA = bepAnchorLinksHM.get(pBepO);
                // new String[]{anchor_O, L, Name, ID, Status}
                for (String[] anchorLinksOSIDStatus : anchorLinksVSA) {
                    if (linkAnchorID.equals(anchorLinksOSIDStatus[3]) &&
                            linkAnchorO.equals(anchorLinksOSIDStatus[0])) {
                        pBAnchorIndex = pBAnchorCounter;
                        // =====================================================
                        pBepNewIndex = pBepIndex;
                        pBAnchorNewIndex = pBAnchorIndex;
                        boolean stillFindingFlag = true;
                        do {
                            String[] thisPBepOStatus = poolBepOStatusVSA.elementAt(pBepNewIndex);
                            String thisPAnchorStatus = this.pooler.getPoolBepStatus(topicID, thisPBepOStatus[0]);
                            if (thisPAnchorStatus.equals("0")) {
                                Vector<String[]> thisAnchorLinksVSA = bepAnchorLinksHM.get(thisPBepOStatus[0]);
                                String[] thisPBAnchorLinkSet = thisAnchorLinksVSA.elementAt(pBAnchorNewIndex);
                                String nextLinkStatus = this.pooler.getPoolBepAnchorLinkStatus(topicID, thisPBepOStatus[0], new String[]{thisPBAnchorLinkSet[0], thisPBAnchorLinkSet[1], thisPBAnchorLinkSet[3]});
                                if (nextLinkStatus.equals("0")) {
                                    stillFindingFlag = false;
                                }
                            }
                            // -------------------------------------------------
                            if (stillFindingFlag) {
                                if (pBAnchorNewIndex == anchorLinksVSA.size() - 1) {
                                    pBAnchorNewIndex = 0;
                                    stillFindingFlag = false;
                                } else if (pBAnchorNewIndex < anchorLinksVSA.size() - 1) {
                                    pBAnchorNewIndex = pBAnchorNewIndex + 1;
                                }
                            }
                        } while (stillFindingFlag);
                        // -----------------------------------------------------
                        // Get Pool BEP O, S, Status
                        String[] thisPBepOStatus = poolBepOStatusVSA.elementAt(pBepIndex);
//                        String[] topicBepSStatus = this.getTopicBepSStatusByOL(topicID, new String[]{thisPBepOStatus[0], ""});
//                        nextTBepO = new String[]{thisPBepOStatus[0], topicBepSStatus[0], thisPBepOStatus[1]};
                        // Get Bep-AnchorLink O, L, S, E, ID, Status
                        Vector<String[]> thisAnchorLinksVSA = bepAnchorLinksHM.get(thisPBepOStatus[0]);
                        // Bep(Offset:1114), Vector<String[]{Offset:1538, Length:9, Name:TITLE, ID:123017, Status}+>
                        String[] thisPBAnchorLinkSet = thisAnchorLinksVSA.elementAt(pBAnchorNewIndex);
                        String thisPBAnchorLinkStatus = this.pooler.getPoolBepAnchorLinkStatus(topicID, thisPBepOStatus[0], new String[]{thisPBAnchorLinkSet[0], thisPBAnchorLinkSet[1], thisPBAnchorLinkSet[3]});
                        nextTBAnchorOLID = new String[]{thisPBAnchorLinkSet[0], thisPBAnchorLinkSet[1], thisPBAnchorLinkSet[3], thisPBAnchorLinkStatus};

                        nextTBA.add(nextTBepO);
                        nextTBA.add(nextTBAnchorOLID);
                    }
                    pBAnchorCounter++;
                }
            }
            pBepCounter++;
        }
        this.updateTBANavigationIndex(new String[]{"0", String.valueOf(pBepNewIndex), String.valueOf(pBAnchorNewIndex), "0", "0"});

        return nextTBA;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Get NEXT UNAssessed TAB/TBA & Update NAV Indice">
    public Vector<String[]> getNextUNAssTABWithUpdateNAV(String topicID, String[] pAnchorOLSA, String[] linkBepOID) {
        // RETURN V[String[]{anchor_O, L}, String[]{BepLink_O, ID}]
        Vector<String[]> nextTAB = new Vector<String[]>();
        String[] nextTAnchorOL = null;
        String[] nextTABepOID = null;
        // Format: new String[]{0, 1_2_0_0}
        String pAnchorO = pAnchorOLSA[0];
        String pAnchorL = pAnchorOLSA[1];
        String linkBepID = linkBepOID[1];
//        log("pAnchorO: " + pAnchorO + " --> " + linkBepID);
        // String[]{anchor Offset, Length, Name, arel}
        int pAnchorNewIndex = 0;
        int pABepNewIndex = 0;
        int pAnchorIndex = 0;
        int pABepIndex = 0;
        int pAnchorCounter = 0;
        Vector<String[]> poolAnchorsOLNameStatusVSA = this.getPoolAnchorsOLNameStatusV();
        // -------------------------------------------------------------
        String[] tabNavIndice = this.getTABNavigationIndex();
        String currTABTopicIndex = tabNavIndice[0];
        String currTABLinkIndex = tabNavIndice[1];
        String[] currTABLinkIndice = currTABLinkIndex.split(" , ");
        String currTABPAnchorIndex = currTABLinkIndice[0];
        String currTABPABLinkIndex = currTABLinkIndice[1];
        // -------------------------------------------------------------
        pAnchorIndex = Integer.valueOf(currTABPAnchorIndex);
        Hashtable<String, Vector<String[]>> anchorBepLinksOIDStatus = this.getPoolAnchorBepLinksHT();
        Vector<String[]> bepLinksVSA = anchorBepLinksOIDStatus.get(pAnchorO + "_" + pAnchorL);
        HashMap<String, Vector<String[]>> anchorBepLinksHM = pooler.getBepSetByAnchor(topicID);
        Vector<String[]> bepLinksOSIDStatusVSA = anchorBepLinksHM.get(pAnchorO + "_" + pAnchorL);
        // -------------------------------------------------------------
        pABepIndex = Integer.valueOf(currTABPABLinkIndex);
        // =================================================
        boolean stillFindingFlag = true;
        do {
            if (pABepIndex == bepLinksVSA.size() - 1) {
                if (pAnchorIndex == poolAnchorsOLNameStatusVSA.size() - 1) {
                    pAnchorNewIndex = 0;
                    pABepNewIndex = 0;
                } else if (pAnchorIndex < poolAnchorsOLNameStatusVSA.size() - 1) {
                    pAnchorNewIndex = pAnchorIndex + 1;
                    pABepNewIndex = 0;
                }
            } else if (pABepIndex < bepLinksVSA.size() - 1) {
                pAnchorNewIndex = pAnchorIndex;
                pABepNewIndex = pABepIndex + 1;
            }
            // -------------------------------------------------
            String[] thisPAOLNameStatus = poolAnchorsOLNameStatusVSA.elementAt(pAnchorNewIndex);
            String thisPAnchorStatus = this.pooler.getPoolAnchorStatus(topicID, new String[]{thisPAOLNameStatus[0], thisPAOLNameStatus[1]});
            bepLinksVSA = anchorBepLinksOIDStatus.get(thisPAOLNameStatus[0] + "_" + thisPAOLNameStatus[1]);
            if (thisPAnchorStatus.equals("0")) {
                String[] thisPABepLinkSet = bepLinksVSA.elementAt(pABepNewIndex);
                String nextLinkStatus = this.pooler.getPoolAnchorBepLinkStatus(topicID, new String[]{thisPAOLNameStatus[0], thisPAOLNameStatus[1]}, thisPABepLinkSet[1]);
                if (nextLinkStatus.equals("0")) {
                    stillFindingFlag = false;
                } else {
                    pAnchorIndex = pAnchorNewIndex;
                    pABepIndex = pABepNewIndex;
                }
            } else {
                pAnchorIndex = pAnchorNewIndex;
                pABepIndex = pABepNewIndex;
            }
        } while (stillFindingFlag);
        // =================================================
        // Get PoolAnchor O, L, S, E, Status
        String[] thisPAnchorOLNameStatus = poolAnchorsOLNameStatusVSA.elementAt(pAnchorNewIndex);
        String[] thisPAnchorSEStatus = this.getTopicAnchorSEStatusByOL(topicID, new String[]{thisPAnchorOLNameStatus[0], thisPAnchorOLNameStatus[1]});
        nextTAnchorOL = new String[]{thisPAnchorOLNameStatus[0], thisPAnchorOLNameStatus[1], thisPAnchorSEStatus[0], thisPAnchorSEStatus[1], thisPAnchorOLNameStatus[3]};
        // Get TargetBepLink
        // new String[]{tbOffset, tbStartP, tbFileID, tbRel}
        Vector<String[]> thisBepLinksVSA = anchorBepLinksOIDStatus.get(thisPAnchorOLNameStatus[0] + "_" + thisPAnchorOLNameStatus[1]);
        String[] thisPABepLinkSet = thisBepLinksVSA.elementAt(pABepNewIndex);
        String bepLinkStartP = "";
        for (String[] bepLinksOSIDS : bepLinksOSIDStatusVSA) {
            if (bepLinksOSIDS[0].equals(thisPABepLinkSet[0])) {
                bepLinkStartP = bepLinksOSIDS[1];
            }
        }
        nextTABepOID = new String[]{thisPABepLinkSet[0], bepLinkStartP, thisPABepLinkSet[1], thisPABepLinkSet[2]};

        nextTAB.add(nextTAnchorOL);
        nextTAB.add(nextTABepOID);

        this.updateTABNavigationIndex(new String[]{"0", String.valueOf(pAnchorNewIndex), String.valueOf(pABepNewIndex), "0", "0"});
        return nextTAB;

    }

    public Vector<String[]> getNextUNAssTBAWithUpdateNAV(String topicID, String pBepOSA, String[] linkAnchorOLID) {
        // RETURN V[String[]{anchor_O}, String[]{AnchorLink_O, L, ID}]
        Vector<String[]> nextTBA = new Vector<String[]>();
        String[] nextTBepO = new String[1];
        String[] nextTBAnchorOLID = new String[3];
        // Format: new String[]{0, 1_2_0_0}
        String pBepO = pBepOSA;
        String linkAnchorO = linkAnchorOLID[0];
        String linkAnchorL = linkAnchorOLID[1];
        String linkAnchorID = linkAnchorOLID[2];
        // String[]{anchor Offset, Length, Name, arel}
        int pBepNewIndex = 0;
        int pBAnchorNewIndex = 0;
        int pBepIndex = 0;
        int pBAnchorIndex = 0;
        int pBepCounter = 0;
        Vector<String[]> poolBepOStatusVSA = this.getPoolBEPsOStatusV();
//        for (String[] pBepOStatus : poolBepOStatusVSA) {
//            if (pBepO.equals(pBepOStatus[0])) {
        // -------------------------------------------------------------
        String[] tbaNavIndice = this.getTBANavigationIndex();
        String currTBATopicIndex = tbaNavIndice[0];
        String currTBALinkIndex = tbaNavIndice[1];
        String[] currTBALinkIndice = currTBALinkIndex.split(" , ");
        String currTBAPBepIndex = currTBALinkIndice[0];
        String currTBAPBALinkIndex = currTBALinkIndice[1];
        // -------------------------------------------------------------
        pBepIndex = Integer.valueOf(currTBAPBepIndex);
//        pBepIndex = pBepCounter;
        // -------------------------------------------------------------
        HashMap<String, Vector<String[]>> bepAnchorLinksHM = pooler.getAnchorFileSetByBep(topicID);
        Vector<String[]> anchorLinksVSA = bepAnchorLinksHM.get(pBepO);


        pBAnchorIndex = Integer.valueOf(currTBAPBALinkIndex);

        // =========================================================
        boolean stillFindingFlag = true;
        do {
            if (pBAnchorIndex == anchorLinksVSA.size() - 1) {
                if (pBepIndex == poolBepOStatusVSA.size() - 1) {
                    pBepNewIndex = 0;
                    pBAnchorNewIndex = 0;
                } else if (pBepIndex < poolBepOStatusVSA.size() - 1) {
                    pBepNewIndex = pBepIndex + 1;
                    pBAnchorNewIndex = 0;
                }
            } else if (pBAnchorIndex < anchorLinksVSA.size() - 1) {
                pBepNewIndex = pBepIndex;
                pBAnchorNewIndex = pBAnchorIndex + 1;
            }
            // ---------------------------------------------
            String[] thisPBepOStatus = poolBepOStatusVSA.elementAt(pBepNewIndex);
            String thisPBepStatus = this.pooler.getPoolBepStatus(topicID, thisPBepOStatus[0]);
            anchorLinksVSA = bepAnchorLinksHM.get(thisPBepOStatus[0]);
            if (thisPBepStatus.equals("0")) {
                String[] thisPBAnchorLinkSet = anchorLinksVSA.elementAt(pBAnchorNewIndex);
                String nextLinkStatus = this.pooler.getPoolBepAnchorLinkStatus(topicID, thisPBepOStatus[0], new String[]{thisPBAnchorLinkSet[0], thisPBAnchorLinkSet[1], thisPBAnchorLinkSet[3]});
                if (nextLinkStatus.equals("0")) {
                    stillFindingFlag = false;
                } else {
                    pBAnchorIndex = pBAnchorNewIndex;
                    pBepIndex = pBepNewIndex;
                }
            } else {
                pBAnchorIndex = pBAnchorNewIndex;
                pBepIndex = pBepNewIndex;
            }
        } while (stillFindingFlag);
        // =========================================================

        // Get Pool BEP O, S, Status
        String[] thisPBepOStatus = poolBepOStatusVSA.elementAt(pBepNewIndex);
        String[] topicBepSStatus = this.getTopicBepSStatusByOL(topicID, new String[]{thisPBepOStatus[0], ""});
        nextTBepO = new String[]{thisPBepOStatus[0], topicBepSStatus[0], thisPBepOStatus[1]};
        // Get Bep-AnchorLink O, L, S, E, ID, Status
        Vector<String[]> thisAnchorLinksVSA = bepAnchorLinksHM.get(thisPBepOStatus[0]);
        // Bep(Offset:1114), Vector<String[]{Offset:1538, Length:9, Name:TITLE, ID:123017, Status}+>
        String[] thisPBAnchorLinkSet = thisAnchorLinksVSA.elementAt(pBAnchorNewIndex);
        nextTBAnchorOLID = new String[]{thisPBAnchorLinkSet[0], thisPBAnchorLinkSet[1], thisPBAnchorLinkSet[3], thisPBAnchorLinkSet[4]};

        nextTBA.add(nextTBepO);
        nextTBA.add(nextTBAnchorOLID);

        this.updateTBANavigationIndex(new String[]{"0", String.valueOf(pBepNewIndex), String.valueOf(pBAnchorNewIndex), "0", "0"});
        return nextTBA;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Get PRE TAB/TBA & Update NAV Indice">
    public Vector<String[]> getPreTABWithUpdateNAV(String topicID, String[] pAnchorOLSA, String[] linkBepOID) {
        // RETURN V[String[]{anchor_O, L}, String[]{BepLink_O, ID}]
        Vector<String[]> nextTAB = new Vector<String[]>();
        String[] nextTAnchorOL = null;
        String[] nextTABepOID = null;
        // Format: new String[]{0, 1_2_0_0}
        String pAnchorO = pAnchorOLSA[0];
        String pAnchorL = pAnchorOLSA[1];
        String linkBepID = linkBepOID[1];
        // String[]{anchor Offset, Length, Name, arel}
        int pAnchorNewIndex = 0;
        int pABepNewIndex = 0;
        int pAnchorIndex = 0;
        int pABepIndex = 0;
        int pAnchorCounter = 0;
        Vector<String[]> poolAnchorsOLNameStatusVSA = this.getPoolAnchorsOLNameStatusV();
        for (String[] pAnchorOLNameStatus : poolAnchorsOLNameStatusVSA) {
            if (pAnchorO.equals(pAnchorOLNameStatus[0])) {
                pAnchorIndex = pAnchorCounter;
                // -------------------------------------------------------------
                int pABepCounter = 0;
                HashMap<String, Vector<String[]>> anchorBepLinksHM = pooler.getBepSetByAnchor(topicID);
                Vector<String[]> bepLinksOSIDStatusVSA = anchorBepLinksHM.get(pAnchorO + "_" + pAnchorL);
                Hashtable<String, Vector<String[]>> anchorBepLinksOIDStatus = this.getPoolAnchorBepLinksHT();
                Vector<String[]> bepLinksVSA = anchorBepLinksOIDStatus.get(pAnchorO + "_" + pAnchorL);
                for (String[] bepLinksOSIDStatus : bepLinksVSA) {
                    if (linkBepID.equals(bepLinksOSIDStatus[1])) {
                        pABepIndex = pABepCounter;
                        // =====================================================
                        // Inidcate the Current AB Index --> Get the PRE one
                        // pABepIndex == 0 : Go Back PRE Anchor 's Last BEP link
                        // pABepIndex > 0  : Go Back PRE BEP link
                        if (pABepIndex == 0) {
                            if (pAnchorIndex == 0) {
                                pAnchorNewIndex = 0;
                                pABepNewIndex = 0;
                            } else if (pAnchorIndex > 0) {
                                pAnchorNewIndex = pAnchorIndex - 1;
                                // ---------------------------------------------
                                String[] prePAnchorOLNSt = poolAnchorsOLNameStatusVSA.elementAt(pAnchorNewIndex);
                                Vector<String[]> preABepLinksVSA = anchorBepLinksOIDStatus.get(prePAnchorOLNSt[0] + "_" + prePAnchorOLNSt[1]);
                                // ---------------------------------------------
                                pABepNewIndex = preABepLinksVSA.size() - 1;
                            }
                        } else if (pABepIndex > 0) {
                            pAnchorNewIndex = pAnchorIndex;
                            pABepNewIndex = pABepIndex - 1;
                        }
                        // -----------------------------------------------------
                        // Get PoolAnchor O, L, S, E, Status
                        String[] thisPAnchorOLNameStatus = poolAnchorsOLNameStatusVSA.elementAt(pAnchorNewIndex);
                        String[] thisPAnchorSEStatus = this.getTopicAnchorSEStatusByOL(topicID, new String[]{thisPAnchorOLNameStatus[0], thisPAnchorOLNameStatus[1]});
                        nextTAnchorOL = new String[]{thisPAnchorOLNameStatus[0], thisPAnchorOLNameStatus[1], thisPAnchorSEStatus[0], thisPAnchorSEStatus[1], thisPAnchorOLNameStatus[3]};
                        // Get TargetBepLink
                        // new String[]{tbOffset, tbStartP, tbFileID, tbRel}
                        Vector<String[]> thisBepLinksVSA = anchorBepLinksOIDStatus.get(thisPAnchorOLNameStatus[0] + "_" + thisPAnchorOLNameStatus[1]);
                        String[] thisPABepLinkSet = thisBepLinksVSA.elementAt(pABepNewIndex);
                        String bepLinkStartP = "";
                        for (String[] bepLinksOSIDS : bepLinksOSIDStatusVSA) {
                            if (bepLinksOSIDS[0].equals(thisPABepLinkSet[0])) {
                                bepLinkStartP = bepLinksOSIDS[1];
                            }
                        }
                        nextTABepOID = new String[]{thisPABepLinkSet[0], bepLinkStartP, thisPABepLinkSet[1], thisPABepLinkSet[2]};

                        nextTAB.add(nextTAnchorOL);
                        nextTAB.add(nextTABepOID);
                    }
                    pABepCounter++;
                }
            }
            pAnchorCounter++;
        }
        this.updateTABNavigationIndex(new String[]{"0", String.valueOf(pAnchorNewIndex), String.valueOf(pABepNewIndex), "0", "0"});

        return nextTAB;
    }

    public Vector<String[]> getPreTBAWithUpdateNAV(String topicID, String pBepOSA, String[] linkAnchorOLID) {
        // RETURN V[String[]{anchor_O}, String[]{AnchorLink_O, L, ID}]
        Vector<String[]> nextTBA = new Vector<String[]>();
        String[] nextTBepO = new String[1];
        String[] nextTBAnchorOLID = new String[3];
        // Format: new String[]{0, 1_2_0_0}
        String pBepO = pBepOSA;
        String linkAnchorO = linkAnchorOLID[0];
        String linkAnchorL = linkAnchorOLID[1];
        String linkAnchorID = linkAnchorOLID[2];
        // String[]{anchor Offset, Length, Name, arel}
        int pBepNewIndex = 0;
        int pBAnchorNewIndex = 0;
        int pBepIndex = 0;
        int pBAnchorIndex = 0;
        int pBepCounter = 0;
        Vector<String[]> poolBepOStatusVSA = this.getPoolBEPsOStatusV();
        for (String[] pBepOStatus : poolBepOStatusVSA) {
            if (pBepO.equals(pBepOStatus[0])) {
                pBepIndex = pBepCounter;
                // -------------------------------------------------------------
                int pBAnchorCounter = 0;
                HashMap<String, Vector<String[]>> bepAnchorLinksHM = pooler.getAnchorFileSetByBep(topicID);
                Vector<String[]> anchorLinksVSA = bepAnchorLinksHM.get(pBepO);
                // new String[]{anchor_O, L, Name, ID, Status}
                for (String[] anchorLinksOSIDStatus : anchorLinksVSA) {
                    if (linkAnchorID.equals(anchorLinksOSIDStatus[3]) &&
                            linkAnchorO.equals(anchorLinksOSIDStatus[0])) {
                        pBAnchorIndex = pBAnchorCounter;
                        // =====================================================
                        // Inidcate the Current AB Index --> Get the PRE one
                        // pABepIndex == 0 : Go Back PRE Anchor 's Last BEP link
                        // pABepIndex > 0  : Go Back PRE BEP link
                        if (pBAnchorIndex == 0) {
                            if (pBepIndex == 0) {
                                pBepNewIndex = 0;
                                pBAnchorNewIndex = 0;
                            } else if (pBepIndex > 0) {
                                pBepNewIndex = pBepIndex - 1;
                                // ---------------------------------------------
                                String[] prePBepOStatus = poolBepOStatusVSA.elementAt(pBepNewIndex);
                                Vector<String[]> preALinksVSA = bepAnchorLinksHM.get(prePBepOStatus[0]);
                                // ---------------------------------------------
                                pBAnchorNewIndex = preALinksVSA.size() - 1;
                            }
                        } else if (pBAnchorIndex > 0) {
                            pBepNewIndex = pBepIndex;
                            pBAnchorNewIndex = pBAnchorIndex - 1;
                        }
                        // -----------------------------------------------------
                        // Get Pool BEP O, S, Status
                        String[] thisPBepOStatus = poolBepOStatusVSA.elementAt(pBepNewIndex);
                        String[] topicBepSStatus = this.getTopicBepSStatusByOL(topicID, new String[]{thisPBepOStatus[0], ""});
                        nextTBepO = new String[]{thisPBepOStatus[0], topicBepSStatus[0], thisPBepOStatus[1]};
                        // Get Bep-AnchorLink O, L, S, E, ID, Status
                        Vector<String[]> thisAnchorLinksVSA = bepAnchorLinksHM.get(thisPBepOStatus[0]);
                        // Bep(Offset:1114), Vector<String[]{Offset:1538, Length:9, Name:TITLE, ID:123017, Status}+>
                        String[] thisPBAnchorLinkSet = thisAnchorLinksVSA.elementAt(pBAnchorNewIndex);
                        nextTBAnchorOLID = new String[]{thisPBAnchorLinkSet[0], thisPBAnchorLinkSet[1], thisPBAnchorLinkSet[3], thisPBAnchorLinkSet[4]};

                        nextTBA.add(nextTBepO);
                        nextTBA.add(nextTBAnchorOLID);
                    }
                    pBAnchorCounter++;
                }
            }
            pBepCounter++;
        }
        this.updateTBANavigationIndex(new String[]{"0", String.valueOf(pBepNewIndex), String.valueOf(pBAnchorNewIndex), "0", "0"});

        return nextTBA;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Get NEXT TAB/TBA & Update NAV Indice">
    public Vector<String[]> getNextTABWithUpdateNAV(String topicID, String[] pAnchorOLSA, String[] linkBepOID) {
        // RETURN V[String[]{anchor_O, L}, String[]{BepLink_O, ID}]
        Vector<String[]> nextTAB = new Vector<String[]>();
        String[] nextTAnchorOL = null;
        String[] nextTABepOID = null;
        // Format: new String[]{0, 1_2_0_0}
        String pAnchorO = pAnchorOLSA[0];
        String pAnchorL = pAnchorOLSA[1];
        String linkBepID = linkBepOID[1];
        // String[]{anchor Offset, Length, Name, arel}
        int pAnchorNewIndex = 0;
        int pABepNewIndex = 0;
        int pAnchorIndex = 0;
        int pABepIndex = 0;
        int pAnchorCounter = 0;
        Vector<String[]> poolAnchorsOLNameStatusVSA = this.getPoolAnchorsOLNameStatusV();
        for (String[] pAnchorOLNameStatus : poolAnchorsOLNameStatusVSA) {
            if (pAnchorO.equals(pAnchorOLNameStatus[0])) {
                pAnchorIndex = pAnchorCounter;
                // -------------------------------------------------------------
                int pABepCounter = 0;
                HashMap<String, Vector<String[]>> anchorBepLinksHM = pooler.getBepSetByAnchor(topicID);
                Vector<String[]> bepLinksOSIDStatusVSA = anchorBepLinksHM.get(pAnchorO + "_" + pAnchorL);
//                log("bepLinksOSIDStatusVSA: " + bepLinksOSIDStatusVSA.size());
                Hashtable<String, Vector<String[]>> anchorBepLinksOIDStatus = this.getPoolAnchorBepLinksHT();
                Vector<String[]> bepLinksVSA = anchorBepLinksOIDStatus.get(pAnchorO + "_" + pAnchorL);
//                log("bepLinksVSA: " + bepLinksVSA.size());
                for (String[] bepLinksOSIDStatus : bepLinksVSA) {
                    if (linkBepID.equals(bepLinksOSIDStatus[1])) {
                        pABepIndex = pABepCounter;
                        // =====================================================
                        if (pABepIndex == bepLinksVSA.size() - 1) {
                            if (pAnchorIndex == poolAnchorsOLNameStatusVSA.size() - 1) {
                                pAnchorNewIndex = 0;
                                pABepNewIndex = 0;
                            } else if (pAnchorIndex < poolAnchorsOLNameStatusVSA.size() - 1) {
                                pAnchorNewIndex = pAnchorIndex + 1;
                                pABepNewIndex = 0;
                            }
                        } else if (pABepIndex < bepLinksVSA.size() - 1) {
                            pAnchorNewIndex = pAnchorIndex;
                            pABepNewIndex = pABepIndex + 1;
                        }
                        // -----------------------------------------------------
                        // Get PoolAnchor O, L, S, E, Status
                        String[] thisPAnchorOLNameStatus = poolAnchorsOLNameStatusVSA.elementAt(pAnchorNewIndex);
                        String[] thisPAnchorSEStatus = this.getTopicAnchorSEStatusByOL(topicID, new String[]{thisPAnchorOLNameStatus[0], thisPAnchorOLNameStatus[1]});
                        nextTAnchorOL = new String[]{thisPAnchorOLNameStatus[0], thisPAnchorOLNameStatus[1], thisPAnchorSEStatus[0], thisPAnchorSEStatus[1], thisPAnchorOLNameStatus[3]};
                        // Get TargetBepLink
                        // new String[]{tbOffset, tbStartP, tbFileID, tbRel}
                        Vector<String[]> thisBepLinksVSA = anchorBepLinksOIDStatus.get(thisPAnchorOLNameStatus[0] + "_" + thisPAnchorOLNameStatus[1]);
                        String[] thisPABepLinkSet = thisBepLinksVSA.elementAt(pABepNewIndex);
                        String bepLinkStartP = "";
                        for (String[] bepLinksOSIDS : bepLinksOSIDStatusVSA) {
                            if (bepLinksOSIDS[0].equals(thisPABepLinkSet[0])) {
                                bepLinkStartP = bepLinksOSIDS[1];
                            }
                        }
                        nextTABepOID = new String[]{thisPABepLinkSet[0], bepLinkStartP, thisPABepLinkSet[1], thisPABepLinkSet[2]};

                        nextTAB.add(nextTAnchorOL);
                        nextTAB.add(nextTABepOID);
                    }
                    pABepCounter++;
                }
            }
            pAnchorCounter++;
        }
        this.updateTABNavigationIndex(new String[]{"0", String.valueOf(pAnchorNewIndex), String.valueOf(pABepNewIndex), "0", "0"});

        return nextTAB;
    }

    public Vector<String[]> getNextTBAWithUpdateNAV(String topicID, String pBepOSA, String[] linkAnchorOLID) {
        // RETURN V[String[]{anchor_O}, String[]{AnchorLink_O, L, ID}]
        Vector<String[]> nextTBA = new Vector<String[]>();
        String[] nextTBepO = new String[1];
        String[] nextTBAnchorOLID = new String[3];
        // Format: new String[]{0, 1_2_0_0}
        String pBepO = pBepOSA;
        String linkAnchorO = linkAnchorOLID[0];
        String linkAnchorL = linkAnchorOLID[1];
        String linkAnchorID = linkAnchorOLID[2];
        // String[]{anchor Offset, Length, Name, arel}
        int pBepNewIndex = 0;
        int pBAnchorNewIndex = 0;
        int pBepIndex = 0;
        int pBAnchorIndex = 0;
        int pBepCounter = 0;
        Vector<String[]> poolBepOStatusVSA = this.getPoolBEPsOStatusV();
        for (String[] pBepOStatus : poolBepOStatusVSA) {
            if (pBepO.equals(pBepOStatus[0])) {
                pBepIndex = pBepCounter;
                // -------------------------------------------------------------
                int pBAnchorCounter = 0;
                HashMap<String, Vector<String[]>> bepAnchorLinksHM = pooler.getAnchorFileSetByBep(topicID);
                Vector<String[]> anchorLinksVSA = bepAnchorLinksHM.get(pBepO);
                // new String[]{anchor_O, L, Name, ID, Status}
                for (String[] anchorLinksOSIDStatus : anchorLinksVSA) {
                    if (linkAnchorID.equals(anchorLinksOSIDStatus[3]) &&
                            linkAnchorO.equals(anchorLinksOSIDStatus[0])) {
                        pBAnchorIndex = pBAnchorCounter;
                        // =====================================================
                        if (pBAnchorIndex == anchorLinksVSA.size() - 1) {
                            if (pBepIndex == poolBepOStatusVSA.size() - 1) {
                                pBepNewIndex = 0;
                                pBAnchorNewIndex = 0;
                            } else if (pBepIndex < poolBepOStatusVSA.size() - 1) {
                                pBepNewIndex = pBepIndex + 1;
                                pBAnchorNewIndex = 0;
                            }
                        } else if (pBAnchorIndex < anchorLinksVSA.size() - 1) {
                            pBepNewIndex = pBepIndex;
                            pBAnchorNewIndex = pBAnchorIndex + 1;
                        }
                        // -----------------------------------------------------
                        // Get Pool BEP O, S, Status
                        String[] thisPBepOStatus = poolBepOStatusVSA.elementAt(pBepNewIndex);
                        String[] topicBepSStatus = this.getTopicBepSStatusByOL(topicID, new String[]{thisPBepOStatus[0], ""});
                        nextTBepO = new String[]{thisPBepOStatus[0], topicBepSStatus[0], thisPBepOStatus[1]};
                        // Get Bep-AnchorLink O, L, S, E, ID, Status
                        Vector<String[]> thisAnchorLinksVSA = bepAnchorLinksHM.get(thisPBepOStatus[0]);
                        // Bep(Offset:1114), Vector<String[]{Offset:1538, Length:9, Name:TITLE, ID:123017, Status}+>
                        String[] thisPBAnchorLinkSet = thisAnchorLinksVSA.elementAt(pBAnchorNewIndex);
                        nextTBAnchorOLID = new String[]{thisPBAnchorLinkSet[0], thisPBAnchorLinkSet[1], thisPBAnchorLinkSet[3], thisPBAnchorLinkSet[4]};

                        nextTBA.add(nextTBepO);
                        nextTBA.add(nextTBAnchorOLID);
                    }
                    pBAnchorCounter++;
                }
            }
            pBepCounter++;
        }
        this.updateTBANavigationIndex(new String[]{"0", String.valueOf(pBepNewIndex), String.valueOf(pBAnchorNewIndex), "0", "0"});

        return nextTBA;
    }
    // </editor-fold>

    public Vector<String[]> getPoolAnchorsOLNameStatusV() {
        // anchor String[]{Offset, Length, Name, Status}
        // status: 0 <-- normal, 1 <-- completed, -1 <-- non-relevant
        // Get Sorted Anchor OL
        // Hashtable<String, Vector<String[]>>
        // outgoing : topicID, V<String[]{anchor Offset, Length, Name, arel}>
        topicAllAnchors = pooler.getTopicAllAnchors();
        Vector<String[]> poolAnchorsOLV = topicAllAnchors.elements().nextElement();
        Vector<String[]> sortedPoolAnchorsOLV = sortOLVectorSANumbers(poolAnchorsOLV);
        return sortedPoolAnchorsOLV;
    }

    public Hashtable<String, Vector<String[]>> getPoolAnchorBepLinksHT() {
        Vector<String> targetFileID = new Vector<String>();
        // Pool_Anchor OL, BEPLinks V<String[]{Offset, fileID, tbrel/status}
        Hashtable<String, Vector<String[]>> poolAnchorBepLinksHT = new Hashtable<String, Vector<String[]>>();
        String poolAnchorOL = "";
        Vector<String[]> aBepLinksV = new Vector<String[]>();
        Enumeration topicKeyEnu = poolOutgoingData.keys();
        while (topicKeyEnu.hasMoreElements()) {
            Object topicKey = topicKeyEnu.nextElement();
            String myTopicID = topicKey.toString();
            Hashtable<String, Hashtable<String, Vector<String[]>>> poolAnchorsHT = poolOutgoingData.get(topicKey.toString());
            Enumeration poolAnchorKeys = poolAnchorsHT.keys();
            while (poolAnchorKeys.hasMoreElements()) {
                Object pAnchorKey = poolAnchorKeys.nextElement();
                poolAnchorOL = pAnchorKey.toString();
                String[] pAnchorOL = poolAnchorOL.toString().split("_");
                aBepLinksV = new Vector<String[]>();
                Hashtable<String, Vector<String[]>> anchorsHT = poolAnchorsHT.get(pAnchorKey.toString());
                Enumeration anchorKeys = anchorsHT.keys();
                while (anchorKeys.hasMoreElements()) {
                    Object anchorKey = anchorKeys.nextElement();
                    // V<String[]{Offset, fileID, tbrel/status}
                    Vector<String[]> bepLinksV = anchorsHT.get(anchorKey.toString());
                    // ---------------------------------------------------------
                    /**
                     * used to record fileID to prevent from Duplicated Target File
                     * In our case,
                     * only an insatnce of a file ID should appear under the same Anchor OL,
                     * because we don't show BEP.
                     */
                    targetFileID = new Vector<String>();
//                    Vector<String[]> newBepLinksV = new Vector<String[]>();
                    for (String[] bepLinkSA : bepLinksV) {
                        String thisBepFileID = bepLinkSA[1];
                        if (!targetFileID.contains(thisBepFileID)) {
                            targetFileID.add(thisBepFileID);
//                            String thisBepOffset = bepLinkSA[0];
//                            String thisBepLinkStatus = this.pooler.getPoolAnchorBepLinkStatus(myTopicID, new String[]{pAnchorOL[0], pAnchorOL[1]}, thisBepFileID);
                            aBepLinksV.add(bepLinkSA);
//                            aBepLinksV.add(new String[]{thisBepOffset, thisBepFileID, thisBepLinkStatus});
                        }
                    }
                    // ---------------------------------------------------------
//                    aBepLinksV = newBepLinksV;
                }
                poolAnchorBepLinksHT.put(poolAnchorOL, aBepLinksV);
            }
        }
        return poolAnchorBepLinksHT;
    }

    public Vector<String[]> getPoolBEPsOStatusV() {
        // anchor String[]{Offset, status}
        // status: 0 <-- normal, 1 <-- completed, -1 <-- non-relevant
        // Get Sorted BEP O
        Vector<String[]> poolBEPsOV = topicAllBEPs.elements().nextElement();
        Vector<String[]> sortedPoolBEPsOV = sortOLVectorSANumbers(poolBEPsOV);
        return sortedPoolBEPsOV;
    }

    public Hashtable<String, Vector<String[]>> getPoolBepAnchorLinksHT() {
        // Pool_BEP, AnchorLinks
        Hashtable<String, Vector<String[]>> poolBepAnchorLinksHT = new Hashtable<String, Vector<String[]>>();
        String poolBepO = "";
        Vector<String[]> bAnchorLinksV = new Vector<String[]>();
        Enumeration topicKeyEnu = poolIncomingData.keys();
        while (topicKeyEnu.hasMoreElements()) {
            Object topicKey = topicKeyEnu.nextElement();
            Hashtable<String, Vector<String[]>> poolBEPsHT = poolIncomingData.get(topicKey.toString());
            Enumeration bepKeys = poolBEPsHT.keys();
            while (bepKeys.hasMoreElements()) {
                Object bepKey = bepKeys.nextElement();
                poolBepO = bepKey.toString();
                Vector<String[]> anchorLinksV = poolBEPsHT.get(bepKey.toString());
                // V<String[]{Offset, Length, AName, fileID, barel}
                bAnchorLinksV = anchorLinksV;
                poolBepAnchorLinksHT.put(poolBepO, bAnchorLinksV);
            }
        }
        return poolBepAnchorLinksHT;
    }
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // check / update Anchor - BEP || BEP - Anchor Status
    // 1) Get Topic ID

    public String getTopicID() {
        return getDataByTagName(afTitleTag, afltwTopicsTag);
    }

    public String getPoolAnchorCompletedStatus(String topicID, String[] poolAnchorOL) {
        // RETURN new String[]{Completed, Total}
        String poolBepCompletedStatus = "0";
        int totalCounter = 0;
        int completeCounter = 0;
        int RelCounter = 0;
        int NONRelCounter = 0;

        Vector<String> tABepSetVSA = this.pooler.getPoolAnchorAllLinkStatus(topicID, poolAnchorOL);
        totalCounter = tABepSetVSA.size();
        for (String bepLinkStatus : tABepSetVSA) {
            if (bepLinkStatus.equals("-1")) {
                NONRelCounter++;
                completeCounter++;
            } else if (bepLinkStatus.equals("1")) {
                RelCounter++;
                completeCounter++;
            }
        }
        if (completeCounter == totalCounter) {
            if (NONRelCounter == totalCounter) {
                poolBepCompletedStatus = "-1";
            } else {
                poolBepCompletedStatus = "1";
            }
        }
        return poolBepCompletedStatus;
    }

    public String[] getPoolAnchorCompletedRatio(String topicID, String[] poolAnchorOL) {
        // RETURN new String[]{Completed, Total}
        String[] tabRatio = new String[2];
        int totalCounter = 0;
        int completeCounter = 0;
        // anchor String[]{Offset, Length, Name, Status}
        Vector<String> tABepSetVSA = this.pooler.getPoolAnchorAllLinkStatus(topicID, poolAnchorOL);
        totalCounter = tABepSetVSA.size();
        for (String bepLinkStatus : tABepSetVSA) {
            if (bepLinkStatus.equals("-1")) {
                completeCounter++;
            } else if (bepLinkStatus.equals("1")) {
                completeCounter++;
            }
        }
        tabRatio = new String[]{String.valueOf(completeCounter), String.valueOf(totalCounter)};
        return tabRatio;
    }

    public String[] getTABCompletedRatio() {
        // RETURN new String[]{Completed, Total}
        String[] tabRatio = new String[2];
        int totalCounter = 0;
        int completeCounter = 0;
        // anchor String[]{Offset, Length, Name, Status}
        Vector<String[]> tAnchorsOLNStatusVSA = this.getPoolAnchorsOLNameStatusV();
        // Pool_Anchor OL, BEPLinks V<String[]{Offset, fileID, tbrel/status}
//        Hashtable<String, Vector<String[]>> tABepSetHM = this.getPoolAnchorBepLinksHT();
        for (String[] tAnchorSet : tAnchorsOLNStatusVSA) {
            String[] tAnchorOL = new String[]{tAnchorSet[0], tAnchorSet[1]};
            String pAnchorStatus = this.pooler.getPoolAnchorStatus(this.getTopicID(), tAnchorOL);
            Vector<String> tABepLinkStatusVSA = this.pooler.getPoolAnchorAllLinkStatus(this.getTopicID(), tAnchorOL);
            totalCounter = totalCounter + tABepLinkStatusVSA.size();
            if (pAnchorStatus.equals("-1") || pAnchorStatus.equals("1")) {
                completeCounter = completeCounter + tABepLinkStatusVSA.size();
            } else {
                for (String tABepLinkStatus : tABepLinkStatusVSA) {
                    if (tABepLinkStatus.equals("-1") || tABepLinkStatus.equals("1")) {
                        completeCounter++;
                    }
                }
            }
        }
        tabRatio = new String[]{String.valueOf(completeCounter), String.valueOf(totalCounter)};
        return tabRatio;
    }

    public String getPoolBepCompletedStatus(String topicID, String poolBepOffset) {
        // RETURN new String[]{Completed, Total}
        String poolBepCompletedStatus = "0";
        int totalCounter = 0;
        int completeCounter = 0;
        int RelCounter = 0;
        int NONRelCounter = 0;

        Vector<String> tBAnchorVSA = this.pooler.getPoolBepAllLinksStatusV(topicID, poolBepOffset);
        totalCounter = tBAnchorVSA.size();
        for (String tBAnchorStatus : tBAnchorVSA) {
            if (tBAnchorStatus.equals("-1")) {
                NONRelCounter++;
                completeCounter++;
            } else if (tBAnchorStatus.equals("1")) {
                RelCounter++;
                completeCounter++;
            }
        }
        if (completeCounter == totalCounter) {
            if (NONRelCounter == totalCounter) {
                poolBepCompletedStatus = "-1";
            } else {
                poolBepCompletedStatus = "1";
            }
        }
        return poolBepCompletedStatus;
    }

    public String[] getPoolBepCompletedRatio(String topicID, String poolBepOffset) {
        // RETURN new String[]{Completed, Total}
        String[] tbaRatio = new String[2];
        int totalCounter = 0;
        int completeCounter = 0;

        Vector<String> tBAnchorVSA = this.pooler.getPoolBepAllLinksStatusV(topicID, poolBepOffset);
        totalCounter = tBAnchorVSA.size();
        for (String tBAnchorVS : tBAnchorVSA) {
            String tBAnchorStatus = tBAnchorVS;
            if (tBAnchorStatus.equals("-1") || tBAnchorStatus.equals("1")) {
                completeCounter++;
            }
        }
        tbaRatio = new String[]{String.valueOf(completeCounter), String.valueOf(totalCounter)};
        return tbaRatio;
    }

    public String[] getTBACompletedRatio() {
        // RETURN new String[]{Completed, Total}
        String[] tbaRatio = new String[2];
        int totalCounter = 0;
        int completeCounter = 0;
        // record Topic (incoming : topicFile) -> [0]:Offset
        Vector<String[]> tBepsHT = this.getPoolBEPsOStatusV();
        // Bep(Offset:1114), // V<String[]{Offset, Length, AName, fileID, barel}
        Hashtable<String, Vector<String[]>> tBAnchorSetHM = this.getPoolBepAnchorLinksHT();
        for (String[] tBepSet : tBepsHT) {
            String tBepO = tBepSet[0];
            Vector<String> tBAnchorLinkStatus = this.pooler.getPoolBepAllLinksStatusV(this.getTopicID(), tBepO);
            totalCounter = totalCounter + tBAnchorLinkStatus.size();
            for (String tBepStatus : tBAnchorLinkStatus) {
                if (tBepStatus.equals("-1") || tBepStatus.equals("1")) {
                    completeCounter++;
                }
            }
        }
        tbaRatio = new String[]{String.valueOf(completeCounter), String.valueOf(totalCounter)};
        return tbaRatio;
    }
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // 2) Get Topic Anchor OL & SCR-SE

    public String[] getCurrTopicAnchorOLNameStatusSA() {
        // String[]{Anchor_Offset, Length}
        String[] currTopicAnchorOLNameStatus = new String[2];
        // ---------------------------------------------------------------------
        // 1) ONLY 1 Topic, so topicIndex = 0
        // anchor-bep Index: 0 , 0 , 0 , 0 --> pool_Anchor , bep_Link , non-used , non-used
        String[] tabIndexSA = this.getTABNavigationIndex();
        String topicIndex = tabIndexSA[0].trim();
        String[] abIndex = tabIndexSA[1].trim().split(" , ");
        String poolAnchorIndex = abIndex[0].trim();
        String bepLinkIndex = abIndex[1].trim();
        // ---------------------------------------------------------------------
        // 2) Get current Anchor OL
        // String[]{anchor Offset, Length, Name, arel}
        Vector<String[]> poolAnchorsOLNameStatusVSA = getPoolAnchorsOLNameStatusV();
        String[] currAnchorOLNameStatusSA = poolAnchorsOLNameStatusVSA.elementAt(Integer.valueOf(poolAnchorIndex));
        currTopicAnchorOLNameStatus = new String[]{currAnchorOLNameStatusSA[0], currAnchorOLNameStatusSA[1], currAnchorOLNameStatusSA[2], currAnchorOLNameStatusSA[3]};
        return currTopicAnchorOLNameStatus;
    }

    public String[] getCurrTopicAnchorOLNameSEStatusSA(JTextPane myTextPane, String topicID) {
        // String[]{Anchor_O, L, Name, SP, EP, Status}
        String[] currTopicAnchorOLNameSE = new String[6];
        // String[]{anchor Offset, Length, Name, arel}
        String[] currAnchorOLSA = this.getCurrTopicAnchorOLNameStatusSA();
        String currAnchorO = currAnchorOLSA[0];
        String currAnchorL = currAnchorOLSA[1];
        // SP, EP
        String currAnchorS = "";
        String currAnchorE = "";
        Vector<String> topicAnchorsOLNameSEVS = this.getTopicAnchorsOLNameSEV();
        for (int i = 0; i < topicAnchorsOLNameSEVS.size(); i++) {
            String topicAnchorsOLNameSE = topicAnchorsOLNameSEVS.elementAt(i);
            String[] topicAnchorsOLNameSESA = topicAnchorsOLNameSE.split(" : ");
            if (currAnchorO.equals(topicAnchorsOLNameSESA[0]) && currAnchorL.equals(topicAnchorsOLNameSESA[1])) {
                currAnchorS = topicAnchorsOLNameSESA[3];
                currAnchorE = topicAnchorsOLNameSESA[4];
                break;
            }
        }
        String[] currTopicAnchorSE = new String[]{currAnchorS, currAnchorE};
        currTopicAnchorOLNameSE = new String[]{currAnchorOLSA[0], currAnchorOLSA[1], currAnchorOLSA[2], currTopicAnchorSE[0], currTopicAnchorSE[1], currAnchorOLSA[3]};
        return currTopicAnchorOLNameSE;
    }

    public String[] getCurrTopicATargetOID(JTextPane myLinkTextPane, String topicID) {
        // String[]{Bep_Offset, File_ID}
        String[] currTopicABepIDO = new String[2];
        // ---------------------------------------------------------------------
        // ONLY 1 Topic, so topicIndex = 0
        // anchor-bep Index: 0 , 0 , 0 , 0 --> pool_Anchor , bep_Link , non-used , non-used
        String[] tabIndexSA = this.getTABNavigationIndex();
        String[] abIndex = tabIndexSA[1].trim().split(" , ");
        String bepLinkIndex = abIndex[1].trim();
        // ---------------------------------------------------------------------
        // Get current Anchor SE
        // Pool_Anchor OL, BEPLinks V<String[]{Offset, fileID, tbrel/status}
        String[] currAnchorOLSA = getCurrTopicAnchorOLNameStatusSA();
        String currAnchorOL = currAnchorOLSA[0] + "_" + currAnchorOLSA[1];
        Hashtable<String, Vector<String[]>> poolAnchorBepLinksHT = getPoolAnchorBepLinksHT();
        Vector<String[]> currBepVSA = poolAnchorBepLinksHT.get(currAnchorOL);
        String[] currBepSA = currBepVSA.elementAt(Integer.valueOf(bepLinkIndex));
        String bepOffset = currBepSA[0].trim();
        String bepFileID = currBepSA[1].trim();
        return currTopicABepIDO = new String[]{bepOffset, bepFileID};
    }

    public String[] getCurrTopicABepSIDStatusSA(JTextPane myLinkTextPane, String topicID) {
        // String[]{Bep_SP, File_ID, bep_Status}
        String[] currTopicABepSIDStatus = new String[3];
        // ---------------------------------------------------------------------
        // ONLY 1 Topic, so topicIndex = 0
        // anchor-bep Index: 0 , 0 , 0 , 0 --> pool_Anchor , bep_Link , non-used , non-used
        String[] tabIndexSA = this.getTABNavigationIndex();
        String[] abIndex = tabIndexSA[1].trim().split(" , ");
        String bepLinkIndex = abIndex[1].trim();
        // ---------------------------------------------------------------------
        // Get current Anchor SE
        // Pool_Anchor OL, BEPLinks V<String[]{Offset, fileID, tbrel/status}
        String[] currAnchorOLSA = getCurrTopicAnchorOLNameStatusSA();
        String currAnchorOL = currAnchorOLSA[0] + "_" + currAnchorOLSA[1];
        Hashtable<String, Vector<String[]>> poolAnchorBepLinksHT = getPoolAnchorBepLinksHT();
        Vector<String[]> currBepVSA = poolAnchorBepLinksHT.get(currAnchorOL);
        String[] currBepSA = currBepVSA.elementAt(Integer.valueOf(bepLinkIndex));
        String bepOffset = currBepSA[0].trim();
        String bepFileID = currBepSA[1].trim();
        String bepStatus = currBepSA[2].trim();
        // ---------------------------------------------------------------------
        // Link BEP
        // convery BEP Offset into SCR Start Point
        String currBepSCRS = this.pooler.getPoolAnchorBepLinkStartP(topicID, new String[]{currAnchorOLSA[0], currAnchorOLSA[1]}, bepFileID);
        currTopicABepSIDStatus = new String[]{currBepSCRS, bepFileID, bepStatus};

//        FOLTXTMatcher folMatcher = new FOLTXTMatcher();
//        if (Integer.valueOf(bepOffset) > -1) {
//            boolean isLinkWikipedia = Boolean.valueOf(System.getProperty(sysPropertyIsLinkWikiKey));
//            String currBepSCRS = folMatcher.getBepSCRSP(myLinkTextPane, bepFileID, bepOffset, isLinkWikipedia);
//            currTopicABepSIDStatus = new String[]{currBepSCRS, bepFileID, bepStatus};
//        } else {
//            // in this case, bepOffset = -1 ; bepStatus could be 0 or -1
//            currTopicABepSIDStatus = new String[]{bepOffset, bepFileID, bepStatus};
//        }
        return currTopicABepSIDStatus;
    }

    public String[] getTopicAnchorOLStatusBySE(String topicID, String[] poolAnchorSESA) {
        String[] poolAnchorOLStatus = new String[3];
        String pAnchorS = poolAnchorSESA[0];
        String pAnchorE = poolAnchorSESA[1];
        // String[]{Anchor_Offset, Length, S, E, Status}
        Vector<String[]> topicAnchorSCRStatusVSA = getTopicAnchorOLSEStatusVSA();
        for (String[] topicAnchorSCRStatus : topicAnchorSCRStatusVSA) {
            String poolAnchorS = topicAnchorSCRStatus[2];
            String poolAnchorE = topicAnchorSCRStatus[3];
            if (poolAnchorS.equals(pAnchorS) && poolAnchorE.equals(pAnchorE)) {
                poolAnchorOLStatus = new String[]{topicAnchorSCRStatus[0], topicAnchorSCRStatus[1], topicAnchorSCRStatus[4]};
            }
        }
        return poolAnchorOLStatus;
    }

    public String[] getTopicAnchorSEStatusByOL(String topicID, String[] poolAnchorOLSA) {
        String[] poolAnchorSEStatus = new String[3];
        String pAnchorO = poolAnchorOLSA[0];
        String pAnchorL = poolAnchorOLSA[1];
        // String[]{Anchor_Offset, Length, S, E, Status}
        Vector<String[]> topicAnchorSCRStatusVSA = getTopicAnchorOLSEStatusVSA();
        for (String[] topicAnchorSCRStatus : topicAnchorSCRStatusVSA) {
            String poolAnchorO = topicAnchorSCRStatus[0];
            String poolAnchorL = topicAnchorSCRStatus[1];
            if (poolAnchorO.equals(pAnchorO) && poolAnchorL.equals(pAnchorL)) {
                poolAnchorSEStatus = new String[]{topicAnchorSCRStatus[2], topicAnchorSCRStatus[3], topicAnchorSCRStatus[4]};
            }
        }
        return poolAnchorSEStatus;
    }

    public Vector<String[]> getTopicAnchorOLSEStatusVSA() {
        // String[]{Anchor_Offset, Length, S, E, Status}
        Vector<String[]> topicAnchorSCRStatusVSA = new Vector<String[]>();
        Vector<String> topicAnchorsOLNameSEV = getTopicAnchorsOLNameSEV();
        Vector<String[]> poolAnchorsOLV = getPoolAnchorsOLNameStatusV();
        for (String anchorOLNameSE : topicAnchorsOLNameSEV) {
            String[] anchorSA = anchorOLNameSE.split(" : ");
            for (String[] pAnchorSA : poolAnchorsOLV) {
                String pAnchorOffset = pAnchorSA[0];
                String pAnchorStatus = pAnchorSA[3];
                if (anchorSA[0].trim().equals(pAnchorOffset)) {
                    topicAnchorSCRStatusVSA.add(new String[]{anchorSA[0].trim(), anchorSA[1].trim(), anchorSA[3].trim(), anchorSA[4].trim(), pAnchorStatus});
                }
            }
        }
        return topicAnchorSCRStatusVSA;
    }

    public Vector<String> getTopicAnchorsOLNameSEV() {
        Vector<String> currAnchorsOLSet = new Vector<String>();
        Document doc = readingXMLFromFile(resourceXMLFile);
        NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
        for (int i = 0; i < titleNodeList.getLength(); i++) {
            Element titleElmn = (Element) titleNodeList.item(i);
            NodeList subNodeList = titleElmn.getElementsByTagName(afAnchorFOLTag);
            Element subElmn = (Element) subNodeList.item(0);
            NodeList subElmnNodes = subElmn.getChildNodes();
            for (int j = 0; j < subElmnNodes.getLength(); j++) {
                Node thisSElmnNode = subElmnNodes.item(j);
                if (thisSElmnNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element thisSElmn = (Element) thisSElmnNode;
                    Node firstNode = thisSElmn.getFirstChild();
                    String anchorOLSet = firstNode.getTextContent();
                    if (!currAnchorsOLSet.contains(anchorOLSet)) {
                        currAnchorsOLSet.add(anchorOLSet);
                    }
                }
            }
        }
        return currAnchorsOLSet;
    }

    public Vector<String> getTopicAnchorsSCRSEVS() {
        Vector<String> currAnchorsOL = new Vector<String>();
        Document doc = readingXMLFromFile(resourceXMLFile);
        NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
        for (int i = 0; i < titleNodeList.getLength(); i++) {
            Element titleElmn = (Element) titleNodeList.item(i);
            NodeList subNodeList = titleElmn.getElementsByTagName(afAnchorFOLTag);
            Element subElmn = (Element) subNodeList.item(0);

            NodeList subElmnNodes = subElmn.getChildNodes();
            for (int j = 0; j < subElmnNodes.getLength(); j++) {
                String thisSElmnNode = subElmnNodes.item(j).getNodeName();
                NodeList thisSSElmn = subElmn.getElementsByTagName(thisSElmnNode);
                Element targetElmn = (Element) thisSSElmn.item(0);
                Node firstNode = targetElmn.getFirstChild();
                String[] anchorTxtSet = firstNode.getTextContent().split(" : ");
                // Format: "669_682"
                String thisSCRAnchorPos = anchorTxtSet[3] + "_" + (Integer.valueOf(anchorTxtSet[3]) + Integer.valueOf(anchorTxtSet[4]));
                if (!currAnchorsOL.contains(thisSCRAnchorPos)) {
                    currAnchorsOL.add(thisSCRAnchorPos);
                }
            }
        }
        return currAnchorsOL;
    }

    public Vector<String[]> getTopicAnchorScrSEVSA() {
        Vector<String[]> topicAnchorScrSEVSA = new Vector<String[]>();
        Vector<String> topicAnchorScrSEVS = getTopicAnchorsSCRSEVS();
        for (String AnchorScrSE : topicAnchorScrSEVS) {
            String[] AnchorScrSESA = AnchorScrSE.split("_");
            topicAnchorScrSEVSA.add(AnchorScrSESA);
        }
        return topicAnchorScrSEVSA;
    }
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // 2) Get Topic BEP OL & SCR-SE
    public String getCurrTopicBepOffsetStatus() {
        // String[]{Anchor_Offset, Length}
        String currTopicBepOStatus = "";
        // ---------------------------------------------------------------------
        // 1) ONLY 1 Topic, so topicIndex = 0
        // anchor-bep Index: 0 , 0 , 0 , 0 --> pool_Bep , anchor_Link , non-used , non-used
        String[] tbaIndexSA = this.getTBANavigationIndex();
        String topicIndex = tbaIndexSA[0].trim();
        String[] baIndex = tbaIndexSA[1].trim().split(" , ");
        String poolBepIndex = baIndex[0].trim();
        // ---------------------------------------------------------------------
        // 2) Get current Anchor OL
        // String[]{Offset, status}
        Vector<String[]> poolBepsOVSA = this.getPoolBEPsOStatusV();
        String[] currBepOSA = poolBepsOVSA.elementAt(Integer.valueOf(poolBepIndex));
        return currTopicBepOStatus = currBepOSA[0] + "_" + currBepOSA[1];
    }

    public String getCurrTopicBepOffset() {
        // String[]{Anchor_Offset, Length}
        String currTopicBepOffset = "";
        // ---------------------------------------------------------------------
        // 1) ONLY 1 Topic, so topicIndex = 0
        // anchor-bep Index: 0 , 0 , 0 , 0 --> pool_Bep , anchor_Link , non-used , non-used
        String[] tbaIndexSA = this.getTBANavigationIndex();
        String topicIndex = tbaIndexSA[0].trim();
        String[] baIndex = tbaIndexSA[1].trim().split(" , ");
        String poolBepIndex = baIndex[0].trim();
        // ---------------------------------------------------------------------
        // 2) Get current Anchor OL
        // String[]{Offset, status}
        Vector<String[]> poolBepsOVSA = this.getPoolBEPsOStatusV();
        String[] currBepOSA = poolBepsOVSA.elementAt(Integer.valueOf(poolBepIndex));
        return currTopicBepOffset = currBepOSA[0];
    }

    public String getCurrTopicBepSCRS(JTextPane myTextPane, String topicID) {
        // String[]{Anchor_SP, EP}
        String currTopicBepSP = "";
        // convert OL into SCR SE
        FOLTXTMatcher folMatcher = new FOLTXTMatcher();
        String currBepOffset = this.getCurrTopicBepOffset();
        return currTopicBepSP = folMatcher.getBepSCRSP(myTextPane, topicID, currBepOffset, Boolean.valueOf(System.getProperty(sysPropertyIsTopicWikiKey)));
    }

    public String[] getCurrTopicBTargetOLID(JTextPane myLinkTextPane, String topicID) {
        // String[]{Anchor_Offset, Length, File_ID}
        String[] currTopicBAnchorOLID = new String[3];
        // ---------------------------------------------------------------------
        // ONLY 1 Topic, so topicIndex = 0
        // anchor-bep Index: 0 , 0 , 0 , 0 --> pool_Bep , anchor_Link , non-used , non-used
        String[] tbaIndexSA = this.getTBANavigationIndex();
        String[] baIndex = tbaIndexSA[1].trim().split(" , ");
        String anchorLinkIndex = baIndex[1].trim();
        // ---------------------------------------------------------------------
        // Get current Anchor SE
        // Pool_Bep O, AnchorLinks V<String[]{Offset, Length, AName, fileID, barel}
        String currBepO = this.getCurrTopicBepOffset();
        Hashtable<String, Vector<String[]>> poolBepAnchorLinksHT = this.getPoolBepAnchorLinksHT();
        Vector<String[]> currAnchorVSA = poolBepAnchorLinksHT.get(currBepO.toString());
        String[] currAnchorSA = currAnchorVSA.elementAt(Integer.valueOf(anchorLinkIndex));
        String anchorOffset = currAnchorSA[0].trim();
        String anchorLength = currAnchorSA[1].trim();
        String anchorFileID = currAnchorSA[3].trim();
        return currTopicBAnchorOLID = new String[]{anchorOffset, anchorLength, anchorFileID};
    }

    public String[] getCurrTopicBAnchorSEIDStatus(JTextPane myLinkTextPane, String topicID) {
        // String[]{Anchor_SP, EP, File_ID, Status}
        String[] currTopicBAnchorSEIDStatus = new String[4];
        // ---------------------------------------------------------------------
        // ONLY 1 Topic, so topicIndex = 0
        // anchor-bep Index: 0 , 0 , 0 , 0 --> pool_Anchor , bep_Link , non-used , non-used
        String[] tbaIndexSA = this.getTBANavigationIndex();
        String[] baIndex = tbaIndexSA[1].trim().split(" , ");
        String anchorLinkIndex = baIndex[1].trim();
        // ---------------------------------------------------------------------
        // Get current Anchor SE
        // Pool_Bep O, AnchorLinks V<String[]{Offset, Length, AName, fileID, barel}
        String currBepOffset = this.getCurrTopicBepOffset();
        Hashtable<String, Vector<String[]>> poolBepAnchorLinksHT = this.getPoolBepAnchorLinksHT();
        Vector<String[]> currAnchorVSA = poolBepAnchorLinksHT.get(currBepOffset);
        String[] currAnchorSA = currAnchorVSA.elementAt(Integer.valueOf(anchorLinkIndex));
        String anchorOffset = currAnchorSA[0].trim();
        String anchorLength = currAnchorSA[1].trim();
        String anchorName = currAnchorSA[2].trim();
        String anchorFileID = currAnchorSA[3].trim();
        String anchorStatus = currAnchorSA[4].trim();
        // ---------------------------------------------------------------------
        // Link BEP
        // convery BEP Offset into SCR Start Point
        FOLTXTMatcher folMatcher = new FOLTXTMatcher();
        // Anchor_Name, SP, EP
        String[] targetASA = folMatcher.getSCRAnchorNameSESA(myLinkTextPane, anchorFileID, new String[]{anchorOffset, anchorLength, anchorName});
        currTopicBAnchorSEIDStatus = new String[]{targetASA[1], targetASA[2], anchorFileID, anchorStatus};
        return currTopicBAnchorSEIDStatus;
    }

    public Vector<String> getTopicBepsOSVS() {
        Vector<String> topicBepsOSList = new Vector<String>();
        Document doc = readingXMLFromFile(resourceXMLFile);
        NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
        for (int i = 0; i < titleNodeList.getLength(); i++) {
            Element titleElmn = (Element) titleNodeList.item(i);
            NodeList subNodeList = titleElmn.getElementsByTagName(afBepOffsetTag);
            Element subElmn = (Element) subNodeList.item(0);
            NodeList subElmnNodes = subElmn.getChildNodes();
            for (int j = 0; j < subElmnNodes.getLength(); j++) {
                Node thisSElmnNode = subElmnNodes.item(j);
                if (thisSElmnNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element thisSElmn = (Element) thisSElmnNode;
                    Node firstNode = thisSElmn.getFirstChild();
                    String anchorOLSet = firstNode.getTextContent();
                    if (!topicBepsOSList.contains(anchorOLSet)) {
                        topicBepsOSList.add(anchorOLSet);
                    }
                }
            }
        }
        return topicBepsOSList;
    }

    public Vector<String> getTopicBepsOLVS() {
        Vector<String> topicBepsOList = new Vector<String>();
        Document doc = readingXMLFromFile(resourceXMLFile);
        NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
        for (int i = 0; i < titleNodeList.getLength(); i++) {
            Element titleElmn = (Element) titleNodeList.item(i);
            NodeList subNodeList = titleElmn.getElementsByTagName(afBepOffsetTag);
            Element subElmn = (Element) subNodeList.item(0);
            NodeList subElmnNodes = subElmn.getChildNodes();
            for (int j = 0; j < subElmnNodes.getLength(); j++) {
                String thisSElmnNode = subElmnNodes.item(j).getNodeName();
                NodeList thisSSElmn = subElmn.getElementsByTagName(thisSElmnNode);
                Element targetElmn = (Element) thisSSElmn.item(0);
                Node firstNode = targetElmn.getFirstChild();
                String anchorOSSet = firstNode.getTextContent();
                String[] anchorOSSA = anchorOSSet.split(" : ");
                if (!topicBepsOList.contains(anchorOSSA[0].trim() + "_" + bepLength)) {
                    topicBepsOList.add(anchorOSSA[0].trim() + "_" + bepLength);
                }
            }
        }
        return topicBepsOList;
    }

    public String[] getTopicBepOStatusBySE(String topicID, String[] poolBepSE) {
        // String[]{Bep_Offset, S, Status}
        String[] poolAnchorOLStatus = new String[3];
        String poolBepS = poolBepSE[0];
        // String[]{Bep_Offset, S, Status}
        Vector<String[]> topicBepOSStatusVSA = getTopicBepOSStatusVSA();
        for (String[] topicBepOSStatus : topicBepOSStatusVSA) {
            String thisPBepO = topicBepOSStatus[0];
            String thisPBepS = topicBepOSStatus[1];
            String thisPBepStatus = topicBepOSStatus[2];
            if (thisPBepS.equals(poolBepS)) {
                poolAnchorOLStatus = new String[]{thisPBepO, thisPBepS, thisPBepStatus};
            }
        }
        return poolAnchorOLStatus;
    }

    public String[] getTopicBepSStatusByOL(String topicID, String[] poolBepOL) {
        // String[]{Bep_Offset, S, Status}
        String[] poolAnchorSStatus = new String[2];
        String poolBepO = poolBepOL[0];
        // String[]{Bep_Offset, S, Status}
        Vector<String[]> topicBepOSStatusVSA = getTopicBepOSStatusVSA();
        for (String[] topicBepOSStatus : topicBepOSStatusVSA) {
            String thisPBepO = topicBepOSStatus[0];
            String thisPBepS = topicBepOSStatus[1];
            String thisPBepStatus = topicBepOSStatus[2];
            if (thisPBepO.equals(poolBepO)) {
                poolAnchorSStatus = new String[]{thisPBepS, thisPBepStatus};
            }
        }
        return poolAnchorSStatus;
    }

    public Vector<String[]> getTopicBepOSStatusVSA() {
        // String[]{Bep_Offset, S, Status}
        Vector<String[]> topicBepSCRStatusVSA = new Vector<String[]>();
        // topicBepsOSVS: Offset : SP
        Vector<String> topicBepsOSVS = this.getTopicBepsOSVS();
        // poolAnchorsOLV: bOffset , bStatus
        Vector<String[]> poolBepsOLV = this.getPoolBEPsOStatusV();
        for (String BepOS : topicBepsOSVS) {
            String[] BepOSSA = BepOS.split(" : ");
            for (String[] pBepSA : poolBepsOLV) {
                String pBepOffset = pBepSA[0];
                String pBepStatus = pBepSA[1];
                if (BepOSSA[0].trim().equals(pBepOffset)) {
                    topicBepSCRStatusVSA.add(new String[]{BepOSSA[0].trim(), BepOSSA[1].trim(), pBepStatus});
                }
            }
        }
        return topicBepSCRStatusVSA;
    }

    public Vector<String> getTopicBepsSCRSEVS() {
        Vector<String> topicBepsOList = new Vector<String>();
        Document doc = readingXMLFromFile(resourceXMLFile);
        NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
        for (int i = 0; i < titleNodeList.getLength(); i++) {
            Element titleElmn = (Element) titleNodeList.item(i);
            NodeList subNodeList = titleElmn.getElementsByTagName(afBepOffsetTag);
            Element subElmn = (Element) subNodeList.item(0);
            NodeList subElmnNodes = subElmn.getChildNodes();
            for (int j = 0; j < subElmnNodes.getLength(); j++) {
                String thisSElmnNode = subElmnNodes.item(j).getNodeName();
                NodeList thisSSElmn = subElmn.getElementsByTagName(thisSElmnNode);
                Element targetElmn = (Element) thisSSElmn.item(0);
                Node firstNode = targetElmn.getFirstChild();
                String anchorOSSet = firstNode.getTextContent();
                String[] anchorOSSA = anchorOSSet.split(" : ");
                String anchorE = String.valueOf(Integer.valueOf(anchorOSSA[1].trim()) + Integer.valueOf(bepLength));
                if (!topicBepsOList.contains(anchorOSSA[1].trim() + "_" + anchorE)) {
                    topicBepsOList.add(anchorOSSA[1].trim() + "_" + anchorE);
                }
            }
        }
        return topicBepsOList;
    }

    public Vector<String[]> getTopicBepsSCRSEVSA() {
        Vector<String[]> topicBepsSEVSA = new Vector<String[]>();
        Vector<String> topicBepsSCRSEVS = getTopicBepsSCRSEVS();
        for (String bepsSCRSE : topicBepsSCRSEVS) {
            topicBepsSEVSA.add(bepsSCRSE.split("_"));
        }
        return topicBepsSEVSA;
    }
    // =========================================================================
    // =========================================================================

    public String getFileNotFoundXmlPath() {
        return fileNotFoundXmlPath;
    }

    public String getWikipediaPoolXMLFile() {
        return wikipediaPool;
    }

    public String getTeAraPoolXMLFile() {
        return teAraPool;
    }
    // =========================================================================

    public String[] getOutgoingCompletion() {
        // new String[]{completed, total}
        String[] outgoingCompletion = getDataByTagName(afTitleTag, "outgoingCompletion").split(" : ");
        return outgoingCompletion;
    }

    public String[] getIncomingCompletion() {
        // new String[]{completed, total}
        String[] incomingCompletion = getDataByTagName(afTitleTag, "incomingCompletion").split(" : ");
        return incomingCompletion;
    }

    public String getLinkingMode() {
        return getDataByTagName(afTitleTag, afLinkingModeTag);
    }

    public String[] getTBANavigationIndex() {
        // [0]: Topic, [1]: Anchor, [2]: Subanchor, [3]: BEP
        String[] NavigationIndex = getDataByTagName(afTitleTag, afTBANavigationIndexTag).split(" : ");
        return NavigationIndex;
    }

    public String[] getTABNavigationIndex() {
        // [0]: Topic, [1]: Anchor, [2]: Subanchor, [3]: BEP
        String[] NavigationIndex = getDataByTagName(afTitleTag, afTABNavigationIndexTag).split(" : ");
        return NavigationIndex;
    }

    public String getErrorXmlFilePath(String errMsg) {
        String errorXmlFilePath = "";
        if (errMsg.startsWith(afTasnCollectionErrors)) {
            // errMsg --> afTasnCollectionErrors + " : " + myAFTask + " - " + myAFCollection
            String errTaskColl = errMsg.substring(errMsg.indexOf(" : ") + 3, errMsg.length()).trim();
            String statMsg = "Please check the specification of Task and Collection. <br></br>" +
                    "Task must be either one of them: <br></br>" +
                    "LTW_F2F, LTW_A2B, LTAra_A2B or LTAraTW_A2B.<br></br>" +
                    "Collection must be either one of them or Both in the case of Link TeAra to Wikipedia: <br></br>" +
                    "Wikipedia_2009_Collection or/and TeAra_2009_Collection.<br></br>";
            updateErrorXmlPage("Your specification is: " + errTaskColl, statMsg);
        } else {
            errorXmlFilePath = afErrorsFilePath;
        }
        return errorXmlFilePath;
    }

    private void updateErrorXmlPage(String passedMsg, String statMsg) {
        String errTitleTag = "Errors";
        String errMsgTag = "errMsg";
        String errStatTag = "errStat";
        try {
            Document doc = readingXMLFromFile(afErrorsFilePath);
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer tformer = tFactory.newTransformer();
            Source source = new DOMSource(doc);
            Result result = new StreamResult(new FileWriter(afErrorsFilePath));
            NodeList titleNodeList = doc.getElementsByTagName(errTitleTag);
            for (int i = 0; i < titleNodeList.getLength(); i++) {
                Element titleElmn = (Element) titleNodeList.item(i);

                NodeList errMsgNodeList = titleElmn.getElementsByTagName(errMsgTag);
                Element errMsgElmn = (Element) errMsgNodeList.item(0);
                errMsgElmn.removeChild(errMsgElmn.getFirstChild());
                errMsgElmn.appendChild(doc.createTextNode(passedMsg));

                NodeList errStatNodeList = titleElmn.getElementsByTagName(errStatTag);
                Element errStatElmn = (Element) errStatNodeList.item(0);
                errStatElmn.removeChild(errStatElmn.getFirstChild());
                errStatElmn.appendChild(doc.createTextNode(statMsg));

                tformer.transform(source, result);
            }
        } catch (IOException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String[] getAnchorXmlOLBySCRSet(String[] anchorSCRSet) {
        String anchorSCRSE = anchorSCRSet[0] + "_" + anchorSCRSet[1];
        String[] anchorXmlOL = new String[2];
        Vector<String> anchorOLSet = this.getTopicAnchorsOLNameSEV();
        for (String thisSet : anchorOLSet) {
            String[] thisSetSA = thisSet.split(" : ");
            String thisSCRSE = thisSetSA[3] + "_" + thisSetSA[4];
            if (thisSCRSE.equals(anchorSCRSE)) {
                anchorXmlOL[0] = thisSetSA[0];
                anchorXmlOL[1] = thisSetSA[1];
            }
        }
        return anchorXmlOL;
    }

    public String getWikipediaFilePathByName(String fileName) {
        String WikipediaPathFile = "";
        String WikipediaDir = "";
        String wikipediaFilePath = getTargetFilePathByFileName(WikipediaDir, WikipediaPathFile, fileName);
        return wikipediaFilePath;
    }

    public String getTeAraFilePathByName(String fileName) {
        String TeAraPathFile = getDataByTagName(afTitleTag, afTeAraFilePathTag);
        String TeAraDir = getTeAraCollectionFolder();
        String teAraFilePath = getTargetFilePathByFileName(TeAraDir, TeAraPathFile, fileName);
        return teAraFilePath;
    }

    public String[] getWikipediaFilePathByName(String[] fileNameArray) {
        String WikipediaPathFile = "";
        String WikipediaDir = "";
        Vector<String> filePathV = new Vector<String>();
        for (String thisXmlFile : fileNameArray) {
            filePathV.add(getTargetFilePathByFileName(WikipediaDir, WikipediaPathFile, thisXmlFile));
        }
        String[] wikipediaFilePathArray = (String[]) filePathV.toArray();
        return wikipediaFilePathArray;
    }

    public String[] getTeAraFilePathByName(String[] fileNameArray) {
        String TeAraPathFile = getDataByTagName(afTitleTag, afTeAraFilePathTag);
        String TeAraDir = getTeAraCollectionFolder();
        Vector<String> filePathV = new Vector<String>();
        for (String thisXmlFile : fileNameArray) {
            filePathV.add(getTargetFilePathByFileName(TeAraDir, TeAraPathFile, thisXmlFile));
        }
        String[] teAraFilePathArray = (String[]) filePathV.toArray();
        return teAraFilePathArray;
    }

    private String getTargetFilePathByFileName(String topDir, String pathCollectionFile, String fileName) {
        String thisFileFullPath = "";
        if (pathCollectionFile.equals("")) {
            // IS Wikipedia
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
//            String subFolder = fileName.substring(fileName.length() - 7, fileName.lastIndexOf(".xml"));
            return thisFileFullPath = defaultWikipediaDirectory + subFolder + "/" + fileName;
        } else {
            // IS TeAra
            try {
                BufferedReader br = new BufferedReader(new FileReader(pathCollectionFile));
                String currTopPath = "";
                String thisLine = "";
                while ((thisLine = br.readLine()) != null) {
                    if (thisLine.startsWith("Path : ")) {
                        currTopPath = thisLine.split(" : ")[1].trim();
                    } else {
                        String[] thisFileListSA = thisLine.split(" ; ");
                        List thisL = Arrays.asList(thisFileListSA);
                        Vector thisV = new Vector(thisL);
                        if (thisV.contains(fileName)) {
                            return thisFileFullPath = defaultTeAraDirectory + currTopPath + "/" + fileName;
                        }
                    }
                }
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // Here it should return "File NOT FOUND XML file for display"
        return thisFileFullPath = "FileNotFound.xml";
    }

    public String getWikipediaCollectionFolder() {
        String wikipediaCollectionFolder = getDataByTagName(afTitleTag, afWikipediaCollTag);
        return wikipediaCollectionFolder;
    }

    public String getTeAraCollectionFolder() {
        String teAraCollectionFolder = getDataByTagName(afTitleTag, afTeAraCollTag);
        return teAraCollectionFolder;
    }

    public String getCurrTopicXmlFile() {
        String CurrTopicXmlFile = getDataByTagName(afTitleTag, afCurrTopicTag);
        return CurrTopicXmlFile;
    }

    private String getDataByTagName(String afTitleTag, String thisTagName) {
        String thisTagValue = "";
        Document doc = this.readingXMLFromFile(resourceXMLFile);
        NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);

        for (int i = 0; i < titleNodeList.getLength(); i++) {
            Element titleElmn = (Element) titleNodeList.item(i);
            NodeList subNodeList = titleElmn.getElementsByTagName(thisTagName);
            Element subNode = (Element) subNodeList.item(0);

            thisTagValue = getCharacterDataFromElement(subNode);
        }
        return thisTagValue;
    }

    public String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }

    public Document readingXMLFromFile(String sourceXml) {
        DocumentBuilderFactory dBF = DocumentBuilderFactory.newInstance();
        dBF.setIgnoringComments(true);
        // Ignore the comments present in the XML File when reading the xml
        DocumentBuilder builder = null;
        InputSource input = new InputSource(sourceXml);
        Document doc = null;
        try {
            builder = dBF.newDocumentBuilder();
            doc = builder.parse(input);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;

    }

    // </editor-fold>
    // =========================================================================
    private Vector<String[]> sortOLVectorSANumbers(Vector<String[]> myOLSANumbersV) {
        // Vector<String[]{Anchor_Offset, Length, Status}
        Vector<String[]> mySortedOLNumbersV = new Vector<String[]>();
        int[] thisIntA = new int[myOLSANumbersV.size()];
        for (int i = 0; i < myOLSANumbersV.size(); i++) {
            thisIntA[i] = Integer.valueOf(myOLSANumbersV.elementAt(i)[0]);
        }
        for (int i = 0; i < thisIntA.length; i++) {
            for (int j = i + 1; j < thisIntA.length; j++) {
                if (thisIntA[i] > thisIntA[j]) {
                    int temp = thisIntA[i];
                    thisIntA[i] = thisIntA[j];
                    thisIntA[j] = temp;
                }
            }
        }
        for (int i = 0; i < thisIntA.length; i++) {
            String thisINT = String.valueOf(thisIntA[i]);
            // ------------------------------------------
            for (String[] myOLSA : myOLSANumbersV) {
                if (myOLSA[0].equals(thisINT)) {
                    mySortedOLNumbersV.add(myOLSA);
                    break;
                }
            }
        }
        return mySortedOLNumbersV;
    }

    private Vector<String> sortOLVectorNumbers(Vector<String> myOLNumbersV) {
        int[] thisIntA = new int[myOLNumbersV.size()];
        for (int i = 0; i < myOLNumbersV.size(); i++) {
            String[] myOLNo = myOLNumbersV.elementAt(i).split("_");
            thisIntA[i] = Integer.valueOf(myOLNo[0]);
        }
        for (int i = 0; i < thisIntA.length; i++) {
            for (int j = i + 1; j < thisIntA.length; j++) {
                if (thisIntA[i] > thisIntA[j]) {
                    int temp = thisIntA[i];
                    thisIntA[i] = thisIntA[j];
                    thisIntA[j] = temp;
                }
            }
        }
        Vector<String> mySortedOLNumbersV = new Vector<String>();
        for (int i = 0; i < thisIntA.length; i++) {
            String thisINT = String.valueOf(thisIntA[i]);
            // ------------------------------------------
            for (String myOL : myOLNumbersV) {
                String[] myOLSA = myOL.split("_");
                if (myOLSA[0].equals(thisINT)) {
                    mySortedOLNumbersV.add(myOL);
                    break;
                }
            }
        }
        return mySortedOLNumbersV;
    }

    private Vector<String> sortVectorNumbers(Vector<String> myNumbersV) {
        int[] thisIntA = new int[myNumbersV.size()];
        for (int i = 0; i < myNumbersV.size(); i++) {
            thisIntA[i] = Integer.valueOf(myNumbersV.elementAt(i));
        }
        for (int i = 0; i < thisIntA.length; i++) {
            for (int j = i + 1; j < thisIntA.length; j++) {
                if (thisIntA[i] > thisIntA[j]) {
                    int temp = thisIntA[i];
                    thisIntA[i] = thisIntA[j];
                    thisIntA[j] = temp;
                }
            }
        }
        Vector<String> mySortedNumbersV = new Vector<String>();
        for (int i = 0; i < thisIntA.length; i++) {
            mySortedNumbersV.add(String.valueOf(thisIntA[i]));
        }
        return mySortedNumbersV;
    }
}
