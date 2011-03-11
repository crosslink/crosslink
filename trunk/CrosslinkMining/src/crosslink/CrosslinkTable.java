package crosslink;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import ltwassessment.utility.WildcardFiles;

//import org.apache.tools.bzip2.CBZip2InputStream;

import com.spreada.utils.chinese.ZHConverter;

/*
 * 	# the link table created from any link extraction software which produce following format
	# <source_document>:<target_document>:<source_document_title>|<target_document_title>
	# 
 */

public class CrosslinkTable {
	private String tablePath;
	
	private boolean isFromEnglishCorpus;
	
	private HashMap<String, CrosslinkTableEntry> table = new HashMap<String, CrosslinkTableEntry>();

	/**
	 * @return the isFromEnglishCorpus
	 */
	public boolean isFromEnglishCorpus() {
		return isFromEnglishCorpus;
	}

	/**
	 * @param isFromEnglishCorpus the isFromEnglishCorpus to set
	 */
	public void setFromEnglishCorpus(boolean isFromEnglishCorpus) {
		this.isFromEnglishCorpus = isFromEnglishCorpus;
	}

	public CrosslinkTable(String tablePath) {
		super();
		this.tablePath = tablePath;
	}

	public void read() {
		try {
		    FileInputStream fis=new FileInputStream(tablePath);			
	        InputStream stream=null;
	        BufferedReader reader=null;
	
	        stream=new BufferedInputStream(fis);
	    	reader=new BufferedReader(new InputStreamReader(stream));
	    	
	    	int cnt=0;
	    	
	    	while (true)
	    	{
	    		String line=reader.readLine();
	    		
	    		if (line==null)
	    		{
	    			break;
	    		}
	    		
	    		//        		System.out.println(line);
	    		
	    		String tokens[]=line.split(":");
	    		
	//    		if (tokens.length!=2) continue;
	    		String[] titles = tokens[2].split("|");
	    		if (titles.length != 2)
	    			continue;
	    		String targetTitle = titles[0];
	    		String sourceTitle = titles[1];
	    		
	    		String sourceId = tokens[0];
	    		String targetId = tokens[1];
	    		
	    		CrosslinkTableEntry entry = new CrosslinkTableEntry(sourceId, targetId, sourceTitle, targetTitle);
	    		
	    		table.put(sourceId, entry);
				
	//    		if (lang.equals("zh"))
	//    			articles.get(lang).put(ZHConverter.convert(tokens[0], ZHConverter.SIMPLIFIED), tokens[1]);
	//    		else
	//    			articles.get(lang).put(tokens[0], tokens[1]);
		    }
		}
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    	System.err.println(e);
	    	e.printStackTrace();
	    }

	}
	
	public boolean hasSourceId(String id) {
		return table.containsKey(id);
	}

	public int getTargetCount(String pagePath) {
		// TODO Auto-generated method stub
		return 0;
	}
}
