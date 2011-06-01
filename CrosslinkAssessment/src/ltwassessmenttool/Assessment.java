package ltwassessmenttool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ltwassessment.AppResource;
import ltwassessment.utility.FileUtil;
import ltwassessment.utility.WildcardFiles;

public class Assessment {

	private static Assessment instance;

	private static final String ASSESSMENT_TOPIC_PATH = "assessment";
	private static final String ASSESSMENT_TOPIC_INBOX_PATH = ASSESSMENT_TOPIC_PATH + File.separator + "inbox" + File.separator;
	private static final String ASSESSMENT_TOPIC_OUTBOX_PATH = ASSESSMENT_TOPIC_PATH + File.separator + "outbox" + File.separator;
	
	private ArrayList<File> topics = new ArrayList<File>();
	

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
	public ArrayList<File> getTopics() {
		return topics;
	}

	private void loadTopicsForAssessment() {
		topics.addAll(WildcardFiles.listFilesInStack(ASSESSMENT_TOPIC_INBOX_PATH));
	}
	
	public void finishTopic(File topicFile) {
		try {
			FileUtil.moveFile(topicFile, new File(ASSESSMENT_TOPIC_OUTBOX_PATH + topicFile.getName()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
