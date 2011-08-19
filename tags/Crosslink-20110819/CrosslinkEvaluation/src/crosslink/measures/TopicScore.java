package crosslink.measures;

public class TopicScore {
	String topicId;
	double score;
	
	public TopicScore(String topicId, double score) {
		this.topicId = topicId;
		this.score = score;
	}
	
	/**
	 * @return the topicId
			 */
				public String getTopicId() {
		return topicId;
	}
	/**
	 * @param topicId the topicId to set
	 */
		public void setTopicId(String topicId) {
		this.topicId = topicId;
	}
	/**
	 * @return the score
			 */
				public double getScore() {
		return score;
	}
	/**
	 * @param score the score to set
	 */
		public void setScore(double score) {
		this.score = score;
	}  	
}
