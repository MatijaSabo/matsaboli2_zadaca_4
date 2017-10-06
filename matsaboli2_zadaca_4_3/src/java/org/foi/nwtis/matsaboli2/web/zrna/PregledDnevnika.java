/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.matsaboli2.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.foi.nwtis.matsaboli2.ejb.eb.Dnevnik;
import org.foi.nwtis.matsaboli2.ejb.sb.DnevnikFacade;

/**
 *
 * @author Matija Sabolić
 */
@Named(value = "pregledDnevnika")
@SessionScoped
public class PregledDnevnika implements Serializable {

    @EJB
    private DnevnikFacade dnevnikFacade;

    String ip_adresa;
    String trajanje;
    String status;
    Date vrijeme_od;
    Date vrijeme_do;

    List<Dnevnik> lista = new ArrayList<>();

    private TimeZone timeZone;

    /**
     * Creates a new instance of PregledDnevnika
     */
    public PregledDnevnika() {
    }

    /**
     * Metoda koja uzima unesene fitere korisnika te prema njima radi pretragu
     * sadržaja iz baze podataka. Podaci se spremaju u listu koja se na kraju
     * prikazuje korisniku. Zahtjev se na kraju upisuje u bazu podataka.
     *
     * @return
     */
    public String filtrirajDnevnik() {
        long pocetak = System.currentTimeMillis();
        
        if ((this.ip_adresa == null || this.ip_adresa.isEmpty())
                && (this.trajanje == null || this.trajanje.isEmpty())
                && (this.status == null || this.status.isEmpty())
                && this.vrijeme_od == null && this.vrijeme_do == null) {
            this.lista = dnevnikFacade.findAll();
        } else {
            List<Dnevnik> list = dnevnikFacade.findAll();
            this.lista.clear();

            if (this.ip_adresa != null && !this.ip_adresa.isEmpty()) {
                List<Dnevnik> temp = new ArrayList<>();
                for (Dnevnik d : list) {
                    if (!d.getIpadresa().contains(this.ip_adresa)) {
                        temp.add(d);
                    }
                }

                for (Dnevnik d : temp) {
                    list.remove(d);
                }
            }

            if (this.trajanje != null && !this.trajanje.isEmpty()) {
                List<Dnevnik> temp = new ArrayList<>();
                for (Dnevnik d : list) {
                    if (d.getTrajanje() != Integer.parseInt(this.trajanje)) {
                        temp.add(d);
                    }
                }

                for (Dnevnik d : temp) {
                    list.remove(d);
                }
            }

            if (this.status != null && !this.status.isEmpty()) {
                List<Dnevnik> temp = new ArrayList<>();
                for (Dnevnik d : list) {
                    if (d.getStatus() != Integer.parseInt(this.status)) {
                        temp.add(d);
                    }
                }

                for (Dnevnik d : temp) {
                    list.remove(d);
                }
            }

            if (this.vrijeme_od != null) {
                List<Dnevnik> temp = new ArrayList<>();
                for (Dnevnik d : list) {
                    if (d.getVrijeme().before(this.vrijeme_od)) {
                        temp.add(d);
                    }
                }

                for (Dnevnik d : temp) {
                    list.remove(d);
                }
            }

            if (this.vrijeme_do != null) {
                List<Dnevnik> temp = new ArrayList<>();
                for (Dnevnik d : list) {
                    if (d.getVrijeme().after(this.vrijeme_do)) {
                        temp.add(d);
                    }
                }

                for (Dnevnik d : temp) {
                    list.remove(d);
                }
            }

            this.lista = list;
        }
        
        dnevnik(pocetak);
        return "filter";
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

    public List<Dnevnik> getLista() {
        return lista;
    }

    public void setLista(List<Dnevnik> lista) {
        this.lista = lista;
    }

    public String getIp_adresa() {
        return ip_adresa;
    }

    public void setIp_adresa(String ip_adresa) {
        this.ip_adresa = ip_adresa;
    }

    public String getTrajanje() {
        return trajanje;
    }

    public void setTrajanje(String trajanje) {
        this.trajanje = trajanje;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getVrijeme_od() {
        return vrijeme_od;
    }

    public void setVrijeme_od(Date vrijeme_od) {
        this.vrijeme_od = vrijeme_od;
    }

    public Date getVrijeme_do() {
        return vrijeme_do;
    }

    public void setVrijeme_do(Date vrijeme_do) {
        this.vrijeme_do = vrijeme_do;
    }

    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

}
