/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.matsaboli2.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.foi.nwtis.matsaboli2.ejb.eb.Dnevnik;
import org.foi.nwtis.matsaboli2.ejb.eb.Promjene;
import org.foi.nwtis.matsaboli2.ejb.eb.Uredaji;
import org.foi.nwtis.matsaboli2.ejb.sb.DnevnikFacade;
import org.foi.nwtis.matsaboli2.ejb.sb.MeteoIoTKlijent;
import org.foi.nwtis.matsaboli2.ejb.sb.PromjeneFacade;
import org.foi.nwtis.matsaboli2.ejb.sb.UredajiFacade;
import org.foi.nwtis.matsaboli2.konfiguracije.Konfiguracija;
import org.foi.nwtis.matsaboli2.rest.klijenti.GMKlijent;
import org.foi.nwtis.matsaboli2.web.SlusacAplikacije;
import org.foi.nwtis.matsaboli2.web.kontrole.Izbornik;
import org.foi.nwtis.matsaboli2.web.podaci.Lokacija;
import org.foi.nwtis.matsaboli2.web.podaci.MeteoPrognoza;

/**
 *
 * @author Matija Sabolić
 */
@Named(value = "odabirIoTPrognoza")
@SessionScoped
public class OdabirIoTPrognoza implements Serializable {

    @EJB
    private DnevnikFacade dnevnikFacade;

    @EJB
    private PromjeneFacade promjeneFacade;

    @EJB
    private UredajiFacade uredajiFacade;

    @EJB
    private MeteoIoTKlijent meteoIoTKlijent;

    private String id_unos;
    private String naziv_unos;
    private String adresa_unos;
    private Boolean error_unos = false;

    private Uredaji uredaj_edit;
    private String id_edit;
    private String naziv_edit;
    private String adresa_edit;
    private Boolean error_edit = false;

    private List<Izbornik> raspolozivi_uredaji = new ArrayList<>();
    private List<Izbornik> odabrani_uredjaji = new ArrayList<>();

    private List<String> popisRaspolozivihUredaja = new ArrayList<>();
    private List<String> popisOdabranihUredaja = new ArrayList<>();

    private boolean edit = false;
    private boolean view = false;

    private String gumbPregledPrognoza = "Prognoze";

    private List<MeteoPrognoza> meteo_prognoze = new ArrayList<>();

    private TimeZone timeZone;
    private String showIot;

    /**
     * Creates a new instance of OdabirIoTPrognoza
     */
    public OdabirIoTPrognoza() {

    }

    /**
     * Metoda koja preuzima sve uređaje iz baze podataka te ih sprema u listu
     * raspoloživih uređaja.
     */
    public void preuzmiRaspolziveIoTuredaje() {
        List<Uredaji> uredaji = uredajiFacade.findAll();

        if (this.raspolozivi_uredaji != null) {
            this.raspolozivi_uredaji.clear();
        }

        if (this.odabrani_uredjaji != null) {
            this.odabrani_uredjaji.clear();
        }

        for (Uredaji u : uredaji) {
            this.raspolozivi_uredaji.add(new Izbornik(u.getNaziv(), u.getId().toString()));
        }
    }

    /**
     * Metoda koja dodaje novi uređaj u bazu podataka. Naakon toga se
     * osvježavaju svi raspoloživi uređaji u listi te se zahtjev zapisuje u bazu
     * podataka.
     *
     * @return
     */
    public String dodajIoT() {
        long pocetak = System.currentTimeMillis();

        Lokacija lokacija = meteoIoTKlijent.dajLokaciju(this.adresa_unos);
        Uredaji uredjaj = new Uredaji(Integer.parseInt(this.id_unos), this.naziv_unos, Float.parseFloat(lokacija.getLatitude()), Float.parseFloat(lokacija.getLongitude()), 0, new Date(), new Date());

        Uredaji uredaj = uredajiFacade.find(Integer.parseInt(this.id_unos));
        if (uredaj != null) {
            this.error_unos = true;
        } else {
            uredajiFacade.create(uredjaj);
            this.id_unos = "";
            this.naziv_unos = "";
            this.adresa_unos = "";
            this.error_unos = false;
        }

        preuzmiRaspolziveIoTuredaje();

        dnevnik(pocetak);
        return "dodan_IoT";
    }

