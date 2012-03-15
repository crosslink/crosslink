package ltwassessment.utility;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

public class WildcardFileStack extends WildcardFiles {
	private Stack<File> stack = new Stack<File>();
	
	private boolean includeAllSubfolders = false;
	
	public WildcardFileStack(String inputfile) throws IOException {
		File file = new File(inputfile);
		
		if (file.exists()) {
			stack.add(file);
		}
		else {
			breakFile(inputfile);
			createPattern(wildcard);
			listInputFolder();
		}
	}
	
	public WildcardFileStack(String inputfile, String pattern) throws IOException {
		breakFile(inputfile);
		createPattern(pattern);
		listInputFolder();
	}
	
	public boolean isIncludeAllSubfolders() {
		return includeAllSubfolders;
	}

	public void setIncludeAllSubfolders(boolean includeAllSubfolders) {
		this.includeAllSubfolders = includeAllSubfolders;
	}

	private void listInputFolder() throws IOException {
		File folder = new File(inputFileDir);
		if (folder.exists())
			listFiles(folder);
	}
	
	private void listFiles(String folder) throws IOException {
		listFiles(new File(folder));
	}
	
	private void listFiles(File parentDirHandler) throws IOException {
		if (parentDirHandler.exists()) {
		   File[] allFiles = parentDirHandler.listFiles();
		    for (File f : allFiles) {
		        if (includeAllSubfolders && f.isDirectory())
		            stack.push(f);
		        else if (accept(f))
		        	stack.push(f);
		    }
		}
		else
			throw new IOException(inputFileDir + ": No such folder exists");
	}

	public File next() {
		if (!stack.empty()) {
			File file = stack.pop();
			if (file.isDirectory()) {
				try {
					listFiles(file);
					next();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else
				return file;
		}
		return null;
	}
}
