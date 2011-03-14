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
	private String lang;
	private String sourceLang;
	private boolean isFromEnglishCorpus;
	
	private HashMap<String, CrosslinkTableEntry> mapForSourceId = new HashMap<String, CrosslinkTableEntry>();
//	private HashMap<String, CrosslinkTableEntry> mapForTargetId = new HashMap<String, CrosslinkTableEntry>();

	/**
	 * @return the isFromEnglishCorpus
	 */
	public boolean isFromEnglishCorpus() {
		return isFromEnglishCorpus;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	/**
	 * @param isFromEnglishCorpus the isFromEnglishCorpus to set
	 */
	public void setFromEnglishCorpus(boolean isFromEnglishCorpus) {
		this.isFromEnglishCorpus = isFromEnglishCorpus;
	}

	public CrosslinkTable(String tablePath, String sourceLang) {
		super();
		this.tablePath = tablePath;
		this.sourceLang = sourceLang;
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
	    		String[] titles = new String(tokens[2].getBytes(), "UTF-8").split("\\|");
	    		if (titles.length != 2)
	    			continue;
	    		String targetTitle = null;
	    		String sourceTitle = null; 		
	    		String sourceId = null;
	    		String targetId = null;
	    		
	    		CrosslinkTableEntry entry = null;
	    		
	    		if (!sourceLang.equalsIgnoreCase(lang)) {
		    		 targetTitle = titles[1];
		    		 sourceTitle = titles[0]; 		
		    		 sourceId = tokens[0];
		    		 targetId = tokens[1];
	    		}
	    		else {
		    		 targetTitle = titles[0];
		    		 sourceTitle = titles[1]; 		
		    		 sourceId = tokens[1];
		    		 targetId = tokens[0];
	    		}
	    		
	    		if (Integer.parseInt(sourceId) == 0 || Integer.parseInt(targetId) == 0)
	    			continue;
	    		
    			entry = new CrosslinkTableEntry(sourceId, targetId, sourceTitle, targetTitle);
	    		mapForSourceId.put(sourceId, entry);
//	    		mapForTargetId.put(targetId, entry);
				
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
		return mapForSourceId.containsKey(id);
	}
	
//	public boolean hasTargetId(String id) {
//		return mapForTargetId.containsKey(id);
//	}

	public int getTargetCount(String pagePath) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getTargetId(String sourceId) {
		return mapForSourceId.get(sourceId).getTargetId();
	}
	
	public String getSourceTitle(String sourceId) {
		return mapForSourceId.get(sourceId).getSourceTitle();
	}
	
	public String getTargetTitle(String sourceId) {
		return mapForSourceId.get(sourceId).getTargetTitle();
	}	
}
