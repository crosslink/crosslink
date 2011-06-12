package ltwassessment.submission;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import ltwassessment.AppResource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Run {
	
	private HashMap<String, Topic> topics = null;
	private String runName = null;
	
	
    public Run() {
		init();
	}
    
	public Run(File runFile) {
		init();
		
		read(runFile, false);
	}
	
	public String getRunName() {
		return runName;
	}

	public void setRunName(String runName) {
		this.runName = runName;
	}

	private void init() {
		//topics = (HashMap<String, Topic>) Collections.synchronizedMap(new HashMap<String, Topic>());
		topics = new HashMap<String, Topic>();
	}
	
	public HashMap<String, Topic> getTopics() {
		return topics;
	}

	public void add(Topic topic) {
		topics.put(topic.getId(), topic);
	}
	
	public void read(File runFile, boolean checkAnchors) {
        boolean forValidationOrAssessment = AppResource.forValidationOrAssessment;
        String afTitleTag = forValidationOrAssessment ? "crosslink-assessment" : "crosslink-submission";
        String afTopicTag = "topic";
        String afOutgoingTag = forValidationOrAssessment ? "outgoinglinks" : "outgoing";
        String afAnchorTag = "anchor";
        String afSubAnchorTag = "subanchor";
        String afToBepTag = "tofile";
        String offsetAttributeName = forValidationOrAssessment ? "aoffset" : "offset";
        String lengthAttributeName = forValidationOrAssessment ? "alength" : "length";
        String tboffsetAttributeName = forValidationOrAssessment ? "tboffset" : "bep_offset";

        Document xmlDoc = readingXMLFromFile(runFile);

        NodeList titleNodeList = xmlDoc.getElementsByTagName(afTitleTag);
        for (int i = 0; i < titleNodeList.getLength(); i++) {
            Element titleElmn = (Element) titleNodeList.item(i);
            runName =  titleElmn.getAttribute("run-id");
            String sourceLang = titleElmn.getAttribute("source_lang");
            String targetLang = titleElmn.getAttribute("default_lang");
            if (sourceLang.length() > 0)
            	AppResource.sourceLang = sourceLang;
            
            if (targetLang.length() == 0)
            	System.err.println("Error: no \"default_lang\" attribute given in " + afTitleTag + " tag");
            else
            	AppResource.targetLang = targetLang;
            
            NodeList topicNodeList = titleElmn.getElementsByTagName(afTopicTag);
            for (int j = 0; j < topicNodeList.getLength(); j++) {
                Element topicElmn = (Element) topicNodeList.item(j);
                String thisTopicID = topicElmn.getAttribute("file");
                String thisTopicName = topicElmn.getAttribute("name");
                Topic topic = topics.get(thisTopicID);
                if (topic == null) {
                	topic = new Topic(thisTopicID, thisTopicName);
                	topics.put(thisTopicID, topic);
                }
//                if (thisTopicID.equals(topicFileID)) {
                NodeList linksNodeList = topicElmn.getElementsByTagName(afOutgoingTag);
                Element outgoingElmn = (Element) linksNodeList.item(0);
                NodeList anchorNodeList = outgoingElmn.getElementsByTagName(afAnchorTag);
                String anchorKey = "";
//                    Vector<String[]> anchorToBEPV;
                LinkedAnchorList anchors = topic.getAnchors();
                for (int k = 0; k < anchorNodeList.getLength(); k++) {
                    Element anchorElmn = (Element) anchorNodeList.item(k);
                    String aOffset = anchorElmn.getAttribute( offsetAttributeName);
                    String aLength = anchorElmn.getAttribute(lengthAttributeName);
                    String anchorName = anchorElmn.getAttribute("name");
                    	
//                        anchorToBEPV = new Vector<String[]>();
                    Anchor anchor = new Anchor(Integer.parseInt(aOffset), Integer.parseInt(aLength), anchorName);
                    if (anchor.validate(topic, Anchor.SHOW_MESSAGE_ERROR)) {
                        Target target = null;
                        if (forValidationOrAssessment) {
                        	anchorKey = aOffset + "_" + aLength;
                            NodeList subAnchorNodeList = anchorElmn.getElementsByTagName(afSubAnchorTag);
                            for (int l = 0; l < subAnchorNodeList.getLength(); l++) {
                                Element subAnchorElmn = (Element) subAnchorNodeList.item(l);
                                NodeList toBepNodeList = subAnchorElmn.getElementsByTagName(afToBepTag);
                                for (int m = 0; m < toBepNodeList.getLength(); m++) {
                                    Element toBepElmn = (Element) toBepNodeList.item(m);
                                    String tbOffset = toBepElmn.getAttribute(tboffsetAttributeName);                                
                                    String tbStartP = toBepElmn.getAttribute("tbstartp");
                                    String tbRel = toBepElmn.getAttribute("tbrel");

                                    Node tbXmlFileIDTextNode = toBepElmn.getFirstChild();
                                    String tbFileID = tbXmlFileIDTextNode.getTextContent();
                                    
                                   // anchorToBEPV.add(new String[]{tbOffset, tbStartP, tbFileID, tbRel});
                                }
                            }
                        }
                        else {
                        	anchorKey = aOffset + "_" + (Integer.valueOf(aOffset) + Integer.valueOf(aLength)) + "_" + anchorName;
                            NodeList toBepNodeList = anchorElmn.getElementsByTagName(afToBepTag);
                            for (int m = 0; m < toBepNodeList.getLength(); m++) {
                                Element toBepElmn = (Element) toBepNodeList.item(m);
                                
                                // new
                                String target_lang = toBepElmn.getAttribute("lang");
                                String target_title = toBepElmn.getAttribute("title");

                                String tbOffset = toBepElmn.getAttribute("bep_offset");
                                Node tbXmlFileIDTextNode = toBepElmn.getFirstChild();
                                String tbFileID = tbXmlFileIDTextNode.getTextContent();
                                
                                //anchorToBEPV.add(new String[]{tbFileID, tbOffset, target_lang, target_title});
                                target = new Target(target_lang, target_title, tbFileID, Integer.parseInt(tbOffset));
                                anchor.insertTarget(target);
                            }
                        }
//                        anchorBepsHT.put(anchorKey, anchorToBEPV);
                        anchor.setRank(k);
//                        if (anchor.getName().equals("古代") && anchor.getTargets().containsKey("4946747"))
//                        	System.err.println("I got you!");
                        anchors.insert(anchor);
                    }
                }
//                }
                    

            }
        } // for

	}

	public static Document readingXMLFromFile(File xmlFile) {
        DocumentBuilderFactory dBF = DocumentBuilderFactory.newInstance();
        dBF.setIgnoringComments(true);
        // Ignore the comments present in the XML File when reading the xml
        DocumentBuilder builder = null;
        Document doc = null;
        try {
            builder = dBF.newDocumentBuilder();
            doc = builder.parse(xmlFile);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }
	
	public void validate(int showMessage) {
		Set set = getTopics().entrySet();

	    Iterator i = set.iterator();

	    while(i.hasNext()) {
	    	Map.Entry me = (Map.Entry)i.next();
	    	Topic topic = (Topic)me.getValue();
	    	
	    	topic.validateIt(showMessage);
	    }
	}
}
