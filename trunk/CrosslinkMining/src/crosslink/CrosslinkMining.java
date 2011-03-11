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
	
	private String SOURCE_LANG = "zh";
	private String TARGET_LANG = "en";

	private String OTHER_LANG = "";
//			OUTPUT=

	private String CORPUS_HOME = "/data/corpus/wikipedia/all/";

//			filename=


	static public String wikiIdToPath(String targetId) {
        int lastPos = targetId.lastIndexOf(".xml");
        String subFolder;
        if (lastPos < 3)
        	subFolder = String.format("%03d", Integer.parseInt(targetId.substring(0, lastPos)));
        else
        	subFolder = targetId.substring(targetId.length() - 7, lastPos);
        return "pages" + File.separator + subFolder + File.separator + targetId;
	}
	
	private void output() {
		// TODO Auto-generated method stub
		
	}
	
	private String getOutputIdFromEnCorpus(String filename) {
		if (new File(CORPUS_HOME + File.separator + OTHER_LANG + File.separator + filename).exists())
			output();
		else
			getOutputIdFromOtherLang(filename);
 	}

	private String getOutputIdFromOtherLang(String filename) {
 			
	}

 	private void getTopicLinks(String topicPath, CrosslinkTable crosslinkTable) {
        int filecount = 0;
      	Stack<File> stack = null;
      	

      	stack = WildcardFiles.listFilesInStack(topicPath, true);
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
			 			String targetId = arr[1];
			 			String pagePath = wikiIdToPath(targetId);
			 			
			 			int count  = crosslinkTable.getTargetCount(pagePath);
			 			if (count > 0) {
			 				if (count > 1)
			 					getOutputIdFromEnCorpus(pagePath);
			 				else
			 					getOutputIdFromOtherLang(pagePath);
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
            catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
				//recordError(inputfile, "IOException");
				e.printStackTrace();
            } 
        }
 
 	}


	public void findWikiGroundTruth() {
		if ( TARGET_LANG.equalsIgnoreCase( "en")) 
		    OTHER_LANG = SOURCE_LANG;
		else
			OTHER_LANG=TARGET_LANG;

 		SOURCE_TOPICS_PATH=~/experiments/ntcir-9-clld/topics/training/${SOURCE_LANG}/
 		TARGET_TOPICS_PATH=~/experiments/ntcir-9-clld/topics/training/${TARGET_LANG}/


 		SOURCE_CROSSLINK_MERGED_TABLE=~/experiments/ntcir-9-clld/assessment/wikipedia_groundtruth/link-mining/all/${SOURCE_LANG}2${TARGET_LANG}_merged.txt
 		TARGET_CROSSLINK_MERGED_TABLE=~/experiments/ntcir-9-clld/assessment/wikipedia_groundtruth/link-mining/all/${TARGET_LANG}2${SOURCE_LANG}_merged.txt

 		./find_crosslingual_topics.sh $TARGET_TOPICS_PATH $SOURCE_TOPICS_PATH ~/experiments/ntcir-9-clld/assessment/wikipedia_groundtruth/link-mining/${TARGET_LANG}-${SOURCE_LANG/${TARGET_LANG}2${SOURCE_LANG}_table.txt $CORPUS_HOME 

 			TMP_LINKS=/tmp/$0.tmp

 			get_file_path() {
 			len=${#1}
 			if [ "$len" -gt "3" ]
 				then
 				subdir=${$1: -3}
 			else
 				subdir=`printf "%03d" $input`
 				fi
 				
 				#echo $subdir
 				
 				filename=$subdir/${input}.xml
 				
 				filename="pages/$filename"
 		}



// 		get_topic_link $TARGET_TOPICS_PATH $TARGET_CROSSLINK_MERGED_TABLE
// 		get_topic_link $SOURCE_TOPICS_PATH $SOURCE_CROSSLINK_MERGED_TABLE
//
// 		#get_zh_topics_links() {
// 			#	for i in `ls $TARGET_TOPICS_PATH`
// 			#	do
// 				#	TOPIC_ID=``
// 				#	
// 				#	~/workspace/ant/build/release/link_extract $SOURCE_TOPICS_PATH/$i > $TMP_LINKS
// 				#	
// 				#	cat $TMP_LINKS | while read line
// 				#	
// 				#	
// 				#	done
// 				#
// 				#}
//
// 		\rm -f $TMP_LINKS
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
