package crosslink.view;

public class Link {
	private int bepOffset;
	private String id;
	private String lang;
	private String title;
	private String anchorName;
	private int startPos;
	private String status;
	
	public int getBepOffset() {
		return bepOffset;
	}
	
	public void setBepOffset(int bepOffset) {
		this.bepOffset = bepOffset;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getLang() {
		return lang;
	}
	
	public void setLang(String lang) {
		this.lang = lang;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getAnchorName() {
		return anchorName;
	}
	
	public void setAnchorName(String anchorName) {
		this.anchorName = anchorName;
	}
	
	public int getStartPos() {
		return startPos;
	}
	
	public void setStartPos(int startPos) {
		this.startPos = startPos;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}
	
	
}
