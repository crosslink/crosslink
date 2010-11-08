
import com.ximpleware.AutoPilot;
import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.TranscodeException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Darren
 */
public class locateABWikiFilesToDIR {

    static void log(Object obj) {
        System.out.println(obj.toString());
    }
    static String poolXmlDir = "C:\\Users\\Darren\\Desktop\\INEX_2009\\POOL_2009\\POOL2009A2B\\BackUP_POOL_DONE03_IN-SORTED\\DONE_WithINCOME";
    static String wikiXmlContainer = "G:\\PHD_Tools\\Wikipedia_XML_A2BContainer\\";

    public static void main(String[] args) {

        try {

            File runDirLocation = new File(poolXmlDir);
            File[] a2bRunFiles = runDirLocation.listFiles();
            if (a2bRunFiles != null) {
                for (int i = 0; i < a2bRunFiles.length; i++) {
                    if (a2bRunFiles[i].isFile()) {
                        String runFile = a2bRunFiles[i].getName();
                        String topicID = runFile.substring(5, runFile.lastIndexOf(".xml"));
                        log("topicID: " + topicID);
                        log("================================================");
                        String runFileFPath = a2bRunFiles[i].getAbsolutePath();
                        // -----------------------------------------------------
                        // Get Outgoing and Incoming Links
                        Hashtable<String, Vector<String>> abPoolOutgoingLinks = getABPoolOutgoingLinks(runFileFPath);
                        Hashtable<String, Vector<String>> abPoolIncomingLinks = getABPoolIncomingLinks(runFileFPath);
//                        log("abPoolOutgoingLinks: " + abPoolOutgoingLinks.get(topicID).size());
//                        log("abPoolIncomingLinks: " + abPoolIncomingLinks.get(topicID).size());
                        // -----------------------------------------------------
                        Enumeration elmnOutEnu = abPoolOutgoingLinks.elements();
                        while (elmnOutEnu.hasMoreElements()) {
                            Object elmnObj = elmnOutEnu.nextElement();
                            Vector<String> elmnVS = (Vector<String>) elmnObj;
                            for (String thisLinkID : elmnVS) {
                                if (thisLinkID.equals("8582") || thisLinkID.equals("22")) {
                                } else {
                                    copyFile(topicID, thisLinkID);
//                                    log("Move " + thisLinkID + " DONE ...");
                                }
                            }
                        }
                        // -----------------------------------------------------
                        Enumeration elmnInEnu = abPoolIncomingLinks.elements();
                        while (elmnInEnu.hasMoreElements()) {
                            Object elmnObj = elmnInEnu.nextElement();
                            Vector<String> elmnVS = (Vector<String>) elmnObj;
                            for (String thisLinkID : elmnVS) {
                                if (thisLinkID.equals("8582") || thisLinkID.equals("22")) {
                                } else {
                                    copyFile(topicID, thisLinkID);
//                                    log("Move " + thisLinkID + " DONE ...");
                                }
                            }
                        }
                    }
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(locateABWikiFilesToDIR.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static void copyFile(String TopicID, String sourceFileID) throws IOException {
        // sourceFileName: 123.xml
        // Source File
//        log("COPYing ... " + sourceFileID);
        String[] sourceFilePathAndSubFolder = getSourceFilePathAndSubFolder(sourceFileID);

        if (sourceFilePathAndSubFolder == null) {
            log("NULL - " + TopicID + " for " + sourceFileID);
        } else {

            File sourceFile = new File(sourceFilePathAndSubFolder[1]);
            // create the Topic Folder
            String destTopicFolderPath = wikiXmlContainer + TopicID;
            File destTopicFolder = new File(destTopicFolderPath);
            if (!destTopicFolder.exists()) {
                destTopicFolder.mkdir();
            }
            // create the SUB Folder
            String destSubFolderPath = destTopicFolderPath + File.separator + sourceFilePathAndSubFolder[0];
            File destSubFolder = new File(destSubFolderPath);
            if (!destSubFolder.exists()) {
                destSubFolder.mkdir();
            }
            // Destination File
            String destFilePath = destSubFolderPath + File.separator + sourceFileID + ".xml";
            File destFile = new File(destFilePath);
            InputStream in = null;
            OutputStream out = null;
            if (!destFile.exists() && sourceFilePathAndSubFolder != null) {
                destFile.createNewFile();
                in = new FileInputStream(sourceFile);
                out = new FileOutputStream(destFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }
        }
    }
    static String sourceXmlDir = "G:\\PHD_Tools\\Wikipedia_2009_Collection\\XML\\";

    private static String[] getSourceFilePathAndSubFolder(String fileID) {
        String[] thisFileFullPath = null;
        int startPoint = 0;
        String subFolder = "";
        if (fileID.length() < 3) {
            startPoint = 0;
            subFolder = fileID.substring(0, fileID.length());
            if (subFolder.length() == 1) {
                subFolder = "00" + subFolder;
            } else if (subFolder.length() == 2) {
                subFolder = "0" + subFolder;
            }
        } else {
            startPoint = fileID.length() - 3;
            subFolder = fileID.substring(startPoint, fileID.length());
        }
        String srcFileFPath = sourceXmlDir + subFolder + File.separator + fileID + ".xml";
        File mySRCFile = new File(srcFileFPath);
        if (mySRCFile.exists()) {
            return thisFileFullPath = new String[]{subFolder, srcFileFPath};
        } else {
            return thisFileFullPath = null;
        }
    }

    private static Hashtable<String, Vector<String>> getABPoolOutgoingLinks(String abPoolFile) {
        Hashtable<String, Vector<String>> abPoolOutgoingLinksHT = new Hashtable<String, Vector<String>>();
        Vector<String> linksIDVS = null;
//        File abPoolFolder = new File(abPoolDir);
//        if (abPoolFolder.isDirectory()) {
//            File[] abPoolList = abPoolFolder.listFiles();
//            for (File abPoolFile : abPoolList) {
//                String abPoolFileName = abPoolFile.getName();
////                log("abPoolFileName: " + abPoolFileName);
        // POOL_2117.xml
        if (abPoolFile.endsWith(".xml")) {
            String abPoolFileID = abPoolFile.substring(abPoolFile.lastIndexOf("POOL_") + 5, abPoolFile.lastIndexOf(".xml"));
//                    log("abPoolFileID: " + abPoolFileID);
            linksIDVS = new Vector<String>();
//                    String abPoolFileAbsPath = abPoolFile.getAbsolutePath();
            VTDGen vg = new VTDGen();
            if (vg.parseFile(abPoolFile, true)) {
                FileOutputStream fos = null;
                try {
                    VTDNav vn = vg.getNav();
                    File fo = new File(abPoolFile);
                    fos = new FileOutputStream(fo);
                    AutoPilot ap = new AutoPilot(vn);
                    XMLModifier xm = new XMLModifier(vn);
                    String xPath = "/crosslink-assessment/topic/outgoinglinks/anchor/subanchor/tobep";
                    ap.selectXPath(xPath);
                    int i = -1;
                    while ((i = ap.evalXPath()) != -1) {
                        int j = vn.getText();
                        String linkID = vn.toRawString(j).toString().trim();
                        if (linkID.endsWith("\"")) {
                            linkID = linkID.substring(0, linkID.length() - 1);
                        }
                        if (!linksIDVS.contains(linkID)) {
//                                    log("linkID: " + linkID);
                            linksIDVS.add(linkID);
                        }
                    }
                    xm.output(fos);
                    fos.close();
                } catch (IOException ex) {
                    Logger.getLogger(locateABWikiFilesToDIR.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TranscodeException ex) {
                    Logger.getLogger(locateABWikiFilesToDIR.class.getName()).log(Level.SEVERE, null, ex);
                } catch (XPathEvalException ex) {
                    Logger.getLogger(locateABWikiFilesToDIR.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NavException ex) {
                    Logger.getLogger(locateABWikiFilesToDIR.class.getName()).log(Level.SEVERE, null, ex);
                } catch (XPathParseException ex) {
                    Logger.getLogger(locateABWikiFilesToDIR.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ModifyException ex) {
                    Logger.getLogger(locateABWikiFilesToDIR.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //--------------------------------------------------
//            log(abPoolFileID + " - IN - " + linksIDVS.size());
            abPoolOutgoingLinksHT.put(abPoolFileID, linksIDVS);
        }
//            }
//            return abPoolOutgoingLinksHT;
//        }
        return abPoolOutgoingLinksHT;
    }

    private static Hashtable<String, Vector<String>> getABPoolIncomingLinks(String abPoolFile) {
        Hashtable<String, Vector<String>> abPoolIncomingLinksHT = new Hashtable<String, Vector<String>>();
        Vector<String> linksIDVS = new Vector<String>();
//        File abPoolFolder = new File(abPoolDir);
//        if (abPoolFolder.isDirectory()) {
//            File[] abPoolList = abPoolFolder.listFiles();
//            for (File abPoolFile : abPoolList) {
//                String abPoolFileName = abPoolFile.getName();
        if (abPoolFile.endsWith(".xml")) {
            // POOL_2117.xml
            String abPoolFileID = abPoolFile.substring(abPoolFile.lastIndexOf("POOL_") + 5, abPoolFile.lastIndexOf(".xml"));
            linksIDVS = new Vector<String>();
//                    String abPoolFileAbsPath = abPoolFile.getAbsolutePath();
            VTDGen vg = new VTDGen();
            if (vg.parseFile(abPoolFile, true)) {
                FileOutputStream fos = null;
                try {
                    VTDNav vn = vg.getNav();
                    File fo = new File(abPoolFile);
                    fos = new FileOutputStream(fo);
                    AutoPilot ap = new AutoPilot(vn);
                    XMLModifier xm = new XMLModifier(vn);
                    String xPath = "/crosslink-assessment/topic/incominglinks/bep/fromanchor";
                    ap.selectXPath(xPath);
                    int i = -1;
                    while ((i = ap.evalXPath()) != -1) {
                        int j = vn.getText();
                        String linkID = vn.toRawString(j).toString().trim();
                        if (linkID.endsWith("\"")) {
                            linkID = linkID.substring(0, linkID.length() - 1);
                        }
                        if (!linksIDVS.contains(linkID)) {
                            linksIDVS.add(linkID);
                        }
                    }
                    xm.output(fos);
                    fos.close();
                } catch (IOException ex) {
                    Logger.getLogger(locateABWikiFilesToDIR.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TranscodeException ex) {
                    Logger.getLogger(locateABWikiFilesToDIR.class.getName()).log(Level.SEVERE, null, ex);
                } catch (XPathEvalException ex) {
                    Logger.getLogger(locateABWikiFilesToDIR.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NavException ex) {
                    Logger.getLogger(locateABWikiFilesToDIR.class.getName()).log(Level.SEVERE, null, ex);
                } catch (XPathParseException ex) {
                    Logger.getLogger(locateABWikiFilesToDIR.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ModifyException ex) {
                    Logger.getLogger(locateABWikiFilesToDIR.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //--------------------------------------------------
//            log(abPoolFileID + " - IN - " + linksIDVS.size());
            abPoolIncomingLinksHT.put(abPoolFileID, linksIDVS);
        }
//            }
//        }
        return abPoolIncomingLinksHT;
    }
}
