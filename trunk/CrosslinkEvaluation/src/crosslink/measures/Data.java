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
    public static String outgoingTag = "Outgoing_Links";
    public static String incomingTag = "Incoming_Links";
    
    protected static String runId = "";
    
    protected static String currentSourceLang = null;
    protected static String currentTargetLang = null;
    
//    protected static void 
    protected static void log(Object aObject) {
        System.out.println(String.valueOf(aObject));
    }
    
    private static void errlog(Object aObject) {
        System.err.println(String.valueOf(aObject));
    }
    
    protected static Hashtable getResultSetLinks(/*File resultfile*/) {
//    	String resultfile = 
        return ResultSetManager.getInstance().getResultSetLinks(currentSourceLang, currentTargetLang);
//        return resultTable;
    }

    protected static Hashtable getRunSet(File runfiles) throws Exception {
        Hashtable runTable = new Hashtable();

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
