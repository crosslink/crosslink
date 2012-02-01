package crosslink.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;


public class Team extends LinkCountTemplate {
//	HashMap<String, Set<String>> topicLinks = new HashMap<String, Set<String>>();  //only keep the links that is in the result set
//	HashMap<String, Set<String>> topicAnchors = new HashMap<String, Set<String>>();  //only keep the links that is in the result set
//	Set<String>		linkSet;
	public class RunLink extends LinkCountTemplate {
		HashSet<String> uniqLinks = new HashSet<String>();

		public RunLink(String runId) {
			super(runId);
		}

		public void addLink(String link) {
			if (!uniqLinks.contains(link)) {
				uniqLinks.add(link);
			}
		}
		public void setUniqLink(String link) {
			uniqLinks.add(link);
		}
		
		public HashSet<String> getUniqLinks() {
			return uniqLinks;
		}
	}
	
	HashMap<String, RunLink> runLinks = new HashMap<String, RunLink>(); //run
	
	protected HashMap<String, HashSet<String>> runIdLinkSet = new HashMap<String, HashSet<String>>(); // link id, run id

	private ArrayList<RunLink> runLinkArray = new ArrayList<RunLink>();
	
//	private HashMap<String, HashSet<String>> runUniqLinks = new HashMap<String, HashSet<String>>();

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
			runLink = new RunLink(runId);
			runLinks.put(runId, runLink);
		}
		if (runIdLinkSet.containsKey(link)) {
			if (!runIdLinkSet.get(link).contains(runId))
				runIdLinkSet.get(link).add(runId);
//			else
//				System.out.println("Something wrong here");
//			runLink  = runIdLinkSet.get(link);
//			runLink.addLink(link); 
		}
		else {
			HashSet<String> runs = new HashSet<String>();
			runs.add(runId);
			runIdLinkSet.put(link, runs);
		}
	}

	public void printRunLinks(boolean printOutLinks) {
		this.sortRunId();
		for (RunLink runLink : runLinkArray) {
			//											number of unique comparing with other teams, number of unique links comparing other runs of the same team
			System.out.println(runLink.getId() + ", " + runLink.getUniqLinkCount()+ ", " + runLink.getUniqLinks().size());
			if (printOutLinks) {
				System.err.print("###################################################################-");
				System.err.print(runLink.getId());
				System.err.println("-###################################################################");
				Collection<String> links = runLink.getUniqLinks();
				for (String link : links)
					System.err.println(link);
			}
		}
	}
	
	public void setUniqLink(String link) {
		this.increaseLinkCount();
		for (Object obj : runIdLinkSet.get(link).toArray()) {
			String runId = (String)obj;
			RunLink runLink = runLinks.get(runId);
			runLink.increaseLinkCount();
			if (!runIdLinkSet.containsKey(link) && runIdLinkSet.get(link).size() == 1) {
				((RunLink)runIdLinkSet.get(link).toArray()[0]).addLink(link);
//				runUniqLinks.get(runLink.getId()).add(link);
			}

		}
	}
}