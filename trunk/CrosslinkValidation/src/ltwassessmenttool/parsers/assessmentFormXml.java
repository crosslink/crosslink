package ltwassessmenttool.parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import ltwassessmenttool.LTWAssessmentToolView;
import ltwassessmenttool.utility.toolErrorHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * @author Darren Huang
 */
public class assessmentFormXml {

    /*
     * Possible Types:
     * 1) Te Ara
     * 2) Wikipedia
     * 3) Te Ara --> to --> Wikipedia
     */
    private String AFXmlFile = "";
    private String docLocalName = "inexltw-submission";
    private String collectionTag = "collection";
    private Hashtable<String, String[]> docTitleTagHT = new Hashtable<String, String[]>();
    private String[] collectionTypes = new String[2];
    private String xmlSchemaFile = "";
    private String xmlAssessXmlFileFolder = "";
    private String xmlAssessXmlFileName = "";
    private String poolingErrorMsg = "";
    private boolean isPoolingDone = false;

    void log(Object content) {
        System.out.println(content);
    }

    public assessmentFormXml(ArrayList<File> xmlFileList) {

        // 1) Title Tags
        docTitleTagHT.put(docLocalName, new String[]{"participant-id", "run-id", "task"});
        // 2) Collection Types
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ltwassessmenttool.LTWAssessmentToolApp.class).getContext().getResourceMap(LTWAssessmentToolView.class);
        collectionTypes[0] = resourceMap.getString("collection.Wikipedia");
//        collectionTypes[1] = resourceMap.getString("collection.TeAra");
        // 4) get XML Schema File
        xmlSchemaFile = resourceMap.getString("pooling.runXmlSchema");
        xmlAssessXmlFileFolder = resourceMap.getString("pooling.AFXmlFolder");
        // ONLY ONE validation XML file will be produced
        AFXmlFile = xmlAssessXmlFileFolder + File.separator + "VerificationXmlForm.xml";
        // 3) get All Passed XML Files
        File[] myRuns = new File[xmlFileList.size()];
        int fileCounter = 0;
        for (File xmlFile : xmlFileList) {
            myRuns[fileCounter] = xmlFile;
            fileCounter++;
        }
        // Produce XML Assessment Form
        produceAssessmentFormXmlFromRun(myRuns);
    }

    public String getPoolingMsg() {
        String poolMsg = "";
        if (isPoolingDone) {
            poolMsg = "Assessment Form XML: " + xmlAssessXmlFileName;
        } else {
        	 poolMsg = (poolingErrorMsg.length() > 0 ? "Err Msg: " : "" ) + poolingErrorMsg;
        }
        return poolMsg;
    }

    private void produceAssessmentFormXmlFromRun(File[] myRuns) {
        // 1) validate Submission XML against Schema
        Vector<File> validatedRunsV = new Vector<File>();
        for (File thisFile : myRuns) {
            if (validateSubmissionXML(xmlSchemaFile, thisFile.getAbsolutePath())) {
                validatedRunsV.add(thisFile);
            }
        }
        // =====================================================================
        // 2) Collect Runs with same Collection
        Vector<File> corpusRunsV = runsWithSameCorpus(validatedRunsV);
        // 3) Pooling & PrintOut to Assessment Form XML file
        if (corpusRunsV.size() > 0) {
            poolSubmissionRuns(corpusRunsV);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Pooling: Produce Assessment Form Xml From Runs">
    // =========================================================================
    private Vector<File> runsWithSameCorpus(Vector<File> validatedRunsV) {
        Vector<File> myRunsV1 = new Vector<File>();
        Vector<File> myRunsV2 = new Vector<File>();
        Vector<File> myRunsV3 = new Vector<File>();
        boolean inCollections = false;
        boolean isTeAraCorpus = false;
        boolean isWikipediaCorpus = false;

        for (File thisRunFile : validatedRunsV) {
            try {
                InputStream in = null;
                String runPath = thisRunFile.getAbsolutePath();
                // 0: participant-id, 1: run-id, 2: task, 3: collection
                XMLInputFactory inputFactory = XMLInputFactory.newInstance();
                in = new FileInputStream(runPath);
                XMLStreamReader xsr = inputFactory.createXMLStreamReader(in);

                while (xsr.hasNext()) {
                    int event = xsr.getEventType();
                    if (event == XMLStreamConstants.START_ELEMENT) {
                        if (xsr.getLocalName().equals(collTags[0])) {
                            // collections
                            inCollections = true;
                        } else if (xsr.getLocalName().equals(collTags[1])) {
                            // collection
                        }
                    } else if (event == XMLStreamConstants.END_ELEMENT) {
                        if (xsr.getLocalName().equals(collTags[0])) {
                            inCollections = false;
                            break;
                        }
                    } else if (event == XMLStreamConstants.CHARACTERS) {
                        if (inCollections) {
                            if (xsr.isCharacters() && xsr.getText().equals(collectionTypes[0])) {
                                isWikipediaCorpus = true;
                            } else if (xsr.isCharacters() && xsr.getText().equals(collectionTypes[1])) {
                                isTeAraCorpus = true;
                            }
                        }
                    }
                    xsr.next();
                }
                if (isWikipediaCorpus && isTeAraCorpus) {
                    myRunsV1.add(thisRunFile);
                } else if (isWikipediaCorpus) {
                    myRunsV2.add(thisRunFile);
                } else if (isTeAraCorpus) {
                    myRunsV3.add(thisRunFile);
                }

            } catch (XMLStreamException ex) {
                poolingErrorMsg = ex.getMessage();
                Logger.getLogger(assessmentFormXml.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                poolingErrorMsg = ex.getMessage();
                Logger.getLogger(assessmentFormXml.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (myRunsV1.size() > 0 && myRunsV1.size() >= myRunsV2.size() && myRunsV1.size() >= myRunsV3.size()) {
            return myRunsV1;
        } else if (myRunsV2.size() > 0 && myRunsV2.size() >= myRunsV1.size() && myRunsV2.size() >= myRunsV3.size()) {
            return myRunsV2;
        } else if (myRunsV3.size() > 0 && myRunsV3.size() >= myRunsV1.size() && myRunsV3.size() >= myRunsV2.size()) {
            return myRunsV3;
        } else {
            if (myRunsV1.size() > 0) {
                return myRunsV1;
            } else if (myRunsV2.size() > 0) {
                return myRunsV2;
            } else if (myRunsV3.size() > 0) {
                return myRunsV3;
            } else {
                return myRunsV1;
            }
        }
    }
    // =========================================================================

    private boolean validateSubmissionXML(String xmlSchemaFile, String runFile) {
        // against XML Schema
        boolean isValid = false;
        try {
            String schemaLang = "http://www.w3.org/2001/XMLSchema";
            SchemaFactory factory = SchemaFactory.newInstance(schemaLang);
            File sFile = new File(xmlSchemaFile);
            Schema schema = factory.newSchema(sFile);
            Validator validator = schema.newValidator();
            Source source = new StreamSource(new FileReader(runFile));

            ErrorHandler myErrorHandler = new toolErrorHandler();
            validator.setErrorHandler(myErrorHandler);
            validator.validate(source);
            isValid = true;

        } catch (IOException ioex) {
            Logger.getLogger(assessmentFormXml.class.getName()).log(Level.SEVERE, null, ioex);
        } catch (SAXException saxex) {
            System.err.println(saxex.getMessage());
            Logger.getLogger(assessmentFormXml.class.getName()).log(Level.SEVERE, null, saxex);
        }
        return isValid;
    }
    // =========================================================================
    String topicTag = "topic";
    String[] topicAttrs = new String[]{"file", "name"};
    String[] collTags = new String[]{"collections", "collection"};
    String outgoingTag = "outgoing";
    String anchorTag = "anchor";
    String[] anchorAttrs = new String[]{"name", "offset", "length"};
    String toBepTag = "tobep";
    String[] toBepAttrs = new String[]{"offset"};
    String incomingTag = "incoming";
    String bepTag = "bep";
    String[] bepAttrs = new String[]{"offset"};
    String fromAnchorTag = "fromanchor";
    String[] fromAnchorAttrs = new String[]{"offset", "length", "file"};

    // =========================================================================
    private void poolSubmissionRuns(Vector<File> xmlRunsV) {
        // check TOPICs
        // 0) Declare Hashtable to store the Result
        //           Topic   -->  Outgoing/Incoming  --> Anchors    -->  SUB_Anchors  --> BEPs
        // Hashtable<String, Hashtable<String, Hashtable<String, Hashtable<String, Vector<String>>>>>
        Hashtable<String, Hashtable<String, Hashtable<String, Hashtable<String, Vector<String>>>>> poolResultHT = new Hashtable<String, Hashtable<String, Hashtable<String, Hashtable<String, Vector<String>>>>>();
        Hashtable<String, Vector<String>> anchorBEPHT = new Hashtable<String, Vector<String>>();
        // 1) Initialization: Parse 1st Submission XML into HT / id=runFileName
        // 2) Pool All Together
        // 0: participant-id, 1: run-id, 2: task, 3: collection (may contain 2)
        String[] property = new String[4];
        String firstSubmissionFile = "";
        Hashtable<String, String> topicFileNameHT = new Hashtable<String, String>();
        Hashtable<String, String> anchorPosNameHT = new Hashtable<String, String>();
        // Topic   -->  Outgoing/Incoming  --> Anchors    -->   BEPs
        Hashtable<String, Hashtable<String, Hashtable<String, Vector<String>>>> runDataHT = new Hashtable<String, Hashtable<String, Hashtable<String, Vector<String>>>>();
        for (File thisRunFile : xmlRunsV) {
            InputStream in = null;
            try {
                String runPath = thisRunFile.getAbsolutePath();
                firstSubmissionFile = runPath;
                Hashtable<String, Hashtable<String, Vector<String>>> linksHT = new Hashtable<String, Hashtable<String, Vector<String>>>();
                Hashtable<String, Vector<String>> anchorsHT = new Hashtable<String, Vector<String>>();
                Vector<String> toBepV = new Vector<String>();
                Hashtable<String, Vector<String>> bepsHT = new Hashtable<String, Vector<String>>();
                Vector<String> fromAnchorV = new Vector<String>();

                XMLInputFactory inputFactory = XMLInputFactory.newInstance();
                in = new FileInputStream(runPath);
                XMLStreamReader xsr = inputFactory.createXMLStreamReader(in);

                if (runDataHT.isEmpty()) {
                    String currLocalName = "";
                    String currTopicFile = "";
                    String currAnchor = "";
                    String currBepOffset = "";
                    String currBep = "";
                    String currAnchorSet = "";
                    while (xsr.hasNext()) {
                        int event = xsr.getEventType();
                        if (event == XMLStreamConstants.START_ELEMENT) {
                            currLocalName = xsr.getLocalName();
                            // <inexltw-submission
                            if (xsr.getLocalName().equals(docLocalName)) {
                                for (int i = 0; i < xsr.getAttributeCount(); i++) {
                                    String attrName = xsr.getAttributeLocalName(i);
                                    String attrValue = xsr.getAttributeValue(i);
                                    // 0: participant-id, 1: run-id, 2: task
                                    if (attrName.equals(docTitleTagHT.get(docLocalName)[0])) {
                                        property[0] = attrValue;
                                    } else if (attrName.equals(docTitleTagHT.get(docLocalName)[1])) {
                                        property[1] = attrValue;
                                    } else if (attrName.equals(docTitleTagHT.get(docLocalName)[2])) {
                                        property[2] = attrValue;
                                    }
                                }
                            } else if (xsr.getLocalName().equals(topicTag)) {
                                currTopicFile = "";
                                // <topic
                                for (int i = 0; i < xsr.getAttributeCount(); i++) {
                                    String attrName = xsr.getAttributeLocalName(i);
                                    String attrValue = xsr.getAttributeValue(i);
                                    // 0: file, 1:name
                                    if (attrName.equals(topicAttrs[0])) {
                                        currTopicFile = attrValue;
                                    } else if (attrName.equals(topicAttrs[1])) {
                                        topicFileNameHT.put(currTopicFile, attrValue);
                                    }
                                }
                                linksHT = new Hashtable<String, Hashtable<String, Vector<String>>>();
                            } else if (xsr.getLocalName().equals(outgoingTag)) {
                                // outgoing
                                // linksHT: outgoing --> anchorsHT: AnchorSet --> BEP Set Vector
                                anchorsHT = new Hashtable<String, Vector<String>>();
                            } else if (xsr.getLocalName().equals(anchorTag)) {
                                // outgoing: anchor
                                String thisName = "";
                                String thisOffset = "";
                                String thisLength = "";
                                for (int i = 0; i < xsr.getAttributeCount(); i++) {
                                    String attrName = xsr.getAttributeLocalName(i);
                                    String attrValue = xsr.getAttributeValue(i);
                                    if (attrName.equals(anchorAttrs[0])) {
                                        thisName = attrValue;
                                    } else if (attrName.equals(anchorAttrs[1])) {
                                        thisOffset = attrValue;
                                    } else if (attrName.equals(anchorAttrs[2])) {
                                        thisLength = attrValue;
                                    }
                                }
                                currAnchor = thisOffset + "_" + thisLength + "_" + thisName;
                                toBepV = new Vector<String>();
                            } else if (xsr.getLocalName().equals(toBepTag)) {
                                // outgoing: tobep
                                for (int i = 0; i < xsr.getAttributeCount(); i++) {
                                    String attrName = xsr.getAttributeLocalName(i);
                                    String attrValue = xsr.getAttributeValue(i);
                                    if (attrName.equals(toBepAttrs[0])) {
                                        currBepOffset = attrValue;
                                    }
                                }
                            } else if (xsr.getLocalName().equals(incomingTag)) {
                                // incoming
                                // linksHT: incoming --> bepsHT: BepSet --> FromAnchor Set Vector
                                bepsHT = new Hashtable<String, Vector<String>>();
                            } else if (xsr.getLocalName().equals(bepTag)) {
                                // incoming: bep
                                for (int i = 0; i < xsr.getAttributeCount(); i++) {
                                    String attrName = xsr.getAttributeLocalName(i);
                                    String attrValue = xsr.getAttributeValue(i);
                                    if (attrName.equals(bepAttrs[0])) {
                                        currBep = attrValue;
                                    }
                                }
                                fromAnchorV = new Vector<String>();
                            } else if (xsr.getLocalName().equals(fromAnchorTag)) {
                                // incoming: fromanchor
                                String thisAnchor = "";
                                String thisOffset = "";
                                String thisLength = "";
                                for (int i = 0; i < xsr.getAttributeCount(); i++) {
                                    String attrName = xsr.getAttributeLocalName(i);
                                    String attrValue = xsr.getAttributeValue(i);
                                    if (attrName.equals(fromAnchorAttrs[0])) {
                                        thisOffset = attrValue;
                                    } else if (attrName.equals(fromAnchorAttrs[1])) {
                                        thisLength = attrValue;
                                    } else if (attrName.equals(fromAnchorAttrs[2])) {
                                        thisAnchor = attrValue;
                                    }
                                }
                                currAnchorSet = thisOffset + "_" + thisLength + "_" + thisAnchor;
                            }
                        } else if (event == XMLStreamConstants.END_ELEMENT) {
                            if (xsr.getLocalName().equals(topicTag)) {
                                // it can be Outgoing or Incoming
                                runDataHT.put(currTopicFile, linksHT);
                            } else if (xsr.getLocalName().equals(outgoingTag)) {
                                linksHT.put(outgoingTag, anchorsHT);
                            } else if (xsr.getLocalName().equals(anchorTag)) {
                                anchorsHT.put(currAnchor, toBepV);
                            } else if (xsr.getLocalName().equals(incomingTag)) {
                                linksHT.put(incomingTag, bepsHT);
                            } else if (xsr.getLocalName().equals(bepTag)) {
                                bepsHT.put(currBep, fromAnchorV);
                            }
                        } else if (event == XMLStreamConstants.CHARACTERS) {
                            if (currLocalName.equals(collectionTag)) {
                                if (property[3] == null) {
                                    property[3] = xsr.getText();
                                } else {
                                    property[3] = property[3] + " : " + xsr.getText();
                                }
                                currLocalName = "";
                            } else if (currLocalName.equals(toBepTag)) {
                                toBepV.add(currBepOffset + "_" + xsr.getText());
                                currLocalName = "";
                            } else if (currLocalName.equals(fromAnchorTag)) {
                                // ERROR
                                log("currAnchorSet: " + currAnchorSet);
                                String[] thisCASet = currAnchorSet.split("_");
                                String newFromAnchorSet = thisCASet[0] + "_" + thisCASet[1] + "_" + xsr.getText() + "_" + thisCASet[2];
                                // END
                                fromAnchorV.add(newFromAnchorSet);
                                currLocalName = "";
                            }
                        }
                        event = xsr.next();
                    }
                } else {
                    // ---------------------------
                    Hashtable<String, Hashtable<String, Vector<String>>> currActLinkHT = new Hashtable<String, Hashtable<String, Vector<String>>>();
                    Hashtable<String, Vector<String>> myAnchorHT = new Hashtable<String, Vector<String>>();
                    Hashtable<String, Vector<String>> currBepHT = new Hashtable<String, Vector<String>>();

                    String currLocalName = "";
                    String currActTopic = "";
                    String currActLinkType = "";

                    boolean isAnchorsFullyMatched = false;
                    String matchedAnchorSet = "";
                    String currAnchorSet = "";
                    String currToBepOffset = "";

                    boolean isBepsFullyMatched = false;
                    String matchedBepSet = "";
                    String currBepSet = "";
                    String currFromAnchorSet = "";

                    while (xsr.hasNext()) {
                        int event = xsr.getEventType();
                        if (event == XMLStreamConstants.START_ELEMENT) {
                            currLocalName = xsr.getLocalName();
                            if (xsr.getLocalName().equals(docLocalName)) {
                                // <inexltw-submission
                                for (int i = 0; i < xsr.getAttributeCount(); i++) {
                                    String attrName = xsr.getAttributeLocalName(i);
                                    String attrValue = xsr.getAttributeValue(i);
                                    if (attrName.equals(docTitleTagHT.get(docLocalName)[2])) {
                                        if (!attrValue.equals(property[2])) {
                                            break;
                                        }
                                    }
                                }
                            } else if (xsr.getLocalName().equals(topicTag)) {
                                // <topic
                                for (int i = 0; i < xsr.getAttributeCount(); i++) {
                                    String attrName = xsr.getAttributeLocalName(i);
                                    String attrValue = xsr.getAttributeValue(i);
                                    if (attrName.equals(topicAttrs[0])) {
                                        currActTopic = attrValue;
                                        currActLinkHT = runDataHT.get(attrValue);
                                    }
                                }
                            } else if (xsr.getLocalName().equals(outgoingTag)) {
                                // outgoing
                                // linksHT: outgoing --> anchorsHT: AnchorSet --> BEP Set Vector
                                myAnchorHT = currActLinkHT.get(outgoingTag);
                            } else if (xsr.getLocalName().equals(anchorTag)) {
                                // outgoing: anchor
                                String thisName = "";
                                String thisOffset = "";
                                String thisLength = "";
                                for (int i = 0; i < xsr.getAttributeCount(); i++) {
                                    String attrName = xsr.getAttributeLocalName(i);
                                    String attrValue = xsr.getAttributeValue(i);
                                    if (attrName.equals(anchorAttrs[0])) {
                                        thisName = attrValue;
                                    } else if (attrName.equals(anchorAttrs[1])) {
                                        thisOffset = attrValue;
                                    } else if (attrName.equals(anchorAttrs[2])) {
                                        thisLength = attrValue;
                                    }
                                }
                                currAnchorSet = thisOffset + "_" + thisLength + "_" + thisName;
                                for (String myAnchorSet : myAnchorHT.keySet()) {
                                    isAnchorsFullyMatched = checkAnchorsMatched(currAnchorSet, myAnchorSet);
                                    if (isAnchorsFullyMatched) {
                                        matchedAnchorSet = myAnchorSet;
                                        break;
                                    }
                                }
                                toBepV = new Vector<String>();
                            } else if (xsr.getLocalName().equals(toBepTag)) {
                                // outgoing: tobep
                                for (int i = 0; i < xsr.getAttributeCount(); i++) {
                                    String attrName = xsr.getAttributeLocalName(i);
                                    String attrValue = xsr.getAttributeValue(i);
                                    if (attrName.equals(toBepAttrs[0])) {
                                        currToBepOffset = attrValue;
                                    }
                                }
                            } else if (xsr.getLocalName().equals(incomingTag)) {
                                // incoming
                                // linksHT: incoming --> bepsHT: BepSet --> FromAnchor Set Vector
                                currBepHT = currActLinkHT.get(incomingTag);
                            } else if (xsr.getLocalName().equals(bepTag)) {
                                // incoming: bep
                                for (int i = 0; i < xsr.getAttributeCount(); i++) {
                                    String attrName = xsr.getAttributeLocalName(i);
                                    String attrValue = xsr.getAttributeValue(i);
                                    if (attrName.equals(bepAttrs[0])) {
                                        currBepSet = attrValue;
                                    }
                                }
                                for (String myBepSet : currBepHT.keySet()) {
                                    if (currBepSet.equals(myBepSet)) {
                                        isBepsFullyMatched = true;
                                        matchedBepSet = myBepSet;
                                        break;
                                    }
                                }
                                fromAnchorV = new Vector<String>();
                            } else if (xsr.getLocalName().equals(fromAnchorTag)) {
                                // incoming: fromanchor
                                String thisAnchor = "";
                                String thisOffset = "";
                                String thisLength = "";
                                for (int i = 0; i < xsr.getAttributeCount(); i++) {
                                    String attrName = xsr.getAttributeLocalName(i);
                                    String attrValue = xsr.getAttributeValue(i);
                                    if (attrName.equals(fromAnchorAttrs[0])) {
                                        thisOffset = attrValue;
                                    } else if (attrName.equals(fromAnchorAttrs[1])) {
                                        thisLength = attrValue;
                                    } else if (attrName.equals(fromAnchorAttrs[2])) {
                                        thisAnchor = attrValue;
                                    }
                                }
                                currFromAnchorSet = thisOffset + "_" + thisLength + "_" + thisAnchor;
                            }
                        } else if (event == XMLStreamConstants.END_ELEMENT) {
                            if (xsr.getLocalName().equals(topicTag)) {
                                // it can be Outgoing or Incoming
                                runDataHT.remove(currActTopic);
                                runDataHT.put(currActTopic, currActLinkHT);
                            } else if (xsr.getLocalName().equals(outgoingTag)) {
                                currActLinkHT.remove(outgoingTag);
                                currActLinkHT.put(outgoingTag, myAnchorHT);
                            } else if (xsr.getLocalName().equals(anchorTag)) {
                                if (isAnchorsFullyMatched) {
                                    myAnchorHT = getNewCurrAnchorHT(toBepV, matchedAnchorSet, myAnchorHT);
                                    matchedAnchorSet = "";
                                } else {
                                    myAnchorHT.put(currAnchorSet, toBepV);
                                }
                                isAnchorsFullyMatched = false;
                            } else if (xsr.getLocalName().equals(incomingTag)) {
                                currActLinkHT.remove(incomingTag);
                                currActLinkHT.put(incomingTag, currBepHT);
                            } else if (xsr.getLocalName().equals(bepTag)) {
                                if (isBepsFullyMatched) {
                                    currBepHT = getNewCurrBepHT(fromAnchorV, matchedBepSet, currBepHT);
                                    matchedBepSet = "";
                                } else {
                                    currBepHT.put(currBepSet, fromAnchorV);
                                }
                                isBepsFullyMatched = false;
                            }
                        } else if (event == XMLStreamConstants.CHARACTERS) {
                            if (currLocalName.equals(toBepTag)) {
                                toBepV.add(currToBepOffset + "_" + xsr.getText());
                                currLocalName = "";
                            } else if (currLocalName.equals(fromAnchorTag)) {
                                // ERROR
                                String[] thisCASet = currAnchorSet.split("_");
                                String newFromAnchorSet = thisCASet[0] + "_" + thisCASet[1] + "_" + xsr.getText() + "_" + thisCASet[2];
                                // END
                                fromAnchorV.add(newFromAnchorSet);
                                currLocalName = "";
                            }
                        }
                        event = xsr.next();
                    }
                }
            } catch (XMLStreamException ex) {
                poolingErrorMsg = ex.getMessage();
                Logger.getLogger(assessmentFormXml.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                poolingErrorMsg = ex.getMessage();
                Logger.getLogger(assessmentFormXml.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    in.close();
                } catch (IOException ex) {
                    poolingErrorMsg = ex.getMessage();
                    Logger.getLogger(assessmentFormXml.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        // Pooling
        poolResultHT = poolSubmissionData(runDataHT);
        // convert it into Assessment Form XML
        produceAssessmentFormXml(firstSubmissionFile, property, poolResultHT, topicFileNameHT);

        isPoolingDone = true;
    }
    // =========================================================================

    private Hashtable<String, Hashtable<String, Hashtable<String, Hashtable<String, Vector<String>>>>> poolSubmissionData(Hashtable<String, Hashtable<String, Hashtable<String, Vector<String>>>> runDataHT) {
        // "runDataHT" Format:
        // Hashtable: Topic<String> --> Hashtable: outgoing/incoming<String>
        // --> Hashtable: anchor<String>
        // --> Vector<String>: tobepOffset_fileName
        // =====================================================================
        // "myNewPoolDataHT" Format:
        // Hashtable: Topic<String> --> Hashtable: outgoing/incoming<String>
        // --> Hashtable: anchor<String>
        // --> Hashtable: sub-Anchor<String> --> Vector<String>: tobepOffset_fileName
        String currTopic = "";
        String[] linkTypes = new String[]{"outgoing", "incoming"};

        Hashtable<String, Hashtable<String, Hashtable<String, Hashtable<String, Vector<String>>>>> myNewPoolDataHT = new Hashtable<String, Hashtable<String, Hashtable<String, Hashtable<String, Vector<String>>>>>();
        Hashtable<String, Hashtable<String, Hashtable<String, Vector<String>>>> newLinkingTypesHT = new Hashtable<String, Hashtable<String, Hashtable<String, Vector<String>>>>();
        Hashtable<String, Hashtable<String, Vector<String>>> newAnchorBEPsHT = new Hashtable<String, Hashtable<String, Vector<String>>>();
        for (String thisTopic : runDataHT.keySet()) {
            // 1) Topic
            currTopic = thisTopic;
            Hashtable<String, Hashtable<String, Vector<String>>> linkingTypeHT = runDataHT.get(currTopic);
            newLinkingTypesHT = new Hashtable<String, Hashtable<String, Hashtable<String, Vector<String>>>>();
            // add Anchor on the Top --> then Sub-Anchor
            for (String linkingType : linkTypes) {
                // outgoing --> incoming
                // get Anchor & its BEPs Set
                if (linkingType.equals("outgoing")) {
                    newAnchorBEPsHT = new Hashtable<String, Hashtable<String, Vector<String>>>();
                    if (linkingTypeHT.containsKey(linkingType)) {
                        Hashtable<String, Vector<String>> anchorBEPSetHT = linkingTypeHT.get(linkingType);
                        Vector thisAnchorV = new Vector(Arrays.asList(anchorBEPSetHT.keySet().toArray()));
                        Vector<Object> DONEAnchorKeysV = new Vector<Object>();
                        for (int i = 0; i < thisAnchorV.size(); i++) {
                            Object thisAnchorKey = thisAnchorV.elementAt(i);
                            if (!DONEAnchorKeysV.contains(thisAnchorKey)) {
                                String[] thisAnchorSet = thisAnchorV.elementAt(i).toString().split("_");
                                int thisSOffset = Integer.valueOf(thisAnchorSet[0]);
                                int thisEOffset = thisSOffset + Integer.valueOf(thisAnchorSet[1]);
                                String thisAnchorText = thisAnchorSet[2];
                                int startOffset = thisSOffset;
                                int endOffset = thisEOffset;
                                String topAnchorText = thisAnchorText;
                                // ---------------------------------------------------------
                                Vector<Object> matchedAnchorKeysV = new Vector<Object>();
                                for (int j = i + 1; j < thisAnchorV.size(); j++) {
                                    Object nextAnchorKey = thisAnchorV.elementAt(j);
                                    String[] nextAnchorSet = thisAnchorV.elementAt(j).toString().split("_");
                                    int nextSOffset = Integer.valueOf(nextAnchorSet[0]);
                                    int nextEOffset = nextSOffset + Integer.valueOf(nextAnchorSet[1]);
                                    String nextAnchorText = nextAnchorSet[2];
                                    if ((nextSOffset >= startOffset) && (nextSOffset <= endOffset)) {
                                        if (nextEOffset >= endOffset) {
                                            topAnchorText += nextAnchorText.substring((nextEOffset - endOffset), nextAnchorText.length());
                                            endOffset = nextEOffset;
                                        }
                                        matchedAnchorKeysV.add(nextAnchorKey);
                                        DONEAnchorKeysV.add(nextAnchorKey);
                                    } else if ((nextEOffset >= startOffset) && (nextEOffset <= endOffset)) {
                                        if (nextSOffset <= startOffset) {
                                            topAnchorText = nextAnchorText.substring(0, (startOffset - nextSOffset)) + topAnchorText;
                                            startOffset = nextSOffset;
                                        }
                                        matchedAnchorKeysV.add(nextAnchorKey);
                                        DONEAnchorKeysV.add(nextAnchorKey);
                                    }
                                }
                                // ---------------------------------------------------------
                                if (!matchedAnchorKeysV.isEmpty()) {
                                    // put extended Anchor on the Top
                                    // a) Loop matched AnchorSet
                                    Hashtable<String, Vector<String>> newSubAnchorBEPsHT = new Hashtable<String, Vector<String>>();
                                    Vector<String> thisMBepsV = anchorBEPSetHT.get(thisAnchorKey);
                                    newSubAnchorBEPsHT.put(thisAnchorKey.toString(), thisMBepsV);
                                    for (Object matchedAnchor : matchedAnchorKeysV) {
                                        Vector<String> thisMatchedBepsV = anchorBEPSetHT.get(matchedAnchor);
                                        newSubAnchorBEPsHT.put(matchedAnchor.toString(), thisMatchedBepsV);
                                    }
                                    newAnchorBEPsHT.put(startOffset + "_" + (endOffset - startOffset) + "_" + topAnchorText, newSubAnchorBEPsHT);
                                // b) Delete Matched AnchorKeys to eliminate duplicated Looping
//                            thisAnchorV.remove(thisAnchorKey);
//                            for (Object matchedAnchor : matchedAnchorKeysV) {
//                                thisAnchorV.remove(matchedAnchor);
//                            }
                                } else {
                                    // no matched: self Top Anchor
                                    // a) get this BEP links
                                    Vector<String> thisNewBepsV = anchorBEPSetHT.get(thisAnchorKey);
                                    Hashtable<String, Vector<String>> newSubAnchorBEPsHT = new Hashtable<String, Vector<String>>();
                                    newSubAnchorBEPsHT.put(thisAnchorKey.toString(), thisNewBepsV);
                                    newAnchorBEPsHT.put(thisAnchorKey.toString(), newSubAnchorBEPsHT);
                                }
                            }
                        }
                    }
                    newLinkingTypesHT.put(linkingType, newAnchorBEPsHT);
                } else if (linkingType.equals("incoming")) {
                    // nothing with Anchor Presenter Case
                    // so JUST add in a sub-BEP level
                    newAnchorBEPsHT = new Hashtable<String, Hashtable<String, Vector<String>>>();
                    if (linkingTypeHT.containsKey(linkingType)) {
                        Hashtable<String, Vector<String>> bepFromAnchorSetHT = linkingTypeHT.get(linkingType);
                        for (String thisBEP : bepFromAnchorSetHT.keySet()) {
                            Vector<String> fromAnchorV = bepFromAnchorSetHT.get(thisBEP);
                            Hashtable<String, Vector<String>> newFromAnchorsHT = new Hashtable<String, Vector<String>>();
                            newFromAnchorsHT.put(thisBEP, fromAnchorV);
                            newAnchorBEPsHT.put(thisBEP, newFromAnchorsHT);
                        }
                    }
                    newLinkingTypesHT.put(linkingType, newAnchorBEPsHT);
                }
            }
            myNewPoolDataHT.put(currTopic, newLinkingTypesHT);
        }

        return myNewPoolDataHT;
    }
    // =========================================================================

    private void produceAssessmentFormXml(String submissionFile, String[] property, Hashtable<String, Hashtable<String, Hashtable<String, Hashtable<String, Vector<String>>>>> poolResultHT, Hashtable<String, String> topicFileNameHT) {
        try {
            Hashtable<String, String[]> docElmnsHT = new Hashtable<String, String[]>();
            String docTitleTag = "inexltw-assessment";
            docElmnsHT.put(docTitleTag, new String[]{"participant-id", "run-id", "task"});
            String outgoingLinkTag = "outgoinglinks";
            Hashtable<String, String[]> outgoingElmnsHT = new Hashtable<String, String[]>();
            outgoingElmnsHT.put(outgoingLinkTag, new String[]{"anchor", "subanchor", "tobep"});
            Hashtable<String, String[]> anchorElmnsHT = new Hashtable<String, String[]>();
            anchorElmnsHT.put(outgoingElmnsHT.get(outgoingLinkTag)[0], new String[]{"arel", "aname", "aoffset", "alength"});
            Hashtable<String, String[]> subanchorElmnsHT = new Hashtable<String, String[]>();
            subanchorElmnsHT.put(outgoingElmnsHT.get(outgoingLinkTag)[1], new String[]{"sarel", "saname", "saoffset", "salength"});
            Hashtable<String, String[]> tobepElmnsHT = new Hashtable<String, String[]>();
            tobepElmnsHT.put(outgoingElmnsHT.get(outgoingLinkTag)[2], new String[]{"tbrel", "timein", "timeout", "tboffset"});
            String incomingLinkTag = "incominglinks";
            Hashtable<String, String[]> incomingElmnsHT = new Hashtable<String, String[]>();
            incomingElmnsHT.put(incomingLinkTag, new String[]{"bep", "fromanchor"});
            Hashtable<String, String[]> bepElmnsHT = new Hashtable<String, String[]>();
            bepElmnsHT.put(incomingElmnsHT.get(incomingLinkTag)[0], new String[]{"borel", "boffset"});
            Hashtable<String, String[]> fromAnchorElmnsHT = new Hashtable<String, String[]>();
            fromAnchorElmnsHT.put(incomingElmnsHT.get(incomingLinkTag)[1], new String[]{"farel", "faoffset", "falength", "faanchor"});
            OutputStream out = null;
            // 1) get Traget File Name & Path
            String fileName = "";
            String fileDir = "";
            if (submissionFile.lastIndexOf(File.separator) >= 0) {
                fileName = submissionFile.substring(submissionFile.lastIndexOf(File.separator) + 1, submissionFile.length());
                fileDir = submissionFile.substring(0, submissionFile.lastIndexOf(File.separator));
            } else if (submissionFile.lastIndexOf(File.separator) >= 0) {
                fileName = submissionFile.substring(submissionFile.lastIndexOf(File.separator) + 1, submissionFile.length());
                fileDir = submissionFile.substring(0, submissionFile.lastIndexOf(File.separator));
            }
            String currDateTimeStr = getCurrDateTime();
//            String AFXmlFile = xmlAssessXmlFileFolder + "VerificationXmlForm_" + currDateTimeStr + ".xml";
            // -----------------------------------------------------------------
            xmlAssessXmlFileName = AFXmlFile;
            // Update Tool Resource XML --> targetFile
            new resourcesManager().updateAFXmlFile(AFXmlFile);
            // -----------------------------------------------------------------
            // 2) create XML writer
            out = new FileOutputStream(AFXmlFile);
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = factory.createXMLStreamWriter(out);
            // document Title
            writer.writeStartElement(docTitleTag);
            for (int i = 0; i < property.length - 1; i++) {
                writer.writeAttribute(docElmnsHT.get(docTitleTag)[i], property[i]);
            }
            // Collection
            writer.writeStartElement(collectionTag);
            writer.writeCharacters(property[property.length - 1]);
            writer.writeEndElement();
            // Topic Main Content
            // outgoing
            // linksHT: outgoing --> anchorsHT: AnchorSet --> BEP Set Vector
            // incoming
            // linksHT: incoming --> bepsHT: BepSet --> FromAnchor Set Vector
            Enumeration topicKeyEnu = poolResultHT.keys();
            while (topicKeyEnu.hasMoreElements()) {
                Object topicKey = topicKeyEnu.nextElement();
                String topicFile = topicKey.toString();
                String topicName = topicFileNameHT.get(topicKey);
                // a) Topic
                writer.writeStartElement(topicTag);
                writer.writeAttribute(topicAttrs[0], topicFile);
                writer.writeAttribute(topicAttrs[1], topicName);
                Hashtable<String, Hashtable<String, Hashtable<String, Vector<String>>>> linkingTypesHT = poolResultHT.get(topicKey);
                Vector<String> linkTypesV = new Vector<String>();
                linkTypesV.add(outgoingTag);
                linkTypesV.add(incomingTag);
                for (String thisLinkType : linkTypesV) {
                    if (thisLinkType.equals(outgoingTag)) {
                        // b) outgoinglinks
                        writer.writeStartElement(outgoingLinkTag);
                        Hashtable<String, Hashtable<String, Vector<String>>> outgoingHT = linkingTypesHT.get(thisLinkType);
                        Enumeration anchorKeyEnu = outgoingHT.keys();
                        while (anchorKeyEnu.hasMoreElements()) {
                            Object anchorKey = anchorKeyEnu.nextElement();
                            String[] anchorSet = anchorKey.toString().split("_");
                            String anchorName = anchorKey.toString().substring(anchorSet[0].length() + 1 + anchorSet[1].length() + 1, anchorKey.toString().length());
                            // c) anchor
                            writer.writeStartElement(outgoingElmnsHT.get(outgoingLinkTag)[0]);
                            writer.writeAttribute(anchorElmnsHT.get(outgoingElmnsHT.get(outgoingLinkTag)[0])[0], "");
                            writer.writeAttribute(anchorElmnsHT.get(outgoingElmnsHT.get(outgoingLinkTag)[0])[1], anchorName);
                            writer.writeAttribute(anchorElmnsHT.get(outgoingElmnsHT.get(outgoingLinkTag)[0])[2], anchorSet[0]);
                            writer.writeAttribute(anchorElmnsHT.get(outgoingElmnsHT.get(outgoingLinkTag)[0])[3], anchorSet[1]);

                            Hashtable<String, Vector<String>> subAnchorBEPsHT = outgoingHT.get(anchorKey);
                            for (String thisSubAnchor : subAnchorBEPsHT.keySet()) {
                                String[] subAnchorSet = thisSubAnchor.toString().split("_");
                                String subAnchorName = thisSubAnchor.toString().substring(subAnchorSet[0].length() + 1 + subAnchorSet[1].length() + 1, thisSubAnchor.toString().length());
                                // d) Sub-anchor
                                writer.writeStartElement(outgoingElmnsHT.get(outgoingLinkTag)[1]);
                                writer.writeAttribute(subanchorElmnsHT.get(outgoingElmnsHT.get(outgoingLinkTag)[1])[0], "");
                                writer.writeAttribute(subanchorElmnsHT.get(outgoingElmnsHT.get(outgoingLinkTag)[1])[1], subAnchorName);
                                writer.writeAttribute(subanchorElmnsHT.get(outgoingElmnsHT.get(outgoingLinkTag)[1])[2], subAnchorSet[0]);
                                writer.writeAttribute(subanchorElmnsHT.get(outgoingElmnsHT.get(outgoingLinkTag)[1])[3], subAnchorSet[1]);

                                Vector<String> bepsV = subAnchorBEPsHT.get(thisSubAnchor);
                                for (String thisBEP : bepsV) {
                                    String[] bepSet = thisBEP.toString().split("_");
                                    String bepFileName = thisBEP.toString().substring(bepSet[0].length() + 1, thisBEP.toString().length());
                                    // e) BEP
                                    writer.writeStartElement(outgoingElmnsHT.get(outgoingLinkTag)[2]);
                                    writer.writeAttribute(tobepElmnsHT.get(outgoingElmnsHT.get(outgoingLinkTag)[2])[0], "");
                                    writer.writeAttribute(tobepElmnsHT.get(outgoingElmnsHT.get(outgoingLinkTag)[2])[1], "");
                                    writer.writeAttribute(tobepElmnsHT.get(outgoingElmnsHT.get(outgoingLinkTag)[2])[2], "");
                                    writer.writeAttribute(tobepElmnsHT.get(outgoingElmnsHT.get(outgoingLinkTag)[2])[3], bepSet[0]);
                                    writer.writeCharacters(bepFileName);
                                    // END of e) BEP
                                    writer.writeEndElement();
                                }
                                // END of d) Sub-anchor
                                writer.writeEndElement();
                            }
                            // END of c) anchor
                            writer.writeEndElement();
                        }
                        // END of b) outgoinglinks
                        writer.writeEndElement();
                    } else if (thisLinkType.equals(incomingTag)) {
                        // b) incominglinks
                        if (linkingTypesHT.containsKey(thisLinkType)) {
                            writer.writeStartElement(incomingLinkTag);
                            Hashtable<String, Hashtable<String, Vector<String>>> incomingHT = linkingTypesHT.get(thisLinkType);
                            Enumeration bepKeyEnu = incomingHT.keys();
                            while (bepKeyEnu.hasMoreElements()) {
                                Object bepKey = bepKeyEnu.nextElement();
                                // eliminate the TOP BEP Elmn
                                Hashtable<String, Vector<String>> topBEPHT = incomingHT.get(bepKey);
                                // --------------------------
                                for (String myBepOffset : topBEPHT.keySet()) {
                                    String bepOffset = myBepOffset;
                                    // c) bep
                                    writer.writeStartElement(incomingElmnsHT.get(incomingLinkTag)[0]);
                                    writer.writeAttribute(bepElmnsHT.get(incomingElmnsHT.get(incomingLinkTag)[0])[0], "");
                                    writer.writeAttribute(bepElmnsHT.get(incomingElmnsHT.get(incomingLinkTag)[0])[1], bepOffset);

                                    Vector<String> fromAnchorV = topBEPHT.get(bepOffset);
                                    for (String thisFromAnchor : fromAnchorV) {
                                        String[] fromAnchorSet = thisFromAnchor.toString().split("_");
                                        // d) fromanchor
                                        writer.writeStartElement(incomingElmnsHT.get(incomingLinkTag)[1]);
                                        writer.writeAttribute(fromAnchorElmnsHT.get(incomingElmnsHT.get(incomingLinkTag)[1])[0], "");
                                        writer.writeAttribute(fromAnchorElmnsHT.get(incomingElmnsHT.get(incomingLinkTag)[1])[1], fromAnchorSet[0]);
                                        writer.writeAttribute(fromAnchorElmnsHT.get(incomingElmnsHT.get(incomingLinkTag)[1])[2], fromAnchorSet[1]);
                                        writer.writeAttribute(fromAnchorElmnsHT.get(incomingElmnsHT.get(incomingLinkTag)[1])[3], fromAnchorSet[2]);
                                        writer.writeCharacters(fromAnchorSet[3]);

                                        // END of d) fromanchor
                                        writer.writeEndElement();
                                    }
                                    // END of c) bep
                                    writer.writeEndElement();
                                }
                            }
                            // END of d) incominglinks
                            writer.writeEndElement();
                        }
                    }
                }
                // END of a) Topic
                writer.writeEndElement();
            }
            // END of Document Title
            writer.writeEndElement();
            writer.flush();
            writer.close();
        } catch (XMLStreamException ex) {
            poolingErrorMsg = ex.getMessage();
            Logger.getLogger(assessmentFormXml.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            poolingErrorMsg = ex.getMessage();
            Logger.getLogger(assessmentFormXml.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    // =========================================================================

    private boolean checkAnchorsMatched(String currAnchorSet, String myAnchorSet) {
        boolean matchedPairs = false;

        String[] myAnchorSA = myAnchorSet.split("_");
        int startOffset = Integer.valueOf(myAnchorSA[0]);
        int endOffset = startOffset + Integer.valueOf(myAnchorSA[1]);

        String[] currAnchorSA = currAnchorSet.split("_");
        int mySOffset = Integer.valueOf(currAnchorSA[0]);
        int myEOffset = mySOffset + Integer.valueOf(currAnchorSA[1]);

        if (mySOffset == startOffset && myEOffset == endOffset) {
            matchedPairs = true;
        }
        return matchedPairs;
    }
    // =========================================================================

    private Hashtable<String, Vector<String>> getNewCurrAnchorHT(Vector<String> newToBepV, String myAnchorSet, Hashtable<String, Vector<String>> currAnchorHT) {
        Vector<String> thisToBepV = currAnchorHT.get(myAnchorSet);
        for (String newToBep : newToBepV) {
            if (!thisToBepV.contains(newToBep)) {
                thisToBepV.add(newToBep);
            }
        }
        currAnchorHT.remove(myAnchorSet);
        currAnchorHT.put(myAnchorSet, thisToBepV);
        return currAnchorHT;
    }
    // =========================================================================

    private Hashtable<String, Vector<String>> getNewCurrBepHT(Vector<String> newFromAnchorV, String myBepSet, Hashtable<String, Vector<String>> currBepHT) {
        Vector<String> thisFromAnchorV = currBepHT.get(myBepSet);
        for (String newFromAnchor : newFromAnchorV) {
            if (!thisFromAnchorV.contains(newFromAnchor)) {
                thisFromAnchorV.add(newFromAnchor);
            }
        }
        currBepHT.remove(myBepSet);
        currBepHT.put(myBepSet, thisFromAnchorV);
        return currBepHT;
    }
    // =========================================================================

    private String getCurrDateTime() {
        String currDateTime = "";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdate = new SimpleDateFormat("MMdd");  // 0915
        String myDate = sdate.format(cal.getTime());
        SimpleDateFormat stime = new SimpleDateFormat("H:mm:ss");   // 9:41:30
        String myTime = stime.format(cal.getTime());
        String[] timeSet = myTime.split(":");
        return currDateTime = myDate + timeSet[0] + timeSet[1] + timeSet[2];
    }
    // =========================================================================
    // </editor-fold>
}
