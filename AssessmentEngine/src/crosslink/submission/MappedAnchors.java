package crosslink.submission;

import java.util.HashMap;

import crosslink.submission.Anchor;
import crosslink.submission.AnchorSetInterface;

public class MappedAnchors extends HashMap<String, Anchor>  implements AnchorSetInterface {

	@Override
	public void insert(Anchor anchor) {
		if (this.containsKey(anchor.getName())) {
			Anchor existing = this.get(anchor.getName());
			existing.addTargets(anchor.getTargets());
		}
		else
			this.put(anchor.getName(), anchor);
	}

	@Override
	public void sort() {
		
	}
}