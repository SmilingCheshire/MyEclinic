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

    override fun showPatients(patients: List<Pair<String, String>>) {
        patientMap.clear()
        val names = patients.map { it.first }
        patients.forEach { patientMap[it.first] = it.second }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, names)
        val actv = findViewById<AutoCompleteTextView?>(R.id.actvPatientName)
        actv?.setAdapter(adapter)
        actv?.threshold = 1
    }


    override fun showSpecializations(specializations: List<String>) {
        this.specializations.clear()
        this.specializations.addAll(specializations)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specializations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSpecialization.adapter = adapter
    }


    override fun showDoctors(doctors: List<Doctor>) {
        this.doctors.clear()
        this.doctors.addAll(doctors)

        val doctorNames = doctors.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, doctorNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDoctor.adapter = adapter
    }


    override fun showTimeslots(timeslots: List<Timeslot>) {
        timeslotAdapter.submitList(timeslots)
    }

    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

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
