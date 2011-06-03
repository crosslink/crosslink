package crosslink;
import java.io.File;
import java.io.FileOutputStream;

import ltwassessment.pool.Pool;
import ltwassessment.submission.Anchor;

public class PoolManager {

	public void genPool(String submissionsPath, boolean splitTopic) {
		Pool pool = new Pool();
		pool.read(submissionsPath);
		pool.checkAnchors(Anchor.SHOW_MESSAGE_ERROR);
		
		/*String xml = */pool.output(splitTopic);
		
//		System.out.println(xml);
	}
	
	public static void usage() {
		System.err.println("Usage: [-s] program submissions_path");
		System.exit(-1);	
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			usage();
		}

		String path;
		boolean splitTopic = false;
		
		if (args[0].charAt(0) == '-') {
			if (args[0].charAt(1) != 's' || args.length < 2)
				usage();
			splitTopic = true;
			path = args[1];
		}
		else
			path = args[0];

		PoolManager manager = new PoolManager();
		manager.genPool(path, splitTopic);
	}

}
