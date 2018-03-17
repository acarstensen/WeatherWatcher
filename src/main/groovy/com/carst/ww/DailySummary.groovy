/*
 * Copyright Medtronic, Inc. 2016
 *
 * MEDTRONIC CONFIDENTIAL - This document is the property of Medtronic,
 * Inc., and must be accounted for. Information herein is confidential. Do
 * not reproduce it, reveal it to unauthorized persons, or send it outside
 * Medtronic without proper authorization.
 */

package com.carst.ww
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DailySummary {
    Logger log = LoggerFactory.getLogger(this.class)
    private String lastRunInfoDailySummaryEmailKey = 'DailySummaryEmailLastSend'

    void sendDailyEmail(String emailAddress, List<String> weatherDataRows){
        log.info("Seeing if it's time to send the daily summary email.")
        if(timeToSendDailySummaryEmail()){
            String emailBody = makeEmailBody(weatherDataRows)
            Email.sendEmail(emailAddress, "Daily Weather Watcher Summary", emailBody)

            // record the date of the last email sent
            LastRunInfo.writeNewVal(this.lastRunInfoDailySummaryEmailKey, LocalDateTime.now().toString())
        }
    }

    private String makeEmailBody(List<String> weatherDataRows){
        List<WeatherDataRow> wdrs = getYesterdaysWeatherDataRows(weatherDataRows)
        WeatherDataRow lowOutdoorTemp = wdrs.get(0)
        WeatherDataRow highOutdoorTemp = wdrs.get(0)
        WeatherDataRow lowIndoorTemp = wdrs.get(0)
        WeatherDataRow highIndoorTemp = wdrs.get(0)
        WeatherDataRow lowOutdoorHumidity = wdrs.get(0)
        WeatherDataRow highOutdoorHumidity = wdrs.get(0)
        WeatherDataRow lowIndoorHumidity = wdrs.get(0)
        WeatherDataRow highIndoorHumidity = wdrs.get(0)
        WeatherDataRow lowWindchill = wdrs.get(0)
        WeatherDataRow highWindchill = wdrs.get(0)
        WeatherDataRow lowBarometricPressure = wdrs.get(0)
        WeatherDataRow highBarometricPressure = wdrs.get(0)
        WeatherDataRow lowRain = wdrs.get(0)
        WeatherDataRow highRain = wdrs.get(0)
        WeatherDataRow lowWindSpeed = wdrs.get(0)
        WeatherDataRow highWindSpeed = wdrs.get(0)

        wdrs.each { WeatherDataRow wdr ->
            if(wdr.outdoorTemperature < lowOutdoorTemp.outdoorTemperature){
                lowOutdoorTemp = wdr
            }
            if(wdr.outdoorTemperature > highOutdoorTemp.outdoorTemperature){
                highOutdoorTemp = wdr
            }
            if(wdr.indoorTemperature < lowIndoorTemp.indoorTemperature){
                lowIndoorTemp = wdr
            }
            if(wdr.indoorTemperature > highIndoorTemp.indoorTemperature){
                highIndoorTemp = wdr
            }
            if(wdr.outdoorHumidity < lowOutdoorHumidity.outdoorHumidity){
                lowOutdoorHumidity = wdr
            }
            if(wdr.outdoorHumidity > highOutdoorHumidity.outdoorHumidity){
                highOutdoorHumidity = wdr
            }
            if(wdr.indoorHumidity < lowIndoorHumidity.indoorHumidity){
                lowIndoorHumidity = wdr
            }
            if(wdr.indoorHumidity > highIndoorHumidity.indoorHumidity){
                highIndoorHumidity = wdr
            }
            if(wdr.windChill < lowWindchill.windChill){
                lowWindchill = wdr
            }
            if(wdr.windChill > highWindchill.windChill){
                highWindchill = wdr
            }
            if(wdr.barometricPressure < lowBarometricPressure.barometricPressure){
                lowBarometricPressure = wdr
            }
            if(wdr.barometricPressure > highBarometricPressure.barometricPressure){
                highBarometricPressure = wdr
            }
            if(wdr.rain < lowRain.rain){
                lowRain = wdr
            }
            if(wdr.rain > highRain.rain){
                highRain = wdr
            }
            if(wdr.windSpeed < lowWindSpeed.windSpeed){
                lowWindSpeed = wdr
            }
            if(wdr.windSpeed > highWindSpeed.windSpeed){
                highWindSpeed = wdr
            }
        }

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")
        String emailBody = "Weather summary for yesterday: ${lowOutdoorTemp.timestamp.format(dayFormatter)}\n\n"
        emailBody = "${emailBody}Low's and High's\n"
        emailBody = appendALowAndHigh(emailBody, "Outdoor Temp",        lowOutdoorTemp.outdoorTemperature,        lowOutdoorTemp.timestamp,        highOutdoorTemp.outdoorTemperature,        highOutdoorTemp.timestamp)
        emailBody = appendALowAndHigh(emailBody, "Outdoor Humidity",    lowOutdoorHumidity.outdoorHumidity,       lowOutdoorHumidity.timestamp,    highOutdoorHumidity.outdoorHumidity,       highOutdoorHumidity.timestamp)
        emailBody = appendALowAndHigh(emailBody, "Wind Speed",          lowWindSpeed.windSpeed,                   lowWindSpeed.timestamp,          highWindSpeed.windSpeed,                   highWindSpeed.timestamp)
        emailBody = appendALowAndHigh(emailBody, "Windchill",           lowWindchill.windChill,                   lowWindchill.timestamp,          highWindchill.windChill,                   highWindchill.timestamp)
        emailBody = appendALowAndHigh(emailBody, "Barometric Pressure", lowBarometricPressure.barometricPressure, lowBarometricPressure.timestamp, highBarometricPressure.barometricPressure, highBarometricPressure.timestamp)
        emailBody = "${emailBody}Rain: ${highRain.rain - lowRain.rain}\n"
        emailBody = appendALowAndHigh(emailBody, "Indoor Temp",         lowIndoorTemp.indoorTemperature,          lowIndoorTemp.timestamp,         highIndoorTemp.indoorTemperature,          highIndoorTemp.timestamp)
        emailBody = appendALowAndHigh(emailBody, "Indoor Humidity",     lowIndoorHumidity.indoorHumidity,         lowIndoorHumidity.timestamp,     highIndoorHumidity.indoorHumidity,         highIndoorHumidity.timestamp)
        emailBody = "${emailBody}\n\n# of readings: ${wdrs.size()}\n"
        emailBody = "${emailBody}# of alert checks since last email: ${LastRunInfo.getAlertCheckVal().toString()}\n"
        LastRunInfo.resetAlertCheckCounter()
        emailBody
    }

    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    private String appendALowAndHigh(String emailBody, String label, def lowVal, LocalDateTime lowTime, def highVal, LocalDateTime highTime){
        "${emailBody}${label}: ${lowVal} (${lowTime.format(timeFormatter)}) -> ${highVal} (${highTime.format(timeFormatter)})\n"
    }

    private List<WeatherDataRow> getYesterdaysWeatherDataRows(List<String> weatherDataRows){
        // start with the last row and work backwards
        ArrayList<WeatherDataRow> wdrs = new ArrayList<WeatherDataRow>()
        int row = weatherDataRows.size()-1
        boolean stillLooking = true
        boolean foundSomeYesterdays = false
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1)
        LocalDateTime startOfYesterday = LocalDateTime.of(yesterday.year, yesterday.monthValue, yesterday.dayOfMonth, 0, 0, 0)
        LocalDateTime endOfYesterday = LocalDateTime.of(yesterday.year, yesterday.monthValue, yesterday.dayOfMonth, 23, 59, 59)

        while(stillLooking){
            WeatherDataRow wdr = new WeatherDataRow(weatherDataRows.get(row))
            if(wdr.timestamp >= startOfYesterday && wdr.timestamp <= endOfYesterday){
                wdrs.add(wdr)
                foundSomeYesterdays = true
            } else {
                if(foundSomeYesterdays){
                    stillLooking = false
                }
            }
            row--
        }

        wdrs
    }

    private boolean timeToSendDailySummaryEmail(){
        boolean timeToSend = false
        String val = LastRunInfo.getVal(this.lastRunInfoDailySummaryEmailKey)
        if(val == null){
            log.info("     Let's send one as no summary email has ever been sent.")
            timeToSend = true
        } else {
            LocalDateTime dailySummaryEmailLastSend = LocalDateTime.parse(val)
            LocalDateTime now = LocalDateTime.now()
            if(dailySummaryEmailLastSend.getDayOfYear() != now.dayOfYear){
                log.info("     No email has been sent today.")
                if(now.getDayOfWeek() == DayOfWeek.SATURDAY || now.getDayOfWeek() == DayOfWeek.SUNDAY){
                    log.info("     It's Saturday or Sunday.")
                    if(now.getHour() >= 8){
                        log.info("     Let's send one as it's passed 8am.")
                        timeToSend = true
                    } else {
                        log.info("     Not sending as it's not passed 8am yet.")
                    }
                } else {
                    log.info("     It's Monday - Friday.")
                    if(now.getHour() >= 7){
                        log.info("     Let's send one as it's passed 7am.")
                        timeToSend = true
                    } else {
                        log.info("     Not sending as it's not passed 7am yet.")
                    }
                }
            } else {
                log.info("     Not sending as one was already sent today.")
            }
        }
        timeToSend
    }
}
