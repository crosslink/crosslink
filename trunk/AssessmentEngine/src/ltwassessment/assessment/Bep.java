package ltwassessment.assessment;

public class Bep {
	private int offset;
	private int startP;
	
	private String fileId;
	
	private int rel;
	
	private String targetLang;
	private String targetTitle;
	
	public Bep(int offset, int startP, String tbFileID, int rel) {
		this.offset = offset;
		this.startP = startP;
		this.fileId = tbFileID;
		this.rel = rel;
	}
	
	public Bep(String tbFileID, int tbOffset, String target_lang,
			String target_title) {
		this.fileId = tbFileID;
		this.offset = tbOffset;
		this.targetLang = target_lang;
		this.targetTitle = target_title;
	}
	
	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getStartP() {
		return startP;
	}

	public void setStartP(int startP) {
		this.startP = startP;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public int getRel() {
		return rel;
	}

	public void setRel(int rel) {
		this.rel = rel;
	}

	public String getTargetLang() {
		return targetLang;
	}

	public void setTargetLang(String targetLang) {
		this.targetLang = targetLang;
	}

	public String getTargetTitle() {
		return targetTitle;
	}

	public void setTargetTitle(String targetTitle) {
		this.targetTitle = targetTitle;
	}

}
