package ltwassessment.parsers;

public class SubmissionFormat {
	public static final String OUTGOING_TAG_NAME = "outgoing";
	public static final String OUTGOINGLINKS_TAG_NAME = "outgoinglinks";
	
	public static final String afTopicTag = "topic";
	public static final String afOutgoingTag = "outgoing";
    public static final String afAnchorTag = "anchor";
    public static final String afSubAnchorTag = "subanchor";
    public static final String afToBepTag = "tofile";
    public static final String offsetAttributeName = "offset";
    public static final String lengthAttributeName = "length";
    public static final String tboffsetAttributeName = "bep_offset";
    
	private SubmissionFormat instance = null;
	
	
	public SubmissionFormat() {

	}
	
	public static String getOutgoingTag() {
		return OUTGOING_TAG_NAME;
	}
	
	public static String getOutgoingLinksTag() {
		return OUTGOINGLINKS_TAG_NAME;
	}
	
	public static String getAftopictag() {
		return afTopicTag;
	}

	public static String getAfoutgoingtag() {
		return afOutgoingTag;
	}

	public static String getAfanchortag() {
		return afAnchorTag;
	}

	public static String getAfsubanchortag() {
		return afSubAnchorTag;
	}

	public static String getAftobeptag() {
		return afToBepTag;
	}

	public static String getOffsetattributename() {
		return offsetAttributeName;
	}

	public static String getLengthattributename() {
		return lengthAttributeName;
	}

	public static String getTboffsetattributename() {
		return tboffsetAttributeName;
	}

	public SubmissionFormat getInstance() {
		if (instance == null)
			instance = new SubmissionFormat();
		return instance;
	}
}
