package com.example.myeclinic.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myeclinic.R
import com.example.myeclinic.presenter.DoctorSchedulePresenter
import com.example.myeclinic.util.UserSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DoctorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor)

        val user = UserSession.currentUser
        val doctorId = user?.userId


        val buttonMe = findViewById<Button>(R.id.button_me)
        val buttonChat = findViewById<Button>(R.id.button_chat)
        val buttonSchedule = findViewById<Button>(R.id.button_schedule)

        buttonMe.setOnClickListener {
            startActivity(Intent(this, DoctorProfileActivity::class.java))
        }

        buttonChat.setOnClickListener {
            startActivity(Intent(this, ChatSelectionActivity::class.java))
        }

        buttonSchedule.setOnClickListener {
            startActivity(Intent(this, DoctorScheduleActivity::class.java))
        }

    }
}
