package crosslink.parsers;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
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

import crosslink.parsers.PoolerManager;

//import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import crosslink.AppResource;
import crosslink.assessment.AssessedAnchor;
import crosslink.assessment.Bep;
import crosslink.assessment.CurrentFocusedAnchor;
import crosslink.assessment.IndexedAnchor;
import crosslink.assessment.InterfaceAnchor;
import crosslink.parsers.FOLTXTMatcher;
import crosslink.parsers.ResourcesManager;
import crosslink.utility.FileUtil;

/**
 * @author Darren HUANG
 */
public class ResourcesManager {
	
	public static final String RESOURCE_MANAGER_FILE = "toolResources.xml";

    private static ResourcesManager instance;
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
//    private String wikipediaPool = "";
    private String teAraPool = "";
    // constants for toolResource XML
    private static String lastSeenSubmissionDirTag = "LastSeenSubmissionFolder";
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
    private Hashtable<String, Vector<IndexedAnchor>> topicAllAnchors = null; //new Hashtable<String, Vector<String[]>>();
    private Hashtable<String, Vector<AssessedAnchor>> topicAllSubanchors = null; 
    private Hashtable<String, Hashtable<String, Hashtable<String, Vector<Bep>>>> poolOutgoingData = null; //new Hashtable<String, Hashtable<String, Hashtable<String, Vector<String[]>>>>();
//    private Hashtable<String, Vector<IndexedAnchor>> topicAllBEPs = new Hashtable<String, Vector<IndexedAnchor>>();
    private Hashtable<String, Hashtable<String, Vector<String[]>>> poolIncomingData = new Hashtable<String, Hashtable<String, Vector<String[]>>>();
    public static PoolerManager pooler = null;
    
	private String wikipediaCollTitle = "";
    private String afTopicPrefixTag = "topic";
    private String afPoolPathTag = "poolingXmlFile";
    
    private Hashtable<String, Vector<Bep>> poolAnchorBepLinksHT = null;
    
    private static final String TEMPLATE_STRING = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
			"<toolResources>\n\n " + "</toolResources>\n";
    
    public static ResourcesManager getInstance() {
        if (instance == null)
            instance = new ResourcesManager();
        return instance;
    }

    private static void log(Object text) {
        System.out.println(text);
    }

    public ResourcesManager() {
        // get toolResources XML file Location/Path
        ////org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(crosslink.ltwassessmentApp.class).getContext().getResourceMap(ltwassessmentView.class);
    	//AppResource.getInstance().setResourceMap(org.jdesktop.application.Application.getInstance(crosslink.ltwassessmentApp.class).getContext().getResourceMap(crosslink.ltwassessmentView.class));
    	init();
    }
    
    public ResourcesManager(org.jdesktop.application.ResourceMap resourceMap) {
    	AppResource.getInstance().setResourceMap(resourceMap);
    	init();
    }
    
    private void init() {
//        resourceXMLFile = AppResource.getInstance().getResourceMap().getString("ResourceXMLFilePath");
//        afTasnCollectionErrors = AppResource.getInstance().getResourceMap().getString("AssFormXml.taskCollectionError");
//        afErrorsFilePath = AppResource.getInstance().getResourceMap().getString("bepFile.ErrorXmlPath");
        //teAraCollTitle = AppResource.getInstance().getResourceMap().getString("collectionType.TeAra");
        
        org.jdesktop.application.ResourceMap resourceMap = AppResource.getInstance().getResourceMap();
        // resource constants for this Class
        resourceXMLFile = resourceMap != null ? resourceMap.getString("ResourceXMLFilePath") : null;
        if (resourceXMLFile == null || resourceXMLFile.length() == 0)
        	resourceXMLFile = "resources" + File.separator + RESOURCE_MANAGER_FILE;
        afTasnCollectionErrors = resourceMap.getString("AssFormXml.taskCollectionError");
        afErrorsFilePath = resourceMap.getString("bepFile.ErrorXmlPath");
        wikipediaCollTitle = resourceMap.getString("collectionType.Wikipedia");
        
        File resourceXMLFileHander = new File(resourceXMLFile);
        if (!resourceXMLFileHander.exists())
			try {
				File alternative = new File("resources" + File.separator + "Tool_Resources" + File.separator + resourceXMLFileHander.getName());
				if (alternative.exists())
					FileUtil.copyFile(alternative, resourceXMLFileHander);
				else
					FileUtil.writeFile(resourceXMLFileHander, TEMPLATE_STRING);
			} catch (IOException e) {
				e.printStackTrace();
			}
        // resource constants for tool setting up property
        fileNotFoundXmlPath = resourceMap.getString("fileNotFound.ReplacedXmlPath");
        defaultWikipediaDirectory = resourceMap.getString("directory.Wikipedia");
//        defaultTeAraDirectory = resourceMap.getString("directory.TeAra");
//        wikipediaPool = resourceMap.getString("pool.wikipedia");
//        teAraPool = resourceMap.getString("pool.teara");     
        
//        if (AppResource.forAssessment)
//        	pooler = PoolerManager.getInstance();
    }
    
    public void pullPoolData() {
      pooler = PoolerManager.getInstance();
      // ---------------------------------------------------------------------
      // Hashtable<String, Hashtable<String, Hashtable<String, Vector<String[]>>>>
      // topicFileID, <PoolAnchor_OL, <Anchor_OL, V<String[]{Offset, fileID, tbrel}
      poolOutgoingData = pooler.getOutgoingPool();
      topicAllAnchors = pooler.getTopicAllAnchors();
      topicAllSubanchors = pooler.getTopicAllSubanchors();
      // Hashtable<String, Vector<String[]>>
      // incoming : topicID, V<String[]{bep Offset, borel}>
//      topicAllBEPs = pooler.getTopicAllBeps();
      // Hashtable<String, Hashtable<String, Vector<String[]>>>
      // topifFileID, <BEP_Offset, V<String[]{Offset, Length, AName, fileID, barel}
//      poolIncomingData = pooler.getIncomingPool();
      setPoolAnchorBepLinksHashtable();
    }
    
    // =========================================================================
    // =========================================================================
    // <editor-fold defaultstate="collapsed" desc="Update RSC">
    public void updateOutgoingCompletion(String outgoingCompletion) {
        updateElement("outgoingCompletion", outgoingCompletion);
    }

    // =========================================================================
    // =========================================================================
    // <editor-fold defaultstate="collapsed" desc="Update RSC">
    
