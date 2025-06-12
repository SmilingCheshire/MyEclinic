package com.example.myeclinic.presenter

import com.example.myeclinic.model.RequestData
import com.google.firebase.firestore.FirebaseFirestore

class RequestsPresenter(private val view: RequestsView) {

    private val db = FirebaseFirestore.getInstance()

    fun loadRequests() {
        db.collection("request").get().addOnSuccessListener { snapshot ->
            val requests = snapshot.documents.mapNotNull { doc ->
                doc.toObject(RequestData::class.java)
            }
            view.onRequestsLoaded(requests)
        }
    }

    fun compareWithDatabase(request: RequestData) {
        val collection = when (request.role.lowercase()) {
            "doctor" -> "doctors"
            "patient" -> "patients"
            else -> return
        }

        db.collection(collection).document(request.userId).get()
            .addOnSuccessListener { doc ->
                val currentData = doc.data ?: return@addOnSuccessListener
                val changes = mutableMapOf<String, Pair<Any?, Any?>>()

                // Common fields
                if (request.name != currentData["name"]) changes["name"] = currentData["name"] to request.name
                if (request.contactNumber != currentData["contuctNumber"]) changes["contactNumber"] = currentData["contuctNumber"] to request.contactNumber

                // Role-specific fields
                if (collection == "doctors") {
                    if (request.bio != currentData["bio"]) changes["bio"] = currentData["bio"] to request.bio
                    if (request.specialization != currentData["specialization"]) changes["specialization"] = currentData["specialization"] to request.specialization
                    if (request.retired != currentData["retired"]) changes["retired"] = currentData["retired"] to request.retired
                } else if (collection == "patients") {
                    if (request.bio != currentData["medicalHistory"]) {
                        changes["medicalHistory"] = currentData["medicalHistory"] to request.bio
                    }
                }

                view.showRequestDialog(changes, request)
            }
    }

    fun applyChanges(request: RequestData, changes: Map<String, Pair<Any?, Any?>>) {
        val collection = when (request.role.lowercase()) {
            "doctor" -> "doctors"
            "patient" -> "patients"
            else -> return
        }

        val updateMap = mutableMapOf<String, Any?>()
        for ((field, change) in changes) {
            val key = when {
                collection == "patients" && field == "medicalHistory" -> "medicalHistory"
                field == "contactNumber" -> "contuctNumber"
                else -> field
            }
            updateMap[key] = change.second
        }

        db.collection(collection).document(request.userId).update(updateMap)
            .addOnSuccessListener {
                deleteRequest(request.role)
            }
    }

    fun discardChanges(request: RequestData) {
        deleteRequest(request.role)
    }


    private fun deleteRequest(role: String) {
        db.collection("request").document(role).delete()
    }

}

interface RequestsView {
    fun onRequestsLoaded(requests: List<RequestData>)
    fun showRequestDialog(
        changes: Map<String, Pair<Any?, Any?>>,
        request: RequestData
    )

}
