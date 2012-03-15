package ltwassessment.utility;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import ltwassessment.AppResource;
import ltwassessment.assessment.Bep;
import ltwassessment.assessment.IndexedAnchor;
import ltwassessment.parsers.FOLTXTMatcher;
import ltwassessment.parsers.PoolerManager;
import ltwassessment.parsers.ResourcesManager;

/**
 * @author Darren HUANG
 */
public class PaneTableIndexing {
    // External Classes

    private PoolerManager myRunsPooler;
    private ResourcesManager myRSCManager;
    private FOLTXTMatcher myFOLMatcher;
    // Global Variables
    private String wikipediaCollTitle = "";
    private String teAraCollTitle = "";
    private String[] afProperty = new String[4];
    private Vector<String[]> RunTopics = null; //new Vector<String[]>();
    private Vector<String> tableTopicIDNameV = null; //new Vector<String>();
    private Vector<String> tableTopicIDV = null; //new Vector<String>();
    private Vector<String[]> paneTableRowIndex = null; //new Vector<String[]>();
    private Vector<String[]> paneTableRowIndexWOText =  new Vector<String[]>();
    // TAB: Outgoing Variables
    private Hashtable<String, Vector<IndexedAnchor>> topicAnchorsHT = null; //new Hashtable<String, Vector<String[]>>();
    private Hashtable<String, Hashtable<String, Hashtable<String, Vector<Bep>>>> poolOutgoingData = null; //new Hashtable<String, Hashtable<String, Hashtable<String, Vector<String[]>>>>();
    private Hashtable<String, Vector<String>> tabAnchorByTopicHT = null;
    private Hashtable<String, Vector<String>> tabAnchorByTopicHTWOTxt = null;
    private Hashtable<String, Vector<String>> tabBepByTopicAnchorHT = null;
    private Hashtable<String, Vector<String>> tabBepByTopicAnchorHTWOTxt = null;
    // TBA: Incoming Variables
    private Hashtable<String, Vector<String[]>> topicBepsHT = null; //new Hashtable<String, Vector<String[]>>();
    private Hashtable<String, Hashtable<String, Vector<String[]>>> poolIncomingData = null; //new Hashtable<String, Hashtable<String, Vector<String[]>>>();
    private Hashtable<String, Vector<String>> tbaBepByTopicHT = null;
    private Hashtable<String, Vector<String>> tbaBepByTopicHTWOTxt = null;
    private Hashtable<String, Vector<String[]>> tbaTargetFileByTopicBepHT = null;
    private Hashtable<String, Vector<String[]>> tbaTargetFileByTopicBepHTWOTxt = null;
    private Hashtable<String, Vector<String[]>> tbaAnchorFileByTopicBepHT = null;
    private Hashtable<String, Vector<String[]>> tbaAnchorFileByTopicBepHTWOTxt = null;
    private Hashtable<String, String[]> NAVIndice = null;

    private static PaneTableIndexing instance = null;
    
    public static PaneTableIndexing getInstance() {
    	if (instance == null)
    		instance = new PaneTableIndexing(true);
    	return instance;
    }
    
    static void log(Object content) {
        System.out.println(content);
    }

    public PaneTableIndexing(boolean isTAB) {       
        //org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ltwassessment.ltwassessmentApp.class).getContext().getResourceMap(ltwassessmentView.class);
        this.wikipediaCollTitle = AppResource.getInstance().getResourceMap().getString("collectionType.Wikipedia");
//        this.teAraCollTitle = AppResource.getInstance().getResourceMap().getString("collectionType.TeAra");
        // ---------------------------------------------------------------------

        this.myRSCManager = ResourcesManager.getInstance();
        this.myFOLMatcher = FOLTXTMatcher.getInstance();

        // ---------------------------------------------------------------------

//        if (isTAB) {
            // Topic --> outgoing --> anchor --> subanchor --> BEPs
            // For GET Methods called by Other Classes
            // populate Row Number, Navigation Indices HT

            // populate All Indexing Variables /above
//            populateTABIndexing();
//        } else {
//            // Hashtable<incoming : topicFileID>: [0]:Offset
//            this.topicBepsHT = myRunsPooler.getTopicAllBeps();
//            // TopicID --> BEP Offset --> Anchor: Offset, Length, Name, FileID
//            this.poolIncomingData = myRunsPooler.getIncomingPool();
//            // For GET Methods called by Other Classes
//            this.tbaBepByTopicHT = new Hashtable<String, Vector<String>>();
//            this.tbaBepByTopicHTWOTxt = new Hashtable<String, Vector<String>>();
//            this.tbaTargetFileByTopicBepHT = new Hashtable<String, Vector<String[]>>();
//            this.tbaTargetFileByTopicBepHTWOTxt = new Hashtable<String, Vector<String[]>>();
//            this.tbaAnchorFileByTopicBepHT = new Hashtable<String, Vector<String[]>>();
//            this.tbaAnchorFileByTopicBepHTWOTxt = new Hashtable<String, Vector<String[]>>();
//            // Get All Column Value in an String[] stored in Vector<String[]>
//            this.paneTableRowIndex = getPaneTableValueSet(isTAB);
//            // populate All Indexing Variables /above
//            populateTBAIndexing();
//        }
    }

