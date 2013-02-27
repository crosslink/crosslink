package crosslink.measures;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import crosslink.rungenerator.InexSubmission;

/**
 * @author Darren Huang
 */
public final class fileToBepMeasures extends Measures {


    public static int R_MAP = 0;
    public static int R_RPREC = 1;
    public static int R_P5 = 2;
    public static int R_P10 = 3;
    public static int R_P20 = 4;
    public static int R_P30 = 5;
    public static int R_P50 = 6;
    public static int R_P250 = 7;
    public static int COL_NUM = 8;
    public static int RESULT_TYPE_NUM = 3;
//    private static String runStatic;

    private static Hashtable<String, Vector> runHT = null;
    private static Hashtable<String, Vector> resultLinks = null;
    private static int rsAnchorNoPerTopic;
    private static Vector runIndexV = null;
	private static Vector runBepV;
    
    public fileToBepMeasures() {
    }

    public static void init(String[] runValues, String[] resultSet) {
        // -------------------------------------------------------------
    	runHT = new Hashtable<String, Vector>();
        resultLinks = new Hashtable<String, Vector>();
    	
        runBepV = new Vector();
        runIndexV = new Vector();
        String currentIndex = "";
        for (int i = 0; i < runValues.length; i++) {
            String runLink = runValues[i].trim();
            String[] linkSet = runLink.split("_");
            String runAnchorOffset = linkSet[0].trim();
            String runAnchorLength = linkSet[1].trim();
            String runbFileID = linkSet[2].trim();
            if (linkSet[3].equals("/article[1]")) {
                linkSet[3] = "0";
            }
            int runbBEP = Integer.valueOf(linkSet[3].trim());

            String index = runAnchorOffset + "_" + runAnchorLength;
            String bepLink = runbFileID + "_" + runbBEP;

            if (currentIndex.equals("")) {
                currentIndex = index;
                runIndexV.add(index);
                runBepV.add(bepLink);
                runHT.put(index, runBepV);
            } else if (runIndexV.contains(index) && runHT.containsKey(index)) {
                // same Anchor
                Vector v = (Vector) runHT.get(index);
                if (v.size() < defaultMaxBepsPerAnchor) {
                    v.add(bepLink);
                    runHT.remove(index);
                    runHT.put(index, v);
                }
            } else if (!runIndexV.contains(index) && !runHT.containsKey(index)) {
                // diff Anchor
                runBepV = new Vector();
                runIndexV.add(index);
                runBepV.add(bepLink);
                runHT.put(index, runBepV);
            }
        }
        rsAnchorNoPerTopic = resultSet.length;
        // ---------------------------------------------------------
        for (int l = 0; l < resultSet.length; l++) {
            String[] rsSet = resultSet[l].trim().split("_");
            String rsAnchorOffset = rsSet[0].trim();
            String rsAnchorLength = rsSet[1].trim();
            String rsbFileID = rsSet[2].trim();
            int rsbBEP = Integer.valueOf(rsSet[3].trim());
            String valueString;
            
            if (isAnchorGToBEP)
            	valueString = rsbFileID + "_" + rsbBEP;
            else
            	valueString = rsbFileID + "_0";
            String keyString = rsAnchorOffset + "_" + rsAnchorLength;
//            if (!aaptV.contains(rsAnchorOffset + "_" + rsAnchorLength)) {
//                aaptV.add(keyString);
//            }
            if (resultLinks.contains(keyString)) {
            	resultLinks.get(keyString).add(valueString);
            }
            else {
            	Vector anchorLinks = new Vector();
            	anchorLinks.add(valueString);
            	resultLinks.put(keyString, anchorLinks);
            }
        }
    }
    
    public static metricsCalculation.EvaluationResult getFileToBepResult(/*File resultfiles, */File runfile, boolean isAllTopics, boolean useFileToBep, boolean useAnchorToFile, boolean useAnchorToBEP, int lang, int linkDirection) throws Exception {

        isUseAllTopics = isAllTopics ? true : false;
        isFileToBEP = useFileToBep ? true : false;
        isAnchorGToFile = useAnchorToFile ? true : false;
        isAnchorGToBEP = useAnchorToBEP ? true : false;

        metricsCalculation.EvaluationResult result = null;
        Hashtable resultTable = null;
        Hashtable runTable = null;

        String resultfile;
        
//        if (isFileToBEP) {
//            // the performance is measured by each File-to-BEP
//            // so the result is calculated by each File-to-BEP in Run against the ONE in ResultSet
//            runTable = fileToBepMeasures.getF2BRunSet(runfiles);
//            resultTable = fileToBepMeasures.getF2BResultSet(resultfiles);
//        } else if (isAnchorGToFile || isAnchorGToBEP) {
//            runTable = fileToBepMeasures.getF2BRunSetByGroup(runfiles);
//			  resultTable = fileToBepMeasures.getF2BResultSetByGroup(resultfiles);
//            }
//        }
        if (isFileToBEP) {
            // the performance is measured by each File-to-BEP
            // so the result is calculated by each File-to-BEP in Run against the ONE in ResultSet
            runTable = getF2BRunSet(runfile, lang);
            if (runTable != null) {
	            resultfile = ResultSetManager.getInstance().getResultSetPathFile(currentSourceLang, currentTargetLang);
	            resultTable = ResultSetManager.getInstance().getResultSet(resultfile);
	            if (resultTable == null) {
	            	resultTable = getF2BResultSet(new File(resultfile));
	            	ResultSetManager.getInstance().addResultSet(resultfile, resultTable);
	            }
            }
        } else if (isAnchorGToFile || isAnchorGToBEP) {
            runTable = getF2BRunSetByGroup(runfile, lang, linkDirection);
            if (runTable != null) {
	            resultfile = ResultSetManager.getInstance().getResultSetPathFile(currentSourceLang, currentTargetLang);
	            resultTable = ResultSetManager.getInstance().getResultSet(resultfile);
	            if (resultTable == null) {
	            	resultTable = getF2BResultSetByGroup(new File(resultfile));
	            	ResultSetManager.getInstance().addResultSet(resultfile, resultTable);
	            }
            }
        } else {
        	throw new Exception("Uncertain evaluation type");
        }
        
        if (runTable != null) {
        	result = new metricsCalculation.EvaluationResult();
	        // =====================================================================
	        // 1) result: get run ID
	        result.runId = runId;
	
	        String tempRunID = result.runId;
            System.err.println("================================================================================");
            System.err.println("==================  Doing calculation for " + tempRunID);
            System.err.println("================================================================================");
	        // =====================================================================
	        // Get MAP
            System.err.println("Calculating the Link Mean Average Precision:");
	        double[] oiMAP = getMAP(tempRunID, resultTable, runTable);
	
	        double outgoingMAP = oiMAP[0];
	        double outMAP = 0.00001 * Math.random();
	        if (outgoingMAP > 0) {
	            outgoingMAP = outMAP + outgoingMAP;
	        }
	
	        double incomingMAP = oiMAP[1];
	        double MAP = 0.0001 * Math.random();
	
	        if ((outgoingMAP + incomingMAP) > 0) {
	            MAP = MAP + (double) 2 * (outgoingMAP * incomingMAP) / (outgoingMAP + incomingMAP);
	        }
	        result.outgoing[metricsCalculation.R_MAP] = outgoingMAP;
	        result.incomming[metricsCalculation.R_MAP] = incomingMAP;
	        result.combination[metricsCalculation.R_MAP] = MAP;
	        
	
	        // =====================================================================
	        // Get R-Precision
            System.err.println("Calculating the R-Precision:");
	        double[] oiRPrecs = getRPrecs(tempRunID, resultTable, runTable);
	        double AveoutgoingRPrec = oiRPrecs[0];
	        double AveincomingRPrecs = oiRPrecs[1];
	        double AveRPrecs = 0.0;
	        if ((AveoutgoingRPrec + AveincomingRPrecs) > 0) {
	            AveRPrecs = (double) 2 * (AveoutgoingRPrec * AveincomingRPrecs) / (AveoutgoingRPrec + AveincomingRPrecs);
	        }
	        result.outgoing[metricsCalculation.R_RPREC] = AveoutgoingRPrec;
	        result.incomming[metricsCalculation.R_RPREC] = AveincomingRPrecs;
	        result.combination[metricsCalculation.R_RPREC] = AveRPrecs;
	        
	        
	        // =====================================================================
	        // Get Precision@
            System.err.println("Calculating the Precision @ N:");
	        double[][] oiPrecsAT = getPrecsAT(tempRunID, resultTable, runTable);
	        double[] AveoutgoingPrecsAt = {oiPrecsAT[0][0], oiPrecsAT[0][1], oiPrecsAT[0][2], oiPrecsAT[0][3], oiPrecsAT[0][4], oiPrecsAT[0][5]};
	        double[] AveincomingPrecsAt = {oiPrecsAT[1][0], oiPrecsAT[1][1], oiPrecsAT[1][2], oiPrecsAT[1][3], oiPrecsAT[1][4], oiPrecsAT[1][5]};
	        for (int i = 0; i < 6; i++) {
	            double AvePrecsAt = 0.0;
	            if ((AveoutgoingPrecsAt[i] + AveincomingPrecsAt[i]) > 0) {
	                AvePrecsAt = (double) 2 * (AveoutgoingPrecsAt[i] * AveincomingPrecsAt[i]) / (AveoutgoingPrecsAt[i] + AveincomingPrecsAt[i]);
	            }
	            result.outgoing[metricsCalculation.R_P5 + i] = AveoutgoingPrecsAt[i];
	            result.incomming[metricsCalculation.R_P5 + i] = AveincomingPrecsAt[i];
	            result.combination[metricsCalculation.R_P5 + i] = AvePrecsAt;
	        }
        }

        return result;
    }

