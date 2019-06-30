package edu.emory.pathology.epitopefinder.webservices;

import edu.emory.pathology.epitopefinder.imgtdb.AlleleFinder;
import edu.emory.pathology.epitopefinder.imgtdb.EpRegEpitopeFinder;
import edu.emory.pathology.epitopefinder.imgtdb.SabPanelFinder;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

/**
 * This class returns session information. All updates to to attributes are
 * persisted in the session and are disposed of when the session ends.
 * 
 * @author ghsmith
 */
@Path("session")
public class Session {

    @GET
    @Produces("application/json")
    public String getJson() {
        return SessionFilter.sessionMutex.get();
    }

    @PUT
    @Path("reset")
    @Produces("application/json")
    public void putReset(@Context HttpServletRequest request) {
        request.getSession().invalidate();
    }

    @PUT
    @Path("reagentLot")
    @Produces("application/json")
    public void putReagentLot(@Context HttpServletRequest request, String reagentLotNumber) {
        Boolean[] computedPropertiesStale = new Boolean[] { false };
        AlleleFinder alleleFinder = new AlleleFinder(request.getServletContext().getInitParameter("imgtXmlFileName"));
        SabPanelFinder sabPanelFinder = new SabPanelFinder(request.getServletContext().getInitParameter("emoryXmlFileName"), reagentLotNumber);
        EpRegEpitopeFinder epRegEpitopeFinder = new EpRegEpitopeFinder();
        alleleFinder.assignCurrentSabPanelAlleles(sabPanelFinder);
        alleleFinder.assignEpRegSabPanelAlleles(epRegEpitopeFinder);
        epRegEpitopeFinder.assignCurrentSabPanelAlleles(sabPanelFinder);
        ((HttpServletRequest)request).getSession().setAttribute("computedPropertiesStale", computedPropertiesStale);
        ((HttpServletRequest)request).getSession().setAttribute("alleleFinder", alleleFinder);
        ((HttpServletRequest)request).getSession().setAttribute("sabPanelFinder", sabPanelFinder);
        ((HttpServletRequest)request).getSession().setAttribute("epRegEpitopeFinder", epRegEpitopeFinder);
        SessionFilter.computedPropertiesStale.set(computedPropertiesStale);
        SessionFilter.alleleFinder.set(alleleFinder);
        SessionFilter.sabPanelFinder.set(sabPanelFinder);
        SessionFilter.epRegEpitopeFinder.set(epRegEpitopeFinder);
    }
    
}
