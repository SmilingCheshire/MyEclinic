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
    /**
     * Initializes the `RequestsActivity`, which allows an admin to view and handle user-submitted requests.
     *
     * This activity is responsible for:
     * - Loading and displaying all pending profile update requests (e.g., doctor retirement or profile changes).
     * - Interacting with the presenter to compare requests against the current database state.
     * - Providing user interface elements for reviewing and approving or discarding requested changes.
     *
     * Flow:
     * - Sets the content view to `activity_requests`.
     * - Initializes the presenter and request container view.
     * - Requests data via the presenter with `loadRequests()`.
     *
     * The presenter communicates back via the `RequestsView` interface callbacks:
     * - `onRequestsLoaded()` for displaying the list of requests.
     * - `showRequestDialog()` to allow the admin to approve or reject changes.
     *
     * @param savedInstanceState Optional saved state of the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_requests)

        requestContainer = findViewById(R.id.requestContainer)
        presenter = RequestsPresenter(this)
        presenter.loadRequests()
    }
    /**
     * Called when a list of pending user modification requests is loaded.
     *
     * This method updates the UI by displaying each request in the `requestContainer`.
     * If the list is empty, it shows a message indicating there are no pending requests.
     *
     * @param requests A list of [RequestData] objects representing profile change requests.
     */
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
    /**
     * Displays a dialog showing the differences between the current database values
     * and a pending profile update request.
     *
     * The dialog presents each changed field, and allows the admin to either:
     * - **Confirm** the changes (applies them to the database),
     * - **Discard** the changes (rejects the request),
     * - **Cancel** (closes the dialog without taking action).
     *
     * @param changes A map of changed fields with pairs of current and requested values.
     * @param request The original [RequestData] object associated with the changes.
     */
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

