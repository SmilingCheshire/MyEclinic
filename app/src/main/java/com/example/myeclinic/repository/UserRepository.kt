package com.example.myeclinic.repository

import android.util.Log
import com.example.myeclinic.model.Patient
import com.example.myeclinic.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseFirestore.getInstance()
    /**
     * Registers a new user (default role: Patient) using Firebase Authentication and saves user details to Firestore.
     *
     * After successful authentication, a new [Patient] object is created and stored in the `patients` collection.
     *
     * @param email The user's email address.
     * @param password The password for the new user.
     * @param name The full name of the user.
     * @param contactNumber The user's contact number.
     * @param role The user's role (default is "Patient").
     * @param callback Callback invoked after registration attempt:
     *   - `true` and `null` on success,
     *   - `false` and an error message on failure.
     */
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
    /**
     * Logs in a user with Firebase Authentication and retrieves their profile from Firestore.
     *
     * The method checks sequentially in `patients`, `doctors`, and `admins` collections
     * to locate the authenticated user's data.
     *
     * @param email The user's email address.
     * @param password The password to authenticate with.
     * @param callback Callback invoked after login attempt:
     *   - On success: `true`, the [User] object, and `null`
     *   - On failure: `false`, `null`, and an error message
     */
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
    /**
     * Updates the FCM device token for a user in the appropriate Firestore collection.
     *
     * The update is role-sensitive and writes to the correct document based on the user's role.
     *
     * @param userId The Firestore document ID of the user.
     * @param token The FCM device token to store.
     * @param role The user's role ("Patient", "Doctor", or "Admin").
     */
    fun updateUserToken(userId: String, token: String, role: String) {
        val collection = when (role) {
            "Patient" -> "patients"
            "Doctor" -> "doctors"
            "Admin" -> "admins"
            else -> null
        }

        if (collection == null) {
            Log.e("UserRepository", "Unknown role: $role. Cannot update token.")
            return
        }

        val userDoc = FirebaseFirestore.getInstance().collection(collection).document(userId)
        userDoc.update("deviceToken", token)
            .addOnSuccessListener {
                Log.d("UserRepository", "Device token updated successfully.")
            }
            .addOnFailureListener {
                Log.e("UserRepository", "Failed to update device token: ${it.message}")
            }
    }

}
