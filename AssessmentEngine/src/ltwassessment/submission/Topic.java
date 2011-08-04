package ltwassessment.submission;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import ltwassessment.AppResource;
import ltwassessment.wiki.WikiArticleXml;


public class Topic extends WikiArticleXml {

	private boolean valid = false;
		
	private LinkedAnchors anchors = null;
		
	public Topic(String id, String name) {
		super(id, name);
		
		load();
	}
//	
//	public Topic(String id, LinkedAnchors anchors) {
//		this.id = id;
//		this.anchors = anchors;
//	}

	public Topic(File file) {
		super(file);
		
		load();
	}

	public void load() {
		if (bytes == null) {
			xmlFile = AppResource.getInstance().getTopicXmlPathNameByFileID(id);
			if (new File(xmlFile).exists()) {
				read();
				extractTitle();
			}
		}
	}
	
	public boolean validateIt(int showMessage, boolean convertToTextOffset) {
		load();
		
		valid = anchors.validateAll(this, showMessage, convertToTextOffset);
		
		return valid;
	}

	public boolean isValid() {
		return valid;
	}
	
	
	public String getAnchor(int offset, int length) throws UnsupportedEncodingException {
		if ((offset + length) > bytes.length)
			return "EXCEEDED TOPIC LENGTH";
		byte[] result = new byte[length];
		System.arraycopy(bytes, offset, result, 0, length);
		return new String(result, "UTF-8");
	}
	
	public String getAnchorWithCharacterOffset(int offset, int length) {
		return fullText.substring(offset, offset + length);
	}

	public LinkedAnchors getAnchors() {
		if (anchors == null)
			 anchors = new LinkedAnchors();
		return anchors;
	}
}
