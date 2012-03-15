package monolink;

public interface OutputXmlInterface {
	public abstract void open();
	
	public void outputTopicStart(String name, String id);
	
	public void outputLink(String targetId);
	
//	public void outputLink(String id, String title, int bep);
	
	public void outputTopicEnd();
	
	public void outputAnchorStart(String anchor, int pos, int length);
	
	public void outputAnchorEnd();

	public void close();
}
