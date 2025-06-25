package com.example.myeclinic.view

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myeclinic.R
import com.example.myeclinic.presenter.UserData
import com.example.myeclinic.presenter.UsersPresenter
import com.example.myeclinic.presenter.UsersView
import com.example.myeclinic.util.UsersAdapter

class UsersActivity : AppCompatActivity(), UsersView {

    private lateinit var presenter: UsersPresenter
    private lateinit var adapter: UsersAdapter
    private val allUsers = mutableListOf<UserData>()
    private val filteredUsers = mutableListOf<UserData>()
    /**
     * Initializes the UsersActivity screen, which displays a searchable and filterable list of users.
     *
     * This screen is primarily used by administrators to:
     * - View all registered users (patients, doctors, admins).
     * - Search users by name or email using a `SearchView`.
     * - Filter users by their assigned role via a filter dialog.
     * - Tap a user to view their profile in `AnotherProfileActivity`.
     * - Tap the role text to change the user’s role via a dialog.
     *
     * Key Functional Setup:
     * - Initializes the `UsersPresenter` to manage business logic.
     * - Configures the `RecyclerView` with a `UsersAdapter` to display user cards.
     * - Sets listeners on `SearchView` and filter button for dynamic filtering.
     * - Triggers loading of user data from the presenter.
     *
     * @param savedInstanceState A `Bundle` containing the activity’s previously saved state, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        presenter = UsersPresenter(this)


        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewUsers)
        adapter = UsersAdapter { user, clickedRole ->
            if (clickedRole) showRoleDialog(user)
            else openProfile(user)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val searchView = findViewById<SearchView>(R.id.searchView)
        val filterButton = findViewById<ImageView>(R.id.roleFilterButton)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                presenter.filterUsers(newText.orEmpty(), selectedRoleFilter, allUsers)
                return true
            }
        })

        filterButton.setOnClickListener {
            showRoleFilterDialog()
        }

        presenter.loadUsers()
    }

    private var selectedRoleFilter: String? = null
    /**
     * Displays the filtered or updated list of users by submitting it to the RecyclerView adapter.
     *
     * @param users A list of [UserData] to be displayed in the user list.
     */
    override fun showUsers(users: List<UserData>) {
        adapter.submitList(users)
    }
    /**
     * Sets the internal reference to the full list of users for filtering and searching purposes.
     *
     * @param users A complete list of [UserData] fetched from the backend or data source.
     */
    override fun setFullUserList(users: List<UserData>) {
        allUsers.clear()
        allUsers.addAll(users)
    }
    /**
     * Navigates to the detailed profile page of the selected user.
     *
     * Opens [AnotherProfileActivity] and passes user ID and role as extras.
     *
     * @param user The selected [UserData] whose profile should be opened.
     */
    private fun openProfile(user: UserData) {
        Log.d("UsersActivity", "Opening profile for userId=${user.userId}, role=${user.role}")
        val intent = Intent(this, AnotherProfileActivity::class.java)
        intent.putExtra("userId", user.userId)
        intent.putExtra("userRole", user.role)
        startActivity(intent)
    }
    /**
     * Shows a dialog allowing the admin to change the role of a selected user.
     *
     * Uses a radio group to display available roles and invokes [UsersPresenter.changeUserRole]
     * if the user confirms the change.
     *
     * @param user The [UserData] for whom the role change is to be applied.
     */
    private fun showRoleDialog(user: UserData) {
        val roles = presenter.getAllRoles()
        val currentIndex = roles.indexOf(user.role)
        var selectedIndex = currentIndex

        val dialogView = layoutInflater.inflate(R.layout.dialog_role_selector, null)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroupRoles)


        roles.forEachIndexed { index, role ->
            val radioButton = RadioButton(this).apply {
                text = role
                id = View.generateViewId()
                setPadding(12, 12, 12, 12)
                if (index == currentIndex) isChecked = true
            }
            radioGroup.addView(radioButton)
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Change Role")
            .setView(dialogView)
            .setPositiveButton("Apply") { _, _ ->
                val selectedRadioId = radioGroup.checkedRadioButtonId
                val selectedRole = dialogView.findViewById<RadioButton>(selectedRadioId)?.text.toString()
                presenter.changeUserRole(user, selectedRole)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Displays a dialog for filtering users by their role.
     *
     * When a role is selected, the user list is filtered using the current search query and role.
     */
    private fun showRoleFilterDialog() {
        val roles = listOf("All") + presenter.getAllRoles()
        val currentIndex = roles.indexOfFirst { it.equals(selectedRoleFilter ?: "All", true) }

        AlertDialog.Builder(this)
            .setTitle("Filter by Role")
            .setSingleChoiceItems(roles.toTypedArray(), currentIndex) { dialog, index ->
                selectedRoleFilter = if (roles[index] == "All") null else roles[index]
                val searchQuery = findViewById<SearchView>(R.id.searchView).query.toString()
                presenter.filterUsers(searchQuery, selectedRoleFilter, allUsers)
                dialog.dismiss()
            }
            .show()
    }

}
