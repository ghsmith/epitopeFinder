package edu.emory.pathology.epitopefinder.webservices;

import edu.emory.pathology.epitopefinder.imgtdb.data.EpRegEpitope;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import org.apache.commons.lang.SerializationUtils;

/**
 * This class implements the EpRegEpitopes RESTful web services.
 * 
 * @author ghsmith
 */
@Path("epRegEpitopes")
public class EpRegEpitopes {

    @GET
    @Produces("application/json")
    public List<EpRegEpitope> getJson(@QueryParam("panelAllelesOnly") String panelAllelesOnly, @DefaultValue("-1") @QueryParam("startSequenceNumber") int startSequenceNumber, @DefaultValue("-1") @QueryParam("endSequenceNumber") int endSequenceNumber) {
        if(SessionFilter.computedPropertiesStale.get()[0]) {
            SessionFilter.epRegEpitopeFinder.get().computeCompatProperties(SessionFilter.alleleFinder.get());
            SessionFilter.alleleFinder.get().computeCompatProperties(SessionFilter.epRegEpitopeFinder.get());
            SessionFilter.computedPropertiesStale.get()[0] = false;
        }
        List<EpRegEpitope> epitopes = SessionFilter.epRegEpitopeFinder.get().getEpitopeList();
        // panelAllelesOnly saves bandwidth
        if("true".equals(panelAllelesOnly)) {
            epitopes = (List)SerializationUtils.clone((Serializable)epitopes);
            epitopes.stream().forEach((epitope) -> {
                List<String> alleleNameRemoveList = new ArrayList<>();
                epitope.getAlleleMap().values().stream().forEach((allele) -> {
                    if(!allele.getInCurrentSabPanel() && !allele.getInEpRegSabPanel()) {
                        alleleNameRemoveList.add(allele.getEpRegAlleleName());
                    }
                });
                alleleNameRemoveList.stream().forEach((alleleName) -> epitope.getAlleleMap().remove(alleleName));
            });
        }
        if(startSequenceNumber != -1 || endSequenceNumber != -1) {
            epitopes = epitopes.subList(startSequenceNumber, endSequenceNumber);
        }
        return epitopes;
    }
    
    @GET
    @Path("{locusGroup}")
    @Produces("application/json")
    public List<EpRegEpitope> getJson(@PathParam("locusGroup") String locusGroup, @QueryParam("panelAllelesOnly") String panelAllelesOnly, @DefaultValue("-1") @QueryParam("startSequenceNumber") int startSequenceNumber, @DefaultValue("-1") @QueryParam("endSequenceNumber") int endSequenceNumber) {
        if(SessionFilter.computedPropertiesStale.get()[0]) {
            SessionFilter.epRegEpitopeFinder.get().computeCompatProperties(SessionFilter.alleleFinder.get());
            SessionFilter.alleleFinder.get().computeCompatProperties(SessionFilter.epRegEpitopeFinder.get());
            SessionFilter.computedPropertiesStale.get()[0] = false;
        }
        List<EpRegEpitope> epitopes = SessionFilter.epRegEpitopeFinder.get().getEpitopeListByEpRegLocusGroup(locusGroup);
        // panelAllelesOnly saves bandwidth
        if("true".equals(panelAllelesOnly)) {
            epitopes = (List)SerializationUtils.clone((Serializable)epitopes);
            epitopes.stream().forEach((epitope) -> {
                List<String> alleleNameRemoveList = new ArrayList<>();
                epitope.getAlleleMap().values().stream().forEach((allele) -> {
                    if(!allele.getInCurrentSabPanel() && !allele.getInEpRegSabPanel()) {
                        alleleNameRemoveList.add(allele.getEpRegAlleleName());
                    }
                });
                alleleNameRemoveList.stream().forEach((alleleName) -> epitope.getAlleleMap().remove(alleleName));
            });
        }
        if(startSequenceNumber != -1 || endSequenceNumber != -1) {
            epitopes = epitopes.subList(startSequenceNumber, endSequenceNumber);
        }
        return epitopes;
    }

    @GET
    @Path("{locusGroup}/{epitopeName}")
    @Produces("application/json")
    public EpRegEpitope getJsonAllele(@PathParam("locusGroup") String locusGroup, @PathParam("epitopeName") String epitopeName, @QueryParam("panelAllelesOnly") String panelAllelesOnly) {
        if(SessionFilter.computedPropertiesStale.get()[0]) {
            SessionFilter.epRegEpitopeFinder.get().computeCompatProperties(SessionFilter.alleleFinder.get());
            SessionFilter.alleleFinder.get().computeCompatProperties(SessionFilter.epRegEpitopeFinder.get());
            SessionFilter.computedPropertiesStale.get()[0] = false;
        }
        EpRegEpitope epitope = SessionFilter.epRegEpitopeFinder.get().getEpitope(locusGroup, epitopeName);
        // panelAllelesOnly saves bandwidth
        if("true".equals(panelAllelesOnly)) {
            List<String> alleleNameRemoveList = new ArrayList<>();
            epitope.getAlleleMap().values().stream().forEach((allele) -> {
                if(!allele.getInCurrentSabPanel() && !allele.getInEpRegSabPanel()) {
                    alleleNameRemoveList.add(allele.getEpRegAlleleName());
                }
            });
            alleleNameRemoveList.stream().forEach((alleleName) -> epitope.getAlleleMap().remove(alleleName));
        }
        return epitope;
    }
    
}
