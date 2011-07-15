package crosslink.measures;

import java.io.File;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import crosslink.resultsetGenerator.LtwResultsetType;

import ltwassessment.utility.WildcardFiles;

public class ResultSetManager {
	
	public static final int A2F_WIKI_GROUNDTRUTH = 0;
	public static final int A2B_WIKI_GROUNDTRUTH = 1;
	public static final int A2B_WIKI_MANUAL = 2;
	
	private static final String[] resultSetNameArray = {"A2FWikiGroundTruthResultSet", "A2FWikiGroundTruthResultSet", "A2BWikiManualResultSet"};
	
	private int evaluationType = A2F_WIKI_GROUNDTRUTH;
	
	private static final String RESULTSET_PARENT_PATH = "resultsets";
	private static final String RESULTSET_MANUAL_NAME = "Manual";
	private static final String RESULTSET_GROUNDTRUTH_NAME = "GroundTruth";
	
	private StringBuffer resultsetPath = new StringBuffer();
	
	private static ResultSetManager instance = null;
	
	private Map<String, String> langMap = new HashMap<String, String>();
	
	private Map<String, Hashtable> resultsetLinks = Collections.synchronizedMap(new HashMap<String, Hashtable>());
	private Map<String, Hashtable> resultsets = Collections.synchronizedMap(new HashMap<String, Hashtable>());
	private Map<String, Hashtable> resultsetLinksNo = Collections.synchronizedMap(new HashMap<String, Hashtable>());
	
	public ResultSetManager() {
		setResultSetPath();
		setLangMap();
	}

	public static ResultSetManager getInstance() {
		if (instance == null)
			instance = new ResultSetManager();
		return instance;
	}
	
    private static void log(Object aObject) {
        System.out.println(String.valueOf(aObject));
    }

    private static void errlog(Object aObject) {
        System.err.println(String.valueOf(aObject));
    }
	
	private void setLangMap() {
		langMap.put("en", "E");
		langMap.put("ko", "K");
		langMap.put("ja", "J");
		langMap.put("zh", "C");
	}
	
	public void setResultSetPath() {
//        String resultsetPath = ;
//        
        resultsetPath.append(RESULTSET_PARENT_PATH  + File.separator);
        
        String resultsetFormalPath = resultsetPath.toString() + "formal";
        if (!new File(resultsetFormalPath).exists())
        	resultsetPath.append("training");
        else
        	resultsetPath.append("formal");
        
        resultsetPath.append(File.separator);
	}
	
    /**
	 * @return the eveluationType
	 */
	public int getEveluationType() {
		return evaluationType;
	}

	/**
	 * @param eveluationType the eveluationType to set
	 */
	public void setEveluationType(int evaluationType) {
		this.evaluationType = evaluationType;
	}

	public boolean checkIfManualResultSetAvailable() {
		String manulResultSetWildcard = resultsetPath + "*" + RESULTSET_MANUAL_NAME + "*.xml";
		return WildcardFiles.listFilesInStack(manulResultSetWildcard).size() > 0;
	}
	
	public String getResultSetPathFile(String sourceLang, String targetLang) {
//        String resultSetType = "";
        String resultSetFile = null;

        resultSetFile = String.format("%s%s-%s2%s.xml", resultsetPath.toString(), resultSetNameArray[evaluationType], langMap.get(sourceLang), langMap.get(targetLang));
        return resultSetFile;
    }
	
    private static Hashtable ResultSetLinksNo(File resultfiles) {
        Hashtable resultLinksTable = new Hashtable();
        try {
            JAXBContext jc;
            jc = JAXBContext.newInstance("crosslink.resultsetGenerator");
            Unmarshaller um = jc.createUnmarshaller();
            LtwResultsetType lrs = (LtwResultsetType) ((um.unmarshal(resultfiles)));

            if (lrs.getLtwTopic().size() > 0) {
                for (int i = 0; i < lrs.getLtwTopic().size(); i++) {

                    int inCount = 0;
                    int outCount = 0;

                    String topicID = lrs.getLtwTopic().get(i).getId();

                    log("topicID: " + topicID);

                    if (lrs.getLtwTopic().get(i).getOutgoingLinks().getOutLink().isEmpty()) {
                    } else {
                        String[] outLinks = new String[lrs.getLtwTopic().get(i).getOutgoingLinks().getOutLink().size()];
                        for (int j = 0; j < lrs.getLtwTopic().get(i).getOutgoingLinks().getOutLink().size(); j++) {
                            outCount++;
                        }
                    }
//                    if (lrs.getLtwTopic().get(i).getIncomingLinks().getInLink().isEmpty()) {
//                    } else {
//                        String[] inLinks = new String[lrs.getLtwTopic().get(i).getIncomingLinks().getInLink().size()];
//                        for (int k = 0; k < lrs.getLtwTopic().get(i).getIncomingLinks().getInLink().size(); k++) {
//                            inCount++;
//                        }
//                    }

                    resultLinksTable.put(topicID/* + ".xml"*/, outCount + ";" + inCount);
                    log("Outgoing Links: " + outCount);
                    log("Incoming Links: " + inCount);
                }
            }

        } catch (JAXBException ex) {
            ex.printStackTrace();
        }

        return resultLinksTable;
    }
    
