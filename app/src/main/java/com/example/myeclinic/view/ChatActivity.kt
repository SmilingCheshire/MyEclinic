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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatId = intent.getStringExtra("chatId") ?: return finish()
        otherUserId = intent.getStringExtra("otherUserId") ?: return finish()

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
