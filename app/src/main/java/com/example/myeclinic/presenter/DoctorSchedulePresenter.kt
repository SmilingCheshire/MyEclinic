package com.example.myeclinic.presenter

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.myeclinic.util.AppointmentDay
import com.example.myeclinic.util.ScheduleConstants
import com.example.myeclinic.util.UserSession
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.time.DayOfWeek
import java.time.LocalDate

class DoctorSchedulePresenter(
    private val firestore: FirebaseFirestore,
    private val doctorId: String,
    private val specialization: String
) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadSchedule(onComplete: (List<AppointmentDay>) -> Unit) {
        val today = LocalDate.now()
        val workingDays = (0..29)
            .map { today.plusDays(it.toLong()) }
            .filter { it.dayOfWeek != DayOfWeek.SATURDAY && it.dayOfWeek != DayOfWeek.SUNDAY }

        val resultList = mutableListOf<AppointmentDay>()
        val fetchTasks = mutableListOf<com.google.android.gms.tasks.Task<DocumentSnapshot>>()
        val availabilityMap = mutableMapOf<String, List<String>>() // date -> unbooked times

        for (day in workingDays) {
            val dateStr = day.toString()
            val task = firestore
                .collection("appointments")
                .document(dateStr)
                .collection(specialization)
                .document(doctorId)
                .get()
            fetchTasks.add(task)
        }

        Tasks.whenAllSuccess<DocumentSnapshot>(fetchTasks)
            .addOnSuccessListener { snapshots ->
                val saveTasks = mutableListOf<com.google.android.gms.tasks.Task<Void>>()

                snapshots.forEachIndexed { index, doc ->
                    val day = workingDays[index]
                    val dateStr = day.toString()

                    val timeSlots: List<Map<String, Any>>
                    if (doc.exists()) {
                        timeSlots = doc["timeslots"] as? List<Map<String, Any>> ?: emptyList()
                        val availableHours = timeSlots
                            .filter { it["booked"] == false }
                            .mapNotNull { it["hour"] as? String }
                        availabilityMap[dateStr] = availableHours
                        resultList.add(AppointmentDay(dateStr, timeSlots))
                    } else {
                        availabilityMap[dateStr] = emptyList()
                        resultList.add(AppointmentDay(dateStr, emptyList()))
                    }

                }

                // Push to doctor_availability collection
                val availabilityTask = firestore
                    .collection("doctor_availability")
                    .document(doctorId)
                    .set(mapOf("availability" to availabilityMap))

                saveTasks.add(availabilityTask)

                Tasks.whenAll(saveTasks)
                    .addOnSuccessListener {
                        onComplete(resultList.sortedBy { it.date })
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                        onComplete(resultList.sortedBy { it.date })
                    }
            }
            .addOnFailureListener {
                it.printStackTrace()
                onComplete(emptyList()) // full failure case
            }
    }


    fun fetchBookingDetails(
        date: String,
        time: String,
        onResult: (patientName: String, patientId: String, description: String) -> Unit,
        onError: (Exception?) -> Unit
    ) {
        firestore.collection("bookings")
            .whereEqualTo("doctorId", doctorId)
            .whereEqualTo("date", date)
            .whereEqualTo("time", time)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val booking = snapshot.documents[0]
                    val patientId = booking.getString("patientId") ?: "Unknown"
                    val description = booking.getString("description") ?: "No description provided"

                    firestore.collection("users")
                        .document(patientId)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            val patientName = userDoc.getString("name") ?: "Patient $patientId"
                            onResult(patientName, patientId, description)
                        }
                        .addOnFailureListener {
                            onResult("Patient $patientId", patientId, description)
                        }
                } else {
                    onError(null)
                }
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveAvailabilityForDays(hoursMap: Map<String, List<String>>) {
        val firestore = FirebaseFirestore.getInstance()
        val user = UserSession.currentUser ?: return
        val doctorId = user.userId

        // Load specialization
        firestore.collection("doctors").document(doctorId).get().addOnSuccessListener { doc ->
            val specialization = doc.getString("specialization") ?: return@addOnSuccessListener

            for ((date, hours) in hoursMap) {
                val timeslotList = hours.map {
                    mapOf("hour" to it, "booked" to false)
                }

                val data = mapOf("timeslots" to timeslotList)
                firestore.collection("appointments")
                    .document(date)
                    .collection(specialization)
                    .document(doctorId)
                    .set(data)
            }
        }
    }


    fun fetchDoctorSchedulesForDate(
        date: String,
        doctorIds: List<String>,
        specialization: String,
        callback: (Map<String, List<Map<String, Any>>>) -> Unit
    ) {
        val appointmentsRef = firestore
            .collection("appointments")
            .document(date)
            .collection(specialization)

        val tasks = doctorIds.map { doctorId ->
            appointmentsRef.document(doctorId).get()
        }

        Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
            .addOnSuccessListener { snapshots ->
                val map = mutableMapOf<String, List<Map<String, Any>>>()
                for (doc in snapshots) {
                    val docId = doc.id
                    val slots = doc.get("timeslots") as? List<Map<String, Any>> ?: emptyList()
                    map[docId] = slots
                }
                callback(map)
            }
            .addOnFailureListener {
                callback(emptyMap())
            }
    }
}
