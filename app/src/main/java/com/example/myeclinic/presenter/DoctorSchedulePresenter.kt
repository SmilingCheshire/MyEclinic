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
    /**
     * Loads a 30-day forward-looking schedule of working days (excluding weekends) for a doctor.
     *
     * For each valid date, this function:
     * - Retrieves existing appointment documents from Firestore
     * - Parses available (unbooked) time slots
     * - Updates the `doctor_availability` collection with a summary map of availability
     *
     * The result is a list of [AppointmentDay] objects passed to [onComplete], sorted by date.
     *
     * @param onComplete Callback invoked with the list of parsed appointment days.
     */
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

    /**
     * Fetches booking details for a specific doctor on a given date and time.
     *
     * This includes patient name, patient ID, and the appointment description.
     * Patient name is retrieved from the `users` collection based on the patient ID.
     *
     * @param date The date of the appointment (format: "YYYY-MM-DD").
     * @param time The time slot for the appointment (e.g., "10:30").
     * @param onResult Callback returning patient name, ID, and description if booking exists.
     * @param onError Callback invoked if the operation fails or no booking is found.
     */
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
    /**
     * Saves the availability for a doctor for multiple days.
     *
     * Each entry in [hoursMap] represents a date with a list of available hours.
     * For each date, creates or overwrites a Firestore document containing timeslot data
     * under the appropriate specialization and doctor ID.
     *
     * @param hoursMap A map of date strings (e.g., "2025-06-26") to available time slots (e.g., ["10:00", "11:00"]).
     */
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

    /**
     * Retrieves all appointment schedules for a list of doctors on a specific date.
     *
     * Looks under the `appointments/{date}/{specialization}/{doctorId}` path for each doctor.
     * Each resulting document contains a list of time slots which are returned in a map.
     *
     * @param date The date for which to retrieve schedules (e.g., "2025-06-26").
     * @param doctorIds List of doctor IDs to query.
     * @param specialization Medical specialization used to locate subcollections.
     * @param callback Callback returning a map of doctorId to list of time slots.
     */
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
