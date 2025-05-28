package com.example.myeclinic.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.myeclinic.R
import com.example.myeclinic.util.UserSession

class PatientActivity : AppCompatActivity() {

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
