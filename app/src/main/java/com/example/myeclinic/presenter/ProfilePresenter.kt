package com.example.myeclinic.presenter

import com.example.myeclinic.model.Admin
import com.example.myeclinic.model.Doctor
import com.example.myeclinic.model.Patient
import com.example.myeclinic.util.UserSession
import com.google.firebase.firestore.FirebaseFirestore
private val db = FirebaseFirestore.getInstance()

class ProfilePresenter (private val view: ProfileView, private val userId: String, private val role: String) {
    fun loadProfile() {
        if (userId == null || role == null) {
            view.showError("User not logged in")
            return
        }

        when (role) {
            "Doctor" -> {
                db.collection("doctors").document(userId).get()
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
                        view.showError("Failed to load doctor profile")
                    }
            }

            "Patient" -> {
                db.collection("patients").document(userId).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val patient = document.toObject(Patient::class.java)
                            patient?.let {
                                view.showPatientInfo(it)
                            } ?: view.showError("Invalid patient data")
                        } else {
                            view.showError("Patient not found")
                        }
                    }
                    .addOnFailureListener {
                        view.showError("Failed to load patient profile")
                    }
            }

            "Admin" -> {
                db.collection("admins").document(userId).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val admin = document.toObject(Admin::class.java)
                            admin?.let {
                                view.showAdminInfo(it)
                            } ?: view.showError("Invalid admin data")
                        } else {
                            view.showError("Admin not found")
                        }
                    }
                    .addOnFailureListener {
                        view.showError("Failed to load admin profile")
                    }
            }

            else -> view.showError("Unknown user role")
        }
    }

    fun createRequest(userId: String, updatedDoctor: Doctor) {
        val requestData = hashMapOf(
            "userId" to userId,
            "name" to updatedDoctor.name,
            "specialization" to updatedDoctor.specialization,
            "bio" to updatedDoctor.bio,
            "contactNumber" to updatedDoctor.contactNumber,
            "retired" to updatedDoctor.retired,
            "role" to "Doctor",
            "timestamp" to System.currentTimeMillis()
        )

        if (role != null) {
            db.collection("request").document(role)
                .set(requestData)
                .addOnSuccessListener {
                    view.showMessage("Profile update request sent.")
                }
                .addOnFailureListener {
                    view.showError("Failed to send request.")
                }
        }
    }

    interface ProfileView {
        fun showDoctorInfo(doctor: Doctor)
        fun showPatientInfo(patient: Patient)
        fun showAdminInfo(admin: Admin)

        fun showError(message: String)
        fun showMessage(message: String)
    }

}