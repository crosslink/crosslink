package ltwassessment.submission;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import ltwassessment.AppResource;


public class Topic {
	private String id = null;
	private String name = null;
	
	private String filePath = null;
	private boolean valid = false;
	private byte[] bytes = null; // the topic text in bytes
	
	private LinkedAnchorList anchors = null;
	
	public Topic(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	
	public Topic(String id, LinkedAnchorList anchors) {
		super();
		this.id = id;
		this.anchors = anchors;
	}

	public Topic(String idD) {
		this.id = id;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getId() {
		return id;
	}

	public String getFilePath() {
		return filePath;
	}
	
	public boolean validateIt() {
		if (bytes == null) {
			filePath = AppResource.getInstance().getTopicXmlPathNameByFileID(id);
			readTopicText();
		}
		
		valid = anchors.validateAll(this);
		
		return valid;
	}

	public boolean isValid() {
		return valid;
	}
	
	public void readTopicText() {
		int size = 0;
//	    
		try {
			FileInputStream fis = new FileInputStream(filePath);
			size = fis.available();
		    bytes = new byte[size];
		    fis.read(bytes, 0, size);
		} catch (IOException e) {
			e.printStackTrace();
		}
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