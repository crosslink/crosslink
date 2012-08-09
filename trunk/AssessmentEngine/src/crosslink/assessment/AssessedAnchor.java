package crosslink.assessment;

import java.util.Vector;

import crosslink.parsers.PoolerManager;
import crosslink.parsers.ResourcesManager;

import crosslink.assessment.Bep;
import crosslink.assessment.IndexedAnchor;

public class AssessedAnchor extends IndexedAnchor {   
	
	
	IndexedAnchor parent = null;

	public AssessedAnchor(int offset, int length, String name) {
		super(offset, length, name);
	}

	public AssessedAnchor(int offset, int length, String name, int rel) {
		super(offset, length, name);
		this.status = rel;
	}
	
	public IndexedAnchor getParent() {
		return parent; // == null ? this : parent;
	}

	public void setParent(IndexedAnchor parent) {
		this.parent = parent;
	}

	
	@Override
	public int getScreenPosStart() {
		if (screenPosStart == UNINITIALIZED_VALUE) {
//			if (parent != null)
			screenPosStart = offset - parent.getOffset() + parent.getScreenPosStart(); 
//			else
//				screenPosStart = offset;
		}
		return screenPosStart;
	}

	@Override
	public int getScreenPosEnd() {
		if (screenPosEnd == UNINITIALIZED_VALUE) {
			screenPosEnd = this.getScreenPosStart() + length;
		}
		return screenPosEnd;
	}
	
	public int checkStatus() {
		int newStatus = status;
		int finished = ASSESSMENT_FINISHED_NO;
		int unassessedLinks = 0;
		
		for (Bep link : getBeps()) {
			int rel = link.getRel();
			if ( rel == Bep.UNASSESSED)
				++unassessedLinks;
			else if (rel == Bep.RELEVANT)
				newStatus = Bep.RELEVANT;
		}
		
		if (unassessedLinks == 0) {
			finished = ASSESSMENT_FINISHED_YES;
			if (newStatus == Bep.UNASSESSED)
				newStatus = Bep.IRRELEVANT;
		}
		else {
			newStatus = Bep.UNASSESSED;
			if (unassessedLinks == getBeps().size()) {
				if (status == Bep.IRRELEVANT) {
					finished = ASSESSMENT_FINISHED_YES;
					newStatus = Bep.IRRELEVANT;
				}
			}

//			else {
//				finished = ASSESSMENT_FINISHED_NO;
//				newStatus = Bep.IRRELEVANT;
//				for (Bep link : getBeps()) {
//					if (link.getRel() == Bep.UNASSESSED) {
//						finished = ASSESSMENT_FINISHED_NO;
//						newStatus = Bep.UNASSESSED;
//						break;
//					}
//					else if (link.getRel() == Bep.RELEVANT)
//						newStatus = Bep.RELEVANT;
//				}
//				
//			}
		}
		if (status != newStatus) {
			status = newStatus;
			PoolerManager.getPoolUpdater().updatePoolSubanchorStatus(ResourcesManager.getInstance().getTopicID(), this);
		}
		return finished;
	}
	
	public Bep getNextLink(Bep currentlink, boolean needNotFinished) {	
    	int i = 0;
    	int currentLinkIndex = 0;
    	Bep link = null;
    	Bep unassessedLink = null;
    	if (currentlink != null)
	    	for (; i < getBeps().size(); ++i) {
	    		link = getBeps().get(i);
	    		if (needNotFinished && unassessedLink == null && link.getRel() == Bep.UNASSESSED)
	    			unassessedLink = link;
	    		
	    		if (link == currentlink) {
	    			currentLinkIndex = i;
	    			++i;
	    			break;
	    		}
	    	}
    	
    	if (needNotFinished)
	    	for (; i < getBeps().size(); ++i) {
	    		link = getBeps().get(i);
	    		if (link.getRel() == Bep.UNASSESSED) {
	    			unassessedLink = link;
	    			break;
	    		}
	    	}
    	
    	if (i >= getBeps().size()) {
//    		i = 0;
    		link = null;
    	}
    	else
    	   	link = getBeps().get(i);
    	return needNotFinished ? unassessedLink : link;
	}
	
	public Bep getPreviousLink(Bep currentlink, boolean needNotFinished) {
    	int i = getBeps().size() - 1;
    	int currentLinkIndex = 0;
    	Bep link = null;
    	Bep unassessedLink = null;
    	if (currentlink != null)
	    	for (; i > -1; --i) {
	    		link = getBeps().get(i);
	    		if (needNotFinished && unassessedLink == null && link.getRel() == Bep.UNASSESSED)
	    			unassessedLink = link;
	    		
	    		if (link == currentlink) {
	    			currentLinkIndex = i;
	    			--i;
	    			break;
	    		}
	    	}
    	
    	if (needNotFinished)
	    	for (; i < getBeps().size(); --i) {
	    		link = getBeps().get(i);
	    		if (link.getRel() == Bep.UNASSESSED) {
	    			unassessedLink = link;
	    			break;
	    		}
	    	}
    	
    	if (i < 0) {
    		//i = getBeps().size() - 1;
    		link = null;
    	}
    	else
    		link = getBeps().get(i);
    	return needNotFinished ? unassessedLink : link;
	}
	
	public void markAllLinksIrrevlent() {
		Bep link = null;
		for (int i = 0; i < getBeps().size(); ++i) {
			link = getBeps().get(i);
//			if (link.getRel() != Bep.IRRELEVANT) {
				link.setRel(Bep.IRRELEVANT);
				PoolerManager.getPoolUpdater().updateTopicAnchorLinkRel(link);
//			}
		}
	}
}
