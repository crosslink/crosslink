package ltwassessment.wiki;

import java.io.BufferedReader;
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

	private String xmlFile;

	private String title;
	private String id;
	
	protected int currentPos = 0;
	
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

	public WikiArticleXml(String xmlFile) {
		super();
		this.xmlFile = xmlFile;
		
		read();
	}
	
	public void read() {
		String filename =  new File(xmlFile).getName();
		id = filename.substring(0, filename.indexOf('.'));
		int pos;
    	try {
			BufferedReader br = new BufferedReader(
			        new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"));
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
