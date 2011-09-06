package crosslink.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import crosslink.XML2TXT;
import crosslink.measures.Data;
import crosslink.rungenerator.InexSubmission;

import ltwassessment.AppResource;
import ltwassessment.submission.Topic;
import ltwassessment.utility.WildcardFiles;

public class UniqLinks {
	
	public final static int TASK_F2F = 1;
	public final static int TASK_A2F = 2;
	
	private static final int defaultMaxAnchorsPerTopic = 250;
	private static final int defaultMaxBepsPerAnchor = 5;
	
	private String currentSourceLang;
	private String currentTargetLang;
	private boolean convertToTextOffset = true;
	private String runId;
	
	private int task;
	
	public class Team {
		String 			teamName;
		HashMap<String, Set<String>> topicLinks = new HashMap<String, Set<String>>();  //only keep the links that is in the result set
		HashMap<String, Set<String>> topicAnchors = new HashMap<String, Set<String>>();  //only keep the links that is in the result set
		Set<String>		linkSet;
	}
	
	private File[] runFileCache;
	
	private ArrayList<Team> teams = new ArrayList<Team>();
	
	public UniqLinks(int index, String[] fl) {
		this.init(index, fl);
	}
	
	public int getTask() {
		return task;
	}

	public void setTask(int task) {
		this.task = task;
	}

	private void init(int index, String[] fl) {
        ArrayList<File> fileList = new ArrayList<File>();
        for (int l = index; l < fl.length; l++) {
            String afile = fl[l].trim();
            if (afile.length() > 0) {
            	File aFile = new File(afile);
            	if (aFile.isDirectory())
            		fileList.addAll(WildcardFiles.listFilesInStack(afile));
            	else
            		fileList.add(aFile);  	
            }
        }
        File[] files = fileList.toArray(new File[fileList.size()]);
        this.runFileCache = files;
	}

	public static void usage() {
		System.out.println("Usage: program [options] result_file run_file1 run_file2 ...");
		System.out.println("options: ");
		System.out.println("         -t f2f|a2f");
		System.exit(-1);
	}
	
    protected void loadRunFile(File runfiles, int lang) throws Exception {

//        Hashtable f2bRunTableByGroup = null;
        try {
            JAXBContext jc;
            jc = JAXBContext.newInstance("crosslink.rungenerator");
            Unmarshaller um = jc.createUnmarshaller();
            InexSubmission is = (InexSubmission) ((um.unmarshal(runfiles)));

            byte[] bytes = null;
            
            currentSourceLang = is.getSourceLang();
            if (currentSourceLang == null || currentSourceLang.length() == 0)
            	currentSourceLang = "en";
            
            // default lang is the target lang
            currentTargetLang = is.getDefaultLang();
            if (currentTargetLang == null || currentTargetLang.length() == 0)
            	throw new Exception(String.format("Incorrect run file - %s which dosen't provide the target language", runfiles.getAbsoluteFile()));

            if ((Data.langMatchMap.get(currentTargetLang) & lang) > 0) {
//	            f2bRunTableByGroup = new Hashtable();
	            runId = is.getRunId();
	            
	            Team team = new Team();
	            // Loop Different Topics
	            for (int i = 0; i < is.getTopic().size(); i++) {
	
	                int endP = is.getTopic().get(i).getFile().toLowerCase().indexOf(".xml");
	                String topicID;
	                if (endP > -1)
	                	topicID = is.getTopic().get(i).getFile().substring(0, endP);
	                else
	                	topicID = is.getTopic().get(i).getFile();
	                
	                Topic topic = null;
	                if (topic == null) {
	                	String thisTopicName = is.getTopic().get(i).getName();
						topic = new Topic(topicID, thisTopicName);
	                }
	                if (convertToTextOffset) {
	                    bytes = topic.getBytes();
	                }
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
	                        int aOffset = 0;
	                        int aLength = 0;
	
	//                        aFile = is.getTopic().get(i).getOutgoing().getAnchor().get(j).getAnchor().getFile();
	                        try {
		                        aOffset = Integer.parseInt(is.getTopic().get(i).getOutgoing().getAnchor().get(j).getOffset());
		//                        aOffset = String.valueOf(Math.floor(1000000000 * Math.random()));
		                        if (is.getTopic().get(i).getOutgoing().getAnchor().get(j) == null) {
		                            aLength = 10; //kludge - when there is no anchor in the submission, just F2F
		                        } else {
		                            aLength = Integer.parseInt(is.getTopic().get(i).getOutgoing().getAnchor().get(j).getLength());
		                        }
		                        
		                        if (convertToTextOffset) {
		                        	aLength = XML2TXT.textLength(bytes, aOffset, aLength);
		                        	aOffset = XML2TXT.byteOffsetToTextOffset(bytes, aOffset);
		                        }
	                        }
	                        catch (Exception ex) {
	                        	ex.printStackTrace();
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
	                        if (aLength  > 0) {
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
//		                            if (!outF2GroupBepV.contains(aOffset + "_" + aLength + "_" + toFile + "_" + toBep)) {
//		                                outF2GroupBepV.add(aOffset + "_" + aLength + "_" + toFile + "_" + toBep);
//		                            }
		                            if (!team.linkSet.contains(toFile))
		                            	team.linkSet.add(toFile);
		                        }
	                        }
	                        else {
	                        	System.err.println("Error: empty anchor");
	                        }
	                    }
	                    // ---------------------------------------------------------
	                    // transfer Vector data into String Array for returning
//	                    if (outF2GroupBepV.size() >= 1) {
//	                        outLinks = new String[outF2GroupBepV.size()];
//	                        int olCounter = 0;
//	                        Enumeration olEnu = outF2GroupBepV.elements();
//	                        while (olEnu.hasMoreElements()) {
//	                            Object olObj = olEnu.nextElement();
//	                            outLinks[olCounter] = olObj.toString().trim();
//	                            olCounter++;
//	                        }
//	                        
////	                        f2bRunTableByGroup.put(topicID + "_" + outgoingTag, outLinks);
//	                    } 
//	                    else {
//	                        outLinks = new String[1];
//	                        outLinks[0] = "";
//	                    }
	                    // ---------------------------------------------------------
//	                    outF2GroupBepV.clear();
	
	                } // get topic
	                
//	                    else {
//	                    outLinks = new String[1];
//	                    outLinks[0] = "";
//	                }
	                teams.add(team);
	
	            }
            }
        } catch (JAXBException ex) {
            ex.printStackTrace();
        }
    }

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int runfileStart = 2;
		UniqLinks uniqLinks = new UniqLinks(runfileStart, args);

		int param_start = 0;
		
		for (int i = 0; i < args.length; ++i) {
			if (args[i].charAt(0) == '-') {
				if (args[i].charAt(1) == 't' ) {
					String task = args[++i];
					if (task.equalsIgnoreCase("a2f"))
						uniqLinks.setTask(TASK_A2F);
					else
						uniqLinks.setTask(TASK_F2F);
				}
//				else if (args[i].charAt(1) == 'p' ) {
//
//				}
//				else if (args[i].charAt(1) == 'c' ) {
//					manager.setValidateAnchors(true);
//					++param_start;
//				}
//				else if (args[i].charAt(1) == 'l' ) {
//					langPair = args[++i];
//					param_start += 2;
//				}
				else
					usage();
			}
			else
				break;
		}
		if (param_start >= args.length)
			usage();
	}

}
