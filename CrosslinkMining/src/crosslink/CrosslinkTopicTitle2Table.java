package crosslink;

import java.io.File;
import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;

import monolink.MonolinkMining;

import crosslink.utility.WildcardFileStack;
import crosslink.wiki.WikiArticleXml;

public class CrosslinkTopicTitle2Table {
	
	public static final String[] langs = {"zh", "ja", "ko"};
	
	private String topicPath;
	private HashMap<String, String> langTopicPathMap = new HashMap<String, String>();
	
	private CrosslinkTable enCorpusCrosslinkTable; 
	private CrosslinkTable otherCorpusCrosslinkTable;
	
	HashMap<String, CrosslinkTable> enLangLinksMap = new HashMap<String, CrosslinkTable>();
	HashMap<String, CrosslinkTable> otherLangLinksMap = new HashMap<String, CrosslinkTable>();

	private String corpusHome = "/data/corpus/wikipedia/all";
	
	private Vector<TopicTitles> rows = new Vector<TopicTitles>();
	
	public class TopicTitles {
		int row;
		HashMap<String, String> titleMap = new HashMap<String, String>(); 
		
		public String toString() {
			return String.format("%d\t%s\t%s\t%s\t%s", row, titleMap.get("en"), titleMap.get("zh"), titleMap.get("ja"), titleMap.get("ko"));
		}
	}
	
	public String getTopicPath() {
		return topicPath;
	}


	public void setTopicPath(String topicPath) {
		this.topicPath = topicPath;
	}

	public String getWikiPagePath(String id, String lang) {
		return langTopicPathMap.get(lang)  + File.separator +id + ".xml";
	}
	
	public boolean wikiPageExists(String id, String lang) {
		String filename = getWikiPagePath(id, lang);
		return new File(filename).exists();
	}
	
	public String findCounterPartTopic(CrosslinkTopic topic, String lang) throws Exception {
		String topicId = null;
		enCorpusCrosslinkTable = enLangLinksMap.get(lang);
		otherCorpusCrosslinkTable = otherLangLinksMap.get(lang);
		
		topicId = CrosslinkMining.extractCrosslinkFromTopics(topic.getXmlFile(), lang);
		if (topicId == null) {
			String id = topic.getId();
			if (enCorpusCrosslinkTable.hasSourceId(id)) {
				topicId = enCorpusCrosslinkTable.getTargetId(id);
				if (!wikiPageExists(topicId, lang))
					topicId = null;
			}
			if (topicId == null && otherCorpusCrosslinkTable.hasSourceId(id))
				topicId = otherCorpusCrosslinkTable.getTargetId(id);
			if (topicId == null)
				throw new Exception("No counterpart find for " + topic.getTitle() + " : " + id);
		}
		return topicId;
	}
	
	public void assignLangTopicPath(String lang) {
		WildcardFileStack filestack;
		try {
			filestack = new WildcardFileStack(topicPath, String.format("*%s*", lang));
			Stack<File> stack = filestack.list();
			for (File file : stack) {
				langTopicPathMap.put(lang, file.getAbsolutePath());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createTable() {
		
		try {
			this.assignLangTopicPath("en");
			for (String lang: langs)
				this.assignLangTopicPath(lang);

			
			WildcardFileStack topicStack = new WildcardFileStack(langTopicPathMap.get("en"));
			Stack<File> topicFiles = topicStack.list();
			
			int count = 0;
			for (File topicFile: topicFiles) {
				CrosslinkTopic topic = new CrosslinkTopic(topicFile);
				TopicTitles titles = new TopicTitles();
				titles.row = ++count;
				titles.titleMap.put("en", topic.getTitle());
				
				for (String lang : langs) {
					String counterTopicId = findCounterPartTopic(topic, lang);
					WikiArticleXml xml = new WikiArticleXml(new File(this.getWikiPagePath(counterTopicId, lang)));
					titles.titleMap.put(lang, xml.getTitle());
				}
				
				System.out.println(titles.toString());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	private void loadLangLinksMap(String crosslinkTablePath) {
		for (String otherLang : langs) {
			enCorpusCrosslinkTable = new CrosslinkTable(String.format("%sen2%s_lang_links.txt", crosslinkTablePath + File.separator + "en" + File.separator, otherLang), "en");
			enCorpusCrosslinkTable.setLang("en");
			enCorpusCrosslinkTable.read();
			
			otherCorpusCrosslinkTable = new CrosslinkTable(String.format("%s%s2en_lang_links.txt", crosslinkTablePath + File.separator  + otherLang + File.separator, otherLang), "en");
			otherCorpusCrosslinkTable.setLang(otherLang);
			otherCorpusCrosslinkTable.read();		
			
			enLangLinksMap.put(otherLang, enCorpusCrosslinkTable);
			otherLangLinksMap.put(otherLang, otherCorpusCrosslinkTable);
		}
	}


	public static void main(String[] args) {
		if (args.length < 1)
			usage();
		
		CrosslinkTopicTitle2Table builder = new CrosslinkTopicTitle2Table();
		
		builder.setTopicPath(args[0]);
		builder.loadLangLinksMap(args[1]);
		
		builder.createTable();
	}

	public static void usage() {
		System.out.println("arg[0] overall topic path");
		System.out.println("arg[1] crosslink tables path");
//		System.out.println("arg[2] source topics path");
//		System.out.println("arg[3] target topics path");
//		System.out.println("arg[4] corpora home: e.g. /corpus");
//		System.out.println("arg[5] run|rs");
		System.exit(-1);
	}
	
}
