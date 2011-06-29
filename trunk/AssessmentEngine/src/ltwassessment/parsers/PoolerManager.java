package ltwassessment.parsers;

import com.ximpleware.AutoPilot;
import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.TranscodeException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import ltwassessment.AppResource;
import ltwassessment.assessment.AssessedAnchor;
import ltwassessment.assessment.Bep;
import ltwassessment.assessment.IndexedAnchor;
import ltwassessment.utility.PoolUpdater;

import org.jdesktop.application.ResourceMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Darren Huang @QUT
 */
public class PoolerManager {

	public final static String OUTGOING_KEY = "outgoing : ";
	
    private final String sysPropertyKey = "isTABKey";
    private final String sysPropertyIsTopicWikiKey = "isTopicWikipedia";
    private final String sysPropertyIsLinkWikiKey = "isLinkWikipedia";
    private static String resourceXMLFile = "";
    private static String fileNotFoundXmlPath = "";
    private static String afTasnCollectionErrors = "";
    //1) including participant-id, run-id, task, collection
    private static String[] afProperty = new String[4];
    //2) record Topic -> [0]:File & [1]:Name
    private Vector<String[]> RunTopics = new Vector<String[]>();
    //3) record Topic (outgoing : topicFile) -> [0]:Offset & [1]:Length & [2]:Anchor_Name
    private Hashtable<String, Vector<IndexedAnchor>> topicAnchorsHT = new Hashtable<String, Vector<IndexedAnchor>>();
    //4) record Topic (incoming : topicFile) -> [0]:Offset
    private Hashtable<String, Vector<String[]>> topicBepsHT = new Hashtable<String, Vector<String[]>>();
    //5) Outgoing Pooling Data
    private Hashtable<String, Hashtable<String, Hashtable<String, Vector<Bep>>>> poolOutgoingData = new Hashtable<String, Hashtable<String, Hashtable<String, Vector<Bep>>>>();
    //6) Incoming Pooling Data
    private Hashtable<String, Hashtable<String, Vector<String[]>>> poolIncomingData = new Hashtable<String, Hashtable<String, Vector<String[]>>>();
    //7) record Topic (outgoing : topicFile) -> [0]:Offset & [1]:Length & [2]:Subanchor_Name
    private Hashtable<String, Vector<AssessedAnchor>> topicSubanchorsHT = new Hashtable<String, Vector<AssessedAnchor>>();
    private static String poolXMLPath = "";
//    static ResourcesManager resManager;
    static String afXmlPath = "";
    static PoolerManager instance = null;
    private static PoolUpdater poolUpdater = null; 
    
    static {
//        resManager = ResourcesManager.getInstance();

        //resourceMap = org.jdesktop.application.Application.getInstance(ltwassessment.ltwassessmentApp.class).getContext().getResourceMap(ltwassessmentView.class);
    	ResourceMap resourceMap = AppResource.getInstance().getResourceMap();
        resourceXMLFile = resourceMap.getString("ResourceXMLFilePath");
        fileNotFoundXmlPath = resourceMap.getString("fileNotFound.ReplacedXmlPath");
        afTasnCollectionErrors = resourceMap.getString("AssFormXml.taskCollectionError");
        afXmlPath = ""; //resourceMap.getString("pool.wikipedia");
        afProperty = new String[]{"", "", "", ""};
    }

    public static PoolerManager getInstance() {
//        if (instance == null)
//            instance = new PoolerManager();
        return instance;
    }

    public static PoolerManager getInstance(String xmlFile) {
        if (instance == null || afXmlPath == null || afXmlPath.length() == 0 || !xmlFile.equals(afXmlPath)) {
            instance = new PoolerManager(xmlFile);
            poolUpdater = new PoolUpdater(xmlFile);
        }
        return instance;
    }

    static void log(Object content) {
        System.out.println(content);
    }

    static void errlog(Object content) {
        System.err.println("errlog: " + content);
    }

    public PoolerManager(String xmlFile) {
        afXmlPath = xmlFile;
        getPoolData();
    }
    
    public PoolerManager() {
        afProperty = new String[]{"", "", "", ""};
        getPoolData();
    }

    public static PoolUpdater getPoolUpdater() {
		return poolUpdater;
	}

	public static void setPoolUpdater(PoolUpdater poolUpdater) {
		PoolerManager.poolUpdater = poolUpdater;
	}

