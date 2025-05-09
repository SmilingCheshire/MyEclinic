package com.example.myeclinic.view

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.myeclinic.R
import com.example.myeclinic.model.Doctor
import com.example.myeclinic.presenter.DoctorProfilePresenter
import com.example.myeclinic.presenter.DoctorProfileView
import com.example.myeclinic.util.UserSession

class DoctorProfileActivity : AppCompatActivity(), DoctorProfileView {

    private lateinit var tvName: TextView
    private lateinit var tvSpecialty: TextView
    private lateinit var tvBio: TextView
    private lateinit var tvContact: TextView
    private lateinit var etName: EditText
    private lateinit var etSpecialty: EditText
    private lateinit var etBio: EditText
    private lateinit var etContact: EditText
    private lateinit var btnAppointment: Button
    private lateinit var btnChat: Button
    private lateinit var btnSave: Button
    private lateinit var btnRetire: Button

    private lateinit var presenter: DoctorProfilePresenter

    private val user = UserSession.currentUser
    private val role = user?.role ?: "Patient"
    private val userId = user?.userId

    lateinit var specialization: String

    private var doctorId: String? = null
    private var loadedDoctor: Doctor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_profile)

        // Bind views
        tvName = findViewById(R.id.tv_doctor_name)
        tvSpecialty = findViewById(R.id.tv_specialty)
        tvBio = findViewById(R.id.tv_bio)
        tvContact = findViewById(R.id.tv_contact)

        etName = findViewById(R.id.et_doctor_name)
        etSpecialty = findViewById(R.id.et_specialty)
        etBio = findViewById(R.id.et_bio)
        etContact = findViewById(R.id.et_contact)

        btnAppointment = findViewById(R.id.btn_appointment)
        btnChat = findViewById(R.id.btn_chat)
        btnSave = findViewById(R.id.btn_save)
        btnRetire = findViewById(R.id.btn_retire)

        presenter = DoctorProfilePresenter(this)
        specialization = intent.getStringExtra("specialization") ?: ""

        doctorId = intent.getStringExtra("doctorId")
        if (doctorId != null) {
            presenter.loadDoctorProfile(doctorId!!)
        } else {
            showError("Doctor ID not provided")
            finish()
        }

        btnSave.setOnClickListener {
            val updatedDoctor = loadedDoctor?.copy(
                name = etName.text.toString(),
                specialization = etSpecialty.text.toString(),
                bio = etBio.text.toString(),
                contactNumber = etContact.text.toString()
            ) ?: return@setOnClickListener

            if (role == "Admin") {
                presenter.updateDoctorDirectly(doctorId!!, updatedDoctor)
            }
        }

        btnRetire.setOnClickListener {
            presenter.retireDoctor(doctorId!!, role)
        }

        btnAppointment.setOnClickListener {
            doctorId?.let { presenter.loadDoctorAvailability(it) }
        }

        btnChat.setOnClickListener {
            Toast.makeText(this, "Chat feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    override fun showDoctorInfo(doctor: Doctor) {
        loadedDoctor = doctor

        val isEditable = role == "Admin"

        if (isEditable) {
            // Hide text views
            tvName.visibility = View.GONE
            tvSpecialty.visibility = View.GONE
            tvBio.visibility = View.GONE
            tvContact.visibility = View.GONE

            // Show and populate edit texts
            etName.visibility = View.VISIBLE
            etSpecialty.visibility = View.VISIBLE
            etBio.visibility = View.VISIBLE
            etContact.visibility = View.VISIBLE

            etName.setText(doctor.name)
            etSpecialty.setText(doctor.specialization)
            etBio.setText(doctor.bio)
            etContact.setText(doctor.contactNumber)

            btnSave.visibility = View.VISIBLE
            btnRetire.visibility = View.VISIBLE

            // Hide appointment/chat for self-profile doctor
            if (role == "Doctor") {
                btnAppointment.visibility = View.GONE
                btnChat.visibility = View.GONE
            }
        } else {
            tvName.text = doctor.name
            tvSpecialty.text = doctor.specialization
            tvBio.text = doctor.bio
            tvContact.text = doctor.contactNumber

            tvName.visibility = View.VISIBLE
            tvSpecialty.visibility = View.VISIBLE
            tvBio.visibility = View.VISIBLE
            tvContact.visibility = View.VISIBLE

            etName.visibility = View.GONE
            etSpecialty.visibility = View.GONE
            etBio.visibility = View.GONE
            etContact.visibility = View.GONE

            btnSave.visibility = View.GONE
            btnRetire.visibility = View.GONE

            btnAppointment.visibility = View.VISIBLE
            btnChat.visibility = View.VISIBLE
        }
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun showCalendarWithAvailability(availability: Map<String, List<String>>) {
        val calendarDialog = DatePickerDialog(this)
        calendarDialog.setOnDateSetListener { _, year, month, day ->
            val dateStr = String.format("%04d-%02d-%02d", year, month + 1, day)
            if (availability[dateStr].isNullOrEmpty()) {
                Toast.makeText(this, "No available slots on selected date", Toast.LENGTH_SHORT).show()
            } else {
                doctorId?.let { presenter.loadTimeSlots(dateStr, specialization, it) }
            }
        }

        // Disable dates with empty hour lists
        calendarDialog.datePicker.setOnDateChangedListener { _, y, m, d ->
            val selected = String.format("%04d-%02d-%02d", y, m + 1, d)
            if (availability[selected].isNullOrEmpty()) {
                calendarDialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false
            } else {
                calendarDialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = true
            }
        }

        calendarDialog.show()
    }

    override fun showTimeslotDialog(date: String, timeslots: List<String>, availability: Map<String, Boolean>) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_date_popup, null)
        val tvSelectedDate = dialogView.findViewById<TextView>(R.id.tvSelectedDate)
        val layoutTimeslots = dialogView.findViewById<LinearLayout>(R.id.layoutTimeslots)
        val btnBookAppointment = dialogView.findViewById<Button>(R.id.btnBookAppointment)

        tvSelectedDate.text = date
        btnBookAppointment.isEnabled = false

        var selectedTime: String? = null
        var selectedButton: Button? = null

        layoutTimeslots.removeAllViews()

        timeslots.forEach { time ->
            val btn = Button(this).apply {
                text = time
                isAllCaps = false
                isEnabled = availability[time] == true
                if (!isEnabled) {
                    setBackgroundColor(android.graphics.Color.RED)
                }
                setOnClickListener {
                    selectedButton?.isEnabled = true
                    selectedButton?.alpha = 1.0f

                    selectedTime = time
                    selectedButton = this

                    this.isEnabled = false
                    this.alpha = 0.5f

                    btnBookAppointment.isEnabled = true
                }
            }
            layoutTimeslots.addView(btn)
        }

        val dialog = android.app.AlertDialog.Builder(this).setView(dialogView).create()

        btnBookAppointment.setOnClickListener {
            val time = selectedTime
            val docId = doctorId
            if (time != null && docId != null) {
                presenter.bookAppointment(docId, specialization, date, time)
                dialog.dismiss()
            } else {
                showError("Please select a valid time.")
            }
        }

        dialog.show()
    }

    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

}