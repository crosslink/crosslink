package crosslink;

import java.util.Collections;
import java.util.HashSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Set;

public class CrosslinkTopic extends WikiArticleXml {
	
	// the existing or recommended links in the topics
	private Set<String> links = Collections.synchronizedSet(new HashSet<String>());
//	private Set<String> groundTruthDirectLinks = Collections.synchronizedSet(new HashSet<String>());
	
	private String lang;
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

	/**
	 * @return the links
	 */
	public Set<String> getLinks() {
		return links;
	}
	
	public void addLink(String id) {
		links.add(id);
	}
}
