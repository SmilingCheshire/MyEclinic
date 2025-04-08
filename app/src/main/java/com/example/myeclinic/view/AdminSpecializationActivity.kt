package com.example.myeclinic.view

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myeclinic.R
import com.example.myeclinic.presenter.SpecializationPresenter
import com.example.myeclinic.presenter.SpecializationView

class AdminSpecializationActivity : AppCompatActivity(), SpecializationView {

    private lateinit var presenter: SpecializationPresenter
    private lateinit var listView: ListView
    private lateinit var etSpecialization: EditText
    private lateinit var btnAdd: Button
    private lateinit var btnRemove: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_specialization)

        listView = findViewById(R.id.list_specializations)
        etSpecialization = findViewById(R.id.et_specialization)
        btnAdd = findViewById(R.id.btn_add_specialization)
        btnRemove = findViewById(R.id.btn_remove_specialization)

        presenter = SpecializationPresenter(this)
        presenter.loadSpecializations()

        btnAdd.setOnClickListener {
            val specialization = etSpecialization.text.toString().trim()
            if (specialization.isNotEmpty()) {
                presenter.addSpecialization(specialization)
                etSpecialization.text.clear()
            }
        }

        btnRemove.setOnClickListener {
            val specialization = etSpecialization.text.toString().trim()
            if (specialization.isNotEmpty()) {
                presenter.removeSpecialization(specialization)
                etSpecialization.text.clear()
            }
        }
    }

    override fun showSpecializations(specializations: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, specializations)
        listView.adapter = adapter
    }

    override fun onSpecializationAdded() {
        presenter.loadSpecializations()
        Toast.makeText(this, "Specialization added", Toast.LENGTH_SHORT).show()
    }

    override fun onSpecializationRemoved() {
        presenter.loadSpecializations()
        Toast.makeText(this, "Specialization removed", Toast.LENGTH_SHORT).show()
    }
}
