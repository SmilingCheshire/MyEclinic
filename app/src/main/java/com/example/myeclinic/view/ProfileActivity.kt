package com.example.myeclinic.view

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myeclinic.R
import com.example.myeclinic.model.Admin
import com.example.myeclinic.model.Doctor
import com.example.myeclinic.model.Patient
import com.example.myeclinic.presenter.ProfilePresenter
import com.example.myeclinic.util.UserSession

class ProfileActivity : AppCompatActivity(), ProfilePresenter.ProfileView {

    private lateinit var tvName: TextView
    private lateinit var tvSpecialty: TextView
    private lateinit var tvBio: TextView
    private lateinit var tvContact: TextView
    private lateinit var etName: EditText
    private lateinit var etSpecialty: EditText
    private lateinit var etBio: EditText
    private lateinit var etContact: EditText
    private lateinit var btnEdit: Button
    private lateinit var btnRetire: Button
    private lateinit var btnSave: Button
    private lateinit var btnDiscard: Button

    private lateinit var presenter: ProfilePresenter

    private var role: String = "Patient"
    private var userId: String? = null

    private var loadedDoctor: Doctor? = null
    private var loadedPatient: Patient? = null
    private var loadedAdmin: Admin? = null
    private var isRetirementRequest = false
    /**
     * Called when the `ProfileActivity` is created.
     *
     * This activity displays and optionally allows editing of the current user's profile,
     * depending on their role (`Doctor`, `Patient`, or `Admin`). It supports:
     * - Viewing name, contact, bio, and specialization.
     * - Editing (for doctors), including requesting retirement.
     * - Saving edits via the presenter logic.
     *
     * Workflow:
     * - Loads the current user from [UserSession].
     * - Initializes UI components including both TextViews (for view mode)
     *   and EditTexts (for edit mode).
     * - Sets up click listeners for editing, retiring, saving, and discarding changes.
     *
     * Behavior varies by role:
     * - **Doctor**: Can edit profile and request retirement.
     * - **Patient** and **Admin**: Can only view profile.
     *
     * @param savedInstanceState Bundle with the activity’s previously saved state, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = UserSession.currentUser
        role = user?.role ?: "Patient"
        userId = user?.userId


        setContentView(R.layout.activity_profile)

        // Bind views
        tvName = findViewById(R.id.tv_name)
        tvSpecialty = findViewById(R.id.tv_specialty)
        tvBio = findViewById(R.id.tv_bio)
        tvContact = findViewById(R.id.tv_contact)

        etName = findViewById(R.id.et_name)
        etSpecialty = findViewById(R.id.et_specialty)
        etBio = findViewById(R.id.et_bio)
        etContact = findViewById(R.id.et_contact)

        btnEdit = findViewById(R.id.btn_edit)
        btnRetire = findViewById(R.id.btn_retire)
        btnSave = findViewById(R.id.btn_save)
        btnDiscard = findViewById(R.id.btn_discard)

        presenter = ProfilePresenter(this, userId!!, role)

        if (userId != null) {
            presenter.loadProfile()
        } else {
            showError("Doctor ID not provided")
            finish()
        }

        btnEdit.setOnClickListener {
            isRetirementRequest = false
            enterEditMode()
        }

        btnRetire.setOnClickListener {
            isRetirementRequest = true
            enterEditMode()
        }

        btnSave.setOnClickListener {
            val updatedDoctor = loadedDoctor?.copy(
                name = etName.text.toString(),
                specialization = etSpecialty.text.toString(),
                bio = etBio.text.toString(),
                contactNumber = etContact.text.toString(),
                retired = isRetirementRequest
            ) ?: return@setOnClickListener

            presenter.createRequest(userId!!, updatedDoctor)
            exitEditMode()
        }

        btnDiscard.setOnClickListener {
            exitEditMode()
        }
    }
    /**
     * Displays the doctor’s information in the profile view.
     *
     * Internally delegates to [showInfo] to handle the UI population.
     *
     * @param doctor The [Doctor] object containing profile data.
     */
    override fun showDoctorInfo(doctor: Doctor) {
        showInfo(doctor)
    }
    /**
     * Displays the patient's information in the profile view.
     *
     * Populates the patient details and switches to read-only text mode.
     *
     * @param patient The [Patient] object containing profile data.
     */
    override fun showPatientInfo(patient: Patient) {
        loadedPatient = patient
        tvName.text = patient.name
        tvSpecialty.text = ""
        tvBio.text = patient.medicalHistory
        tvContact.text = patient.contactNumber
        showTextMode()
    }
    /**
     * Displays the admin's information in the profile view.
     *
     * Populates the admin contact data and switches to read-only text mode.
     *
     * @param admin The [Admin] object containing profile data.
     */
    override fun showAdminInfo(admin: Admin) {
        loadedAdmin = admin
        tvName.text = admin.name
        tvSpecialty.text = ""
        tvBio.text = ""
        tvContact.text = admin.contactNumber
        showTextMode()
    }
    /**
     * Populates the profile UI with doctor-specific details.
     *
     * @param doctor The [Doctor] object containing profile data.
     */
    private fun showInfo(doctor: Doctor) {
        loadedDoctor = doctor
        tvName.text = doctor.name
        tvSpecialty.text = doctor.specialization
        tvBio.text = doctor.bio
        tvContact.text = doctor.contactNumber
        showTextMode()
    }

    /**
     * Switches the UI to edit mode.
     *
     * Replaces TextViews with EditTexts and shows save/discard buttons.
     */
    private fun enterEditMode() {
        etName.setText(tvName.text)
        etSpecialty.setText(tvSpecialty.text)
        etBio.setText(tvBio.text)
        etContact.setText(tvContact.text)

        tvName.visibility = View.GONE
        tvSpecialty.visibility = View.GONE
        tvBio.visibility = View.GONE
        tvContact.visibility = View.GONE

        etName.visibility = View.VISIBLE
        etSpecialty.visibility = View.VISIBLE
        etBio.visibility = View.VISIBLE
        etContact.visibility = View.VISIBLE

        btnEdit.visibility = View.GONE
        btnRetire.visibility = View.GONE
        btnSave.visibility = View.VISIBLE
        btnDiscard.visibility = View.VISIBLE
    }
    /**
     * Exits the edit mode and updates the TextViews with edited values.
     */
    private fun exitEditMode() {
        tvName.text = etName.text.toString()
        tvSpecialty.text = etSpecialty.text.toString()
        tvBio.text = etBio.text.toString()
        tvContact.text = etContact.text.toString()

        showTextMode()
    }
    /**
     * Displays the profile in read-only text mode.
     *
     * Adjusts the visibility of views based on the user's role.
     */
    private fun showTextMode() {
        tvName.visibility = View.VISIBLE
        tvSpecialty.visibility = View.VISIBLE
        tvBio.visibility = View.VISIBLE
        tvContact.visibility = View.VISIBLE

        etName.visibility = View.GONE
        etSpecialty.visibility = View.GONE
        etBio.visibility = View.GONE
        etContact.visibility = View.GONE

        if (role == "Doctor") {
            btnEdit.visibility = View.VISIBLE
            btnRetire.visibility = View.VISIBLE
        } else {
            btnEdit.visibility = View.GONE
            btnRetire.visibility = View.GONE
        }
        btnSave.visibility = View.GONE
        btnDiscard.visibility = View.GONE
    }
    /**
     * Shows a short Toast with an error message.
     *
     * @param message The error message to display.
     */
    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    /**
     * Shows a longer Toast with a general message (e.g., confirmation).
     *
     * @param message The message to display.
     */
    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
