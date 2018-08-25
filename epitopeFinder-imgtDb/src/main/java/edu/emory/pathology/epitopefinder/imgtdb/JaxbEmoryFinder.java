package edu.emory.pathology.epitopefinder.imgtdb;

import edu.emory.pathology.epitopefinder.imgtdb.jaxb.emory.SabPanels;
import java.io.File;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * This finder class loads the Emory data classes generated from the Emory XML
 * schema (sabPanels.xsd). It requires a reference to the Emory XML database
 * (sabPanels.xml).
 * 
 * @author ghsmith
 */
public class JaxbEmoryFinder {

    private static final Logger LOG = Logger.getLogger(JaxbEmoryFinder.class.getName());

    private String xmlFileName = "/tmp/sabPanels.xml";
    private SabPanels sabPanels;

    public JaxbEmoryFinder() {
    }

    public JaxbEmoryFinder(String xmlFileName) {
        if(xmlFileName != null) {
            this.xmlFileName = xmlFileName;
        }
    }
    
    public SabPanels getSabPanels() {
        if(sabPanels == null) {
            try {
                JAXBContext jc0 = JAXBContext.newInstance("edu.emory.pathology.epitopefinder.imgtdb.jaxb.emory");
                sabPanels = (SabPanels)jc0.createUnmarshaller().unmarshal(new File(xmlFileName));
            }
            catch(JAXBException e) {
                throw new RuntimeException(e);
            }
            LOG.info(String.format("%d SAB panels loaded from Emory XML database", sabPanels.getSabPanel().size()));
        }
        return sabPanels;
    }
    
}
