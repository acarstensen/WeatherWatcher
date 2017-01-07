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

    private static String getLastReadingProcessedValue(String label, weatherDataRow){
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
}
