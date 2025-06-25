package com.example.myeclinic.util
/**
 * Represents a single time slot in a doctor's daily schedule.
 *
 * @property booked Indicates whether the time slot has already been booked.
 * @property hour The time represented as a string in "HH:mm" format (e.g., "10:00").
 */
data class Timeslot(
    val booked: Boolean = false,
    val hour: String = ""
)
