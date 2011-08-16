package ltwassessment.parsers;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import com.sun.org.apache.xpath.internal.XPathAPI;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import ltwassessment.AppResource;
import ltwassessment.Assessment;
import ltwassessment.assessment.IndexedAnchor;
import ltwassessment.parsers.FOLTXTMatcher;
import ltwassessment.parsers.PoolerManager;
import ltwassessment.parsers.ResourcesManager;
import ltwassessment.submission.Anchor;
import ltwassessment.validation.InvalidOffsetException;
import ltwassessment.validation.ValidationMessage;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Darren Huang
 */
public class FOLTXTMatcher {

    private final String sysPropertyKey = "isTABKey";
    private final String sysPropertyIsTopicWikiKey = "isTopicWikipedia";
    private final String sysPropertyIsLinkWikiKey = "isLinkWikipedia";
    private ResourcesManager myRSCManager;
    private PoolerManager myPooler;
    private Vector<String> xmlSingleCharV = new Vector<String>();
    private String wikipediaTopicFileDir = "";
    private static String tempFileDir = "";
    private String fullXmlTxt = "";
    private Vector<String> entityNumExpressV;
    private Vector<String> entityExpressV;
    private String wikipediaTypeName = "";
    private String tearaTypeName = "";
    private boolean isTopicWikipedia = false;
    private boolean isLinkWikipedia = false;
    private static JTextPane dummyPane = new JTextPane();
    
    private static FOLTXTMatcher instance = null;
    
    public static FOLTXTMatcher getInstance() {
    	if (instance == null)
    		instance = new FOLTXTMatcher();
    	return instance;
    }
    
    private static void log(Object text) {
        System.out.println(text);
    }

    public FOLTXTMatcher() {

        this.myRSCManager = ResourcesManager.getInstance();
        this.myPooler = PoolerManager.getInstance();

        //org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ltwassessment.ltwassessmentApp.class).getContext().getResourceMap(ltwassessmentView.class);
        this.wikipediaTopicFileDir = AppResource.getInstance().getResourceMap().getString("wikipedia.topics.folder") + File.separator;
        this.tempFileDir = AppResource.getInstance().getResourceMap().getString("temp.folder") + File.separator;
        this.wikipediaTypeName = AppResource.getInstance().getResourceMap().getString("collectionType.Wikipedia");
        this.tearaTypeName = AppResource.getInstance().getResourceMap().getString("collectionType.TeAra");

        if (this.wikipediaTypeName.equals(myRSCManager.getTopicCollType())) {
            this.isTopicWikipedia = true;
        } else if (this.tearaTypeName.equals(myRSCManager.getTopicCollType())) {
            this.isTopicWikipedia = false;
        }
        if (this.wikipediaTypeName.equals(myRSCManager.getLinkCollType())) {
            this.isLinkWikipedia = true;
        } else if (this.tearaTypeName.equals(myRSCManager.getLinkCollType())) {
            this.isLinkWikipedia = false;
        }
        
        dummyPane.setContentType("text/html; charset=UTF-8");
        
        populateEntityV();
//        getCurrFullXmlText();
    }

    /**
	 * @return the fullXmlTxt
	 */
	public String getFullXmlTxt() {
		return fullXmlTxt;
	}

	/**
	 * @param fullXmlTxt the fullXmlTxt to set
	 */
	public void setFullXmlTxt(String fullXmlTxt) {
		this.fullXmlTxt = fullXmlTxt;
	}

