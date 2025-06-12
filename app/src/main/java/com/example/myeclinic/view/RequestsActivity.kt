package com.example.myeclinic.view

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myeclinic.R
import com.example.myeclinic.model.RequestData
import com.example.myeclinic.presenter.RequestsPresenter
import com.example.myeclinic.presenter.RequestsView

class RequestsActivity : AppCompatActivity(), RequestsView {

    private lateinit var presenter: RequestsPresenter
    private lateinit var requestContainer: LinearLayout
    private var currentRequests: List<RequestData> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requests)

        requestContainer = findViewById(R.id.requestContainer)
        presenter = RequestsPresenter(this)
        presenter.loadRequests()
    }

    override fun onRequestsLoaded(requests: List<RequestData>) {
        currentRequests = requests
        requestContainer.removeAllViews()

        if (requests.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "No pending requests"
                textSize = 18f
            }
            requestContainer.addView(emptyText)
            return
        }

        requests.forEach { request ->
            val view = LayoutInflater.from(this)
                .inflate(R.layout.item_request_card, requestContainer, false)
            view.findViewById<TextView>(R.id.requestName).text = request.name
            view.findViewById<TextView>(R.id.requestRole).text = request.role
            view.findViewById<TextView>(R.id.requestSummary).text = "Tap to compare changes"

            view.setOnClickListener {
                presenter.compareWithDatabase(request)
            }

            requestContainer.addView(view)
        }
    }

    override fun showRequestDialog(changes: Map<String, Pair<Any?, Any?>>, request: RequestData) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Requested Changes")

        val message = StringBuilder()
        changes.forEach { (field, value) ->
            message.append("🔹 $field\nIn DB: ${value.first}\nRequest: ${value.second}\n\n")
        }

        builder.setMessage(message.toString().ifEmpty { "No changes found." })

        builder.setPositiveButton("Confirm") { _, _ ->
            presenter.applyChanges(request, changes)
        }

        builder.setNegativeButton("Discard") { _, _ ->
            presenter.discardChanges(request)
        }

        builder.setNeutralButton("Cancel", null)
        builder.show()
    }

}

