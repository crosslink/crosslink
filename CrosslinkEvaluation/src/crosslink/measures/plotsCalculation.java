/*
 * plotsCalculation.java
 *
 * To caculate Basic Measures of Answer Set
 * Recall = (Relevant Docs Retrieved)/(Relevant Docs)
 * Precision = (Relevant Docs Retrieved)/(Retrieved Docs)
 */
package crosslink.measures;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import crosslink.resultsetGenerator.LtwResultsetType;
import crosslink.rungenerator.InexSubmission;

/**
 * Created on 1 October 2007, 12:48
 * @author Darren Huang
 */
public final class plotsCalculation {

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
    private static boolean useRestrictedNum = false;
    // define the number of links accepted in Incoming & Outgoing
    private static int limitedOutLinks = 50;
    private static int limitedInLinks = 250;
    

    private static void log(Object aObject) {
        System.out.println(String.valueOf(aObject));
    }

    private static void errlog(Object aObject) {
        System.err.println(String.valueOf(aObject));
    }

    public plotsCalculation() {
    }

    public static class PRCurveResult {

        public PRCurveResult() {
        }

        ;
        public String plotRunId;
        public double[] incomming = new double[plotsCalculation.RECALL_LEVEL];
        public double[] outgoing = new double[plotsCalculation.RECALL_LEVEL];
        public double[] combination = new double[plotsCalculation.RECALL_LEVEL];
    }

