/*
 * ltwRecall.java
 *
 * To caculate Basic Measures of Answer Set
 * Recall = (Relevant Docs Retrieved)/(Relevant Docs)
 * Precision = (Relevant Docs Retrieved)/(Retrieved Docs)
 * R-Precision = The Precision at the R cut-off number, R is the number of relevant document
 * Average Precision = Sigma(P(r)*rel(r))/(number of relevant documents)
 * MAP (Mean Average Precision) = Sigma(APqi)/N
 * Precision@ = The Precision at the cut-off number: @5, @10, @20, @30, @50, @250
 */
package crosslink.measures;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import crosslink.resultsetGenerator.LtwResultsetType;
import crosslink.rungenerator.InexSubmission;

/**
 * Created on 27 September 2007, 11:26
 * @author Darren Huang
 */
public final class metricsCalculation {

    private static String outgoingTag = "Outgoing_Links";
    private static String incomingTag = "Incoming_Links";
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
    private static String runStatic;
    private static boolean isUseAllTopics = false;
    private static boolean isFileToBEP = false;
    private static boolean isAnchorGToFile = false;
    private static boolean isAnchorGToBEP = false;
    // -------------------------------------------------------------------------
    // define the number of links accepted in Incoming & Outgoing
    private static boolean useRestrictedNum = false;
    private static int limitedOutLinks = 50;
    private static int limitedInLinks = 250;


    private static void log(Object aObject) {
        System.out.println(String.valueOf(aObject));
    }

    private static void errlog(Object aObject) {
        System.err.println(String.valueOf(aObject));
    }

    private metricsCalculation() {
    }

    ;

    public static class EvaluationResult {

        public EvaluationResult() {
        }

        ;
        public String runId;
        public double[] incomming = new double[metricsCalculation.COL_NUM];
        public double[] outgoing = new double[metricsCalculation.COL_NUM];
        public double[] combination = new double[metricsCalculation.COL_NUM];
    }

