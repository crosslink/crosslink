package ltwassessment.assessment;

import java.util.Vector;

import ltwassessment.submission.Anchor;

public class IndexedAnchor extends Anchor {
	public final static int UNINITIALIZED_VALUE = -1; 
	
	protected int status = 0;
	
	protected int offsetIndex;
	protected int lengthIndex;
	
    protected int screenPosStart = -1;
    protected int screenPosEnd = -1;
	
	Vector<AssessedAnchor> childrenAnchors = new Vector<AssessedAnchor>();

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
	
	public String screenPosStartToString() {
		return String.valueOf(screenPosStart);
	}
	

	public String screenPosEndToString() {
		return String.valueOf(screenPosEnd);
	}
	

	public int getScreenPosStart() {
		return screenPosStart;
	}

	public void setScreenPosStart(int screenPosStart) {
		this.screenPosStart = screenPosStart;
	}

	public int getScreenPosEnd() {
		return screenPosEnd;
	}

	public void setScreenPosEnd(int screenPosEnd) {
		this.screenPosEnd = screenPosEnd;
	}
    
    public void setIndexOffset(int offset, int length) {
    	setOffsetIndex(offset);
    	setLengthIndex(length);
    }
    
	public void setCurrentAnchorProperty(int offset, int length, int screenPosStart, int screenPosEnd, int status, int extLength) {
		setOffset(offset);
		setLength(length);
		screenPosStart = screenPosStart;
		screenPosEnd = screenPosEnd;
		
		setStatus(status);
		
		setIndexOffset(offset, length);
		//setCurrentAnchorProperty(String.valueOf(offset), String.valueOf(length), String.valueOf(screenPosStart), String.valueOf(screenPosEnd), String.valueOf(status), String.valueOf(extLength));
	}
	
	public void setCurrentAnchorProperty(String offsetStr, String lengthStr, String screenPosStartStr, String screenPosEndStr, String statusStr, String extLengthStr) {
		//String sysPropertyValue = offsetStr + "_" + lengthStr + "_" + screenPosStartStr + "_" + screenPosEndStr + "_" + statusStr + "_" + extLengthStr;
		//System.setProperty(sysPropertyCurrTopicOLSEStatusKey, sysPropertyValue);
		setCurrentAnchorProperty(Integer.valueOf(offsetStr), Integer.valueOf(lengthStr), Integer.valueOf(screenPosStartStr), Integer.valueOf(screenPosEndStr), Integer.valueOf(statusStr), Integer.valueOf(extLengthStr));
	}
	
	public void addChildAnchor(IndexedAnchor anchor) {
		childrenAnchors.add((AssessedAnchor) anchor);
	}

	public Vector<AssessedAnchor> getChildrenAnchors() {
		return childrenAnchors;
	}
	
	public String toKey() {
		return this.offsetToString() + "_" +  this.lengthToString();
	}
}
