package crosslink;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class CrosslinkTopic {

	private String outputPath = null;
	
	private static final String LINK_START = "<link";
	private static final String LINK_END = "</link>";
	
	public CrosslinkTopic() {
		
	}

	private void setOutputPath(String path) {
		this.outputPath = path;
	}
	
	public String cleanLink(String content) {
//		byte[] copy = new byte[content.length];
//		int count = content.length;
//		byte[] result = new byte[count];
//		
//		System.arraycopy(copy, 0, result, 0, count);
		int pos = 0;
		int pre_pos = 0;
		StringBuffer sb = new StringBuffer();
		while((pos = content.indexOf(LINK_START, pos)) > -1) {
			sb.append(content.substring(pre_pos, pos));
			
			while (content.charAt(pos) != '>')
				++pos;
			++pos;
			
			while (content.charAt(pos) == '\r' || content.charAt(pos) == '\n')
				++pos;
			
			pre_pos = pos;
			
			pos = content.indexOf(LINK_END, pre_pos);
			
			sb.append(content.substring(pre_pos, pos));
			
			pos += LINK_END.length();
			while (content.charAt(pos) == '\r' || content.charAt(pos) == '\n')
				++pos;
			pre_pos = pos;
		}
		if (pos < 0)
			sb.append(content.substring(pre_pos));
		
		return sb.toString();
	}
	
	public byte[] readTopic(String xmlfile) {

	    int size;
	    byte[] bytes = null;
		try {
			FileInputStream fis = new FileInputStream(xmlfile);
			size = fis.available();
		    bytes    = new byte[size];
		    fis.read(bytes, 0, size);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bytes;
	}
	
	public void processTopic(String[] files) {
		for (String file : files) {
			try {
				String result = cleanLink(new String(readTopic(file), "UTF-8"));
				StringBuffer resultFile = new StringBuffer(new File(file).getName());
				if (outputPath != null && outputPath.length() > 0)
					resultFile.insert(0, outputPath + File.separator);
//				resultFile.append(new File(file).getName());
				writeTopic(resultFile.toString(), result);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void writeTopic(String file, String content) {
		try {
//			FileOutputStream out = new FileOutputStream(file);
	        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8")); 
			out.write(content);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static void usage() {
		System.out.println("Usage: CrosslinkTopic [-p output_path] input_xml ...");
		//System.out.println("			[-r] -r replace the non-alphabet characters");
		System.exit(-1);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			CrosslinkTopic crosslinkTopic = new CrosslinkTopic();
			String[] files = null;
			if (args[0].equals("-p")) {
				crosslinkTopic.setOutputPath(args[1]);
				files = new String[args.length - 2];
				System.arraycopy(args, 2, files, 0, args.length - 2);
			}
			else
				files = args;
			
			crosslinkTopic.processTopic(files);
			return;
		}

		usage();
	}
}
