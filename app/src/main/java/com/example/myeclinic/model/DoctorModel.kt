package com.example.myeclinic.model

import com.google.firebase.database.FirebaseDatabase

class DoctorModel {
    private val database = FirebaseDatabase.getInstance().reference

    data class Doctor(
        val userId: String = "",
        val username: String = "",
        val email: String = "",
        val specialization: String = "",
        val availability: String = "", // Example: "Monday-Friday 9 AM - 5 PM"
        val consultationMode: String = "Chat", // Future: could support Video, Phone, etc.
        val experience: String = "", // Years of experience
        val qualifications: String = "", // Medical degrees
        val profilePictureUrl: String = "",
        val adminPermissions: Boolean = false
    )

    fun saveDoctorData(doctor: Doctor, callback: (Boolean, String?) -> Unit) {
        database.child("doctors").child(doctor.userId).setValue(doctor)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) callback(true, null)
                else callback(false, task.exception?.message)
            }
    }

    fun getDoctorData(userId: String, callback: (Doctor?) -> Unit) {
        database.child("doctors").child(userId).get().addOnSuccessListener { snapshot ->
            val doctor = snapshot.getValue(Doctor::class.java)
            callback(doctor)
        }.addOnFailureListener {
            callback(null)
        }
    }

    fun updateDoctorProfile(userId: String, updatedDoctor: Doctor, callback: (Boolean, String?) -> Unit) {
        database.child("doctors").child(userId).setValue(updatedDoctor)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    fun deleteDoctor(userId: String, callback: (Boolean, String?) -> Unit) {
        database.child("doctors").child(userId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }
}
