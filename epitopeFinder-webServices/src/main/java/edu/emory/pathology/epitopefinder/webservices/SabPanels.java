package edu.emory.pathology.epitopefinder.webservices;

import edu.emory.pathology.epitopefinder.imgtdb.data.SabPanel;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * This class implements the SabPanels RESTful web services.
 * 
 * @author ghsmith
 */
@Path("sabPanels")
public class SabPanels {

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
