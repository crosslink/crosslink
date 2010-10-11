
import com.sun.org.apache.xpath.internal.XPathAPI;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author DHuang
 */
public class poolingMen {

    static void log(Object obj) {
        System.out.println(obj.toString());
    }
    private static String poolXMLPath = "resources\\Pool\\wikipedia_pool_TEST.xml";
    private static int option = 4;

    public static void main(String[] args) {

        switch (option) {
            case 0:
                // convert XML to Text
                String xmlDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS\\LTW2009_Topics";
                parseXmlToTxt(xmlDir);
                break;
            case 1:
                String txtFolder = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS\\LTW2009_Topics";
                String linkFolder = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS\\anchorLinkPairPerTopic";
                String kellyDir = "C:\\PHD\\INEX_2009\\LTW2009_SUBMISSION\\Wikipedia_F2FonA2B\\UWaterloo_links";
                f2fToA2BMaker(txtFolder, linkFolder, kellyDir);
                break;
            case 2:
                String txtFolderX = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS\\LTW2009_Topics";
                String linkFolderX = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS\\anchorLinkPairPerTopic";
                // Anchor TXT --> File
                poolWikiGroundTruth(txtFolderX, linkFolderX);
                break;
            case 3:
                String topicXMLFileFolder = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS\\LTW2009_Topics\\XML";
                // Anchor TXT --> File
                indexTopicAnchors(topicXMLFileFolder);
                break;
            case 4:
                String linkFolderT = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS\\anchorLinkPairPerTopic";
                String topicAnchorIndexFolder = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS\\LTW2009_Topics\\TopicAnchors_Index";
                // Anchor TXT --> File
                anchorFinder(linkFolderT, topicAnchorIndexFolder);
                break;
            default:
                log("This is the default option...");
        }

    }

