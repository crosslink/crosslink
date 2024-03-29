package com.spreada.utils.chinese;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/*
 * Modified from http://code.google.com/p/java-zhconverter/
 * Original author: Edward.Y.K.Tse
 * Modified by: Eric Tang
 */

public class ZHConverter {


	private Properties charMap = new Properties();
	private Set conflictingSets  = new HashSet();

	public static final int TRADITIONAL = 0;
	public static final int SIMPLIFIED = 1;
	private static final int NUM_OF_CONVERTERS = 2;
	private static final ZHConverter[] converters = new ZHConverter[NUM_OF_CONVERTERS];
	private static final String[]  propertyFiles = new String[2];

	static {
		propertyFiles[TRADITIONAL] = "zh2Hant.properties";
		propertyFiles[SIMPLIFIED] = "zh2Hans.properties";
	}



	/**
	 *
	 * @param converterType 0 for traditional and 1 for simplified
	 * @return
	 */
	public static ZHConverter getInstance(int converterType) {

		if (converterType >= 0 && converterType < NUM_OF_CONVERTERS) {

			if (converters[converterType] == null) {
				synchronized(ZHConverter.class) {
					if (converters[converterType] == null) {
						converters[converterType] = new ZHConverter(propertyFiles[converterType]);
					}
				}
			}
			return converters[converterType];

		} else {
			return null;
		}
	}

	public static String convert(String text, int converterType) {
		ZHConverter instance = getInstance(converterType);
		return instance.convert(text);
	}


	private ZHConverter(String propertyFile) {

	    InputStream is = null;

	    is = getClass().getResourceAsStream(propertyFile);

		if (is != null) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(is));
				charMap.load(reader);
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (reader != null)
						reader.close();
					if (is != null)
						is.close();
				} catch (IOException e) {
				}
			}
		}
		initializeHelper();
	}

	private void initializeHelper() {
		Map stringPossibilities = new HashMap();
		Iterator iter = charMap.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			if (key.length() >= 1) {

				for (int i = 0; i < (key.length()); i++) {
					String keySubstring = key.substring(0, i + 1);
					if (stringPossibilities.containsKey(keySubstring)) {
						Integer integer = (Integer)(stringPossibilities.get(keySubstring));
						stringPossibilities.put(keySubstring, new Integer(
								integer.intValue() + 1));

					} else {
						stringPossibilities.put(keySubstring, new Integer(1));
					}

				}
			}
		}

		iter = stringPossibilities.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			if (((Integer)(stringPossibilities.get(key))).intValue() > 0) {
				conflictingSets.add(key);
			}
		}
	}

	public String convert(String in) {
		StringBuilder outString = new StringBuilder();
		StringBuilder stackString = new StringBuilder();

		for (int i = 0; i < in.length(); i++) {

			char c = in.charAt(i);
			String key = "" + c;
			stackString.append(key);

			if (conflictingSets.contains(stackString.toString())) {
			} else if (charMap.containsKey(stackString.toString())) {
				outString.append(charMap.get(stackString.toString()));
				stackString.setLength(0);
			} else {
				StringBuffer sequence = new StringBuffer(stackString.substring(0, stackString.length()-1));
				while (sequence.length() > 1 && !charMap.containsKey(sequence.toString()))
					sequence.deleteCharAt(sequence.length() - 1);
				if (charMap.containsKey(sequence.toString()))
					outString.append(charMap.get(sequence.toString()));
				else
					outString.append(sequence.toString());
				stackString.delete(0, sequence.length());
				if (!conflictingSets.contains(stackString.toString())) 
					flushStack(outString, stackString);
			}
		}

		flushStack(outString, stackString);

		return outString.toString();
	}


	private void flushStack(StringBuilder outString, StringBuilder stackString) {
		while (stackString.length() > 0){
			if (charMap.containsKey(stackString.toString())) {
				outString.append(charMap.get(stackString.toString()));
				stackString.setLength(0);
			} else {
				String first = "" + stackString.charAt(0);
				if (charMap.containsKey(first))
					outString.append("" + charMap.get(first));
				else
					outString.append("" + first);
				stackString.delete(0, 1);
			}

		}
	}


	String parseOneChar(String c) {

		if (charMap.containsKey(c)) {
			return (String) charMap.get(c);

		}
		return c;
	}
	
	private static void usage() {
		System.out.println("Usage:");
		System.out.println("program -f file [-T]");
		System.out.println("or");
		System.out.println("program -s input_string [-T]");
		System.out.println("");
		System.out.println("Note: last option -T is optional, default or incorrect option will lead to converting input to simplified Chinese.");
		System.out.println("With -T, input will be converted into traditional Chinese");
		System.exit(-1);
	}
	
	private static String convertString(String input, int toform) {
		return ZHConverter.convert(input, toform);	
	}
	
	private static String convertFile(String filename, int toform) {
	    int size;
	    byte[] bytes = null;
		try {
			FileInputStream fis = new FileInputStream(filename);
			size = fis.available();
		    bytes    = new byte[size];
		    fis.read(bytes, 0, size);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return convertString(new String(bytes), toform);
	}

	public static void main(String[] args) {
		if (args.length < 2)
			usage();
		
		String result = null;
		
		try {
			int toform = SIMPLIFIED;
			if (args.length >= 3) {
				if (args[2].charAt(1) == 'T')
					toform = TRADITIONAL;
			}
			
			if (args[0].equals("-f")) {
				result = convertFile(args[1], toform);
				System.out.print(result);
			}
			else if (args[0].equals("-s")) {
				result = convertString(args[1], toform);
				System.out.println(result);
			}
			else
				usage();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			usage();
		}
	}
}
