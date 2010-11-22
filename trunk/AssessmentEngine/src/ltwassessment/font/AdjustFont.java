package ltwassessment.font;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JTextPane;

import ltwassessment.AppResource;

public class AdjustFont {
	private static Font zhFontOrigin = null;
	private static Font zhFont = null;
	private static Font enFontOrigin = null;
	private static Font enFont = null;
//	private static Font jaFontOrigin = null;
//	private static Font jaFont = null;
//	private static Font koFontOrigin = null;
//	private static Font koFont = null;	
	private static final String zhFontFile = "wqy-zenhei.ttc";
	private static AdjustFont instance = null;
	
	
	public AdjustFont() {
		try {
			if (zhFont == null) {
				zhFontOrigin = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, getClass().getResourceAsStream(zhFontFile));
				zhFont = zhFontOrigin.deriveFont(Font.PLAIN, 13);
			}
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

	public static void setComponentFont(JComponent jCom, String lang) {
		if (lang.equals("zh") || lang.equals("jp") || lang.equals("ko")) {
			enFontOrigin = jCom.getFont();
			jCom.setFont(zhFont);
		}
		else {
			if (enFontOrigin != null)
				jCom.setFont(enFontOrigin);
		}
		//else (lang.equals("zh"))
		if (jCom instanceof JTextPane)
			jCom.putClientProperty(((JTextPane)jCom).HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
	}
}
