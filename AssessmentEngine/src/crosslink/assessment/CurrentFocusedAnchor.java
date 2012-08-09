package crosslink.assessment;

import java.util.HashMap;
import java.util.Vector;

import crosslink.view.TopicHighlightManager;
import crosslink.assessment.AssessedAnchor;
import crosslink.assessment.Bep;
import crosslink.assessment.CurrentFocusedAnchor;
import crosslink.assessment.InterfaceAnchor;

public class CurrentFocusedAnchor {
	  
    public final static String sysPropertyCurrTopicOLSEStatusKey = "currTopicOLSE";
    
    private String screenAnchorText;
    
    private InterfaceAnchor anchor = null;
    private Bep currentBep = null;
    
    private HashMap<String, AssessedAnchor> anchors;;

    private static CurrentFocusedAnchor currentFocusedAnchor = null;
    
    static {
    	currentFocusedAnchor = new CurrentFocusedAnchor(); 
    }
    
    public CurrentFocusedAnchor(InterfaceAnchor anchor) {
    	this.anchor = anchor;
    }

//	public CurrentFocusedAnchor(int offset, int length, String name) {
//		anchor = new AssessedAnchor(offset, length, name);
//		
//	}

    public CurrentFocusedAnchor() {
//    	anchor = new AssessedAnchor(0, 0, "");
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
	    
    public AssessedAnchor getAnchor() {
		return (AssessedAnchor) anchor;
	}

	public void setAnchor(AssessedAnchor previous, AssessedAnchor current, Bep bep) {
		try {
			if (previous != null) {
				if (previous.getParent() != current.getParent())
					previous.getParent().statusCheck();
			}
			
			this.anchor = current;
			
			TopicHighlightManager.getInstance().update(previous, (AssessedAnchor)anchor);
			TopicHighlightManager.getInstance().updateLinkPane(bep, currentBep == null || bep != currentBep);
			
			this.currentBep = bep;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			bep.setRel(Bep.IRRELEVANT);
		}
	}

	public Bep getCurrentBep() {
		return currentBep;
	}

	public void setCurrentBep(Bep currentBep) {
		this.currentBep = currentBep;
	}

	public void setAnchor(InterfaceAnchor anchor) {
		this.anchor = anchor;
	}

	public void updateHigherter(int offset, int length) {
		// get the selected Assessed Anchor
		
	}
	
	public void updateHigherter(String offsetKey, String target) {
		// get the selected Assessed Anchor
		AssessedAnchor selectedAnchor = anchors.get(offsetKey);
		if (selectedAnchor != null) {
			Bep found = null;
			for (Bep bep : selectedAnchor.getBeps()) {
				if (bep.getFileId().equals(target)) {
					found = bep;
					break;
				}
			}
			if (found != null)
				setAnchor((AssessedAnchor)anchor, selectedAnchor, found);
			else
				setAnchor((AssessedAnchor)anchor, selectedAnchor, selectedAnchor.getBeps().get(0));
		}
	}

//	public HashMap<String, AssessedAnchor> getAnchors() {
//		return anchors;
//	}

	public void setAnchors(Vector<IndexedAnchor> list) {
		this.anchors = new HashMap<String, AssessedAnchor>();
		for (IndexedAnchor indexedAnchor : list) {
			for (InterfaceAnchor anchor : indexedAnchor.getChildrenAnchors()) {
				String offsetKey = String.valueOf(anchor.getOffset()) + "_" + anchor.getLength();
				anchors.put(offsetKey, (AssessedAnchor)anchor);
			}
		}
	}
}
