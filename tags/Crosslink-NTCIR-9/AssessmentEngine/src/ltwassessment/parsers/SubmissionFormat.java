package ltwassessment.parsers;

import ltwassessment.AppResource;

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
	
	public static String getRootTag() {
		return AppResource.forAssessment ? "crosslink-assessment" : "crosslink-submission";
	}
	
	public static String getOutgoingTag() {
		return AppResource.forAssessment ? "outgoinglinks" : "outgoing";
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
		return AppResource.forAssessment ? "tobep" : afToBepTag;
	}

	public static String getOffsetAttributeName() {
		return AppResource.forAssessment ? "aoffset" : "offset";
	}

	public static String getLengthAttributeName() {
		return AppResource.forAssessment ? "alength" : "length";
	}

	public static String getTboffsetAttributeName() {
		return AppResource.forAssessment ? "tboffset" : "bep_offset";
	}

	public SubmissionFormat getInstance() {
		if (instance == null)
			instance = new SubmissionFormat();
		return instance;
	}
}
