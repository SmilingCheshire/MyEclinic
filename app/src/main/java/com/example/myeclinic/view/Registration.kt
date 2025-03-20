package com.example.myeclinic.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myeclinic.R
import com.example.myeclinic.presenter.RegistrationPresenter
import com.example.myeclinic.presenter.RegistrationView

class Registration : AppCompatActivity(), RegistrationView {

    private lateinit var presenter: RegistrationPresenter
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etRepeatPassword: EditText
    private lateinit var etUsername: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        presenter = RegistrationPresenter(this)

        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etRepeatPassword = findViewById(R.id.etRepeatPassword)

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val repeatPassword = etRepeatPassword.text.toString().trim()

            presenter.handleRegistration(email, password, repeatPassword, username)
        }

        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        tvLogin.setOnClickListener {
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
        }
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun navigateToLobby() {
        val intent = Intent(this, SpecializationActivity::class.java)
        startActivity(intent)
        finish()
    }
}
