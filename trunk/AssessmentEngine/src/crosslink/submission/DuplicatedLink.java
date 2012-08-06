package crosslink.submission;

import java.util.HashSet;

import crosslink.submission.Target;

public class DuplicatedLink {
	private Target target;
	private HashSet<String> runIds = new HashSet<String>();
	
	public void addRunId(String id) {
		runIds.add(id);
	}

	public Target getTarget() {
		return target;
	}

	public void setTarget(Target target) {
		this.target = target;
	}
}
