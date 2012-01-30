package ltwassessment.submission;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import com.spreada.utils.chinese.ZHConverter;

import ltwassessment.AppResource;

public class LinkedAnchors extends LinkedList<Anchor> implements AnchorSetInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1333492441614276545L;
	
	private HashMap<String, Anchor> anchorMap = null;

	public LinkedAnchors() {
		super();
		
		anchorMap = new HashMap<String, Anchor>();
	}
	
	public void insert(Anchor anchor) {
		String key = anchor.getName();
		if (AppResource.sourceLang.equalsIgnoreCase("zh")) {
			key = ZHConverter.convert(key, ZHConverter.SIMPLIFIED);
			
			if (!key.equals(anchor.getName()))
				System.err.println("Converted anchor name from: " + anchor.getName() + ", to " + key);
		}
		if (!anchorMap.containsKey(key))
			anchorMap.put(key, anchor);
		else {
			anchorMap.get(key).addTargets(anchor.getTargets());
			return;
		}
		
		this.add(anchor);
	}
	
	public void sort() {
	     Collections.sort(this, new Comparator<Anchor>() {
	         @Override
	         public int compare(Anchor anchor1, Anchor anchor2) {
	             return anchor1.compareTo(anchor2);
	         }
	     });

	}
	
	public boolean validateAll(Topic topic, int showMessage, boolean convertToTextOffset) {
		boolean result = true;
		for (Anchor anchor: this) {
			if (!anchor.validate(topic, showMessage, convertToTextOffset))
				result = false;
		}
		return result;
	}

}
