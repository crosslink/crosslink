package ltwassessment.pool;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import de.mpii.clix.support.XML;

import ltwassessment.AppResource;
import ltwassessment.submission.Anchor;
import ltwassessment.submission.Target;
import ltwassessment.submission.Topic;

public class ToXml {
	
	public static final String ASSESSMENT_TAG = "<crosslink-assessment participant-id=\"2273087\" run-id=\"2273087_A2B\" task=\"A2B\" default_lang=\"%s\" source_lang=\"%s\">\n" +
	           "	<collection>Chinese Wikipedia 2010 Collection</collection>\n" +
	           "	<collection>Japanese Wikipedia 2010 Collection</collection>\n" +
	           "	<collection>Korean Wikipedia 2010 Collection</collection>\n";

	public static String anchorToXml(Anchor anchor) {
		StringBuffer xmlText = new StringBuffer();
		
		anchorToXml(anchor, xmlText);
		return xmlText.toString();
	}
	
	public static void anchorToXml(Anchor anchor, StringBuffer xmlText) {
		String anchorElementStart = "\t\t\t<anchor arel=\"0\" aname=\"%s\" aoffset=\"%d\" alength=\"%d\" ext_length=\"%d\">\n";
//		String anchorElementEnd = "\t\t\t\t\t</subanchor>\n";
		
		xmlText.append(String.format(anchorElementStart, anchor.getName(), anchor.getOffset(), anchor.getLength(), anchor.getExtendedLength()));
		
//		for (Target target : anchor.getTargets())
//			targetToXml(target, xmlText);
//		xmlText.append(anchorElementEnd);
	}
	
	
	public static void subAnchorToXml(Anchor anchor, StringBuffer xmlText) {
		String anchorElementStart = "\t\t\t\t<subanchor sarel=\"0\" saname=\"%s\" saoffset=\"%d\" salength=\"%d\" rank=\"%d\">\n";
		String anchorElementEnd = "\t\t\t\t</subanchor>\n";
		
		xmlText.append(String.format(anchorElementStart, anchor.getName(), anchor.getOffset(), anchor.getLength(), anchor.getRank()));
		
		Set set = anchor.getTargets().entrySet();
		Iterator i = set.iterator();

	    while(i.hasNext()) {
	    	Map.Entry me = (Map.Entry)i.next();
	    	Target target = (Target)me.getValue();
//	    	if (target.exists())
	    		targetToXml(target, xmlText);
	    }
//		for (Target target : anchor.getTargets())
//			targetToXml(target, xmlText);
		xmlText.append(anchorElementEnd);
	}	
	
	public static String targetToXml(Target target) {
		StringBuffer xmlText = new StringBuffer();
		targetToXml(target, xmlText);
		return xmlText.toString();
	}
	
	public static void targetToXml(Target target, StringBuffer xmlText) {
		String targetElement = "\t\t\t\t\t<tobep tbrel=\"0\" timein=\"\" timeout=\"\" tboffset=\"%d\" tbstartp=\"0\" lang=\"%s\" title=\"%s\">%s</tobep>\n";
		
		xmlText.append(String.format(targetElement, target.getBepOffset(), target.getLang(), XML.XMLify(target.getTitle()), target.getId()));
	}
	
	public static void startRootElement(StringBuffer xmlText, String sourceLang, String targetLang) {
		String tagAssessment = String.format(ASSESSMENT_TAG, targetLang, sourceLang);
		xmlText.append(tagAssessment);
	}
	
	public static void endRootElement(StringBuffer xmlText) {
		xmlText.append("</crosslink-assessment>\n");
	}
	
	public static void topicToXml(Topic topic, StringBuffer xmlText) throws Exception {
		String topicElemStart = "\t<topic file=\"%s\" name=\"%s\">\n" + 
        						"\t\t<outgoinglinks>\n";
		String topicElemEnd = "\t\t</outgoinglinks>\n\t</topic>\n";
		xmlText.append(String.format(topicElemStart, topic.getId(), topic.getTitle()));
		
		int offsetSofar = 0;
		
//		topic.getAnchors().calculateOverlappedAnchorExtensionLength();
//		topic.getAnchors().connectIfOverlapped();
		LinkedList<Anchor> anchorList = topic.getAnchors(); //.getAnchorList();
		
		String anchorElementEnd = "\t\t\t</anchor>\n";
		
		Anchor start = anchorList.get(0);
		Anchor pre = start, anchor = null;
		offsetSofar = start.getOffset();
//		anchorToXml(pre, xmlText);
		StringBuffer subanchorXmlText = new StringBuffer();
		int offsetEnd = start.getOffset() + start.getLength(); // = offset + length;
		int nextEnd = 0;
		
		for (int i = 1; i < anchorList.size(); ++i) {
			anchor = anchorList.get(i);
			if (anchor.getTargets().size() == 0)
				continue;
			nextEnd = anchor.getOffset() + anchor.getLength();
			
//			if (anchor.getName().equals("Holy"))
//				System.err.println("Stop here");
			
			if (anchor.getOffset() < offsetSofar) {
				System.err.println("Topic id:" + topic.getId() + " " + anchor.toString());
//				throw new Exception("Incorrect anch
			}
			subAnchorToXml(pre, subanchorXmlText);
//			if (pre.getNext() != null && pre.getNext() == anchor) {
			if (anchor.getOffset() < offsetEnd/* || anchor.isOverlapped(pre)*/) {		
				if (nextEnd > offsetEnd)
					offsetEnd = nextEnd;
			}
			else {
				start.setExtendedLength(offsetEnd - start.getOffset() - start.getLength());
				anchorToXml(start, xmlText);
				xmlText.append(subanchorXmlText);
				xmlText.append(anchorElementEnd);
				
//				if (i == (anchorList.size() - 1))
//					subAnchorToXml(anchor, xmlText);
				subanchorXmlText.setLength(0);
				start = anchor;
				offsetEnd = start.getOffset() + start.getLength();
			}
			pre = anchor;
			offsetSofar = pre.getOffset();
		}
		subAnchorToXml(anchor, subanchorXmlText);
		nextEnd = anchor.getOffset() + anchor.getLength();
		if (nextEnd > offsetEnd)
			offsetEnd = nextEnd;
		start.setExtendedLength(offsetEnd - start.getOffset() - start.getLength());
		
		anchorToXml(start, xmlText);
		xmlText.append(subanchorXmlText);
		xmlText.append(anchorElementEnd);
		
		xmlText.append(topicElemEnd);
	}
}