
import com.ximpleware.AutoPilot;
import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.TranscodeException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
 * @author DHuang
 */
public class genResultSet {

    static void log(Object obj) {
        System.out.println(obj.toString());
    }
    private static int allowedAnchorNumber = 50;
    private static int allowedInLinkNumber = 250;
    private static int option = 4;
    private static String destDir = ".";

    public static void main(String[] args) {
    	
    	if (args.length < 1) {
    		System.out.println("Usage: program /path/to/runs_folder [/path/to/dest]");
    		System.exit(-1);
    	}
    	
    	if (args.length >= 2)
    		destDir = args[1];

//        String topicFileDir = "";
//        String topicFileFPath = "";
//        String gtDir = "";
//        String outgoingGTDir = "";
//        String incomingGTDir = "";
//        String a2bRunsRootDir = "";

        Vector<String> f2fRunDirV = new Vector<String>();

//        switch (option) {
//            case 0:
//                // convert XML to Text
//                topicFileDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\LTW2009_F2FTOPICS\\";
//                topicFileFPath = topicFileDir + "LTW2009_F2FTOPICS.txt";
//                gtDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\LTW2009_GROUND_TRUTH\\";
//                convertToF2FResultSet(topicFileFPath, gtDir);
//                break;
//            case 1:
////                f2fRunDirV.add("C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\Wikipedia_F2F\\QUT");
////                f2fRunDirV.add("C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\Wikipedia_F2F\\UKP");
////                f2fRunDirV.add("C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\Wikipedia_F2F\\WOLLONGONG");
////                f2fRunDirV.add("G:\\PHD_Tools\\LTW2009_SUBMISSION\\Wikipedia_F2FonA2B\\UWaterloo");
////                f2fRunDirV.add("C:\\PHD\\INEX_2009\\LTW2009_SUBMISSION\\Wikipedia_F2FonA2B\\QUT");
////                f2fRunDirV.add("C:\\PHD\\INEX_2009\\LTW2009_SUBMISSION\\Wikipedia_F2F\\QUT");
////                f2fRunDirV.add("C:\\PHD\\INEX_2009\\LTW2009_SUBMISSION\\Wikipedia_F2FonA2B\\Otago");
////                f2fRunDirV.add("C:\\PHD\\INEX_2009\\LTW2009_SUBMISSION\\Wikipedia_F2F\\OTAGO");
//                f2fRunDirV.add("C:\\PHD\\INEX_2009\\24TOPICS_POOL\\From_Andrew");
//                convertF2FRunToEvalForm(f2fRunDirV);
//                break;
//            case 2:
////                f2fRunDirV.add("C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\Wikipedia_F2F\\QUT");
////                f2fRunDirV.add("C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\Wikipedia_F2F\\UKP");
//                f2fRunDirV.add("C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\Wikipedia_F2F\\WOLLONGONG");
//                reformateRuns(f2fRunDirV);
//                break;
//            case 3:
//                f2fRunDirV.add("C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\Wikipedia_F2F\\QUT");
////                f2fRunDirV.add("C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\Wikipedia_F2F\\UKP");
////                f2fRunDirV.add("C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\Wikipedia_F2F\\WOLLONGONG");
//                parseF2FRunToEvalForm(f2fRunDirV);
//                break;
//            case 4:
//                f2fRunDirV.add("C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\Wikipedia_F2F\\QUT");
//                f2fRunDirV.add("C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\Wikipedia_F2F\\UKP");
//                f2fRunDirV.add("C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\Wikipedia_F2F\\WOLLONGONG");
//                f2fRunDirV.add("C:\\PHD\\INEX_2009\\LTW2009_SUBMISSION\\Wikipedia_F2F\\From_Johannes");
                //f2fRunDirV.add("C:\\PHD\\INEX_2009\\LTW2009_SUBMISSION\\Wikipedia_F2FonA2B\\FROM_Eric");
                f2fRunDirV.add(args[0]);
                readToParseF2FToEvalForm(f2fRunDirV);
//                break;
//            case 5:
//                // convert XML to Text
//                topicFileDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\LTW2009_F2FTOPICS\\";
//                topicFileFPath = topicFileDir + "LTW2009_F2FTOPICS.txt";
//                outgoingGTDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\LTW2009_GROUND_TRUTH\\LTW2009_TOPICS_F2FGT\\LTW2009_GROUND_TRUTH_OUTGOING\\";
//                incomingGTDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\LTW2009_GROUND_TRUTH\\LTW2009_TOPICS_F2FGT\\LTW2009_GROUND_TRUTH_INCOMING\\";
//                produceF2FGTResultSet(topicFileFPath, outgoingGTDir, incomingGTDir);
//                break;
//            case 6:
//                // convert XML to Text
//                topicFileDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_A2B\\LTW2009_Topics\\XML\\";
//                outgoingGTDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_A2B\\LTW2009_TOPICS_A2BGT\\A2B_OUTGOING_BACKUP\\";
//                incomingGTDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_A2B\\LTW2009_TOPICS_A2BGT\\A2B_INCOMING_BACKUP\\";
//                produceA2BGTResultSet(topicFileDir, outgoingGTDir, incomingGTDir);
//                break;
//            case 7:
//                // convert A2B Runs to Evaluation XML Form
//                a2bRunsRootDir = "C:\\PHD\\INEX_2009\\LTW2009_SUBMISSION\\Wikipedia_A2B\\";
//                Vector<String> a2bRunsDirVS = new Vector<String>();
//                a2bRunsDirVS.add(a2bRunsRootDir + "OTAGO");
//                a2bRunsDirVS.add(a2bRunsRootDir + "QUT");
//                a2bRunsDirVS.add(a2bRunsRootDir + "UAMS");
//                a2bRunsDirVS.add(a2bRunsRootDir + "UWaterloo");
//                convertA2BRunToEvalXML(a2bRunsDirVS);
//                break;
//            case 8:
//                // convert XML to Text
//                topicFileDir = "C:\\Users\\Darren\\Desktop\\INEX_2009\\LTW2009_TOPICS\\LTW2009_Topics\\XML\\";
//                outgoingGTDir = "C:\\Users\\Darren\\Desktop\\INEX_2009\\LTW2009_TOPICS_A2B\\POOL_GROUNDTRUTH\\A2B_OUTGOING_BACKUP\\";
//                incomingGTDir = "";
////                produceA2FGTResultSet(topicFileDir, outgoingGTDir, incomingGTDir);
//                break;
//            case 9:
//                // inspect submission against Wikipedia GR
//                Vector<String> runsDirV = new Vector<String>();
//                runsDirV.add("C:\\PHD\\INEX_2009\\LTW2009_SUBMISSION\\Wikipedia_A2B\\OTAGO");
//                runsDirV.add("C:\\PHD\\INEX_2009\\LTW2009_SUBMISSION\\Wikipedia_A2B\\QUT");
//                runsDirV.add("C:\\PHD\\INEX_2009\\LTW2009_SUBMISSION\\Wikipedia_A2B\\UAMS");
//                runsDirV.add("C:\\PHD\\INEX_2009\\LTW2009_SUBMISSION\\Wikipedia_A2B\\UWaterloo");
//                inspectA2BRunAgainstGT(runsDirV);
//                break;
//            case 10:
//                // convert XML to Text
//                topicFileDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\LTW2009_F2FTOPICS\\";
//                topicFileFPath = topicFileDir + "LTW2009_F2FTOPICS.txt";
//                outgoingGTDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\LTW2009_GROUND_TRUTH\\LTW2009_TOPICS_F2FGT\\LTW2009_GROUND_TRUTH_OUTGOING\\";
//                incomingGTDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\LTW2009_GROUND_TRUTH\\LTW2009_TOPICS_F2FGT\\LTW2009_GROUND_TRUTH_INCOMING\\";
//                produceF2FGTBaseline(topicFileFPath, outgoingGTDir, incomingGTDir);
//                break;
//            case 11:
//                // convert XML to Text
//                topicFileDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_A2B\\LTW2009_Topics\\XML\\";
//                outgoingGTDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_A2B\\LTW2009_TOPICS_A2BGT\\A2B_OUTGOING_BACKUP\\";
//                incomingGTDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_A2B\\LTW2009_TOPICS_A2BGT\\A2B_INCOMING_BACKUP\\";
//                produceA2BGTBaseline(topicFileDir, outgoingGTDir, incomingGTDir);
//                break;
//            case 12:
//                // convert A2B Runs to Evaluation XML Form
//                a2bRunsRootDir = "C:\\PHD\\INEX_2009\\LTW2009_SUBMISSION\\Wikipedia_A2B\\";
//                Vector<String> a2bSubmissionDirVS = new Vector<String>();
////                a2bSubmissionDirVS.add(a2bRunsRootDir + "OTAGO");
////                a2bSubmissionDirVS.add(a2bRunsRootDir + "QUT");
//                a2bSubmissionDirVS.add(a2bRunsRootDir + "UAMS");
////                a2bSubmissionDirVS.add(a2bRunsRootDir + "UWaterloo");
//                boolean onlyLimitNu = true;
//                // only 50 outgoing and 250 incoming
//                parseA2BRunToEvalXML(a2bSubmissionDirVS, onlyLimitNu);
//                break;
//            case 13:
//                String poolFileDir = "G:\\PHD_Tools\\LTW2009_Assessment_Results\\0000_POOL";
//                produceA2BManualResultSet(poolFileDir);
//                break;
//            case 14:
//                produceA2BManualBaseline();
//                break;
//            default:
//                log("This is the default option...");
//        }
    }
    // =========================================================================

    private static void produceA2BManualBaseline() {
        String linkFolderT = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_A2B\\anchorLinkPairPerTopic";
        String topicAnchorIndexFolder = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_A2B\\LTW2009_Topics\\TopicAnchors_Index";
        // 1) Get GT-Pool Data into Vector & Hashtable
        //    Topic ID,
        //    Vector.add(new String[]{String.valueOf(thisOffset), String.valueOf(thisLength), thisAnchorTxt, thisALinkID})
        // V is sorted by Offset
        Hashtable<String, Vector<String[]>> gtTopicDataSetHT = gtDataExtractor(linkFolderT, topicAnchorIndexFolder);
        // =====================================================================
        // Properties: participant-id, run-id, task
        Vector<String> wikiA2BBaselineProperty = new Vector<String>();
        wikiA2BBaselineProperty.add("99999");
        wikiA2BBaselineProperty.add("A2B_GT_00");
        wikiA2BBaselineProperty.add("LTW_A2B");
        // ---------------------------------------------------------------------
        String targetWikiBaseline = "C:\\JTemp\\LTWEvaluationTool\\Resources\\33A2BWikipediaMBaseline.xml";
        boolean onlyLimitNu = false;
        // ---------------------------------------------------------------------
        writeWikiGTEvalBaseline(onlyLimitNu, targetWikiBaseline, wikiA2BBaselineProperty, gtTopicDataSetHT);
    }

    private static void writeWikiGTEvalBaseline(boolean onlyLimitNu, String targetEvalFile, Vector<String> a2bSubmissionProperty, Hashtable<String, Vector<String[]>> submissionDataHT) {
        /**
         * <details><machine><cpu>AMD Athlon64 3500+</cpu><speed>2.2GHz</speed><cores></cores><hyperthreads></hyperthreads><memory> 2GB</memory></machine><time>1160.26 seconds</time></details>
         * <description>Anchor to BEP with cutoff and ER</description>
         * <collections><collection>Wikipedia</collection></collections>
         */
        try {
            log("TargetFile: " + targetEvalFile);
            FileWriter fw = new FileWriter(new File(targetEvalFile));
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
            fw.write("\n");
            fw.write("<crosslink-submission participant-id=\"" + a2bSubmissionProperty.elementAt(0) + "\" run-id=\"" + a2bSubmissionProperty.elementAt(1) + "\" task=\"" + a2bSubmissionProperty.elementAt(2) + "\" format=\"FOL\">");
            fw.write("\n");
            fw.write("<details><machine><cpu>AMD Athlon64 3500+</cpu><speed>2.2GHz</speed><cores></cores><hyperthreads></hyperthreads><memory>2GB</memory></machine><time>1160.26 seconds</time></details>");
            fw.write("\n");
            fw.write("<description>Anchor to BEP with cutoff and ER</description>");
            fw.write("\n");
            fw.write("<collections><collection>Wikipedia</collection></collections>");
            fw.write("\n");
            // -----------------------------------------------------------------
            Enumeration topicEnu = submissionDataHT.keys();
            while (topicEnu.hasMoreElements()) {
                Object topicObj = topicEnu.nextElement();
                String topicFile = topicObj.toString().trim() + ".xml";
                String topicName = "";
                fw.write("<topic name=\"" + topicName + "\" file=\"" + topicFile + "\">");
                fw.write("\n");
                // -------------------------------------------------------------
                // -------------------------------------------------------------
                // Outgoing Links
                /**
                 * <link>
                 * <anchor><file>100011.xml</file><offset>1288</offset><length>25</length></anchor>
                 * <linkto><file>1085526.xml</file><bep>0</bep></linkto>
                 * </link>
                 */
                fw.write("<outgoing>");
                fw.write("\n");
                // -------------------------------------------------------------
                // Outgoing: topicOutABLinks.add(new String[]{anchorO, anchorL, bepOffset, bepFileID});
                Vector<String> olTrackV = new Vector<String>();
                int anchorNum = 0;
                Vector<String[]> outgoingABLinksV = submissionDataHT.get(topicObj.toString());
                for (String[] outgoingABLinks : outgoingABLinksV) {
                    if (onlyLimitNu) {
                        String aOffsetLength = outgoingABLinks[0].trim() + "_" + outgoingABLinks[1].trim();
                        if (!olTrackV.contains(aOffsetLength)) {
                            olTrackV.add(aOffsetLength);
                            // -------------------------------------------------
                            fw.write("<link>");
                            fw.write("<anchor><file>" + topicFile + "</file><offset>" + outgoingABLinks[0] + "</offset><length>" + outgoingABLinks[1] + "</length></anchor>");
                            fw.write("<linkto><file>" + outgoingABLinks[3] + ".xml</file><bep>0</bep></linkto>");
                            fw.write("</link>");
                            fw.write("\n");
                            // -------------------------------------------------
                            anchorNum++;
                            if (anchorNum >= allowedAnchorNumber) {
                                break;
                            }
                        }
                    } else {
                        fw.write("<link>");
                        fw.write("<anchor><file>" + topicFile + "</file><offset>" + outgoingABLinks[0] + "</offset><length>" + outgoingABLinks[1] + "</length></anchor>");
                        fw.write("<linkto><file>" + outgoingABLinks[3] + ".xml</file><bep>0</bep></linkto>");
                        fw.write("</link>");
                        fw.write("\n");
                    }
                }
                log("total Anchor-link Num: " + anchorNum);
                // -------------------------------------------------------------
                fw.write("</outgoing>");
                fw.write("\n");
                // -------------------------------------------------------------
                // -------------------------------------------------------------
                // Incoming Links
                /**
                 * <link>
                 * <anchor><file>98089.xml</file><offset>15</offset><length>8</length></anchor>
                 * <linkto><file>100011.xml</file><bep>0</bep></linkto>
                 * </link>
                 */
                fw.write("<incoming>");
                fw.write("\n");
                // -------------------------------------------------------------
                // Incoming: topicInBALinks.add(new String[]{bepO, fAnchorOffset, fAnchorLength, fAnchorFileID});
//                int bepLinkNum = 0;
//                Vector<String[]> incomingABLinksV = runOutInDataSet.elementAt(1);
//                for (String[] incomingBALinks : incomingABLinksV) {
//                    bepLinkNum++;
//                    if (bepLinkNum <= allowedInLinkNumber) {
//                        fw.write("<link>");
//                        fw.write("<anchor><file>" + incomingBALinks[3] + ".xml</file><offset>" + incomingBALinks[1] + "</offset><length>" + incomingBALinks[2] + "</length></anchor>");
//                        fw.write("<linkto><file>" + topicFile + "</file><bep>" + incomingBALinks[0] + "</bep></linkto>");
//                        fw.write("</link>");
//                        fw.write("\n");
//                    } else {
//                        break;
//                    }
//                }
                // -------------------------------------------------------------
                fw.write("</incoming>");
                fw.write("\n");
                // -------------------------------------------------------------
                // -------------------------------------------------------------
                fw.write("</topic>");
                fw.write("\n");
            }
            fw.write("</crosslink-submission>");
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static Hashtable<String, Vector<String[]>> gtDataExtractor(String linkFolderT, String topicAnchorIndexFolder) {
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
//                                log("Duplicated Offset ... " + myOffset + " <-- " + allA2BLinksOLTxtIDSA[2]);
                            }
                            abOffsetVS.add(myOffset);
                            abOffsetHT.put(myOffset, allA2BLinksOLTxtIDSA);
                        }
                        Vector<String> sortedABOffsetVS = sortVectorNumbers(abOffsetVS);
                        for (String sOffset : sortedABOffsetVS) {
                            String[] a2BLinksOLTxtIDSA = abOffsetHT.get(sOffset);
                            sortedAllA2BLinksOLTxtIDVSA.add(a2BLinksOLTxtIDSA);
                        } // =====================================================
                        // Produce a Sorted Offset ABLinks Vector for each Topic
                        allAtoBLinksHT.put(thisTopicID, sortedAllA2BLinksOLTxtIDVSA);
                        // </editor-fold>
                    } catch (IOException ex) {
                        Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        return allAtoBLinksHT;
    }

