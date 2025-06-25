package com.example.myeclinic.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.myeclinic.R
import com.example.myeclinic.util.UserSession

class PatientActivity : AppCompatActivity() {
    /**
     * Called when the `PatientActivity` is created.
     *
     * This activity serves as the main dashboard for patients, offering navigation to:
     * - Profile management ([ProfileActivity])
     * - Chat with doctors or admins ([ChatListActivity])
     * - Appointment scheduling ([SpecializationActivity])
     *
     * It retrieves the current user from [UserSession] to access role or ID if needed
     * (though not used directly here, it could be used for future customizations).
     *
     * Buttons:
     * - `button_me`: Navigates to the patient’s profile screen.
     * - `button_chat`: Opens the chat list activity for ongoing conversations.
     * - `button_appointments`: Redirects the patient to select a specialization and book appointments.
     *
     * @param savedInstanceState The saved instance state bundle from a previous state, if available.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient)

        val user = UserSession.currentUser
        val role = user?.role
        val userId = user?.userId


        val buttonMe = findViewById<Button>(R.id.button_me)
        val buttonChat = findViewById<Button>(R.id.button_chat)
        val buttonAppointments = findViewById<Button>(R.id.button_appointments)

        buttonMe.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        buttonChat.setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
        }

        buttonAppointments.setOnClickListener {
            startActivity(Intent(this, SpecializationActivity::class.java))
        }

    }
}
