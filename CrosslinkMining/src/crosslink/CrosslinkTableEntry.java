package crosslink;

public class CrosslinkTableEntry {
	private String sourceId;
	private String targetId;
	private String sourceTitle;
	private String targetTitle;
	
	public CrosslinkTableEntry(String sourceId, String targetId,
			String sourceTitle, String targetTitle) {
		super();
		this.sourceId = sourceId;
		this.targetId = targetId;
		this.sourceTitle = sourceTitle;
		this.targetTitle = targetTitle;
	}

	/**
	 * @return the sourceId
	 */
	public String getSourceId() {
		return sourceId;
	}

	/**
	 * @param sourceId the sourceId to set
	 */
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	/**
	 * @return the targetId
	 */
	public String getTargetId() {
		return targetId;
	}

	/**
	 * @param targetId the targetId to set
	 */
	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	/**
	 * @return the sourceTitle
	 */
	public String getSourceTitle() {
		return sourceTitle;
	}

	/**
	 * @param sourceTitle the sourceTitle to set
	 */
	public void setSourceTitle(String sourceTitle) {
		this.sourceTitle = sourceTitle;
	}

	/**
	 * @return the targetTitle
	 */
	public String getTargetTitle() {
		return targetTitle;
	}

	/**
	 * @param targetTitle the targetTitle to set
	 */
	public void setTargetTitle(String targetTitle) {
		this.targetTitle = targetTitle;
	}

}
