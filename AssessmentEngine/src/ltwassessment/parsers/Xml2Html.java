package ltwassessment.parsers;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import ltwassessment.AppResource;
import ltwassessment.parsers.Xml2Html;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Darren HUANG
 */
public class Xml2Html {

    private String appDirectory;
    private String imageDirectory;
    private String htmlFileTopFolder;
    private String xmlFileID;
    private String xmlPath;
    private StringBuffer htmlSB = new StringBuffer();
    private String wikipediaTitleTag = "title";
    private boolean isImageHref = false;
    private boolean isLinkHref = false;

    static void log(Object content) {
        System.out.println(content);
    }

    public Xml2Html(String xmlFilePath, boolean isWikipedia) {

        //org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ltwassessment.ltwassessmentApp.class).getContext().getResourceMap(ltwassessmentView.class);
        this.htmlFileTopFolder = AppResource.getInstance().getResourceMap().getString("temp.folder") + File.separator;

        this.xmlPath = xmlFilePath;
        this.xmlFileID = xmlFilePath.substring(xmlFilePath.lastIndexOf(File.separator) + 1, xmlFilePath.lastIndexOf(".xml"));

        this.appDirectory = System.getProperty("user.dir");
        this.imageDirectory = "file:\\" + this.appDirectory + File.separator + "images" + File.separator;

        // populate topic HTML
        if (isWikipedia) {
            populateWikipediaTopicHtml();
        } else {
            populateTeAraTopicHtml();
        }
    }

    public StringBuffer getHtmlContent() {
        return this.htmlSB;
    }

    public StringBuffer getHtmlPath() {
        StringBuffer htmlFilePathSB = new StringBuffer();
        try {
            clearFolder(this.htmlFileTopFolder, ".html");
            String htmlFilePath = this.htmlFileTopFolder + xmlFileID + ".html";
            BufferedWriter bw = new BufferedWriter(new FileWriter(htmlFilePath));
            bw.write(htmlSB.toString());
            bw.close();
            htmlFilePathSB.append(htmlFilePath);
        } catch (IOException ex) {
            Logger.getLogger(Xml2Html.class.getName()).log(Level.SEVERE, null, ex);
        }
        return htmlFilePathSB;
    }

    private boolean isImageExist(String imageID) {
        boolean isImageExist = false;
        String imgFolderPath = "images" + File.separator;
        File imgFolder = new File(imgFolderPath);
        if (imgFolder.isDirectory()) {
            File[] imgFiles = imgFolder.listFiles();
            for (File thisFile : imgFiles) {
                String thisFileName = thisFile.getName();
                if (imageID.equals(thisFileName)) {
                    isImageExist = true;
                }
            }
        }
        return isImageExist;
    }

