package com.example.myeclinic.view

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myeclinic.R
import com.example.myeclinic.presenter.DoctorSchedulePresenter
import com.example.myeclinic.util.AppointmentDay
import com.example.myeclinic.util.UserSession
import com.google.firebase.firestore.FirebaseFirestore
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields
import java.util.*

class DoctorScheduleActivity : AppCompatActivity() {

    private lateinit var presenter: DoctorSchedulePresenter
    private lateinit var tvCurrentDate: TextView
    private lateinit var layoutSchedule: LinearLayout
    private lateinit var btnPrevDate: ImageButton
    private lateinit var btnNextDate: ImageButton

    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy\nEEEE", Locale.getDefault())
    private var currentIndex = 0
    private var days: List<AppointmentDay> = emptyList()

    val user = UserSession.currentUser
    val userId = user?.userId

    /**
     * Displays a series of dialogs to let the user pick available working hours for each given day.
     *
     * For each date in the [days] list, a multi-choice dialog is shown where the user can select
     * multiple time slots (e.g., 08:00, 09:00). The selected availability is collected and passed to the presenter
     * once all days have been processed.
     *
     * @param days A list of [LocalDate] objects for which availability needs to be selected.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showAvailabilityPickerDialog(days: List<LocalDate>) {
        val availableOptions = com.example.myeclinic.util.ScheduleConstants.workingHours
        val selectedHoursPerDay = mutableMapOf<String, List<String>>()

        fun promptForDay(index: Int) {
            if (index >= days.size) {
                presenter.saveAvailabilityForDays(selectedHoursPerDay)
                return
            }

            val day = days[index]
            val checkedItems = BooleanArray(availableOptions.size)
            val selected = mutableListOf<String>()

            AlertDialog.Builder(this)
                .setTitle("Select hours for ${day.format(DateTimeFormatter.ofPattern("EEEE, dd MMM"))}")
                .setMultiChoiceItems(availableOptions.toTypedArray(), checkedItems) { _, which, isChecked ->
                    val hour = availableOptions[which]
                    if (isChecked) selected.add(hour) else selected.remove(hour)
                }
                .setPositiveButton("Next") { _, _ ->
                    selectedHoursPerDay[day.toString()] = selected.toList()
                    promptForDay(index + 1)
                }
                .setNegativeButton("Skip") { _, _ ->
                    promptForDay(index + 1)
                }
                .setCancelable(false)
                .show()
        }

        promptForDay(0)
    }

    /**
     * Displays a list of time slots for a selected appointment day, highlighting booked vs available slots.
     *
     * Time slots marked as booked are disabled and styled differently. If a booked slot is clicked,
     * [showPatientDialog] is triggered to show further details.
     *
     * @param index The index of the day in the [days] list whose schedule should be shown.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDay(index: Int) {
        val appointmentDay = days[index]
        val date = LocalDate.parse(appointmentDay.date)
        tvCurrentDate.text = date.format(formatter)

        layoutSchedule.removeAllViews()
        appointmentDay.timeslots.forEach { slot ->
            val view = layoutInflater.inflate(R.layout.item_timeslot, layoutSchedule, false)
            val tvSlot = view.findViewById<TextView>(R.id.tvSlotTime)
            val isBooked = slot["booked"] == true
            val hour = slot["hour"] as? String ?: return@forEach
            tvSlot.text = hour
            tvSlot.isEnabled = !isBooked
            tvSlot.setBackgroundColor(
                if (isBooked) Color.LTGRAY else Color.parseColor("#D0F0C0")
            )
            tvSlot.setOnClickListener {
                if (isBooked) showPatientDialog(hour)
            }
            layoutSchedule.addView(view)
        }
    }
    /**
     * Called when the `DoctorScheduleActivity` is starting.
     *
     * This activity shows a day-by-day schedule of the currently logged-in doctor, including
     * booked and available timeslots. It enables doctors to:
     * - View appointments for each weekday
     * - Navigate between days
     * - See booking information for a selected time
     * - Optionally fill in their availability for the upcoming week
     *
     * Core flow:
     * - Retrieves the current doctor's ID and specialization from Firebase Firestore
     * - Loads the doctor’s full schedule using [DoctorSchedulePresenter]
     * - Displays available and booked timeslots in a scrollable linear layout
     * - Prompts the doctor to enter next week’s availability if it hasn’t been submitted yet
     * - Allows viewing patient details for booked slots via a dialog
     *
     * UI Elements:
     * - [tvCurrentDate] shows the currently selected date.
     * - [btnPrevDate] and [btnNextDate] navigate through the available schedule.
     * - [layoutSchedule] is dynamically populated with timeslot views.
     *
     * Conditions for prompting future availability input:
     * - The current day is Friday, Saturday, Sunday, or Monday
     * - No availability has been set for next week
     * - The prompt hasn’t already been shown this week
     *
     * @param savedInstanceState Previously saved state of the activity, if any.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_schedule)

        tvCurrentDate = findViewById(R.id.tvCurrentDate)
        layoutSchedule = findViewById(R.id.layoutSchedule)
        btnPrevDate = findViewById(R.id.btnPrevDate)
        btnNextDate = findViewById(R.id.btnNextDate)

        val firestore = FirebaseFirestore.getInstance()
        val user = UserSession.currentUser
        val doctorId = user?.userId ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        // fetch specialization
        firestore.collection("doctors").document(doctorId).get()
            .addOnSuccessListener { doc ->
                val specialization = doc.getString("specialization") ?: ""
                presenter = DoctorSchedulePresenter(firestore, doctorId, specialization)
                presenter.loadSchedule { scheduleList ->
                    days = scheduleList.sortedBy { it.date }

                    val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    val lastPromptWeek = sharedPref.getInt("lastPromptWeek_$userId", -1)

                    val today = LocalDate.now()
                    val currentWeek = today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)

                    val shouldCheckNextWeek = today.dayOfWeek in listOf(
                        DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.MONDAY
                    )

                    val nextWeekDays = (1..7).map { today.plusDays(it.toLong()) }
                        .filter { it.dayOfWeek != DayOfWeek.SATURDAY && it.dayOfWeek != DayOfWeek.SUNDAY }

                    val hasNextWeekAvailability = days.any { day ->
                        val date = LocalDate.parse(day.date)
                        date in nextWeekDays && day.timeslots.any { it["booked"] == false }
                    }

                    if (shouldCheckNextWeek && !hasNextWeekAvailability && lastPromptWeek != currentWeek) {
                        showAvailabilityPickerDialog(nextWeekDays)
                        sharedPref.edit().putInt("lastPromptWeek_$userId", currentWeek).apply()
                    }

                    if (days.isNotEmpty()) showDay(0)
                }

                btnPrevDate.setOnClickListener {
                    if (currentIndex > 0) {
                        currentIndex--
                        showDay(currentIndex)
                    }
                }
                btnNextDate.setOnClickListener {
                    if (currentIndex < days.size - 1) {
                        currentIndex++
                        showDay(currentIndex)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load doctor data", Toast.LENGTH_SHORT).show()
            }
    }
    /**
     * Displays a dialog with detailed booking information for a given time slot on the currently selected date.
     *
     * This function triggers a call to the [presenter] to fetch booking details (patient name, ID, and problem description).
     * If data is found, an [AlertDialog] is shown with the information. If the booking is not found or an error occurs,
     * a toast is displayed instead.
     *
     * @param hour The time slot (e.g., "10:00") for which booking details should be shown.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showPatientDialog(hour: String) {
        val selectedDate = days[currentIndex].date
        presenter.fetchBookingDetails(
            date = selectedDate,
            time = hour,
            onResult = { name, patientId, description ->
                AlertDialog.Builder(this)
                    .setTitle("Booking Info")
                    .setMessage(
                        "Patient: $name\nID: $patientId\nTime: $hour\nDate: $selectedDate" +
                                "\n\nProblem:\n$description"
                    )
                    .setPositiveButton("OK", null)
                    .show()
            },
            onError = {
                Toast.makeText(this, "Booking info not found.", Toast.LENGTH_SHORT).show()
            }
        )
    }
}