package com.example.myeclinic.model
import com.google.firebase.Timestamp

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val contactNumber: String = "",
    val role: String = "",
    val timestamp: Timestamp? = null
)