    private void populateWikipediaTopicHtml() {
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        try {
            fileInputStream = new FileInputStream(new File(this.xmlPath));
            inputStreamReader = new InputStreamReader(fileInputStream, "utf-8");
            InputSource inputSource = new InputSource(inputStreamReader);

            // Parse the XML File
            DOMParser parser = new DOMParser();
            parser.parse(inputSource);
            Document doc = parser.getDocument();

            htmlSB.append("<html>");
            htmlWikipediaTransformer(doc, htmlSB);
            htmlSB.append("</html>");

        } catch (SAXException ex) {
            Logger.getLogger(Xml2Html.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Xml2Html.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fileInputStream.close();
                inputStreamReader.close();
            } catch (IOException ex) {
                Logger.getLogger(Xml2Html.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void htmlWikipediaTransformer(Node node, StringBuffer htmlSB) {

        switch (node.getNodeType()) {
            case Node.DOCUMENT_NODE:
                // recurse on each child
                NodeList nodes = node.getChildNodes();
                if (nodes != null) {
                    for (int i = 0; i < nodes.getLength(); i++) {
                        htmlWikipediaTransformer(nodes.item(i), htmlSB);
                    }
                }
                break;
            case Node.ELEMENT_NODE:
                String element = node.getNodeName();
                // =============================================================
                // Start of Tag: -----------------------------------------------
                if (element.equals(wikipediaTitleTag)) {
                    htmlSB.append("<h1><center>");
                } else if (element.equals("header")) {
                    htmlSB.append("<p>");
                } else if (element.equals("image")) {
                    isImageHref = true;
                } else if (element.equals("img")) {
                    htmlSB.append("<" + element);
                } else if (element.equals("link")) {
//                    isLinkHref = true;
                } else if (element.equals("p")) {
                    htmlSB.append("<p>");
                } else if (element.equals("b")) {
                    htmlSB.append("<b>");
                } else if (element.equals("categories")) {
                    htmlSB.append("<dl>");
                } else if (element.equals("category")) {
                    htmlSB.append("<dt>");
                }
                // =============================================================
                // Attributes: -------------------------------------------------
                // <link xlink:type="simple" xlink:href="../332/23332.xml">
                // <image width="32x28px" src="Libertybell_alone_small.jpg"></image>
                NamedNodeMap attributes = node.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node current = attributes.item(i);
                    if (isImageHref && current.getNodeName().equals("src")) {
                        String imageID = current.getNodeValue();
                        if (isImageExist(imageID)) {
                            htmlSB.append("<center><BR></BR><BR></BR><img src=\""  + this.imageDirectory + imageID +
                                    "\" alt=\"" + imageID + "\" width=\"120\" height=\"120\"><BR></BR>");
                        } else {
                            htmlSB.append("<center><BR></BR><BR></BR><img src=\"" + this.imageDirectory + "Image_Unavailable.png" +
                                    "\" alt=\"Image Unavailable\" width=\"96\" height=\"96\"><BR></BR>");
                        }
                    } else if (current.getNodeName().equals("xlink:href") && isLinkHref) {
//                        htmlSB.append("<a href=\"" + current.getNodeValue() + "\">");
                    }
                }
                // Recurse each child: e.g. TEXT ---------------------------
                NodeList children = node.getChildNodes();
                if (children != null) {
                    for (int i = 0; i < children.getLength(); i++) {
                        htmlWikipediaTransformer(children.item(i), htmlSB);
                    }
                }
                // =============================================================
                // End of Tag: ---------------------------------------------
                if (element.equals(wikipediaTitleTag)) {
                    htmlSB.append("</center></h1><BR></BR>");
                } else if (element.equals("header")) {
                    htmlSB.append("</p>");
                } else if (element.equals("image")) {
                    htmlSB.append("</img><BR></BR><BR></BR></center>");
                    isImageHref = false;
                } else if (element.equals("img")) {
                    htmlSB.append("</img>");
                } else if (element.equals("link")) {
//                    htmlSB.append("</a>");
//                    isLinkHref = false;
                } else if (element.equals("p")) {
                    htmlSB.append("</p>");
                } else if (element.equals("b")) {
                    htmlSB.append("</b>");
                } else if (element.equals("categories")) {
                    htmlSB.append("</dl>");
                } else if (element.equals("category")) {
                    htmlSB.append("</dt>");
                }
                break;
            case Node.TEXT_NODE:
//                htmlSB.append("<font face=\"Verdana\">");
                htmlSB.append(node.getNodeValue());
//                htmlSB.append("</font>");
                break;
        }
    }

    private void populateTeAraTopicHtml() {
        try {
            int nameCount = 0;
            XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(this.xmlPath), "utf-8");
            while (xsr.hasNext()) {
                int eventType = xsr.next();

                if (eventType == XMLEvent.START_ELEMENT) {
                    String xmlTagLName = xsr.getLocalName();
                    if (xmlTagLName.equals("Entry")) {
                        htmlSB.append("<html>");
                    } else if (xmlTagLName.equals("FeatureBlurb")) {
                        // EMPTY
                    } else if (xmlTagLName.equals("Name")) {
                        if (nameCount == 0) {
                            htmlSB.append("<h1>");
                        } else {
                            htmlSB.append("<h3>");
                        }
                    } else if (xmlTagLName.equals("Abstract")) {
                        // EMPTY
                    } else if (xmlTagLName.equals("Contributor")) {
                        htmlSB.append("<b>");
                    } else if (xmlTagLName.equals("h3")) {
                        htmlSB.append("<h2>");
                    } else if (xmlTagLName.equals("p")) {
                        htmlSB.append("<p>");
                    } else if (xmlTagLName.equals("Intro")) {
                        // EMPTY
                    } else if (xmlTagLName.equals("ul")) {
                        htmlSB.append("<ul>");
                    } else if (xmlTagLName.equals("li")) {
                        htmlSB.append("<li>");
                    } else if (xmlTagLName.equals("SubEntries")) {
                        // EMPTY
                    } else if (xmlTagLName.equals("SubEntry")) {
                        htmlSB.append("<br>");
                    } else if (xmlTagLName.equals("Sections")) {
                        // EMPTY
                    } else if (xmlTagLName.equals("FurtherReading")) {
                        htmlSB.append("<dl>");
                    } else if (xmlTagLName.equals("Reference")) {
                        htmlSB.append("<dt>");
                    } else if (xmlTagLName.equals("em")) {
                        htmlSB.append("<b>");
                    } else if (xmlTagLName.equals("ExternalSites")) {
                        htmlSB.append("<p>");
                    } else if (xmlTagLName.equals("ExternalSite")) {
                        htmlSB.append("<dl>");
                    } else if (xmlTagLName.equals("Title")) {
                        htmlSB.append("<dt>");
                    } else if (xmlTagLName.equals("Hyperlink")) {
                        htmlSB.append("<dd>");
                    } else if (xmlTagLName.equals("Description")) {
                        htmlSB.append("<dd>");
                    } else if (xmlTagLName.equals("TopicBox")) {
                        htmlSB.append("<div><dl>");
                    } else if (xmlTagLName.equals("Heading")) {
                        htmlSB.append("<dt><h3>");
                    } else if (xmlTagLName.equals("FootNoteRef")) {
                        htmlSB.append("<sup>");
                    } else if (xmlTagLName.equals("FootNotes")) {
                        htmlSB.append("<p>");
                    } else if (xmlTagLName.equals("FootNote")) {
                        htmlSB.append("<dl>");
                    } else if (xmlTagLName.equals("Number")) {
                        htmlSB.append("<dt>");
                    } else if (xmlTagLName.equals("Text")) {
                        htmlSB.append("<dd>");
                    } else if (xmlTagLName.equals("ChannelPath") ||
                            xmlTagLName.equals("Sequence") ||
                            xmlTagLName.equals("Language") ||
                            xmlTagLName.equals("StylesheetColumnLayout") ||
                            xmlTagLName.equals("StylesheetColour") ||
                            xmlTagLName.equals("FeatureImageLocation") ||
                            xmlTagLName.equals("FeatureImageAltTag") ||
                            xmlTagLName.equals("IconLocation") ||
                            xmlTagLName.equals("IntroImageLocation") ||
                            xmlTagLName.equals("IntroImageCaption") ||
                            xmlTagLName.equals("IntroImageAltTag") ||
                            xmlTagLName.equals("AbstractImageLocation") ||
                            xmlTagLName.equals("AbstractImageCaption") ||
                            xmlTagLName.equals("AbstractImageAltTag") ||
                            xmlTagLName.equals("ChannelPath") ||
                            xmlTagLName.equals("Type")) {
                        htmlSB.append("<div  style=\"background:#F7F7F6; color:#ADADA7\">");
                    }
                } else if (eventType == XMLEvent.END_ELEMENT) {
                    String xmlTagLName = xsr.getLocalName();
                    if (xmlTagLName.equals("Entry")) {
                        htmlSB.append("</html>");
                    } else if (xmlTagLName.equals("FeatureBlurb")) {
                        htmlSB.append("<br/>");
                    } else if (xmlTagLName.equals("Name")) {
                        if (nameCount == 0) {
                            htmlSB.append("</h1>");
                            nameCount++;
                        } else {
                            htmlSB.append("</h3>");
                        }
                    } else if (xmlTagLName.equals("Abstract")) {
                        // EMPTY
                    } else if (xmlTagLName.equals("Contributor")) {
                        htmlSB.append("</b>");
                    } else if (xmlTagLName.equals("h3")) {
                        htmlSB.append("</h2>");
                    } else if (xmlTagLName.equals("p")) {
                        htmlSB.append("</p>");
                    } else if (xmlTagLName.equals("Intro")) {
                        // EMPTY
                    } else if (xmlTagLName.equals("ul")) {
                        htmlSB.append("</ul>");
                    } else if (xmlTagLName.equals("li")) {
                        htmlSB.append("</li>");
                    } else if (xmlTagLName.equals("SubEntries")) {
                        // EMPTY
                    } else if (xmlTagLName.equals("SubEntry")) {
                        htmlSB.append("<br>");
                    } else if (xmlTagLName.equals("Sections")) {
                        // EMPTY
                    } else if (xmlTagLName.equals("FurtherReading")) {
                        htmlSB.append("</dl>");
                    } else if (xmlTagLName.equals("Reference")) {
                        htmlSB.append("</dt>");
                    } else if (xmlTagLName.equals("em")) {
                        htmlSB.append("</b>");
                    } else if (xmlTagLName.equals("ExternalSites")) {
                        htmlSB.append("</p>");
                    } else if (xmlTagLName.equals("ExternalSite")) {
                        htmlSB.append("</dl>");
                    } else if (xmlTagLName.equals("Title")) {
                        htmlSB.append("</dt>");
                    } else if (xmlTagLName.equals("Hyperlink")) {
                        htmlSB.append("</dd>");
                    } else if (xmlTagLName.equals("Description")) {
                        htmlSB.append("</dd>");
                    } else if (xmlTagLName.equals("TopicBox")) {
                        htmlSB.append("</dl></div>");
                    } else if (xmlTagLName.equals("Heading")) {
                        htmlSB.append("</h3></dt>");
                    } else if (xmlTagLName.equals("FootNoteRef")) {
                        htmlSB.append("</sup>");
                    } else if (xmlTagLName.equals("FootNotes")) {
                        htmlSB.append("</p>");
                    } else if (xmlTagLName.equals("FootNote")) {
                        htmlSB.append("</dl>");
                    } else if (xmlTagLName.equals("Number")) {
                        htmlSB.append("</dt>");
                    } else if (xmlTagLName.equals("Text")) {
                        htmlSB.append("</dd>");
                    } else if (xmlTagLName.equals("ChannelPath") ||
                            xmlTagLName.equals("Sequence") ||
                            xmlTagLName.equals("Language") ||
                            xmlTagLName.equals("StylesheetColumnLayout") ||
                            xmlTagLName.equals("StylesheetColour") ||
                            xmlTagLName.equals("FeatureImageLocation") ||
                            xmlTagLName.equals("FeatureImageAltTag") ||
                            xmlTagLName.equals("IconLocation") ||
                            xmlTagLName.equals("IntroImageLocation") ||
                            xmlTagLName.equals("IntroImageCaption") ||
                            xmlTagLName.equals("IntroImageAltTag") ||
                            xmlTagLName.equals("AbstractImageLocation") ||
                            xmlTagLName.equals("AbstractImageCaption") ||
                            xmlTagLName.equals("AbstractImageAltTag") ||
                            xmlTagLName.equals("ChannelPath") ||
                            xmlTagLName.equals("Type")) {
                        htmlSB.append("</div>");
                    }
                } else if (eventType == XMLEvent.CHARACTERS && eventType != XMLEvent.SPACE) {
                    String thisText = xsr.getText();
                    thisText = thisText.replace("\\s+", " ");
                    htmlSB.append(thisText);
                }

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Xml2Html.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XMLStreamException ex) {
            Logger.getLogger(Xml2Html.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void clearFolder(String strFolder, final String strExt) {
        // Declare variables
        File fLogDir = new File(strFolder);
        // Get all BCS files
        File[] fLogs = fLogDir.listFiles(new FilenameFilter() {

            public boolean accept(File fDir, String strName) {
                return (strName.endsWith("." + strExt));
            }
        });
        // Delete all files
        for (int i = 0; i < fLogs.length; i++) {
            File thisFile = new File(fLogs[i].getAbsolutePath());
            thisFile.delete();
        }
    }
}
