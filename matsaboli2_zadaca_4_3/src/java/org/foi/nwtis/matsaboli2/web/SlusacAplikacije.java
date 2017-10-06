/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.matsaboli2.web;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.foi.nwtis.matsaboli2.konfiguracije.Konfiguracija;
import org.foi.nwtis.matsaboli2.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.matsaboli2.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.matsaboli2.konfiguracije.NemaKonfiguracije;

/**
 * Web application lifecycle listener.
 *
 * @author Matija Sabolić
 */
@WebListener
public class SlusacAplikacije implements ServletContextListener {

    static ServletContext kontekst = null;

    public static ServletContext getKontekst() {
        return kontekst;
    }

    /**
     * Metoda koja se poziva prilikom poretanja aplikacije. Učitava se
     * konfiguracijska datoteka te se nakon toga sprema u kontekst aplikacije.
     *
     * @param sce
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String datoteka = sce.getServletContext().getRealPath("/WEB-INF") + File.separator + sce.getServletContext().getInitParameter("konfiguracija");

        Konfiguracija konf = null;
        try {
            konf = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datoteka);
            sce.getServletContext().setAttribute("App_Konfig", konf);
        } catch (NemaKonfiguracije ex) {
            Logger.getLogger(SlusacAplikacije.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NeispravnaKonfiguracija ex) {
            Logger.getLogger(SlusacAplikacije.class.getName()).log(Level.SEVERE, null, ex);
        }

        kontekst = sce.getServletContext();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
