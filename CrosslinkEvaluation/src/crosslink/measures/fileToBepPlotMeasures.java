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
public final class fileToBepPlotMeasures {

    private static String outgoingTag = "Outgoing_Links";
    private static String incomingTag = "Incoming_Links";
    public static int RECALL_LEVEL = 20; // 20 Recall Levels
    private static double[] FormalRecallLevel = {0.05, 0.10, 0.15, 0.20, 0.25, 0.30, 0.35, 0.40, 0.45, 0.50,
        0.55, 0.60, 0.65, 0.70, 0.75, 0.80, 0.85, 0.90, 0.95, 1.00
    };
    public static int RESULT_TYPE_NUM = 3;
    private static String plotStatic;
    private static boolean isUseAllTopics = false;
    private static boolean isFileToBEP = false;
    private static boolean isAnchorGToFile = false;
    private static boolean isAnchorGToBEP = false;
    // -------------------------------------------------------------------------
    // 1) to calculate the score of each BEP pointed from the Anchor Topic
    private static int distanceFactor = 1000;
    // 2) set Up Max Anchors per Topic is 50.
    //    If maxAnchors = 0 or a number that is greater than the total returned Anchors
    //    the Anchors retrived will be "the total returned Anchors"
    private static int defaultMaxAnchorsPerTopic = 50;
    // 3) set Up Max BEP links per Anchor is 5.
    //    If maxBepsPerAnchor = 0 or a number that is greater than the total returned BEPs
    //    the BEPs retrived will be "the total returned BEPs"
    private static int defaultMaxBepsPerAnchor = 1;
    // 4) to be the denominator to calculate the score of each Anchor
    //    If bepDenominator is set to 0 here, bepDenominator will be the number of the BEPs in the Anchor.
    //    If the BEP number is assumed to be 5, bepDenominator can be 5.
    private static int bepDenominator = 0;
    // -------------------------------------------------------------------------
    private static boolean useOnlyAnchorGroup = false;
    // -------------------------------------------------------------------------
    private static String currentSourceLang = null;
    private static String currentTargetLang = null;

    private static void log(Object aObject) {
        System.out.println(String.valueOf(aObject));
    }

