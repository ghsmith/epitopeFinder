package edu.emory.pathology.epitopefinder.webservices;

import edu.emory.pathology.epitopefinder.imgtdb.data.EpRegEpitope;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * This class implements the EpRegEpitopes RESTful web services.
 * 
 * @author ghsmith
 */
@Path("epRegEpitopes")
public class EpRegEpitopes {

    @GET
    @Produces("application/json")
    public List<EpRegEpitope> getJson() {
        List<EpRegEpitope> epitopes = SessionFilter.epRegEpitopeFinder.get().getEpitopeList();
        return epitopes;
    }
    
    @GET
    @Path("{locusGroup}/{epitopeName}")
    @Produces("application/json")
    public EpRegEpitope getJsonAllele(@PathParam("locusGroup") String locusGroup, @PathParam("epitopeName") String epitopeName, @QueryParam("noCodons") String noCodons) {
        EpRegEpitope epRegEpitope = SessionFilter.epRegEpitopeFinder.get().getEpitope(locusGroup, epitopeName);
        return epRegEpitope;
    }
    
}
