package crosslink.submission;

import java.io.File;
import java.util.ArrayList;

import crosslink.AppResource;
import crosslink.submission.Anchor;
import crosslink.submission.Run;


public class RunChecker {
	
	public void checkRun(String file) {
		Run<ArrayList> run = new Run<ArrayList>(new File(file));
		
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
		String topicPath = null;
		String corpusPath = null;
		
		while (args[i].charAt(0) == '-' && i < args.length) {
//			if (args[0].charAt(0) == '-') {
				if (args[i].charAt(1) == 't') {
					topicPath = args[++i];
//					i += 2;
				}
				else if (args[i].charAt(1) == 'c') {
					corpusPath = args[++i];
					if (!new File(corpusPath).exists()) {
						System.err.println("Incorrect corpus path: " + corpusPath);
						usage();						
					}
					if (!corpusPath.endsWith(File.separator))
						corpusPath = corpusPath + File.separator;
					AppResource.corpusHome = corpusPath;
//					i += 2;
				}
				else
					usage();
//			}
				++i;
		}
		
		if (topicPath == null) {
			System.err.println("Please specify the path for topics");
			usage();
		}
		else {
			if (!new File(topicPath).exists()) {
				System.err.println("Incorrect topic path: " + topicPath);
				usage();						
			}
			if (!topicPath.endsWith(File.separator))
				topicPath = topicPath + File.separator;
			AppResource.getInstance().setTopicPath(topicPath);
		}
		
		if (corpusPath == null) {
			System.err.println("warining: no path specified for corpus, the target file won't be checked");
		}
			
		for (; i < args.length; ++i) {
			String file = args[i];
			checker.checkRun(file);
		}
	}

	private static void usage() {
		System.err.println("Usage: program -t topic_path [-c corpus_path] run1 [run 2] [run 3] ...");
		System.err.println("\t Optional: [corpus_path] is a path (folder) where all the directories (zh, en, ja, ko) of the Wikipedia collections located");
		System.exit(-1);	
	}

}
