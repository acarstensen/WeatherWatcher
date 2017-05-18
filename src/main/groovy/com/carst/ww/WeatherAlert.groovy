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

    static ArrayList<WeatherAlert> processForHighIndoorTemp(Logger log, ArrayList<WeatherAlert> weatherAlerts, List<String> weatherDataRows, Integer highIndoorTempThreshhold){
        log.info("Processing data for High Indoor Temp alert.")

        // grab the last row only
        WeatherDataRow wdr = new WeatherDataRow(weatherDataRows.get(weatherDataRows.size()-1))

        // process the alert
        String lastReadingProcessedLabel = 'processFoHighIndoorTemp'
        WeatherAlert wa = null
        if(LastRunInfo.readingWasAlreadyProcessed(lastReadingProcessedLabel, wdr)){
            log.info("     No alert generated as this reading was already processed.")
        } else if(wdr.indoorTemperature > highIndoorTempThreshhold){
            log.info("     Alert Generated!!!")
            wa = new WeatherAlert(typeCd: 'HighIndoorTemp', type: 'High Indoor Temperature',
                    description: "The latest indoor temperature was too high!\n" +
                            "     Temp: ${wdr.indoorTemperature}\n" +
                            "     Threshold: ${highIndoorTempThreshhold}\n" +
                            "     Timestamp: ${wdr.timestamp}\n")
        }

        LastRunInfo.recordLastReadingProcessed(lastReadingProcessedLabel, wdr)
        addNewWeatherAlert(weatherAlerts, wa)
    }

    static ArrayList<WeatherAlert> processForHighIndoorHumidity(Logger log, ArrayList<WeatherAlert> weatherAlerts, List<String> weatherDataRows){
        log.info("Processing data for High Indoor Humidity alert.")

        // grab the last row only
        WeatherDataRow wdr = new WeatherDataRow(weatherDataRows.get(weatherDataRows.size()-1))

        // process the alert
        String lastReadingProcessedLabel = 'processForHighIndoorHumidity'
        WeatherAlert wa = null

        Integer highIndoorHumidityThreshhold = 0
        String subMsg
        if(wdr.outdoorTemperature >= 40){
            highIndoorHumidityThreshhold = 55
            subMsg = ">= 40 degrees"
            // Thresholds taken from: http://www.startribune.com/fixit-what-is-the-ideal-winter-indoor-humidity-level/11468916/
            // If outside temperature is 20 to 40 degrees (current: ${wdr.outdoorTemperature}), humidity indoors should not be more than 40 percent.
        } else if(wdr.outdoorTemperature >= 20 && wdr.outdoorTemperature < 40){
            highIndoorHumidityThreshhold = 40
            subMsg = "20 to 40 degrees"
            // If outside temperature is 10 to 20 degrees (current: ${wdr.outdoorTemperature}), humidity indoors should not be more than 35 percent.
        } else if(wdr.outdoorTemperature >= 10 && wdr.outdoorTemperature < 20){
            highIndoorHumidityThreshhold = 35
            subMsg = "10 to 20 degrees"
            // If outside temperature is 0 to 10 degrees (current: ${wdr.outdoorTemperature}), humidity indoors should not be more than 30 percent.
        } else if(wdr.outdoorTemperature >= 0 && wdr.outdoorTemperature < 10){
            highIndoorHumidityThreshhold = 30
            subMsg = "0 to 10 degrees"
            // If outside temperature is 10-below to 0, humidity indoors should not be more than 25 percent.
        } else if(wdr.outdoorTemperature >= -10 && wdr.outdoorTemperature < 0){
            highIndoorHumidityThreshhold = 25
            subMsg = "-10 to 0 degrees"
            // If outside temperature is 20-below to 10-below, humidity indoors should not be more than 20 percent.
        } else if(wdr.outdoorTemperature >= -20 && wdr.outdoorTemperature < -10){
            highIndoorHumidityThreshhold = 20
            subMsg = "-20 to -10 degrees"
            // If outdoor temperature is lower than 20-below, inside humidity should not be more than 15 percent.
        } else if(wdr.outdoorTemperature < -20){
            highIndoorHumidityThreshhold = 15
            subMsg = "less than -20 degrees"
        }
        String message = "Outdoor temp is ${subMsg} (current: ${wdr.outdoorTemperature}), humidity indoors should not be more than ${highIndoorHumidityThreshhold} percent."

        if(LastRunInfo.readingWasAlreadyProcessed(lastReadingProcessedLabel, wdr)){
            log.info("     No alert generated as this reading was already processed.")
            // https://www.reference.com/home-garden/recommended-indoor-humidity-level-homes-f35a6556707f6bb
        } else if(wdr.indoorHumidity > highIndoorHumidityThreshhold){
            log.info("     Alert Generated!!!")
            wa = new WeatherAlert(typeCd: 'LowIndoorHumidity', type: 'High Indoor Humidity',
                    description: "The latest indoor humidity was too high!\n" +
                            "     Humidity: ${wdr.indoorHumidity}\n" +
                            "     Threshold: ${message}\n" +
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
