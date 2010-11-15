package ltwassessment.pool;

import java.util.LinkedList;

public class LinkedAnchorList {
	private LinkedList<Anchor> anchorList = null;

	public LinkedAnchorList() {
		anchorList = new LinkedList<Anchor> ();
	}

	public void insert(Anchor anchor) {
		if (anchorList.size() == 0)
			anchorList.add(anchor);
		else {
			int index = after(anchor.getOffset());
			if (index == -1)
				anchorList.add(anchor);
			else
				anchorList.add(index, anchor);
		}		
	}
	
	public int after(int offset, int low, int high) {
		int mid = (high - low) / 2;
		
		Anchor midAnchor = anchorList.get(mid);
		if (midAnchor.getOffset() > offset) {
			if (mid == low)
				return mid;
			return after(offset, low, mid);
		}
		if (mid == high)
			return -1;
		return after(offset, mid, high);
	}
	
	public int after(int offset) {
		int low = 0, high = anchorList.size() - 1;

		return after(offset, 0, high);
	}
}
