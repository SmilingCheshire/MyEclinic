package com.example.myeclinic.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.myeclinic.R
/**
 * Called when the activity is starting. Sets up the admin dashboard layout and configures
 * navigation buttons to different admin features.
 *
 * Buttons and their targets:
 * - Requests: Opens [RequestsActivity] for reviewing profile change requests.
 * - Users: Opens [UsersActivity] to view and manage users.
 * - Specializations: Opens [SpecializationActivity] to manage medical specializations.
 * - Appointments: Opens [AppointmentManagementActivity] for viewing/modifying appointments.
 * - Availability: Opens [MedicalHistoryActivity] to view patient history (used here as availability).
 * - Communication: Opens [CommunicationOversightActivity] for overseeing chats.
 *
 * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
 *        this Bundle contains the most recent data. Otherwise, it is null.
 */
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
