package ltwassessment.submission;

import java.util.LinkedList;


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
		Anchor theOne = null;
		Anchor first = null;
		if (anchorList.size() == 0)
			anchorList.add(anchor);
		else {
			int index = -1;
			boolean overlapping = false;
			boolean addBefore = false;
			
			index = after(anchor.getOffset(), anchor.getLength());
			
			if (index == -1) 
				theOne = anchorList.getLast();
			else 
				theOne = anchorList.get(index);
			
            if ((anchor.getName().equals("古代") && anchor.getOffset() == 596)
            		|| theOne.getName().equals("古代")  && theOne.getOffset() == 596)
            	System.err.println("I got you!");
            
			// to see if overlapping
			if (anchor.getOffset() == theOne.getOffset()
					|| (anchor.getOffset() > theOne.getOffset() && (theOne.getOffset() + theOne.getLength()) > anchor.getOffset()) 				
						|| (anchor.getOffset() < theOne.getOffset() && (anchor.getOffset() + anchor.getLength()) > theOne.getOffset())
						)
				overlapping = true;
            
			if (overlapping) { //which means definitely overlapping
				Anchor next = theOne;
				while (true) {
					theOne = next;
					if ((anchor.getOffset() == next.getOffset() && next.getLength() >= anchor.getLength())
							|| (next.getOffset() > anchor.getOffset()))
						break;	
					if ((next = next.getNext()) == null)
						break;
					++index;
				}
			}
			if (anchor.getOffset() == theOne.getOffset()) {
				if (anchor.getLength() == theOne.getLength()) { // same anchor, merge the targets 
					theOne.addTargets(anchor.getTargets()); //theOne.getTargets().putAll(anchor.getTargets());
					return; 
				}
				else if (anchor.getLength() > theOne.getLength()) {// add to the position right after theOne
					anchorList.add(index + 1, anchor);
				}
				else { // right before the index
					anchorList.add(index, anchor);
					addBefore = true;
				}
			}
			else if (anchor.getOffset() > theOne.getOffset()) { // add after the one
				if (index == (anchorList.size() - 1)) 
					anchorList.add(anchor);		
				else 
					anchorList.add(index + 1, anchor);		
			}
			else { // add before
				anchorList.add(index, anchor);
				addBefore = true;	
			}
			
			// now link the anchors together if there is overlapping
			if (overlapping) {			
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
	public int after(int offset, int length, int low, int high) {
//		int mid = (high - low) / 2 + low;
//		
//		if (mid == high || mid == low)
//			return mid;
//		
//		Anchor midAnchor = anchorList.get(mid);
//		if (midAnchor.getOffset() >= offset) 
//			return after(offset, low, mid);
//		return after(offset, mid, high);
		
		int mid;
		Anchor target;
		while (low < high)
		{
			mid = (low + high) / 2;
			target = anchorList.get(mid);
			if (target.getOffset() < offset && (target.getOffset() + target.getLength()) < (offset + length))
				low = mid;
			else
				high = mid;
		}
		return low;
	}
	
	public int after(int offset, int length) {
		int low = 0, high = anchorList.size() - 1;

		return after(offset, length, low, high);
	}
	
	public void sortByRank() {
		
	}

	public boolean validateAll(Topic topic, int showMessage) {
		boolean result = true;
		for (Anchor anchor: anchorList) {
			if (!anchor.validate(topic, showMessage))
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
//				if (next != null) {
					while (next != null) {
						++count;
						int extension  = anchor.getOffset() + anchor.getLength() + anchor.getExtendedLength();
						int nextEnd = (next.getOffset() + next.getLength());
						if (extension < nextEnd)
							anchor.setExtendedLength(nextEnd - extension);
							
						next = next.getNext();
					}
//				}
				
				++count;
			}
		}
	}
}