    // <editor-fold defaultstate="collapsed" desc="Populate All Variables via Indexing">
//    private void populateTBAIndexing() {
//        // Get All Column Value in an String[] stored in Vector<String[]>
//        this.paneTableRowIndex = getPaneTableValueSet();
//        
//        String thisTopicIDName = "";
//        String thisTopicID = "";
//        String lastTopicIDName = "";
//        String lastTopicID = "";
//        String thisBepOffset = "";
//        Vector<String> bepOffsetV = new Vector<String>();
//        String thisTopicBepOffset = "";
//        String lastTopicBepOffset = "";
//        String[] thisAnchorOLNameSA;
//        String[] thisAnchorOLSA;
//        String[] thisFileIDNameSA;
//        String[] thisFileIDSA;
//        Vector<String[]> AnchorOLNameFileIDNameSAV = new Vector<String[]>();
//        Vector<String[]> AnchorOLFileIDSAV = new Vector<String[]>();
//        String[] thisAnchorOLNameFileIDNameSA;
//        String[] thisAnchorOLFileIDSA;
//        Vector<String[]> TargetFileIDNameSAV = new Vector<String[]>();
//        Vector<String[]> TargetFileIDSAV = new Vector<String[]>();
//
//        int rowCounter = 0;
//        int topicIDIndexCounter = 0;
//        int bepIndexCounter = 0;
//        int fileIndexCounter = 0;
//        int anchorFileIndexCounter = 0;
//
//        for (String[] thisTBASet : paneTableRowIndex) {
//            thisTopicIDName = thisTBASet[0];
//            thisTopicID = thisTopicIDName.split(" : ")[0].trim();
//            if (!tableTopicIDNameV.contains(thisTopicIDName)) {
//                tableTopicIDNameV.add(thisTopicIDName);    // Topic -> 50268
//                tableTopicIDV.add(thisTopicID);
//                if (bepOffsetV.size() > 0) {
//                    topicIDIndexCounter++;
//                    tbaBepByTopicHT.put(lastTopicIDName, bepOffsetV);
//                    tbaBepByTopicHTWOTxt.put(lastTopicID, bepOffsetV);
//                }
//                lastTopicIDName = thisTopicIDName;
//                lastTopicID = thisTopicID;
//
//                bepIndexCounter = 0;
//                bepOffsetV = new Vector<String>(); // Anchor -> 1794_16 : Name
//                thisBepOffset = thisTBASet[1];
//                bepOffsetV.add(thisBepOffset);
//
//                thisTopicBepOffset = thisTopicID + " : " + thisBepOffset; // 50268 : 1256
//                if (AnchorOLNameFileIDNameSAV.size() > 0) {
//                    tbaAnchorFileByTopicBepHT.put(lastTopicBepOffset, AnchorOLNameFileIDNameSAV);
//                    tbaAnchorFileByTopicBepHTWOTxt.put(lastTopicBepOffset, AnchorOLFileIDSAV);
//                    tbaTargetFileByTopicBepHT.put(lastTopicBepOffset, TargetFileIDNameSAV);
//                    tbaTargetFileByTopicBepHTWOTxt.put(lastTopicBepOffset, TargetFileIDSAV);
//                }
//                lastTopicBepOffset = thisTopicBepOffset;
//                // -------------------------------------------------------------
//                // Anchor O L Name - Link ID Name
//                anchorFileIndexCounter = 0;
//                AnchorOLNameFileIDNameSAV = new Vector<String[]>();
//                AnchorOLFileIDSAV = new Vector<String[]>();
//                TargetFileIDNameSAV = new Vector<String[]>();
//                TargetFileIDSAV = new Vector<String[]>();
//
//                String[] thisAnchorSet = thisTBASet[2].split(" : ");
//                thisAnchorOLSA = thisAnchorSet[0].trim().split("_");
//                thisAnchorOLNameSA = new String[]{thisAnchorOLSA[0], thisAnchorOLSA[1], thisAnchorSet[1].trim()};
//
//                thisFileIDNameSA = thisTBASet[3].split(" : ");
//                thisFileIDSA = new String[]{thisFileIDNameSA[0]};
//
//                thisAnchorOLFileIDSA = new String[]{thisAnchorOLSA[0], thisAnchorOLSA[1], thisFileIDNameSA[0]};
//                thisAnchorOLNameFileIDNameSA = new String[]{thisAnchorOLSA[0], thisAnchorOLSA[1], thisAnchorSet[1].trim(), thisFileIDNameSA[0], thisFileIDNameSA[1]};
//
//                TargetFileIDSAV.add(thisFileIDSA);
//                TargetFileIDNameSAV.add(thisFileIDNameSA);
//                AnchorOLFileIDSAV.add(thisAnchorOLFileIDSA);
//                AnchorOLNameFileIDNameSAV.add(thisAnchorOLNameFileIDNameSA);
//
//            } else {
//                if (!bepOffsetV.contains(thisTBASet[1])) {
//                    thisBepOffset = thisTBASet[1];
//                    bepOffsetV.add(thisBepOffset);
//                    bepIndexCounter++;
//
//                    thisTopicBepOffset = thisTopicID + " : " + thisBepOffset; // 50268 : 1256
//                    if (AnchorOLNameFileIDNameSAV.size() > 0) {
//                        tbaAnchorFileByTopicBepHT.put(lastTopicBepOffset, AnchorOLNameFileIDNameSAV);
//                        tbaAnchorFileByTopicBepHTWOTxt.put(lastTopicBepOffset, AnchorOLFileIDSAV);
//                        tbaTargetFileByTopicBepHT.put(lastTopicBepOffset, TargetFileIDNameSAV);
//                        tbaTargetFileByTopicBepHTWOTxt.put(lastTopicBepOffset, TargetFileIDSAV);
//                    }
//                    lastTopicBepOffset = thisTopicBepOffset;
//                    // ---------------------------------------------------------
//                    // Anchor O L Name - Link ID Name
//                    anchorFileIndexCounter = 0;
//                    AnchorOLNameFileIDNameSAV = new Vector<String[]>();
//                    AnchorOLFileIDSAV = new Vector<String[]>();
//                    TargetFileIDNameSAV = new Vector<String[]>();
//                    TargetFileIDSAV = new Vector<String[]>();
//
//                    String[] thisAnchorSet = thisTBASet[2].split(" : ");
//                    thisAnchorOLSA = thisAnchorSet[0].trim().split("_");
//                    thisAnchorOLNameSA = new String[]{thisAnchorOLSA[0], thisAnchorOLSA[1], thisAnchorSet[1].trim()};
//
//                    thisFileIDNameSA = thisTBASet[3].split(" : ");
//                    thisFileIDSA = new String[]{thisFileIDNameSA[0]};
//
//                    thisAnchorOLFileIDSA = new String[]{thisAnchorOLSA[0], thisAnchorOLSA[1], thisFileIDNameSA[0]};
//                    thisAnchorOLNameFileIDNameSA = new String[]{thisAnchorOLSA[0], thisAnchorOLSA[1], thisAnchorSet[1].trim(), thisFileIDNameSA[0], thisFileIDNameSA[1]};
//
//                    TargetFileIDSAV.add(thisFileIDSA);
//                    TargetFileIDNameSAV.add(thisFileIDNameSA);
//                    AnchorOLFileIDSAV.add(thisAnchorOLFileIDSA);
//                    AnchorOLNameFileIDNameSAV.add(thisAnchorOLNameFileIDNameSA);
//                } else {
//                    String[] thisAnchorSet = thisTBASet[2].split(" : ");
//                    thisAnchorOLSA = thisAnchorSet[0].trim().split("_");
//                    thisAnchorOLNameSA = new String[]{thisAnchorOLSA[0], thisAnchorOLSA[1], thisAnchorSet[1].trim()};
//
//                    thisFileIDNameSA = thisTBASet[3].split(" : ");
//                    thisFileIDSA = new String[]{thisFileIDNameSA[0]};
//
//                    thisAnchorOLFileIDSA = new String[]{thisAnchorOLSA[0], thisAnchorOLSA[1], thisFileIDNameSA[0]};
//                    thisAnchorOLNameFileIDNameSA = new String[]{thisAnchorOLSA[0], thisAnchorOLSA[1], thisAnchorSet[1].trim(), thisFileIDNameSA[0], thisFileIDNameSA[1]};
//
//                    TargetFileIDSAV.add(thisFileIDSA);
//                    TargetFileIDNameSAV.add(thisFileIDNameSA);
//                    AnchorOLFileIDSAV.add(thisAnchorOLFileIDSA);
//                    AnchorOLNameFileIDNameSAV.add(thisAnchorOLNameFileIDNameSA);
//                    anchorFileIndexCounter++;
//                }
//            }
//            String[] thisNavIndicesSA = new String[]{String.valueOf(topicIDIndexCounter), String.valueOf(bepIndexCounter),
//                String.valueOf("0"), String.valueOf(anchorFileIndexCounter)};
//            NAVIndice.put(String.valueOf(rowCounter), thisNavIndicesSA);
//            rowCounter++;
//        }
//        // Last ONEs
//        tbaBepByTopicHT.put(thisTopicIDName, bepOffsetV);
//        tbaBepByTopicHTWOTxt.put(thisTopicID, bepOffsetV);
//        tbaAnchorFileByTopicBepHT.put(thisTopicBepOffset, AnchorOLNameFileIDNameSAV);
//        tbaAnchorFileByTopicBepHTWOTxt.put(thisTopicBepOffset, AnchorOLFileIDSAV);
//        tbaTargetFileByTopicBepHT.put(thisTopicBepOffset, TargetFileIDNameSAV);
//        tbaTargetFileByTopicBepHTWOTxt.put(thisTopicBepOffset, TargetFileIDSAV);
//
//        String[] thisNavIndicesSA = new String[]{String.valueOf(topicIDIndexCounter), String.valueOf(bepIndexCounter),
//            String.valueOf("0"), String.valueOf(anchorFileIndexCounter)};
//        NAVIndice.put(String.valueOf(rowCounter), thisNavIndicesSA);
//    }

