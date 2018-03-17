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

class WeatherWatcher {
    Logger log = LoggerFactory.getLogger(this.class)

    static void main(String[] args) {
        if(args.size() == 0){
            throw new Exception("\n\nYou need to pass the following program arguments:\n" +
                    "- acuriteweather.csv file location\n" +
                    "- email address\n" +
                    "- low indoor temperature alert threshold temp\n" +
                    "- high indoor temperature alert threshold temp\n" +
                    "- inches of rain per week threshold")
        }

        File weatherDataCSV = new File(args[0])
        String emailAddress = args[1]
        Integer lowIndoorTempThreshold = Integer.parseInt(args[2])
        Integer highIndoorTempThreshold = Integer.parseInt(args[3])
        Double inchesRainPerWeek = Double.parseDouble(args[4])
        WeatherWatcher ww = new WeatherWatcher()
        ww.go(weatherDataCSV, emailAddress, lowIndoorTempThreshold, highIndoorTempThreshold, inchesRainPerWeek)
    }

    void go(File weatherDataCSV, String emailAddress, Integer lowIndoorTempThreshold, Integer highIndoorTempThreshold, Double inchesRainPerWeek){
        log.info("Hello I am the Weather Watcher.  Here are my settings:\n" +
                "     acuriteweather.csv file location: ${weatherDataCSV.absolutePath}\n" +
                "     email address: ${emailAddress}\n" +
                "     low indoor temperature alert threshold temp: ${lowIndoorTempThreshold}\n" +
                "     high indoor temperature alert threshold temp: ${highIndoorTempThreshold}")
        List<String> weatherDataRows = FileUtils.readLines(weatherDataCSV)

        // make sure the csv file is still in the same format the code expects
        assert weatherDataRows.get(0) == "Timestamp,Outdoor Temperature,Outdoor Humidity,Dew Point,Heat Index,Wind Chill,Barometric Pressure,Rain,Wind Speed,Wind Average,Peak Wind,Wind Direction,Indoor Temperature,Indoor Humidity" : "The schema of the CSV file must have change!?!\n"

        // process all alerts
        ArrayList<WeatherAlert> weatherAlerts = new ArrayList<WeatherAlert>()
        LastRunInfo.incrementAlertCheckCounter()
        weatherAlerts = WeatherAlert.processForLowIndoorTemp(log, weatherAlerts, weatherDataRows, lowIndoorTempThreshold)
        weatherAlerts = WeatherAlert.processForHighIndoorTemp(log, weatherAlerts, weatherDataRows, highIndoorTempThreshold)
        weatherAlerts = WeatherAlert.processForHighIndoorHumidity(log, weatherAlerts, weatherDataRows)
        weatherAlerts = WeatherAlert.processForSprinklerOffOn(log, weatherAlerts, weatherDataRows, inchesRainPerWeek)

        // if there are any alerts, send email
        if(weatherAlerts.size() > 0){
            log.info("Sending email for ${weatherAlerts.size()} alert(s).")
            String body = ""
            weatherAlerts.each { WeatherAlert wa ->
                body = "${body}Type: ${wa.type}\n" +
                        "Description: ${wa.description}\n\n"
            }

            Email.sendEmail(emailAddress, "Weather Watcher Alert!", body)
        } else {
            log.info("Not emailing as there are no alerts.")
        }

        // send daily summary email
        DailySummary dailySummary = new DailySummary()
        dailySummary.sendDailyEmail(emailAddress, weatherDataRows)

        log.info("Weather Watcher out.")
    }
}
