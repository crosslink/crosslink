package crosslink.measures;

import java.util.ArrayList;

public class RunTopicScore implements Comparable {
	private ArrayList<TopicScore> topicScores = new ArrayList<TopicScore>();
	
	private String runId;
	
	private double measureScore;
	
	private int index;

	public RunTopicScore(String id, double score) {
		runId = id;
		measureScore = score;
	}
	
	public ArrayList<TopicScore> getTopicScores() {
		return topicScores;
	}

	public void setTopicScores(ArrayList<TopicScore> topicScores) {
		this.topicScores = topicScores;
	}

	public String getRunId() {
		return runId;
	}

	public void setRunId(String runId) {
		this.runId = runId;
	}

	public double getMeasureScore() {
		return measureScore;
	}

	public void setMeasureScore(double measureScore) {
		this.measureScore = measureScore;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public int compareTo(Object o) {
		RunTopicScore another = (RunTopicScore) o;
		if (this.getMeasureScore() > another.getMeasureScore())
			return -1;
		else if (this.getMeasureScore() < another.getMeasureScore())
			return 1;
		return 0;
	}
}
