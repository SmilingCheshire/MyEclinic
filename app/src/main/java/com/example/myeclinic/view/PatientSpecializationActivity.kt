package com.example.myeclinic.view

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.myeclinic.R
import com.example.myeclinic.presenter.SpecializationPresenter
import com.example.myeclinic.presenter.SpecializationView

class PatientSpecializationActivity : AppCompatActivity(), SpecializationView {

    private lateinit var presenter: SpecializationPresenter
    private lateinit var listView: ListView
    private val role = "Patient"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_specialization)

        listView = findViewById(R.id.list_specializations)
        presenter = SpecializationPresenter(this)
        presenter.loadSpecializations()

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedSpecialization = listView.adapter.getItem(position) as String
            val intent = Intent(this, SpecializationOptionsActivity::class.java)
            intent.putExtra("specialization", selectedSpecialization)
            intent.putExtra("userRole", role)
            startActivity(intent)
        }
    }

    override fun showSpecializations(specializations: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, specializations)
        listView.adapter = adapter
    }

    override fun onSpecializationAdded() {
        // Not needed for patients
    }

    override fun onSpecializationRemoved() {
        // Not needed for patients
    }
}
