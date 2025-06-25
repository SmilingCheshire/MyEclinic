package com.example.myeclinic.util

/**
 * Holds predefined scheduling constants used across the application.
 */
object ScheduleConstants {

    /**
     * A list of standard working hours used for doctor availability and appointment scheduling.
     *
     * Time slots are formatted as "HH:mm" and represent typical business hours,
     * excluding lunch breaks and outside-office times.
     */
    val workingHours = listOf(
        "08:00", "09:00", "10:00", "11:00",
        "13:00", "14:00", "15:00", "16:00", "17:00"
    )
}