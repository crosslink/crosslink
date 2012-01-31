package qut.crosslink;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import ltwassessment.submission.MappedAnchors;
import ltwassessment.submission.Run;
import ltwassessment.submission.Topic;


public class CrosslinkRemix {
	private HashMap<String, Run> runs = new HashMap<String, Run>();
	private HashMap<String, Topic> topics = new HashMap<String, Topic>();
	
	public void load(String[] args, int start, int len) {
		for (int i = start; i < len; ++i) {
			Run<MappedAnchors> run = new Run<MappedAnchors>();
			run.read(new File(args[i]));
			runs.put(run.getRunName(), run);
		}
	}
	
	public void merge() {
		Collection<Run> allRuns = runs.values();
		for (Run run : allRuns) {
			Collection<Topic> allTopics = run.getTopics().values();
			
			for (Topic topic : allTopics) {
				String topicId = topic.getId();
				
				Topic newTopic = null;
				if (topics.containsKey(topicId))
					newTopic = topics.get(topicId);
				else
					newTopic = new Topic(topic.getId(), topic.getTitle());
				
			}
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
			else if (args[0].charAt(1) == 'r') {

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
		System.err.println("Usage: program [-r qrel] run1 [run 2] [run 3] ...");
		System.exit(-1);	
	}

}
