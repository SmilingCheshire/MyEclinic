package com.example.myeclinic.repository

import com.example.myeclinic.model.Admin
import com.google.firebase.firestore.FirebaseFirestore

class AdminRepository {
    private val database = FirebaseFirestore.getInstance().collection("admins")

    fun createAdmin(admin: Admin, callback: (Boolean, String?) -> Unit) {
        database.document(admin.adminId).set(admin)
            .addOnSuccessListener { callback(true, null) }
            .addOnFailureListener { e -> callback(false, e.message) }
    }

    fun getAdminData(adminId: String, callback: (Admin?) -> Unit) {
        database.document(adminId).get()
            .addOnSuccessListener { snapshot ->
                val admin = snapshot.toObject(Admin::class.java)
                callback(admin)
            }
            .addOnFailureListener { callback(null) }
    }


}
