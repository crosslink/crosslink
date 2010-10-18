
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
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class reviseDuplicateSubAnchor {

    static void log(Object obj) {
        System.out.println(obj.toString());
    }
    private static int option = 0;

    public static void main(String[] args) {

        switch (option) {
            case 0:
                Vector<String> packagesV = new Vector<String>();
//                packagesV.add("G:\\PHD_Tools\\LTW2009_A2B_POOLBACKUP_24TOPICS\\ltwATool_ANDREW");
//                packagesV.add("G:\\PHD_Tools\\LTW2009_A2B_POOLBACKUP_24TOPICS\\ltwATool_UMAS");
//                packagesV.add("G:\\PHD_Tools\\LTW2009_A2B_POOLBACKUP_9TOPICS\\ltwATool_Shlomo");
//                packagesV.add("G:\\PHD_Tools\\LTW2009_A2B_POOLBACKUP_9TOPICS\\DARREN");
                packagesV.add("G:\\PHD_Tools\\LTW2009_A2B_POOLBACKUP_24TOPICS\\ltwATool_Kelly\\Topic30003");
                String endWithStr = "_wikipedia_pool.xml";
                String revisedPoolExten = "_Revised";
                reviseDuplicateSAOL(packagesV, endWithStr, revisedPoolExten);
                break;
            case 1:
                break;
            case 2:
                break;
            default:
                log("This is the default option...");
        }

    }

    // <editor-fold defaultstate="collapsed" desc="revise Duplicate SubAnchor Offset-Length">
    private static void reviseDuplicateSAOL(Vector<String> packagesDirV, String endWithStr, String revisedPoolExten) {


        for (String packageDir : packagesDirV) {
            File packageDirF = new File(packageDir);
            if (packageDirF.isDirectory()) {
                File[] packagePoolFList = packageDirF.listFiles();
                for (File packagePoolFile : packagePoolFList) {
                    String packagePoolFPath = packagePoolFile.getAbsolutePath();
                    if (packagePoolFPath.endsWith(endWithStr)) {
                        log(packagePoolFPath);
                        String packagePoolFName = packagePoolFile.getName();
                        String packagePoolName = packagePoolFName.substring(0, packagePoolFName.lastIndexOf(".xml"));
                        String packagePoolFPathRevised = packageDir + "\\" + packagePoolName + revisedPoolExten + ".xml";
                        // =====================================================
                        // =====================================================
                        VTDGen vg = new VTDGen();
                        if (vg.parseFile(packagePoolFPath, true)) {
                            FileOutputStream fos = null;
                            try {
                                VTDNav vn = vg.getNav();
                                File fo = new File(packagePoolFPathRevised);
                                fos = new FileOutputStream(fo);

                                AutoPilot ap = new AutoPilot(vn);
                                XMLModifier xm = new XMLModifier(vn);

                                Vector<String[]> pAnchorOLV = new Vector<String[]>();
                                String xPath = "/inexltw-assessment/topic/outgoinglinks/anchor";
                                ap.selectXPath(xPath);
                                int i = -1;
                                while ((i = ap.evalXPath()) != -1) {
                                    int j = vn.getAttrVal("aoffset");
                                    int k = vn.getAttrVal("alength");
                                    if (j != -1) {
                                        String aOffset = vn.toRawString(j).trim();
                                        String aLength = vn.toRawString(k).trim();
                                        if (!pAnchorOLV.contains(new String[]{aOffset, aLength})) {
                                            pAnchorOLV.add(new String[]{aOffset, aLength});
                                        } else {
                                            log("******************* Duplicated Pool Anchor: " + aOffset + " - " + aLength);
                                        }
                                    }
                                }

                                Vector<String> saOLRecorder = new Vector<String>();
                                for (String[] thisAOLSet : pAnchorOLV) {
                                    saOLRecorder = new Vector<String>();
                                    String thisAOffset = thisAOLSet[0].trim();
                                    String thisALength = thisAOLSet[1].trim();
                                    String xPathT = "/inexltw-assessment/topic/outgoinglinks/anchor[@aoffset='" + thisAOffset + "' and @alength='" + thisALength + "']/subanchor";
                                    ap.selectXPath(xPathT);
                                    int ii = -1;
                                    while ((ii = ap.evalXPath()) != -1) {
                                        int l = vn.getAttrVal("saoffset");
                                        int m = vn.getAttrVal("salength");
                                        String saOffset = vn.toRawString(l).toString();
                                        String saLength = vn.toRawString(m).toString();
                                        String saOLStr = saOffset + "_" + saLength;
                                        if (saOLRecorder.contains(saOLStr)) {
                                            log("============================");
                                            log("Duplicated SUB-Anchor: " + saOLStr);
                                            do {
                                                saLength = String.valueOf(Integer.valueOf(saLength) + 1);
                                                saOLStr = saOffset + "_" + saLength;
                                            } while (saOLRecorder.contains(saOLStr));
                                            log("Updated SUB-Anchor: " + saOLStr);
                                            xm.updateToken(m, saLength);

                                        } else {
                                            saOLRecorder.add(saOLStr);
                                        }
                                    }
                                }
                                xm.output(fos);
                                fos.close();
                            } catch (IOException ex) {
                                Logger.getLogger(reviseDuplicateSubAnchor.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (TranscodeException ex) {
                                Logger.getLogger(reviseDuplicateSubAnchor.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (XPathEvalException ex) {
                                Logger.getLogger(reviseDuplicateSubAnchor.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (NavException ex) {
                                Logger.getLogger(reviseDuplicateSubAnchor.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (XPathParseException ex) {
                                Logger.getLogger(reviseDuplicateSubAnchor.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (ModifyException ex) {
                                Logger.getLogger(reviseDuplicateSubAnchor.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        // =====================================================
                    }
                }
            }
        }
    }
    // </editor-fold>
}
