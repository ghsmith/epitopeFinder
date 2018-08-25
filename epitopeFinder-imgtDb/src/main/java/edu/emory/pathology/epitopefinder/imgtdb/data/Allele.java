package edu.emory.pathology.epitopefinder.imgtdb.data;

import java.io.Serializable;
import java.util.List;

/**
 * Data class for an allele.
 * @author ghsmith
 */
public class Allele implements Serializable {

    private Integer sequenceNumber;
    private String version;
    private String alleleName;
    private String epRegLocusGroup;
    private String epRegAlleleName;
    private List<String> epRegEpitopeNameList;
    private Boolean recipientAntibodyForCompat = false;
    private Boolean recipientTypeForCompat = false;
    private Boolean donorTypeForCompat = false;
    private String compatInterpretation;

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAlleleName() {
        return alleleName;
    }

    public void setAlleleName(String alleleName) {
        this.alleleName = alleleName;
    }

    public String getEpRegLocusGroup() {
        return epRegLocusGroup;
    }

    public void setEpRegLocusGroup(String epRegLocusGroup) {
        this.epRegLocusGroup = epRegLocusGroup;
    }

    public String getEpRegAlleleName() {
        return epRegAlleleName;
    }

    public void setEpRegAlleleName(String epRegAlleleName) {
        this.epRegAlleleName = epRegAlleleName;
    }

    public List<String> getEpRegEpitopeNameList() {
        return epRegEpitopeNameList;
    }

    public void setEpRegEpitopeNameList(List<String> epRegEpitopeNameList) {
        this.epRegEpitopeNameList = epRegEpitopeNameList;
    }

    public Boolean getRecipientAntibodyForCompat() {
        return recipientAntibodyForCompat;
    }

    public void setRecipientAntibodyForCompat(Boolean recipientAntibodyForCompat) {
        this.recipientAntibodyForCompat = recipientAntibodyForCompat;
    }

    public Boolean getRecipientTypeForCompat() {
        return recipientTypeForCompat;
    }

    public void setRecipientTypeForCompat(Boolean recipientTypeForCompat) {
        this.recipientTypeForCompat = recipientTypeForCompat;
    }

    public Boolean getDonorTypeForCompat() {
        return donorTypeForCompat;
    }

    public void setDonorTypeForCompat(Boolean donorTypeForCompat) {
        this.donorTypeForCompat = donorTypeForCompat;
    }

    public String getCompatInterpretation() {
        return compatInterpretation;
    }

    public void setCompatInterpretation(String compatInterpretation) {
        this.compatInterpretation = compatInterpretation;
    }
    
}
