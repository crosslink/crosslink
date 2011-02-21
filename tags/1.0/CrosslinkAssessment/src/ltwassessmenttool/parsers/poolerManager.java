package ltwassessmenttool.parsers;

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
import ltwassessmenttool.LTWAssessmentToolView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Darren Huang @QUT
 */
public class poolerManager {

    private final String sysPropertyKey = "isTABKey";
    private final String sysPropertyIsTopicWikiKey = "isTopicWikipedia";
    private final String sysPropertyIsLinkWikiKey = "isLinkWikipedia";
    private String resourceXMLFile = "";
    private String fileNotFoundXmlPath = "";
    private String afTasnCollectionErrors = "";
    //1) including participant-id, run-id, task, collection
    private String[] afProperty = new String[4];
    //2) record Topic -> [0]:File & [1]:Name
    private Vector<String[]> RunTopics = new Vector<String[]>();
    //3) record Topic (outgoing : topicFile) -> [0]:Offset & [1]:Length & [2]:Anchor_Name
    private Hashtable<String, Vector<String[]>> topicAnchorsHT = new Hashtable<String, Vector<String[]>>();
    //4) record Topic (incoming : topicFile) -> [0]:Offset
    private Hashtable<String, Vector<String[]>> topicBepsHT = new Hashtable<String, Vector<String[]>>();
    //5) Outgoing Pooling Data
    private Hashtable<String, Hashtable<String, Hashtable<String, Vector<String[]>>>> poolOutgoingData = new Hashtable<String, Hashtable<String, Hashtable<String, Vector<String[]>>>>();
    //6) Incoming Pooling Data
    private Hashtable<String, Hashtable<String, Vector<String[]>>> poolIncomingData = new Hashtable<String, Hashtable<String, Vector<String[]>>>();
    private String poolXMLPath = "";

    static void log(Object content) {
        System.out.println(content);
    }

    static void errlog(Object content) {
        System.err.println("errlog: " + content);
    }

    public poolerManager() {

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ltwassessmenttool.LTWAssessmentToolApp.class).getContext().getResourceMap(LTWAssessmentToolView.class);
        resourceXMLFile = resourceMap.getString("ResourceXMLFilePath");
        fileNotFoundXmlPath = resourceMap.getString("fileNotFound.ReplacedXmlPath");
        afTasnCollectionErrors = resourceMap.getString("AssFormXml.taskCollectionError");
        poolXMLPath = resourceMap.getString("pool.wikipedia");

        afProperty = new String[]{"", "", "", ""};

