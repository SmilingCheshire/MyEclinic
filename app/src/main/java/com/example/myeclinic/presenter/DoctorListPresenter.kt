package com.example.myeclinic.presenter

import com.example.myeclinic.model.Doctor
import com.example.myeclinic.view.DoctorListActivity
import com.google.firebase.firestore.FirebaseFirestore

class DoctorListPresenter(private val view: DoctorListActivity) {
    private val db = FirebaseFirestore.getInstance()
    /**
     * Loads a list of doctors matching the given specialization from Firestore.
     *
     * The specialization string is normalized to lowercase before querying. The result is mapped into
     * a list of [Doctor] objects and passed to the view. If the query fails, an error message is shown.
     *
     * @param specialization The specialization used to filter doctors (e.g., "cardiology").
     */
    fun loadDoctorsBySpecialization(specialization: String) {
        val normalizedSpecialization = specialization.lowercase()

        db.collection("doctors")
            .whereEqualTo("specialization", normalizedSpecialization)
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
/**
 * View interface for displaying doctor data and error messages in the UI.
 */
interface DoctorView {
    fun showDoctors(doctors: List<Doctor>)
    fun showError(message: String)
}