    /**
     * Metoda koja preuzima odabrane uređaje iz liste raspoloživih uređaja te ih
     * premješta u listu odabranih uređaja. Zahtjev se na kraju zapisuje u bazu
     * podataka.
     *
     * @return
     */
    public String preuzmiIoT() {
        long pocetak = System.currentTimeMillis();

        if (this.popisRaspolozivihUredaja != null && !this.popisRaspolozivihUredaja.isEmpty()) {
            List<Izbornik> lista = new ArrayList<>();
            for (String odabran : this.popisRaspolozivihUredaja) {
                for (Izbornik i : this.raspolozivi_uredaji) {
                    if (i.getVrijednost().equals(odabran)) {
                        this.odabrani_uredjaji.add(i);
                        lista.add(i);
                    }
                }
            }

            this.popisRaspolozivihUredaja.clear();

            for (Izbornik i : lista) {
                this.raspolozivi_uredaji.remove(i);
            }
        }

        dnevnik(pocetak);
        return "preuzeto";
    }

    /**
     * Metoda koja preuzima odabrane uređaje iz liste odabranih uređaja te ih
     * vraća u listu raspoloživih uređaja. Zahtjev se na kraju zapisuje u bazu
     * podataka.
     *
     * @return
     */
    public String vratiIoT() {
        long pocetak = System.currentTimeMillis();

        if (this.popisOdabranihUredaja != null && !this.popisOdabranihUredaja.isEmpty()) {
            List<Izbornik> lista = new ArrayList<>();
            for (String odabran : this.popisOdabranihUredaja) {
                for (Izbornik i : this.odabrani_uredjaji) {
                    if (i.getVrijednost().equals(odabran)) {
                        this.raspolozivi_uredaji.add(i);
                        lista.add(i);
                    }
                }
            }

            this.popisOdabranihUredaja.clear();

            for (Izbornik i : lista) {
                this.odabrani_uredjaji.remove(i);
            }
        }

        dnevnik(pocetak);
        return "vraceno";
    }

    /**
     * Metoda koja preuzima podatke za odabrani uređaj te ih smješta u formu za
     * ažuriranje uređaja. Zahtjev se na kraju zapisuje u bazu podataka.
     *
     * @return
     */
    public String editIoT() {
        long pocetak = System.currentTimeMillis();

        if (this.popisRaspolozivihUredaja != null && !this.popisRaspolozivihUredaja.isEmpty() && this.popisRaspolozivihUredaja.size() == 1) {
            if (this.edit) {
                this.edit = false;
            } else {
                EntityManager entitiyManager = uredajiFacade.getEntityManager();
                CriteriaBuilder cq = entitiyManager.getCriteriaBuilder();
                CriteriaQuery<Uredaji> q = cq.createQuery(Uredaji.class);
                Root<Uredaji> c = q.from(Uredaji.class);
                ParameterExpression<Integer> p = cq.parameter(Integer.class);
                q.select(c).where(cq.equal(c.get("id"), p));
                TypedQuery<Uredaji> query = entitiyManager.createQuery(q);
                query.setParameter(p, Integer.parseInt(this.popisRaspolozivihUredaja.get(0)));
                List<Uredaji> rezultat = query.getResultList();

                Uredaji u = rezultat.get(0);
                GMKlijent gms = new GMKlijent();
                String adresa = gms.getAddress(String.valueOf(u.getLatitude()), String.valueOf(u.getLongitude()));

                this.uredaj_edit = u;
                this.adresa_edit = adresa;
                this.naziv_edit = u.getNaziv();
                this.id_edit = u.getId().toString();

                this.edit = true;
            }
        }

        dnevnik(pocetak);
        return "ureden_iot";
    }

