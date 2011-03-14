package crosslink;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Stack;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
	
	public boolean wikiPageExists(String id, String lang) {
		String filename = corpusHome + File.separator + lang + File.separator + wikiIdToPath(id) + ".xml";
		return new File(filename).exists();
	}
	
	private void output(CrosslinkTable table, String id) {
		System.out.println(String.format("%s:%s:(%s, %s)", id, table.getTargetId(id), table.getSourceTitle(id), table.getTargetTitle(id)));
	}
	
	private void getOutputIdFromEnCorpus(String id) {
		String targetId = enCorpusCrosslinkTable.getTargetId(id);
		if (wikiPageExists(targetId, otherLang))
			output(enCorpusCrosslinkTable, id);

		getOutputIdFromOtherLang(id);
 	}

	private void getOutputIdFromOtherLang(String id) {
		String targetId = otherCorpusCrosslinkTable.getTargetId(id);
		if (wikiPageExists(targetId, otherLang))
			output(otherCorpusCrosslinkTable, id);
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
	
 	private void getTopicLinks(String topicPath) {
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
            	try {
                    String osName = System.getProperty("os.name");
//                    String Command = Command2;
                    String Command = "/usr/local/bin/link_extract " + inputfile;
                    Process runCommand;
//                    if (osName.equalsIgnoreCase("Linux")) {
//                        Command = Command2;
//                        runCommand = Runtime.getRuntime().exec(Command, null, fileHandlder);
//                    }
//                    else if (osName.equalsIgnoreCase("Solaris") || osName.equalsIgnoreCase("SunOS")) {
//                        runCommand = Runtime.getRuntime().exec(new String[] {Command10, Command11, outputFile}, null, fileHandlder); //Command = Command10 + Command11;
//                    } else
                        runCommand = Runtime.getRuntime().exec(Command);

                    //runCommand = Runtime.getRuntime().exec(new String[] {"bash", "-c \"cd /home/tangl3/corpus/wikipedia/wuuwiki/xml/wuu/; tar cjvf /home/tangl3/corpus/wikipedia/wuuwiki/xml/wuuwiki-20100207-pages-articles.xml.bz2 *\""}); // ; \\rm -rf /home/tangl3/corpus/wikipedia/wuuwiki/xml//wuu/*"});
                    //runCommand = Runtime.getRuntime().exec(Command, null, fileHandlder);

                    BufferedReader Resultset = new BufferedReader(
                            new InputStreamReader (
                                    runCommand.getInputStream(), "UTF-8"));

                    String line;
                    while ((line = Resultset.readLine()) != null) {
//                        System.out.println(line);
//					 	target_id=`echo $line | cut -f 2 -d :`
			 			String[] arr = line.split(":");
			 			String sourceId = arr[0];
			 			String targetId = arr[1];

			 			
//			 			int count  = 0; //crosslinkTable.getTargetCount(pagePath);
			 			String id;
			 			if (sourceLang.equalsIgnoreCase("en"))
			 				id = targetId;
			 			else
			 				id = sourceId;
			 			if (enCorpusCrosslinkTable.hasSourceId(id)) {

//			 				if (otherCorpusCrosslinkTable.hasSourceId(sourceId))
			 					getOutputIdFromEnCorpus(id);
//			 				else
//			 					getOutputIdFromOtherLang(pagePath);
			 			}
                    }

//                    Resultset = new BufferedReader(
//                            new InputStreamReader (
//                                    runCommand.getErrorStream()));
//                    while ((line = Resultset.readLine()) != null)
//                        System.out.println(line);


                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            catch (IOException e) {
				//recordError(inputfile, "IOException");
				e.printStackTrace();
            } 
        }
 
 	}


	public void findWikiGroundTruth() {

// 		sourceCROSSLINK_MERGED_TABLE=~/experiments/ntcir-9-clld/assessment/wikipedia_groundtruth/link-mining/all/${sourceLang}2${targetLang}_merged.txt
// 		targetCROSSLINK_MERGED_TABLE=~/experiments/ntcir-9-clld/assessment/wikipedia_groundtruth/link-mining/all/${targetLang}2${sourceLang}_merged.txt

// 		./find_crosslingual_topics.sh $targetTopicPath $sourceTopicPath ~/experiments/ntcir-9-clld/assessment/wikipedia_groundtruth/link-mining/${targetLang}-${sourceLang/${targetLang}2${sourceLang}_table.txt $corpusHome 

// 		get_topic_link $targetTopicPath $targetCROSSLINK_MERGED_TABLE
// 		get_topic_link $sourceTopicPath $sourceCROSSLINK_MERGED_TABLE
//
		getTopicLinks(sourceTopicPath);
		getTopicLinks(targetTopicPath);
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
