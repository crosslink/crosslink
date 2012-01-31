package ltwassessment.submission;

import java.util.HashMap;

public class TopicLink {
	private String id;
	private HashMap<String, Target> targets = new HashMap<String, Target>();
	private HashMap<String, DuplicatedLink> duplicatedLinks = new HashMap<String, DuplicatedLink>();
	
	
	
	public TopicLink(String id) {
		super();
		this.id = id;
	}



	public void addLink(Target link) {
		String linkId = link.getId();
		if (!targets.containsKey(linkId)) {
			targets.put(linkId, link);
		}
		else {
			DuplicatedLink dupLink = new DuplicatedLink();
			
			dupLink.setTarget(link);
			dupLink.addRunId(link.getParent().getAssociatedTopic().getParent().getRunName());
		}
	}
}
