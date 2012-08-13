package monolink;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import crosslink.utility.WildcardFiles;
import crosslink.wiki.Corpora;


public class MonolinkMining {
	
	protected String sourceLang = "zh";
	protected String targetLang = "en";
	
	protected String sourceTopicPath = null;
	
	protected String corpusHome = "/data/corpus/wikipedia/all/";
	
	protected ResultSetXml resultSetOut = null;
	
	private ArrayList<MonolinkTopic> topics = new ArrayList<MonolinkTopic>();
	
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
	}
	
	/**
	 * @param corpusHome the corpusHome to set
	 */
	public void setCorpusHome(String corpusHome) {
		this.corpusHome = corpusHome;
	}
	
	/**
	 * @return the corpusHome
	 */
	public String getCorpusHome() {
		return corpusHome;
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
	
	protected ArrayList<String>extractLinksFromTopics(String inputfile) {
		return extractLinksFromTopics(inputfile, "");
	}
	
	protected ArrayList<String> extractLinksFromTopics(String inputfile, String parameter) {
		ArrayList<String> links = new ArrayList<String>();
        String osName = System.getProperty("os.name");
		//      String Command = Command2;
		String Command = "/usr/local/bin/link_extract " + parameter + inputfile;
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
	
	protected void getDirectLinks(ArrayList<String> links, MonolinkTopic topic) {
    	for (String link : links)
    		topic.addLink(link);
    	System.err.println(String.format("Found %d direct links for topic %s - %s", topic.getLinks().size(), topic.getId(), topic.getTitle()));
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
	        	MonolinkTopic topic = new MonolinkTopic(new File(inputfile));
	        	topics.add(topic);
        	
	        	ArrayList<String> directLinks = extractLinksFromTopics(inputfile);
	        	getDirectLinks(directLinks, topic);
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
		for (MonolinkTopic topic : topics) {
			resultSetOut.outputTopicStart(topic.getTitle(), topic.getId());
			Set<String> directLinks = topic.getLinks();
			Iterator it = directLinks.iterator();
			while (it.hasNext()) {
			    String id = (String) it.next();
			    if (!directLinks.contains(id))
			    	resultSetOut.outputLink(id);
			}		
        	resultSetOut.outputTopicEnd();	
		}
		resultSetOut.close();
	}	
	
	public static void usage() {
		System.out.println("arg[0] topic lang, e.g en, zh");
		System.out.println("arg[1] topics path");
		System.out.println("arg[2] corpora home");
		System.exit(-1);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 4)
			usage();
		// TODO Auto-generated method stub
		MonolinkMining mining = new MonolinkMining();
		String[] arr = args[0].split(":");
		if (arr.length != 2)
			usage();
		mining.setSourceLang(arr[0]);
		mining.setTargetLang(arr[0]);
		
		mining.setSourceTopicPath(args[2]);
		
		Corpora.initialize();
		if (args.length > 4) 
			Corpora.getInstance().setHome(args[4]);
		else
			Corpora.getInstance().setHome(mining.getCorpusHome());
		
		mining.findWikiGroundTruth();

	}

}