    // =========================================================================
    private static double[] getMAP(String tempRunId, Hashtable resultTable, Hashtable runTable) {

        /*
         * resultTable: topicID_Outgoing_Links, <String[]>outLinkArray
         *              <String[]>outLinkArray:
         * runTable:    topicID_Outgoing_Links, <String[]>outLinkArray
         *              <String[]>outLinkArray:
         */
        double[] oimap = new double[2];
        double aTopicAvePrecision = 0.0;
        double outgoingAPs = 0.0;
        double incomingAPs = 0.0;
        ArrayList<TopicScore> topicScores = new ArrayList<TopicScore>();
        // =============================================================
        Hashtable outgoingHt = new Hashtable();
        Hashtable incomingHt = new Hashtable();
        // =====================================================================
        // Loop all Topics (topicID_Outgoing_Links) in the submitted run
        for (Enumeration e = runTable.keys(); e.hasMoreElements();) {
            int ffmCount = 0;
            double anchorScore = 0.0;
            String key = e.nextElement().toString();
//            log("Topic: " + key);
//            if (key.contains("22156522"))
//            	System.err.println("Got you");
            // get RUN (incoming or outgoing) links array for each Topic
            String[] runValues = (String[]) runTable.get(key);
            // get Result (incoming or outgoing) links array for each Topic
            String[] resultSet = (String[]) resultTable.get(key);
            // =================================================================
            // Run:
            // group BEP links by Anchor
            // ResultSet: 
            // get total number of Anchors for this Topic: key: Outgoing / Incoming
//            int rsAnchorNoPerTopic = 0;
            // -----------------------------------------------------------------
            // Loop all run Data (aOffset_aLength_bFileID_bOffset) Vector &
            // re-arrange them into:
            //    runIndexV: that stores All Anchor pairs (aOffset_aLength)
            //    runHT: that stores All BEP pairs for each AnchorIndex (aOffset_aLength, Vector<bFileID_bOffset>)
            //           eliminate All duplicate Data
            String topicId = key.substring(0, key.indexOf("_"));
            if (isAnchorGToFile || isAnchorGToBEP) {
                if (key.endsWith("Outgoing_Links")) {
                	init(runValues, resultSet);
                } else if (key.endsWith("Incoming_Links")) {
                    // Currently there is NO incoming links
                    // Incoming links should be further implemented
                }
            }
            // -----------------------------------------------------------------
            // =================================================================
            // case1: If Run only contains an Empty String (No return Value),
            //       1, topicAP = 1 when no value in ResultSet
            //       2, topicAP = 1 when there is value in ResultSet
            // case2: If Run contains equal or more than onr Value,
            if (runValues.length == 1 && runValues[0].equalsIgnoreCase("")) {
                if (resultSet.length == 1 && resultSet[0].equalsIgnoreCase("")) {
                    aTopicAvePrecision = 1.0;
                } else {
                    aTopicAvePrecision = 0.0;
                }
                // =============================================================
                if (key.endsWith("Outgoing_Links")) {
                    outgoingHt.put(key.substring(0, key.indexOf("_Outgoing_Links")), "0");
                } else if (key.endsWith("Incoming_Links")) {
                    incomingHt.put(key.substring(0, key.indexOf("_Incoming_Links")), "0");
                }
                // =============================================================
            } else {
                // =============================================================
                if (isAnchorGToFile || isAnchorGToBEP) {
                    // <editor-fold defaultstate="collapsed" desc="Anchor-to-BEP & Anchor-to-File">
                    double APinR = 0.0;
                    if (key.endsWith("Outgoing_Links")) {
                        // run link --> aOffset_aLength_bFileID_bBEP
                        // result set --> aOffset_aLength_bFileID_bBEP
                        // -----------------------------------------------------
                        int anchorCounter = 1;
                        // Loop each Anchor (aOffset_aLength) in a Topic
                        // to calculate Each Anchor's Score
                        Enumeration runAnchorEnu = runIndexV.elements();
                        while (runAnchorEnu.hasMoreElements()) {
                            // get Run BEP links (Vector) by Anchor Index
                            Object runAnchorSet = runAnchorEnu.nextElement();
                            Vector runBepSetV = (Vector) runHT.get(runAnchorSet.toString());
                            String[] runAnchorOL = runAnchorSet.toString().split("_");
                            if (runBepSetV == null || runAnchorOL == null || runAnchorOL[0].equalsIgnoreCase("null")) {
                            	System.err.println("Error: " + runAnchorSet.toString());
                            }
//                            log("runAnchorSet: " + runAnchorSet.toString());
                            int runAStartPoint = Integer.valueOf(runAnchorOL[0]);
                            int runAEndPoint = Integer.valueOf(runAnchorOL[0]) + Integer.valueOf(runAnchorOL[1]);
                            
                            Vector resultBeps = resultLinks.get(runAnchorSet);
                            if (resultBeps != null) {
	                            // -------------------------------------------------
	                            // Anchor Group: multiple BEP links in an Anchor
	                            boolean isMatched = false;
	                            double bepScore = 0.0;
	                            Enumeration bSetEnu = runBepSetV.elements();
	                            while (bSetEnu.hasMoreElements()) {
	                                Object runBepSet = bSetEnu.nextElement();
	                                // bFileID_bOffset
	                                String[] bepLink = runBepSet.toString().split("_");
	                                String runBepID = bepLink[0].trim();
	                                int runBepOffset = Integer.valueOf(bepLink[1].trim());
	                                // ---------------------------------------------
	                                // Loop every BEP link from each Anchor in RS
	                                Vector<Double> bepsDisV = new Vector<Double>();
	
	                                if (!isAnchorGToBEP) {
		                            	if (resultBeps.contains(runBepID + "_0"))
		                            		bepsDisV.add(0.0);
	                                }
	                                else {
	//                                for (int m = 0; m < resultSet.length; m++) {
	//                                    // aOffset_aLength_bFileID_bOffset
	//                                    String[] rsSet = resultSet[m].trim().split("_");
	//                                    int rsaStartPoint = Integer.valueOf(rsSet[0].trim());
	//                                    int rsaEndPoint = Integer.valueOf(rsSet[0].trim()) + Integer.valueOf(rsSet[1].trim());
	//                                    // -----------------------------------------
	//                                    if (useOnlyAnchorGroup) {
	//                                        // -------------------------------------
	//                                        // 2) to find matched BEP file ID in RS
	//                                        String rsbFileID = rsSet[2].trim();
	//                                        int rsbBEP = Integer.valueOf(rsSet[3].trim());
	//                                        // run BEP file ID matches RS BEP file ID
	//                                        if (runBepID.equalsIgnoreCase(rsbFileID)) {
	//                                            if (Math.abs(runBepOffset - rsbBEP) <= distanceFactor) {
	//                                                double bd = 0;
	//                                                if (isAnchorGToBEP) {
	//                                                    bd = Math.abs(runBepOffset - rsbBEP);
	//                                                }
	//                                                bepsDisV.add(bd);
	//                                            } else {
	//                                                double bd = 0;
	//                                                if (isAnchorGToBEP) {
	//                                                    bd = distanceFactor;
	//                                                }
	//                                                bepsDisV.add(bd);
	//                                            }
	//                                        }
	//                                    } else {
	//                                        // 1) to match Anchor Offset & Length
	////                                        if ((runAStartPoint >= rsaStartPoint && runAStartPoint <= rsaEndPoint) ||
	////                                                (runAEndPoint >= rsaStartPoint && runAEndPoint <= rsaEndPoint)) {
	//                                        if ((runAStartPoint == runAStartPoint ) &&
	//                                                (runAEndPoint == rsaEndPoint)) {
	//                                            // -------------------------------------
	//                                            // 2) to find matched BEP file ID in RS
	//                                            String rsbFileID = rsSet[2].trim();
	//                                            int rsbBEP = Integer.valueOf(rsSet[3].trim());
	//                                            // run BEP file ID matches RS BEP file ID
	//                                            if (runBepID.equalsIgnoreCase(rsbFileID)) {
	//                                                if (Math.abs(runBepOffset - rsbBEP) <= distanceFactor) {
	//                                                    double bd = 0;
	//                                                    if (isAnchorGToBEP) {
	//                                                        bd = Math.abs(runBepOffset - rsbBEP);
	//                                                    }
	//                                                    bepsDisV.add(bd);
	//                                                } else {
	//                                                    double bd = 0;
	//                                                    if (isAnchorGToBEP) {
	//                                                        bd = distanceFactor;
	//                                                    }
	//                                                    bepsDisV.add(bd);
	//                                                }
	//                                            }
	//                                        }
	//                                        // End of Anchor & BEP links Check
	//                                    }
	//                                    // -----------------------------------------
	//                                }
	                                }
	                                // End of Loop All RS to find match
	                                // ---------------------------------------------
	                                if (bepsDisV.size() > 0) {
	                                    /**
	                                     * This situation should not happen??? However,
	                                     * in case, if more than one anchor OL is matched (overlapped) and
	                                     * the BEP file ID with BEP is within range,
	                                     * we take the one with the shortest distance.
	                                     */
	                                    double shortestDis = Collections.min(bepsDisV);
	                                    double bs = 0.0;
	                                    if (isAnchorGToBEP) {
	                                        // A2B formula: at least get score 1 when the BEP file ID is natched
	                                        bs = (distanceFactor - 0.9 * shortestDis) / distanceFactor;
	                                    } else {
	                                        // A2F: if BEP file ID is matched, get score 1
	                                        // It means shortestDis = 0 & bs = 1
	                                        bs = (distanceFactor - shortestDis) / distanceFactor;
	                                    }
	                                    bepScore = bepScore + bs;
	                                    isMatched = true;
	                                }
	                                // End of looping BEPs in an Anchor
	                            }
	                            // -------------------------------------------------
	                            if (isMatched) {
	                                // To calculate Precision Score for this Anchor
	                                // runBepSetV.size() cannot be 0
	                                if (bepDenominator <= 0) {
	                                    if (runBepSetV.size() != 0) {
	                                        int Denominator = Math.min(5, runBepSetV.size()); //resultBeps.size() > 5 ? 5 : resultBeps.size(); 
	                                        anchorScore += (double) bepScore / Denominator;
	                                    }
	                                } else {
	                                    anchorScore += (double) bepScore / bepDenominator;
	                                }
	                                // Precision = Relevant Documents Retrieved / Nu of Retrieved Documents at R
	                                // High precision indicates that most of the items you retrieve are relevant.
	                                // Here: we cumulate Precision Value of Each Anchor in a Topic
	                                APinR += (double) anchorScore / (anchorCounter);
	//                                log("anchorScore: " + anchorScore);
	//                                log("anchorCounter: " + anchorCounter);
	                                isMatched = false;
	                            }
	                            anchorCounter++;
	                        }   // End of looping Anchors in a Topic
                        }
                        // THEN, calculate the Precision for the Topic
                        // Basically, APinR should be divided by "rsAnchorNoPerTopic" <-- number of Anchors in the ResultSet Topic.
                        // BUT this target is File-to-BEP, we don't know Exactly how many Anchors in the ResultSet
                        //     and this Number may be meaningless
                        // Here, we temporarily use "anchorCounter" in Run to replace it
                        // to prevent from producing funny result
//                         aTopicAvePrecision = (double) APinR / rsAnchorNoPerTopic;
//                        aTopicAvePrecision = (double) APinR / anchorCounter;
                        int N = Math.min(250, resultLinks.size()/*resultSet.length*/);
//                        log("N: " + N);
                        aTopicAvePrecision = (double) APinR / N;

                    } else if (key.endsWith("Incoming_Links")) {
                        aTopicAvePrecision = 0.0;
                    }
                    // </editor-fold>
                } else {
                    // <editor-fold defaultstate="collapsed" desc="File-to-BEP without Grouping">
                    // bepFileID_bepOffset in Run against bepFileID_bepOffset in ResultSet
                    /**
                     * The same outgoing link (BepFileID) may have multiple destinations (BepOffset)
                     * but these multiple destinations can only be counted as 1
                     * Precision = Relevant Documents Retrieved / Nu of Retrieved Documents at R
                     * Recall = Relevant Documents Retrieved / Total Relevant Docuements
                     */
                    if (key.endsWith("Outgoing_Links")) {
                        double APinR = 0.0;
                        double bepLinkScore = 0.0;
                        int bepLinkCounter = 0;
                        List relData = new ArrayList();
                        // =========================================================
                        // Loop "bepFileID_bepOffset" in Run
                        for (int i = 0; i < runValues.length; i++) {
                            String link = runValues[i].trim();
                            // duplicate links should NOT include
                            // in the submission run
                            if (!relData.contains(link)) {
                                relData.add(link);
                                bepLinkCounter = bepLinkCounter + 1;

                                String[] linkSet = link.split("_");
                                String runbFileID = linkSet[0].trim();
                                if (linkSet[1].equals("/article[1]")) {
                                    linkSet[1] = "0";
                                }
                                int runbBEP = Integer.valueOf(linkSet[1].trim());
                                // Loop all bepLinks in ResultSet
                                // To caculate Average Precision in each topic
                                Vector<Double> bepsDisV = new Vector<Double>();
                                for (int j = 0; j < resultSet.length; j++) {
                                    // To find out if a RUN link in the Result Set (links)
                                    // link --> bFileID_bOffset
                                    String[] rsSet = resultSet[j].trim().split("_");
                                    String rsbFileID = rsSet[0].trim();
                                    int rsbBEP = Integer.valueOf(rsSet[1].trim());
                                    // =========================================
                                    if (runbFileID.equalsIgnoreCase(rsbFileID)) {
                                        if (Math.abs(runbBEP - rsbBEP) <= distanceFactor) {
                                            double bd = Math.abs(runbBEP - rsbBEP);
                                            bepsDisV.add(bd);
                                        } else {
                                            double bd = distanceFactor;
                                            bepsDisV.add(bd);
                                        }
                                    }
                                }   // End of Loop all bepLinks in ResultSet
                                if (bepsDisV.size() > 0) {
                                    double shortestDis = Collections.min(bepsDisV);
                                    double fw = (distanceFactor - 0.9 * shortestDis) / distanceFactor;
                                    bepLinkScore = bepLinkScore + fw;
//                                    APinR += (double) bepLinkScore / (bepLinkCounter);
                                    APinR += (double) bepLinkScore / (bepLinkCounter);
                                }
                            }   // End of Unique bepLink Measurement
                        }   // End of Looping links: "bepFileID_bepOffset" in a Topic
                        // =====================================================
                        aTopicAvePrecision = (double) APinR / (resultSet.length);
                        // =====================================================
                        if (key.endsWith("Outgoing_Links")) {
                            outgoingHt.put(key.substring(0, key.indexOf("_Outgoing_Links")), relData.size());
                        } else if (key.endsWith("Incoming_Links")) {
                            incomingHt.put(key.substring(0, key.indexOf("_Incoming_Links")), relData.size());
                        }
                    } else if (key.endsWith("Incoming_Links")) {
                        aTopicAvePrecision = 0.0;
                    }
                    // End of File-to-BEP without Grouping
                    // </editor-fold>
                }
            }   // End of calculating a Topic AP

            // Add "aTopicAvePrecision" to related APs
            if (key.endsWith(outgoingTag)) {
            	TopicScore score = new TopicScore(topicId, aTopicAvePrecision); 
                topicScores.add(score);
                outgoingAPs += aTopicAvePrecision;
            } else if (key.endsWith(incomingTag)) {
                incomingAPs += aTopicAvePrecision;
            }
        }   // End of Looping All Topics

        outputTopicScore(topicScores);
        
        // =====================================================================
        // The submission run may only contains a certain number of Topics (NOT all Topics in there)
        // case1: using All Topics by dividing by all ResultSet Topics
        // case2: using ONLY submitted Topics by dividing by all submitted Topics
        if (isUseAllTopics) {
            oimap[0] = (double) outgoingAPs / (resultTable.size() / 2);
            oimap[1] = (double) incomingAPs / (resultTable.size() / 2);
        } else {
            oimap[0] = (double) outgoingAPs / (runTable.size() / 2);
            oimap[1] = (double) incomingAPs / (runTable.size() / 2);
        }
        RunTopicScore runTopicScore = new RunTopicScore(tempRunId, oimap[0]);
        runTopicScore.setTopicScores(topicScores);
        
        Data.runTopicScores.get(Data.MEASURE_LMAP).add(runTopicScore);
        return oimap;
    }

