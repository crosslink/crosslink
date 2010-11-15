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
	
	public int after(int offset) {
		int mid = anchorList.size() / 2;
		
		if ()
		return -1;
	}
}
