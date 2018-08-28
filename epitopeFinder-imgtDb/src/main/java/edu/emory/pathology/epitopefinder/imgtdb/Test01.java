/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.emory.pathology.epitopefinder.imgtdb;

import edu.emory.pathology.epitopefinder.imgtdb.data.Allele;
import edu.emory.pathology.epitopefinder.imgtdb.data.EpRegEpitope;
import edu.emory.pathology.epitopefinder.imgtdb.data.SabPanel;

/**
 *
 * @author priam
 */
public class Test01 {

    public static void main(String[] args) {
        AlleleFinder af = new AlleleFinder("data/hla.xml");
        int x = 1;
        for(Allele allele : af.getAlleleList()) {
            if(allele.getAlleleName().matches("HLA-DPA[^*]*\\*.*")) {
                System.out.println(String.format("%5d: %s %s", x++, allele.getAlleleName(), allele.getEpRegAlleleName()));
            }
        };
        //System.out.println(af.getAllele("HLA-C*18:10"));
        //System.out.println(af.getAlleleByEpRegAlleleName("C*18:10"));
        //SabPanelFinder sf = new SabPanelFinder("data/sabPanels.xml");
        //for(SabPanel  sabPanel : sf.getSabPanelList()) {
        //    System.out.println(String.format("%s: %s", sabPanel.getEpRegLocusGroup(), sabPanel.getEpRegAlleleNameList()));
        //}
        af.assignCurrentSabPanelAlleles(new SabPanelFinder("data/sabPanels.xml"));
        EpRegEpitopeFinder ef = new EpRegEpitopeFinder();
        ef.assignCurrentSabPanelAlleles(new SabPanelFinder("data/sabPanels.xml"));
        for(EpRegEpitope epitope : ef.getEpitopeList()) {
            if(epitope.getAlleleMap() == null) {
                System.out.println(epitope.getLocusGroup() + " " + epitope.getEpitopeName());
            }
        }
        
    }
    
}
