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

    private val user = UserSession.currentUser
    private val role = user?.role ?: "Patient"
    private val userId = user?.userId
    private var loadedDoctor: Doctor? = null
    private var loadedPatient: Patient? = null
    private var loadedAdmin: Admin? = null
    private var isRetirementRequest = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        presenter = ProfilePresenter(this)

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

    override fun showDoctorInfo(doctor: Doctor) {
        showInfo(doctor)
    }

    override fun showPatientInfo(patient: Patient) {
        loadedPatient = patient
        tvName.text = patient.name
        tvSpecialty.text = ""
        tvBio.text = patient.medicalHistory
        tvContact.text = patient.contactNumber
        showTextMode()
    }

    override fun showAdminInfo(admin: Admin) {
        loadedAdmin = admin
        tvName.text = admin.name
        tvSpecialty.text = ""
        tvBio.text = ""
        tvContact.text = admin.contactNumber
        showTextMode()
    }

    private fun showInfo(doctor: Doctor) {
        loadedDoctor = doctor
        tvName.text = doctor.name
        tvSpecialty.text = doctor.specialization
        tvBio.text = doctor.bio
        tvContact.text = doctor.contactNumber
        showTextMode()
    }


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

    private fun exitEditMode() {
        tvName.text = etName.text.toString()
        tvSpecialty.text = etSpecialty.text.toString()
        tvBio.text = etBio.text.toString()
        tvContact.text = etContact.text.toString()

        showTextMode()
    }

    private fun showTextMode() {
        tvName.visibility = View.VISIBLE
        tvSpecialty.visibility = View.VISIBLE
        tvBio.visibility = View.VISIBLE
        tvContact.visibility = View.VISIBLE

        etName.visibility = View.GONE
        etSpecialty.visibility = View.GONE
        etBio.visibility = View.GONE
        etContact.visibility = View.GONE

        btnEdit.visibility = View.VISIBLE
        btnRetire.visibility = View.VISIBLE
        btnSave.visibility = View.GONE
        btnDiscard.visibility = View.GONE
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
