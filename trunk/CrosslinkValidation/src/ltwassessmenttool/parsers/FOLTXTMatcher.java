package ltwassessmenttool.parsers;

import com.sun.org.apache.xpath.internal.XPathAPI;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import ltwassessmenttool.LTWAssessmentToolView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author Darren Huang
 */
public class FOLTXTMatcher {

    private final String sysPropertyKey = "isTABKey";
    private final String sysPropertyIsTopicWikiKey = "isTopicWikipedia";
    private final String sysPropertyIsLinkWikiKey = "isLinkWikipedia";
    private resourcesManager myRSCManager;
    private poolerManager myPooler;
    private Vector<String> xmlSingleCharV = new Vector<String>();
    private String wikipediaTopicFileDir = "";
    private String tempFileDir = "";
    private String fullXmlTxt = "";
    private Vector<String> entityNumExpressV;
    private Vector<String> entityExpressV;
    private String wikipediaTypeName = "";
    private String tearaTypeName = "";
    private boolean isTopicWikipedia = false;
    private boolean isLinkWikipedia = false;

    private static void log(Object text) {
        System.out.println(text);
    }

    public FOLTXTMatcher() {

        this.myRSCManager = new resourcesManager();
        this.myPooler = poolerManager.getInstance();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ltwassessmenttool.LTWAssessmentToolApp.class).getContext().getResourceMap(LTWAssessmentToolView.class);
        this.wikipediaTopicFileDir = resourceMap.getString("wikipedia.topics.folder");
        this.tempFileDir = resourceMap.getString("temp.folder");
        this.wikipediaTypeName = resourceMap.getString("collectionType.Wikipedia");
        this.tearaTypeName = resourceMap.getString("collectionType.TeAra");

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

        populateEntityV();

