package com.example.myeclinic.repository

import com.example.myeclinic.model.Patient
import com.google.firebase.firestore.FirebaseFirestore

class PatientRepository {
    private val database = FirebaseFirestore.getInstance().collection("patients")

    fun createPatient(patient: Patient, callback: (Boolean, String?) -> Unit) {
        database.document(patient.patientId).set(patient)
            .addOnSuccessListener { callback(true, null) }
            .addOnFailureListener { e -> callback(false, e.message) }
    }

    fun getPatientData(patientId: String, callback: (Patient?) -> Unit) {
        database.document(patientId).get()
            .addOnSuccessListener { snapshot ->
                val patient = snapshot.toObject(Patient::class.java)
                callback(patient)
            }
            .addOnFailureListener { callback(null) }
    }
}
