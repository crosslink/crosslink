package ltwassessment.assessment;

public class CurrentFocusedAnchor {
	  
    public final static String sysPropertyCurrTopicOLSEStatusKey = "currTopicOLSE";
    
    private String screenAnchorText;
    
    IndexedAnchor anchor = null;

    private static CurrentFocusedAnchor currentFocusedAnchor = null;
    
    static {
    	currentFocusedAnchor = new CurrentFocusedAnchor(); 
    }

	public CurrentFocusedAnchor(int offset, int length, String name) {
		anchor = new IndexedAnchor(offset, length, name);
		
	}

    public CurrentFocusedAnchor() {
    	anchor = new IndexedAnchor(0, 0, "");
	}

	public static CurrentFocusedAnchor getCurrentFocusedAnchor() {
		return currentFocusedAnchor;
	}


//	public void setCurrentAnchorProperty(int offset, int length, int screenPosStart, int screenPosEnd, int status, int extLength) {
//		anchor.setOffset(offset);
//		anchor.setLength(length);
//		this.screenPosStart = screenPosStart;
//		this.screenPosEnd = screenPosEnd;
//		
//		anchor.setStatus(status);
//		
//		setIndexOffset(offset, length);
////    	setCurrentAnchorProperty(String.valueOf(offset), String.valueOf(length), String.valueOf(screenPosStart), String.valueOf(screenPosEnd), String.valueOf(status), String.valueOf(extLength));
//    }
//    
//    public void setCurrentAnchorProperty(String offsetStr, String lengthStr, String screenPosStartStr, String screenPosEndStr, String statusStr, String extLengthStr) {
////    	String sysPropertyValue = offsetStr + "_" + lengthStr + "_" + screenPosStartStr + "_" + screenPosEndStr + "_" + statusStr + "_" + extLengthStr;
////    	System.setProperty(sysPropertyCurrTopicOLSEStatusKey, sysPropertyValue);
//    	setCurrentAnchorProperty(Integer.valueOf(offsetStr), Integer.valueOf(lengthStr), Integer.valueOf(screenPosStartStr), Integer.valueOf(screenPosEndStr), Integer.valueOf(statusStr), Integer.valueOf(extLengthStr));
//    }
    
    public String[] toArray() {
    	String[] array = new String[8];
    	array[0] = anchor.offsetToString();
    	array[1] = anchor.lengthToString();
    	array[2] = anchor.screenPosStartToString();
    	array[3] = anchor.screenPosEndToString();
    	array[4] = anchor.statusToString();
    	array[5] = anchor.extendedLengthToString();
    	array[6] = anchor.offsetIndexToString();
    	array[7] = anchor.lengthIndexToString();
		return array; 	
    }
    
    public void setIndexOffset(int offset, int length) {
    	anchor.setOffsetIndex(offset);
    	anchor.setLengthIndex(length);
    }

	public String getScreenAnchorText() {
		return screenAnchorText;
	}

	public void setScreenAnchorText(String screenAnchorText) {
		this.screenAnchorText = screenAnchorText;
	}
	    
    public IndexedAnchor getAnchor() {
		return anchor;
	}

	public void setAnchor(IndexedAnchor anchor) {
		this.anchor = anchor;
	}
}
