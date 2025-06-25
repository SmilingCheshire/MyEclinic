package com.example.myeclinic.model

import com.google.firebase.Timestamp

/**
 * Represents an appointment between a doctor and a patient in the eClinic system.
 *
 * @property doctorId Unique identifier of the doctor.
 * @property doctorName Full name of the doctor.
 * @property patientId Unique identifier of the patient.
 * @property patientName Full name of the patient.
 * @property time Time of the appointment (e.g., "10:30 AM").
 * @property date Date of the appointment (e.g., "2025-06-25").
 * @property description Additional notes or reasons for the appointment.
 * @property status Current status of the appointment (e.g., "Confirmed", "Cancelled").
 * @property specialization Medical specialization related to the appointment.
 */
data class Appointment(
    val doctorId: String = "",
    val doctorName: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val time: String = "",
    val date: String = "",
    val description: String = "",
    val status: String = "",
    val specialization: String = ""
)

/**
 * Represents a booking record for an appointment, including booking metadata.
 *
 * @property doctorId Unique identifier of the doctor.
 * @property doctorName Full name of the doctor.
 * @property patientId Unique identifier of the patient.
 * @property patientName Full name of the patient.
 * @property time Scheduled time of the appointment.
 * @property date Scheduled date of the appointment.
 * @property description Reason or notes for the booking.
 * @property status Current status of the booking (e.g., "Pending", "Confirmed").
 * @property specialization Doctor's area of specialization relevant to the booking.
 * @property bookedAt Firebase timestamp indicating when the booking was made.
 */
data class Booking(
    val doctorId: String = "",
    val doctorName: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val time: String = "",
    val date: String = "",
    val description: String = "",
    val status: String = "",
    val specialization: String = "",
    val bookedAt: Timestamp? = null
)
