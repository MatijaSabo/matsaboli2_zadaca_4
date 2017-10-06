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
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import org.foi.nwtis.matsaboli2.ejb.eb.Dnevnik;
import org.foi.nwtis.matsaboli2.ejb.eb.Promjene;
import org.foi.nwtis.matsaboli2.ejb.sb.DnevnikFacade;
import org.foi.nwtis.matsaboli2.ejb.sb.PromjeneFacade;

/**
 *
 * @author Matija Sabolić
 */
@Named(value = "pregledPromjena")
@SessionScoped
public class PregledPromjena implements Serializable {

    @EJB
    private PromjeneFacade promjeneFacade;

    @EJB
    private DnevnikFacade dnevnikFacade;

    private String id;
    private String naziv;
    private List<Promjene> lista = new ArrayList<>();

    private TimeZone timeZone;

    /**
     * Creates a new instance of PregledPromjena
     */
    public PregledPromjena() {
    }

    /**
     * Metoda koja uzima unesene fitere korisnika te prema njima radi pretragu
     * sadržaja iz baze podataka. Podaci se spremaju u listu koja se na kraju
     * prikazuje korisniku. Zahtjev se na kraju upisuje u bazu podataka.
     *
     * @return
     */
    public String filtrirajPromjene() {
        long pocetak = System.currentTimeMillis();

        lista.clear();
        if (this.id != null && this.naziv != null && !this.naziv.isEmpty() && !this.id.isEmpty()) {

            EntityManager entitiyManager = promjeneFacade.getEntityManager();
            CriteriaBuilder cq = entitiyManager.getCriteriaBuilder();
            CriteriaQuery<Promjene> q = cq.createQuery(Promjene.class);
            Root<Promjene> c = q.from(Promjene.class);

            ParameterExpression<Integer> p = cq.parameter(Integer.class);
            ParameterExpression<String> s = cq.parameter(String.class);

            q.select(c).where(
                    cq.equal(c.get("id"), p),
                    cq.equal(c.get("naziv"), s)
            );

            TypedQuery<Promjene> query = entitiyManager.createQuery(q);
            query.setParameter(p, Integer.parseInt(this.id));
            query.setParameter(s, this.naziv);
            lista = query.getResultList();

        } else if (this.id != null && !this.id.isEmpty()) {

            EntityManager entitiyManager = promjeneFacade.getEntityManager();
            CriteriaBuilder cq = entitiyManager.getCriteriaBuilder();
            CriteriaQuery<Promjene> q = cq.createQuery(Promjene.class);
            Root<Promjene> c = q.from(Promjene.class);

            ParameterExpression<Integer> p = cq.parameter(Integer.class);

            q.select(c).where(
                    cq.equal(c.get("id"), p)
            );

            TypedQuery<Promjene> query = entitiyManager.createQuery(q);
            query.setParameter(p, Integer.parseInt(this.id));
            lista = query.getResultList();

        } else if (this.naziv != null && !this.naziv.isEmpty()) {

            EntityManager entitiyManager = promjeneFacade.getEntityManager();
            CriteriaBuilder cq = entitiyManager.getCriteriaBuilder();
            CriteriaQuery<Promjene> q = cq.createQuery(Promjene.class);
            Root<Promjene> c = q.from(Promjene.class);

            ParameterExpression<String> s = cq.parameter(String.class);

            q.select(c).where(
                    cq.equal(c.get("naziv"), s)
            );

            TypedQuery<Promjene> query = entitiyManager.createQuery(q);
            query.setParameter(s, this.naziv);
            lista = query.getResultList();

        } else {
            lista = promjeneFacade.findAll();;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public List<Promjene> getLista() {
        return lista;
    }

    public void setLista(List<Promjene> lista) {
        this.lista = lista;
    }

    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

}
