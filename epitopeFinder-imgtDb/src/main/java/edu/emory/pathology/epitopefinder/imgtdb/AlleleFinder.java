package edu.emory.pathology.epitopefinder.imgtdb;

import edu.emory.pathology.epitopefinder.imgtdb.data.Allele;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This finder class loads our local data classes from the IMGT data classes.
 * Our local data classes are optimized for the HLA-DPB1 classifier.
 * 
 * @author ghsmith
 */
public class AlleleFinder {

    private static final Logger LOG = Logger.getLogger(AlleleFinder.class.getName());

    private String xmlFileName;
    private List<Allele> alleleList;

    public AlleleFinder() {
    }

    public AlleleFinder(String xmlFileName) {
        this.xmlFileName = xmlFileName;
    }
    
    public Allele getAllele(String alleleName) {
        return getAlleleList().stream().filter((allele) -> (alleleName.equals(allele.getAlleleName()))).findFirst().get();
    }
    
    public Allele getAlleleByEpRegAlleleName(String epRegAlleleName) {
        return getAlleleList().stream().filter((allele) -> (epRegAlleleName.equals(allele.getEpRegAlleleName()))).findFirst().get();
    }

    public List<Allele> getAlleleList() {
        
        if(alleleList == null) {
            alleleList = new ArrayList();
            JaxbImgtFinder imgtFinder = new JaxbImgtFinder(xmlFileName);
            edu.emory.pathology.epitopefinder.imgtdb.jaxb.imgt.Alleles imgtAlleles = imgtFinder.getAlleles();
            // Process each IMGT HLA-DPB1 allele that has a sequence element.
            Pattern pattern = Pattern.compile("HLA-([^\\*]*)\\*([0-9]*):([0-9]*).*");
            imgtAlleles.getAllele().stream().forEach((imgtAllele) -> {
                Matcher matcher = pattern.matcher(imgtAllele.getName());
                if(matcher.find()) {
                    String epRegAlleleName = String.format("%s*%s:%s", matcher.group(1), matcher.group(2), matcher.group(3));
                    // Only adding the first IMGT allele corresponding to an epitope registry allele name.
                    if(alleleList.stream().filter((filterAllele) -> (filterAllele.getEpRegAlleleName().equals(epRegAlleleName))).count() == 0) {
                        Allele allele = new Allele();
                        alleleList.add(allele);
                        allele.setVersion(imgtAllele.getReleaseversions().getCurrentrelease());
                        allele.setAlleleName(imgtAllele.getName());
                        allele.setEpRegAlleleName(epRegAlleleName);
                    }
                }
            });
            // Add sequence number for sorting.
            int sequenceNumber = 1;
            for(Allele allele : alleleList) {
                allele.setSequenceNumber(sequenceNumber++);
            }
        }
        
        return alleleList;
        
    }

    
}
