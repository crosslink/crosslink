package ltwassessmenttool.utility;

import java.util.HashMap;

/**
 * @author Darren HUANG
 */
public class Xml2HtmlTagsMap {

    private HashMap startTagesMap = new HashMap();
    private HashMap endTagesMap = new HashMap();
    private String[] recTags;

    public Xml2HtmlTagsMap(String[] topicTags) {
        this.recTags = topicTags;
        populateStartHtmlTagsMap(startTagesMap);
        populateEndHtmlTagsMap(endTagesMap);
    }

    public HashMap getHtmlStartTagsMap() {
        return startTagesMap;
    }
    public HashMap getHtmlEndTagsMap() {
        return endTagesMap;
    }

    private void populateStartHtmlTagsMap(HashMap sTagesMap) {
        for (int i = 0; i < recTags.length; i++) {

            String xmlTagLName = recTags[i];
            if (xmlTagLName.equals("Entry")){
                sTagesMap.put(xmlTagLName, "<HTML>");
            } else if (xmlTagLName.equals("Name")){
                sTagesMap.put(xmlTagLName, "<p><H3>");
            } else if (xmlTagLName.equals("Contributor")){
                sTagesMap.put(xmlTagLName, "<p><b>");
            } else if (xmlTagLName.equals("h3")){
                sTagesMap.put(xmlTagLName, "<H3>");
            } else if (xmlTagLName.equals("p")){
                sTagesMap.put(xmlTagLName, "<p>");
            } else if (xmlTagLName.equals("Intro")){
                sTagesMap.put(xmlTagLName, "<DIV>");
            } else if (xmlTagLName.equals("ul")){
                sTagesMap.put(xmlTagLName, "<ul>");
            } else if (xmlTagLName.equals("li")){
                sTagesMap.put(xmlTagLName, "<li>");
            } else if (xmlTagLName.equals("SubEntry")){
                sTagesMap.put(xmlTagLName, "<p>");
            } else if (xmlTagLName.equals("FurtherReading")){
                sTagesMap.put(xmlTagLName, "<p><ul>");
            } else if (xmlTagLName.equals("Reference")){
                sTagesMap.put(xmlTagLName, "<li>");
            } else if (xmlTagLName.equals("em")){
                sTagesMap.put(xmlTagLName, "<b>");
            } else if (xmlTagLName.equals("ExternalSites")){
                sTagesMap.put(xmlTagLName, "<p>");
            } else if (xmlTagLName.equals("ExternalSite")){
                sTagesMap.put(xmlTagLName, "<dl>");
            } else if (xmlTagLName.equals("Title")){
                sTagesMap.put(xmlTagLName, "<dt>");
            } else if (xmlTagLName.equals("Hyperlink")){
                sTagesMap.put(xmlTagLName, "<dd>");
            } else if (xmlTagLName.equals("Description")){
                sTagesMap.put(xmlTagLName, "<dd>");
            } else if (xmlTagLName.equals("TopicBox")){
                sTagesMap.put(xmlTagLName, "<DIV>");
            } else if (xmlTagLName.equals("Heading")){
                sTagesMap.put(xmlTagLName, "<H3>");
            } else if (xmlTagLName.equals("FootNoteRef")){
                sTagesMap.put(xmlTagLName, "<sup>");
            } else if (xmlTagLName.equals("Heading")){
                sTagesMap.put(xmlTagLName, "<dd>");
            } else if (xmlTagLName.equals("FootNotes")){
                sTagesMap.put(xmlTagLName, "<p>");
            } else if (xmlTagLName.equals("FootNote")){
                sTagesMap.put(xmlTagLName, "<dd>");
            } else {
                sTagesMap.put(xmlTagLName, "<DIV>");
            }

        }
    }

    private void populateEndHtmlTagsMap(HashMap endTagesMap) {

    }

}