    public static boolean isInNumber(String num) {
        try {
            int intI = Integer.parseInt(num);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
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
                        } // -----------------------------------------------------
                        if ((thisAnchorSet[0] + "s").equals(aTXT)) {
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
            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
        }
        // -----------------------------------------------------------------
//        log(thisTopicID + "*********** NOT FOUND ************ " + thisAnchorTxt);
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

    private static Vector<String> sortVectorNumbers(Vector<String> myNumbersV) {
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
    // =========================================================================
    // =========================================================================
    // <editor-fold defaultstate="collapsed" desc="produce A2B Manual Result Set">
    // Topic Name & ID
    private static Vector<String[]> topicIDName = new Vector<String[]>();
    // Hashtable<TopicID, Vector<new String[]{aoffset, alength, aname, bepOffset, bepFileID}>>
    private static Hashtable<String, Vector<String[]>> topicOutData = new Hashtable<String, Vector<String[]>>();
    // Hashtable<TopicID, Vector<new String[]{boffset, aOffset, alength, aname, aFileID}>>
    private static Hashtable<String, Vector<String[]>> topicInData = new Hashtable<String, Vector<String[]>>();
    // ---------------------------------------------------------------------

    private static void produceA2BManualResultSet(String poolFileDir) {
        populateTopicOutInData(poolFileDir);
        // ---------------------------------------------------------------------
        produceA2BManualResultSet(topicIDName, topicOutData, topicInData);
    }

    private static void produceA2BManualResultSet(Vector<String[]> myTopicIDName, Hashtable<String, Vector<String[]>> myTopicOutData, Hashtable<String, Vector<String[]>> myTopicInData) {
        /**
         * <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
         * <ltwResultsetType>
         * <ltw_Topic name="Fender Stratocaster" id="531921">
         * <outgoingLinks>
         * <outLink aname="tremolo" aoffset="85" alength="7" boffset="0">65785</outLink>
         * </outgoingLinks>
         * <incomingLinks>
         * </incomingLinks>
         * </ltw_Topic>
         */
        try {
            String rDir = "C:\\JTemp\\LTWEvaluationTool\\Resources\\";
            String rsXmlFPath = rDir + "33A2BManualResultSet_FullOutName.xml";
            FileWriter fw = new FileWriter(new File(rsXmlFPath));
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
            fw.write("\n");
            fw.write("<ltwResultsetType>");
            fw.write("\n");
            for (String[] topicSet : myTopicIDName) {
                String topicID = topicSet[0];
                String topicName = topicSet[1];
                fw.write("<ltw_Topic name=\"" + topicName + "\" id=\"" + topicID + "\">");
                fw.write("\n");
                // -------------------------------------------------------------
                // myTOutgoingData.add(new String[]{pAOffset, pALength, pAName, bepOffsetTxt, bepFileIDTxt});
                // <outLink aname="tremolo" aoffset="85" alength="7" boffset="0">65785</outLink>
                Vector<String[]> topicOutDataSet = myTopicOutData.get(topicID);
                fw.write("<outgoingLinks>");
                fw.write("\n");
                for (String[] tOutDataSet : topicOutDataSet) {
                    String pAOffset = tOutDataSet[0];
                    String pALength = tOutDataSet[1];
//                    String pAName = tOutDataSet[2];
                    String pAName = "";
                    String bepOffsetTxt = tOutDataSet[3];
                    String bepFileIDTxt = tOutDataSet[4];
                    fw.write("<outLink aname=\"" + pAName + "\" aoffset=\"" + pAOffset + "\" alength=\"" + pALength + "\" boffset=\"" + bepOffsetTxt + "\">" + bepFileIDTxt + "</outLink>");
                    fw.write("\n");
                }
                fw.write("</outgoingLinks>");
                fw.write("\n");
                // -------------------------------------------------------------
                // myTIncomingData.add(new String[]{pBOffset, fAOffsetTxt, fALengthTxt, fAAnchorTxt, fAFileIDTxt});
                Vector<String[]> topicInDataSet = myTopicInData.get(topicID);
                fw.write("<incomingLinks>");
                fw.write("\n");
                for (String[] tInDataSet : topicInDataSet) {
                    String pBOffset = tInDataSet[0];
                    String fAOffsetTxt = tInDataSet[1];
                    String fALengthTxt = tInDataSet[2];
//                    String fAAnchorTxt = tInDataSet[3];
                    String fAAnchorTxt = "";
                    String fAFileIDTxt = tInDataSet[4];
                    fw.write("<inLink aname=\"" + fAAnchorTxt + "\" aoffset=\"" + fAOffsetTxt + "\" alength=\"" + fALengthTxt + "\" boffset=\"" + pBOffset + "\">" + fAFileIDTxt + "</inLink>");
                    fw.write("\n");
                }
                fw.write("</incomingLinks>");
                fw.write("\n");
                // -------------------------------------------------------------
                fw.write("</ltw_Topic>");
                fw.write("\n");
            }
            fw.write("</ltwResultsetType>");
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void populateTopicOutInData(String poolFileDir) {

        File poolFDir = new File(poolFileDir);
        if (poolFDir.isDirectory()) {
            File[] poolFiles = poolFDir.listFiles();
            for (File thisPoolFile : poolFiles) {
                String thisPoolFPath = thisPoolFile.getAbsolutePath();
                log(thisPoolFPath);
                if (thisPoolFPath.endsWith(".xml")) {

                    VTDGen vg = new VTDGen();
                    if (vg.parseFile(thisPoolFPath, true)) {
                        FileOutputStream fos = null;
                        try {
                            VTDNav vn = vg.getNav();
                            File fo = new File(thisPoolFPath);
                            fos = new FileOutputStream(fo);

                            AutoPilot ap = new AutoPilot(vn);
                            XMLModifier xm = new XMLModifier(vn);
                            // -------------------------------------------------
                            String fileTxt = "";
                            String nameTxt = "";
                            String xPath = "/crosslink-assessment/topic";
                            ap.selectXPath(xPath);
                            int i = -1;
                            while ((i = ap.evalXPath()) != -1) {
                                int j = vn.getAttrVal("file");
                                int k = vn.getAttrVal("name");
                                if (j != -1) {
                                    fileTxt = vn.toRawString(j).trim().toString();
                                    nameTxt = vn.toRawString(k).trim().toString();
                                    topicIDName.add(new String[]{fileTxt, nameTxt});
                                    log(fileTxt + " - " + nameTxt);
                                }
                            }
                            // =================================================
                            // -------------------------------------------------
                            // Hashtable<TopicID, Vector<new String[]{aoffset, alength, aname, bepOffset, bepFileID}>>
                            Vector<String[]> pAnchorSet = new Vector<String[]>();
                            String xPath1 = "/crosslink-assessment/topic[@file='" + fileTxt + "']/outgoinglinks/anchor";
                            ap.selectXPath(xPath1);
                            int i1 = -1;
                            while ((i1 = ap.evalXPath()) != -1) {
                                int j1 = vn.getAttrVal("arel");
                                int k1 = vn.getAttrVal("aname");
                                int l1 = vn.getAttrVal("aoffset");
                                int m1 = vn.getAttrVal("alength");
                                if (m1 != -1) {
                                    String aRelTxt = vn.toRawString(j1).trim().toString();
                                    String aNameTxt = vn.toRawString(k1).trim().toString();
                                    String aOffsetTxt = vn.toRawString(l1).trim().toString();
                                    String aLengthTxt = vn.toRawString(m1).trim().toString();
                                    if (aRelTxt.equals("1")) {
                                        pAnchorSet.add(new String[]{aOffsetTxt, aLengthTxt, aNameTxt});
                                        log(aRelTxt + " - " + aOffsetTxt + " - " + aLengthTxt + " - " + aNameTxt);
                                    }
                                }
                            }
                            // -------------------------------------------------
                            // Hashtable<TopicID, Vector<new String[]{boffset, aOffset, alength, aname, aFileID}>>
                            Vector<String[]> pBepSet = new Vector<String[]>();
                            String xPath2 = "/crosslink-assessment/topic[@file='" + fileTxt + "']/incominglinks/bep";
                            ap.selectXPath(xPath2);
                            int i2 = -1;
                            while ((i2 = ap.evalXPath()) != -1) {
                                int j2 = vn.getAttrVal("borel");
                                int k2 = vn.getAttrVal("boffset");
                                if (k2 != -1) {
                                    String bRelTxt = vn.toRawString(j2).trim().toString();
                                    String bOffsetTxt = vn.toRawString(k2).trim().toString();
                                    if (bRelTxt.equals("0") || bRelTxt.equals("1")) {
                                        pBepSet.add(new String[]{bOffsetTxt});
                                        log(bRelTxt + " - " + bOffsetTxt);
                                    }
                                }
                            }
                            // =================================================
                            // -------------------------------------------------
                            // Hashtable<TopicID, Vector<new String[]{aoffset, alength, aname, bepOffset, bepFileID}>>
                            Vector<String[]> myTOutgoingData = new Vector<String[]>();
                            for (String[] pAnchorOLName : pAnchorSet) {
                                String pAOffset = pAnchorOLName[0];
                                String pALength = pAnchorOLName[1];
                                String pAName = pAnchorOLName[2];
                                String xPath11 = "/crosslink-assessment/topic[@file='" + fileTxt + "']/outgoinglinks/anchor[@aoffset='" + pAOffset + "' and @alength='" + pALength + "']/subanchor/tobep";
                                ap.selectXPath(xPath11);
                                int i11 = -1;
                                while ((i11 = ap.evalXPath()) != -1) {
                                    // <tobep tbrel="-1" timein="" timeout="" tboffset="218" tbstartp="212">856</tobep>
                                    int j11 = vn.getAttrVal("tbrel");
                                    int k11 = vn.getAttrVal("tboffset");
                                    int l11 = vn.getText();
                                    if (l11 != -1) {
                                        String bepRelTxt = vn.toRawString(j11).trim().toString();
                                        String bepOffsetTxt = vn.toRawString(k11).trim().toString();
                                        String bepFileIDTxt = vn.toRawString(l11).trim().toString();
                                        if (bepRelTxt.equals("1")) {
                                            myTOutgoingData.add(new String[]{pAOffset, pALength, pAName, bepOffsetTxt, bepFileIDTxt});
                                            log(bepRelTxt + " - " + pAOffset + " - " + pALength + " - " + pAName + " - " + bepOffsetTxt + " - " + bepFileIDTxt);
                                        }
                                    }
                                }
                            }
                            topicOutData.put(fileTxt, myTOutgoingData);
                            // -------------------------------------------------
                            // Hashtable<TopicID, Vector<new String[]{boffset, aOffset, alength, aname, aFileID}>>
                            Vector<String[]> myTIncomingData = new Vector<String[]>();
                            for (String[] pBepO : pBepSet) {
                                String pBOffset = pBepO[0];
                                String xPath11 = "/crosslink-assessment/topic[@file='" + fileTxt + "']/incominglinks/bep[@boffset='" + pBOffset + "']/fromanchor";
                                ap.selectXPath(xPath11);
                                int i22 = -1;
                                while ((i22 = ap.evalXPath()) != -1) {
                                    // <fromanchor farel="-1" faoffset="1600" falength="9" faanchor="Apple III">856</fromanchor>
                                    int j22 = vn.getAttrVal("farel");
                                    int k22 = vn.getAttrVal("faoffset");
                                    int l22 = vn.getAttrVal("falength");
                                    int m22 = vn.getAttrVal("faanchor");
                                    int n22 = vn.getText();
                                    if (n22 != -1) {
                                        String fARelTxt = vn.toRawString(j22).trim().toString();
                                        String fAOffsetTxt = vn.toRawString(k22).trim().toString();
                                        String fALengthTxt = vn.toRawString(l22).trim().toString();
                                        String fAAnchorTxt = vn.toRawString(m22).trim().toString();
                                        String fAFileIDTxt = vn.toRawString(n22).trim().toString();
                                        if (fARelTxt.equals("1")) {
                                            myTIncomingData.add(new String[]{pBOffset, fAOffsetTxt, fALengthTxt, fAAnchorTxt, fAFileIDTxt});
                                            log(fARelTxt + " - " + pBOffset + " - " + fAOffsetTxt + " - " + fALengthTxt + " - " + fAAnchorTxt + " - " + fAFileIDTxt);
                                        }
                                    }
                                }
                            }
                            topicInData.put(fileTxt, myTIncomingData);
                            // =================================================
                            xm.output(fos);
                            fos.close();
                        } catch (IOException ex) {
                            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (TranscodeException ex) {
                            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (XPathEvalException ex) {
                            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (NavException ex) {
                            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (XPathParseException ex) {
                            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ModifyException ex) {
                            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
    }
    // <editor-fold defaultstate="collapsed" desc="OUT / IN DATA">
//    private static Hashtable<String, Vector<String[]>> getPoolOutData(String poolFileDir) {
//        Hashtable<String, Vector<String[]>> topicOutData = new Hashtable<String, Vector<String[]>>();
//        Vector<String[]> tOutgoingData = new Vector<String[]>();
//        File poolFDir = new File(poolFileDir);
//        if (poolFDir.isDirectory()) {
//            File[] poolFiles = poolFDir.listFiles();
//            for (File thisPoolFile : poolFiles) {
//                String thisPoolFPath = thisPoolFile.getAbsolutePath();
//                log(thisPoolFPath);
//                if (thisPoolFPath.endsWith(".xml")) {
//                    VTDGen vg = new VTDGen();
//                    if (vg.parseFile(thisPoolFPath, true)) {
//                        FileOutputStream fos = null;
//                        try {
//                            VTDNav vn = vg.getNav();
//                            File fo = new File(thisPoolFPath);
//                            fos = new FileOutputStream(fo);
//                            AutoPilot ap = new AutoPilot(vn);
//                            XMLModifier xm = new XMLModifier(vn);
//
////                            String xPath = "/crosslink-assessment/topic[@file='" + topicID + "']/outgoinglinks/anchor[@aoffset='" + pAnchorOL[0] + "' and @alength='" + pAnchorOL[1] + "']/subanchor/tobep";
//                            String xPath = "/crosslink-assessment/topic";
//                            ap.selectXPath(xPath);
//                            int i = -1;
//                            while ((i = ap.evalXPath()) != -1) {
//                                int j = vn.getAttrVal("file");
//                                int k = vn.getAttrVal("name");
//                                if (j != -1) {
//                                    String fileTxt = vn.toRawString(j).trim().toString();
//                                    String nameTxt = vn.toRawString(k).trim().toString();
//                                    topicIDName.add(new String[]{fileTxt, nameTxt});
//                                    log(fileTxt + " - " + nameTxt);
//                                }
//                            }
//                            xm.output(fos);
//                            fos.close();
//                        } catch (IOException ex) {
//                            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
//                        } catch (TranscodeException ex) {
//                            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
//                        } catch (XPathEvalException ex) {
//                            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
//                        } catch (NavException ex) {
//                            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
//                        } catch (XPathParseException ex) {
//                            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
//                        } catch (ModifyException ex) {
//                            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    }
//                }
//            }
//        }
//        return topicOutData;
//    }
//
//    private static Hashtable<String, Vector<String[]>> getPoolInData(String poolFileDir) {
//        Hashtable<String, Vector<String[]>> topicInData = new Hashtable<String, Vector<String[]>>();
//        File poolFDir = new File(poolFileDir);
//        if (poolFDir.isDirectory()) {
//            File[] poolFiles = poolFDir.listFiles();
//            for (File thisPoolFile : poolFiles) {
//                String thisPoolFPath = thisPoolFile.getAbsolutePath();
//                log(thisPoolFPath);
//                if (thisPoolFPath.endsWith(".xml")) {
//                }
//            }
//        }
//        return topicInData;
//    }
    // </editor-fold>
    // </editor-fold>
    // =========================================================================
    // =========================================================================
    // <editor-fold defaultstate="collapsed" desc="convert A2BRun To Eval XML">

    private static void parseA2BRunToEvalXML(Vector<String> a2bSubmissionDirVS, boolean onlyLimitNu) {
        for (String a2bRunsDir : a2bSubmissionDirVS) {
            File a2bRunsDirF = new File(a2bRunsDir);
            if (a2bRunsDirF.isDirectory()) {
                File[] a2bRunsList = a2bRunsDirF.listFiles();
                for (File a2bRunsXml : a2bRunsList) {
                    String a2bRunsXmlFPath = a2bRunsXml.getAbsolutePath();
                    if (a2bRunsXmlFPath.endsWith(".xml")) {
                        String a2bRunsXmlFName = a2bRunsXml.getName();
                        String a2bRunsXmlName = a2bRunsXmlFName.substring(0, a2bRunsXmlFName.lastIndexOf(".xml"));
                        // -----------------------------------------------------
                        // Get RUN Properties:
                        // participant-id, run-id, task
                        Vector<String> a2bSubmissionProperty = getA2BSubmissionProperty(a2bRunsXmlFPath);
                        // -----------------------------------------------------
                        // Format:
                        // topicID, V{Outgoing, Incoming}
                        // Outgoing: topicOutABLinks.add(new String[]{anchorO, anchorL, bepOffset, bepFileID});
                        // Incoming: topicInBALinks.add(new String[]{bepO, fAnchorOffset, fAnchorLength, fAnchorFileID});
                        Hashtable<String, Vector<Vector<String[]>>> submissionDataHT = getA2BSubmissionData(a2bRunsXmlFPath);
                        // -----------------------------------------------------
                        String targetDir = a2bRunsDirF.getAbsolutePath() + "_EvalLimited";
                        File targetDirF = new File(targetDir);
                        if (!targetDirF.exists()) {
                            targetDirF.mkdir();
                        }
                        // -----------------------------------------------------
                        String targetEvalFile = targetDir + "\\" + a2bRunsXmlName + "_EvalO50I250.xml";
                        writeToEvalXML(onlyLimitNu, targetEvalFile, a2bSubmissionProperty, submissionDataHT);
                    }
                }
            }
        }
    }

    private static void writeToEvalXML(boolean onlyLimitNu, String targetEvalFile, Vector<String> a2bSubmissionProperty, Hashtable<String, Vector<Vector<String[]>>> submissionDataHT) {
        /**
         * <details><machine><cpu>AMD Athlon64 3500+</cpu><speed>2.2GHz</speed><cores></cores><hyperthreads></hyperthreads><memory> 2GB</memory></machine><time>1160.26 seconds</time></details>
         * <description>Anchor to BEP with cutoff and ER</description>
         * <collections><collection>Wikipedia</collection></collections>
         */
        try {
            log("TargetFile: " + targetEvalFile);
            FileWriter fw = new FileWriter(new File(targetEvalFile));
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
            fw.write("\n");
            fw.write("<crosslink-submission participant-id=\"" + a2bSubmissionProperty.elementAt(0) + "\" run-id=\"" + a2bSubmissionProperty.elementAt(1) + "\" task=\"" + a2bSubmissionProperty.elementAt(2) + "\" format=\"FOL\">");
            fw.write("\n");
            fw.write("<details><machine><cpu>AMD Athlon64 3500+</cpu><speed>2.2GHz</speed><cores></cores><hyperthreads></hyperthreads><memory>2GB</memory></machine><time>1160.26 seconds</time></details>");
            fw.write("\n");
            fw.write("<description>Anchor to BEP with cutoff and ER</description>");
            fw.write("\n");
            fw.write("<collections><collection>Wikipedia</collection></collections>");
            fw.write("\n");
            // -----------------------------------------------------------------
            Enumeration topicEnu = submissionDataHT.keys();
            while (topicEnu.hasMoreElements()) {
                Object topicObj = topicEnu.nextElement();
                String topicFile = topicObj.toString().trim();
                
                if (topicFile.length() == 0)
					System.err.println("Enpty topic id.");
                
                topicFile.concat(".xml");
                String topicName = "";
                fw.write("<topic name=\"" + topicName + "\" file=\"" + topicFile + "\">");
                fw.write("\n");
                // -------------------------------------------------------------
                Vector<Vector<String[]>> runOutInDataSet = submissionDataHT.get(topicObj.toString());
                // -------------------------------------------------------------
                // -------------------------------------------------------------
                // Outgoing Links
                /**
                 * <link>
                 * <anchor><file>100011.xml</file><offset>1288</offset><length>25</length></anchor>
                 * <linkto><file>1085526.xml</file><bep>0</bep></linkto>
                 * </link>
                 */
                fw.write("<outgoing>");
                fw.write("\n");
                // -------------------------------------------------------------
                // Outgoing: topicOutABLinks.add(new String[]{anchorO, anchorL, bepOffset, bepFileID});
                Vector<String> olTrackV = new Vector<String>();
                int anchorNum = 0;
                Vector<String[]> outgoingABLinksV = runOutInDataSet.elementAt(0);
                for (String[] outgoingABLinks : outgoingABLinksV) {
                    if (onlyLimitNu) {
                        String aOffsetLength = outgoingABLinks[0].trim() + "_" + outgoingABLinks[1].trim();
                        if (!olTrackV.contains(aOffsetLength)) {
                            olTrackV.add(aOffsetLength);
                            // -------------------------------------------------
                            fw.write("<link>");
                            fw.write("<anchor><file>" + topicFile + "</file><offset>" + outgoingABLinks[0] + "</offset><length>" + outgoingABLinks[1] + "</length></anchor>");
                            fw.write("<linkto><file>" + outgoingABLinks[3] + ".xml</file><bep>" + outgoingABLinks[2] + "</bep></linkto>");
                            fw.write("</link>");
                            fw.write("\n");
                            // -------------------------------------------------
                            anchorNum++;
                            if (anchorNum >= allowedAnchorNumber) {
                                break;
                            }
                        }
                    } else {
                        fw.write("<link>");
                        fw.write("<anchor><file>" + topicFile + "</file><offset>" + outgoingABLinks[0] + "</offset><length>" + outgoingABLinks[1] + "</length></anchor>");
                        fw.write("<linkto><file>" + outgoingABLinks[3] + ".xml</file><bep>" + outgoingABLinks[2] + "</bep></linkto>");
                        fw.write("</link>");
                        fw.write("\n");
                    }
                }
                log("total Anchor-link Num: " + anchorNum);
                // -------------------------------------------------------------
                fw.write("</outgoing>");
                fw.write("\n");
                // -------------------------------------------------------------
                // -------------------------------------------------------------
                // Incoming Links
                /**
                 * <link>
                 * <anchor><file>98089.xml</file><offset>15</offset><length>8</length></anchor>
                 * <linkto><file>100011.xml</file><bep>0</bep></linkto>
                 * </link>
                 */
                fw.write("<incoming>");
                fw.write("\n");
                // -------------------------------------------------------------
                // Incoming: topicInBALinks.add(new String[]{bepO, fAnchorOffset, fAnchorLength, fAnchorFileID});
                int bepLinkNum = 0;
                Vector<String[]> incomingABLinksV = runOutInDataSet.elementAt(1);
                for (String[] incomingBALinks : incomingABLinksV) {
                    bepLinkNum++;
                    if (bepLinkNum <= allowedInLinkNumber) {
                        fw.write("<link>");
                        fw.write("<anchor><file>" + incomingBALinks[3] + ".xml</file><offset>" + incomingBALinks[1] + "</offset><length>" + incomingBALinks[2] + "</length></anchor>");
                        fw.write("<linkto><file>" + topicFile + "</file><bep>" + incomingBALinks[0] + "</bep></linkto>");
                        fw.write("</link>");
                        fw.write("\n");
                    } else {
                        break;
                    }
                }
                // -------------------------------------------------------------
                fw.write("</incoming>");
                fw.write("\n");
                // -------------------------------------------------------------
                // -------------------------------------------------------------
                fw.write("</topic>");
                fw.write("\n");
            }
            fw.write("</crosslink-submission>");
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static Vector<String> getA2BSubmissionProperty(String thisRunFullPath) {
        Vector<String> a2bRunProperty = new Vector<String>();
        VTDGen vg = new VTDGen();
        if (vg.parseFile(thisRunFullPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(thisRunFullPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);
                /**
                 * <crosslink-submission participant-id="5" run-id="QUT_LTW_A2B_SEA_01" task="LTW_A2B">
                 */
                String xPath = "/crosslink-submission";
                ap.selectXPath(xPath);
                int i = -1;
                while ((i = ap.evalXPath()) != -1) {
                    int j = vn.getAttrVal("participant-id");
                    int k = vn.getAttrVal("run-id");
                    int l = vn.getAttrVal("task");
                    if (j != -1) {
                        String pID = vn.toRawString(j).toString();
                        String runID = vn.toRawString(k).toString();
                        String task = vn.toRawString(l).toString();
                        a2bRunProperty.add(pID);
                        a2bRunProperty.add(runID);
                        a2bRunProperty.add(task);
                    }
                }
                // =============================================================
                xm.output(fos);
                fos.close();
            } catch (TranscodeException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return a2bRunProperty;
    }
    // </editor-fold>
    // =========================================================================
    // =========================================================================
    // <editor-fold defaultstate="collapsed" desc="produce F2F Wikipedia GR Baseline">

    private static void produceF2FGTBaseline(String topicFileFPath, String outgoingGTDir, String incomingGTDir) {
        try {
            // -----------------------------------------------------------------
            Vector<String[]> topicPairsV = new Vector<String[]>();
            File tpFile = new File(topicFileFPath);
            FileInputStream fstream = new FileInputStream(topicFileFPath);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] thisLineSet = strLine.split(" : ");
                String topicID = thisLineSet[1].trim().substring(0, thisLineSet[1].trim().lastIndexOf(".xml"));
                if (isInNumber(topicID)) {
                    topicPairsV.add(new String[]{topicID, thisLineSet[2]});
//                    log(thisLineSet[0]);
                } else {
                    log("Invalid Topic ID: " + topicID + " at line: " + strLine);
                }
            }
            fstream.close();
            // -----------------------------------------------------------------
            String topicFileDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\LTW2009_F2FTOPICS\\";
            Vector<String> topicLinkIDS = null;
            // populate F2F Outgoing GT Links
            Hashtable<String, Vector<String>> topicOutgoingLinksHT = new Hashtable<String, Vector<String>>();
            for (String[] topicPairSA : topicPairsV) {
                String topicID = topicPairSA[0];
                String topicName = topicPairSA[1];
                topicLinkIDS = new Vector<String>();
                String topicGTFPath = outgoingGTDir + topicID + "_F2FGT.txt";
                FileInputStream fis = new FileInputStream(topicGTFPath);
                DataInputStream dis = new DataInputStream(fis);
                BufferedReader br2 = new BufferedReader(new InputStreamReader(dis));
                String strLine2;
                while ((strLine2 = br2.readLine()) != null) {
                    log("this Line: " + strLine2);
                    String thisLinkID = strLine2.trim().substring(strLine2.trim().lastIndexOf("/") + 1, strLine2.trim().length());
                    log("out LinkID: " + thisLinkID);
                    topicLinkIDS.add(thisLinkID);
                }
                topicOutgoingLinksHT.put(topicID, topicLinkIDS);
            }
            // -----------------------------------------------------------------
            // populate F2F Incoming GT Links
            Hashtable<String, Vector<String>> topicIncomingLinksHT = new Hashtable<String, Vector<String>>();
            for (String[] topicPairSA : topicPairsV) {
                String topicID = topicPairSA[0];
                String topicName = topicPairSA[1];
                topicLinkIDS = new Vector<String>();
                String topicGTFPath = incomingGTDir + topicID + "_gtIncoming.txt";
                FileInputStream fis = new FileInputStream(topicGTFPath);
                DataInputStream dis = new DataInputStream(fis);
                BufferedReader br2 = new BufferedReader(new InputStreamReader(dis));
                String strLine2;
                while ((strLine2 = br2.readLine()) != null) {
                    log("this Line: " + strLine2);
                    String thisLinkID = strLine2.trim().substring(0, strLine2.trim().lastIndexOf("_links"));
                    log("in LinkID: " + thisLinkID);
                    topicLinkIDS.add(thisLinkID);
                }
                topicIncomingLinksHT.put(topicID, topicLinkIDS);
            }
            // -----------------------------------------------------------------
            /**
             * <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
             * <ltwResultsetType>
             * <ltw_Topic name="Scurvy" id="28266">
             * <outgoingLinks>
             * <outLink>10843</outLink>
             */
            // produce RESULT SET XML
            String rsXmlFPath = topicFileDir + "5000F2FWikipediaBaseline.xml";
            FileWriter fw = new FileWriter(new File(rsXmlFPath));
//            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
//            fw.write("\n");
            fw.write("<crosslink-submission participant-id='999' run-id='Wikipoedia_GT_Baseline' task='LTW_F2FonA2B' format='FOL'>");
            fw.write("\n");
            fw.write("<details>");
            fw.write("\n");
            fw.write("<machine><cpu>Intel Pentium</cpu><speed>1.86GHz</speed><cores>2</cores><hyperthreads>1</hyperthreads><memory>1GB</memory></machine>");
            fw.write("\n");
            fw.write("</details>");
            fw.write("\n");
            fw.write("<description>Wikipoedia_GT_Baseline</description>");
            fw.write("\n");
            fw.write("<collections><collection>Wikipedia_2009_Collection</collection></collections>");
            fw.write("\n");

            for (String[] topicPairSA : topicPairsV) {
                String topicID = topicPairSA[0];
//                String topicName = topicPairSA[1];
                String topicName = "";
                fw.write("<topic name=\"" + topicName + "\" file=\"" + topicID + ".xml\">");
                fw.write("\n");
                // -------------------------------------------------------------
                // Outgoing Links
                fw.write("<outgoing>");
                fw.write("\n");
                Vector<String> thisOutLinkIDV = topicOutgoingLinksHT.get(topicID);
                for (String thisOutLinkID : thisOutLinkIDV) {
                    fw.write("<link><linkto><file>" + thisOutLinkID + ".xml</file></linkto></link>");
                    fw.write("\n");
                }
                fw.write("</outgoing>");
                fw.write("\n");
                // -------------------------------------------------------------
                // Incoming Links
                fw.write("<incoming>");
                fw.write("\n");
                Vector<String> thisInLinkIDV = topicIncomingLinksHT.get(topicID);
                for (String thisInLinkID : thisInLinkIDV) {
                    fw.write("<link><anchor><file>" + thisInLinkID + ".xml</file></anchor></link>");
                    fw.write("\n");
                }
                fw.write("</incoming>");
                fw.write("\n");
                // -------------------------------------------------------------
                fw.write("</topic>");
                fw.write("\n");
            }
            fw.write("</crosslink-submission>");
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    // </editor-fold>
    // =========================================================================
    // <editor-fold defaultstate="collapsed" desc="produce A2B Wikipedia GR Baseline">

    private static void produceA2BGTBaseline(String topicXMLFileDir, String outgoingGTDir, String incomingGTDir) {
        try {
            // -----------------------------------------------------------------
            Vector<String[]> topicPairsV = new Vector<String[]>();
            File tpFile = new File(topicXMLFileDir);
            if (tpFile.isDirectory()) {
                File[] a2bXmlFileList = tpFile.listFiles();
                for (File a2bXmlFile : a2bXmlFileList) {
                    String a2bXmlFileFName = a2bXmlFile.getName();
                    if (a2bXmlFileFName.endsWith(".xml")) {
                        String a2bXmlFileID = a2bXmlFileFName.substring(0, a2bXmlFileFName.lastIndexOf(".xml"));
                        log("a2bXmlFileID: " + a2bXmlFileID);
                        topicPairsV.add(new String[]{a2bXmlFileID, ""});
                    }
                }
            }
            // -----------------------------------------------------------------
            Vector<String[]> topicIDNamePairsV = new Vector<String[]>();
            String topicFileDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_A2B\\LTW2009_Topics\\";
            Vector<String> topicLinkIDS = null;
            // populate A2B Outgoing GT Links
            Hashtable<String, Vector<String>> topicOutgoingLinksHT = new Hashtable<String, Vector<String>>();
            for (String[] topicPairSA : topicPairsV) {
                String topicID = topicPairSA[0];
                String topicName = topicPairSA[1];
                topicLinkIDS = new Vector<String>();
                String topicGTFPath = outgoingGTDir + topicID + ".xml.txt";
                FileInputStream fis = new FileInputStream(topicGTFPath);
                DataInputStream dis = new DataInputStream(fis);
                BufferedReader br2 = new BufferedReader(new InputStreamReader(dis));
                int lineCounter = 0;
                String strLine2;
                while ((strLine2 = br2.readLine()) != null) {
                    if (lineCounter == 0) {
                        String thisTName = strLine2.trim();
                        topicIDNamePairsV.add(new String[]{topicID, thisTName});
                    } else if (lineCounter % 2 == 0) {
                        log("this Line: " + strLine2);
                        String thisLinkID = strLine2.trim();
                        log("out LinkID: " + thisLinkID);
                        if (!topicLinkIDS.contains(thisLinkID)) {
                            topicLinkIDS.add(thisLinkID);
                        }
                    }
                    lineCounter++;
                }
                topicOutgoingLinksHT.put(topicID, topicLinkIDS);
            }
            // -----------------------------------------------------------------
            // populate A2B Incoming GT Links
            Hashtable<String, Vector<String>> topicIncomingLinksHT = new Hashtable<String, Vector<String>>();
            for (String[] topicPairSA : topicPairsV) {
                String topicID = topicPairSA[0];
                String topicName = topicPairSA[1];
                topicLinkIDS = new Vector<String>();
                String topicGTFPath = incomingGTDir + topicID + "_gtIncoming.txt";
                FileInputStream fis = new FileInputStream(topicGTFPath);
                DataInputStream dis = new DataInputStream(fis);
                BufferedReader br2 = new BufferedReader(new InputStreamReader(dis));
                String strLine2;
                while ((strLine2 = br2.readLine()) != null) {
                    log("this Line: " + strLine2);
                    String thisLinkID = strLine2.trim().substring(0, strLine2.trim().lastIndexOf("_links"));
                    log("in LinkID: " + thisLinkID);
                    if (!topicLinkIDS.contains(thisLinkID)) {
                        topicLinkIDS.add(thisLinkID);
                    }
                }
                topicIncomingLinksHT.put(topicID, topicLinkIDS);
            }
            // -----------------------------------------------------------------
            String rsXmlFPath = topicFileDir + "33A2BWikipediaBaseline_50Out250In.xml";
            FileWriter fw = new FileWriter(new File(rsXmlFPath));
//            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
//            fw.write("\n");
            fw.write("<crosslink-submission participant-id='999' run-id='Wikipoedia_GT_Baseline' task='LTW_F2FonA2B' format='FOL'>");
            fw.write("\n");
            fw.write("<details>");
            fw.write("\n");
            fw.write("<machine><cpu>Intel Pentium</cpu><speed>1.86GHz</speed><cores>2</cores><hyperthreads>1</hyperthreads><memory>1GB</memory></machine>");
            fw.write("\n");
            fw.write("</details>");
            fw.write("\n");
            fw.write("<description>Wikipoedia_GT_Baseline</description>");
            fw.write("\n");
            fw.write("<collections><collection>Wikipedia_2009_Collection</collection></collections>");
            fw.write("\n");

            for (String[] topicPairSA : topicIDNamePairsV) {
                String topicID = topicPairSA[0];
                String topicName = topicPairSA[1];
                fw.write("<topic name=\"" + topicName + "\" file=\"" + topicID + ".xml\">");
                fw.write("\n");
                // -------------------------------------------------------------
                int allowedOutLinks = 50;
                int outLinkCounter = 0;
                // -------------------------------------------------------------
                // Outgoing Links
                fw.write("<outgoing>");
                fw.write("\n");
                Vector<String> thisOutLinkIDV = topicOutgoingLinksHT.get(topicID);
                for (String thisOutLinkID : thisOutLinkIDV) {
                    outLinkCounter++;
                    if (outLinkCounter <= allowedOutLinks) {
                        fw.write("<link><linkto><file>" + thisOutLinkID + ".xml</file></linkto></link>");
                        fw.write("\n");
                    } else {
                        break;
                    }
                }
                fw.write("</outgoing>");
                fw.write("\n");
                // -------------------------------------------------------------
                int allowedInLinks = 250;
                int inLinkCounter = 0;
                // -------------------------------------------------------------
                // Incoming Links
                fw.write("<incoming>");
                fw.write("\n");
                Vector<String> thisInLinkIDV = topicIncomingLinksHT.get(topicID);
                for (String thisInLinkID : thisInLinkIDV) {
                    inLinkCounter++;
                    if (inLinkCounter <= allowedInLinks) {
                        fw.write("<link><anchor><file>" + thisInLinkID + ".xml</file></anchor></link>");
                        fw.write("\n");
                    } else {
                        break;
                    }
                }
                fw.write("</incoming>");
                fw.write("\n");
                // -------------------------------------------------------------
                fw.write("</topic>");
                fw.write("\n");
            }
            fw.write("</crosslink-submission>");
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    // </editor-fold>
    // =========================================================================
    // <editor-fold defaultstate="collapsed" desc="inspect submission against Wikipedia GR">

    private static void inspectA2BRunAgainstGT(Vector<String> runsDirV) {
        // collect Wikipedia GT result set
        String gtRSFileFPath = "C:\\JTemp\\LTWEvaluationTool\\33A2BWikiResultSet.xml";
//        String gtRSFileFPath = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_A2B\\LTW2009_Topics\\33A2BWikiResultSet_OutIn_NODuplicated.xml";
        Hashtable<String, Vector<Vector<String>>> gtRSHT = getWikipediaGTResultSet(gtRSFileFPath);
        // =====================================================================
        for (String thisRunDir : runsDirV) {
            File thisRunDirF = new File(thisRunDir);
            if (thisRunDirF.isDirectory()) {
                File[] thisRunFileList = thisRunDirF.listFiles();
                for (File thisRunFile : thisRunFileList) {
                    String thisRunFileFPath = thisRunFile.getAbsolutePath();
                    if (thisRunFileFPath.endsWith(".xml")) {
                        log(thisRunFileFPath);
                        String thisRunFileFName = thisRunFile.getName();
                        String thisRunFileName = thisRunFileFName.substring(0, thisRunFileFName.lastIndexOf(".xml"));
                        // -----------------------------------------------------
                        // Get topicID, Anchor O_L (in order),
                        Hashtable<String, Vector<Vector<String[]>>> runDataHT = getA2BSubmissionData(thisRunFileFPath);
                        // -----------------------------------------------------
                        // print out CSV
//                        String targetFileFPath = thisRunDir + "\\" + thisRunFileName + ".csv";
//                        try {
//                            FileWriter fw = new FileWriter(targetFileFPath);
//                            Enumeration topicEnu = runDataHT.keys();
//                            while (topicEnu.hasMoreElements()) {
//                                String topicID = topicEnu.nextElement().toString();
//                                // ---------------------------------------------
//                                Vector<Vector<String>> thisTopicRSV = gtRSHT.get(topicID);
//                                Vector<String> thisTOutRSLinks = thisTopicRSV.elementAt(0);
//                                Vector<String> thisTInRSLinks = thisTopicRSV.elementAt(1);
//                                // ---------------------------------------------
//                                fw.write(topicID);
//                                fw.write("\n");
//                                // ---------------------------------------------
//                                Vector<Vector<String[]>> topicRunData = runDataHT.get(topicID);
//                                Vector<String[]> topicOutLinks = topicRunData.elementAt(0);
//                                for (String[] topicOutLinkSet : topicOutLinks) {
//                                    String thisBepFileID = topicOutLinkSet[3].trim();
//                                    String relMarker = "";
//                                    if (thisTOutRSLinks.contains(thisBepFileID)){
//                                        relMarker = "Got_It";
//                                    }
//                                    fw.write(topicOutLinkSet[0] + "," + topicOutLinkSet[1] + "," + topicOutLinkSet[2] + "," + topicOutLinkSet[3] + "," + relMarker);
//                                    fw.write("\n");
//                                }
//                                // ---------------------------------------------
//                                Vector<String[]> topicInLinks = topicRunData.elementAt(1);
//                                for (String[] topicInLinkSet : topicInLinks) {
//                                    String thisFAnchorFileID = topicInLinkSet[3].trim();
//                                    String relMarker = "";
//                                    if (thisTInRSLinks.contains(thisFAnchorFileID)){
//                                        relMarker = "Got_It";
//                                    }
//                                    fw.write(topicInLinkSet[0] + "," + topicInLinkSet[1] + "," + topicInLinkSet[2] + "," + topicInLinkSet[3] + "," + relMarker);
//                                    fw.write("\n");
//                                }
//                            }
//                            fw.close();
//                        } catch (IOException ex) {
//                            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
//                        }
                    }
                }
            }
        }
    }

    private static Hashtable<String, Vector<Vector<String[]>>> getA2BSubmissionData(String a2bRunsXmlFPath) {
        Hashtable<String, Vector<Vector<String[]>>> runDataHT = new Hashtable<String, Vector<Vector<String[]>>>();
        Vector<Vector<String[]>> topicOutInLinksV = new Vector<Vector<String[]>>();
        Vector<String[]> topicOutABLinks = new Vector<String[]>();
        Vector<String[]> topicInBALinks = new Vector<String[]>();
        // ---------------------------------------------------------------------
        VTDGen vg = new VTDGen();
        if (vg.parseFile(a2bRunsXmlFPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(a2bRunsXmlFPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);
                /**
                 * <topic file="10313" name="E. O. Wilson">
                 */
                Vector<String> topicIDV = new Vector<String>();
                String xPath = "/crosslink-submission/topic";
                ap.selectXPath(xPath);
                int i = -1;
                while ((i = ap.evalXPath()) != -1) {
                    int j = vn.getAttrVal("file");
                    int k = vn.getAttrVal("name");
                    if (j != -1) {
                        String topicIDTxt = vn.toRawString(j).toString();
                        if (!topicIDV.contains(topicIDTxt)) {
                            topicIDV.add(topicIDTxt);
                        }
                    }
                }
                // =============================================================
                // =============================================================
                Vector<String[]> anchorOLSA = null;
                Vector<String> bepOSA = null;
                for (String thisTID : topicIDV) {
                    topicOutInLinksV = new Vector<Vector<String[]>>();
                    // ---------------------------------------------------------
                    /**
                     * <outgoing>
                     * <anchor offset="13093" length="24" name="Harvard University Press">
                     * 	<tobep offset="1200">1251937</tobep>
                     * 	<tobep offset="52600">18426501</tobep>
                     * </anchor>
                     */
                    anchorOLSA = new Vector<String[]>();
                    String xPath1 = "/crosslink-submission/topic[@file='" + thisTID + "']/outgoing/anchor";
                    ap.selectXPath(xPath1);
                    int i1 = -1;
                    String anchorOffset = "";
                    String anchorLength = "";
                    while ((i1 = ap.evalXPath()) != -1) {
                        int j1 = vn.getAttrVal("offset");
                        int k1 = vn.getAttrVal("length");
                        anchorOffset = vn.toRawString(j1).toString();
                        anchorLength = vn.toRawString(k1).toString();
                        if (!anchorOLSA.contains(new String[]{anchorOffset, anchorLength})) {
                            anchorOLSA.add(new String[]{anchorOffset, anchorLength});
                        }
                    }
                    // ---------------------------------------------------------
                    topicOutABLinks = new Vector<String[]>();
                    for (String[] anchorOL : anchorOLSA) {
                        String anchorO = anchorOL[0];
                        String anchorL = anchorOL[1];
                        // -----------------------------------------------------
                        String xPath11 = "/crosslink-submission/topic[@file='" + thisTID + "']/outgoing/anchor[@offset='" + anchorO + "' and @length='" + anchorL + "']/tobep";
                        ap.selectXPath(xPath11);
                        int i11 = -1;
                        String bepOffset = "";
                        String bepFileID = "";
                        while ((i11 = ap.evalXPath()) != -1) {
                            int j11 = vn.getAttrVal("offset");
                            int k11 = vn.getText();
                            bepOffset = vn.toRawString(j11).toString();
                            bepFileID = vn.toRawString(k11).toString();
                            topicOutABLinks.add(new String[]{anchorO, anchorL, bepOffset, bepFileID});
                        }
                    }
                    topicOutInLinksV.add(topicOutABLinks);
                    // =========================================================
                    // =========================================================
                    /**
                     * <incoming>
                     * 	<bep offset="13000">
                     *      	<fromanchor offset="9894" length="1" file="1448737">E</fromanchor>
                     * 	</bep>
                     */
                    bepOSA = new Vector<String>();
                    String xPath2 = "/crosslink-submission/topic[@file='" + thisTID + "']/incoming/bep";
                    ap.selectXPath(xPath2);
                    int i2 = -1;
                    String bepOffset = "";
                    while ((i2 = ap.evalXPath()) != -1) {
                        int j2 = vn.getAttrVal("offset");
                        bepOffset = vn.toRawString(j2).toString();
                        if (!bepOSA.contains(bepOffset)) {
                            bepOSA.add(bepOffset);
                        }
                    }
                    // ---------------------------------------------------------
                    topicInBALinks = new Vector<String[]>();
                    for (String thisBepO : bepOSA) {
                        String bepO = thisBepO;
                        // -----------------------------------------------------
                        String xPath22 = "/crosslink-submission/topic[@file='" + thisTID + "']/incoming/bep[@offset='" + bepO + "']/fromanchor";
                        ap.selectXPath(xPath22);
                        int i22 = -1;
                        String fAnchorOffset = "";
                        String fAnchorLength = "";
                        String fAnchorFileID = "";
                        while ((i22 = ap.evalXPath()) != -1) {
                            int j22 = vn.getAttrVal("offset");
                            int k22 = vn.getAttrVal("length");
                            int l22 = vn.getAttrVal("file");
                            fAnchorOffset = vn.toRawString(j22).toString();
                            fAnchorLength = vn.toRawString(k22).toString();
                            fAnchorFileID = vn.toRawString(l22).toString();
                            topicInBALinks.add(new String[]{bepO, fAnchorOffset, fAnchorLength, fAnchorFileID});
                        }
                    }
                    topicOutInLinksV.add(topicInBALinks);
                    // =========================================================
                    runDataHT.put(thisTID, topicOutInLinksV);
                }
                // =============================================================
                xm.output(fos);
                fos.close();
            } catch (TranscodeException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return runDataHT;
    }

    private static Hashtable<String, Vector<Vector<String>>> getWikipediaGTResultSet(String gtRSFileFPath) {
        Hashtable<String, Vector<Vector<String>>> gtRSHT = new Hashtable<String, Vector<Vector<String>>>();
        Vector<Vector<String>> topicOutInLinksV = new Vector<Vector<String>>();
        Vector<String> topicOutLinks = new Vector<String>();
        Vector<String> topicInLinks = new Vector<String>();
        VTDGen vg = new VTDGen();
        if (vg.parseFile(gtRSFileFPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(gtRSFileFPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);

                Vector<String> topicIDV = new Vector<String>();
                String xPath = "/ltwResultsetType/ltw_Topic";
                ap.selectXPath(xPath);
                int i = -1;
                while ((i = ap.evalXPath()) != -1) {
                    int j = vn.getAttrVal("id");
                    if (j != -1) {
                        String topicIDTxt = vn.toRawString(j).toString();
                        if (!topicIDV.contains(topicIDTxt)) {
                            topicIDV.add(topicIDTxt);
                        } else {
                            log("Duplicated Topic ID: " + topicIDTxt);
                        }
                    }
                }
                // =============================================================
                // =============================================================
                for (String thisTID : topicIDV) {
                    topicOutInLinksV = new Vector<Vector<String>>();
                    topicOutLinks = new Vector<String>();
                    topicInLinks = new Vector<String>();
                    // ---------------------------------------------------------
                    String xPath1 = "/ltwResultsetType/ltw_Topic[@id='" + thisTID + "']/outgoingLinks/outLink";
                    ap.selectXPath(xPath1);
                    int i1 = -1;
                    String outLinkID = "";
                    while ((i1 = ap.evalXPath()) != -1) {
                        int j1 = vn.getText();
                        outLinkID = vn.toRawString(j1).toString().trim();
                        if (!topicOutLinks.contains(outLinkID)) {
                            topicOutLinks.add(outLinkID);
                        } else {
                            log(thisTID + " - Duplicated Outgoing Link ID: " + outLinkID);
                        }
                    }
                    topicOutInLinksV.add(topicOutLinks);
                    // ---------------------------------------------------------
                    String xPath2 = "/ltwResultsetType/ltw_Topic[@id='" + thisTID + "']/incomingLinks/inLink";
                    ap.selectXPath(xPath2);
                    int i2 = -1;
                    String inLinkID = "";
                    while ((i2 = ap.evalXPath()) != -1) {
                        int j2 = vn.getText();
                        inLinkID = vn.toRawString(j2).toString().trim();
                        if (!topicInLinks.contains(inLinkID)) {
                            topicInLinks.add(inLinkID);
                        } else {
                            log(thisTID + " - Duplicated Incoming Link ID: " + inLinkID);
                        }
                    }
                    topicOutInLinksV.add(topicInLinks);
                    // ---------------------------------------------------------
                    gtRSHT.put(thisTID, topicOutInLinksV);
                }
                // =============================================================
                xm.output(fos);
                fos.close();
            } catch (TranscodeException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return gtRSHT;
    }
    // </editor-fold>
    // =========================================================================
    // =========================================================================
    // <editor-fold defaultstate="collapsed" desc="Produce A2F GT Result Set">
    // topicID, V.add(new String(){offset, length})
    private static Hashtable<String, Vector<String[]>> topicAnchorOLHT = new Hashtable<String, Vector<String[]>>();
    // topicID_AO_L, V.add(new String(){bepOffset, linkID})
    private static Hashtable<String, Vector<String[]>> topicAOLLinksHT = new Hashtable<String, Vector<String[]>>();
//    private static void produceA2FGTResultSet(String topicXmlFileDir, String outgoingGTDir, String incomingGTDir) {
//        try {
//            // -----------------------------------------------------------------
//            Vector<String[]> topicPairsV = new Vector<String[]>();
//            File tpFile = new File(topicXmlFileDir);
//            if (tpFile.isDirectory()) {
//                File[] a2bXmlFileList = tpFile.listFiles();
//                for (File a2bXmlFile : a2bXmlFileList) {
//                    String a2bXmlFileFName = a2bXmlFile.getName();
//                    if (a2bXmlFileFName.endsWith(".xml")) {
//                        String a2bXmlFileID = a2bXmlFileFName.substring(0, a2bXmlFileFName.lastIndexOf(".xml"));
//                        log("a2bXmlFileID: " + a2bXmlFileID);
//                        topicPairsV.add(new String[]{a2bXmlFileID, ""});
//                    }
//                }
//            }
//            // -----------------------------------------------------------------
//            // topicID, V.add(new String(){offset, length})
//            topicAnchorOLHT = new Hashtable<String, Vector<String[]>>();
//            // topicID_AO_L, V.add(new String(){bepOffset, linkID})
//            topicAOLLinksHT = new Hashtable<String, Vector<String[]>>();
//            populateTopicIDAnchorOLLinks(outgoingGTDir);
//            // -----------------------------------------------------------------
//            // populate A2B Incoming GT Links
//            Hashtable<String, Vector<String>> topicIncomingLinksHT = new Hashtable<String, Vector<String>>();
//            // -----------------------------------------------------------------
//            /**
//             * <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
//             * <ltwResultsetType>
//             * <ltw_Topic name="Scurvy" id="28266">
//             * <outgoingLinks>
//             * <outLink>10843</outLink>
//             */
//            // produce RESULT SET XML
//            String targetDir = "C:\\Users\\Darren\\Desktop\\INEX_2009\\LTW2009_TOPICS_A2B\\";
//            String rsXmlFPath = targetDir + "33A2FWikiResultSet_OutIn.xml";
//            FileWriter fw = new FileWriter(new File(rsXmlFPath));
//            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
//            fw.write("\n");
//            fw.write("<ltwResultsetType>");
//            fw.write("\n");
//
//            for (String[] topicPairSA : topicPairsV) {
//                String topicID = topicPairSA[0];
//                String topicName = topicPairSA[1];
//                fw.write("<ltw_Topic name=\"" + topicName + "\" id=\"" + topicID + "\">");
//                fw.write("\n");
//                // -------------------------------------------------------------
//                // Outgoing Links
//                fw.write("<outgoingLinks>");
//                fw.write("\n");
//                Vector<String> thisOutLinkIDV = topicOutgoingLinksHT.get(topicID);
//                for (String thisOutLinkID : thisOutLinkIDV) {
//                    fw.write("<outLink>" + thisOutLinkID + "</outLink>");
//                    fw.write("\n");
//                }
//                fw.write("</outgoingLinks>");
//                fw.write("\n");
//                // -------------------------------------------------------------
//                // Incoming Links
//                fw.write("<incomingLinks>");
//                fw.write("\n");
//                Vector<String> thisInLinkIDV = topicIncomingLinksHT.get(topicID);
//                for (String thisInLinkID : thisInLinkIDV) {
//                    fw.write("<inLink>" + thisInLinkID + "</inLink>");
//                    fw.write("\n");
//                }
//                fw.write("</incomingLinks>");
//                fw.write("\n");
//                // -------------------------------------------------------------
//                fw.write("</ltw_Topic>");
//                fw.write("\n");
//            }
//            fw.write("</ltwResultsetType>");
//            fw.close();
//        } catch (IOException ex) {
//            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//    private static void populateTopicIDAnchorOLLinks(String outgoingGTDir) {
//        /**
//         * <anchor arel="0" aname="Apple" aoffset="2" alength="5">
//         *    <subanchor sarel="0" saname="Apple" saoffset="2" salength="5">
//         *       <tobep tbrel="0" timein="" timeout="" tboffset="-1" tbstartp="-1">856</tobep>
//         *    </subanchor>
//         * </anchor>
//         */
//        // topicAnchorOLHT = new Hashtable<String, Vector<String[]>>();
//        // topicAOLLinksHT = new Hashtable<String, Vector<String[]>>();
//        Vector<String[]> anchorOLName = new Vector<String[]>();
//        File outgoingGTXmlDir = new File(outgoingGTDir);
//        if (outgoingGTXmlDir.isDirectory()) {
//            File[] outgoingGTXmlList = outgoingGTXmlDir.listFiles();
//            for (File outgoingGTXmlFile : outgoingGTXmlList) {
//                String outgoingGTXmlFPath = outgoingGTXmlFile.getAbsolutePath();
//                if (outgoingGTXmlFPath.endsWith(".xml")) {
//                    String outgoingGTXmlFName = outgoingGTXmlFile.getName();
//                    String outgoingGTXmlName = outgoingGTXmlFName.substring(0, outgoingGTXmlFName.lastIndexOf("GTPool.xml"));
//
//                    VTDGen vg = new VTDGen();
//                    if (vg.parseFile(outgoingGTXmlFPath, true)) {
//                        try {
//                            FileOutputStream fos = null;
//                            VTDNav vn = vg.getNav();
//                            File fo = new File(outgoingGTXmlFPath);
//                            fos = new FileOutputStream(fo);
//                            AutoPilot ap = new AutoPilot(vn);
//                            XMLModifier xm = new XMLModifier(vn);
//                            String xPath = "/anchor";
//                            ap.selectXPath(xPath);
//                            int i = -1;
//                            while ((i = ap.evalXPath()) != -1) {
//                                int j = vn.getAttrVal("aoffset");
//                                int k = vn.getAttrVal("alength");
//                                int l = vn.getAttrVal("aname");
//
//
//                            }
//                            // <editor-fold defaultstate="collapsed" desc="Populate A2F GT Data">
//                            // </editor-fold>
//                        } catch (XPathEvalException ex) {
//                            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
//                        } catch (NavException ex) {
//                            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
//                        } catch (XPathParseException ex) {
//                            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
//                        } catch (ModifyException ex) {
//                            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
//                        } catch (FileNotFoundException ex) {
//                            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    }
//                }
//            }
//
//            VTDGen vg = new VTDGen();
//            if (vg.parseFile(a2bRunsXmlFPath, true)) {
//                FileOutputStream fos = null;
//                try {
//                    VTDNav vn = vg.getNav();
//                    File fo = new File(a2bRunsXmlFPath);
//                    fos = new FileOutputStream(fo);
//
//                    AutoPilot ap = new AutoPilot(vn);
//                    XMLModifier xm = new XMLModifier(vn);
//
//                    Vector<String> topicIDV = new Vector<String>();
//                    String xPath = "/crosslink-submission/topic";
//                    ap.selectXPath(xPath);
//                    int i = -1;
//                    while ((i = ap.evalXPath()) != -1) {
//                        int j = vn.getAttrVal("file");
//                        int k = vn.getAttrVal("name");
//                        if (j != -1) {
//                            String topicIDTxt = vn.toRawString(j).toString();
//                            topicIDV.add(topicIDTxt);
//                            String topicNameTxt = vn.toRawString(k).toString();
//                            if (!topicIDNameHT.containsKey(topicIDTxt)) {
//                                topicIDNameHT.put(topicIDTxt, topicNameTxt);
//                            }
////                        log("topicID-Name: " + topicIDTxt + " - " + topicNameTxt);
//                        }
//                    }
//                    // =============================================================
//                    // =============================================================
//                    Vector<String[]> anchorOLNVSA = null;
//                    Vector<String[]> bepOVSA = null;
//                    for (String thisTID : topicIDV) {
//                        anchorOLNVSA = new Vector<String[]>();
//                        bepOVSA = new Vector<String[]>();
//                        // ---------------------------------------------------------
//                        String xPath1 = "/crosslink-submission/topic[@file='" + thisTID + "']/outgoing/anchor";
//                        ap.selectXPath(xPath1);
//                        int i1 = -1;
//                        String anchorOffset = "";
//                        String anchorLength = "";
//                        String anchorName = "";
//                        while ((i1 = ap.evalXPath()) != -1) {
//                            int j1 = vn.getAttrVal("offset");
//                            int k1 = vn.getAttrVal("length");
//                            int l1 = vn.getAttrVal("name");
//                            anchorOffset = vn.toRawString(j1).toString();
//                            anchorLength = vn.toRawString(k1).toString();
//                            anchorName = vn.toRawString(l1).toString();
//                            anchorOLNVSA.add(new String[]{anchorOffset, anchorLength, anchorName});
////                        log(anchorOffset + " - " + anchorLength + " - " + anchorName);
//                        }
//                        topicIDAnchorOLHT.put(thisTID, anchorOLNVSA);
//                        // ---------------------------------------------------------
//                        String xPath2 = "/crosslink-submission/topic[@file='" + thisTID + "']/incoming/bep";
//                        ap.selectXPath(xPath2);
//                        int i2 = -1;
//                        String bepOffset = "";
//                        while ((i2 = ap.evalXPath()) != -1) {
//                            int j2 = vn.getAttrVal("offset");
//                            bepOffset = vn.toRawString(j2).toString();
//                            bepOVSA.add(new String[]{bepOffset});
////                        log(bepOffset);
//                        }
//                        topicIDBepOHT.put(thisTID, bepOVSA);
//                    }
//                    // =============================================================
//                    // =============================================================
//                    // tAnchorOutInDataV : Outgoing Hashtable & Incoming Hashtable Data
//                    Vector<Hashtable<String, Vector<String[]>>> tAnchorOutInDataV = null;
//                    // anchorO_L, V.add(new String[]{})
//                    Hashtable<String, Vector<String[]>> linkKeyData = null;
//                    for (String thisTID : topicIDV) {
//                        tAnchorOutInDataV = new Vector<Hashtable<String, Vector<String[]>>>();
//                        // ---------------------------------------------------------
//                        // Outgoing
//                        linkKeyData = new Hashtable<String, Vector<String[]>>();
//                        Vector<String[]> tIDAnchorOLNameV = topicIDAnchorOLHT.get(thisTID);
//                        for (String[] tIDAnchorOLName : tIDAnchorOLNameV) {
//                            String anchorOffset = tIDAnchorOLName[0];
//                            String anchorLength = tIDAnchorOLName[1];
//                            String anchorKey = anchorOffset + "_" + anchorLength;
//                            String xPath3 = "/crosslink-submission/topic[@file='" + thisTID + "']/outgoing/anchor[@offset='" + anchorOffset + "' and @length='" + anchorLength + "']/tobep";
//                            ap.selectXPath(xPath3);
//                            int i1 = -1;
//                            String bepOffset = "";
//                            String bepFileID = "";
//                            Vector<String[]> linkData = new Vector<String[]>();
//                            while ((i1 = ap.evalXPath()) != -1) {
//                                int j1 = vn.getAttrVal("offset");
//                                int k1 = vn.getText();
//                                bepOffset = vn.toRawString(j1).toString();
//                                bepFileID = vn.toRawString(k1).toString();
//                                linkData.add(new String[]{bepOffset, bepFileID});
//                            }
//                            linkKeyData.put(anchorKey, linkData);
//                        }
//                        tAnchorOutInDataV.add(linkKeyData);
//                        // ---------------------------------------------------------
//                        // Incoming
//                        linkKeyData = new Hashtable<String, Vector<String[]>>();
//                        Vector<String[]> tIDBepOffsetV = topicIDBepOHT.get(thisTID);
//                        int bepOSupCounter = 0;
//                        for (String[] tIDBepOffset : tIDBepOffsetV) {
//                            String bepOffset = tIDBepOffset[0].trim();
//                            String bepONum = tIDBepOffset[0].trim() + "_" + String.valueOf(bepOSupCounter);
//                            bepOSupCounter++;
//                            String xPath3 = "/crosslink-submission/topic[@file='" + thisTID + "']/incoming/bep[@offset='" + bepOffset + "']/fromanchor";
//                            ap.selectXPath(xPath3);
//                            int i1 = -1;
//                            String linkAOffset = "";
//                            String linkALength = "";
//                            String linkAFileID = "";
//                            String linkAName = "";
//                            Vector<String[]> linkData = new Vector<String[]>();
//                            while ((i1 = ap.evalXPath()) != -1) {
//                                int j1 = vn.getAttrVal("offset");
//                                int k1 = vn.getAttrVal("length");
//                                int l1 = vn.getAttrVal("file");
//                                int m1 = vn.getText();
//                                linkAOffset = vn.toRawString(j1).toString();
//                                linkALength = vn.toRawString(k1).toString();
//                                linkAFileID = vn.toRawString(l1).toString();
//                                linkAName = vn.toRawString(m1).toString();
//                                linkData.add(new String[]{linkAOffset, linkALength, linkAFileID, linkAName});
//                            }
////                        if (linkKeyData.containsKey(bepOffset)) {
////                            log("Duplicate Incoming BEP Offset: " + bepOffset);
////                        } else {
//                            linkKeyData.put(bepONum, linkData);
////                        }
//                        }
//                        tAnchorOutInDataV.add(linkKeyData);
//                        // ---------------------------------------------------------
//                        topicIDOutInDataHT.put(thisTID, tAnchorOutInDataV);
//                    }
//                    // =============================================================
//                    xm.output(fos);
//                    fos.close();
//                } catch (TranscodeException ex) {
//                    Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (IOException ex) {
//                    Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (XPathEvalException ex) {
//                    Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (NavException ex) {
//                    Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (XPathParseException ex) {
//                    Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (ModifyException ex) {
//                    Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }
//        // </editor-fold>
    // =========================================================================
    // <editor-fold defaultstate="collapsed" desc="Convert A2B Run To Eval XML">
    private static Vector<String> xmlRunProperty = null;
    // Get Topic ID & Name Pair
    private static Hashtable<String, String> topicIDNameHT = null;
    // Get Topic ID & its Anchor OL in order: new String[]{}
    private static Hashtable<String, Vector<String[]>> topicIDAnchorOLHT = null;
    private static Hashtable<String, Vector<String[]>> topicIDBepOHT = null;
    // Get Topic ID & its Outgoing/Incoming Data
    private static Hashtable<String, Vector<Hashtable<String, Vector<String[]>>>> topicIDOutInDataHT = null;

    private static void convertA2BRunToEvalXML(Vector<String> a2bRunsDirVS) {
        for (String a2bRunsDir : a2bRunsDirVS) {
            File a2bRunsDirF = new File(a2bRunsDir);
            if (a2bRunsDirF.isDirectory()) {
                File[] a2bRunsList = a2bRunsDirF.listFiles();
                for (File a2bRunsXml : a2bRunsList) {
                    String a2bRunsXmlFPath = a2bRunsXml.getAbsolutePath();
                    if (a2bRunsXmlFPath.endsWith(".xml")) {
                        String a2bRunsXmlFName = a2bRunsXml.getName();
                        String a2bRunsXmlName = a2bRunsXmlFName.substring(0, a2bRunsXmlFName.lastIndexOf(".xml"));
                        // -----------------------------------------------------
                        // Get RUN Properties:
                        // participant-id, run-id, task
                        xmlRunProperty = new Vector<String>();
                        populateABRunProperty(a2bRunsXmlFPath);
                        // CHECKING
//                        for (String property : xmlRunProperty) {
//                            log(property);
//                        }
                        // -----------------------------------------------------
                        // Get RUN Data
                        topicIDNameHT = new Hashtable<String, String>();
                        topicIDAnchorOLHT = new Hashtable<String, Vector<String[]>>();
                        topicIDBepOHT = new Hashtable<String, Vector<String[]>>();
                        topicIDOutInDataHT = new Hashtable<String, Vector<Hashtable<String, Vector<String[]>>>>();
                        populateSubmissionRunData(a2bRunsXmlFPath);
                        // -----------------------------------------------------
                        // re-produce submission run
//                        String targetDir = a2bRunsDirF.getAbsolutePath() + "_EvalForm";
                        String targetDir = a2bRunsDirF.getAbsolutePath() + "_EvalOnly50";
                        File targetDirF = new File(targetDir);
                        if (!targetDirF.exists()) {
                            targetDirF.mkdir();
                        }
//                        String targetFilePath = targetDir + "\\" + a2bRunsXmlName + "_EvalF.xml";
                        String targetFilePath = targetDir + "\\" + a2bRunsXmlName + "_EvalF50.xml";
                        produceNewSubmissionRun(targetFilePath);
                    }
                }
            }
        }
    }

    private static void produceNewSubmissionRun(String targetFilePath) {
        /**
         * <details><machine><cpu>AMD Athlon64 3500+</cpu><speed>2.2GHz</speed><cores></cores><hyperthreads></hyperthreads><memory> 2GB</memory></machine><time>1160.26 seconds</time></details>
         * <description>Anchor to BEP with cutoff and ER</description>
         * <collections><collection>Wikipedia</collection></collections>
         */
        try {
            log("TargetFile: " + targetFilePath);
            FileWriter fw = new FileWriter(new File(targetFilePath));
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
            fw.write("\n");
            fw.write("<crosslink-submission participant-id=\"" + xmlRunProperty.elementAt(0) + "\" run-id=\"" + xmlRunProperty.elementAt(1) + "\" task=\"" + xmlRunProperty.elementAt(2) + "\" format=\"FOL\">");
            fw.write("\n");
            fw.write("<details><machine><cpu>AMD Athlon64 3500+</cpu><speed>2.2GHz</speed><cores></cores><hyperthreads></hyperthreads><memory>2GB</memory></machine><time>1160.26 seconds</time></details>");
            fw.write("\n");
            fw.write("<description>Anchor to BEP with cutoff and ER</description>");
            fw.write("\n");
            fw.write("<collections><collection>Wikipedia</collection></collections>");
            fw.write("\n");
            for (String topicID : topicIDNameHT.keySet()) {
                String topicFile = topicID + ".xml";
                String topicName = topicIDNameHT.get(topicID);
                // -------------------------------------------------------------
                fw.write("<topic name=\"" + topicName + "\" file=\"" + topicFile + "\">");
                fw.write("\n");
                // -------------------------------------------------------------
                // Outgoing Links
                /**
                 * <link>
                 * <anchor><file>100011.xml</file><offset>1288</offset><length>25</length></anchor>
                 * <linkto><file>1085526.xml</file><bep>0</bep></linkto>
                 * </link>
                 */
                fw.write("<outgoing>");
                fw.write("\n");
                // Format:
                // AnchorOffset_Length, link bep Offset FileID
                log(topicIDOutInDataHT.get(topicID).size());
                Hashtable<String, Vector<String[]>> outEDataHT = topicIDOutInDataHT.get(topicID).elementAt(0);
                log("outEDataHT.size(): " + outEDataHT.size());
                for (String anchorOL : outEDataHT.keySet()) {
                    String[] anchorOLSet = anchorOL.split("_");
                    fw.write("<link>");
                    fw.write("\n");
                    fw.write("<anchor><file>" + topicFile + "</file><offset>" + anchorOLSet[0] + "</offset><length>" + anchorOLSet[1] + "</length></anchor>");
                    fw.write("\n");
                    for (String[] linkOID : outEDataHT.get(anchorOL)) {
                        fw.write("<linkto><file>" + linkOID[1] + ".xml</file><bep>" + linkOID[0] + "</bep></linkto>");
                        fw.write("\n");
                    }
                    fw.write("</link>");
                    fw.write("\n");
                }
                fw.write("</outgoing>");
                fw.write("\n");
                // -------------------------------------------------------------
                // Incoming Links
                /**
                 * <link>
                 * <anchor><file>98089.xml</file><offset>15</offset><length>8</length></anchor>
                 * <linkto><file>100011.xml</file><bep>0</bep></linkto>
                 * </link>
                 */
                fw.write("<incoming>");
                fw.write("\n");
                // Format:
                // BEP Offset, link anchor Offset FileID
                Hashtable<String, Vector<String[]>> inEDataHT = topicIDOutInDataHT.get(topicID).elementAt(1);
                log("inEDataHT.size(): " + inEDataHT.size());
                for (String bepONum : inEDataHT.keySet()) {
                    String[] bepOSet = bepONum.split("_");
                    log("inEDataHT.get(bepONum): " + inEDataHT.get(bepONum).size());
                    for (String[] linkOLIDName : inEDataHT.get(bepONum)) {
                        fw.write("<link>");
                        fw.write("\n");
                        fw.write("<anchor><file>" + linkOLIDName[2] + ".xml</file><offset>" + linkOLIDName[0] + "</offset><length>" + linkOLIDName[1] + "</length></anchor>");
                        fw.write("\n");
                        fw.write("<linkto><file>" + topicFile + "</file><bep>" + bepOSet[0] + "</bep></linkto>");
                        fw.write("\n");
                        fw.write("</link>");
                        fw.write("\n");
                    }
                }
                fw.write("</incoming>");
                fw.write("\n");
                // -------------------------------------------------------------
                fw.write("</topic>");
                fw.write("\n");
            }
            fw.write("</crosslink-submission>");
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void populateSubmissionRunData(String a2bRunsXmlFPath) {
        VTDGen vg = new VTDGen();
        if (vg.parseFile(a2bRunsXmlFPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(a2bRunsXmlFPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);

                Vector<String> topicIDV = new Vector<String>();
                String xPath = "/crosslink-submission/topic";
                ap.selectXPath(xPath);
                int i = -1;
                while ((i = ap.evalXPath()) != -1) {
                    int j = vn.getAttrVal("file");
                    int k = vn.getAttrVal("name");
                    if (j != -1) {
                        String topicIDTxt = vn.toRawString(j).toString();
                        topicIDV.add(topicIDTxt);
                        String topicNameTxt = vn.toRawString(k).toString();
                        if (!topicIDNameHT.containsKey(topicIDTxt)) {
                            topicIDNameHT.put(topicIDTxt, topicNameTxt);
                        }
//                        log("topicID-Name: " + topicIDTxt + " - " + topicNameTxt);
                    }
                }
                // =============================================================
                // =============================================================
                Vector<String[]> anchorOLNVSA = null;
                Vector<String[]> bepOVSA = null;
                for (String thisTID : topicIDV) {
                    anchorOLNVSA = new Vector<String[]>();
                    bepOVSA = new Vector<String[]>();
                    // ---------------------------------------------------------
                    String xPath1 = "/crosslink-submission/topic[@file='" + thisTID + "']/outgoing/anchor";
                    ap.selectXPath(xPath1);
                    int i1 = -1;
                    String anchorOffset = "";
                    String anchorLength = "";
                    String anchorName = "";
                    while ((i1 = ap.evalXPath()) != -1) {
                        int j1 = vn.getAttrVal("offset");
                        int k1 = vn.getAttrVal("length");
                        int l1 = vn.getAttrVal("name");
                        anchorOffset = vn.toRawString(j1).toString();
                        anchorLength = vn.toRawString(k1).toString();
                        anchorName = vn.toRawString(l1).toString();
                        anchorOLNVSA.add(new String[]{anchorOffset, anchorLength, anchorName});
//                        log(anchorOffset + " - " + anchorLength + " - " + anchorName);
                    }
                    topicIDAnchorOLHT.put(thisTID, anchorOLNVSA);
                    // ---------------------------------------------------------
                    String xPath2 = "/crosslink-submission/topic[@file='" + thisTID + "']/incoming/bep";
                    ap.selectXPath(xPath2);
                    int i2 = -1;
                    String bepOffset = "";
                    while ((i2 = ap.evalXPath()) != -1) {
                        int j2 = vn.getAttrVal("offset");
                        bepOffset = vn.toRawString(j2).toString();
                        if (!bepOVSA.contains(new String[]{bepOffset})) {
                            bepOVSA.add(new String[]{bepOffset});
                        }
//                        log(bepOffset);
                    }
                    topicIDBepOHT.put(thisTID, bepOVSA);
                }
                // =============================================================
                // =============================================================
                // tAnchorOutInDataV : Outgoing Hashtable & Incoming Hashtable Data
                Vector<Hashtable<String, Vector<String[]>>> tAnchorOutInDataV = null;
                // anchorO_L, V.add(new String[]{})
                Hashtable<String, Vector<String[]>> linkKeyData = null;
                for (String thisTID : topicIDV) {
                    tAnchorOutInDataV = new Vector<Hashtable<String, Vector<String[]>>>();
                    // ---------------------------------------------------------
                    // Outgoing
                    linkKeyData = new Hashtable<String, Vector<String[]>>();
                    Vector<String[]> tIDAnchorOLNameV = topicIDAnchorOLHT.get(thisTID);
                    for (String[] tIDAnchorOLName : tIDAnchorOLNameV) {
                        String anchorOffset = tIDAnchorOLName[0];
                        String anchorLength = tIDAnchorOLName[1];
                        String anchorKey = anchorOffset + "_" + anchorLength;
                        String xPath3 = "/crosslink-submission/topic[@file='" + thisTID + "']/outgoing/anchor[@offset='" + anchorOffset + "' and @length='" + anchorLength + "']/tobep";
                        ap.selectXPath(xPath3);
                        int i1 = -1;
                        String bepOffset = "";
                        String bepFileID = "";
                        Vector<String[]> linkData = new Vector<String[]>();
                        while ((i1 = ap.evalXPath()) != -1) {
                            int j1 = vn.getAttrVal("offset");
                            int k1 = vn.getText();
                            bepOffset = vn.toRawString(j1).toString();
                            bepFileID = vn.toRawString(k1).toString();
                            linkData.add(new String[]{bepOffset, bepFileID});
                        }
                        linkKeyData.put(anchorKey, linkData);
                    }
                    tAnchorOutInDataV.add(linkKeyData);
                    // ---------------------------------------------------------
                    // Incoming
                    linkKeyData = new Hashtable<String, Vector<String[]>>();
                    Vector<String[]> tIDBepOffsetV = topicIDBepOHT.get(thisTID);
                    int bepOSupCounter = 0;
                    for (String[] tIDBepOffset : tIDBepOffsetV) {
                        String bepOffset = tIDBepOffset[0].trim();
                        String bepONum = tIDBepOffset[0].trim() + "_" + String.valueOf(bepOSupCounter);
                        bepOSupCounter++;
                        String xPath3 = "/crosslink-submission/topic[@file='" + thisTID + "']/incoming/bep[@offset='" + bepOffset + "']/fromanchor";
                        ap.selectXPath(xPath3);
                        int i1 = -1;
                        String linkAOffset = "";
                        String linkALength = "";
                        String linkAFileID = "";
                        String linkAName = "";
                        Vector<String[]> linkData = new Vector<String[]>();
                        while ((i1 = ap.evalXPath()) != -1) {
                            int j1 = vn.getAttrVal("offset");
                            int k1 = vn.getAttrVal("length");
                            int l1 = vn.getAttrVal("file");
                            int m1 = vn.getText();
                            linkAOffset = vn.toRawString(j1).toString();
                            linkALength = vn.toRawString(k1).toString();
                            linkAFileID = vn.toRawString(l1).toString();
                            linkAName = vn.toRawString(m1).toString();
                            linkData.add(new String[]{linkAOffset, linkALength, linkAFileID, linkAName});
                        }
//                        if (linkKeyData.containsKey(bepOffset)) {
//                            log("Duplicate Incoming BEP Offset: " + bepOffset);
//                        } else {
                        linkKeyData.put(bepONum, linkData);
//                        }
                    }
                    tAnchorOutInDataV.add(linkKeyData);
                    // ---------------------------------------------------------
                    topicIDOutInDataHT.put(thisTID, tAnchorOutInDataV);
                }
                // =============================================================
                xm.output(fos);
                fos.close();
            } catch (TranscodeException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void populateABRunProperty(String thisRunFPath) {
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(thisRunFPath));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, xmlUnicode);
            InputSource inputSource = new InputSource(inputStreamReader);

            // Parse the XML File
            DOMParser parser = new DOMParser();
            parser.parse(inputSource);
            Document myDoc = parser.getDocument();

            getABRunProperty(myDoc);

            fileInputStream.close();
            inputStreamReader.close();
        } catch (SAXException ex) {
            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void getABRunProperty(Node node) {
        switch (node.getNodeType()) {
            case Node.DOCUMENT_NODE:
                // recurse on each child
                NodeList nodes = node.getChildNodes();
                if (nodes != null) {
                    for (int i = 0; i < nodes.getLength(); i++) {
                        getABRunProperty(nodes.item(i));
                    }
                }
                break;
            case Node.ELEMENT_NODE:
                String element = node.getNodeName();
                // =============================================================
                // Start of Tag: -----------------------------------------------
                if (element.equals("crosslink-submission")) {
                    isRootFlag = true;
                }
                // =============================================================
                NamedNodeMap attributes = node.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node current = attributes.item(i);
                    if (isRootFlag && current.getNodeName().equals("participant-id")) {
                        String parID = current.getNodeValue();
                        xmlRunProperty.add(parID);
                    } else if (isRootFlag && current.getNodeName().equals("run-id")) {
                        String runID = current.getNodeValue();
                        xmlRunProperty.add(runID);
                    } else if (isRootFlag && current.getNodeName().equals("task")) {
                        String task = current.getNodeValue();
                        xmlRunProperty.add(task);
                    }
                }
                if (xmlRunProperty.size() == 3 && isRootFlag) {
                    isRootFlag = false;
                    return;
                }
                // =============================================================
                // Recurse each child: e.g. TEXT ---------------------------
                NodeList children = node.getChildNodes();
                if (children != null) {
                    for (int i = 0; i < children.getLength(); i++) {
                        getABRunProperty(children.item(i));
                    }
                }
                break;
            case Node.TEXT_NODE:
                break;
        }
    }
    // </editor-fold>
    // =========================================================================
    // <editor-fold defaultstate="collapsed" desc="produce A2B GT Result Set">

    private static void produceA2BGTResultSet(String topicXmlFileDir, String outgoingGTDir, String incomingGTDir) {
        try {
            // -----------------------------------------------------------------
            Vector<String[]> topicPairsV = new Vector<String[]>();
            File tpFile = new File(topicXmlFileDir);
            if (tpFile.isDirectory()) {
                File[] a2bXmlFileList = tpFile.listFiles();
                for (File a2bXmlFile : a2bXmlFileList) {
                    String a2bXmlFileFName = a2bXmlFile.getName();
                    if (a2bXmlFileFName.endsWith(".xml")) {
                        String a2bXmlFileID = a2bXmlFileFName.substring(0, a2bXmlFileFName.lastIndexOf(".xml"));
                        log("a2bXmlFileID: " + a2bXmlFileID);
                        topicPairsV.add(new String[]{a2bXmlFileID, ""});
                    }
                }
            }
            // -----------------------------------------------------------------
            Vector<String[]> topicIDNamePairsV = new Vector<String[]>();
            String topicFileDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_A2B\\LTW2009_Topics\\";
            Vector<String> topicLinkIDS = null;
            // populate A2B Outgoing GT Links
            Hashtable<String, Vector<String>> topicOutgoingLinksHT = new Hashtable<String, Vector<String>>();
            for (String[] topicPairSA : topicPairsV) {
                String topicID = topicPairSA[0];
                String topicName = topicPairSA[1];
                topicLinkIDS = new Vector<String>();
                String topicGTFPath = outgoingGTDir + topicID + ".xml.txt";
                FileInputStream fis = new FileInputStream(topicGTFPath);
                DataInputStream dis = new DataInputStream(fis);
                BufferedReader br2 = new BufferedReader(new InputStreamReader(dis));
                int lineCounter = 0;
                String strLine2;
                while ((strLine2 = br2.readLine()) != null) {
                    if (lineCounter == 0) {
                        String thisTName = strLine2.trim();
                        topicIDNamePairsV.add(new String[]{topicID, thisTName});
                    } else if (lineCounter % 2 == 0) {
                        log("this Line: " + strLine2);
                        String thisLinkID = strLine2.trim();
                        log("out LinkID: " + thisLinkID);
                        if (!topicLinkIDS.contains(thisLinkID)) {
                            topicLinkIDS.add(thisLinkID);
                        }
                    }
                    lineCounter++;
                }
                topicOutgoingLinksHT.put(topicID, topicLinkIDS);
            }
            // -----------------------------------------------------------------
            // populate A2B Incoming GT Links
            Hashtable<String, Vector<String>> topicIncomingLinksHT = new Hashtable<String, Vector<String>>();
            for (String[] topicPairSA : topicPairsV) {
                String topicID = topicPairSA[0];
                String topicName = topicPairSA[1];
                topicLinkIDS = new Vector<String>();
                String topicGTFPath = incomingGTDir + topicID + "_gtIncoming.txt";
                FileInputStream fis = new FileInputStream(topicGTFPath);
                DataInputStream dis = new DataInputStream(fis);
                BufferedReader br2 = new BufferedReader(new InputStreamReader(dis));
                String strLine2;
                while ((strLine2 = br2.readLine()) != null) {
                    log("this Line: " + strLine2);
                    String thisLinkID = strLine2.trim().substring(0, strLine2.trim().lastIndexOf("_links"));
                    log("in LinkID: " + thisLinkID);
                    if (!topicLinkIDS.contains(thisLinkID)) {
                        topicLinkIDS.add(thisLinkID);
                    }
                }
                topicIncomingLinksHT.put(topicID, topicLinkIDS);
            }
            // -----------------------------------------------------------------
            /**
             * <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
             * <ltwResultsetType>
             * <ltw_Topic name="Scurvy" id="28266">
             * <outgoingLinks>
             * <outLink>10843</outLink>
             */
            // produce RESULT SET XML
            String rsXmlFPath = topicFileDir + "33A2BWikiResultSet_OutIn_NODuplicated.xml";
            FileWriter fw = new FileWriter(new File(rsXmlFPath));
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
            fw.write("\n");
            fw.write("<ltwResultsetType>");
            fw.write("\n");

            for (String[] topicPairSA : topicIDNamePairsV) {
                String topicID = topicPairSA[0];
                String topicName = topicPairSA[1];
                fw.write("<ltw_Topic name=\"" + topicName + "\" id=\"" + topicID + "\">");
                fw.write("\n");
                // -------------------------------------------------------------
                // Outgoing Links
                fw.write("<outgoingLinks>");
                fw.write("\n");
                Vector<String> thisOutLinkIDV = topicOutgoingLinksHT.get(topicID);
                for (String thisOutLinkID : thisOutLinkIDV) {
                    fw.write("<outLink>" + thisOutLinkID + "</outLink>");
                    fw.write("\n");
                }
                fw.write("</outgoingLinks>");
                fw.write("\n");
                // -------------------------------------------------------------
                // Incoming Links
                fw.write("<incomingLinks>");
                fw.write("\n");
                Vector<String> thisInLinkIDV = topicIncomingLinksHT.get(topicID);
                for (String thisInLinkID : thisInLinkIDV) {
                    fw.write("<inLink>" + thisInLinkID + "</inLink>");
                    fw.write("\n");
                }
                fw.write("</incomingLinks>");
                fw.write("\n");
                // -------------------------------------------------------------
                fw.write("</ltw_Topic>");
                fw.write("\n");
            }

            fw.write("</ltwResultsetType>");
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    // </editor-fold>
    // =========================================================================
    // <editor-fold defaultstate="collapsed" desc="produce F2F GT Result Set">

    private static void produceF2FGTResultSet(String topicFileFPath, String outgoingGTDir, String incomingGTDir) {
        try {
            // -----------------------------------------------------------------
            Vector<String[]> topicPairsV = new Vector<String[]>();
            File tpFile = new File(topicFileFPath);
            FileInputStream fstream = new FileInputStream(topicFileFPath);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] thisLineSet = strLine.split(" : ");
                String topicID = thisLineSet[1].trim().substring(0, thisLineSet[1].trim().lastIndexOf(".xml"));
                if (isInNumber(topicID)) {
                    topicPairsV.add(new String[]{topicID, thisLineSet[2]});
//                    log(thisLineSet[0]);
                } else {
                    log("Invalid Topic ID: " + topicID + " at line: " + strLine);
                }
            }
            fstream.close();
            // -----------------------------------------------------------------
            String topicFileDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\LTW2009_F2FTOPICS\\";
            Vector<String> topicLinkIDS = null;
            // populate F2F Outgoing GT Links
            Hashtable<String, Vector<String>> topicOutgoingLinksHT = new Hashtable<String, Vector<String>>();
            for (String[] topicPairSA : topicPairsV) {
                String topicID = topicPairSA[0];
                String topicName = topicPairSA[1];
                topicLinkIDS = new Vector<String>();
                String topicGTFPath = outgoingGTDir + topicID + "_F2FGT.txt";
                FileInputStream fis = new FileInputStream(topicGTFPath);
                DataInputStream dis = new DataInputStream(fis);
                BufferedReader br2 = new BufferedReader(new InputStreamReader(dis));
                String strLine2;
                while ((strLine2 = br2.readLine()) != null) {
                    log("this Line: " + strLine2);
                    String thisLinkID = strLine2.trim().substring(strLine2.trim().lastIndexOf("/") + 1, strLine2.trim().length());
                    log("out LinkID: " + thisLinkID);
                    topicLinkIDS.add(thisLinkID);
                }
                topicOutgoingLinksHT.put(topicID, topicLinkIDS);
            }
            // -----------------------------------------------------------------
            // populate F2F Incoming GT Links
            Hashtable<String, Vector<String>> topicIncomingLinksHT = new Hashtable<String, Vector<String>>();
            for (String[] topicPairSA : topicPairsV) {
                String topicID = topicPairSA[0];
                String topicName = topicPairSA[1];
                topicLinkIDS = new Vector<String>();
                String topicGTFPath = incomingGTDir + topicID + "_gtIncoming.txt";
                FileInputStream fis = new FileInputStream(topicGTFPath);
                DataInputStream dis = new DataInputStream(fis);
                BufferedReader br2 = new BufferedReader(new InputStreamReader(dis));
                String strLine2;
                while ((strLine2 = br2.readLine()) != null) {
                    log("this Line: " + strLine2);
                    String thisLinkID = strLine2.trim().substring(0, strLine2.trim().lastIndexOf("_links"));
                    log("in LinkID: " + thisLinkID);
                    topicLinkIDS.add(thisLinkID);
                }
                topicIncomingLinksHT.put(topicID, topicLinkIDS);
            }
            // -----------------------------------------------------------------
            /**
             * <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
             * <ltwResultsetType>
             * <ltw_Topic name="Scurvy" id="28266">
             * <outgoingLinks>
             * <outLink>10843</outLink>
             */
            // produce RESULT SET XML
            String rsXmlFPath = topicFileDir + "5000F2FWikiResultSet_WithoutTName.xml";
            FileWriter fw = new FileWriter(new File(rsXmlFPath));
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
            fw.write("\n");
            fw.write("<ltwResultsetType>");
            fw.write("\n");

            for (String[] topicPairSA : topicPairsV) {
                String topicID = topicPairSA[0];
//                String topicName = topicPairSA[1];
                String topicName = "";
                fw.write("<ltw_Topic name=\"" + topicName + "\" id=\"" + topicID + "\">");
                fw.write("\n");
                // -------------------------------------------------------------
                // Outgoing Links
                fw.write("<outgoingLinks>");
                fw.write("\n");
                Vector<String> thisOutLinkIDV = topicOutgoingLinksHT.get(topicID);
                for (String thisOutLinkID : thisOutLinkIDV) {
                    fw.write("<outLink>" + thisOutLinkID + "</outLink>");
                    fw.write("\n");
                }
                fw.write("</outgoingLinks>");
                fw.write("\n");
                // -------------------------------------------------------------
                // Incoming Links
                fw.write("<incomingLinks>");
                fw.write("\n");
                Vector<String> thisInLinkIDV = topicIncomingLinksHT.get(topicID);
                for (String thisInLinkID : thisInLinkIDV) {
                    fw.write("<inLink>" + thisInLinkID + "</inLink>");
                    fw.write("\n");
                }
                fw.write("</incomingLinks>");
                fw.write("\n");
                // -------------------------------------------------------------
                fw.write("</ltw_Topic>");
                fw.write("\n");
            }

            fw.write("</ltwResultsetType>");
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
        }

        // ---------------------------------------------------------------------


    }
    // </editor-fold>
    // =========================================================================
    // <editor-fold defaultstate="collapsed" desc="Produce WOLLONGONG Submission TO Eval XML">
    private static Vector<String> myRunProperty = null;
    private static Hashtable<String, String> topicFileNameHT = null;
    private static Hashtable<String, Vector<String>> runEData = null;
    private static Vector<String> topicLinksV = new Vector<String>();

    private static void readToParseF2FToEvalForm(Vector<String> f2fRunDirV) {
        for (String runDir : f2fRunDirV) {
            File runFDir = new File(runDir);
            if (runFDir.isDirectory()) {
                File[] runFiles = runFDir.listFiles();
                for (File thisRunFile : runFiles) {
                    String thisRunFPath = thisRunFile.getAbsolutePath();
                    log(thisRunFPath);
                    if (thisRunFPath.endsWith(".xml")) {
                        myRunProperty = new Vector<String>();
                        topicFileNameHT = new Hashtable<String, String>();
                        runEData = new Hashtable<String, Vector<String>>();
                        Vector<String> tLinksV = null;
                        // =====================================================
                        String thisRunFName = thisRunFile.getName();
                        String thisRunName = thisRunFName.substring(0, thisRunFName.lastIndexOf(".xml"));
                        // -----------------------------------------------------
                        String participantID = "";
                        String runID = "";
                        String task = "";
                        String topicFile = "";
                        String topicName = "";
                        String linkToFiles = "";
                        String linkFromFiles = "";
                        int pos = 0;
                        try {
                            FileInputStream fis = new FileInputStream(thisRunFPath);
                            DataInputStream dis = new DataInputStream(fis);
                            BufferedReader br = new BufferedReader(new InputStreamReader(dis));
                            String strLine;
                            String lastLine;
                            while ((strLine = br.readLine()) != null) {
                                String thisLine = strLine.trim();
                                if (thisLine.indexOf("<crosslink-submission") != -1) {
                                    log(thisLine);
                                    // <crosslink-submission participant-id="119" run-id="Wollongong_LTWF2F_01" task="LTW_F2F">
                                    int pIDStart = thisLine.indexOf("participant-id=") + 16;
                                    int pIDEnd = thisLine.indexOf("\"", pIDStart);
                                    participantID = thisLine.substring(pIDStart, pIDEnd);
                                    myRunProperty.add(participantID);
                                    int rIDStart = thisLine.indexOf("run-id=") + 8;
                                    int rIDEnd = thisLine.indexOf("\"", rIDStart);
                                    runID = thisLine.substring(rIDStart, rIDEnd);
                                    myRunProperty.add(runID);
                                    int taskStart = thisLine.indexOf("task=") + 6;
                                    int taskEnd = thisLine.indexOf("\"", taskStart);
                                    task = thisLine.substring(taskStart, taskEnd);
                                    myRunProperty.add(task);
                                    log(participantID + " : " + runID + " : " + task);
                                } 
                                if (thisLine.indexOf("<topic") != -1) {
                                    log(thisLine);
                                    // <topic file="10317130" name="Dudley County, New South Wales">
                                    int tFileStart = thisLine.indexOf("file=") + 6;
                                    int tFileEnd = thisLine.indexOf("\"", tFileStart);
                                    topicFile = thisLine.substring(tFileStart, tFileEnd);
                                    int tNameStart = thisLine.indexOf("name=") + 6;
                                    int tNameEnd = thisLine.indexOf("\"", tNameStart);
                                    topicName = thisLine.substring(tNameStart, tNameEnd);
                                    log(topicFile + " : " + topicName);                              	
                                    topicFileNameHT.put(topicFile, topicName);
                                    
                                    tLinksV = new Vector<String>();
                                } 
                                if ((pos = thisLine.indexOf("<tofile")) != -1) {
                                	pos += 7;
                                    // <linkto>48361, 3434750</linkto>
                                    int linktoStart = thisLine.indexOf(">", pos) + 1;
                                    
                                    int linktoEnd = thisLine.indexOf("</tofile>", linktoStart);
                                    linkToFiles = thisLine.substring(linktoStart, linktoEnd);
                                }
//                                if (thisLine.indexOf("<linkfrom>") != -1) {
//                                    // <linkfrom>48361, 3434750</linkfrom>
//                                    int linkfromStart = thisLine.indexOf("<linkfrom>") + 8;
//                                    int linkfromEnd = thisLine.indexOf("</linkfrom>", linkfromStart);
//                                    linkFromFiles = thisLine.substring(linkfromStart, linkfromEnd);
//                                } 
                                if (thisLine.indexOf("</topic>") != -1) {
                                    // END of Topic
                                    tLinksV.add(linkToFiles);
                                    tLinksV.add(linkFromFiles);
                                    if (topicFile.length() == 0)
                                    	System.err.println("Enpty topic id.");                                        
                                    runEData.put(topicFile, tLinksV);
                                }

                                lastLine = thisLine;
//                                log(strLine);
                            }
                            fis.close();
                        } catch (IOException ex) {
                            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        // -----------------------------------------------------
                        // produce Eval XML Form
                        // (String thisRunName, Vector<String> runProperty, Hashtable<String, Vector<String>> runEData)
                        produceNewRunXML(thisRunName, myRunProperty, topicFileNameHT, runEData);
                    }
                }
            }
        }
    }

    private static void produceNewRunXML(String thisRunName, Vector<String> runProperty, Hashtable<String, String> topicFileNameHT, Hashtable<String, Vector<String>> runEData) {
        try {
//            String rDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\Wikipedia_F2F\\RevisedSubmission\\";
//            String rDir = "C:\\PHD\\INEX_2009\\LTW2009_SUBMISSION\\Wikipedia_F2F\\RevisedSubmission\\";
            //String rDir = "C:\\PHD\\INEX_2009\\LTW2009_SUBMISSION\\Wikipedia_F2FonA2B\\FROM_Eric\\";
        	String rDir = destDir + File.separator; //"/data/corpus/inex/2009/ltw/unofficial_runs_revised/";
            String rsXmlFPath = rDir + thisRunName + "_4EVAL.xml";
            FileWriter fw = new FileWriter(new File(rsXmlFPath));
            String pID = runProperty.elementAt(0);
            String rID = runProperty.elementAt(1);
            String task = runProperty.elementAt(2);

            fw.write("<crosslink-submission participant-id='" + pID + "' run-id='" + rID + "' task='" + task + "' format='FOL'>");
            fw.write("\n");
            fw.write("<details>");
            fw.write("\n");
            fw.write("<machine><cpu>Intel Pentium</cpu><speed>1.86GHz</speed><cores>2</cores><hyperthreads>1</hyperthreads><memory>1GB</memory></machine>");
            fw.write("\n");
            fw.write("</details>");
            fw.write("\n");
            fw.write("<description>" + rID + "</description>");
            fw.write("\n");
            fw.write("<collections><collection>Wikipedia_2009_Collection</collection></collections>");
            fw.write("\n");

            Enumeration topicIDEnu = runEData.keys();
            while (topicIDEnu.hasMoreElements()) {
                String topicID = topicIDEnu.nextElement().toString();
                // With Name
//                String tName = topicFileNameHT.get(topicID);
                // Without Name
                if (topicID.length() == 0)
                	System.err.println("Enpty topic id.");
                
                Vector<String> linkDataVS = runEData.get(topicID);
                String tName = linkDataVS.elementAt(1);
                fw.write("<topic file='" + topicID + ".xml' name='" + tName + "'>");
                fw.write("\n");
                // -------------------------------------------------------------
                String outStrSet = linkDataVS.elementAt(0);
                fw.write("<outgoing>");
                fw.write("\n");
                if (!outStrSet.equals("")) {
                    String[] outgoingLinks = outStrSet.split(",");
                    for (String outLinkID : outgoingLinks) {
                        fw.write("<link>");
                        fw.write("\n");
                        fw.write("<linkto><file>");
                        fw.write(outLinkID);
                        fw.write(".xml</file></linkto>");
                        fw.write("\n");
                        fw.write("</link>");
                        fw.write("\n");
                    }
                }
                fw.write("</outgoing>");
                fw.write("\n");
                // -------------------------------------------------------------
//                String inStrSet = linkDataVS.elementAt(1);
//                fw.write("<incoming>");
//                fw.write("\n");
//                if (!inStrSet.equals("")) {
//                    String[] incomingLinks = inStrSet.split(",");
//                    for (String inLinkID : incomingLinks) {
//                        fw.write("<link>");
//                        fw.write("\n");
//                        fw.write("<anchor><file>");
//                        fw.write(inLinkID);
//                        fw.write(".xml</file></anchor>");
//                        fw.write("\n");
//                        fw.write("</link>");
//                        fw.write("\n");
//                    }
//                }
//                fw.write("</incoming>");
//                fw.write("\n");
                // -------------------------------------------------------------
                fw.write("</topic>");
                fw.write("\n");
            }

            fw.write("</crosslink-submission>");
            fw.write("\n");
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    // </editor-fold>
    // =========================================================================
    // <editor-fold defaultstate="collapsed" desc="Convert F2F Runs to Eval XML">

    private static void parseF2FRunToEvalForm(Vector<String> f2fRunDirV) {
        for (String runDir : f2fRunDirV) {
            File runFDir = new File(runDir);
            if (runFDir.isDirectory()) {
                File[] runFiles = runFDir.listFiles();
                for (File thisRunFile : runFiles) {
                    String thisRunFPath = thisRunFile.getAbsolutePath();
//                    log(thisRunFPath);
                    if (thisRunFPath.endsWith(".xml")) {
                        String thisRunFName = thisRunFile.getName();
                        String thisRunName = thisRunFName.substring(0, thisRunFName.lastIndexOf(".xml"));
                        Vector<String> runProperty = getXmlRunProperty(thisRunFPath);
                        Hashtable<String, Vector<String>> submissionData = transformF2FXmlRun(thisRunFPath);
                        // -----------------------------------------------------
                        // produce the Evaluation XML Form for this Run
                        log("runProperty.size(): " + runProperty.size());
                        produceEvalFormXML(thisRunName, runProperty, runEData);
                    }
                }
            }
        }
    }
    private static String xmlUnicode = "utf-8";

    private static Vector<String> getXmlRunProperty(String thisRunFPath) {
        log(thisRunFPath);
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(thisRunFPath));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, xmlUnicode);
            InputSource inputSource = new InputSource(inputStreamReader);

            // Parse the XML File
            DOMParser parser = new DOMParser();
            parser.parse(inputSource);
            Document myDoc = parser.getDocument();

            myRunProperty = new Vector<String>();
            pageElmnFinder(myDoc);

            fileInputStream.close();
            inputStreamReader.close();
        } catch (SAXException ex) {
            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return myRunProperty;
    }
    private static boolean isRootFlag = false;

    private static void pageElmnFinder(Node node) {
        switch (node.getNodeType()) {
            case Node.DOCUMENT_NODE:
                // recurse on each child
                NodeList nodes = node.getChildNodes();
                if (nodes != null) {
                    for (int i = 0; i < nodes.getLength(); i++) {
                        pageElmnFinder(nodes.item(i));
                    }
                }
                break;
            case Node.ELEMENT_NODE:
                String element = node.getNodeName();
                log("NodeName: " + element);
                // =============================================================
                // Start of Tag: -----------------------------------------------
                if (element.equals("crosslink-submission")) {
                    isRootFlag = true;
                    log("isRootFlag: " + isRootFlag);
                }
                // =============================================================
                NamedNodeMap attributes = node.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node current = attributes.item(i);
                    if (isRootFlag && current.getNodeName().equals("participant-id")) {
                        String parID = current.getNodeValue();
                        myRunProperty.add(parID);
                    } else if (isRootFlag && current.getNodeName().equals("run-id")) {
                        String runID = current.getNodeValue();
                        myRunProperty.add(runID);
                    } else if (isRootFlag && current.getNodeName().equals("task")) {
                        String task = current.getNodeValue();
                        myRunProperty.add(task);
                    }
                }
                if (myRunProperty.size() == 3 && isRootFlag) {
                    isRootFlag = false;
                    return;
                }
                // =============================================================
                // Recurse each child: e.g. TEXT ---------------------------
                NodeList children = node.getChildNodes();
                if (children != null) {
                    for (int i = 0; i < children.getLength(); i++) {
                        pageElmnFinder(children.item(i));
                    }
                }
                break;
            case Node.TEXT_NODE:
                break;
        }
    }
    private static Hashtable<String, String> topicIDNamePairs = null;

    private static Hashtable<String, Vector<String>> transformF2FXmlRun(String thisRunFPath) {
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(thisRunFPath));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, xmlUnicode);
            InputSource inputSource = new InputSource(inputStreamReader);

            // Parse the XML File
            DOMParser parser = new DOMParser();
            parser.parse(inputSource);
            Document myDoc = parser.getDocument();

            runEData = new Hashtable<String, Vector<String>>();
            topicIDNamePairs = new Hashtable<String, String>();
            runOutInDataExtractor(myDoc);

            fileInputStream.close();
            inputStreamReader.close();
        } catch (SAXException ex) {
            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return runEData;
    }
    private static boolean isTopicFlag = false;
    private static boolean isLinkToFlag = false;
    private static boolean isLinkFromFlag = false;
    private static String topicFileID = "";

    private static void runOutInDataExtractor(Node node) {
        switch (node.getNodeType()) {
            case Node.DOCUMENT_NODE:
                // recurse on each child
                NodeList nodes = node.getChildNodes();
                if (nodes != null) {
                    for (int i = 0; i < nodes.getLength(); i++) {
                        pageElmnFinder(nodes.item(i));
                    }
                }
                break;
            case Node.ELEMENT_NODE:
                String element = node.getNodeName();
                // =============================================================
                // Start of Tag: -----------------------------------------------
                if (element.equals("topic")) {
                    isTopicFlag = true;
                    topicLinksV = new Vector<String>();
                } else if (element.equals("linkto")) {
                    isLinkToFlag = true;
                } else if (element.equals("linkfrom")) {
                    isLinkFromFlag = true;
                }
                // =============================================================
                String thisFileID = "";
                String thisFileName = "";
                NamedNodeMap attributes = node.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node current = attributes.item(i);
                    if (isTopicFlag && current.getNodeName().equals("file")) {
                        thisFileID = current.getNodeValue();
                    } else if (isTopicFlag && current.getNodeName().equals("name")) {
                        thisFileName = current.getNodeValue();
                    }
                }
                if (!thisFileID.equals("") && !thisFileName.equals("") && isTopicFlag) {
                    isRootFlag = false;
                    if (!topicIDNamePairs.containsKey(thisFileID)) {
                        topicIDNamePairs.put(thisFileID, thisFileName);
                    }
                }
                // =============================================================
                // Recurse each child: e.g. TEXT ---------------------------
                NodeList children = node.getChildNodes();
                if (children != null) {
                    for (int i = 0; i < children.getLength(); i++) {
                        pageElmnFinder(children.item(i));
                    }
                }
                // =============================================================
                // End of Tag: -----------------------------------------------
                if (element.equals("topic")) {
                    isTopicFlag = false;
                } else if (element.equals("linkto")) {
                    isLinkToFlag = false;
                } else if (element.equals("linkfrom")) {
                    isLinkFromFlag = false;
                }
                break;
            case Node.TEXT_NODE:
                // node.getNodeValue()
                if (isLinkToFlag) {
                    String outgoingLinks = node.getNodeValue();
                    topicLinksV.add(0, outgoingLinks);
                } else if (isLinkFromFlag) {
                    String incomingLinks = node.getNodeValue();
                    topicLinksV.add(1, incomingLinks);
                }
                break;
        }
    }
    // =========================================================================

    private static void reformateRuns(Vector<String> f2fRunDirV) {

        Vector<String> runContentVS = new Vector<String>();
        for (String f2fRunDir : f2fRunDirV) {
            log(f2fRunDir);
            File f2fRunFDir = new File(f2fRunDir);
            if (f2fRunFDir.isDirectory()) {
                File[] f2fRunFileList = f2fRunFDir.listFiles();
                for (File f2fRunFile : f2fRunFileList) {
                    String f2fRunFileFPath = f2fRunFile.getAbsolutePath();
                    log(f2fRunFileFPath);
                    if (f2fRunFileFPath.endsWith(".xml")) {
                        String f2fRunFileFName = f2fRunFile.getName();
                        String f2fRunFileID = f2fRunFileFName.substring(0, f2fRunFileFName.lastIndexOf(".xml"));
                        // -----------------------------------------------------
                        try {
                            FileInputStream fis = new FileInputStream(f2fRunFileFPath);
                            DataInputStream dis = new DataInputStream(fis);
                            BufferedReader br2 = new BufferedReader(new InputStreamReader(dis));
                            String strLine2;
                            while ((strLine2 = br2.readLine()) != null) {
                                String topicID = "";
                                if (strLine2.indexOf("name=") > -1) {
                                    int startP = strLine2.indexOf("file=") + 6;
                                    int endP = strLine2.indexOf("\"", startP);
                                    topicID = strLine2.substring(startP, endP);
                                    log(strLine2);
                                    log(topicID);
                                    String nesLine = "<topic file=\"" + topicID + "\" name=\"\">";
                                    runContentVS.add(nesLine);
                                } else {
                                    runContentVS.add(strLine2);
                                }
                            }
                            fis.close();
                        } catch (IOException ex) {
                            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        // -----------------------------------------------------
                        String rDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\Wikipedia_F2F\\WOLLONGONG\\RevisedSUBMIT\\";
                        String rsXmlFPath = rDir + f2fRunFileID + "_Reformed.xml";
                        try {
                            FileWriter fw = new FileWriter(new File(rsXmlFPath));
                            for (String thisLine : runContentVS) {
                                fw.append(thisLine);
                                fw.write("\n");
                            }
                            fw.close();
                        } catch (IOException ex) {
                            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        // -----------------------------------------------------
                        log("DONE - " + rsXmlFPath);
                    }
                }
            }
        }
    }
    // =========================================================================

    private static void convertF2FRunToEvalForm(Vector<String> f2fRunDirV) {
        for (String runDir : f2fRunDirV) {
            File runFDir = new File(runDir);
            if (runFDir.isDirectory()) {
                File[] runFiles = runFDir.listFiles();
                for (File thisRunFile : runFiles) {
                    String thisRunFPath = thisRunFile.getAbsolutePath();
                    log(thisRunFPath);
                    if (thisRunFPath.endsWith(".xml")) {
                        String thisRunFName = thisRunFile.getName();
                        String thisRunName = thisRunFName.substring(0, thisRunFName.lastIndexOf(".xml"));
                        Vector<String> runProperty = getRunProperty(thisRunFPath);
                        Hashtable<String, Vector<String>> runEEData = transformF2FRun(thisRunFPath);
                        // -----------------------------------------------------
                        // produce the Evaluation XML Form for this Run
                        log("runProperty.size(): " + runProperty.size());
                        produceEvalFormXML(thisRunName, runProperty, runEEData);
                    }
                }
            }
        }
    }

    private static void produceEvalFormXML(String thisRunName, Vector<String> runProperty, Hashtable<String, Vector<String>> runEData) {
        try {
//            String rDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\Wikipedia_F2F\\RevisedSubmission\\";
//            String rDir = "G:\\PHD_Tools\\LTW2009_SUBMISSION\\Wikipedia_F2FonA2B\\UWaterloo\\";
//            String rDir = "C:\\PHD\\INEX_2009\\LTW2009_SUBMISSION\\Wikipedia_F2FonA2B\\OTAGO\\";
//            String rDir = "C:\\PHD\\INEX_2009\\LTW2009_SUBMISSION\\Wikipedia_F2F\\OTAGO";
//            String rDir = "C:\\PHD\\INEX_2009\\24TOPICS_POOL\\From_Andrew";
            String rDir = "C:\\PHD\\INEX_2009\\24TOPICS_POOL\\From_Andrew\\";
            String rsXmlFPath = rDir + thisRunName + "_Revised.xml";
            FileWriter fw = new FileWriter(new File(rsXmlFPath));
            String pID = runProperty.elementAt(0);
            String rID = runProperty.elementAt(1);
            String task = runProperty.elementAt(2);
            String tName = runProperty.elementAt(3);

            fw.write("<crosslink-submission participant-id='" + pID + "' run-id='" + rID + "' task='" + task + "' format='FOL'>");
            fw.write("\n");
            fw.write("<details>");
            fw.write("\n");
            fw.write("<machine><cpu>Intel Pentium</cpu><speed>1.86GHz</speed><cores>2</cores><hyperthreads>1</hyperthreads><memory>1GB</memory></machine>");
            fw.write("\n");
            fw.write("</details>");
            fw.write("\n");
            fw.write("<description>" + rID + "</description>");
            fw.write("\n");
            fw.write("<collections><collection>Wikipedia_2009_Collection</collection></collections>");
            fw.write("\n");

            Enumeration topicIDEnu = runEData.keys();
            while (topicIDEnu.hasMoreElements()) {
                String topicID = topicIDEnu.nextElement().toString();
                fw.write("<topic file='" + topicID + ".xml' name='" + tName + "'>");
                fw.write("\n");
                Vector<String> linkDataVS = runEData.get(topicID);
                // -------------------------------------------------------------
                String outStrSet = linkDataVS.elementAt(0);
                fw.write("<outgoing>");
                fw.write("\n");
                if (!outStrSet.equals("")) {
                    String[] outgoingLinks = outStrSet.split(",");
//                    for (String outLinkID : outgoingLinks) {
                    int outEndIndex = Math.min(250, outgoingLinks.length);
                    for (int i = 0; i < outEndIndex; i++) {
                        String outLinkID = outgoingLinks[i].trim();
                        fw.write("<link>");
                        fw.write("\n");
                        fw.write("<linkto><file>");
                        fw.write(outLinkID.trim());
                        fw.write(".xml</file></linkto>");
                        fw.write("\n");
                        fw.write("</link>");
                        fw.write("\n");
                    }
                }
                fw.write("</outgoing>");
                fw.write("\n");
                // -------------------------------------------------------------
                String inStrSet = linkDataVS.elementAt(1);
                fw.write("<incoming>");
                fw.write("\n");
                if (!inStrSet.equals("")) {
                    String[] incomingLinks = inStrSet.split(",");
//                    for (String inLinkID : incomingLinks) {
                    int inEndIndex = Math.min(250, incomingLinks.length);
                    for (int j = 0; j < inEndIndex; j++) {
                        String inLinkID = incomingLinks[j].trim();
                        fw.write("<link>");
                        fw.write("\n");
                        fw.write("<anchor><file>");
                        fw.write(inLinkID.trim());
                        fw.write(".xml</file></anchor>");
                        fw.write("\n");
                        fw.write("</link>");
                        fw.write("\n");
                    }
                }
                fw.write("</incoming>");
                fw.write("\n");
                // -------------------------------------------------------------
                fw.write("</topic>");
                fw.write("\n");
            }

            fw.write("</crosslink-submission>");
            fw.write("\n");
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Vector<String> getRunProperty(String f2fRunFPath) {
        Vector<String> runPropertyV = new Vector<String>();

        VTDGen vg = new VTDGen();
        if (vg.parseFile(f2fRunFPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(f2fRunFPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);

                String participantID = "";
                String runID = "";
                String task = "";
                String xPath0 = "/crosslink-submission";
                ap.selectXPath(xPath0);
                int i0 = -1;
                while ((i0 = ap.evalXPath()) != -1) {
                    int j0 = vn.getAttrVal("participant-id");
                    int k0 = vn.getAttrVal("run-id");
                    int l0 = vn.getAttrVal("task");

                    participantID = vn.toRawString(j0).toString();
                    runID = vn.toRawString(k0).toString();
                    task = vn.toRawString(l0).toString();

                    log(participantID + " - " + runID + " - " + task);

                    runPropertyV.add(participantID);
                    runPropertyV.add(runID);
                    runPropertyV.add(task);
                }
                String xPath = "/crosslink-submission/topic";
                ap.selectXPath(xPath);
                int i = -1;
                while ((i = ap.evalXPath()) != -1) {
                    int j = vn.getAttrVal("name");
                    if (j != -1) {
                        String topicNameTxt = vn.toRawString(j).toString();
                        runPropertyV.add(topicNameTxt);
                    }
                }
                xm.output(fos);
                fos.close();
            } catch (TranscodeException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return runPropertyV;
    }

    public static Hashtable<String, Vector<String>> transformF2FRun(String f2fRunFPath) {
        Hashtable<String, Vector<String>> runDataHT = new Hashtable<String, Vector<String>>();
        Vector<String> linksV = new Vector<String>();

        VTDGen vg = new VTDGen();
        if (vg.parseFile(f2fRunFPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(f2fRunFPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);

                Vector<String> topicIDV = new Vector<String>();
                String xPath = "/crosslink-submission/topic";
                ap.selectXPath(xPath);
                int i = -1;
                while ((i = ap.evalXPath()) != -1) {
                    int j = vn.getAttrVal("file");
                    if (j != -1) {
                        String topicIDTxt = vn.toRawString(j).toString();
                        topicIDV.add(topicIDTxt);
                    }
                }
                // =============================================================
                for (String thisTID : topicIDV) {
                    linksV = new Vector<String>();
                    // ---------------------------------------------------------
                    String xPath1 = "/crosslink-submission/topic[@file='" + thisTID + "']/outgoing/linkto";
                    ap.selectXPath(xPath1);
                    int i1 = -1;
                    String outgoingLinks = "";
                    while ((i1 = ap.evalXPath()) != -1) {
                        int j1 = vn.getText();
                        if (j1 != -1) {
                            outgoingLinks = vn.toRawString(j1).toString();
                        }
                    }
                    linksV.add(outgoingLinks);
                    // ---------------------------------------------------------
                    String xPath2 = "/crosslink-submission/topic[@file='" + thisTID + "']/incoming/linkfrom";
                    ap.selectXPath(xPath2);
                    int i2 = -1;
                    String incomingLinks = "";
                    while ((i2 = ap.evalXPath()) != -1) {
                        int j2 = vn.getText();
                        if (j2 != -1) {
                            incomingLinks = vn.toRawString(j2).toString();
                        }
                    }
                    linksV.add(incomingLinks);
                    // ---------------------------------------------------------
                    runDataHT.put(thisTID, linksV);
//                    log("thisTID: " + thisTID + " DONE...");
                }
                xm.output(fos);
                fos.close();
            } catch (TranscodeException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return runDataHT;
    }
    // =========================================================================

    private static void convertToF2FResultSet(String topicFileFPath, String gtDir) {

        try {
            Vector<String[]> topicPairsV = new Vector<String[]>();
            File tpFile = new File(topicFileFPath);
            FileInputStream fstream = new FileInputStream(topicFileFPath);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] thisLineSet = strLine.split(" : ");
                String topicID = thisLineSet[1].trim().substring(0, thisLineSet[1].trim().lastIndexOf(".xml"));
                if (isInNumber(topicID)) {
                    topicPairsV.add(new String[]{topicID, thisLineSet[2]});
//                    log(thisLineSet[0]);
                } else {
                    log("Invalid Topic ID: " + topicID + " at line: " + strLine);
                }
            }
            fstream.close();
            // -----------------------------------------------------------------
            // Generate F2F Result Set
            Hashtable<String, Vector<String>> topicLinksHT = new Hashtable<String, Vector<String>>();
            Vector<String> topicLinkIDS = null;
            String topicFileDir = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS_F2F\\LTW2009_F2FTOPICS\\";
            for (String[] topicPairSA : topicPairsV) {
                String topicID = topicPairSA[0];
                String topicName = topicPairSA[1];
                topicLinkIDS = new Vector<String>();
                String topicGTFPath = gtDir + topicID + "_F2FGT.txt";
                FileInputStream fis = new FileInputStream(topicGTFPath);
                DataInputStream dis = new DataInputStream(fis);
                BufferedReader br2 = new BufferedReader(new InputStreamReader(dis));
                String strLine2;
                while ((strLine2 = br2.readLine()) != null) {
                    log("strLine2: " + strLine2);
                    String thisLinkID = strLine2.trim().substring(strLine2.trim().lastIndexOf("/"), strLine2.trim().length());
                    log("thisLinkID" + thisLinkID);
                    topicLinkIDS.add(thisLinkID);
                }
                topicLinksHT.put(topicID, topicLinkIDS);
            }
            // -----------------------------------------------------------------
            /**
             * <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
             * <ltwResultsetType>
             * <ltw_Topic name="Scurvy" id="28266">
             * <outgoingLinks>
             * <outLink>10843</outLink>
             */
            // produce RESULT SET XML
            String rsXmlFPath = topicFileDir + "5000F2FWikiResultSet_WithoutName.xml";
            FileWriter fw = new FileWriter(new File(rsXmlFPath));
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
            fw.write("\n");
            fw.write("<ltwResultsetType>");
            fw.write("\n");

            for (String[] topicPairSA : topicPairsV) {
                String topicID = topicPairSA[0];
//                String topicName = topicPairSA[1];
                String topicName = "";
                fw.write("<ltw_Topic name=\"" + topicName + "\" id=\"" + topicID + "\">");
                fw.write("\n");
                // -------------------------------------------------------------
                // Outgoing Links
                fw.write("<outgoingLinks>");
                fw.write("\n");
                Vector<String> thisOutLinkIDV = topicLinksHT.get(topicID);
                for (String thisOutLinkID : thisOutLinkIDV) {
                    fw.write("<outLink>" + thisOutLinkID + "</outLink>");
                    fw.write("\n");
                }
                fw.write("</outgoingLinks>");
                fw.write("\n");
                // -------------------------------------------------------------
                // Incoming Links
                fw.write("<incomingLinks>");
                fw.write("\n");
//                Vector<String> thisInLinkIDV = topicLinksHT.get(topicID);
//                for (String thisInLinkID : thisInLinkIDV) {
//                }
                fw.write("</incomingLinks>");
                fw.write("\n");
                // -------------------------------------------------------------
                fw.write("</ltw_Topic>");
                fw.write("\n");
            }

            fw.write("</ltwResultsetType>");
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(genResultSet.class.getName()).log(Level.SEVERE, null, ex);
        }

        // ---------------------------------------------------------------------


    }
//    public static boolean isInNumber(String num) {
//        try {
//            int intI = Integer.parseInt(num);
//        } catch (NumberFormatException nfe) {
//            return false;
//        }
//        return true;
//    }
    // </editor-fold>
}
