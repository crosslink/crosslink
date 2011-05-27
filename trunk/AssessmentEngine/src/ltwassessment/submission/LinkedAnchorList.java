package ltwassessment.submission;

import java.util.LinkedList;

import ltwassessment.wiki.Topic;

/**
 * @author monfee
 *
 */
public class LinkedAnchorList {
	private LinkedList<Anchor> anchorList = null;

	public LinkedAnchorList() {
		anchorList = new LinkedList<Anchor> ();
	}

	/*
	 * The possible anchor overlappings 
	 * 1.  ______________
	 *         _____________________
	 *         
	 * 2.  __________________
	 *       ________
	 *       
	 * 3.  ____________________
	 *        ______________
	 *              ________________
	 *             
	 * 4. __________________
	 *      ____________
	 *      ______                     
	 *                      
	 * The principle of ordering the anchors is the offset the anchor. the lower offset, the higher ranking
	 * If same offset, the shorter length get higher ranking 
	 * 
	 */
	public void insert(Anchor anchor) {
		if (anchorList.size() == 0)
			anchorList.add(anchor);
		else {
			int index = -1;
			Anchor theOne = null;
			boolean overlapping = false;
			boolean addBefore = false;
			
			index = after(anchor.getOffset());
			
			if (index == -1) 
				theOne = anchorList.getLast();
			else 
				theOne = anchorList.get(index);
			
			if (anchor.getOffset() == theOne.getOffset()) { //which means definitely overlapping
				overlapping = true;
				if (anchor.getLength() == theOne.getLength()) { // same anchor, merge the targets 
					theOne.getTargets().putAll(anchor.getTargets());
					return; 
				}
				else if (anchor.getLength() > theOne.getLength()) {// add to the end
					anchorList.add(anchor);
				}
				else { // right before the last one
					anchorList.add(anchorList.size() - 1, anchor);
					
					addBefore = true;
				}
			}
			else if (anchor.getOffset() > theOne.getOffset()) { // add after the one
				if (index == (anchorList.size() - 1)) 
					anchorList.add(anchor);		
				else 
					anchorList.add(index + 1, anchor);		
				
				// to see if overlapping
				if ((theOne.getOffset() + theOne.getLength()) > anchor.getOffset()) 
					overlapping = true;
			}
			else {
				anchorList.add(index, anchor);
				
				if ((anchor.getOffset() + anchor.getLength()) > theOne.getOffset()) { 
					overlapping = true;
					addBefore = true;
				}
			}
			
			// now link the anchors together if there is overlapping
			if (overlapping) {
				Anchor first = theOne.getFirst();
				Anchor last = theOne.getLast();
				if (addBefore) {
					if (theOne.getPrevious() != null) {
						anchor.setPrevious(theOne.getPrevious());
						theOne.getPrevious().setNext(anchor);			
					}
					anchor.setNext(theOne);
					theOne.setPrevious(anchor);
					
//					if (first == null/* || theOne.getLast() == null*/) {
//						first = theOne;
//						theOne.setFirst(first);
//						anchor.setFirst(first);
//						theOne.setLast(anchor);
//						anchor.setLast(anchor);
//					}
//					else {
//						anchor.setFirst(first);
//						anchor.setLast(last);
//					}
				}
				else { //add after
 					if (theOne.getNext() != null) {
						anchor.setNext(theOne.getNext());
						theOne.getNext().setPrevious(anchor);			
					}
					anchor.setPrevious(theOne);
					theOne.setNext(anchor);
					
//					if (first == null/* || theOne.getLast() == null*/) {
//						first = theOne;
//						theOne.setFirst(first);
//						anchor.setFirst(first);
//						theOne.setLast(anchor);
//						anchor.setLast(anchor);
//					}
//					else {
//						anchor.setFirst(first);
//						anchor.setLast(last);
//					}
				}
				
//				if ()
			}
		}		
	}

	public void append(Anchor anchor) {
		anchorList.add(anchor);
	}
	
	
	/**
	 * @param offset
	 * @param low
	 * @param high
	 * @return the index of the anchor in the list
	 */
	public int after(int offset, int low, int high) {
		int mid = (high - low) / 2 + low;
		
		if (mid == high || mid == low)
			return mid;
		
		Anchor midAnchor = anchorList.get(mid);
		if (midAnchor.getOffset() > offset) 
			return after(offset, low, mid);
		return after(offset, mid, high);
	}
	
	public int after(int offset) {
		int low = 0, high = anchorList.size() - 1;

		return after(offset, 0, high);
	}
	
	public void sortByRank() {
		
	}

	public boolean validateAll(Topic topic) {
		boolean result = true;
		for (Anchor anchor: anchorList) {
			if (!anchor.validate(topic))
				result = false;
		}
		return result;
	}

	public LinkedList<Anchor> getAnchorList() {
		return anchorList;
	}
	
	public void calculateOverlappedAnchorExtensionLength() {
		if (anchorList.size() > 0) {
			int count = 0;
			Anchor anchor = null;
			Anchor next = null;
			while (count < anchorList.size()) {
				anchor = anchorList.get(count);
				
				next = anchor.getNext();
				if (next != null) {
					while (next != null) {
						++count;
						int extension  = anchor.getOffset() + anchor.getLength() + anchor.getExtendedLength();
						int nextEnd = (next.getOffset() + next.getLength());
						if (extension < nextEnd)
							anchor.setExtendedLength(nextEnd - extension);
							
						next = next.getNext();
					}
				}
				
				++count;
			}
		}
	}
}
