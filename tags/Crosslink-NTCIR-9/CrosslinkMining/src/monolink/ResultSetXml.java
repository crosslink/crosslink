package monolink;

public class ResultSetXml extends OutputXml implements OutputXmlInterface {
	
	public static final String START = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
										"<ltwResultsetType>\n";
	public static final String END = "</ltwResultsetType>";
	
	public static final String TOPIC_START = "\t<ltw_Topic name=\"%s\" id=\"%s\"> \n" + 
											"\t\t<outgoingLinks>\n";
	public static final String TOPIC_END =	"\t\t</outgoingLinks>\n" + 
											"\t</ltw_Topic>\n";
	public static final String LINK = "\t\t\t<outLink>%s</outLink>\n";
	public static final String LINK2 = "\t\t\t<outLink aname=\"\" aoffset=\"%s\" alength=\"%d\" boffset=\"%d\">%s</outLink>\n";
	
	public ResultSetXml(String sourceLang, String targetLang) {
		super(sourceLang, targetLang);
	}
	
	public void outputTopicStart(String name, String id) {
		String temp = String.format(TOPIC_START, name, id);
		xmlbuf.append(temp);
	}
	
	public void outputLink(String targetId) {
		String temp = String.format(LINK, targetId);
		xmlbuf.append(temp);
	}
	
//	public void outputLink(String id, String nameOffset, int length, int bep) {
//		outputLink(id);
//	}
	
	public void outputTopicEnd() {
		xmlbuf.append(TOPIC_END);
	}

	public void close() {
		xmlbuf.append(END);
	}

	@Override
	public void open() {
		xmlbuf.append(START);	
	}

	@Override
	public void outputAnchorStart(String anchor, int pos, int length) {
		
	}

	@Override
	public void outputAnchorEnd() {
		
	}
}