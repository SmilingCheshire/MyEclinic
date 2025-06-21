package com.example.myeclinic.model

data class RequestData(
    val userId: String = "",
    val name: String = "",
    val role: String = "",
    val bio: String? = null,
    val contactNumber: String? = null,
    val specialization: String? = null,
    val retired: Boolean? = null
)
