package ltwassessment.assessment;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;

import ltwassessment.parsers.PoolerManager;
import ltwassessment.parsers.ResourcesManager;
import ltwassessment.submission.Anchor;
import ltwassessment.utility.PoolUpdater;
import ltwassessment.utility.highlightPainters;
import ltwassessment.view.TopicHighlightManager;

public class IndexedAnchor extends Anchor implements InterfaceAnchor {
	public final static int UNINITIALIZED_VALUE = -1; 
	
	public static final int ASSESSMENT_FINISHED_YES = 4;
	public static final int ASSESSMENT_FINISHED_NO = 8;
	
	protected int status = 0;
	
	protected int offsetIndex;
	protected int lengthIndex;
	
    protected int screenPosStart = -1;
    protected int screenPosEnd = -1;
	
	private Vector<InterfaceAnchor> childrenAnchors = new Vector<InterfaceAnchor>();
	
	private Vector<Object> anchorHighlightReferences = new Vector<Object>();
	
	String[] scrFOL = null;
	
	protected Vector<Bep> beps = new Vector<Bep>();

	public IndexedAnchor(int offset, int length, String name) {
		super(offset, length, name);
	}

	public IndexedAnchor(String offset, String length, String name) {
		super(Integer.parseInt(offset), Integer.parseInt(length), name);
	}
	
	public IndexedAnchor(int offset, int length, String name, int rel) {
		super(offset, length, name);
		this.status = rel;
	}
	
	public IndexedAnchor(String offset, String length, String name, String rel) {
		super(Integer.parseInt(offset), Integer.parseInt(length), name);
		this.status = Integer.parseInt(rel);
	}
	
	public IndexedAnchor() {
		super(0, 0, "");
	}

	public void addBep(Bep bep) {
		beps.add(bep);
	}
	
