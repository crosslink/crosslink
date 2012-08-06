package crosslink.utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;

/*
 * the below code is from:
 * 
 *  http://stackoverflow.com/questions/106770/standard-concise-way-to-copy-a-file-in-java
 */

public class FileUtil {
	public static void copyFile(File sourceFile, File destFile) throws IOException {
		 if(!destFile.exists()) {
			  destFile.createNewFile();
		 }

		 FileChannel source = null;
		 FileChannel destination = null;
		 try {
			  source = new FileInputStream(sourceFile).getChannel();
			  destination = new FileOutputStream(destFile).getChannel();
			  destination.transferFrom(source, 0, source.size());
		 }
		 finally {
			  if(source != null) {
				   source.close();
			  }
			  if(destination != null) {
				   destination.close();
			  }
		 }
	}
	
	public static void copyFile(String sourceFile, String destFile) throws IOException {
		copyFile(new File(sourceFile), new File(destFile));
	}
	
	public static void moveFile(String sourceFile, String destFile) throws IOException {
		moveFile(new File(sourceFile), new File(destFile));
	}
	
	public static void moveFile(File sourceFile, File destFile) throws IOException {
		copyFile(sourceFile, destFile);
		
		sourceFile.delete();
	}
	 
	public static void writeFile(File file, String content) {
		writeFile(file, content, "UTF-8");
	}
	
	public static void writeFile(File file, String content, String charset) {
        BufferedWriter out;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
	        out.write(content);
	        out.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 

	}
}
