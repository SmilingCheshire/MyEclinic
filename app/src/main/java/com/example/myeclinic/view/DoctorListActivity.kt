package com.example.myeclinic.view

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myeclinic.R
import com.example.myeclinic.model.Doctor
import com.example.myeclinic.presenter.DoctorListPresenter
import com.example.myeclinic.presenter.DoctorView
import com.google.firebase.firestore.FirebaseFirestore

class DoctorListActivity : AppCompatActivity(), DoctorView {

    private lateinit var presenter: DoctorListPresenter
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val doctorList = mutableListOf<Doctor>()
    private lateinit var specialization: String

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
            val intent = Intent(this, DoctorProfileActivity::class.java)
            intent.putExtra("doctorId", selectedDoctor.doctorId)
            intent.putExtra("specialization", specialization)
            startActivity(intent)
        }
    }

    override fun showDoctors(doctors: List<Doctor>) {
        doctorList.clear()
        doctorList.addAll(doctors)
        adapter.clear()
        adapter.addAll(doctors.map { "${it.name}" })
        adapter.notifyDataSetChanged()
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

