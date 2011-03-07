package ltwassessment;

import java.io.File;

import org.jdesktop.application.Application;
import ltwassessment.font.AdjustFont;;

public class AppResource {
	
	public static AppResource instance = null;
    public static boolean forValidationOrAssessment = false;
    public static String targetLang = "zh"; // zh, ja, ko
    public static String sourceLang = "en"; // zh, ja, ko
    
	private org.jdesktop.application.ResourceMap resourceMap = null;
	private static AdjustFont adjustFont = null;
	public static boolean debug = false;

	public AppResource() {
		adjustFont = AdjustFont.getInstance();
//		resourceMap = org.jdesktop.application.Application.getInstance(appClass.getClass()).getContext().getResourceMap(viewClass);
	}

	public static AppResource getInstance() {
		if (instance == null)
			instance = new AppResource(); 
		return instance;
	}

	public org.jdesktop.application.ResourceMap getResourceMap() {
		return resourceMap;
	}
	
	public void setResourceMap(org.jdesktop.application.ResourceMap resourceMap) {
		this.resourceMap = resourceMap;
	}
	
	public static String getTopicDirectory() {
		return getTopicDirectory(sourceLang);
    }
	
	public static String getTopicDirectory(String topicLang) {
		return "resources" + File.separator + "Topics" + File.separator + topicLang + File.separator;
    }
    
    public static String getTopicPathNameByFileID(String fileID) {
        return getTopicDirectory() + fileID;
    }
    public static String getTopicXmlPathNameByFileID(String fileID) {
        return getTopicPathNameByFileID(fileID) + ".xml";
    }

	public static String getTopicXmlPathNameByFileID(String topicID,
			String topicLang) {
    	return getTopicDirectory(topicLang) + topicID + ".xml";
	}
}
