package com.example.myeclinic.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.myeclinic.R

class AdminDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        findViewById<Button>(R.id.btnRequests).setOnClickListener {
            startActivity(Intent(this, RequestsActivity::class.java))
        }

        findViewById<Button>(R.id.btnUsers).setOnClickListener {
            startActivity(Intent(this, UsersActivity::class.java))
        }

        findViewById<Button>(R.id.btnSpecializations).setOnClickListener {
            startActivity(Intent(this, SpecializationActivity::class.java))
        }

        findViewById<Button>(R.id.btnAppointments).setOnClickListener {
            startActivity(Intent(this, AppointmentManagementActivity::class.java))
        }

        findViewById<Button>(R.id.btnAvailability).setOnClickListener {
            startActivity(Intent(this, MedicalHistoryActivity::class.java))
        }

        findViewById<Button>(R.id.btnCommunication).setOnClickListener {
            startActivity(Intent(this, CommunicationOversightActivity::class.java))
        }
    }
}
