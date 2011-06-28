package ltwassessment.view;

import java.awt.Color;
import java.io.File;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import ltwassessment.AppResource;
import ltwassessment.Assessment;
import ltwassessment.assessment.AssessedAnchor;
import ltwassessment.assessment.Bep;
import ltwassessment.assessment.IndexedAnchor;
import ltwassessment.parsers.PoolerManager;
import ltwassessment.parsers.ResourcesManager;
import ltwassessment.parsers.Xml2Html;
import ltwassessment.utility.highlightPainters;

public class TopicHighlightManager {
	
	private Highlighter txtPaneHighlighter = null;
	private JTextPane pane = null;
	private JTextPane linkPane = null;
	private String afTasnCollectionErrors;
	private String bepIconImageFilePath;
	private String bepHighlightIconImageFilePath;
	private String bepCompletedIconImageFilePath;
	private String bepNonrelevantIconImageFilePath;
    private static highlightPainters painters = new highlightPainters();
    
    public final static Color linkPaneWhiteColor = Color.WHITE;
    public final static Color linkPaneRelColor = new Color(168, 232, 177);
    public final static Color linkPaneNonRelColor = new Color(255, 183, 165);
    
    protected final int bepLength = 4;
	
	private static TopicHighlightManager instance = null;
	
	public TopicHighlightManager() {
        org.jdesktop.application.ResourceMap resourceMap = AppResource.getInstance().getResourceMap();
        afTasnCollectionErrors = resourceMap.getString("AssFormXml.taskCollectionError");
        bepIconImageFilePath = resourceMap.getString("bepIcon.imageFilePath");
        bepHighlightIconImageFilePath = resourceMap.getString("bepHighlightIcon.imageFilePath");
        bepCompletedIconImageFilePath = resourceMap.getString("bepCompletedIcon.imageFilePath");
        bepNonrelevantIconImageFilePath = resourceMap.getString("bepNonrelevantIcon.imageFilePath");
	}
	
	public static TopicHighlightManager getInstance() {
		if (instance == null)
			instance = new TopicHighlightManager();
		return instance;
	}

	public JTextPane getPane() {
		return pane;
	}

	public void setPane(JTextPane pane) {
		this.pane = pane;
		
		txtPaneHighlighter = pane.getHighlighter();
	}

	public JTextPane getLinkPane() {
		return linkPane;
	}

	public void setLinkPane(JTextPane linkPane) {
		this.linkPane = linkPane;
	}

	public void initializeHighlighter(Vector<IndexedAnchor> anchors) {
//    	txtPaneHighlighter.removeAllHighlights();

        for (IndexedAnchor anchor : anchors)
        	anchor.setHighlighter(txtPaneHighlighter, painters);
	}
	
	public void update(AssessedAnchor preAnchor, AssessedAnchor currAnchor) {
		if (preAnchor != null) {
			preAnchor.getParent().setHighlighter(txtPaneHighlighter, painters);
		}
		
		currAnchor.getParent().setToCurrentAnchor(txtPaneHighlighter, painters, currAnchor);
		
        pane.getCaret().setDot(currAnchor.getScreenPosEnd());
        pane.scrollRectToVisible(pane.getVisibleRect());
        pane.repaint();
	}

	public void updateLinkPaneText(Bep link) {
    	String bepID = link.getFileId();
		//      String bepRel = PoolerManager.getInstance().getPoolAnchorBepLinkStatus(Assessment.getInstance().getCurrentTopic().getId(), link.getAssociatedAnchor().getParent(), bepID);
		String bepXmlFilePath = ResourcesManager.getInstance().getWikipediaFilePathByName(bepID + ".xml", link.getTargetLang());
		int bepStartp = link.getStartP(); //this.poolerManager.getPoolAnchorBepLinkStartP(this.currTopicID, currSCRSEName, bepID);
		if (!new File(bepXmlFilePath).exists()) {
		    bepXmlFilePath = ResourcesManager.getInstance().getErrorXmlFilePath(bepXmlFilePath);
		}
		Xml2Html xmlParser = new Xml2Html(bepXmlFilePath, true);
		String xmlHtmlText = xmlParser.getHtmlContent().toString();
		
		linkPane.setText(xmlHtmlText);
		linkPane.getCaret().setDot(Integer.valueOf(bepStartp));
		linkPane.scrollRectToVisible(linkPane.getVisibleRect());
		
		updatePaneBepIcon(linkPane, link, false);
	}
	
