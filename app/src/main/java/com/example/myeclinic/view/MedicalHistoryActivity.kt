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
    /**
     * Called when the `MedicalHistoryActivity` is starting.
     *
     * This activity allows doctors or administrators to review and manage a patient's medical booking history.
     * On startup, it:
     * - Initializes UI components (AutoCompleteTextView for selecting a patient, RecyclerView for showing bookings)
     * - Sets up the [BookingsAdapter] to display and cancel bookings
     * - Loads all patients from the backend and displays them in a dropdown
     * - Responds to patient selection by loading their booking history
     *
     * Each booking in the list includes an option to cancel it, which invokes the presenter logic.
     * Uses [MedicalHistoryPresenter] to manage data operations.
     *
     * UI Elements:
     * - [AutoCompleteTextView] for patient name selection
     * - [RecyclerView] for listing bookings
     *
     * @param savedInstanceState The saved state of the activity, if any.
     */
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
    /**
     * Displays the list of patients in the AutoCompleteTextView.
     *
     * Extracts patient names and maps each name to its corresponding [Patient] object
     * for later retrieval. Sets up the adapter for the autocomplete field.
     *
     * @param patients The list of [Patient] objects to display.
     */
    override fun showPatientList(patients: List<Patient>) {
        val names = patients.map { it.name }
        patients.forEach { patientsMap[it.name] = it }

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, names)
        patientAutoComplete.setAdapter(adapter)
    }
    /**
     * Updates the RecyclerView with a list of bookings.
     *
     * Each item in the list contains a [Booking] and its corresponding booking ID.
     *
     * @param bookings List of booking entries as (Booking, bookingId) pairs.
     */
    override fun showBookings(bookings: List<Pair<Booking, String>>) {
        bookingsAdapter.submitList(bookings)
    }
    /**
     * Displays an error message to the user as a Toast.
     *
     * @param message The error message to display.
     */
    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    /**
     * Displays a short confirmation or success message to the user as a Toast.
     *
     * @param message The success message to display.
     */
    override fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
