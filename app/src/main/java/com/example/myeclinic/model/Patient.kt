package com.example.myeclinic.model

/**
 * Represents a patient in the eClinic system.
 *
 * @property patientId Unique identifier assigned to the patient.
 * @property name Full name of the patient.
 * @property email Contact email address of the patient.
 * @property contactNumber Phone number for contacting the patient.
 * @property role User role, defaults to "Patient".
 * @property medicalHistory Medical background or health history of the patient.
 */
data class Patient(
    val patientId: String = "",
    val name: String = "",
    val email: String = "",
    val contactNumber: String = "",
    val role: String = "Patient",
    val medicalHistory: String = "" // Example additional field for patients
)
