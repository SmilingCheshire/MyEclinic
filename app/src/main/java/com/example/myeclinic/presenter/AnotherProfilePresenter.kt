package com.example.myeclinic.presenter

import com.example.myeclinic.model.Doctor
import com.example.myeclinic.model.Patient
import com.example.myeclinic.util.UserSession
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions

class DoctorProfilePresenter(private val view: DoctorProfileView) {

    private val db = FirebaseFirestore.getInstance()

    fun loadUserProfile(userId: String, role: String) {
        val collection = if (role == "Doctor") "doctors" else "patients"
        db.collection(collection).document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    if (role == "Doctor") {
                        val doctor = document.toObject(Doctor::class.java)
                        doctor?.let { view.showDoctorInfo(it) } ?: view.showError("Invalid doctor data")
                    } else {
                        val patient = document.toObject(Patient::class.java)
                        patient?.let { view.showPatientInfo(it) } ?: view.showError("Invalid patient data")
                    }
                } else {
                    view.showError("User not found")
                }
            }
            .addOnFailureListener {
                view.showError("Failed to load profile")
            }
    }


    fun updateDoctorDirectly(doctorId: String, doctor: Doctor) {
        db.collection("doctors").document(doctorId).set(doctor)
            .addOnSuccessListener {
                view.showError("Doctor updated successfully")
            }
            .addOnFailureListener {
                view.showError("Failed to update doctor")
            }
    }

    fun retireDoctor(doctorId: String, role: String) {
        if (role == "Admin") {
            db.collection("doctors").document(doctorId)
                .update("isRetired", true)
                .addOnSuccessListener {
                    view.showError("Doctor retired")
                }
                .addOnFailureListener {
                    view.showError("Failed to retire doctor")
                }
        } else if (role == "Doctor") {
            db.collection("doctors").document(doctorId)
                .collection("pendingUpdates")
                .document("retire")
                .set(mapOf("isRetired" to true))
                .addOnSuccessListener {
                    view.showError("Retire request sent to admin")
                }
                .addOnFailureListener {
                    view.showError("Failed to send retire request")
                }
        }
    }

    fun loadDoctorAvailability(doctorId: String) {

        FirebaseFirestore.getInstance().collection("doctor_availability")
            .document(doctorId)
            .get()
            .addOnSuccessListener { document ->
                val availability = document.get("availability") as? Map<String, List<String>> ?: emptyMap()
                view.showCalendarWithAvailability(availability)

            }
            .addOnFailureListener {
                view.showError("Failed to load availability")
            }
    }


    fun bookAppointment(doctorId: String, specialization: String, date: String, hour: String) {
        val firestore = FirebaseFirestore.getInstance()
        val user = UserSession.currentUser ?: run {
            view.showError("User not logged in")
            return
        }

        firestore.collection("doctors").document(doctorId).get()
            .addOnSuccessListener { doctorSnapshot ->
                if (!doctorSnapshot.exists()) {
                    view.showError("Doctor not found")
                    return@addOnSuccessListener
                }
                val doctorName = doctorSnapshot.getString("name") ?: ""
                val appointmentRef = firestore.collection("appointments")
                    .document(date)
                    .collection(specialization)
                    .document(doctorId)

                val availabilityRef = firestore.collection("doctor_availability")
                    .document(doctorId)

                val bookingId = firestore.collection("bookings").document().id

                val bookingData = mapOf(
                    "doctorId" to doctorId,
                    "doctorName" to doctorName,
                    "patientId" to user.userId,
                    "patientName" to user.name,
                    "specialization" to specialization,
                    "date" to date,
                    "time" to hour,
                    "description" to "Appointment was booked by doctor search",
                    "status" to "booked",
                    "bookedAt" to Timestamp.now()
                )

                val bookingRef = firestore.collection("bookings").document(bookingId)
                val doctorBookingRef = firestore.collection("doctors").document(doctorId)
                    .collection("bookings").document(bookingId)
                val patientBookingRef = firestore.collection("patients").document(user.userId)
                    .collection("bookings").document(bookingId)

                firestore.runTransaction { transaction ->
                    val appointmentSnapshot = transaction.get(appointmentRef)
                    val availabilitySnapshot = transaction.get(availabilityRef)

                    val timeslots = appointmentSnapshot.get("timeslots") as? List<Map<String, Any>> ?: emptyList()
                    val updatedTimeslots = timeslots.map { slot ->
                        val mutableSlot = slot.toMutableMap()
                        if (mutableSlot["hour"] == hour) {
                            if (mutableSlot["booked"] == true) {
                                throw FirebaseFirestoreException(
                                    "Time slot already booked",
                                    FirebaseFirestoreException.Code.ABORTED
                                )
                            }
                            mutableSlot["booked"] = true
                        }
                        mutableSlot
                    }

                    val availabilityMap =
                        availabilitySnapshot.get("availability") as? Map<String, List<String>> ?: emptyMap()
                    val updatedAvailability = availabilityMap.toMutableMap()
                    val dayAvailability = updatedAvailability[date]?.toMutableList() ?: mutableListOf()
                    dayAvailability.remove(hour)
                    if (dayAvailability.isEmpty()) {
                        updatedAvailability.remove(date)
                    } else {
                        updatedAvailability[date] = dayAvailability
                    }

                    // Update time slot and availability
                    transaction.set(appointmentRef, mapOf("timeslots" to updatedTimeslots), SetOptions.merge())
                    transaction.set(availabilityRef, mapOf("availability" to updatedAvailability), SetOptions.merge())

                    // Create booking in all relevant paths
                    transaction.set(bookingRef, bookingData)
                    transaction.set(doctorBookingRef, bookingData)
                    transaction.set(patientBookingRef, bookingData)

                    //Add reference to doctor and patient
                    val patientRef = db.collection("patients").document(user.userId)
                    val doctorRef = db.collection("doctors").document(doctorId)

                    transaction.update(patientRef, "bookings", FieldValue.arrayUnion(bookingRef.id))
                    transaction.update(doctorRef, "bookings", FieldValue.arrayUnion(bookingRef.id))


                }.addOnSuccessListener {
                    view.showMessage("Appointment booked successfully")
                }.addOnFailureListener {
                    view.showError("Booking failed: ${it.message}")
                }
            }
    }

    fun loadTimeSlots(date: String, specialization: String, doctorId: String) {
        if (specialization.isBlank()) {
            view.showError("Specialization not provided")
            return
        }
        FirebaseFirestore.getInstance()
            .collection("appointments")
            .document(date)
            .collection(specialization)
            .document(doctorId)
            .get()
            .addOnSuccessListener { document ->
                val timeslots = document.get("timeslots") as? List<Map<String, Any>> ?: emptyList()
                val availabilityMap = mutableMapOf<String, Boolean>()
                val times = mutableListOf<String>()

                for (slot in timeslots) {
                    val hour = slot["hour"] as? String ?: continue
                    val booked = slot["booked"] as? Boolean ?: true
                    availabilityMap[hour] = !booked
                    times.add(hour)
                }

                view.showTimeslotDialog(date, times.sorted(), availabilityMap)
            }
            .addOnFailureListener {
                view.showError("Failed to load time slots")
            }
    }

}

interface DoctorProfileView {
    fun showDoctorInfo(doctor: Doctor)
    fun showPatientInfo(patient: Patient)
    fun showError(message: String)
    fun showMessage(message: String)
    fun showCalendarWithAvailability(availability: Map<String, List<String>>)
    fun showTimeslotDialog(date: String, timeslots: List<String>, availability: Map<String, Boolean>)
}