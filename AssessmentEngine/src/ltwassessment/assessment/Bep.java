package ltwassessment.assessment;

import java.util.Observable;

public class Bep extends Observable {
	public static final int IRRELEVANT = -1;
	public static final int RELEVANT = 1;
	public static final int UNASSESSED = 0;
	
	public static final int MARK_INITIALISED = 0;
	public static final int MARK_ASSESSED = 1;
	public static final int MARK_UNASSESSED = 2;
	
	private int offset;
	private int startP;
	
	private String fileId;
	
	private int rel;
	
	private String targetLang;
	private String targetTitle;
	
	private int index;
	
	AssessedAnchor associatedAnchor = null;
	
	public Bep(int offset, int startP, String tbFileID, int rel) {
		init();
		
		this.offset = offset;
		this.startP = startP;
		this.fileId = tbFileID;
		this.rel = rel;
	}
	
	public Bep(String offset, String startP, String tbFileID, String rel) {
		init();
		
		this.offset = Integer.parseInt(offset);
		this.startP = Integer.parseInt(startP);
		this.fileId = tbFileID;
		this.rel = Integer.parseInt(rel);
	}
	
	public Bep(String tbFileID, int tbOffset, String target_lang,
			String target_title) {
		init();
		
		this.fileId = tbFileID;
		this.offset = tbOffset;
		this.targetLang = target_lang;
		this.targetTitle = target_title;
	}
	
	public Bep() {
		init();
	}
	
	private void init() {
		Completion.getInstance().oneMoreLinkForAssessment();
		this.addObserver(Completion.getInstance());
		rel = UNASSESSED;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public void setOffset(String offset) {
		this.offset = Integer.parseInt(offset);
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

	public void initRel(int rel) {
		this.rel = rel;
	}
	
	public void initRel(String rel) {
		initRel(Integer.parseInt(rel));
	}
	
	public void setRel(int rel) {
		int oldRel = this.rel;
		
		if (oldRel != rel) {
			int value;
			if (rel == Bep.IRRELEVANT) { 
				value = MARK_ASSESSED;
			}
			else {
				value = MARK_UNASSESSED;
			}
			setChanged();
			notifyObservers(value);
			this.rel = rel;
		}
	}

	public void setRel(String rel) {
		setRel(Integer.parseInt(rel));
	}
	
	public String relString() {
		return String.valueOf(this.rel);
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

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public AssessedAnchor getAssociatedAnchor() {
		return associatedAnchor;
	}

	public void setAssociatedAnchor(AssessedAnchor associatedAnchor) {
		this.associatedAnchor = associatedAnchor;
	}

	public String offsetToString() {
		return String.valueOf(offset);
	}
	

	public String startPToString() {
		return String.valueOf(startP);
	}
}
