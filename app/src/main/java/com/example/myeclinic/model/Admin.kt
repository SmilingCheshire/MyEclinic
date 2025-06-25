package com.example.myeclinic.model

/**
 * Represents an administrator in the eClinic system.
 *
 * @property adminId Unique identifier for the admin.
 * @property name Full name of the admin.
 * @property email Email address associated with the admin.
 * @property contactNumber Contact phone number of the admin.
 * @property role Role of the user, defaults to "Admin".
 */
data class Admin(
    val adminId: String = "",
    val name: String = "",
    val email: String = "",
    val contactNumber: String = "",
    val role: String = "Admin"
)
