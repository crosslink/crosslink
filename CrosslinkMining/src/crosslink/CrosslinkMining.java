package crosslink;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import crosslink.utility.FileUtil;
import crosslink.utility.WildcardFiles;
import crosslink.wiki.Corpora;

import monolink.MonolinkMining;
import monolink.OutputXmlInterface;
import monolink.ResultSetXml;
import monolink.SubmissionXml;

/*
 * converted from the bash script, so mainly the naming is not changed
 */
public class CrosslinkMining extends MonolinkMining {

	public static final int OUTPUT_FORMAT_RESULTSET = 0;
	public static final int OUTPUT_FORMAT_SUBMISSION = 1;
	
	private String otherLang = "";
//			OUTPUT=

	private String targetTopicPath = null ; //~/experiments/ntcir-9-clld/topics/training/${targetLang}/
//			filename=
	
	private String crosslinkTablePath;
	private CrosslinkTable enCorpusCrosslinkTable; 
	private CrosslinkTable otherCorpusCrosslinkTable;

	private ArrayList<CrosslinkTopic> topics = new ArrayList<CrosslinkTopic>();
	
	String counterTopicId = null;
	
	/**
	 * @param targetLang the targetLang to set
	 */
	public void setTargetLang(String targetLang) {
		super.setTargetLang(targetLang);
		
		if (targetLang.equalsIgnoreCase( "en")) 
		    otherLang = sourceLang;
		else
			otherLang = targetLang;
	}

	/**
	 * @return the otherLang
	 */
	public String getOtherLang() {
		return otherLang;
	}

	/**
	 * @param otherLang the otherLang to set
	 */
	public void setOtherLang(String otherLang) {
		this.otherLang = otherLang;
	}

	/**
	 * @return the crosslinkTablePath
	 */
	public String getCrosslinkTablePath() {
		return crosslinkTablePath;
	}

	/**
	 * @param crosslinkTablePath the crosslinkTablePath to set
	 */
	public void setCrosslinkTablePath(String crosslinkTablePath) {
		this.crosslinkTablePath = crosslinkTablePath;
	}

	/**
	 * @return the targetTopicPath
	 */
	public String getTargetTopicPath() {
		return targetTopicPath;
	}

	/**
	 * @param targetTopicPath the targetTopicPath to set
	 */
	public void setTargetTopicPath(String targetTopicPath) {
		this.targetTopicPath = targetTopicPath;
	}

	static public String wikiIdToPath(String targetId) {
//        int lastPos = targetId.lastIndexOf(".xml");
        String subFolder;
        if (targetId.length() < 3)
        	subFolder = String.format("%03d", Integer.parseInt(targetId));
        else
        	subFolder = targetId.substring(targetId.length() - 3); //(targetId.length() - 7);
        return "pages" + File.separator + subFolder + File.separator + targetId;
	}
	
	public String getWikiPagePath(String id, String lang) {
		return corpusHome + File.separator + lang + File.separator + wikiIdToPath(id) + ".xml";
	}
	
	public boolean wikiPageExists(String id, String lang) {
		String filename = getWikiPagePath(id, lang);
		return new File(filename).exists();
	}
	
	private void output(CrosslinkTable table, String id) {
//		resultSetOut.outputLink(table.getTargetId(id));
//		topic.addLink(id);
		System.err.println(String.format("%s:%s:(%s, %s)", id, table.getTargetId(id), table.getSourceTitle(id), table.getTargetTitle(id)));
	}
	
	private String getOutputIdFromEnCorpus(String id) {
		String targetId = enCorpusCrosslinkTable.getTargetId(id);
		if (wikiPageExists(targetId, targetLang)) {
			output(enCorpusCrosslinkTable, id);
			return targetId;
		}

		return getOutputIdFromOtherLang(id);
 	}

	private String getOutputIdFromOtherLang(String id) {
		String targetId = null;
		targetId = otherCorpusCrosslinkTable.getTargetId(id);
		assert(targetId != null);
		if (targetId != null && wikiPageExists(targetId, targetLang)) {
			output(otherCorpusCrosslinkTable, id);
			return targetId;
		}
		else
			System.err.println(String.format("No coresponding topic for %s in %s corpus", id, targetLang));
		return null;	
	}

	private void createCrosslinkTable(String crosslinkTablePath) {
		setCrosslinkTablePath(crosslinkTablePath);
		enCorpusCrosslinkTable = new CrosslinkTable(String.format("%sen2%s_lang_links.txt", crosslinkTablePath + File.separator, otherLang), sourceLang);
		enCorpusCrosslinkTable.setLang("en");
		enCorpusCrosslinkTable.read();
		
		otherCorpusCrosslinkTable = new CrosslinkTable(String.format("%s%s2en_lang_links.txt", crosslinkTablePath + File.separator, otherLang), sourceLang);
		otherCorpusCrosslinkTable.setLang(otherLang);
		otherCorpusCrosslinkTable.read();
	}
	
	private String findCounterPartTopic(CrosslinkTopic topic) throws Exception {
		counterTopicId = extractCrosslinkFromTopics(topic.getXmlFile());
		if (counterTopicId == null) {
			String id = topic.getId();
			if (enCorpusCrosslinkTable.hasSourceId(id)) {
				counterTopicId = enCorpusCrosslinkTable.getTargetId(id);
				if (!wikiPageExists(counterTopicId, targetLang))
					counterTopicId = null;
			}
			if (counterTopicId == null && otherCorpusCrosslinkTable.hasSourceId(id))
				counterTopicId = otherCorpusCrosslinkTable.getTargetId(id);
			if (counterTopicId == null)
				throw new Exception("No counterpart find for " + topic.getTitle() + " : " + id);
		}
		
		String targetTopicFile = targetTopicPath + File.separator + counterTopicId + ".xml";
		
		if (! new File(targetTopicFile).exists()) {
			String wikiFilePath = getWikiPagePath(counterTopicId, targetLang);
			FileUtil.copyFile(wikiFilePath, targetTopicFile);
		}
		return targetTopicFile;
	}
	
