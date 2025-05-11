package com.example.myeclinic.view

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myeclinic.R
import com.example.myeclinic.presenter.ChatPresenter

class ChatActivity : AppCompatActivity() {

    private lateinit var presenter: ChatPresenter
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var sendButton: Button
    private lateinit var input: EditText
    private lateinit var chatId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatId = intent.getStringExtra("chatId") ?: return finish()

        messageAdapter = MessageAdapter()
        findViewById<RecyclerView>(R.id.messageRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = messageAdapter
        }

        input = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)

        presenter = ChatPresenter(chatId,
            onMessagesLoaded = {
                messageAdapter.submitList(it)
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
