package com.example.myeclinic.util
/**
 * Interface for handling login-related UI interactions and navigation.
 *
 * Implemented by the UI layer to respond to login results and route users
 * to their respective dashboards based on role.
 */
interface LogInView {
    fun onLoginSuccess()
    fun onLoginFailure(errorMessage: String)
    fun navigateToDoctorDashboard()
    fun navigateToPatientDashboard()
    fun navigateToAdminDashboard()
}
