package com.example.myeclinic.presenter

import com.example.myeclinic.model.User
import com.example.myeclinic.repository.UserRepository
import com.example.myeclinic.util.LogInView

class LogInPresenter(private val view: LogInView) {
    private val userRepository = UserRepository()

    fun performLogin(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            view.onLoginFailure("Email and Password are required!")
            return
        }

        userRepository.loginUser(email, password) { success, user, errorMessage ->
            if (success) {
                when (user?.role) {
                    "Admin" -> view.navigateToAdminDashboard()
                    "Doctor" -> view.navigateToDoctorDashboard()
                    "Patient" -> view.navigateToPatientDashboard()
                    else -> view.onLoginFailure("Unknown role")
                }
            } else {
                view.onLoginFailure(errorMessage ?: "Login failed.")
            }
        }
    }
}
