package com.example.myeclinic.presenter

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore

class UsersPresenter(private val view: UsersView) {

    private val db = FirebaseFirestore.getInstance()
    private val allRoles = listOf("Doctor", "Patient", "Admin")


    fun loadUsers() {
        val users = mutableListOf<UserData>()
        val collections = listOf("patients", "doctors", "admins")

        val tasks = collections.map { collection ->
            db.collection(collection).get().continueWith { task ->
                task.result?.documents?.map { doc ->
                    val data = doc.data ?: return@map null
                    UserData(
                        userId = doc.id,
                        name = data["name"] as? String ?: "",
                        role = data["role"] as? String ?: "",
                        data = data,
                        sourceCollection = collection
                    )
                } ?: listOf()
            }
        }

        Tasks.whenAllComplete(tasks).addOnSuccessListener {
            tasks.forEach { task ->
                users.addAll(task.result.filterNotNull())
            }
            view.setFullUserList(users.filterNotNull())
            view.showUsers(users.filterNotNull())
        }
    }

    fun changeUserRole(user: UserData, newRole: String) {
        if (user.role == newRole) return

        val newCollection = when (newRole.lowercase()) {
            "doctor" -> "doctors"
            "patient" -> "patients"
            "admin" -> "admins"
            else -> return
        }

        val updatedData = user.data.toMutableMap()
        updatedData["role"] = newRole

        when (newRole.lowercase()) {
            "doctor" -> {
                updatedData["doctorId"] = user.userId
                updatedData.remove("patientId")
                updatedData.remove("adminId")
            }
            "patient" -> {
                updatedData["patientId"] = user.userId
                updatedData.remove("doctorId")
                updatedData.remove("adminId")
            }
            "admin" -> {
                updatedData["adminId"] = user.userId
                updatedData.remove("doctorId")
                updatedData.remove("patientId")
            }
        }

        db.collection(newCollection).document(user.userId).set(updatedData)
            .addOnSuccessListener {
                db.collection(user.sourceCollection).document(user.userId).delete()
                    .addOnSuccessListener { loadUsers() }
            }
    }

    fun filterUsers(query: String, roleFilter: String?, users: List<UserData>) {
        val filtered = users.filter {
            it.name.contains(query, ignoreCase = true) &&
                    (roleFilter == null || it.role.equals(roleFilter, ignoreCase = true))
        }
        view.showUsers(filtered)
    }

    fun getAllRoles(): List<String> = allRoles
}

data class UserData(
    val userId: String,
    val name: String,
    val role: String,
    val data: Map<String, Any>,
    val sourceCollection: String
)

interface UsersView {
    fun setFullUserList(users: List<UserData>)
    fun showUsers(users: List<UserData>)
}
