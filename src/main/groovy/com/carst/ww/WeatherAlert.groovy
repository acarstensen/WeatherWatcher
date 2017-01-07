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

class WeatherAlert {
    String typeCd
    String type
    String description

    static ArrayList<WeatherAlert> processForLowIndoorTemp(Logger log, ArrayList<WeatherAlert> weatherAlerts, List<String> weatherDataRows, Integer lowIndoorTempThreshhold){
        log.info("Processing data for Low Indoor Temp alert.")

        // grab the last row only
        WeatherDataRow wdr = new WeatherDataRow(weatherDataRows.get(weatherDataRows.size()-1))

        // process the alert
        String lastReadingProcessedLabel = 'processForLowIndoorTemp'
        WeatherAlert wa = null
        if(LastRunInfo.readingWasAlreadyProcessed(lastReadingProcessedLabel, wdr)){
            log.info("     No alert generated as this reading was already processed.")
        } else if(wdr.indoorTemperature < lowIndoorTempThreshhold){
            log.info("     Alert Generated!!!")
            wa = new WeatherAlert(typeCd: 'LowIndoorTemp', type: 'Low Indoor Temperature',
                    description: "The latest indoor temperature was too low!\n" +
                            "     Temp: ${wdr.indoorTemperature}\n" +
                            "     Threshold: ${lowIndoorTempThreshhold}\n" +
                            "     Timestamp: ${wdr.timestamp}\n")
        }

        LastRunInfo.recordLastReadingProcessed(lastReadingProcessedLabel, wdr)
        addNewWeatherAlert(weatherAlerts, wa)
    }

    static ArrayList<WeatherAlert> processForHighIndoorHumidity(Logger log, ArrayList<WeatherAlert> weatherAlerts, List<String> weatherDataRows, Integer highIndoorHumidityThreshhold){
        log.info("Processing data for High Indoor Humidity alert.")

        // grab the last row only
        WeatherDataRow wdr = new WeatherDataRow(weatherDataRows.get(weatherDataRows.size()-1))

        // process the alert
        String lastReadingProcessedLabel = 'processForHighIndoorHumidity'
        WeatherAlert wa = null
        if(LastRunInfo.readingWasAlreadyProcessed(lastReadingProcessedLabel, wdr)){
            log.info("     No alert generated as this reading was already processed.")
        } else if(wdr.indoorHumidity > highIndoorHumidityThreshhold){
            log.info("     Alert Generated!!!")
            wa = new WeatherAlert(typeCd: 'LowIndoorHumidity', type: 'High Indoor Humidity',
                    description: "The latest indoor humidity was too high!\n" +
                            "     Humidity: ${wdr.indoorHumidity}\n" +
                            "     Threshold: ${highIndoorHumidityThreshhold}\n" +
                            "     Timestamp: ${wdr.timestamp}\n")
        }

        LastRunInfo.recordLastReadingProcessed(lastReadingProcessedLabel, wdr)
        addNewWeatherAlert(weatherAlerts, wa)
    }

    private static ArrayList<WeatherAlert> addNewWeatherAlert(ArrayList<WeatherAlert> weatherAlerts, WeatherAlert weatherAlert){
        if(weatherAlert != null){
            weatherAlerts.add(weatherAlert)
        }
        weatherAlerts
    }

    private static void recordLastReadingProcessed(String label){
        FileUtils.readFileToString()

    }
}
