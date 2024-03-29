package edu.emory.pathology.epitopefinder.imgtdb;

import edu.emory.pathology.epitopefinder.imgtdb.data.Allele;
import edu.emory.pathology.epitopefinder.imgtdb.data.Allele.AlleleEpRegEpitopeRef;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This finder class loads our local data classes from the IMGT data classes.
 * Our local data classes are optimized for the epitope classifier.
 * 
 * @author ghsmith
 */
public class AlleleFinder {

    private static final Logger LOG = Logger.getLogger(AlleleFinder.class.getName());

    private static Map<String, String> imgtLocusToEpRegLocusGroupMap;

    static {
        imgtLocusToEpRegLocusGroupMap = new HashMap<>();
        imgtLocusToEpRegLocusGroupMap.put("HLA-A", "ABC");
        imgtLocusToEpRegLocusGroupMap.put("HLA-B", "ABC");
        imgtLocusToEpRegLocusGroupMap.put("HLA-C", "ABC");
        imgtLocusToEpRegLocusGroupMap.put("HLA-DRB1", "DRB");
        imgtLocusToEpRegLocusGroupMap.put("HLA-DRB2", "DRB");
        imgtLocusToEpRegLocusGroupMap.put("HLA-DRB3", "DRB");
        imgtLocusToEpRegLocusGroupMap.put("HLA-DRB4", "DRB");
        imgtLocusToEpRegLocusGroupMap.put("HLA-DRB5", "DRB");
        imgtLocusToEpRegLocusGroupMap.put("HLA-DQB1", "DQ");
        imgtLocusToEpRegLocusGroupMap.put("HLA-DQA1", "DQ");
        imgtLocusToEpRegLocusGroupMap.put("HLA-DPB1", "DP");
        imgtLocusToEpRegLocusGroupMap.put("HLA-DPA1", "DP");
    }
    
    
    private String xmlFileName;
    private List<Allele> alleleList;

    public AlleleFinder() {
    }

    public AlleleFinder(String xmlFileName) {
        this.xmlFileName = xmlFileName;
    }
    
    public Allele getAllele(String alleleName) {
        try {
            return getAlleleList().stream().filter((allele) -> (alleleName.equals(allele.getAlleleName()))).findFirst().get();
        }
        catch(NoSuchElementException e) {
            return null;
        }
    }
    
    public Allele getAlleleByEpRegAlleleName(String epRegAlleleName) {
        try {
            return getAlleleList().stream().filter((allele) -> (epRegAlleleName.equals(allele.getEpRegAlleleName()))).findFirst().get();
        }
        catch(NoSuchElementException e) {
            return null;
        }
    }

    public List<Allele> getAlleleListByEpRegLocusGroup(String epRegLocusGroup) {
        return getAlleleList().stream().filter((allele) -> (epRegLocusGroup.equals(allele.getEpRegLocusGroup()))).collect(Collectors.toList());
    }

    public List<Allele> getAlleleList() {
        if(alleleList == null) {
            alleleList = new ArrayList();
            JaxbImgtFinder imgtFinder = new JaxbImgtFinder(xmlFileName);
            edu.emory.pathology.epitopefinder.imgtdb.jaxb.imgt.Alleles imgtAlleles = imgtFinder.getAlleles();
            // Process each IMGT HLA-DPB1 allele that has a sequence element.
            Pattern pattern = Pattern.compile("HLA-([^\\*]*)\\*([0-9]*):([0-9]*).*");
            imgtAlleles.getAllele().stream().filter((imgtAllele) -> (!imgtAllele.getName().endsWith("N"))).forEach((imgtAllele) -> {
                Matcher matcher = pattern.matcher(imgtAllele.getName());
                if(matcher.find()) {
                    String epRegLocusGroup = imgtLocusToEpRegLocusGroupMap.get(String.format("HLA-%s", matcher.group(1)));
                    // Only adding IMGT alleles that correspond to an epitope registry locus group.
                    if(epRegLocusGroup != null) {
                        String epRegAlleleName = String.format("%s*%s:%s", matcher.group(1), matcher.group(2), matcher.group(3));
                        // Only adding the first IMGT allele corresponding to an epitope registry allele name.
                        if(alleleList.stream().filter((filterAllele) -> (filterAllele.getEpRegAlleleName().equals(epRegAlleleName))).count() == 0) {
                            Allele allele = new Allele();
                            alleleList.add(allele);
                            allele.setVersion(imgtAllele.getReleaseversions().getCurrentrelease());
                            allele.setAlleleName(imgtAllele.getName());
                            allele.setEpRegLocusGroup(epRegLocusGroup);
                            allele.setEpRegAlleleName(epRegAlleleName);
                            allele.setCompatEpRegEpitopeMap(new TreeMap<>());
                        }
                    }
                }
            });
            // Add sequence number for sorting.
            int sequenceNumber = 0;
            for(Allele allele : alleleList) {
                allele.setSequenceNumber(sequenceNumber++);
            }
        }
        return alleleList;
    }
    
    public void assignCurrentSabPanelAlleles(SabPanelFinder sabPanelFinder) {
        getAlleleList().stream().forEach((allele) -> { allele.setInCurrentSabPanel(false); });
        sabPanelFinder.getSabPanelList().stream().forEach((sabPanel) -> {
            sabPanel.getEpRegAlleleNameList().stream().forEach((epRegAlleleName) -> {
                getAlleleByEpRegAlleleName(epRegAlleleName).setInCurrentSabPanel(true);
            });
        });
    }

