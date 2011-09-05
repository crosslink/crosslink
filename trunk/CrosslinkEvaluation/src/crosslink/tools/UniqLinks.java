package crosslink.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import ltwassessment.utility.WildcardFiles;

public class UniqLinks {
	
	public class Team {
		String 			teamName;
		Set<String>		linkSet;
	}
	
	private File[] runFileCache;
	
	
	
	public UniqLinks(int index, String[] fl) {
		this.init(index, fl);
	}

	private void init(int index, String[] fl) {
        ArrayList<File> fileList = new ArrayList<File>();
        for (int l = index; l < fl.length; l++) {
            String afile = fl[l].trim();
            if (afile.length() > 0) {
            	File aFile = new File(afile);
            	if (aFile.isDirectory())
            		fileList.addAll(WildcardFiles.listFilesInStack(afile));
            	else
            		fileList.add(aFile);  	
            }
        }
        File[] files = fileList.toArray(new File[fileList.size()]);
        this.runFileCache = files;
	}

	public static void usage() {
		System.out.println("Usage: program [options] result_file run_file1 run_file2 ...");
		System.exit(-1);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int runfileStart = 2;
		UniqLinks uniqLinks = new UniqLinks(runfileStart, args);

	}

}
