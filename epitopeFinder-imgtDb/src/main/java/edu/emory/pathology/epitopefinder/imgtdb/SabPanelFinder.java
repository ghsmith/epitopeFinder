package edu.emory.pathology.epitopefinder.imgtdb;

import edu.emory.pathology.epitopefinder.imgtdb.data.SabPanel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This finder class loads our local data classes from the Emory data classes.
 * Our local data classes are optimized for the HLA-DPB1 classifier.
 *
 * @author ghsmith
 */
public class SabPanelFinder {

    private static final Logger LOG = Logger.getLogger(SabPanelFinder.class.getName());
    
    private String xmlFileName;
    private List<SabPanel> sabPanelList;

    public SabPanelFinder(String xmlFileName) {
        this.xmlFileName = xmlFileName;
    }

    public SabPanel getSabPanel(String epRegLocusGroup) {
        return getSabPanelList().stream().filter((sabPanel) -> (epRegLocusGroup.equals(sabPanel.getEpRegLocusGroup()))).findFirst().get();
    }
    
    public List<SabPanel> getSabPanelList() {
        if(sabPanelList == null) {
            sabPanelList = new ArrayList();
            JaxbEmoryFinder emoryFinder = new JaxbEmoryFinder(xmlFileName);
            edu.emory.pathology.epitopefinder.imgtdb.jaxb.emory.SabPanels emorySabPanels = emoryFinder.getSabPanels();
            emorySabPanels.getSabPanel().stream().forEach((emorySabPanel) -> {
                SabPanel sabPanel = new SabPanel();
                sabPanelList.add(sabPanel);
                sabPanel.setEpRegLocusGroup(emorySabPanel.getEpRegLocusGroup());
                sabPanel.setEpRegAlleleNameList(emorySabPanel.getSab().stream().map((sab) -> { return sab.getEpRegAlleleName(); }).collect(Collectors.toList()));
            });
            LOG.info(String.format("%d SAB panels loaded", sabPanelList.size()));
        }
        return sabPanelList;
    }

}
