package crosslink.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import crosslink.measures.Data;
import crosslink.resultsetGenerator.LtwResultsetType;
import crosslink.resultsetGenerator.OutLink;

public class ResultSet {

	HashMap<String, HashSet<String>> linkSet = new HashMap<String, HashSet<String>>(); // link, participant id
	HashMap<String, HashSet<String>> anchorSet = new HashMap<String, HashSet<String>>(); //key = topicid_anchoroffset_anchor_length
	File resultSetFile = null; 
	
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
		this.resultSetFile = new File(resultSetPathFile);
        try {
            JAXBContext jc;
            jc = JAXBContext.newInstance("crosslink.resultsetGenerator");
            Unmarshaller um = jc.createUnmarshaller();
            LtwResultsetType lrs = (LtwResultsetType) ((um.unmarshal(resultSetFile)));

            
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
		HashMap<String, Team> teamMap = new HashMap<String, Team>();
		Set set = linkSet.entrySet();
		Iterator it = set.iterator();
		Team team = null;
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String key = (String) entry.getKey();
			HashSet<String> participants = (HashSet<String>) entry.getValue();
			
			if (participants.size() == 1) {
				String teamId = (String) participants.toArray()[0];

				if (teamMap.containsKey(teamId))
					team = teamMap.get(teamId);
				else {
					team = new Team(teamId);
					teamMap.put(teamId, team);
					teams.add(team);
				}
				team.increaseLinkCount(1);
			}
		}
		return teams;
	}
	
	public ArrayList<Team> getTeamAnchorCount() {
		ArrayList<Team> teams = new ArrayList<Team>();
		
		return teams;
	}
	
	public void compareTo(ResultSet rs) {
		HashMap<String, HashSet<String>> rsLinkSet = rs.getLinkSet();
		Set thisLinkSet = linkSet.keySet();
		Set thatLinkSet = rsLinkSet.keySet();
		int count = 0;
		Iterator it  = thisLinkSet.iterator();
		while (it.hasNext()) {
			String thisLink = (String)it.next();
			if (thatLinkSet.contains(thisLink))
				++count;
		}
		
		System.out.println(this.resultSetFile.getName() + ": " + thisLinkSet.size());
		System.out.println(rs.getResultSetFile().getName() + ": " + thatLinkSet.size());
		System.out.println("Overlapping: " + count);
	}

	private HashMap<String, HashSet<String>> getLinkSet() {
		return linkSet;
	}

	public File getResultSetFile() {
		return resultSetFile;
	}
}