	public Hashtable getResultSetLinks(String resultSetPathFile) {
		Hashtable table = resultsetLinks.get(resultSetPathFile);
		if (table == null) {
			table = loadResultSetLinks(resultSetPathFile);
		}
		return table;
	}
	
	public Hashtable getResultSetLinks(String sourceLang, String targetLang) {
		String resultSetPathFile = getResultSetPathFile(sourceLang, targetLang);
		return getResultSetLinks(resultSetPathFile);
	}
	
	public Hashtable getResultSetLinksNo(String resultSetPathFile) {
		Hashtable table = resultsetLinksNo.get(resultSetPathFile);
		if (table == null) {
			loadResultSetLinks(resultSetPathFile);
			table = resultsetLinksNo.get(resultSetPathFile);
		}
		return table;
	}
	
	public Hashtable getResultSetLinksNo(String sourceLang, String targetLang) {
		String resultSetPathFile = getResultSetPathFile(sourceLang, targetLang);
		return getResultSetLinksNo(resultSetPathFile);
	}

	public void loadResultSetLinks(String sourceLang, String targetLang) {
		String resultSetPathFile = getResultSetPathFile(sourceLang, targetLang);
		loadResultSetLinks(resultSetPathFile);
	}
	
	private Hashtable loadResultSetLinks(String resultSetPathFile) {
		
        Hashtable resultTable = new Hashtable();
        Hashtable resultLinksTable = new Hashtable();
        try {
            JAXBContext jc;
            jc = JAXBContext.newInstance("crosslink.resultsetGenerator");
            Unmarshaller um = jc.createUnmarshaller();
            LtwResultsetType lrs = (LtwResultsetType) ((um.unmarshal(new File(resultSetPathFile))));

            if (lrs.getLtwTopic().size() > 0) {
                for (int i = 0; i < lrs.getLtwTopic().size(); i++) {

                    int inCount = 0;
                    int outCount = 0;

                    String topicID = lrs.getLtwTopic().get(i).getId().trim();

                    String[] outLinks = null;
	                String[] emptyLinks = {""};
                    if (lrs.getLtwTopic().get(i).getOutgoingLinks().getOutLink().isEmpty()) {
                        outLinks = emptyLinks;
                    } else {
                        Vector outLinksV = new Vector();
                        for (int j = 0; j < lrs.getLtwTopic().get(i).getOutgoingLinks().getOutLink().size(); j++) {
                            String outLinkStr = lrs.getLtwTopic().get(i).getOutgoingLinks().getOutLink().get(j).getValue().toString().trim();
                            if (!outLinksV.contains(outLinkStr)) {
                                outLinksV.add(outLinkStr);
                            }
                        }
                        outLinks = new String[outLinksV.size()];
                        Enumeration oEnu = outLinksV.elements();
                        while (oEnu.hasMoreElements()) {
                            Object obj = oEnu.nextElement();
                            outLinks[outCount] = obj.toString().trim();
                            outCount++;
                        }
                        resultTable.put(topicID + "_" + Data.outgoingTag, outLinks);
                    }
                    
                    resultTable.put(topicID + "_" + Data.outgoingTag, outLinks);
	                resultTable.put(topicID + "_" + Data.incomingTag, emptyLinks);
	                resultLinksTable.put(topicID/* + ".xml"*/, outCount + ";" + inCount);
//                    if (lrs.getLtwTopic().get(i).getIncomingLinks().getInLink().isEmpty()) {
//                        String[] inLinks = {""};
//                        resultTable.put(topicID + "_" + metricsCalculation.incomingTag, inLinks);
//                    } else {
//                        Vector inLinksV = new Vector();
//                        for (int k = 0; k < lrs.getLtwTopic().get(i).getIncomingLinks().getInLink().size(); k++) {
//                            String inLinkStr = lrs.getLtwTopic().get(i).getIncomingLinks().getInLink().get(k).getValue().toString().trim();
//                            if (!inLinksV.contains(inLinkStr)) {
//                                inLinksV.add(inLinkStr);
//                            }
//                        }
//                        String[] inLinks = new String[inLinksV.size()];
//                        Enumeration iEnu = inLinksV.elements();
//                        while (iEnu.hasMoreElements()) {
//                            Object obj = iEnu.nextElement();
//                            inLinks[inCount] = obj.toString().trim();
//                            inCount++;
//                        }
//                        resultTable.put(topicID + "_" + metricsCalculation.incomingTag, inLinks);
//                    }
                }
            }

        } catch (JAXBException ex) {
            ex.printStackTrace();
        }
        
        resultsetLinks.put(resultSetPathFile, resultTable);
        resultsetLinksNo.put(resultSetPathFile, resultLinksTable);
        return resultTable;
	}
	
	public void addResultSet(String resultfile, Hashtable resultTable) {
		resultsets.put(resultfile, resultTable);
	}
	
	public Hashtable getResultSet(String resultfile) {
		return resultsets.get(resultfile);
	}
}
