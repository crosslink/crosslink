package monolink;

import java.io.File;

import ltwassessment.wiki.Corpora;
import ltwassessment.wiki.WikiArticleXml;

public class SubmissionXml extends OutputXml implements OutputXmlInterface {
	public static final String START = "<crosslink-submission participant-id=\"GT\" run-id=\"GroundTruth\" task=\"F2F\" default_lang=\"%s\" source_lang=\"%s\">" +
	"<details>\n " +
		"<machine>\n" +
		"	<cpu>Core 2 Duo</cpu>\n" +
		"	<speed>2.33 MHz</speed>\n" +
		"	<cores>2</cores>\n" +
		"	<hyperthreads>1</hyperthreads>\n" +
		"	<memory>8GB</memory>\n" +
		"</machine>\n" +
		"<time>TBC</time>\n" +
	"</details>\n" +
	"<description>\n" + 
		"This is the ground truth run\n" +
	"</description>\n" +
	"<collections><collection>CJK Wikipedia</collection></collections>\n";
	
	
	public static final String END = "</crosslink-submission>";
	
	public static final String TOPIC_START = "\t<topic file=\"%s\" name=\"%s\"> \n" + 
			"\t\t<outgoing>\n" +
			"";
	
	public static final String TOPIC_END =	"" +
				"\t\t</outgoing>\n" + 
				"\t</topic>\n";
	
	public static final String ANCHOR_START_BLANK = "\t\t\t<anchor offset=\"-1\" length=\"0\" name=\"\">\n";
	
	public static final String ANCHOR_END = "\t\t\t</anchor>\n";
			
	
	public static final String LINK = "\t\t\t<tofile>%s</tofile>\n";
	
	public static final String LINK2 = "\t\t\t<tofile bep_offset=\"%d\" lang=\"%s\" title=\"%s\">%s</tofile>\n";
	
	public SubmissionXml(String sourceLang, String targetLang) {
		super(sourceLang, targetLang);
	}

	public void open() {
		String temp = String.format(START, targetLang, sourceLang);
		xmlbuf.append(temp);
	}

	public void outputTopicStart(String name, String id) {
		String temp = String.format(TOPIC_START, name, id);
		xmlbuf.append(temp);
		outputAnchorStart();
	}

	public void outputLink(String targetId) {
//		String temp = String.format(LINK, targetId);
		String title = null;
		String wikiFilePath = Corpora.getInstance().getWikiFilePath(targetId);
		File wikiFile = new File(wikiFilePath);
		if (!wikiFile.exists()) {
			System.err.println(wikiFilePath + " not found!");
			return;
		}
		
		WikiArticleXml article = new WikiArticleXml(wikiFile);
		title = article.getTitle();
		
		String temp = String.format(LINK2, 0, targetLang, title, targetId);
		xmlbuf.append(temp);
	}

	public void outputLink(String id, String title, int length, int bep) {
		String temp = String.format(LINK2, bep, targetLang, title, id);
		xmlbuf.append(temp);
	}

	public void outputTopicEnd() {
		outputAnchorEnd();
		xmlbuf.append(TOPIC_END);	
	}

	public void close() {
		xmlbuf.append(END);	
	}

	@Override
	public void outputAnchorStart() {
		xmlbuf.append(ANCHOR_START_BLANK);
	}

	@Override
	public void outputAnchorEnd() {
		xmlbuf.append(ANCHOR_END);
	}
	
	
	
}
