/*
 * Copyright Medtronic, Inc. 2016
 *
 * MEDTRONIC CONFIDENTIAL - This document is the property of Medtronic,
 * Inc., and must be accounted for. Information herein is confidential. Do
 * not reproduce it, reveal it to unauthorized persons, or send it outside
 * Medtronic without proper authorization.
 */

package com.carst.ww

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WeatherDataRow {
    LocalDateTime timestamp
    Double outdoorTemperature
    Integer outdoorHumidity
    Integer dewPoint
    Integer heatIndex
    Integer windChill
    Double barometricPressure
    Double rain
    Double windSpeed
    Double windAverage
    Double peakWind
    Double windDirection
    Double indoorTemperature
    Integer indoorHumidity

    WeatherDataRow(String csvWeatherDataRow){
        csvWeatherDataRow = csvWeatherDataRow.replace('"','')
        String[] cols = csvWeatherDataRow.split(',')
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a")
        this.timestamp = LocalDateTime.parse(cols[0], formatter)
        this.outdoorTemperature = Double.parseDouble(cols[1])
        this.outdoorHumidity = Integer.parseInt(cols[2])
        this.dewPoint = Integer.parseInt(cols[3])
        this.heatIndex = Integer.parseInt(cols[4])
        this.windChill = Integer.parseInt(cols[5])
        this.barometricPressure = Double.parseDouble(cols[6])
        this.rain = Double.parseDouble(cols[7])
        this.windSpeed = Double.parseDouble(cols[8])
        this.windAverage = Double.parseDouble(cols[9])
        this.peakWind = Double.parseDouble(cols[10])
        this.windDirection = Double.parseDouble(cols[11])
        this.indoorTemperature = Double.parseDouble(cols[12])
        this.indoorHumidity = Integer.parseInt(cols[13])
    }
}
