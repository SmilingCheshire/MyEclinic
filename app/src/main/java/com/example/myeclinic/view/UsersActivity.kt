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

    override fun showUsers(users: List<UserData>) {
        adapter.submitList(users)
    }

    override fun setFullUserList(users: List<UserData>) {
        allUsers.clear()
        allUsers.addAll(users)
    }

    private fun openProfile(user: UserData) {
        Log.d("UsersActivity", "Opening profile for userId=${user.userId}, role=${user.role}")
        val intent = Intent(this, AnotherProfileActivity::class.java)
        intent.putExtra("userId", user.userId)
        intent.putExtra("userRole", user.role)
        startActivity(intent)
    }

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
