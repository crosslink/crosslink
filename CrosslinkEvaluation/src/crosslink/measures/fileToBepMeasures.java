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
public final class fileToBepMeasures {

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

    private static void log(Object aObject) {
        System.out.println(String.valueOf(aObject));
    }

    private static void errlog(Object aObject) {
        System.err.println(String.valueOf(aObject));
    }

    public fileToBepMeasures() {
    }

    public static metricsCalculation.EvaluationResult getFileToBepResult(File resultfiles, File runfiles, boolean isAllTopics, boolean useFileToBep, boolean useAnchorToFile, boolean useAnchorToBEP) {

        isUseAllTopics = isAllTopics ? true : false;
        isFileToBEP = useFileToBep ? true : false;
        isAnchorGToFile = useAnchorToFile ? true : false;
        isAnchorGToBEP = useAnchorToBEP ? true : false;

        metricsCalculation.EvaluationResult result = new metricsCalculation.EvaluationResult();
        Hashtable resultTable = null;
        Hashtable runTable = null;

        if (isFileToBEP) {
            // the performance is measured by each File-to-BEP
            // so the result is calculated by each File-to-BEP in Run against the ONE in ResultSet
            resultTable = fileToBepMeasures.getF2BResultSet(resultfiles);
            runTable = fileToBepMeasures.getF2BRunSet(runfiles);
        } else if (isAnchorGToFile || isAnchorGToBEP) {
            resultTable = fileToBepMeasures.getF2BResultSetByGroup(resultfiles);
            runTable = fileToBepMeasures.getF2BRunSetByGroup(runfiles);
        }

        // =====================================================================
        // 1) result: get run ID
        result.runId = fileToBepMeasures.runStatic;

        String tempRunID = result.runId;
        // =====================================================================
        // Get MAP
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
        double[] oiRPrecs = getRPrecs(resultTable, runTable);
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
        double[][] oiPrecsAT = getPrecsAT(resultTable, runTable);
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

        return result;
    }

