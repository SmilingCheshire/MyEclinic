package com.example.myeclinic.presenter

import com.example.myeclinic.model.Booking
import com.example.myeclinic.model.Patient
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date

class MedicalHistoryPresenter(private val view: MedicalHistoryView) {

    private val db = FirebaseFirestore.getInstance()
    /**
     * Loads all patients from the Firestore `patients` collection.
     *
     * The resulting list of [Patient] objects is passed to [MedicalHistoryView.showPatientList].
     * On failure, [MedicalHistoryView.showError] is called with an error message.
     */
    fun loadAllPatients() {
        db.collection("patients")
            .get()
            .addOnSuccessListener { snapshot ->
                val patients = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Patient::class.java)
                }
                view.showPatientList(patients)
            }
            .addOnFailureListener {
                view.showError("Failed to load patients")
            }
    }
    /**
     * Loads all bookings for a specific patient, sorted in reverse chronological order by booking time.
     *
     * The results are mapped to [Booking] objects along with their Firestore document IDs
     * and passed to [MedicalHistoryView.showBookings].
     *
     * @param patientId The Firestore document ID of the patient.
     */
    fun loadBookings(patientId: String) {
        db.collection("patients")
            .document(patientId)
            .collection("bookings")
            .orderBy("bookedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val bookings = snapshot.documents.mapNotNull { doc ->
                    val booking = doc.toObject(Booking::class.java)
                    if (booking != null) Pair(booking, doc.id) else null
                }
                view.showBookings(bookings)
            }
            .addOnFailureListener {
                view.showError("Failed to load bookings")
            }
    }
    /**
     * Cancels a booking for a given patient and booking ID.
     *
     * This function performs a lookup to gather required metadata (doctor ID, date, etc.)
     * and then delegates the cancellation to an internal transaction function.
     *
     * @param patientId The ID of the patient who owns the booking.
     * @param bookingId The Firestore document ID of the booking.
     * @param bookedAt The original booking timestamp (currently unused but may assist with audit logging).
     */
    fun cancelBooking(patientId: String, bookingId: String, bookedAt: Timestamp?) {

        val bookingRef = db.collection("patients")
            .document(patientId)
            .collection("bookings")
            .document(bookingId)

        bookingRef.get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    view.showError("Booking not found")
                    return@addOnSuccessListener
                }

                val doctorId = doc.getString("doctorId") ?: return@addOnSuccessListener view.showError("Missing doctorId")
                val date = doc.getString("date") ?: return@addOnSuccessListener view.showError("Missing date")
                val hour = doc.getString("time") ?: return@addOnSuccessListener view.showError("Missing time")
                val specialization = doc.getString("specialization") ?: return@addOnSuccessListener view.showError("Missing specialization")

                cancelBookingInternal(patientId, bookingId, date, specialization, doctorId, hour)
            }
            .addOnFailureListener {
                view.showError("Failed to fetch booking: \${it.message}")
            }
    }
    /**
     * Cancels a booking for a given patient and booking ID.
     *
     * This function performs a lookup to gather required metadata (doctor ID, date, etc.)
     * and then delegates the cancellation to an internal transaction function.
     *
     * @param patientId The ID of the patient who owns the booking.
     * @param bookingId The Firestore document ID of the booking.
     * @param bookedAt The original booking timestamp (currently unused but may assist with audit logging).
     */
    private fun cancelBookingInternal(patientId: String, bookingId: String, date: String, specialization: String, doctorId: String, hour: String) {
        val timeslotRef = db.collection("appointments").document(date)
            .collection(specialization).document(doctorId)

        val availabilityRef = db.collection("doctor_availability").document(doctorId)

        val doctorBookingRef = db.collection("doctors")
            .document(doctorId)
            .collection("bookings")
            .document(bookingId)

        val bookingRef = db.collection("bookings").document(bookingId)
        val patientBookingRef = db.collection("patients")
            .document(patientId)
            .collection("bookings")
            .document(bookingId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(timeslotRef)
            val availabilitySnapshot = transaction.get(availabilityRef)

            val timeslots = snapshot.get("timeslots") as? List<Map<String, Any>> ?: emptyList()
            val updatedTimeslots = timeslots.map {
                if (it["hour"] == hour) {
                    it.toMutableMap().apply { this["booked"] = false }
                } else it
            }

            val availabilityMap = availabilitySnapshot.get("availability") as? MutableMap<String, MutableList<String>>
                ?: mutableMapOf()

            val updatedAvailability = availabilityMap.toMutableMap()
            val slots = updatedAvailability[date]?.toMutableList() ?: mutableListOf()
            if (!slots.contains(hour)) slots.add(hour)
            updatedAvailability[date] = slots

            transaction.update(timeslotRef, "timeslots", updatedTimeslots)
            transaction.update(availabilityRef, "availability", updatedAvailability)
            transaction.update(bookingRef, "status", "cancelled")
            transaction.update(patientBookingRef, "status", "cancelled")
            transaction.update(doctorBookingRef, "status", "cancelled")

        }.addOnSuccessListener {
            view.showSuccess("Booking cancelled.")
            loadBookings(patientId)
        }.addOnFailureListener {
            view.showError("Failed to cancel booking: \${it.message}")
        }
    }
}

interface MedicalHistoryView {
    fun showPatientList(patients: List<Patient>)
    fun showBookings(bookings: List<Pair<Booking, String>>)
    fun showError(message: String)
    fun showSuccess(message: String)
}
