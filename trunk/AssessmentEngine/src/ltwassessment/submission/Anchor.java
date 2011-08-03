package ltwassessment.submission;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import crosslink.XML2TXT;
import de.mpii.clix.support.XML;

import ltwassessment.parsers.FOLTXTMatcher;


public class Anchor {
	
	public static final String ANCHOR_CHECK_MESSAGE = "Topic: %s (%s), anchor: %s (%d:%d): ";
	public static final String ANCHOR_CHECK_STATUS_OK = "ok";
	public static final String ANCHOR_CHECK_STATUS_ERROR = "error";
	
	public static final int SHOW_MESSAGE_NONE = 0;
	public static final int SHOW_MESSAGE_OK = 2;
	public static final int SHOW_MESSAGE_ERROR = 4;
	
	protected int offset = 0;
	protected int length = 0;
	protected int extendedLength = 0; // overlapping
	protected String name = "";
	protected int rank = 0;
//	protected int over
	
	protected boolean valid = false;
	
	// for the overlapping Anchors
	protected Anchor first = null;
	protected Anchor last = null;
	protected Anchor next = null;
	protected Anchor previous = null;

//	protected Vector<Target> targets = null;
	protected Map<String, Target>	targets = null;
	
//	public Anchor(Anchor anchor) {
//		this.offset = anchor.getOffset();
//		this.length = anchor.getLength();
//		this = anchor;
//		this = anchor;
//		this = anchor;
//	}
	
	public Anchor(int offset, int length, String name) {
		super();
		this.offset = offset;
		this.length = length;
		this.name = name;
		
//		targets = new Vector<Target>();
		targets = new HashMap<String, Target>();
	}
	
	public int getExtendedLength() {
		return extendedLength;
	}

	public void setExtendedLength(int extendedLength) {
		this.extendedLength = extendedLength;
	}

	public Anchor getFirst() {
		return first;
	}

	public void setFirst(Anchor first) {
		this.first = first;
	}

	public Anchor getLast() {
		return last;
	}

	public void setLast(Anchor last) {
		this.last = last;
	}
	
	public Anchor getNext() {
		return next;
	}

	public void setNext(Anchor next) {
		this.next = next;
	}

	public Anchor getPrevious() {
		return previous;
	}

	public void setPrevious(Anchor previous) {
		this.previous = previous;
	}

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
	
	public Map<String, Target> getTargets() {
		return targets;
	}
	
	public void addTargets(Map<String, Target> many) {
		Set<String> entry = many.keySet();
		Iterator it = entry.iterator();
		while (it.hasNext()) {
			String id = (String) it.next();
			if (!targets.containsKey(id))
				targets.put(id, many.get(id));
		}
	}
	
	public void insertTarget(Target target) {
		if (!targets.containsKey((target.getId()))) 
			targets.put(target.getId(), target);
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
	
//	public boolean validate(Topic topic) {
//		valid = false;
//		boolean result = true;
//		Anchor cur = first;
//		while (cur != null && cur != last) {		
//			if (!cur.validateIt(topic));
//				result = false;
//			cur = cur.getFirst();
//		}
//		result = last.validateIt(topic);
//
//		return valid;
//	}
	
//	public boolean matchAnchor(int offset, int length, String name) {
//
//	}

	public boolean validate(Topic topic, int showMessage, boolean convertToTextOffset) {
		String result = null;
		try {
			if (convertToTextOffset)
				result = topic.getAnchorWithCharacterOffset(offset, length);
			else
				result = topic.getAnchor(offset, length);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		valid = result.equals(name);
		
		if (!valid) {
			String reform = result;
			if (reform.contains("<") || reform.contains(">"))
				reform = XML2TXT.getInstance().cleanTag(reform);
			if (reform.contains("&")) {
				reform = XML.unXMLify(reform);
//				reform = XML2TXT.getInstance().cleanTag(FOLTXTMatcher.parseXmlText(reform));
			}
			valid = reform.equalsIgnoreCase(name);
			if (!valid) {
				String secondReform = reform.replaceAll("\\s", "");
				String secondName = name.replaceAll("\\s", "");
				valid = secondReform.equalsIgnoreCase(secondName);
			}
		}
		
		if (showMessage > SHOW_MESSAGE_NONE) {
			StringBuffer message = new StringBuffer(String.format(ANCHOR_CHECK_MESSAGE, topic.getTitle(), topic.getId(), name, this.getOffset(), this.getLength()));
			if (valid) {
				if ((SHOW_MESSAGE_OK & showMessage) == SHOW_MESSAGE_OK) {
					message.append(ANCHOR_CHECK_STATUS_OK);
					System.out.println(message);
				}
			}
			else {
				if ((SHOW_MESSAGE_ERROR & showMessage) == SHOW_MESSAGE_ERROR) {
		 			message.append(ANCHOR_CHECK_STATUS_ERROR);
		 			message.append(". Got \"" + result + "\" from topic instead");
		 			System.err.println(message);
				}
			}
		}
		
		return valid;
	}

	@Override
	public String toString() {
		return "Anchor: " + getName() + " Offset: " + getOffset();
	}
	
	public String offsetToString() {
		return String.valueOf(offset);
	}
	

	public String lengthToString() {
		return String.valueOf(length);
	}
	

	public String extendedLengthToString() {
		return String.valueOf(extendedLength);
	}
}
