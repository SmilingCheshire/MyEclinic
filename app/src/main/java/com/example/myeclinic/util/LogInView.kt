package com.example.myeclinic.util

interface LogInView {
    fun onLoginSuccess()
    fun onLoginFailure(errorMessage: String)
    fun navigateToDoctorDashboard()
    fun navigateToPatientDashboard()
    fun navigateToAdminDashboard()
}
