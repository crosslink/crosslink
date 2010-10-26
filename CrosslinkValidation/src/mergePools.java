
import com.sun.org.apache.xpath.internal.XPathAPI;
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
import java.io.BufferedWriter;
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
public class mergePools {

    static void log(Object obj) {
        System.out.println(obj.toString());
    }
    private static String myPoolDir = "C:\\Users\\Darren\\Desktop\\INEX_2009\\POOL_2009\\POOL2009A2B\\";
    private static String abPoolDir = "C:\\Users\\Darren\\Desktop\\INEX_2009\\POOL_2009\\POOL_A2BRUN\\";
    private static String gtPoolDir = "C:\\Users\\Darren\\Desktop\\INEX_2009\\POOL_2009\\POOL_GROUNDTRUTH\\";
    private static int option = 3;

    public static void main(String[] args) {

        switch (option) {
            case 0:
                mergeGTtoABPools(gtPoolDir, abPoolDir, myPoolDir);
                break;
            case 1:
                String kellyLinksDir = "C:\\Users\\Darren\\Desktop\\INEX_2009\\POOL_2009\\POOL_KELLY_F2FONA2B\\";
                mergeKellytoABPools(kellyLinksDir, abPoolDir, myPoolDir);
                break;
            case 2:
                String testing_Pool = "C:\\JTemp\\POOL_10313.xml";
                validatePool(testing_Pool);
                break;
            case 3:
                String wikipedia_Pool = "C:\\JTemp\\LTWAssessmentTool\\resources\\toolResources.xml";
                int elmnNum = 590;
                correctStartPoints(wikipedia_Pool, elmnNum);
                break;
            default:
                log("This is the default option...");
        }

    }

