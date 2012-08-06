package crosslink.utility;

/**
 * @author Darren HUANG
 */
public class TBARecord {

    // Column Titles
    protected String topicCol;
    protected String bepCol;
    protected String anchorCol;
    protected String fileIDCol;
    protected String fiddenFieldCol;


    public TBARecord() {
        topicCol = "";
        bepCol = "";
        anchorCol = "";
        fileIDCol = "";
        fiddenFieldCol = "";
    }

    public String getTopic(){
        return topicCol;
    }
    public void setTopic(String topicIDName){
        this.topicCol = topicIDName;
    }

    public String getBEP(){
        return bepCol;
    }
    public void setBEP(String bepOffset){
        this.bepCol = bepOffset;
    }

    public String getAnchor(){
        return anchorCol;
    }
    public void setAnchor(String anchorOLName){
        this.anchorCol = anchorOLName;
    }

    public String getAnchorFileID(){
        return fileIDCol;
    }
    public void setAnchorFileID(String anchorFileIDName){
        this.fileIDCol = anchorFileIDName;
    }

    public String getHiddenFieldValue(){
        return fiddenFieldCol;
    }
    public void setHiddenFieldValue(String hiddenFieldValue){
        this.fiddenFieldCol = hiddenFieldValue;
    }

}
