package com.example.myeclinic.model

data class Admin(
    val adminId: String = "",
    val name: String = "",
    val email: String = "",
    val contactNumber: String = "",
    val role: String = "Admin"
)
