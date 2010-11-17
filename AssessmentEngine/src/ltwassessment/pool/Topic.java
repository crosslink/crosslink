package ltwassessment.pool;

import java.io.FileInputStream;
import java.io.IOException;

public class Topic {
	private String id = null;
	private String filePath = null;
	private boolean valid = false;
	private byte[] bytes = null; // the topic text in bytes
	private LinkedAnchorList anchors = null;
	
	public Topic(String id, String filePath) {
		super();
		this.id = id;
		this.filePath = filePath;
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

	public String getId() {
		return id;
	}

	public String getFilePath() {
		return filePath;
	}
	
	public boolean validateIt() {
		valid = anchors.validateAll(this);
		
		return valid;
	}

	public boolean isValid() {
		return valid;
	}
	
	public void readTopicText() {
		int size = 0;
	    byte[] bytes = null;
		try {
			FileInputStream fis = new FileInputStream(filePath);
			size = fis.available();
		    bytes    = new byte[size];
		    fis.read(bytes, 0, size);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean matchAnchor(int offset, int length, String name) {
		byte[] result = new byte[length];
		System.arraycopy(bytes, offset, result, offset, length);
		return new String(result).equals(name);
	}

	public LinkedAnchorList getAnchors() {
		if (anchors == null)
			 anchors = new LinkedAnchorList();
		return anchors;
	}
}
