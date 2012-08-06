package crosslink;

import java.io.File;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import crosslink.AppResource;
import crosslink.parsers.ResourcesManager;
import crosslink.submission.Anchor;
import crosslink.submission.LinkedAnchors;
import crosslink.submission.Target;
import crosslink.submission.Topic;

public class RunDescription2Table {

	public static void usage() {
		System.err.println("Usage: [-l source_lang:target_lang] program submissions_path");
		System.err.println("		-l language pair, zh:en, en:zh, en:ja, en:ko ...");
		System.exit(-1);	
	}
	
	public void read(File runFile, String sourceLang, String targetLang) {
        String afTitleTag = "crosslink-submission";
        String descTagStr = "description";
        String runName;

        Document xmlDoc = ResourcesManager.readingXMLFromFile(runFile.getAbsolutePath());

        NodeList titleNodeList = xmlDoc.getElementsByTagName(afTitleTag);
        byte[] bytes = null;
        
		int aOffset = 0;
		int aLength = 0;
        for (int i = 0; i < titleNodeList.getLength(); i++) {
            Element titleElmn = (Element) titleNodeList.item(i);
            runName =  titleElmn.getAttribute("run-id");
            String description = null;
            
            String runSourceLang = titleElmn.getAttribute("source_lang").trim().toLowerCase();
            if (runSourceLang.length() == 0)
            	runSourceLang = "en";
            String runTargetLang = titleElmn.getAttribute("default_lang").trim().toLowerCase();
            
        	if (!runSourceLang.equalsIgnoreCase(sourceLang) || !runTargetLang.equalsIgnoreCase(targetLang)) {
        		System.err.println("Different source/target lang: " + runSourceLang + "/" + runTargetLang + " for run file " + runFile.getName());
        		break;
        	}
            
            NodeList descNodeList = titleElmn.getElementsByTagName(descTagStr);
            for (int j = 0; j < descNodeList.getLength(); j++) {
                Element descElmn = (Element) descNodeList.item(j);
                description = descElmn.getTextContent();
                break;     
            }
            description = description.replaceAll("\\n", " ");
            description = description.replaceAll("\\t", " ");
            description = description.replaceAll("\\r", " ");
            System.out.println(runName + "\t" + description);
            break;
        } // for

	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String langPair = null;
		String path = null;
		int param_start = 0;
		
		RunDescription2Table toTable = new RunDescription2Table();
		
		for (int i = 0; i < args.length; ++i) {
			if (args[i].charAt(0) == '-') {
				if (args[i].charAt(1) == 'l' ) {
					langPair = args[++i];
					param_start += 2;
				}
				else
					usage();
			}
			else
				break;
		}
		if (param_start >= args.length)
			usage();
		
		path = args[param_start];

		toTable.printTable(path, langPair);
	}

	private void printTable(String path, String langPair) {
		String[] arr = langPair.split(":");
		
		File[] files = null;
		File runDirHandler = new File(path);
		if (runDirHandler.isDirectory())
			files = runDirHandler.listFiles();
		else
			files = new File[] {runDirHandler};
		
		String sourceLang = arr[0];
		String targetLang = arr[1];
		for (File file : files) 
			read(file, sourceLang, targetLang);
	}

}
