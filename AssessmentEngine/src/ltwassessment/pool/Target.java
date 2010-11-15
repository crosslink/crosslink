package ltwassessment.pool;

public class Target {
	private String lang = "";
	private String title = "";
	private String id = "";
	private int bepOffset;
	
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
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public int getBepOffset() {
		return bepOffset;
	}
	
	public void setBepOffset(int bepOffset) {
		this.bepOffset = bepOffset;
	}
}