    private static void correctStartPoints(String toolResoourceXML, int elmnNum) {
        VTDGen vg = new VTDGen();
        if (vg.parseFile(toolResoourceXML, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(toolResoourceXML);
                fos = new FileOutputStream(fo);
                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);

                int preEndPoint = 0;

                for (int i = 1; i <= elmnNum; i++) {
                    String xPath = "/toolResources/currTopicAnchors/anchor" + String.valueOf(i);
                    ap.selectXPath(xPath);
                    int j = -1;
                    if ((j = ap.evalXPath()) != -1) {
                        // 3 : 23 : Politics of New Zealand : 0 : 25
                        int k = vn.getText();
                        String thisAnchorList = vn.toRawString(k);
                        String[] thisAnchorListSet = thisAnchorList.split(" : ");
                        int thisStartPoint = Integer.valueOf(thisAnchorListSet[3]);
                        if (i > 1) {
                            if (thisStartPoint <= preEndPoint) {
                                int diff = thisStartPoint - preEndPoint;
                                if (Math.abs(diff) > 2) {
                                    log("Anchor Index: " + thisAnchorListSet[0] + " - " + diff);
                                } else {
                                    String revisedValue = thisAnchorListSet[0] + " : " + thisAnchorListSet[1] + " : " +
                                            thisAnchorListSet[2] + " : " + String.valueOf(thisStartPoint + 2) + " : " + thisAnchorListSet[4];
                                    log("Anchor Index: " + i + " - " + thisStartPoint + " --> " + String.valueOf(thisStartPoint + 2));
                                    log("Anchor Index: " + i + " - " + revisedValue);
                                    xm.updateToken(k, revisedValue);
                                }
                            }
                        }
                        preEndPoint = Integer.valueOf(thisAnchorListSet[4]);
                    }
                }
                xm.output(fos);
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void validatePool(String testing_Pool) {
//        String dir = "C:\\Users\\Darren\\Desktop\\INEX_2009\\POOL_2009\\POOL2009A2B\\BackUP_POOL_DONE03_IN-SORTED";
        String dir = "C:\\Tmp\\POOL_20091102";
        File thisDIR = new File(dir);
        File[] thisPoolFileList = thisDIR.listFiles();

        FileWriter fw = null;
        try {
            int preAOffset = -1;
            int preALength = -1;

//            String testing_RESULR = "C:\\Users\\Darren\\Desktop\\INEX_2009\\POOL_2009\\POOL2009A2B\\BackUP_POOL_DONE03_IN-SORTED\\CHECKING\\";
            String testing_RESULR = "C:\\Tmp\\POOL_20091102\\CHECKING\\";

            for (File thisFile : thisPoolFileList) {
                String thisFileAbsPath = thisFile.getAbsolutePath();
                if (thisFileAbsPath.endsWith(".xml")) {

                    String thisFileName = thisFile.getName();

                    fw = new FileWriter(testing_RESULR + thisFileName + ".txt");

                    fw.write("================================================");
                    fw.write("================================================");
                    fw.write("\n");
                    fw.write(thisFileAbsPath);
                    fw.write("\n");

                    Vector<int[]> overlapAnchors = new Vector<int[]>();
                    VTDGen vg = new VTDGen();
                    if (vg.parseFile(thisFileAbsPath, true)) {
                        FileOutputStream fos = null;
                        try {
                            VTDNav vn = vg.getNav();
                            File fo = new File(thisFileAbsPath);
                            fos = new FileOutputStream(fo);
                            AutoPilot ap = new AutoPilot(vn);
                            XMLModifier xm = new XMLModifier(vn);
                            String xPath = "/crosslink-assessment/topic/outgoinglinks/anchor";
                            ap.selectXPath(xPath);
                            int i = -1;
                            while ((i = ap.evalXPath()) != -1) {
                                int j = vn.getAttrVal("aoffset");
                                int k = vn.getAttrVal("alength");
                                if (j != -1) {
                                    String bOValue = vn.toRawString(j).trim();
                                    String bLValue = vn.toRawString(k).trim();
                                    int intBOValue = Integer.valueOf(bOValue);
                                    int intBLValue = Integer.valueOf(bLValue);
                                    if (preAOffset != -1 && preALength != -1) {
                                        if (intBOValue >= preAOffset && intBOValue <= (preAOffset + preALength)) {
                                            log("1st: aoffset=" + preAOffset + " - " + "alength=" + preALength);
                                            log("2nd: aoffset=" + intBOValue + " - " + "alength=" + intBLValue);
                                            log("-------------------------------------------------------");
                                            overlapAnchors.add(new int[]{preAOffset, preALength, intBOValue, intBLValue});

                                            fw.write("1st: aoffset=" + preAOffset + " - " + "alength=" + preALength);
                                            fw.write("\n");
                                            fw.write("2nd: aoffset=" + intBOValue + " - " + "alength=" + intBLValue);
                                            fw.write("\n");
                                            fw.write("-------------------------------------------------------");
                                            fw.write("\n");
                                        }
                                    }
                                    preAOffset = intBOValue;
                                    preALength = intBLValue;
                                }
                            }
                            // =============================================================
//                for (int[] anchorSet : overlapAnchors) {
//                    String preOffset = String.valueOf(anchorSet[0]);
//                    String preLength = String.valueOf(anchorSet[1]);
//                    String bAOffset = String.valueOf(anchorSet[2]);
//                    String bALength = String.valueOf(anchorSet[3]);
//                    // ---------------------------------------------
//                    Vector<String> linkIDVS = new Vector<String>();
//                    // ---------------------------------------------
//                    String xPathT1 = "/crosslink-assessment/topic/outgoinglinks/anchor[@aoffset='" + preOffset + "' and @alength='" + preLength + "']/subanchor/tobep";
//                    ap.selectXPath(xPathT1);
//                    int l1 = -1;
//                    while ((l1 = ap.evalXPath()) != -1) {
//                        int i1 = vn.getText();
//                        String linkID = vn.toRawString(i1).toString();
//                        if (!linkIDVS.contains(linkID)) {
//                            linkIDVS.add(linkID);
//                        }
//                    }
//                    // ---------------------------------------------
//                    String xPathT2 = "/crosslink-assessment/topic/outgoinglinks/anchor[@aoffset='" + bAOffset + "' and @alength='" + bALength + "']/subanchor/tobep";
//                    ap.selectXPath(xPathT2);
//                    int l2 = -1;
//                    while ((l2 = ap.evalXPath()) != -1) {
//                        int i1 = vn.getText();
//                        String linkID = vn.toRawString(i1).toString();
//                        if (linkIDVS.contains(linkID)) {
////                            vn.toElement();
//                            xm.remove();
//                        }
//                    }
//                }
                            // =============================================================
                            xm.output(fos);
                            fos.close();

                        } catch (IOException ex) {
                            Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (TranscodeException ex) {
                            Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (XPathEvalException ex) {
                            Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (NavException ex) {
                            Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (XPathParseException ex) {
                            Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ModifyException ex) {
                            Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    fw.close();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="mergeKellytoABPools">
    private static void mergeKellytoABPools(String kellyLinksDir, String abPoolDir, String myPoolDir) {
        // 1) Get AB Pool links arranged by Topic ID
        // <editor-fold defaultstate="collapsed" desc="Get AB Pool links arranged by Topic ID">
        Hashtable<String, Vector<String>> abPoolOutgoingLinksHT = new Hashtable<String, Vector<String>>();
        Hashtable<String, Vector<String>> abPoolIncomingLinksHT = new Hashtable<String, Vector<String>>();
        abPoolOutgoingLinksHT = getABPoolOutgoingLinks(abPoolDir);
        abPoolIncomingLinksHT = getABPoolIncomingLinks(abPoolDir);
        // </editor-fold>
        // 2) Get Kelly Pool links arranged by Topic ID & filtered by AB-Pool
        //    NO Duplicated
        // <editor-fold defaultstate="collapsed" desc="Get Kelly Pool links arranged by Topic ID & filtered by AB-Pool">
        Hashtable<String, Vector<String>> kellyPoolOutgoingLinksHT = new Hashtable<String, Vector<String>>();
        Hashtable<String, Vector<String>> kellyPoolIncomingLinksHT = new Hashtable<String, Vector<String>>();
        kellyPoolOutgoingLinksHT = getKellyPoolOutgoingLinks(kellyLinksDir);
        kellyPoolIncomingLinksHT = getKellyPoolIncomingLinks(kellyLinksDir);
        // </editor-fold>
        // 3) Merger Kelly into AB Pool
        // <editor-fold defaultstate="collapsed" desc="Merger Kelly into AB Pool">
        // 3-1) Get Kelly's Outgoing WITHOUT duplicated Link IDs
        Hashtable<String, Vector<String>> newkellyPoolOutgoingLinksHT = new Hashtable<String, Vector<String>>();
        Vector<String> newkellyPoolOutgoingLinksVS = null;
        Enumeration outTopicKeyEny = kellyPoolOutgoingLinksHT.keys();
        while (outTopicKeyEny.hasMoreElements()) {
            Object outTopicKeyObj = outTopicKeyEny.nextElement();
            String thisOutTopicID = outTopicKeyObj.toString();
            Vector<String> kellyLinkIDV = kellyPoolOutgoingLinksHT.get(thisOutTopicID);
            Vector<String> aBLinkIDV = abPoolOutgoingLinksHT.get(thisOutTopicID);
            newkellyPoolOutgoingLinksVS = new Vector<String>();
            for (String kellyLinkID : kellyLinkIDV) {
                if (!aBLinkIDV.contains(kellyLinkID)) {
                    if (!newkellyPoolOutgoingLinksVS.contains(kellyLinkID)) {
                        newkellyPoolOutgoingLinksVS.add(kellyLinkID);
                    }
                }
            }
            newkellyPoolOutgoingLinksHT.put(thisOutTopicID, newkellyPoolOutgoingLinksVS);
        }
        // 3-2) Get Kelly's Incoming WITHOUT duplicated Link IDs
        Hashtable<String, Vector<String>> newkellyPoolIncomingLinksHT = new Hashtable<String, Vector<String>>();
        Vector<String> newkellyPoolIncomingLinksVS = null;
        Enumeration inTopicKeyEny = kellyPoolIncomingLinksHT.keys();
        while (inTopicKeyEny.hasMoreElements()) {
            Object inTopicKeyObj = inTopicKeyEny.nextElement();
            String thisInTopicID = inTopicKeyObj.toString();
            Vector<String> kellyLinkIDV = kellyPoolIncomingLinksHT.get(thisInTopicID);
            Vector<String> aBLinkIDV = abPoolIncomingLinksHT.get(thisInTopicID);
            newkellyPoolIncomingLinksVS = new Vector<String>();
            for (String kellyLinkID : kellyLinkIDV) {
                if (!aBLinkIDV.contains(kellyLinkID)) {
                    if (!newkellyPoolIncomingLinksVS.contains(kellyLinkID)) {
                        newkellyPoolIncomingLinksVS.add(kellyLinkID);
                    }
                }
            }
            newkellyPoolIncomingLinksHT.put(thisInTopicID, newkellyPoolIncomingLinksVS);
        }
        // 3-3) merge Kelly's Link into AB-POOL
        // =====================================================================
        mergeKellyOUTInABPool(newkellyPoolOutgoingLinksHT, myPoolDir);
        // =====================================================================
        // </editor-fold>
        // 4) produce Individual Incoming XML Form FOR ALL
        // <editor-fold defaultstate="collapsed" desc="produce Individual Incoming XML Form">
//        String incomingDir = "C:\\Users\\Darren\\Desktop\\INEX_2009\\POOL_2009\\POOL2009A2B\\INCOMING_FULLXMLFORM\\";
//        Enumeration topicEnu = newkellyPoolIncomingLinksHT.keys();
//        while (topicEnu.hasMoreElements()) {
//            Object topicObj = topicEnu.nextElement();
//            String topicID = topicObj.toString();
//            // =================================================================
//            String incomingFPath = incomingDir + topicID + "_incomingFull.xml";
//            Vector<String> kellyLinksVS = newkellyPoolIncomingLinksHT.get(topicID);
//            Hashtable<String, Vector<String[]>> incomingLinksByBOffsetHT = getIncomingLinksByOffset(topicID);
//            // -----------------------------------------------------------------
//            Vector<String> kellyIDDuplicatedChecker = new Vector<String>();
//            Vector<String[]> kellyLinksOLTxTIDVSA = new Vector<String[]>();
//            for (String kellyLinkID : kellyLinksVS) {
//                if (!kellyIDDuplicatedChecker.contains(kellyLinkID.trim())) {
//                    kellyIDDuplicatedChecker.add(kellyLinkID.trim());
//                } else {
////                    log("Kelly ID duplicated ................................");
//                }
//                if (kellyLinkID.equals("7426") || kellyLinkID.equals("51714") || kellyLinkID.equals("355676")
//                        || kellyLinkID.equals("696004") || kellyLinkID.equals("148268") || kellyLinkID.equals("22218")
//                        || kellyLinkID.equals("32798") || kellyLinkID.equals("2304733") || kellyLinkID.equals("21233")
//                        || kellyLinkID.equals("194901")) {
//                } else {
//                    String kLinkID = kellyLinkID;
//                    String kAnchotTxt = getLinkTitle(kLinkID);
//                    int kAnchorLength = kAnchotTxt.length();
//                    String kALinkPath = getWikipediaFilePathByName(kLinkID + ".xml");
//                    int kAnchorOffset = txtOffsetFinder(kALinkPath, kAnchotTxt);
//                    kellyLinksOLTxTIDVSA.add(new String[]{String.valueOf(kAnchorOffset), String.valueOf(kAnchorLength), kAnchotTxt, kLinkID});
//                }
//            }
//            // -----------------------------------------------------------------
//            Vector<String[]> zeroVSA = incomingLinksByBOffsetHT.get("0");
//            zeroVSA.addAll(kellyLinksOLTxTIDVSA);
//            incomingLinksByBOffsetHT.remove("0");
//            incomingLinksByBOffsetHT.put("0", zeroVSA);
//            // =================================================================
//            // SORT by Link ID
//            Vector<String> zeroAnchorOffsetVS = new Vector<String>();
//            Hashtable<String, String[]> zeroAnchorOffsetHT = new Hashtable<String, String[]>();
//            Vector<String[]> zeroOAnchorLinksVSA = incomingLinksByBOffsetHT.get("0");
//            for (String[] zeroOAnchorLinkSet : zeroOAnchorLinksVSA) {
//                String ooLinkID = zeroOAnchorLinkSet[3];
//                if (!zeroAnchorOffsetVS.contains(ooLinkID)) {
//                    zeroAnchorOffsetVS.add(ooLinkID);
//                    zeroAnchorOffsetHT.put(ooLinkID, zeroOAnchorLinkSet);
//                }
//            }
//            Vector<String> sortedZEROAnchorOffsetVS = sortVectorNumbers(zeroAnchorOffsetVS);
//            Vector<String[]> sortedZOAnchorLinksVSA = new Vector<String[]>();
//            for (String myLinkID : sortedZEROAnchorOffsetVS){
//                String[] zeroOAnchorLinkSA = zeroAnchorOffsetHT.get(myLinkID);
//                sortedZOAnchorLinksVSA.add(zeroOAnchorLinkSA);
//            }
//            incomingLinksByBOffsetHT.remove("0");
//            incomingLinksByBOffsetHT.put("0", sortedZOAnchorLinksVSA);
//            // -----------------------------------------------------------------
//            produceIndivINCOMINGXmlForm(incomingLinksByBOffsetHT, incomingFPath);
//        }
        // </editor-fold>
    }

    private static void produceIndivINCOMINGXmlForm(Hashtable<String, Vector<String[]>> incomingLinksByBOffsetHT, String incomingFPath) {
//        <bep borel="" boffset="0">
//           <fromanchor farel="" faoffset="9594" falength="8" faanchor="Apple III">IIc Plus</fromanchor>
//        </bep>
        try {
            FileWriter fw = new FileWriter(incomingFPath);
            BufferedWriter bw = new BufferedWriter(fw);
            Enumeration idKeyEnu = incomingLinksByBOffsetHT.keys();
            while (idKeyEnu.hasMoreElements()) {
                Object idKey = idKeyEnu.nextElement();
                log("idKey: " + idKey.toString());
                bw.write("<bep borel=\"0\" boffset=\"" + idKey.toString() + "\">");
                bw.newLine();
                Vector<String[]> myPPT = incomingLinksByBOffsetHT.get(idKey.toString());
                for (String[] thisAnchorSA : myPPT) {
                    String myOffset = thisAnchorSA[0];
                    String myLength = thisAnchorSA[1];
                    String myAnchorTXT = thisAnchorSA[2];
                    String myLinkID = thisAnchorSA[3];
                    bw.write("<fromanchor farel=\"0\" faoffset=\"" + myOffset + "\" falength=\"" + myLength + "\" faanchor=\"" + myAnchorTXT + "\">" + myLinkID + "</fromanchor>");
                    bw.newLine();
                }
                bw.write("</bep>");
                bw.newLine();
            }
            bw.close();
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(poolingMen.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static Hashtable<String, Vector<String[]>> getIncomingLinksByOffset(String topicID) {
        log("topicID: " + topicID);
        String xmlFormDir = "C:\\Users\\Darren\\Desktop\\INEX_2009\\POOL_2009\\POOL2009A2B\\";
        String thisTopicXmlFormFPath = xmlFormDir + "POOL_" + topicID + ".xml";
//        <bep borel="" boffset="0">
//           <fromanchor farel="" faoffset="9594" falength="8" faanchor="Apple III">123456</fromanchor>
//        </bep>
        Hashtable<String, Vector<String[]>> incomingLinksByBOffsetHT = new Hashtable<String, Vector<String[]>>();
        Vector<String[]> inZEROLinkOLAID = new Vector<String[]>();
        Vector<String> inZEROLinkID = new Vector<String>();
        String TOffset = "";
        Vector<String[]> inOLinkOLAID = new Vector<String[]>();
        Vector<String> inOLinkID = new Vector<String>();

        Vector<String> bOffsetVS = new Vector<String>();
        log(thisTopicXmlFormFPath);
        VTDGen vg = new VTDGen();
        if (vg.parseFile(thisTopicXmlFormFPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(thisTopicXmlFormFPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);

                String xPath = "/crosslink-assessment/topic/incominglinks/bep";
                ap.selectXPath(xPath);
                int i = -1;
                while ((i = ap.evalXPath()) != -1) {
                    int j = vn.getAttrVal("boffset");
                    if (j != -1) {
                        String boValue = vn.toRawString(j).trim();
                        if (!bOffsetVS.contains(boValue)) {
                            log("boValue: " + boValue);
                            bOffsetVS.add(boValue);
                        }
                    }
                }
//                bOffsetVS.add("3");
//                bOffsetVS.add("2");
//                bOffsetVS.add("4900");
//                bOffsetVS.add("0");
                for (String thisBOffset : bOffsetVS) {
                    log("thisBOffset: " + thisBOffset);
                    String xPathT = "/crosslink-assessment/topic/incominglinks/bep[@boffset='" + thisBOffset + "']/fromanchor";
                    ap.selectXPath(xPathT);
                    int k = -1;
                    while ((k = ap.evalXPath()) != -1) {
                        int l = vn.getAttrVal("faoffset");
                        String aOffset = vn.toRawString(l).toString();
                        int m = vn.getAttrVal("falength");
                        String aLength = vn.toRawString(m).toString();
                        int n = vn.getAttrVal("faanchor");
                        String aText = vn.toRawString(n).toString();
                        int o = vn.getText();
                        String aLinkID = vn.toRawString(o).toString();
                        if (Integer.valueOf(thisBOffset) > 30) {
                            if (TOffset.equals("")) {
                                TOffset = thisBOffset;
                            }
//                            log("GREATER: " + thisBOffset + " - " + TOffset);
                            if (!inOLinkID.contains(aLinkID)) {
                                inOLinkID.add(aLinkID);
                                inOLinkOLAID.add(new String[]{aOffset, aLength, aText, aLinkID});
                            }
                        } else {
//                            log("LESS: " + thisBOffset);
                            if (!inZEROLinkID.contains(aLinkID)) {
                                inZEROLinkID.add(aLinkID);
                                inZEROLinkOLAID.add(new String[]{aOffset, aLength, aText, aLinkID});
                            }
                        }
                    }
                }
                xm.output(fos);
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //--------------------------------------------------
        log("inZEROLinkOLAID: " + inZEROLinkOLAID.size());
        log("TOffset: " + TOffset);
        log("inOLinkOLAID: " + inOLinkOLAID.size());
        incomingLinksByBOffsetHT.put("0", inZEROLinkOLAID);
        incomingLinksByBOffsetHT.put(TOffset, inOLinkOLAID);
        return incomingLinksByBOffsetHT;
    }

    private static void mergeKellyOUTInABPool(Hashtable<String, Vector<String>> newkellyPoolOutgoingLinksHT, String poolDir) {
        Enumeration topicEnu = newkellyPoolOutgoingLinksHT.keys();
        while (topicEnu.hasMoreElements()) {
            Object topicObj = topicEnu.nextElement();
            String topicID = topicObj.toString();
            String poolFileFPath = poolDir + "POOL_" + topicID + ".xml";
            String topicXmlFileFPath = "C:\\Users\\Darren\\Desktop\\INEX_2009\\LTW2009_TOPICS\\LTW2009_Topics\\XML\\" + topicID + ".xml";
            // -----------------------------------------------------------------
            String topicTitle = getLinkTitle(topicID);
            int ttOffset = txtOffsetFinder(topicXmlFileFPath, topicTitle);
            int ttLength = topicTitle.length();
            Vector<String> newKellyLinkIDVS = newkellyPoolOutgoingLinksHT.get(topicObj.toString());
            // -----------------------------------------------------------------
//            log(String.valueOf(ttOffset) + " - " + String.valueOf(ttLength) + " - " + topicTitle);
            mergeMultiDataToPool(new String[]{String.valueOf(ttOffset), String.valueOf(ttLength), topicTitle}, newKellyLinkIDVS, poolFileFPath);
        }
    }

    private static void mergeMultiDataToPool(String[] myOLTxT, Vector<String> kellyLinkID, String abPoolPath) {
        StringBuffer gtElmnSB = new StringBuffer();
        String myOffset = myOLTxT[0];
        int ttOffset = Integer.valueOf(myOffset);
        String myLength = myOLTxT[1];
        String myAnchorTXT = myOLTxT[2];
        gtElmnSB.append("<anchor arel=\"0\" aname=\"" + myAnchorTXT + "\" aoffset=\"" + myOffset + "\" alength=\"" + myLength + "\">");
        gtElmnSB.append("<subanchor sarel=\"0\" saname=\"" + myAnchorTXT + "\" saoffset=\"" + myOffset + "\" salength=\"" + myLength + "\">");
        for (String linkID : kellyLinkID) {
            gtElmnSB.append("<tobep tbrel=\"0\" timein=\"\" timeout=\"\" tboffset=\"-1\" tbstartp=\"-1\">" + linkID + "</tobep>");
        }
        gtElmnSB.append("</subanchor>");
        gtElmnSB.append("</anchor>");

        VTDGen vg = new VTDGen();
        if (vg.parseFile(abPoolPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(abPoolPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);

                String xPath = "/crosslink-assessment/topic/outgoinglinks/anchor";
                ap.selectXPath(xPath);
                int i = -1;
                while ((i = ap.evalXPath()) != -1) {
                    int j = vn.getAttrVal("aoffset");
                    if (j != -1) {
                        String aOffset = vn.toRawString(j);
                        int thisOffset = Integer.valueOf(aOffset);
                        if (ttOffset == thisOffset) {
                            break;
                        } else if (ttOffset < thisOffset) {
//                            log("<MERGE --- " + myOffset + " bfo " + thisOffset);
                            xm.insertBeforeElement(gtElmnSB.toString());
                            break;
                        }
                    }
                }
                xm.output(fos);
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static Hashtable<String, Vector<String>> getABPoolOutgoingLinks(String abPoolDir) {
        Hashtable<String, Vector<String>> abPoolOutgoingLinksHT = new Hashtable<String, Vector<String>>();
        Vector<String> linksIDVS = null;
        File abPoolFolder = new File(abPoolDir);
        if (abPoolFolder.isDirectory()) {
            File[] abPoolList = abPoolFolder.listFiles();
            for (File abPoolFile : abPoolList) {
                String abPoolFileName = abPoolFile.getName();
//                log("abPoolFileName: " + abPoolFileName);
                // POOL_2117.xml
                if (abPoolFileName.endsWith(".xml")) {
                    String abPoolFileID = abPoolFileName.substring(5, abPoolFileName.lastIndexOf(".xml"));
//                    log("abPoolFileID: " + abPoolFileID);
                    linksIDVS = new Vector<String>();
                    String abPoolFileAbsPath = abPoolFile.getAbsolutePath();
                    VTDGen vg = new VTDGen();
                    if (vg.parseFile(abPoolFileAbsPath, true)) {
                        FileOutputStream fos = null;
                        try {
                            VTDNav vn = vg.getNav();
                            File fo = new File(abPoolFileAbsPath);
                            fos = new FileOutputStream(fo);
                            AutoPilot ap = new AutoPilot(vn);
                            XMLModifier xm = new XMLModifier(vn);
                            String xPath = "/crosslink-assessment/topic/outgoinglinks/anchor/subanchor/tobep";
                            ap.selectXPath(xPath);
                            int i = -1;
                            while ((i = ap.evalXPath()) != -1) {
                                int j = vn.getText();
                                String linkID = vn.toRawString(j).toString().trim();
                                if (linkID.endsWith("\"")) {
                                    linkID = linkID.substring(0, linkID.length() - 1);
                                }
                                if (!linksIDVS.contains(linkID)) {
//                                    log("linkID: " + linkID);
                                    linksIDVS.add(linkID);
                                }
                            }
                            xm.output(fos);
                            fos.close();
                        } catch (IOException ex) {
                            Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (TranscodeException ex) {
                            Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (XPathEvalException ex) {
                            Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (NavException ex) {
                            Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (XPathParseException ex) {
                            Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ModifyException ex) {
                            Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    //--------------------------------------------------
                    abPoolOutgoingLinksHT.put(abPoolFileID, linksIDVS);
                }
            }
            return abPoolOutgoingLinksHT;
        }
        return abPoolOutgoingLinksHT;
    }

    private static Hashtable<String, Vector<String>> getABPoolIncomingLinks(String abPoolDir) {
        Hashtable<String, Vector<String>> abPoolIncomingLinksHT = new Hashtable<String, Vector<String>>();
        Vector<String> linksIDVS = new Vector<String>();
        File abPoolFolder = new File(abPoolDir);
        if (abPoolFolder.isDirectory()) {
            File[] abPoolList = abPoolFolder.listFiles();
            for (File abPoolFile : abPoolList) {
                String abPoolFileName = abPoolFile.getName();
                if (abPoolFileName.endsWith(".xml")) {
                    // POOL_2117.xml
                    String abPoolFileID = abPoolFileName.substring(5, abPoolFileName.lastIndexOf(".xml"));
                    linksIDVS = new Vector<String>();
                    String abPoolFileAbsPath = abPoolFile.getAbsolutePath();
                    VTDGen vg = new VTDGen();
                    if (vg.parseFile(abPoolFileAbsPath, true)) {
                        FileOutputStream fos = null;
                        try {
                            VTDNav vn = vg.getNav();
                            File fo = new File(abPoolFileAbsPath);
                            fos = new FileOutputStream(fo);
                            AutoPilot ap = new AutoPilot(vn);
                            XMLModifier xm = new XMLModifier(vn);
                            String xPath = "/crosslink-assessment/topic/incominglinks/bep/fromanchor";
                            ap.selectXPath(xPath);
                            int i = -1;
                            while ((i = ap.evalXPath()) != -1) {
                                int j = vn.getText();
                                String linkID = vn.toRawString(j).toString().trim();
                                if (linkID.endsWith("\"")) {
                                    linkID = linkID.substring(0, linkID.length() - 1);
                                }
                                if (!linksIDVS.contains(linkID)) {
                                    linksIDVS.add(linkID);
                                }
                            }
                            xm.output(fos);
                            fos.close();
                        } catch (IOException ex) {
                            Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (TranscodeException ex) {
                            Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (XPathEvalException ex) {
                            Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (NavException ex) {
                            Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (XPathParseException ex) {
                            Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ModifyException ex) {
                            Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    //--------------------------------------------------
                    abPoolIncomingLinksHT.put(abPoolFileID, linksIDVS);
                }
            }
        }
        return abPoolIncomingLinksHT;
    }

    private static Hashtable<String, Vector<String>> getKellyPoolOutgoingLinks(String kellyLinksDir) {
        Hashtable<String, Vector<String>> kellyPoolOutgoingLinksHT = new Hashtable<String, Vector<String>>();
        Vector<String> kellyOutgoingVS = null;
        File kellyPoolFolder = new File(kellyLinksDir);
        if (kellyPoolFolder.isDirectory()) {
            File[] kellyPoolList = kellyPoolFolder.listFiles();
            for (File abPoolFile : kellyPoolList) {
                String abPoolFileName = abPoolFile.getName();
                String abPoolFileAbsPath = abPoolFile.getAbsolutePath();
                if (abPoolFileAbsPath.endsWith("_outgoing.txt")) {
                    String kellyPoolID = abPoolFileName.substring(0, abPoolFileName.lastIndexOf("_outgoing.txt"));
                    kellyOutgoingVS = getKellyLinks(abPoolFileAbsPath);
                    kellyPoolOutgoingLinksHT.put(kellyPoolID, kellyOutgoingVS);
                }
            }
        }
        return kellyPoolOutgoingLinksHT;
    }

    private static Hashtable<String, Vector<String>> getKellyPoolIncomingLinks(String kellyLinksDir) {
        Hashtable<String, Vector<String>> kellyPoolIncomingLinksHT = new Hashtable<String, Vector<String>>();
        Vector<String> kellyIncomingVS = null;
        File kellyPoolFolder = new File(kellyLinksDir);
        if (kellyPoolFolder.isDirectory()) {
            File[] kellyPoolList = kellyPoolFolder.listFiles();
            for (File abPoolFile : kellyPoolList) {
                String abPoolFileName = abPoolFile.getName();
                String abPoolFileAbsPath = abPoolFile.getAbsolutePath();
                if (abPoolFileAbsPath.endsWith("_incoming.txt")) {
                    String kellyPoolID = abPoolFileName.substring(0, abPoolFileName.lastIndexOf("_incoming.txt"));
                    kellyIncomingVS = getKellyLinks(abPoolFileAbsPath);
                    kellyPoolIncomingLinksHT.put(kellyPoolID, kellyIncomingVS);
                }
            }
        }
        return kellyPoolIncomingLinksHT;
    }

    private static Vector<String> getKellyLinks(String linkPath) {
        FileInputStream txtFs = null;
        Vector<String> myLinksV = new Vector<String>();
        try {
            File txtFile = new File(linkPath);
            txtFs = new FileInputStream(txtFile.getAbsoluteFile());
            DataInputStream txtIn = new DataInputStream(txtFs);
            BufferedReader txtBr = new BufferedReader(new InputStreamReader(txtIn));
            String thisLine = "";
            int counter = 0;
            while ((thisLine = txtBr.readLine()) != null) {
                if (!myLinksV.contains(thisLine.trim())) {
                    myLinksV.add(thisLine.trim());
                }
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
        return myLinksV;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Offset Finder">
    private static int txtOffsetFinder(String xmlLinkFullPath, String anchorTXT) {
//        log(xmlLinkFullPath);
//        log("anchorTXT: " + anchorTXT);
        Vector<String> txtSglCharV = new Vector<String>();
        String wikipediaTxt = ConvertXMLtoTXT(xmlLinkFullPath, "", true);
        for (int i = 0; i < wikipediaTxt.length(); i++) {
            String mySingle = wikipediaTxt.substring(i, i + 1);
            txtSglCharV.add(mySingle);
        } // ---------------------------------------------------------------------
        Vector<String> titleSglCharV = new Vector<String>();
        for (int i = 0; i < anchorTXT.trim().length(); i++) {
            String mySingle = anchorTXT.trim().substring(i, i + 1);
            titleSglCharV.add(mySingle);
        } // ---------------------------------------------------------------------
        int myOffset = 0;
        boolean matchFlag = true;
        for (int i = 0; i < txtSglCharV.size(); i++) {
            String thisTxtChar = txtSglCharV.elementAt(i);
            if (thisTxtChar.equals(titleSglCharV.elementAt(0))) {
                myOffset = i;
                int myCounter = 0;
                for (int j = i; j < i + anchorTXT.trim().length() - 1; j++) {
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
                        break;
                    }
                }
                if (matchFlag) {
                    return myOffset;
                }
            }
        }
        // ---------------------------------------------------------------------
        return myOffset;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Wikipedia Page Title Finder">
    private static String xmlUnicode = "utf-8";
    private static boolean isTitleFlag = false;
    private static String wikipediaXmlDir = "G:\\PHD_Tools\\Wikipedia_2009_Collection\\XML\\";
    static StringBuffer htmlSB = null;

    private static String getLinkTitle(String myLinkID) {
        String thisPageTitle = "";
        htmlSB = new StringBuffer();
        String xmlPath = getWikipediaFilePathByName(myLinkID + ".xml");
        pageTitleExtractor(
                xmlPath);




        return thisPageTitle = htmlSB.toString();




    }

    private static void pageTitleExtractor(String xmlPath) {
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(xmlPath));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, xmlUnicode);
            InputSource inputSource = new InputSource(inputStreamReader);

            // Parse the XML File
            DOMParser parser = new DOMParser();
            parser.parse(inputSource);
            Document myDoc = parser.getDocument();

            pageElmnFinder(
                    myDoc, htmlSB);

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




                } // Recurse each child: e.g. TEXT ---------------------------
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

    // <editor-fold defaultstate="collapsed" desc="mergeGTtoABPools">
    private static void mergeGTtoABPools(String gtPoolDir, String abPoolDir, String myPoolDir) {
        String linkFolderT = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS\\anchorLinkPairPerTopic";
        String topicAnchorIndexFolder = "C:\\PHD\\INEX_2009\\LTW2009_TOPICS\\LTW2009_Topics\\TopicAnchors_Index";
        // 1) Get GT-Pool Data into Vector & Hashtable
        Hashtable<String, Vector<String[]>> gtTopicDataSetHT = gtDataExtractor(linkFolderT, topicAnchorIndexFolder);
        // 2) Loop AB-Pool to INSERT
        Enumeration keyEnu = gtTopicDataSetHT.keys();




        while (keyEnu.hasMoreElements()) {
            Object keyObj = keyEnu.nextElement();
            String topicID = keyObj.toString();
//            log("Merge POOL -- " + topicID);
            String abPoolPath = abPoolDir + "POOL_" + topicID + ".xml";
            Vector<String[]> gtOLTxTIDVSA = gtTopicDataSetHT.get(keyObj.toString());




            for (String[] gtOLTxTID : gtOLTxTIDVSA) {
                int thisOffset = Integer.valueOf(gtOLTxTID[0]);
                // -------------------------------------------------------------
                // Loop A2B Pool to find out the POSITION to insert
                String newPoolPath = myPoolDir + "NEWPOOL_" + topicID + ".xml";
                mergeDataToPool(
                        thisOffset, gtOLTxTID, abPoolPath, newPoolPath);





            }
        }
    }

    private static void mergeDataToPool(int myOffset, String[] gtOLTxTID, String abPoolPath, String newPoolFilePath) {
        StringBuffer gtElmnSB = new StringBuffer();
        String myLength = gtOLTxTID[1];
        String myAnchorTXT = gtOLTxTID[2];
        String myLinkID = gtOLTxTID[3];
        gtElmnSB.append("<anchor arel=\"0\" aname=\"" + myAnchorTXT + "\" aoffset=\"" + myOffset + "\" alength=\"" + myLength + "\">");
        gtElmnSB.append("<subanchor sarel=\"0\" saname=\"" + myAnchorTXT + "\" saoffset=\"" + myOffset + "\" salength=\"" + myLength + "\">");
        gtElmnSB.append("<tobep tbrel=\"0\" timein=\"\" timeout=\"\" tboffset=\"-1\" tbstartp=\"-1\">" + myLinkID + "</tobep>");
        gtElmnSB.append("</subanchor>");
        gtElmnSB.append("</anchor>");

        VTDGen vg = new VTDGen();





        if (vg.parseFile(abPoolPath, true)) {
            FileOutputStream fos = null;




            try {
                VTDNav vn = vg.getNav();
                File fo = new File(abPoolPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);

                String xPath = "/crosslink-assessment/topic/outgoinglinks/anchor";
                ap.selectXPath(xPath);




                int i = -1;




                while ((i = ap.evalXPath()) != -1) {
                    int j = vn.getAttrVal("aoffset");




                    if (j != -1) {
                        String aOffset = vn.toRawString(j);




                        int thisOffset = Integer.valueOf(aOffset);




                        if (myOffset < thisOffset) {
//                            log("<MERGE --- " + myOffset + " bfo " + thisOffset);
                            xm.insertBeforeElement(gtElmnSB.toString());




                            break;




                        }
//                        else if (myOffset == thisOffset) {
//                            String xPath2 = "/crosslink-assessment/topic/outgoinglinks/anchor/subanchor/tobep";
//                            ap.selectXPath(xPath2);
//                            int k = -1;
//                            boolean isINSERT = true;
//                            while ((k = ap.evalXPath()) != -1) {
//                                String linkID = vn.toRawString(k).toString();
//                                if (myLinkID.equals(linkID)) {
//                                    log("Duplicated LINK ID - " + myLinkID + " : " + linkID);
//                                    isINSERT = false;
//                                    break;
//                                }
//                            }
//                            if (isINSERT) {
//                                log("=MERGE --- " + myOffset + " bfo " + thisOffset);
//                                xm.insertBeforeElement(gtElmnSB.toString());
//                                break;
//                            }
//                        }
                    }
                }
                xm.output(fos);
                fos.close();






            } catch (IOException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
            }




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
                        Logger.getLogger(mergePools.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(poolingMen.class.getName()).log(Level.SEVERE, null, ex);
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
}
