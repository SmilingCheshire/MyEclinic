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
    /**
     * Called when the activity is starting. Sets up the Chat List screen,
     * displaying a list of recent chats and allowing the user to tap on one to open the conversation.
     *
     * Responsibilities:
     * - Initializes a [ChatPreviewAdapter] with a callback that launches [ChatActivity] when a chat is clicked.
     * - Configures the RecyclerView to display the list of chat previews.
     * - Initializes a [ChatListPresenter] to fetch and observe chat data.
     * - Loads chat previews and updates the UI or shows a Toast in case of an error.
     *
     * Layout: `activity_chat_list.xml`
     *
     * Chat items are displayed using:
     * - [ChatPreviewAdapter]
     * - Item layout: `item_chat_preview.xml`
     *
     * Navigation:
     * - When a chat is selected, it launches [ChatActivity] with `chatId` and `otherUserId` passed via intent.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     * this contains the data it most recently supplied.
     */
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
