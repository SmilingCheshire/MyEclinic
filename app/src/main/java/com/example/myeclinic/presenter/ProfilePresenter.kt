package com.example.myeclinic.presenter

import com.example.myeclinic.model.Admin
import com.example.myeclinic.model.Doctor
import com.example.myeclinic.model.Patient
import com.example.myeclinic.util.UserSession
import com.google.firebase.firestore.FirebaseFirestore
private val db = FirebaseFirestore.getInstance()

class ProfilePresenter (private val view: ProfileView, private val userId: String, private val role: String) {
    /**
     * Loads the user profile from Firestore based on their role.
     *
     * - If the user is a Doctor, Patient, or Admin, their data is retrieved from the respective collection.
     * - The resulting object is passed to the corresponding view method.
     * - Displays an error message via the view in case of failure or unknown role.
     */
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
    /**
     * Submits a profile update request for a doctor.
     *
     * Creates a Firestore document in the `request` collection under the user's role
     * containing updated profile fields such as name, bio, and specialization.
     *
     * @param userId The ID of the user requesting the update.
     * @param updatedDoctor The [Doctor] object containing new profile values.
     */
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
    /**
     * View interface for presenting user profile data and request status in the UI.
     */
    interface ProfileView {
        fun showDoctorInfo(doctor: Doctor)
        fun showPatientInfo(patient: Patient)
        fun showAdminInfo(admin: Admin)

        fun showError(message: String)
        fun showMessage(message: String)
    }

}