        getCurrFullXmlText();
    }

    // =========================================================================
    // =========================================================================
    private void populateEntityV() {
        entityNumExpressV = new Vector<String>();
        entityExpressV = new Vector<String>();

        String entityListFile = "resources\\Tool_Resources\\entityList.txt";
        File entityFile = new File(entityListFile);
        if (entityFile.isFile()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(entityListFile));
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

    private String[] screenOffsetLengthFinder(String fullScreenTxt, String fullXmlTxt, String[] thisAnchorXmlOLName) {
        // thisAnchorSet --> [0]:Offset, [1]:Length, [2]:Anchor_Name
        // myScreenPosition --> [0]:Anchor_Name, [1]:Offset, [2]:Offset+Length
        String[] myScreenPosition = new String[3];
        String aOffset = thisAnchorXmlOLName[0];
        String aLength = thisAnchorXmlOLName[1];
        String aName = thisAnchorXmlOLName[2];
        String myXmlTxt = fullXmlTxt.substring(0, (Integer.valueOf(aOffset) + Integer.valueOf(aLength)));
        String myPureXmlTxtUntilOL = myXmlTxt.replaceAll("[\\s]+", "");
        String myPureXmlOffsetTxt = fullXmlTxt.substring(0, Integer.valueOf(aOffset)).replaceAll("[\\s]+", "");
        int myPureXmlOffset = myPureXmlOffsetTxt.length() - 1;  // counting from 0
        Vector<String> pureXmlCharUntilOLV = new Vector<String>();
        for (int i = 0; i < myPureXmlTxtUntilOL.length(); i++) {
            pureXmlCharUntilOLV.add(myPureXmlTxtUntilOL.substring(i, i + 1));
        }
        Vector<String> screenTxtCharV = new Vector<String>();
        for (int j = 0; j < fullScreenTxt.length(); j++) {
            screenTxtCharV.add(fullScreenTxt.substring(j, j + 1));
        }

        int scrAnchorOffset = 0;
        int scrAnchorLength = 0;
        int lastMatchedPosition = 0;
        int scrCharCounter = 0;
        for (int i = 0; i < pureXmlCharUntilOLV.size(); i++) {
            String thisXmlChar = pureXmlCharUntilOLV.elementAt(i);
            // -------------------------------------------------------------
            boolean isEntity = false;
            boolean isSpecial = false;
            int entityLength = 0;
            if (thisXmlChar.equals("&")) {
                String myEntity = "";
                for (int j = i; j < i + 10; j++) {
                    entityLength++;
                    myEntity = myEntity + pureXmlCharUntilOLV.elementAt(j);
                    if (pureXmlCharUntilOLV.elementAt(j).equals(";")) {
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
            if (isSpecial) {
                i = i + (entityLength - 1);
                if (i - (entityLength - 1) <= myPureXmlOffset && myPureXmlOffset <= i) {
                    scrAnchorOffset = lastMatchedPosition + 1;
                }
                scrCharCounter++;
            } else {
                boolean notMatched = true;
                while (notMatched) {
                    String scrChar = screenTxtCharV.elementAt(scrCharCounter);
                    if (scrCharCounter < 10) {
//                        log("XML: " + thisXmlChar + " <-> " + "SCR: " + scrChar);
                    }
                    if (thisXmlChar.equals(scrChar)) {
                        notMatched = false;
                        lastMatchedPosition = scrCharCounter;
                        if (i == myPureXmlOffset) {
                            scrAnchorOffset = scrCharCounter;
                        }
                    }
                    scrCharCounter++;
                }
            }
        }
        scrAnchorLength = scrCharCounter - scrAnchorOffset;
        myScreenPosition[0] = String.valueOf(aName);
        myScreenPosition[1] = String.valueOf(scrAnchorOffset);
        myScreenPosition[2] = String.valueOf(scrAnchorOffset + scrAnchorLength);

        return myScreenPosition;
    }
    // =========================================================================

    private String screenBepOffsetFinder(String fullScreenTxt, String fullXmlTxt, String bepOffset) {
        // thisAnchorSet --> [0]:Offset, [1]:Length, [2]:Anchor_Name
        // myScreenPosition --> [0]:Anchor_Name, [1]:Offset, [2]:Offset+Length
        String mySCRBepOffset = "";
        String xmlBepOffset = bepOffset;
        String myXmlTxt = fullXmlTxt.substring(0, Integer.valueOf(xmlBepOffset));
        String myPureXmlTxt = myXmlTxt.replaceAll("\\s", "");
        int myPureXmlOffset = myPureXmlTxt.length();
        Vector<String> pureXmlCharV = new Vector<String>();
        for (int i = 0; i < myPureXmlTxt.length(); i++) {
            pureXmlCharV.add(myPureXmlTxt.substring(i, i + 1));
        }
        Vector<String> screenTxtCharV = new Vector<String>();
        for (int j = 0; j < fullScreenTxt.length(); j++) {
            screenTxtCharV.add(fullScreenTxt.substring(j, j + 1));
        }
        int scrBepOffset = 0;
        int lastMatchedPosition = 0;
        int scrCharCounter = 0;
        for (int i = 0; i < pureXmlCharV.size(); i++) {
            String thisXmlChar = pureXmlCharV.elementAt(i);
            // -------------------------------------------------------------
            boolean isEntity = false;
            boolean isSpecial = false;
            int entityLength = 0;
            if (thisXmlChar.equals("&")) {
                String myEntity = "";
                for (int j = i; j < i + 10; j++) {
                    entityLength++;
                    myEntity = myEntity + pureXmlCharV.elementAt(j);
                    if (pureXmlCharV.elementAt(j).equals(";")) {
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
            if (isSpecial) {
                i = i + (entityLength - 1);
                if (i - (entityLength - 1) <= myPureXmlOffset && myPureXmlOffset <= i) {
                    scrBepOffset = lastMatchedPosition + 1;
                }
                scrCharCounter++;
            } else {
                boolean notMatched = true;
                while (notMatched) {
                    String scrChar = screenTxtCharV.elementAt(scrCharCounter);
                    if (thisXmlChar.equals(scrChar)) {
                        notMatched = false;
                        lastMatchedPosition = scrCharCounter;
                    }
                    scrCharCounter++;
                }
            }
        }
        scrBepOffset = scrCharCounter;
        mySCRBepOffset = String.valueOf(scrBepOffset);
        return mySCRBepOffset;
    }
    // =========================================================================

    private void getCurrFullXmlText() {
        this.isTopicWikipedia = Boolean.valueOf(System.getProperty(sysPropertyIsTopicWikiKey));
        String xmlFilePath = getCurrentTopicFilePath();
        String targetFilePath = "";
        if (this.isTopicWikipedia) {
            if (xmlFilePath.lastIndexOf(File.separator) >= 0){
                targetFilePath = this.tempFileDir + xmlFilePath.substring(xmlFilePath.lastIndexOf(File.separator), xmlFilePath.lastIndexOf(".xml")) + "_pureTxt.txt";
            } else if (xmlFilePath.lastIndexOf(File.separator) >= 0) {
                targetFilePath = this.tempFileDir + xmlFilePath.substring(xmlFilePath.lastIndexOf(File.separator), xmlFilePath.lastIndexOf(".xml")) + "_pureTxt.txt";
            }
        } else {
            if (xmlFilePath.lastIndexOf(File.separator) >= 0){
                targetFilePath = this.tempFileDir + xmlFilePath.substring(xmlFilePath.lastIndexOf(File.separator), xmlFilePath.lastIndexOf(".xml")) + "_pureTxt.txt";
            } else if (xmlFilePath.lastIndexOf(File.separator) >= 0) {
                targetFilePath = this.tempFileDir + xmlFilePath.substring(xmlFilePath.lastIndexOf(File.separator), xmlFilePath.lastIndexOf(".xml")) + "_pureTxt.txt";
            }
        }
        String wikipediaTxt = ConvertXMLtoTXT(xmlFilePath, targetFilePath, this.isTopicWikipedia);
        fullXmlTxt = wikipediaTxt;
        for (int i = 0; i < wikipediaTxt.length(); i++) {
            String mySingle = wikipediaTxt.substring(i, i + 1);
            xmlSingleCharV.add(mySingle);
        }
    }

    private String getFullXmlTextByFileID(String fileID) {
        this.isTopicWikipedia = Boolean.valueOf(System.getProperty(sysPropertyIsTopicWikiKey));
        String xmlFilePath = myPooler.getXmlFilePathByTargetID(fileID);
        String targetFilePath = tempFileDir + fileID + "_pureTxt.txt";
        String wikipediaTxt = ConvertXMLtoTXT(xmlFilePath, targetFilePath, this.isTopicWikipedia);
        return wikipediaTxt;
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
        String topicXmlPath = "";
        return topicXmlPath = myRSCManager.getCurrTopicXmlFile();
    }

    public Vector<String[]> getSCRAnchorPosV(JTextPane textPane, String currTopicID, Hashtable<String, Vector<String[]>> topicAnchorsHT) {
        getCurrFullXmlText();
        // "outgoing : " + thisTopicFile, anchorsVbyTopic
        // Offset : Length : Anchor_Name : scrOffset : scrLength
        // record into toolResource.xml
        Vector<String> anchorSetV = new Vector<String>();
        // anchorsVbyTopic --> [0]:Offset, [1]:Length, [3]:Anchor_Name
        Vector<String[]> anchorOLV = new Vector<String[]>();
        Enumeration keyEnu = topicAnchorsHT.keys();
        while (keyEnu.hasMoreElements()) {
            Object keyObj = keyEnu.nextElement();
            String[] keySet = keyObj.toString().split(" : ");
            String thisTopicID = keySet[1];
            if (currTopicID.equals(thisTopicID)) {
                anchorOLV = topicAnchorsHT.get(keyObj);
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
        for (String[] thisAnchorSet : anchorOLV) {
            String[] scrFOL = screenOffsetLengthFinder(fullScreenText, fullXmlTxt, thisAnchorSet);
            screenAnchorPos.add(scrFOL);
            anchorSetV.add(thisAnchorSet[0] + " : " + thisAnchorSet[1] + " : " + thisAnchorSet[2] + " : " + scrFOL[1] + " : " + scrFOL[2]);
        }
        // ============================
        // record into toolResource.xml
        myRSCManager.updateCurrAnchorFOL(anchorSetV);
        // ============================
        return screenAnchorPos;
    }

    public String[] getSCRAnchorPosSA(JTextPane myTextPane, String fileID, String[] thisAnchorXmlOLName) {
        // myScreenAnchorOL/thisAnchorSet
        // --> [0]:Offset, [1]:Length, [2]:Anchor_Name
        String[] myScreenAnchorPos = new String[3];
        String myFullXmlTxt = getFullXmlTextByFileID(fileID);
        String fullScreenText = "";
        try {
            fullScreenText = myTextPane.getDocument().getText(0, myTextPane.getDocument().getLength());
        } catch (BadLocationException ex) {
            Logger.getLogger(FOLTXTMatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        return myScreenAnchorPos = screenOffsetLengthFinder(fullScreenText, myFullXmlTxt, thisAnchorXmlOLName);
    }

    public String getScreenBepOffset(JTextPane linkTxtPane, String bepFileID, String xmlBepOffset, boolean isWikipedia) {
        String scrBepOffset = "";
        String fullScreenText = "";
        String fullXmlText = "";
        try {
            fullScreenText = linkTxtPane.getDocument().getText(0, linkTxtPane.getDocument().getLength());
            String bepFilePath = "";
            if (isWikipedia) {
                String subPath = myRSCManager.getWikipediaFilePathByName(bepFileID + ".xml");
                if (subPath.equals("FileNotFound.xml")) {
                    bepFilePath = "resources\\Tool_Resources\\" + subPath;
                } else {
                    bepFilePath = myRSCManager.getWikipediaCollectionFolder() + subPath;
                }
//                bepFilePath = myRSCManager.getWikipediaCollectionFolder() + myRSCManager.getWikipediaFilePathByName(bepFileID + ".xml");
            } else {
                String subPath = myRSCManager.getTeAraFilePathByName(bepFileID + ".xml");
                if (subPath.equals("FileNotFound.xml")) {
                    bepFilePath = "resources\\Tool_Resources\\" + subPath;
                } else {
                    bepFilePath = myRSCManager.getTeAraCollectionFolder() + subPath;
                }
//                bepFilePath = myRSCManager.getTeAraCollectionFolder() + myRSCManager.getTeAraFilePathByName(bepFileID + ".xml");
            }
            String targetFilePath = "resources\\Temp\\" + bepFileID + "_pureTxt.txt";
            fullXmlText = ConvertXMLtoTXT(bepFilePath, targetFilePath, isWikipedia);
        } catch (BadLocationException ex) {
            Logger.getLogger(FOLTXTMatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        return scrBepOffset = screenBepOffsetFinder(fullScreenText, fullXmlText, xmlBepOffset);
    }

    public Vector<String[]> getSCRBepPosV(JTextPane txtPane, String xmlFileID, Vector<String[]> xmlBepOffsetV, boolean isWikipedia) {
        Vector<String[]> scrBepOffsetV = new Vector<String[]>();
        Vector<String> bepSetV = new Vector<String>();
        String fullScreenText = "";
        String fullXmlText = "";
        try {
            fullScreenText = txtPane.getDocument().getText(0, txtPane.getDocument().getLength());
            String xmlFilePath = "";
            if (isWikipedia) {
                xmlFilePath = wikipediaTopicFileDir + xmlFileID + ".xml";
            } else {
                xmlFilePath = myRSCManager.getTeAraCollectionFolder() + myRSCManager.getTeAraFilePathByName(xmlFileID + ".xml");
            }
            String targetFilePath = "resources\\Topics\\" + xmlFileID + "_pureTxt.txt";
            fullXmlText = ConvertXMLtoTXT(xmlFilePath, targetFilePath, isWikipedia);
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
            String scrBepOffset = screenBepOffsetFinder(fullScreenText, fullXmlText, thisXmlBepOffset);
            scrBepOffsetV.add(new String[]{scrBepOffset, String.valueOf(Integer.valueOf(scrBepOffset) + 4)});
            bepSetV.add(thisXmlBepOffset + " : " + scrBepOffset);
        }
        // ============================
        // record into toolResource.xml
        myRSCManager.updateCurrBepOffsetList(bepSetV);
        // ============================
        return scrBepOffsetV;
    }

    public String getXmlBepOffset(JTextPane linkTxtPane, String currTopicID, String scrBepOffset) {
        String xmlBepOffset = "";

        return xmlBepOffset;
    }
    // </editor-fold>
    // =========================================================================
    // =========================================================================
    // <editor-fold defaultstate="collapsed" desc="Convert XML to TXT">

    public String ConvertXMLtoTXT(String inname, String outname, boolean isWikipedia) {
        String myPureTxt = convertXMLFileToTxt(inname, outname, isWikipedia);
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
                myPureTxt = getNodeText("article");
            } else {
                myPureTxt = getNodeText("Entry");
                if (myPureTxt.equals("")) {
                    myPureTxt = getNodeText("SubEntryResources");
                }
            }

            // write the text to a new file
            BufferedWriter out = new BufferedWriter(new FileWriter(textfilename));
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
    private String getNodeText(String xpath) {

        NodeList nodelist;
        Element elem;
        String text = "";

        try {
            nodelist = XPathAPI.selectNodeList(doc, xpath);

            // Process the elements in the nodelist
            // note that because we usually specify a particular node we get the text of one node only
            // and so the loop is not executed more than once.  But we can get many nodes if we specify
            // an path like this, for instance:  "//p".  It will get all p elements text.
            for (int i = 0; i < nodelist.getLength(); i++) {
                // Get element
                org.w3c.dom.Node n = nodelist.item(i);
                text = n.getTextContent();

                text += text;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }

    /** gets the text for a given xpath from a given start position
     * @param xpath - the xpath
     * @param start - the offset to start retrieving text
     * @return - the text of the xpath
     */
    private String getNodeText(String xpath, int start) {
        return getNodeText(xpath).substring(start);
    }

    /** gets the text for a given xpath from a given start position with a given length
     * @param xpath - the xpath
     * @param start - the offset to start retrieving text
     * @param length - the length of text to retrieve
     * @return - the text of the xpath
     */
    private String getNodeText(String xpath, int start, int length) {
        String text = getNodeText(xpath);
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
