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

import ltwassessment.utility.FileUtil;
import ltwassessment.utility.WildcardFiles;

/*
 * converted from the bash script, so mainly the naming is not changed
 */
public class CrosslinkMining {
	
	private String sourceLang = "zh";
	private String targetLang = "en";

	private String otherLang = "";
//			OUTPUT=

	private String corpusHome = "/data/corpus/wikipedia/all/";
	private String sourceTopicPath = null;
	private String targetTopicPath = null ; //~/experiments/ntcir-9-clld/topics/training/${targetLang}/
//			filename=
	
	private String crosslinkTablePath;
	private CrosslinkTable enCorpusCrosslinkTable; 
	private CrosslinkTable otherCorpusCrosslinkTable;

	private ResultSetXml resultSetOut = new ResultSetXml();
	private ArrayList<CrosslinkTopic> topics = new ArrayList<CrosslinkTopic>();

	/**
	 * @return the sourceLang
	 */
	public String getSourceLang() {
		return sourceLang;
	}

	/**
	 * @param sourceLang the sourceLang to set
	 */
	public void setSourceLang(String sourceLang) {
		this.sourceLang = sourceLang;
	}

	/**
	 * @return the targetLang
	 */
	public String getTargetLang() {
		return targetLang;
	}

	/**
	 * @param targetLang the targetLang to set
	 */
	public void setTargetLang(String targetLang) {
		this.targetLang = targetLang;
		
		if ( targetLang.equalsIgnoreCase( "en")) 
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
	 * @return the corpusHome
	 */
	public String getCorpusHome() {
		return corpusHome;
	}

	/**
	 * @param corpusHome the corpusHome to set
	 */
	public void setCorpusHome(String corpusHome) {
		this.corpusHome = corpusHome;
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
	 * @return the sourceTopicPath
	 */
	public String getSourceTopicPath() {
		return sourceTopicPath;
	}

	/**
	 * @param sourceTopicPath the sourceTopicPath to set
	 */
	public void setSourceTopicPath(String sourceTopicPath) {
		this.sourceTopicPath = sourceTopicPath;
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
		if (wikiPageExists(targetId, otherLang)) {
			output(enCorpusCrosslinkTable, id);
			return targetId;
		}

		return getOutputIdFromOtherLang(id);
 	}

	private String getOutputIdFromOtherLang(String id) {
		String targetId = null;
		targetId = otherCorpusCrosslinkTable.getTargetId(id);
		assert(targetId != null);
		if (targetId != null && wikiPageExists(targetId, otherLang)) {
			output(otherCorpusCrosslinkTable, id);
			return targetId;
		}
		else
			System.err.println(String.format("No coresponding topic for %s in %s corpus", id, targetLang));
		return null;	
	}

	private void createCrosslinkTable(String crosslinkTablePath) {
		setCrosslinkTablePath(crosslinkTablePath);
		enCorpusCrosslinkTable = new CrosslinkTable(String.format("%sen_corpus_%s2en.txt", crosslinkTablePath + File.separator, otherLang), sourceLang);
		enCorpusCrosslinkTable.setLang("en");
		enCorpusCrosslinkTable.read();
		
		otherCorpusCrosslinkTable = new CrosslinkTable(String.format("%s%s_corpus_en2%s.txt", crosslinkTablePath + File.separator, otherLang, otherLang), sourceLang);
		otherCorpusCrosslinkTable.setLang(otherLang);
		otherCorpusCrosslinkTable.read();
	}
	
	private ArrayList<String> extractLinksFromTopics(String inputfile) {
		ArrayList<String> links = new ArrayList<String>();
        String osName = System.getProperty("os.name");
		//      String Command = Command2;
		String Command = "/usr/local/bin/link_extract " + inputfile;
		Process runCommand;
		//if (osName.equalsIgnoreCase("Linux")) {
		//  Command = Command2;
		//runCommand = Runtime.getRuntime().exec(Command, null, fileHandlder);
		//}
		//else if (osName.equalsIgnoreCase("Solaris") || osName.equalsIgnoreCase("SunOS")) {
		//  runCommand = Runtime.getRuntime().exec(new String[] {Command10, Command11, outputFile}, null, fileHandlder); //Command = Command10 + Command11;
		//} else
	    try {
			runCommand = Runtime.getRuntime().exec(Command);

	
			//runCommand = Runtime.getRuntime().exec(new String[] {"bash", "-c \"cd /home/tangl3/corpus/wikipedia/wuuwiki/xml/wuu/; tar cjvf /home/tangl3/corpus/wikipedia/wuuwiki/xml/wuuwiki-20100207-pages-articles.xml.bz2 *\""}); // ; \\rm -rf /home/tangl3/corpus/wikipedia/wuuwiki/xml//wuu/*"});
			//runCommand = Runtime.getRuntime().exec(Command, null, fileHandlder);
			
			BufferedReader Resultset = new BufferedReader(
			        new InputStreamReader (
			                runCommand.getInputStream(), "UTF-8"));
			
			String line;
			while ((line = Resultset.readLine()) != null) {
	 			String[] arr = line.split(":");
	 			String sourceId = arr[0];
	 			String targetId = arr[1];

				links.add(targetId);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return links;
	}
	
	private String findCounterPartTopic(CrosslinkTopic topic) throws Exception {
		String topicId = null;
		String id = topic.getId();
		if (enCorpusCrosslinkTable.hasSourceId(id)) {
			topicId = enCorpusCrosslinkTable.getTargetId(id);
			if (!wikiPageExists(topicId, targetLang))
				topicId = null;
		}
		if (topicId == null && otherCorpusCrosslinkTable.hasSourceId(id))
			topicId = otherCorpusCrosslinkTable.getTargetId(id);
		if (topicId == null)
			throw new Exception("No counterpart find for " + topic.getTitle() + " : " + id);
		
		String targetTopicFile = targetTopicPath + File.separator + topicId + ".xml";
		
		if (! new File(targetTopicFile).exists()) {
			String wikiFilePath = getWikiPagePath(topicId, targetLang);
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
    	System.err.println(String.format("Found %d indirect links", topic.getLinks().size()));
	}
	
	private void getDirectLinks(ArrayList<String> links, CrosslinkTopic topic) {
    	for (String link : links)
    		topic.addLink(link);
    	System.err.println(String.format("Found %d direct links", topic.getLinks().size()));
	}
	
 	private void getTopicLinks(String topicPath, String lang) {
        int filecount = 0;
      	Stack<File> stack = null;
      	
      	stack = WildcardFiles.listFilesInStack(topicPath);
      	while (!stack.isEmpty())
        {
//          		good = true;
            File onefile = (File)stack.pop();
            ++filecount;
        	try {
	        	String inputfile = onefile.getCanonicalPath();
	        	CrosslinkTopic topic = new CrosslinkTopic(inputfile);
	        	topics.add(topic);
        	
	        	ArrayList<String> links = extractLinksFromTopics(inputfile);
	        	getIndirectLinks(links, topic);
	        	
	        	String counterPartTopicFile = findCounterPartTopic(topic);
	        	topic.setCounterTopic(new CrosslinkTopic(counterPartTopicFile));
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

 	

	public void findWikiGroundTruth() {

		getTopicLinks(sourceTopicPath, sourceLang);
//		getTopicLinks(targetTopicPath, targetLang, true);
		createResultSet();
		
		System.out.println(resultSetOut.toString());
	}
	
	public void createResultSet() {
		resultSetOut.open();
		for (CrosslinkTopic topic : topics) {
			resultSetOut.outputTopicStart(topic.getTitle(), topic.getId());
			Set<String> indirectLinks = topic.getLinks();
			Iterator it = indirectLinks.iterator();
			while (it.hasNext()) {
			    // Get element
				resultSetOut.outputLink((String) it.next());
			}
			Set<String> directLinks = topic.getCounterPart().getLinks();
			it = directLinks.iterator();
			while (it.hasNext()) {
			    String id = (String) it.next();
			    if (!indirectLinks.contains(id))
			    	resultSetOut.outputLink(id);
			}		
        	resultSetOut.outputTopicEnd();	
		}
		resultSetOut.close();
	}
	
	public static void usage() {
		System.out.println("arg[0] source and target language pair, e.g en:zh");
		System.out.println("arg[1] crosslink table path");
		System.out.println("arg[2] source topics path");
		System.out.println("arg[3] target topics path");
		System.out.println("arg[4] corpora home");
		System.exit(-1);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 4)
			usage();
		// TODO Auto-generated method stub
		CrosslinkMining mining = new CrosslinkMining();
		String[] arr = args[0].split(":");
		if (arr.length != 2)
			usage();
		mining.setSourceLang(arr[0]);
		mining.setTargetLang(arr[1]);

		mining.createCrosslinkTable(args[1]);
		
		mining.setSourceTopicPath(args[2]);
		mining.setTargetTopicPath(args[3]);
		
		if (args.length > 5)
			mining.setCorpusHome(args[4]);
		
		mining.findWikiGroundTruth();
	}

}
