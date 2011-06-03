package ltwassessment.pool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
	
	public void output(boolean splitTopic) {
		StringBuffer poolXml = new StringBuffer();
		
		ToXml.startRootElement(poolXml);
		
		Set set = runs.getTopics().entrySet();

	    Iterator i = set.iterator();

	    while(i.hasNext()) {
	    	Map.Entry me = (Map.Entry)i.next();
	    	Topic topic = (Topic)me.getValue();
			ToXml.topicToXml(topic, poolXml);
			
			if (splitTopic) {
				ToXml.endRootElement(poolXml);
				output2File(poolXml, "wikipedia_pool_" + topic.getId() + ".xml");
				poolXml.setLength(0);
				ToXml.startRootElement(poolXml);
			}
	    }
		ToXml.endRootElement(poolXml);
		
		if (!splitTopic)
			System.out.println(poolXml);
//		return poolXml.toString();
	}
	
	public void output2File(StringBuffer xmlFile, String filename) {
	            try {
	                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
	        		        new FileOutputStream(filename), "UTF-8")); //new BufferedWriter(new FileWriter(textfilename));
	        	            out.write(xmlFile.toString());
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}
	
	public void checkAnchors(int showMessage) {
		runs.validate(showMessage);
	}
}
