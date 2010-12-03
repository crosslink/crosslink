package crosslink;
import java.io.File;
import java.io.FileOutputStream;

import ltwassessment.pool.Pool;

public class PoolManager {

	public void genPool(String submissionsPath) {
		Pool pool = new Pool();
		pool.read(submissionsPath);
		
		String xml = pool.output();
		
		System.out.println(xml);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: program submissions_path");
			System.exit(-1);
		}

		PoolManager manager = new PoolManager();
		manager.genPool(args[0]);
	}

}
