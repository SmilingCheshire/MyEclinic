package com.example.myeclinic.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myeclinic.R
import com.example.myeclinic.presenter.ChatListPresenter
import com.example.myeclinic.util.ChatPreviewAdapter

class ChatListActivity : AppCompatActivity() {

    private lateinit var presenter: ChatListPresenter
    private lateinit var adapter: ChatPreviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        adapter = ChatPreviewAdapter { chatId,  otherUserId->
            android.util.Log.d("ChatListActivity", "Opening chat with ID: $chatId")
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatId", chatId)
            intent.putExtra("otherUserId", otherUserId)
            startActivity(intent)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.chatListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        presenter = ChatListPresenter(
            onChatsLoaded = {
                adapter.submitList(it)
            },
            onError = {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        )

        presenter.loadChatPreviews()
    }
}