	public Vector<Bep> getBeps() {
		return beps;
	}	
	
	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}
	
	/**
	 * @return the offsetIndex
	 */
	public int getOffsetIndex() {
		return offsetIndex;
	}

	/**
	 * @param offsetIndex the offsetIndex to set
	 */
	public void setOffsetIndex(int offsetIndex) {
		this.offsetIndex = offsetIndex;
	}

	/**
	 * @return the lengthIndex
	 */
	public int getLengthIndex() {
		return lengthIndex;
	}

	/**
	 * @param lengthIndex the lengthIndex to set
	 */
	public void setLengthIndex(int lengthIndex) {
		this.lengthIndex = lengthIndex;
	}

	public String statusToString() {
		return String.valueOf(status);
	}

	public String lengthIndexToString() {
		return String.valueOf(lengthIndex);
	}

	public String offsetIndexToString() {
		return String.valueOf(offsetIndex);
	}
	
	public String screenPosStartToString() {
		return String.valueOf(screenPosStart);
	}
	

	public String screenPosEndToString() {
		return String.valueOf(screenPosEnd);
	}
	

	public int getScreenPosStart() {
		return screenPosStart;
	}

	public void setScreenPosStart(int screenPosStart) {
		this.screenPosStart = screenPosStart;
	}

	public int getScreenPosEnd() {
		return screenPosEnd;
	}

	public void setScreenPosEnd(int screenPosEnd) {
		this.screenPosEnd = screenPosEnd;
	}
    
    public void setIndexOffset(int offset, int length) {
    	setOffsetIndex(offset);
    	setLengthIndex(length);
    }
    
	public void setCurrentAnchorProperty(int offset, int length, int screenPosStart, int screenPosEnd, int status, int extLength) {
		setOffset(offset);
		setLength(length);
		screenPosStart = screenPosStart;
		screenPosEnd = screenPosEnd;
		
		setStatus(status);
		
		setIndexOffset(offset, length);
		//setCurrentAnchorProperty(String.valueOf(offset), String.valueOf(length), String.valueOf(screenPosStart), String.valueOf(screenPosEnd), String.valueOf(status), String.valueOf(extLength));
	}
	
	public void setCurrentAnchorProperty(String offsetStr, String lengthStr, String screenPosStartStr, String screenPosEndStr, String statusStr, String extLengthStr) {
		//String sysPropertyValue = offsetStr + "_" + lengthStr + "_" + screenPosStartStr + "_" + screenPosEndStr + "_" + statusStr + "_" + extLengthStr;
		//System.setProperty(sysPropertyCurrTopicOLSEStatusKey, sysPropertyValue);
		setCurrentAnchorProperty(Integer.valueOf(offsetStr), Integer.valueOf(lengthStr), Integer.valueOf(screenPosStartStr), Integer.valueOf(screenPosEndStr), Integer.valueOf(statusStr), Integer.valueOf(extLengthStr));
	}
	
	public void addChildAnchor(IndexedAnchor anchor) {
		childrenAnchors.add((InterfaceAnchor) anchor);
	}

	public Vector<InterfaceAnchor> getChildrenAnchors() {
		return childrenAnchors;
	}
	
	public String[] getScrFOL() {
		return scrFOL;
	}

	public void setScrFOL(String[] scrFOL) {
		this.scrFOL = scrFOL;
	}

	public String toKey() {
		return this.offsetToString() + "_" +  this.lengthToString();
	}

	public Vector<Object> getAnchorHighlightReference() {
		return anchorHighlightReferences;
	}

	public void addAnchorHighlightReference(Object anchorHighlightReference) {
		this.anchorHighlightReferences.add(anchorHighlightReference);
	}
	
	public void setHighlighter(Highlighter txtPaneHighlighter, highlightPainters painters) {
		assert(this instanceof IndexedAnchor);
		Object anchorHighlightReference = null;
        int ext_length = getExtendedLength(); 
    	int sp = getScreenPosStart(); 
    	int se = getScreenPosEnd() + ext_length;
    	
    	for (Object theHighlightReference : anchorHighlightReferences)
    		txtPaneHighlighter.removeHighlight(theHighlightReference);
    			
    	try {
    		if (status == 1) {
	            anchorHighlightReference = txtPaneHighlighter.addHighlight(sp, se, painters.getCompletePainter());
	        } else if (status == -1) {
	            anchorHighlightReference = txtPaneHighlighter.addHighlight(sp, se, painters.getIrrelevantPainter());
	        } 
	        else {
		        anchorHighlightReference = txtPaneHighlighter.addHighlight(sp, se, painters.getAnchorPainter());
	        }
	        
	        anchorHighlightReferences.add(anchorHighlightReference);
	    } catch (BadLocationException ex) {
	        Logger.getLogger(TopicHighlightManager.class.getName()).log(Level.SEVERE, null, ex);
	    } catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public void setToCurrentAnchor(Highlighter txtPaneHighlighter, highlightPainters painters, InterfaceAnchor currAnchorSE) {	
    	int subanchorScreenStart = currAnchorSE.getScreenPosStart(); 
    	int subanchorScreenEnd = currAnchorSE.getScreenPosEnd(); 
    	Object anchorHighlightReference = null;

    	int sp = getScreenPosStart(); 
    	int se = getScreenPosStart() + getLength() + getExtendedLength(); 
    	
    	for (Object theHighlightReference : currAnchorSE.getParent().getAnchorHighlightReference())
    		txtPaneHighlighter.removeHighlight(theHighlightReference);
    	
        int sp1 = 0, se1 = 0;
        int sp2 = 0, se2 = 0;
    	if (subanchorScreenStart != sp || subanchorScreenEnd != se) {
    		
    		if (subanchorScreenStart > sp) {
    			sp1 = sp;
    			se1 = subanchorScreenStart; 			
    		}
    		
    		if (subanchorScreenEnd < se) {
    			sp2 = subanchorScreenEnd;
    			se2 = se; 
    		}

        } 

    	try {
			anchorHighlightReference = txtPaneHighlighter.addHighlight(subanchorScreenStart, subanchorScreenEnd, painters.getSelectedPainter());
			anchorHighlightReferences.add(anchorHighlightReference);
			
	        if (se1 > sp1) {
	            if (status == 1) {
	                anchorHighlightReference = txtPaneHighlighter.addHighlight(sp1, se1, painters.getCompletePainter());
	            } else if (status == -1) {
	                anchorHighlightReference = txtPaneHighlighter.addHighlight(sp1, se1, painters.getIrrelevantPainter());
	            }
	            else
	            	anchorHighlightReference = txtPaneHighlighter.addHighlight(sp1, se1, painters.getAnchorPainter());	  
	            anchorHighlightReferences.add(anchorHighlightReference);
	        }
	        
	        if (se2 > sp2) {
	            if (status == 1) {
	                anchorHighlightReference = txtPaneHighlighter.addHighlight(sp2, se2, painters.getCompletePainter());
	            } else if (status == -1) {
	                anchorHighlightReference = txtPaneHighlighter.addHighlight(sp2, se2, painters.getIrrelevantPainter());
	            }
	            else
	            	anchorHighlightReference = txtPaneHighlighter.addHighlight(sp2, se2, painters.getAnchorPainter());
	            anchorHighlightReferences.add(anchorHighlightReference);
	        }
    	}
    	catch (BadLocationException ex) {
	        Logger.getLogger(TopicHighlightManager.class.getName()).log(Level.SEVERE, null, ex);
	    } 
	}
	
	public InterfaceAnchor getNext(InterfaceAnchor currentAnchor, boolean needNotFinished) {
    	int i = 0;
    	InterfaceAnchor anchor = null;
    	InterfaceAnchor  unassessedAnchor = null;
    	if (currentAnchor != null)
	    	for (; i < getChildrenAnchors().size(); ++i) {
	    		anchor = getChildrenAnchors().get(i);
	    		
	    		if (needNotFinished && unassessedAnchor == null && anchor.checkStatus() == ASSESSMENT_FINISHED_NO)
	    			unassessedAnchor = anchor;
	    		
	    		if (anchor == currentAnchor) {
	    			++i;
	    			break;
	    		}
	    	}
    	
    	if (needNotFinished)
	    	for (; i < getChildrenAnchors().size(); ++i) {
	    		anchor = getChildrenAnchors().get(i);
	    		if (anchor.checkStatus() == ASSESSMENT_FINISHED_NO) {
	    			unassessedAnchor = anchor;
	    			break;
	    		}
	    	}
    	
    	if (i >= getChildrenAnchors().size())
    		i = 0;
    	
    	anchor = getChildrenAnchors().get(i);
    	return needNotFinished ? unassessedAnchor : anchor;
	}
	
	public InterfaceAnchor getPrevious(InterfaceAnchor currentAnchor, boolean needNotFinished) {		
    	int i = getChildrenAnchors().size() - 1;
    	InterfaceAnchor anchor = null;
    	InterfaceAnchor  unassessedAnchor = null;
    	if (currentAnchor != null)
	    	for (; i > -1; --i) {
	    		anchor = getChildrenAnchors().get(i);
	    		
	    		if (needNotFinished && unassessedAnchor == null && anchor.checkStatus() == ASSESSMENT_FINISHED_NO)
	    			unassessedAnchor = anchor;
	    		
	    		if (anchor == currentAnchor) {
	    			--i;
	    			break;
	    		}
	    	}
    	
    	if (needNotFinished)
	    	for (; i > -1; --i) {
	    		anchor = getChildrenAnchors().get(i);
	    		if (anchor.checkStatus() == ASSESSMENT_FINISHED_NO) {
	    			unassessedAnchor = anchor;
	    			break;
	    		}
	    	}
    	
    	if (i < 0)
    		i = getChildrenAnchors().size() - 1;
    	
    	anchor = getChildrenAnchors().get(i);
    	return needNotFinished ? unassessedAnchor : anchor;
	}
	
	public void statusCheck() {
//		if (status == Bep.UNASSESSED) {
			int rel = status; //Bep.IRRELEVANT;
			
//	    	if (getName().equalsIgnoreCase("fermented fish"))
//	    		System.out.println("I got you");
	    	
			if (name.trim().length() == 0)
				rel = Bep.IRRELEVANT;
			else {
				boolean hasRelevantLink = false;
				
//				if (offset == 1750 || name.equals("試驗"))
//					System.out.println("Stop here");
				
				for (InterfaceAnchor subanchor : getChildrenAnchors()) {
					if (subanchor.checkStatus() == ASSESSMENT_FINISHED_NO) {
		//				finished = ASSESSMENT_FINISHED_NO;
						rel = Bep.UNASSESSED;
						hasRelevantLink = true;
						break;
					}
					else {
						if (rel == Bep.UNASSESSED)
							rel = subanchor.getStatus();
						
						int subStatus = subanchor.getStatus();
						
						if (subStatus == Bep.RELEVANT) {
							hasRelevantLink = true;
							rel = Bep.RELEVANT;
						}
					}
				}
				
				if (!hasRelevantLink && status == Bep.RELEVANT)
					rel = Bep.IRRELEVANT;
			}
		
			if (rel != status) {
				setStatus(rel);
				PoolerManager.getPoolUpdater().updatePoolAnchorStatus(ResourcesManager.getInstance().getTopicID(), this);
			}
//		}
//		return finished;
	}

	public void markIrrevlent() {
		for (InterfaceAnchor anchor : this.getChildrenAnchors())
			anchor.markAllLinksIrrevlent();
	}

	@Override
	public IndexedAnchor getParent() {
		return null;
	}

	@Override
	public void markAllLinksIrrevlent() {
		
	}

	@Override
	public int checkStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Bep getPreviousLink(Bep currentLink, boolean nextUnassessed) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bep getNextLink(Bep currentLink, boolean nextUnassessed) {
		// TODO Auto-generated method stub
		return null;
	}
}
