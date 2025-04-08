package com.example.myeclinic.presenter

import com.google.firebase.firestore.FirebaseFirestore

class SpecializationPresenter(private val view: SpecializationView) {

    private val db = FirebaseFirestore.getInstance()
    private val specializationsRef = db.collection("specializations")

    fun loadSpecializations() {
        specializationsRef.get().addOnSuccessListener { result ->
            val specializations = result.documents.map { it.getString("name") ?: "" }
            view.showSpecializations(specializations)
        }
    }

    fun addSpecialization(name: String) {
        val specialization = hashMapOf("name" to name)
        specializationsRef.add(specialization).addOnSuccessListener {
            view.onSpecializationAdded()
        }
    }

    fun removeSpecialization(name: String) {
        specializationsRef.whereEqualTo("name", name).get().addOnSuccessListener { result ->
            for (document in result) {
                specializationsRef.document(document.id).delete().addOnSuccessListener {
                    view.onSpecializationRemoved()
                }
            }
        }
    }
}

interface SpecializationView {
    fun showSpecializations(specializations: List<String>)
    fun onSpecializationAdded()
    fun onSpecializationRemoved()
}
