
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

/**
 * @author Darren
 */
public class poolUpdater {

    static void log(Object obj) {
        System.out.println(obj.toString());
    }
    private static String poolXMLPath = "resources" + File.separator + "Pool" + File.separator + "wikipedia_pool_TEST.xml";
    private static int option = 5;

    public static void main(String[] args) {

        String topicID = "50585";
        String[] pAnchorOL = new String[]{"1114", "19"};
        String targetID = "15355278";
        String pBepO = "815";
        String[] targetOLID = new String[]{"10", "9", "2594030"};


        switch (option) {
            case 0:
                // update Anchor Link
                String tbRelStatus = "1";   // only 1 or -1
                updateTopicAnchorLinkRel(topicID, pAnchorOL, targetID, tbRelStatus);
                break;
            case 1:
                // update Anchor Links
//                updateTopicAnchorLinksRel(topicID, pAnchorOL, pAnchorRelStatus);
                break;
            case 2:
                // update Pool Anchor + its Links <-- non-relevant from Anchor Right-Click
                String pAnchorRelStatus = "-1";
                updatePoolAnchorWithLinksRel(topicID, pAnchorOL, pAnchorRelStatus);
                break;
            case 3:
                // update Bep Link
                String taRelStatus = "-1";   // only 1 or -1
                updateTopicBepLinkRel(topicID, pBepO, targetOLID, taRelStatus);
                break;
            case 4:
                // update Anchor Links
//                updateTopicBepLinksRel();
                break;
            case 5:
                // update Pool Anchor + its Links <-- non-relevant from Anchor Right-Click
                String pbRelStatus = "-1";   // only -1
                updatePoolBepWithLinksRel(topicID, pBepO, pbRelStatus);
                break;
            case 6:
                // update Target Link offset & s-point
                String[] linkOS = new String[]{"1234", "1019"};
                updateTopicAnchorLinkOS(topicID, pAnchorOL, targetID, linkOS);
                break;
            default:
                log("This is the default option...");
        }
    }

    // =========================================================================
    // =========================================================================
    private static void updateTopicAnchorLinkOS(String topicID, String[] pAnchorOL, String targetID, String[] linkOS) {
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
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // =========================================================================
    // =========================================================================
    private static void updatePoolBepWithLinksRel(String topicID, String poolBepO, String boRelStatus) {
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
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void updateTopicBepLinkRel(String topicID, String poolBepO, String[] targetLinkOLID, String faRelStatus) {
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
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // =========================================================================
    // =========================================================================
    private static void updatePoolAnchorWithLinksRel(String topicID, String[] poolAnchorOL, String pAnchorStatus) {
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
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void updateTopicAnchorLinkRel(String topicID, String[] poolAnchorOL, String targetLinkID, String tbRelStatus) {
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

                String xPath = "/crosslink-assessment/topic[@file='" + topicID + "']/outgoinglinks/anchor[@aoffset='" + poolAnchorOL[0] + "' and @alength='" + poolAnchorOL[1] + "']/subanchor/tobep";
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
                            }
                        }
                    }
                }

                xm.output(fos);
                fos.close();

            } catch (IOException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscodeException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathEvalException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NavException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathParseException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ModifyException ex) {
                Logger.getLogger(poolUpdater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
