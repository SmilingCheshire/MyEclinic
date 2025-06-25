package com.example.myeclinic.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myeclinic.R
import com.example.myeclinic.presenter.SpecializationPresenter
import com.example.myeclinic.presenter.SpecializationView
import com.example.myeclinic.util.UserSession

class SpecializationActivity : AppCompatActivity(), SpecializationView {

    private lateinit var presenter: SpecializationPresenter
    private lateinit var listView: ListView
    private lateinit var etSpecialization: EditText
    private lateinit var btnAdd: Button
    private lateinit var btnRemove: Button

    private val user = UserSession.currentUser
    private val role = user?.role ?: "Patient" // default to "Patient" if null
    /**
     * Initializes the SpecializationActivity, which displays a list of medical specializations.
     *
     * This screen is shared by both Admin and Patient roles, but with different functionalities:
     * - **Admin users** can view, add, and remove specializations.
     * - **Patients** can only view specializations and tap to proceed with booking or viewing options.
     *
     * Functional Flow:
     * - Loads the UI from `activity_admin_specialization.xml`.
     * - Initializes views: specialization list, input field, and action buttons.
     * - Attaches `SpecializationPresenter` to handle logic and data retrieval.
     * - Displays all available specializations using a ListView.
     * - Enables item click to navigate to `SpecializationOptionsActivity` with the selected specialization.
     * - For admins only:
     *   - Allows adding/removing specializations via the presenter.
     * - For non-admin users:
     *   - Hides add/remove UI components (EditText and Buttons).
     *
     * @param savedInstanceState Optional saved state for restoring the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_specialization) // use the same layout for both

        listView = findViewById(R.id.list_specializations)
        etSpecialization = findViewById(R.id.et_specialization)
        btnAdd = findViewById(R.id.btn_add_specialization)
        btnRemove = findViewById(R.id.btn_remove_specialization)

        presenter = SpecializationPresenter(this)
        presenter.loadSpecializations()

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedSpecialization = listView.adapter.getItem(position) as String
            val intent = Intent(this, SpecializationOptionsActivity::class.java)
            intent.putExtra("specialization", selectedSpecialization)
            startActivity(intent)
        }

        if (role == "Admin") {
            btnAdd.setOnClickListener {
                val specialization = etSpecialization.text.toString().trim()
                if (specialization.isNotEmpty()) {
                    presenter.addSpecialization(specialization)
                    etSpecialization.text.clear()
                }
            }

            btnRemove.setOnClickListener {
                val specialization = etSpecialization.text.toString().trim()
                if (specialization.isNotEmpty()) {
                    presenter.removeSpecialization(specialization)
                    etSpecialization.text.clear()
                }
            }
        } else {
            // If not Admin, hide Add/Remove buttons and EditText
            etSpecialization.visibility = View.GONE
            btnAdd.visibility = View.GONE
            btnRemove.visibility = View.GONE
        }
    }
    /**
     * Displays the list of medical specializations in the associated [ListView].
     *
     * This method updates the list view using a simple adapter that presents each specialization
     * as a separate item.
     *
     * @param specializations A list of specialization names to be displayed.
     */
    override fun showSpecializations(specializations: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, specializations)
        listView.adapter = adapter
    }
    /**
     * Callback triggered after a specialization is successfully added.
     *
     * If the current user has the role "Admin", this method reloads the list of specializations
     * and shows a confirmation toast message.
     */
    override fun onSpecializationAdded() {
        if (role == "Admin") {
            presenter.loadSpecializations()
            Toast.makeText(this, "Specialization added", Toast.LENGTH_SHORT).show()
        }
    }
    /**
     * Callback triggered after a specialization is successfully removed.
     *
     * If the current user has the role "Admin", this method reloads the list of specializations
     * and shows a confirmation toast message.
     */
    override fun onSpecializationRemoved() {
        if (role == "Admin") {
            presenter.loadSpecializations()
            Toast.makeText(this, "Specialization removed", Toast.LENGTH_SHORT).show()
        }
    }
}
