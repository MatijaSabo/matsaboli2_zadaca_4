/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.matsaboli2.web.kontrole;

/**
 *
 * @author Matija Sabolić
 */
public class Izbornik {
    private String labela;
    private String vrijednost;

    public String getLabela() {
        return labela;
    }

    public String getVrijednost() {
        return vrijednost;
    }

    public void setLabela(String labela) {
        this.labela = labela;
    }

    public void setVrijednost(String vrijednost) {
        this.vrijednost = vrijednost;
    }

    public Izbornik(String labela, String vrijednost) {
        this.labela = labela;
        this.vrijednost = vrijednost;
    }
    
    
}
