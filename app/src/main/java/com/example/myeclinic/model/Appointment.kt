package com.example.myeclinic.model

import com.google.firebase.Timestamp

data class Appointment(
    val doctorId: String = "",
    val doctorName: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val time: String = "",
    val date: String = "",
    val description: String = "",
    val status: String = "",
    val specialization: String = ""
)

data class Booking(
    val doctorId: String = "",
    val doctorName: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val time: String = "",
    val date: String = "",
    val description: String = "",
    val status: String = "",
    val specialization: String = "",
    val bookedAt: Timestamp? = null
)