    // =========================================================================
    // -------------------------------------------------------------------------
    // Hashtable: topicID_Outgoing_Links, Vector(bepFileID_bepOffset, ...)
    // notes01: get All Data from "ResultSet.xml"
    // notes02: Incoming links should be further modified
    private static Hashtable getF2BResultSet(File resultfiles) {
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
                                f2bResultTable.put(topicID + "_" + fileToBepMeasures.outgoingTag, outLinkArray);
                            } else {
                                String[] outLinkArray = new String[outLinkV.size()];
                                int oCounter = 0;
                                Enumeration outEnu = outLinkV.elements();
                                while (outEnu.hasMoreElements()) {
                                    Object outObj = outEnu.nextElement();
                                    outLinkArray[oCounter] = outObj.toString().trim();
                                    oCounter++;
                                }
                                f2bResultTable.put(topicID + "_" + fileToBepMeasures.outgoingTag, outLinkArray);
                            }
                            break;
                        } else if (isIncomingLink(endLocalName)) {
                            isInIncoming = false;
                            // =================================================
                            // Currently there is NO Incoming Links in Result Set
                            // This part should be further modified in the future
                            // =================================================
                            String[] inLinkArray = {""};
                            f2bResultTable.put(topicID + "_" + fileToBepMeasures.incomingTag, inLinkArray);
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
        return f2bResultTable;
    }

    // -------------------------------------------------------------------------
    // Hashtable: topicID_Outgoing_Links, Vector(anchorOffset_anchorLength_bepFileID_bepOffset, ...)
    // notes01: get All Data from "ResultSet.xml"
    // notes02: Incoming links should be further modified
    private static Hashtable getF2BResultSetByGroup(File resultfiles) {
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
                                f2bResultTableByGroup.put(topicID + "_" + fileToBepMeasures.outgoingTag, outLinkArray);
                            } else {
                                String[] outLinkArray = new String[outLinkV.size()];
                                int oCounter = 0;
                                Enumeration outEnu = outLinkV.elements();
                                while (outEnu.hasMoreElements()) {
                                    Object outObj = outEnu.nextElement();
                                    outLinkArray[oCounter] = outObj.toString().trim();
                                    oCounter++;
                                }
                                f2bResultTableByGroup.put(topicID + "_" + fileToBepMeasures.outgoingTag, outLinkArray);
                            }
                            break;
                        } else if (isIncomingLink(endLocalName)) {
                            isInIncoming = false;
                            // =================================================
                            // Currently there is NO Incoming Links in Result Set
                            // This part should be further modified in the future
                            // =================================================
                            String[] inLinkArray = {""};
                            f2bResultTableByGroup.put(topicID + "_" + fileToBepMeasures.incomingTag, inLinkArray);
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
    private static Hashtable getF2BRunSet(File runfiles) {
        Hashtable f2bRunTable = new Hashtable();

        try {

            JAXBContext jc;
            jc = JAXBContext.newInstance("crosslink.rungenerator");
            Unmarshaller um = jc.createUnmarshaller();
            InexSubmission is = (InexSubmission) ((um.unmarshal(runfiles)));

            runStatic = is.getRunId();
            for (int i = 0; i < is.getTopic().size(); i++) {

                int endP = is.getTopic().get(i).getFile().toLowerCase().indexOf(".xml");
                String topicID = is.getTopic().get(i).getFile().substring(0, endP);
                // -------------------------------------------------------------
                // populate Outgoing Link Data from the submission Run
                String[] outLinks = null;
                if (!is.getTopic().get(i).getOutgoing().getLink().isEmpty()) {

                    Vector outF2BV = new Vector();
                    for (int j = 0; j < is.getTopic().get(i).getOutgoing().getLink().size(); j++) {

                        String toFile = "";
                        String toFileID = "";
                        String toBep = "";
                        List<crosslink.rungenerator.ToFileType> linkTo = is.getTopic().get(i).getOutgoing().getLink().get(j).getLinkto();
                        for (int k = 0; k < linkTo.size(); k++) {
                            toFile = linkTo.get(k).getFile().toString().trim();
                            if (!toFile.equals("")) {
                                int endop = toFile.toLowerCase().indexOf(".xml");
                                if (endop != -1) {
                                    toFileID = toFile.substring(0, endop);
                                }
                            }
                            if (linkTo.get(k).getBep() != null) {
                                toBep = linkTo.get(k).getBep().toString().trim();
                            } else {
                                toBep = "0";
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
                String[] inLinks = new String[is.getTopic().get(i).getIncoming().getLink().size()];
                if (!is.getTopic().get(i).getIncoming().getLink().isEmpty()) {
                    for (int k = 0; k < is.getTopic().get(i).getIncoming().getLink().size(); k++) {
                        String fromFile = is.getTopic().get(i).getIncoming().getLink().get(k).getAnchor().getFile().toString().trim();
                        if (!fromFile.equals("")) {
                            int endip = fromFile.toLowerCase().indexOf(".xml");
                            if (endip != -1) {
                                inLinks[k] = fromFile.substring(0, endip);
                            }
                        }
                    }
                    if (inLinks[0] == null) {
                        inLinks = new String[1];
                        inLinks[0] = "";
                    }
                } else {
                    inLinks = new String[1];
                    inLinks[0] = "";
                }
                f2bRunTable.put(topicID + "_" + incomingTag, inLinks);
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
    private static Hashtable getF2BRunSetByGroup(File runfiles) {

        Hashtable f2bRunTableByGroup = new Hashtable();
        try {
            JAXBContext jc;
            jc = JAXBContext.newInstance("crosslink.rungenerator");
            Unmarshaller um = jc.createUnmarshaller();
            InexSubmission is = (InexSubmission) ((um.unmarshal(runfiles)));

            runStatic = is.getRunId();
            // Loop Different Topics
            for (int i = 0; i < is.getTopic().size(); i++) {

                int endP = is.getTopic().get(i).getFile().toLowerCase().indexOf(".xml");
                String topicID = is.getTopic().get(i).getFile().substring(0, endP);
                // -------------------------------------------------------------
                // Inside Outgoing Links
                String[] outLinks = null;
                if (!is.getTopic().get(i).getOutgoing().getLink().isEmpty()) {

                    Vector outF2GroupBepV = new Vector();
                    // Loop Each Group by Anchor
                    // BUT the same anchor may distribute in different groups
                    int maxAnchors = defaultMaxAnchorsPerTopic;
                    if (maxAnchors == 0) {
                        maxAnchors = is.getTopic().get(i).getOutgoing().getLink().size();
                    } else if (maxAnchors >= is.getTopic().get(i).getOutgoing().getLink().size()) {
                        maxAnchors = is.getTopic().get(i).getOutgoing().getLink().size();
                    }
                    for (int j = 0; j < maxAnchors; j++) {

                        // to get AnchorInfo: File, Offset & Length ------------
                        String aFile = "";
                        String aOffset = "";
                        String aLength = "";

//                        aFile = is.getTopic().get(i).getOutgoing().getLink().get(j).getAnchor().getFile();
                        aOffset = is.getTopic().get(i).getOutgoing().getLink().get(j).getAnchor().getOffset();
//                        aOffset = String.valueOf(Math.floor(1000000000 * Math.random()));
                        if (is.getTopic().get(i).getOutgoing().getLink().get(j).getAnchor() == null) {
                            aLength = "10"; //kludge - when there is no anchor in the submission, just F2F
                        } else if (is.getTopic().get(i).getOutgoing().getLink().get(j).getAnchor().getLength() == null) {
                            aLength = "12";
                        } else {
                            aLength = is.getTopic().get(i).getOutgoing().getLink().get(j).getAnchor().getLength();
                        }
                        // -----------------------------------------------------
                        String toFile = "";
                        String toFileID = "";
                        String toBep = "";
                        List<crosslink.rungenerator.ToFileType> linkTo = is.getTopic().get(i).getOutgoing().getLink().get(j).getLinkto();
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
                            if (linkTo.get(k).getBep() != null) {
                                toBep = linkTo.get(k).getBep().toString().trim();
                            } else {
                                toBep = "0";
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
                String[] inLinks = new String[is.getTopic().get(i).getIncoming().getLink().size()];
                if (!is.getTopic().get(i).getIncoming().getLink().isEmpty()) {
                    for (int k = 0; k < is.getTopic().get(i).getIncoming().getLink().size(); k++) {
                        String fromFile = is.getTopic().get(i).getIncoming().getLink().get(k).getAnchor().getFile().toString().trim();
                        if (!fromFile.equals("")) {
                            int endip = fromFile.toLowerCase().indexOf(".xml");
                            if (endip != -1) {
                                inLinks[k] = fromFile.substring(0, endip);
                            }
                        }
                    }
                    if (inLinks[0] == null) {
                        inLinks = new String[1];
                        inLinks[0] = "";
                    }
                } else {
                    inLinks = new String[1];
                    inLinks[0] = "";
                }
                f2bRunTableByGroup.put(topicID + "_" + incomingTag, inLinks);
                // =================================================================
            }

        } catch (JAXBException ex) {
            ex.printStackTrace();
        }

        return f2bRunTableByGroup;
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

        // =============================================================
        Hashtable outgoingHt = new Hashtable();
        Hashtable incomingHt = new Hashtable();
        // =====================================================================
        // Loop all Topics (topicID_Outgoing_Links) in the submitted run
        for (Enumeration e = runTable.keys(); e.hasMoreElements();) {
            int ffmCount = 0;
            double anchorScore = 0.0;
            String key = e.nextElement().toString();
            log("Topic: " + key);
            // get RUN (incoming or outgoing) links array for each Topic
            String[] runValues = (String[]) runTable.get(key);
            // get Result (incoming or outgoing) links array for each Topic
            String[] resultSet = (String[]) resultTable.get(key);
            // =================================================================
            // Run:
            // group BEP links by Anchor
            // ResultSet: 
            // get total number of Anchors for this Topic: key: Outgoing / Incoming
            int rsAnchorNoPerTopic = 0;
            Hashtable<String, Vector> runHT = new Hashtable<String, Vector>();
            Vector runIndexV = new Vector();
            // -----------------------------------------------------------------
            // Loop all run Data (aOffset_aLength_bFileID_bOffset) Vector &
            // re-arrange them into:
            //    runIndexV: that stores All Anchor pairs (aOffset_aLength)
            //    runHT: that stores All BEP pairs for each AnchorIndex (aOffset_aLength, Vector<bFileID_bOffset>)
            //           eliminate All duplicate Data
            if (isAnchorGToFile || isAnchorGToBEP) {
                if (key.endsWith("Outgoing_Links")) {
                    Vector runBepV = new Vector();
                    String currentIndex = "";
                    for (int i = 0; i < runValues.length; i++) {
                        String runLink = runValues[i].trim();
                        String[] linkSet = runLink.split("_");
                        String runAnchorOffset = linkSet[0].trim();
                        String runAnchorLength = linkSet[1].trim();
                        log("runAnchorOffset: " + runAnchorOffset);
                        log("runAnchorLength: " + runAnchorLength);
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
                    // ---------------------------------------------------------
                    // ---------------------------------------------------------
                    // get the total Number of Anchors per Topic in ResultSet
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
                    // ---------------------------------------------------------
                    rsAnchorNoPerTopic = aaptV.size();
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
//                            log("runAnchorSet: " + runAnchorSet.toString());
                            int runAStartPoint = Integer.valueOf(runAnchorOL[0]);
                            int runAEndPoint = Integer.valueOf(runAnchorOL[0]) + Integer.valueOf(runAnchorOL[1]);
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
                                        // run BEP file ID matches RS BEP file ID
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
                                            // run BEP file ID matches RS BEP file ID
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
                                        // End of Anchor & BEP links Check
                                    }
                                    // -----------------------------------------
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
                                        int Denominator = runBepSetV.size();
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
                        // THEN, calculate the Precision for the Topic
                        // Basically, APinR should be divided by "rsAnchorNoPerTopic" <-- number of Anchors in the ResultSet Topic.
                        // BUT this target is File-to-BEP, we don't know Exactly how many Anchors in the ResultSet
                        //     and this Number may be meaningless
                        // Here, we temporarily use "anchorCounter" in Run to replace it
                        // to prevent from producing funny result
//                         aTopicAvePrecision = (double) APinR / rsAnchorNoPerTopic;
//                        aTopicAvePrecision = (double) APinR / anchorCounter;
                        int N = Math.min(50, resultSet.length);
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
                outgoingAPs += aTopicAvePrecision;
            } else if (key.endsWith(incomingTag)) {
                incomingAPs += aTopicAvePrecision;
            }
        }   // End of Looping All Topics

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
        return oimap;
    }

    private static double[] getRPrecs(Hashtable resultTable, Hashtable runTable) {
        double[] oirprecs = new double[2];
        double outgoingRPrecs = 0.0;
        double incomingRPrecs = 0.0;

        // Loop all submitted run Topics
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
            int rsAnchorNoPerTopic = 0;
            Hashtable<String, Vector> runHT = new Hashtable<String, Vector>();
            Vector runIndexV = new Vector();
            if (isAnchorGToFile || isAnchorGToBEP) {
                if (key.endsWith("Outgoing_Links")) {
                    // -------------------------------------------------------------
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
                                    anchorScore += (double) bepScore / bepDenominator;      // score for each Anchor
                                }
                                isMatched = false;
                            }
                        }

                        int N = Math.max(50, resultSet.length);
                        outgoingRPrecs += (double) anchorScore / N;
//                        outgoingRPrecs += (double) anchorScore / Rvalue;

                    } else if (key.endsWith(incomingTag)) {
                        incomingRPrecs += 0;
                    }

                } else {
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
            }

            // =================================================================
        }

        if (isUseAllTopics) {
            oirprecs[0] = (double) outgoingRPrecs / (resultTable.size() / 2);
            oirprecs[1] = (double) incomingRPrecs / (resultTable.size() / 2);
        } else {
            oirprecs[0] = (double) outgoingRPrecs / (runTable.size() / 2);
            oirprecs[1] = (double) incomingRPrecs / (runTable.size() / 2);
        }

        return oirprecs;
    }

    private static double[][] getPrecsAT(Hashtable resultTable, Hashtable runTable) {
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
                    // -------------------------------------------------------------
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
                                        int Denominator = bepSetV.size();
                                        anchorScore += (double) bepScore / Denominator;   // score for each Anchor
                                    }
                                } else {
                                    anchorScore += (double) bepScore / bepDenominator;
                                }
                                isMatched = false;
                            }

                            // Loop each Topic for Incoming & Outgoing
                            // ONLY Calculate Precision @5, 10, 20, 30, 50, 250
                            if (runIndexV.size() <= pAtValue[0]) {
                                if ((i + 1) == runIndexV.size()) {
                                    outgoingPrecsAt[0] += (double) anchorScore / pAtValue[0];
                                    outgoingPrecsAt[1] += (double) anchorScore / pAtValue[1];
                                    outgoingPrecsAt[2] += (double) anchorScore / pAtValue[2];
                                    outgoingPrecsAt[3] += (double) anchorScore / pAtValue[3];
                                    outgoingPrecsAt[4] += (double) anchorScore / pAtValue[4];
                                    outgoingPrecsAt[5] += (double) anchorScore / pAtValue[5];
                                }
                            } else if (runIndexV.size() <= pAtValue[1]) {
                                if ((i + 1) == pAtValue[0]) {
                                    outgoingPrecsAt[0] += (double) anchorScore / (i + 1);
                                } else {
                                    if ((i + 1) == runIndexV.size()) {
                                        outgoingPrecsAt[1] += (double) anchorScore / pAtValue[1];
                                        outgoingPrecsAt[2] += (double) anchorScore / pAtValue[2];
                                        outgoingPrecsAt[3] += (double) anchorScore / pAtValue[3];
                                        outgoingPrecsAt[4] += (double) anchorScore / pAtValue[4];
                                        outgoingPrecsAt[5] += (double) anchorScore / pAtValue[5];
                                    }
                                }
                            } else if (runIndexV.size() <= pAtValue[2]) {
                                if ((i + 1) == pAtValue[0]) {
                                    outgoingPrecsAt[0] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == pAtValue[1]) {
                                    outgoingPrecsAt[1] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == runIndexV.size()) {
                                    outgoingPrecsAt[2] += (double) anchorScore / pAtValue[2];
                                    outgoingPrecsAt[3] += (double) anchorScore / pAtValue[3];
                                    outgoingPrecsAt[4] += (double) anchorScore / pAtValue[4];
                                    outgoingPrecsAt[5] += (double) anchorScore / pAtValue[5];
                                }
                            } else if (runIndexV.size() <= pAtValue[3]) {
                                if ((i + 1) == pAtValue[0]) {
                                    outgoingPrecsAt[0] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == pAtValue[1]) {
                                    outgoingPrecsAt[1] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == pAtValue[2]) {
                                    outgoingPrecsAt[2] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == runIndexV.size()) {
                                    outgoingPrecsAt[3] += (double) anchorScore / pAtValue[3];
                                    outgoingPrecsAt[4] += (double) anchorScore / pAtValue[4];
                                    outgoingPrecsAt[5] += (double) anchorScore / pAtValue[5];
                                }
                            } else if (runIndexV.size() <= pAtValue[4]) {
                                if ((i + 1) == pAtValue[0]) {
                                    outgoingPrecsAt[0] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == pAtValue[1]) {
                                    outgoingPrecsAt[1] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == pAtValue[2]) {
                                    outgoingPrecsAt[2] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == pAtValue[3]) {
                                    outgoingPrecsAt[3] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == runIndexV.size()) {
                                    outgoingPrecsAt[4] += (double) anchorScore / pAtValue[4];
                                    outgoingPrecsAt[5] += (double) anchorScore / pAtValue[5];
                                }
                            } else {
                                if ((i + 1) == pAtValue[0]) {
                                    outgoingPrecsAt[0] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == pAtValue[1]) {
                                    outgoingPrecsAt[1] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == pAtValue[2]) {
                                    outgoingPrecsAt[2] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == pAtValue[3]) {
                                    outgoingPrecsAt[3] += (double) anchorScore / (i + 1);
                                } else if ((i + 1) == pAtValue[4]) {
                                    outgoingPrecsAt[4] += (double) anchorScore / pAtValue[4];
                                } else if ((i + 1) == runIndexV.size() && (i + 1) < pAtValue[5]) {
                                    outgoingPrecsAt[5] += (double) anchorScore / pAtValue[5];
                                } else if ((i + 1) == pAtValue[5]) {
                                    outgoingPrecsAt[5] += (double) anchorScore / pAtValue[5];
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
    // =========================================================================
    // =========================================================================
    // additional Methods
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
