package ltwassessment;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;
import java.util.Map.Entry;

import ltwassessment.AppResource;
import ltwassessment.utility.FileUtil;
import ltwassessment.utility.WildcardFiles;
import ltwassessment.submission.Topic;

public class Assessment {

	private static Assessment instance;

	private static final String ASSESSMENT_TOPIC_PATH = "assessment";
	private static final String ASSESSMENT_TOPIC_INBOX_PATH = ASSESSMENT_TOPIC_PATH + File.separator + "inbox" + File.separator;
	private static final String ASSESSMENT_TOPIC_OUTBOX_PATH = ASSESSMENT_TOPIC_PATH + File.separator + "outbox" + File.separator;
	private static final String ASSESSMENT_POOL_PREFIX = "wikipedia_pool";
	private static final String ASSESSMENT_POOL_PATH = "resources" + File.separator + "Pool" + File.separator;
	
	private Hashtable<String, File> topics = new Hashtable<String, File>();
	private Iterator it  = null;
	
	private Topic currentTopic;
	
	public static final String ASSESSMENT_POOL_BACKUP_DIR =  ASSESSMENT_POOL_PATH + File.separator + "POOL_BACKUP" + File.separator;
	private static File poolDirHandler = null;
	private static File poolBackupDirHandler = null;
	
	private static String assessmentLang = "";
	
	static {
		poolDirHandler = new File(ASSESSMENT_POOL_PATH);
		if (!poolDirHandler.exists())
			poolDirHandler.mkdir();
			
		poolBackupDirHandler = new File(ASSESSMENT_POOL_BACKUP_DIR);
		if (!poolBackupDirHandler.exists())
			poolBackupDirHandler.mkdir();
		
		getAssessmentLang();
	}

	public Assessment() {
		loadTopicsForAssessment();
	}

	
	private static void getAssessmentLang() {
	    try { 
	        URL url = new URL("http://131.181.88.158/lang.txt"); 

	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.connect();
	        InputStreamReader in = new InputStreamReader(conn.getInputStream());
	        BufferedReader buff = new BufferedReader(in);
	        String line;
	        line = buff.readLine();
	        assessmentLang = line.trim();
	    } catch(Exception e) { 
	        throw new RuntimeException(e); 
	    } 
		
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

	public Topic getCurrentTopic() {
		return currentTopic;
	}


	public void setCurrentTopicWithId(String currentTopicId) {
		setCurrentTopic(new Topic(currentTopicId));
	}
	
	public void setCurrentTopic(Topic currentTopic) {
		this.currentTopic = currentTopic;
	}


	private void loadTopicsForAssessment() {
		Stack<File> stack = WildcardFiles.listFilesInStack(ASSESSMENT_TOPIC_INBOX_PATH + "*.xml");
		
		for (File file: stack) {
    		String filename =  file.getName();
        	String	topicID = filename.substring(0, filename.indexOf('.'));
			topics.put(topicID, file);
		}
		
		it = topics.entrySet().iterator();
	}
	
	public void finishTopic(String topic) {
		File topicFile = topics.get(topic);
		if (topicFile != null)
			finishTopic(topicFile);
	}
	
	public void finishTopic(File topicFile) {
		try {
			FileUtil.moveFile(topicFile, new File(ASSESSMENT_TOPIC_OUTBOX_PATH + topicFile.getName()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getPoolFile(String id) {
		if (assessmentLang.length() == 0)
			return ASSESSMENT_POOL_PATH + ASSESSMENT_POOL_PREFIX + "_" + id + ".xml";
		return ASSESSMENT_POOL_PATH + assessmentLang + File.separator + ASSESSMENT_POOL_PREFIX + "_" + id + ".xml";
	}
	
	public String getNextTopic() {
		String topic = null;
		if (it.hasNext()) {
    		Entry entry = (Entry) it.next();
    		topic = (String) entry.getKey();
		}
		return topic;
	}
}
