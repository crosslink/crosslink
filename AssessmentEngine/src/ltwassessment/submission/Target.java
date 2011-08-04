package ltwassessment.submission;

import java.io.File;

import ltwassessment.AppResource;

public class Target {
	private String lang = "";
	private String title = "";
	private String id = "";
	private int bepOffset = 0;
	private boolean relevant = false;
	
	public static String corpusHome = "/data/corpus/wikipedia/all/";
	public static File corpusHomeHandler = null;
	
	static {
		if (new File(corpusHome).exists()) {
			corpusHomeHandler = new File(corpusHome);
		}
		else {
			System.err.println("Cannot find CORPUS: " + corpusHome);
			System.err.println("Are you sure that you want to continue?");
		}
	}
	
	public Target(String lang, String title, String id, int bepOffset) {
		super();
		this.lang = lang;
		this.title = title;
		this.id = id;
		this.bepOffset = bepOffset;
	}

	public boolean isRelevant() {
		return relevant;
	}

	public void setRelevant(boolean relevant) {
		this.relevant = relevant;
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

	public static String id2Path(String fileId) {
		if (corpusHomeHandler == null)
			return null;
		
        String subFolder;
        if (fileId.length() < 3)
        	subFolder = String.format("%03d", Integer.parseInt(fileId));
        else
        	subFolder = fileId.substring(fileId.length() - 3);
		return corpusHome + File.separator + AppResource.targetLang + File.separator + "pages" + File.separator + subFolder + File.separator +  fileId + ".xml";
	}
	
	public boolean exists() {
		String filePath = id2Path(id);
		if (filePath != null)
			return new File(filePath).exists();
		return false;
	}
}
