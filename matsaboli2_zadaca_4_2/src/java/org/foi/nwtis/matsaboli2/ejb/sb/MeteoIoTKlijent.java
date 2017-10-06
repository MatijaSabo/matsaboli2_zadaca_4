/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.matsaboli2.ejb.sb;

import java.text.ParseException;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import org.foi.nwtis.matsaboli2.rest.klijenti.GMKlijent;
import org.foi.nwtis.matsaboli2.rest.klijenti.OWMKlijent;
import org.foi.nwtis.matsaboli2.web.podaci.Lokacija;
import org.foi.nwtis.matsaboli2.web.podaci.MeteoPrognoza;

/**
 *
 * @author Matija Sabolić
 */
@Stateless
@LocalBean
public class MeteoIoTKlijent {

    private String apiKey;

    /**
     * Metoda koja postavlja apiKey koji će se koristiti kod preuzimanja
     * vremenske prognoze.
     *
     * @param apiKey
     */
    public void postaviKorisnickePodatke(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Metoda koja vraća lokaciju za unsesnu adresu uređaja
     *
     * @param adresa
     * @return
     */
    public Lokacija dajLokaciju(String adresa) {
        GMKlijent gmKlijent = new GMKlijent();
        Lokacija lokacija = gmKlijent.getGeoLocation(adresa);
        return lokacija;
    }

    /**
     * Metoda koja vraća vremenske prognoze za sljedecih 5 dana za odabranu
     * adresu uređaja
     *
     * @param id
     * @param adresa
     * @return
     * @throws ParseException
     */
    public MeteoPrognoza[] dajMeteoPrognoze(int id, String adresa) throws ParseException {
        OWMKlijent owmk = new OWMKlijent(apiKey);

        Lokacija lokacija = dajLokaciju(adresa);
        String longitude = lokacija.getLongitude();
        String latitude = lokacija.getLatitude();

        MeteoPrognoza[] prognoza = owmk.getWeatherForecast(id, latitude, longitude);

        return prognoza;
    }

}