	// <editor-fold defaultstate="collapsed" desc="GET Pool Properties"> 
    // instant status
    public int getPABepLinkStartP(String topicID, IndexedAnchor pAnchorOLSA, String currALinkID) {
        int pABepLinkStartP = -1;
        VTDGen vg = new VTDGen();

        if (vg.parseFile(poolXMLPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(poolXMLPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);

                // Pool Anchor
                String xPath = "/crosslink-assessment/topic[@file='" + topicID + "']/outgoinglinks/anchor[@aoffset='" + pAnchorOLSA.offsetToString() + "' and @alength='" + pAnchorOLSA.lengthToString() + "']/subanchor/tobep";
                ap.selectXPath(xPath);
                int i = -1;
                while ((i = ap.evalXPath()) != -1) {
                    int l = vn.getAttrVal("tbstartp");
                    int m = vn.getText();
                    if (m != -1) {
                        String rawTxt = vn.toRawString(m).toString();
                        if (rawTxt.equals(currALinkID)) {
                            if (l != -1) {
                                pABepLinkStartP = Integer.valueOf(vn.toRawString(l));
                            }
                        }
                    }
                }
                xm.output(fos);
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return pABepLinkStartP;
    }

    public String getPoolAnchorNameByOL(String topicID, IndexedAnchor currSCRSEName) {
        String pAnchorName = "";
        VTDGen vg = new VTDGen();

        if (vg.parseFile(poolXMLPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(poolXMLPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);

                // Pool Anchor
                String xPath1 = "/crosslink-assessment/topic[@file='" + topicID + "']/outgoinglinks/anchor[@aoffset='" + currSCRSEName.offsetToString() + "' and @alength='" + currSCRSEName.lengthToString() + "']";
                ap.selectXPath(xPath1);
                int k = -1;
                while ((k = ap.evalXPath()) != -1) {
                    int l = vn.getAttrVal("aname");
                    if (l != -1) {
                        pAnchorName = vn.toRawString(l);
                    }
                }
                xm.output(fos);
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return pAnchorName;
    }

    public String getPoolAnchorStatus(String topicID, IndexedAnchor poolAnchorOL) {
        String pAnchorStatus = "0";
        VTDGen vg = new VTDGen();

        if (vg.parseFile(poolXMLPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(poolXMLPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);

                // Pool Anchor
                String xPath1 = "/crosslink-assessment/topic[@file='" + topicID + "']/outgoinglinks/anchor[@aoffset='" + poolAnchorOL.offsetToString()/*[0]*/ + "' and @alength='" + poolAnchorOL.lengthToString()/*[1]*/ + "']";
                ap.selectXPath(xPath1);
                int k = -1;
                while ((k = ap.evalXPath()) != -1) {
                    int l = vn.getAttrVal("arel");
                    if (l != -1) {
                        pAnchorStatus = vn.toRawString(l);
                    }
                }
                xm.output(fos);
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return pAnchorStatus;
    }

    public Vector<String> getPoolAnchorAllLinkStatus(String topicID, IndexedAnchor poolAnchorOL) {
        Vector<String> pAnchorAllLinkStatus = new Vector<String>();
        VTDGen vg = new VTDGen();

        if (vg.parseFile(poolXMLPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(poolXMLPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);

                // Pool Anchor
                String xPath1 = "/crosslink-assessment/topic[@file='" + topicID + "']/outgoinglinks/anchor[@aoffset='" + poolAnchorOL.offsetToString()/*[0]*/ + "' and @alength='" + poolAnchorOL.lengthToString()/*[1]*/ + "']/subanchor/tobep";
                ap.selectXPath(xPath1);
                int i = -1;
                while ((i = ap.evalXPath()) != -1) {
                    int j = vn.getAttrVal("tbrel");
                    if (j != -1) {
                        pAnchorAllLinkStatus.add(vn.toRawString(j));
                    }
                }
                xm.output(fos);
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return pAnchorAllLinkStatus;
    }
    
    public Vector<String> getPoolSubanchorAllLinkStatus(String topicID, AssessedAnchor poolAnchorOL) {
        Vector<String> pAnchorAllLinkStatus = new Vector<String>();
        VTDGen vg = new VTDGen();

        if (vg.parseFile(poolXMLPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(poolXMLPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);

                // Pool Anchor
                String xPath1 = "/crosslink-assessment/topic[@file='" + topicID + "']/outgoinglinks/anchor[@aoffset='" + poolAnchorOL.getParent().offsetToString()/*[0]*/ + "' and @alength='" + poolAnchorOL.getParent().lengthToString()/*[1]*/ + 
                		"']/subanchor[@saoffset='" + poolAnchorOL.offsetToString() + "' and @salength='" + poolAnchorOL.lengthToString() + "']/tobep";
                ap.selectXPath(xPath1);
                int i = -1;
                while ((i = ap.evalXPath()) != -1) {
                    int j = vn.getAttrVal("tbrel");
                    if (j != -1) {
                        pAnchorAllLinkStatus.add(vn.toRawString(j));
                    }
                }
                xm.output(fos);
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return pAnchorAllLinkStatus;
    }

    public String getPoolAnchorBepLinkStartP(String topicID, IndexedAnchor currSCRSEName, String targetID) {
        String pAnchorStartP = "0";
        VTDGen vg = new VTDGen();

        if (vg.parseFile(poolXMLPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(poolXMLPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);

                // Pool Anchor
                String xPath1 = "/crosslink-assessment/topic[@file='" + topicID + "']/outgoinglinks/anchor[@aoffset='" + currSCRSEName.offsetToString()/*[0]*/ + "' and @alength='" + currSCRSEName.lengthToString()/*[1]*/ + "']/subanchor/tobep";
                ap.selectXPath(xPath1);
                int i = -1;
                while ((i = ap.evalXPath()) != -1) {
                    int j = vn.getAttrVal("tbstartp");
                    int k = vn.getText();
                    if (k != -1) {
                        String rawTxt = vn.toRawString(k).toString();
                        if (rawTxt.equals(targetID)) {
                            if (j != -1) {
                                pAnchorStartP = vn.toRawString(j);
                            }
                        }
                    }
                }
                xm.output(fos);
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return pAnchorStartP;
    }

    public String getPoolAnchorBepLinkStatus(String topicID, IndexedAnchor poolAnchorOL, String targetID) {
        String pAnchorStatus = "0";
        VTDGen vg = new VTDGen();

        if (vg.parseFile(poolXMLPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(poolXMLPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);

                // Pool Anchor
                String xPath1 = "/crosslink-assessment/topic[@file='" + topicID + "']/outgoinglinks/anchor[@aoffset='" + poolAnchorOL.offsetToString()/*[0]*/ + "' and @alength='" + poolAnchorOL.lengthToString()/*[1]*/ + "']/subanchor/tobep";
                ap.selectXPath(xPath1);
                int i = -1;
                while ((i = ap.evalXPath()) != -1) {
                    int j = vn.getAttrVal("tbrel");
                    int k = vn.getText();
                    if (k != -1) {
                        String rawTxt = vn.toRawString(k).toString();
                        if (rawTxt.endsWith("\"")) {
                            rawTxt = rawTxt.substring(0, rawTxt.length() - 1);
                        }
                        if (rawTxt.equals(targetID)) {
                            if (j != -1) {
                                pAnchorStatus = vn.toRawString(j);
                            }
                        }
                    }
                }
                xm.output(fos);
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return pAnchorStatus;
    }

    public Vector<String> getPoolBepAllLinksStatusV(String topicID, String poolBepOffset) {
        Vector<String> pBepAllLinkStatus = new Vector<String>();
        VTDGen vg = new VTDGen();

        if (vg.parseFile(poolXMLPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(poolXMLPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);
                // Pool BEP
                String xPath = "/crosslink-assessment/topic[@file='" + topicID.trim() + "']/incominglinks/bep[@boffset='" + poolBepOffset.trim() + "']/fromanchor";
                ap.selectXPath(xPath);
                int i = -1;
                while ((i = ap.evalXPath()) != -1) {
                    int j = vn.getAttrVal("farel");
                    if (j != -1) {
                        pBepAllLinkStatus.add(vn.toRawString(j));
                    }
                }
                xm.output(fos);
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return pBepAllLinkStatus;
    }

    public String getPoolBepAnchorLinkStatus(String topicID, String poolBepOffset, String[] pBepLinkOLID) {
        String pBepAnchorLinkStatus = "";
        VTDGen vg = new VTDGen();

        if (vg.parseFile(poolXMLPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(poolXMLPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);
                // Pool BEP
                String xPath = "/crosslink-assessment/topic[@file='" + topicID.trim() + "']/incominglinks/bep[@boffset='" + poolBepOffset.trim() + "']/fromanchor[@faoffset='" + pBepLinkOLID[0].trim() + "' and @falength='" + pBepLinkOLID[1].trim() + "']";
                ap.selectXPath(xPath);
                int i = -1;
                while ((i = ap.evalXPath()) != -1) {
                    int j = vn.getAttrVal("farel");
                    int k = vn.getText();
                    if (k != -1) {
                        String rawTxt = vn.toRawString(k).toString();
                        if (rawTxt.equals(pBepLinkOLID[2].trim())) {
                            if (j != -1) {
                                pBepAnchorLinkStatus = vn.toRawString(j);
                            }
                        }
                    }
                }
                xm.output(fos);
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return pBepAnchorLinkStatus;
    }

    public String getPoolBepLinkAnchorName(String topicID, String poolBepOffset, String[] pBepLinkOLID) {
        String pBepLinkAnchorName = "";
        VTDGen vg = new VTDGen();

        if (vg.parseFile(poolXMLPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(poolXMLPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);
                // Pool BEP
                String xPath = "/crosslink-assessment/topic[@file='" + topicID.trim() + "']/incominglinks/bep[@boffset='" + poolBepOffset.trim() + "']/fromanchor[@faoffset='" + pBepLinkOLID[0].trim() + "' and @falength='" + pBepLinkOLID[1].trim() + "']";
                ap.selectXPath(xPath);
                int i = -1;
                while ((i = ap.evalXPath()) != -1) {
                    int j = vn.getAttrVal("faanchor");
                    int k = vn.getText();
                    if (k != -1) {
                        String rawTxt = vn.toRawString(k).toString();
                        if (rawTxt.equals(pBepLinkOLID[2].trim())) {
                            if (j != -1) {
                                pBepLinkAnchorName = vn.toRawString(j);
                            }
                        }
                    }
                }
                xm.output(fos);
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return pBepLinkAnchorName;
    }

    public String getPoolBepStatus(String topicID, String poolBepOffset) {
        String pBepStatus = "";
        VTDGen vg = new VTDGen();

        if (vg.parseFile(poolXMLPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(poolXMLPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);
                // Pool BEP
                String xPath = "/crosslink-assessment/topic[@file='" + topicID.trim() + "']/incominglinks/bep[@boffset='" + poolBepOffset.trim() + "']";
                ap.selectXPath(xPath);
                int k = -1;
                while ((k = ap.evalXPath()) != -1) {
                    int l = vn.getAttrVal("borel");
                    if (l != -1) {
                        pBepStatus = vn.toRawString(l);
                    }
                }
                xm.output(fos);
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return pBepStatus;
    }
    // =========================================================================

    public Hashtable<String, Hashtable<String, Hashtable<String, Vector<Bep>>>> getOutgoingPool() {
        return poolOutgoingData;
    }

    public Hashtable<String, Hashtable<String, Vector<String[]>>> getIncomingPool() {
        return poolIncomingData;
    }

    public String[] getPoolProperty() {
        // including participant-id, run-id, task, collection
        return afProperty;
    }

    public Vector<String[]> getAllTopicsInPool() {
        // record Topic -> [0]:File & [1]:Name
        return RunTopics;
    }

    public Hashtable<String, Vector<IndexedAnchor>> getTopicAllAnchors() {
        // record Topic (outgoing : topicFile) -> [0]:Offset & [1]:Length & [2]:Anchor_Name & [3]:Status
        return topicAnchorsHT;
    }

    public Hashtable<String, Vector<AssessedAnchor>> getTopicAllSubanchors() {
        // record Topic (outgoing : topicFile) -> [0]:Offset & [1]:Length & [2]:Anchor_Name & [3]:Status
        return topicSubanchorsHT ;
    }
    
    public Hashtable<String, Vector<String[]>> getTopicAllBeps() {
        // record Topic (incoming : topicFile) -> [0]:Offset
        return topicBepsHT;
    }

    public String getXmlFilePathByTargetID(String xmlFileID, String lang) {
        String xmlFilePath = "";
        String myAFTask = afProperty[2].trim();
        String[] AFColls = afProperty[3].trim().split(" : ");
        String myAFTopicColl = "";
        String myAFLinkColl = "";
        if (AFColls.length == 2) {
            myAFTopicColl = AFColls[0].trim();
            myAFLinkColl = AFColls[1].trim();
        } else {
            myAFTopicColl = afProperty[3].trim();
            myAFLinkColl = afProperty[3].trim();
        }
//        ResourcesManager rscManager = ResourcesManager.getInstance();
//        if (Boolean.valueOf(System.getProperty(sysPropertyIsTopicWikiKey))) {
            String subPath = ResourcesManager.getInstance().getWikipediaFilePathByName(xmlFileID + ".xml", lang);
            if (subPath.equals("FileNotFound.xml")) {
                xmlFilePath = fileNotFoundXmlPath;
            } else {
                xmlFilePath = /*ResourcesManager.getInstance().getWikipediaFileFolder(AppResource.targetLang) + */subPath;
            }
//        } else {
//            String subPath = rscManager.getTeAraFilePathByName(xmlFileID + ".xml");
//            if (subPath.equals("FileNotFound.xml")) {
//                xmlFilePath = fileNotFoundXmlPath;
//            } else {
//                xmlFilePath = subPath;
//            }
//        }
        return xmlFilePath;
    }
    
    private HashMap<String, Vector<String[]>> getBepAnchorSetbyTopicID(String topicFileID, String afXmlPath) {
        // Format:
        // Bep(Offset:1114), Vector<String[]{Offset:1538, Length:9, Name:TITLE, ID:123017}+>
        String afTitleTag = "crosslink-assessment";
        String afTopicTag = "topic";
        String afIncomingTag = "incominglinks";
        String afBepTag = "bep";
        String afFromAnchorTag = "fromanchor";

        HashMap<String, Vector<String[]>> bepAnchorsHT = new HashMap<String, Vector<String[]>>();
        Document xmlDoc = readingXMLFromFile(afXmlPath);

        NodeList titleNodeList = xmlDoc.getElementsByTagName(afTitleTag);
        for (int i = 0; i < titleNodeList.getLength(); i++) {
            Element titleElmn = (Element) titleNodeList.item(i);
            NodeList topicNodeList = titleElmn.getElementsByTagName(afTopicTag);
            for (int j = 0; j < topicNodeList.getLength(); j++) {
                Element topicElmn = (Element) topicNodeList.item(j);
                String thisTopicID = topicElmn.getAttribute("file");
                if (thisTopicID.equals(topicFileID)) {
                    NodeList linksNodeList = topicElmn.getElementsByTagName(afIncomingTag);
                    Element incomingElmn = (Element) linksNodeList.item(0);
                    NodeList bepNodeList = incomingElmn.getElementsByTagName(afBepTag);
                    String bepOffsetKey = "";
                    Vector<String[]> bepToAnchorFileV;
                    for (int k = 0; k < bepNodeList.getLength(); k++) {
                        Element anchorElmn = (Element) bepNodeList.item(k);
                        String bOffset = anchorElmn.getAttribute("boffset");
                        bepOffsetKey = bOffset;
                        bepToAnchorFileV = new Vector<String[]>();
                        NodeList fromAnchorNodeList = anchorElmn.getElementsByTagName(afFromAnchorTag);
                        for (int l = 0; l < fromAnchorNodeList.getLength(); l++) {
                            Element fromAnchorElmn = (Element) fromAnchorNodeList.item(l);
                            String taOffset = fromAnchorElmn.getAttribute("faoffset");
                            String taLength = fromAnchorElmn.getAttribute("falength");
                            String taAName = fromAnchorElmn.getAttribute("faanchor");
                            Node taXmlFileIDTextNode = fromAnchorElmn.getFirstChild();
                            String taFileID = taXmlFileIDTextNode.getTextContent();
                            bepToAnchorFileV.add(new String[]{taOffset, taLength, taAName, taFileID});
                        }
                        bepAnchorsHT.put(bepOffsetKey, bepToAnchorFileV);
                    }
                }
            }
        }
        return bepAnchorsHT;
    }
    
    public HashMap<String, Vector<String[]>> getAnchorFileSetByBep(String topicFileID) {
        // assign a Topic File ID (i.e. 112398)
        // to get Bep(Offset:1114), Vector<String[]{Offset:1538, Length:9, Name:TITLE, ID:123017, Status}+>
        HashMap<String, Vector<String[]>> bepAnchorsHT = null; //new HashMap<String, Vector<String[]>>();
        bepAnchorsHT = getBepAnchorSetbyTopicID(topicFileID, poolXMLPath);
        return bepAnchorsHT;
    }

    public HashMap<String, Vector<IndexedAnchor>> getBepSetByAnchor(String topicFileID) {
        // assign a Topic File ID (i.e. 112398)
        // to get Anchor(1114_1133), Vector<String[]{123017, 1538}+>
        HashMap<String, Vector<IndexedAnchor>> anchorBepsHT = null; //new HashMap<String, Vector<String[]>>();
        anchorBepsHT = getAnchorBepSetbyTopicID(topicFileID, poolXMLPath);
        return anchorBepsHT;
    }

    private HashMap<String, Vector<IndexedAnchor>> getAnchorBepSetbyTopicID(String topicFileID, String afXmlPath) {
        // Format:
        // Anchor(1114_1133), Vector<String[]{123017, 1538}+>
        boolean forValidationOrAssessment = AppResource.forValidationOrAssessment;
        String afTitleTag = forValidationOrAssessment ? "crosslink-assessment" : "crosslink-submission";
        String afTopicTag = "topic";
        String afOutgoingTag = forValidationOrAssessment ? "outgoinglinks" : "outgoing";
        String afAnchorTag = "anchor";
        String afSubAnchorTag = "subanchor";
        String afToBepTag = forValidationOrAssessment ? "tobep" : "tofile";
        String offsetAttributeName = forValidationOrAssessment ? "aoffset" : "offset";
        String lengthAttributeName = forValidationOrAssessment ? "alength" : "length";
        String tboffsetAttributeName = forValidationOrAssessment ? "tboffset" : "bep_offset";
        
        byte[] bytes = FOLTXTMatcher.getInstance().getFullXmlTxt().getBytes();
        
		int aExtLength;
		int aOffset;
		int aLength;
        
        HashMap<String, Vector<IndexedAnchor>> anchorBepsHT = new HashMap<String, Vector<IndexedAnchor>>();
        Document xmlDoc = readingXMLFromFile(afXmlPath);

        NodeList titleNodeList = xmlDoc.getElementsByTagName(afTitleTag);
        for (int i = 0; i < titleNodeList.getLength(); i++) {
            Element titleElmn = (Element) titleNodeList.item(i);
            NodeList topicNodeList = titleElmn.getElementsByTagName(afTopicTag);
            for (int j = 0; j < topicNodeList.getLength(); j++) {
                Element topicElmn = (Element) topicNodeList.item(j);
                String thisTopicID = topicElmn.getAttribute("file");
                if (thisTopicID.equals(topicFileID)) {
                    NodeList linksNodeList = topicElmn.getElementsByTagName(afOutgoingTag);
                    Element outgoingElmn = (Element) linksNodeList.item(0);
                    NodeList anchorNodeList = outgoingElmn.getElementsByTagName(afAnchorTag);
                    String anchorKey = "";
                    Vector<IndexedAnchor> anchorToBEPV = new Vector<IndexedAnchor>();;
                    for (int k = 0; k < anchorNodeList.getLength(); k++) {
                        Element anchorElmn = (Element) anchorNodeList.item(k);
                        String anchorName = anchorElmn.getAttribute("name");

                		aExtLength = Integer.valueOf(anchorElmn.getAttribute("ext_length"));
                		aOffset = Integer.valueOf(anchorElmn.getAttribute(offsetAttributeName));
                		aLength = Integer.valueOf(anchorElmn.getAttribute(lengthAttributeName));
                        	
                        String aOffsetStr = String.valueOf(aOffset);
                        String aLengthStr =  String.valueOf(aLength);
                        String aExtLengthStr = String.valueOf(aExtLength);
//                      String aOffsetStr = String.valueOf(FOLTXTMatcher.byteOffsetToTextOffset(bytes, aOffset));
//                      String aLengthStr =  String.valueOf(FOLTXTMatcher.textLength(bytes, aOffset, aLength));
//						String aExtLengthStr = String.valueOf(FOLTXTMatcher.textLength(bytes, aOffset + aLength, aExtLength));
                		
                        AssessedAnchor anchor = null;
                        if (forValidationOrAssessment) {
                        	anchorKey = aOffsetStr + "_" + aLengthStr;
                            NodeList subAnchorNodeList = anchorElmn.getElementsByTagName(afSubAnchorTag);
                            for (int l = 0; l < subAnchorNodeList.getLength(); l++) {
                                Element subAnchorElmn = (Element) subAnchorNodeList.item(l);
                                           		                     		                        
                                String saOffset = subAnchorElmn.getAttribute("saoffset");
                                String saLength = subAnchorElmn.getAttribute("salength");
                                
//                                aOffset = FOLTXTMatcher.byteOffsetToTextOffset(bytes, aOffset);
//                                aLength = FOLTXTMatcher.textLength(bytes, aOffset, aLength);
//                                
//                                saOffset = String.valueOf(aOffset);
//                                saLength = String.valueOf(aLength);
                                String sanchorName = subAnchorElmn.getAttribute("saname");
                                String sarel = subAnchorElmn.getAttribute("sarel");
                                anchor = new AssessedAnchor(Integer.parseInt(saOffset), Integer.parseInt(saLength), sanchorName);
                                NodeList toBepNodeList = subAnchorElmn.getElementsByTagName(afToBepTag);
                                for (int m = 0; m < toBepNodeList.getLength(); m++) {
                                    Element toBepElmn = (Element) toBepNodeList.item(m);
                                    String tbOffset = toBepElmn.getAttribute(tboffsetAttributeName);                                
                                    String tbStartP = toBepElmn.getAttribute("tbstartp");
                                    String tbRel = toBepElmn.getAttribute("tbrel");

                                    Node tbXmlFileIDTextNode = toBepElmn.getFirstChild();
                                    String tbFileID = tbXmlFileIDTextNode.getTextContent();
                                                                       
                                    //anchorToBEPV.add(new String[]{tbOffset, tbStartP, tbFileID, tbRel, sanchorName, saOffset, saLength, sarel});
                                    anchor.addBep(new Bep(Integer.parseInt(tbOffset), Integer.parseInt(tbStartP), tbFileID, Integer.parseInt(tbRel)));
                                }
                            }
                        }
                        else {
                        	anchorKey = aOffsetStr + "_" + (Integer.valueOf(aOffset) + Integer.valueOf(aLength)) + "_" + anchorName;
                            NodeList toBepNodeList = anchorElmn.getElementsByTagName(afToBepTag);
                            anchor = new AssessedAnchor(aOffset, aLength, anchorName);
                            anchor.setExtendedLength(aExtLength);
                            
                            for (int m = 0; m < toBepNodeList.getLength(); m++) {
                                Element toBepElmn = (Element) toBepNodeList.item(m);
                                
                                // new
                                String target_lang = anchorElmn.getAttribute( offsetAttributeName);
                                String target_title = anchorElmn.getAttribute(lengthAttributeName);

                                String tbOffset = toBepElmn.getAttribute("bep_offset");
                                Node tbXmlFileIDTextNode = toBepElmn.getFirstChild();
                                String tbFileID = tbXmlFileIDTextNode.getTextContent();
                                //anchorToBEPV.add(new String[]{tbFileID, tbOffset, target_lang, target_title});
                                Bep bep = new Bep(tbFileID, Integer.parseInt(tbOffset), target_lang, target_title);
                                anchor.addBep(bep);
                            }
                        }
                        anchorBepsHT.put(anchorKey, anchorToBEPV);
                    }
                }
            }
        }
        return anchorBepsHT;
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Pooling: poolSubmissionRuns()">
    public Document readingXMLFromFile(String sourceXml) {
        DocumentBuilderFactory dBF = DocumentBuilderFactory.newInstance();
        dBF.setIgnoringComments(true);
        // Ignore the comments present in the XML File when reading the xml
        DocumentBuilder builder = null;
        InputSource input = new InputSource(sourceXml);
        Document doc = null;
        try {
            builder = dBF.newDocumentBuilder();
            doc = builder.parse(input);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;

    }

    private void getPoolData() {
        try {
            poolXMLPath = afXmlPath;//resManager.getPoolXMLFile();
            if (poolXMLPath == null || poolXMLPath.length() == 0)
            	return;
            
            if (!new File(poolXMLPath).exists()) {
            	System.err.println("\"" + poolXMLPath + "\" doesn't exist!");
                return;
            }
            
            poolOutgoingData.clear();
            
            	//throw new FileNotFoundException();
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            InputStream in = new FileInputStream(poolXMLPath);
            XMLStreamReader xsr = inputFactory.createXMLStreamReader(in);
            // Format: Topic --> outgoing/incoming --> anchor
            //               --> subanchor --> BEPs
            // FOL - Screen Position Pairs
            boolean isThisTopic = false;
            boolean isOutgoing = false;
            boolean isIncoming = false;
            String thisTopicFileID = "";
            // Outgoing
            Vector<IndexedAnchor> anchorsVbyTopic = new Vector<IndexedAnchor>();
            String thisAnchorSet = "";
            Hashtable<String, Hashtable<String, Vector<Bep>>> anchorsHT = new Hashtable<String, Hashtable<String, Vector<Bep>>>();
            Vector<AssessedAnchor> subAnchorsVbyTopic = new Vector<AssessedAnchor>();
            String thisSubAnchorSet = "";
            Hashtable<String, Vector<Bep>> subAnchorsToBepsHT = new Hashtable<String, Vector<Bep>>();
//            Vector<Bep> toBepsVbySubAnchor = new Vector<IndexedAnchor>();
            // Incoming
            Hashtable<String, Vector<String[]>> bepsHT = new Hashtable<String, Vector<String[]>>();
            String thisInBepOffset = "";
            Vector<String[]> fromAnchorsV = new Vector<String[]>();
            Vector<String[]> bepOffsetVbyTopic = new Vector<String[]>();
            String[] thisSubAnchorProperty = null;
            String[] thisAnchorProperty = null;
            int index = 0;

//            byte[] bytes = null;
            
    		int aExtLength = 0;
    		int aOffset = 0;
    		int aLength = 0;
    		
    		IndexedAnchor parsedAnchor = null;
    		AssessedAnchor parsedSubanchor = null;
    		
            while (xsr.hasNext()) {
                xsr.next();
                if (xsr.isStartElement()) {
                    String tagName = xsr.getLocalName();
                    if (tagName.equals("crosslink-assessment") || tagName.equals("crosslink-submission")) {
                        for (int i = 0; i < xsr.getAttributeCount(); i++) {
                            if (xsr.getAttributeLocalName(i).equals("participant-id")) {
                                afProperty[0] = xsr.getAttributeValue(i);
                            } else if (xsr.getAttributeLocalName(i).equals("run-id")) {
                                afProperty[1] = xsr.getAttributeValue(i);
                            } else if (xsr.getAttributeLocalName(i).equals("task")) {
                                afProperty[2] = xsr.getAttributeValue(i);
                            } else if (xsr.getAttributeLocalName(i).equals("default_lang")) {
                            	String lang = xsr.getAttributeValue(i);
                            	if (lang.equals("zh") || lang.equals("ja") || lang.equals("ko") || lang.equals("en"))
                            		AppResource.targetLang = lang;
                            	else
                            		AppResource.targetLang = "zh";
                            }
                            else if (xsr.getAttributeLocalName(i).equals("source_lang")) {
                            	String lang = xsr.getAttributeValue(i);
                            	if (lang.equals("zh") || lang.equals("ja") || lang.equals("ko") || lang.equals("en"))
                            		AppResource.sourceLang = lang;
                            	else
                            		AppResource.sourceLang = "en";
                            }
                        }
                    } else if (tagName.equals("collection")) {
                        xsr.next();
                        if (xsr.isCharacters()) {
                            if (afProperty[3].equals("") || afProperty[3] == null) {
                                afProperty[3] = xsr.getText();
                            } else {
                                afProperty[3] = afProperty[3] + " : " + xsr.getText();
                            }
                        }
                    } else if (tagName.equals("topic")) {
                        String[] thisTopic = new String[3];
                        thisTopic[2] = AppResource.sourceLang;
                        for (int i = 0; i < xsr.getAttributeCount(); i++) {
                            if (xsr.getAttributeLocalName(i).equals("file")) {
                                thisTopic[0] = xsr.getAttributeValue(i);
                                thisTopicFileID = thisTopic[0];
                            } else if (xsr.getAttributeLocalName(i).equals("name")) {
                                thisTopic[1] = xsr.getAttributeValue(i);
                            } else if (xsr.getAttributeLocalName(i).equals("lang")) {
                                thisTopic[2] = xsr.getAttributeValue(i);
                            }
                            if (thisTopic[2] == null)
                            	thisTopic[2] = "en";
                        }
                        RunTopics.add(thisTopic);
                        isThisTopic = true;
                        
//                        // setup the topic path stuff                        
//                        String currTopicFilePath = AppResource.getInstance().getTopicXmlPathNameByFileID(thisTopic[0], AppResource.sourceLang);
//                        ResourcesManager.getInstance().updateCurrTopicFilePath(currTopicFilePath);
//                        FOLTXTMatcher.getInstance().getCurrFullXmlText();
//                        bytes = FOLTXTMatcher.getInstance().getFullXmlTxt().getBytes();
                        
                    } else if (tagName.equals("outgoinglinks")  || tagName.equals("outgoing")) {
//                        isOutgoing = true;
                        anchorsVbyTopic = new Vector<IndexedAnchor>();
                        anchorsHT = new Hashtable<String, Hashtable<String, Vector<Bep>>>();
                        subAnchorsVbyTopic = new Vector<AssessedAnchor>();                  
                    } else if (tagName.equals("anchor")) {
                        thisAnchorProperty = new String[5];
                        for (int i = 0; i < xsr.getAttributeCount(); i++) {
                            String aName = xsr.getAttributeLocalName(i);
                            if (aName.equals("aname") || aName.equals("name")) {
                                thisAnchorProperty[2] = xsr.getAttributeValue(i);
                            } else if (aName.equals("aoffset") || aName.equals("offset")) {
                            	aOffset = Integer.valueOf(xsr.getAttributeValue(i));
                                
                            } else if (aName.equals("alength") || aName.equals("length")) {
                            	aLength = Integer.valueOf(xsr.getAttributeValue(i));
                                
                            }  else if (xsr.getAttributeLocalName(i).equals("arel")) {
                                thisAnchorProperty[3] = xsr.getAttributeValue(i);
                            } else if (xsr.getAttributeLocalName(i).equals("ext_length")) {
                            	aExtLength = Integer.valueOf(xsr.getAttributeValue(i));
                                
                            }
                            
                        }
                        thisAnchorProperty[0] = String.valueOf(aOffset);
                        thisAnchorProperty[1] = String.valueOf(aLength); 
                        thisAnchorProperty[4] = String.valueOf(aExtLength);
                        
//                        thisAnchorProperty[0] = String.valueOf(FOLTXTMatcher.byteOffsetToTextOffset(bytes, aOffset));
//                        thisAnchorProperty[1] = String.valueOf(FOLTXTMatcher.textLength(bytes, aOffset, aLength));
//                        thisAnchorProperty[4] = String.valueOf(FOLTXTMatcher.textLength(bytes, aOffset + aLength, aExtLength));
                        
//                        anchorsVbyTopic.add(thisAnchorProperty);
                        thisAnchorSet = thisAnchorProperty[0] + "_" + thisAnchorProperty[1];
                        subAnchorsToBepsHT = new Hashtable<String, Vector<Bep>>();
                        
                        
                        
                        
                        if (!AppResource.forValidationOrAssessment) { // validation
//                            thisSubAnchorProperty = new String[4];
//                            System.arraycopy(thisAnchorProperty, 0, thisSubAnchorProperty, 0, 4);
//                            subAnchorsVbyTopic.add(thisSubAnchorProperty);
                            thisSubAnchorSet = thisSubAnchorProperty[0] + "_" + thisSubAnchorProperty[1];
//                            toBepsVbySubAnchor = new Vector<IndexedAnchor>();                        	

                        	parsedSubanchor = new AssessedAnchor(aOffset, aLength, thisAnchorProperty[2]);
                        	parsedSubanchor.setStatus(Integer.valueOf(thisAnchorProperty[3]));
                        	parsedSubanchor.setExtendedLength(aExtLength);
                        	subAnchorsVbyTopic.add(parsedSubanchor);
                        }
                        else {
//                        	parsedAnchor = new IndexedAnchor(aOffset, aLength, thisAnchorProperty[2]);
                        	parsedAnchor = new IndexedAnchor(aOffset, aLength, thisAnchorProperty[2]);
                        	parsedAnchor.setStatus(Integer.valueOf(thisAnchorProperty[3]));
                        	parsedAnchor.setExtendedLength(aExtLength); 	
                        	anchorsVbyTopic.add(parsedAnchor);
                        }
                        	
                        index = 0;
                    } else if (tagName.equals("subanchor")) {
                    	assert(AppResource.forValidationOrAssessment);
                    	thisSubAnchorProperty = new String[4];
                    	String attrName = "";
                        for (int i = 0; i < xsr.getAttributeCount(); i++) {
                        	attrName = xsr.getAttributeLocalName(i);
                            if (attrName.equals("saoffset")) {
                            	aOffset = Integer.valueOf(xsr.getAttributeValue(i));
                            } else if (attrName.equals("salength")) {
                            	aLength = Integer.valueOf(xsr.getAttributeValue(i));
                            } else if (attrName.equals("saname")) {
                                thisSubAnchorProperty[2] = xsr.getAttributeValue(i);
                            } else if (attrName.equals("sarel")) {
                                thisSubAnchorProperty[3] = xsr.getAttributeValue(i);
                            }
                        }
//                        thisSubAnchorProperty[0] = String.valueOf(FOLTXTMatcher.byteOffsetToTextOffset(bytes, aOffset));
//                        thisSubAnchorProperty[1] = String.valueOf(FOLTXTMatcher.textLength(bytes, aOffset, aLength)); 
                        thisSubAnchorProperty[0] = String.valueOf(aOffset);
                        thisSubAnchorProperty[1] = String.valueOf(aLength);                          
                        
//                        subAnchorsVbyTopic.add(thisSubAnchorProperty);
                        thisSubAnchorSet = thisSubAnchorProperty[0] + "_" + thisSubAnchorProperty[1];
//                        toBepsVbySubAnchor = new Vector<IndexedAnchor>();
                        parsedSubanchor = new AssessedAnchor(aOffset, aLength, thisSubAnchorProperty[2], Integer.valueOf(thisSubAnchorProperty[3]));
                        parsedSubanchor.setOffsetIndex(Integer.valueOf(thisAnchorProperty[0]));
                        parsedSubanchor.setLengthIndex(Integer.valueOf(thisAnchorProperty[1]));
                        
                        subAnchorsVbyTopic.add(parsedSubanchor);
                        parsedAnchor.addChildAnchor(parsedSubanchor);
                        parsedSubanchor.setParent(parsedAnchor);
//                        toBepsVbySubAnchor.add
                    } else if (tagName.equals(SubmissionFormat.getAftobeptag())) { // tobep , now tofile
                        String[] thisToBepProperty = null;
                        Bep bep = new Bep();
//                        if (AppResource.forValidationOrAssessment) {
                            thisToBepProperty = new String[10];
                            thisToBepProperty[0] = "0";
                        	thisToBepProperty[5] = "";
                    		thisToBepProperty[6] = "";
                			thisToBepProperty[7] = "";
                			thisToBepProperty[8] = "";
//                        	if (index > 3)
//                        		System.err.println("We got more than 3 beps here");
//                            thisToBepProperty[9] = String.valueOf(index++);
                            for (int i = 0; i < xsr.getAttributeCount(); i++) {
                                if (xsr.getAttributeLocalName(i).equals("tboffset")) {
                                    thisToBepProperty[0] = xsr.getAttributeValue(i);
                                } else if (xsr.getAttributeLocalName(i).equals("tbrel")) {
                                    thisToBepProperty[2] = xsr.getAttributeValue(i);
                                } else if (xsr.getAttributeLocalName(i).equals("lang"))
	                            	thisToBepProperty[3] = xsr.getAttributeValue(i);
	                        	else if (xsr.getAttributeLocalName(i).equals("title"))
	                        		thisToBepProperty[4] = xsr.getAttributeValue(i);
                            }
//                        }
//                            if (thisSubAnchorSet.length() > 0) {
//                            	thisToBepProperty[5] = thisSubAnchorProperty[2]; //name
//                        		thisToBepProperty[6] = thisSubAnchorProperty[0]; // offset
//                    			thisToBepProperty[7] = thisSubAnchorProperty[1]; // length
//                    			thisToBepProperty[8] = thisSubAnchorProperty[3]; // status
//                            }

                        xsr.next();
                        if (xsr.isCharacters()) {
                            thisToBepProperty[1] = xsr.getText();
                        }
                        
                        bep.setTargetTitle(thisToBepProperty[4]);
                        bep.setTargetLang(thisToBepProperty[3]);
                        bep.setRel(thisToBepProperty[2]);
                        if (bep.getRel() == 1 && parsedAnchor.getStatus() == -1)
                        	parsedAnchor.setStatus(Bep.RELEVANT);
                        if (parsedSubanchor.getStatus() == -1) {
	                        if (bep.getRel() == 0)
	                        		bep.setRel(Bep.IRRELEVANT);
                        }
                        bep.setOffset(thisToBepProperty[0]);
                        bep.setFileId(thisToBepProperty[1]);
                        bep.setIndex(index++);
                        parsedSubanchor.addBep(bep);
                        bep.setAssociatedAnchor(parsedSubanchor);
                    } else if (tagName.equals("incominglinks")) {
                        isIncoming = true;
                        bepOffsetVbyTopic = new Vector<String[]>();
                        bepsHT = new Hashtable<String, Vector<String[]>>();
                    } else if (tagName.equals("bep")) {
                        String[] thisBepProperty = new String[2];
                        for (int i = 0; i < xsr.getAttributeCount(); i++) {
                            if (xsr.getAttributeLocalName(i).equals("boffset")) {
                                thisBepProperty[0] = xsr.getAttributeValue(i);
                                thisInBepOffset = xsr.getAttributeValue(i);
                            }
	                        else if (xsr.getAttributeLocalName(i).equals("borel")) {
	                            thisBepProperty[1] = xsr.getAttributeValue(i);
	                        }
                        }
                        bepOffsetVbyTopic.add(thisBepProperty);
                        fromAnchorsV = new Vector<String[]>();
                    } else if (tagName.equals("fromanchor")) {
                        String[] fromAnchorProperty = new String[4];
                        for (int i = 0; i < xsr.getAttributeCount(); i++) {
                            if (xsr.getAttributeLocalName(i).equals("faoffset")) {
                                fromAnchorProperty[0] = xsr.getAttributeValue(i);
                            } else if (xsr.getAttributeLocalName(i).equals("falength")) {
                                fromAnchorProperty[1] = xsr.getAttributeValue(i);
                            } else if (xsr.getAttributeLocalName(i).equals("faanchor")) {
                                fromAnchorProperty[2] = xsr.getAttributeValue(i);
                            }
                        }
                        xsr.next();
                        if (xsr.isCharacters()) {
                            fromAnchorProperty[3] = xsr.getText();
                        }
                        fromAnchorsV.add(fromAnchorProperty);
                    }
                } else if (xsr.isEndElement()) {
                    String tagName = xsr.getLocalName();
                    if (tagName.equals("topic")) {
                        if (isThisTopic) {
                        }
                        //topicAnchorsHT
                        isThisTopic = false;
                    } else if (tagName.equals("outgoinglinks") || tagName.equals("outgoing")) {
//                        if (isOutgoing) {
                            topicAnchorsHT.put(OUTGOING_KEY + thisTopicFileID, anchorsVbyTopic);
                            topicSubanchorsHT.put(OUTGOING_KEY + thisTopicFileID, subAnchorsVbyTopic);
                            poolOutgoingData.put(thisTopicFileID, anchorsHT);
//                        }
//                        isOutgoing = false;
                    } else if (tagName.equals("anchor")) {
                    	assert (anchorsHT.get(thisAnchorSet) == null);
                        anchorsHT.put(thisAnchorSet, subAnchorsToBepsHT);
                        if (!AppResource.forValidationOrAssessment) {
                        	subAnchorsToBepsHT.put(thisSubAnchorSet, parsedSubanchor.getBeps()/*toBepsVbySubAnchor*/);
                        	parsedAnchor = null;
                        }
                    } else if (tagName.equals("subanchor")) {
                        subAnchorsToBepsHT.put(thisSubAnchorSet, parsedSubanchor.getBeps()/*toBepsVbySubAnchor*/);
                    	thisSubAnchorSet = "";
                    	thisSubAnchorProperty = null;
                    	parsedSubanchor = null;
                    } else if (tagName.equals("incominglinks")) {
                        if (isIncoming) {
                            topicBepsHT.put("incoming : " + thisTopicFileID, bepOffsetVbyTopic);
                            poolIncomingData.put(thisTopicFileID, bepsHT);
                        }
                        isIncoming = false;
                    } else if (tagName.equals("bep")) {
                        bepsHT.put(thisInBepOffset, fromAnchorsV);
                    } /*else if (tagName.equals(SubmissionFormat.getAftobeptag())) {
                        ++index;
                    }*/
                }
            }
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XMLStreamException ex) {
            Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) { 
        	Logger.getLogger(PoolerManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

	public String getPoolTopicLang() {
		// TODO Auto-generated method stub
		return null;
	}
}
