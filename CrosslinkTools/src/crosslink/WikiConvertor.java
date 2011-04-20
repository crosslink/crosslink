package crosslink;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Stack;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ltwassessment.utility.WildcardFiles;
import monolink.WikiArticleXml;

public class WikiConvertor {
	
	protected String newPath = null;
	protected String currentFileName = null;
	
	public String getNewPath() {
		return newPath;
	}

	public void setNewPath(String newPath) {
		this.newPath = newPath;
	}
	
	public String createNewPath(String id) {
		String subPath = "";
		if (id.length() < 3)
			subPath = String.format("%03d", Integer.parseInt(id));
		else
			subPath = id.substring(id.length() - 3);
		return newPath + File.separator + subPath + File.separator + id + ".xml";
	}
	
	private void write(String content) {
		new File(new File(currentFileName).getParent()).mkdir();
        try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(currentFileName),"UTF-8")); 
			out.write(content);
			out.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected String getContent(String file) {
		WikiArticleXml article = new WikiArticleXml(file);
		currentFileName = createNewPath(article.getId());
		return article.getTitle();
	}

	public void convert(String[] args) {
		if (args.length < 2) {
			usage();
			System.exit(-1);
		}
		
		setNewPath(args[0]);
//		wikiTitle.createTitleOnlyCorpus(args[1]);
		String input = args[1];
        int filecount = 0;
        String inputfile = null;
      	Stack<File> stack = null;
      	
//      	for (String input : args) {
          	stack = WildcardFiles.listFilesInStack(input, true);
          	while (!stack.isEmpty())
            {
//          		good = true;
                File onefile = (File)stack.pop();
                if (onefile.isDirectory()) {
                	stack.addAll(Arrays.asList(WildcardFiles.getInstance().listSubfolderFiles(onefile)));
                	continue;
                }
                ++filecount;
                if ((filecount % 1000) == 0)
                	System.err.println("processing " + onefile.getAbsolutePath() + "...");

                try {
                	inputfile = onefile.getCanonicalPath();

                	write(getContent(inputfile));
//                reader.parse(new InputSource(inputfile)); //SAX
                	// do whatever you want with the input file...
                } catch (IOException e) {
					//recordError(inputfile, "IOException");
					e.printStackTrace();
                } 
            }
//      	}
	}
	
	public static void usage() {
		System.err.println("Usage: program new_path wiki_path");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
