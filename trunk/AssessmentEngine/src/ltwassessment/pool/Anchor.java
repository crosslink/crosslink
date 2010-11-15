package ltwassessment.pool;

import java.util.Vector;

public class Anchor {
	
	private int offset = 0;
	private int length = 0;
	private String name = "";
	private int rank = 0;
	
	private boolean valid = false;

	Vector<Target> targets = null;
	
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
}
