package com.example.myeclinic.model

data class Patient(
    val patientId: String = "",
    val name: String = "",
    val email: String = "",
    val contactNumber: String = "",
    val role: String = "Patient",
    val medicalHistory: String = "" // Example additional field for patients
)
