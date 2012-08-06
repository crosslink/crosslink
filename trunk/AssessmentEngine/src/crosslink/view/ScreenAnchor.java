package crosslink.view;

import crosslink.submission.Anchor;
import crosslink.view.Link;

public class ScreenAnchor {
	private int offset;
	private	int	length;
	private String screenPosStart;
	private String screenPosEnd;
	private String status;
	private int extLength;
	
	private Link link;

	private String screenSubPosStart;
	private String screenSubPosEnd;
	
	public ScreenAnchor(String screenPosStart, String screenPosEnd,
			String status, int extLength) {
		super();
		this.screenPosStart = screenPosStart;
		this.screenPosEnd = screenPosEnd;
		this.status = status;
		this.extLength = extLength;
	}
	
	public ScreenAnchor(String screenPosStart, String screenPosEnd,
			String status, int extLength, String screenSubPosStart,
			String screenSubPosEnd) {
		super();
		this.screenPosStart = screenPosStart;
		this.screenPosEnd = screenPosEnd;
		this.status = status;
		this.extLength = extLength;
		this.screenSubPosStart = screenSubPosStart;
		this.screenSubPosEnd = screenSubPosEnd;
	}

	public ScreenAnchor() {

	}

	public String getScreenPosStart() {
		return screenPosStart;
	}

	public void setScreenPosStart(String screenPosStart) {
		this.screenPosStart = screenPosStart;
	}

	public String getScreenPosEnd() {
		return screenPosEnd;
	}

	public void setScreenPosEnd(String screenPosEnd) {
		this.screenPosEnd = screenPosEnd;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getExtLength() {
		return extLength;
	}

	public void setExtLength(int extLength) {
		this.extLength = extLength;
	}

	public String getScreenSubPosStart() {
		return screenSubPosStart;
	}

	public void setScreenSubPosStart(String screenSubPosStart) {
		this.screenSubPosStart = screenSubPosStart;
	}

	public String getScreenSubPosEnd() {
		return screenSubPosEnd;
	}

	public void setScreenSubPosEnd(String screenSubPosEnd) {
		this.screenSubPosEnd = screenSubPosEnd;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getLength() {
		return length;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getOffset() {
		return offset;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	public Link getLink() {
		return link;
	}

	
}
