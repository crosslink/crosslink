package crosslink;
import java.io.File;
import java.io.FileOutputStream;

import ltwassessment.AppResource;
import ltwassessment.pool.Pool;
import ltwassessment.submission.Anchor;

public class PoolManager {
	
	private boolean validateAnchors = false;

	public boolean isValidateAnchors() {
		return validateAnchors;
	}

	public void setValidateAnchors(boolean validateAnchors) {
		this.validateAnchors = validateAnchors;
	}

	public void genPool(String submissionsPath, boolean splitTopic) {
		Pool pool = new Pool();
		pool.read(submissionsPath);
		
		if (validateAnchors)
			pool.checkAnchors(Anchor.SHOW_MESSAGE_ERROR);
		
		/*String xml = */pool.output(splitTopic);
		
//		System.out.println(xml);
	}
	
	public static void usage() {
		System.err.println("Usage: [-s] [-v topic_path] program submissions_path");
		System.exit(-1);	
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			usage();
		}

		PoolManager manager = new PoolManager();
		
		String path = null;
		boolean splitTopic = false;
		
		int param_start = 0;
		
		for (int i = 0; i < args.length; ++i) {
			if (args[i].charAt(0) == '-') {
				if (args[0].charAt(1) == 's' ) {
					splitTopic = true;
					++param_start;
				}
				else if (args[i].charAt(1) == 'c' ) {
					manager.setValidateAnchors(true);
					String topicPath = args[++i];
					if (!new File(topicPath).exists()) {
						System.err.println("Incorrect topic path: " + topicPath);
						usage();						
					}
					if (!topicPath.endsWith(File.separator))
						topicPath = topicPath + File.separator;
					AppResource.getInstance().setTopicPath(topicPath);
					param_start += 2;
				}
				else
					usage();
			}
			else
				break;
		}
		if (param_start >= args.length)
			usage();
		
		path = args[param_start];

		manager.genPool(path, splitTopic);
	}

}
