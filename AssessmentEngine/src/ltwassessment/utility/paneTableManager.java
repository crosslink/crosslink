package ltwassessment.utility;

import java.util.Vector;

import ltwassessment.AppResource;
import ltwassessment.parsers.FOLTXTMatcher;
import ltwassessment.parsers.PoolerManager;
import ltwassessment.parsers.resourcesManager;
import ltwassessment.utility.TABInteractiveTableModel;
import ltwassessment.utility.TBAInteractiveTableModel;
import ltwassessment.utility.paneTableIndexing;

/**
 * @author Darren HUANG
 */
public class paneTableManager {

    // constant variables
    private boolean isTAB = false;
    private String wikipediaCollTitle = "";
    private String teAraCollTitle = "";
    private Vector<String[]> paneTableValueSetV;
    // import external Classes
    private PoolerManager myRunsPooler;
    private resourcesManager myRSCManager;
    private FOLTXTMatcher myFOLMatcher;
    private paneTableIndexing myTableIndexing;

    static void log(Object content) {
        System.out.println(content);
    }

    public paneTableManager(paneTableIndexing tabIndexing) {
        // constant variables from resource
        //org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ltwassessment.ltwassessmentApp.class).getContext().getResourceMap(ltwassessmentView.class);
        wikipediaCollTitle = AppResource.getInstance().getResourceMap().getString("collectionType.Wikipedia");
        teAraCollTitle = AppResource.getInstance().getResourceMap().getString("collectionType.TeAra");
        // ---------------------------------------------------------------------
        this.myRunsPooler = PoolerManager.getInstance();
        this.myRSCManager = resourcesManager.getInstance();
        this.myFOLMatcher = new FOLTXTMatcher();
        this.myTableIndexing = tabIndexing;
    }

    public void populateTABTable(TABInteractiveTableModel tabTableModel) {
        isTAB = true;
        paneTableValueSetV = myTableIndexing.getPaneTableValueSet(isTAB);
        int hiddenItem = 0;
        for (int i = 0; i < paneTableValueSetV.size(); i++) {
            String[] thisTABSet = paneTableValueSetV.elementAt(i);
            if (!tabTableModel.hasEmptyRow()) {
                tabTableModel.addEmptyRow();
            }
            for (int j = 0; j < thisTABSet.length; j++) {
                tabTableModel.setValueAt(thisTABSet[j], i, j);
            }
            tabTableModel.setValueAt("linkBepItem_" + hiddenItem, i, thisTABSet.length);
            hiddenItem++;
        }
    }

    public void populateTBATable(TBAInteractiveTableModel tbaTableModel) {
        isTAB = false;
        paneTableValueSetV = myTableIndexing.getPaneTableValueSet(isTAB);
        int hiddenItem = 0;
        for (int i = 0; i < paneTableValueSetV.size(); i++) {
            String[] thisTBASet = paneTableValueSetV.elementAt(i);
            if (!tbaTableModel.hasEmptyRow()) {
                tbaTableModel.addEmptyRow();
            }
            for (int j = 0; j < thisTBASet.length; j++) {
                tbaTableModel.setValueAt(thisTBASet[j], i, j);
            }
            tbaTableModel.setValueAt("linkAnchorItem_" + hiddenItem, i, thisTBASet.length);
            hiddenItem++;
        }
    }
}
