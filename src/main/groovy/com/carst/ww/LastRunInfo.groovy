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

class LastRunInfo {
    private static File lastRunFile = new File('src/main/resources/LastRun.info')

    private static String getLastReadingProcessedKey(String label){
        "${label}_LastReadingProcessed"
    }

    private static String getLastReadingProcessedValue(String label, WeatherDataRow weatherDataRow){
        "${getLastReadingProcessedKey(label)}=${weatherDataRow.timestamp.toString()}"
    }

    static void recordLastReadingProcessed(String label, WeatherDataRow weatherDataRow){
        // read all lines in except for the one we want to overwrite
        ArrayList<String> newLastRunFileContents = new ArrayList<String>()
        FileUtils.readLines(lastRunFile).each { String line ->
            if(!line.startsWith(getLastReadingProcessedKey(label))){
                newLastRunFileContents.add(line)
            }
        }

        // add passed in reading
        newLastRunFileContents.add(getLastReadingProcessedValue(label, weatherDataRow))
        FileUtils.writeLines(lastRunFile, newLastRunFileContents)
    }

    static boolean readingWasAlreadyProcessed(String label, WeatherDataRow weatherDataRow){
        boolean alreadyProcessed = false
        FileUtils.readLines(lastRunFile).each { String line ->
            if(line == getLastReadingProcessedValue(label, weatherDataRow)){
                alreadyProcessed = true
            }
        }
        alreadyProcessed
    }

    static String getVal(String key){
        String val = null
        FileUtils.readLines(lastRunFile).each { String line ->
            if(line.startsWith(key)){
                val = line.substring(line.lastIndexOf('=')+1)
            }
        }
        val
    }

    static void writeNewVal(String key, String val){
        ArrayList<String> newLastRunFileContents = new ArrayList<String>()
        String newLine = "${key}=${val}"
        boolean found = false
        FileUtils.readLines(lastRunFile).each { String line ->
            if(line.startsWith(key)){
                line = newLine
                found = true
            }
            newLastRunFileContents.add(line)
        }

        if(!found){
            newLastRunFileContents.add(newLine)
        }
        FileUtils.writeLines(lastRunFile, newLastRunFileContents)
    }

    static String alertCheckCountKey = 'AlertChecksSinceLastDailySummary'
    static void incrementAlertCheckCounter(){
        writeNewVal(this.alertCheckCountKey, (getAlertCheckVal()+1).toString())
    }

    static Integer getAlertCheckVal(){
        String val = getVal(this.alertCheckCountKey)
        if(val == null){
            val = '0'
        }
        Integer.parseInt(val)
    }

    static void resetAlertCheckCounter(){
        writeNewVal(this.alertCheckCountKey, '0')
    }

    static String isSprinklerOnKey = 'isSprinklerOn'
    static boolean isSprinklerOn(){
        Boolean.parseBoolean(getVal(isSprinklerOnKey))
    }

    static String isLowIndoorTempAlertOnKey = 'isLowIndoorTempAlertOn'
    static boolean isLowIndoorTempAlertOn(){
        Boolean.parseBoolean(getVal(isLowIndoorTempAlertOnKey))
    }

    static String isHighIndoorTempAlertOnKey = 'isHighIndoorTempAlertOn'
    static boolean isHighIndoorTempAlertOn(){
        Boolean.parseBoolean(getVal(isHighIndoorTempAlertOnKey))
    }

    static String isHighIndoorHumidityAlertOnKey = 'isHighIndoorHumidityAlertOn'
    static boolean isHighIndoorHumidityAlertOn(){
        Boolean.parseBoolean(getVal(isHighIndoorHumidityAlertOnKey))
    }

}
