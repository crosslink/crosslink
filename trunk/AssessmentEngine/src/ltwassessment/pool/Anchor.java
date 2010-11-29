package ltwassessment.pool;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class Anchor {
	
	private int offset = 0;
	private int length = 0;
	private int extendedLength = 0; // overlapping
	private String name = "";
	private int rank = 0;
//	private int over
	
	private boolean valid = false;
	
	// for the overlapping Anchors
	private Anchor first = null;
	private Anchor last = null;
	private Anchor next = null;
	private Anchor previous = null;

//	private Vector<Target> targets = null;
	private Map<String, Target>	targets = null;
	
//	public Anchor(Anchor anchor) {
//		this.offset = anchor.getOffset();
//		this.length = anchor.getLength();
//		this = anchor;
//		this = anchor;
//		this = anchor;
//	}
	
	public Anchor(int offset, int length, String name) {
		super();
		this.offset = offset;
		this.length = length;
		this.name = name;
		
//		targets = new Vector<Target>();
		targets = new HashMap<String, Target>();
	}
	
	public int getExtendedLength() {
		return extendedLength;
	}

	public void setExtendedLength(int extendedLength) {
		this.extendedLength = extendedLength;
	}

	public Anchor getFirst() {
		return first;
	}

	public void setFirst(Anchor first) {
		this.first = first;
	}

	public Anchor getLast() {
		return last;
	}

	public void setLast(Anchor last) {
		this.last = last;
	}
	
	public Anchor getNext() {
		return next;
	}

	public void setNext(Anchor next) {
		this.next = next;
	}

	public Anchor getPrevious() {
		return previous;
	}

	public void setPrevious(Anchor previous) {
		this.previous = previous;
	}

	public int getOffset() {
		return offset;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	public int getLength() {
		return length;
	}
	
	public void setLength(int length) {
		this.length = length;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Map<String, Target> getTargets() {
		return targets;
	}
	
	public void insertTarget(Target target) {
		if (!targets.containsKey((target.getId()))) 
			targets.put(target.getId(), target);
	}
	
	public boolean isValid() {
		return valid;
	}
	
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getRank() {
		return rank;
	}	
	
//	public boolean validate(Topic topic) {
//		valid = false;
//		boolean result = true;
//		Anchor cur = first;
//		while (cur != null && cur != last) {		
//			if (!cur.validateIt(topic));
//				result = false;
//			cur = cur.getFirst();
//		}
//		result = last.validateIt(topic);
//
//		return valid;
//	}

	public boolean validate(Topic topic) {
		return topic.matchAnchor(offset, length, name);
	}

}
