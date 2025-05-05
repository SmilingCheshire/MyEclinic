package com.example.myeclinic.presenter

import com.example.myeclinic.model.Doctor
import com.example.myeclinic.presenter.DoctorProfileView
import com.google.firebase.firestore.FirebaseFirestore

class DoctorProfilePresenter(private val view: DoctorProfileView) {

    private val db = FirebaseFirestore.getInstance()

    fun loadDoctorProfile(doctorId: String) {
        db.collection("doctors").document(doctorId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val doctor = document.toObject(Doctor::class.java)
                    doctor?.let {
                        view.showDoctorInfo(it)
                    } ?: view.showError("Invalid doctor data")
                } else {
                    view.showError("Doctor not found")
                }
            }
            .addOnFailureListener {
                view.showError("Failed to load profile")
            }
    }

    fun updateDoctorDirectly(doctorId: String, doctor: Doctor) {
        db.collection("doctors").document(doctorId).set(doctor)
            .addOnSuccessListener {
                view.showError("Doctor updated successfully")
            }
            .addOnFailureListener {
                view.showError("Failed to update doctor")
            }
    }

    fun storePendingUpdate(doctorId: String, doctor: Doctor) {
        db.collection("doctors").document(doctorId)
            .collection("pendingUpdates").document("info")
            .set(doctor)
            .addOnSuccessListener {
                view.showError("Changes sent for admin approval")
            }
            .addOnFailureListener {
                view.showError("Failed to send pending changes")
            }
    }

    fun retireDoctor(doctorId: String, role: String) {
        if (role == "Admin") {
            db.collection("doctors").document(doctorId)
                .update("isRetired", true)
                .addOnSuccessListener {
                    view.showError("Doctor retired")
                }
                .addOnFailureListener {
                    view.showError("Failed to retire doctor")
                }
        } else if (role == "Doctor") {
            db.collection("doctors").document(doctorId)
                .collection("pendingUpdates")
                .document("retire")
                .set(mapOf("isRetired" to true))
                .addOnSuccessListener {
                    view.showError("Retire request sent to admin")
                }
                .addOnFailureListener {
                    view.showError("Failed to send retire request")
                }
        }
    }
}

interface DoctorProfileView {
    fun showDoctorInfo(doctor: Doctor)
    fun showError(message: String)
}