    public static EvaluationResult calculate(File resultfiles, File runfiles, boolean isAllTopics, boolean useFileToBep, boolean useAnchorToFile, boolean useAnchorToBEP) throws Exception {

        isUseAllTopics = isAllTopics ? true : false;
        isFileToBEP = useFileToBep ? true : false;
        isAnchorGToFile = useAnchorToFile ? true : false;
        isAnchorGToBEP = useAnchorToBEP ? true : false;

        EvaluationResult result = new EvaluationResult();
        Hashtable resultTable = null;
        Hashtable runTable = null;

        if (isFileToBEP || isAnchorGToFile || isAnchorGToBEP) {

            result = fileToBepMeasures.getFileToBepResult(resultfiles, runfiles, isAllTopics, useFileToBep, useAnchorToFile, useAnchorToBEP);

        } else {

            resultTable = metricsCalculation.getResultSet(resultfiles);
            runTable = metricsCalculation.getRunSet(runfiles);

            result.runId = metricsCalculation.runStatic;
            // =================================================================
            String tempRunID = result.runId;
            // =================================================================
            // <editor-fold defaultstate="collapsed" desc="MAP: Mean Average Precision">
            // Get MAP
            double[] oiMAP = getOutInMAP(tempRunID, resultTable, runTable);

            double outgoingMAP = oiMAP[0];
            double outMAP = 0.00001 * Math.random();
            if (outgoingMAP > 0) {
                outgoingMAP = outMAP + outgoingMAP;
            }

            double incomingMAP = oiMAP[1];
            double MAP = 0.00001 * Math.random();
            if ((outgoingMAP + incomingMAP) > 0) {
                MAP = MAP + (double) 2 * (outgoingMAP * incomingMAP) / (outgoingMAP + incomingMAP);
            }
            result.outgoing[metricsCalculation.R_MAP] = outgoingMAP;
            result.incomming[metricsCalculation.R_MAP] = incomingMAP;
            result.combination[metricsCalculation.R_MAP] = MAP;
            // </editor-fold>
            // =====================================================================
            // Get R-Precision
            double[] oiRPrecs = getOutInRPrecs(resultTable, runTable);
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
            double[][] oiPrecsAT = getOutInPrecsAT(resultTable, runTable);
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

    private static Hashtable getResultSet(File resultfiles) {
        
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
                        resultTable.put(topicID + "_" + metricsCalculation.outgoingTag, outLinks);
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
                        resultTable.put(topicID + "_" + metricsCalculation.outgoingTag, outLinks);
                    }
                    if (lrs.getLtwTopic().get(i).getIncomingLinks().getInLink().isEmpty()) {
                        String[] inLinks = {""};
                        resultTable.put(topicID + "_" + metricsCalculation.incomingTag, inLinks);
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
                        resultTable.put(topicID + "_" + metricsCalculation.incomingTag, inLinks);
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
            InexSubmission is = (InexSubmission) ((um.unmarshal(runfiles)));

            runStatic = is.getRunId();
            for (int i = 0; i < is.getTopic().size(); i++) {

//                int endP = is.getTopic().get(i).getFile().toLowerCase().indexOf(".xml");
                String topicID = is.getTopic().get(i).getFile(); //.substring(0, endP);

                String[] outLinks = null;
                if (!is.getTopic().get(i).getOutgoing().getAnchor().isEmpty()) {

                    Vector outF2FV = new Vector();
                    for (int j = 0; j < is.getTopic().get(i).getOutgoing().getAnchor().size(); j++) {

                        String toFile = "";
//                        String toFileID = "";
                        String toBep = "";
                        List<crosslink.rungenerator.ToFileType> linkTo = is.getTopic().get(i).getOutgoing().getAnchor().get(j).getTofile();
                        for (int k = 0; k < linkTo.size(); k++) {
                            toFile = linkTo.get(k).getFile().toString().trim();
//                            toFileId = toFile;
//                            if (!toFile.equals("")) {
//                                int endop = toFile.toLowerCase().indexOf(".xml");
//                                if (endop != -1) {
//                                    toFileID = toFile.substring(0, endop);
//                                }
//                            }
                            if (!outF2FV.contains(toFile)) {
                                outF2FV.add(toFile);
                            } else {
                                log(topicID + "<-- Topic ID: Duplicated: " + toFile);
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
//                runTable.put(topicID + "_" + incomingTag, inLinks);
            }

        } catch (JAXBException ex) {
            ex.printStackTrace();
        }

        return runTable;
    }

    private static double[] getOutInMAP(String tempRunId, Hashtable resultTable, Hashtable runTable) {
        double[] oimap = new double[2];
        double aTopicAvePrecision = 0.0;
        double outgoingAPs = 0.0;
        double incomingAPs = 0.0;

        // =============================================================
        Hashtable outgoingHt = new Hashtable();
        Hashtable incomingHt = new Hashtable();
        // =============================================================

        // Loop all Topics in the submitted run
        for (Enumeration e = runTable.keys(); e.hasMoreElements();) {
            double ffmCount = 0.0;
            double abmCount = 0.0;
            double anchorScore = 0.0;
            String key = e.nextElement().toString();
            // get RUN (incoming or outgoing) links array for each Topic
            String[] runValues = (String[]) runTable.get(key);
            // get Result (incoming or outgoing) links array for each Topic
            String[] resultSet = (String[]) resultTable.get(key);
            // =================================================================
            // =================================================================

            double APinR = 0.0;
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
                /**
                 * The same outgoing link may have multiple destination
                 * but these multiple destinations can only be counted as 1
                 * Precision = Relevant Documents Retrieved / Total Retrieved Documents
                 * Recall = Relevant Documents Retrieved / Total Relevant Docuements
                 */
                int k = 0;
                List relData = new ArrayList();
                for (int i = 0; i < runValues.length; i++) {
                    String link = runValues[i].trim();

                    // duplicate links should NOT be included
                    // in the submission run
                    if (!relData.contains(link)) {
                        relData.add(link);
                        k = k + 1;
                        // To caculate Average Precision for each topic
                        boolean isMatched = false;
                        for (int j = 0; j < resultSet.length; j++) {
                            // To find out if a RUN link in the Result Set (links)
                            if (link.equalsIgnoreCase(resultSet[j].trim())) {
                                isMatched = true;
                            }
                        }
                        // =====================================================
                        // Precision = Relevant Documents Retrieved / Nu of Retrieved Documents at R
                        // High precision indicates that most of the items you retrieve are relevant.
                        // Here: we cumulate Precision Value of Each Anchor-(Bep Link) in a Topic
                        if (isMatched) {
                            ffmCount = ffmCount + 1;
                            APinR += (double) ffmCount / (k);
                        }
                    }
                }
                // THEN, calculate the Precision for the Topic
                // Basically, APinR should be divided by "rsAnchorNoPerTopic" <-- number of Anchors in the ResultSet Topic.
                // BUT this target is File-to-BEP, we don't know Exactly how many Anchors in the ResultSet
                //     and this Number may be meaningless
                // Here, we temporarily use "anchorCounter" in Run to replace it
                // to prevent from producing funny result
                if (useRestrictedNum) {
                    if (key.endsWith("Outgoing_Links")) {
                        int rsNumber = Math.min(limitedOutLinks, resultSet.length);
                        aTopicAvePrecision = (double) APinR / (rsNumber);
                    } else if (key.endsWith("Incoming_Links")) {
                        int rsNumber = Math.min(limitedInLinks, resultSet.length);
                        aTopicAvePrecision = (double) APinR / (rsNumber);
                    }
                } else {
                    aTopicAvePrecision = (double) APinR / (resultSet.length);
                }
                // =============================================================
                if (key.endsWith("Outgoing_Links")) {
                    outgoingHt.put(key.substring(0, key.indexOf("_Outgoing_Links")), relData.size());
                } else if (key.endsWith("Incoming_Links")) {
                    incomingHt.put(key.substring(0, key.indexOf("_Incoming_Links")), relData.size());
                }
            }

            // Add "aTopicAvePrecision" to related APs
            if (key.endsWith(outgoingTag)) {
                outgoingAPs += aTopicAvePrecision;
            } else if (key.endsWith(incomingTag)) {
                incomingAPs += aTopicAvePrecision;
            }
        }
        if (isUseAllTopics) {
            oimap[0] = (double) outgoingAPs / (resultTable.size() / 2);
            oimap[1] = (double) incomingAPs / (resultTable.size() / 2);
        } else {
            oimap[0] = (double) outgoingAPs / (runTable.size() / 2);
            oimap[1] = (double) incomingAPs / (runTable.size() / 2);
        }
        return oimap;
    }

    private static double[] getOutInRPrecs(Hashtable resultTable, Hashtable runTable) {
        double[] oirprecs = new double[2];
        double outgoingRPrecs = 0.0;
        double incomingRPrecs = 0.0;

        // Loop all submitted run Topics
        for (Enumeration e = runTable.keys(); e.hasMoreElements();) {
            double mCount = 0.0;
            String key = e.nextElement().toString();
            // get RUN (incoming or outgoing) links array for each Topic
            String[] runValues = (String[]) runTable.get(key);
            // get Result (incoming or outgoing) links array for each Topic
            String[] resultSet = (String[]) resultTable.get(key);

            //R-Precision: ONLY Caculate Precision value at the resultSet.length position
            double APinR = 0.0;
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
                // Get non-duplicate items =====================================
                List rpItemsList = new ArrayList();
                for (int i = 0; i < runValues.length; i++) {
                    String item = runValues[i];
                    if (!rpItemsList.contains(item)) {
                        rpItemsList.add(item);
                    }
                }
                String[] rpRunItems = (String[]) rpItemsList.toArray(new String[rpItemsList.size()]);
                // =============================================================
                int k = 0;
                for (int i = 0; i < rpRunItems.length; i++) {
                    k = k + 1;
                    // To Count the Retrieved Relevant links
                    String link = rpRunItems[i];
                    boolean isMatched = false;
                    for (int j = 0; j < resultSet.length; j++) {
                        if (link.equalsIgnoreCase(resultSet[j].trim())) {
                            isMatched = true;
                        }
                    }
                    if (isMatched) {
                        mCount = mCount + 1;
//                        APinR += (double) mCount / (k);
                    }
                    // In R_Precision, R is the number of relevant document for a given query
                    // Here, R is resultSet.length
                    if ((resultSet.length <= rpRunItems.length) && (i + 1) == resultSet.length) {
                        if (key.endsWith(outgoingTag)) {
                            if (useRestrictedNum) {
                                int rsNumber = Math.min(limitedOutLinks, resultSet.length);
                                outgoingRPrecs += (double) mCount / (rsNumber);
                            } else {
                                outgoingRPrecs += (double) mCount / (resultSet.length);
                            }
                        } else if (key.endsWith(incomingTag)) {
                            if (useRestrictedNum) {
                                int rsNumber = Math.min(limitedInLinks, resultSet.length);
                                incomingRPrecs += (double) mCount / (rsNumber);
                            } else {
                                incomingRPrecs += (double) mCount / (resultSet.length);
                            }
                        }
                    } else if ((resultSet.length > rpRunItems.length) && (i + 1) == rpRunItems.length) {
                        if (key.endsWith(outgoingTag)) {
                            if (useRestrictedNum) {
                                int rsNumber = Math.min(limitedOutLinks, resultSet.length);
                                outgoingRPrecs += (double) mCount / (rsNumber);
                            } else {
                                outgoingRPrecs += (double) mCount / (resultSet.length);
                            }
                        } else if (key.endsWith(incomingTag)) {
                            if (useRestrictedNum) {
                                int rsNumber = Math.min(limitedInLinks, resultSet.length);
                                incomingRPrecs += (double) mCount / (rsNumber);
                            } else {
                                incomingRPrecs += (double) mCount / (resultSet.length);
                            }
                        }
                    }
                }   // End of Loop links in a Topic
            }
            // =================================================================
        }   // End of Loop all Topics
        if (isUseAllTopics) {
            oirprecs[0] = (double) outgoingRPrecs / (resultTable.size() / 2);
            oirprecs[1] = (double) incomingRPrecs / (resultTable.size() / 2);
        } else {
            oirprecs[0] = (double) outgoingRPrecs / (runTable.size() / 2);
            oirprecs[1] = (double) incomingRPrecs / (runTable.size() / 2);
        }
        return oirprecs;
    }

    private static double[][] getOutInPrecsAT(Hashtable resultTable, Hashtable runTable) {
        // Declare [2]: as Outgoing & Incoming
        // Declare [2]: as AT levels: 5, 10, 20, 30, 50, 250
        // Precision@: ONLY Caculate Precision value at the @5, 10, 20, 30, 50, 250 values
        int recallDegree = 6;
        //        int[] pAtValue = {10, 30, 50, 100, 200};
        int[] pAtValue = {5, 10, 20, 30, 50, 250};
        double[][] oiprecsat = new double[2][recallDegree];
        double[] outgoingPrecsAt = new double[recallDegree];
        double[] incomingPrecsAt = new double[recallDegree];

        for (Enumeration e = runTable.keys(); e.hasMoreElements();) {
            double mCount = 0.;
            double anchorScore = 0.0;
            String key = e.nextElement().toString();
            // get RUN (incoming or outgoing) links array for each Topic
            String[] runValues = (String[]) runTable.get(key);
            // get Result (incoming or outgoing) links array for each Topic
            String[] resultSet = (String[]) resultTable.get(key);

            if (runValues.length == 1 && runValues[0].equalsIgnoreCase("")) {
                if (resultSet.length == 1 && resultSet[0].equalsIgnoreCase("")) {
                    if (key.endsWith(outgoingTag)) {
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
                // Only get nonDuplicate run Items to calculate
                List itemsList = new ArrayList();
                for (int i = 0; i <
                        runValues.length; i++) {
                    String item = runValues[i];
                    if (!itemsList.contains(item)) {
                        itemsList.add(item);
                    }
                }
                String[] runItems = (String[]) itemsList.toArray(new String[itemsList.size()]);
                // =============================================================
                double APinR = 0.0;
                double k = 0;
                for (int i = 0; i < runItems.length; i++) {
                    k = k + 1;
                    // To Count the Retrieved Relevant links
                    String link = runItems[i];
                    boolean isMatched = false;
                    for (int j = 0; j < resultSet.length; j++) {
                        if (link.equalsIgnoreCase(resultSet[j].trim())) {
                            isMatched = true;
                        }
                    }
                    if (isMatched) {
                        mCount++;
//                        APinR += (double) mCount / (k);
                    }
                    // Loop each Topic for Incoming & Outgoing
                    // ONLY Calculate Precision @5, 10, 20, 30, 50, 250
                    if (runItems.length <= pAtValue[0]) {
                        if ((i + 1) == runItems.length) {
                            if (key.endsWith(outgoingTag)) {
                                outgoingPrecsAt[0] += (double) mCount / pAtValue[0];
                                outgoingPrecsAt[1] += (double) mCount / pAtValue[1];
                                outgoingPrecsAt[2] += (double) mCount / pAtValue[2];
                                outgoingPrecsAt[3] += (double) mCount / pAtValue[3];
                                outgoingPrecsAt[4] += (double) mCount / pAtValue[4];
                                outgoingPrecsAt[5] += (double) mCount / pAtValue[5];
                            } else if (key.endsWith(incomingTag)) {
                                incomingPrecsAt[0] += (double) mCount / pAtValue[0];
                                incomingPrecsAt[1] += (double) mCount / pAtValue[1];
                                incomingPrecsAt[2] += (double) mCount / pAtValue[2];
                                incomingPrecsAt[3] += (double) mCount / pAtValue[3];
                                incomingPrecsAt[4] += (double) mCount / pAtValue[4];
                                incomingPrecsAt[5] += (double) mCount / pAtValue[5];
                            }

                        }
                    } else if (runItems.length <= pAtValue[1]) {
                        if ((i + 1) == pAtValue[0]) {
                            if (key.endsWith(outgoingTag)) {
                                outgoingPrecsAt[0] += (double) mCount / (i + 1);
                            } else if (key.endsWith(incomingTag)) {
                                incomingPrecsAt[0] += (double) mCount / (i + 1);
                            }

                        } else {
                            if ((i + 1) == runItems.length) {
                                if (key.endsWith(outgoingTag)) {
                                    outgoingPrecsAt[1] += (double) mCount / pAtValue[1];
                                    outgoingPrecsAt[2] += (double) mCount / pAtValue[2];
                                    outgoingPrecsAt[3] += (double) mCount / pAtValue[3];
                                    outgoingPrecsAt[4] += (double) mCount / pAtValue[4];
                                    outgoingPrecsAt[5] += (double) mCount / pAtValue[5];
                                } else if (key.endsWith(incomingTag)) {
                                    incomingPrecsAt[1] += (double) mCount / pAtValue[1];
                                    incomingPrecsAt[2] += (double) mCount / pAtValue[2];
                                    incomingPrecsAt[3] += (double) mCount / pAtValue[3];
                                    incomingPrecsAt[4] += (double) mCount / pAtValue[4];
                                    incomingPrecsAt[5] += (double) mCount / pAtValue[5];
                                }
                            }
                        }
                    } else if (runItems.length <= pAtValue[2]) {
                        if ((i + 1) == pAtValue[0]) {
                            if (key.endsWith(outgoingTag)) {
                                outgoingPrecsAt[0] += (double) mCount / (i + 1);
                            } else if (key.endsWith(incomingTag)) {
                                incomingPrecsAt[0] += (double) mCount / (i + 1);
                            }
                        } else if ((i + 1) == pAtValue[1]) {
                            if (key.endsWith(outgoingTag)) {
                                outgoingPrecsAt[1] += (double) mCount / (i + 1);
                            } else if (key.endsWith(incomingTag)) {
                                incomingPrecsAt[1] += (double) mCount / (i + 1);
                            }
                        } else if ((i + 1) == runItems.length) {
                            if (key.endsWith(outgoingTag)) {
                                outgoingPrecsAt[2] += (double) mCount / pAtValue[2];
                                outgoingPrecsAt[3] += (double) mCount / pAtValue[3];
                                outgoingPrecsAt[4] += (double) mCount / pAtValue[4];
                                outgoingPrecsAt[5] += (double) mCount / pAtValue[5];
                            } else if (key.endsWith(incomingTag)) {
                                incomingPrecsAt[2] += (double) mCount / pAtValue[2];
                                incomingPrecsAt[3] += (double) mCount / pAtValue[3];
                                incomingPrecsAt[4] += (double) mCount / pAtValue[4];
                                incomingPrecsAt[5] += (double) mCount / pAtValue[5];
                            }
                        }
                    } else if (runItems.length <= pAtValue[3]) {
                        if ((i + 1) == pAtValue[0]) {
                            if (key.endsWith(outgoingTag)) {
                                outgoingPrecsAt[0] += (double) mCount / (i + 1);
                            } else if (key.endsWith(incomingTag)) {
                                incomingPrecsAt[0] += (double) mCount / (i + 1);
                            }
                        } else if ((i + 1) == pAtValue[1]) {
                            if (key.endsWith(outgoingTag)) {
                                outgoingPrecsAt[1] += (double) mCount / (i + 1);
                            } else if (key.endsWith(incomingTag)) {
                                incomingPrecsAt[1] += (double) mCount / (i + 1);
                            }
                        } else if ((i + 1) == pAtValue[2]) {
                            if (key.endsWith(outgoingTag)) {
                                outgoingPrecsAt[2] += (double) mCount / (i + 1);
                            } else if (key.endsWith(incomingTag)) {
                                incomingPrecsAt[2] += (double) mCount / (i + 1);
                            }
                        } else if ((i + 1) == runItems.length) {
                            if (key.endsWith(outgoingTag)) {
                                outgoingPrecsAt[3] += (double) mCount / pAtValue[3];
                                outgoingPrecsAt[4] += (double) mCount / pAtValue[4];
                                outgoingPrecsAt[5] += (double) mCount / pAtValue[5];
                            } else if (key.endsWith(incomingTag)) {
                                incomingPrecsAt[3] += (double) mCount / pAtValue[3];
                                incomingPrecsAt[4] += (double) mCount / pAtValue[4];
                                incomingPrecsAt[5] += (double) mCount / pAtValue[5];
                            }
                        }
                    } else if (runItems.length <= pAtValue[4]) {
                        if ((i + 1) == pAtValue[0]) {
                            if (key.endsWith(outgoingTag)) {
                                outgoingPrecsAt[0] += (double) mCount / (i + 1);
                            } else if (key.endsWith(incomingTag)) {
                                incomingPrecsAt[0] += (double) mCount / (i + 1);
                            }

                        } else if ((i + 1) == pAtValue[1]) {
                            if (key.endsWith(outgoingTag)) {
                                outgoingPrecsAt[1] += (double) mCount / (i + 1);
                            } else if (key.endsWith(incomingTag)) {
                                incomingPrecsAt[1] += (double) mCount / (i + 1);
                            }

                        } else if ((i + 1) == pAtValue[2]) {
                            if (key.endsWith(outgoingTag)) {
                                outgoingPrecsAt[2] += (double) mCount / (i + 1);
                            } else if (key.endsWith(incomingTag)) {
                                incomingPrecsAt[2] += (double) mCount / (i + 1);
                            }

                        } else if ((i + 1) == pAtValue[3]) {
                            if (key.endsWith(outgoingTag)) {
                                outgoingPrecsAt[3] += (double) mCount / (i + 1);
                            } else if (key.endsWith(incomingTag)) {
                                incomingPrecsAt[3] += (double) mCount / (i + 1);
                            }

                        } else if ((i + 1) == runItems.length) {
                            if (key.endsWith(outgoingTag)) {
                                outgoingPrecsAt[4] += (double) mCount / pAtValue[4];
                                outgoingPrecsAt[5] += (double) mCount / pAtValue[5];
                            } else if (key.endsWith(incomingTag)) {
                                incomingPrecsAt[4] += (double) mCount / pAtValue[4];
                                incomingPrecsAt[5] += (double) mCount / pAtValue[5];
                            }

                        }
                    } else {
                        if ((i + 1) == pAtValue[0]) {
                            if (key.endsWith(outgoingTag)) {
                                outgoingPrecsAt[0] += (double) mCount / (i + 1);
                            } else if (key.endsWith(incomingTag)) {
                                incomingPrecsAt[0] += (double) mCount / (i + 1);
                            }

                        } else if ((i + 1) == pAtValue[1]) {
                            if (key.endsWith(outgoingTag)) {
                                outgoingPrecsAt[1] += (double) mCount / (i + 1);
                            } else if (key.endsWith(incomingTag)) {
                                incomingPrecsAt[1] += (double) mCount / (i + 1);
                            }

                        } else if ((i + 1) == pAtValue[2]) {
                            if (key.endsWith(outgoingTag)) {
                                outgoingPrecsAt[2] += (double) mCount / (i + 1);
                            } else if (key.endsWith(incomingTag)) {
                                incomingPrecsAt[2] += (double) mCount / (i + 1);
                            }

                        } else if ((i + 1) == pAtValue[3]) {
                            if (key.endsWith(outgoingTag)) {
                                outgoingPrecsAt[3] += (double) mCount / (i + 1);
                            } else if (key.endsWith(incomingTag)) {
                                incomingPrecsAt[3] += (double) mCount / (i + 1);
                            }

                        } else if ((i + 1) == pAtValue[4]) {
                            if (key.endsWith(outgoingTag)) {
                                outgoingPrecsAt[4] += (double) mCount / pAtValue[4];
                            } else if (key.endsWith(incomingTag)) {
                                incomingPrecsAt[4] += (double) mCount / pAtValue[4];
                            }

                        } else if ((i + 1) == runItems.length && (i + 1) < pAtValue[5]) {
                            if (key.endsWith(outgoingTag)) {
                                outgoingPrecsAt[5] += (double) mCount / pAtValue[5];
                            } else if (key.endsWith(incomingTag)) {
                                incomingPrecsAt[5] += (double) mCount / pAtValue[5];
                            }

                        } else if ((i + 1) == pAtValue[5]) {
                            if (key.endsWith(outgoingTag)) {
                                outgoingPrecsAt[5] += (double) mCount / pAtValue[5];
                            } else if (key.endsWith(incomingTag)) {
                                incomingPrecsAt[5] += (double) mCount / pAtValue[5];
                            }
                        }
                    }
                }
            }
        }

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

        return oiprecsat;
    }
}



