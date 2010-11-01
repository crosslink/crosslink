package ltwassessment.font;

import java.awt.Font;

import javax.swing.JTextPane;

import ltwassessment.AppResource;

public class TargeFontSetting {
	static void setFont(JTextPane pane) {
		if (AppResource.targetLang.equals("zh"))
			pane.setFont(new Font("WenQuanYi Zen Hei", Font.PLAIN, 13)/*resourceMap.getFont("topicTextPane.font")*/);
		//else (AppResource.targetLang.equals("zh"))
		pane.putClientProperty(pane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
	}
}
