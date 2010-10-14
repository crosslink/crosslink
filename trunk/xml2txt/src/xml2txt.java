
import java.io.FileInputStream;
import java.io.IOException;

public class xml2txt {

	/*
	 * This snippet code is written by Andrew Trotman initially
	 */
	static void clean(byte[] file)
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
				file[count++] = ' '; // replace >
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
//	from = to = file;
//	while (isspace(*from))
//		from++;
//	while (*from != '\0')
//		{
//		while (isalnum(*from))
//			*to++ = *from++;
//		if (isspace(*from))
//			*to++ = *from++;
//		while (isspace(*from))
//			from++;
//		}
//	if (to > file && isspace(*(to - 1)))
//		to--;
//	*to = '\0';
	}
	
	public byte[] convert(String xmlfile) {

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
		clean(bytes);
		return bytes;
	}
	
	static void usage() {
		System.out.println("Usage: xml2txt [-o:offset:length] input_xml");
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
		xml2txt converter = new xml2txt();
		String xmlfile = null;
		if (args[0].charAt(0) == '-')
		{
			if (args.length < 2)
				usage();
			int pos = 0, pos2 = 0;
        	if ((pos = args[1].indexOf(":", pos)) != -1){
        		++pos;
        		if ((pos2 = args[1].indexOf(":", pos)) != -1) { 
        			offset = Integer.valueOf(args[1].substring(pos, pos2)).intValue();
        			length = Integer.valueOf(args[1].substring(pos2 + 1)).intValue();
        		}
        		else
        			offset = Integer.valueOf(args[1].substring(pos + 1)).intValue();
        	}
        	System.err.printf("Showing offset: %d with length %d\n", (Object [])new Integer[] {new Integer(offset), new Integer(length)});
        	xmlfile = args[1];
		}			
		else 
			xmlfile = args[0];
		byte[] bytes = converter.convert(xmlfile);
		System.out.print(new String(bytes));
	}

}
