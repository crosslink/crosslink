package ltwassessment;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;

import org.jdesktop.application.Application;

import ltwassessment.font.AdjustFont;;

public class AppResource {
	
	public static AppResource instance = null;
    public static boolean forValidationOrAssessment = false;
    public static String targetLang = "zh"; // zh, ja, ko
    public static String sourceLang = "en"; // zh, ja, ko
    
	private org.jdesktop.application.ResourceMap resourceMap = null;
	private static AdjustFont adjustFont = null;
	public static boolean debug = true;
	
	private static String topicPath = "resources" + File.separator + "Topics";
	public static final String TOPIC_LIST_FILE = "topics.txt"; 
	
	public static Hashtable<String, String> topics = new Hashtable<String, String>(); 

	
	public AppResource() {
		adjustFont = AdjustFont.getInstance();
		
		if (topics.size() == 0)
			loadTopicList();
//		resourceMap = org.jdesktop.application.Application.getInstance(appClass.getClass()).getContext().getResourceMap(viewClass);
	}

	private void loadTopicList() {
    	BufferedInputStream stream;
		try {
			stream = new BufferedInputStream(new FileInputStream(getTopicPathWithLang() + TOPIC_LIST_FILE));
	    	BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
	    	
        	while (true)
        	{
        		String line=reader.readLine();
        		
        		if (line==null)
        		{
        			break;
        		}
        		
        		//        		System.out.println(line);
        		
        		String tokens[]=line.split(" ");
        		
        		if (tokens.length!=2) continue;
        		
        		topics.put(tokens[0], tokens[1]);
        	}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	public String getTopicDirectory(String lang, String fileID) {
		String key = fileID + ".xml";
		String dir;
		if (topics.containsKey(key)) {
			dir = getTopicPathWithLang(lang) + topics.get(key); 
		}
		else
			dir = getTopicPathWithLang(lang) + "test";
		return dir + File.separator;
    }
	
	public String getTopicDirectory(String fileID) {
		return getTopicDirectory(sourceLang, fileID);
    }
	
	private String getTopicPathWithLang(String lang) {
		return topicPath + File.separator + lang + File.separator;
	}
	
	private String getTopicPathWithLang() {
		return getTopicPathWithLang(sourceLang);
	}

//	public static String getTopicDirectory(String topicLang) {
//		return  + topicLang + File.separator;
//    }
    
    public String getTopicPathNameByFileID(String fileID) {
        return getTopicDirectory(fileID) + fileID;
    }
    public String getTopicXmlPathNameByFileID(String fileID) {
        return getTopicPathNameByFileID(fileID) + ".xml";
    }

	public String getTopicXmlPathNameByFileID(String topicID,
			String topicLang) {
    	return getTopicDirectory(topicLang, topicID) + topicID + ".xml";
	}
}
