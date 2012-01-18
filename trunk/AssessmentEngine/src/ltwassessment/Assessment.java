package ltwassessment;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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
	
	private static final String RESOURCES_PATH = "resources" + File.separator ;

	private static final String ASSESSMENT_TOPIC_ASSESSMENT_PATH = "assessment";
	private static final String ASSESSMENT_TOPIC_INBOX_PATH = ASSESSMENT_TOPIC_ASSESSMENT_PATH + File.separator + "inbox" + File.separator;
	private static final String ASSESSMENT_TOPIC_OUTBOX_PATH = ASSESSMENT_TOPIC_ASSESSMENT_PATH + File.separator + "outbox" + File.separator;
	private static final String ASSESSMENT_TOPIC_TOPIC_PATH = "resources" + File.separator + "Topics" + File.separator;
	
	private static final String ASSESSMENT_POOL_PREFIX = "wikipedia_pool";
	private static final String ASSESSMENT_POOL_SUBMISSION_PATH = "Pool" + File.separator;
	private static final String ASSESSMENT_POOL_GROUNDTRUTH_PATH = "GroundTruthPool" + File.separator;
	
	private static final String ASSESSMENT_LANG_ADDRESS = "http://131.181.88.158/lang.txt";
	
	private Hashtable<String, File> topics = new Hashtable<String, File>();
	private Hashtable<String, File> finishedTopics = new Hashtable<String, File>();
	private Iterator it  = null;
	
	private Topic currentTopic;
	
	private static final String ASSESSMENT_POOL_BACKUP_DIR =  "POOL_BACKUP" + File.separator;
	
	private StringBuffer poolDir = null;
	private File poolDirHandler = null;
	private File poolBackupDirHandler = null;
	private File poolBackupTempDirHandler = null;
	
	private String topicPath = null;
	private String finishedTopicPath = null;
	private File finishedTopicPathHandler = null;
	
	private String assessmentLang = "";
	
	private boolean assessmentType = false; // false: pool assessment; true: ground-truth assessment 
	
	static {

	}

	public Assessment() {
		getAssessmentLang();
		
		poolDir = new StringBuffer(RESOURCES_PATH);
		if (!assessmentType) {
			poolDir.append(ASSESSMENT_POOL_SUBMISSION_PATH);
		}
		else { // assess Wikipedia ground-truth
			poolDir.append(ASSESSMENT_POOL_GROUNDTRUTH_PATH);
		}
		
		poolDirHandler = new File(poolDir.toString() + assessmentLang);
		if (assessmentLang.length() > 0 && !poolDirHandler.exists()) {
			assessmentLang = "";
			poolDirHandler = new File(poolDir.toString());
		}
		poolDir.append(File.separator);

//			poolDirHandler.mkdir();
			
		poolBackupDirHandler = new File(poolDir.toString() + ASSESSMENT_POOL_BACKUP_DIR + assessmentLang);
		if (!poolBackupDirHandler.exists())
			poolBackupDirHandler.mkdirs();
		
		String baseTempPath = System.getProperty("java.io.tmpdir");
		poolBackupTempDirHandler = new File(baseTempPath + File.separator + "CrosslinkAssesment");
		if (!poolBackupTempDirHandler.exists())
			poolBackupTempDirHandler.mkdirs();
		
		System.err.println("System temp dir: " + poolBackupTempDirHandler.getAbsolutePath());
		
		finishedTopicPath = ASSESSMENT_TOPIC_OUTBOX_PATH;
		
		loadTopicsForAssessment();
	}

	
	private void getAssessmentLang() {
	    try { 
	    	File langFile = new File("lang.txt");
	    	InputStream is = null;
	    	if (langFile.exists()) {
	    		is = new BufferedInputStream(new FileInputStream(langFile));
		        InputStreamReader in = new InputStreamReader(is);
		        BufferedReader buff = new BufferedReader(in);
		        String line;
		        line = buff.readLine();
		        assessmentLang = line.trim();
	    	}
	    	else
	    		assessmentLang = "zh";
//	    	else {
//		        URL url = new URL(ASSESSMENT_LANG_ADDRESS); 
//	
//		        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//		        conn.connect();
//		        is = conn.getInputStream();
//	    	}

	    } catch(Exception e) { 
	        e.printStackTrace();
	        System.err.println("Can't get the lanuage of the assessment pool from " + ASSESSMENT_LANG_ADDRESS);
	        assessmentLang = "ja";
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


	public void setCurrentTopic(File currentTopicId) {
		setCurrentTopic(new Topic(currentTopicId));
	}
	
	public void setCurrentTopic(Topic currentTopic) {
		this.currentTopic = currentTopic;
	}


	private void loadTopicsForAssessment() {
		
		Stack<File> stack = WildcardFiles.listFilesInStack(ASSESSMENT_TOPIC_OUTBOX_PATH + "*.xml");
		for (File file: stack) {
    		String filename =  file.getName();
        	String	topicID = filename.substring(0, filename.indexOf('.'));
			finishedTopics.put(topicID, file);
		}	
		
		if (!assessmentType) {
			topicPath = ASSESSMENT_TOPIC_INBOX_PATH;
		}
		else { // assess Wikipedia ground-truth
			topicPath = ASSESSMENT_TOPIC_TOPIC_PATH;
		}
		
		stack = WildcardFiles.listFilesInStack(topicPath + "*.xml");
		
		for (File file: stack) {
    		String filename =  file.getName();
        	String	topicID = filename.substring(0, filename.indexOf('.'));
        	if (!finishedTopics.contains(topicID))
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
			FileUtil.copyFile(topicFile, new File(ASSESSMENT_TOPIC_OUTBOX_PATH + topicFile.getName()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getPoolFile(String id) {
//		if (assessmentLang.length() == 0)
//			return ASSESSMENT_POOL_PATH + ASSESSMENT_POOL_PREFIX + "_" + id + ".xml";
//		return ASSESSMENT_POOL_PATH + assessmentLang + File.separator + ASSESSMENT_POOL_PREFIX + "_" + id + ".xml";
		return poolDirHandler.getAbsolutePath() + File.separator + ASSESSMENT_POOL_PREFIX + "_" + id + ".xml";
	}
	
	public String getNextTopic() {
		String topic = null;
		if (it.hasNext()) {
    		Entry entry = (Entry) it.next();
    		topic = (String) entry.getKey();
		}
		return topic;
	}

	public File getPoolBackupTempDirHandler() {
		return poolBackupTempDirHandler;
	}
	
	public File getPoolBackupDirHandler() {
		return poolBackupDirHandler;
	}


	public boolean getAssessmentType() {
		return assessmentType;
	}


	public void setAssessmentType(boolean assessmentType) {
		this.assessmentType = assessmentType;
	}
}