    /**
     * Metoda koja preuzima vremenske prognoze za 5 dana za svaki od odabranih
     * uređaja i sprema ih u listu. Zahtjev se na kraju zapisuje u bazu
     * podataka.
     *
     * @return
     * @throws ParseException
     */
    public String weatherView() throws ParseException {
        long pocetak = System.currentTimeMillis();

        if (this.popisOdabranihUredaja != null && !this.popisOdabranihUredaja.isEmpty()) {
            meteoIoTKlijent.postaviKorisnickePodatke(apiKey());

            if (this.view) {
                if (this.meteo_prognoze != null) {
                    this.meteo_prognoze.clear();
                }
                this.gumbPregledPrognoza = "Prognoze";
                this.view = false;
            } else {
                GMKlijent gms = new GMKlijent();

                EntityManager entitiyManager = uredajiFacade.getEntityManager();
                CriteriaBuilder cq = entitiyManager.getCriteriaBuilder();
                CriteriaQuery<Uredaji> q = cq.createQuery(Uredaji.class);
                Root<Uredaji> c = q.from(Uredaji.class);
                ParameterExpression<Integer> p = cq.parameter(Integer.class);
                q.select(c).where(cq.equal(c.get("id"), p));

                for (String id : this.popisOdabranihUredaja) {
                    TypedQuery<Uredaji> query = entitiyManager.createQuery(q);
                    query.setParameter(p, Integer.parseInt(id));
                    List<Uredaji> rezultat = query.getResultList();

                    Uredaji u = rezultat.get(0);
                    String adresa = gms.getAddress(String.valueOf(u.getLatitude()), String.valueOf(u.getLongitude()));

                    MeteoPrognoza[] mp = meteoIoTKlijent.dajMeteoPrognoze(Integer.parseInt(id), adresa);
                    for (MeteoPrognoza m : mp) {
                        this.meteo_prognoze.add(m);
                    }
                }

                this.gumbPregledPrognoza = "Zatvori prognoze";
                this.view = true;
            }
        }

        dnevnik(pocetak);
        return "view";
    }

    /**
     * Metoda koja ažurira uređaj na bazi podataka te sprema novi zapis u
     * tablicu promjene. Zahtjev se na kraju zapisuje u bazu podataka te se
     * osvježavaju raspoloživi uređaji.
     *
     * @return
     */
    public String azurirajIoT() {
        long pocetak = System.currentTimeMillis();

        if (this.edit && this.id_edit != null && !this.id_edit.isEmpty()
                && this.naziv_edit != null && !this.naziv_edit.isEmpty()
                && this.adresa_edit != null && !this.adresa_edit.isEmpty()) {

            if (uredaj_edit.getId() == Integer.parseInt(this.id_edit)) {
                GMKlijent gm = new GMKlijent();
                Lokacija lokacija = gm.getGeoLocation(this.adresa_edit);

                uredaj_edit.setId(Integer.parseInt(this.id_edit));
                uredaj_edit.setNaziv(this.naziv_edit);
                uredaj_edit.setVrijemePromjene(new Date());
                uredaj_edit.setLatitude(Float.parseFloat(lokacija.getLatitude()));
                uredaj_edit.setLongitude(Float.parseFloat(lokacija.getLongitude()));

                int id = promjeneFacade.findAll().size();
                promjeneFacade.create(new Promjene(id + 1, uredaj_edit.getId(), uredaj_edit.getNaziv(), uredaj_edit.getLatitude(), uredaj_edit.getLongitude(), uredaj_edit.getStatus(), uredaj_edit.getVrijemePromjene(), uredaj_edit.getVrijemeKreiranja()));
                uredajiFacade.edit(uredaj_edit);

                this.error_edit = false;
                this.edit = false;
            } else {
                Uredaji uredaj = uredajiFacade.find(Integer.parseInt(this.id_edit));
                if (uredaj != null) {
                    this.error_edit = true;
                } else {
                    uredajiFacade.remove(uredaj_edit);

                    GMKlijent gm = new GMKlijent();
                    Lokacija lokacija = gm.getGeoLocation(this.adresa_edit);

                    uredaj_edit.setId(Integer.parseInt(this.id_edit));
                    uredaj_edit.setNaziv(this.naziv_edit);
                    uredaj_edit.setVrijemePromjene(new Date());
                    uredaj_edit.setLatitude(Float.parseFloat(lokacija.getLatitude()));
                    uredaj_edit.setLongitude(Float.parseFloat(lokacija.getLongitude()));

                    int id = promjeneFacade.findAll().size();
                    promjeneFacade.create(new Promjene(id + 1, uredaj_edit.getId(), uredaj_edit.getNaziv(), uredaj_edit.getLatitude(), uredaj_edit.getLongitude(), uredaj_edit.getStatus(), uredaj_edit.getVrijemePromjene(), uredaj_edit.getVrijemeKreiranja()));
                    uredajiFacade.edit(uredaj_edit);

                    this.error_edit = false;
                    this.edit = false;
                }
            }
            preuzmiRaspolziveIoTuredaje();
        }

        dnevnik(pocetak);
        return "azuriran";
    }

