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

import java.time.LocalDate
import java.time.temporal.ChronoUnit

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

    static ArrayList<WeatherAlert> processForSprinklerOffOn(Logger log, ArrayList<WeatherAlert> weatherAlerts, List<String> weatherDataRows, Double inchesRainPerWeek){
        log.info("Processing data for sprinklers on/off alert.")

        // read in from log file the last status
        Boolean lastIsSprinklerOn = LastRunInfo.isSprinklerOn()
        log.info("Previous isSprinklerOn from LastRun.info: ${lastIsSprinklerOn}")

        // figure out new status
        Boolean newIsSprinklerOn = false
        String msg
        LocalDate today = LocalDate.now()
        if(today.monthValue >= 5 && today.monthValue<=9){
            log.info("It's May through September!!!  Let's see if sprinklers should be on...")

            // grab the last row and the one from a week ago
            int lastRow = weatherDataRows.size()-1
            WeatherDataRow lastWdr = new WeatherDataRow(weatherDataRows.get(lastRow))
            WeatherDataRow weekAgoWdr
            for(int i=lastRow; i>=0; i--){
                weekAgoWdr = new WeatherDataRow(weatherDataRows.get(i))
                if(ChronoUnit.DAYS.between(weekAgoWdr.timestamp, lastWdr.timestamp) >= 7){
                    // we found our reading, exit loop
                    i=-1
                    log.info("Found a reading from a week ago taken at: ${weekAgoWdr.timestamp}")
                }
            }

            // calculate how much rain in last week
            Double rainInLastWeek = lastWdr.rain - weekAgoWdr.rain
            log.info("lastWdr.rain: ${lastWdr.rain} weekAgoWdr.rain: ${weekAgoWdr.rain} rainInLastWeek: ${rainInLastWeek}")

            if(rainInLastWeek < inchesRainPerWeek){
                msg = "We've had < the threshold of ${inchesRainPerWeek} inches of rain this week.  The sprinklers should be on!"
                newIsSprinklerOn = true
            } else {
                msg = "We've had >= the threshold of ${inchesRainPerWeek} inches of rain this week.  The sprinklers should be off."
            }
            log.info(msg)
            msg = "${msg}\n\nRain in the last week: ${rainInLastWeek} inches"
        } else {
            log.info("It's October through April :(  No need for sprinklers.")
            msg = "Winter is coming!"
        }


        // See if we should generate an alert
        WeatherAlert wa = null
        log.info("We want the sprinklers ${getOnOffText(newIsSprinklerOn)}.  Last time they were: ${getOnOffText(lastIsSprinklerOn)}")
        if(lastIsSprinklerOn == newIsSprinklerOn){
            log.info("Sprinklers were already ${getOnOffText(newIsSprinklerOn)} so we are done.")
        } else {
            log.info("Sprinklers were in a different state!?  Setting alert to get them turned ${getOnOffText(newIsSprinklerOn)}.")
            wa = new WeatherAlert(typeCd: 'SprinklerOffOn', type: "Turn sprinklers ${getOnOffText(newIsSprinklerOn)}",
                    description: "${msg}\n\nTurn ${getOnOffText(newIsSprinklerOn)} your sprinklers!!")
        }

        // now that we are all done, write the status out to log and send back the alert or null
        log.info("Writing out isSprinklerOn to LastRun.info: ${newIsSprinklerOn}")
        LastRunInfo.writeNewVal(LastRunInfo.isSprinklerOnKey, newIsSprinklerOn.toString())
        addNewWeatherAlert(weatherAlerts, wa)
    }

    private static getOnOffText(Boolean isOn){
        String text = 'off'
        if(isOn){
            text = 'on'
        }
        text
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
