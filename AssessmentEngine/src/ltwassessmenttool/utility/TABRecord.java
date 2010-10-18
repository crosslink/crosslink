package ltwassessmenttool.utility;

/**
 * @author Darren HUANG
 */
public class TABRecord {

    // Column Titles
    protected String topicCol;
    protected String anchorCol;
    protected String subAnchorCol;
    protected String bepCol;
    protected String fiddenFieldCol;

    public TABRecord() {
        topicCol = "";
        anchorCol = "";
        subAnchorCol = "";
        bepCol = "";
        fiddenFieldCol = "";
    }

    public String getTopic(){
        return topicCol;
    }
    public void setTopic(String topicIDName){
        this.topicCol = topicIDName;
    }

    public String getAnchor(){
        return anchorCol;
    }
    public void setAnchor(String poolAnchorOLName){
        this.anchorCol = poolAnchorOLName;
    }

    public String getSubAnchor(){
        return subAnchorCol;
    }
    public void setSubAnchor(String anchorOLName){
        this.subAnchorCol = anchorOLName;
    }

    public String getBEP(){
        return bepCol;
    }
    public void setBEP(String bepOffsetFileIDName){
        this.bepCol = bepOffsetFileIDName;
    }

    public String getHiddenFieldValue(){
        return fiddenFieldCol;
    }
    public void setHiddenFieldValue(String hiddenFieldValue){
        this.fiddenFieldCol = hiddenFieldValue;
    }
}
