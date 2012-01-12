package monolink;

public class SubmissionXml {
	public static final String START = "<crosslink-submission participant-id=\"GT\" run-id=\"GroundTruth\" task=\"F2F\" default_lang=\"%s\" source_lang=\"%s\">" +
	"<details>\n " +
		"<machine>\n" +
		"	<cpu>Core 2 Duo</cpu>\n" +
		"	<speed>2.33 MHz</speed>\n" +
		"	<cores>2</cores>\n" +
		"	<hyperthreads>1</hyperthreads>\n" +
		"	<memory>8GB</memory>\n" +
		"</machine>\n" +
		"<time>TBC</time>\n" +
	"</details>\n" +
	"<description>\n" + 
		"This is the ground truth run\n" +
	"</description>\n" +
	"<collections><collection>CJK Wikipedia</collection></collections>\n";
	
	
	public static final String END = "</crosslink-submission>";
	
	public static final String TOPIC_START = "\t<topic file=\"%s\" name=\"%s\"> \n" + 
			"\t\t<outgoing>\n" +
			"<anchor offset=\"-1\" length=\"0\" name=\"\">";
	
	public static final String TOPIC_END =	"\t\t\t</anchor>" +
				"\t\t</outgoing>\n" + 
				"\t</topic>\n";
	
	public static final String LINK = "\t\t\t<tofile>%s</tofile>\n";
	
	public static final String LINK2 = "\t\t\t<tofile aname=\"\" aoffset=\"%s\" alength=\"%d\" boffset=\"%d\">%s</tofile>\n";
	
}
