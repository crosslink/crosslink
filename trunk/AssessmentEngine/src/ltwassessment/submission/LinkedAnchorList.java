package ltwassessment.submission;

import java.util.HashMap;
import java.util.LinkedList;


/**
 * @author monfee
 *
 */
public class LinkedAnchorList {
	private LinkedList<Anchor> anchorList = null;
	private HashMap<String, Anchor> anchorMap = null;

	public LinkedAnchorList() {
		anchorList = new LinkedList<Anchor> ();
		anchorMap = new HashMap<String, Anchor>();
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
		if (!anchorMap.containsKey(anchor.getName()))
			anchorMap.put(anchor.getName(), anchor);
		else {
			anchorMap.get(anchor.getName()).addTargets(anchor.getTargets());
			return;
		}
		
		Anchor theOne = null;

		if (anchorList.size() == 0) {
			anchorList.add(anchor);
			return;
		}

		int index = -1;
		boolean overlapping = false;
		boolean addBefore = false;
		
//        if ((anchor.getName().equals("古代") && anchor.getOffset() == 596))
//        	System.err.println("I got you!");
        
		index = after(anchor.getOffset(), anchor.getLength());
		
		if (index == -1) 
			theOne = anchorList.getLast();
		else 
			theOne = anchorList.get(index);
		
//        if (theOne.getName().equals("古代")  && theOne.getOffset() == 596)
//        	System.err.println("I got you!");
        
		// to see if overlapping
		overlapping = isOverlapped(anchor, theOne);	
        
//			if (overlapping) { //which means definitely overlapping
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
//			}
		if (anchor.getOffset() == theOne.getOffset()) {
			if (anchor.getLength() == theOne.getLength()) { // same anchor, merge the targets 
				theOne.addTargets(anchor.getTargets()); //theOne.getTargets().putAll(anchor.getTargets());
				return; 
			}
			else if (anchor.getLength() > theOne.getLength()) {// add to the position right after theOne
//					anchorList.add(index + 1, anchor);
				++index;
			}
			else { // right before the index
//					anchorList.add(index, anchor);
				addBefore = true;
			}
		}
		else if (anchor.getOffset() > theOne.getOffset()) { // add after the one
//				if (index == (anchorList.size() - 1)) 
//					anchorList.add(anchor);		
//				else 
//					anchorList.add(index + 1, anchor);		
			++index;
		}
		else { // add before
//				anchorList.add(index, anchor);
			addBefore = true;	
		}
		
		insert(index, anchor);
		
		// now link the anchors together if there is overlapping
		if (overlapping) {			
			if (addBefore) //{
				connectBefore(anchor, theOne);
			else
				connectAfter(anchor, theOne);
		}
		
		// because the overlapping is checked on the most adjecent one but
		// still one more needed to be checked
//		Anchor before = null;
//		Anchor after = null;
//			if (addBefore) {
//				if (index > 0)
//					before = anchorList.get(index - 1);			
//			}
//			else {
//				if (index < (anchorList.size() - 1))
//					after = anchorList.get(index + 1);
//			}
//			
//			if (before != null && isOverlapped(anchor, before)) {
////				if (before.getNext() == null || before.getNext() != anchor)
//						connectAfter(anchor, before);
//			}
//			
//			if (after != null && isOverlapped(anchor, after)) {
////				if (after.getNext() == null || after.getNext() != anchor)
//					connectBefore(anchor, after);
//			}		
	}
	
	private boolean isOverlapped(Anchor anchor, Anchor theOne) {
		return (anchor.getOffset() == theOne.getOffset()
				|| (anchor.getOffset() > theOne.getOffset() && (theOne.getOffset() + theOne.getLength()) > anchor.getOffset()) 				
					|| (anchor.getOffset() < theOne.getOffset() && (anchor.getOffset() + anchor.getLength()) > theOne.getOffset())
					);
	}
	
	private void insert(int index, Anchor anchor) {
		if (index == anchorList.size())
			anchorList.add(anchor);
		else
			anchorList.add(index, anchor);		
	}
	
	private void connectAfter(Anchor anchor, Anchor theOne) {
		assert(anchor != theOne);
		if (theOne.getOffset() < anchor.getOffset() || (anchor.getOffset() == theOne.getOffset() && theOne.getLength() < anchor.getLength())) {
			if (theOne.getNext() != null) {
				anchor.setNext(theOne.getNext());
				theOne.getNext().setPrevious(anchor);			
			}
			anchor.setPrevious(theOne);
			theOne.setNext(anchor);		
		}
		else {
			if (theOne.getPrevious() != null && theOne.getPrevious().getOffset() < anchor.getOffset() || (anchor.getOffset() == theOne.getPrevious().getOffset() && theOne.getPrevious().getLength() < anchor.getLength()))
				connectAfter(anchor, theOne.getPrevious());
			connectBefore(anchor, theOne);
		}
	}
	
