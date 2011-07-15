package crosslink.measures;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
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
 *
 * @author n4275187
 */
public final class fileToBepPlotMeasures extends Measures {

    private static String outgoingTag = "Outgoing_Links";
    private static String incomingTag = "Incoming_Links";
    public static int RECALL_LEVEL = 20; // 20 Recall Levels
    private static double[] FormalRecallLevel = {0.05, 0.10, 0.15, 0.20, 0.25, 0.30, 0.35, 0.40, 0.45, 0.50,
        0.55, 0.60, 0.65, 0.70, 0.75, 0.80, 0.85, 0.90, 0.95, 1.00
    };
    public static int RESULT_TYPE_NUM = 3;

    public fileToBepPlotMeasures() {
    }

    public static plotsCalculation.PRCurveResult getFileToBepPlotResult(/*File resultfile, */File runfile, boolean isAllTopics, boolean useFileToBep, boolean useAnchorToFile, boolean useAnchorToBEP)
    	throws Exception {

        isUseAllTopics = isAllTopics ? true : false;
        isFileToBEP = useFileToBep ? true : false;
        isAnchorGToFile = useAnchorToFile ? true : false;
        isAnchorGToBEP = useAnchorToBEP ? true : false;

        plotsCalculation.PRCurveResult plotResult = new plotsCalculation.PRCurveResult();
        Hashtable resultTable = null;
        Hashtable runTable = null;
        String resultfile = null;
        if (isFileToBEP) {
            // the performance is measured by each File-to-BEP
            // so the result is calculated by each File-to-BEP in Run against the ONE in ResultSet
            runTable = getF2BRunSet(runfile);
            resultfile = ResultSetManager.getInstance().getResultSetPathFile(currentSourceLang, currentTargetLang);
            resultTable = ResultSetManager.getInstance().getResultSet(resultfile);
            if (resultTable == null) {
            	resultTable = fileToBepMeasures.getF2BResultSet(new File(resultfile));
            	ResultSetManager.getInstance().addResultSet(resultfile, resultTable);
            }
        } else if (isAnchorGToFile || isAnchorGToBEP) {
            runTable = getF2BRunSetByGroup(runfile);
            resultfile = ResultSetManager.getInstance().getResultSetPathFile(currentSourceLang, currentTargetLang);
            resultTable = ResultSetManager.getInstance().getResultSet(resultfile);
            if (resultTable == null) {
            	resultTable = getF2BResultSetByGroup(new File(resultfile));
            	ResultSetManager.getInstance().addResultSet(resultfile, resultTable);
            }
        } else {
        	throw new Exception("Uncertain evaluation type");
        }

        // =================================================================
        // get Run ID: QUT_LTW_RUN_003
        plotResult.plotRunId = runId; //fileToBepPlotMeasures.plotStatic;

        // =================================================================
        HashMap incomingPRData = new HashMap();
        HashMap outgoingPRData = new HashMap();

        double incomingAPs = 0.0;
        double outgoingAPs = 0.0;
        double recallLevel = 0.0;
        double precAtR = 0.0;
        // =================================================================

        // Loop each Topic: all Topics in each Run
        int runcount = 0;
        for (Enumeration e = runTable.keys(); e.hasMoreElements();) {

            runcount++;
            double mCount = 0;
            double anchorScore = 0.0;
            // key is Topic ID_outgoing/incoming
            String key = e.nextElement().toString();

            // get RUN link ID Array according to key
            String[] runValues = (String[]) runTable.get(key);
            // get ResultSet link ID Array according to key
            String[] resultSet = (String[]) resultTable.get(key);

            // =================================================================
            // Run:
            // group BEP links by Anchor
            // ResultSet:
            // get total number of Anchors for this Topic: key: Outgoing / Incoming
            int rsAnchorNoPerTopic = 0;
            Hashtable<String, Vector> runHT = new Hashtable<String, Vector>();
            Vector runIndexV = new Vector();
            if (isAnchorGToFile || isAnchorGToBEP) {
                if (key.endsWith("Outgoing_Links")) {
                    // ---------------------------------------------------------
                    Vector runBepV = new Vector();
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
                            v.add(bepLink);
                            runHT.remove(index);
                            runHT.put(index, v);
                        } else if (!runIndexV.contains(index) && !runHT.containsKey(index)) {
                            // diff Anchor
                            runBepV = new Vector();
                            runIndexV.add(index);
                            runBepV.add(bepLink);
                            runHT.put(index, runBepV);
                        }
                    }
                    // ---------------------------------------------------------
                    Vector aaptV = new Vector();
                    for (int l = 0; l < resultSet.length; l++) {
                        String[] rsSet = resultSet[l].trim().split("_");
                        String rsAnchorOffset = rsSet[0].trim();
                        String rsAnchorLength = rsSet[1].trim();

                        String rsbFileID = rsSet[2].trim();
                        int rsbBEP = Integer.valueOf(rsSet[3].trim());
                        if (!aaptV.contains(rsAnchorOffset + "_" + rsAnchorLength)) {
                            aaptV.add(rsAnchorOffset + "_" + rsAnchorLength);
                        }
                    }
                    rsAnchorNoPerTopic = aaptV.size();
                } else if (key.endsWith("Incoming_Links")) {
                }
            }
            // =================================================================
            // =================================================================
            HashMap<Double, Double> RPmap = new HashMap<Double, Double>();
            if (runValues[0].equals("") && runValues.length == 1) {
                if (resultSet[0].equals("") && resultSet.length == 1) {
                    for (int k = 0; k < FormalRecallLevel.length; k++) {
                        precAtR = 1.0;
                        RPmap.put(FormalRecallLevel[k], precAtR);
                    }
                } else {
                    for (int m = 0; m < FormalRecallLevel.length; m++) {
                        precAtR = 0.0;
                        RPmap.put(FormalRecallLevel[m], precAtR);
                    }
                }
            } else {
                if (isAnchorGToFile || isAnchorGToBEP) {
                    // <editor-fold defaultstate="collapsed" desc="Anchor-to-BEP & Anchor-to-File">
                    if (key.endsWith("Outgoing_Links")) {
                        // run link --> aOffset_aLength_bFileID_bBEP
                        // result set --> aOffset_aLength_bFileID_bBEP
                        // -----------------------------------------------------
                        int anchorCounter = 1;
                        // Loop each Anchor in a Topic
                        Enumeration runAnchorEnu = runIndexV.elements();
                        while (runAnchorEnu.hasMoreElements()) {
                            // aOffset_aLength
                            Object runAnchorSet = runAnchorEnu.nextElement();
                            Vector bepSetV = (Vector) runHT.get(runAnchorSet.toString());
                            String[] runAnchorOL = runAnchorSet.toString().split("_");
                            int runAStartPoint = Integer.valueOf(runAnchorOL[0]);
                            int runAEndPoint = Integer.valueOf(runAnchorOL[0]) + Integer.valueOf(runAnchorOL[1]);
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
                                for (int m = 0; m < resultSet.length; m++) {
                                    // aOffset_aLength_bFileID_bOffset
                                    String[] rsSet = resultSet[m].trim().split("_");
                                    int rsaStartPoint = Integer.valueOf(rsSet[0].trim());
                                    int rsaEndPoint = Integer.valueOf(rsSet[0].trim()) + Integer.valueOf(rsSet[1].trim());
                                    // -----------------------------------------
                                    if (useOnlyAnchorGroup) {
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
                                        int Denominator = bepSetV.size();
                                        anchorScore += (double) bepScore / Denominator;   // score for each Anchor
                                    }
                                } else {
                                    anchorScore += (double) bepScore / bepDenominator;       // score for each Anchor
                                }

                                precAtR = (double) anchorScore / (anchorCounter);

//                            recallLevel = (double) anchorScore / rsAnchorNoPerTopic;
                                int N = Math.min(50, resultSet.length);
                                recallLevel = (double) anchorScore / N;

                                RPmap.put(recallLevel, precAtR);

                                isMatched = false;
                            }
                            anchorCounter++;
                        }

                    } else if (key.endsWith("Incoming_Links")) {
                    }
                    // </editor-fold>
                } else {
                    // <editor-fold defaultstate="collapsed" desc="File-to-BEP without Grouping">
                    double APinR = 0.0;
                    // Get non-duplicate run items
                    List plotItemsList = new ArrayList();
                    for (int i = 0; i < runValues.length; i++) {
                        String item = runValues[i];
                        if (!plotItemsList.contains(item)) {
                            plotItemsList.add(item);
                        }
                    }
                    String[] plotItems = (String[]) plotItemsList.toArray(new String[plotItemsList.size()]);
                    // =============================================================
                    // Loop each BEP link inside a Topic
                    int k = 0;
                    for (int i = 0; i < plotItems.length; i++) {
                        k = k + 1;
                        // get link ID at position i
                        String link = plotItems[i].toString().trim();
                        boolean noMatched = true;
                        // Check the (incoming/outgoing) link with Result Set
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
                                        noMatched = false;
                                    } else {
                                        double bd = distanceFactor;
                                        bepsDisV.add(bd);
                                        noMatched = false;
                                    }
                                }
                            } else if (key.endsWith(incomingTag)) {
                            }
                        }

                        if (!noMatched) {
                            double shortestDis = Collections.min(bepsDisV);
                            double fw = (distanceFactor - 0.9 * shortestDis) / distanceFactor;
                            mCount = mCount + fw;
                            // -------------------------------------------------
                            precAtR = (double) mCount / (k);
//                            APinR += (double) mCount / (k);
//                            if (mCount > 0) {
//                                precAtR = (double) APinR / (mCount);
//                            } else {
//                                precAtR = 0;
//                            }
                            recallLevel = (double) mCount / resultSet.length;
                            // -------------------------------------------------
                            RPmap.put(recallLevel, precAtR);
                        }
                    }
                    // </editor-fold>
                }
            }

            HashMap InterpolatedPR = PRPairFormalization(RPmap);

            if (key.endsWith(outgoingTag)) {
                outgoingPRData.put(key, InterpolatedPR);
            } else if (key.endsWith(incomingTag)) {
                incomingPRData.put(key, InterpolatedPR);
            }
            // ========================================================
        }

        for (int j = 0; j < FormalRecallLevel.length; j++) {
            int inCount = 0;
            double inaveP = 0.0;
            Iterator in = incomingPRData.keySet().iterator();
            while (in.hasNext()) {
                Object inKey = in.next();
                HashMap inAvePR = (HashMap) incomingPRData.get(inKey);
                Double v = (Double) inAvePR.get(FormalRecallLevel[j]);
                inaveP += v;
                inCount++;
            }
            // ========================================================
            double inAP = 0.0;
            if (isUseAllTopics) {
//                inAP = (double)inaveP/allTopicNo;
                inAP = (double) inaveP / (resultTable.size() / 2);
            } else {
                inAP = (double) inaveP / inCount;
            }
//            double inAP = (double)inaveP/inCount;
            // ========================================================
            plotResult.incomming[j] = inAP;
        }
        for (int k = 0; k < FormalRecallLevel.length; k++) {
            int otCount = 0;
            double otaveP = 0.0;
            Iterator ot = outgoingPRData.keySet().iterator();
            while (ot.hasNext()) {
                Object otKey = ot.next();
                HashMap otAvePR = (HashMap) outgoingPRData.get(otKey);
                otaveP += (Double) otAvePR.get(FormalRecallLevel[k]);
                otCount++;
            }
            // ========================================================
            double otAP = 0.0;
            if (isUseAllTopics) {
//                otAP = (double)otaveP/allTopicNo;
                otAP = (double) otaveP / (resultTable.size() / 2);
            } else {
                otAP = (double) otaveP / otCount;
            }
            // ========================================================
            plotResult.outgoing[k] = otAP;
        }
        for (int m = 0; m < FormalRecallLevel.length; m++) {
            if ((plotResult.incomming[m] + plotResult.outgoing[m]) > 0) {
                plotResult.combination[m] =
                        (2 * (plotResult.incomming[m] * plotResult.outgoing[m])) / (plotResult.incomming[m] + plotResult.outgoing[m]);
            } else {
                plotResult.combination[m] = 0.0;
            }
        }
        // =====================================================================

        return plotResult;
    }

    // =========================================================================
    // =========================================================================
    private static HashMap PRPairFormalization(HashMap<Double, Double> iRP) {

        HashMap<Double, Double> intPR = new HashMap<Double, Double>();
        double[] preP = new double[FormalRecallLevel.length];

        for (int i = 0; i < FormalRecallLevel.length; i++) {
            List<Double> PRlist = new ArrayList<Double>();
            Iterator it = iRP.keySet().iterator();
            while (it.hasNext()) {
                Object prKey = it.next();   // prKey == Real Recall Value
                double recallL = FormalRecallLevel[i];
                if (recallL == 0.05) {
                    if (((Double) prKey) <= recallL) {
                        PRlist.add(iRP.get(prKey));
                    }
                } else {
                    if (((Double) prKey) <= recallL && ((Double) prKey) > ((Double) FormalRecallLevel[i - 1])) {
                        PRlist.add(iRP.get(prKey));
                    }
                }
            }
            if (PRlist.isEmpty()) {
                preP[i] = 0.0;
            } else {
                preP[i] = Collections.max(PRlist);
            }
        }
        while (true) {
            int count = 0;
            double tmp = 0.0;
            for (int k = 0; k < preP.length; k++) {
                tmp = preP[k];
                if (k < preP.length - 1) {
                    if (preP[k + 1] > tmp) {
                        preP[k] = preP[k + 1];
                        count++;
                    }
                }
            }
            if (count == 0) {
                break;
            }
        }

        for (int i = 0; i < FormalRecallLevel.length; i++) {
            intPR.put(FormalRecallLevel[i], preP[i]);
        }

        return intPR;
    }
}