package com.example.myeclinic.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myeclinic.R
import com.example.myeclinic.view.QuickAssessmentActivity

class SpecializationOptionsActivity : AppCompatActivity() {

    private lateinit var specialization: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_specialization_options)

        // Get info passed from previous activity
        specialization = intent.getStringExtra("specialization") ?: "Unknown"

        val textSpecialization = findViewById<TextView>(R.id.text_specialization)
        val buttonQuickAssessment = findViewById<Button>(R.id.button_quick_assessment)
        val buttonChooseDoctor = findViewById<Button>(R.id.button_choose_doctor)

        textSpecialization.text = "Specialization: $specialization"

        buttonQuickAssessment.setOnClickListener {
            val intent = Intent(this, QuickAssessmentActivity::class.java)
            intent.putExtra("specialization", specialization)
            startActivity(intent)
        }

        buttonChooseDoctor.setOnClickListener {
            val intent = Intent(this, DoctorListActivity::class.java)
            intent.putExtra("specialization", specialization)
            startActivity(intent)
        }
    }
}
