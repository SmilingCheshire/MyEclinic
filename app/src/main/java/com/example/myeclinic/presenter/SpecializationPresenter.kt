package com.example.myeclinic.presenter

import com.google.firebase.firestore.FirebaseFirestore

class SpecializationPresenter(private val view: SpecializationView) {

    private val db = FirebaseFirestore.getInstance()
    private val specializationsRef = db.collection("specializations")
    /**
     * Loads all available specializations from the Firestore `specializations` collection.
     *
     * Each document is expected to have a `name` field. The names are passed to [SpecializationView.showSpecializations].
     */
    fun loadSpecializations() {
        specializationsRef.get().addOnSuccessListener { result ->
            val specializations = result.documents.map { it.getString("name") ?: "" }
            view.showSpecializations(specializations)
        }
    }
    /**
     * Adds a new specialization to the Firestore collection.
     *
     * Creates a new document with a single field `"name"`. On success,
     * [SpecializationView.onSpecializationAdded] is called.
     *
     * @param name The name of the new specialization (e.g., "Cardiology").
     */
    fun addSpecialization(name: String) {
        val specialization = hashMapOf("name" to name)
        specializationsRef.add(specialization).addOnSuccessListener {
            view.onSpecializationAdded()
        }
    }
    /**
     * Removes a specialization by name from the Firestore collection.
     *
     * Searches for documents where `name` equals the provided value and deletes them.
     * On success, [SpecializationView.onSpecializationRemoved] is called.
     *
     * @param name The name of the specialization to remove.
     */
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
/**
 * View interface for displaying and managing specialization-related UI updates.
 */
interface SpecializationView {
    fun showSpecializations(specializations: List<String>)
    fun onSpecializationAdded()
    fun onSpecializationRemoved()
}
