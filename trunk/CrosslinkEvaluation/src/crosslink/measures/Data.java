package crosslink.measures;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import crosslink.resultsetGenerator.LtwResultsetType;
import crosslink.rungenerator.InexSubmission;

public class Data {
    public static String outgoingTag = "Outgoing_Links";
    public static String incomingTag = "Incoming_Links";
    
    public static final int ENGLISH_TO_CJK = 1;
    public static final int CJK_TO_ENGLISH = 2;
    public static final int LINK_CJKE = 3;
    
    public static final int LANGUAGE_CHINESE = 2;
    public static final int LANGUAGE_JAPANESE = 4;
    public static final int LANGUAGE_KOREAN = 8;
    public static final int LANGUAGE_ENGLISH = 16;
    public static final int LANGUAGE_ALL = 30;
    
    public static Map<String, Integer> langMatchMap = new HashMap<String, Integer>();
    
    protected static String runId = "";
    
    protected static String currentSourceLang = null;
    protected static String currentTargetLang = null;
    
    // -------------------------------------------------------------------------
    // define the number of links accepted in Incoming & Outgoing
    protected static boolean useRestrictedNum = false;
    protected static int limitedOutLinks = 50;
    protected static int limitedInLinks = 250;
    
    protected static boolean isUseAllTopics = false;
    protected static boolean isFileToBEP = false;
    protected static boolean isAnchorGToFile = false;
    protected static boolean isAnchorGToBEP = false;
    
    // -------------------------------------------------------------------------
    // fro anchor to bep evaluation using manual assessment result
//    protected static int[] pAtValue = {5, 10, 20, 30, 50, 250};
//    public final static int[] pAtValue = {100, 200, 300, 500, 750, 1250};
    public final static int[] pAtValue = {5, 10, 20, 30, 50, 250};
    // fro anchor to file (file to file) evaluation using Wikipedia ground truth
//    protected static int[] pAtValue_A2F = {5, 10, 20, 30, 50, 100};
    public final static int[] pAtValue_A2F = {5, 10, 20, 30, 50, 250};
    
    private static int[] pAtN = null; 
    
    static {
		langMatchMap.put("zh", LANGUAGE_CHINESE);
		langMatchMap.put("ko", LANGUAGE_KOREAN);
		langMatchMap.put("ja", LANGUAGE_JAPANESE);
		langMatchMap.put("en", LANGUAGE_ENGLISH);
		langMatchMap.put("all", LANGUAGE_ALL);
    }
    
//    protected static void 
    protected static void log(Object aObject) {
        System.out.println(String.valueOf(aObject));
    }
    
    protected static void errlog(Object aObject) {
        System.err.println(String.valueOf(aObject));
    }
    
    protected static Hashtable getResultSetLinks(/*File resultfile*/) {
//    	String resultfile = 
        return ResultSetManager.getInstance().getResultSetLinks(currentSourceLang, currentTargetLang);
//        return resultTable;
    }
    
    protected static double average(ArrayList<TopicScore> table, int dividedBy) {
    	double sum = 0.0;

    	for (TopicScore score : table)
    		System.err.print(score.getTopicId() + ", ");
		System.err.println();
    			
    	for (TopicScore score : table) {
    		System.err.print(score.getScore() + ", ");
    		sum += score.getScore();
    	}
    	System.err.println();
		return sum / (double)dividedBy;
    }

    protected static Hashtable getRunSet(File runfiles, int lang, int linkDirection) throws Exception {
        Hashtable runTable = null;

        try {

            JAXBContext jc;
            jc = JAXBContext.newInstance("crosslink.rungenerator");
            Unmarshaller um = jc.createUnmarshaller();
            InexSubmission is = (InexSubmission) ((um.unmarshal(runfiles)));

            currentSourceLang = is.getSourceLang();
            if (currentSourceLang == null || currentSourceLang.length() == 0)
            	throw new Exception(String.format("Incorrect run file - %s which dosen't provide the source language", runfiles.getAbsoluteFile()));
            
            // default lang is the target lang
            currentTargetLang = is.getDefaultLang();
            if (currentTargetLang == null || currentTargetLang.length() == 0)
            	throw new Exception(String.format("Incorrect run file - %s which dosen't provide the target language", runfiles.getAbsoluteFile()));
            
            int thisRunLinkDirection = currentSourceLang.equalsIgnoreCase("en") ? ENGLISH_TO_CJK : CJK_TO_ENGLISH;

            if ((langMatchMap.get(currentTargetLang) & lang) > 0 && (linkDirection & thisRunLinkDirection) > 0) {
            	runTable = new Hashtable();
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
	//                        log(topicID + "Anchor: " + is.getTopic().get(i).getOutgoing().getAnchor().get(j).getName());
	                        List<crosslink.rungenerator.ToFileType> linkTo = is.getTopic().get(i).getOutgoing().getAnchor().get(j).getTofile();
	                        if (linkTo != null)
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
	//	                                log(topicID + "<-- Topic ID: Duplicated: " + toFile);
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
            }

        } catch (JAXBException ex) {
            ex.printStackTrace();
        }

        return runTable;
    }

	public static int[] getpAtN() {
		return pAtN;
	}

	public static void setpAtN(int[] pAtN) {
		Data.pAtN = pAtN;
	}
}
