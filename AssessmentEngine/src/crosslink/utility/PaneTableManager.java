package crosslink.utility;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import crosslink.AppResource;
import crosslink.parsers.FOLTXTMatcher;
import crosslink.parsers.PoolerManager;
import crosslink.parsers.ResourcesManager;
import crosslink.utility.PaneTableIndexing;
import crosslink.utility.TABInteractiveTableModel;
import crosslink.utility.TBAInteractiveTableModel;


/**
 * @author Darren HUANG
 */
public class PaneTableManager {

    // constant variables
    private boolean isTAB = false;
    private String wikipediaCollTitle = "";
    private String teAraCollTitle = "";
    private Vector<String[]> paneTableValueSetV;
    // import external Classes
    private PoolerManager myRunsPooler;
    private ResourcesManager myRSCManager;
    private FOLTXTMatcher myFOLMatcher;
    private PaneTableIndexing myTableIndexing;

    static void log(Object content) {
        System.out.println(content);
    }

    public PaneTableManager(PaneTableIndexing tabIndexing) {
        // constant variables from resource
        //org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(crosslink.ltwassessmentApp.class).getContext().getResourceMap(ltwassessmentView.class);
        wikipediaCollTitle = AppResource.getInstance().getResourceMap().getString("collectionType.Wikipedia");
        teAraCollTitle = AppResource.getInstance().getResourceMap().getString("collectionType.TeAra");
        // ---------------------------------------------------------------------
        this.myRunsPooler = PoolerManager.getInstance();
        this.myRSCManager = ResourcesManager.getInstance();
        this.myFOLMatcher = FOLTXTMatcher.getInstance();
        this.myTableIndexing = tabIndexing;
    }

    public void populateTABTable(TABInteractiveTableModel tabTableModel) {
        paneTableValueSetV = myTableIndexing.getPaneTableValueSet();
        int hiddenItem = 0;
        int size = paneTableValueSetV.size();
        for (int i = 0; i < size; i++) {
            String[] thisTABSet = paneTableValueSetV.elementAt(i);
            if (!tabTableModel.hasEmptyRow()) {
                tabTableModel.addEmptyRow();
            }
            tabTableModel.setValueAt(i + 1, i, 0);
            for (int j = 0; j < thisTABSet.length; j++) {
                tabTableModel.setValueAt(thisTABSet[j], i, j + 1);
            }
            tabTableModel.setValueAt("linkBepItem_" + hiddenItem, i, TABInteractiveTableModel.HIDDEN_INDEX/*thisTABSet.length*/);
            hiddenItem++;
        }
        tabTableModel.setRowCount(size);
        tabTableModel.fireTableDataChanged();
    }

//    public void populateTBATable(TBAInteractiveTableModel tbaTableModel) {
//        paneTableValueSetV = myTableIndexing.getPaneTableValueSet();
//        int hiddenItem = 0;
//        for (int i = 0; i < paneTableValueSetV.size(); i++) {
//            String[] thisTBASet = paneTableValueSetV.elementAt(i);
//            if (!tbaTableModel.hasEmptyRow()) {
//                tbaTableModel.addEmptyRow();
//            }
//            for (int j = 0; j < thisTBASet.length; j++) {
//                tbaTableModel.setValueAt(thisTBASet[j], i, j);
//            }
//            tbaTableModel.setValueAt("linkAnchorItem_" + hiddenItem, i, thisTBASet.length);
//            hiddenItem++;
//        }
//    }
}
