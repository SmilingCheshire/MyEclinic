package com.example.myeclinic.presenter

import android.util.Patterns
import com.example.myeclinic.repository.UserRepository

class RegistrationPresenter(private val view: RegistrationView) {
    private val userRepository = UserRepository()

    fun handleRegistration(
        email: String,
        password: String,
        repeatPassword: String,
        name: String,
        contactNumber: String
    ) {
        if (email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty() || name.isEmpty()) {
            view.showError("All fields are required!")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            view.showError("Invalid email format!")
            return
        }

        if (password != repeatPassword) {
            view.showError("Passwords do not match!")
            return
        }

        val role = "Patient" // Patients only!

        userRepository.registerUser(email, password, name, contactNumber, role) { success, errorMessage ->
        if (success) {
                view.navigateToPatientDashboard()
            } else {
                view.showError(errorMessage ?: "Registration failed.")
            }
        }
    }
}

interface RegistrationView {
    fun showError(message: String)
    fun navigateToPatientDashboard()
}
