package crosslink;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ximpleware.AutoPilot;
import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.TranscodeException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

/*
 * This class will create the result set for evaluation 
 */
public class Pool2ResultSet {
	private String resultSetFile;
	private String poolDir;

    private static Vector<String[]> topicIDName = new Vector<String[]>();
    // Hashtable<TopicID, Vector<new String[]{aoffset, alength, aname, bepOffset, bepFileID}>>
    private static Hashtable<String, Vector<String[]>> topicOutData = new Hashtable<String, Vector<String[]>>();
    // Hashtable<TopicID, Vector<new String[]{boffset, aOffset, alength, aname, aFileID}>>
    private static Hashtable<String, Vector<String[]>> topicInData = new Hashtable<String, Vector<String[]>>();
    
    public Pool2ResultSet() {

	}

	public Pool2ResultSet(String resultSetFile, String poolDir) {
		super();
		this.resultSetFile = resultSetFile;
		this.poolDir = poolDir;
	}

	static void log(Object obj) {
        System.out.println(obj.toString());
    }
    
    public void produceA2BManualResultSet() {
        populateTopicOutInData(poolDir);
        // ---------------------------------------------------------------------
        produceA2BManualResultSet(topicIDName, topicOutData, topicInData);
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
                            Logger.getLogger(Pool2ResultSet.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (TranscodeException ex) {
                            Logger.getLogger(Pool2ResultSet.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (XPathEvalException ex) {
                            Logger.getLogger(Pool2ResultSet.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (NavException ex) {
                            Logger.getLogger(Pool2ResultSet.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (XPathParseException ex) {
                            Logger.getLogger(Pool2ResultSet.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ModifyException ex) {
                            Logger.getLogger(Pool2ResultSet.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
    }
    
    private void produceA2BManualResultSet(Vector<String[]> myTopicIDName, Hashtable<String, Vector<String[]>> myTopicOutData, Hashtable<String, Vector<String[]>> myTopicInData) {
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
//            String rDir = "C:\\JTemp\\LTWEvaluationTool\\Resources\\";
//            String resultSetFile = rDir + "33A2BManualResultSet_FullOutName.xml";
            FileWriter fw = new FileWriter(new File(resultSetFile));
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
            Logger.getLogger(Pool2ResultSet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Usage: program pool_path resultset_file");
			System.exit(-1);
		}

//		resultSetFile = args[1];
//		poolDir = args[0];
		Pool2ResultSet pool2ResultSet = new Pool2ResultSet(args[0], args[1]);
		pool2ResultSet.produceA2BManualResultSet();
	}
}
