package com.example.myeclinic.view

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myeclinic.R
import com.example.myeclinic.model.Booking
import com.example.myeclinic.model.Patient
import com.example.myeclinic.presenter.MedicalHistoryPresenter
import com.example.myeclinic.presenter.MedicalHistoryView
import com.example.myeclinic.util.BookingsAdapter

class MedicalHistoryActivity : AppCompatActivity(), MedicalHistoryView {

    private lateinit var presenter: MedicalHistoryPresenter
    private lateinit var patientAutoComplete: AutoCompleteTextView
    private lateinit var bookingsRecyclerView: RecyclerView
    private lateinit var bookingsAdapter: BookingsAdapter

    private var currentPatientId: String? = null
    private val patientsMap = mutableMapOf<String, Patient>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medical_history)

        patientAutoComplete = findViewById(R.id.patientAutoComplete)
        bookingsRecyclerView = findViewById(R.id.bookingsRecyclerView)

        presenter = MedicalHistoryPresenter(this)

        bookingsAdapter = BookingsAdapter { booking, bookingId ->
            currentPatientId?.let {
                presenter.cancelBooking(it, bookingId, booking.bookedAt)
            }
        }

        bookingsRecyclerView.layoutManager = LinearLayoutManager(this)
        bookingsRecyclerView.adapter = bookingsAdapter

        presenter.loadAllPatients()

        patientAutoComplete.setOnItemClickListener { _, _, position, _ ->
            val name = patientAutoComplete.adapter.getItem(position).toString()
            val patient = patientsMap[name]
            patient?.let {
                currentPatientId = it.patientId
                presenter.loadBookings(it.patientId)
            }
        }
    }

    override fun showPatientList(patients: List<Patient>) {
        val names = patients.map { it.name }
        patients.forEach { patientsMap[it.name] = it }

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, names)
        patientAutoComplete.setAdapter(adapter)
    }

    override fun showBookings(bookings: List<Pair<Booking, String>>) {
        bookingsAdapter.submitList(bookings)
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
