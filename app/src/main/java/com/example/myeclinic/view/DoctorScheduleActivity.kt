package com.example.myeclinic.view

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myeclinic.R
import com.example.myeclinic.presenter.DoctorSchedulePresenter
import com.example.myeclinic.util.AppointmentDay
import com.example.myeclinic.util.UserSession
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class DoctorScheduleActivity : AppCompatActivity() {

    private lateinit var presenter: DoctorSchedulePresenter
    private lateinit var tvCurrentDate: TextView
    private lateinit var layoutSchedule: LinearLayout
    private lateinit var btnPrevDate: ImageButton
    private lateinit var btnNextDate: ImageButton

    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy\nEEEE", Locale.getDefault())
    private var currentIndex = 0
    private var days: List<AppointmentDay> = emptyList()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDay(index: Int) {
        val appointmentDay = days[index]
        val date = LocalDate.parse(appointmentDay.date)
        tvCurrentDate.text = date.format(formatter)

        layoutSchedule.removeAllViews()
        appointmentDay.timeslots.forEach { slot ->
            val view = layoutInflater.inflate(R.layout.item_timeslot, layoutSchedule, false)
            val tvSlot = view.findViewById<TextView>(R.id.tvSlotTime)
            val isBooked = slot["booked"] == true
            val hour = slot["hour"] as? String ?: return@forEach
            tvSlot.text = hour
            tvSlot.isEnabled = !isBooked
            tvSlot.setBackgroundColor(
                if (isBooked) Color.LTGRAY else Color.parseColor("#D0F0C0")
            )
            tvSlot.setOnClickListener {
                if (isBooked) showPatientDialog(hour)
            }
            layoutSchedule.addView(view)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_schedule)

        tvCurrentDate = findViewById(R.id.tvCurrentDate)
        layoutSchedule = findViewById(R.id.layoutSchedule)
        btnPrevDate = findViewById(R.id.btnPrevDate)
        btnNextDate = findViewById(R.id.btnNextDate)

        val firestore = FirebaseFirestore.getInstance()
        val user = UserSession.currentUser
        val doctorId = user?.userId ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        // fetch specialization
        firestore.collection("doctors").document(doctorId).get()
            .addOnSuccessListener { doc ->
                val specialization = doc.getString("specialization") ?: ""
                presenter = DoctorSchedulePresenter(firestore, doctorId, specialization)
                presenter.loadSchedule { scheduleList ->
                    days = scheduleList.sortedBy { it.date }
                    if (days.isNotEmpty()) showDay(0)
                }
                btnPrevDate.setOnClickListener {
                    if (currentIndex > 0) {
                        currentIndex--
                        showDay(currentIndex)
                    }
                }
                btnNextDate.setOnClickListener {
                    if (currentIndex < days.size - 1) {
                        currentIndex++
                        showDay(currentIndex)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load doctor data", Toast.LENGTH_SHORT).show()
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showPatientDialog(hour: String) {
        val selectedDate = days[currentIndex].date
        presenter.fetchBookingDetails(
            date = selectedDate,
            time = hour,
            onResult = { name, patientId, description ->
                AlertDialog.Builder(this)
                    .setTitle("Booking Info")
                    .setMessage(
                        "Patient: $name\nID: $patientId\nTime: $hour\nDate: $selectedDate" +
                                "\n\nProblem:\n$description"
                    )
                    .setPositiveButton("OK", null)
                    .show()
            },
            onError = {
                Toast.makeText(this, "Booking info not found.", Toast.LENGTH_SHORT).show()
            }
        )
    }
}