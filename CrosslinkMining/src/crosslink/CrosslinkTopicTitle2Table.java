package crosslink;

import java.io.File;
import java.util.Stack;

import crosslink.utility.WildcardFileStack;

public class CrosslinkTopicTitle2Table {
	
	private String topicPath;
	
	public String getTopicPath() {
		return topicPath;
	}


	public void setTopicPath(String topicPath) {
		this.topicPath = topicPath;
	}


	public void createTable() {
		try {
			WildcardFileStack filestack = new WildcardFileStack(topicPath, "*en*");
			Stack<File> stack = filestack.list();
					
			for (File file : stack) {
				System.out.println(file.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	public static void main(String[] args) {
		CrosslinkTopicTitle2Table builder = new CrosslinkTopicTitle2Table();
		
		builder.setTopicPath(args[0]);
		
		builder.createTable();
	}

	public static void usage() {
		System.out.println("arg[1] overall topic path");
		System.out.println("arg[1] crosslink tables path");
		System.out.println("arg[2] source topics path");
		System.out.println("arg[3] target topics path");
		System.out.println("arg[4] corpora home: e.g. /corpus");
		System.out.println("arg[5] run|rs");
		System.exit(-1);
	}
	
}
