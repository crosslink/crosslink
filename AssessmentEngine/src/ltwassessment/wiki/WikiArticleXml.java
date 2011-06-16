package ltwassessment.wiki;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WikiArticleXml {

	protected String xmlFile;

	protected String title;
	protected String id;
	
	protected int currentPos = 0;
	
	protected byte[] bytes = null; // the text in bytes
	protected String fullText = null;
	
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

//	public WikiArticleXml(String xmlFile) {
//		read(new File(xmlFile));
//	}
	
	/**
	 * @return the bytes
	 */
	public byte[] getBytes() {
		return bytes;
	}

	/**
	 * @param bytes the bytes to set
	 */
	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	/**
	 * @return the fullText
	 */
	public String getFullText() {
		return fullText;
	}

	/**
	 * @param fullText the fullText to set
	 */
	public void setFullText(String fullText) {
		this.fullText = fullText;
	}

	public WikiArticleXml(File xmlFile) {
		extractTitle(xmlFile);
	}
	
	public WikiArticleXml(String id) {
		this.id = id;
	}

	public WikiArticleXml(String id, String name) {
		this.id = id;
		this.title = name;
	}

	public void read() {	
		readInBytes(new File(xmlFile));
	}
	
	public void readInBytes(File file) {
		int size = 0;
//	    
		try {
			FileInputStream fis = new FileInputStream(file);
			size = fis.available();
		    bytes = new byte[size];
		    fis.read(bytes, 0, size);
		    
		    fullText = new String(bytes, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public void extractTitle(File file) {
		this.xmlFile = file.getAbsolutePath();	
		String filename =  file.getName();
		id = filename.substring(0, filename.indexOf('.'));	
		
		readInBytes(file);
		extractTitle();
	}
	
	public void extractTitle() {
		extractTitle(bytes);
	}
	
	public void extractTitle(byte[] content) {
		int pos;
    	try {
			BufferedReader br = new BufferedReader(
			        new InputStreamReader(new ByteArrayInputStream(content), "UTF-8"));
			String strLine;
			while ((strLine = br.readLine()) != null) {
			    if ((pos = strLine.indexOf("<title>")) > -1) {
			    	pos += 7;
			    	title = strLine.substring(pos, strLine.indexOf('<', pos));
			    	break;
			    }
			}
			br.close();
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
	
	public void readXml() {
		try {
			File file = new File(xmlFile);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
//			System.err.println("Root element " + doc.getDocumentElement().getNodeName());
			NodeList nodeLst = doc.getElementsByTagName("article");
//			System.err.println("Information of topic");

//			  for (int s = 0; s < nodeLst.getLength(); s++) {
			
			Node articleNode = nodeLst.item(0);
			
			if (articleNode.getNodeType() == Node.ELEMENT_NODE) {
				  
		       Element articleElmnt = (Element) articleNode;
		       NodeList titleElmntLst = articleElmnt.getElementsByTagName("title");
		       Element titleElmnt = (Element) titleElmntLst.item(0);
		       NodeList titleList = titleElmnt.getChildNodes();
		       title = ((Node) titleList.item(0)).getNodeValue();
//		       System.err.println("Title : "  + title);
		       NodeList idElmntLst = articleElmnt.getElementsByTagName("id");
		       Element idElmnt = (Element) idElmntLst.item(0);
		       NodeList idList = idElmnt.getChildNodes();
		       id = ((Node) idList.item(0)).getNodeValue();
//		       System.err.println("Id : " + id);
			}

//			  }
		  } catch (Exception e) {
			    e.printStackTrace();
		  }
	}
}