        getPoolData();
    }

    // <editor-fold defaultstate="collapsed" desc="GET Pool Properties">
    // instant status
    public int getPABepLinkStartP(String topicID, String[] pAnchorOLSA, String currALinkID) {
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
                String xPath = "/inexltw-assessment/topic[@file='" + topicID + "']/outgoinglinks/anchor[@aoffset='" + pAnchorOLSA[0] + "' and @alength='" + pAnchorOLSA[1] + "']/subanchor/tobep";
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
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return pABepLinkStartP;
    }

    public String getPoolAnchorNameByOL(String topicID, String[] poolAnchorOL) {
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
                String xPath1 = "/inexltw-assessment/topic[@file='" + topicID + "']/outgoinglinks/anchor[@aoffset='" + poolAnchorOL[0] + "' and @alength='" + poolAnchorOL[1] + "']";
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
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return pAnchorName;
    }

    public String getPoolAnchorStatus(String topicID, String[] poolAnchorOL) {
        String pAnchorStatus = "";
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
                String xPath1 = "/inexltw-assessment/topic[@file='" + topicID + "']/outgoinglinks/anchor[@aoffset='" + poolAnchorOL[0] + "' and @alength='" + poolAnchorOL[1] + "']";
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
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return pAnchorStatus;
    }

    public Vector<String> getPoolAnchorAllLinkStatus(String topicID, String[] poolAnchorOL) {
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
                String xPath1 = "/inexltw-assessment/topic[@file='" + topicID + "']/outgoinglinks/anchor[@aoffset='" + poolAnchorOL[0] + "' and @alength='" + poolAnchorOL[1] + "']/subanchor/tobep";
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
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return pAnchorAllLinkStatus;
    }

    public String getPoolAnchorBepLinkStartP(String topicID, String[] poolAnchorOL, String targetID) {
        String pAnchorStartP = "";
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
                String xPath1 = "/inexltw-assessment/topic[@file='" + topicID + "']/outgoinglinks/anchor[@aoffset='" + poolAnchorOL[0] + "' and @alength='" + poolAnchorOL[1] + "']/subanchor/tobep";
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
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return pAnchorStartP;
    }

    public String getPoolAnchorBepLinkStatus(String topicID, String[] poolAnchorOL, String targetID) {
        String pAnchorStatus = "";
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
                String xPath1 = "/inexltw-assessment/topic[@file='" + topicID + "']/outgoinglinks/anchor[@aoffset='" + poolAnchorOL[0] + "' and @alength='" + poolAnchorOL[1] + "']/subanchor/tobep";
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
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
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
                String xPath = "/inexltw-assessment/topic[@file='" + topicID.trim() + "']/incominglinks/bep[@boffset='" + poolBepOffset.trim() + "']/fromanchor";
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
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
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
                String xPath = "/inexltw-assessment/topic[@file='" + topicID.trim() + "']/incominglinks/bep[@boffset='" + poolBepOffset.trim() + "']/fromanchor[@faoffset='" + pBepLinkOLID[0].trim() + "' and @falength='" + pBepLinkOLID[1].trim() + "']";
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
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
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
                String xPath = "/inexltw-assessment/topic[@file='" + topicID.trim() + "']/incominglinks/bep[@boffset='" + poolBepOffset.trim() + "']/fromanchor[@faoffset='" + pBepLinkOLID[0].trim() + "' and @falength='" + pBepLinkOLID[1].trim() + "']";
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
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
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
                String xPath = "/inexltw-assessment/topic[@file='" + topicID.trim() + "']/incominglinks/bep[@boffset='" + poolBepOffset.trim() + "']";
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
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return pBepStatus;
    }
    // =========================================================================

    public Hashtable<String, Hashtable<String, Hashtable<String, Vector<String[]>>>> getOutgoingPool() {
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

    public Hashtable<String, Vector<String[]>> getTopicAllAnchors() {
        // record Topic (outgoing : topicFile) -> [0]:Offset & [1]:Length & [2]:Anchor_Name & [3]:Status
        return topicAnchorsHT;
    }

    public Hashtable<String, Vector<String[]>> getTopicAllBeps() {
        // record Topic (incoming : topicFile) -> [0]:Offset
        return topicBepsHT;
    }

    public String getXmlFilePathByTargetID(String xmlFileID) {
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
        resourcesManager rscManager = new resourcesManager();
        if (Boolean.valueOf(System.getProperty(sysPropertyIsTopicWikiKey))) {
            String subPath = rscManager.getWikipediaFilePathByName(xmlFileID + ".xml");
            if (subPath.equals("FileNotFound.xml")) {
                xmlFilePath = fileNotFoundXmlPath;
            } else {
                xmlFilePath = subPath;
            }
        } else {
            String subPath = rscManager.getTeAraFilePathByName(xmlFileID + ".xml");
            if (subPath.equals("FileNotFound.xml")) {
                xmlFilePath = fileNotFoundXmlPath;
            } else {
                xmlFilePath = subPath;
            }
        }
        return xmlFilePath;
    }

    public HashMap<String, Vector<String[]>> getAnchorFileSetByBep(String topicFileID) {
        // assign a Topic File ID (i.e. 112398)
        // to get Bep(Offset:1114), Vector<String[]{Offset:1538, Length:9, Name:TITLE, ID:123017, Status}+>
        HashMap<String, Vector<String[]>> bepAnchorsHT = new HashMap<String, Vector<String[]>>();
        bepAnchorsHT = getBepAnchorSetbyTopicID(topicFileID, poolXMLPath);
        return bepAnchorsHT;
    }

    private HashMap<String, Vector<String[]>> getBepAnchorSetbyTopicID(String topicFileID, String afXmlPath) {
        // Format:
        // Bep(Offset:1114), Vector<String[]{Offset:1538, Length:9, Name:TITLE, ID:123017, Status}+>
        String afTitleTag = "inexltw-assessment";
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
                            String taARel = fromAnchorElmn.getAttribute("farel");
                            Node taXmlFileIDTextNode = fromAnchorElmn.getFirstChild();
                            String taFileID = taXmlFileIDTextNode.getTextContent();
                            bepToAnchorFileV.add(new String[]{taOffset, taLength, taAName, taFileID, taARel});
                        }
                        bepAnchorsHT.put(bepOffsetKey, bepToAnchorFileV);
                    }
                }
            }
        }
        return bepAnchorsHT;
    }

    public HashMap<String, Vector<String[]>> getBepSetByAnchor(String topicFileID) {
        // assign a Topic File ID (i.e. 112398)
        // to get Anchor(1114_1133), Vector<String[]{123017, 1538}+>
        HashMap<String, Vector<String[]>> anchorBepsHT = new HashMap<String, Vector<String[]>>();
        anchorBepsHT = getAnchorBepSetbyTopicID(topicFileID, poolXMLPath);
        return anchorBepsHT;
    }

    private HashMap<String, Vector<String[]>> getAnchorBepSetbyTopicID(String topicFileID, String afXmlPath) {
        // Format:
        // Anchor(1114_1133), Vector<String[]{123017, 1538}+>
        String afTitleTag = "inexltw-assessment";
        String afTopicTag = "topic";
        String afOutgoingTag = "outgoinglinks";
        String afAnchorTag = "anchor";
        String afSubAnchorTag = "subanchor";
        String afToBepTag = "tobep";

        HashMap<String, Vector<String[]>> anchorBepsHT = new HashMap<String, Vector<String[]>>();
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
                    String anchorOLKey = "";
                    Vector<String[]> anchorToBEPOIDV;
                    for (int k = 0; k < anchorNodeList.getLength(); k++) {
                        Element anchorElmn = (Element) anchorNodeList.item(k);
                        String aOffset = anchorElmn.getAttribute("aoffset");
                        String aLength = anchorElmn.getAttribute("alength");
                        anchorOLKey = aOffset + "_" + aLength;
                        anchorToBEPOIDV = new Vector<String[]>();
                        NodeList subAnchorNodeList = anchorElmn.getElementsByTagName(afSubAnchorTag);
                        for (int l = 0; l < subAnchorNodeList.getLength(); l++) {
                            Element subAnchorElmn = (Element) subAnchorNodeList.item(l);
                            NodeList toBepNodeList = subAnchorElmn.getElementsByTagName(afToBepTag);
                            for (int m = 0; m < toBepNodeList.getLength(); m++) {
                                Element toBepElmn = (Element) toBepNodeList.item(m);
                                String tbOffset = toBepElmn.getAttribute("tboffset");
                                String tbStartP = toBepElmn.getAttribute("tbstartp");
                                String tbRel = toBepElmn.getAttribute("tbrel");
                                Node tbXmlFileIDTextNode = toBepElmn.getFirstChild();
                                String tbFileID = tbXmlFileIDTextNode.getTextContent();
                                anchorToBEPOIDV.add(new String[]{tbOffset, tbStartP, tbFileID, tbRel});
                            }
                        }
                        anchorBepsHT.put(anchorOLKey, anchorToBEPOIDV);
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
            Vector<String[]> anchorsVbyTopic = new Vector<String[]>();
            String thisAnchorSet = "";
            Hashtable<String, Hashtable<String, Vector<String[]>>> anchorsHT = new Hashtable<String, Hashtable<String, Vector<String[]>>>();
            Vector<String[]> subAnchorsVbyTopic = new Vector<String[]>();
            String thisSubAnchorSet = "";
            Hashtable<String, Vector<String[]>> subAnchorsToBepsHT = new Hashtable<String, Vector<String[]>>();
            Vector<String[]> toBepsVbySubAnchor = new Vector<String[]>();
            // Incoming
            Hashtable<String, Vector<String[]>> bepsHT = new Hashtable<String, Vector<String[]>>();
            String thisInBepOffset = "";
            Vector<String[]> fromAnchorsV = new Vector<String[]>();
            Vector<String[]> bepOffsetVbyTopic = new Vector<String[]>();
            while (xsr.hasNext()) {
                xsr.next();
                if (xsr.isStartElement()) {
                    if (xsr.getLocalName().equals("inexltw-assessment")) {
                        for (int i = 0; i < xsr.getAttributeCount(); i++) {
                            if (xsr.getAttributeLocalName(i).equals("participant-id")) {
                                afProperty[0] = xsr.getAttributeValue(i);
                            } else if (xsr.getAttributeLocalName(i).equals("run-id")) {
                                afProperty[1] = xsr.getAttributeValue(i);
                            } else if (xsr.getAttributeLocalName(i).equals("task")) {
                                afProperty[2] = xsr.getAttributeValue(i);
                            }
                        }
                    } else if (xsr.getLocalName().equals("collection")) {
                        xsr.next();
                        if (xsr.isCharacters()) {
                            if (afProperty[3].equals("") || afProperty[3] == null) {
                                afProperty[3] = xsr.getText();
                            } else {
                                afProperty[3] = afProperty[3] + " : " + xsr.getText();
                            }
                        }
                    } else if (xsr.getLocalName().equals("topic")) {
                        String[] thisTopic = new String[2];
                        for (int i = 0; i < xsr.getAttributeCount(); i++) {
                            if (xsr.getAttributeLocalName(i).equals("file")) {
                                thisTopic[0] = xsr.getAttributeValue(i);
                                thisTopicFileID = thisTopic[0];
                            } else if (xsr.getAttributeLocalName(i).equals("name")) {
                                thisTopic[1] = xsr.getAttributeValue(i);
                            }
                        }
                        RunTopics.add(thisTopic);
                        isThisTopic = true;
                    } else if (xsr.getLocalName().equals("outgoinglinks")) {
                        isOutgoing = true;
                        anchorsVbyTopic = new Vector<String[]>();
                        anchorsHT = new Hashtable<String, Hashtable<String, Vector<String[]>>>();
                        subAnchorsVbyTopic = new Vector<String[]>();
                    } else if (xsr.getLocalName().equals("anchor")) {
                        String[] thisAnchorProperty = new String[4];
                        for (int i = 0; i < xsr.getAttributeCount(); i++) {
                            if (xsr.getAttributeLocalName(i).equals("aoffset")) {
                                thisAnchorProperty[0] = xsr.getAttributeValue(i);
                            } else if (xsr.getAttributeLocalName(i).equals("alength")) {
                                thisAnchorProperty[1] = xsr.getAttributeValue(i);
                            } else if (xsr.getAttributeLocalName(i).equals("aname")) {
                                thisAnchorProperty[2] = xsr.getAttributeValue(i);
                            } else if (xsr.getAttributeLocalName(i).equals("arel")) {
                                thisAnchorProperty[3] = xsr.getAttributeValue(i);
                            }
                        }
                        anchorsVbyTopic.add(thisAnchorProperty);
                        thisAnchorSet = thisAnchorProperty[0] + "_" + thisAnchorProperty[1];
                        subAnchorsToBepsHT = new Hashtable<String, Vector<String[]>>();
                    } else if (xsr.getLocalName().equals("subanchor")) {
                        String[] thisSubAnchorProperty = new String[4];
                        for (int i = 0; i < xsr.getAttributeCount(); i++) {
                            if (xsr.getAttributeLocalName(i).equals("saoffset")) {
                                thisSubAnchorProperty[0] = xsr.getAttributeValue(i);
                            } else if (xsr.getAttributeLocalName(i).equals("salength")) {
                                thisSubAnchorProperty[1] = xsr.getAttributeValue(i);
                            } else if (xsr.getAttributeLocalName(i).equals("saname")) {
                                thisSubAnchorProperty[2] = xsr.getAttributeValue(i);
                            } else if (xsr.getAttributeLocalName(i).equals("sarel")) {
                                thisSubAnchorProperty[3] = xsr.getAttributeValue(i);
                            }
                        }
                        subAnchorsVbyTopic.add(thisSubAnchorProperty);
                        thisSubAnchorSet = thisSubAnchorProperty[0] + "_" + thisSubAnchorProperty[1];
                        toBepsVbySubAnchor = new Vector<String[]>();
                    } else if (xsr.getLocalName().equals("tobep")) {
                        String[] thisToBepProperty = new String[3];
                        for (int i = 0; i < xsr.getAttributeCount(); i++) {
                            if (xsr.getAttributeLocalName(i).equals("tboffset")) {
                                thisToBepProperty[0] = xsr.getAttributeValue(i);
                            } else if (xsr.getAttributeLocalName(i).equals("tbrel")) {
                                thisToBepProperty[2] = xsr.getAttributeValue(i);
                            }
                        }
                        xsr.next();
                        if (xsr.isCharacters()) {
                            thisToBepProperty[1] = xsr.getText();
                        }
                        toBepsVbySubAnchor.add(thisToBepProperty);
                    } else if (xsr.getLocalName().equals("incominglinks")) {
                        isIncoming = true;
                        bepOffsetVbyTopic = new Vector<String[]>();
                        bepsHT = new Hashtable<String, Vector<String[]>>();
                    } else if (xsr.getLocalName().equals("bep")) {
                        String[] thisBepProperty = new String[2];
                        for (int i = 0; i < xsr.getAttributeCount(); i++) {
                            if (xsr.getAttributeLocalName(i).equals("boffset")) {
                                thisBepProperty[0] = xsr.getAttributeValue(i);
                                thisInBepOffset = xsr.getAttributeValue(i);
                            } else if (xsr.getAttributeLocalName(i).equals("borel")) {
                                thisBepProperty[1] = xsr.getAttributeValue(i);
                            }
                        }
                        bepOffsetVbyTopic.add(thisBepProperty);
                        fromAnchorsV = new Vector<String[]>();
                    } else if (xsr.getLocalName().equals("fromanchor")) {
                        String[] fromAnchorProperty = new String[5];
                        for (int i = 0; i < xsr.getAttributeCount(); i++) {
                            if (xsr.getAttributeLocalName(i).equals("faoffset")) {
                                fromAnchorProperty[0] = xsr.getAttributeValue(i);
                            } else if (xsr.getAttributeLocalName(i).equals("falength")) {
                                fromAnchorProperty[1] = xsr.getAttributeValue(i);
                            } else if (xsr.getAttributeLocalName(i).equals("faanchor")) {
                                fromAnchorProperty[2] = xsr.getAttributeValue(i);
                            } else if (xsr.getAttributeLocalName(i).equals("farel")) {
                                fromAnchorProperty[4] = xsr.getAttributeValue(i);
                            }
                        }
                        xsr.next();
                        if (xsr.isCharacters()) {
                            fromAnchorProperty[3] = xsr.getText();
                        }
                        fromAnchorsV.add(fromAnchorProperty);
                    }
                } else if (xsr.isEndElement()) {
                    if (xsr.getLocalName().equals("topic")) {
                        if (isThisTopic) {
                        }
                        //topicAnchorsHT
                        isThisTopic = false;
                    } else if (xsr.getLocalName().equals("outgoinglinks")) {
                        if (isOutgoing) {
                            topicAnchorsHT.put("outgoing : " + thisTopicFileID, anchorsVbyTopic);
                            poolOutgoingData.put(thisTopicFileID, anchorsHT);
                        }
                        isOutgoing = false;
                    } else if (xsr.getLocalName().equals("anchor")) {
                        anchorsHT.put(thisAnchorSet, subAnchorsToBepsHT);
                    } else if (xsr.getLocalName().equals("subanchor")) {
                        subAnchorsToBepsHT.put(thisSubAnchorSet, toBepsVbySubAnchor);
                    } else if (xsr.getLocalName().equals("incominglinks")) {
                        if (isIncoming) {
                            topicBepsHT.put("incoming : " + thisTopicFileID, bepOffsetVbyTopic);
                            poolIncomingData.put(thisTopicFileID, bepsHT);
                        }
                        isIncoming = false;
                    } else if (xsr.getLocalName().equals("bep")) {
                        bepsHT.put(thisInBepOffset, fromAnchorsV);
                    }
                }
            }
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XMLStreamException ex) {
            Logger.getLogger(poolerManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    // </editor-fold>
}