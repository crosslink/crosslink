package ltwassessment.pool;

import java.io.File;
import java.util.Vector;

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
		
		return poolXml.toString();
	}
}
