package com.example.myeclinic.view

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myeclinic.R
import com.example.myeclinic.model.Doctor
import com.example.myeclinic.presenter.DoctorProfilePresenter
import com.example.myeclinic.presenter.DoctorProfileView

class DoctorProfileActivity : AppCompatActivity(), DoctorProfileView {

    private lateinit var tvName: TextView
    private lateinit var tvSpecialty: TextView
    private lateinit var tvBio: TextView
    private lateinit var tvContact: TextView
    private lateinit var scheduleLayout: LinearLayout
    private lateinit var btnAppointment: Button
    private lateinit var btnChat: Button

    private lateinit var presenter: DoctorProfilePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_profile)

        tvName = findViewById(R.id.tv_doctor_name)
        tvSpecialty = findViewById(R.id.tv_specialty)
        tvBio = findViewById(R.id.tv_bio)
        tvContact = findViewById(R.id.tv_contact)
        scheduleLayout = findViewById(R.id.schedule_layout)
        btnAppointment = findViewById(R.id.btn_appointment)
        btnChat = findViewById(R.id.btn_chat)

        presenter = DoctorProfilePresenter(this)

        val doctorId = intent.getStringExtra("doctorId")
        if (doctorId != null) {
            presenter.loadDoctorProfile(doctorId)
        } else {
            showError("Doctor ID not provided")
            finish()
        }

        btnAppointment.setOnClickListener {
            Toast.makeText(this, "Booking feature coming soon", Toast.LENGTH_SHORT).show()
        }

        btnChat.setOnClickListener {
            Toast.makeText(this, "Chat feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    override fun showDoctorInfo(doctor: Doctor) {
        tvName.text = doctor.name
        tvSpecialty.text = doctor.specialty
        tvBio.text = doctor.bio
        tvContact.text = doctor.contactNumber
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showSchedule(availability: Map<String, List<String>>?) {
        scheduleLayout.removeAllViews()

        if (availability.isNullOrEmpty()) {
            val emptyView = TextView(this).apply {
                text = "No availability info provided."
                textSize = 16f
                setPadding(0, 8, 0, 8)
            }
            scheduleLayout.addView(emptyView)
            return
        }

        // Define the correct day order
        val dayOrder = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

        // Sort the map by the custom day order
        val sortedAvailability = availability.toSortedMap(compareBy { dayOrder.indexOf(it) })

        sortedAvailability.forEach { (day, times) ->
            val dayView = TextView(this).apply {
                text = if (times.isNotEmpty()) "$day: ${times.joinToString(", ")}" else "$day: Not Available"
                textSize = 16f
                setPadding(0, 8, 0, 8)
            }
            scheduleLayout.addView(dayView)
        }
    }
}
