package com.example.myeclinic.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myeclinic.R
import com.example.myeclinic.presenter.CommunicationOversightPresenter
import com.example.myeclinic.presenter.CommunicationOversightPresenter.ChatInfo
import com.example.myeclinic.util.ChatListAdapter

class CommunicationOversightActivity : AppCompatActivity() {

    private lateinit var presenter: CommunicationOversightPresenter
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_communication_oversight)

        recyclerView = findViewById(R.id.chatRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatListAdapter { chat ->
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("chatId", chat.chatId)
                putExtra("otherUserId", chat.user2Id)
                putExtra("adminView", true)
            }
            startActivity(intent)
        }
        recyclerView.adapter = chatAdapter

        presenter = CommunicationOversightPresenter(
            onChatsLoaded = { chatList ->
                chatAdapter.submitList(chatList)
            },
            onError = {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        )

        presenter.loadAllChats()
    }
}