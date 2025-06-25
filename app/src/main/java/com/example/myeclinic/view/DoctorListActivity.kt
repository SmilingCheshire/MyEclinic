package com.example.myeclinic.view

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myeclinic.R
import com.example.myeclinic.model.Doctor
import com.example.myeclinic.presenter.DoctorListPresenter
import com.example.myeclinic.presenter.DoctorView

class DoctorListActivity : AppCompatActivity(), DoctorView {

    private lateinit var presenter: DoctorListPresenter
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val doctorList = mutableListOf<Doctor>()
    private lateinit var specialization: String
    /**
     * Initializes the Doctor List screen.
     *
     * This activity displays a list of doctors filtered by a specialization provided via intent extras.
     * It allows users to:
     * - View names of doctors with the given specialization.
     * - Tap on a doctor to open their profile in [AnotherProfileActivity].
     *
     * Workflow:
     * - Loads the layout `activity_doctor_list`.
     * - Initializes the [ListView] and sets a simple text adapter.
     * - Retrieves the specialization string from the intent.
     * - Uses [DoctorListPresenter] to load doctors for that specialization.
     * - Handles list item clicks by starting [AnotherProfileActivity] with doctor details.
     *
     * @param savedInstanceState The previously saved instance state, if available.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_list)

        listView = findViewById(R.id.list_doctors)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        listView.adapter = adapter

        presenter = DoctorListPresenter(this)

        val specialization = intent.getStringExtra("specialization") ?: ""
        presenter.loadDoctorsBySpecialization(specialization)

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedDoctor = doctorList[position]
            val intent = Intent(this, AnotherProfileActivity::class.java)
            intent.putExtra("userId", selectedDoctor.doctorId)
            intent.putExtra("userRole", "Doctor")
            intent.putExtra("specialization", specialization)
            startActivity(intent)
        }
    }
    /**
     * Updates the UI with a list of doctors retrieved from the presenter.
     *
     * Clears the current doctor list, stores the new one, and updates the adapter to display
     * each doctor's name in the associated ListView or UI component.
     *
     * @param doctors A list of [Doctor] objects representing the filtered or fetched doctors.
     */
    override fun showDoctors(doctors: List<Doctor>) {
        doctorList.clear()
        doctorList.addAll(doctors)
        adapter.clear()
        adapter.addAll(doctors.map { "${it.name}" })
        adapter.notifyDataSetChanged()
    }
    /**
     * Displays an error message to the user in a Toast.
     *
     * Commonly used to notify about issues like network failures or empty results.
     *
     * @param message The error message to display.
     */
    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

