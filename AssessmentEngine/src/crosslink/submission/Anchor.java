package crosslink.submission;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import crosslink.XML2TXT;
import crosslink.parsers.FOLTXTMatcher;
import crosslink.submission.Anchor;
import crosslink.submission.Target;
import crosslink.submission.Topic;
import crosslink.utility.XML;


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
	
	private Topic parent = null;
	
//	// for the overlapping Anchors
//	protected Anchor first = null;
//	protected Anchor last = null;
//	protected Anchor next = null;
//	protected Anchor previous = null;

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

//	public Anchor getFirst() {
//		return first;
//	}
//
//	public void setFirst(Anchor first) {
//		this.first = first;
//	}
//
//	public Anchor getLast() {
//		return last;
//	}
//
//	public void setLast(Anchor last) {
//		this.last = last;
//	}
//	
//	public Anchor getNext() {
//		return next;
//	}
//
//	public void setNext(Anchor next) {
//		this.next = next;
//	}
//
//	public Anchor getPrevious() {
//		return previous;
//	}
//
//	public void setPrevious(Anchor previous) {
//		this.previous = previous;
//	}

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

	public Topic getAssociatedTopic() {
		return parent;
	}

	public void setAssociatedTopic(Topic parent) {
		this.parent = parent;
	}

	public boolean validate(Topic topic, int showMessage, boolean convertToTextOffset) {
		String result = null;
		String reform = null;
		try {
			if (convertToTextOffset)
				result = topic.getAnchorWithCharacterOffset(offset, length);
			else
				result = topic.getAnchor(offset, length);
			
			reform = result;
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		valid = reform.equals(name);
		
//		if (name.equals("Standard &amp; Poor's"))
//			System.err.println("I got you");
//		
		if (!valid) {
			if (reform.contains("<") || reform.contains(">"))
				reform = XML2TXT.getInstance().cleanTag(reform);
			if (reform.contains("&")) {
				reform = XML.unXMLify(reform);
//				reform = XML2TXT.getInstance().cleanTag(FOLTXTMatcher.parseXmlText(reform));
			}
			
			valid = reform.equalsIgnoreCase(name);
			if (!valid) {
				if (!reform.contains("\n")) {
					String secondReform = reform.replaceAll("\\s", "");
					String secondName = name.replaceAll("\\s", "");
					valid = secondReform.equalsIgnoreCase(secondName);
				}
			}
		}
		
		if (valid && !name.equals(reform))
			name = reform;
		
		PrintStream out = null;
		StringBuffer message = null;
		if (showMessage > SHOW_MESSAGE_NONE) {
			message = new StringBuffer(String.format(ANCHOR_CHECK_MESSAGE, topic.getTitle(), topic.getId(), name, this.getOffset(), this.getLength()));
			if (valid) {
				if ((SHOW_MESSAGE_OK & showMessage) == SHOW_MESSAGE_OK) {
					message.append(ANCHOR_CHECK_STATUS_OK);
					out = System.out;
//					System.out.println(message);
				}
			}
			else {
				if ((SHOW_MESSAGE_ERROR & showMessage) == SHOW_MESSAGE_ERROR) {
		 			message.append(ANCHOR_CHECK_STATUS_ERROR);
		 			message.append(". Got \"" + result + "\" from topic instead");
		 			out = System.err;
//		 			System.err.println(message);
				}
			}
		}
		if (out != null) {
			try {
				PrintStream ps = new PrintStream(out, true, "UTF-8");
				ps.println(message);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		return valid;			// TODO Auto-generated method stub
	}

	@Override
	public String toString() {
		return "Anchor: " + getName() + ", Offset: " + getOffset();
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

	public int compareTo(Anchor anchor2) {
		if (this.getOffset() < anchor2.getOffset())
			return -1;
		if (this.getOffset() == anchor2.getOffset()) {
			if (this.getLength() < anchor2.getLength())
				return -1;
			return 1;
		}
		return 1;
	}

	public String toXml() {
		StringBuffer sb = new StringBuffer();
		String openTag = "\t\t\t<anchor offset=\"%d\" length=\"%d\" name=\"%s\">\n";
		sb.append(String.format(openTag, this.offset, this.length, XML.XMLify(this.name)));
		for (Target target : targets.values()) {
			sb.append(target.toXml());
		}
		String closeTag = "\t\t\t</anchor>\n";
		sb.append(closeTag);
		return sb.toString();
	}
}
