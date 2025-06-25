package com.example.myeclinic.view

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myeclinic.R
import com.example.myeclinic.presenter.RegistrationPresenter
import com.example.myeclinic.presenter.RegistrationView

class Registration : AppCompatActivity(), RegistrationView {

    private lateinit var presenter: RegistrationPresenter
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etRepeatPassword: EditText
    private lateinit var etUsername: EditText
    private lateinit var etContactNumber: EditText
    /**
     * Called when the `Registration` activity is created.
     *
     * This activity allows users to register for the application. The following user inputs are collected:
     * - Username
     * - Email
     * - Password
     * - Repeated password (confirmation)
     * - Contact number
     *
     * Main actions:
     * - Initializes the input fields and presenter.
     * - Binds the registration button to trigger the presenter’s `handleRegistration()` method with all inputs.
     * - Provides a navigation option to return to the login screen.
     *
     * Presenter is responsible for handling business logic such as:
     * - Validating input
     * - Registering the user in Firebase or another backend
     * - Navigating to the appropriate dashboard after successful registration
     *
     * @param savedInstanceState Previously saved instance state, if the activity is being re-initialized after being shut down.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        presenter = RegistrationPresenter(this)
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etRepeatPassword = findViewById(R.id.etRepeatPassword)
        etContactNumber = findViewById(R.id.etContactNumber)

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val repeatPassword = etRepeatPassword.text.toString().trim()
            val contactNumber = etContactNumber.text.toString().trim()

            presenter.handleRegistration(email, password, repeatPassword, username, contactNumber)
        }

        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        tvLogin.setOnClickListener {
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
        }
    }
    /**
     * Displays a brief toast message to inform the user of an error.
     *
     * @param message The error message to be displayed.
     */
    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    /**
     * Navigates the user to the patient dashboard screen.
     *
     * This method starts the [PatientActivity] and finishes the current activity to prevent the user
     * from returning to the previous screen via the back button.
     */
    override fun navigateToPatientDashboard() {
        startActivity(Intent(this, PatientActivity::class.java))
        finish()
    }
}
