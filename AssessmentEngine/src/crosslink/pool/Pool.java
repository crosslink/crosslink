package crosslink.pool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import crosslink.AppResource;
import crosslink.submission.LinkedAnchors;
import crosslink.submission.Run;
import crosslink.submission.Topic;

import crosslink.pool.ToXml;

public class Pool {
	private Run runs = null;
	private String sourceLang = null;
	private String targetLang = null;
	
	public Pool() {
//		this.sourceLang = AppResource.sourceLang;
//		this.targetLang = AppResource.targetLang;
		runs = new Run();
		init();
	}
	
	public Pool(String sourceLang, String targetLang) {
		this.sourceLang = sourceLang;
		this.targetLang = targetLang;
		AppResource.targetLang = this.targetLang;
		AppResource.sourceLang = this.sourceLang; 		
		runs = new Run();
		init();
	}
	
	private void init() {
		runs.setAnchorSetFactory(LinkedAnchors.class);
	}

	public void read(String rundir, boolean checkAnchors) {
		File[] files = null;
		File runDirHandler = new File(rundir);
		if (runDirHandler.isDirectory())
			files = runDirHandler.listFiles();
		else
			files = new File[] {runDirHandler};
		
		for (File file : files) 
			runs.read(file, checkAnchors, sourceLang, targetLang);
	}
	
	public void output(boolean splitTopic, String targetPath) throws Exception {
		StringBuffer poolXml = new StringBuffer();
		
		ToXml.startRootElement(poolXml, sourceLang, targetLang);
		
		Set set = runs.getTopics().entrySet();

	    Iterator i = set.iterator();

	    while(i.hasNext()) {
	    	Map.Entry me = (Map.Entry)i.next();
	    	Topic topic = (Topic)me.getValue();
			ToXml.topicToXml(topic, poolXml);
			
			if (splitTopic) {
				ToXml.endRootElement(poolXml);
				output2File(poolXml, targetPath + File.separator + "wikipedia_pool_" + topic.getId() + ".xml");
				poolXml.setLength(0);
				ToXml.startRootElement(poolXml, sourceLang, targetLang);
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
