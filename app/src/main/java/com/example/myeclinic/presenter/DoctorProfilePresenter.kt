package com.example.myeclinic.presenter

import com.example.myeclinic.model.Doctor
import com.google.firebase.firestore.FirebaseFirestore

interface DoctorProfileView {
    fun showDoctorInfo(doctor: Doctor)
    fun showError(message: String)
    fun showSchedule(availability: Map<String, List<String>>?)
}

class DoctorProfilePresenter(private val view: DoctorProfileView) {

    private val db = FirebaseFirestore.getInstance()

    fun loadDoctorProfile(doctorId: String) {
        db.collection("doctors").document(doctorId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val doctor = document.toObject(Doctor::class.java)
                    doctor?.let {
                        view.showDoctorInfo(it)
                        view.showSchedule(it.availability)
                    } ?: view.showError("Invalid doctor data")
                } else {
                    view.showError("Doctor not found")
                }
            }
            .addOnFailureListener {
                view.showError("Failed to load profile")
            }
    }
}