    private static void errlog(Object aObject) {
        System.err.println(String.valueOf(aObject));
    }

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
            runTable = fileToBepPlotMeasures.getF2BPlotRunSet(runfile);
            resultfile = ResultSetManager.getInstance().getResultSetPathFile(currentSourceLang, currentTargetLang);
            resultTable = ResultSetManager.getInstance().getResultSet(resultfile);
            if (resultTable == null) {
            	resultTable = fileToBepPlotMeasures.getF2BPlotResultSet(new File(resultfile));
            	ResultSetManager.getInstance().addResultSet(resultfile, resultTable);
            }
        } else if (isAnchorGToFile || isAnchorGToBEP) {
            runTable = fileToBepPlotMeasures.getF2BPlotRunSetByGroup(runfile);
            resultfile = ResultSetManager.getInstance().getResultSetPathFile(currentSourceLang, currentTargetLang);
            resultTable = ResultSetManager.getInstance().getResultSet(resultfile);
            if (resultTable == null) {
            	resultTable = fileToBepPlotMeasures.getF2BPlotResultSetByGroup(new File(resultfile));
            	ResultSetManager.getInstance().addResultSet(resultfile, resultTable);
            }
        } else {
        	throw new Exception("Uncertain evaluation type");
        }

        // =================================================================
        // get Run ID: QUT_LTW_RUN_003
        plotResult.plotRunId = fileToBepPlotMeasures.plotStatic;

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

    // =========================================================================
    // -------------------------------------------------------------------------
    // Hashtable: topicID_Outgoing_Links, Vector(bepFileID_bepOffset, ...)
    // notes01: get All Data from "ResultSet.xml"
    // notes02: Incoming links should be further modified
    private static Hashtable getF2BPlotResultSet(File resultfiles) {
        Hashtable f2bResultTable = new Hashtable();
        XMLStreamReader parser;

        try {
            String filePath = resultfiles.getAbsolutePath();
            URL u = new URL("file:///" + filePath);
            InputStream in = u.openStream();
            XMLInputFactory factory = XMLInputFactory.newInstance();
            parser = factory.createXMLStreamReader(in);

            String topicID = "";
            String topicName = "";
            boolean isInOutgoing = false;
            Vector outLinkV = null;
            boolean isInIncoming = false;
            Vector inLinkV = null;

            for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser.next()) {
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        String localName = parser.getLocalName();
                        if (isTopic(localName)) {
                            int attrNo = parser.getAttributeCount();
                            for (int j = 0; j < attrNo; j++) {
                                if (parser.getAttributeLocalName(j).trim().equals("id")) {
                                    topicID = parser.getAttributeValue(j).trim();
                                } else if (parser.getAttributeLocalName(j).trim().equals("name")) {
                                    topicName = parser.getAttributeValue(j).trim();
                                }
                            }
                            break;
                        } else if (isOutgoingLink(localName)) {
                            isInOutgoing = true;
                            outLinkV = new Vector();
                            break;
                        } else if (isIncomingLink(localName)) {
                            isInIncoming = true;
                            inLinkV = new Vector();
                            break;
                        } else if (isOutLink(localName)) {
                            String aName = "";
                            String aOffset = "";
                            String aLength = "";
                            String bFileID = "";
                            String bOffset = "";
                            int attrNo = parser.getAttributeCount();
                            for (int l = 0; l < attrNo; l++) {
                                if (parser.getAttributeLocalName(l).trim().equals("aname")) {
                                    aName = parser.getAttributeValue(l).trim();
                                } else if (parser.getAttributeLocalName(l).trim().equals("aoffset")) {
                                    aOffset = parser.getAttributeValue(l).trim();
                                } else if (parser.getAttributeLocalName(l).trim().equals("alength")) {
                                    aLength = parser.getAttributeValue(l).trim();
                                } else if (parser.getAttributeLocalName(l).trim().equals("boffset")) {
                                    bOffset = parser.getAttributeValue(l).trim();
                                }
                            }
                            bFileID = parser.getElementText().trim();
                            if (isInOutgoing) {
                                if (!outLinkV.contains(bFileID + "_" + bOffset)) {
                                    outLinkV.add(bFileID + "_" + bOffset);
                                }
                            }
                            break;
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        String endLocalName = parser.getLocalName();
                        if (isOutgoingLink(endLocalName)) {
                            isInOutgoing = false;
                            if (outLinkV.size() == 0) {
                                String[] outLinkArray = {""};
                                f2bResultTable.put(topicID + "_" + fileToBepPlotMeasures.outgoingTag, outLinkArray);
                            } else {
                                String[] outLinkArray = new String[outLinkV.size()];
                                int oCounter = 0;
                                Enumeration outEnu = outLinkV.elements();
                                while (outEnu.hasMoreElements()) {
                                    Object outObj = outEnu.nextElement();
                                    outLinkArray[oCounter] = outObj.toString().trim();
                                    oCounter++;
                                }
                                f2bResultTable.put(topicID + "_" + fileToBepPlotMeasures.outgoingTag, outLinkArray);
                            }
                            break;
                        } else if (isIncomingLink(endLocalName)) {
                            isInIncoming = false;
                            // =================================================
                            // Currently there is NO Incoming Links in Result Set
                            // This part should be further modified in the future
                            // =================================================
                            String[] inLinkArray = {""};
                            f2bResultTable.put(topicID + "_" + fileToBepPlotMeasures.incomingTag, inLinkArray);
                        }
                        break;
                }
            }
            parser.close();

        } catch (XMLStreamException ex) {
            Logger.getLogger(fileToBepPlotMeasures.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(fileToBepPlotMeasures.class.getName()).log(Level.SEVERE, null, ex);
        }
        return f2bResultTable;
    }

    // -------------------------------------------------------------------------
    // Hashtable: topicID_Outgoing_Links, Vector(anchorOffset_anchorLength_bepFileID_bepOffset, ...)
    // notes01: get All Data from "ResultSet.xml"
    // notes02: Incoming links should be further modified
    private static Hashtable getF2BPlotResultSetByGroup(File resultfiles) {
        Hashtable f2bResultTableByGroup = new Hashtable();
        XMLStreamReader parser;

        try {
            String filePath = resultfiles.getAbsolutePath();
            URL u = new URL("file:///" + filePath);
            InputStream in = u.openStream();
            XMLInputFactory factory = XMLInputFactory.newInstance();
            parser = factory.createXMLStreamReader(in);

            String topicID = "";
            String topicName = "";
            boolean isInOutgoing = false;
            Vector outLinkV = null;
            boolean isInIncoming = false;
            Vector inLinkV = null;

            for (int event = parser.next(); event != XMLStreamConstants.END_DOCUMENT; event = parser.next()) {
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        String localName = parser.getLocalName();
                        if (isTopic(localName)) {
                            int attrNo = parser.getAttributeCount();
                            for (int j = 0; j < attrNo; j++) {
                                if (parser.getAttributeLocalName(j).trim().equals("id")) {
                                    topicID = parser.getAttributeValue(j).trim();
                                } else if (parser.getAttributeLocalName(j).trim().equals("name")) {
                                    topicName = parser.getAttributeValue(j).trim();
                                }
                            }
                            break;
                        } else if (isOutgoingLink(localName)) {
                            isInOutgoing = true;
                            outLinkV = new Vector();
                            break;
                        } else if (isIncomingLink(localName)) {
                            isInIncoming = true;
                            inLinkV = new Vector();
                            break;
                        } else if (isOutLink(localName)) {
                            String aName = "";
                            String aOffset = "";
                            String aLength = "";
                            String bFileID = "";
                            String bOffset = "";
                            int attrNo = parser.getAttributeCount();
                            for (int l = 0; l < attrNo; l++) {
                                if (parser.getAttributeLocalName(l).trim().equals("aname")) {
                                    aName = parser.getAttributeValue(l).trim();
                                } else if (parser.getAttributeLocalName(l).trim().equals("aoffset")) {
                                    aOffset = parser.getAttributeValue(l).trim();
                                } else if (parser.getAttributeLocalName(l).trim().equals("alength")) {
                                    aLength = parser.getAttributeValue(l).trim();
                                } else if (parser.getAttributeLocalName(l).trim().equals("boffset")) {
                                    bOffset = parser.getAttributeValue(l).trim();
                                }
                            }
                            bFileID = parser.getElementText().trim();
                            if (isInOutgoing) {
                                if (!outLinkV.contains(aOffset + "_" + aLength + "_" + bFileID + "_" + bOffset)) {
                                    outLinkV.add(aOffset + "_" + aLength + "_" + bFileID + "_" + bOffset);
                                }
                            }
                            break;
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        String endLocalName = parser.getLocalName();
                        if (isOutgoingLink(endLocalName)) {
                            isInOutgoing = false;
                            if (outLinkV.size() == 0) {
                                String[] outLinkArray = {""};
                                f2bResultTableByGroup.put(topicID + "_" + fileToBepPlotMeasures.outgoingTag, outLinkArray);
                            } else {
                                String[] outLinkArray = new String[outLinkV.size()];
                                int oCounter = 0;
                                Enumeration outEnu = outLinkV.elements();
                                while (outEnu.hasMoreElements()) {
                                    Object outObj = outEnu.nextElement();
                                    outLinkArray[oCounter] = outObj.toString().trim();
                                    oCounter++;
                                }
                                f2bResultTableByGroup.put(topicID + "_" + fileToBepPlotMeasures.outgoingTag, outLinkArray);
                            }
                            break;
                        } else if (isIncomingLink(endLocalName)) {
                            isInIncoming = false;
                            // =================================================
                            // Currently there is NO Incoming Links in Result Set
                            // This part should be further modified in the future
                            // =================================================
                            String[] inLinkArray = {""};
                            f2bResultTableByGroup.put(topicID + "_" + fileToBepPlotMeasures.incomingTag, inLinkArray);
                        }
                        break;
                }
            }
            parser.close();

        } catch (XMLStreamException ex) {
            Logger.getLogger(fileToBepMeasures.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(fileToBepMeasures.class.getName()).log(Level.SEVERE, null, ex);
        }
        return f2bResultTableByGroup;
    }

    // -------------------------------------------------------------------------
    // Hashtable: topicID_Outgoing_Links, Vector(bepFileID_bepOffset, ...)
    // notes01: Since this is NOT an Anchor-to-BEP evaluation,
    //          we do not separate BEPs into Anchor Group,
    //          but compare them one by one as "BepFileID-Offset".
    //          We do not limit the number of Anchors per Topic as well as BEPs per Anchor
    // notes02: Incoming links should be further modified
    private static Hashtable getF2BPlotRunSet(File runfiles) throws Exception {
        Hashtable f2bRunTable = new Hashtable();

        try {

            JAXBContext jc;
            jc = JAXBContext.newInstance("crosslink.rungenerator");
            Unmarshaller um = jc.createUnmarshaller();
            InexSubmission is = (InexSubmission) ((um.unmarshal(runfiles)));
            
            currentSourceLang = is.getSourceLang();
            if (currentSourceLang == null || currentSourceLang.length() == 0)
            	currentSourceLang = "en";
            
            // default lang is the target lang
            currentTargetLang = is.getDefaultLang();
            if (currentTargetLang == null || currentTargetLang.length() == 0)
            	throw new Exception(String.format("Incorrect run file - %s which dosen't provide the target language", runfiles.getAbsoluteFile()));

            plotStatic = is.getRunId();
            for (int i = 0; i < is.getTopic().size(); i++) {

                int endP = is.getTopic().get(i).getFile().toLowerCase().indexOf(".xml");
                String topicID = is.getTopic().get(i).getFile().substring(0, endP);
                // -------------------------------------------------------------
                // populate Outgoing Link Data from the submission Run
                String[] outLinks = null;
                if (!is.getTopic().get(i).getOutgoing().getAnchor().isEmpty()) {

                    Vector outF2BV = new Vector();
                    for (int j = 0; j < is.getTopic().get(i).getOutgoing().getAnchor().size(); j++) {

                        String toFile = "";
                        String toFileID = "";
                        String toBep = "";
                        List<crosslink.rungenerator.ToFileType> linkTo = is.getTopic().get(i).getOutgoing().getAnchor().get(j).getTofile();
                        for (int k = 0; k < linkTo.size(); k++) {
                            toFile = linkTo.get(k).getFile().toString().trim();
                            if (!toFile.equals("")) {
                                int endop = toFile.toLowerCase().indexOf(".xml");
                                if (endop != -1) {
                                    toFileID = toFile.substring(0, endop);
                                }
                            }
                            if (linkTo.get(k).getBep_offset() == null) {
                                toBep = "0";
                            } else {
                                toBep = linkTo.get(k).getBep_offset().toString().trim();
                            }
                            // ---------------------------------------------
                            // 1234_58
                            if (!outF2BV.contains(toFileID + "_" + toBep)) {
                                outF2BV.add(toFileID + "_" + toBep);
                            }
                        }
                    }

                    if (outF2BV.size() >= 1) {
                        outLinks = new String[outF2BV.size()];
                        int olCounter = 0;
                        Enumeration olEnu = outF2BV.elements();
                        while (olEnu.hasMoreElements()) {
                            Object olObj = olEnu.nextElement();
                            outLinks[olCounter] = olObj.toString().trim();
                            olCounter++;
                        }
                    } else {
                        outLinks = new String[1];
                        outLinks[0] = "";
                    }

                } else {
                    outLinks = new String[1];
                    outLinks[0] = "";
                }
                f2bRunTable.put(topicID + "_" + outgoingTag, outLinks);

                // -------------------------------------------------------------
                // populate Incoming Link Data in the submission Run
                // (XXX) There is NO Incoming Links Currently.
                // (XXX) Thus, this part should be further modified in the future
//                String[] inLinks = new String[is.getTopic().get(i).getIncoming(getAnchor()().size()];
//                if (!is.getTopic().get(i).getIncoming().getLink().isEmpty()) {
//                    for (int k = 0; k < is.getTopic().get(i).getIncoming().getLink().size(); k++) {
//                        String fromFile = is.getTopic().get(i).getIncoming().getLink().get(k).getAnchor().getFile().toString().trim();
//                        if (!fromFile.equals("")) {
//                            int endip = fromFile.toLowerCase().indexOf(".xml");
//                            if (endip != -1) {
//                                inLinks[k] = fromFile.substring(0, endip);
//                            }
//                        }
//                    }
//                    if (inLinks[0] == null) {
//                        inLinks = new String[1];
//                        inLinks[0] = "";
//                    }
//                } else {
//                    inLinks = new String[1];
//                    inLinks[0] = "";
//                }
//                f2bRunTable.put(topicID + "_" + incomingTag, inLinks);
                // =============================================================
            }

        } catch (JAXBException ex) {
            ex.printStackTrace();
        }

        return f2bRunTable;
    }

    // -------------------------------------------------------------------------
    // Hashtable: topicID_Outgoing_Links, Vector(anchorOffset_anchorLength_bepFileID_bepOffset, ...)
    // notes01: get a pre-set number of Data from Submission Run
    //          maxAnchors == 0 or > size(): equal to size(), otherwise, specified number (e.g. 50)
    //          maxBepsPerAnchor == 0 or > size(): equal to size(), otherwise, specified number (e.g. 5)
    // notes02: Incoming links should be further modified
    private static Hashtable getF2BPlotRunSetByGroup(File runfiles) throws Exception {

        Hashtable f2bRunTableByGroup = new Hashtable();
        try {
            JAXBContext jc;
            jc = JAXBContext.newInstance("crosslink.rungenerator");
            Unmarshaller um = jc.createUnmarshaller();
            InexSubmission is = (InexSubmission) ((um.unmarshal(runfiles)));

            currentSourceLang = is.getSourceLang();
            if (currentSourceLang == null || currentSourceLang.length() == 0)
            	currentSourceLang = "en";
            
            // default lang is the target lang
            currentTargetLang = is.getDefaultLang();
            if (currentTargetLang == null || currentTargetLang.length() == 0)
            	throw new Exception(String.format("Incorrect run file - %s which dosen't provide the target language", runfiles.getAbsoluteFile()));

            
            plotStatic = is.getRunId();
            // Loop Different Topics
            for (int i = 0; i < is.getTopic().size(); i++) {

                int endP = is.getTopic().get(i).getFile().toLowerCase().indexOf(".xml");
                String topicID = is.getTopic().get(i).getFile().substring(0, endP);
                // -------------------------------------------------------------
                // Inside Outgoing Links
                String[] outLinks = null;
                if (!is.getTopic().get(i).getOutgoing().getAnchor().isEmpty()) {

                    Vector outF2GroupBepV = new Vector();
                    // Loop Each Group by Anchor
                    // BUT the same anchor may distribute in different groups
                    int maxAnchors = defaultMaxAnchorsPerTopic;
                    if (maxAnchors == 0) {
                        maxAnchors = is.getTopic().get(i).getOutgoing().getAnchor().size();
                    } else if (maxAnchors >= is.getTopic().get(i).getOutgoing().getAnchor().size()) {
                        maxAnchors = is.getTopic().get(i).getOutgoing().getAnchor().size();
                    }
                    for (int j = 0; j < maxAnchors; j++) {

                        // to get AnchorInfo: File, Offset & Length ------------
                        String aFile = "";
                        String aOffset = "";
                        String aLength = "";

//                        aFile = is.getTopic().get(i).getOutgoing().getAnchor().get(j).getAnchor().getFile();
                        aOffset = is.getTopic().get(i).getOutgoing().getAnchor().get(j).getOffset();
//                        aOffset = String.valueOf(Math.floor(1000000000 * Math.random()));
                        if (is.getTopic().get(i).getOutgoing().getAnchor().get(j) == null) {
                            aLength = "10"; //kludge - when there is no anchor in the submission, just F2F
                        } else {
                            aLength = is.getTopic().get(i).getOutgoing().getAnchor().get(j).getLength();
                        }
                        // -----------------------------------------------------
                        String toFile = "";
                        String toFileID = "";
                        String toBep = "";
                        List<crosslink.rungenerator.ToFileType> linkTo = is.getTopic().get(i).getOutgoing().getAnchor().get(j).getTofile();
                        // -----------------------------------------------------
                        int maxBepsPerAnchor = defaultMaxBepsPerAnchor;
                        if (maxBepsPerAnchor == 0) {
                            maxBepsPerAnchor = linkTo.size();
                        } else if (maxBepsPerAnchor >= linkTo.size()) {
                            maxBepsPerAnchor = linkTo.size();
                        }
                        // -----------------------------------------------------
                        // see below: an Anchor may be pointed to a number of BEP links
                        // <link><anchor></anchor><linkto></linkto><linkto></linkto>...</link>
                        for (int k = 0; k < maxBepsPerAnchor; k++) {
                            toFile = linkTo.get(k).getFile().toString().trim();
                            if (!toFile.equals("")) {
                                int endop = toFile.toLowerCase().indexOf(".xml");
                                if (endop != -1) {
                                    toFileID = toFile.substring(0, endop);
                                }
                            }
                            if (linkTo.get(k).getBep_offset() == null) {
                                toBep = "0";
                            } else {
                                toBep = linkTo.get(k).getBep_offset().toString().trim();
                            }
                            // -------------------------------------------------
                            // to eliminate duplicate Anchor-BEP links
                            // aOffset_aLength_bFileID_bBep
                            // 99_13_1234_58
                            if (!outF2GroupBepV.contains(aOffset + "_" + aLength + "_" + toFileID + "_" + toBep)) {
                                outF2GroupBepV.add(aOffset + "_" + aLength + "_" + toFileID + "_" + toBep);
                            }
                        }
                        // ---------------------------------------------------------
                        // we can see a problem here WHEN the run format is incorrect
                        // by distributing the same Anchor into different <link></link> tags
                        // For each <link> tag, the program will get "maxBepsPerAnchor" BEPs
                        // for each anchor, even they are the same anchor (offset-length)
                        // *** We can resolve this PROBLEM
                        // when we calculate the performance (measurement) ***
                        // ---------------------------------------------------------
                    }
                    // ---------------------------------------------------------
                    // transfer Vector data into String Array for returning
                    if (outF2GroupBepV.size() >= 1) {
                        outLinks = new String[outF2GroupBepV.size()];
                        int olCounter = 0;
                        Enumeration olEnu = outF2GroupBepV.elements();
                        while (olEnu.hasMoreElements()) {
                            Object olObj = olEnu.nextElement();
                            outLinks[olCounter] = olObj.toString().trim();
                            olCounter++;
                        }
                    } else {
                        outLinks = new String[1];
                        outLinks[0] = "";
                    }
                    // ---------------------------------------------------------
                    outF2GroupBepV.clear();

                } else {
                    outLinks = new String[1];
                    outLinks[0] = "";
                }
                f2bRunTableByGroup.put(topicID + "_" + outgoingTag, outLinks);
                // =============================================================

                // -------------------------------------------------------------
                // Inside Incoming Links
                // (XXX) There is NO Incoming Links Currently.
                //       Thus, this part should be further modified in the future
//                String[] inLinks = new String[is.getTopic().get(i).getIncoming().getLink().size()];
//                if (!is.getTopic().get(i).getIncoming().getLink().isEmpty()) {
//                    for (int k = 0; k < is.getTopic().get(i).getIncoming().getLink().size(); k++) {
//                        String fromFile = is.getTopic().get(i).getIncoming().getLink().get(k).getAnchor().getFile().toString().trim();
//                        if (!fromFile.equals("")) {
//                            int endip = fromFile.toLowerCase().indexOf(".xml");
//                            if (endip != -1) {
//                                inLinks[k] = fromFile.substring(0, endip);
//                            }
//                        }
//                    }
//                    if (inLinks[0] == null) {
//                        inLinks = new String[1];
//                        inLinks[0] = "";
//                    }
//                } else {
//                    inLinks = new String[1];
//                    inLinks[0] = "";
//                }
//                f2bRunTableByGroup.put(topicID + "_" + incomingTag, inLinks);
                // =================================================================
            }

        } catch (JAXBException ex) {
            ex.printStackTrace();
        }

        return f2bRunTableByGroup;
    }
    // =========================================================================
    static String topicIndicator = "ltw_Topic";

    private static boolean isTopic(String name) {
        if (name.equals(topicIndicator)) {
            return true;
        }

        return false;
    }
    static String outGoingIndicator = "outgoingLinks";

    private static boolean isOutgoingLink(String name) {
        if (name.equals(outGoingIndicator)) {
            return true;
        }

        return false;
    }
    static String inComingIndicator = "incomingLinks";

    private static boolean isIncomingLink(String name) {
        if (name.equals(inComingIndicator)) {
            return true;
        }

        return false;
    }
    static String outLinkIndicator = "outLink";

    private static boolean isOutLink(String name) {
        if (name.equals(outLinkIndicator)) {
            return true;
        }

        return false;
    }
    // =========================================================================
}
