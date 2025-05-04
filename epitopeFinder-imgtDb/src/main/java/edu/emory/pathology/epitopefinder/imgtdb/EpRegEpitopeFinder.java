package edu.emory.pathology.epitopefinder.imgtdb;

import edu.emory.pathology.epitopefinder.imgtdb.data.Allele;
import edu.emory.pathology.epitopefinder.imgtdb.data.EpRegEpitope;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
        //"https://www.epregistry.com.br";
        "http://xyz.epregistry.com.br";
    
    private String xmlFileName;
    private List<EpRegEpitope> epitopeList;

    public EpRegEpitopeFinder() {
    }

    public EpRegEpitope getEpitope(String locusGroup, String epitopeName) {
        try {
            return getEpitopeList().stream().filter((epitope) -> (locusGroup.equals(epitope.getLocusGroup()) && epitopeName.equals(epitope.getEpitopeName()))).findFirst().get();
        }
        catch(NoSuchElementException e) {
            return null;
        }
    }

    public List<EpRegEpitope> getEpitopeListByEpRegLocusGroup(String locusGroup) {
        return getEpitopeList().stream().filter((epitope) -> (locusGroup.equals(epitope.getLocusGroup()))).collect(Collectors.toList());
    }
    
    public List<EpRegEpitope> getEpitopeList() {
        if(epitopeList == null) {
            epitopeList = new ArrayList();
            for(String locusGroup : locusGroups) {
                try {
                    Document document = Jsoup.connect(stringUrl + "/databases/" + locusGroup).maxBodySize(0).timeout(20000).userAgent("Mozilla").get();
                    int x = 0;
                    for(Element rowE : Xsoup.compile("//section[@id='table-result']/div/table/tbody/tr").evaluate(document).getElements()) {
                        x++;
                        String id = Xsoup.compile("/td").evaluate(rowE).list().get(0).replaceAll("<td>(.*)</td>", "$1").trim();
                        String epitopeName = Xsoup.compile("/td").evaluate(rowE).list().get(1).replaceAll("<td>.*<b>(.*)</b>.*</td>", "$1").replaceAll("<sub[^>]*>(.*)</sub>", "-$1").replace("&nbsp;", "").trim();
                        List<String> alleleNameListLuminex = new ArrayList<>(Arrays.asList(Xsoup.compile("/td").evaluate(rowE).list().get(8).replaceAll("<td><small>(.*)</small></td>", "$1").replaceAll("<a[^>]*>([^<]*)</a>", "$1").replace(" ", "").split(",")));
                        String allAlleleUri = Xsoup.compile("/td").evaluate(rowE).getElements().get(9).select("a").first().attr("data-modal-url-value");
                        Document nestedDocument = Jsoup.connect(stringUrl + allAlleleUri).maxBodySize(0).timeout(20000).userAgent("Mozilla").get();
                        List<String> alleleNameList = new ArrayList<>(Arrays.asList(Xsoup.compile("//div[@class='modal-body']/p").evaluate(nestedDocument).get().replaceAll("<p>(.*)</p>", "$1").replaceAll(" ", "").split(",")));
                        EpRegEpitope epitope = new EpRegEpitope();
                        epitopeList.add(epitope);
                        epitope.setSourceUrl((stringUrl + "/databases/" + locusGroup).replace("http://xyz.", "https://www."));
                        epitope.setLocusGroup(locusGroup);
                        epitope.setEpitopeName(epitopeName);
                        epitope.setAlleleMap(new TreeMap<>());
                        epitope.setCompatAlleleFilterMap(new TreeMap<>());
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
            int sequenceNumber = 0;
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

    public void computeCompatProperties(AlleleFinder alleleFinder) {

        // 0. Nullify everything computed.
        getEpitopeList().stream().forEach((epitope) -> { epitope.getCompatAlleleFilterMap().clear(); });
        getEpitopeList().stream().forEach((epitope) -> { epitope.getAlleleMap().values().stream().forEach((allele) -> { allele.setCompatStatus(null); }); });
        getEpitopeList().stream().forEach((epitope) -> { epitope.setCompatSabPanelCountPresent(null);
                                                         epitope.setCompatSabPanelCountAbsent(null);
                                                         epitope.setCompatSabPanelCountUnknown(null);
                                                         epitope.setCompatSabPanelPctPresent(null); });
        
        // 1. Query the filtered epitopes from the epitope registry, one query
        //    per recipient antibody.
        for(String locusGroup : locusGroups) {
            alleleFinder.getAlleleListByEpRegLocusGroup(locusGroup).stream().filter((allele) -> (allele.getRecipientAntibodyForCompat())).forEach((alleleSab) -> {
                StringWriter queryString = new StringWriter();
                queryString.append(String.format("?confirmation_status=All&hla_typing=%s", alleleSab.getEpRegAlleleName()));
                alleleFinder.getAlleleListByEpRegLocusGroup(locusGroup).stream().filter((allele) -> (allele.getRecipientTypeForCompat())).forEach((alleleType) -> {
                    queryString.append(String.format("&patient_hla_typeing%%5B%%5D=%s", alleleType.getEpRegAlleleName()));
                });
                queryString.append("&commit=Search");
                try {
                    Document document = Jsoup.connect(stringUrl + "/databases/" + locusGroup + queryString.toString()).maxBodySize(0).timeout(20000).userAgent("Mozilla").get();
                    for(Element rowE : Xsoup.compile("//section[@id='table-result']/div/table/tbody/tr").evaluate(document).getElements()) {
                        String epitopeName = Xsoup.compile("/td").evaluate(rowE).list().get(1).replaceAll("<td>.*<b>(.*)</b>.*</td>", "$1").replaceAll("<sub[^>]*>(.*)</sub>", "-$1").replace("&nbsp;", "").trim();
                        EpRegEpitope epitope = getEpitope(locusGroup, epitopeName);
                        EpRegEpitope.EpRegEpitopeAlleleFilterRef alleleFilter = new EpRegEpitope.EpRegEpitopeAlleleFilterRef();
                        epitope.getCompatAlleleFilterMap().put(alleleSab.getEpRegAlleleName(), alleleFilter);
                        //alleleFilter.setSourceUrl(url.toString());
                        alleleFilter.setSourceUrl((stringUrl + "/databases/" + locusGroup + queryString.toString()).replace("http://xyz.", "https://www."));
                        alleleFilter.setReactiveEpRegAlleleName(alleleSab.getEpRegAlleleName());
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
            });
        }

        // 2. Set the compatStatus properties for each epitope allele.
        for(String locusGroup : locusGroups) {
            getEpitopeListByEpRegLocusGroup(locusGroup).stream().forEach((epitope) -> {
                epitope.getAlleleMap().values().stream().filter((allele) -> (allele.getInCurrentSabPanel() || allele.getInEpRegSabPanel())).forEach((allele) -> {
                    if(epitope.getCompatAlleleFilterMap().values().stream().filter((alleleFilter) -> (allele.getEpRegAlleleName().equals(alleleFilter.getReactiveEpRegAlleleName()))).findFirst().isPresent()) {
                        allele.setCompatStatus("+"); // bead is positive
                    }
                    else {
                        if(allele.getInCurrentSabPanel()) {
                            allele.setCompatStatus("0"); // bead is negative
                        }
                        else {
                            allele.setCompatStatus("?"); // we don't know status of bead (bead is part of Epitope Registry Panel but not current panel)
                        }
                    }
                });
            });
        }
            
        // 3. Set the compatSabPanelCount properties for each epitope.
        for(String locusGroup : locusGroups) {
            getEpitopeListByEpRegLocusGroup(locusGroup).stream().forEach((epitope) -> {
                epitope.setCompatSabPanelCountPresent(new Long(epitope.getAlleleMap().values().stream().filter((allele) -> ("+".equals(allele.getCompatStatus()))).count()).intValue());
                epitope.setCompatSabPanelCountAbsent(new Long(epitope.getAlleleMap().values().stream().filter((allele) -> ("0".equals(allele.getCompatStatus()))).count()).intValue());
                epitope.setCompatSabPanelCountUnknown(new Long(epitope.getAlleleMap().values().stream().filter((allele) -> ("?".equals(allele.getCompatStatus()))).count()).intValue());
                if((epitope.getCompatSabPanelCountPresent() + epitope.getCompatSabPanelCountAbsent()) > 0) {
                    epitope.setCompatSabPanelPctPresent((100 * epitope.getCompatSabPanelCountPresent()) / (epitope.getCompatSabPanelCountPresent() + epitope.getCompatSabPanelCountAbsent()));
                }

                // 4. Identify recipient epitopes. Currently limiting to only the epitopes we're going to show, because this is slow.
                if(epitope.getCompatSabPanelCountPresent() > 0) {
                    epitope.setCompatRecipientEpitope(false);
                    for(String epRegAlleleName : epitope.getAlleleMap().keySet()) {
                        Allele allele = alleleFinder.getAlleleByEpRegAlleleName(epRegAlleleName);
                        if(allele == null) {
                            //LOG.log(Level.SEVERE, String.format("epitope %s references allele %s, but that allele is not in the database", epitope.getEpitopeName(), epRegAlleleName));
                            continue;
                        }
                        if(allele.getRecipientTypeForCompat()) {
                            epitope.setCompatRecipientEpitope(true);
                            break;
                        }
                    }
                }

            });
        }
        
    }
    
}
