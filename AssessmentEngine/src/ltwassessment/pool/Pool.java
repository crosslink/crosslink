package ltwassessment.pool;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import ltwassessment.submission.Run;
import ltwassessment.submission.Topic;

public class Pool {
	private Run runs = null;
	
	public Pool() {
		runs = new Run();
	}
	
	public void read(String rundir) {
		File runDirHandler = new File(rundir);
		File[] files = runDirHandler.listFiles();
		
		for (File file : files) 
			runs.read(file);
	}
	
	public String output() {
		StringBuffer poolXml = new StringBuffer();
		
		ToXml.startRootElement(poolXml);
		
		Set set = runs.getTopics().entrySet();

	    Iterator i = set.iterator();

	    while(i.hasNext()) {
	    	Map.Entry me = (Map.Entry)i.next();
	    	Topic topic = (Topic)me.getValue();
			ToXml.topicToXml(topic, poolXml);
	    }
		ToXml.endRootElement(poolXml);
		return poolXml.toString();
	}
}
