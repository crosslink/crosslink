package ltwassessmenttool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;

import ltwassessment.AppResource;
import ltwassessment.utility.FileUtil;
import ltwassessment.utility.WildcardFiles;

public class Assessment {

	private static Assessment instance;

	private static final String ASSESSMENT_TOPIC_PATH = "assessment";
	private static final String ASSESSMENT_TOPIC_INBOX_PATH = ASSESSMENT_TOPIC_PATH + File.separator + "inbox" + File.separator;
	private static final String ASSESSMENT_TOPIC_OUTBOX_PATH = ASSESSMENT_TOPIC_PATH + File.separator + "outbox" + File.separator;
	private static final String ASSESSMENT_POOL_PREFIX = "wikipedia_pool";
	private static final String ASSESSMENT_POOL_PATH = "resources" + File.separator + "Pool" + File.separator;
	
	private Hashtable<String, File> topics = new Hashtable<String, File>();
	

	public Assessment() {
		loadTopicsForAssessment();
	}

	
	public static Assessment getInstance() {
		if (instance == null)
			instance = new Assessment(); 
		return instance;
	}

	/**
	 * @return the topics
	 */
	public Hashtable<String, File> getTopics() {
		return topics;
	}

	private void loadTopicsForAssessment() {
		Stack<File> stack = WildcardFiles.listFilesInStack(ASSESSMENT_TOPIC_INBOX_PATH + "*.xml");
		
		for (File file: stack) {
    		String filename =  file.getName();
        	String	topicID = filename.substring(0, filename.indexOf('.'));
			topics.put(topicID, file);
		}
		
	}
	
	public void finishTopic(File topicFile) {
		try {
			FileUtil.moveFile(topicFile, new File(ASSESSMENT_TOPIC_OUTBOX_PATH + topicFile.getName()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getPoolFile(String id) {
		return ASSESSMENT_POOL_PATH + ASSESSMENT_POOL_PREFIX + "_" + id + ".xml";
	}
}
