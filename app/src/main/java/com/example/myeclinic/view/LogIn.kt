package com.example.myeclinic.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myeclinic.R
import com.example.myeclinic.presenter.LogInPresenter
import com.example.myeclinic.repository.UserRepository
import com.example.myeclinic.util.LogInView
import com.example.myeclinic.util.UserSession
import com.google.firebase.messaging.FirebaseMessaging
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class LogIn : AppCompatActivity(), LogInView {
    private lateinit var presenter: LogInPresenter
    private val NOTIFICATION_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        presenter = LogInPresenter(this) // Connects presenter to this activity

        val emailInput = findViewById<EditText>(R.id.etEmail)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val toRegistration = findViewById<TextView>(R.id.tvRegister)

        requestNotificationPermission()

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            presenter.performLogin(email, password)
        }

        toRegistration.setOnClickListener {
            val intent = Intent(this, Registration::class.java)
            startActivity(intent)
        }

    }

    override fun onLoginSuccess() {
        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                val user = UserSession.currentUser
                if (user != null) {
                    val userRepo = UserRepository()
                    userRepo.updateUserToken(user.userId, token, user.role)
                }
            } else {
                Log.e("FCM", "Fetching FCM token failed", task.exception)
            }
        }

    }

    override fun onLoginFailure(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun navigateToDoctorDashboard() {
        //startActivity(Intent(this, SpecializationActivity::class.java))
        startActivity(Intent(this, DoctorActivity::class.java))
        finish()
    }

    override fun navigateToPatientDashboard() {
        Log.d("Navigation", "Navigating to PatientSpecializationActivity")
        startActivity(Intent(this, PatientActivity::class.java))
        finish()
    }

    override fun navigateToAdminDashboard() {
        Log.d("Navigation", "Navigating to AdminActivity")
        startActivity(Intent(this, AdminDashboardActivity::class.java))
        finish()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }
}


