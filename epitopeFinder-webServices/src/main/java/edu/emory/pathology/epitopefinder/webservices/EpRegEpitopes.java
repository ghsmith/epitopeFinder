package edu.emory.pathology.epitopefinder.webservices;

import edu.emory.pathology.epitopefinder.imgtdb.data.EpRegEpitope;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