	// =========================================================================
    // =========================================================================
    private void populateEntityV() {
        entityNumExpressV = new Vector<String>();
        entityExpressV = new Vector<String>();

        String entityListFile = "resources" + File.separator + "Tool_Resources" + File.separator + "entityList.txt";
        File entityFile = new File(entityListFile);
        if (entityFile.isFile()) {
            try {
                BufferedReader br = new BufferedReader(
            	        new InputStreamReader(new FileInputStream(entityListFile), "UTF-8")); //new BufferedReader(new FileReader(entityListFile));
                String thisLine = "";
                while ((thisLine = br.readLine()) != null) {
                    String[] thisEntity = thisLine.split(" -- ");
                    if (!entityNumExpressV.contains(thisEntity[0])) {
                        entityNumExpressV.add(thisEntity[0]);
                    }
                    if (!entityExpressV.contains(thisEntity[1])) {
                        entityExpressV.add(thisEntity[1]);
                    }
                }
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(FOLTXTMatcher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    // =========================================================================
    // =========================================================================
    // <editor-fold defaultstate="collapsed" desc="Get F.O.L">

    public String[] getXMLOffsetLength(int srOffset, int srLength, String tilTxt) {
        // [0]:Offset, [1]:Length, [2]: AnchorText
        String[] myXMLPosition = new String[3];
        String myScreenAnchor = tilTxt.substring(srOffset, srOffset + srLength);
        String myScreenTxtTillOffsetWoutSpace = tilTxt.substring(0, srOffset).replaceAll("\\s", "");
        int myScreenOffset = myScreenTxtTillOffsetWoutSpace.length();
        String myXmlAnchor = "";
        String pureScreenTxt = tilTxt.replaceAll("\\s", "");
        Vector<String> scrTxtCharV = new Vector<String>();
        for (int i = 0; i < pureScreenTxt.length(); i++) {
            String mySglChar = pureScreenTxt.substring(i, i + 1);
            scrTxtCharV.add(mySglChar);
        }
        int xmlAnchorOffset = 0;
        int xmlAnchorLength = 0;
        int lastMatchedPosition = 0;
        int xmlCounter = 0;
        for (int j = 0; j < scrTxtCharV.size(); j++) {
            String thisChar = scrTxtCharV.elementAt(j);
            boolean notMatched = true;
            while (notMatched) {
                String xmlChar = xmlSingleCharV.elementAt(xmlCounter);
                // -------------------------------------------------------------
//                log("SCR: " + thisChar + " - XML: " + xmlChar);
                // -------------------------------------------------------------
                // for &minus, &#xfeff;
                boolean isEntity = false;
                boolean isSpecial = false;
                int entityLength = 0;
                if (xmlChar.equals("&")) {
                    String myEntity = "";
                    for (int k = xmlCounter; k < xmlCounter + 10; k++) {
                        entityLength++;
                        myEntity = myEntity + xmlSingleCharV.elementAt(k);
                        if (xmlSingleCharV.elementAt(k).equals(";")) {
                            isEntity = true;
                            break;
                        }
                    }
                    if (isEntity) {
                        if (entityExpressV.contains(myEntity) || entityNumExpressV.contains(myEntity)) {
                            isSpecial = true;
                        } else {
                            isSpecial = true;
                        }
                    } else {
                        isSpecial = false;
                    }
                }
                // -------------------------------------------------------------
                if (thisChar.equals(xmlChar)) {
                    notMatched = false;
                    lastMatchedPosition = xmlCounter;
                    if (j == myScreenOffset) {
                        xmlAnchorOffset = xmlCounter;
                    }
                } else if (isSpecial) {
                    notMatched = false;
                    lastMatchedPosition = xmlCounter + (entityLength - 1);
                    if (j == myScreenOffset) {
                        xmlAnchorOffset = xmlCounter;
                    }
                }
                xmlCounter++;
            }
            // If still NO MATCHED --> Keep loop BOTH until their FIRST match
            if (notMatched) {
                xmlCounter = lastMatchedPosition + 1;
            }
        }
        xmlAnchorLength = xmlCounter - xmlAnchorOffset;
        myXmlAnchor = fullXmlTxt.substring(xmlAnchorOffset, xmlCounter);
        myXMLPosition[0] = String.valueOf(xmlAnchorOffset);
        myXMLPosition[1] = String.valueOf(xmlAnchorLength);
        myXMLPosition[2] = myXmlAnchor;
        return myXMLPosition;

    }
    // =========================================================================

//    private String[] screenOffsetLengthFinder(String fullScreenTxt, String fullXmlTxt, String[] thisAnchorXmlOLName) {
//        // thisAnchorSet --> [0]:Offset, [1]:Length, [2]:Anchor_Name
//        // myScreenPosition --> [0]:Anchor_Name, [1]:Offset, [2]:Offset+Length
//        String[] myScreenPosition = new String[3];
//        String aOffset = thisAnchorXmlOLName[0];
//        String aLength = thisAnchorXmlOLName[1];
//        String aName = thisAnchorXmlOLName[2];
//        String myXmlTxt = fullXmlTxt.substring(0, (Integer.valueOf(aOffset) + Integer.valueOf(aLength)));
//        String myPureXmlTxtUntilOL = myXmlTxt.replaceAll("[\\s]+", "");
//        String myPureXmlOffsetTxt = fullXmlTxt.substring(0, Integer.valueOf(aOffset)).replaceAll("[\\s]+", "");
//        int myPureXmlOffset = myPureXmlOffsetTxt.length() - 1;  // counting from 0
//        Vector<String> pureXmlCharUntilOLV = new Vector<String>();
//        for (int i = 0; i < myPureXmlTxtUntilOL.length(); i++) {
//            pureXmlCharUntilOLV.add(myPureXmlTxtUntilOL.substring(i, i + 1));
//        }
//        Vector<String> screenTxtCharV = new Vector<String>();
//        for (int j = 0; j < fullScreenTxt.length(); j++) {
//            screenTxtCharV.add(fullScreenTxt.substring(j, j + 1));
//        }
//
//        int scrAnchorOffset = 0;
//        int scrAnchorLength = 0;
//        int lastMatchedPosition = 0;
//        int scrCharCounter = 0;
//        for (int i = 0; i < pureXmlCharUntilOLV.size(); i++) {
//            String thisXmlChar = pureXmlCharUntilOLV.elementAt(i);
//            // -------------------------------------------------------------
//            boolean isEntity = false;
//            boolean isSpecial = false;
//            int entityLength = 0;
//            if (thisXmlChar.equals("&")) {
//                String myEntity = "";
//                for (int j = i; j < i + 10; j++) {
//                    entityLength++;
//                    myEntity = myEntity + pureXmlCharUntilOLV.elementAt(j);
//                    if (pureXmlCharUntilOLV.elementAt(j).equals(";")) {
//                        isEntity = true;
//                        break;
//                    }
//                }
//                if (isEntity) {
//                    if (entityExpressV.contains(myEntity) || entityNumExpressV.contains(myEntity)) {
//                        isSpecial = true;
//                    } else {
//                        isSpecial = true;
//                    }
//                } else {
//                    isSpecial = false;
//                }
//            }
//            // -------------------------------------------------------------
//            if (isSpecial) {
//                i = i + (entityLength - 1);
//                if (i - (entityLength - 1) <= myPureXmlOffset && myPureXmlOffset <= i) {
//                    scrAnchorOffset = lastMatchedPosition + 1;
//                }
//                scrCharCounter++;
//            } else {
//                boolean notMatched = true;
//                while (notMatched) {
//                    String scrChar = screenTxtCharV.elementAt(scrCharCounter);
//                    if (scrCharCounter < 10) {
////                        log("XML: " + thisXmlChar + " <-> " + "SCR: " + scrChar);
//                    }
//                    if (thisXmlChar.equals(scrChar)) {
//                        notMatched = false;
//                        lastMatchedPosition = scrCharCounter;
//                        if (i == myPureXmlOffset) {
//                            scrAnchorOffset = scrCharCounter;
//                        }
//                    }
//                    scrCharCounter++;
//                }
//            }
//        }
//        scrAnchorLength = scrCharCounter - scrAnchorOffset;
//        myScreenPosition[0] = String.valueOf(aName);
//        myScreenPosition[1] = String.valueOf(scrAnchorOffset);
//        myScreenPosition[2] = String.valueOf(scrAnchorOffset + scrAnchorLength);
//
//        return myScreenPosition;
//    }
    
	private String DeXMLify(String input)
	{
		//System.out.print("XMLify("+input+")=");
		input=input.replaceAll("&amp;", "&");
		input=input.replaceAll("&lt;", "<");
		input=input.replaceAll("&gt;", ">");
		input=input.replaceAll("&quot;", "\"");
		input=input.replaceAll("&apos;", "'");
		input=input.replaceAll("&nbsp;", " ");
		
		input=input.replaceAll("&#32;", " ");
		input=input.replaceAll("&#38;", "&");
		input=input.replaceAll("&#60;", "<");
		input=input.replaceAll("&#62;", ">");
		input=input.replaceAll("&#34;", "\"");
		input=input.replaceAll("&#39;", "'");
		//System.out.println(input);
		return input;
	}
	
    private String[] screenOffsetLengthFinder(String fullScreenTxt, String fullXmlTxt, IndexedAnchor thisAnchorSet) {
    	String[] myScreenPosition = new String[4];
        
		int aOffset = thisAnchorSet.getOffset(); //Integer.valueOf(thisAnchorSet[0]);
		int aLength = thisAnchorSet.getLength(); //Integer.valueOf(thisAnchorSet[1]);

    	byte[] bytes = fullXmlTxt.getBytes();
//    	int len = bytes.length - aOffset;
    	
    	int textOffset = aOffset; //byteOffsetToTextOffset(bytes, aOffset);
    	int anchorTextLength = aLength; //textLength(bytes, aOffset, aLength);

    	String aName = fullXmlTxt.substring(textOffset, textOffset + anchorTextLength);
    	if (!aName.equals(thisAnchorSet.getName())) {
    		System.err.println("Something serious happened: different anchors for " + thisAnchorSet.toString());
    	}
    		
    	String fullName = fullXmlTxt.substring(textOffset, textOffset + anchorTextLength + thisAnchorSet.getExtendedLength());
    	
    	thisAnchorSet.setName(fullName);

    	try {
	        myScreenPosition[0] = ""; //String.valueOf(aName);
	        myScreenPosition[1] = String.valueOf(0);
	        myScreenPosition[2] = String.valueOf(0);
	
	        if (aName.trim().length() > 0) {
	        	int offset = Integer.parseInt(screenBepOffsetFinder(fullScreenTxt, fullXmlTxt, Integer.toString(textOffset)));
	//        if (Character.isWhitespace(fullScreenTxt.charAt(offset))) {
	//	        while (Character.isWhitespace(fullScreenTxt.charAt(offset)))
	//	        	--offset;
	//	        ++offset;
	//        }
		        myScreenPosition[1] = String.valueOf(offset);
		        myScreenPosition[2] = String.valueOf(offset + anchorTextLength);
		        myScreenPosition[0] = fullScreenTxt.substring(offset, offset + aName.length());
	        }
//	        myScreenPosition[3] = String.valueOf(anchorTextExtLength);
    	}
    	catch (InvalidOffsetException ioe) {
    		ValidationMessage.getInstance().append("Invalid offset " + aOffset + " for anchor " + aName);
    		ioe.printStackTrace();
    	}
    	catch (Exception ex){
    		ex.printStackTrace();
    	}

    	return myScreenPosition;
    }
    // =========================================================================

    private String screenBepOffsetFinder(String fullScreenTxt, String fullXmlTxt, String bepOffsetStr) throws InvalidOffsetException {
    	if (bepOffsetStr.length() == 0)
    		bepOffsetStr = "0";

    	int bepOffset = Integer.parseInt(bepOffsetStr);
//    	if (bepOffset == 399)
//    		System.err.println("I got you");
        String source = null;
        //source = new String(parseXmlText(fullXmlTxt.substring(bepOffset)), "UTF-8");
		source = parseXmlText(fullXmlTxt.substring(bepOffset));

//      List<String> puzzles = new Vector<String>();
//      source = DeXMLify(source);
      //String puzzle = source.replaceAll("[\\W]+", " ");

		String puzzle = source.replaceAll("\\s+", "");
		int offset = puzzle.length();

		int pos = 0;
		if (offset > fullScreenTxt.length())
			throw new InvalidOffsetException();
		
		offset = fullScreenTxt.length() - offset;
		String part = fullScreenTxt.substring(offset);
		
		part = part.replaceAll("\\s+", "");
		StringBuffer sb = new StringBuffer(part);
		
		int sb_len = sb.length();
		int puzzle_len = puzzle.length();
		int gap = 0;
		String replace_part = null;
		while (offset > 0) {
			//puzzle.equals(part) || 
			sb_len = sb.length();
			puzzle_len = puzzle.length();
			if (sb_len >= puzzle_len/* || puzzle.equals(sb.toString())*/) {
				try {
				String newSB = new String(sb.toString().getBytes(), "UTF-8");
				if (AppResource.debug == true && /*!Arrays.equals(puzzle.getBytes(), sb.toString().getBytes())*/!puzzle.equals(newSB)) {
					System.err.println("Puzzle:");
					System.err.println(puzzle);
					System.err.println("SB:");
					System.err.println(newSB.substring(newSB.length() - puzzle_len));
					System.err.println("");
				}

//					System.err.println();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
		        break;
			}
		
			gap = puzzle.length() - sb.length();
			offset -= gap;
			part = fullScreenTxt.substring(offset, offset + gap);
			replace_part = part.replaceAll("\\s+", "");
			sb.insert(0, replace_part);
		}
		return String.valueOf(offset);
    }
//    private String screenBepOffsetFinder(String fullScreenTxt, String fullXmlTxt, String bepOffset) {
//        // thisAnchorSet --> [0]:Offset, [1]:Length, [2]:Anchor_Name
//        // myScreenPosition --> [0]:Anchor_Name, [1]:Offset, [2]:Offset+Length
//        String mySCRBepOffset = "0";
//        String xmlBepOffset = bepOffset;
//        String myXmlTxt = fullXmlTxt.substring(0, Integer.valueOf(xmlBepOffset));
//        String myPureXmlTxt = myXmlTxt.replaceAll("\\s", "");
//        int myPureXmlOffset = myPureXmlTxt.length();
//        Vector<String> pureXmlCharV = new Vector<String>();
//        for (int i = 0; i < myPureXmlTxt.length(); i++) {
//            pureXmlCharV.add(myPureXmlTxt.substring(i, i + 1));
//        }
//        Vector<String> screenTxtCharV = new Vector<String>();
//        for (int j = 0; j < fullScreenTxt.length(); j++) {
//            screenTxtCharV.add(fullScreenTxt.substring(j, j + 1));
//        }
//        int scrBepOffset = 0;
//        int lastMatchedPosition = 0;
//        int scrCharCounter = 0;
//        for (int i = 0; i < pureXmlCharV.size(); i++) {
//            String thisXmlChar = pureXmlCharV.elementAt(i);
//            // -------------------------------------------------------------
//            boolean isEntity = false;
//            boolean isSpecial = false;
//            int entityLength = 0;
//            if (thisXmlChar.equals("&")) {
//                String myEntity = "";
//                for (int j = i; j < i + 10; j++) {
//                    entityLength++;
//                    myEntity = myEntity + pureXmlCharV.elementAt(j);
//                    if (pureXmlCharV.elementAt(j).equals(";")) {
//                        isEntity = true;
//                        break;
//                    }
//                }
//                if (isEntity) {
//                    if (entityExpressV.contains(myEntity) || entityNumExpressV.contains(myEntity)) {
//                        isSpecial = true;
//                    } else {
//                        isSpecial = true;
//                    }
//                } else {
//                    isSpecial = false;
//                }
//            }
//            // -------------------------------------------------------------
//            if (isSpecial) {
//                i = i + (entityLength - 1);
//                if (i - (entityLength - 1) <= myPureXmlOffset && myPureXmlOffset <= i) {
//                    scrBepOffset = lastMatchedPosition + 1;
//                }
//                scrCharCounter++;
//            } else {
//                boolean notMatched = true;
//                while (notMatched) {
//                    String scrChar = screenTxtCharV.elementAt(scrCharCounter);
//                    if (thisXmlChar.equals(scrChar)) {
//                        notMatched = false;
//                        lastMatchedPosition = scrCharCounter;
//                    }
//                    scrCharCounter++;
//                }
//            }
//        }
//        scrBepOffset = scrCharCounter;
//        mySCRBepOffset = String.valueOf(scrBepOffset);
//        return mySCRBepOffset;
//    }
    // =========================================================================

    public void getCurrFullXmlText() {
//        this.isTopicWikipedia = Boolean.valueOf(System.getProperty(sysPropertyIsTopicWikiKey));
        String xmlFilePath = getCurrentTopicFilePath();
        String targetFilePath = "";

            if (xmlFilePath.lastIndexOf(File.separator) >= 0){
                targetFilePath = this.tempFileDir + xmlFilePath.substring(xmlFilePath.lastIndexOf(File.separator), xmlFilePath.lastIndexOf(".xml")) + "_pureTxt.txt";
            } else if (xmlFilePath.lastIndexOf(File.separator) >= 0) {
                targetFilePath = this.tempFileDir + xmlFilePath.substring(xmlFilePath.lastIndexOf(File.separator), xmlFilePath.lastIndexOf(".xml")) + "_pureTxt.txt";
            }

        String wikipediaTxt = ConvertXMLtoTXT(xmlFilePath, targetFilePath);
        fullXmlTxt = wikipediaTxt;
        for (int i = 0; i < wikipediaTxt.length(); i++) {
            String mySingle = wikipediaTxt.substring(i, i + 1);
            xmlSingleCharV.add(mySingle);
        }
    }

    private static String getFullXmlText(String fileID, String xmlFilePath) {
//        this.isTopicWikipedia = Boolean.valueOf(System.getProperty(sysPropertyIsTopicWikiKey));
        String targetFilePath = tempFileDir + fileID + "_pureTxt.txt";
        String wikipediaTxt = ConvertXMLtoTXT(xmlFilePath, targetFilePath);
        return wikipediaTxt;
    }

    private String getFullXmlTextByFileID(String fileID, String lang) {
        String xmlFilePath = myPooler.getXmlFilePathByTargetID(fileID, lang);
        return getFullXmlText(fileID, xmlFilePath);
    }

    public static String getTopicFullXmlTextByFileID(String fileID) {
        String xmlFilePath = AppResource.getInstance().getTopicXmlPathNameByFileID(fileID);
         return getFullXmlText(fileID, xmlFilePath);
    }

    public String getWikiXmlTitleByFilePath(String xmlFilePath) {
        this.isTopicWikipedia = Boolean.valueOf(System.getProperty(sysPropertyIsTopicWikiKey));
        String myWikiTitle = "";
        String topTag = "";
        String titleTag = "";
        if (this.isTopicWikipedia) {
            topTag = "article";
            titleTag = "title";
        } else {
            topTag = "Entry";
            titleTag = "Name";
        }
        try {
            Document myXmlDoc = LoadDocument(xmlFilePath);
            NodeList articleNodeList = myXmlDoc.getElementsByTagName(topTag);
            Element artElmn = (Element) articleNodeList.item(0);
            NodeList titleNodeList = artElmn.getElementsByTagName(titleTag);
            Element titleElmn = (Element) titleNodeList.item(0);
            Node titleNode = titleElmn.getFirstChild();
            myWikiTitle = titleNode.getTextContent();
        } catch (java.lang.NullPointerException npe) {
            if (xmlFilePath.lastIndexOf(File.separator)>=0){
                myWikiTitle = xmlFilePath.substring(xmlFilePath.lastIndexOf(File.separator) + 1, xmlFilePath.lastIndexOf(".xml"));
            } else if (xmlFilePath.lastIndexOf(File.separator)>=0){
                myWikiTitle = xmlFilePath.substring(xmlFilePath.lastIndexOf(File.separator) + 1, xmlFilePath.lastIndexOf(".xml"));
            }
        }
        return myWikiTitle;
    }

    private String getCurrentTopicFilePath() {
        return myRSCManager.getCurrTopicXmlFile();
    }

    public Vector<String[]> getSCRAnchorPosV(JTextPane textPane, String currTopicID, Hashtable<String, Vector<IndexedAnchor>> topicAnchorsHT) {
        // "outgoing : " + thisTopicFile, anchorsVbyTopic
        // Offset : Length : Anchor_Name : scrOffset : scrLength
        // record into toolResource.xml
//        Vector<String> anchorSetV = new Vector<String>();
        // anchorsVbyTopic --> [0]:Offset, [1]:Length, [3]:Anchor_Name
        Vector<IndexedAnchor> anchorOLV = null; //new Vector<String[]>();
        Enumeration keyEnu = topicAnchorsHT.keys();
        while (keyEnu.hasMoreElements()) {
            Object keyObj = keyEnu.nextElement();
            String[] keySet = keyObj.toString().split(" : ");
            String thisTopicID = keySet[1];
            if (currTopicID.equals(thisTopicID)) {
                anchorOLV = topicAnchorsHT.get(keyObj);
                break;
            }
        }
        // [0]:Offset, [1]:Length, [3]:Anchor_Name
        String fullScreenText = "";
        try {
            fullScreenText = textPane.getDocument().getText(0, textPane.getDocument().getLength());
        } catch (BadLocationException ex) {
            Logger.getLogger(FOLTXTMatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        Vector<String[]> screenAnchorPos = new Vector<String[]>();
        for (IndexedAnchor thisAnchorSet : anchorOLV) {
//        	if (thisAnchorSet.getOffset() == 3001)
//        		System.out.println("I got you");
            String[] scrFOL = screenOffsetLengthFinder(fullScreenText, fullXmlTxt, thisAnchorSet);
    		scrFOL[3] = thisAnchorSet.extendedLengthToString(); //[4];
    		thisAnchorSet.setScrFOL(scrFOL);
            screenAnchorPos.add(scrFOL);
//            anchorSetV.add(thisAnchorSet.getOffset()/*[0]*/ + " : " + thisAnchorSet.lengthToString()/*[1]*/ + " : " + thisAnchorSet.getName()/*[2]*/ + " : " + scrFOL[1] + " : " + scrFOL[2]  + " : " +  thisAnchorSet.extendedLengthToString()/*[4]*/);
        }
        // ============================
        return screenAnchorPos;
    }

    public String[] getSCRAnchorPosSA(JTextPane myTextPane, String fileID, IndexedAnchor thisAnchorXmlOLName, String lang) {
            String myFullXmlTxt = getFullXmlTextByFileID(fileID, lang);
        return getSCRAnchorPosSA(myTextPane, fileID, thisAnchorXmlOLName, myFullXmlTxt, lang);
    }

    public String[] getSCRAnchorPosSA(JTextPane myTextPane, String fileID, IndexedAnchor thisAnchorXmlOLName, String myFullXmlTxt, String lang) {
        // myScreenAnchorOL/thisAnchorSet
        // --> [0]:Offset, [1]:Length, [2]:Anchor_Name
        String[] myScreenAnchorPos = new String[3];

        String fullScreenText = "";
        try {
            fullScreenText = myTextPane.getDocument().getText(0, myTextPane.getDocument().getLength());
        } catch (BadLocationException ex) {
            Logger.getLogger(FOLTXTMatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        return myScreenAnchorPos = screenOffsetLengthFinder(fullScreenText, myFullXmlTxt, thisAnchorXmlOLName);
    }

    public String getScreenBepOffset(JTextPane linkTxtPane, String bepFileID, String xmlBepOffset, boolean isWikipedia) {
        String scrBepOffset = "0";
        String fullScreenText = "";
        String fullXmlText = "";
        try {
            fullScreenText = linkTxtPane.getDocument().getText(0, linkTxtPane.getDocument().getLength());
            String bepFilePath = "";
//            if (isWikipedia) {
            bepFilePath = myRSCManager.getWikipediaFilePathByName(bepFileID + ".xml", AppResource.targetLang);
            String targetFilePath = "resources" + File.separator + "Temp" + File.separator + bepFileID + "_pureTxt.txt";
            fullXmlText = ConvertXMLtoTXT(bepFilePath, targetFilePath);
        } catch (BadLocationException ex) {
            Logger.getLogger(FOLTXTMatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
        	if (Integer.parseInt(xmlBepOffset) == 0)
        		scrBepOffset = "0";
        	else
        		scrBepOffset = screenBepOffsetFinder(fullScreenText, fullXmlText, xmlBepOffset);
		} catch (InvalidOffsetException e) {
			e.printStackTrace();
		}
        return scrBepOffset;
    }

    public String[] getSCRAnchorNameSESA(JTextPane myTextPane, String fileID, IndexedAnchor thisAnchorXmlOLName, String lang) {
        // myScreenAnchorOL/thisAnchorSet
        String[] myScreenAnchorPos = new String[3];
        String myFullXmlTxt = getFullXmlTextByFileID(fileID, lang);
        String fullScreenText = "";
        try {
            fullScreenText = myTextPane.getDocument().getText(0, myTextPane.getDocument().getLength());
        } catch (BadLocationException ex) {
            Logger.getLogger(FOLTXTMatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        // --> [0]:Anchor_Name, [1]:SP, [2]:EP
        return myScreenAnchorPos = screenOffsetLengthFinder(fullScreenText, myFullXmlTxt, thisAnchorXmlOLName);
    }
    
    public Vector<String[]> getSCRBepPosV(JTextPane txtPane, String xmlFileID, Vector<String[]> xmlBepOffsetV, boolean isWikipedia) {
        Vector<String[]> scrBepOffsetV = new Vector<String[]>();
        Vector<String> bepSetV = new Vector<String>();
        String fullScreenText = "";
        String fullXmlText = "";
        try {
            fullScreenText = txtPane.getDocument().getText(0, txtPane.getDocument().getLength());
            String xmlFilePath = "";
//            if (isWikipedia) {
                xmlFilePath = wikipediaTopicFileDir + xmlFileID + ".xml";
//            } else {
//                xmlFilePath = myRSCManager.getTeAraCollectionFolder() + myRSCManager.getTeAraFilePathByName(xmlFileID + ".xml");
//            }
            String targetFilePath = AppResource.getInstance().getTopicPathNameByFileID(xmlFileID) + "_pureTxt.txt";
            fullXmlText = ConvertXMLtoTXT(xmlFilePath, targetFilePath);
        } catch (BadLocationException ex) {
            Logger.getLogger(FOLTXTMatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        int[] xmlBepO = new int[xmlBepOffsetV.size()];
        int bepCounter = 0;
        for (String[] xmlBepOffsetSA : xmlBepOffsetV) {
            String thisXmlBepOffset = xmlBepOffsetSA[0].trim();
            xmlBepO[bepCounter] = Integer.valueOf(thisXmlBepOffset);
            bepCounter++;
        }
        Arrays.sort(xmlBepO);
        for (int xmlBepOffsetI : xmlBepO) {
            String thisXmlBepOffset = String.valueOf(xmlBepOffsetI);
            String scrBepOffset = "0";
			try {
				scrBepOffset = screenBepOffsetFinder(fullScreenText, fullXmlText, thisXmlBepOffset);
			} catch (InvalidOffsetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            scrBepOffsetV.add(new String[]{scrBepOffset, String.valueOf(Integer.valueOf(scrBepOffset) + 4)});
            bepSetV.add(thisXmlBepOffset + " : " + scrBepOffset);
        }
        // ============================
        // record into toolResource.xml
        myRSCManager.updateCurrBepOffsetList(bepSetV);
        // ============================
        return scrBepOffsetV;
    }

    public String getBepSCRSP(JTextPane myTxtPane, String bepFileID, String bepOffset, boolean isWikipedia, String lang) {
        String scrBepOffset = "";
        String fullScreenText = "";
        String fullXmlText = "";
        try {
            fullScreenText = myTxtPane.getDocument().getText(0, myTxtPane.getDocument().getLength());
            String bepFilePath = "";
//            if (isWikipedia) {
                String subPath = myRSCManager.getWikipediaFilePathByName(bepFileID + ".xml", lang);
                if (subPath.equals("FileNotFound.xml")) {
                    bepFilePath = "resources" + File.separator + "Tool_Resources" + File.separator + subPath;
                } else {
                    bepFilePath = subPath;
                }
//            } else {
//                String subPath = myRSCManager.getTeAraFilePathByName(bepFileID + ".xml");
//                if (subPath.equals("FileNotFound.xml")) {
//                    bepFilePath = "resources\\Tool_Resources\\" + subPath;
//                } else {
//                    bepFilePath = subPath;
//                }
//            }
            String targetFilePath = "resources" + File.separator + "Temp" + File.separator + bepFileID + "_pureTxt.txt";
            fullXmlText = ConvertXMLtoTXT(bepFilePath, targetFilePath);
        } catch (BadLocationException ex) {
            Logger.getLogger(FOLTXTMatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
        	scrBepOffset = screenBepOffsetFinder(fullScreenText, fullXmlText, bepOffset);
		} catch (InvalidOffsetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return scrBepOffset;
    }
    
    private Vector<String> getLinkXmlTextByID(String linkID, String lang) {
        Vector<String> xmlSglCharV = new Vector<String>();
        String xmlFilePath = "";
        String targetFilePath = "";
//        if (Boolean.valueOf(System.getProperty(sysPropertyIsLinkWikiKey))) {
            xmlFilePath = this.myRSCManager.getWikipediaFilePathByName(linkID + ".xml", lang);
            targetFilePath = this.tempFileDir + xmlFilePath.substring(xmlFilePath.lastIndexOf("/") + 1, xmlFilePath.lastIndexOf(".xml")) + "_pureTxt.txt";
//        } else {
//            xmlFilePath = this.myRSCManager.getTeAraFilePathByName(linkID + ".xml");
//            targetFilePath = this.tempFileDir + xmlFilePath.substring(xmlFilePath.lastIndexOf("\\"), xmlFilePath.lastIndexOf(".xml")) + "_pureTxt.txt";
//        }
        String wikipediaTxt = ConvertXMLtoTXT(xmlFilePath, targetFilePath);
        for (int i = 0; i < wikipediaTxt.length(); i++) {
            String mySingle = wikipediaTxt.substring(i, i + 1);
            xmlSglCharV.add(mySingle);
        }
        return xmlSglCharV;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Get F.O.L">
    public String getXmlBepOffset(String linkID, String tilTxt, String lang) {
        String xmlBepOffset = "";
        // [0]:Offset, [1]:Length, [2]: AnchorText
        Vector<String> xmlSglCharV = this.getLinkXmlTextByID(linkID, lang);
        String pureScreenTxt = tilTxt.replaceAll("[\\s]+", "");
        Vector<String> scrPureTxtCharV = new Vector<String>();
        for (int i = 0; i < pureScreenTxt.length(); i++) {
            String mySglChar = pureScreenTxt.substring(i, i + 1);
            scrPureTxtCharV.add(mySglChar);
        }
        int xmlAnchorOffset = 0;
        int xmlAnchorLength = 0;
        int lastMatchedPosition = 0;
        int xmlCounter = 0;
        for (int j = 0; j < scrPureTxtCharV.size(); j++) {
            String thisChar = scrPureTxtCharV.elementAt(j);
            boolean notMatched = true;
            while (notMatched) {
                String xmlChar = xmlSglCharV.elementAt(xmlCounter);
                // for &minus, &#xfeff;
                boolean isEntity = false;
                boolean isSpecial = false;
                int entityLength = 0;
                if (xmlChar.equals("&")) {
                    String myEntity = "";
                    for (int k = xmlCounter; k < xmlCounter + 10; k++) {
                        entityLength++;
                        myEntity = myEntity + xmlSglCharV.elementAt(k);
                        if (xmlSglCharV.elementAt(k).equals(";")) {
                            isEntity = true;
                            break;
                        }
                    }
                    if (isEntity) {
                        if (entityExpressV.contains(myEntity) || entityNumExpressV.contains(myEntity)) {
                            isSpecial = true;
                        } else {
                            isSpecial = true;
                        }
                    } else {
                        isSpecial = false;
                    }
                }
                // -------------------------------------------------------------
                if (thisChar.equals(xmlChar)) {
                    notMatched = false;
                    lastMatchedPosition = xmlCounter;
                } else if (isSpecial) {
                    notMatched = false;
                    lastMatchedPosition = xmlCounter + (entityLength - 1);
                }
                xmlCounter++;
            }
            // If still NO MATCHED --> Keep loop BOTH until their FIRST match
            if (notMatched) {
                xmlCounter = lastMatchedPosition + 1;
            }
        }
        xmlAnchorOffset = xmlCounter;
        xmlBepOffset = String.valueOf(xmlAnchorOffset);

        return xmlBepOffset;
    }
    
    @SuppressWarnings("finally")
	public static String parseXmlText(String input) {
    	StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE article SYSTEM \"article.dtd\"><article>");
    	sb.append(input);
    	sb.append("</article>");
    	String out = input;
        InputStreamReader inputStreamReader = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes());
            inputStreamReader = new InputStreamReader(bais, "UTF-8");
            InputSource inputSource = new InputSource(inputStreamReader);

            // Parse the XML File
            DOMParser parser = new DOMParser();
            parser.parse(inputSource);
            Document tempDoc = parser.getDocument();
//            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//            DocumentBuilder db = dbf.newDocumentBuilder();
//            Document tempDoc = db.parse(inputSource);
            
            out = getNodeText(tempDoc, "article");
            String content = "<html><body>" + out + "</body></html>";
            dummyPane.setText(content);
            out = dummyPane.getDocument().getText(0, dummyPane.getDocument().getLength());

        } catch (SAXException ex) {
        	ex.printStackTrace();
        } catch (IOException ex) {
        	ex.printStackTrace();
        } finally {
        	return out;
        }
    }
    // </editor-fold>
    // =========================================================================
    // =========================================================================
    // <editor-fold defaultstate="collapsed" desc="Convert XML to TXT">

    public static String ConvertXMLtoTXT(String inname, String outname) {
//        String myPureTxt = convertXMLFileToTxt(inname, outname, isWikipedia);
    	String myPureTxt = "";
//		try {
			myPureTxt = crosslink.XML2TXT.getInstance().toText(inname); //new String(crosslink.XML2TXT.getInstance().convertFile(inname), "UTF-8");
//		} catch (UnsupportedEncodingException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
        // write the text to a new file
        try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
			        new FileOutputStream(outname), "UTF-8"));
			
			out.write(myPureTxt);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return myPureTxt;
    }
    static Document doc = null;

    private String convertXMLFileToTxt(String xmlfilename, String textfilename, boolean isWikipedia) {
        String myPureTxt = "";
        try {
            // Set up a DOM tree to support node-text retrieval.
            doc = LoadDocument(xmlfilename);

            //get the text of the document from the XMLfile Entry
//            if (this.isTopicWikipedia) {
            if (isWikipedia) {
                myPureTxt = getNodeText(doc, "article");
            } else {
                myPureTxt = getNodeText(doc, "Entry");
                if (myPureTxt.equals("")) {
                    myPureTxt = getNodeText(doc, "SubEntryResources");
                }
            }

            // write the text to a new file
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
			        new FileOutputStream(textfilename), "UTF-8")); //new BufferedWriter(new FileWriter(textfilename));
            out.write(myPureTxt);
            out.close();

        } catch (Exception e) {
            System.out.println("Error with File " + xmlfilename);
            e.printStackTrace();
        }
        return myPureTxt;
    }

    /** gets the text for a given xpath
     * @param xpath - the xpath
     * @return - the text of the xpath
     */
    private static String getNodeText(Document sourceDoc, String xpath) {
        NodeList nodelist;
        Element elem;
        StringBuffer text = new StringBuffer();

//        Node root = sourceDoc.getDocumentElement(); //sourceDoc.getFirstChild();
        try {
//            nodelist = root.getChildNodes(); 
            nodelist = XPathAPI.selectNodeList(sourceDoc, xpath);

            // Process the elements in the nodelist
            // note that because we usually specify a particular node we get the text of one node only
            // and so the loop is not executed more than once.  But we can get many nodes if we specify
            // an path like this, for instance:  "//p".  It will get all p elements text.
            for (int i = 0; i < nodelist.getLength(); i++) {
                // Get element
                org.w3c.dom.Node n = nodelist.item(i);
                text.append(n.getTextContent());

                //text += text;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    /** gets the text for a given xpath from a given start position
     * @param xpath - the xpath
     * @param start - the offset to start retrieving text
     * @return - the text of the xpath
     */
    private String getNodeText(String xpath, int start) {
        return getNodeText(doc, xpath).substring(start);
    }

    /** gets the text for a given xpath from a given start position with a given length
     * @param xpath - the xpath
     * @param start - the offset to start retrieving text
     * @param length - the length of text to retrieve
     * @return - the text of the xpath
     */
    private String getNodeText(String xpath, int start, int length) {
        String text = getNodeText(doc, xpath);
        int end = end = start + length;
        if (end > start + text.length()) {
            return text.substring(start);
        } else {
            return text.substring(start, end);
        }
    }

    public static Document LoadDocument(String xmlfilename) {
        // Load XML Document
        // must modify 
        try {
            InputSource in = new InputSource(new FileInputStream(xmlfilename));
            DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dfactory.newDocumentBuilder();
            in.setEncoding("UTF-8");
            doc = builder.parse(in);
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }
    // </editor-fold>

}