    public void populateTABIndexing() {
        // Get All Column Value in an String[] stored in Vector<String[]>
        this.paneTableRowIndex = getPaneTableValueSet();

        this.NAVIndice = new Hashtable<String, String[]>();
        this.tableTopicIDNameV = new Vector<String>();
        this.tableTopicIDV = new Vector<String>();
        this.tabAnchorByTopicHT = new Hashtable<String, Vector<String>>();
        this.tabAnchorByTopicHTWOTxt = new Hashtable<String, Vector<String>>();
        this.tabBepByTopicAnchorHT = new Hashtable<String, Vector<String>>();
        this.tabBepByTopicAnchorHTWOTxt = new Hashtable<String, Vector<String>>();
        
        String thisTopicIDName = "";
        String thisTopicID = "";
        String lastTopicIDName = "";
        String lastTopicID = "";
        String thisAnchorOLName = "";
        String thisAnchorOL = "";
        Vector<String> anchorOLNameV = new Vector<String>();
        Vector<String> anchorOLV = new Vector<String>();
        String topicAnchorIDName = "";
        String topicAnchorID = "";
        String lastTopicAnchorIDName = "";
        String lastTopicAnchorID = "";
        Vector<String> BepOIDNameV = new Vector<String>();
        Vector<String> BepOIDV = new Vector<String>();
        String thisBepOIDNameSet = "";
        String thisBepOIDSet = "";

        int rowCounter = 0;
        int topicIDIndexCounter = 0;
        int poolAnchorIndexCounter = 0;
        int anchorIndexCounter = 0;
        int BEPIndexCounter = 0;

        for (String[] thisTABSet : paneTableRowIndex) {
            thisTopicIDName = thisTABSet[0];
            thisTopicID = thisTopicIDName.split(" : ")[0].trim();
            if (!tableTopicIDNameV.contains(thisTopicIDName)) {
                tableTopicIDNameV.add(thisTopicIDName);    // Topic -> 50268
                tableTopicIDV.add(thisTopicID);
                if (anchorOLNameV.size() > 0) {
                    topicIDIndexCounter++;
                    tabAnchorByTopicHT.put(lastTopicIDName, anchorOLNameV);
                    tabAnchorByTopicHTWOTxt.put(lastTopicID, anchorOLV);
                }
                lastTopicIDName = thisTopicIDName;
                anchorOLNameV = new Vector<String>(); // Anchor -> 1794_16 : Name
                thisAnchorOLName = thisTABSet[1];
                anchorOLNameV.add(thisAnchorOLName);
                lastTopicID = thisTopicID;

                poolAnchorIndexCounter = 0;
                anchorOLV = new Vector<String>(); // Anchor -> 1794_16
                thisAnchorOL = thisAnchorOLName.split(" : ")[0].trim();
                anchorOLV.add(thisAnchorOL);

                topicAnchorIDName = thisTopicIDName + " : " + thisAnchorOLName;
                topicAnchorID = thisTopicID + " : " + thisAnchorOL; // 50268 : 1794_16
                if (BepOIDNameV.size() > 0) {
                    tabBepByTopicAnchorHT.put(lastTopicAnchorIDName, BepOIDNameV);
                    tabBepByTopicAnchorHTWOTxt.put(lastTopicAnchorID, BepOIDV);
                }
                lastTopicAnchorIDName = topicAnchorIDName;
                lastTopicAnchorID = topicAnchorID;
                // BEP link
                BepOIDNameV = new Vector<String>();
                thisBepOIDNameSet = thisTABSet[3];
                BepOIDNameV.add(thisBepOIDNameSet);

                BEPIndexCounter = 0;
                BepOIDV = new Vector<String>(); // BEP -> 728_16797009
                thisBepOIDSet = thisBepOIDNameSet.split(" : ")[0].trim();
                BepOIDV.add(thisBepOIDSet);
            } else {
                if (!anchorOLNameV.contains(thisTABSet[1])) {
                    thisAnchorOLName = thisTABSet[1];
                    anchorOLNameV.add(thisAnchorOLName);
                    thisAnchorOL = thisAnchorOLName.split(" : ")[0].trim();
                    anchorOLV.add(thisAnchorOL);
                    poolAnchorIndexCounter++;

                    topicAnchorIDName = thisTopicIDName + " : " + thisAnchorOLName;
                    topicAnchorID = thisTopicID + " : " + thisAnchorOL;   // 50268 : 1794_16
                    tabBepByTopicAnchorHT.put(lastTopicAnchorIDName, BepOIDNameV);
                    tabBepByTopicAnchorHTWOTxt.put(lastTopicAnchorID, BepOIDV);
                    lastTopicAnchorIDName = topicAnchorIDName;
                    lastTopicAnchorID = topicAnchorID;
                    BepOIDNameV = new Vector<String>();
                    thisBepOIDNameSet = thisTABSet[3];
                    BepOIDNameV.add(thisBepOIDNameSet);

                    BEPIndexCounter = 0;
                    BepOIDV = new Vector<String>(); // BEP -> 728_16797009
                    thisBepOIDSet = thisBepOIDNameSet.split(" : ")[0].trim();
                    BepOIDV.add(thisBepOIDSet);
                } else {
                    thisBepOIDNameSet = thisTABSet[3];
                    BepOIDNameV.add(thisBepOIDNameSet);
                    thisBepOIDSet = thisBepOIDNameSet.split(" : ")[0].trim();
                    BepOIDV.add(thisBepOIDSet);
                    BEPIndexCounter++;
                }
            }
            String[] thisNavIndicesSA = new String[]{String.valueOf(topicIDIndexCounter), String.valueOf(poolAnchorIndexCounter),
                String.valueOf("0"), String.valueOf(BEPIndexCounter)};
            NAVIndice.put(String.valueOf(rowCounter), thisNavIndicesSA);
            rowCounter++;
        }
        // Last ONEs
        tabAnchorByTopicHT.put(thisTopicIDName, anchorOLNameV);
        tabAnchorByTopicHTWOTxt.put(thisTopicID, anchorOLV);
        tabBepByTopicAnchorHT.put(topicAnchorIDName, BepOIDNameV);
        tabBepByTopicAnchorHTWOTxt.put(topicAnchorID, BepOIDV);

        String[] thisNavIndicesSA = new String[]{String.valueOf(topicIDIndexCounter), String.valueOf(poolAnchorIndexCounter),
            String.valueOf("0"), String.valueOf(BEPIndexCounter)};
        NAVIndice.put(String.valueOf(rowCounter), thisNavIndicesSA);
    }

