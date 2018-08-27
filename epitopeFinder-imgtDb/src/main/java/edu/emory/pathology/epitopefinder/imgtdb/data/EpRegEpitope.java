package edu.emory.pathology.epitopefinder.imgtdb.data;

import java.io.Serializable;
import java.util.Map;
import java.util.SortedMap;

/**
 * Data class for an Epitope Registry Epitope.
 * @author ghsmith
 */
public class EpRegEpitope implements Serializable {

    public static class EpRegEpitopeAlleleRef {
        
        private String epRegAlleleName;
        private Boolean inCurrentSabPanel;
        private Boolean inEpRegSabPanel;
        private Boolean compatFilteredSourceUrl;
        private Boolean compatFiltered;
        private String compatInterpretation;

        public String getEpRegAlleleName() {
            return epRegAlleleName;
        }

        public void setEpRegAlleleName(String epRegAlleleName) {
            this.epRegAlleleName = epRegAlleleName;
        }

        public Boolean getInCurrentSabPanel() {
            return inCurrentSabPanel;
        }

        public void setInCurrentSabPanel(Boolean inCurrentSabPanel) {
            this.inCurrentSabPanel = inCurrentSabPanel;
        }

        public Boolean getInEpRegSabPanel() {
            return inEpRegSabPanel;
        }

        public void setInEpRegSabPanel(Boolean inEpRegSabPanel) {
            this.inEpRegSabPanel = inEpRegSabPanel;
        }

        public Boolean getCompatFilteredSourceUrl() {
            return compatFilteredSourceUrl;
        }

        public void setCompatFilteredSourceUrl(Boolean compatFilteredSourceUrl) {
            this.compatFilteredSourceUrl = compatFilteredSourceUrl;
        }

        public Boolean getCompatFiltered() {
            return compatFiltered;
        }

        public void setCompatFiltered(Boolean compatFiltered) {
            this.compatFiltered = compatFiltered;
        }

        public String getCompatInterpretation() {
            return compatInterpretation;
        }

        public void setCompatInterpretation(String compatInterpretation) {
            this.compatInterpretation = compatInterpretation;
        }

    }
    
    private Integer sequenceNumber;
    private String sourceUrl;
    private String locusGroup;
    private String epitopeName;
    private SortedMap<String, EpRegEpitopeAlleleRef> alleleMap;
    private Integer compatSabPanelCountPresent;
    private Integer compatSabPanelCountAbsent;
    private Integer compatSabPanelCountUnknown;

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getLocusGroup() {
        return locusGroup;
    }

    public void setLocusGroup(String locusGroup) {
        this.locusGroup = locusGroup;
    }

    public String getEpitopeName() {
        return epitopeName;
    }

    public void setEpitopeName(String epitopeName) {
        this.epitopeName = epitopeName;
    }

    public SortedMap<String, EpRegEpitopeAlleleRef> getAlleleMap() {
        return alleleMap;
    }

    public void setAlleleMap(SortedMap<String, EpRegEpitopeAlleleRef> alleleMap) {
        this.alleleMap = alleleMap;
    }

    public Integer getCompatSabPanelCountPresent() {
        return compatSabPanelCountPresent;
    }

    public void setCompatSabPanelCountPresent(Integer compatSabPanelCountPresent) {
        this.compatSabPanelCountPresent = compatSabPanelCountPresent;
    }

    public Integer getCompatSabPanelCountAbsent() {
        return compatSabPanelCountAbsent;
    }

    public void setCompatSabPanelCountAbsent(Integer compatSabPanelCountAbsent) {
        this.compatSabPanelCountAbsent = compatSabPanelCountAbsent;
    }

    public Integer getCompatSabPanelCountUnknown() {
        return compatSabPanelCountUnknown;
    }

    public void setCompatSabPanelCountUnknown(Integer compatSabPanelCountUnknown) {
        this.compatSabPanelCountUnknown = compatSabPanelCountUnknown;
    }
   
}
