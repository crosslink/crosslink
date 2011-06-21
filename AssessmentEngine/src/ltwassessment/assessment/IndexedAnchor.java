package ltwassessment.assessment;

import ltwassessment.submission.Anchor;

public class IndexedAnchor extends Anchor {
	
	protected int status = 0;
	
	protected int offsetIndex;
	protected int lengthIndex;

	public IndexedAnchor(int offset, int length, String name) {
		super(offset, length, name);
		
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
}
