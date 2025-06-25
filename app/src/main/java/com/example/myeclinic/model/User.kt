package com.example.myeclinic.model
import com.google.firebase.Timestamp

/**
 * Represents a general user in the eClinic system.
 * This model can be used for any type of user (e.g., Admin, Doctor, Patient),
 * and contains common attributes shared across roles.
 *
 * @property userId Unique identifier assigned to the user.
 * @property name Full name of the user.
 * @property email Email address associated with the user account.
 * @property contactNumber Phone number for contacting the user.
 * @property role Role assigned to the user (e.g., "Admin", "Doctor", "Patient").
 * @property timestamp Optional Firebase timestamp indicating when the user was created or last updated.
 */
data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val contactNumber: String = "",
    val role: String = "",
    val timestamp: Timestamp? = null
)