    public Vector<String[]> getPaneTableValueSet() {
        // Target: String[4]:
        // TAB: Topic(22560) : Anchor(1114_19) : Subanchor(1114_13) : BEP([0]:1538, [1]:123017)
        // TBA: Topic(22560) : BEP(1538) : Anchor(1114_13) : FileID(123017)
        this.myRunsPooler = PoolerManager.getInstance();
        this.poolOutgoingData = myRunsPooler.getOutgoingPool();
        // [0]:participant-id, [1]:run-id, [2]:task, [3]:collection
        this.afProperty = myRunsPooler.getPoolProperty();
        // Vector<String[]>: [0]:FileID, [1]:Name
        this.RunTopics = myRunsPooler.getAllTopicsInPool();
        
        // Hashtable<outgoing : topicFileID>: [0]:Offset, [1]:Length, [2]:Anchor_Name
        this.topicAnchorsHT = myRunsPooler.getTopicAllAnchors();
        
        paneTableRowIndexWOText.clear();
        
        Vector<String[]> myTableSetV = new Vector<String[]>();
        Vector<String> myTopicIDsV = new Vector<String>();
//        if (isTAB) {
            // Topic --> outgoing --> pool anchor --> anchor --> BEP Links
            // Hashtable<outgoing : topicFileID, Vector<[0]:Offset, [1]:Length, [2]:Anchor_Name>>
            Hashtable<String, Vector<String>> myTopicAnchorOLHT = new Hashtable<String, Vector<String>>();
            Vector<String> myAnchorOLV = new Vector<String>();
            Enumeration topicKeyEnu = poolOutgoingData.keys();
            while (topicKeyEnu.hasMoreElements()) {
                // Get Topic ID
                Object topicKeyObj = topicKeyEnu.nextElement();
                if (!myTopicIDsV.contains(topicKeyObj.toString())) {
                    myTopicIDsV.add(topicKeyObj.toString());
                }
                // Get Anchor OL
                myAnchorOLV = new Vector<String>();
                Hashtable<String, Hashtable<String, Vector<Bep>>> anchorBepsH = poolOutgoingData.get(topicKeyObj);
                Enumeration anchorKeyEnu = anchorBepsH.keys();
                while (anchorKeyEnu.hasMoreElements()) {
                    Object anchorOLObj = anchorKeyEnu.nextElement();
                    if (!myAnchorOLV.contains(anchorOLObj.toString())) {
                        myAnchorOLV.add(anchorOLObj.toString());
                    }
                }
                Vector<String> mySortedAnchorOLV = sortOLVectorNumbers(myAnchorOLV);
                myTopicAnchorOLHT.put(topicKeyObj.toString(), mySortedAnchorOLV);
            }
            Collections.sort(myTopicIDsV);
            for (String thisTopic : myTopicIDsV) {
                Hashtable<String, Hashtable<String, Vector<Bep>>> anchorBepsH = poolOutgoingData.get(thisTopic);
                Vector<String> anchorOLV = myTopicAnchorOLHT.get(thisTopic);
                for (String thisAnchor : anchorOLV) {
                    // Anchor(1114_19)
                    Hashtable<String, Vector<Bep>> subAnchorBEPsH = anchorBepsH.get(thisAnchor);
                    Enumeration subAnchorKeyEnu = subAnchorBEPsH.keys();
                    while (subAnchorKeyEnu.hasMoreElements()) {
                        Object subAnchorKeyObj = subAnchorKeyEnu.nextElement();
                        String thisSubAnchor = subAnchorKeyObj.toString();  // Subanchor(1114_13)
                        Vector<Bep> bepsV = subAnchorBEPsH.get(subAnchorKeyObj);
                        /*
                         * contain lang info here "zh", "ja", "ko"
                         */
//                        for (IndexedAnchor anchor : bepsV) 
	                        for (Bep bepSA : bepsV) {
	                            String bepOffset = bepSA.offsetToString(); //[0];
	                            String bepFileID = bepSA.getFileId(); //[1];
	                            String bepSet = bepOffset + "_" + bepFileID;    // BEP([0]:1538, [1]:123017)
	                            // add to TABSet
	                            // Topic(22560) : Anchor(1114_19) : Subanchor(1114_13) : BEP([0]:1538, [1]:123017)
	                            // With Text
	                            String[] myTABLine = new String[4];
	                            myTABLine[0] = getTopicIDNamePair(thisTopic);
	                            myTABLine[1] = getAnchorOLNamePair(thisTopic, thisAnchor);
	                            if (AppResource.forAssessment)
	                            	myTABLine[2] = getAnchorOLNamePair(thisTopic, thisSubAnchor);
	                            else
	                            	myTABLine[2] = bepSA.getTargetLang(); //[3];
	                            myTABLine[3] = getLinkBepIdONamePair(bepSet); //String.valueOf(bepOffset); //bepSet; //
	                            myTableSetV.add(myTABLine);
	                            // Without Text
	                            String[] myTABWOTextLine = new String[4];
	                            myTABWOTextLine[0] = thisTopic;
	                            myTABWOTextLine[1] = thisAnchor;
	                            if (AppResource.forAssessment)
	                            	 myTABWOTextLine[2] = thisSubAnchor;
	                            else
	                            	 myTABWOTextLine[2] = bepSA.getTargetLang(); //[3];
	                            myTABWOTextLine[3] = bepSet;
	                            paneTableRowIndexWOText.add(myTABWOTextLine);
	                        }
                    }
                }
            }
        return myTableSetV;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Get Topic / Anchor / BEP Indices">
    public Hashtable<String, String[]> getRowNAVIndices() {
        return NAVIndice;
    }

    public Vector<String> getTABTopicIDNameIndex() {
        // List all Topics Sorted
        return tableTopicIDNameV;
    }

    public Hashtable<String, Vector<String>> getTABTopicAnchorIndices() {
        // List all Anchors Sorted for each Topic
        return tabAnchorByTopicHT;
    }

    public Hashtable<String, Vector<String>> getTABTopicAnchorBepIndices() {
        // List all BEPs Sorted for each Topic_AnchorOL <-- 25640 : 1794_16
        return tabBepByTopicAnchorHT;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Get Row-Set w/wo Txt by Row-/Index">
    public String[] rowTBASAByIndices(int selectedTopicIndex, int selectedBepIndex, int selectedAnchorLinkIndex) {
        String[] rowTBASA = new String[4];
        String topicID = tableTopicIDV.elementAt(selectedTopicIndex);
        Vector<String> tBepOffsetV = tbaBepByTopicHTWOTxt.get(topicID);
        String tBepOffset = tBepOffsetV.elementAt(selectedBepIndex);
        String topicBepKey = topicID + " : " + tBepOffset;
        Vector<String[]> tbAnchorOLFileIDV = tbaAnchorFileByTopicBepHTWOTxt.get(topicBepKey);
        String[] taBepOID = tbAnchorOLFileIDV.elementAt(selectedAnchorLinkIndex);
        rowTBASA[0] = topicID;  // Topic -> 50268
        rowTBASA[1] = tBepOffset;  // Bep Offset -> 1256
        rowTBASA[2] = taBepOID[0] + "_" + taBepOID[1];  // Anchor O_L
        rowTBASA[3] = taBepOID[2];  // File ID -> 1679700

        return rowTBASA;
    }

    public String[] rowTABSAByIndices(int selectedTopicIndex, int selectedAnchorIndex, int selectedBEPLinkIndex) {
        String[] rowTABSA = new String[4];
        String topicID = tableTopicIDV.elementAt(selectedTopicIndex);
        Vector<String> tAnchorOLV = tabAnchorByTopicHTWOTxt.get(topicID);
        String tAnchorOL = tAnchorOLV.elementAt(selectedAnchorIndex);
        String bepSetKey = topicID + " : " + tAnchorOL;
        Vector<String> taBepOIDV = tabBepByTopicAnchorHTWOTxt.get(bepSetKey);
        String taBepOID = taBepOIDV.elementAt(selectedBEPLinkIndex);
        rowTABSA[0] = topicID;      // Topic -> 50268
        rowTABSA[1] = tAnchorOL;    // Pool Anchor -> 1794_16
        rowTABSA[2] = "";
        rowTABSA[3] = taBepOID;     // BEP -> 728_16797009

        return rowTABSA;
    }

    public Vector<String[]> getTableValuesByRowIndex() {
        return paneTableRowIndex;
    }

    public Vector<String[]> getTableValuesByRowIndexWOText() {
        return paneTableRowIndexWOText;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Get -Name Pairs">
    private String getLinkXmlFileIdNamePair(String xmlFileID) {
        // bepSet: 1538_123017
        String xmlFileIDNamePair = "";
        String linkXmlPath = "";
//        if (myRSCManager.getLinkCollType().equals(wikipediaCollTitle)) {
            linkXmlPath = myRSCManager.getWikipediaFilePathByName(xmlFileID + ".xml", AppResource.targetLang);
//        } else if (myRSCManager.getLinkCollType().equals(teAraCollTitle)) {
//            linkXmlPath = myRSCManager.getTeAraCollectionFolder() + myRSCManager.getTeAraFilePathByName(xmlFileID + ".xml");
//        }
        String bepXmlTitle = myFOLMatcher.getWikiXmlTitleByFilePath(linkXmlPath);
        return xmlFileIDNamePair = xmlFileID + " : " + bepXmlTitle;
    }

    private String getLinkBepIdONamePair(String bepSet) {
        // bepSet: 1538_123017
        String bepIdONamePair = "";
        String[] bepSetSA = bepSet.split("_");
        String xmlFileID = bepSetSA[1].trim();
        String linkXmlPath = "";
//        if (myRSCManager.getLinkCollType().equals(wikipediaCollTitle)) {
            if (myRSCManager.getWikipediaFilePathByName(xmlFileID + ".xml", AppResource.targetLang).equals("FileNotFound.xml")) {
            } else {
                linkXmlPath = myRSCManager.getWikipediaFilePathByName(xmlFileID + ".xml", AppResource.targetLang);
                String bepXmlTitle = myFOLMatcher.getWikiXmlTitleByFilePath(linkXmlPath);
                return bepIdONamePair = bepSet + " : " + bepXmlTitle;
            }
//        } else if (myRSCManager.getLinkCollType().equals(teAraCollTitle)) {
//            if (myRSCManager.getTeAraFilePathByName(xmlFileID + ".xml").equals("FileNotFound.xml")) {
//            } else {
//                linkXmlPath = myRSCManager.getTeAraCollectionFolder() + myRSCManager.getTeAraFilePathByName(xmlFileID + ".xml");
//                String bepXmlTitle = myFOLMatcher.getWikiXmlTitleByFilePath(linkXmlPath);
//                return bepIdONamePair = bepSet + " : " + bepXmlTitle;
//            }
//        }
//        String bepXmlTitle = myFOLMatcher.getWikiXmlTitleByFilePath(linkXmlPath);
        return bepIdONamePair = "";
    }

    private String getAnchorOLNamePair(String topicID, String anchorOL) {
        // topicID(i.e. 112369), anchorOL(i.e. 1114_19)
        String myPair = "";
        Enumeration keyEnu = topicAnchorsHT.keys();
        while (keyEnu.hasMoreElements()) {
            Object keyObj = keyEnu.nextElement();
            String[] keyOutgoingTopicStr = keyObj.toString().split(" : ");
            if (keyOutgoingTopicStr[1].equals(topicID.trim())) {
                Vector<IndexedAnchor> anchorsV = topicAnchorsHT.get(keyObj);
                for (IndexedAnchor thisAnchorSA : anchorsV) {
                    String OLSet = thisAnchorSA.offsetToString().trim() + "_" + thisAnchorSA.lengthToString().trim();
                    if (OLSet.equals(anchorOL)) //{
                        return myPair = anchorOL + " : " + thisAnchorSA.getName()/*[2]*/.trim();
                }
            }
        }
        return myPair = "Error, " + anchorOL + " not found! ";
    }

    private String getTopicIDNamePair(String topicFileID) {
        // topicID(i.e. 112369)
        String myPair = "";
        // Vector<String[]>: [0]:File, [1]:Name
        for (String[] thisTopicIDName : RunTopics) {
            if (thisTopicIDName[0].trim().equals(topicFileID.trim())) {
                return myPair = topicFileID + " : " + thisTopicIDName[1].trim();
            }
        }
        return myPair = topicFileID + " : ";
    }
    // </editor-fold>

    private Vector<String> sortOLVectorNumbers(Vector<String> myOLNumbersV) {
        int[] thisIntA = new int[myOLNumbersV.size()];
        for (int i = 0; i < myOLNumbersV.size(); i++) {
            String[] myOLNo = myOLNumbersV.elementAt(i).split("_");
            thisIntA[i] = Integer.valueOf(myOLNo[0]);
        }
        for (int i = 0; i < thisIntA.length; i++) {
            for (int j = i + 1; j < thisIntA.length; j++) {
                if (thisIntA[i] > thisIntA[j]) {
                    int temp = thisIntA[i];
                    thisIntA[i] = thisIntA[j];
                    thisIntA[j] = temp;
                }
            }
        }
        Vector<String> mySortedOLNumbersV = new Vector<String>();
        for (int i = 0; i < thisIntA.length; i++) {
            String thisINT = String.valueOf(thisIntA[i]);
            // ------------------------------------------
            for (String myOL : myOLNumbersV) {
                String[] myOLSA = myOL.split("_");
                if (myOLSA[0].equals(thisINT)) {
                    mySortedOLNumbersV.add(myOL);
                    break;
                }
            }
        }
        return mySortedOLNumbersV;
    }

    private Vector<String> sortVectorNumbers(Vector<String> myNumbersV) {
        int[] thisIntA = new int[myNumbersV.size()];
        for (int i = 0; i < myNumbersV.size(); i++) {
            thisIntA[i] = Integer.valueOf(myNumbersV.elementAt(i));
        }
        for (int i = 0; i < thisIntA.length; i++) {
            for (int j = i + 1; j < thisIntA.length; j++) {
                if (thisIntA[i] > thisIntA[j]) {
                    int temp = thisIntA[i];
                    thisIntA[i] = thisIntA[j];
                    thisIntA[j] = temp;
                }
            }
        }
        Vector<String> mySortedNumbersV = new Vector<String>();
        for (int i = 0; i < thisIntA.length; i++) {
            mySortedNumbersV.add(String.valueOf(thisIntA[i]));
        }
        return mySortedNumbersV;
    }
}
