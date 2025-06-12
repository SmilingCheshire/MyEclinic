package com.example.myeclinic.presenter

import android.util.Log
import com.example.myeclinic.model.User
import com.example.myeclinic.repository.UserRepository
import com.example.myeclinic.util.LogInView
import com.example.myeclinic.util.UserSession
import com.google.firebase.messaging.FirebaseMessaging

class LogInPresenter(private val view: LogInView) {
    private val userRepository = UserRepository()

    fun performLogin(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            view.onLoginFailure("Email and Password are required!")
            return
        }

        userRepository.loginUser(email, password) { success, user, errorMessage ->
            if (success && user != null) {
                UserSession.currentUser = user

                // Step 1: Fetch device token
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        Log.d("FCM", "Fetched FCM Token: $token")


                        // Step 2: Save token to Firestore
                        userRepository.updateUserToken(user.userId, token, user.role)

                        // Step 3: Redirect to appropriate dashboard
                        when (user.role) {
                            "Admin" -> view.navigateToAdminDashboard()
                            "Doctor" -> view.navigateToDoctorDashboard()
                            "Patient" -> view.navigateToPatientDashboard()
                            else -> view.onLoginFailure("Unknown role")
                        }
                    } else {
                        view.onLoginFailure("Failed to retrieve device token.")
                    }
                }
            } else {
                view.onLoginFailure(errorMessage ?: "Login failed.")
            }
        }
    }
}
