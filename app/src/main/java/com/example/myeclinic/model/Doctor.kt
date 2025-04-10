package com.example.myeclinic.model

data class Doctor(
    val doctorId: String = "",
    val name: String = "",
    val email: String = "",
    val specialty: String = "",
    val contactNumber: String = "",
    val bio: String = "",
    val availability: Map<String, List<String>> = mapOf(
        "Monday" to emptyList(),
        "Tuesday" to emptyList(),
        "Wednesday" to emptyList(),
        "Thursday" to emptyList(),
        "Friday" to emptyList()
    ),
    val role: String = "Doctor"
)