package ltwassessment.utility;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ltwassessment.AppResource;
import ltwassessment.assessment.IndexedAnchor;

/**
 * @author Darren
 */
public class PoolUpdater {

    private String poolXMLPath = "";

    private void log(Object obj) {
        System.out.println(obj.toString());
    }

    public PoolUpdater(String poolXmlFile) {
        org.jdesktop.application.ResourceMap resourceMap = AppResource.getInstance().getResourceMap();
//        poolXMLPath = resourceMap.getString("pool.wikipedia");
        poolXMLPath = poolXmlFile;
    }

    // =========================================================================
    // =========================================================================
    public void updateTopicAnchorLinkOS(String topicID, String[] pAnchorOL, String targetID, String[] linkOS) {
        VTDGen vg = new VTDGen();

        if (vg.parseFile(poolXMLPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(poolXMLPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);

                String xPath = "/crosslink-assessment/topic[@file='" + topicID + "']/outgoinglinks/anchor[@aoffset='" + pAnchorOL[0] + "' and @alength='" + pAnchorOL[1] + "']/subanchor/tobep";
                ap.selectXPath(xPath);
                int i = -1;
                while ((i = ap.evalXPath()) != -1) {
                    int j = vn.getAttrVal("tboffset");
                    int k = vn.getAttrVal("tbstartp");
                    int l = vn.getText();
                    if (l != -1) {
                        String rawTxt = vn.toRawString(l).toString();
                        if (rawTxt.equals(targetID)) {
                            if (j != -1) {
                                xm.updateToken(j, linkOS[0].trim());
                            }
                            if (k != -1) {
                                xm.updateToken(k, linkOS[1].trim());
                            }
                        }
                    }
                }
                xm.output(fos);
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // =========================================================================
    // =========================================================================
    public void updatePoolBepStatus(String topicID, String pBepOffset, String toBepStatus) {
        VTDGen vg = new VTDGen();

        if (vg.parseFile(poolXMLPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(poolXMLPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);

                String xPath1 = "/crosslink-assessment/topic[@file='" + topicID + "']/incominglinks/bep[@boffset='" + pBepOffset + "']";
                ap.selectXPath(xPath1);
                int k = -1;
                while ((k = ap.evalXPath()) != -1) {
                    int l = vn.getAttrVal("borel");
                    if (l != -1) {
                        xm.updateToken(l, toBepStatus);
                    }
                }

                xm.output(fos);
                fos.close();

            } catch (IOException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void updatePoolBepWithLinksRel(String topicID, String poolBepO, String boRelStatus) {
        VTDGen vg = new VTDGen();

        if (vg.parseFile(poolXMLPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(poolXMLPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);

                String xPath1 = "/crosslink-assessment/topic[@file='" + topicID + "']/incominglinks/bep[@boffset='" + poolBepO + "']";
                ap.selectXPath(xPath1);
                int k = -1;
                while ((k = ap.evalXPath()) != -1) {
                    int l = vn.getAttrVal("borel");
                    if (l != -1) {
                        xm.updateToken(l, boRelStatus);
                    }
                }

                String xPath2 = "/crosslink-assessment/topic[@file='" + topicID + "']/incominglinks/bep[@boffset='" + poolBepO + "']/fromanchor";
                ap.selectXPath(xPath2);
                int i = -1;
                while ((i = ap.evalXPath()) != -1) {
                    int j = vn.getAttrVal("farel");
                    if (j != -1) {
                        xm.updateToken(j, boRelStatus);
                    }
                }

                xm.output(fos);
                fos.close();

            } catch (IOException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void updateTopicBepLinkRel(String topicID, String poolBepO, String[] targetLinkOLID, String faRelStatus) {
        String targetOffset = targetLinkOLID[0];
        String targetLength = targetLinkOLID[1];
        String targetID = targetLinkOLID[2];

        VTDGen vg = new VTDGen();

        if (vg.parseFile(poolXMLPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(poolXMLPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);

                String xPath = "/crosslink-assessment/topic[@file='" + topicID + "']/incominglinks/bep[@boffset='" + poolBepO + "']/fromanchor[@faoffset='" + targetOffset + "' and @falength='" + targetLength + "']";
                ap.selectXPath(xPath);
                int i = -1;
                while ((i = ap.evalXPath()) != -1) {
                    int j = vn.getAttrVal("farel");
                    int k = vn.getText();
                    if (k != -1) {
                        String rawTxt = vn.toRawString(k).toString();
                        if (rawTxt.equals(targetID)) {
                            if (j != -1) {
                                xm.updateToken(j, faRelStatus);
                            }
                        }
                    }
                }

                xm.output(fos);
                fos.close();

            } catch (IOException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // =========================================================================
    // =========================================================================
    public void updatePoolAnchorStatus(String topicID, IndexedAnchor poolAnchorOL, String pAnchorStatus) {
        // For Right-Click on Anchor Text: ONLY change Pool Anchor Status, Keep panchor-BEPLinks Status
        VTDGen vg = new VTDGen();
        log("poolXMLPath: " + poolXMLPath);
        if (vg.parseFile(poolXMLPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(poolXMLPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);

                log(poolAnchorOL.offsetToString()/*[0]*/ + " - " + poolAnchorOL.lengthToString()/*[1]*/ + " - " + topicID);
                // Anchor
                String xPath1 = "/crosslink-assessment/topic[@file='" + topicID + "']/outgoinglinks/anchor[@aoffset='" + poolAnchorOL.offsetToString()/*[0]*/ + "' and @alength='" + poolAnchorOL.lengthToString()/*[1]*/ + "']";
                ap.selectXPath(xPath1);
                int k = -1;
                while ((k = ap.evalXPath()) != -1) {
                    int l = vn.getAttrVal("arel");
                    if (l != -1) {
                        log(pAnchorStatus);
                        xm.updateToken(l, pAnchorStatus);
                    }
                }
                xm.output(fos);
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void updatePoolAnchorWithLinksRel(String topicID, String[] poolAnchorOL, String pAnchorStatus) {
        VTDGen vg = new VTDGen();

        if (vg.parseFile(poolXMLPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(poolXMLPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);

                // Anchor
                String xPath1 = "/crosslink-assessment/topic[@file='" + topicID + "']/outgoinglinks/anchor[@aoffset='" + poolAnchorOL[0] + "' and @alength='" + poolAnchorOL[1] + "']";
                ap.selectXPath(xPath1);
                int k = -1;
                while ((k = ap.evalXPath()) != -1) {
                    int l = vn.getAttrVal("arel");
                    if (l != -1) {
                        xm.updateToken(l, pAnchorStatus);
                    }
                }
                // Anchor -> Bep Links
                String xPath2 = "/crosslink-assessment/topic[@file='" + topicID + "']/outgoinglinks/anchor[@aoffset='" + poolAnchorOL[0] + "' and @alength='" + poolAnchorOL[1] + "']/subanchor/tobep";
                ap.selectXPath(xPath2);
                int i = -1;
                while ((i = ap.evalXPath()) != -1) {
                    int j = vn.getAttrVal("tbrel");
                    if (j != -1) {
                        xm.updateToken(j, pAnchorStatus);
                    }
                }

                xm.output(fos);
                fos.close();

            } catch (IOException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void updateTopicAnchorLinkRel(String topicID, IndexedAnchor poolAnchorOL, String targetLinkID, String tbRelStatus) {
        String targetID = targetLinkID;

        VTDGen vg = new VTDGen();

        if (vg.parseFile(poolXMLPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(poolXMLPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);

                String xPath = "/crosslink-assessment/topic[@file='" + topicID + "']/outgoinglinks/anchor[@aoffset='" + poolAnchorOL.offsetToString() + "' and @alength='" + poolAnchorOL.lengthToString() + "']/subanchor/tobep";
                ap.selectXPath(xPath);
                int i = -1;
                while ((i = ap.evalXPath()) != -1) {
                    int j = vn.getAttrVal("tbrel");
                    int k = vn.getText();
                    if (k != -1) {
                        String rawTxt = vn.toRawString(k).toString();
                        if (rawTxt.equals(targetID)) {
                            if (j != -1) {
                                xm.updateToken(j, tbRelStatus);
                                break;
                            }
                        }
                    }
                }

                xm.output(fos);
                fos.close();

            } catch (IOException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void updateTopicAnchorLinkOSStatus(String topicID, IndexedAnchor poolAnchorOL, String[] targetLinkOSID, String tbRelStatus) {
        String targetOffset = targetLinkOSID[0];
        String targetStartP = targetLinkOSID[1];
        String targetID = targetLinkOSID[2];

        VTDGen vg = new VTDGen();

        if (vg.parseFile(poolXMLPath, true)) {
            FileOutputStream fos = null;
            try {
                VTDNav vn = vg.getNav();
                File fo = new File(poolXMLPath);
                fos = new FileOutputStream(fo);

                AutoPilot ap = new AutoPilot(vn);
                XMLModifier xm = new XMLModifier(vn);

                String xPath = "/crosslink-assessment/topic[@file='" + topicID + "']/outgoinglinks/anchor[@aoffset='" + poolAnchorOL.offsetToString()/*[0]*/ + "' and @alength='" + poolAnchorOL.lengthToString()/*[1]*/ + "']/subanchor/tobep";
                ap.selectXPath(xPath);
                int i = -1;
                while ((i = ap.evalXPath()) != -1) {
                    int j = vn.getAttrVal("tbrel");
                    int k = vn.getAttrVal("tboffset");
                    int l = vn.getAttrVal("tbstartp");
                    int m = vn.getText();
                    if (m != -1) {
                        String rawTxt = vn.toRawString(m).toString();
                        if (rawTxt.equals(targetID)) {
                            if (j != -1) {
                                xm.updateToken(j, tbRelStatus);
                            }
                            if (k != -1) {
                                xm.updateToken(k, targetOffset);
                            }
                            if (l != -1) {
                                xm.updateToken(l, targetStartP);
                            }
                        }
                    }
                }

                xm.output(fos);
                fos.close();

            } catch (IOException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(PoolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
