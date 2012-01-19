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

	public void genPool(String submissionsPath, boolean splitTopic, String langPair) {
		Pool pool = null;
		if (langPair != null) {
			String[] arr = langPair.split(":");
			pool = new Pool(arr[0], arr[1]);
		}
		else
			pool = new Pool();
		pool.read(submissionsPath, validateAnchors);
		
//		if (validateAnchors)
//			pool.checkAnchors(Anchor.SHOW_MESSAGE_ERROR);
		
		/*String xml = */
		try {
			pool.output(splitTopic);
		} catch (Exception e) {	
			e.printStackTrace();
			System.err.println("There is a sever bug in the pooling program, fixes needed");
			System.exit(-2);
		} 
//		System.out.println(xml);
	}
	
	public static void usage() {
		System.err.println("Usage: [-s] [-c] [-p topic_path] [-l source_lang:target_lang] program submissions_path");
		System.err.println("		-s output will be generated in seperated files");
		System.err.println("		-c check whether anchors have correct offset");
		System.err.println("		-l language pair, zh:en, en:zh, en:ja, en:ko ...");
		System.exit(-1);	
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		AppResource.forValidationOrAssessment = true;
		
		if (args.length < 1) {
			usage();
		}

		PoolManager manager = new PoolManager();
		
		String path = null;
		String langPair = null;
		boolean splitTopic = false;
		
		int param_start = 0;
		
		for (int i = 0; i < args.length; ++i) {
			if (args[i].charAt(0) == '-') {
				if (args[i].charAt(1) == 's' ) {
					splitTopic = true;
					++param_start;
				}
				else if (args[i].charAt(1) == 'p' ) {
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
				else if (args[i].charAt(1) == 'c' ) {
					manager.setValidateAnchors(true);
					++param_start;
				}
				else if (args[i].charAt(1) == 'l' ) {
					langPair = args[++i];
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

		manager.genPool(path, splitTopic, langPair);
	}

}
