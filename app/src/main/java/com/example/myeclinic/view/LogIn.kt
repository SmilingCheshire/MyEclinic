package com.example.myeclinic.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myeclinic.R
import com.example.myeclinic.presenter.LogInPresenter
import com.example.myeclinic.presenter.LogInView


class LogIn : AppCompatActivity(), LogInView {

    private lateinit var presenter: LogInPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        presenter = LogInPresenter(this)

        val emailInput = findViewById<EditText>(R.id.etEmail)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        // Set up the login button click listener
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            presenter.performLogin(email, password) // Pass the email and password to the presenter
        }

        tvRegister.setOnClickListener {
            val intent = Intent(this, Registration::class.java)
            startActivity(intent)
        }
    }

    override fun onLoginSuccess() {
        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, SpecializationActivity::class.java)
        startActivity(intent)
    }

    override fun onLoginFailure(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
}

