package qut.crosslink;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import crosslink.submission.Anchor;
import crosslink.submission.MappedAnchors;
import crosslink.submission.Run;
import crosslink.submission.Target;
import crosslink.submission.Topic;
import crosslink.submission.TopicLink;



public class CrosslinkRemix {
	private HashMap<String, Run> runs = new HashMap<String, Run>();
	private HashMap<String, TopicLink> topics = new HashMap<String, TopicLink>();
//	private HashMap<String, HashSet<Target>> topicLinks = new HashMap<String, HashSet<Target>>();
	
	public void load(String[] args, int start, int len) {
		for (int i = start; i < len; ++i) {
			Run<MappedAnchors> run = new Run<MappedAnchors>();
			run.setAnchorSetFactory(MappedAnchors.class);
			run.setConvertToTextOffset(false);
			run.read(new File(args[i]), false);
			runs.put(run.getRunName(), run);
		}
	}
	
	public void merge() {
		Collection<Run> allRuns = runs.values();
		for (Run run : allRuns) {
			Collection<Topic> allTopics = run.getTopics().values();
			
			for (Topic<MappedAnchors> topic : allTopics) {
				String topicId = topic.getId();
				TopicLink topicLink = new TopicLink(topicId);
				
				Collection<Anchor> anchors = topic.getAnchors().values();
				for (Anchor anchor : anchors) {
					Collection<Target> targets = anchor.getTargets().values();
					
					for (Target target : targets) {
						topicLink.addLink(target);
					}
				}
				
//				Topic<MappedAnchors> newTopic = null;
//				if (topics.containsKey(topicId))
//					newTopic = topics.get(topicId);
//				else {
//					newTopic = new Topic(topic.getId(), topic.getTitle());
//					newTopic.load();
//				}
//				
//				newTopic.getAnchors().putAll(topic.getAnchors());
				
				
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
		mixer.merge();
			
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
