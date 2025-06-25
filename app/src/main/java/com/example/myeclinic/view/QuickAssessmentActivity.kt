package com.example.myeclinic.view

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myeclinic.R
import com.example.myeclinic.presenter.QuickAssessmentPresenter
import java.util.*

class QuickAssessmentActivity : AppCompatActivity(), QuickAssessmentPresenter.QuickAssessmentView {

    private lateinit var calendarView: CalendarView
    private lateinit var presenter: QuickAssessmentPresenter
    private lateinit var specialization: String
    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var selectedButton: Button? = null
    private var currentTimeButtons: MutableMap<String, Button> = mutableMapOf()
    /**
     * Called when the `QuickAssessmentActivity` is starting.
     *
     * This activity allows patients to quickly book an appointment by:
     * - Selecting a date from a calendar view.
     * - Viewing available timeslots for the selected date.
     * - Choosing an available time.
     * - Selecting a doctor from the list of available doctors for that time and specialization.
     * - Providing a short description of the medical issue.
     *
     * Workflow:
     * - Retrieves the medical specialization from the intent.
     * - Initializes the [CalendarView] to only allow future dates.
     * - Sets up a date change listener that triggers timeslot loading from the presenter.
     *
     * The UI flow continues in `showTimeslotDialog()` and `showDoctorSelectionDialog()` once user input is gathered.
     *
     * @param savedInstanceState A `Bundle` containing the activity's previously saved state, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_assessment)

        specialization = intent.getStringExtra("specialization") ?: "Unknown"
        calendarView = findViewById(R.id.calendarView)
        presenter = QuickAssessmentPresenter(this, specialization)

        calendarView.minDate = Calendar.getInstance().timeInMillis

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            selectedTime = null
            presenter.loadTimeslots(selectedDate!!)
        }
    }
    /**
     * Displays a dialog showing available timeslots for the selected date.
     *
     * Each timeslot is represented as a button. Unavailable timeslots are shown in red and disabled.
     * When a user selects a time, the "Book Appointment" button becomes active.
     *
     * @param date The selected date for which timeslots are shown (formatted as yyyy-MM-dd).
     * @param timeslots A list of time slot strings (e.g., "10:00", "14:30").
     * @param availability A map indicating which timeslots are available (true = available).
     */
    override fun showTimeslotDialog(date: String, timeslots: List<String>, availability: Map<String, Boolean>) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_date_popup, null)
        val tvSelectedDate = dialogView.findViewById<TextView>(R.id.tvSelectedDate)
        val layoutTimeslots = dialogView.findViewById<LinearLayout>(R.id.layoutTimeslots)
        val btnBookAppointment = dialogView.findViewById<Button>(R.id.btnBookAppointment)

        tvSelectedDate.text = date
        btnBookAppointment.isEnabled = false
        selectedTime = null
        selectedButton = null
        currentTimeButtons.clear()
        layoutTimeslots.removeAllViews()

        timeslots.forEach { time ->
            val btn = Button(this).apply {
                text = time
                isAllCaps = false
                isEnabled = availability[time] == true
                if (!isEnabled) setBackgroundColor(Color.RED)
                setOnClickListener {
                    selectedTime = time
                    selectedButton?.isEnabled = true
                    selectedButton?.alpha = 1.0f
                    selectedButton = this
                    this.isEnabled = false
                    this.alpha = 0.5f
                    btnBookAppointment.isEnabled = true
                }
            }
            currentTimeButtons[time] = btn
            layoutTimeslots.addView(btn)
        }

        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        btnBookAppointment.setOnClickListener {
            dialog.dismiss()
            val time = selectedTime
            if (time != null) {
                presenter.loadDoctors(date, time)
            } else {
                showError("Please select a time slot.")
            }
        }

        dialog.show()
    }
    /**
     * Marks a specific hour slot as unavailable in the current dialog.
     *
     * This is used when the state of availability changes dynamically (e.g., booking conflict).
     *
     * @param hour The time string to disable (e.g., "14:00").
     */
    override fun markHourUnavailable(hour: String) {
        currentTimeButtons[hour]?.let {
            it.isEnabled = false
            it.setBackgroundColor(Color.RED)
        }
    }
    /**
     * Displays a doctor selection dialog after a timeslot is chosen.
     *
     * Allows the patient to choose a doctor and describe their health issue before confirming.
     *
     * @param date The selected appointment date.
     * @param time The selected appointment time.
     * @param doctorList A list of pairs (doctorId, doctorName) to populate the dropdown.
     */
    override fun showDoctorSelectionDialog(date: String, time: String, doctorList: List<Pair<String, String>>) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_doctor_selection, null)
        val spinnerDoctor = dialogView.findViewById<Spinner>(R.id.spinnerDoctor)
        val etDescription = dialogView.findViewById<EditText>(R.id.etProblemDescription)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmBooking)
        val tvSelectedTime = dialogView.findViewById<TextView>(R.id.tvSelectedTime)

        tvSelectedTime.text = "Select a doctor for $time"

        val doctorNames = doctorList.map { it.second }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, doctorNames)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDoctor.adapter = adapter

        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        btnConfirm.setOnClickListener {
            val doctorIndex = spinnerDoctor.selectedItemPosition
            val doctorId = doctorList.getOrNull(doctorIndex)?.first
            val description = etDescription.text.toString().trim()

            if (doctorId == null) {
                showError("Please select a doctor.")
                return@setOnClickListener
            }
            if (description.isEmpty()) {
                showError("Please describe your problem.")
                return@setOnClickListener
            }

            dialog.dismiss()
            presenter.bookAppointment(date, time, doctorId, description)
        }

        dialog.show()
    }
    /**
     * Shows a short toast indicating that the appointment was booked successfully.
     */
    override fun showSuccessMessage() {
        Toast.makeText(this, "Appointment booked successfully!", Toast.LENGTH_SHORT).show()
    }
    /**
     * Displays a toast message to inform the user about an error.
     *
     * @param message The error message to be shown.
     */
    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
