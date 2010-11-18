package ltwassessment.pool;

public class ToXml {

	public static String anchorToXml(Anchor anchor) {
		StringBuffer xmlText = new StringBuffer();
		
		return anchorToXml(anchor, xmlText);
	}
	
	public static String anchorToXml(Anchor anchor, StringBuffer xmlText) {

		
		return xmlText.toString();
	}
	
	public static String targetToXml(Target target) {
		StringBuffer xmlText = new StringBuffer();
		targetToXml(target, xmlText);
		return xmlText.toString();
	}
	
	public static void targetToXml(Target target, StringBuffer xmlText) {

		
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
	}
}
