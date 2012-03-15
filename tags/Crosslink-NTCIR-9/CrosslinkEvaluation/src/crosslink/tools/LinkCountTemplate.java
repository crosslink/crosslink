package crosslink.tools;

public class LinkCountTemplate<E> implements Comparable {

	protected String		id;
	protected int 	uniqLinkCount = 0;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getUniqLinkCount() {
		return uniqLinkCount;
	}

	public void setUniqLinkCount(int uniqLinkCount) {
		this.uniqLinkCount = uniqLinkCount;
	}
	
	
	public LinkCountTemplate(String id) {
		super();
		this.id = id;
	}

	@Override
	public int compareTo(Object arg0) {
		LinkCountTemplate<E> item = (LinkCountTemplate<E>)arg0;
		
		if (item.uniqLinkCount < this.uniqLinkCount)
			return -1;
		else if (item.uniqLinkCount > this.uniqLinkCount)
			return 1;
		return 0;
	}

	public void increaseLinkCount(int i) {
		uniqLinkCount += i;
	}
	
	public void increaseLinkCount() {
		increaseLinkCount(1);
	}
}
