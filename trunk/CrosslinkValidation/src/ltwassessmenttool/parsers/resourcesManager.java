package ltwassessmenttool.parsers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private String wikipediaCollTitle = "";
    private String teAraCollTitle = "";
    private String resourceXMLFile = "";
    private String afTasnCollectionErrors = "";
    private String afErrorsFilePath = "";
    private String afTitleTag = "toolResources";
    private String afTopicCollTypeTag = "topicCollectionType";
    private String afLinkCollTypeTag = "linkCollectionType";
    private String afCurrTopicTag = "currTopicXmlFile";
    private String afPoolPathTag = "poolingXmlFile";
    private String afWikipediaCollTag = "wikipediaCollectionPath";
    private String afTeAraCollTag = "tearaCollectionPath";
    private String afWikipediaFilePathTag = "wikipediaFilePath";
    private String afTeAraFilePathTag = "tearaFilePath";
    private String afltwTopicsTag = "ltwTopics";
    private String afTopicPrefixTag = "topic";
    private String afAnchorFOLTag = "currTopicAnchors";
    private String afAnchorFOLPrefixTag = "anchor";
    private String afBepOffsetTag = "currTopicBeps";
    private String afBepOffsetPrefixTag = "bep";
    private String afTABNavigationIndexTag = "tabNavigationIndex";
    private String afTBANavigationIndexTag = "tbaNavigationIndex";

    private static void log(Object text) {
        System.out.println(text);
    }

    public resourcesManager() {
        // get toolResources XML file Location/Path
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ltwassessmenttool.LTWAssessmentToolApp.class).getContext().getResourceMap(LTWAssessmentToolView.class);
        resourceXMLFile = resourceMap.getString("ResourceXMLFilePath");
        afTasnCollectionErrors = resourceMap.getString("AssFormXml.taskCollectionError");
        afErrorsFilePath = resourceMap.getString("bepFile.ErrorXmlPath");
        wikipediaCollTitle = resourceMap.getString("collectionType.Wikipedia");
        teAraCollTitle = resourceMap.getString("collectionType.TeAra");
    }

    // =========================================================================
    // =========================================================================
    // <editor-fold defaultstate="collapsed" desc="Update RSC">
    
    private void updateResources(Document doc)
    {
        Source source = new DOMSource(doc);
        try {
        	Result result = new StreamResult(new FileWriter(resourceXMLFile));
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer tformer = tFactory.newTransformer();
        	tformer.transform(source, result);
        } catch (IOException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void updateElement(String childTagName, String childTextNode)
    {
        Document doc = readingXMLFromFile(resourceXMLFile);

        NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
        for (int i = 0; i < titleNodeList.getLength(); i++) {
            Element titleElmn = (Element) titleNodeList.item(i);
            NodeList subNodeList = titleElmn.getElementsByTagName(childTagName);
            // add NEW
            Element clonedElmn = (Element) doc.createElement(childTagName);
            clonedElmn.appendChild(doc.createTextNode(childTagName));
            if (childTextNode.length() > 0)
            	 clonedElmn.appendChild(doc.createTextNode(childTextNode));
            
            if (subNodeList.getLength() > 0) {
                Element subElmn = (Element) subNodeList.item(0);
                subElmn.getParentNode().insertBefore(clonedElmn, subElmn.getNextSibling());

                // remove OLD element
                subElmn.getParentNode().removeChild(subElmn);
            }
            else
            	titleElmn.insertBefore(clonedElmn, null);
            
            break; // only the first one
        }        
        updateResources(doc);
    }
    
    public void updateTBANavigationIndex(String[] navIndices) {      
    	updateElement(afTBANavigationIndexTag, navIndices[0] + " : " + navIndices[1] + " , " + navIndices[2] + " , " + navIndices[3] + " , " + navIndices[4]);
    }

    public void updateTABNavigationIndex(String[] navIndices) {
        updateElement(afTABNavigationIndexTag, navIndices[0] + " : " + navIndices[1] + " , " + navIndices[2] + " , " + navIndices[3] + " , " + navIndices[4]);
    }

    public void updateTopicCollType(String collType) {
        updateElement(afTopicCollTypeTag, "");
    }

    public void updateLinkCollType(String collType) {
        updateElement(afLinkCollTypeTag, "");
    }

    public void updateCurrTopicID(String currTopicPath) {
        updateElement(afCurrTopicTag, "");
    }

    public void updateTopicList(Vector<String> topicFilesV) {
            Document doc = readingXMLFromFile(resourceXMLFile);
            NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
            Vector<String> filterElementsV = new Vector<String>();
            for (int i = 0; i < titleNodeList.getLength(); i++) {
                Element titleElmn = (Element) titleNodeList.item(i);
                NodeList subNodeList = titleElmn.getElementsByTagName(afltwTopicsTag);
                Node subNode = subNodeList.item(0);
                Element subElmn = (Element) subNode;
                NodeList subElmnNodes = subElmn.getChildNodes();
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
                for (int j = 0; j < topicFilesV.size(); j++) {
                    String thisAnchorList = topicFilesV.elementAt(j);
                    Element newElmn = (Element) doc.createElement(afTopicPrefixTag + (j + 1));
                    subElmn.appendChild(newElmn);
                    newElmn.appendChild(doc.createTextNode(thisAnchorList));
                }
            }
            condenceXml(resourceXMLFile);
            updateResources(doc);
    }

    public void updateCurrAnchorFOL(Vector<String> currAnchorFOLV) {
            Document doc = readingXMLFromFile(resourceXMLFile);

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
            }
            condenceXml(resourceXMLFile);
            updateResources(doc);
    }

    public void updateCurrBepOffsetList(Vector<String> bepSetV) {
            Document doc = readingXMLFromFile(resourceXMLFile);

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
            }
            condenceXml(resourceXMLFile);
            updateResources(doc);
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

    public void updateWikipediaCollectionDirectory(String newWikipediaCorpusURL) {
        try {
            Document doc = readingXMLFromFile(resourceXMLFile);

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer tformer = tFactory.newTransformer();
            Source source = new DOMSource(doc);
            Result result = new StreamResult(new FileWriter(resourceXMLFile));

            NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
            for (int i = 0; i < titleNodeList.getLength(); i++) {
                Element titleElmn = (Element) titleNodeList.item(i);
                NodeList subNodeList = titleElmn.getElementsByTagName(afWikipediaCollTag);
                
                // add NEW
                Element clonedElmn = (Element) doc.createElement(afWikipediaCollTag);  
                clonedElmn.appendChild(doc.createTextNode(newWikipediaCorpusURL));
                if (subNodeList.getLength() > 0) {
	                Element subElmn = (Element) subNodeList.item(0);

	                subElmn.getParentNode().insertBefore(clonedElmn, subElmn.getNextSibling());   
	                // remove OLD element
	                subElmn.getParentNode().removeChild(subElmn);
	
                } 
                else
                	titleElmn.insertBefore(clonedElmn, null);
                tformer.transform(source, result);
            }

        } catch (IOException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateTeAraCollectionDirectory(String newTeAraCorpusURL) {
        try {
            Document doc = readingXMLFromFile(resourceXMLFile);

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer tformer = tFactory.newTransformer();
            Source source = new DOMSource(doc);
            Result result = new StreamResult(new FileWriter(resourceXMLFile));

            NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
            for (int i = 0; i < titleNodeList.getLength(); i++) {
                Element titleElmn = (Element) titleNodeList.item(i);
                NodeList subNodeList = titleElmn.getElementsByTagName(afTeAraCollTag);
                Element subElmn = (Element) subNodeList.item(0);
                // add NEW
                Element clonedElmn = (Element) doc.createElement(afTeAraCollTag);
                subElmn.getParentNode().insertBefore(clonedElmn, subElmn.getNextSibling());
                clonedElmn.appendChild(doc.createTextNode(newTeAraCorpusURL));
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

    public void updateAFXmlFile(String newAFXmlURL) {
        try {
            Document doc = readingXMLFromFile(resourceXMLFile);

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer tformer = tFactory.newTransformer();
            Source source = new DOMSource(doc);
            Result result = new StreamResult(new FileWriter(resourceXMLFile));

            NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
            for (int i = 0; i < titleNodeList.getLength(); i++) {
                Element titleElmn = (Element) titleNodeList.item(i);
                NodeList subNodeList = titleElmn.getElementsByTagName(afPoolPathTag);
                Element subElmn = (Element) subNodeList.item(0);
                // add NEW
                Element clonedElmn = (Element) doc.createElement(afPoolPathTag);
                subElmn.getParentNode().insertBefore(clonedElmn, subElmn.getNextSibling());
                clonedElmn.appendChild(doc.createTextNode(newAFXmlURL));
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

    public String[] getAnchorXmlOLBySCRSet(String[] anchorSCRSet) {
        String anchorSCRSE = anchorSCRSet[0] + "_" + anchorSCRSet[1];
        String[] anchorXmlOL = new String[2];
        Vector<String> anchorOLSet = this.getCurrAnchorsOLV();
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

    public String getTopicCollType() {
        String TopicCollType = getDataByTagName(afTitleTag, afTopicCollTypeTag);
        return TopicCollType;
    }

    public String getLinkCollType() {
        String LinkCollType = getDataByTagName(afTitleTag, afLinkCollTypeTag);
        return LinkCollType;
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

    public Vector<String> getTopicIDsV() {
        Vector<String> topicIDsV = new Vector<String>();

        Document doc = readingXMLFromFile(resourceXMLFile);
        NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
        for (int i = 0; i < titleNodeList.getLength(); i++) {
            Element titleElmn = (Element) titleNodeList.item(i);
            NodeList subNodeList = titleElmn.getElementsByTagName(afltwTopicsTag);
            Element subElmn = (Element) subNodeList.item(0);

            NodeList subElmnNodes = subElmn.getChildNodes();
            for (int j = 0; j < subElmnNodes.getLength(); j++) {
                String thisSElmnNode = subElmnNodes.item(j).getNodeName();
                NodeList thisSSElmn = subElmn.getElementsByTagName(thisSElmnNode);
                Element targetElmn = (Element) thisSSElmn.item(0);
                Node firstNode = targetElmn.getFirstChild();
                String thisTopicID = firstNode.getTextContent();
                if (!topicIDsV.contains(thisTopicID)) {
                    topicIDsV.add(thisTopicID);
                }
            }
        }
        Collections.sort(topicIDsV);
        return topicIDsV;
    }

    public Vector<String> getCurrBepsOV() {
        Vector<String> currBepsOList = new Vector<String>();

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
                String anchorOLSet = firstNode.getTextContent();
                if (!currBepsOList.contains(anchorOLSet)) {
                    currBepsOList.add(anchorOLSet);
                }
            }
        }
        return currBepsOList;
    }

    public Vector<String> getCurrAnchorsOLV() {
        Vector<String> currAnchorsOLSet = new Vector<String>();

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
                String anchorOLSet = firstNode.getTextContent();
                if (!currAnchorsOLSet.contains(anchorOLSet)) {
                    currAnchorsOLSet.add(anchorOLSet);
                }
            }
        }
        return currAnchorsOLSet;
    }

    public Vector<String> getCurrAnchorsSCROL() {
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
            String subFolder = fileName.substring(fileName.length() - 7, fileName.lastIndexOf(".xml"));
            return thisFileFullPath = File.separator + subFolder + File.separator + fileName;
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
                            return thisFileFullPath = currTopPath + File.separator + fileName;
                        }
                    }
                }
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(resourcesManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // Here it should return "File NOT FOUND XML file for display"
//        log("fileName: " + fileName);
        return thisFileFullPath = "FileNotFound.xml";
    }

    public String getCurrTopicXmlFile() {
        String CurrTopicXmlFile = getDataByTagName(afTitleTag, afCurrTopicTag);
        return CurrTopicXmlFile;
    }

    public String getWikipediaCollectionFolder() {
        String wikipediaCollectionFolder = getDataByTagName(afTitleTag, afWikipediaCollTag);
        return wikipediaCollectionFolder;
    }

    public String getTeAraCollectionFolder() {
        String teAraCollectionFolder = getDataByTagName(afTitleTag, afTeAraCollTag);
        return teAraCollectionFolder;
    }

    public String getPoolXMLFile() {
        String poolXMLFilePath = getDataByTagName(afTitleTag, afPoolPathTag);
        return poolXMLFilePath;
    }

    private String getDataByTagName(String afTitleTag, String thisTagName) {
        String thisTagValue = "";
        Document doc = this.readingXMLFromFile(resourceXMLFile);
        NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);

        for (int i = 0; i < titleNodeList.getLength(); i++) {
            Element titleElmn = (Element) titleNodeList.item(i);
            NodeList subNodeList = titleElmn.getElementsByTagName(thisTagName);
            if (subNodeList.getLength() > 0) {
                Element subNode = (Element) subNodeList.item(0);

                thisTagValue = getCharacterDataFromElement(subNode);
            }
            else
                return "";
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
}
