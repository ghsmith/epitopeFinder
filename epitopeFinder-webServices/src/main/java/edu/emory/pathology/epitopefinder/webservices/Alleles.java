package edu.emory.pathology.epitopefinder.webservices;

import edu.emory.pathology.epitopefinder.imgtdb.data.Allele;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * This class implements the Alleles RESTful web services.
 * 
 * @author ghsmith
 */
@Path("alleles")
public class Alleles {

    @GET
    @Produces("application/json")
    public List<Allele> getJson(@QueryParam("locusGroup") String locusGroup, @DefaultValue("-1") @QueryParam("startSequenceNumber") int startSequenceNumber, @DefaultValue("-1") @QueryParam("endSequenceNumber") int endSequenceNumber) {
        if(SessionFilter.computedPropertiesStale.get()[0]) {
            SessionFilter.epRegEpitopeFinder.get().computeCompatProperties(SessionFilter.alleleFinder.get());
            SessionFilter.alleleFinder.get().computeCompatProperties(SessionFilter.epRegEpitopeFinder.get());
            SessionFilter.computedPropertiesStale.get()[0] = false;
        }
        List<Allele> alleles = null;
        if(locusGroup != null) {
            alleles = SessionFilter.alleleFinder.get().getAlleleListByEpRegLocusGroup(locusGroup);
        }
        else {
            alleles = SessionFilter.alleleFinder.get().getAlleleList();
        }
        if(startSequenceNumber != -1 || endSequenceNumber != -1) {
            alleles = alleles.subList(startSequenceNumber, endSequenceNumber);
        }
        return alleles;
    }
    
    @GET
    @Path("{alleleName}")
    @Produces("application/json")
    public Allele getJsonAllele(@PathParam("alleleName") String alleleName) {
        if(SessionFilter.computedPropertiesStale.get()[0]) {
            SessionFilter.epRegEpitopeFinder.get().computeCompatProperties(SessionFilter.alleleFinder.get());
            SessionFilter.alleleFinder.get().computeCompatProperties(SessionFilter.epRegEpitopeFinder.get());
            SessionFilter.computedPropertiesStale.get()[0] = false;
        }
        Allele allele = SessionFilter.alleleFinder.get().getAllele(alleleName);
        // Fall back to the abbreviated epitope registry allele name.
        if(allele == null) {
            allele = SessionFilter.alleleFinder.get().getAlleleByEpRegAlleleName(alleleName);
        }
        return allele;
    }

    @PUT
    @Path("{alleleName}")
    @Consumes("application/json")
    public void putJsonAllele(@PathParam("alleleName") String alleleName, Allele updateAllele) {
        Allele allele = SessionFilter.alleleFinder.get().getAllele(alleleName);
        // Fall back to the abbreviated epitope registry allele name.
        if(allele == null) {
            allele = SessionFilter.alleleFinder.get().getAlleleByEpRegAlleleName(alleleName);
        }
        allele.setRecipientAntibodyForCompat(updateAllele.getRecipientAntibodyForCompat());
        allele.setRecipientTypeForCompat(updateAllele.getRecipientTypeForCompat());
        allele.setDonorTypeForCompat(updateAllele.getDonorTypeForCompat());
        SessionFilter.computedPropertiesStale.get()[0] = true;
    }
    
}
