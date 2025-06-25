package com.example.myeclinic.model

/**
 * Represents a data structure used for role-based user requests,
 * such as onboarding, profile update requests, or role change submissions.
 *
 * This can be used by admins to review and process user-submitted data.
 *
 * @property userId Unique identifier of the user making the request.
 * @property name Full name of the user.
 * @property role Role of the user (e.g., "Doctor", "Patient").
 * @property bio Optional biography or professional summary (typically for doctors).
 * @property contactNumber Optional phone number provided by the user.
 * @property specialization Optional medical specialization (relevant for doctor roles).
 * @property retired Optional flag indicating if the user (doctor) is retired.
 */
data class RequestData(
    val userId: String = "",
    val name: String = "",
    val role: String = "",
    val bio: String? = null,
    val contactNumber: String? = null,
    val specialization: String? = null,
    val retired: Boolean? = null
)
