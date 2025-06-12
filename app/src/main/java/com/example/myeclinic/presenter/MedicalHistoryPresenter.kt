package com.example.myeclinic.presenter

import com.example.myeclinic.model.Booking
import com.example.myeclinic.model.Patient
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date

class MedicalHistoryPresenter(private val view: MedicalHistoryView) {

    private val db = FirebaseFirestore.getInstance()

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
