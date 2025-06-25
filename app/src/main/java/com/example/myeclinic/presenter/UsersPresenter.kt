package com.example.myeclinic.presenter

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore

class UsersPresenter(private val view: UsersView) {

    private val db = FirebaseFirestore.getInstance()
    private val allRoles = listOf("Doctor", "Patient", "Admin")

    /**
     * Loads all users from Firestore across the `patients`, `doctors`, and `admins` collections.
     *
     * Each user is wrapped in a [UserData] object including their metadata and source collection.
     * After loading, the full list is passed to [UsersView.setFullUserList] and [UsersView.showUsers].
     */
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
    /**
     * Changes the role of a user by moving their document to a different collection.
     *
     * Updates their role field and sets the appropriate ID field based on the new role.
     * The document is then removed from the original collection, and users are reloaded.
     *
     * @param user The user whose role is to be changed.
     * @param newRole The new role to assign ("Doctor", "Patient", or "Admin").
     */
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
    /**
     * Filters a list of users by name and optionally by role.
     *
     * The filtered list is passed to [UsersView.showUsers] for UI display.
     *
     * @param query A partial or full name to match (case-insensitive).
     * @param roleFilter Optional role to filter by ("Doctor", "Patient", "Admin").
     * @param users The full list of [UserData] to search within.
     */
    fun filterUsers(query: String, roleFilter: String?, users: List<UserData>) {
        val filtered = users.filter {
            it.name.contains(query, ignoreCase = true) &&
                    (roleFilter == null || it.role.equals(roleFilter, ignoreCase = true))
        }
        view.showUsers(filtered)
    }
    /**
     * Returns a list of all supported user roles.
     *
     * @return A list of strings representing roles (e.g., ["Doctor", "Patient", "Admin"]).
     */
    fun getAllRoles(): List<String> = allRoles
}
/**
 * Represents a user fetched from Firestore, including raw data and collection context.
 *
 * @property userId The Firestore document ID of the user.
 * @property name The user's full name.
 * @property role The user's current role.
 * @property data A map of all fields associated with the user.
 * @property sourceCollection The Firestore collection where the user was originally stored.
 */
data class UserData(
    val userId: String,
    val name: String,
    val role: String,
    val data: Map<String, Any>,
    val sourceCollection: String
)
/**
 * View interface for presenting and updating user data in the UI.
 */
interface UsersView {
    fun setFullUserList(users: List<UserData>)
    fun showUsers(users: List<UserData>)
}
