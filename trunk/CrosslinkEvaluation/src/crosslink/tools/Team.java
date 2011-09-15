package crosslink.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;


public class Team extends LinkCountTemplate {
//	HashMap<String, Set<String>> topicLinks = new HashMap<String, Set<String>>();  //only keep the links that is in the result set
//	HashMap<String, Set<String>> topicAnchors = new HashMap<String, Set<String>>();  //only keep the links that is in the result set
//	Set<String>		linkSet;
	public class RunLink extends LinkCountTemplate {
//		HashSet<String> uniqLinks = new HashSet<String>();
		
		public RunLink(String runId, String link) {
			super(runId);
//			addLink(link);
		}

		public RunLink(String runId) {
			super(runId);
		}

//		public void addLink(String link) {
//			if (!uniqLinks.contains(link)) {
//				uniqLinks.add(link);
//			}
//		}
		
	}
	
	HashMap<String, RunLink> runLinks = new HashMap<String, RunLink>(); //run
	
	private HashMap<String, HashSet<RunLink>> runIdLinkSet = new HashMap<String, HashSet<RunLink>>(); // link id, run (id, count)
	private ArrayList<RunLink> runLinkArray = new ArrayList<RunLink>();

	public Team(String teamId) {
		super(teamId);
	}

//	public Team(String participantId, String runId) {
//		super(participantId);
//		this.runIdLinkSet.put(runId, new RunLink(runId));
//	}
	
	public void sortRunId() {
		runLinkArray.clear();
//		runLinkArray.addAll(this.runIdLinkSet.values());
		runLinkArray.addAll(runLinks.values());
		Collections.sort(runLinkArray);
	}

	public void addRunId(String runId, String link) {
		int linkNum = 0;
		RunLink runLink = null;
		
		if (runLinks.containsKey(runId))
			runLink = runLinks.get(runId);
		else {
			runLink = new RunLink(runId, link);
			runLinks.put(runId, runLink);
		}
		if (runIdLinkSet.containsKey(link)) {
			if (!runIdLinkSet.get(link).contains(runLink))
				runIdLinkSet.get(link).add(runLink);
//			runLink  = runIdLinkSet.get(link);
//			runLink.addLink(link); 
		}
		else {
			HashSet<RunLink> runs = new HashSet<RunLink>();
			runs.add(runLink);
			runIdLinkSet.put(link, runs);
		}
	}

	public void printRunLinks() {
		this.sortRunId();
		for (RunLink runLink : runLinkArray)
			System.out.println(runLink.getId() + ", " + runLink.getUniqLinkCount());
	}
	
	public void setUniqLink(String link) {
		this.increaseLinkCount();
		for (Object obj : runIdLinkSet.get(link).toArray()) {
			RunLink runLink = (RunLink)obj;
			runLink.increaseLinkCount();
		}
	}
}