    /**
     * Metoda koja vraća apikey za openweathermap koji se nalazi u
     * konfiguracijskoj datoteci.
     *
     * @return
     */
    public String apiKey() {
        ServletContext sc = SlusacAplikacije.getKontekst();
        Konfiguracija konf = (Konfiguracija) sc.getAttribute("App_Konfig");
        String key = konf.dajPostavku("apikey");

        return key;
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

    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public String getId_unos() {
        return id_unos;
    }

    public void setId_unos(String id_unos) {
        this.id_unos = id_unos;
    }

    public String getNaziv_unos() {
        return naziv_unos;
    }

    public void setNaziv_unos(String naziv_unos) {
        this.naziv_unos = naziv_unos;
    }

    public String getAdresa_unos() {
        return adresa_unos;
    }

    public void setAdresa_unos(String adresa_unos) {
        this.adresa_unos = adresa_unos;
    }

    public String getId_edit() {
        return id_edit;
    }

    public void setId_edit(String id_edit) {
        this.id_edit = id_edit;
    }

    public String getNaziv_edit() {
        return naziv_edit;
    }

    public void setNaziv_edit(String naziv_edit) {
        this.naziv_edit = naziv_edit;
    }

    public String getAdresa_edit() {
        return adresa_edit;
    }

    public void setAdresa_edit(String adresa_edit) {
        this.adresa_edit = adresa_edit;
    }

    public List<Izbornik> getRaspolozivi_uredaji() {
        return raspolozivi_uredaji;
    }

    public void setRaspolozivi_uredaji(List<Izbornik> raspolozivi_uredaji) {
        this.raspolozivi_uredaji = raspolozivi_uredaji;
    }

    public List<Izbornik> getOdabrani_uredjaji() {
        return odabrani_uredjaji;
    }

    public void setOdabrani_uredjaji(List<Izbornik> odabrani_uredjaji) {
        this.odabrani_uredjaji = odabrani_uredjaji;
    }

    public List<String> getPopisRaspolozivihUredaja() {
        return popisRaspolozivihUredaja;
    }

    public void setPopisRaspolozivihUredaja(List<String> popisRaspolozivihUredaja) {
        this.popisRaspolozivihUredaja = popisRaspolozivihUredaja;
    }

    public List<String> getPopisOdabranihUredaja() {
        return popisOdabranihUredaja;
    }

    public void setPopisOdabranihUredaja(List<String> popisOdabranihUredaja) {
        this.popisOdabranihUredaja = popisOdabranihUredaja;
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    public boolean isView() {
        return view;
    }

    public void setView(boolean view) {
        this.view = view;
    }

    public String getGumbPregledPrognoza() {
        return gumbPregledPrognoza;
    }

    public void setGumbPregledPrognoza(String gumbPregledPrognoza) {
        this.gumbPregledPrognoza = gumbPregledPrognoza;
    }

    public List<MeteoPrognoza> getMeteo_prognoze() {
        return meteo_prognoze;
    }

    public void setMeteo_prognoze(List<MeteoPrognoza> meteo_prognoze) {
        this.meteo_prognoze = meteo_prognoze;
    }

    public Boolean getError_unos() {
        return error_unos;
    }

    public void setError_unos(Boolean error_unos) {
        this.error_unos = error_unos;
    }

    public Boolean getError_edit() {
        return error_edit;
    }

    public void setError_edit(Boolean error_edit) {
        this.error_edit = error_edit;
    }

    public String getShowIot() {
        preuzmiRaspolziveIoTuredaje();
        return showIot;
    }

    public void setShowIot(String showIot) {
        this.showIot = showIot;
    }
}