    private static double[] getRPrecs(String tempRunID, Hashtable resultTable, Hashtable runTable) {
        double[] oirprecs = new double[2];
        double outgoingRPrecs = 0.0;
        double incomingRPrecs = 0.0;

        // Loop all submitted run Topics
        ArrayList<TopicScore> topicScores = new ArrayList<TopicScore>();
        
        for (Enumeration e = runTable.keys(); e.hasMoreElements();) {
            double anchorScore = 0.0;
            String key = e.nextElement().toString();
            // get RUN (incoming or outgoing) links array for each Topic
            String[] runValues = (String[]) runTable.get(key);
            // get Result (incoming or outgoing) links array for each Topic
            String[] resultSet = (String[]) resultTable.get(key);

            // =================================================================
            // Run:
            // group BEP links by Anchor
            // ResultSet: 
            // get total number of Anchors for this Topic: key: Outgoing / Incoming
//            int rsAnchorNoPerTopic = 0;
//
//            Vector runIndexV = new Vector();
            String topicId = key.substring(0, key.indexOf("_"));
            if (isAnchorGToFile || isAnchorGToBEP) {
                if (key.endsWith("Outgoing_Links")) {
                	init(runValues, resultSet);
                } else if (key.endsWith("Incoming_Links")) {
                    // Currently there is NO incoming links
                    // Incoming links should be further implemented
                }
            }
            // =================================================================

            //R-Precision: ONLY Caculate Precision value at the resultSet.length position
            if (runValues.length == 1 && runValues[0].equalsIgnoreCase("")) {
                if (resultSet.length == 1 && resultSet[0].equalsIgnoreCase("")) {
                    if (key.endsWith(outgoingTag)) {
                        outgoingRPrecs += 1;
                    } else if (key.endsWith(incomingTag)) {
                        incomingRPrecs += 1;
                    }
                } else {
                    if (key.endsWith(outgoingTag)) {
                        outgoingRPrecs += 0;
                    } else if (key.endsWith(incomingTag)) {
                        incomingRPrecs += 0;
                    }
                }
            } else {

                if (isAnchorGToFile || isAnchorGToBEP) {
                    if (key.endsWith("Outgoing_Links")) {
                        // run link --> aOffset_aLength_bFileID_bBEP
                        // result set --> aOffset_aLength_bFileID_bBEP
                        // -----------------------------------------------------
                        int anchorCounter = 1;
                        // Loop each Anchor in a Topic
                        int Rvalue = 0;
//                        if (runIndexV.size() >= rsAnchorNoPerTopic) {
//                            Rvalue = rsAnchorNoPerTopic;
                        if (runIndexV.size() >= resultSet.length) {
                            Rvalue = resultSet.length;
                        } else {
                            Rvalue = runIndexV.size();
                        }

                        // Loop each Anchor Until reach R
                        for (int i = 0; i < Rvalue; i++) {

                            Object runAnchorSet = runIndexV.get(i);
                            String[] runAnchorOL = runAnchorSet.toString().split("_");
                            Vector bepSetV = (Vector) runHT.get(runAnchorSet.toString());  // get BEP links inside Each Anchor
                            int runAStartPoint = Integer.valueOf(runAnchorOL[0]);
                            int runAEndPoint = Integer.valueOf(runAnchorOL[0]) + Integer.valueOf(runAnchorOL[1]);
                            
                            Vector resultBeps = resultLinks.get(runAnchorSet);
                            if (resultBeps != null) {
	                            // Loop each BEP link in an Anchor
	                            boolean isMatched = false;
	                            double bepScore = 0.0;
	                            Enumeration bSetEnu = bepSetV.elements();
	                            while (bSetEnu.hasMoreElements()) {
	                                Object runBepSet = bSetEnu.nextElement();
	                                // bFileID_bOffset
	                                String[] bepLink = runBepSet.toString().split("_");
	                                String runBepID = bepLink[0].trim();
	                                int runBepOffset = Integer.valueOf(bepLink[1].trim());
	
	                                Vector<Double> bepsDisV = new Vector<Double>();
	                                
	                                if (!isAnchorGToBEP) {
		                            	if (resultBeps.contains(runBepID + "_0"))
		                            		bepsDisV.add(0.0);
	                                }
	                                else {
		                                for (int m = 0; m < resultSet.length; m++) {
		                                    String[] rsSet = resultSet[m].trim().split("_");
		                                    int rsaStartPoint = Integer.valueOf(rsSet[0].trim());
		                                    int rsaEndPoint = Integer.valueOf(rsSet[0].trim()) + Integer.valueOf(rsSet[1].trim());
		                                    // -----------------------------------------
		                                    if (useOnlyAnchorGroup) {
		                                        // 2) to find matched BEP file ID in RS
		                                        String rsbFileID = rsSet[2].trim();
		                                        int rsbBEP = Integer.valueOf(rsSet[3].trim());
		                                        if (runBepID.equalsIgnoreCase(rsbFileID)) {
		                                            if (Math.abs(runBepOffset - rsbBEP) <= distanceFactor) {
		                                                double bd = 0;
		                                                if (isAnchorGToBEP) {
		                                                    bd = Math.abs(runBepOffset - rsbBEP);
		                                                }
		                                                bepsDisV.add(bd);
		                                            } else {
		                                                double bd = 0;
		                                                if (isAnchorGToBEP) {
		                                                    bd = distanceFactor;
		                                                }
		                                                bepsDisV.add(bd);
		                                            }
		                                        }
		                                    } else {
		                                        // 1) to match Anchor Offset & Length
		                                        if ((runAStartPoint >= rsaStartPoint && runAStartPoint <= rsaEndPoint) ||
		                                                (runAEndPoint >= rsaStartPoint && runAEndPoint <= rsaEndPoint)) {
		                                            // -------------------------------------
		                                            // 2) to find matched BEP file ID in RS
		                                            String rsbFileID = rsSet[2].trim();
		                                            int rsbBEP = Integer.valueOf(rsSet[3].trim());
		                                            if (runBepID.equalsIgnoreCase(rsbFileID)) {
		                                                if (Math.abs(runBepOffset - rsbBEP) <= distanceFactor) {
		                                                    double bd = 0;
		                                                    if (isAnchorGToBEP) {
		                                                        bd = Math.abs(runBepOffset - rsbBEP);
		                                                    }
		                                                    bepsDisV.add(bd);
		                                                } else {
		                                                    double bd = 0;
		                                                    if (isAnchorGToBEP) {
		                                                        bd = distanceFactor;
		                                                    }
		                                                    bepsDisV.add(bd);
		                                                }
		                                            }
		                                        }
		                                    }
		                                }
	                                }
	                                // ---------------------------------------------
	                                if (bepsDisV.size() > 0) {
	                                    double shortestDis = Collections.min(bepsDisV);
	                                    double bs = (distanceFactor - 0.9 * shortestDis) / distanceFactor;
	                                    bepScore = bepScore + bs;
	                                    isMatched = true;
	                                }
	                            }
	
	                            if (isMatched) {
	                                if (bepDenominator == 0) {
	                                    if (bepSetV.size() != 0) {
	                                        int Denominator;
	                                        Denominator = Math.min(5, bepSetV.size());
	                                        anchorScore += (double) bepScore / Denominator;   // score for each Anchor
	                                    }
	                                } else {
	                                    anchorScore += (double) bepScore / bepDenominator;      // score for each Anchor
	                                }
	                                isMatched = false;
	                            }
	                        }
                        }

                        int N = Math.min(250, resultLinks.size()/*resultSet.length*/);
                        outgoingRPrecs += (double) anchorScore / N;
//                        outgoingRPrecs += (double) anchorScore / Rvalue;

                    } else if (key.endsWith(incomingTag)) {
                        incomingRPrecs += 0;
                    }

                }
                else {
                    // File-To-BEP without Grouping
                    // Get non-duplicate items =====================================
                    if (key.endsWith(outgoingTag)) {
                        double bepLinkScore = 0.0;
                        List rpItemsList = new ArrayList();
                        for (int i = 0; i < runValues.length; i++) {
                            String item = runValues[i];
                            if (!rpItemsList.contains(item)) {
                                rpItemsList.add(item);
                            }
                        }
                        String[] rpRunItems = (String[]) rpItemsList.toArray(new String[rpItemsList.size()]);
                        // =============================================================
                        double APinR = 0.0;
                        int k = 0;
                        for (int i = 0; i < rpRunItems.length; i++) {
                            k = k + 1;
                            // To Count the Retrieved Relevant links
                            String link = rpRunItems[i];
                            Vector<Double> bepsDisV = new Vector<Double>();
                            for (int j = 0; j < resultSet.length; j++) {
                                // link --> bFileID_bBEP
                                String[] linkSet = link.split("_");
                                String runbFileID = linkSet[0].trim();
                                if (linkSet[1].equals("/article[1]")) {
                                    linkSet[1] = "0";
                                }
                                int runbBEP = Integer.valueOf(linkSet[1].trim());
                                String[] rsSet = resultSet[j].trim().split("_");
                                String rsbFileID = rsSet[0].trim();
                                int rsbBEP = Integer.valueOf(rsSet[1].trim());
                                if (runbFileID.equalsIgnoreCase(rsbFileID)) {
                                    if (Math.abs(runbBEP - rsbBEP) <= distanceFactor) {
                                        double bd = Math.abs(runbBEP - rsbBEP);
                                        bepsDisV.add(bd);
                                    } else {
                                        double bd = distanceFactor;
                                        bepsDisV.add(bd);
                                    }
                                }
                            }
                            if (bepsDisV.size() > 0) {
                                double shortestDis = Collections.min(bepsDisV);
                                double fw = (distanceFactor - 0.9 * shortestDis) / distanceFactor;
                                bepLinkScore = bepLinkScore + fw;   // score for each BEP link
//                                APinR += (double) bepLinkScore / (k);
                            }
                            // In R_Precision, R is the number of relevant document for a given query
                            // Here, R is resultSet.length
                            if ((resultSet.length <= rpRunItems.length) && (i + 1) == resultSet.length) {
                                outgoingRPrecs += (double) bepLinkScore / (resultSet.length);
                            } else if ((resultSet.length > rpRunItems.length) && (i + 1) == rpRunItems.length) {
                                outgoingRPrecs += (double) bepLinkScore / (resultSet.length);
                            }
                        }

                    } else if (key.endsWith(incomingTag)) {
                        incomingRPrecs += 0;
                    }
                }
                
                TopicScore topicScore = new TopicScore(topicId, outgoingRPrecs);
                topicScores.add(topicScore);
            }

            // =================================================================
        }
        
//        Data.runTopicScores.get(Data.MEASURE_R_PREC).put(tempRunID, topicScores);
        outputTopicScore(topicScores);

        if (isUseAllTopics) {
            oirprecs[0] = (double) outgoingRPrecs / (resultTable.size() / 2);
            oirprecs[1] = (double) incomingRPrecs / (resultTable.size() / 2);
        } else {
            oirprecs[0] = (double) outgoingRPrecs / (runTable.size() / 2);
            oirprecs[1] = (double) incomingRPrecs / (runTable.size() / 2);
        }

        RunTopicScore runTopicScore = new RunTopicScore(tempRunID, oirprecs[0]);
        runTopicScore.setTopicScores(topicScores);
        
        Data.runTopicScores.get(Data.MEASURE_R_PREC).add(runTopicScore);
        return oirprecs;
    }

