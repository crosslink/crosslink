package monolink;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import crosslink.ResultSetXml;

public class MonolinkMining {

	protected ResultSetXml resultSetOut = new ResultSetXml();
	
	protected ArrayList<String> extractLinksFromTopics(String inputfile) {
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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
