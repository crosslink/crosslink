package crosslink.tools;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import crosslink.measures.Data;
import crosslink.resultsetGenerator.LtwResultsetType;

public class ResultSet {

	Set<String>		linkSet;
	Set<String>		anchorSet;
	
	private void load(String resultSetPathFile) {
		
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

                }
            }

        } catch (JAXBException ex) {
            ex.printStackTrace();
        }
        
	}
	
}
