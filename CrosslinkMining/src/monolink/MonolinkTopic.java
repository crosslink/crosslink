package monolink;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ltwassessment.wiki.WikiArticleXml;

public class MonolinkTopic extends WikiArticleXml {
	// the existing or recommended links in the topics
	protected Set<String> links = Collections.synchronizedSet(new HashSet<String>());
//	protected Set<String> groundTruthDirectLinks = Collections.synchronizedSet(new HashSet<String>());
	
	protected String lang;


	/**
	 * @return the links
	 */
	public Set<String> getLinks() {
		return links;
	}
	
	public void addLink(String id) {
		links.add(id);
	}
	
	public MonolinkTopic(String xmlFile) {
		super(xmlFile);
	}
}
