package crosslink.submission;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;

import crosslink.AppResource;
import crosslink.utility.XML;
import crosslink.wiki.WikiArticleXml;

import crosslink.submission.Run;

public class Topic<AnchorSet> extends WikiArticleXml {

	private boolean valid = false;
		
	private AnchorSet anchors = null;
	
	private Run parent;
		
	public Topic(String id, String name) {
		super(id, name);
		
		
//		load();
	}
	
	
//	
//	public Topic(String id, AnchorSet anchors) {
//		this.id = id;
//		this.anchors = anchors;
//	}

	public Topic(String id, String name, String lang) {
		super(id, name, lang);
	}


	public Topic(File file) {
		super(file);
		
//		load();
	}
	
	@Override
	public void read() {
		if (this.xmlFile == null)
			setXmlFile(AppResource.getInstance().getTopicXmlPathNameByFileID(id, lang));
		super.read();
	}

	public void load() {
		if (bytes == null) {
			setXmlFile(AppResource.getInstance().getTopicXmlPathNameByFileID(id));
			if (new File(xmlFile).exists()) {
				read();
				extractTitle();
			}
		}
	}
	
	public boolean validateIt(int showMessage, boolean convertToTextOffset) {
		load();
		
		valid = true;
		
		if (anchors instanceof Collection) {
			Collection<Anchor> anchorSet = (Collection<Anchor>) anchors;
			for (Anchor anchor : anchorSet) {
				if (!anchor.validate(this, showMessage, convertToTextOffset))
					valid = false;
			}
		}
		else {
			System.err.println("Unsupported implementation of AnchorSet type");
			System.exit(-1);
		}
			
//		valid = anchors.validateAll(this, showMessage, convertToTextOffset);
		
		return valid;
	}

	public boolean isValid() {
		return valid;
	}
	
	
	public String getAnchor(int offset, int length) throws UnsupportedEncodingException {
		if ((offset + length) > bytes.length)
			return "EXCEEDED TOPIC LENGTH";
		byte[] result = new byte[length];
		if (offset > -1 && length > 0) {
			System.arraycopy(bytes, offset, result, 0, length);
			return new String(result, "UTF-8");
		}
		return "";
	}
	
	public String getAnchorWithCharacterOffset(int offset, int length) {
		return fullText.substring(offset, offset + length);
	}
	
	public void setAnchors(AnchorSet anchors) {
		this.anchors = anchors;
	}

	public AnchorSet getAnchors() {
		return anchors;
	}

	public Run getParent() {
		return parent;
	}

	public void setParent(Run parent) {
		this.parent = parent;
	}

	public String anchorsToXml() {
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("\t<topic file=\"%s\" name=\"%s\">\n", id, XML.XMLify(title)));
		sb.append("\t\t<outgoing>\n");
		if (anchors instanceof Collection) {
//			Anchor[] anchorArray = (Anchor[]) ((Collection<Anchor>)anchors).toArray();
			Collection anchorCollection = (Collection)anchors;
			Iterator<Anchor> it = anchorCollection.iterator();
			while (it.hasNext()) {
				Anchor anchor = it.next();
				sb.append(anchor.toXml());
			}
		}
		sb.append("\t\t</outgoing>\n");
		sb.append("\t</topic>\n");
		return sb.toString();
	}
}
