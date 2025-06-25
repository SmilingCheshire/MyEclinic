package com.example.myeclinic.presenter

import android.util.Patterns
import com.example.myeclinic.repository.UserRepository

class RegistrationPresenter(private val view: RegistrationView) {
    private val userRepository = UserRepository()
    /**
     * Validates the registration form and attempts to register a new patient.
     *
     * This method checks:
     * - All fields are non-empty
     * - Email is in correct format
     * - Passwords match
     *
     * If valid, it registers the user as a "Patient" using the [UserRepository],
     * and navigates to the patient dashboard upon success.
     *
     * @param email The user's email address.
     * @param password The user's chosen password.
     * @param repeatPassword Confirmation of the user's password.
     * @param name The full name of the patient.
     * @param contactNumber The patient's contact number.
     */
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

        userRepository.registerUser(
            email,
            password,
            name,
            contactNumber,
            role
        ) { success, errorMessage ->
            if (success) {
                view.navigateToPatientDashboard()
            } else {
                view.showError(errorMessage ?: "Registration failed.")
            }
        }
    }
}
/**
 * View interface for registration-related UI feedback.
 */
interface RegistrationView {
    fun showError(message: String)
    fun navigateToPatientDashboard()
}
