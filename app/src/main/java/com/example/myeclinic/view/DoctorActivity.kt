package com.example.myeclinic.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.myeclinic.R
import com.example.myeclinic.util.UserSession

class DoctorActivity : AppCompatActivity() {
    /**
     * Initializes the Doctor Dashboard screen.
     *
     * This activity is the main entry point for users with the "Doctor" role.
     * It displays navigation options allowing the doctor to:
     * - View and edit their profile ([ProfileActivity])
     * - Open the list of active chats ([ChatListActivity])
     * - Manage their availability schedule ([DoctorScheduleActivity])
     *
     * Responsibilities:
     * - Inflates the `activity_doctor` layout.
     * - Reads the current user from [UserSession].
     * - Configures click listeners for the dashboard buttons:
     *   - "Me" button navigates to the profile screen.
     *   - "Chat" button navigates to the chat list.
     *   - "Schedule" button navigates to the doctor's schedule management screen.
     *
     * @param savedInstanceState Bundle containing the activity’s previously saved state, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor)

        val user = UserSession.currentUser
        val doctorId = user?.userId


        val buttonMe = findViewById<Button>(R.id.button_me)
        val buttonChat = findViewById<Button>(R.id.button_chat)
        val buttonSchedule = findViewById<Button>(R.id.button_schedule)

        buttonMe.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        buttonChat.setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
        }

        buttonSchedule.setOnClickListener {
            startActivity(Intent(this, DoctorScheduleActivity::class.java))
        }

    }
}