    private static double[][] getPrecsAT(String tempRunID, Hashtable resultTable, Hashtable runTable) {
        // Declare [2]: as Outgoing & Incoming
        // Declare [2]: as AT levels: 5, 10, 20, 30, 50, 250
        // Precision@: ONLY Caculate Precision value at the @5, 10, 20, 30, 50, 250 values
        int recallDegree = 6;
        //        int[] pAtValue = {10, 30, 50, 100, 200};
      
//      int[] pAtValue = {5, 10, 20, 30, 50, 250};
        int[] pAtN = Data.pAtValue;
        
        double[][] oiprecsat = new double[2][recallDegree];
        double[] outgoingPrecsAt = new double[recallDegree];
        double[] incomingPrecsAt = new double[recallDegree];
        
        ArrayList<TopicScore> topicScores = new ArrayList<TopicScore>();
        
        for (Enumeration e = runTable.keys(); e.hasMoreElements();) {
            double mCount = 0.;
            double anchorScore = 0.0;
            String key = e.nextElement().toString();
            // get RUN (incoming or outgoing) links array for each Topic
            String[] runValues = (String[]) runTable.get(key);
            // get Result (incoming or outgoing) links array for each Topic
            String[] resultSet = (String[]) resultTable.get(key);

            // =================================================================
            // Run:
            // group BEP links by Anchor
            // ResultSet: 
            // get total number of Anchors for this Topic: key: Outgoing / Incoming
//            int rsAnchorNoPerTopic = 0;
//            Hashtable<String, Vector> runHT = new Hashtable<String, Vector>();
//            Vector runIndexV = new Vector();
            String topicId = key.substring(0, key.indexOf("_"));
            if (isAnchorGToFile || isAnchorGToBEP) {
                if (key.endsWith("Outgoing_Links")) {
                	init(runValues, resultSet);
                } else if (key.endsWith("Incoming_Links")) {
                }
            }
            // =================================================================

            TopicScore topicScore = new TopicScore(topicId);
            
            if (runValues.length == 1 && runValues[0].equalsIgnoreCase("")) {
                if (resultSet.length == 1 && resultSet[0].equalsIgnoreCase("")) {
                    if (key.endsWith(outgoingTag)) {
                    	topicScore.setScore(1.0);
                        outgoingPrecsAt[0] += 1.0;
                        outgoingPrecsAt[1] += 1.0;
                        outgoingPrecsAt[2] += 1.0;
                        outgoingPrecsAt[3] += 1.0;
                        outgoingPrecsAt[4] += 1.0;
                        outgoingPrecsAt[5] += 1.0;
                    } else if (key.endsWith(incomingTag)) {
                        incomingPrecsAt[0] += 1.0;
                        incomingPrecsAt[1] += 1.0;
                        incomingPrecsAt[2] += 1.0;
                        incomingPrecsAt[3] += 1.0;
                        incomingPrecsAt[4] += 1.0;
                        incomingPrecsAt[5] += 1.0;
                    }
                } else {
                    if (key.endsWith(outgoingTag)) {
                    	topicScore.setScore(0.0);
                        outgoingPrecsAt[0] += 0;
                        outgoingPrecsAt[1] += 0;
                        outgoingPrecsAt[2] += 0;
                        outgoingPrecsAt[3] += 0;
                        outgoingPrecsAt[4] += 0;
                        outgoingPrecsAt[5] += 0;
                    } else if (key.endsWith(incomingTag)) {
                        incomingPrecsAt[0] += 0;
                        incomingPrecsAt[1] += 0;
                        incomingPrecsAt[2] += 0;
                        incomingPrecsAt[3] += 0;
                        incomingPrecsAt[4] += 0;
                        incomingPrecsAt[5] += 0;
                    }
                }
            } else {

                if (isAnchorGToFile || isAnchorGToBEP) {

                    if (key.endsWith(outgoingTag)) {
                        // run link --> aOffset_aLength_bFileID_bBEP
                        // result set --> aOffset_aLength_bFileID_bBEP
                        // -----------------------------------------------------
//                        log("runIndexV.size(): " + runIndexV.size());
                        for (int i = 0; i < runIndexV.size(); i++) {

                            Object runAnchorSet = runIndexV.get(i);             // Loop each Anchor Until reach R
                            String[] runAnchorOL = runAnchorSet.toString().split("_");
                            Vector bepSetV = (Vector) runHT.get(runAnchorSet.toString());  // get BEP links inside Each Anchor
                            int runAStartPoint = Integer.valueOf(runAnchorOL[0]);
                            int runAEndPoint = Integer.valueOf(runAnchorOL[0]) + Integer.valueOf(runAnchorOL[1]);

                            Vector resultBeps = resultLinks.get(runAnchorSet);
                            
                            if (resultBeps != null) {
	                            // Loop each BEP link in an Anchor
	                            boolean isMatched = false;
	                            double bepScore = 0.0;
	                            Enumeration bSetEnu = bepSetV.elements();
	                            while (bSetEnu.hasMoreElements()) {
	                                Object runBepSet = bSetEnu.nextElement();
	                                // bFileID_bOffset
	                                String[] bepLink = runBepSet.toString().split("_");
	                                String runBepID = bepLink[0].trim();
	                                int runBepOffset = Integer.valueOf(bepLink[1].trim());
	                                Vector<Double> bepsDisV = new Vector<Double>();
	                                
	                                if (!isAnchorGToBEP) {
		                            	if (resultBeps.contains(runBepID + "_0"))
		                            		bepsDisV.add(0.0);
	                                }
	                                else {
		                                for (int m = 0; m < resultSet.length; m++) {
		                                    String[] rsSet = resultSet[m].trim().split("_");
		                                    int rsaStartPoint = Integer.valueOf(rsSet[0].trim());
		                                    int rsaEndPoint = Integer.valueOf(rsSet[0].trim()) + Integer.valueOf(rsSet[1].trim());
		                                    // -----------------------------------------
		                                    if (useOnlyAnchorGroup) {
		                                        String rsbFileID = rsSet[2].trim();
		                                        int rsbBEP = Integer.valueOf(rsSet[3].trim());
		
		                                        if (runBepID.equalsIgnoreCase(rsbFileID)) {
		                                            if (Math.abs(runBepOffset - rsbBEP) <= distanceFactor) {
		                                                double bd = 0;
		                                                if (isAnchorGToBEP) {
		                                                    bd = Math.abs(runBepOffset - rsbBEP);
		                                                }
		                                                bepsDisV.add(bd);
		                                            } else {
		                                                double bd = 0;
		                                                if (isAnchorGToBEP) {
		                                                    bd = distanceFactor;
		                                                }
		                                                bepsDisV.add(bd);
		                                            }
		                                        }
		                                    } else {
		                                        // 1) to match Anchor Offset & Length
		                                        if ((runAStartPoint >= rsaStartPoint && runAStartPoint <= rsaEndPoint) ||
		                                                (runAEndPoint >= rsaStartPoint && runAEndPoint <= rsaEndPoint)) {
		                                            // -------------------------------------
		                                            // 2) to find matched BEP file ID in RS
		                                            String rsbFileID = rsSet[2].trim();
		                                            int rsbBEP = Integer.valueOf(rsSet[3].trim());
		
		                                            if (runBepID.equalsIgnoreCase(rsbFileID)) {
		                                                if (Math.abs(runBepOffset - rsbBEP) <= distanceFactor) {
		                                                    double bd = 0;
		                                                    if (isAnchorGToBEP) {
		                                                        bd = Math.abs(runBepOffset - rsbBEP);
		                                                    }
		                                                    bepsDisV.add(bd);
		                                                } else {
		                                                    double bd = 0;
		                                                    if (isAnchorGToBEP) {
		                                                        bd = distanceFactor;
		                                                    }
		                                                    bepsDisV.add(bd);
		                                                }
		                                            }
		                                        }
		                                    }
		                                }
	                                }
	                                // ---------------------------------------------
	                                if (bepsDisV.size() > 0) {
	                                    double shortestDis = Collections.min(bepsDisV);
	                                    double bs = (distanceFactor - 0.9 * shortestDis) / distanceFactor;
	                                    bepScore = bepScore + bs;
	                                    isMatched = true;
	                                }
	                            }
	                            
	                            if (isMatched) {
	                                if (bepDenominator <= 0) {
	                                    if (bepSetV.size() != 0) {
	                                        int Denominator =  Math.min(5, bepSetV.size()); //resultBeps.size() > 5 ? 5 : resultBeps.size();
	                                        anchorScore += (double) bepScore / Denominator;   // score for each Anchor
	                                    }
	                                } else {
	                                    anchorScore += (double) bepScore / bepDenominator;
	                                }
	                                isMatched = false;
	                            }
                            }

                            // Loop each Topic for Incoming & Outgoing
                            // ONLY Calculate Precision @5, 10, 20, 30, 50, 250
                            if (runIndexV.size() <= pAtN[0]) {
                                if ((i + 1) == runIndexV.size()) {
                                	topicScore.setScore(topicScore.getScore() + (double) anchorScore / pAtN[0]);
                                    outgoingPrecsAt[0] += (double) anchorScore / pAtN[0];
                                    outgoingPrecsAt[1] += (double) anchorScore / pAtN[1];
                                    outgoingPrecsAt[2] += (double) anchorScore / pAtN[2];
                                    outgoingPrecsAt[3] += (double) anchorScore / pAtN[3];
                                    outgoingPrecsAt[4] += (double) anchorScore / pAtN[4];
                                    outgoingPrecsAt[5] += (double) anchorScore / pAtN[5];
                                }
                            } else if (runIndexV.size() <= pAtN[1]) {
                                if ((i + 1) == pAtN[0]) {
                                    outgoingPrecsAt[0] += (double) anchorScore / (i + 1);
                                } else {
                                    if ((i + 1) == runIndexV.size()) {
                                        outgoingPrecsAt[1] += (double) anchorScore / pAtN[1];
                                        outgoingPrecsAt[2] += (double) anchorScore / pAtN[2];
                                        outgoingPrecsAt[3] += (double) anchorScore / pAtN[3];
                                        outgoingPrecsAt[4] += (double) anchorScore / pAtN[4];
                                        outgoingPrecsAt[5] += (double) anchorScore / pAtN[5];
                                    }
                                }
                            } else if (runIndexV.size() <= pAtN[2]) {
                                if ((i + 1) == pAtN[0]) {
                                	topicScore.setScore(topicScore.getScore() + (double) anchorScore / (i + 1));
                                    outgoingPrecsAt[0] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == pAtN[1]) {
                                    outgoingPrecsAt[1] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == runIndexV.size()) {
                                    outgoingPrecsAt[2] += (double) anchorScore / pAtN[2];
                                    outgoingPrecsAt[3] += (double) anchorScore / pAtN[3];
                                    outgoingPrecsAt[4] += (double) anchorScore / pAtN[4];
                                    outgoingPrecsAt[5] += (double) anchorScore / pAtN[5];
                                }
                            } else if (runIndexV.size() <= pAtN[3]) {
                                if ((i + 1) == pAtN[0]) {
                                	topicScore.setScore(topicScore.getScore() + (double) anchorScore / (i + 1));
                                    outgoingPrecsAt[0] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == pAtN[1]) {
                                    outgoingPrecsAt[1] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == pAtN[2]) {
                                    outgoingPrecsAt[2] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == runIndexV.size()) {
                                    outgoingPrecsAt[3] += (double) anchorScore / pAtN[3];
                                    outgoingPrecsAt[4] += (double) anchorScore / pAtN[4];
                                    outgoingPrecsAt[5] += (double) anchorScore / pAtN[5];
                                }
                            } else if (runIndexV.size() <= pAtN[4]) {
                                if ((i + 1) == pAtN[0]) {
                                	topicScore.setScore(topicScore.getScore() + (double) anchorScore / (i + 1));
                                    outgoingPrecsAt[0] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == pAtN[1]) {
                                    outgoingPrecsAt[1] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == pAtN[2]) {
                                    outgoingPrecsAt[2] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == pAtN[3]) {
                                    outgoingPrecsAt[3] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == runIndexV.size()) {
                                    outgoingPrecsAt[4] += (double) anchorScore / pAtN[4];
                                    outgoingPrecsAt[5] += (double) anchorScore / pAtN[5];
                                }
                            } else {
                                if ((i + 1) == pAtN[0]) {
                                	topicScore.setScore(topicScore.getScore() + (double) anchorScore / (i + 1));
                                    outgoingPrecsAt[0] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == pAtN[1]) {
                                    outgoingPrecsAt[1] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == pAtN[2]) {
                                    outgoingPrecsAt[2] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == pAtN[3]) {
                                    outgoingPrecsAt[3] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == pAtN[4]) {
                                    outgoingPrecsAt[4] += (double) anchorScore / pAtN[4];
                                } else if ((i + 1) == runIndexV.size() && (i + 1) < pAtN[5]) {
                                    outgoingPrecsAt[5] += (double) anchorScore / pAtN[5];
                                } else if ((i + 1) == pAtN[5]) {
                                    outgoingPrecsAt[5] += (double) anchorScore / pAtN[5];
                                }
                            }

                        }
                    } else if (key.endsWith("Incoming_Links")) {
                        incomingPrecsAt[0] += 0;
                        incomingPrecsAt[1] += 0;
                        incomingPrecsAt[2] += 0;
                        incomingPrecsAt[3] += 0;
                        incomingPrecsAt[4] += 0;
                        incomingPrecsAt[5] += 0;
                    }

                } else {
                    // File-to-BEP
                    // Only get nonDuplicate run Items to calculate
                    List itemsList = new ArrayList();
                    for (int i = 0; i < runValues.length; i++) {
                        String item = runValues[i];
                        if (!itemsList.contains(item)) {
                            itemsList.add(item);
                        }
                    }
                    String[] runItems = (String[]) itemsList.toArray(new String[itemsList.size()]);
                    // =========================================================
                    double APinR = 0.0;
                    double k = 0;
                    for (int i = 0; i < runItems.length; i++) {
                        k = k + 1;
                        // To Count the Retrieved Relevant links
                        String link = runItems[i];
                        Vector<Double> bepsDisV = new Vector<Double>();
                        for (int j = 0; j < resultSet.length; j++) {
                            if (key.endsWith(outgoingTag)) {
                                // link --> bFileID_bBEP
                                String[] linkSet = link.split("_");
                                String runbFileID = linkSet[0].trim();
                                if (linkSet[1].equals("/article[1]")) {
                                    linkSet[1] = "0";
                                }
                                int runbBEP = Integer.valueOf(linkSet[1].trim());
                                String[] rsSet = resultSet[j].trim().split("_");
                                String rsbFileID = rsSet[0].trim();
                                int rsbBEP = Integer.valueOf(rsSet[1].trim());
                                if (runbFileID.equalsIgnoreCase(rsbFileID)) {
                                    if (Math.abs(runbBEP - rsbBEP) <= distanceFactor) {
                                        double bd = Math.abs(runbBEP - rsbBEP);
                                        bepsDisV.add(bd);
                                    }
                                }
                            } else if (key.endsWith(incomingTag)) {
                                mCount = 0;
                            }
                        }
                        if (bepsDisV.size() > 0) {
                            double shortestDis = Collections.min(bepsDisV);
                            double fw = (distanceFactor - 0.9 * shortestDis) / distanceFactor;
                            mCount = mCount + fw;
//                            APinR += (double) mCount / (k);
                        }
                        // Loop each Topic for Incoming & Outgoing
                        // ONLY Calculate Precision @5, 10, 20, 30, 50, 250
                        if (runItems.length <= pAtN[0]) {
                            if ((i + 1) == runItems.length) {
                                if (key.endsWith(outgoingTag)) {
                                	topicScore.setScore(topicScore.getScore() + (double) mCount / pAtN[0]);
                                    outgoingPrecsAt[0] += (double) mCount / pAtN[0];
                                    outgoingPrecsAt[1] += (double) mCount / pAtN[1];
                                    outgoingPrecsAt[2] += (double) mCount / pAtN[2];
                                    outgoingPrecsAt[3] += (double) mCount / pAtN[3];
                                    outgoingPrecsAt[4] += (double) mCount / pAtN[4];
                                    outgoingPrecsAt[5] += (double) mCount / pAtN[5];
                                } else if (key.endsWith(incomingTag)) {
                                    incomingPrecsAt[0] += (double) mCount / pAtN[0];
                                    incomingPrecsAt[1] += (double) mCount / pAtN[1];
                                    incomingPrecsAt[2] += (double) mCount / pAtN[2];
                                    incomingPrecsAt[3] += (double) mCount / pAtN[3];
                                    incomingPrecsAt[4] += (double) mCount / pAtN[4];
                                    incomingPrecsAt[5] += (double) mCount / pAtN[5];
                                }
                            }
                        } else if (runItems.length <= pAtN[1]) {
                            if ((i + 1) == pAtN[0]) {
                                if (key.endsWith(outgoingTag)) {
                                	topicScore.setScore(topicScore.getScore() + (double) mCount / (i + 1));
                                    outgoingPrecsAt[0] += (double) mCount / (i + 1);
                                } else if (key.endsWith(incomingTag)) {
                                    incomingPrecsAt[0] += (double) mCount / (i + 1);
                                }
                            } else {
                                if ((i + 1) == runItems.length) {
                                    if (key.endsWith(outgoingTag)) {
                                        outgoingPrecsAt[1] += (double) mCount / pAtN[1];
                                        outgoingPrecsAt[2] += (double) mCount / pAtN[2];
                                        outgoingPrecsAt[3] += (double) mCount / pAtN[3];
                                        outgoingPrecsAt[4] += (double) mCount / pAtN[4];
                                        outgoingPrecsAt[5] += (double) mCount / pAtN[5];
                                    } else if (key.endsWith(incomingTag)) {
                                        incomingPrecsAt[1] += (double) mCount / pAtN[1];
                                        incomingPrecsAt[2] += (double) mCount / pAtN[2];
                                        incomingPrecsAt[3] += (double) mCount / pAtN[3];
                                        incomingPrecsAt[4] += (double) mCount / pAtN[4];
                                        incomingPrecsAt[5] += (double) mCount / pAtN[5];
                                    }
                                }
                            }
                        } else if (runItems.length <= pAtN[2]) {
                            if ((i + 1) == pAtN[0]) {
                                if (key.endsWith(outgoingTag)) {
                                	topicScore.setScore(topicScore.getScore() + (double) mCount / (i + 1));
                                    outgoingPrecsAt[0] += (double) mCount / (i + 1);
                                } else if (key.endsWith(incomingTag)) {
                                    incomingPrecsAt[0] += (double) mCount / (i + 1);
                                }
                            } else if ((i + 1) == pAtN[1]) {
                                if (key.endsWith(outgoingTag)) {
                                    outgoingPrecsAt[1] += (double) mCount / (i + 1);
                                } else if (key.endsWith(incomingTag)) {
                                    incomingPrecsAt[1] += (double) mCount / (i + 1);
                                }
                            } else if ((i + 1) == runItems.length) {
                                if (key.endsWith(outgoingTag)) {
                                    outgoingPrecsAt[2] += (double) mCount / pAtN[2];
                                    outgoingPrecsAt[3] += (double) mCount / pAtN[3];
                                    outgoingPrecsAt[4] += (double) mCount / pAtN[4];
                                    outgoingPrecsAt[5] += (double) mCount / pAtN[5];
                                } else if (key.endsWith(incomingTag)) {
                                    incomingPrecsAt[2] += (double) mCount / pAtN[2];
                                    incomingPrecsAt[3] += (double) mCount / pAtN[3];
                                    incomingPrecsAt[4] += (double) mCount / pAtN[4];
                                    incomingPrecsAt[5] += (double) mCount / pAtN[5];
                                }
                            }
                        } else if (runItems.length <= pAtN[3]) {
                            if ((i + 1) == pAtN[0]) {
                                if (key.endsWith(outgoingTag)) {
                                	topicScore.setScore(topicScore.getScore() + (double) mCount / (i + 1));
                                    outgoingPrecsAt[0] += (double) mCount / (i + 1);
                                } else if (key.endsWith(incomingTag)) {
                                    incomingPrecsAt[0] += (double) mCount / (i + 1);
                                }
                            } else if ((i + 1) == pAtN[1]) {
                                if (key.endsWith(outgoingTag)) {
                                    outgoingPrecsAt[1] += (double) mCount / (i + 1);
                                } else if (key.endsWith(incomingTag)) {
                                    incomingPrecsAt[1] += (double) mCount / (i + 1);
                                }
                            } else if ((i + 1) == pAtN[2]) {
                                if (key.endsWith(outgoingTag)) {
                                    outgoingPrecsAt[2] += (double) mCount / (i + 1);
                                } else if (key.endsWith(incomingTag)) {
                                    incomingPrecsAt[2] += (double) mCount / (i + 1);
                                }
                            } else if ((i + 1) == runItems.length) {
                                if (key.endsWith(outgoingTag)) {
                                    outgoingPrecsAt[3] += (double) mCount / pAtN[3];
                                    outgoingPrecsAt[4] += (double) mCount / pAtN[4];
                                    outgoingPrecsAt[5] += (double) mCount / pAtN[5];
                                } else if (key.endsWith(incomingTag)) {
                                    incomingPrecsAt[3] += (double) mCount / pAtN[3];
                                    incomingPrecsAt[4] += (double) mCount / pAtN[4];
                                    incomingPrecsAt[5] += (double) mCount / pAtN[5];
                                }
                            }
                        } else if (runItems.length <= pAtN[4]) {
                            if ((i + 1) == pAtN[0]) {
                                if (key.endsWith(outgoingTag)) {
                                	topicScore.setScore(topicScore.getScore() + (double) mCount / (i + 1));
                                    outgoingPrecsAt[0] += (double) mCount / (i + 1);
                                } else if (key.endsWith(incomingTag)) {
                                    incomingPrecsAt[0] += (double) mCount / (i + 1);
                                }
                            } else if ((i + 1) == pAtN[1]) {
                                if (key.endsWith(outgoingTag)) {
                                    outgoingPrecsAt[1] += (double) mCount / (i + 1);
                                } else if (key.endsWith(incomingTag)) {
                                    incomingPrecsAt[1] += (double) mCount / (i + 1);
                                }
                            } else if ((i + 1) == pAtN[2]) {
                                if (key.endsWith(outgoingTag)) {
                                    outgoingPrecsAt[2] += (double) mCount / (i + 1);
                                } else if (key.endsWith(incomingTag)) {
                                    incomingPrecsAt[2] += (double) mCount / (i + 1);
                                }
                            } else if ((i + 1) == pAtN[3]) {
                                if (key.endsWith(outgoingTag)) {
                                    outgoingPrecsAt[3] += (double) mCount / (i + 1);
                                } else if (key.endsWith(incomingTag)) {
                                    incomingPrecsAt[3] += (double) mCount / (i + 1);
                                }
                            } else if ((i + 1) == runItems.length) {
                                if (key.endsWith(outgoingTag)) {
                                    outgoingPrecsAt[4] += (double) mCount / pAtN[4];
                                    outgoingPrecsAt[5] += (double) mCount / pAtN[5];
                                } else if (key.endsWith(incomingTag)) {
                                    incomingPrecsAt[4] += (double) mCount / pAtN[4];
                                    incomingPrecsAt[5] += (double) mCount / pAtN[5];
                                }
                            }
                        } else {
                            if ((i + 1) == pAtN[0]) {
                                if (key.endsWith(outgoingTag)) {
                                	topicScore.setScore(topicScore.getScore() + (double) mCount / (i + 1));
                                    outgoingPrecsAt[0] += (double) mCount / (i + 1);
                                } else if (key.endsWith(incomingTag)) {
                                    incomingPrecsAt[0] += (double) mCount / (i + 1);
                                }
                            } else if ((i + 1) == pAtN[1]) {
                                if (key.endsWith(outgoingTag)) {
                                    outgoingPrecsAt[1] += (double) mCount / (i + 1);
                                } else if (key.endsWith(incomingTag)) {
                                    incomingPrecsAt[1] += (double) mCount / (i + 1);
                                }
                            } else if ((i + 1) == pAtN[2]) {
                                if (key.endsWith(outgoingTag)) {
                                    outgoingPrecsAt[2] += (double) mCount / (i + 1);
                                } else if (key.endsWith(incomingTag)) {
                                    incomingPrecsAt[2] += (double) mCount / (i + 1);
                                }
                            } else if ((i + 1) == pAtN[3]) {
                                if (key.endsWith(outgoingTag)) {
                                    outgoingPrecsAt[3] += (double) mCount / (i + 1);
                                } else if (key.endsWith(incomingTag)) {
                                    incomingPrecsAt[3] += (double) mCount / (i + 1);
                                }
                            } else if ((i + 1) == pAtN[4]) {
                                if (key.endsWith(outgoingTag)) {
                                    outgoingPrecsAt[4] += (double) mCount / pAtN[4];
                                } else if (key.endsWith(incomingTag)) {
                                    incomingPrecsAt[4] += (double) mCount / pAtN[4];
                                }
                            } else if ((i + 1) == runItems.length && (i + 1) < pAtN[5]) {
                                if (key.endsWith(outgoingTag)) {
                                    outgoingPrecsAt[5] += (double) mCount / pAtN[5];
                                } else if (key.endsWith(incomingTag)) {
                                    incomingPrecsAt[5] += (double) mCount / pAtN[5];
                                }
                            } else if ((i + 1) == pAtN[5]) {
                                if (key.endsWith(outgoingTag)) {
                                    outgoingPrecsAt[5] += (double) mCount / pAtN[5];
                                } else if (key.endsWith(incomingTag)) {
                                    incomingPrecsAt[5] += (double) mCount / pAtN[5];
                                }
                            }
                        }

                    }
                }
            }
            topicScores.add(topicScore);
        }
        
        System.err.println("P@5 scores:");
        outputTopicScore(topicScores);
//        Data.runTopicScores.get(Data.MEASURE_P_AT_5).put(tempRunID, topicScores);

        if (isUseAllTopics) {
            // Outgoing
            oiprecsat[0][0] = (double) outgoingPrecsAt[0] / (resultTable.size() / 2);
            oiprecsat[0][1] = (double) outgoingPrecsAt[1] / (resultTable.size() / 2);
            oiprecsat[0][2] = (double) outgoingPrecsAt[2] / (resultTable.size() / 2);
            oiprecsat[0][3] = (double) outgoingPrecsAt[3] / (resultTable.size() / 2);
            oiprecsat[0][4] = (double) outgoingPrecsAt[4] / (resultTable.size() / 2);
            oiprecsat[0][5] = (double) outgoingPrecsAt[5] / (resultTable.size() / 2);
            // Incoming
            oiprecsat[1][0] = (double) incomingPrecsAt[0] / (resultTable.size() / 2);
            oiprecsat[1][1] = (double) incomingPrecsAt[1] / (resultTable.size() / 2);
            oiprecsat[1][2] = (double) incomingPrecsAt[2] / (resultTable.size() / 2);
            oiprecsat[1][3] = (double) incomingPrecsAt[3] / (resultTable.size() / 2);
            oiprecsat[1][4] = (double) incomingPrecsAt[4] / (resultTable.size() / 2);
            oiprecsat[1][5] = (double) incomingPrecsAt[5] / (resultTable.size() / 2);
        } else {
            // Outgoing
            oiprecsat[0][0] = (double) outgoingPrecsAt[0] / (runTable.size() / 2);
            oiprecsat[0][1] = (double) outgoingPrecsAt[1] / (runTable.size() / 2);
            oiprecsat[0][2] = (double) outgoingPrecsAt[2] / (runTable.size() / 2);
            oiprecsat[0][3] = (double) outgoingPrecsAt[3] / (runTable.size() / 2);
            oiprecsat[0][4] = (double) outgoingPrecsAt[4] / (runTable.size() / 2);
            oiprecsat[0][5] = (double) outgoingPrecsAt[5] / (runTable.size() / 2);
            // Incoming
            oiprecsat[1][0] = (double) incomingPrecsAt[0] / (runTable.size() / 2);
            oiprecsat[1][1] = (double) incomingPrecsAt[1] / (runTable.size() / 2);
            oiprecsat[1][2] = (double) incomingPrecsAt[2] / (runTable.size() / 2);
            oiprecsat[1][3] = (double) incomingPrecsAt[3] / (runTable.size() / 2);
            oiprecsat[1][4] = (double) incomingPrecsAt[4] / (runTable.size() / 2);
            oiprecsat[1][5] = (double) incomingPrecsAt[5] / (runTable.size() / 2);
        }

        RunTopicScore runTopicScore = new RunTopicScore(tempRunID, oiprecsat[0][0]);
        runTopicScore.setTopicScores(topicScores);
        
        Data.runTopicScores.get(Data.MEASURE_P_AT_5).add(runTopicScore);
        return oiprecsat;
    }
}