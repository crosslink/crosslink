package crosslink;
import java.io.FileInputStream;
import java.io.IOException;

public class XML2TXT {

	private static XML2TXT instance = null;
	
	public XML2TXT() {
		
	}
	
	public static XML2TXT getInstance() {
		if (instance == null)
			instance = new XML2TXT();
		return instance;
	}
	
	public String replaceNonAlphabet(String source, String with)
	{
		source.replaceAll("[\\W]", with);
		return source;
	}
	
	/*
	 * This snippet code is written by Andrew Trotman initially
	 */
	public byte[] clean(byte[] file)
	{
		//byte[] ch; //, *from, *to;
		/*
			remove XML tags and remove all non-alnum (but keep case)
		*/
		//ch = file;
		int count = 0;
		while (count < file.length)
			{
			byte ch = file[count];
			if (ch == '<')			// then remove the XML tags
				{
				while (file[count] != '>')
					file[count++] = ' ';
				file[count] = ' '; // replace >
				}
//			else if (!isalnum(ch))	// then remove it
//				ch++ = ' ';
//			else
//				{
//				if (lower_case_only)
//					{
////					ch = (char)tolower(ch);
//					ch++;
//					}
//				else
//					ch++;
//				}
			++count;
			}


	/*
		now remove multiple, head, and tail spaces.
	*/
//		int offset = 0;
//		int length = file.length;
//		while (Character.isWhitespace(file[offset]) && offset < length)
//			++offset;
//		while (Character.isWhitespace(file[length - 1]) && length > 0)
//			--length;
//		length -= offset;
//		byte[] result = new byte[length];
//		System.arraycopy(file, offset, result, 0, length);
//		return result;
		return file;
	}
	
	public byte[] convertFile(String xmlfile) {

	    int size;
	    byte[] bytes = null;
		try {
			FileInputStream fis = new FileInputStream(xmlfile);
			size = fis.available();
		    bytes    = new byte[size];
		    fis.read(bytes, 0, size);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//	    for (int i = 0; i < size;)
//	        theChars[i] = (char)(bytes[i++]&0xff);
		return clean(bytes);
	}
	
	public byte[] convert(String xml) {
		return clean(xml.getBytes());
	}
	
	public String getText(String xmlfile) {
		return new String(convert(xmlfile));
	}
	
	public String getXmlFileText(String xmlfile) {
		return new String(convertFile(xmlfile));
	}
	
	static void usage() {
		System.out.println("Usage: XML2TXT [-o:offset:length] input_xml");
		//System.out.println("			[-r] -r replace the non-alphabet characters");
		System.exit(-1);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			usage();
		}
		int offset = 0, length = -1;
		XML2TXT converter = new XML2TXT();
		String xmlfile = null;
		if (args[0].charAt(0) == '-')
		{
			if (args.length < 2)
				usage();
			int pos = 0, pos2 = 0;
        	if ((pos = args[0].indexOf(":", pos)) != -1){
        		++pos;
        		if ((pos2 = args[0].indexOf(":", pos)) != -1) { 
        			offset = Integer.valueOf(args[0].substring(pos, pos2)).intValue();
        			length = Integer.valueOf(args[0].substring(pos2 + 1)).intValue();
        		}
        		else
        			offset = Integer.valueOf(args[0].substring(pos + 1)).intValue();
        	}
        	System.err.printf("Showing offset: %d with length %d\n", (Object [])new Integer[] {new Integer(offset), new Integer(length)});
        	xmlfile = args[1];
		}			
		else 
			xmlfile = args[0];
		byte[] bytes = converter.convert(xmlfile);
		if (length == -1)
			length = bytes.length;
		int count = 0;
		while (count < length) {
			System.out.print(new String(new byte[]{bytes[count + offset]}));
			++count;
		}
		System.out.print("\n");
	}

}
