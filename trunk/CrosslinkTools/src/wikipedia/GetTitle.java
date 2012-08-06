package wikipedia;

import java.io.File;

import crosslink.wiki.WikiArticleXml;


public class GetTitle {
	public static void usage() {
		System.err.println("Usage: program /a/path/to/a/wikipedia/article");
		System.exit(-1);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length <=0)
			usage();
		System.out.println(new WikiArticleXml(new File(args[0])).getTitle());
	}
}
