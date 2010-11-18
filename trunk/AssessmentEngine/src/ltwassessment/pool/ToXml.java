package ltwassessment.pool;

import java.util.LinkedList;

public class ToXml {

	public static String anchorToXml(Anchor anchor) {
		StringBuffer xmlText = new StringBuffer();
		
		anchorToXml(anchor, xmlText);
		return xmlText.toString();
	}
	
	public static void anchorToXml(Anchor anchor, StringBuffer xmlText) {
		String anchorElementStart = "\t\t\t\t<anchor arel=\"-1\" aname=\"%s\" aoffset=\"%d\" alength=\"%d\">\n";
//		String anchorElementEnd = "\t\t\t\t\t</subanchor>\n";
		
		xmlText.append(String.format(anchorElementStart, anchor.getName(), anchor.getOffset(), anchor.getLength()));
		
//		for (Target target : anchor.getTargets())
//			targetToXml(target, xmlText);
//		xmlText.append(anchorElementEnd);
	}
	
	
	public static void subAnchorToXml(Anchor anchor, StringBuffer xmlText) {
		String anchorElementStart = "\t\t\t\t\t<subanchor arel=\"-1\" aname=\"%s\" aoffset=\"%d\" alength=\"%d\">\n";
		String anchorElementEnd = "\t\t\t\t\t</subanchor>\n";
		
		xmlText.append(String.format(anchorElementStart, anchor.getName(), anchor.getOffset(), anchor.getLength()));
		
		for (Target target : anchor.getTargets())
			targetToXml(target, xmlText);
		xmlText.append(anchorElementEnd);
	}	
	
	public static String targetToXml(Target target) {
		StringBuffer xmlText = new StringBuffer();
		targetToXml(target, xmlText);
		return xmlText.toString();
	}
	
	public static void targetToXml(Target target, StringBuffer xmlText) {
		String targetElement = "\t\t\t\t\t\t<tobep tbrel=\"0\" timein=\"\" timeout=\"\" tboffset=\"%d\" tbstartp=\"-1\" lang=\"%s\" title=\"%s\">%s</tobep>\n";
		
		xmlText.append(String.format(targetElement, target.getBepOffset(), target.getLang(), target.getTitle(), target.getId()));
	}
	
	public static void startRootElement(StringBuffer xmlText) {
		xmlText.append("<crosslink-assessment participant-id=\"2273087\" run-id=\"2273087_A2B\" task=\"A2B\">\n" +
			           "	<collection>Wikipedia_2009_Collection</collection>");
		
	}
	
	public static void endRootElement(StringBuffer xmlText) {
		xmlText.append("</inexltw-assessment>\n");
	}
	
	public static void topicToXml(Topic topic, StringBuffer xmlText) {
		String topicElemStart = "\t\t<topic file=\"%s\" name=\"%s\">\n" + 
        						"\t\t\t<outgoinglinks>\n";
		String topicElemEnd = "\t\t\t</outgoinglinks>\n\t\t</topic>\n";
		xmlText.append(String.format(topicElemStart, topic.getId(), topic.getName()));
		
		LinkedList<Anchor> anchorList = topic.getAnchors().getAnchorList();
		
		String anchorElementEnd = "\t\t\t\t</anchor>\n";
		Anchor pre = null, anchor = null;
		pre = anchorList.get(0);
		anchorToXml(pre, xmlText);
		for (int i = 1; i < anchorList.size(); ++i) {
			anchor = anchorList.get(i);
			subAnchorToXml(anchor, xmlText);
			if (pre.getNext() != null && pre.getNext() == anchor)
				;
			else {
				xmlText.append(anchorElementEnd);
			}
			pre = anchor;
		}
		xmlText.append(anchorElementEnd);
		
		xmlText.append(topicElemEnd);
	}
}
