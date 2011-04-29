package crosslink;

import java.util.Collections;
import java.util.HashSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Set;

import monolink.MonolinkTopic;

public class CrosslinkTopic extends MonolinkTopic {
	
	private String targetLang;
	
	private CrosslinkTopic counterPart;

	public CrosslinkTopic(String xmlFile) {
		super(xmlFile);
	}

	public CrosslinkTopic(String xmlFile, String counterPartXml) {
		super(xmlFile);
		
		counterPart = new CrosslinkTopic(counterPartXml);
	}
	
	/**
	 * @return the counterPart
	 */
	public CrosslinkTopic getCounterPart() {
		return counterPart;
	}
	
	public void setCounterTopic(CrosslinkTopic topic) {
		counterPart = topic;
	}
}
