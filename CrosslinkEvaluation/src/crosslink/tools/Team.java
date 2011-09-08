package crosslink.tools;

public class Team implements Comparable {
	String 			teamName;
//	HashMap<String, Set<String>> topicLinks = new HashMap<String, Set<String>>();  //only keep the links that is in the result set
//	HashMap<String, Set<String>> topicAnchors = new HashMap<String, Set<String>>();  //only keep the links that is in the result set
//	Set<String>		linkSet;
	
	int 			uniqLinkCount = 0;

	public Team(String teamId) {
		teamName = teamId;
	}

	@Override
	public int compareTo(Object arg0) {
		Team team = (Team)arg0;
		
		if (team.uniqLinkCount < this.uniqLinkCount)
			return -1;
		else if (team.uniqLinkCount > this.uniqLinkCount)
			return 1;
		return 0;
	}

	public void increaseLinkCount(int i) {
		uniqLinkCount += i;
	}
	
}