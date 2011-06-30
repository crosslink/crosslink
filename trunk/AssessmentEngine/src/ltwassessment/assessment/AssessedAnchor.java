package ltwassessment.assessment;

import java.util.Vector;

public class AssessedAnchor extends IndexedAnchor {   
	Vector<Bep> beps = new Vector<Bep>();
	
	IndexedAnchor parent = null;

	public AssessedAnchor(int offset, int length, String name) {
		super(offset, length, name);
	}

	public AssessedAnchor(int offset, int length, String name, int rel) {
		super(offset, length, name);
		this.status = rel;
	}
	
	public IndexedAnchor getParent() {
		return parent;
	}

	public void setParent(IndexedAnchor parent) {
		this.parent = parent;
	}

	public void addBep(Bep bep) {
		beps.add(bep);
	}
	
	public Vector<Bep> getBeps() {
		return beps;
	}	
	
	@Override
	public int getScreenPosStart() {
		if (screenPosStart == UNINITIALIZED_VALUE) {
			screenPosStart = offset - parent.getOffset() + parent.getScreenPosStart(); 
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
	
	@Override
	public int checkStatus() {
		int finished = ASSESSMENT_FINISHED_NO;
		if (status == Bep.IRRELEVANT)
			finished = ASSESSMENT_FINISHED_YES;
		else {
			finished = ASSESSMENT_FINISHED_YES;
			for (Bep link : getBeps()) {
				if (link.getRel() == Bep.UNASSESSED) {
					finished = ASSESSMENT_FINISHED_NO;
					break;
				}
			}
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
    	
    	if (i >= getBeps().size())
    		i = 0;
    	
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
    	
    	if (i < 0)
    		i = getBeps().size() - 1;
    	
    	link = getBeps().get(i);
    	return needNotFinished ? unassessedLink : link;
	}
}
