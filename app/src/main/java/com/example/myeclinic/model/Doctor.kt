package com.example.myeclinic.model

data class Doctor(
    val doctorId: String = "",
    val name: String = "",
    val email: String = "",
    val specialty: String = "",  // Example additional field for doctors
    val contactNumber: String = "",
    val role: String = "Doctor"
)