    private void updateResources(Document doc)
    {
        Source source = new DOMSource(doc);
        try {
        	Result result = new StreamResult(new OutputStreamWriter(new FileOutputStream(resourceXMLFile), "UTF-8"));
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer tformer = tFactory.newTransformer();
        	tformer.transform(source, result);
        } catch (IOException ex) {
            Logger.getLogger(ResourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(ResourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private synchronized void updateElement(String childTagName, String childTextNode)
    {
        Document doc = readingXMLFromFile(resourceXMLFile);

        NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
        for (int i = 0; i < titleNodeList.getLength(); i++) {
            Element titleElmn = (Element) titleNodeList.item(i);
            NodeList subNodeList = titleElmn.getElementsByTagName(childTagName);
            // add NEW
            Element clonedElmn = (Element) doc.createElement(childTagName);
           // clonedElmn.appendChild(doc.createTextNode(childTagName));
            if (childTextNode.length() > 0)
            	 clonedElmn.appendChild(doc.createTextNode(childTextNode));
            
            if (subNodeList.getLength() > 0) {
                Element subElmn = (Element) subNodeList.item(0);
                subElmn.getParentNode().insertBefore(clonedElmn, subElmn.getNextSibling());

                // remove OLD element
                subElmn.getParentNode().removeChild(subElmn);
            }
            else
//            	titleElmn.insertBefore(clonedElmn, null);
            	titleElmn.appendChild(clonedElmn);
            
            break; // only the first one
        }        
        updateResources(doc);
    }
    
    public void updateLastSeenSumbmissionDirectory(String dir) {
    	updateElement(lastSeenSubmissionDirTag, dir);
    }
    
    public void updateTBANavigationIndex(String[] navIndices) {      
    	updateElement(afTBANavigationIndexTag, navIndices[0] + " : " + navIndices[1] + " , " + navIndices[2] + " , " + navIndices[3] + " , " + navIndices[4]);
    }

    public void updateTABNavigationIndex(String[] navIndices) {
        updateElement(afTABNavigationIndexTag, navIndices[0] + " : " + navIndices[1] + " , " + navIndices[2] + " , " + navIndices[3] + " , " + navIndices[4]);
    }

    public void updateTopicCollType(String collType) {
        updateElement(afTopicCollTypeTag, collType);
    }

    public void updateLinkCollType(String collType) {
        updateElement(afLinkCollTypeTag, collType);
    }

    public void updateCurrTopicFilePath(String currTopicPath) {
        updateElement(afCurrTopicTag, currTopicPath);
    }
    
    public void updateTopicID(String id) {
        updateElement(afltwTopicsTag, id);
    }

    private void updateElementList(String childTagName, String subChildTagName, Vector<String> list) {
        Document doc = readingXMLFromFile(resourceXMLFile);
        NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
//        Vector<String> filterElementsV = new Vector<String>();
        for (int i = 0; i < titleNodeList.getLength(); i++) {
            Element titleElmn = (Element) titleNodeList.item(i);
            NodeList subNodeList = titleElmn.getElementsByTagName(childTagName);
            Node childNode = null;
            
            for (int j = 0; j < subNodeList.getLength(); j++) {
            	childNode = subNodeList.item(j);
	            childNode.getParentNode().removeChild(childNode);
            }
            Element subElmn = null;
//            if (subNodeList.getLength() > 0) {
//	            Node subNode = subNodeList.item(0);
//	            subElmn = (Element) subNode;
//	            NodeList subElmnNodes = subElmn.getChildNodes();
//
//	            for (int j = 0; j < subElmnNodes.getLength(); j++) {
//	                Node subElmnNode = subElmnNodes.item(j);
//	                
////	                if (list == null)
//	                	subElmn.removeChild(subElmnNode);
////	                else
////		                if (subElmnNode.getNodeType() == Node.ELEMENT_NODE) {
////		                    String thisSElmnNode = subElmnNodes.item(j).getNodeName();
////		                    if (!filterElementsV.contains(thisSElmnNode)) {
////		                        filterElementsV.add(thisSElmnNode);
////		                    }
////		                }
//	            }
//	            
//	            // Remove These Elements
////	            for (String thisElmn : filterElementsV) {
////	                filterElements(doc, thisElmn);
////	            }
//            }
//            else {
            
                subElmn = (Element) doc.createElement(childTagName);
                titleElmn.appendChild(subElmn);
//            }
            if (list != null)
                for (int j = 0; j < list.size(); j++) {
                    String thisAnchorList = list.elementAt(j);
                    Element newElmn = (Element) doc.createElement(subChildTagName + (j + 1));
                    subElmn.appendChild(newElmn);
                    newElmn.appendChild(doc.createTextNode(thisAnchorList));
                }

            break;
        }
        updateResources(doc);
        
        //condenceXml(resourceXMLFile);
    }
    
    public void updateTopicList(Vector<String> topicFilesV) {
        updateElementList(afltwTopicsTag, afTopicPrefixTag, topicFilesV);
    }

    public void updateCurrAnchorFOL(Vector<String> currAnchorFOLV) {
    	updateElementList(afAnchorFOLTag, afAnchorFOLPrefixTag, currAnchorFOLV);
    }

    public void updateCurrBepOffsetList(Vector<String> bepSetV) {
        updateElementList(afBepOffsetTag, afBepOffsetPrefixTag, bepSetV);
    }

    private void condenceXml(String xmlFile) {
        try {
            String xmlContent = "";
            BufferedReader br = new BufferedReader(
        	        new InputStreamReader(new FileInputStream(xmlFile), "UTF-8")); //new BufferedReader(new FileReader(xmlFile));
            String thisLine = "";
            while ((thisLine = br.readLine()) != null) {
                if (thisLine.trim().startsWith("\\n")) {
                    xmlContent = xmlContent + "";
                } else {
                    xmlContent = xmlContent + thisLine.trim();
                }
            }
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
			        new FileOutputStream(xmlFile), "UTF-8")); //new BufferedWriter(new FileWriter(xmlFile));
            bw.write(xmlContent);
            bw.close();
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(ResourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateWikipediaCollectionDirectory(String newWikipediaCorpusURL) {
        updateElement(afWikipediaCollTag, newWikipediaCorpusURL);
    }

    public void updateTeAraCollectionDirectory(String newTeAraCorpusURL) {
        updateElement(afTeAraCollTag, newTeAraCorpusURL);
    }

    public void updateAFXmlFile(String newAFXmlURL) {
        updateElement(afPoolPathTag, newAFXmlURL);
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
    	String result = getDataByTagName(afTitleTag, afTBANavigationIndexTag);
    	if (result.length() == 0)
    		result = "0 : 0 , 0 , 0 , 0";
        String[] NavigationIndex = result.split(" : ");
        return NavigationIndex;
    }

    public String[] getTABNavigationIndex() {
        // [0]: Topic, [1]: Anchor, [2]: Subanchor, [3]: BEP
        String result = getDataByTagName(afTitleTag, afTABNavigationIndexTag);
    	if (result.length() == 0)
    		result = "0 : 0 , 0 , 0 , 0";
        String[] NavigationIndex = result.split(" : ");
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
            Logger.getLogger(ResourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(ResourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Vector<String> getTopicIDsV() {
        Vector<String> topicIDsV = new Vector<String>();

        Document doc = readingXMLFromFile(resourceXMLFile);
        NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
        for (int i = 0; i < titleNodeList.getLength(); i++) {
            Element titleElmn = (Element) titleNodeList.item(i);
            NodeList subNodeList = titleElmn.getElementsByTagName(afltwTopicsTag);
            if (subNodeList.getLength() > 0) {
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
            if (subNodeList.getLength() > 0) {
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

    public String getWikipediaFilePathByName(String fileName, String lang) {
        String WikipediaPathFile = "";
        String WikipediaDir = this.getWikipediaFileFolder(lang);
        String wikipediaFilePath = getTargetFilePathByFileName(WikipediaDir, WikipediaPathFile, fileName);
        return wikipediaFilePath;
    }

//    public String getTeAraFilePathByName(String fileName) {
//        String TeAraPathFile = getDataByTagName(afTitleTag, afTeAraFilePathTag);
//        String TeAraDir = getTeAraCollectionFolder();
//        String teAraFilePath = getTargetFilePathByFileName(TeAraDir, TeAraPathFile, fileName);
//        return teAraFilePath;
//    }

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

//    public String[] getTeAraFilePathByName(String[] fileNameArray) {
//        String TeAraPathFile = getDataByTagName(afTitleTag, afTeAraFilePathTag);
//        String TeAraDir = getTeAraCollectionFolder();
//        Vector<String> filePathV = new Vector<String>();
//        for (String thisXmlFile : fileNameArray) {
//            filePathV.add(getTargetFilePathByFileName(TeAraDir, TeAraPathFile, thisXmlFile));
//        }
//        String[] teAraFilePathArray = (String[]) filePathV.toArray();
//        return teAraFilePathArray;
//    }

    private String getTargetFilePathByFileName(String topDir, String pathCollectionFile, String fileName) {
    	if (fileName.length() <= 4) {
    		System.err.println("Invalid file id: " + fileName);
    		return "FileNotFound.xml";
    	}
        String thisFileFullPath = "";
//        if (pathCollectionFile.equals("")) {
            // IS Wikipedia
            int lastPos = fileName.lastIndexOf(".xml");
            String subFolder;
            if (lastPos < 3)
            	subFolder = String.format("%03d", Integer.parseInt(fileName.substring(0, lastPos)));
            else
            	subFolder = fileName.substring(fileName.length() - 7, lastPos);
            return thisFileFullPath = topDir + subFolder + File.separator + fileName;
//        } else {
//            // IS TeAra
//            try {
//                BufferedReader br = new BufferedReader(new FileReader(pathCollectionFile));
//                String currTopPath = "";
//                String thisLine = "";
//                while ((thisLine = br.readLine()) != null) {
//                    if (thisLine.startsWith("Path : ")) {
//                        currTopPath = thisLine.split(" : ")[1].trim();
//                    } else {
//                        String[] thisFileListSA = thisLine.split(" ; ");
//                        List thisL = Arrays.asList(thisFileListSA);
//                        Vector thisV = new Vector(thisL);
//                        if (thisV.contains(fileName)) {
//                            return thisFileFullPath = currTopPath + File.separator + fileName;
//                        }
//                    }
//                }
//                br.close();
//            } catch (IOException ex) {
//                Logger.getLogger(ResourcesManager.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
        // Here it should return "File NOT FOUND XML file for display"
//        log("fileName: " + fileName);
//        return thisFileFullPath = "FileNotFound.xml";
    }

    public String getCurrTopicXmlFile() {
        String CurrTopicXmlFile = getDataByTagName(afTitleTag, afCurrTopicTag);
        return CurrTopicXmlFile;
    }

    public String getWikipediaCollectionFolder() {
        String wikipediaCollectionFolder = getDataByTagName(afTitleTag, afWikipediaCollTag);
        return wikipediaCollectionFolder;
    }
    
    public String getWikipediaFileFolder(String lang) {
    	String wikiFileDir = getWikipediaCollectionFolder();
//        if (!wikipediaCollectionFolder.endsWith("pages") || !wikipediaCollectionFolder.endsWith("pages" + File.separator))
//        	wikipediaCollectionFolder += File.separator + "pages" + File.separator;
    	return wikiFileDir + File.separator + lang + File.separator + "pages" + File.separator;
    }

//    public String getTeAraCollectionFolder() {
//        String teAraCollectionFolder = getDataByTagName(afTitleTag, afTeAraCollTag);
//        return teAraCollectionFolder;
//    }
    
    public String getLastSeenSubmissionDirectory() {
    	return getDataByTagName(afTitleTag, lastSeenSubmissionDirTag);
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

    public synchronized static Document readingXMLFromFile(String sourceXml) {
        DocumentBuilderFactory dBF = DocumentBuilderFactory.newInstance();
        dBF.setIgnoringComments(true);
        // Ignore the comments present in the XML File when reading the xml
        DocumentBuilder builder = null;
        Document doc = null;
        try {
            InputSource input = new InputSource(new InputStreamReader(new FileInputStream(sourceXml), "UTF-8"));
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
    
    public void updateTABNavIndex(String topicID, IndexedAnchor currSCRSEName, Bep bepInfo) {
    	if (currSCRSEName == null)
    		return;
    	
        // Format: new String[]{0, 1_2_0_0}
        String pAnchorO = currSCRSEName.offsetToString();
        String pAnchorL = currSCRSEName.lengthToString();
        String linkBepID = bepInfo.getFileId();
        // String[]{anchor Offset, Length, Name, arel}
        int pAnchorIndex = 0;
        int pABepIndex = 0;
        int pAnchorCounter = 0;
        Vector<IndexedAnchor> poolAnchorsOLNameStatusVSA = this.getPoolAnchorsOLNameStatusV();
        for (IndexedAnchor pAnchorOLNameStatus : poolAnchorsOLNameStatusVSA) {
            if (pAnchorO.equals(pAnchorOLNameStatus.offsetToString()/*[0]*/)) {
                pAnchorIndex = pAnchorCounter;
                // -------------------------------------------------------------
                int pABepCounter = 0;
//                HashMap<String, Vector<String[]>> anchorBepLinksHM = pooler.getBepSetByAnchor(topicID);
                Hashtable<String, Vector<Bep>> anchorBepLinksHM = this.getPoolAnchorBepLinksHashtable();
                Vector<Bep> bepLinksVSA = anchorBepLinksHM.get(pAnchorO + "_" + pAnchorL);
                for (Bep bepLinksOSIDStatus : bepLinksVSA) {
                    if (linkBepID.equals(bepLinksOSIDStatus.relString()/*[2]*/)) {
                        pABepIndex = pABepCounter;
                    }
                    pABepCounter++;
                }
            }
            pAnchorCounter++;
        }
        this.updateTABNavigationIndex(new String[]{"0", String.valueOf(pAnchorIndex), String.valueOf(pABepIndex), "0", "0"});
    }

//    public void updateTBANavIndex(String topicID, String pBepOSA, String[] linkAnchorOLID) {
//        // Format: new String[]{0, 1_2_0_0}
//        String pBepO = pBepOSA;
//        String linkAnchorO = linkAnchorOLID[0];
//        String linkAnchorL = linkAnchorOLID[1];
//        String linkAnchorID = linkAnchorOLID[2];
//        // String[]{anchor Offset, Length, Name, arel}
//        int pBepIndex = 0;
//        int pBAnchorIndex = 0;
//        int pBepCounter = 0;
//        Vector<String[]> poolBepOStatusVSA = this.getPoolBEPsOStatusV();
//        for (String[] pBepOStatus : poolBepOStatusVSA) {
//            if (pBepO.equals(pBepOStatus[0])) {
//                pBepIndex = pBepCounter;
//                // -------------------------------------------------------------
//                int pBAnchorCounter = 0;
//                HashMap<String, Vector<String[]>> bepAnchorLinksHM = pooler.getAnchorFileSetByBep(topicID);
//                Vector<String[]> anchorLinksVSA = bepAnchorLinksHM.get(pBepO);
//                // new String[]{anchor_O, L, Name, ID, Status}
//                for (String[] anchorLinksOSIDStatus : anchorLinksVSA) {
//                    if (linkAnchorID.equals(anchorLinksOSIDStatus[3]) &&
//                            linkAnchorO.equals(anchorLinksOSIDStatus[0])) {
//                        pBAnchorIndex = pBAnchorCounter;
//                    }
//                    pBAnchorCounter++;
//                }
//            }
//            pBepCounter++;
//        }
//        this.updateTBANavigationIndex(new String[]{"0", String.valueOf(pBepIndex), String.valueOf(pBAnchorIndex), "0", "0"});
//    }

    public void updateLinkingMode(String linkingMode) {
        updateElement(afLinkingModeTag, linkingMode);
    }

    private StringBuffer htmlSB = null;
    
    public String getWikipediaPageTitle(String xmlFileID) {
    	return getWikipediaPageTitle(xmlFileID, AppResource.targetLang);
    }

    public String getWikipediaPageTitle(String xmlFileID, String lang) {
        String thisPageTitle = "";
        htmlSB = new StringBuffer();
        String xmlPath = this.getWikipediaFilePathByName(xmlFileID + ".xml", lang);
        pageTitleExtractor(xmlPath);
        return thisPageTitle = htmlSB.toString();
    }
    // <editor-fold defaultstate="collapsed" desc="Wikipedia Page Title Finder">
    private String xmlUnicode = "UTF-8";

    private void pageTitleExtractor(String xmlPath) {
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(xmlPath));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, xmlUnicode);
            InputSource inputSource = new InputSource(inputStreamReader);

            // Parse the XML File
            com.sun.org.apache.xerces.internal.parsers.DOMParser parser = new com.sun.org.apache.xerces.internal.parsers.DOMParser();
            parser.parse(inputSource);
            Document doc = parser.getDocument();

            pageElmnFinder(doc, htmlSB);

            fileInputStream.close();
            inputStreamReader.close();
        } catch (SAXException ex) {
            Logger.getLogger(ResourcesManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ResourcesManager.class.getName()).log(Level.SEVERE, null, ex);
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
//    public Vector<String[]> getCurrPAnchorUNAssBLinkWithUpdateNAV(String topicID, String[] pAnchorOLSA, String[] linkBepOID) {
//        // RETURN V[String[]{anchor_O, L}, String[]{BepLink_O, ID}]
//        Vector<String[]> nextTAB = new Vector<String[]>();
//        String[] nextTAnchorOL = null;
//        String[] nextTABepOID = null;
//        // Format: new String[]{0, 1_2_0_0}
//        String pAnchorO = pAnchorOLSA[0];
//        String pAnchorL = pAnchorOLSA[1];
//        String linkBepID = linkBepOID[1];
//        // String[]{anchor Offset, Length, Name, arel}
//        int pAnchorNewIndex = 0;
//        int pABepNewIndex = 0;
//        int pAnchorIndex = 0;
//        int pABepIndex = 0;
//        int pAnchorCounter = 0;
//        Vector<IndexedAnchor> poolAnchorsOLNameStatusVSA = this.getPoolAnchorsOLNameStatusV();
//        for (IndexedAnchor pAnchorOLNameStatus : poolAnchorsOLNameStatusVSA) {
//            if (pAnchorO.equals(pAnchorOLNameStatus.offsetToString()/*[0]*/)) {
//                pAnchorIndex = pAnchorCounter;
//                // -------------------------------------------------------------
//                int pABepCounter = 0;
//                HashMap<String, Vector<IndexedAnchor>> anchorBepLinksHM = pooler.getBepSetByAnchor(topicID);
//                Vector<IndexedAnchor> bepLinksOSIDStatusVSA = anchorBepLinksHM.get(pAnchorO + "_" + pAnchorL);
//                Hashtable<String, Vector<Bep>> anchorBepLinksOIDStatus = this.getPoolAnchorBepLinksHashtable();
//                Vector<Bep> bepLinksVSA = anchorBepLinksOIDStatus.get(pAnchorO + "_" + pAnchorL);
//                for (Bep bepLinksOSIDStatus : bepLinksVSA) {
//                    if (linkBepID.equals(bepLinksOSIDStatus.getFileId()/*[1]*/)) {
//                        pABepIndex = pABepCounter;
//                        // =====================================================
//                        pAnchorNewIndex = pAnchorIndex;
//                        pABepNewIndex = bepLinksOSIDStatus.getIndex()/*[9])*/; // pABepIndex;
//                        boolean stillFindingFlag = true;
//                        do {
//                            IndexedAnchor thisPAOLNameStatus = poolAnchorsOLNameStatusVSA.elementAt(pAnchorNewIndex);
//                            String thisPAnchorStatus = this.pooler.getPoolAnchorStatus(topicID, new String[]{thisPAOLNameStatus.offsetToString(), thisPAOLNameStatus.lengthToString()});
//                            if (thisPAnchorStatus.equals("0")) {
//                                Vector<Bep> thisPABepLinksVSA = anchorBepLinksOIDStatus.get(thisPAOLNameStatus.offsetToString() + "_" + thisPAOLNameStatus.lengthToString());
//                                Bep thisPABepLinkSet = thisPABepLinksVSA.elementAt(pABepNewIndex);
//                                String nextLinkStatus = this.pooler.getPoolAnchorBepLinkStatus(topicID, new String[]{thisPAOLNameStatus.offsetToString(), thisPAOLNameStatus.lengthToString()}, thisPABepLinkSet.getFileId()/*[1]*/);
//                                if (nextLinkStatus.equals("0")) {
//                                    stillFindingFlag = false;
//                                }
//                            }
//                            // -------------------------------------------------
//                            if (stillFindingFlag) {
//                                if (pABepNewIndex == bepLinksVSA.size() - 1) {
//                                    pABepNewIndex = 0;
//                                    stillFindingFlag = false;
//                                } else if (pABepNewIndex < bepLinksVSA.size() - 1) {
//                                    pABepNewIndex = pABepNewIndex + 1;
//                                }
//                            }
//                        } while (stillFindingFlag);
//                        // =====================================================
//                        // Get PoolAnchor O, L, S, E, Status
//                        IndexedAnchor thisPAnchorOLNameStatus = poolAnchorsOLNameStatusVSA.elementAt(pAnchorIndex);
////                        String[] thisPAnchorSEStatus = this.getTopicAnchorSEStatusByOL(topicID, new String[]{thisPAnchorOLNameStatus.offsetToString(), thisPAnchorOLNameStatus.lengthToString()});
////                        nextTAnchorOL = new String[]{thisPAnchorOLNameStatus.offsetToString(), thisPAnchorOLNameStatus.lengthToString(), thisPAnchorSEStatus[0], thisPAnchorSEStatus[1], thisPAnchorOLNameStatus[3]};
//                        // Get TargetBepLink
//                        // new String[]{tbOffset, tbStartP, tbFileID, tbRel}
//                        Vector<Bep> thisBepLinksVSA = anchorBepLinksOIDStatus.get(thisPAnchorOLNameStatus.offsetToString() + "_" + thisPAnchorOLNameStatus.lengthToString());
//                        Bep thisPABepLinkSet = thisBepLinksVSA.elementAt(pABepNewIndex);
//                        String bepLinkStartP = "0";
//                        String subAnchorName = null;
//                        String subAnchorOffset = null;
//                        String subAnchorLength = null;
//                        String subAnchorRel = null;
//                        subAnchorName = thisPABepLinkSet.getAssociatedAnchor().getName(); //[5];
//                        subAnchorOffset = thisPABepLinkSet.getAssociatedAnchor().offsetToString(); //[6];
//                        subAnchorLength = thisPABepLinkSet.getAssociatedAnchor().lengthToString(); //[7];
//                        subAnchorRel = thisPABepLinkSet.getAssociatedAnchor().statusToString(); //[8];
//                        boolean stop = false;
//                        for (IndexedAnchor anchor : bepLinksOSIDStatusVSA) {
//                        	for (Bep bepLinksOSIDS : anchor.getBeps()) {
////	                            if (bepLinksOSIDS.offsetToString()/*[0]*/.equals(thisPABepLinkSet.get[0])) {
//                        		if (bepLinksOSIDS.getOffset() == thisPABepLinkSet.getOffset())
//	                                bepLinkStartP = bepLinksOSIDS.startPToString(); //[1];
//                        			stop = true;
//	                                break;
//	                            }
//	                        if (stop)
//	                        	break;
//                        }
//                        nextTABepOID = new String[]{thisPABepLinkSet.offsetToString()/*[0]*/, bepLinkStartP, thisPABepLinkSet.getFileId()/*[1]*/, thisPABepLinkSet.relString()/*[2]*/, thisPABepLinkSet.getTargetLang()/*[3]*/, thisPABepLinkSet.getTargetTitle()/*[4]*/, subAnchorName, subAnchorOffset, subAnchorLength, subAnchorRel};
//
//                        String thisPABepLinkStatus = this.pooler.getPoolAnchorBepLinkStatus(topicID, pAnchorOLSA, thisPABepLinkSet.getFileId()/*[1]*/);
//                        // new String[]{Bep_Offset, StartPoint, ID, Status}
////                        nextTABepOID = new String[]{thisPABepLinkSet[0], bepLinkStartP, thisPABepLinkSet[1], thisPABepLinkStatus, thisPABepLinkSet[3]/*lang*/, thisPABepLinkSet[4]/*title*/};
//
//                        nextTAB.add(pAnchorOLSA);
//                        nextTAB.add(nextTABepOID);
//                        break;
//                    }
//                    pABepCounter++;
//                }
//            }
//            pAnchorCounter++;
//        }
//        this.updateTABNavigationIndex(new String[]{"0", String.valueOf(pAnchorNewIndex), String.valueOf(pABepNewIndex), "0", "0"});
//
//        return nextTAB;
//    }

//    public Vector<String[]> getCurrPBepUNAssALinkWithUpdateNAV(String topicID, String pBepOSA, String[] linkAnchorOLID) {
//        // RETURN V[String[]{anchor_O}, String[]{AnchorLink_O, L, ID}]
//        Vector<String[]> nextTBA = new Vector<String[]>();
//        String[] nextTBepO = new String[1];
//        String[] nextTBAnchorOLID = new String[3];
//        // Format: new String[]{0, 1_2_0_0}
//        String pBepO = pBepOSA;
//        String linkAnchorO = linkAnchorOLID[0];
//        String linkAnchorL = linkAnchorOLID[1];
//        String linkAnchorID = linkAnchorOLID[2];
//        // String[]{anchor Offset, Length, Name, arel}
//        int pBepNewIndex = 0;
//        int pBAnchorNewIndex = 0;
//        int pBepIndex = 0;
//        int pBAnchorIndex = 0;
//        int pBepCounter = 0;
//        Vector<String[]> poolBepOStatusVSA = this.getPoolBEPsOStatusV();
//        for (String[] pBepOStatus : poolBepOStatusVSA) {
//            if (pBepO.equals(pBepOStatus[0])) {
//                pBepIndex = pBepCounter;
//                // -------------------------------------------------------------
//                int pBAnchorCounter = 0;
//                HashMap<String, Vector<String[]>> bepAnchorLinksHM = pooler.getAnchorFileSetByBep(topicID);
//                Vector<String[]> anchorLinksVSA = bepAnchorLinksHM.get(pBepO);
//                // new String[]{anchor_O, L, Name, ID, Status}
//                for (String[] anchorLinksOSIDStatus : anchorLinksVSA) {
//                    if (linkAnchorID.equals(anchorLinksOSIDStatus[3]) &&
//                            linkAnchorO.equals(anchorLinksOSIDStatus[0])) {
//                        pBAnchorIndex = pBAnchorCounter;
//                        // =====================================================
//                        pBepNewIndex = pBepIndex;
//                        pBAnchorNewIndex = pBAnchorIndex;
//                        boolean stillFindingFlag = true;
//                        do {
//                            String[] thisPBepOStatus = poolBepOStatusVSA.elementAt(pBepNewIndex);
//                            String thisPAnchorStatus = this.pooler.getPoolBepStatus(topicID, thisPBepOStatus[0]);
//                            if (thisPAnchorStatus.equals("0")) {
//                                Vector<String[]> thisAnchorLinksVSA = bepAnchorLinksHM.get(thisPBepOStatus[0]);
//                                String[] thisPBAnchorLinkSet = thisAnchorLinksVSA.elementAt(pBAnchorNewIndex);
//                                String nextLinkStatus = this.pooler.getPoolBepAnchorLinkStatus(topicID, thisPBepOStatus[0], new String[]{thisPBAnchorLinkSet[0], thisPBAnchorLinkSet[1], thisPBAnchorLinkSet[3]});
//                                if (nextLinkStatus.equals("0")) {
//                                    stillFindingFlag = false;
//                                }
//                            }
//                            // -------------------------------------------------
//                            if (stillFindingFlag) {
//                                if (pBAnchorNewIndex == anchorLinksVSA.size() - 1) {
//                                    pBAnchorNewIndex = 0;
//                                    stillFindingFlag = false;
//                                } else if (pBAnchorNewIndex < anchorLinksVSA.size() - 1) {
//                                    pBAnchorNewIndex = pBAnchorNewIndex + 1;
//                                }
//                            }
//                        } while (stillFindingFlag);
//                        // -----------------------------------------------------
//                        // Get Pool BEP O, S, Status
//                        String[] thisPBepOStatus = poolBepOStatusVSA.elementAt(pBepIndex);
////                        String[] topicBepSStatus = this.getTopicBepSStatusByOL(topicID, new String[]{thisPBepOStatus[0], ""});
////                        nextTBepO = new String[]{thisPBepOStatus[0], topicBepSStatus[0], thisPBepOStatus[1]};
//                        // Get Bep-AnchorLink O, L, S, E, ID, Status
//                        Vector<String[]> thisAnchorLinksVSA = bepAnchorLinksHM.get(thisPBepOStatus[0]);
//                        // Bep(Offset:1114), Vector<String[]{Offset:1538, Length:9, Name:TITLE, ID:123017, Status}+>
//                        String[] thisPBAnchorLinkSet = thisAnchorLinksVSA.elementAt(pBAnchorNewIndex);
//                        String thisPBAnchorLinkStatus = this.pooler.getPoolBepAnchorLinkStatus(topicID, thisPBepOStatus[0], new String[]{thisPBAnchorLinkSet[0], thisPBAnchorLinkSet[1], thisPBAnchorLinkSet[3]});
//                        nextTBAnchorOLID = new String[]{thisPBAnchorLinkSet[0], thisPBAnchorLinkSet[1], thisPBAnchorLinkSet[2], thisPBAnchorLinkStatus, thisPBAnchorLinkSet[3], thisPBAnchorLinkSet[4]};
//
//                        nextTBA.add(nextTBepO);
//                        nextTBA.add(nextTBAnchorOLID);
//                        break;
//                    }
//                    pBAnchorCounter++;
//                }
//            }
//            pBepCounter++;
//        }
//        this.updateTBANavigationIndex(new String[]{"0", String.valueOf(pBepNewIndex), String.valueOf(pBAnchorNewIndex), "0", "0"});
//
//        return nextTBA;
//    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Get NEXT UNAssessed TAB/TBA & Update NAV Indice">
    public Bep getNextUNAssTABWithUpdateNAV(String topicID, Bep linkBepOID) {
    	return getTABWithUpdateNAV(topicID, linkBepOID, true, true);
    }
//        // RETURN V[String[]{anchor_O, L}, String[]{BepLink_O, ID}]
//        Vector<String[]> nextTAB = new Vector<String[]>();
//        String[] nextTAnchorOL = null;
//        String[] nextTABepOID = null;
//        // Format: new String[]{0, 1_2_0_0}
//        String pAnchorO = pAnchorOLSA[0];
//        String pAnchorL = pAnchorOLSA[1];
//        String linkBepID = linkBepOID[1];
////        log("pAnchorO: " + pAnchorO + " --> " + linkBepID);
//        // String[]{anchor Offset, Length, Name, arel}
//        int pAnchorNewIndex = 0;
//        int pABepNewIndex = 0;
//        int pAnchorIndex = 0;
//        int pABepIndex = 0;
//        int pAnchorCounter = 0;
//        Vector<IndexedAnchor> poolAnchorsOLNameStatusVSA = this.getPoolAnchorsOLNameStatusV();
//        // -------------------------------------------------------------
//        String[] tabNavIndice = this.getTABNavigationIndex();
//        String currTABTopicIndex = tabNavIndice[0];
//        String currTABLinkIndex = tabNavIndice[1];
//        String[] currTABLinkIndice = currTABLinkIndex.split(" , ");
//        String currTABPAnchorIndex = currTABLinkIndice[0];
//        String currTABPABLinkIndex = currTABLinkIndice[1];
//        // -------------------------------------------------------------
//        pAnchorIndex = Integer.valueOf(currTABPAnchorIndex);
//        Hashtable<String, Vector<Bep>> anchorBepLinksOIDStatus = this.getPoolAnchorBepLinksHT();
//        Vector<Bep> bepLinksVSA = anchorBepLinksOIDStatus.get(pAnchorO + "_" + pAnchorL);
//        HashMap<String, Vector<IndexedAnchor>> anchorBepLinksHM = pooler.getBepSetByAnchor(topicID);
//        Vector<IndexedAnchor> bepLinksOSIDStatusVSA = anchorBepLinksHM.get(pAnchorO + "_" + pAnchorL);
//        // -------------------------------------------------------------
//        pABepIndex = Integer.valueOf(currTABPABLinkIndex);
//        // =================================================
//        boolean stillFindingFlag = true;
//        do {
//            if (pABepIndex == bepLinksVSA.size() - 1) {
//                if (pAnchorIndex == poolAnchorsOLNameStatusVSA.size() - 1) {
//                    pAnchorNewIndex = 0;
//                    pABepNewIndex = 0;
//                } else if (pAnchorIndex < poolAnchorsOLNameStatusVSA.size() - 1) {
//                    pAnchorNewIndex = pAnchorIndex + 1;
//                    pABepNewIndex = 0;
//                }
//            } else if (pABepIndex < bepLinksVSA.size() - 1) {
//                pAnchorNewIndex = pAnchorIndex;
//                pABepNewIndex = pABepIndex + 1;
//            }
//            // -------------------------------------------------
//            IndexedAnchor thisPAOLNameStatus = poolAnchorsOLNameStatusVSA.elementAt(pAnchorNewIndex);
//            String thisPAnchorStatus = this.pooler.getPoolAnchorStatus(topicID, new String[]{thisPAOLNameStatus.offsetToString(), thisPAOLNameStatus.lengthToString()});
//            bepLinksVSA = anchorBepLinksOIDStatus.get(thisPAOLNameStatus.offsetToString() + "_" + thisPAOLNameStatus.lengthToString());
//            if (thisPAnchorStatus.equals("0")) {
//                Bep thisPABepLinkSet = bepLinksVSA.elementAt(pABepNewIndex);
//                String nextLinkStatus = this.pooler.getPoolAnchorBepLinkStatus(topicID, new String[]{thisPAOLNameStatus.offsetToString(), thisPAOLNameStatus.lengthToString()}, thisPABepLinkSet.getFileId()/*[1]*/);
//                if (nextLinkStatus.equals("0")) {
//                    stillFindingFlag = false;
//                } else {
//                    pAnchorIndex = pAnchorNewIndex;
//                    pABepIndex = pABepNewIndex;
//                }
//            } else {
//                pAnchorIndex = pAnchorNewIndex;
//                pABepIndex = pABepNewIndex;
//            }
//        } while (stillFindingFlag);
//        // =================================================
//        // Get PoolAnchor O, L, S, E, Status
//        IndexedAnchor thisPAnchorOLNameStatus = poolAnchorsOLNameStatusVSA.elementAt(pAnchorNewIndex);
//        String[] thisPAnchorSEStatus = this.getTopicAnchorSEStatusByOL(topicID, new String[]{thisPAnchorOLNameStatus.offsetToString(), thisPAnchorOLNameStatus.lengthToString()});
//        nextTAnchorOL = new String[]{thisPAnchorOLNameStatus.offsetToString(), thisPAnchorOLNameStatus.lengthToString(), thisPAnchorSEStatus[0], thisPAnchorSEStatus[1], thisPAnchorOLNameStatus.statusToString()};
//        // Get TargetBepLink
//        // new String[]{tbOffset, tbStartP, tbFileID, tbRel}
//        Vector<Bep> thisBepLinksVSA = anchorBepLinksOIDStatus.get(thisPAnchorOLNameStatus.offsetToString() + "_" + thisPAnchorOLNameStatus.lengthToString());
//        Bep thisPABepLinkSet = thisBepLinksVSA.elementAt(pABepNewIndex);
//        String bepLinkStartP = "0";
//        String subAnchorName = null;
//        String subAnchorOffset = null;
//        String subAnchorLength = null;
//        String subAnchorRel = null;
//        subAnchorName = thisPABepLinkSet[5];
//        subAnchorOffset = thisPABepLinkSet[6];
//        subAnchorLength = thisPABepLinkSet[7];
//        subAnchorRel = thisPABepLinkSet[8];
////        for (String[] bepLinksOSIDS : bepLinksOSIDStatusVSA) {
////            if (bepLinksOSIDS[0].equals(thisPABepLinkSet[0])) {
////                bepLinkStartP = bepLinksOSIDS[1];
////                break;
////            }
////        }
//        nextTABepOID = new String[]{thisPABepLinkSet[0], bepLinkStartP, thisPABepLinkSet[1], thisPABepLinkSet[2], thisPABepLinkSet[3], thisPABepLinkSet[4], subAnchorName, subAnchorOffset, subAnchorLength, subAnchorRel};
//
//        nextTAB.add(nextTAnchorOL);
//        nextTAB.add(nextTABepOID);
//
//        this.updateTABNavigationIndex(new String[]{"0", String.valueOf(pAnchorNewIndex), String.valueOf(pABepNewIndex), "0", "0"});
//        return nextTAB;
//
//    }

//    public Vector<String[]> getNextUNAssTBAWithUpdateNAV(String topicID, String pBepOSA, String[] linkAnchorOLID) {
//        // RETURN V[String[]{anchor_O}, String[]{AnchorLink_O, L, ID}]
//        Vector<String[]> nextTBA = new Vector<String[]>();
//        String[] nextTBepO = new String[1];
//        String[] nextTBAnchorOLID = new String[3];
//        // Format: new String[]{0, 1_2_0_0}
//        String pBepO = pBepOSA;
//        String linkAnchorO = linkAnchorOLID[0];
//        String linkAnchorL = linkAnchorOLID[1];
//        String linkAnchorID = linkAnchorOLID[2];
//        // String[]{anchor Offset, Length, Name, arel}
//        int pBepNewIndex = 0;
//        int pBAnchorNewIndex = 0;
//        int pBepIndex = 0;
//        int pBAnchorIndex = 0;
//        int pBepCounter = 0;
//        Vector<String[]> poolBepOStatusVSA = this.getPoolBEPsOStatusV();
////        for (String[] pBepOStatus : poolBepOStatusVSA) {
////            if (pBepO.equals(pBepOStatus[0])) {
//        // -------------------------------------------------------------
//        String[] tbaNavIndice = this.getTBANavigationIndex();
//        String currTBATopicIndex = tbaNavIndice[0];
//        String currTBALinkIndex = tbaNavIndice[1];
//        String[] currTBALinkIndice = currTBALinkIndex.split(" , ");
//        String currTBAPBepIndex = currTBALinkIndice[0];
//        String currTBAPBALinkIndex = currTBALinkIndice[1];
//        // -------------------------------------------------------------
//        pBepIndex = Integer.valueOf(currTBAPBepIndex);
////        pBepIndex = pBepCounter;
//        // -------------------------------------------------------------
//        HashMap<String, Vector<String[]>> bepAnchorLinksHM = pooler.getAnchorFileSetByBep(topicID);
//        Vector<String[]> anchorLinksVSA = bepAnchorLinksHM.get(pBepO);
//
//
//        pBAnchorIndex = Integer.valueOf(currTBAPBALinkIndex);
//
//        // =========================================================
//        boolean stillFindingFlag = true;
//        do {
//            if (pBAnchorIndex == anchorLinksVSA.size() - 1) {
//                if (pBepIndex == poolBepOStatusVSA.size() - 1) {
//                    pBepNewIndex = 0;
//                    pBAnchorNewIndex = 0;
//                } else if (pBepIndex < poolBepOStatusVSA.size() - 1) {
//                    pBepNewIndex = pBepIndex + 1;
//                    pBAnchorNewIndex = 0;
//                }
//            } else if (pBAnchorIndex < anchorLinksVSA.size() - 1) {
//                pBepNewIndex = pBepIndex;
//                pBAnchorNewIndex = pBAnchorIndex + 1;
//            }
//            // ---------------------------------------------
//            String[] thisPBepOStatus = poolBepOStatusVSA.elementAt(pBepNewIndex);
//            String thisPBepStatus = this.pooler.getPoolBepStatus(topicID, thisPBepOStatus[0]);
//            anchorLinksVSA = bepAnchorLinksHM.get(thisPBepOStatus[0]);
//            if (thisPBepStatus.equals("0")) {
//                String[] thisPBAnchorLinkSet = anchorLinksVSA.elementAt(pBAnchorNewIndex);
//                String nextLinkStatus = this.pooler.getPoolBepAnchorLinkStatus(topicID, thisPBepOStatus[0], new String[]{thisPBAnchorLinkSet[0], thisPBAnchorLinkSet[1], thisPBAnchorLinkSet[3]});
//                if (nextLinkStatus.equals("0")) {
//                    stillFindingFlag = false;
//                } else {
//                    pBAnchorIndex = pBAnchorNewIndex;
//                    pBepIndex = pBepNewIndex;
//                }
//            } else {
//                pBAnchorIndex = pBAnchorNewIndex;
//                pBepIndex = pBepNewIndex;
//            }
//        } while (stillFindingFlag);
//        // =========================================================
//
//        // Get Pool BEP O, S, Status
//        String[] thisPBepOStatus = poolBepOStatusVSA.elementAt(pBepNewIndex);
//        String[] topicBepSStatus = this.getTopicBepSStatusByOL(topicID, new String[]{thisPBepOStatus[0], ""});
//        nextTBepO = new String[]{thisPBepOStatus[0], topicBepSStatus[0], thisPBepOStatus[1]};
//        // Get Bep-AnchorLink O, L, S, E, ID, Status
//        Vector<String[]> thisAnchorLinksVSA = bepAnchorLinksHM.get(thisPBepOStatus[0]);
//        // Bep(Offset:1114), Vector<String[]{Offset:1538, Length:9, Name:TITLE, ID:123017, Status}+>
//        String[] thisPBAnchorLinkSet = thisAnchorLinksVSA.elementAt(pBAnchorNewIndex);
//        nextTBAnchorOLID = new String[]{thisPBAnchorLinkSet[0], thisPBAnchorLinkSet[1], thisPBAnchorLinkSet[2], thisPBAnchorLinkSet[3], thisPBAnchorLinkSet[4]};
//
//        nextTBA.add(nextTBepO);
//        nextTBA.add(nextTBAnchorOLID);
//
//        this.updateTBANavigationIndex(new String[]{"0", String.valueOf(pBepNewIndex), String.valueOf(pBAnchorNewIndex), "0", "0"});
//        return nextTBA;
//    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Get PRE TAB/TBA & Update NAV Indice">
//    public Vector<String[]> getPreTABWithUpdateNAV(String topicID, String[] pAnchorOLSA, String[] linkBepOID, boolean b) {
//        // RETURN V[String[]{anchor_O, L}, String[]{BepLink_O, ID}]
//        Vector<String[]> nextTAB = new Vector<String[]>();
//        String[] nextTAnchorOL = null;
//        String[] nextTABepOID = null;
//        // Format: new String[]{0, 1_2_0_0}
//        String pAnchorO = pAnchorOLSA[0];
//        String pAnchorL = pAnchorOLSA[1];
//        String linkBepID = linkBepOID[1];
//        // String[]{anchor Offset, Length, Name, arel}
//        int pAnchorNewIndex = 0;
//        int pABepNewIndex = 0;
//        int pAnchorIndex = 0;
//        int pABepIndex = 0;
//        int pAnchorCounter = 0;
//        Vector<String[]> poolAnchorsOLNameStatusVSA = this.getPoolAnchorsOLNameStatusV();
//        for (String[] pAnchorOLNameStatus : poolAnchorsOLNameStatusVSA) {
//            if (pAnchorO.equals(pAnchorOLNameStatus[0])) {
//                pAnchorIndex = pAnchorCounter;
//                // -------------------------------------------------------------
//                int pABepCounter = 0;
//                HashMap<String, Vector<String[]>> anchorBepLinksHM = pooler.getBepSetByAnchor(topicID);
//                Vector<String[]> bepLinksOSIDStatusVSA = anchorBepLinksHM.get(pAnchorO + "_" + pAnchorL);
//                Hashtable<String, Vector<String[]>> anchorBepLinksOIDStatus = this.getPoolAnchorBepLinksHT();
//                Vector<String[]> bepLinksVSA = anchorBepLinksOIDStatus.get(pAnchorO + "_" + pAnchorL);
//                for (String[] bepLinksOSIDStatus : bepLinksVSA) {
//                    if (linkBepID.equals(bepLinksOSIDStatus[1])) {
//                        pABepIndex = Integer.parseInt(bepLinksOSIDStatus[9]); // pABepCounter;
//                        // =====================================================
//                        // Inidcate the Current AB Index --> Get the PRE one
//                        // pABepIndex == 0 : Go Back PRE Anchor 's Last BEP link
//                        // pABepIndex > 0  : Go Back PRE BEP link
//                        if (pABepIndex == 0) {
//                            if (pAnchorIndex == 0) {
//                                pAnchorNewIndex = 0;
//                                pABepNewIndex = 0;
//                            } else if (pAnchorIndex > 0) {
//                                pAnchorNewIndex = pAnchorIndex - 1;
//                                // ---------------------------------------------
//                                String[] prePAnchorOLNSt = poolAnchorsOLNameStatusVSA.elementAt(pAnchorNewIndex);
//                                Vector<String[]> preABepLinksVSA = anchorBepLinksOIDStatus.get(prePAnchorOLNSt[0] + "_" + prePAnchorOLNSt[1]);
//                                // ---------------------------------------------
//                                pABepNewIndex = preABepLinksVSA.size() - 1;
//                            }
//                        } else if (pABepIndex > 0) {
//                            pAnchorNewIndex = pAnchorIndex;
//                            pABepNewIndex = pABepIndex - 1;
//                        }
//                        // -----------------------------------------------------
//                        // Get PoolAnchor O, L, S, E, Status
//                        String[] thisPAnchorOLNameStatus = poolAnchorsOLNameStatusVSA.elementAt(pAnchorNewIndex);
//                        String[] thisPAnchorSEStatus = this.getTopicAnchorSEStatusByOL(topicID, new String[]{thisPAnchorOLNameStatus.offsetToString(), thisPAnchorOLNameStatus.lengthToString()});
//                        nextTAnchorOL = new String[]{thisPAnchorOLNameStatus.offsetToString(), thisPAnchorOLNameStatus.lengthToString(), thisPAnchorSEStatus[0], thisPAnchorSEStatus[1], thisPAnchorOLNameStatus[3], thisPAnchorOLNameStatus[4]};
//                        // Get TargetBepLink
//                        // new String[]{tbOffset, tbStartP, tbFileID, tbRel}
//                        Vector<String[]> thisBepLinksVSA = anchorBepLinksOIDStatus.get(thisPAnchorOLNameStatus.offsetToString() + "_" + thisPAnchorOLNameStatus.lengthToString());
//                        String[] thisPABepLinkSet = thisBepLinksVSA.elementAt(pABepNewIndex);
//                        String bepLinkStartP = "";
//                        String subAnchorName = null;
//                        String subAnchorOffset = null;
//                        String subAnchorLength = null;
//                        String subAnchorRel = null;
//                        subAnchorName = thisPABepLinkSet[5];
//                        subAnchorOffset = thisPABepLinkSet[6];
//                        subAnchorLength = thisPABepLinkSet[7];
//                        subAnchorRel = thisPABepLinkSet[8];
//                        for (String[] bepLinksOSIDS : bepLinksOSIDStatusVSA) {
//                            if (bepLinksOSIDS[0].equals(thisPABepLinkSet[0])) {
//                                bepLinkStartP = bepLinksOSIDS[1];
//                                break;
//                            }
//                        }
//                        nextTABepOID = new String[]{thisPABepLinkSet[0], bepLinkStartP, thisPABepLinkSet[1], thisPABepLinkSet[2], thisPABepLinkSet[3], thisPABepLinkSet[4], subAnchorName, subAnchorOffset, subAnchorLength, subAnchorRel};
//
//                        nextTAB.add(nextTAnchorOL);
//                        nextTAB.add(nextTABepOID);
//                        break;
//                    }
//                    pABepCounter++;
//                }
//            }
//            pAnchorCounter++;
//        }
//        this.updateTABNavigationIndex(new String[]{"0", String.valueOf(pAnchorNewIndex), String.valueOf(pABepNewIndex), "0", "0"});
//
//        return nextTAB;
//    }

//    public Vector<String[]> getPreTBAWithUpdateNAV(String topicID, String pBepOSA, String[] linkAnchorOLID) {
//        // RETURN V[String[]{anchor_O}, String[]{AnchorLink_O, L, ID}]
//        Vector<String[]> nextTBA = new Vector<String[]>();
//        String[] nextTBepO = new String[1];
//        String[] nextTBAnchorOLID = new String[3];
//        // Format: new String[]{0, 1_2_0_0}
//        String pBepO = pBepOSA;
//        String linkAnchorO = linkAnchorOLID[0];
//        String linkAnchorL = linkAnchorOLID[1];
//        String linkAnchorID = linkAnchorOLID[2];
//        // String[]{anchor Offset, Length, Name, arel}
//        int pBepNewIndex = 0;
//        int pBAnchorNewIndex = 0;
//        int pBepIndex = 0;
//        int pBAnchorIndex = 0;
//        int pBepCounter = 0;
//        Vector<String[]> poolBepOStatusVSA = this.getPoolBEPsOStatusV();
//        for (String[] pBepOStatus : poolBepOStatusVSA) {
//            if (pBepO.equals(pBepOStatus[0])) {
//                pBepIndex = Integer.parseInt(pBepOStatus[9]); // pBepCounter;
//                // -------------------------------------------------------------
//                int pBAnchorCounter = 0;
//                HashMap<String, Vector<String[]>> bepAnchorLinksHM = pooler.getAnchorFileSetByBep(topicID);
//                Vector<String[]> anchorLinksVSA = bepAnchorLinksHM.get(pBepO);
//                // new String[]{anchor_O, L, Name, ID, Status}
//                for (String[] anchorLinksOSIDStatus : anchorLinksVSA) {
//                    if (linkAnchorID.equals(anchorLinksOSIDStatus[3]) &&
//                            linkAnchorO.equals(anchorLinksOSIDStatus[0])) {
//                        pBAnchorIndex = pBAnchorCounter;
//                        // =====================================================
//                        // Inidcate the Current AB Index --> Get the PRE one
//                        // pABepIndex == 0 : Go Back PRE Anchor 's Last BEP link
//                        // pABepIndex > 0  : Go Back PRE BEP link
//                        if (pBAnchorIndex == 0) {
//                            if (pBepIndex == 0) {
//                                pBepNewIndex = 0;
//                                pBAnchorNewIndex = 0;
//                            } else if (pBepIndex > 0) {
//                                pBepNewIndex = pBepIndex - 1;
//                                // ---------------------------------------------
//                                String[] prePBepOStatus = poolBepOStatusVSA.elementAt(pBepNewIndex);
//                                Vector<String[]> preALinksVSA = bepAnchorLinksHM.get(prePBepOStatus[0]);
//                                // ---------------------------------------------
//                                pBAnchorNewIndex = preALinksVSA.size() - 1;
//                            }
//                        } else if (pBAnchorIndex > 0) {
//                            pBepNewIndex = pBepIndex;
//                            pBAnchorNewIndex = pBAnchorIndex - 1;
//                        }
//                        // -----------------------------------------------------
//                        // Get Pool BEP O, S, Status
//                        String[] thisPBepOStatus = poolBepOStatusVSA.elementAt(pBepNewIndex);
//                        String[] topicBepSStatus = this.getTopicBepSStatusByOL(topicID, new String[]{thisPBepOStatus[0], ""});
//                        nextTBepO = new String[]{thisPBepOStatus[0], topicBepSStatus[0], thisPBepOStatus[1]};
//                        // Get Bep-AnchorLink O, L, S, E, ID, Status
//                        Vector<String[]> thisAnchorLinksVSA = bepAnchorLinksHM.get(thisPBepOStatus[0]);
//                        // Bep(Offset:1114), Vector<String[]{Offset:1538, Length:9, Name:TITLE, ID:123017, Status}+>
//                        String[] thisPBAnchorLinkSet = thisAnchorLinksVSA.elementAt(pBAnchorNewIndex);
//                        nextTBAnchorOLID = new String[]{thisPBAnchorLinkSet[0], thisPBAnchorLinkSet[1], thisPBAnchorLinkSet[3], thisPBAnchorLinkSet[4]};
//
//                        nextTBA.add(nextTBepO);
//                        nextTBA.add(nextTBAnchorOLID);
//                    }
//                    pBAnchorCounter++;
//                }
//            }
//            pBepCounter++;
//        }
//        this.updateTBANavigationIndex(new String[]{"0", String.valueOf(pBepNewIndex), String.valueOf(pBAnchorNewIndex), "0", "0"});
//
//        return nextTBA;
//    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Get NEXT TAB/TBA & Update NAV Indice">
    public Bep getPreTABWithUpdateNAV(String topicID,Bep currALinkOIDSA, boolean nextUnassessed) {
    	return getTABWithUpdateNAV(topicID, currALinkOIDSA, nextUnassessed, false);
    }
    
    public Bep getNextTABWithUpdateNAV(String topicID, Bep linkBepOID, boolean nextUnassessed) {
    	return getTABWithUpdateNAV(topicID, linkBepOID, nextUnassessed, true);
    }
    
    public Bep getTABWithUpdateNAV(String topicID, Bep currentLink, boolean nextUnassessed, boolean forwardOrBackward) {
    	Bep link = null;
    	IndexedAnchor anchor = null; 
    	AssessedAnchor subanchor = null;
    	
    	link = forwardOrBackward ? currentLink.getAssociatedAnchor().getNextLink(currentLink, nextUnassessed) : currentLink.getAssociatedAnchor().getPreviousLink(currentLink, nextUnassessed);
    	if (link == null) {
    		subanchor = (AssessedAnchor) (forwardOrBackward ? currentLink.getAssociatedAnchor().getParent().getNext(currentLink.getAssociatedAnchor(), nextUnassessed) : currentLink.getAssociatedAnchor().getParent().getPrevious(currentLink.getAssociatedAnchor(), nextUnassessed));
    		if (subanchor != null)
    			link = forwardOrBackward ? subanchor.getNextLink(null, nextUnassessed) : subanchor.getPreviousLink(null, nextUnassessed);
    	}
    	
//    	subanchor = forwardOrBackward ? pAnchorOLSA.getParent().getNext(pAnchorOLSA, nextUnassessed) : pAnchorOLSA.getParent().getPrevious(pAnchorOLSA, nextUnassessed);
//    	
//    	if (subanchor != null)
//    		link = forwardOrBackward ? subanchor.getNextLink(currALinkOIDSA, nextUnassessed) : subanchor.getPreviousLink(currALinkOIDSA, nextUnassessed);
//		else {
    	if (link == null) {
			anchor = forwardOrBackward ? getNextAnchor(currentLink.getAssociatedAnchor().getParent(), nextUnassessed) : getPreviousAnchor(currentLink.getAssociatedAnchor().getParent(), nextUnassessed);
			if (anchor != null) {
				subanchor = (AssessedAnchor) (forwardOrBackward ? anchor.getNext(null, nextUnassessed) : anchor.getPrevious(null, nextUnassessed));
				if (subanchor != null)
					link = forwardOrBackward ? subanchor.getNextLink(null, nextUnassessed) : subanchor.getPreviousLink(null, nextUnassessed);
			}
    	}
    	 
    	 if (link == null)
    		 link = currentLink;
    	return link;
    }
    
    private IndexedAnchor getNextAnchor(IndexedAnchor currentAnchor, boolean nextUnassessed) {
    	Vector<IndexedAnchor> poolAnchorsOLNameStatusVSA = this.getPoolAnchorsOLNameStatusV();
    	int i = 0;
    	IndexedAnchor anchor = null;
    	IndexedAnchor  unassessedAnchor = null;
    	for (; i < poolAnchorsOLNameStatusVSA.size(); ++i) {
    		anchor = poolAnchorsOLNameStatusVSA.get(i);
    		
    		if (nextUnassessed && unassessedAnchor == null && anchor.getStatus() == Bep.UNASSESSED)
    			unassessedAnchor = anchor;
    		
    		if (anchor == currentAnchor) {
    			++i;
    			break;
    		}
    	}
    	
    	if (nextUnassessed)
	    	for (; i < poolAnchorsOLNameStatusVSA.size(); ++i) {
	    		anchor = poolAnchorsOLNameStatusVSA.get(i);
	    		if (anchor.getStatus() == Bep.UNASSESSED) {
	    			unassessedAnchor = anchor;
	    			break;
	    		}
	    	}
    	
    	if (i >= poolAnchorsOLNameStatusVSA.size())
    		i = 0;
    	
    	anchor = poolAnchorsOLNameStatusVSA.get(i);
    	return nextUnassessed ? unassessedAnchor : anchor;
    }
    
    private IndexedAnchor getPreviousAnchor(IndexedAnchor currentAnchor, boolean nextUnassessed) {
    	Vector<IndexedAnchor> poolAnchorsOLNameStatusVSA = this.getPoolAnchorsOLNameStatusV();
    	int i = poolAnchorsOLNameStatusVSA.size() - 1;
    	IndexedAnchor anchor = null;
    	IndexedAnchor  unassessedAnchor = null;
    	for (; i > -1; --i) {
    		anchor = poolAnchorsOLNameStatusVSA.get(i);
    		
    		if (nextUnassessed && unassessedAnchor == null && anchor.getStatus() == Bep.UNASSESSED)
    			unassessedAnchor = anchor;
    		
    		if (anchor == currentAnchor) {
    			--i;
    			break;
    		}
    	}
    	
    	if (nextUnassessed)
	    	for (; i > -1; --i) {
	    		anchor = poolAnchorsOLNameStatusVSA.get(i);
	    		if (anchor.getStatus() == Bep.UNASSESSED) {
	    			unassessedAnchor = anchor;
	    			break;
	    		}
	    	}
    	
    	if (i < 0)
    		i = poolAnchorsOLNameStatusVSA.size() - 1;
    	
    	anchor = poolAnchorsOLNameStatusVSA.get(i);
    	return nextUnassessed ? unassessedAnchor : anchor;
    }
    
    
    public Bep getTABWithUpdateNAVOld(String topicID, AssessedAnchor pAnchorOLSA, Bep currALinkOIDSA, boolean nextUnassessed, boolean forwardOrBackward) {
        // RETURN V[String[]{anchor_O, L}, String[]{BepLink_O, ID}]
        Bep nextTAB = null; //new Vector<String[]>();
        // Format: new String[]{0, 1_2_0_0}
        int pAnchorO = pAnchorOLSA.getParent().getOffset(); //Integer.valueOf(pAnchorOLSA[0]);
        int pAnchorL = pAnchorOLSA.getParent().getLength(); //Integer.valueOf(pAnchorOLSA[1]);
        String linkBepID = currALinkOIDSA.getFileId(); //[1];
        // String[]{anchor Offset, Length, Name, arel}
        int pAnchorFirstIndex = 0;
        int pABepFirstIndex = 0;
        int pAnchorIndex = 0;
        int pABepIndex = 0;
        int pAnchorCounter = 0;
        Vector<IndexedAnchor> poolAnchorsOLNameStatusVSA = this.getPoolAnchorsOLNameStatusV();
        int i = 0, j = 0, k = 0;
        IndexedAnchor pAnchorOLNameStatus = null;
        IndexedAnchor thisPAnchorOLNameStatus = null;
//        String[] thisPAnchorSEStatus = null;
//        String[] firstTAnchorOL = null;
        Bep firstTABepOID= null;
        Hashtable<String, Vector<Bep>> anchorBepLinksOIDStatus = this.getPoolAnchorBepLinksHashtable();
        Bep thisPABepLinkSet = null;
        
        boolean firstAnchorFound = false;
        
        int lhs, rhs, step;
        	
        if (forwardOrBackward) {
        	i = 0;
        	pAnchorIndex = poolAnchorsOLNameStatusVSA.size() - 1;
        	step = 1;
        }
        else {
        	i = poolAnchorsOLNameStatusVSA.size() - 1;
        	pAnchorIndex = 0;
        	step = -1;
        }
        
        while (true) {
//        for (; i < poolAnchorsOLNameStatusVSA.size(); ++i) {
            if (forwardOrBackward) {
            	lhs = i;
            	rhs = poolAnchorsOLNameStatusVSA.size() - 1;       	
            }
            else {
            	lhs = 0;
            	rhs = i;
            }
        	if (lhs > rhs)
        		break;
        	
        	pAnchorOLNameStatus = poolAnchorsOLNameStatusVSA.get(i);
        	/*
        	 * the first part is used when reach the end the topic, then the next link will be the first one that
        	 * hasn't been assessed
        	 */
        	if ((!firstAnchorFound && !nextUnassessed) || (!firstAnchorFound && nextUnassessed && pAnchorOLNameStatus.getStatus() == 0)) {
        		
        		thisPAnchorOLNameStatus = poolAnchorsOLNameStatusVSA.elementAt(i);
//        		thisPAnchorSEStatus = this.getTopicAnchorSEStatusByOL(topicID, new String[]{thisPAnchorOLNameStatus.offsetToString(), thisPAnchorOLNameStatus.lengthToString()});
//        		firstTAnchorOL = new String[]{thisPAnchorOLNameStatus.offsetToString(), thisPAnchorOLNameStatus.lengthToString(), thisPAnchorSEStatus[0], thisPAnchorSEStatus[1], thisPAnchorOLNameStatus.statusToString(), thisPAnchorOLNameStatus.extendedLengthToString()};
                Vector<Bep> thisBepLinksVSA = anchorBepLinksOIDStatus.get(thisPAnchorOLNameStatus.offsetToString() + "_" + thisPAnchorOLNameStatus.lengthToString());
 
                thisPABepLinkSet = thisBepLinksVSA.elementAt(0);
        		
        		j = 1;
        		while (thisPABepLinkSet.getAssociatedAnchor().getStatus() != 0 && j < thisBepLinksVSA.size()) {
        			thisPABepLinkSet = thisBepLinksVSA.elementAt(j);
        			++j;
        		}
        		firstTABepOID = thisPABepLinkSet;
//        		firstTABepOID.getAssociatedAnchor().setScreenPosStart(Integer.parseInt(thisPAnchorSEStatus[0]));
//        		firstTABepOID.getAssociatedAnchor().setScreenPosEnd(Integer.parseInt(thisPAnchorSEStatus[1]));
        		pABepFirstIndex = j - 1;
        		pAnchorFirstIndex = i;
        		firstAnchorFound = true;
        		
       			if ((forwardOrBackward && i > pAnchorIndex) || (!forwardOrBackward && i < pAnchorIndex))
        			break;

        	}        	   	
        	
            if (pAnchorO == pAnchorOLNameStatus.getOffset()) {
                pAnchorIndex = i; //pAnchorCounter;
                // -------------------------------------------------------------
//                int pABepCounter = 0;
//                pABepNewIndex = 0;

//                log("bepLinksOSIDStatusVSA: " + bepLinksOSIDStatusVSA.size());
               
                int subanchorIndex = 0;
                for (InterfaceAnchor subanchor : pAnchorOLSA.getParent().getChildrenAnchors()) {
                	if (subanchor == pAnchorOLSA)
                		break;
                	++subanchorIndex;
                }
                Vector<Bep> bepLinksVSA = pAnchorOLSA.getBeps(); //anchorBepLinksOIDStatus.get(pAnchorO + "_" + pAnchorL);
//                log("bepLinksVSA: " + bepLinksVSA.size());
                Bep bepLinksOSIDStatus = null;
                int k_lhs, k_rhs;
                if (forwardOrBackward) 
                	k = 0;
                else
                	k = bepLinksVSA.size() - 1;
                while (true) {
	                if (forwardOrBackward) {
	                	k_lhs = k;
	                	k_rhs = bepLinksVSA.size() - 1;               	
	                }
	                else {
	                	k_lhs = 0;
	                	k_rhs = k;                	
	                }
                	if (k_lhs > k_rhs)
                		break;
                	
                	bepLinksOSIDStatus = bepLinksVSA.get(k);
                    if (linkBepID.equals(bepLinksOSIDStatus.getFileId()/*[1]*/)/* && pAnchorOLSA[0].equals(bepLinksOSIDStatus[6])*/) 
                    	break;
	                k += step;
                }
                
                k += step;
                
                if (nextUnassessed) {
                    while (true) {
    	                if (forwardOrBackward) {
    	                	k_lhs = k;
    	                	k_rhs = bepLinksVSA.size() - 1;               	
    	                }
    	                else {
    	                	k_lhs = 0;
    	                	k_rhs = k;                	
    	                }
                    	if (k_lhs > k_rhs)
                    		break;
                    	
                    	bepLinksOSIDStatus = bepLinksVSA.get(k);
                        if (bepLinksOSIDStatus.getRel() == 0/*[2].equals("0")*/)
                        	break;
    	                k += step;
                    }
                }
                
                if ((forwardOrBackward && k >= bepLinksVSA.size()) || (!forwardOrBackward && k < 0)) {
                	subanchorIndex += step;
                	if ((forwardOrBackward && subanchorIndex >= pAnchorOLSA.getParent().getChildrenAnchors().size()) || (!forwardOrBackward && subanchorIndex < 0))
                	   	firstAnchorFound = false;
                	else {
                		if (forwardOrBackward) {
                			nextTAB = pAnchorOLSA.getParent().getChildrenAnchors().get(subanchorIndex).getBeps().firstElement();
                			pABepIndex = 0;
                		}
                		else {
                			nextTAB = pAnchorOLSA.getParent().getChildrenAnchors().get(subanchorIndex).getBeps().lastElement();
                			pABepIndex = pAnchorOLSA.getParent().getChildrenAnchors().get(subanchorIndex).getBeps().size() - 1;
                		}
                		break;
                	}
                		
//                	continue;
                }
                else {
                	pABepIndex = k; //Integer.parseInt(bepLinksOSIDStatus[9]); //pABepCounter;
	                // =====================================================
	//                // -----------------------------------------------------
	//                // Get PoolAnchor O, L, S, E, Status
//                	thisPAnchorOLNameStatus = pAnchorOLNameStatus;
//	                thisPAnchorOLNameStatus = poolAnchorsOLNameStatusVSA.elementAt(i);
//	                thisPAnchorSEStatus = this.getTopicAnchorSEStatusByOL(topicID, new String[]{thisPAnchorOLNameStatus.offsetToString(), thisPAnchorOLNameStatus.lengthToString()});
	                thisPABepLinkSet = bepLinksVSA.elementAt(pABepIndex);
	                nextTAB = thisPABepLinkSet;
//	        		nextTAB.getAssociatedAnchor().setScreenPosStart(Integer.parseInt(thisPAnchorSEStatus[0]));
//	        		nextTAB.getAssociatedAnchor().setScreenPosEnd(Integer.parseInt(thisPAnchorSEStatus[1]));

	                
	                break;
	//                    }
	//                    pABepCounter++;
	//                }
	            }
            }
            
            i += step;
            
//            pAnchorCounter++;
        }
        

        if (nextTAB == null/*nextTAB.size() == 0*/) {
//        	nextTAB.add(firstTAnchorOL);
//        	nextTAB.add(firstTABepOID);
        	nextTAB = firstTABepOID;
        	this.updateTABNavigationIndex(new String[]{"0", String.valueOf(pAnchorFirstIndex), String.valueOf(pABepFirstIndex), "0", "0"});
        }
        else
        	this.updateTABNavigationIndex(new String[]{"0", String.valueOf(i), String.valueOf(pABepIndex), "0", "0"});
        return nextTAB;
    }

//    public Vector<String[]> getNextTBAWithUpdateNAV(String topicID, String pBepOSA, String[] linkAnchorOLID) {
//        // RETURN V[String[]{anchor_O}, String[]{AnchorLink_O, L, ID}]
//        Vector<String[]> nextTBA = new Vector<String[]>();
//        String[] nextTBepO = new String[1];
//        String[] nextTBAnchorOLID = new String[3];
//        // Format: new String[]{0, 1_2_0_0}
//        String pBepO = pBepOSA;
//        String linkAnchorO = linkAnchorOLID[0];
//        String linkAnchorL = linkAnchorOLID[1];
//        String linkAnchorID = linkAnchorOLID[2];
//        // String[]{anchor Offset, Length, Name, arel}
//        int pBepNewIndex = 0;
//        int pBAnchorNewIndex = 0;
//        int pBepIndex = 0;
//        int pBAnchorIndex = 0;
//        int pBepCounter = 0;
//        Vector<String[]> poolBepOStatusVSA = this.getPoolBEPsOStatusV();
//        for (String[] pBepOStatus : poolBepOStatusVSA) {
//            if (pBepO.equals(pBepOStatus[0])) {
//                pBepIndex = pBepCounter;
//                // -------------------------------------------------------------
//                int pBAnchorCounter = 0;
//                HashMap<String, Vector<String[]>> bepAnchorLinksHM = pooler.getAnchorFileSetByBep(topicID);
//                Vector<String[]> anchorLinksVSA = bepAnchorLinksHM.get(pBepO);
//                // new String[]{anchor_O, L, Name, ID, Status}
//                for (String[] anchorLinksOSIDStatus : anchorLinksVSA) {
//                    if (linkAnchorID.equals(anchorLinksOSIDStatus[3]) &&
//                            linkAnchorO.equals(anchorLinksOSIDStatus[0])) {
//                        pBAnchorIndex = pBAnchorCounter;
//                        // =====================================================
//                        if (pBAnchorIndex == anchorLinksVSA.size() - 1) {
//                            if (pBepIndex == poolBepOStatusVSA.size() - 1) {
//                                pBepNewIndex = 0;
//                                pBAnchorNewIndex = 0;
//                            } else if (pBepIndex < poolBepOStatusVSA.size() - 1) {
//                                pBepNewIndex = pBepIndex + 1;
//                                pBAnchorNewIndex = 0;
//                            }
//                        } else if (pBAnchorIndex < anchorLinksVSA.size() - 1) {
//                            pBepNewIndex = pBepIndex;
//                            pBAnchorNewIndex = pBAnchorIndex + 1;
//                        }
//                        // -----------------------------------------------------
//                        // Get Pool BEP O, S, Status
//                        String[] thisPBepOStatus = poolBepOStatusVSA.elementAt(pBepNewIndex);
//                        String[] topicBepSStatus = this.getTopicBepSStatusByOL(topicID, new String[]{thisPBepOStatus[0], ""});
//                        nextTBepO = new String[]{thisPBepOStatus[0], topicBepSStatus[0], thisPBepOStatus[1]};
//                        // Get Bep-AnchorLink O, L, S, E, ID, Status
//                        Vector<String[]> thisAnchorLinksVSA = bepAnchorLinksHM.get(thisPBepOStatus[0]);
//                        // Bep(Offset:1114), Vector<String[]{Offset:1538, Length:9, Name:TITLE, ID:123017, Status}+>
//                        String[] thisPBAnchorLinkSet = thisAnchorLinksVSA.elementAt(pBAnchorNewIndex);
//                        nextTBAnchorOLID = new String[]{thisPBAnchorLinkSet[0], thisPBAnchorLinkSet[1], thisPBAnchorLinkSet[3], thisPBAnchorLinkSet[4]};
//
//                        nextTBA.add(nextTBepO);
//                        nextTBA.add(nextTBAnchorOLID);
//                        break;
//                    }
//                    pBAnchorCounter++;
//                }
//            }
//            pBepCounter++;
//        }
//        this.updateTBANavigationIndex(new String[]{"0", String.valueOf(pBepNewIndex), String.valueOf(pBAnchorNewIndex), "0", "0"});
//
//        return nextTBA;
//    }
    // </editor-fold>

    public Vector<IndexedAnchor> getPoolAnchorsOLNameStatusV() {
        // anchor String[]{Offset, Length, Name, Status}
        // status: 0 <-- normal, 1 <-- completed, -1 <-- non-relevant
        // Get Sorted Anchor OL
        // Hashtable<String, Vector<String[]>>
        // outgoing : topicID, V<String[]{anchor Offset, Length, Name, arel, extension length}>
        String topicID = this.getTopicID();
        Vector<IndexedAnchor> poolAnchorsOLV = topicAllAnchors.get(PoolerManager.OUTGOING_KEY + topicID); //topicAllAnchors.elements().nextElement();
//        Vector<IndexedAnchor> sortedPoolAnchorsOLV = sortOLVectorSANumbers(poolAnchorsOLV);
        return poolAnchorsOLV; 
//        return sortedPoolAnchorsOLV;
    }
    
    public Vector<AssessedAnchor> getPoolSubanchorsOLNameStatusV() {
        // anchor String[]{Offset, Length, Name, Status}
        // status: 0 <-- normal, 1 <-- completed, -1 <-- non-relevant
        // Get Sorted Anchor OL
        // Hashtable<String, Vector<String[]>>
        // outgoing : topicID, V<String[]{anchor Offset, Length, Name, arel}>
        
//        Vector<AssessedAnchor> poolAnchorsOLV = topicAllSubanchors.elements().nextElement();
//        Vector<IndexedAnchor> sortedPoolAnchorsOLV = sortOLVectorSANumbers(poolAnchorsOLV);
    	return topicAllSubanchors.get(PoolerManager.OUTGOING_KEY + this.getTopicID());
//        return sortedPoolAnchorsOLV;
    }
    
    
    
    public Hashtable<String, Vector<Bep>> getPoolAnchorBepLinksHashtable() {
    	return poolAnchorBepLinksHT;
    }
    
    public IndexedAnchor getBepsPoolByAnchor(int index) {
    	String topicKey = PoolerManager.OUTGOING_KEY + getTopicID();
    	Hashtable<String, Vector<IndexedAnchor>> anchors = pooler.getTopicAllAnchors();
    	return anchors.get(topicKey).get(index);
    }
    
    public Hashtable<String, Hashtable<String, Vector<Bep>>> getBepsByTopic(String topicKey) {
    	return poolOutgoingData.get(topicKey);
    }
    
    public void setPoolAnchorBepLinksHashtable() {
        Vector<String> targetFileID = null; //new Vector<String>();
        // Pool_Anchor OL, BEPLinks V<String[]{Offset, fileID, tbrel/status}
        poolAnchorBepLinksHT = new Hashtable<String, Vector<Bep>>();
        String poolAnchorOL = "";
        Vector<Bep> aBepLinksV = null; //new Vector<String[]>();
        Enumeration topicKeyEnu = poolOutgoingData.keys();
        while (topicKeyEnu.hasMoreElements()) {
            Object topicKey = topicKeyEnu.nextElement();
            String myTopicID = topicKey.toString();
            Hashtable<String, Hashtable<String, Vector<Bep>>> poolAnchorsHT = getBepsByTopic(topicKey.toString());
            Enumeration poolAnchorKeys = poolAnchorsHT.keys();
            while (poolAnchorKeys.hasMoreElements()) {
                Object pAnchorKey = poolAnchorKeys.nextElement();
                poolAnchorOL = pAnchorKey.toString();
                String[] pAnchorOL = poolAnchorOL.toString().split("_");
                aBepLinksV = new Vector<Bep>();
                Hashtable<String, Vector<Bep>> anchorsHT = poolAnchorsHT.get(pAnchorKey.toString());
                Enumeration anchorKeys = anchorsHT.keys();
                while (anchorKeys.hasMoreElements()) {
                    Object anchorKey = anchorKeys.nextElement();
                    // V<String[]{Offset, fileID, tbrel/status}
                    Vector<Bep> bepLinksV = anchorsHT.get(anchorKey.toString());
                    // ---------------------------------------------------------
                    /**
                     * used to record fileID to prevent from Duplicated Target File
                     * In our case,
                     * only an insatnce of a file ID should appear under the same Anchor OL,
                     * because we don't show BEP.
                     */
                    targetFileID = new Vector<String>();
//                    Vector<String[]> newBepLinksV = new Vector<String[]>();
//                    for (IndexedAnchor anchor : bepLinksV)
	                    for (Bep bepLinkSA : bepLinksV) {
	                        String thisBepFileID = bepLinkSA.getFileId();//[1];
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
                Vector<Bep> bepLinksVSorted = new Vector<Bep>(aBepLinksV.size());
                bepLinksVSorted.setSize(aBepLinksV.size());
                for (Bep bepInfo : aBepLinksV) {
                	int index = bepInfo.getIndex(); //Integer.parseInt(bepInfo[9]);
//                	if (index > 3)
//                		System.err.println("We got more than 3 beps here");
                	bepLinksVSorted.set(index, bepInfo);
                }
                
                poolAnchorBepLinksHT.put(poolAnchorOL, bepLinksVSorted);
            }
        }
//        return poolAnchorBepLinksHT;
    }

//    public Vector<IndexedAnchor> getPoolBEPsOStatusV() {
//        // anchor String[]{Offset, status}
//        // status: 0 <-- normal, 1 <-- completed, -1 <-- non-relevant
//        // Get Sorted BEP O
//        Vector<IndexedAnchor> poolBEPsOV = topicAllBEPs.elements().nextElement();
//        Vector<IndexedAnchor> sortedPoolBEPsOV = sortOLVectorSANumbers(poolBEPsOV);
//        return sortedPoolBEPsOV;
//    }

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
    	String topicID[] = getDataByTagName(afTitleTag, afltwTopicsTag).split(":");
        return topicID[0].trim();
    }
    
    public String getTopicLang() {
    	String topicID[] = getDataByTagName(afTitleTag, afltwTopicsTag).split(":");
    	if (topicID.length < 2)
    		return AppResource.sourceLang;
        return topicID[1].trim();
    }

    public int getPoolAnchorCompletedStatus(String topicID, IndexedAnchor poolAnchorOL) {
        // RETURN new String[]{Completed, Total}
    	int status = poolAnchorOL.getStatus();
    	int subStatus = Bep.UNASSESSED;
    	for (InterfaceAnchor subanchor : poolAnchorOL.getChildrenAnchors()) {
    		subStatus = getPoolSubanchorCompletedStatus(topicID, (AssessedAnchor)subanchor);
    			
    		if (subStatus == Bep.UNASSESSED) {
    			status = Bep.UNASSESSED;
    			break;
    		}
    			
    		if (subStatus == Bep.RELEVANT)
    			status = Bep.RELEVANT;
    	}
    	
    	if (subStatus != Bep.UNASSESSED)
    		poolAnchorOL.setStatus(status);
    	else
    		poolAnchorOL.setStatus(Bep.UNASSESSED);
        return poolAnchorOL.getStatus();        
    }
    
    public int getPoolSubanchorCompletedStatus(String topicID, AssessedAnchor poolAnchorOL) {
        // RETURN new String[]{Completed, Total}
    	int status;
        if (poolAnchorOL.getStatus() != Bep.IRRELEVANT) {
	        
	        Vector<String> tABepSetVSA = this.pooler.getPoolSubanchorAllLinkStatus(topicID, poolAnchorOL);
	        status  = getCompletedStatus(tABepSetVSA);
	        poolAnchorOL.setStatus(status);
        }
        return poolAnchorOL.getStatus();        
    }
    
    public int getCompletedStatus(Vector<String> tABepSetVSA) {
        int poolBepCompletedStatus = 0;
        int totalCounter = 0;
        int completeCounter = 0;
        int RelCounter = 0;
        int NONRelCounter = 0;
        
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
                poolBepCompletedStatus = -1;
            } else {
                poolBepCompletedStatus = 1;
            }
        }
        return poolBepCompletedStatus;
    }

    public String[] getPoolAnchorCompletedRatio(String topicID, IndexedAnchor poolAnchorOL) {
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
        Vector<IndexedAnchor> tAnchorsOLNStatusVSA = this.getPoolAnchorsOLNameStatusV();
        // Pool_Anchor OL, BEPLinks V<String[]{Offset, fileID, tbrel/status}
//        Hashtable<String, Vector<String[]>> tABepSetHM = this.getPoolAnchorBepLinksHT();
        for (IndexedAnchor tAnchorSet : tAnchorsOLNStatusVSA) {
//            String[] tAnchorOL = new String[]{tAnchorSet.offsetToString(), tAnchorSet.lengthToString()};
            String pAnchorStatus = this.pooler.getPoolAnchorStatus(this.getTopicID(), tAnchorSet);
            Vector<String> tABepLinkStatusVSA = this.pooler.getPoolAnchorAllLinkStatus(this.getTopicID(), tAnchorSet);
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

//    public String[] getTBACompletedRatio() {
//        // RETURN new String[]{Completed, Total}
//        String[] tbaRatio = new String[2];
//        int totalCounter = 0;
//        int completeCounter = 0;
//        // record Topic (incoming : topicFile) -> [0]:Offset
//        Vector<String[]> tBepsHT = this.getPoolBEPsOStatusV();
//        // Bep(Offset:1114), // V<String[]{Offset, Length, AName, fileID, barel}
//        Hashtable<String, Vector<String[]>> tBAnchorSetHM = this.getPoolBepAnchorLinksHT();
//        for (String[] tBepSet : tBepsHT) {
//            String tBepO = tBepSet[0];
//            Vector<String> tBAnchorLinkStatus = this.pooler.getPoolBepAllLinksStatusV(this.getTopicID(), tBepO);
//            totalCounter = totalCounter + tBAnchorLinkStatus.size();
//            for (String tBepStatus : tBAnchorLinkStatus) {
//                if (tBepStatus.equals("-1") || tBepStatus.equals("1")) {
//                    completeCounter++;
//                }
//            }
//        }
//        tbaRatio = new String[]{String.valueOf(completeCounter), String.valueOf(totalCounter)};
//        return tbaRatio;
//    }
    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
    // 2) Get Topic Anchor OL & SCR-SE

    public IndexedAnchor getCurrTopicAnchorOLNameStatusSA() {
        // String[]{Anchor_Offset, Length}
        String[] currTopicAnchorOLNameStatus = new String[2];
        // ---------------------------------------------------------------------
        // 1) ONLY 1 Topic, so topicIndex = 0
        // anchor-bep Index: 0 , 0 , 0 , 0 --> pool_Anchor , bep_Link , non-used , non-used
        String[] tabIndexSA = this.getTABNavigationIndex();
        String topicIndex = tabIndexSA[0].trim();
        String[] abIndex = tabIndexSA[1].trim().split(" , ");
        int poolAnchorIndex = 0;
        try {
        	poolAnchorIndex = Integer.valueOf(abIndex[0].trim());
        }
        catch (Exception ex) {
        	poolAnchorIndex = 0;
        }
        String bepLinkIndex = abIndex[1].trim();
        // ---------------------------------------------------------------------
        // 2) Get current Anchor OL
        // String[]{anchor Offset, Length, Name, arel}
        Vector<IndexedAnchor> poolAnchorsOLNameStatusVSA = getPoolAnchorsOLNameStatusV();
        if (poolAnchorIndex < 0 || poolAnchorIndex >= poolAnchorsOLNameStatusVSA.size())
        	poolAnchorIndex = 0;
        IndexedAnchor currAnchorOLNameStatusSA = poolAnchorsOLNameStatusVSA.elementAt(poolAnchorIndex);
        if (currAnchorOLNameStatusSA.getName().trim().length() == 0)
        	currAnchorOLNameStatusSA = ResourcesManager.getInstance().getNextAnchor(currAnchorOLNameStatusSA, false);
//        currTopicAnchorOLNameStatus = new String[]{currAnchorOLNameStatusSA.offsetToString(), currAnchorOLNameStatusSA.lengthToString(), currAnchorOLNameStatusSA.getName(), currAnchorOLNameStatusSA.statusToString()};
        return currAnchorOLNameStatusSA;
//        return currTopicAnchorOLNameStatus;
    }
    
    public String[] getCurrTopicSubanchorOLNameStatusSA() {
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
        Vector<AssessedAnchor> poolAnchorsOLNameStatusVSA = getPoolSubanchorsOLNameStatusV();
        IndexedAnchor currAnchorOLNameStatusSA = poolAnchorsOLNameStatusVSA.elementAt(Integer.valueOf(poolAnchorIndex));
        currTopicAnchorOLNameStatus = new String[]{currAnchorOLNameStatusSA.offsetToString(), currAnchorOLNameStatusSA.lengthToString(), currAnchorOLNameStatusSA.getName(), currAnchorOLNameStatusSA.statusToString()};
        return currTopicAnchorOLNameStatus;
    }

    public String[] getCurrTopicAnchorOLNameSEStatusSA(JTextPane myTextPane, String topicID, Vector<String> topicAnchorsOLNameSEVS) {
        // String[]{Anchor_O, L, Name, SP, EP, Status}
        String[] currTopicAnchorOLNameSE = new String[6];
        // String[]{anchor Offset, Length, Name, arel}
        IndexedAnchor currAnchorOLSA = this.getCurrTopicAnchorOLNameStatusSA();
        String currAnchorO = currAnchorOLSA.offsetToString(); //[0];
        String currAnchorL = currAnchorOLSA.lengthToString(); //[1];
        // SP, EP
        String currAnchorS = "0";
        String currAnchorE = "0";
        
        String extensionLength = "0";  // extension length here means overlapping anchors
        
        for (int i = 0; i < topicAnchorsOLNameSEVS.size(); i++) {
            String topicAnchorsOLNameSE = topicAnchorsOLNameSEVS.elementAt(i);
            String[] topicAnchorsOLNameSESA = topicAnchorsOLNameSE.split(" : ");
            if (currAnchorO.equals(topicAnchorsOLNameSESA[0]) && currAnchorL.equals(topicAnchorsOLNameSESA[1])) {
                currAnchorS = topicAnchorsOLNameSESA[3];
                currAnchorE = topicAnchorsOLNameSESA[4];
                extensionLength = topicAnchorsOLNameSESA[5];
                break;
            }
        }
        String[] currTopicAnchorSE = new String[]{currAnchorS, currAnchorE};
        currTopicAnchorOLNameSE = new String[]{currAnchorOLSA.offsetToString()/*[0]*/, currAnchorOLSA.lengthToString()/*[1]*/, currAnchorOLSA.getName()/*[2]*/, currTopicAnchorSE[0], currTopicAnchorSE[1], currAnchorOLSA.statusToString()/*[3]*/, extensionLength};
        return currTopicAnchorOLNameSE;
    }
    
    public String[] getCurrTopicSubanchorOLNameSEStatusSA(JTextPane myTextPane, String topicID, Vector<String> topicAnchorsOLNameSEVS) {
        // String[]{Anchor_O, L, Name, SP, EP, Status}
        String[] currTopicAnchorOLNameSE = new String[6];
        // String[]{anchor Offset, Length, Name, arel}
        String[] currAnchorOLSA = this.getCurrTopicSubanchorOLNameStatusSA();
        String currAnchorO = currAnchorOLSA[0];
        String currAnchorL = currAnchorOLSA[1];
        // SP, EP
        String currAnchorS = "";
        String currAnchorE = "";
        
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

    public Bep getCurrTopicATargetOID(JTextPane myLinkTextPane, String topicID) {
        // String[]{Bep_Offset, File_ID}
//        String[] currTopicABepIDO = new String[2];
        // ---------------------------------------------------------------------
        // ONLY 1 Topic, so topicIndex = 0
        // anchor-bep Index: 0 , 0 , 0 , 0 --> pool_Anchor , bep_Link , non-used , non-used
        String[] tabIndexSA = this.getTABNavigationIndex();
        String[] abIndex = tabIndexSA[1].trim().split(" , ");
        int bepLinkIndex = Integer.valueOf(abIndex[1].trim());
        // ---------------------------------------------------------------------
        // Get current Anchor SE
        // Pool_Anchor OL, BEPLinks V<String[]{Offset, fileID, tbrel/status}
        IndexedAnchor currAnchorOLSA = getCurrTopicAnchorOLNameStatusSA();
//        String currAnchorOL = currAnchorOLSA.toKey(); //[0] + "_" + currAnchorOLSA[1];
//        Hashtable<String, Vector<Bep>> poolAnchorBepLinksHT = getPoolAnchorBepLinksHashtable();
//        Vector<Bep> currBepVSA = poolAnchorBepLinksHT.get(currAnchorOL);
        if (bepLinkIndex < 0 || bepLinkIndex >= currAnchorOLSA.getChildrenAnchors().get(0).getBeps().size())
        	bepLinkIndex = 0;
        Bep currBepSA = currAnchorOLSA.getChildrenAnchors().get(0).getBeps().get(bepLinkIndex); //currBepVSA.elementAt(bepLinkIndex);
        return currBepSA;
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
        IndexedAnchor currAnchorOLSA = getCurrTopicAnchorOLNameStatusSA();
        String currAnchorOL = currAnchorOLSA.toKey(); //[0] + "_" + currAnchorOLSA[1];
        Hashtable<String, Vector<Bep>> poolAnchorBepLinksHT = getPoolAnchorBepLinksHashtable();
        Vector<Bep> currBepVSA = poolAnchorBepLinksHT.get(currAnchorOL);
        Bep currBepSA = currBepVSA.elementAt(Integer.valueOf(bepLinkIndex));
        String bepOffset = currBepSA.offsetToString().trim();
        String bepFileID = currBepSA.getFileId().trim();
        String bepStatus = currBepSA.relString().trim();
        // ---------------------------------------------------------------------
        // Link BEP
        // convery BEP Offset into SCR Start Point
        String currBepSCRS = this.pooler.getPoolAnchorBepLinkStartP(topicID, (AssessedAnchor)currBepSA.getAssociatedAnchor()/*new String[]{currAnchorOLSA[0], currAnchorOLSA[1]}*/, bepFileID);
        currTopicABepSIDStatus = new String[]{currBepSCRS, bepFileID, bepStatus};

//        FOLTXTMatcher folMatcher = FOLTXTMatcher.getInstance();
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
    
    public void setTopicAnchorOLStatusBySE() {
        Vector<String> topicAnchorsOLNameSEV = getTopicAnchorsOLNameSEV();
        Vector<IndexedAnchor> poolAnchorsOLV = getPoolAnchorsOLNameStatusV();
        for (String anchorOLNameSE : topicAnchorsOLNameSEV) {
            String[] anchorSA = anchorOLNameSE.split(" : ");
            for (IndexedAnchor pAnchorSA : poolAnchorsOLV) {
                String pAnchorOffset = pAnchorSA.offsetToString(); //[0];
                String pAnchorStatus = pAnchorSA.statusToString(); //[3];
                if (anchorSA[0].trim().equals(pAnchorOffset)) { 
                	pAnchorSA.setScreenPosStart(Integer.parseInt(anchorSA[3]));
                	pAnchorSA.setScreenPosEnd(Integer.parseInt(anchorSA[4]));
                	break;
                }
            }
        }
    }
    
    public void checkAnchorStatus() {
        String topicID = this.getTopicID();
    	PoolerManager.getInstance().checkAnchorSatus(topicID);
    }

//    public String[] getTopicAnchorOLStatusBySE(String topicID, IndexedAnchor currSCRSEName) {
//        String[] poolAnchorOLStatus = new String[3];
//        int pAnchorS = currSCRSEName.getOffset(); //getScreenPosStart();
//        int pAnchorE = currSCRSEName.getScreenPosEnd();
//        // String[]{Anchor_Offset, Length, S, E, Status, extension length}
//        Vector<String[]> topicAnchorSCRStatusVSA = getTopicAnchorOLSEStatusVSA();
//        for (String[] topicAnchorSCRStatus : topicAnchorSCRStatusVSA) {
//            int poolAnchorS = Integer.parseInt(topicAnchorSCRStatus[0]);
//            int poolAnchorE = poolAnchorS + Integer.parseInt(topicAnchorSCRStatus[1]) + Integer.parseInt(topicAnchorSCRStatus[5]);
//            if (poolAnchorS >= pAnchorS && poolAnchorE <= pAnchorE) {
//                poolAnchorOLStatus = new String[]{topicAnchorSCRStatus[0], topicAnchorSCRStatus[1], topicAnchorSCRStatus[4]};
//                
//            }
//        }
//        return poolAnchorOLStatus;
//    }

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

    private Vector<String[]> getTopicAnchorOLSEStatusVSA() {
        // String[]{Anchor_Offset, Length, S, E, Status}
        Vector<String[]> topicAnchorSCRStatusVSA = new Vector<String[]>();
        Vector<String> topicAnchorsOLNameSEV = getTopicAnchorsOLNameSEV();
        Vector<IndexedAnchor> poolAnchorsOLV = getPoolAnchorsOLNameStatusV();
        for (String anchorOLNameSE : topicAnchorsOLNameSEV) {
            String[] anchorSA = anchorOLNameSE.split(" : ");
            for (IndexedAnchor pAnchorSA : poolAnchorsOLV) {
                String pAnchorOffset = pAnchorSA.offsetToString(); //[0];
                String pAnchorStatus = pAnchorSA.statusToString(); //[3];
                if (anchorSA[0].trim().equals(pAnchorOffset)) 
                    topicAnchorSCRStatusVSA.add(new String[]{anchorSA[0].trim(), anchorSA[1].trim(), anchorSA[3].trim(), anchorSA[4].trim(), pAnchorStatus, anchorSA[5].trim()});
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
            
            if (subNodeList.getLength() > 0) {
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
//    public String getCurrTopicBepOffsetStatus() {
//        // String[]{Anchor_Offset, Length}
//        String currTopicBepOStatus = "";
//        // ---------------------------------------------------------------------
//        // 1) ONLY 1 Topic, so topicIndex = 0
//        // anchor-bep Index: 0 , 0 , 0 , 0 --> pool_Bep , anchor_Link , non-used , non-used
//        String[] tbaIndexSA = this.getTBANavigationIndex();
//        String topicIndex = tbaIndexSA[0].trim();
//        String[] baIndex = tbaIndexSA[1].trim().split(" , ");
//        String poolBepIndex = baIndex[0].trim();
//        // ---------------------------------------------------------------------
//        // 2) Get current Anchor OL
//        // String[]{Offset, status}
//        Vector<String[]> poolBepsOVSA = this.getPoolBEPsOStatusV();
//        String[] currBepOSA = poolBepsOVSA.elementAt(Integer.valueOf(poolBepIndex));
//        return currTopicBepOStatus = currBepOSA[0] + "_" + currBepOSA[1];
//    }

//    public String getCurrTopicBepOffset() {
//        // String[]{Anchor_Offset, Length}
//        String currTopicBepOffset = "";
//        // ---------------------------------------------------------------------
//        // 1) ONLY 1 Topic, so topicIndex = 0
//        // anchor-bep Index: 0 , 0 , 0 , 0 --> pool_Bep , anchor_Link , non-used , non-used
//        String[] tbaIndexSA = this.getTBANavigationIndex();
//        String topicIndex = tbaIndexSA[0].trim();
//        String[] baIndex = tbaIndexSA[1].trim().split(" , ");
//        String poolBepIndex = baIndex[0].trim();
//        // ---------------------------------------------------------------------
//        // 2) Get current Anchor OL
//        // String[]{Offset, status}
//        Vector<String[]> poolBepsOVSA = this.getPoolBEPsOStatusV();
//        String[] currBepOSA = poolBepsOVSA.elementAt(Integer.valueOf(poolBepIndex));
//        return currTopicBepOffset = currBepOSA[0];
//    }

//    public String getCurrTopicBepSCRS(JTextPane myTextPane, String topicID, String lang) {
//        // String[]{Anchor_SP, EP}
//        String currTopicBepSP = "";
//        // convert OL into SCR SE
//        FOLTXTMatcher folMatcher = FOLTXTMatcher.getInstance();
//        String currBepOffset = this.getCurrTopicBepOffset();
//        return currTopicBepSP = folMatcher.getBepSCRSP(myTextPane, topicID, currBepOffset, Boolean.valueOf(System.getProperty(sysPropertyIsTopicWikiKey)), lang);
//    }

//    public String[] getCurrTopicBTargetOLID(JTextPane myLinkTextPane, String topicID) {
//        // String[]{Anchor_Offset, Length, File_ID}
//        String[] currTopicBAnchorOLID = new String[3];
//        // ---------------------------------------------------------------------
//        // ONLY 1 Topic, so topicIndex = 0
//        // anchor-bep Index: 0 , 0 , 0 , 0 --> pool_Bep , anchor_Link , non-used , non-used
//        String[] tbaIndexSA = this.getTBANavigationIndex();
//        String[] baIndex = tbaIndexSA[1].trim().split(" , ");
//        String anchorLinkIndex = baIndex[1].trim();
//        // ---------------------------------------------------------------------
//        // Get current Anchor SE
//        // Pool_Bep O, AnchorLinks V<String[]{Offset, Length, AName, fileID, barel}
//        String currBepO = this.getCurrTopicBepOffset();
//        Hashtable<String, Vector<String[]>> poolBepAnchorLinksHT = this.getPoolBepAnchorLinksHT();
//        Vector<String[]> currAnchorVSA = poolBepAnchorLinksHT.get(currBepO.toString());
//        String[] currAnchorSA = currAnchorVSA.elementAt(Integer.valueOf(anchorLinkIndex));
//        String anchorOffset = currAnchorSA[0].trim();
//        String anchorLength = currAnchorSA[1].trim();
//        String anchorFileID = currAnchorSA[3].trim();
//        return currTopicBAnchorOLID = new String[]{anchorOffset, anchorLength, anchorFileID};
//    }

//    public String[] getCurrTopicBAnchorSEIDStatus(JTextPane myLinkTextPane, String topicID, String lang) {
//        // String[]{Anchor_SP, EP, File_ID, Status}
//        String[] currTopicBAnchorSEIDStatus = new String[4];
//        // ---------------------------------------------------------------------
//        // ONLY 1 Topic, so topicIndex = 0
//        // anchor-bep Index: 0 , 0 , 0 , 0 --> pool_Anchor , bep_Link , non-used , non-used
//        String[] tbaIndexSA = this.getTBANavigationIndex();
//        String[] baIndex = tbaIndexSA[1].trim().split(" , ");
//        String anchorLinkIndex = baIndex[1].trim();
//        // ---------------------------------------------------------------------
//        // Get current Anchor SE
//        // Pool_Bep O, AnchorLinks V<String[]{Offset, Length, AName, fileID, barel}
//        String currBepOffset = this.getCurrTopicBepOffset();
//        Hashtable<String, Vector<String[]>> poolBepAnchorLinksHT = this.getPoolBepAnchorLinksHT();
//        Vector<String[]> currAnchorVSA = poolBepAnchorLinksHT.get(currBepOffset);
//        String[] currAnchorSA = currAnchorVSA.elementAt(Integer.valueOf(anchorLinkIndex));
//        String anchorOffset = currAnchorSA[0].trim();
//        String anchorLength = currAnchorSA[1].trim();
//        String anchorName = currAnchorSA[2].trim();
//        String anchorFileID = currAnchorSA[3].trim();
//        String anchorStatus = currAnchorSA[4].trim();
//        // ---------------------------------------------------------------------
//        // Link BEP
//        // convery BEP Offset into SCR Start Point
//        FOLTXTMatcher folMatcher = FOLTXTMatcher.getInstance();
//        // Anchor_Name, SP, EP
//        String[] targetASA = folMatcher.getSCRAnchorNameSESA(myLinkTextPane, anchorFileID, new String[]{anchorOffset, anchorLength, anchorName}, lang);
//        currTopicBAnchorSEIDStatus = new String[]{targetASA[1], targetASA[2], anchorFileID, anchorStatus};
//        return currTopicBAnchorSEIDStatus;
//    }

    public Vector<String> getTopicBepsOSVS() {
        Vector<String> topicBepsOSList = new Vector<String>();
        Document doc = readingXMLFromFile(resourceXMLFile);
        NodeList titleNodeList = doc.getElementsByTagName(afTitleTag);
        for (int i = 0; i < titleNodeList.getLength(); i++) {
            Element titleElmn = (Element) titleNodeList.item(i);
            NodeList subNodeList = titleElmn.getElementsByTagName(afBepOffsetTag);
            if (subNodeList.getLength() > 0) {
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

//    public String[] getTopicBepOStatusBySE(String topicID, CurrentFocusedAnchor currSCRSEName) {
//        // String[]{Bep_Offset, S, Status}
//        String[] poolAnchorOLStatus = new String[3];
//        String poolBepS = currSCRSEName.screenPosEndToString();
//        // String[]{Bep_Offset, S, Status}
//        Vector<String[]> topicBepOSStatusVSA = getTopicBepOSStatusVSA();
//        for (String[] topicBepOSStatus : topicBepOSStatusVSA) {
//            String thisPBepO = topicBepOSStatus[0];
//            String thisPBepS = topicBepOSStatus[1];
//            String thisPBepStatus = topicBepOSStatus[2];
//            if (thisPBepS.equals(poolBepS)) {
//                poolAnchorOLStatus = new String[]{thisPBepO, thisPBepS, thisPBepStatus};
//            }
//        }
//        return poolAnchorOLStatus;
//    }

//    public String[] getTopicBepSStatusByOL(String topicID, String[] poolBepOL) {
//        // String[]{Bep_Offset, S, Status}
//        String[] poolAnchorSStatus = new String[2];
//        String poolBepO = poolBepOL[0];
//        // String[]{Bep_Offset, S, Status}
//        Vector<String[]> topicBepOSStatusVSA = getTopicBepOSStatusVSA();
//        for (String[] topicBepOSStatus : topicBepOSStatusVSA) {
//            String thisPBepO = topicBepOSStatus[0];
//            String thisPBepS = topicBepOSStatus[1];
//            String thisPBepStatus = topicBepOSStatus[2];
//            if (thisPBepO.equals(poolBepO)) {
//                poolAnchorSStatus = new String[]{thisPBepS, thisPBepStatus};
//            }
//        }
//        return poolAnchorSStatus;
//    }

//    public Vector<String[]> getTopicBepOSStatusVSA() {
//        // String[]{Bep_Offset, S, Status}
//        Vector<String[]> topicBepSCRStatusVSA = new Vector<String[]>();
//        // topicBepsOSVS: Offset : SP
//        Vector<String> topicBepsOSVS = this.getTopicBepsOSVS();
//        // poolAnchorsOLV: bOffset , bStatus
//        Vector<String[]> poolBepsOLV = this.getPoolBEPsOStatusV();
//        for (String BepOS : topicBepsOSVS) {
//            String[] BepOSSA = BepOS.split(" : ");
//            for (String[] pBepSA : poolBepsOLV) {
//                String pBepOffset = pBepSA[0];
//                String pBepStatus = pBepSA[1];
//                if (BepOSSA[0].trim().equals(pBepOffset)) {
//                    topicBepSCRStatusVSA.add(new String[]{BepOSSA[0].trim(), BepOSSA[1].trim(), pBepStatus});
//                }
//            }
//        }
//        return topicBepSCRStatusVSA;
//    }

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

//    public String getWikipediaPoolXMLFile() {
//        return wikipediaPool;
//    }

//    public String getTeAraPoolXMLFile() {
//        return teAraPool;
//    }
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
    
    

    // =========================================================================
    private Vector<IndexedAnchor> sortOLVectorSANumbers(Vector<AssessedAnchor> poolAnchorsOLV) {
        // Vector<String[]{Anchor_Offset, Length, Status}
        Vector<IndexedAnchor> mySortedOLNumbersV = new Vector<IndexedAnchor>();
        int[] thisIntA = new int[poolAnchorsOLV.size()];
        for (int i = 0; i < poolAnchorsOLV.size(); i++) {
            thisIntA[i] = poolAnchorsOLV.elementAt(i).getOffset(); //Integer.valueOf(myOLSANumbersV.elementAt(i)[0]);
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
//            String thisINT = String.valueOf(thisIntA[i]);
            // ------------------------------------------
            for (IndexedAnchor myOLSA : poolAnchorsOLV) {
                if (myOLSA.getOffset() == thisIntA[i]/*[0].equals(thisINT)*/) {
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

//	public String getTopicFilePath(String topicID, String topicLang) {
//		return AppResource.getInstance().getTopicXmlPathNameByFileID(topicID, topicLang);
//	}
    
    
    public void updateAnchorScreenOffset() {
        Vector<String> anchorSetV = new Vector<String>();
        Vector<IndexedAnchor> poolAnchorsOLNameStatusVSA = this.getPoolAnchorsOLNameStatusV();
        for (IndexedAnchor thisAnchorSet : poolAnchorsOLNameStatusVSA) {
        	String[] scrFOL = thisAnchorSet.getScrFOL();
    		scrFOL[3] = thisAnchorSet.extendedLengthToString(); //[4];
            anchorSetV.add(thisAnchorSet.getScreenPosStart()/*[0]*/ + " : " + thisAnchorSet.lengthToString()/*[1]*/ + " : " + thisAnchorSet.getName()/*[2]*/ + " : " + scrFOL[1] + " : " + scrFOL[2]  + " : " +  thisAnchorSet.extendedLengthToString()/*[4]*/);	
        }
        // ============================
        // record into toolResource.xml
        updateCurrAnchorFOL(anchorSetV);
    }


    // </editor-fold>
    // =========================================================================

}