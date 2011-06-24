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
}
