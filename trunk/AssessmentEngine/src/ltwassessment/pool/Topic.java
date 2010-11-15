package ltwassessment.pool;

public class Topic {

	private String id = null;
	private String filePath = null;
	
	private LinkedAnchorList anchors = null;
	
	public Topic(String id, String filePath) {
		super();
		this.id = id;
		this.filePath = filePath;
	}
}