	private void connectBefore(Anchor anchor, Anchor theOne) {
		assert(anchor != theOne);
		if (anchor.getOffset() < theOne.getOffset() || (anchor.getOffset() == theOne.getOffset() && anchor.getLength() < theOne.getLength())) {
			if (theOne.getPrevious() != null ) {
				anchor.setPrevious(theOne.getPrevious());
				theOne.getPrevious().setNext(anchor);			
			}
			anchor.setNext(theOne);
			theOne.setPrevious(anchor);
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
			if (target.getOffset() == offset) {
				if (target.getLength() < length)
					low = mid + 1;
				else
					high = mid;
			}
			else {
				if (target.getOffset() < offset)
					low = mid + 1;
				else
					high = mid;
			}
		}
		return low;
	}
	
	public int after(int offset, int length) {
		int low = 0, high = anchorList.size() - 1;

		return after(offset, length, low, high);
	}
	
	public void sortByRank() {
		
	}

	public boolean validateAll(Topic topic, int showMessage, boolean convertToTextOffset) {
		boolean result = true;
		for (Anchor anchor: anchorList) {
			if (!anchor.validate(topic, showMessage, convertToTextOffset))
				result = false;
		}
		return result;
	}

	public LinkedList<Anchor> getAnchorList() {
		return anchorList;
	}
	
	public void connectIfOverlapped() {
		if (anchorList.size() > 0) {
			int count = 1;
			Anchor anchor = null;
			Anchor next = null;

			anchor = anchorList.get(0);
			int nextEnd, offsetEnd = anchor.getOffset() + anchor.getLength();
			boolean overlapped = false;
			
			for (;count < (anchorList.size() - 1); ++count) {
				
				next = anchorList.get(count);
				nextEnd = (next.getOffset() + next.getLength());
				
				if (anchor.getNext() != null && anchor.getNext() == next) {
						if (nextEnd > offsetEnd)
							offsetEnd = nextEnd;
				}
				else {				
//					if ((anchor.getNext() != null && (anchor.getNext() != next)))
//						System.err.println(anchor.toString() + " has incorrect connection with next one");
					
					if (nextEnd <= offsetEnd || isOverlapped(anchor, next)) { // overlapped
						if ((anchor.getNext() != null && (anchor.getNext() != next))) {
							System.err.println(anchor.toString() + ", has incorrect connection with next one");
							connectAfter(anchor.getNext(), next);
							nextEnd = (anchor.getNext().getOffset() + anchor.getNext().getLength());
						}
						connectBefore(anchor, next);
						
						if (nextEnd > offsetEnd)
							offsetEnd = nextEnd;
					}
					else {
						offsetEnd = nextEnd;
						if ((anchor.getNext() != null && (anchor.getNext() != next))) {
							System.err.println(anchor.toString() + " should not be linked with next one " + next.toString());
							anchor.getNext().setPrevious(null);
							anchor.setNext(null);
						}
					}
				}
				
				anchor = next;
			}
		}
	}
	
	public void calculateOverlappedAnchorExtensionLength() {
		if (anchorList.size() > 0) {
			int count = 0;
			Anchor anchor = null;
			Anchor next = null;
			Anchor pre = null;
			int offsetEnd; // = offset + length;
			while (count < anchorList.size()) {
				anchor = anchorList.get(count);
				
				next = anchorList.get(count + 1);
				offsetEnd = anchor.getOffset() + anchor.getLength();
				if (anchor.getNext() != null) {
					assert(anchor.getNext() == next);
					while (next != null) {
						++count;
						int extension  = anchor.getOffset() + anchor.getLength() + anchor.getExtendedLength();
						int nextEnd = (next.getOffset() + next.getLength());
						if (extension < nextEnd)
							anchor.setExtendedLength(nextEnd - extension);
							
						if (next.getNext() != next)
							next = next.getNext();
						else {
							System.out.println(next.toString());
							break;
						}
					}
				}
				else {

				}
				
				++count;
			}
		}
	}
}

//if (first == null/* || theOne.getLast() == null*/) {
//first = theOne;
//theOne.setFirst(first);
//anchor.setFirst(first);
//theOne.setLast(anchor);
//anchor.setLast(anchor);
//}
//else {
//anchor.setFirst(first);
//anchor.setLast(last);
//}
//}
//else { //add after


//if (first == null/* || theOne.getLast() == null*/) {
//first = theOne;
//theOne.setFirst(first);
//anchor.setFirst(first);
//theOne.setLast(anchor);
//anchor.setLast(anchor);
//}
//else {
//anchor.setFirst(first);
//anchor.setLast(last);
//}
//}

//if ()
