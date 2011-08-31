package crosslink.measures;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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

public class Measures extends Data {
    // -------------------------------------------------------------------------
    // 1) to calculate the score of each BEP pointed from the Anchor Topic
    protected static int distanceFactor = 1000;
    // 2) set Up Max Anchors per Topic is 50. 
    //    If maxAnchors = 0 or a number that is greater than the total returned Anchors
    //    the Anchors retrived will be "the total returned Anchors"
    protected static int defaultMaxAnchorsPerTopic = 250;
    // 3) set Up Max BEP links per Anchor is 5. 
    //    If maxBepsPerAnchor = 0 or a number that is greater than the total returned BEPs
    //    the BEPs retrived will be "the total returned BEPs"
    protected static int defaultMaxBepsPerAnchor = 1;
    // 4) to be the denominator to calculate the score of each Anchor
    //    If bepDenominator is set to 0 here, bepDenominator will be the number of the BEPs in the Anchor.
    //    If the BEP number is assumed to be 5, bepDenominator can be 5.
    protected static int bepDenominator = 0;
    // -------------------------------------------------------------------------
    protected static boolean useOnlyAnchorGroup = false;
    
    
    // =========================================================================
    // -------------------------------------------------------------------------
    // Hashtable: topicID_Outgoing_Links, Vector(bepFileID_bepOffset, ...)
    // notes01: get All Data from "ResultSet.xml"
    // notes02: Incoming links should be further modified
    protected static Hashtable getF2BResultSet(File resultfiles) {
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
                                f2bResultTable.put(topicID + "_" + outgoingTag, outLinkArray);
                            } else {
                                String[] outLinkArray = new String[outLinkV.size()];
                                int oCounter = 0;
                                Enumeration outEnu = outLinkV.elements();
                                while (outEnu.hasMoreElements()) {
                                    Object outObj = outEnu.nextElement();
                                    outLinkArray[oCounter] = outObj.toString().trim();
                                    oCounter++;
                                }
                                f2bResultTable.put(topicID + "_" + outgoingTag, outLinkArray);
                            }
                            break;
                        } else if (isIncomingLink(endLocalName)) {
                            isInIncoming = false;
                            // =================================================
                            // Currently there is NO Incoming Links in Result Set
                            // This part should be further modified in the future
                            // =================================================
                            String[] inLinkArray = {""};
                            f2bResultTable.put(topicID + "_" + incomingTag, inLinkArray);
                        }
                        break;
                }
            }
            parser.close();

        } catch (XMLStreamException ex) {
            Logger.getLogger(Measures.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Measures.class.getName()).log(Level.SEVERE, null, ex);
        }
        return f2bResultTable;
    }

    // -------------------------------------------------------------------------
    // Hashtable: topicID_Outgoing_Links, Vector(anchorOffset_anchorLength_bepFileID_bepOffset, ...)
    // notes01: get All Data from "ResultSet.xml"
    // notes02: Incoming links should be further modified
    protected static Hashtable getF2BResultSetByGroup(File resultfiles) {
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
                                f2bResultTableByGroup.put(topicID + "_" + outgoingTag, outLinkArray);
                            } else {
                                String[] outLinkArray = new String[outLinkV.size()];
                                int oCounter = 0;
                                Enumeration outEnu = outLinkV.elements();
                                while (outEnu.hasMoreElements()) {
                                    Object outObj = outEnu.nextElement();
                                    outLinkArray[oCounter] = outObj.toString().trim();
                                    oCounter++;
                                }
                                f2bResultTableByGroup.put(topicID + "_" + outgoingTag, outLinkArray);
                            }
                            break;
                        } else if (isIncomingLink(endLocalName)) {
                            isInIncoming = false;
                            // =================================================
                            // Currently there is NO Incoming Links in Result Set
                            // This part should be further modified in the future
                            // =================================================
                            String[] inLinkArray = {""};
                            f2bResultTableByGroup.put(topicID + "_" + incomingTag, inLinkArray);
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
    protected static Hashtable getF2BRunSet(File runfiles, int lang) throws Exception {
        Hashtable f2bRunTable = null;

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

            if ((langMatchMap.get(currentTargetLang) & lang) > 0) {
            	f2bRunTable = new Hashtable();
	            runId = is.getRunId();
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
    protected static Hashtable getF2BRunSetByGroup(File runfiles, int lang) throws Exception {

        Hashtable f2bRunTableByGroup = null;
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

            if ((langMatchMap.get(currentTargetLang) & lang) > 0) {
	            f2bRunTableByGroup = new Hashtable();
	            runId = is.getRunId();
	            // Loop Different Topics
	            for (int i = 0; i < is.getTopic().size(); i++) {
	
	                int endP = is.getTopic().get(i).getFile().toLowerCase().indexOf(".xml");
	                String topicID;
	                if (endP > -1)
	                	topicID = is.getTopic().get(i).getFile().substring(0, endP);
	                else
	                	topicID = is.getTopic().get(i).getFile();
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
	                        String aLength = "0";
	
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
	                        if (aLength != null && Integer.parseInt(aLength) > 0) {
	                        	for (int k = 0; k < maxBepsPerAnchor; k++) {
		                            toFile = linkTo.get(k).getFile().toString().trim();
		                            if (toFile.equals("")) {
		                                int endop = toFile.toLowerCase().indexOf(".xml");
		                                if (endop != -1) {
		                                    toFile = toFile.substring(0, endop);
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
		                            if (!outF2GroupBepV.contains(aOffset + "_" + aLength + "_" + toFile + "_" + toBep)) {
		                                outF2GroupBepV.add(aOffset + "_" + aLength + "_" + toFile + "_" + toBep);
		                            }
		                        }
	                        }
	                        else {
	                        	System.err.println("Error: empty anchor");
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
	                        
	                        f2bRunTableByGroup.put(topicID + "_" + outgoingTag, outLinks);
	                    } 
//	                    else {
//	                        outLinks = new String[1];
//	                        outLinks[0] = "";
//	                    }
	                    // ---------------------------------------------------------
	                    outF2GroupBepV.clear();
	
	                } 
//	                    else {
//	                    outLinks = new String[1];
//	                    outLinks[0] = "";
//	                }
	                
	                
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
            }
        } catch (JAXBException ex) {
            ex.printStackTrace();
        }

        if (f2bRunTableByGroup != null && f2bRunTableByGroup.size() == 0)
        	f2bRunTableByGroup = null;
        return f2bRunTableByGroup;
    }

    // =========================================================================
    // =========================================================================
    // additional Methods
    static String topicIndicator = "ltw_Topic";

    protected static boolean isTopic(String name) {
        if (name.equals(topicIndicator)) {
            return true;
        }

        return false;
    }
    static String outGoingIndicator = "outgoingLinks";

    protected static boolean isOutgoingLink(String name) {
        if (name.equals(outGoingIndicator)) {
            return true;
        }

        return false;
    }
    static String inComingIndicator = "incomingLinks";

    protected static boolean isIncomingLink(String name) {
        if (name.equals(inComingIndicator)) {
            return true;
        }

        return false;
    }
    static String outLinkIndicator = "outLink";

    protected static boolean isOutLink(String name) {
        if (name.equals(outLinkIndicator)) {
            return true;
        }

        return false;
    }
    // =========================================================================

}
