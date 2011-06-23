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
