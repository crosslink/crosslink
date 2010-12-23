package crosslink.validator;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Usage:
 *         
      	String[] arrFile = WildcardFiles.list(inputfiles);
        for (String onefile : arrFile) 
        {
        	String inputfile = WildcardFiles.getDirectory() + File.separator + onefile;
        	// do whatever you want with the input file...
        }
 */

class WildcardFiles implements FilenameFilter, FileFilter
{
	   private Pattern pattern;
	   private static String wildcard = "";
	   private static String inputFileDir = ".";
	    
	   WildcardFiles(String search)
	   {
		   createPattern(search);      
	   }
	   
	   private void createPattern(String search) {
		   String reform = search;
		   //reform = reform.replaceAll("\\.", "\\.");  
		   //reform = reform.replaceAll("\\?", "."); 
		   reform = reform.replaceAll("\\*", ".*");
		   
		   pattern = Pattern.compile(reform);
	   }
	   
	   public static String getDirectory()
	   {
		   return inputFileDir;
	   }
	   
	   private static void breakFile(String inputfile)
	   {
			wildcard = inputfile;
			int lastIndex = 0;
			if ((lastIndex = inputfile.lastIndexOf(File.separator)) > -1) {
				inputFileDir = inputfile.substring(0, lastIndex);
				wildcard = inputfile.substring(lastIndex + 1);
			}		   
	   }
	   
//	   private static void listFiles(String inputfile, Stack stack) 
//	   {
//			File[] arrFile = new File(inputFileDir).listFiles((FileFilter)new WildcardFiles(wildcard));
//			stack.addAll(Arrays.asList(arrFile));
//	   }
	   
	   public static Stack listFilesInStack(String inputfile, boolean includeSubFolder) 
	   {
		   breakFile(inputfile);
		   WildcardFiles wildcardfiles = new WildcardFiles(wildcard);
		   Stack<File> stack = new Stack();
		   if (includeSubFolder) {
			   File[] allFiles = new File(inputFileDir).listFiles();
			    for (File f : allFiles) {
			        if (f.isDirectory()) {
			            stack.push(f);
			        }
			        else if (wildcardfiles.accept(f))
			        	stack.push(f);
			    }
			    return stack;
		   }
		   File[] arrFile = new File(inputFileDir).listFiles((FileFilter)wildcardfiles);
		   stack.addAll(Arrays.asList(arrFile));
		   return stack;
	   }
	   
	   public static Stack listFilesInStack(String inputfile) 
	   {
		   breakFile(inputfile);
		   return listFilesInStack(inputfile, false);
	   }
	   
	   /**
	 * @return the wildcard
	 */
	public static String getWildcard() {
		return wildcard;
	}

	/**
	 * @param wildcard the wildcard to set
	 */
	public static void setWildcard(String wildcard) {
		WildcardFiles.wildcard = wildcard;
	}

	public static String[] list(String inputfile) 
	   {
		   breakFile(inputfile);
			String[] arrFile = new File(inputFileDir).list(new WildcardFiles(wildcard));
		    return arrFile;
	   }
	   
	   private boolean match(String name) 
	   {
		   Matcher matcher = pattern.matcher(name);
		   return matcher.matches();		   
	   }
	   
	   public boolean accept(File file)
	   {
		   return match(file.getName());
	   }

		//@Override
		public boolean accept(File dir, String name) {
			return match(name);
		}	   
}
