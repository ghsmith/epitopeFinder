package edu.emory.pathology.epitopefinder.imgtdb;

import edu.emory.pathology.epitopefinder.imgtdb.data.EpRegEpitope;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import us.codecraft.xsoup.Xsoup;

/**
 * This finder class loads our local data classes from the Epitope Registry.
 * Our local data classes are optimized for the epitope classifier.
 * 
 * @author ghsmith
 */
public class EpRegEpitopeFinder {

    private static final Logger LOG = Logger.getLogger(EpRegEpitopeFinder.class.getName());

    static private String[] locusGroups = { "ABC", "DRB", "DQ", "DP" };
    
    static private String stringUrl = 
        "http://www.epregistry.com.br/index/databases/database/%s/"
        + "?and_or=AND"
        + "&confirmed=All";
    
    private String xmlFileName;
    private List<EpRegEpitope> epitopeList;

    public EpRegEpitopeFinder() {
    }

    public EpRegEpitope getEpitope(String locusGroup, String epitopeName) {
        return getEpitopeList().stream().filter((epitope) -> (locusGroup.equals(epitope.getLocusGroup()) && epitopeName.equals(epitope.getEpitopeName()))).findFirst().get();
    }
    
    public List<EpRegEpitope> getEpitopeList() {
        if(epitopeList == null) {
            epitopeList = new ArrayList();
            for(String locusGroup : locusGroups) {
                try {
                    URL url = new URL(String.format(stringUrl, locusGroup));
                    Document document = Jsoup.connect(url.toString()).maxBodySize(0).timeout(5000).get();
                    for(Element rowE : Xsoup.compile("//section[@id='table-result']/div/table/tbody/tr").evaluate(document).getElements()) {
                        String epitopeName = Xsoup.compile("/td").evaluate(rowE).list().get(0).replaceAll("<td[^>]*>(.*)</td>", "$1").replaceAll("<sub[^>]*>(.*)</sub>", "-$1").trim();
                        List<String> alleleNameList;
                        if("trVariant".equals(rowE.className())) {
                            alleleNameList = new ArrayList<>(Arrays.asList(Xsoup.compile("//div[starts-with(@id, 'myModalVariantAlleleAll')]/div[@class='modal-body']/p/text()").evaluate(rowE).get().replace(" ", "").split(",")));
                        }
                        else {
                            alleleNameList = new ArrayList<>(Arrays.asList(Xsoup.compile("//div[starts-with(@id, 'myModalAlleleAll')]/div[@class='modal-body']/p/text()").evaluate(rowE).get().replace(" ", "").split(",")));
                        }
                        List<String> alleleNameListLuminex;
                        if("trVariant".equals(rowE.className())) {
                            alleleNameListLuminex = new ArrayList<>(Arrays.asList(Xsoup.compile("//div[starts-with(@id, 'myModalVariantAlleleLuminex')]/div[@class='modal-body']/p/text()").evaluate(rowE).get().replace(" ", "").split(",")));
                        }
                        else {
                            alleleNameListLuminex = new ArrayList<>(Arrays.asList(Xsoup.compile("//div[starts-with(@id, 'myModalAlleleLuminex')]/div[@class='modal-body']/p/text()").evaluate(rowE).get().replace(" ", "").split(",")));
                        }
                        EpRegEpitope epitope = new EpRegEpitope();
                        epitopeList.add(epitope);
                        epitope.setSourceUrl(url.toString());
                        epitope.setLocusGroup(locusGroup);
                        epitope.setEpitopeName(epitopeName);
                        epitope.setAlleleMap(new TreeMap<>());
                        alleleNameList.stream().forEach((alleleName) -> {
                            EpRegEpitope.EpRegEpitopeAlleleRef epitopeAlleleRef = new EpRegEpitope.EpRegEpitopeAlleleRef();
                            epitope.getAlleleMap().put(alleleName, epitopeAlleleRef);
                            epitopeAlleleRef.setEpRegAlleleName(alleleName);
                            epitopeAlleleRef.setInCurrentSabPanel(null);
                            epitopeAlleleRef.setInEpRegSabPanel(alleleNameListLuminex.contains(alleleName));
                        });
                    }
                    
                }
                catch (MalformedURLException e) {
                    LOG.log(Level.SEVERE, null, e);
                    throw new RuntimeException(e);
                }
                catch (IOException e) {
                    LOG.log(Level.SEVERE, null, e);
                    throw new RuntimeException(e);
                }
            }
            // Add sequence number for sorting.
            int sequenceNumber = 1;
            for(EpRegEpitope epitope : epitopeList) {
                epitope.setSequenceNumber(sequenceNumber++);
            }
        }
        return epitopeList;
    }

    public void assignCurrentSabPanelAlleles(SabPanelFinder sabPanelFinder) {
        getEpitopeList().stream().forEach((epitope) -> {
            epitope.getAlleleMap().values().stream().forEach((allele) -> {
                allele.setInCurrentSabPanel(sabPanelFinder.getSabPanel(epitope.getLocusGroup()).getEpRegAlleleNameList().contains(allele.getEpRegAlleleName()));
            });
        });
    }

    public void computeCompatInterpretation(AlleleFinder alleleFinder) {
        for(String locusGroup : locusGroups) {
            
        }
    }
    
}
