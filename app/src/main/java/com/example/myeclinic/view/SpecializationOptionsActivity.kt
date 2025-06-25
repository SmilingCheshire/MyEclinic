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
    /**
     * Initializes the SpecializationOptionsActivity, which allows a user to proceed with
     * booking an appointment either via quick assessment or by selecting a doctor directly.
     *
     * This activity is launched after a user selects a medical specialization from the list.
     * It displays the chosen specialization and presents two navigation options:
     *
     * 1. **Quick Assessment** – Automatically finds available doctors for a selected date/time.
     * 2. **Choose Doctor** – Lets the user view a list of doctors under this specialization.
     *
     * Functional Flow:
     * - Extracts the specialization string from the Intent extras.
     * - Displays the specialization in a `TextView`.
     * - Binds click listeners to two buttons:
     *   - Navigates to `QuickAssessmentActivity` with the selected specialization.
     *   - Navigates to `DoctorListActivity` with the selected specialization.
     *
     * @param savedInstanceState Optional bundle for restoring activity state.
     */
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
