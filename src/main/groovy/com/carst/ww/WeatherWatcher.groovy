/*
 * Copyright Medtronic, Inc. 2016
 *
 * MEDTRONIC CONFIDENTIAL - This document is the property of Medtronic,
 * Inc., and must be accounted for. Information herein is confidential. Do
 * not reproduce it, reveal it to unauthorized persons, or send it outside
 * Medtronic without proper authorization.
 */

package com.carst.ww
import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
/**
 * Tool to grab the Gherkin from Cucumber feature files and push it to Code Beamer.
 */
class WeatherWatcher {
    Logger log = LoggerFactory.getLogger(this.class)

    public static void main(String[] args) {
        if(args.size() == 0){
            throw new Exception("\n\nYou need to pass the following program arguments:\n" +
                    "- acuriteweather.csv file location\n" +
                    "- email address\n" +
                    "- low indoor temperature alert threshold temp\n" +
                    "- high indoor humidity alert threshold")
        }

        File weatherDataCSV = new File(args[0])
        String emailAddress = args[1]
        Integer lowIndoorTempThreshold = Integer.parseInt(args[2])
        Integer highIndoorHumidityThreshold = Integer.parseInt(args[3])
        WeatherWatcher ww = new WeatherWatcher()
        ww.go(weatherDataCSV, emailAddress, lowIndoorTempThreshold, highIndoorHumidityThreshold)
    }

    void go(File weatherDataCSV, String emailAddress, Integer lowIndoorTempThreshold, Integer highIndoorHumidityThreshold){
        log.info("Hello I am the Weather Watcher.  Here are my settings:\n" +
                "     acuriteweather.csv file location: ${weatherDataCSV.absolutePath}\n" +
                "     email address: ${emailAddress}\n" +
                "     low indoor temperature alert threshold temp: ${lowIndoorTempThreshold}\n" +
                "     high indoor humidity alert threshold: ${highIndoorHumidityThreshold}")
        List<String> weatherDataRows = FileUtils.readLines(weatherDataCSV)

        // make sure the csv file is still in the same format the code expects
        assert weatherDataRows.get(0) == "Timestamp,Outdoor Temperature,Outdoor Humidity,Dew Point,Heat Index,Wind Chill,Barometric Pressure,Rain,Wind Speed,Wind Average,Peak Wind,Wind Direction,Indoor Temperature,Indoor Humidity" : "The schema of the CSV file must have change!?!\n"

        // process all alerts
        ArrayList<WeatherAlert> weatherAlerts = new ArrayList<WeatherAlert>()
        weatherAlerts = WeatherAlert.processForLowIndoorTemp(log, weatherAlerts, weatherDataRows, lowIndoorTempThreshold)
        weatherAlerts = WeatherAlert.processForHighIndoorHumidity(log, weatherAlerts, weatherDataRows, highIndoorHumidityThreshold)

        // if there are any alerts, send email
        if(weatherAlerts.size() > 0){
            EmailAlert.sendAlertEmail(log, emailAddress, weatherAlerts)
        } else {
            log.info("Not emailing as there are no alerts.")
        }
        log.info("Weather Watcher out.")
    }
}
