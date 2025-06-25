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
    /**
     * Initializes the Communication Oversight screen for admin users.
     *
     * This screen displays a list of all active chat sessions between users (patients, doctors, etc.)
     * allowing administrators to monitor or intervene if necessary.
     *
     * Responsibilities:
     * - Sets up the layout `activity_communication_oversight`.
     * - Initializes a [RecyclerView] with a [ChatListAdapter] to display [ChatInfo] entries.
     * - Handles item clicks by launching [ChatActivity] in admin view mode, passing along
     *   the `chatId`, the `otherUserId`, and a boolean `adminView = true`.
     * - Initializes [CommunicationOversightPresenter] to load all chat data from backend.
     * - Displays a Toast message if loading fails.
     *
     * Intent Extras used when navigating to ChatActivity:
     * - `chatId`: Unique identifier for the chat (String)
     * - `otherUserId`: User ID of the second participant in the chat (String)
     * - `adminView`: Boolean flag indicating admin is accessing the chat (true)
     *
     * @param savedInstanceState Bundle containing activity's previously saved state, if any.
     */
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