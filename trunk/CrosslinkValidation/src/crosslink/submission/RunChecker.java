package crosslink.submission;

import java.io.File;

import crosslink.AppResource;
import crosslink.submission.Anchor;
import crosslink.submission.Run;


public class RunChecker {
	
	public void checkRun(String file) {
		Run run = new Run(new File(file));
		
		System.err.println("Checking run: " + run.getRunName());
		System.err.println("===========================================================================");
		run.validate(Anchor.SHOW_MESSAGE_ERROR | Anchor.SHOW_MESSAGE_OK);	
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			usage();
		}

		RunChecker checker = new RunChecker();
		int i = 0;
		if (args[0].charAt(0) == '-') {
			if (args[0].charAt(1) == 'c') {
				String topicPath = args[1];
				if (!new File(topicPath).exists()) {
					System.err.println("Incorrect topic path: " + topicPath);
					usage();						
				}
				if (!topicPath.endsWith(File.separator))
					topicPath = topicPath + File.separator;
				AppResource.getInstance().setTopicPath(topicPath);
				i += 2;
			}
			else
				usage();
		}
			
		for (; i < args.length; ++i) {
			String file = args[i];
			checker.checkRun(file);
		}
	}

	private static void usage() {
		System.err.println("Usage: program [-c topic_path] run1 [run 2] [run 3] ...");
		System.exit(-1);	
	}

}
