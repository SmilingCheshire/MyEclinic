package com.example.myeclinic.view

import com.example.myeclinic.model.Specialization

interface SpecializationView {
    fun showSpecializations(specializations: List<Specialization>)
    fun showMessage(message: String)
    fun showError(error: String)
}
