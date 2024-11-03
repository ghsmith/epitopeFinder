package edu.emory.pathology.epitopefinder.webservices;

import edu.emory.pathology.epitopefinder.imgtdb.data.SabPanel;
import java.util.List;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

/**
 * This class implements the SabPanels RESTful web services.
 * 
 * @author ghsmith
 */
@Path("sabPanels")
public class SabPanels {

    @GET
    @Path("reagentLotNumber")
    @Produces("application/json")
    public String getJsonReagentLotNumber() {
        return SessionFilter.sabPanelFinder.get().getReagentLotNumber();
    }

    @GET
    @Produces("application/json")
    public List<SabPanel> getJson() {
        return SessionFilter.sabPanelFinder.get().getSabPanelList();
    }

    @GET
    @Path("{locusGroup}")
    @Produces("application/json")
    public SabPanel getJson(@PathParam("locusGroup") String locusGroup) {
        return SessionFilter.sabPanelFinder.get().getSabPanel(locusGroup);
    }
    
}