    public void assignEpRegSabPanelAlleles(EpRegEpitopeFinder epitopeFinder) {
        getAlleleList().stream().forEach((allele) -> { allele.setInEpRegSabPanel(false); });
        epitopeFinder.getEpitopeList().stream().forEach((epitope) -> {
            epitope.getAlleleMap().values().stream().filter((alleleRef) -> (alleleRef.getInEpRegSabPanel())).forEach((alleleRef) -> {
                getAlleleByEpRegAlleleName(alleleRef.getEpRegAlleleName()).setInEpRegSabPanel(true);
            });
        });
    }

    // This method should only be invoked after epRegEpitope.computeCompatProperties.
    public void computeCompatProperties(EpRegEpitopeFinder epitopeFinder) {

        // 0. Nullify everything computed.
        getAlleleList().stream().forEach((allele) -> { allele.getCompatEpRegEpitopeMap().clear(); });
        getAlleleList().stream().forEach((allele) -> { allele.setCompatInterpretation(null); });

        // 1. Handle auto-antibody (AA).
        getAlleleList().stream().filter((allele) -> (allele.getRecipientAntibodyForCompat() && allele.getRecipientTypeForCompat())).forEach((allele) -> { allele.setCompatInterpretation("AA"); });

        // 2. Handle incompatible (I).
        getAlleleList().stream().filter((allele) -> (allele.getCompatInterpretation() == null && allele.getRecipientAntibodyForCompat() && !allele.getRecipientTypeForCompat())).forEach((allele) -> { allele.setCompatInterpretation("I"); });

        // 3. Handle electronically incompatible (EI).
        getAlleleList().stream().filter((allele) -> (allele.getCompatInterpretation() == null)).forEach((allele) -> {
            epitopeFinder.getEpitopeListByEpRegLocusGroup(allele.getEpRegLocusGroup()).stream().filter((epitope) -> (
                epitope.getCompatSabPanelPctPresent() != null
                && epitope.getCompatSabPanelPctPresent() == 100
                && epitope.getCompatSabPanelCountPresent() == getAlleleList().stream().filter((x) -> (x.getRecipientAntibodyForCompat())).count()
            )).forEach((epitope) -> {
                if(epitope.getAlleleMap().values().stream().filter((epitopeAllele) -> (allele.getEpRegAlleleName().equals(epitopeAllele.getEpRegAlleleName()))).findFirst().isPresent()) {
                    allele.setCompatInterpretation("EI");
                }
            });
        });

        // 4. Handle not electornically incompatible - review (NEI-R)
        getAlleleList().stream().filter((allele) -> (allele.getCompatInterpretation() == null)).forEach((allele) -> {
            epitopeFinder.getEpitopeListByEpRegLocusGroup(allele.getEpRegLocusGroup()).stream().filter((epitope) -> (
                epitope.getCompatSabPanelPctPresent() != null
                && epitope.getCompatSabPanelPctPresent() == 100
            )).forEach((epitope) -> {
                if(epitope.getAlleleMap().values().stream().filter((epitopeAllele) -> (allele.getEpRegAlleleName().equals(epitopeAllele.getEpRegAlleleName()))).findFirst().isPresent()) {
                    allele.setCompatInterpretation("NEI-R");
                }
            });
        });
        
        // 5. Handle not electronically incompatible (NEI).
        getAlleleList().stream().filter((allele) -> (allele.getCompatInterpretation() == null)).forEach((allele) -> {
           allele.setCompatInterpretation("NEI");
        });
        
        // 6. Set the epitope map up.
        epitopeFinder.getEpitopeList().stream().filter((epitope) -> (epitope.getCompatSabPanelPctPresent() != null && epitope.getCompatSabPanelPctPresent() > 0)).forEach((epitope) -> {
            epitope.getAlleleMap().values().stream().forEach((alleleRef) -> {
                if(getAlleleByEpRegAlleleName(alleleRef.getEpRegAlleleName()) != null) { // epitope registry references some NULL alleles which I'm not pulling in from IMGT... not sure what this means
                    AlleleEpRegEpitopeRef alleleEpRegEpitopeRef = new Allele.AlleleEpRegEpitopeRef();
                    alleleEpRegEpitopeRef.setEpitopeName(epitope.getEpitopeName());
                    alleleEpRegEpitopeRef.setCompatSabPanelCountPresent(epitope.getCompatSabPanelCountPresent());
                    alleleEpRegEpitopeRef.setCompatSabPanelCountAbsent(epitope.getCompatSabPanelCountAbsent());
                    alleleEpRegEpitopeRef.setCompatSabPanelCountUnknown(epitope.getCompatSabPanelCountUnknown());
                    alleleEpRegEpitopeRef.setCompatSabPanelPctPresent(epitope.getCompatSabPanelPctPresent());
                    getAlleleByEpRegAlleleName(alleleRef.getEpRegAlleleName()).getCompatEpRegEpitopeMap().put(epitope.getEpitopeName(), alleleEpRegEpitopeRef);
                }
            });
        });
        
    }
        
}
