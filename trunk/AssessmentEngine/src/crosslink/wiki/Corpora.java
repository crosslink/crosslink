package crosslink.wiki;

import java.io.File;

import crosslink.wiki.Corpora;

public class Corpora {

	private String home;
	private String lang;
	private static Corpora instance;
	
	public static void initialize() {
		instance = new Corpora();
	}
	
	public static Corpora getInstance() {
		return instance;
	}
	
	public String getHome() {
		return home;
	}
	
	public void setHome(String home) {
		this.home = home;
	}
	
	public String getLang() {
		return lang;
	}
	
	public void setLang(String lang) {
		this.lang = lang;
	}
	
    public String getWikipediaFileFolder(String lang) {
    	return home + File.separator + lang + File.separator + "pages" + File.separator;
    }
    
    public String getWikipediaFileFolder() {
    	return getWikipediaFileFolder(lang);
    }
    
    public String getWikiFilePath(String id) {
    	
        return getWikipediaFileFolder() + getIdWithParentDir(id) + File.separator + id + ".xml";
    }
    
    public static String getIdWithParentDir(String id) {
        String subFolder;
        if (id.length() < 3)
        	subFolder = String.format("%03d", Integer.parseInt(id));
        else
        	subFolder = id.substring(id.length() - 3);
        return subFolder;
    }
}
