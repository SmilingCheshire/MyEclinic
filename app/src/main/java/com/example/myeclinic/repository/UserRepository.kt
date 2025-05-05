package com.example.myeclinic.repository

import com.example.myeclinic.model.Patient
import com.example.myeclinic.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseFirestore.getInstance()

    fun registerUser(
        email: String,
        password: String,
        name: String,
        contactNumber: String,
        role: String = "Patient",
        callback: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    val newPatient = Patient(
                        patientId = userId,
                        name = name,
                        email = email,
                        contactNumber = contactNumber,
                        role = role,
                        medicalHistory = ""
                    )

                    database.collection("patients").document(userId).set(newPatient)
                        .addOnSuccessListener { callback(true, null) }
                        .addOnFailureListener { e -> callback(false, e.message) }
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    fun loginUser(email: String, password: String, callback: (Boolean, User?, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                    // First, check in "patients" collection
                    database.collection("patients").document(userId).get()
                        .addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                val patient = snapshot.toObject(User::class.java)?.copy(userId = snapshot.id)
                                callback(true, patient, null)
                            } else {
                                // If not found in "patients", check in "doctors"
                                database.collection("doctors").document(userId).get()
                                    .addOnSuccessListener { doctorSnapshot ->
                                        if (doctorSnapshot.exists()) {
                                            val doctor = doctorSnapshot.toObject(User::class.java)?.copy(userId = doctorSnapshot.id)
                                            callback(true, doctor, null)
                                        } else {
                                            // If not found in "doctors", check in "admins"
                                            database.collection("admins").document(userId).get()
                                                .addOnSuccessListener { adminSnapshot ->
                                                    if (adminSnapshot.exists()) {
                                                        val admin = adminSnapshot.toObject(User::class.java)?.copy(userId = adminSnapshot.id)
                                                        callback(true, admin, null)
                                                    } else {
                                                        callback(false, null, "User not found in database.")
                                                    }
                                                }
                                                .addOnFailureListener { callback(false, null, "Failed to retrieve admin data.") }
                                        }
                                    }
                                    .addOnFailureListener { callback(false, null, "Failed to retrieve doctor data.") }
                            }
                        }
                        .addOnFailureListener { callback(false, null, "Failed to retrieve patient data.") }
                } else {
                    callback(false, null, task.exception?.message ?: "Login failed.")
                }
            }
    }

}
