package ltwassessment.font;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JTextPane;

import ltwassessment.AppResource;

public class AdjustFont {
	private static Font zhFont = null;
	private static final String zhFontFile = "wqy-zenhei.ttc";
	private static AdjustFont instance = null;
	
	
	public AdjustFont() {
		try {
			if (zhFont == null)
				zhFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, getClass().getResourceAsStream(zhFontFile));
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static AdjustFont getInstance() {
		if (instance == null)
			instance = new AdjustFont();
		return instance;
	}

	static void setFont(JComponent jCom) {
		if (AppResource.targetLang.equals("zh"))
			jCom.setFont(zhFont);
		//else (AppResource.targetLang.equals("zh"))
		if (jCom instanceof JTextPane)
			jCom.putClientProperty(((JTextPane)jCom).HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
	}
}
