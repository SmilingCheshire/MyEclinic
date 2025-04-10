package com.example.myeclinic.presenter

import com.example.myeclinic.model.Doctor
import com.example.myeclinic.view.DoctorListActivity
import com.google.firebase.firestore.FirebaseFirestore

class DoctorListPresenter(private val view: DoctorListActivity) {
    private val db = FirebaseFirestore.getInstance()

    fun loadDoctorsBySpecialization(specialization: String) {
        val normalizedSpecialization = specialization.lowercase()

        db.collection("doctors")
            .whereEqualTo("specialty", normalizedSpecialization)
            .get()
            .addOnSuccessListener { result ->
                val doctors = result.documents.mapNotNull { it.toObject(Doctor::class.java) }
                view.showDoctors(doctors)
            }
            .addOnFailureListener {
                view.showError("Failed to fetch doctors")
            }
    }
}
interface DoctorView {
    fun showDoctors(doctors: List<Doctor>)
    fun showError(message: String)
}
