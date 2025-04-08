package com.example.myeclinic.repository

import com.example.myeclinic.model.Doctor
import com.google.firebase.firestore.FirebaseFirestore

class DoctorRepository {
    private val database = FirebaseFirestore.getInstance().collection("doctors")

    fun createDoctor(doctor: Doctor, callback: (Boolean, String?) -> Unit) {
        database.document(doctor.doctorId).set(doctor)
            .addOnSuccessListener { callback(true, null) }
            .addOnFailureListener { e -> callback(false, e.message) }
    }

    fun getDoctorData(doctorId: String, callback: (Doctor?) -> Unit) {
        database.document(doctorId).get()
            .addOnSuccessListener { snapshot ->
                val doctor = snapshot.toObject(Doctor::class.java)
                callback(doctor)
            }
            .addOnFailureListener { callback(null) }
    }
}
