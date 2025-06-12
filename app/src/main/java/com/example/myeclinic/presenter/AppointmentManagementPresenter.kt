package com.example.myeclinic.presenter

import com.example.myeclinic.model.Appointment
import com.example.myeclinic.model.Doctor
import com.example.myeclinic.util.Timeslot
import com.example.myeclinic.util.UserSession
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AppointmentManagementPresenter(
    private val view: AppointmentManagementContract.View
) : AppointmentManagementContract.Presenter {

    private val db = FirebaseFirestore.getInstance()

    override fun loadDoctorsBySpecialization(specialization: String) {
        db.collection("doctors")
            .whereEqualTo("specialization", specialization)
            .whereEqualTo("retired", false)
            .get()
            .addOnSuccessListener { snapshot ->
                val doctors = snapshot.documents.mapNotNull {
                    val id = it.getString("doctorId")
                    val name = it.getString("name")
                    if (id != null && name != null) Doctor(id, name) else null
                }
                view.showDoctors(doctors)
            }
            .addOnFailureListener {
                view.showMessage("Failed to load doctors.")
            }
    }


    override fun loadTimeslots(date: String, specialization: String, doctorId: String) {
        val docRef = db.collection("appointments").document(date)
            .collection(specialization).document(doctorId)

        docRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                view.showMessage("No timeslot data found for this doctor on $date.")
                view.showTimeslots(emptyList())
                return@addOnSuccessListener
            }

            val timeslotsRaw = snapshot.get("timeslots")
            if (timeslotsRaw !is List<*>) {
                view.showMessage("Invalid timeslot data format.")
                view.showTimeslots(emptyList())
                return@addOnSuccessListener
            }

            val parsed = timeslotsRaw.mapNotNull { entry ->
                (entry as? Map<*, *>)?.let {
                    val hour = it["hour"] as? String
                    val booked = it["booked"] as? Boolean
                    if (hour != null && booked != null) Timeslot(booked, hour) else null
                }
            }

            view.showTimeslots(parsed)
        }.addOnFailureListener {
            view.showMessage("Failed to load timeslots: ${it.message}")
        }
    }



    override fun createBooking(appointment: Appointment) {
        val db = FirebaseFirestore.getInstance()


        // Step 1: Fetch the patient name first
        db.collection("doctors").document(appointment.doctorId)
            .get()
            .addOnSuccessListener { doctorSnapshot ->
                val specialization = doctorSnapshot.getString("specialization") ?: ""

                db.collection("patients").document(appointment.patientId)
                    .get()
                    .addOnSuccessListener { patientSnapshot ->
                        val patientName = patientSnapshot.getString("name") ?: "Unknown Patient"

                        val bookingData = hashMapOf(
                            "bookedAt" to Timestamp.now(),
                            "date" to appointment.date,
                            "description" to appointment.description,
                            "doctorId" to appointment.doctorId,
                            "doctorName" to appointment.doctorName,
                            "patientId" to appointment.patientId,
                            "patientName" to patientName,
                            "status" to "booked",
                            "specialization" to specialization,
                            "time" to appointment.time
                        )

                        val bookingRef = db.collection("bookings").document()
                        val patientRef = db.collection("patients").document(appointment.patientId)
                            .collection("bookings").document(bookingRef.id)
                        val doctorRef = db.collection("doctors").document(appointment.doctorId)
                            .collection("bookings").document(bookingRef.id)
                        val timeslotRef = db.collection("appointments").document(appointment.date)
                            .collection(appointment.specialization).document(appointment.doctorId)
                        val availabilityRef =
                            db.collection("doctor_availability").document(appointment.doctorId)

                        // Step 2: Get and safely update the timeslots
                        timeslotRef.get().addOnSuccessListener { doc ->
                            val timeslots =
                                doc.get("timeslots") as? List<Map<String, Any>> ?: emptyList()

                            val updatedTimeslots = timeslots.map { slot ->
                                val hour = slot["hour"] as? String
                                if (hour == appointment.time) {
                                    slot.toMutableMap().apply { this["booked"] = true }
                                } else {
                                    slot.toMutableMap()
                                }
                            }

                            // Step 3: Save all booking documents + update timeslot in batch
                            db.runBatch { batch ->
                                batch.set(bookingRef, bookingData)
                                batch.set(patientRef, bookingData)
                                batch.set(doctorRef, bookingData)
                                batch.update(timeslotRef, "timeslots", updatedTimeslots)
                                batch.update(
                                    availabilityRef, mapOf(
                                        "availability.${appointment.date}" to FieldValue.arrayRemove(
                                            appointment.time
                                        )
                                    )
                                )
                            }.addOnSuccessListener {
                                view.showMessage("Booking created.")
                            }.addOnFailureListener {
                                view.showMessage("Failed to create booking.")
                            }

                        }.addOnFailureListener {
                            view.showMessage("Failed to read timeslots.")
                        }

                    }.addOnFailureListener {
                        view.showMessage("Failed to fetch patient info.")
                    }
            }
    }

    override fun cancelBooking(date: String, specialization: String, doctorId: String, hour: String) {
        val timeslotRef = db.collection("appointments").document(date)
            .collection(specialization).document(doctorId)

        val availabilityRef = db.collection("doctor_availability").document(doctorId)


        db.collection("bookings")
            .whereEqualTo("doctorId", doctorId)
            .whereEqualTo("date", date)
            .whereEqualTo("time", hour)
            .whereEqualTo("status", "booked")
            .get()
            .addOnSuccessListener { bookings ->
                if (bookings.isEmpty) {
                    view.showMessage("No booking found to cancel.")
                    return@addOnSuccessListener
                }

                val bookingDoc = bookings.documents.first()
                val bookingId = bookingDoc.id
                val patientId = bookingDoc.getString("patientId") ?: ""

                val patientBookingRef = db.collection("patients")
                    .document(patientId)
                    .collection("bookings")
                    .document(bookingId)

                val doctorBookingRef = db.collection("doctors")
                    .document(doctorId)
                    .collection("bookings")
                    .document(bookingId)

                val bookingRef = db.collection("bookings").document(bookingId)

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
                    view.showMessage("Booking cancelled.")
                }.addOnFailureListener {
                    view.showMessage("Failed to cancel booking: ${it.message}")
                }
            }
            .addOnFailureListener {
                view.showMessage("Failed to find booking: ${it.message}")
            }
    }


    override fun loadSpecializations() {
        db.collection("specializations")
            .get()
            .addOnSuccessListener { snapshot ->
                val specializations = snapshot.documents.mapNotNull { it.getString("name") }
                view.showSpecializations(specializations)
            }
            .addOnFailureListener {
                view.showMessage("Failed to load specializations.")
            }
    }

    override fun loadPatients() {
        db.collection("patients")
            .get()
            .addOnSuccessListener { snapshot ->
                val patientList = snapshot.documents.mapNotNull {
                    val name = it.getString("name")
                    val id = it.id
                    if (name != null) Pair(name, id) else null
                }
                view.showPatients(patientList)
            }
            .addOnFailureListener {
                view.showMessage("Failed to load patients.")
            }
    }


}

interface AppointmentManagementContract {
    interface View {
        fun showTimeslots(timeslots: List<Timeslot>)
        fun showDoctors(doctors: List<Doctor>)
        fun showMessage(message: String)
        fun showSpecializations(specializations: List<String>)
        fun showPatients(patients: List<Pair<String, String>>)
    }

    interface Presenter {
        fun loadDoctorsBySpecialization(specialization: String)
        fun loadTimeslots(date: String, specialization: String, doctorId: String)
        fun createBooking(appointment: Appointment)
        fun cancelBooking(date: String, specialization: String, doctorId: String, hour: String)
        fun loadSpecializations()
        fun loadPatients()
    }
}