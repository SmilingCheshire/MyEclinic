package com.example.myeclinic.model

/**
 * Represents a doctor within the eClinic system.
 *
 * @property doctorId Unique identifier assigned to the doctor.
 * @property name Full name of the doctor.
 * @property email Contact email address of the doctor.
 * @property specialization Medical field in which the doctor specializes (e.g., "Cardiology").
 * @property contactNumber Phone number for direct communication with the doctor.
 * @property bio Short biography or description about the doctor’s experience or background.
 * @property role User role, defaulting to "Doctor".
 * @property retired Indicates whether the doctor is retired; `false` by default.
 */
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