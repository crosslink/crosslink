package ltwassessment.pool;

import java.io.File;
import java.util.Vector;

public class Pool {
	private Vector<Run> runs = null;
	
	public Pool() {
		runs = new Vector<Run>();
	}
	
	public void add(Run run) {
		runs.add(run);
	}
	
	public void read(String rundir) {
		File runDirHandler = new File(rundir);
		File[] files = runDirHandler.listFiles();
		
		for (File file : files) {
			Run run = new Run(file);
			add(run);
		}
	}
	
	public void mergeRuns() {
		//TODO
	}
}
