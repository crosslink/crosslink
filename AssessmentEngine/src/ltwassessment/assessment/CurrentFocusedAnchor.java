package ltwassessment.assessment;

public class CurrentFocusedAnchor extends IndexedAnchor {
	  
    public final static String sysPropertyCurrTopicOLSEStatusKey = "currTopicOLSE";
    
    private int screenPosStart;
    private int screenPosEnd;
    
    private static CurrentFocusedAnchor currentFocusedAnchor = null;
    
    static {
    	currentFocusedAnchor = new CurrentFocusedAnchor(); 
    }

	public CurrentFocusedAnchor(int offset, int length, String name) {
		super(offset, length, name);
		
	}

    public CurrentFocusedAnchor() {
    	super(0, 0, "");
	}

	public static CurrentFocusedAnchor getCurrentFocusedAnchor() {
		return currentFocusedAnchor;
	}
	

	public String screenPosStartToString() {
		return String.valueOf(screenPosStart);
	}
	

	public String screenPosEndToString() {
		return String.valueOf(screenPosEnd);
	}

	public void setCurrentAnchorProperty(int offset, int length, int screenPosStart, int screenPosEnd, int status, int extLength) {
		this.offset = offset;
		this.length = length;
		this.screenPosStart = screenPosStart;
		this.screenPosEnd = screenPosEnd;
		
		this.status = status;
		
//    	setCurrentAnchorProperty(String.valueOf(offset), String.valueOf(length), String.valueOf(screenPosStart), String.valueOf(screenPosEnd), String.valueOf(status), String.valueOf(extLength));
    }
    
    public void setCurrentAnchorProperty(String offsetStr, String lengthStr, String screenPosStartStr, String screenPosEndStr, String statusStr, String extLengthStr) {
//    	String sysPropertyValue = offsetStr + "_" + lengthStr + "_" + screenPosStartStr + "_" + screenPosEndStr + "_" + statusStr + "_" + extLengthStr;
//    	System.setProperty(sysPropertyCurrTopicOLSEStatusKey, sysPropertyValue);
    	setCurrentAnchorProperty(Integer.valueOf(offsetStr), Integer.valueOf(lengthStr), Integer.valueOf(screenPosStartStr), Integer.valueOf(screenPosEndStr), Integer.valueOf(statusStr), Integer.valueOf(extLengthStr));
    }
    
    public String[] toArray() {
    	String[] array = new String[8];
    	array[0] = this.offsetToString();
    	array[1] = this.lengthToString();
    	array[2] = this.screenPosStartToString();
    	array[3] = this.screenPosEndToString();
    	array[4] = this.statusToString();
    	array[5] = this.extendedLengthToString();
    	array[6] = this.offsetIndexToString();
    	array[7] = this.lengthIndexToString();
		return array; 	
    }
}
