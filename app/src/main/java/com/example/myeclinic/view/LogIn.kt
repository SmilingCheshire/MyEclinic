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
    /**
     * Called when the `LogIn` activity is created.
     *
     * This is the entry screen for users to log in to the eClinic application. It initializes UI components,
     * sets up event listeners for login and navigation to the registration screen, and handles notification
     * permission requests for devices running Android 13+.
     *
     * Functionality includes:
     * - Accepting email and password input from the user
     * - Invoking [LogInPresenter] to perform login validation
     * - Redirecting the user to their role-specific dashboard upon successful login (Doctor, Patient, or Admin)
     * - Requesting runtime permission to post notifications if required by Android OS (Tiramisu / API 33+)
     * - Registering for Firebase Cloud Messaging (FCM) token after successful login and saving it to Firestore
     *
     * UI Elements:
     * - [EditText] for email and password
     * - [Button] for login submission
     * - [TextView] for navigating to registration
     *
     * @param savedInstanceState the previously saved instance state, or null if none.
     */
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
    /**
     * Called when login is successful.
     *
     * Displays a success toast, retrieves the Firebase Cloud Messaging (FCM) token,
     * and updates the logged-in user's token in the repository.
     */
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
    /**
     * Called when login fails.
     *
     * Displays a toast with the error message received from the presenter.
     *
     * @param errorMessage The reason why login failed.
     */
    override fun onLoginFailure(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
    /**
     * Navigates the authenticated doctor user to their dashboard.
     *
     * This replaces the current activity with [DoctorActivity].
     */
    override fun navigateToDoctorDashboard() {
        //startActivity(Intent(this, SpecializationActivity::class.java))
        startActivity(Intent(this, DoctorActivity::class.java))
        finish()
    }
    /**
     * Navigates the authenticated patient user to their dashboard.
     *
     * This replaces the current activity with [PatientActivity].
     */
    override fun navigateToPatientDashboard() {
        Log.d("Navigation", "Navigating to PatientSpecializationActivity")
        startActivity(Intent(this, PatientActivity::class.java))
        finish()
    }
    /**
     * Navigates the authenticated admin user to their dashboard.
     *
     * This replaces the current activity with [AdminDashboardActivity].
     */
    override fun navigateToAdminDashboard() {
        Log.d("Navigation", "Navigating to AdminActivity")
        startActivity(Intent(this, AdminDashboardActivity::class.java))
        finish()
    }
    /**
     * Requests the POST_NOTIFICATIONS permission for Android 13+ (API level 33+).
     *
     * This ensures the app can show push notifications when required by the system.
     * Uses runtime permission checks for Android Tiramisu and above.
     */
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


