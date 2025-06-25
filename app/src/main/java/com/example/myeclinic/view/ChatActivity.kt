package com.example.myeclinic.view

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myeclinic.R
import com.example.myeclinic.presenter.ChatPresenter
import com.example.myeclinic.util.MessageAdapter

class ChatActivity : AppCompatActivity() {

    private lateinit var presenter: ChatPresenter
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var sendButton: Button
    private lateinit var input: EditText
    private lateinit var chatId: String
    private lateinit var otherUserId: String
    /**
     * Initializes the ChatActivity screen where users can view and send messages.
     *
     * Responsibilities:
     * - Retrieves the `chatId` and `otherUserId` from the incoming Intent. If missing, the activity closes.
     * - Displays a toast message if the user is viewing the chat as an admin (monitoring mode).
     * - Sets up the RecyclerView with the [MessageAdapter] to display chat messages.
     * - Initializes the [ChatPresenter] with callbacks for loading messages and handling errors.
     * - Automatically scrolls to the most recent message once loaded.
     * - Listens for send button clicks and triggers message sending via the presenter.
     *
     * Layout resource: `activity_chat.xml`
     *
     * Expected Intent extras:
     * - `"chatId"`: Unique identifier for the chat.
     * - `"otherUserId"`: The ID of the user the current user is chatting with.
     * - `"adminView"` (optional): Boolean indicating if the current user is viewing as an admin.
     *
     * @param savedInstanceState The saved state of the activity, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatId = intent.getStringExtra("chatId") ?: return finish()
        otherUserId = intent.getStringExtra("otherUserId") ?: return finish()

        val isAdminView = intent.getBooleanExtra("adminView", false)
        if (isAdminView) {
            Toast.makeText(this, "Admin view: you're monitoring this chat.", Toast.LENGTH_LONG).show()
        }

        input = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)

        messageAdapter = MessageAdapter()
        val recyclerView = findViewById<RecyclerView>(R.id.messageRecyclerView)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = messageAdapter
        }

        presenter = ChatPresenter(
            chatId = chatId,
            otherUserId = otherUserId,
            onMessagesLoaded = {
                messageAdapter.submitList(it)
                recyclerView.scrollToPosition(it.lastIndex)
            },
            onError = {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        )

        presenter.loadMessages()

        sendButton.setOnClickListener {
            val text = input.text.toString().trim()
            if (text.isNotEmpty()) {
                presenter.sendMessage(text)
                input.setText("")
            }
        }
    }
}
