package crosslink.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import crosslink.measures.Data;
import crosslink.resultsetGenerator.LtwResultsetType;
import crosslink.resultsetGenerator.OutLink;
import crosslink.tools.UniqLinks.Team;

public class ResultSet {

	HashMap<String, HashSet<String>>		linkSet = new HashMap<String, HashSet<String>>(); // link, participant id
	HashMap<String, HashSet<String>> anchorSet = new HashMap<String, HashSet<String>>(); //key = topicid_anchoroffset_anchor_length
	
	public boolean containLink(String link) {
		return linkSet.containsKey(link);
	}
	
	public void addLinkParticipantId(String link, String id) {
		if (!linkSet.get(link).contains(id))
			linkSet.get(link).add(id);
	}
	
	public boolean containTopicAnchor(String anchor) {
		return anchorSet.containsKey(anchor);
	}
	
	public void addAnchorParticipantId(String anchor, String id) {
		if (!anchorSet.get(anchor).contains(id))
			anchorSet.get(anchor).add(id);
	}
	
	public void load(String resultSetPathFile) {
		
//        Hashtable resultTable = new Hashtable();
//        Hashtable resultLinksTable = new Hashtable();
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

//                    HashSet<String> anchorsSet = new HashSet<String>();
//                    anchorSet.put(topicID, anchorsSet);
                    
                    String[] outLinks = null;
	                String[] emptyLinks = {""};
                    if (lrs.getLtwTopic().get(i).getOutgoingLinks().getOutLink().isEmpty()) {
                        outLinks = emptyLinks;
                    } else {
                        Vector outLinksV = new Vector();
                        for (int j = 0; j < lrs.getLtwTopic().get(i).getOutgoingLinks().getOutLink().size(); j++) {
                        	OutLink link = lrs.getLtwTopic().get(i).getOutgoingLinks().getOutLink().get(j);
                            String outLinkStr = link.getValue().toString().trim();
	                        if (!linkSet.containsKey(outLinkStr)) {
			                     linkSet.put(outLinkStr, new HashSet<String>());
			                }
	                        anchorSet.put(topicID + "_" + link.getAoffset() + "_" + link.getAlength(), new HashSet<String>());
//                            if (!outLinksV.contains(outLinkStr)) {
////                                outLinksV.add(outLinkStr);
//                            }
                        }
//                        outLinks = new String[outLinksV.size()];
//                        Enumeration oEnu = outLinksV.elements();
//                        while (oEnu.hasMoreElements()) {
//                            Object obj = oEnu.nextElement();
//                            outLinks[outCount] = obj.toString().trim();
//                            outCount++;
//                        }
//                        resultTable.put(topicID + "_" + Data.outgoingTag, outLinks);
                    }
                    
//                    resultTable.put(topicID + "_" + Data.outgoingTag, outLinks);
//	                resultTable.put(topicID + "_" + Data.incomingTag, emptyLinks);
//	                resultLinksTable.put(topicID/* + ".xml"*/, outCount + ";" + inCount);

                }
            }

        } catch (JAXBException ex) {
            ex.printStackTrace();
        }
        
	}

	public ArrayList<Team> getTeamLinkCount() {
		ArrayList<Team> teams = new ArrayList<Team>();
		
		return teams;
	}
	
}
