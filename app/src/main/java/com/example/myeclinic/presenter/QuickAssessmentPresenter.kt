package com.example.myeclinic.presenter

import com.example.myeclinic.util.UserSession
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject

class QuickAssessmentPresenter(
    private val view: QuickAssessmentView,
    private val specialization: String
) {

    private val db = FirebaseFirestore.getInstance()

    fun loadTimeslots(date: String) {
        val doctorsRef = db.collection("appointments")
            .document(date)
            .collection(specialization)

        doctorsRef.get()
            .addOnSuccessListener { querySnapshot ->
                val timeslotMap = mutableMapOf<String, String>() // time -> doctorId
                val allSlots = mutableMapOf<String, Boolean>() // time -> isAvailable

                for (doc in querySnapshot) {
                    val doctorId = doc.id
                    val timeslots = doc.get("timeslots") as? List<Map<String, Any>> ?: continue
                    for (slot in timeslots) {
                        val hour = slot["hour"] as? String ?: continue
                        val booked = slot["booked"] as? Boolean ?: true
                        if (!booked && !timeslotMap.containsKey(hour)) {
                            timeslotMap[hour] = doctorId
                        }
                        if (!allSlots.containsKey(hour)) {
                            allSlots[hour] = !booked
                        } else {
                            allSlots[hour] = allSlots[hour] == true || !booked
                        }
                    }
                }

                if (allSlots.isEmpty()) {
                    view.showError("No available timeslots.")
                } else {
                    val sorted = allSlots.toSortedMap()
                    view.showTimeslotDialog(date, sorted.keys.toList(), sorted)
                }
            }
            .addOnFailureListener {
                view.showError("Failed to load timeslots: ${it.message}")
            }
    }

    fun loadDoctors(date: String, time: String) {
        val doctorRef = db.collection("appointments").document(date).collection(specialization)

        doctorRef.get().addOnSuccessListener { snapshot ->
            val doctorIdsWithSlot = snapshot.mapNotNull { doc ->
                val doctorId = doc.id
                val timeslots = doc.get("timeslots") as? List<Map<String, Any>> ?: return@mapNotNull null
                if (timeslots.any { it["hour"] == time && it["booked"] == false }) {
                    doctorId
                } else null
            }.toSet()

            if (doctorIdsWithSlot.isEmpty()) {
                view.markHourUnavailable(time)
                view.showError("No doctors available at $time.")
                return@addOnSuccessListener
            }

            db.collection("doctors")
                .whereIn(FieldPath.documentId(), doctorIdsWithSlot.toList())
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val doctors = querySnapshot.documents.mapNotNull {
                        val name = it.getString("name") ?: return@mapNotNull null
                        Pair(it.id, name)
                    }
                    view.showDoctorSelectionDialog(date, time, doctors)
                }
        }.addOnFailureListener {
            view.showError("Failed to load doctors: ${it.message}")
        }
    }

    fun bookAppointment(date: String, hour: String, doctorId: String, description: String) {
        val user = UserSession.currentUser ?: run {
            view.showError("User not logged in.")
            return
        }

        db.collection("doctors").document(doctorId).get()
            .addOnSuccessListener { doctorSnapshot ->
                if (!doctorSnapshot.exists()) {
                    view.showError("Doctor not found")
                    return@addOnSuccessListener
                }

                val doctorName = doctorSnapshot.getString("name") ?: ""

                val timeslotDocRef = db.collection("appointments")
                    .document(date)
                    .collection(specialization)
                    .document(doctorId)

                val bookingRef = db.collection("bookings").document()
                val patientBookingRef = db.collection("patients")
                    .document(user.userId)
                    .collection("bookings")
                    .document(bookingRef.id)

                val doctorBookingRef = db.collection("doctors")
                    .document(doctorId)
                    .collection("bookings")
                    .document(bookingRef.id)

                val availabilityRef = db.collection("doctor_availability").document(doctorId)

                db.runTransaction { transaction ->
                    val snapshot = transaction.get(timeslotDocRef)
                    val availabilitySnapshot = transaction.get(availabilityRef)

                    val timeslots =
                        snapshot.get("timeslots") as? List<Map<String, Any>> ?: emptyList()
                    val availabilityMap =
                        availabilitySnapshot.get("availability") as? Map<String, List<String>>
                            ?: emptyMap()

                    val updatedTimeslots = timeslots.map {
                        if (it["hour"] == hour) {
                            if (it["booked"] == true) {
                                throw FirebaseFirestoreException(
                                    "Time slot already booked",
                                    FirebaseFirestoreException.Code.ABORTED
                                )
                            }
                            it.toMutableMap().apply { this["booked"] = true }
                        } else it
                    }

                    val bookingData = mapOf(
                        "doctorId" to doctorId,
                        "doctorName" to doctorName,
                        "patientId" to user.userId,
                        "patientName" to user.name,
                        "date" to date,
                        "time" to hour,
                        "description" to description,
                        "status" to "booked",
                        "bookedAt" to Timestamp.now()
                    )

                    transaction.set(timeslotDocRef, mapOf("timeslots" to updatedTimeslots), SetOptions.merge())
                    transaction.set(availabilityRef, mapOf("availability" to availabilityMap.toMutableMap().apply {
                        val slots = this[date]?.toMutableList()
                        slots?.remove(hour)
                        if (slots != null) {
                            if (slots.isEmpty()) remove(date)
                            else this[date] = slots
                        }
                    }), SetOptions.merge())

                    transaction.set(bookingRef, bookingData)
                    transaction.set(patientBookingRef, bookingData)
                    transaction.set(doctorBookingRef, bookingData)
                }.addOnSuccessListener {
                    view.showSuccessMessage()
                }.addOnFailureListener {
                    view.showError("Booking failed: ${it.message}")
                }
            }
    }


    interface QuickAssessmentView {
        fun showTimeslotDialog(date: String, timeslots: List<String>, availability: Map<String, Boolean>)
        fun showDoctorSelectionDialog(date: String, time: String, doctorList: List<Pair<String, String>>)
        fun showSuccessMessage()
        fun showError(message: String)
        fun markHourUnavailable(hour: String)
    }
}
