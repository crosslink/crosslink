package ltwassessment.submission;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import ltwassessment.AppResource;
import ltwassessment.wiki.WikiArticleXml;


public class Topic extends WikiArticleXml {

	private boolean valid = false;
		
	private LinkedAnchorList anchors = null;
	
	public Topic(String id, String name) {
		super(id, name);
	}
//	
//	public Topic(String id, LinkedAnchorList anchors) {
//		this.id = id;
//		this.anchors = anchors;
//	}

	public Topic(String id) {
		super(id);
		
		load();
	}

	public void load() {
		if (bytes == null) {
			xmlFile = AppResource.getInstance().getTopicXmlPathNameByFileID(id);
			read();
			extractTitle();
		}
	}
	
	public boolean validateIt() {
		load();
		
		valid = anchors.validateAll(this);
		
		return valid;
	}

	public boolean isValid() {
		return valid;
	}
	
	
	public String getAnchor(int offset, int length) throws UnsupportedEncodingException {
		byte[] result = new byte[length];
		System.arraycopy(bytes, offset, result, 0, length);
		return new String(result, "UTF-8");
	}

	public LinkedAnchorList getAnchors() {
		if (anchors == null)
			 anchors = new LinkedAnchorList();
		return anchors;
	}
}
