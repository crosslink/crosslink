package monolink;

public class OutputXml {
	
	protected StringBuffer xmlbuf = new StringBuffer();
	
	protected String sourceLang;
	protected String targetLang;
	
	public OutputXml(String sourceLang, String targetLang) {
		this.sourceLang = sourceLang;
		this.targetLang = targetLang;
	}
	
	public String toString() {
		return xmlbuf.toString();
	}

	public String getSourceLang() {
		return sourceLang;
	}

	public void setSourceLang(String sourceLang) {
		this.sourceLang = sourceLang;
	}

	public String getTargetLang() {
		return targetLang;
	}

	public void setTargetLang(String targetLang) {
		this.targetLang = targetLang;
	}
}
