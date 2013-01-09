package crosslink;

import java.io.File;
import java.util.ArrayList;

public class Validation {
	private ArrayList<String> topics;
	
	private int index = -1;
	
	public void addTopics(String folder) {
		topics = new ArrayList<String>();
		File folderHandler = new File(folder);
		if (folderHandler.isDirectory()) {
			String[] files = folderHandler.list();
			if (files != null)
				for (String file : files) 
					topics.add(folderHandler.getAbsolutePath() + File.separator + file);
		}
		else
			topics.add(folder);
	}
	
//	public void addTopic(String file) {
//		topics.add(file);
//	}
	
	public String first() {
		if (topics.size() > 0) {
			index = 0;
			return topics.get(0);
		}
		return null;
	}
	
	public String next() {
		if (index != -1) {
			++index;
			if (index >= topics.size())
				index %= (topics.size());
			return topics.get(index);
		}
		return null;
	}
	
	public String prev() {
		if (index != -1) {
			--index;
			while (index < 0)
				index += (topics.size());
			return topics.get(index);
		}
		return null;
	}
}