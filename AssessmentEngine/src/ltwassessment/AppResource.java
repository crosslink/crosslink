package ltwassessment;

import org.jdesktop.application.Application;

public class AppResource {
	
	public static AppResource instance = null;
    public static boolean forValidationOrAssessment = false;
    public static String targetLang = "en"; // zh, ja, ko
    
	private org.jdesktop.application.ResourceMap resourceMap = null;

	public AppResource() {
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
}
