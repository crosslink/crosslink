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
import ltwassessment.parsers.ResourcesManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import crosslink.XML2TXT;

public class Run<AnchorSet>{
	
	private HashMap<String, Topic> topics = null;
	private String runName = null;
	private boolean convertToTextOffset = true;
	private String runSourceLang = null;
	private String runTargetLang = null;
	private boolean checkAnchors = false;
	private boolean needSorted = false;
	
    public Run() {
		init();
	}
    
    public Run(File runFile) {
		init();	
    }
    
	public Run(File runFile, String sourceLang, String targetLang) {
		runSourceLang = sourceLang;
		runTargetLang = targetLang;
		init();
	}
	
	public String getRunName() {
		return runName;
	}

	public void setRunName(String runName) {
		this.runName = runName;
	}

	/**
	 * @return the convertToTextOffset
	 */
	public boolean isConvertToTextOffset() {
		return convertToTextOffset;
	}

	/**
	 * @param convertToTextOffset the convertToTextOffset to set
	 */
	public void setConvertToTextOffset(boolean convertToTextOffset) {
		this.convertToTextOffset = convertToTextOffset;
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
	
	public void read(File runFile) {
		read(runFile, checkAnchors, runSourceLang, runTargetLang, true);
	}
	
	public void read(File runFile, boolean needSorted) {
		read(runFile, checkAnchors, runSourceLang, runTargetLang, needSorted);
	}
	
	public void read(File runFile, boolean checkAnchors, String sourceLang, String targetLang) {
		read(runFile, checkAnchors, sourceLang, targetLang, true);
	}
	
	public void read(File runFile, boolean checkAnchors, String sourceLang, String targetLang, boolean needSorted) {
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

        Document xmlDoc = ResourcesManager.readingXMLFromFile(runFile.getAbsolutePath());

        NodeList titleNodeList = xmlDoc.getElementsByTagName(afTitleTag);
        byte[] bytes = null;
        
		int aOffset = 0;
		int aLength = 0;
        for (int i = 0; i < titleNodeList.getLength(); i++) {
            Element titleElmn = (Element) titleNodeList.item(i);
            runName =  titleElmn.getAttribute("run-id");
            runSourceLang = titleElmn.getAttribute("source_lang").trim().toLowerCase();
            if (runSourceLang.length() == 0)
            	runSourceLang = "en";
            runTargetLang = titleElmn.getAttribute("default_lang").trim().toLowerCase();
            
            if (sourceLang == null && targetLang == null) {
	            if (runSourceLang.length() > 0)
	            	AppResource.sourceLang = runSourceLang;
	            
	            if (runTargetLang.length() == 0)
	            	System.err.println("Error: no \"default_lang\" attribute given in " + afTitleTag + " tag");
	            else
	            	AppResource.targetLang = runTargetLang;
            }
            else {
            	if (!runSourceLang.equalsIgnoreCase(sourceLang) || !runTargetLang.equalsIgnoreCase(targetLang)) {
            		System.err.println("Different source/target lang: " + runSourceLang + "/" + runTargetLang + " for run file " + runFile.getName());
            		break;
            	}
            }
            
            NodeList topicNodeList = titleElmn.getElementsByTagName(afTopicTag);
            for (int j = 0; j < topicNodeList.getLength(); j++) {
                Element topicElmn = (Element) topicNodeList.item(j);
                String thisTopicID = topicElmn.getAttribute("file");
                String thisTopicName = topicElmn.getAttribute("name");
                Topic topic = topics.get(thisTopicID);
                if (topic == null) {
                	topic = new Topic<AnchorSet>(thisTopicID, thisTopicName);
                	topics.put(thisTopicID, topic);
                }
                if (convertToTextOffset) {
                    bytes = topic.getBytes();
                }

//                if (thisTopicID.equals(topicFileID)) {
                NodeList linksNodeList = topicElmn.getElementsByTagName(afOutgoingTag);
                Element outgoingElmn = (Element) linksNodeList.item(0);
                NodeList anchorNodeList = outgoingElmn.getElementsByTagName(afAnchorTag);
                String anchorKey = "";
//                    Vector<String[]> anchorToBEPV;
                AnchorSet anchors = (AnchorSet) topic.getAnchors();
                for (int k = 0; k < anchorNodeList.getLength(); k++) {
                    Element anchorElmn = (Element) anchorNodeList.item(k);
                    try {
                    	aOffset = Integer.parseInt(anchorElmn.getAttribute(offsetAttributeName));
                    }
                    catch (Exception ex) {
                    	aOffset = 0;
                    }
                    try {
                    	aLength = Integer.parseInt(anchorElmn.getAttribute(lengthAttributeName));
                    }
                    catch (Exception ex) {
                    	aLength = 0;
                    }
                    String anchorName = anchorElmn.getAttribute("name");
                    
                    if (convertToTextOffset) {
                    	aLength = XML2TXT.textLength(bytes, aOffset, aLength);
                    	aOffset = XML2TXT.byteOffsetToTextOffset(bytes, aOffset);
                    }
                    	
//                        anchorToBEPV = new Vector<String[]>();
                    if (aLength > 0) {
	                    Anchor anchor = new Anchor(aOffset, aLength, anchorName);
	                    if (checkAnchors == false || (checkAnchors && anchor.validate(topic, Anchor.SHOW_MESSAGE_NONE, convertToTextOffset))) {
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
	                                String target_lang = toBepElmn.getAttribute("lang").trim().toLowerCase();
	                                String target_title = toBepElmn.getAttribute("title");
	
	                                String tbOffset = toBepElmn.getAttribute("bep_offset").trim();
	                                if (tbOffset.length() == 0)
	                                	tbOffset = "0";
	                                Node tbXmlFileIDTextNode = toBepElmn.getFirstChild();
	                                if (tbXmlFileIDTextNode != null) {
		                                String tbFileID = tbXmlFileIDTextNode.getTextContent();
		                                
		                                //anchorToBEPV.add(new String[]{tbFileID, tbOffset, target_lang, target_title});
		                                if (Target.exist(tbFileID, target_lang)) {
			                                target = new Target(target_lang, target_title, tbFileID, Integer.parseInt(tbOffset));
			                                anchor.insertTarget(target);
		                                }
	                                }
	                            }
	                        }
	//                        anchorBepsHT.put(anchorKey, anchorToBEPV);
	                        anchor.setRank(k);
	//                        if (anchor.getName().equals("古代") && anchor.getTargets().containsKey("4946747"))
	//                        	System.err.println("I got you!");
	                        ((AnchorSetInterface)anchors).insert(anchor);
	                    }
                    }
                }
                
                /**
                 * REMEMBER, it has to be sorted before being used
                 */
                if (needSorted)
                	((AnchorSetInterface)anchors).sort();
//                }
                    

            }
        } // for

	}

//	public static Document readingXMLFromFile(File xmlFile) {
//        DocumentBuilderFactory dBF = DocumentBuilderFactory.newInstance();
//        dBF.setIgnoringComments(true);
//        // Ignore the comments present in the XML File when reading the xml
//        DocumentBuilder builder = null;
//        Document doc = null;
//        try {
//            builder = dBF.newDocumentBuilder();
//            doc = builder.parse(xmlFile);
//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
//        } catch (SAXException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return doc;
//    }
	
	public void validate(int showMessage) {
		Set set = getTopics().entrySet();

	    Iterator i = set.iterator();

	    while(i.hasNext()) {
	    	Map.Entry me = (Map.Entry)i.next();
	    	Topic topic = (Topic)me.getValue();
	    	
	    	topic.validateIt(showMessage, this.convertToTextOffset);
	    }
	}
}
