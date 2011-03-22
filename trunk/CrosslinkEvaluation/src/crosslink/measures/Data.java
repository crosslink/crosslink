package crosslink.measures;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import crosslink.resultsetGenerator.LtwResultsetType;
import crosslink.rungenerator.InexSubmission;

public class Data {
    protected static String outgoingTag = "Outgoing_Links";
    protected static String incomingTag = "Incoming_Links";
    
    protected static String runId = "";
    
//    protected static void 
    protected static void log(Object aObject) {
        System.out.println(String.valueOf(aObject));
    }
    
    protected static Hashtable getResultSet(File resultfiles) {
        
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

                    String[] outLinks = null;
	                String[] emptyLinks = {""};
                    if (lrs.getLtwTopic().get(i).getOutgoingLinks().getOutLink().isEmpty()) {
                        outLinks = emptyLinks;
                    } else {
                        Vector outLinksV = new Vector();
                        for (int j = 0; j < lrs.getLtwTopic().get(i).getOutgoingLinks().getOutLink().size(); j++) {
                            String outLinkStr = lrs.getLtwTopic().get(i).getOutgoingLinks().getOutLink().get(j).getValue().toString().trim();
                            if (!outLinksV.contains(outLinkStr)) {
                                outLinksV.add(outLinkStr);
                            }
                        }
                        outLinks = new String[outLinksV.size()];
                        Enumeration oEnu = outLinksV.elements();
                        while (oEnu.hasMoreElements()) {
                            Object obj = oEnu.nextElement();
                            outLinks[outCount] = obj.toString().trim();
                            outCount++;
                        }
                        resultTable.put(topicID + "_" + outgoingTag, outLinks);
                    }
                    
                    resultTable.put(topicID + "_" + outgoingTag, outLinks);
	                resultTable.put(topicID + "_" + incomingTag, emptyLinks);
//                    if (lrs.getLtwTopic().get(i).getIncomingLinks().getInLink().isEmpty()) {
//                        String[] inLinks = {""};
//                        resultTable.put(topicID + "_" + metricsCalculation.incomingTag, inLinks);
//                    } else {
//                        Vector inLinksV = new Vector();
//                        for (int k = 0; k < lrs.getLtwTopic().get(i).getIncomingLinks().getInLink().size(); k++) {
//                            String inLinkStr = lrs.getLtwTopic().get(i).getIncomingLinks().getInLink().get(k).getValue().toString().trim();
//                            if (!inLinksV.contains(inLinkStr)) {
//                                inLinksV.add(inLinkStr);
//                            }
//                        }
//                        String[] inLinks = new String[inLinksV.size()];
//                        Enumeration iEnu = inLinksV.elements();
//                        while (iEnu.hasMoreElements()) {
//                            Object obj = iEnu.nextElement();
//                            inLinks[inCount] = obj.toString().trim();
//                            inCount++;
//                        }
//                        resultTable.put(topicID + "_" + metricsCalculation.incomingTag, inLinks);
//                    }
                }
            }

        } catch (JAXBException ex) {
            ex.printStackTrace();
        }

        return resultTable;
    }

    protected static Hashtable getRunSet(File runfiles) {
        Hashtable runTable = new Hashtable();

        try {

            JAXBContext jc;
            jc = JAXBContext.newInstance("crosslink.rungenerator");
            Unmarshaller um = jc.createUnmarshaller();
            InexSubmission is = (InexSubmission) ((um.unmarshal(runfiles)));

            runId = is.getRunId();
            for (int i = 0; i < is.getTopic().size(); i++) {

//                int endP = is.getTopic().get(i).getFile().toLowerCase().indexOf(".xml");
                String topicID = is.getTopic().get(i).getFile(); //.substring(0, endP);

                String[] outLinks = null;
                String[] emptyLinks = {""};
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
                        outLinks = emptyLinks;
                    }

                } else {
                    outLinks = new String[1];
                    outLinks[0] = "";
                }
                runTable.put(topicID + "_" + outgoingTag, outLinks);
                runTable.put(topicID + "_" + incomingTag, emptyLinks);

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
}
