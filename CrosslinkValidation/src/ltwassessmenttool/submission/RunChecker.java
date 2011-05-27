package ltwassessmenttool.submission;

import java.io.File;

import ltwassessment.submission.Run;

public class RunChecker {
	
	public void checkRun(String file) {
		Run run = new Run(new File(file));
		
		System.err.println("Checking run: " + run.getRunName());
		System.err.println("===========================================================================");
		run.validate();	
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: program run1 [run 2] [run 3] ...");
			System.exit(-1);
		}

		RunChecker checker = new RunChecker();
		int i = 0;
//		if (args[0].charAt(0) == '-' && args[0].charAt(1) == 'p')
//			ltwassessment.parsers.resourcesManager.getInstance().
		for (i = 0; i < args.length; ++i) {
			String file = args[i];
			checker.checkRun(file);
		}
	}

}
