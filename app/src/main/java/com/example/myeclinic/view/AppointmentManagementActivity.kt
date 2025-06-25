package com.example.myeclinic.view

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myeclinic.R
import com.example.myeclinic.model.Appointment
import com.example.myeclinic.model.Doctor
import com.example.myeclinic.presenter.AppointmentManagementContract
import com.example.myeclinic.presenter.AppointmentManagementPresenter
import com.example.myeclinic.util.Timeslot
import com.example.myeclinic.util.TimeslotAdapter
import java.text.SimpleDateFormat
import java.util.*

class AppointmentManagementActivity : AppCompatActivity(), AppointmentManagementContract.View {

    private lateinit var presenter: AppointmentManagementContract.Presenter
    private lateinit var timeslotAdapter: TimeslotAdapter
    private lateinit var selectedDoctorId: String
    private lateinit var selectedDoctorName: String
    private lateinit var selectedSpecialization: String
    private lateinit var selectedDate: String

    private val doctors = mutableListOf<Doctor>()
    private val specializations = mutableListOf<String>()

    private lateinit var btnSelectDate: Button
    private lateinit var spinnerSpecialization: Spinner
    private lateinit var spinnerDoctor: Spinner

    private val patientMap = mutableMapOf<String, String>()
    /**
     * Initializes the Appointment Management screen. This function:
     *
     * - Binds UI elements such as buttons, spinners, and recycler view.
     * - Initializes the [AppointmentManagementPresenter] to handle logic and data operations.
     * - Sets up the [TimeslotAdapter] with booking and cancellation behavior.
     * - Loads available specializations and patients.
     * - Sets up listeners for:
     *   - Date selection using a date picker.
     *   - Specialization selection to filter doctors.
     *   - Doctor selection to fetch available timeslots.
     *
     * This activity allows administrators to:
     * - View available appointment slots.
     * - Create new bookings by selecting a patient and timeslot.
     * - Cancel existing bookings.
     *
     * The activity layout is defined in `activity_appointment_management.xml`.
     *
     * @param savedInstanceState Previously saved state of the activity, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_management)

        btnSelectDate = findViewById(R.id.btnSelectDate)
        spinnerSpecialization = findViewById(R.id.spinnerSpecialization)
        spinnerDoctor = findViewById(R.id.spinnerDoctor)

        presenter = AppointmentManagementPresenter(this)
        timeslotAdapter = TimeslotAdapter { timeslot ->
            if (timeslot.booked) {
                showCancelDialog(timeslot)
            } else {
                showBookingDialog(timeslot)
            }
        }

        presenter.loadSpecializations()
        presenter.loadPatients() // Load all patients at start

        findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(this@AppointmentManagementActivity)
            adapter = timeslotAdapter
        }

        btnSelectDate.setOnClickListener {
            showDatePicker()
        }

        presenter.loadSpecializations()

        spinnerSpecialization.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSpecialization = specializations[position]
                presenter.loadDoctorsBySpecialization(selectedSpecialization)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerDoctor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val doctor = doctors[position]
                selectedDoctorId = doctor.doctorId
                selectedDoctorName = doctor.name
                if (::selectedDate.isInitialized && selectedSpecialization.isNotEmpty()) {
                    presenter.loadTimeslots(selectedDate, selectedSpecialization, selectedDoctorId)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    }
    /**
     * Displays the list of patients in an AutoCompleteTextView.
     *
     * The list is used to allow quick patient selection by name, mapping names to their IDs
     * in the `patientMap` for lookup during booking.
     *
     * @param patients A list of pairs, where each pair contains the patient name and patient ID.
     */
    override fun showPatients(patients: List<Pair<String, String>>) {
        patientMap.clear()
        val names = patients.map { it.first }
        patients.forEach { patientMap[it.first] = it.second }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, names)
        val actv = findViewById<AutoCompleteTextView?>(R.id.actvPatientName)
        actv?.setAdapter(adapter)
        actv?.threshold = 1
    }

    /**
     * Populates the specialization spinner with the provided list of specializations.
     *
     * This is typically called when the admin or doctor is assigning or filtering by specialization.
     *
     * @param specializations A list of medical specializations (e.g., Cardiology, Pediatrics).
     */
    override fun showSpecializations(specializations: List<String>) {
        this.specializations.clear()
        this.specializations.addAll(specializations)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specializations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSpecialization.adapter = adapter
    }

    /**
     * Populates the doctor spinner with available doctors for the selected specialization.
     *
     * Also stores the list of doctors internally for ID lookup and later use.
     *
     * @param doctors A list of [Doctor] objects representing available practitioners.
     */
    override fun showDoctors(doctors: List<Doctor>) {
        this.doctors.clear()
        this.doctors.addAll(doctors)

        val doctorNames = doctors.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, doctorNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDoctor.adapter = adapter
    }

    /**
     * Displays a list of available and booked timeslots for a given doctor and date.
     *
     * Used to visually indicate when a doctor is available for appointments.
     *
     * @param timeslots A list of [Timeslot] objects representing availability.
     */
    override fun showTimeslots(timeslots: List<Timeslot>) {
        timeslotAdapter.submitList(timeslots)
    }
    /**
     * Shows a brief toast message to the user.
     *
     * Useful for displaying confirmation or error messages.
     *
     * @param message The message to display.
     */
    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    /**
     * Opens a date picker dialog and updates the selected date.
     *
     * Once a date is picked, the system triggers loading of timeslots for the selected
     * doctor and specialization, if available.
     */
    private fun showDatePicker() {
        val now = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            val cal = Calendar.getInstance()
            cal.set(year, month, day)
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            selectedDate = dateStr
            btnSelectDate.text = dateStr

            if (::selectedDoctorId.isInitialized && selectedSpecialization.isNotEmpty()) {
                presenter.loadTimeslots(selectedDate, selectedSpecialization, selectedDoctorId)
            }
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
    }
    /**
     * Displays a booking dialog where the admin or doctor can assign a timeslot to a patient.
     *
     * This includes an AutoCompleteTextView for selecting a patient by name and a text field
     * for describing the medical issue.
     *
     * @param timeslot The [Timeslot] object representing the time being booked.
     */
    private fun showBookingDialog(timeslot: Timeslot) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_booking, null)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.etDescription)

        val actvPatientName = dialogView.findViewById<AutoCompleteTextView>(R.id.actvPatientName)
        actvPatientName.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, patientMap.keys.toList()))
        actvPatientName.threshold = 1


        AlertDialog.Builder(this)
            .setTitle("Create Booking for ${timeslot.hour}")
            .setView(dialogView)
            .setPositiveButton("Book") { _, _ ->

                val selectedName = actvPatientName.text.toString().trim()
                val patientId = patientMap[selectedName]

                if (patientId == null) {
                    Toast.makeText(this, "Please select a valid patient name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val description = descriptionInput.text.toString().trim()
                val appointment = Appointment(
                    doctorId = selectedDoctorId,
                    doctorName = selectedDoctorName,
                    patientId = patientId,
                    patientName = "", // could be fetched later if needed
                    date = selectedDate,
                    time = timeslot.hour,
                    status = "booked",
                    description = description,
                    specialization = selectedSpecialization
                )
                presenter.createBooking(appointment)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    /**
     * Displays a confirmation dialog for canceling an existing booking.
     *
     * If confirmed, triggers cancellation logic via the presenter.
     *
     * @param timeslot The [Timeslot] to be cancelled.
     */
    private fun showCancelDialog(timeslot: Timeslot) {
        AlertDialog.Builder(this)
            .setTitle("Cancel Booking at ${timeslot.hour}?")
            .setPositiveButton("Yes") { _, _ ->
                presenter.cancelBooking(selectedDate, selectedSpecialization, selectedDoctorId, timeslot.hour)
            }
            .setNegativeButton("No", null)
            .show()
    }


}
