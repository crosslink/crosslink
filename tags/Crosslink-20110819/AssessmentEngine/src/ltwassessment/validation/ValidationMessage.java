package ltwassessment.validation;

import javax.swing.JTextPane;

public class ValidationMessage {
	private StringBuffer buffer = new StringBuffer();
	private JTextPane outputPane = null;
	
	private static ValidationMessage instance = null;
	
	public static ValidationMessage getInstance() {
		if (instance == null)
			instance = new ValidationMessage();
		return instance;
	}
	
	/**
	 * @param outputPane the outputPane to set
	 */
	public void setOutputPane(JTextPane outputPane) {
		this.outputPane = outputPane;
	}

	public void append(String msg) {
		buffer.append(msg);
		buffer.append("\n");
	}
	
	public String getMessage() {
		return buffer.toString();
	}
	
	public void flush() {
		outputPane.setText(getMessage());
		buffer.setLength(0);
	}
}
