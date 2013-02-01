package crosslink.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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

import crosslink.AppResource;
import crosslink.XML2TXT;
import crosslink.measures.Data;
import crosslink.rungenerator.InexSubmission;
import crosslink.submission.Topic;
import crosslink.utility.WildcardFiles;


public class UniqLinks {
	
	public final static int TASK_F2F = 1;
	public final static int TASK_A2F = 2;
	
	private static final int defaultMaxAnchorsPerTopic = 250;
	private static final int defaultMaxBepsPerAnchor = 5;
	
	private String currentSourceLang;
	private String currentTargetLang;
	private boolean convertToTextOffset = true;
	private String runId;
	
	private boolean printOutLinks = false;
	
	ResultSet resultSet = new ResultSet();
	
	private int task;
	
	private File[] runFileCache;
	
	private ArrayList<Team> teams = null; //new ArrayList<Team>();
	private int lang;
	
	public UniqLinks(int index, String[] fl) {
		this.init(index, fl);
	}
	
	public void loadResultSet(String file) {
		resultSet.load(file);
	}
	
	public int getTask() {
		return task;
	}

	public void setTask(int task) {
		this.task = task;
	}

	public void setLang(String langStr) {
		lang = Data.langMatchMap.get(langStr);
	}
	
	public boolean isPrintOutLinks() {
		return printOutLinks;
	}

	public void setPrintOutLinks(boolean printOutLinks) {
		this.printOutLinks = printOutLinks;
	}

	private void init(int index, String[] fl) {
        ArrayList<File> fileList = new ArrayList<File>();
        for (int l = index; l < fl.length; l++) {
            String afile = fl[l].trim();
            if (afile.length() > 0) {
            	File aFile = new File(afile);
            	if (aFile.isDirectory())
					try {
						fileList.addAll(new WildcardFiles().listFilesInStack(afile));
					} catch (Exception e) {
						e.printStackTrace();
					}
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
		System.out.println("	-t f2f|a2f");
		System.out.println("	-l language zh | en | ja | ko");
		System.out.println("	-p print out the links ( there should be a run at a time");
		System.exit(-1);
	}
	
	public void loadRunFiles() {
		for (File file : runFileCache)
			loadRunFile(file, lang);
	}
	
    protected void loadRunFile(File runfiles, int lang) {

//        Hashtable f2bRunTableByGroup = null;
        try {
            JAXBContext jc;
            jc = JAXBContext.newInstance("crosslink.rungenerator");
            Unmarshaller um = jc.createUnmarshaller();
            InexSubmission is = (InexSubmission) ((um.unmarshal(runfiles)));
            String participantId;

            byte[] bytes = null;
            
            currentSourceLang = is.getSourceLang();
            if (currentSourceLang == null || currentSourceLang.length() == 0)
            	currentSourceLang = "en";
            
            AppResource.sourceLang = currentSourceLang;
            
            // default lang is the target lang
            currentTargetLang = is.getDefaultLang();
            if (currentTargetLang == null || currentTargetLang.length() == 0)
            	throw new Exception(String.format("Incorrect run file - %s which dosen't provide the target language", runfiles.getAbsoluteFile()));

            AppResource.targetLang = currentTargetLang;
            
            runId = is.getRunId();
            participantId = is.getParticipantId();
            
            if ((Data.langMatchMap.get(currentTargetLang) & lang) > 0) {
//	            f2bRunTableByGroup = new Hashtable();
	            
//	            Team team = new Team();
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
						topic.load();
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
	                        	aOffset = 0;
	                        	aLength = 0;
//	                        	ex.printStackTrace();
	                        }
	                        // -----------------------------------------------------
	                        String toFile = "";
	                        String toFileID = "";
	                        String toBep = "";
	                        List<crosslink.rungenerator.ToFileType> linkTo = is.getTopic().get(i).getOutgoing().getAnchor().get(j).getTofile();
	                        if (linkTo == null)
	                        	return;
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
//	                            if (!outF2GroupBepV.contains(aOffset + "_" + aLength + "_" + toFile + "_" + toBep)) {
//	                                outF2GroupBepV.add(aOffset + "_" + aLength + "_" + toFile + "_" + toBep);
//	                            }
	                            if (resultSet.containLink(toFile))
	                            	resultSet.addLinkParticipantId(toFile, participantId, runId);
	                            
//	                            if (!team.linkSet.contains(toFile))
//	                            	team.linkSet.add(toFile);
	                        }
	                        if (task == TASK_A2F && aLength  > 0) {
	                        	String anchor = String.format("%s_%s_%s", topicID, aOffset, aLength);
	                        	if (resultSet.containTopicAnchor(anchor))
	                        		resultSet.addAnchorParticipantId(anchor, participantId);
	                        }
//	                        else {
//	                        	System.err.println("Error: empty anchor");
//	                        }
	                    }
	
	                } // get topic
//	                teams.add(team);
	
	            }
            }
            else
            	System.err.println("Run ignored: " + runId);
            	
        } catch (JAXBException ex) {
            ex.printStackTrace();
        }
        catch (Exception ex) {
        	ex.printStackTrace();
        }
    }

    public void anaysis() {
    	teams = resultSet.getTeamLinkCount();
    	Collections.sort(teams);
    	
    	for (Team team : teams) {
    		String line = String.format("%s, %d", team.id, team.uniqLinkCount);
    		System.out.println(line);
    		team.printRunLinks(printOutLinks);
    	}
    	
    	System.out.println();
    }
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int runfileStart = 2;
		UniqLinks uniqLinks = new UniqLinks(runfileStart, args);
		uniqLinks.setLang("zh");
		
		int param_start = 0;
		String resultSetFile;
		
		for (; param_start < args.length; ++param_start) {
			if (args[param_start].charAt(0) == '-') {
				if (args[param_start].charAt(1) == 't' ) {
					String task = args[++param_start];
					if (task.equalsIgnoreCase("a2f"))
						uniqLinks.setTask(TASK_A2F);
					else
						uniqLinks.setTask(TASK_F2F);
				}
				else if (args[param_start].charAt(1) == 'p' ) {
					uniqLinks.setPrintOutLinks(true);
				}
//				else if (args[i].charAt(1) == 'c' ) {
//					manager.setValidateAnchors(true);
//					++param_start;
//				}
				else if (args[param_start].charAt(1) == 'l' ) {
					uniqLinks.setLang(args[++param_start]);
//					param_start;
				}
				else
					usage();
			}
			else
				break;
		}
		if (param_start >= args.length)
			usage();
		
		resultSetFile = args[param_start++];
		uniqLinks.loadResultSet(resultSetFile);
		uniqLinks.init(param_start, args);
		uniqLinks.loadRunFiles();
		uniqLinks.anaysis();
	}

}