	private void getIndirectLinks(ArrayList<String> links, CrosslinkTopic topic) {
    	for (String id : links) {
    		String targetId = null;
			if (enCorpusCrosslinkTable.hasSourceId(id)) 
				targetId = getOutputIdFromEnCorpus(id);
			else if ((otherCorpusCrosslinkTable.hasSourceId(id))) 
				targetId = getOutputIdFromOtherLang(id);
			
			if (targetId != null)
				topic.addLink(targetId);
        }		
    	System.err.println(String.format("Found %d indirect links for topic %s - %s", topic.getLinks().size(), topic.getId(), topic.getTitle()));
	}
	
 	private void getTopicLinks(String topicPath, String lang) {
        int filecount = 0;
      	Stack<File> stack = null;
      	
      	stack = new WildcardFiles().listFilesInStack(topicPath);
      	while (!stack.isEmpty())
        {
//          		good = true;
            File onefile = (File)stack.pop();
            ++filecount;
        	try {
	        	String inputfile = onefile.getCanonicalPath();
	        	if (inputfile.indexOf("101991.xml") > -1)
	        		System.err.println("I got you");
	        	CrosslinkTopic topic = new CrosslinkTopic(new File(inputfile));
	        	topics.add(topic);
        	
	        	ArrayList<String> links = extractLinksFromTopics(inputfile);
	        	getIndirectLinks(links, topic);
	        	
	        	String counterPartTopicFile = findCounterPartTopic(topic);
	        	topic.setCounterTopic(new CrosslinkTopic(new File(counterPartTopicFile)));
	        	ArrayList<String> directLinks = extractLinksFromTopics(counterPartTopicFile);
	        	getDirectLinks(directLinks, topic.getCounterPart());
        	}
            catch (Exception e) {
				//recordError(inputfile, "IOException");
				e.printStackTrace();
            } 
            finally {

            }
        }
 
 	}

	public void findWikiGroundTruth(int format) {

		getTopicLinks(sourceTopicPath, sourceLang);
//		getTopicLinks(targetTopicPath, targetLang, true);
		
		OutputXmlInterface xml = null;
		switch (format) {
		case OUTPUT_FORMAT_SUBMISSION:
			xml = new SubmissionXml(sourceLang, targetLang);
			break;
		
		case OUTPUT_FORMAT_RESULTSET:
		default:
			xml = new ResultSetXml(sourceLang, targetLang);
			break;
		}
		createOutput(xml);
		System.out.println(xml.toString());
	}
	
	private void createOutput(OutputXmlInterface xml) {
		xml.open();
		for (CrosslinkTopic topic : topics) {
			xml.outputTopicStart(topic.getTitle(), topic.getId());
			xml.outputAnchorStart(topic.getTitle(), topic.getTitlePos(), topic.getTitleLength());
			
			Set<String> indirectLinks = topic.getLinks();
			Iterator it = indirectLinks.iterator();
			while (it.hasNext()) {
			    // Get element
				xml.outputLink((String) it.next());
			}
			if (topic.getCounterPart() != null) {
			Set<String> directLinks = topic.getCounterPart().getLinks();
				it = directLinks.iterator();
				while (it.hasNext()) {
				    String id = (String) it.next();
				    if (!indirectLinks.contains(id))
				    	xml.outputLink(id);
				}		
			}
			xml.outputAnchorEnd();
        	xml.outputTopicEnd();	
		}
		xml.close();
		
	}
	
	public static void usage() {
		System.out.println("arg[0] source and target language pair, e.g en:zh");
		System.out.println("arg[1] crosslink tables path");
		System.out.println("arg[2] source topics path");
		System.out.println("arg[3] target topics path");
		System.out.println("arg[4] corpora home: e.g. /corpus");
		System.out.println("arg[5] run|rs");
		System.exit(-1);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 4)
			usage();
		
		int format = CrosslinkMining.OUTPUT_FORMAT_RESULTSET;
		
		CrosslinkMining mining = new CrosslinkMining();
		String[] arr = args[0].split(":");
		if (arr.length != 2)
			usage();
		mining.setSourceLang(arr[0]);
		mining.setTargetLang(arr[1]);

		mining.createCrosslinkTable(args[1]);
		
		mining.setSourceTopicPath(args[2]);
		mining.setTargetTopicPath(args[3]);
		
		Corpora.initialize();
		Corpora.getInstance().setLang(mining.getTargetLang());
		
		if (args.length > 4)
			Corpora.getInstance().setHome(args[4]);
		else
			Corpora.getInstance().setHome(mining.getCorpusHome());
		
		if (args.length > 5) {
			if (args[5].equalsIgnoreCase("run"))
				format = CrosslinkMining.OUTPUT_FORMAT_SUBMISSION;
			else if ((args[5].equalsIgnoreCase("rs")))
				;
			else
				usage();
		}
		
		mining.findWikiGroundTruth(format);
	}

	protected String extractCrosslinkFromTopics(String inputfile) {
		ArrayList<String> links = extractLinksFromTopics(inputfile, "-valid-target-only -crosslink:" + targetLang, 0);
		if (links.size() > 0) {
			if (links.size() > 1)
				System.err.println("Alter: more than 1 language links were found.");
			return links.get(links.size() - 1);
		}
		return null;
	}
}
