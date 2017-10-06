/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.matsaboli2.rest.klijenti;

import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.foi.nwtis.matsaboli2.web.podaci.MeteoPodaci;
import org.foi.nwtis.matsaboli2.web.podaci.MeteoPrognoza;

/**
 *
 * @author Matija Sabolić
 */
public class OWMKlijent {

    String apiKey;
    OWMRESTHelper helper;
    Client client;

    public OWMKlijent(String apiKey) {
        this.apiKey = apiKey;
        helper = new OWMRESTHelper(apiKey);
        client = ClientBuilder.newClient();
    }

    public MeteoPodaci getRealTimeWeather(String latitude, String longitude) {
        WebTarget webResource = client.target(OWMRESTHelper.getOWM_BASE_URI())
                .path(OWMRESTHelper.getOWM_Current_Path());
        webResource = webResource.queryParam("lat", latitude);
        webResource = webResource.queryParam("lon", longitude);
        webResource = webResource.queryParam("lang", "hr");
        webResource = webResource.queryParam("units", "metric");
        webResource = webResource.queryParam("APIKEY", apiKey);

        String odgovor = webResource.request(MediaType.APPLICATION_JSON).get(String.class);
        try {
            JsonReader reader = Json.createReader(new StringReader(odgovor));

            JsonObject jo = reader.readObject();

            MeteoPodaci mp = new MeteoPodaci();
            mp.setSunRise(new Date(jo.getJsonObject("sys").getJsonNumber("sunrise").bigDecimalValue().longValue() * 1000));
            mp.setSunSet(new Date(jo.getJsonObject("sys").getJsonNumber("sunset").bigDecimalValue().longValue() * 1000));

            mp.setTemperatureValue(new Double(jo.getJsonObject("main").getJsonNumber("temp").doubleValue()).floatValue());
            mp.setTemperatureMin(new Double(jo.getJsonObject("main").getJsonNumber("temp_min").doubleValue()).floatValue());
            mp.setTemperatureMax(new Double(jo.getJsonObject("main").getJsonNumber("temp_max").doubleValue()).floatValue());
            mp.setTemperatureUnit("celsius");

            mp.setHumidityValue(new Double(jo.getJsonObject("main").getJsonNumber("humidity").doubleValue()).floatValue());
            mp.setHumidityUnit("%");

            mp.setPressureValue(new Double(jo.getJsonObject("main").getJsonNumber("pressure").doubleValue()).floatValue());
            mp.setPressureUnit("hPa");

            mp.setWindSpeedValue(new Double(jo.getJsonObject("wind").getJsonNumber("speed").doubleValue()).floatValue());
            mp.setWindSpeedName("");

            mp.setWindDirectionValue(new Double(jo.getJsonObject("wind").getJsonNumber("deg").doubleValue()).floatValue());
            mp.setWindDirectionCode("");
            mp.setWindDirectionName("");

            mp.setCloudsValue(jo.getJsonObject("clouds").getInt("all"));
            mp.setCloudsName(jo.getJsonArray("weather").getJsonObject(0).getString("description"));
            mp.setPrecipitationMode("");

            mp.setWeatherNumber(jo.getJsonArray("weather").getJsonObject(0).getInt("id"));
            mp.setWeatherValue(jo.getJsonArray("weather").getJsonObject(0).getString("description"));
            mp.setWeatherIcon(jo.getJsonArray("weather").getJsonObject(0).getString("icon"));

            mp.setLastUpdate(new Date(jo.getJsonNumber("dt").bigDecimalValue().longValue() * 1000));
            return mp;

        } catch (Exception ex) {
            Logger.getLogger(OWMKlijent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Metoda koja koristi openeathermap API kako bi se na temelju
     * geolokacijskih podataka uređaja dohvatile vremenske prognoze za
     * sljdedecih 5 dana za taj uređaj.
     *
     * @param id
     * @param latitude
     * @param longitude
     * @return
     * @throws ParseException
     */
    public MeteoPrognoza[] getWeatherForecast(int id, String latitude, String longitude) throws ParseException {
        WebTarget webResource = client.target(OWMRESTHelper.getOWM_BASE_URI())
                .path(OWMRESTHelper.getOWM_Forecast_Path());
        webResource = webResource.queryParam("lat", latitude);
        webResource = webResource.queryParam("lon", longitude);
        webResource = webResource.queryParam("lang", "hr");
        webResource = webResource.queryParam("units", "metric");
        webResource = webResource.queryParam("APIKEY", apiKey);

        String odgovor = webResource.request(MediaType.APPLICATION_JSON).get(String.class);
        JsonReader reader = Json.createReader(new StringReader(odgovor));
        JsonObject jo = reader.readObject();

        JsonArray array = jo.getJsonArray("list");

        List<MeteoPrognoza> mprl = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            JsonObject object = array.getJsonObject(i);

            MeteoPrognoza prognoza = new MeteoPrognoza();
            MeteoPodaci podaci = new MeteoPodaci();

            prognoza.setId(id);

            podaci.setSunRise(new Date());
            podaci.setSunSet(new Date());
            podaci.setTemperatureValue(new Double(object.getJsonObject("main").getJsonNumber("temp").doubleValue()).floatValue());
            podaci.setTemperatureMin(new Double(object.getJsonObject("main").getJsonNumber("temp_min").doubleValue()).floatValue());
            podaci.setTemperatureMax(new Double(object.getJsonObject("main").getJsonNumber("temp_max").doubleValue()).floatValue());
            podaci.setTemperatureUnit("celsius");
            podaci.setHumidityValue(new Double(object.getJsonObject("main").getJsonNumber("humidity").doubleValue()).floatValue());
            podaci.setHumidityUnit("%");
            podaci.setPressureValue(new Double(object.getJsonObject("main").getJsonNumber("pressure").doubleValue()).floatValue());
            podaci.setPressureUnit("hPa");
            podaci.setWindSpeedValue(new Double(object.getJsonObject("wind").getJsonNumber("speed").doubleValue()).floatValue());
            podaci.setWindSpeedName("");
            podaci.setWindDirectionValue(new Double(object.getJsonObject("wind").getJsonNumber("deg").doubleValue()).floatValue());
            podaci.setWindDirectionCode("");
            podaci.setWindDirectionName("");
            podaci.setCloudsValue(object.getJsonObject("clouds").getInt("all"));
            podaci.setCloudsName(object.getJsonArray("weather").getJsonObject(0).getString("description"));
            podaci.setPrecipitationMode("");
            podaci.setWeatherNumber(object.getJsonArray("weather").getJsonObject(0).getInt("id"));
            podaci.setWeatherValue(object.getJsonArray("weather").getJsonObject(0).getString("description"));
            podaci.setWeatherIcon(object.getJsonArray("weather").getJsonObject(0).getString("icon"));

            String string = object.getString("dt_txt");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = format.parse(string);

            int dan = -1;
            int danas = -1;
            podaci.setLastUpdate(date);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            dan = cal.get(Calendar.DAY_OF_YEAR);
            cal.setTime(new Date());
            danas = cal.get(Calendar.DAY_OF_YEAR);

            if (dan != -1 && danas != -1) {
                prognoza.setDan(dan - danas);
            } else {
                prognoza.setDan(0);
            }

            prognoza.setPrognoza(podaci);
            mprl.add(prognoza);
        }

        MeteoPrognoza[] mp = new MeteoPrognoza[mprl.size()];
        int i = 0;
        for (MeteoPrognoza prognoza : mprl) {
            mp[i++] = prognoza;
        }

        return mp;
    }
}
