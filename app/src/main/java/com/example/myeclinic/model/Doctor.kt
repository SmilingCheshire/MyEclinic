package com.example.myeclinic.model

data class Doctor(
    val doctorId: String = "",
    val name: String = "",
    val email: String = "",
    val specialization: String = "",
    val contactNumber: String = "",
    val bio: String = "",
    val role: String = "Doctor",
    val retired: Boolean? = false
)