	public void updateLinkPane(Bep link, boolean updateText) {
		if (updateText)
			updateLinkPaneText(link);
		
		updateLinkPaneBackground(link);
		linkPane.repaint();		
	}
	
	public void updateLinkPaneBackground(Bep link) {
        int bepRel = link.getRel();
		if (bepRel == 1) {
            linkPane.setBackground(TopicHighlightManager.linkPaneRelColor);
        } else if (bepRel == -1) {
            linkPane.setBackground(TopicHighlightManager.linkPaneNonRelColor);
        } else if (bepRel == 0) {
            linkPane.setBackground(TopicHighlightManager.linkPaneWhiteColor);
        }
	}

	private void updatePaneBepIcon(JTextPane txtPane, Bep link, boolean isTopicBEP) {
    // TODO: "isHighlighBEP"
    // 1) YES: Remove previous Highlight BEPs + Make a new Highlight BEP
    // 2) NO: INSERT BEP ICONs for this Topic
		try {
        // Get Pre-BEP data
//      String[] preBepOLSEStatusSA = currentAnchor;
//          String prePBepO = preBepOLSEStatusSA[0];
//          String prePBepL = preBepOLSEStatusSA[1];
//String prePBepS = preBepOLSEStatusSA[2];
//String prePBepE = preBepOLSEStatusSA[3];
			AssessedAnchor currentAnchor = link.getAssociatedAnchor();
			String prePBepO = currentAnchor.offsetToString(); //preAnchorOLSEStatusSA[0];
			String prePBepL = currentAnchor.lengthToString(); //preAnchorOLSEStatusSA[1];
			String prePBepS = currentAnchor.screenPosStartToString(); //preAnchorOLSEStatusSA[2];
			String prePBepE = currentAnchor.screenPosEndToString(); //preAnchorOLSEStatusSA[3];
			String prePBepStatus = currentAnchor.statusToString(); //preAnchorOLSEStatusSA[4];
			//String prePAnchorSE = prePAnchorS + "_" + prePAnchorE;
			//String prePBepStatus = preBepOLSEStatusSA[4];
			
			// set up BEP Icon
			StyledDocument styDoc = (StyledDocument) txtPane.getDocument();
			
			Style bepHStyle = styDoc.addStyle("bepHIcon", null);
			StyleConstants.setIcon(bepHStyle, new ImageIcon(bepHighlightIconImageFilePath));
			Style bepCStyle = styDoc.addStyle("bepCIcon", null);
			StyleConstants.setIcon(bepCStyle, new ImageIcon(bepCompletedIconImageFilePath));
			Style bepNStyle = styDoc.addStyle("bepNIcon", null);
			StyleConstants.setIcon(bepNStyle, new ImageIcon(bepNonrelevantIconImageFilePath));
			Style bepStyle = styDoc.addStyle("bepIcon", null);
			StyleConstants.setIcon(bepStyle, new ImageIcon(bepIconImageFilePath));
			
			if (isTopicBEP) {
			    // PRE-BEP Icon back to its ORIGINAL
			    if (prePBepStatus.equals("0")) {
			        styDoc.remove(Integer.valueOf(prePBepS), bepLength);
			        styDoc.insertString(Integer.valueOf(prePBepS), "TBEP", bepStyle);
			    } else if (prePBepStatus.equals("1")) {
			        styDoc.remove(Integer.valueOf(prePBepS), bepLength);
			        styDoc.insertString(Integer.valueOf(prePBepS), "CBEP", bepCStyle);
			    } else if (prePBepStatus.equals("-1")) {
			        styDoc.remove(Integer.valueOf(prePBepS), bepLength);
			        styDoc.insertString(Integer.valueOf(prePBepS), "NBEP", bepNStyle);
			    }
			
			 // Curr Highlight BEP Icon
//			 for (String scrOffset : link) {
//			     styDoc.remove(Integer.valueOf(scrOffset), bepLength);
//			         styDoc.insertString(Integer.valueOf(scrOffset), "HBEP", bepHStyle);
//			 }
			
			} 
			else {
//			    for (String scrOffset : link) {
//			        styDoc.insertString(Integer.valueOf(scrOffset), "TBEP", bepStyle);
//			    }
			
			
			}

	    } catch (BadLocationException ex) {
	        Logger.getLogger(TopicHighlightManager.class.getName()).log(Level.SEVERE, null, ex);
	    }
	    txtPane.repaint();
	}
}