    public static PRCurveResult plotCalculate(File resultfile, File runfile, boolean isAllTopics, boolean useFileToBep, boolean useAnchorToFile, boolean useAnchorToBEP) throws Exception {

        isUseAllTopics = isAllTopics ? true : false;
        isFileToBEP = useFileToBep ? true : false;
        isAnchorGToFile = useAnchorToFile ? true : false;
        isAnchorGToBEP = useAnchorToBEP ? true : false;

        PRCurveResult plotResult = new PRCurveResult();
        // resultTable: <topicID_outgoing, link> & <topicID_incoming, link>
        // runTable: <topicID_outgoing, link> & <topicID_incoming, link>
        Hashtable resultTable = null;
        Hashtable runTable = null;
        if (isFileToBEP || isAnchorGToFile || isAnchorGToBEP) {

            plotResult = fileToBepPlotMeasures.getFileToBepPlotResult(resultfile, runfile, isAllTopics, useFileToBep, useAnchorToFile, useAnchorToBEP);

        } else {
            resultTable = plotsCalculation.getResultSet(resultfile);
            runTable = plotsCalculation.getRunSet(runfile);

            // =================================================================
            // get Run ID: QUT_LTW_RUN_003
            plotResult.plotRunId = plotsCalculation.plotStatic;

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

                // =============================================================
                // Run:
                // group BEP links by Anchor
                // ResultSet:
                // get total number of Anchors for this Topic: key: Outgoing / Incoming
                int rsAnchorNoPerTopic = 0;
                Hashtable<String, Vector> runHT = new Hashtable<String, Vector>();
                Vector runIndexV = new Vector();
                // =============================================================
                // =============================================================
                double APinR = 0.0;
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
                    // Get non-duplicate run items
                    List plotItemsList = new ArrayList();
                    for (int i = 0; i < runValues.length; i++) {
                        String item = runValues[i];
                        if (!plotItemsList.contains(item)) {
                            plotItemsList.add(item);
                        }
                    }
                    String[] plotItems = (String[]) plotItemsList.toArray(new String[plotItemsList.size()]);
                    // =========================================================
                    // Loop each BEP link inside a Topic
                    int k = 0;
                    for (int i = 0; i < plotItems.length; i++) {
                        k = k + 1;
                        // get link ID at position i
                        String link = plotItems[i].toString().trim();
                        boolean isMatched = false;
                        // Check the (incoming/outgoing) link with Result Set
                        for (int j = 0; j < resultSet.length; j++) {
                            if (link.equalsIgnoreCase(resultSet[j].trim())) {
                                isMatched = true;
                            }
                        }
                        if (isMatched) {
                            mCount = mCount + 1;   // matched count: # of documents @ N
                            // =================================================
                            precAtR = (double) mCount / (k);
//                            APinR += (double) mCount / (k);
//                            if (mCount > 0) {
//                                precAtR = (double) APinR / (mCount);
//                            } else {
//                                precAtR = 0;
//                            }
                            // -------------------------------------------------
                            if (useRestrictedNum) {
                                // restrict the number of outgoing/incoming links
                                //
                                if (key.endsWith(outgoingTag)) {
                                    int minResultNum = Math.min(limitedOutLinks, resultSet.length);
                                    recallLevel = (double) mCount / minResultNum;
                                } else {
                                    int minResultNum = Math.min(limitedInLinks, resultSet.length);
                                    recallLevel = (double) mCount / minResultNum;
                                }
                            } else {
                                recallLevel = (double) mCount / resultSet.length;
                            }
                            // -------------------------------------------------
                            RPmap.put(recallLevel, precAtR);
                        }
                    }
                }
                HashMap InterpolatedPR = PRPairFormalization(RPmap);
                if (key.endsWith(outgoingTag)) {
                    outgoingPRData.put(key, InterpolatedPR);
                } else if (key.endsWith(incomingTag)) {
                    incomingPRData.put(key, InterpolatedPR);
                }
            }
            // =================================================================
            // For incoming links: calculate the average precision score
            //                     for each submission
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
                // -------------------------------------------------------------
                double inAP = 0.0;
                if (isUseAllTopics) {
                    inAP = (double) inaveP / (resultTable.size() / 2);
                } else {
                    inAP = (double) inaveP / inCount;
                }
                // -------------------------------------------------------------
                plotResult.incomming[j] = inAP;
            }
            // =================================================================
            // For outgoing links: calculate the average precision score
            //                     for each submission
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
                // -------------------------------------------------------------
                // resultTable contains topics of outgoing and incoming
                double otAP = 0.0;
                if (isUseAllTopics) {
                    otAP = (double) otaveP / (resultTable.size() / 2);
                } else {
                    otAP = (double) otaveP / otCount;
                }
                // -------------------------------------------------------------
                plotResult.outgoing[k] = otAP;
            }
            // =================================================================
            // For F-Score (combination of outgoing and incoming)
            for (int m = 0; m < FormalRecallLevel.length; m++) {
                if ((plotResult.incomming[m] + plotResult.outgoing[m]) > 0) {
                    plotResult.combination[m] =
                            (2 * (plotResult.incomming[m] * plotResult.outgoing[m])) / (plotResult.incomming[m] + plotResult.outgoing[m]);
                } else {
                    plotResult.combination[m] = 0.0;
                }
            }
            // =================================================================
            // =================================================================
            // End of File-To-File Plot calculation
        }
        return plotResult;
    }

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

    private static Hashtable getResultSet(File resultfiles) {
        //
        Hashtable resultTable = new Hashtable();
        try {
            JAXBContext jc;
            jc = JAXBContext.newInstance("crosslink.resultsetGenerator");
            Unmarshaller um = jc.createUnmarshaller();
            LtwResultsetType lrs = (LtwResultsetType) ((um.unmarshal(resultfiles)));

            if (lrs.getLtwTopic().size() > 0) {
                for (int i = 0; i < lrs.getLtwTopic().size(); i++) {

                    int inCount = 0;
                    int outCount = 0;

                    String topicID = lrs.getLtwTopic().get(i).getId().trim();

                    if (lrs.getLtwTopic().get(i).getOutgoingLinks().getOutLink().isEmpty()) {
                        String[] outLinks = {""};
                        resultTable.put(topicID + "_" + plotsCalculation.outgoingTag, outLinks);
                    } else {
                        Vector outLinksV = new Vector();
                        for (int j = 0; j < lrs.getLtwTopic().get(i).getOutgoingLinks().getOutLink().size(); j++) {
                            String outLinkStr = lrs.getLtwTopic().get(i).getOutgoingLinks().getOutLink().get(j).getValue().toString().trim();
                            if (!outLinksV.contains(outLinkStr)) {
                                outLinksV.add(outLinkStr);
                            }
                        }
                        String[] outLinks = new String[outLinksV.size()];
                        Enumeration oEnu = outLinksV.elements();
                        while (oEnu.hasMoreElements()) {
                            Object obj = oEnu.nextElement();
                            outLinks[outCount] = obj.toString().trim();
                            outCount++;
                        }
                        resultTable.put(topicID + "_" + plotsCalculation.outgoingTag, outLinks);
                    }
                    if (lrs.getLtwTopic().get(i).getIncomingLinks().getInLink().isEmpty()) {
                        String[] inLinks = {""};
                        resultTable.put(topicID + "_" + plotsCalculation.incomingTag, inLinks);
                    } else {
                        Vector inLinksV = new Vector();
                        for (int k = 0; k < lrs.getLtwTopic().get(i).getIncomingLinks().getInLink().size(); k++) {
                            String inLinkStr = lrs.getLtwTopic().get(i).getIncomingLinks().getInLink().get(k).getValue().toString().trim();
                            if (!inLinksV.contains(inLinkStr)) {
                                inLinksV.add(inLinkStr);
                            }
                        }
                        String[] inLinks = new String[inLinksV.size()];
                        Enumeration iEnu = inLinksV.elements();
                        while (iEnu.hasMoreElements()) {
                            Object obj = iEnu.nextElement();
                            inLinks[inCount] = obj.toString().trim();
                            inCount++;
                        }
                        resultTable.put(topicID + "_" + plotsCalculation.incomingTag, inLinks);
                    }
                }
            }

        } catch (JAXBException ex) {
            ex.printStackTrace();
        }

        return resultTable;
    }

    private static Hashtable getRunSet(File runfiles) {
        Hashtable runTable = new Hashtable();
        try {
            JAXBContext jc;
            jc = JAXBContext.newInstance("crosslink.rungenerator");
            Unmarshaller um = jc.createUnmarshaller();
            InexSubmission pis = (InexSubmission) (um.unmarshal(runfiles));
            plotStatic = pis.getRunId();
            for (int i = 0; i < pis.getTopic().size(); i++) {

                int endP = pis.getTopic().get(i).getFile().toLowerCase().indexOf(".xml");
                String topicID = pis.getTopic().get(i).getFile().substring(0, endP);

                String[] outLinks = new String[pis.getTopic().get(i).getOutgoing().getAnchor().size()];
                if (!pis.getTopic().get(i).getOutgoing().getAnchor().isEmpty()) {

                    Vector outF2FV = new Vector();
                    for (int j = 0; j < pis.getTopic().get(i).getOutgoing().getAnchor().size(); j++) {

                        String toFile = "";
                        String toFileID = "";
                        String toBep = "";
                        List<crosslink.rungenerator.ToFileType> linkTo = pis.getTopic().get(i).getOutgoing().getAnchor().get(j).getTofile();
                        for (int k = 0; k < linkTo.size(); k++) {
                            toFile = linkTo.get(k).getFile().toString().trim();
                            if (!toFile.equals("")) {
                                int endop = toFile.toLowerCase().indexOf(".xml");
                                if (endop != -1) {
                                    toFileID = toFile.substring(0, endop);
                                }
                            }
                            if (!outF2FV.contains(toFileID)) {
                                outF2FV.add(toFileID);
                            }
                        }
                    }
                    if (outF2FV.size() >= 1) {
                        outLinks = new String[outF2FV.size()];
                        int olCounter = 0;
                        Enumeration olEnu = outF2FV.elements();
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
                runTable.put(topicID + "_" + outgoingTag, outLinks);

//                String[] inLinks = new String[pis.getTopic().get(i).getIncoming().getLink().size()];
//                if (!pis.getTopic().get(i).getIncoming().getLink().isEmpty()) {
//                    for (int k = 0; k < pis.getTopic().get(i).getIncoming().getLink().size(); k++) {
//                        String fromFile = pis.getTopic().get(i).getIncoming().getLink().get(k).getAnchor().getFile().toString().trim();
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
//                runTable.put(topicID + "_" + incomingTag, inLinks);

            }

        } catch (JAXBException ex) {
            ex.printStackTrace();
        }

        return runTable;
    }
}
