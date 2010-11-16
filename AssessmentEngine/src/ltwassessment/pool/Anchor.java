package ltwassessment.pool;

import java.util.Vector;

public class Anchor {
	
	private int offset = 0;
	private int length = 0;
	private int extendedLength = 0; // overlapping
	private String name = "";
	private int rank = 0;
	
	private boolean valid = false;
	
	// for the overlapping Anchors
	private Anchor next = null;
	private Anchor last = null;
	
	private Vector<Target> targets = null;
	
	public int getExtendedLength() {
		return extendedLength;
	}

	public void setExtendedLength(int extendedLength) {
		this.extendedLength = extendedLength;
	}

	public Anchor getNext() {
		return next;
	}

	public void setNext(Anchor next) {
		this.next = next;
	}

	public Anchor getLast() {
		return last;
	}

	public void setLast(Anchor last) {
		this.last = last;
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
	
	public Vector<Target> getTargets() {
		return targets;
	}
	
	public void insertTarget(Target target) {
		targets.add(target);
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
	
	public boolean validate() {
		valid = false;
		boolean result = true;
		Anchor cur = next;
		while (cur != null && cur != last) {		
			if (!cur.validateIt());
				result = false;
			cur = cur.getNext();
		}
		result = last.validateIt();

		return valid;
	}

	private boolean validateIt() {
		// TODO Auto-generated method stub
		return false;
	}
}
