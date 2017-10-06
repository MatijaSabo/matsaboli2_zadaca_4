/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.matsaboli2.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Date;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.foi.nwtis.matsaboli2.ejb.eb.Dnevnik;
import org.foi.nwtis.matsaboli2.ejb.sb.DnevnikFacade;

/**
 *
 * @author Matija Sabolić
 */
@Named(value = "navigacija")
@SessionScoped
public class Navigacija implements Serializable {

    @EJB
    private DnevnikFacade dnevnikFacade;

    /**
     * Creates a new instance of Navigacija
     */
    public Navigacija() {
    }

    /**
     * Metoda koja se poziva kada korisnik u navigaciji odabere opciju Početak.
     *
     * @return
     */
    public String goToIndex() {
        long pocetak = System.currentTimeMillis();
        dnevnik(pocetak);
        return "pocetak";
    }

    /**
     * Metoda koja se poziva kada korisnik u navigaciji odabere opciju Pregled
     * IoT.
     *
     * @return
     */
    public String goToPregledIoT() {
        long pocetak = System.currentTimeMillis();
        dnevnik(pocetak);
        return "IoT";
    }

    /**
     * Metoda koja se poziva kada korisnik u navigaciji odabere opciju Pregled
     * dnevnika.
     *
     * @return
     */
    public String goToPregledDnevnika() {
        long pocetak = System.currentTimeMillis();
        dnevnik(pocetak);
        return "dnevnik";
    }

    /**
     * Metoda koja se poziva kada korisnik u navigaciji odabere opciju Pregled
     * promjena.
     *
     * @return
     */
    public String goToPregledPromjena() {
        long pocetak = System.currentTimeMillis();
        dnevnik(pocetak);
        return "promjene";
    }

    /**
     * Metoda koja upisuje podatke o pojedinom zahtjevu korisnika u bazu
     * podataka u tablicu dnevnik.
     *
     * @return
     */
    public void dnevnik(long pocetak) {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String ipadresa = request.getRemoteAddr();
        String url = request.getRequestURI();
        long kraj = System.currentTimeMillis();
        int trajanje = (int) (kraj - pocetak);
        int id = dnevnikFacade.findAll().size();

        Dnevnik dnevnik = new Dnevnik();
        dnevnik.setId(id + 1);
        dnevnik.setIpadresa(ipadresa);
        dnevnik.setKorisnik("matsaboli2");
        dnevnik.setStatus(0);
        dnevnik.setTrajanje(trajanje);
        dnevnik.setUrl(url);
        dnevnik.setVrijeme(new Date());

        dnevnikFacade.create(dnevnik);
    }

}
