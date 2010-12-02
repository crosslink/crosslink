package ltwassessment.view;

import ltwassessment.pool.Anchor;

public class ScreenAnchor {
	private String screenPosStart;
	private String screenPosEnd;
	private String status;
	private String extLength;

	private String screenSubPosStart;
	private String screenSubPosEnd;
	
	public ScreenAnchor(String screenPosStart, String screenPosEnd,
			String status, String extLength) {
		super();
		this.screenPosStart = screenPosStart;
		this.screenPosEnd = screenPosEnd;
		this.status = status;
		this.extLength = extLength;
	}
	
	public ScreenAnchor(String screenPosStart, String screenPosEnd,
			String status, String extLength, String screenSubPosStart,
			String screenSubPosEnd) {
		super();
		this.screenPosStart = screenPosStart;
		this.screenPosEnd = screenPosEnd;
		this.status = status;
		this.extLength = extLength;
		this.screenSubPosStart = screenSubPosStart;
		this.screenSubPosEnd = screenSubPosEnd;
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

	public String getExtLength() {
		return extLength;
	}

	public void setExtLength(String extLength) {
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

	
}
