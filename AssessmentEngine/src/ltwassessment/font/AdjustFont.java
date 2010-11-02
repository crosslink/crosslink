package ltwassessment.font;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;

import javax.swing.JTextPane;

import ltwassessment.AppResource;

public class AdjustFont {
	private static Font zhFont = null;
	private static final String zhFontPath = "ltwassessment/font/wqy-zenhei.ttc";
	private AdjustFont instance = null;
	
	
	public AdjustFont() {
		try {
			if (zhFont == null)
				zhFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, getClass().getResourceAsStream(zhFontPath));
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public AdjustFont getInstance() {
		if (instance == null)
			instance = new AdjustFont();
		return instance;
	}

	static void setFont(JTextPane pane) {
		if (AppResource.targetLang.equals("zh"))
			pane.setFont(new Font("WenQuanYi Zen Hei", Font.PLAIN, 13)/*resourceMap.getFont("topicTextPane.font")*/);
		//else (AppResource.targetLang.equals("zh"))
		pane.putClientProperty(pane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
	}
}
