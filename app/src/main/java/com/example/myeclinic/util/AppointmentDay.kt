package com.example.myeclinic.util
/**
 * Represents a single day in a doctor's appointment schedule, including available time slots.
 *
 * @property date The date of the appointments (formatted as "YYYY-MM-DD").
 * @property timeslots A list of time slot maps, where each map contains:
 * - `"hour"`: A [String] representing the time (e.g., "10:00").
 * - `"booked"`: A [Boolean] indicating whether the slot is already booked.
 */
data class AppointmentDay(
    val date: String,
    val timeslots: List<Map<String, Any>>
)