    // <editor-fold defaultstate="collapsed" desc="anchor Finder">
    private static void anchorFinder(String linkFolderT, String topicAnchorIndexFolder) {

        Hashtable<String, Vector<String[]>> allAtoBLinksHT = null;
        Vector<String[]> allA2BLinksOLTxtIDVSA = null;
        Vector<String> topicTitleV = null;
        Vector<String[]> anchorLinkPairsVSA = null;
        Vector<String> anchorTxtCheckerV = null;
        File linkDir = new File(linkFolderT);
        if (linkDir.isDirectory()) {
            allAtoBLinksHT = new Hashtable<String, Vector<String[]>>();
            File[] linkFileList = linkDir.listFiles();
            for (File thisLinkFile : linkFileList) {
                String thisLinkFName = thisLinkFile.getName();
                String thisTopicID = "";
                if (thisLinkFName.endsWith(".xml.txt")) {
                    try {
                        // <editor-fold defaultstate="collapsed" desc="Get Ground Truth Anchor TXT & Link ID">
                        thisTopicID = thisLinkFName.substring(0, thisLinkFName.lastIndexOf(".xml.txt"));
                        topicTitleV = new Vector<String>();
                        anchorLinkPairsVSA = new Vector<String[]>();
                        anchorTxtCheckerV = new Vector<String>();
                        FileInputStream fstream = null;
                        String thisLinkAbsPath = thisLinkFile.getAbsolutePath();
                        fstream = new FileInputStream(thisLinkAbsPath);
                        DataInputStream in = new DataInputStream(fstream);
                        BufferedReader br = new BufferedReader(new InputStreamReader(in));
                        int counter = 0;
                        String strLine;
                        String anchorTxt = "";
                        while ((strLine = br.readLine()) != null) {
                            if (counter == 0) {
                                // topic Title
                                topicTitleV.add(strLine.trim());
                            } else {
                                if (counter % 2 == 0) {
                                    // anchor link ID
                                    String linkID = strLine.trim();
                                    if (!anchorTxtCheckerV.contains(anchorTxt)) {
                                        anchorTxtCheckerV.add(anchorTxt);
                                        if (isInNumber(linkID)) {
                                            anchorLinkPairsVSA.add(new String[]{anchorTxt, linkID});
//                                            log("linkID --> " + linkID + " -- " + anchorTxt + " <-- anchorTxt");
                                        }
                                    }
                                } else {
                                    // anchor Text
                                    anchorTxt = strLine.trim();
                                }
                            }
                            counter++;
                        }
                        fstream.close();
                        // </editor-fold>
                        // =====================================================
                        // =====================================================
                        // <editor-fold defaultstate="collapsed" desc="Get Ground Truth Anchor Offset & Length">
                        allA2BLinksOLTxtIDVSA = new Vector<String[]>();
                        for (String[] thisAnchorLink : anchorLinkPairsVSA) {
                            String thisAnchorTxt = thisAnchorLink[0];
                            String thisALinkID = thisAnchorLink[1];
                            int thisLength = thisAnchorTxt.length();
                            // -------------------------------------------------
                            int thisOffset = topicAnchorTxtFinder(thisTopicID, thisAnchorTxt, topicAnchorIndexFolder);
                            if (thisOffset > -1) {
                                allA2BLinksOLTxtIDVSA.add(new String[]{String.valueOf(thisOffset), String.valueOf(thisLength), thisAnchorTxt, thisALinkID});
                            }
                        }
                        // =====================================================
                        // sort Anchor Offset
                        Vector<String[]> sortedAllA2BLinksOLTxtIDVSA = new Vector<String[]>();
                        Vector<String> abOffsetVS = new Vector<String>();
                        Hashtable<String, String[]> abOffsetHT = new Hashtable<String, String[]>();
                        for (String[] allA2BLinksOLTxtIDSA : allA2BLinksOLTxtIDVSA) {
                            String myOffset = allA2BLinksOLTxtIDSA[0];
                            if (abOffsetVS.contains(myOffset)) {
                                log("Duplicated Offset ... " + myOffset + " <-- " + allA2BLinksOLTxtIDSA[2]);
                            }
                            abOffsetVS.add(myOffset);
                            abOffsetHT.put(myOffset, allA2BLinksOLTxtIDSA);
                        }
                        Vector<String> sortedABOffsetVS = sortVectorNumbers(abOffsetVS);
                        for (String sOffset : sortedABOffsetVS) {
                            String[] a2BLinksOLTxtIDSA = abOffsetHT.get(sOffset);
                            sortedAllA2BLinksOLTxtIDVSA.add(a2BLinksOLTxtIDSA);
                        }
                        // =====================================================
                        // Produce a Sorted Offset ABLinks Vector for each Topic
                        allAtoBLinksHT.put(thisTopicID, sortedAllA2BLinksOLTxtIDVSA);
                        // </editor-fold>
                    } catch (IOException ex) {
                        Logger.getLogger(poolingMen.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            // =================================================================
            produceIndividualXmlForm(allAtoBLinksHT);
        }
    }

    private static int topicAnchorTxtFinder(String thisTopicID, String thisAnchorTxt, String topicAnchorIndexFolder) {
        int myOffset = -1;
        try {
            String topicIndexFilePath = topicAnchorIndexFolder + File.separator + thisTopicID + ".txt";
            // ---------------------------------------------------------------------
            Vector<String[]> anchorIndexPairV = new Vector<String[]>();
            FileInputStream fstream = new FileInputStream(topicIndexFilePath);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            int counter = 0;
            String thisOffsetValue = "";
            String strLine;
            while ((strLine = br.readLine()) != null) {
                counter++;
                if (counter % 2 == 0) {
                    String thisATxt = strLine.trim();
                    anchorIndexPairV.add(new String[]{thisATxt, thisOffsetValue});
                } else {
                    thisOffsetValue = strLine.trim();
                }
            }
            fstream.close();
            // -----------------------------------------------------------------
            String[] thisAnchorSet = thisAnchorTxt.split(" ");
            if (thisAnchorSet.length == 1) {
                for (String[] anchorIndexPair : anchorIndexPairV) {
                    String aTXT = anchorIndexPair[0].trim();
                    if (aTXT.equals(thisAnchorSet[0])) {
                        return myOffset = Integer.valueOf(anchorIndexPair[1]);
                    } else {
                        // check symbol: Get only Digit & Letter
                        String revisedATXT = revisedIndexTerm(aTXT);
                        if (revisedATXT.equals(thisAnchorSet[0])) {
                            return myOffset = Integer.valueOf(anchorIndexPair[1]);
                        }
                        // -----------------------------------------------------
                        if ((thisAnchorSet[0] + "s").equals(aTXT)) {
                            return myOffset = Integer.valueOf(anchorIndexPair[1]);
                        } else if ((thisAnchorSet[0] + "s,").equals(aTXT)) {
                            return myOffset = Integer.valueOf(anchorIndexPair[1]);
                        }
                    }
                    if (aTXT.endsWith(".") || aTXT.endsWith(",") || aTXT.endsWith("?")) {
                        String revisedATXT = aTXT.substring(0, aTXT.length() - 1);
                        if (revisedATXT.equals(thisAnchorSet[0])) {
                            return myOffset = Integer.valueOf(anchorIndexPair[1]);
                        }
                    }
                }
            } else if (thisAnchorSet.length > 1) {
                int setNum = thisAnchorSet.length;
                for (int i = 0; i < anchorIndexPairV.size(); i++) {
                    String[] anchorIndexPair = anchorIndexPairV.get(i);
                    String aIndexTXT = anchorIndexPair[0].trim();
                    if (aIndexTXT.equals(thisAnchorSet[0])) {
                        int sCounter = 0;
                        myOffset = Integer.valueOf(anchorIndexPair[1].trim());
                        boolean rightFlag = true;
                        for (int j = i + 1; j < i + setNum; j++) {
                            sCounter++;
                            String[] myAIndexPair = anchorIndexPairV.get(j);
                            String myAIndexTxt = myAIndexPair[0];

                            if (!myAIndexTxt.equals(thisAnchorSet[sCounter])) {
                                if (myAIndexTxt.endsWith(",") || myAIndexTxt.endsWith(".") || myAIndexTxt.endsWith("s")) {
                                    myAIndexTxt = myAIndexTxt.substring(0, myAIndexTxt.length() - 1);
                                    if (!myAIndexTxt.equals(thisAnchorSet[sCounter])) {
                                        rightFlag = false;
//                                        log(myOffset + " - " + thisAnchorTxt + " ; " + myAIndexTxt + " <-> " + thisAnchorSet[sCounter]);
//                                        log("===========" + rightFlag + "==============" + " & " + thisAnchorSet[sCounter]);
                                        break;
                                    }
                                } else {
                                    rightFlag = false;
//                                    log(myOffset + " - " + thisAnchorTxt + " ; " + myAIndexTxt + " <-> " + thisAnchorSet[sCounter]);
//                                    log("===========" + rightFlag + "==============" + " & " + thisAnchorSet[sCounter]);
                                    break;
                                }
                            }
//                            log(myOffset + " - " + thisAnchorTxt + " ; " + myAIndexTxt + " <-> " + thisAnchorSet[sCounter]);
                        }
                        if (rightFlag) {
//                            log("RETURN --> " + myOffset);
                            return myOffset;
                        }
                    }
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(poolingMen.class.getName()).log(Level.SEVERE, null, ex);
        }
        // -----------------------------------------------------------------
        log(thisTopicID + "*********** NOT FOUND ************ " + thisAnchorTxt);
        return myOffset;
    }

    private static String revisedIndexTerm(String anchorTXT) {
        String revisedATXT = "";
        for (int i = 0; i < anchorTXT.length(); i++) {
            String mySingleChar = anchorTXT.substring(i, i + 1);
            if (Character.isLetterOrDigit(mySingleChar.toCharArray()[0])) {
                revisedATXT += mySingleChar;
            }
        }
        return revisedATXT;
    }

    private static void produceIndividualXmlForm(Hashtable<String, Vector<String[]>> allLinksHT) {
//        <anchor arel="-1" aname="Power Macintosh family" aoffset="10855" alength="22">
//          <subanchor sarel="" saname="Power Macintosh family" saoffset="10855" salength="22">
//             <tobep tbrel="" timein="" timeout="" tboffset="-1" tbstartp="-1">24886</tobep>
//          </subanchor>
//        </anchor>
        String indivPoolDir = "C:\\PHD\\INEX_2009\\POOL_2009\\POOL_GROUNDTRUTH\\";
        Enumeration idKeyEnu = allLinksHT.keys();
        while (idKeyEnu.hasMoreElements()) {
            FileWriter fw = null;
            try {
                Object idKey = idKeyEnu.nextElement();
                log("========================================================");
                log("TOPIC ID: " + idKey.toString());
                String titleXmlFPath = indivPoolDir + idKey.toString() + "_GTPool.xml";
                fw = new FileWriter(titleXmlFPath);
                BufferedWriter bw = new BufferedWriter(fw);
                Vector<String[]> myPPT = allLinksHT.get(idKey.toString());
                for (String[] thisAnchorSA : myPPT) {
                    String myOffset = thisAnchorSA[0];
                    String myLength = thisAnchorSA[1];
                    String myAnchorTXT = thisAnchorSA[2];
                    String myLinkID = thisAnchorSA[3];

                    bw.write("<anchor arel=\"0\" aname=\"" + myAnchorTXT + "\" aoffset=\"" + myOffset + "\" alength=\"" + myLength + "\">");
                    bw.newLine();
                    bw.write("<subanchor sarel=\"0\" saname=\"" + myAnchorTXT + "\" saoffset=\"" + myOffset + "\" salength=\"" + myLength + "\">");
                    bw.newLine();
                    bw.write("<tobep tbrel=\"0\" timein=\"\" timeout=\"\" tboffset=\"-1\" tbstartp=\"-1\">" + myLinkID + "</tobep>");
                    bw.newLine();
                    bw.write("</subanchor>");
                    bw.newLine();
                    bw.write("</anchor>");
                    bw.newLine();
                }
                bw.close();
                fw.close();
            } catch (IOException ex) {
//                return;
                Logger.getLogger(poolingMen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Topic Anchor Indexing">
    private static void indexTopicAnchors(String topicXMLFileFolder) {
        Vector<Character> txtSglCharV = new Vector<Character>();
        Vector<String[]> topicAnchorsIndexV = new Vector<String[]>();

        File topicTxtFolder = new File(topicXMLFileFolder);
        if (topicTxtFolder.isDirectory()) {
            File[] topicTxtFiles = topicTxtFolder.listFiles();
            for (File thisTopicTxt : topicTxtFiles) {
                String thisTopicFileFName = thisTopicTxt.getName();
                if (thisTopicFileFName.endsWith(".xml")) {
                    String thisTopicID = thisTopicTxt.getName().substring(0, thisTopicTxt.getName().lastIndexOf(".xml"));
                    try {
                        txtSglCharV = new Vector<Character>();
                        String thisTopicTXT = convertXMLFileToTxt(thisTopicTxt.getAbsolutePath(), "", true);
                        for (int i = 0; i < thisTopicTXT.length(); i++) {
                            String mySingle = thisTopicTXT.substring(i, i + 1);
                            txtSglCharV.add(mySingle.toCharArray()[0]);
                        }
                        // -----------------------------------------------------
                        topicAnchorsIndexV = new Vector<String[]>();
                        int thisOffset = -1;
                        String thisSingleAnchor = "";
                        boolean charFlag = false;
                        for (int i = 0; i < txtSglCharV.size(); i++) {
                            char txtChar = txtSglCharV.elementAt(i);
                            if (Character.isSpaceChar(txtChar) || Character.isWhitespace(txtChar)) {
                                if (charFlag) {
                                    topicAnchorsIndexV.add(new String[]{String.valueOf(thisOffset), thisSingleAnchor});
                                    // -----------------------------------------
                                    thisOffset = -1;
                                    thisSingleAnchor = "";
                                    charFlag = false;
                                }
                            } else {
                                if (thisOffset == -1) {
                                    thisOffset = i;
                                }
                                thisSingleAnchor += txtChar;
                                charFlag = true;
                            }
                        }
                        // -----------------------------------------------------
                        String indexResultFolder = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS\\LTW2009_Topics\\TopicAnchors_Index\\";
                        String indexResultPath = indexResultFolder + thisTopicID + ".txt";
                        FileWriter xlsFile = null;
                        xlsFile = new FileWriter(indexResultPath);
                        for (String[] topicAnchorOffsetTXT : topicAnchorsIndexV) {
                            xlsFile.write(topicAnchorOffsetTXT[0].trim());
                            xlsFile.write("\n");
                            xlsFile.write(topicAnchorOffsetTXT[1].trim());
                            xlsFile.write("\n");
                        }
                        xlsFile.close();
                    } catch (IOException ex) {
                        Logger.getLogger(poolingMen.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="A to File Maker">
    private static void poolWikiGroundTruth(String txtFolderX, String linkFolderX) {
        Hashtable<String, String[]> allFtoFLinksHT = null;
        Hashtable<String, Vector<String[]>> allAtoBLinksHT = null;
        Vector<String[]> allA2BLinksOLTxtIDVSA = null;
        Vector<String> topicTitleV = null;
        Vector<String[]> anchorLinkPairsVSA = null;
        Vector<String> anchorTxtCheckerV = null;
        File linkDir = new File(linkFolderX);
        if (linkDir.isDirectory()) {
            File[] linkFileList = linkDir.listFiles();
            for (File thisLinkFile : linkFileList) {
                String thisLinkFName = thisLinkFile.getName();
                String thisTopicID = "";
                if (thisLinkFName.endsWith(".xml.txt")) {
                    try {
                        thisTopicID = thisLinkFName.substring(0, thisLinkFName.lastIndexOf(".xml.txt"));
                        allFtoFLinksHT = new Hashtable<String, String[]>();
                        allAtoBLinksHT = new Hashtable<String, Vector<String[]>>();
                        topicTitleV = new Vector<String>();
                        anchorLinkPairsVSA = new Vector<String[]>();
                        anchorTxtCheckerV = new Vector<String>();
                        FileInputStream fstream = null;
                        String thisLinkAbsPath = thisLinkFile.getAbsolutePath();
                        fstream = new FileInputStream(thisLinkAbsPath);
                        DataInputStream in = new DataInputStream(fstream);
                        BufferedReader br = new BufferedReader(new InputStreamReader(in));
                        int counter = 0;
                        String strLine;
                        String anchorTxt = "";
                        while ((strLine = br.readLine()) != null) {
                            if (counter == 0) {
                                // topic Title
                                topicTitleV.add(strLine.trim());
                            } else {
                                if (counter % 2 == 0) {
                                    // anchor link ID
                                    String linkID = strLine.trim();
                                    if (!anchorTxtCheckerV.contains(anchorTxt)) {
//                                        log("Anchor Text: " + anchorTxt);
                                        anchorTxtCheckerV.add(anchorTxt);
                                        if (isInNumber(linkID)) {
                                            anchorLinkPairsVSA.add(new String[]{anchorTxt, linkID});
                                            log("linkID --> " + linkID + " -- " + anchorTxt + " <-- anchorTxt");
                                        }
                                    }
                                } else {
                                    // anchor Text
                                    anchorTxt = strLine.trim();
                                }
                            }
                            counter++;
                        }
                        fstream.close();
                        // -----------------------------------------------------
                        // -----------------------------------------------------
                        String topicTXTFileFPath = txtFolderX + File.separator + thisTopicID + ".txt";
                        File txtFile = new File(topicTXTFileFPath);
                        FileInputStream txtFs = new FileInputStream(txtFile.getAbsoluteFile());
                        DataInputStream txtIn = new DataInputStream(txtFs);
                        BufferedReader txtBr = new BufferedReader(new InputStreamReader(txtIn));
                        StringBuffer sb = new StringBuffer();
                        String thisLine = "";
                        while ((thisLine = txtBr.readLine()) != null) {
                            sb.append(thisLine);
                        }
                        // =====================================================
                        // =====================================================
                        // <editor-fold defaultstate="collapsed" desc="For Kelly's F2FonA2B">
//                        for (String thistitle : topicTitleV) {
//                            int thisLength = thistitle.length();
//                            String xmlFolder = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS\\LTW2009_Topics\\XML\\";
//                            String linkFullPath = xmlFolder + thisLinkID + ".xml";
//                            int thisOffset = txtOffsetFinder(linkFullPath, thisLinkID, thistitle);
//                            if (thisOffset == -1) {
//                                log("ERROR OFFSET: " + thistitle);
//                            } else {
//                                log(thisOffset + " - " + thisLength + " - " + thistitle);
//                                String thisATXT = sb.toString().substring(thisOffset, thisOffset + thisLength);
//
//                                String outgoingLinks = kellyFolder + File.separator + thisLinkID + "_outgoing.txt";
//                                String incomingLinks = kellyFolder + File.separator + thisLinkID + "_incoming.txt";
//                                String kellyOutgoing = getKellyLinks(outgoingLinks);
//                                String kellyIncoming = getKellyLinks(incomingLinks);
//                                allFtoFLinksHT.put(thisLinkID, new String[]{String.valueOf(thisOffset), String.valueOf(thisLength), thistitle, kellyOutgoing, kellyIncoming});
//                            }
//                        }
                        // </editor-fold>

                        allA2BLinksOLTxtIDVSA = new Vector<String[]>();
                        for (String[] thisAnchorLink : anchorLinkPairsVSA) {
                            String thisAnchorTxt = thisAnchorLink[0];
                            String thisALinkID = thisAnchorLink[1];
                            int thisLength = thisAnchorTxt.length();

                            String xmlFolder = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS\\LTW2009_Topics\\XML\\";
                            String linkFullPath = xmlFolder + thisTopicID + ".xml";
                            int thisOffset = txtOffsetFinder(linkFullPath, thisTopicID, thisAnchorTxt);
                            if (thisOffset == -1) {
                                log("ERROR OFFSET: " + thisAnchorTxt);
                            } else {
                                log(thisOffset + " - " + thisLength + " - " + thisAnchorTxt);
                                allA2BLinksOLTxtIDVSA.add(new String[]{String.valueOf(thisOffset), String.valueOf(thisLength), thisAnchorTxt, thisALinkID});
                            }
                        }
                        // =====================================================
                        // sort Anchor Offset
                        Vector<String[]> sortedAllA2BLinksOLTxtIDVSA = new Vector<String[]>();
                        Vector<String> abOffsetVS = new Vector<String>();
                        Hashtable<String, String[]> abOffsetHT = new Hashtable<String, String[]>();
                        for (String[] allA2BLinksOLTxtIDSA : allA2BLinksOLTxtIDVSA) {
                            String myOffset = allA2BLinksOLTxtIDSA[0];
                            if (abOffsetVS.contains(myOffset)) {
                                log("Duplicated Offset ... " + myOffset);
                            }
                            abOffsetVS.add(myOffset);
                            abOffsetHT.put(myOffset, allA2BLinksOLTxtIDSA);
                        }
                        Vector<String> sortedABOffsetVS = sortVectorNumbers(abOffsetVS);
                        for (String sOffset : sortedABOffsetVS) {
                            String[] a2BLinksOLTxtIDSA = abOffsetHT.get(sOffset);
                            sortedAllA2BLinksOLTxtIDVSA.add(a2BLinksOLTxtIDSA);
                        }
                        // =====================================================
                        allAtoBLinksHT.put(thisTopicID, sortedAllA2BLinksOLTxtIDVSA);

                    } catch (IOException ex) {
                        Logger.getLogger(poolingMen.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            // -----------------------------------------------------
            // -----------------------------------------------------
            embededToPool(allAtoBLinksHT);
        }
    }

    private static void embededToPool(Hashtable<String, Vector<String[]>> allAtoBLinksHT) {
        // <editor-fold defaultstate="collapsed" desc="PART I - Produce individual XML for each Topic">
        createA2FXmlForm(allAtoBLinksHT);
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="PART II - Embeded into respective Pool">

        // </editor-fold>
    }

    private static void createA2FXmlForm(Hashtable<String, Vector<String[]>> allAtoBLinksHT) {
//        <anchor arel="-1" aname="Power Macintosh family" aoffset="10855" alength="22">
//          <subanchor sarel="" saname="Power Macintosh family" saoffset="10855" salength="22">
//             <tobep tbrel="" timein="" timeout="" tboffset="-1" tbstartp="-1">24886</tobep>
//          </subanchor>
//        </anchor>
        String gtXmlDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS\\anchorLinkPairPerTopic\\XmlForm\\";
        Enumeration idKeyEnu = allAtoBLinksHT.keys();
        while (idKeyEnu.hasMoreElements()) {
            FileWriter fw = null;
            try {
                Object idKey = idKeyEnu.nextElement();
                log("========================================================");
                log("TOPIC ID: " + idKey.toString());
                String titleXmlFPath = gtXmlDir + idKey.toString() + ".xml";
                fw = new FileWriter(titleXmlFPath);
                BufferedWriter bw = new BufferedWriter(fw);

                Vector<String[]> myPPT = allAtoBLinksHT.get(idKey.toString());
                for (String[] oltxtidSA : myPPT) {

                    String myOffset = oltxtidSA[0];
                    String myLength = oltxtidSA[1];
                    String myATxt = oltxtidSA[2];
                    String myID = oltxtidSA[3];

                    bw.write("<anchor arel=\"-1\" aname=\"" + myATxt + "\" aoffset=\"" + myOffset + "\" alength=\"" + myLength + "\">");
                    bw.newLine();
                    bw.write("<subanchor sarel=\"-1\" saname=\"" + myATxt + "\" saoffset=\"" + myOffset + "\" salength=\"" + myLength + "\">");
                    bw.newLine();
                    bw.write("<tobep tbrel=\"-1\" timein=\"\" timeout=\"\" tboffset=\"-1\" tbstartp=\"-1\">" + myID + "</tobep>");
                    bw.newLine();
                    bw.write("</subanchor>");
                    bw.newLine();
                    bw.write("</anchor>");
                }

            } catch (IOException ex) {
                return;
//                Logger.getLogger(poolingMen.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    fw.close();
                } catch (IOException ex) {
                    return;
//                    Logger.getLogger(poolingMen.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="f2f To A2B Maker">
    private static void f2fToA2BMaker(String txtFolder, String linkFolder, String kellyFolder) {
        Hashtable<String, String[]> allLinksHT = null;
        Vector<String> topicTitleV = null;
        Vector<String[]> anchorLinkPairsVSA = null;
        Vector<String> anchorTxtCheckerV = null;
        File linkDir = new File(linkFolder);
        if (linkDir.isDirectory()) {
            File[] linkFileList = linkDir.listFiles();
            for (File thisLinkFile : linkFileList) {
                String thisLinkFName = thisLinkFile.getName();
                String thisLinkID = "";
                if (thisLinkFName.endsWith(".xml.txt")) {
                    try {
                        thisLinkID = thisLinkFName.substring(0, thisLinkFName.lastIndexOf(".xml.txt"));
                        allLinksHT = new Hashtable<String, String[]>();
                        topicTitleV = new Vector<String>();
                        anchorLinkPairsVSA = new Vector<String[]>();
                        anchorTxtCheckerV = new Vector<String>();
                        FileInputStream fstream = null;
                        String thisLinkAbsPath = thisLinkFile.getAbsolutePath();
                        fstream = new FileInputStream(thisLinkAbsPath);
                        DataInputStream in = new DataInputStream(fstream);
                        BufferedReader br = new BufferedReader(new InputStreamReader(in));
                        int counter = 0;
                        String strLine;
                        String anchorTxt = "";
                        while ((strLine = br.readLine()) != null) {
                            if (counter == 0) {
                                // topic Title
                                topicTitleV.add(strLine.trim());
                            } else {
                                if (counter % 2 == 0) {
                                    // anchor link ID
                                    String linkID = strLine.trim();
                                    if (!anchorTxtCheckerV.contains(anchorTxt)) {
//                                        log("Anchor Text: " + anchorTxt);
                                        anchorTxtCheckerV.add(anchorTxt);
                                        if (isInNumber(linkID)) {
                                            anchorLinkPairsVSA.add(new String[]{anchorTxt, linkID});
                                        }
                                    }
                                } else {
                                    // anchor Text
                                    anchorTxt = strLine.trim();
                                }
                            }
                            counter++;
                        }
                        fstream.close();
                        // -----------------------------------------------------
                        String topicTXTFileFPath = txtFolder + File.separator + thisLinkID + ".txt";
                        File txtFile = new File(topicTXTFileFPath);
                        FileInputStream txtFs = new FileInputStream(txtFile.getAbsoluteFile());
                        DataInputStream txtIn = new DataInputStream(txtFs);
                        BufferedReader txtBr = new BufferedReader(new InputStreamReader(txtIn));
                        StringBuffer sb = new StringBuffer();
                        String thisLine = "";
                        while ((thisLine = txtBr.readLine()) != null) {
                            sb.append(thisLine);
                        }
                        // =====================================================
                        // =====================================================
                        for (String thistitle : topicTitleV) {
                            int thisLength = thistitle.length();
//                            int thisOffset = sb.toString().indexOf(thistitle);
                            String xmlFolder = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS\\LTW2009_Topics\\XML\\";
                            String linkFullPath = xmlFolder + thisLinkID + ".xml";
                            int thisOffset = txtOffsetFinder(linkFullPath, thisLinkID, thistitle);
                            if (thisOffset == -1) {
//                                log("ERROR OFFSET: " + thistitle);
                            } else {
//                                log(thisOffset + " - " + thisLength + " - " + thistitle);
                                String thisATXT = sb.toString().substring(thisOffset, thisOffset + thisLength);

                                String outgoingLinks = kellyFolder + File.separator + thisLinkID + "_outgoing.txt";
                                String incomingLinks = kellyFolder + File.separator + thisLinkID + "_incoming.txt";
                                String kellyOutgoing = getKellyLinks(outgoingLinks);
                                String kellyIncoming = getKellyLinks(incomingLinks);
                                allLinksHT.put(thisLinkID, new String[]{String.valueOf(thisOffset), String.valueOf(thisLength), thistitle, kellyOutgoing, kellyIncoming});
                            }
                        }
                        // -----------------------------------------------------
                        // -----------------------------------------------------
                        createXmlForm(allLinksHT);

                    } catch (IOException ex) {
                        Logger.getLogger(poolingMen.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    private static void createXmlForm(Hashtable<String, String[]> allLinksHT) {
//        <anchor arel="-1" aname="Power Macintosh family" aoffset="10855" alength="22">
//          <subanchor sarel="" saname="Power Macintosh family" saoffset="10855" salength="22">
//             <tobep tbrel="" timein="" timeout="" tboffset="-1" tbstartp="-1">24886</tobep>
//          </subanchor>
//        </anchor>
//        <bep borel="" boffset="0">
//           <fromanchor farel="" faoffset="9594" falength="8" faanchor="Apple III">IIc Plus</fromanchor>
//        </bep>
        String kellyDir = "C:\\PHD\\INEX_2009\\LTW2009_SUBMISSION\\Wikipedia_F2FonA2B\\UWaterloo_links\\";
        Enumeration idKeyEnu = allLinksHT.keys();
        while (idKeyEnu.hasMoreElements()) {
            FileWriter fw = null;
            try {
                Object idKey = idKeyEnu.nextElement();
                log("========================================================");
                log("TOPIC ID: " + idKey.toString());
                String titleXmlFPath = kellyDir + idKey.toString() + ".xml";
                fw = new FileWriter(titleXmlFPath);
                BufferedWriter bw = new BufferedWriter(fw);

                String[] myPPT = allLinksHT.get(idKey.toString());
                String myOffset = myPPT[0];
                String myLength = myPPT[1];
                String myTitle = myPPT[2];
                String[] myOutgoing = myPPT[3].split(" , ");
                String[] myIncoming = myPPT[4].split(" , ");

                bw.write("<anchor arel=\"-1\" aname=\"" + myTitle + "\" aoffset=\"" + myOffset + "\" alength=\"" + myLength + "\">");
                bw.newLine();
                bw.write("<subanchor sarel=\"-1\" saname=\"" + myTitle + "\" saoffset=\"" + myOffset + "\" salength=\"" + myLength + "\">");
                bw.newLine();
                for (String myOutID : myOutgoing) {
                    bw.write("<tobep tbrel=\"-1\" timein=\"\" timeout=\"\" tboffset=\"-1\" tbstartp=\"-1\">" + myOutID + "</tobep>");
                    bw.newLine();
                }
                bw.write("</subanchor>");
                bw.newLine();
                bw.write("</anchor>");
                bw.newLine();

                bw.newLine();
                bw.write("<bep borel=\"\" boffset=\"0\">");
                bw.newLine();
                for (String myInID : myIncoming) {
                    String myLinkTitle = getLinkTitle(myInID).trim();
                    log("Link Title: " + myInID + " - " + myLinkTitle);
                    String linkFullPath = getWikipediaFilePathByName(myInID + ".xml");
                    File thisLinkFile = new File(linkFullPath);
                    int linkOffset = 0;
                    if (thisLinkFile.exists() && !myLinkTitle.trim().equals("")) {
                        linkOffset = txtOffsetFinder(linkFullPath, myInID, myLinkTitle);
                    }
                    log("Link Offset: " + linkOffset);
                    bw.write("<fromanchor farel=\"-1\" faoffset=\"" + linkOffset + "\" falength=\"" + myLinkTitle.length() + "\" faanchor=\"" + myLinkTitle + "\">" + myInID + "</fromanchor>");
                    bw.newLine();
                }
                bw.write("</bep>");
                bw.newLine();

            } catch (IOException ex) {
                return;
//                Logger.getLogger(poolingMen.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    fw.close();
                } catch (IOException ex) {
                    return;
//                    Logger.getLogger(poolingMen.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Offset Finder">
    private static int txtOffsetFinder(String linkFullPath, String thisLinkID, String anchorTXT) {

        Vector<String> txtSglCharV = new Vector<String>();
//        String xmlFolder = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS\\LTW2009_Topics\\XML\\";
        String txtTempFolder = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS\\LTW2009_Topics\\XML\\TXTTEMP\\";
//        String xmlFilePath = xmlFolder + thisLinkID + ".xml";
        String txtFilePath = txtTempFolder + thisLinkID + ".txt";
//        log(linkFullPath);
//        log(txtFilePath);
//        log("anchorTXT: " + anchorTXT);
        String wikipediaTxt = ConvertXMLtoTXT(linkFullPath, txtFilePath, true);
        for (int i = 0; i < wikipediaTxt.length(); i++) {
            String mySingle = wikipediaTxt.substring(i, i + 1);
            txtSglCharV.add(mySingle);
        }
        // ---------------------------------------------------------------------
        Vector<String> titleSglCharV = new Vector<String>();
        for (int i = 0; i < anchorTXT.trim().length(); i++) {
            String mySingle = anchorTXT.trim().substring(i, i + 1);
            titleSglCharV.add(mySingle);
        }
        // ---------------------------------------------------------------------
        int myOffset = 0;
        boolean matchFlag = true;
        for (int i = 0; i < txtSglCharV.size(); i++) {
            String thisTxtChar = txtSglCharV.elementAt(i);
            if (thisTxtChar.equals(titleSglCharV.elementAt(0))) {
                myOffset = i;
                int myCounter = 0;
                for (int j = i; j < i + anchorTXT.length() - 1; j++) {
                    String myTxtChar = txtSglCharV.elementAt(j);
                    String myTitleChar = titleSglCharV.elementAt(myCounter);
                    if (myTxtChar.equals(myTitleChar)) {
                        myCounter++;
                    } else if (myTxtChar.equals("") || myTxtChar.equals(" ")) {
                        if (myTitleChar.equals("") || myTitleChar.equals(" ")) {
                            myCounter++;
                        }
                    } else if (!myTxtChar.equals("") && !myTxtChar.equals(" ")) {
                        matchFlag = false;
//                        log("BREAK...");
                        break;
                    }
                }
                if (matchFlag) {
//                    log("RETURN: " + myOffset);
                    return myOffset;
                }
            }
//            log("LOOP AGAIN...");
        }
        // ---------------------------------------------------------------------
//        log("CANNOT FIND OFFSET: " + myOffset + " *************************** ");
        return myOffset;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Wikipedia Page Title Finder">
    static StringBuffer htmlSB = null;

    private static String getLinkTitle(String myInID) {
        String thisPageTitle = "";
        htmlSB = new StringBuffer();
        String xmlPath = getWikipediaFilePathByName(myInID + ".xml");
        pageTitleExtractor(xmlPath);
        return thisPageTitle = htmlSB.toString();
    }
    private static String xmlUnicode = "utf-8";

    private static void pageTitleExtractor(String xmlPath) {
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
    private static boolean isTitleFlag = false;

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
                    return;
                }
                break;
        }
    }

    public static String getWikipediaFilePathByName(String fileName) {
        String WikipediaPathFile = "";
        String WikipediaDir = "";
        String wikipediaFilePath = getTargetFilePathByFileName(WikipediaDir, WikipediaPathFile, fileName);
        return wikipediaFilePath;
    }
    private static String wikipediaXmlDir = "E:\\PHD_Tools\\Wikipedia_2009_Collection\\XML\\";

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

    // <editor-fold defaultstate="collapsed" desc="Utilities">
    private static String getKellyLinks(String linkPath) {
        FileInputStream txtFs = null;
        String myLinks = "";
        try {
            File txtFile = new File(linkPath);
            txtFs = new FileInputStream(txtFile.getAbsoluteFile());
            DataInputStream txtIn = new DataInputStream(txtFs);
            BufferedReader txtBr = new BufferedReader(new InputStreamReader(txtIn));
            String thisLine = "";
            int counter = 0;
            while ((thisLine = txtBr.readLine()) != null) {
                if (counter == 0) {
                    myLinks = thisLine;
                } else {
                    myLinks = myLinks + " , " + thisLine;
                }
                counter++;
            }
        } catch (IOException ex) {
            Logger.getLogger(poolingMen.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                txtFs.close();
            } catch (IOException ex) {
                Logger.getLogger(poolingMen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return myLinks;
    }

    public static boolean isInNumber(String num) {
        try {
            int intI = Integer.parseInt(num);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private static void parseXmlToTxt(String xmlDir) {
        File xmlFolder = new File(xmlDir);
        if (xmlFolder.isDirectory()) {
            File[] xmlFileList = xmlFolder.listFiles();
            for (File xmlFile : xmlFileList) {
                String thisXmlName = xmlFile.getName();
                String thisXmlAbsPath = xmlFile.getAbsolutePath();
                String pureTXT = xmlDir + File.separator + thisXmlName.substring(0, thisXmlName.lastIndexOf(".xml")) + ".txt";
                String convert = ConvertXMLtoTXT(thisXmlAbsPath, pureTXT, true);
            }
        }
    }

    private static Vector<String> sortVectorNumbers(Vector<String> myNumbersV) {
        int[] thisIntA = new int[myNumbersV.size()];
        for (int i = 0; i < myNumbersV.size(); i++) {
//            log(myNumbersV.elementAt(i));
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
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Convert XML to TXT">
    public static String ConvertXMLtoTXT(String inname, String outname, boolean isWikipedia) {
        String myPureTxt = convertXMLFileToTxt(inname, outname, isWikipedia);
        return myPureTxt;
    }
    static Document doc = null;

    private static String convertXMLFileToTxt(String xmlfilename, String textfilename, boolean isWikipedia) {
        String myPureTxt = "";
        try {
            doc = LoadDocument(xmlfilename);
            if (isWikipedia) {
                myPureTxt = getNodeText("article");
            } else {
                myPureTxt = getNodeText("Entry");
                if (myPureTxt.equals("")) {
                    myPureTxt = getNodeText("SubEntryResources");
                }
            }

            // write the text to a new file
//            BufferedWriter out = new BufferedWriter(new FileWriter(textfilename));
//            out.write(myPureTxt);
//            out.close();

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
    private static String getNodeText(String xpath) {

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
    private static String getNodeText(String xpath, int start) {
        return getNodeText(xpath).substring(start);
    }

    /** gets the text for a given xpath from a given start position with a given length
     * @param xpath - the xpath
     * @param start - the offset to start retrieving text
     * @param length - the length of text to retrieve
     * @return - the text of the xpath
     */
    private static String getNodeText(String xpath, int start, int length) {
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
