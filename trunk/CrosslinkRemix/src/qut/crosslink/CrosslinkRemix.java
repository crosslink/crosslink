package qut.crosslink;

import java.io.File;
import java.util.HashMap;

import ltwassessment.submission.Run;


public class CrosslinkRemix {
	private HashMap<String, Run> runs = new HashMap<String, Run>();
	
	public void load(String[] args, int start, int len) {
		for (int i = start; i < len; ++i) {
			Run run = new Run();
			run.read(new File(args[i]));
			runs.put(run.getRunName(), run);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			usage();
		}

		CrosslinkRemix mixer = new CrosslinkRemix();
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
//				AppResource.getInstance().setTopicPath(topicPath);
				i += 2;
			}
			else
				usage();
		}
		
		mixer.load(args, i, args.length - i);
			
//		for (; i < args.length; ++i) {
//			String file = args[i];
//			checker.checkRun(file);
//		}
	}

	private static void usage() {
		System.err.println("Usage: program [-c topic_path] run1 [run 2] [run 3] ...");
		System.exit(-1);	
	}

}
