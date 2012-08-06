package crosslink.utility;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import crosslink.utility.WildcardFiles;

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

public class WildcardFiles implements FilenameFilter, FileFilter
{
	   protected Pattern pattern = null;
	   protected static String wildcard = "";
	   protected static String inputFileDir = ".";
	   protected static WildcardFiles wildcardfiles = null; //new WildcardFiles(wildcard);
	   
	   public WildcardFiles(){
		   createPattern("*.*");   
	   }
	   
	   public WildcardFiles(String search)
	   {
		   createPattern(search);      
	   }
	   
	   protected void createPattern(String search) {
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
	   
	   protected static void breakFile(String inputfile)
	   {
			if (new File(inputfile).isDirectory()) {
				inputFileDir = inputfile;
				wildcard = "*";		
			}
			else {
				wildcard = inputfile;
				int lastIndex = 0;
				if ((lastIndex = inputfile.lastIndexOf(File.separator)) > -1) {
					inputFileDir = inputfile.substring(0, lastIndex);
					wildcard = inputfile.substring(lastIndex + 1);
					wildcard.trim();
					if (wildcard.length() == 0)
						wildcard = "*";
				}
			}
	   }
	   
//	   private static void listFiles(String inputfile, Stack stack) 
//	   {
//			File[] arrFile = new File(inputFileDir).listFiles((FileFilter)new WildcardFiles(wildcard));
//			stack.addAll(Arrays.asList(arrFile));
//	   }
	   
	   public Stack listFilesInStack(String inputfile, boolean includeSubFolder) 
	   {
		   wildcardfiles = new WildcardFiles();
		   Stack<File> stack = new Stack();
		   File inputFileHandler = new File(inputfile);
		   if (inputFileHandler.isFile()) {
			   stack.add(new File(inputfile));
		   }
		   else if (inputFileHandler.isDirectory() && includeSubFolder) {
			   stack.addAll(Arrays.asList(wildcardfiles.listSubfolderFiles(inputFileHandler)));
		   }
		   else {
			   breakFile(inputfile);
			   wildcardfiles.createPattern(wildcard);
//			   wildcardfiles = new WildcardFiles(wildcard);
	
			   if (includeSubFolder) {
				   File[] allFiles = new File(inputFileDir).listFiles();
				    for (File f : allFiles) {
				        if (f.isDirectory()) {
				            stack.push(f);
				        }
				        else if (wildcardfiles.accept(f))
				        	stack.push(f);
				    }
			   }
			   else {
				   File[] arrFile = new File(inputFileDir).listFiles((FileFilter)this);
				   if (arrFile != null)
					   stack.addAll(Arrays.asList(arrFile));
			   }
		   }
		   return stack;
	   }
	   
	   public Stack listFilesInStack(String inputfile) 
	   {
//		   breakFile(inputfile);
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

	/**
	 * @return the wildcardfiles
	 */
	public static WildcardFiles getInstance() {
		return wildcardfiles;
	}

	public File[] listSubfolderFiles(File subfolder) 
	{
		File[] arrFile = subfolder.listFiles((FileFilter)this);
		return arrFile;
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
	   
	   @Override
	   public boolean accept(File file)
	   {
		   return match(file.getName());
	   }

		@Override
		public boolean accept(File dir, String name) {
			return match(name);
		}	   
}
