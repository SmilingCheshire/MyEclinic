package com.example.myeclinic.util

data class AppointmentDay(
    val date: String,
    val timeslots: List<Map<String, Any>>
)
