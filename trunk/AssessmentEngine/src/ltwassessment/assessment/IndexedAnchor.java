package ltwassessment.assessment;

import java.util.Vector;

import ltwassessment.submission.Anchor;

public class IndexedAnchor extends Anchor {
	
	protected int status = 0;
	
	protected int offsetIndex;
	protected int lengthIndex;
	
	Vector<Bep> beps = new Vector<Bep>();

	public IndexedAnchor(int offset, int length, String name) {
		super(offset, length, name);
	}

	public IndexedAnchor(String offset, String length, String name) {
		super(Integer.parseInt(offset), Integer.parseInt(length), name);
	}
	
	public IndexedAnchor(int offset, int length, String name, int rel) {
		super(offset, length, name);
		this.status = rel;
	}
	
	public IndexedAnchor(String offset, String length, String name, String rel) {
		super(Integer.parseInt(offset), Integer.parseInt(length), name);
		this.status = Integer.parseInt(rel);
	}
	
	public IndexedAnchor() {
		super(0, 0, "");
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}
	
	/**
	 * @return the offsetIndex
	 */
	public int getOffsetIndex() {
		return offsetIndex;
	}

	/**
	 * @param offsetIndex the offsetIndex to set
	 */
	public void setOffsetIndex(int offsetIndex) {
		this.offsetIndex = offsetIndex;
	}

	/**
	 * @return the lengthIndex
	 */
	public int getLengthIndex() {
		return lengthIndex;
	}

	/**
	 * @param lengthIndex the lengthIndex to set
	 */
	public void setLengthIndex(int lengthIndex) {
		this.lengthIndex = lengthIndex;
	}

	public String statusToString() {
		return String.valueOf(status);
	}

	public String lengthIndexToString() {
		return String.valueOf(lengthIndex);
	}

	public String offsetIndexToString() {
		return String.valueOf(offsetIndex);
	}

	public void addBep(Bep bep) {
		beps.add(bep);
	}
	
	public Vector<Bep> getBeps() {
		return beps;
	}	
}
