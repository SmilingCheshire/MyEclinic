package com.example.myeclinic.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myeclinic.R
import com.example.myeclinic.presenter.ChatListPresenter.ChatPreview

class ChatPreviewAdapter(
    private val onChatClicked: (chatId: String, otherUserId: String) -> Unit
) : RecyclerView.Adapter<ChatPreviewAdapter.ViewHolder>() {

    private val chats = mutableListOf<ChatPreview>()

    fun submitList(newList: List<ChatPreview>) {
        chats.clear()
        chats.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_preview, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = chats.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(chats[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val username: TextView = itemView.findViewById(R.id.usernameTextView)
        private val lastMessage: TextView = itemView.findViewById(R.id.lastMessageTextView)
        private val timestampView: TextView = itemView.findViewById(R.id.timestampTextView)

        fun bind(chat: ChatPreview) {
            val currentUser = com.example.myeclinic.util.UserSession.currentUser

            username.text = if (currentUser?.role == "Admin") {
                // Show both names
                "${chat.user1Name} ↔ ${chat.user2Name}"
            } else {
                // Show the other user's name only
                if (chat.user1Id == currentUser?.userId) chat.user2Name else chat.user1Name
            }

            lastMessage.text = chat.lastMessage

            chat.timestamp?.let { date ->
                val now = java.util.Calendar.getInstance()
                val msgCal = java.util.Calendar.getInstance().apply { time = date }

                val formatted = if (
                    now.get(java.util.Calendar.YEAR) == msgCal.get(java.util.Calendar.YEAR) &&
                    now.get(java.util.Calendar.DAY_OF_YEAR) == msgCal.get(java.util.Calendar.DAY_OF_YEAR)
                ) {
                    android.text.format.DateFormat.format("hh:mm a", date).toString()
                } else {
                    android.text.format.DateFormat.format("MMM dd", date).toString()
                }

                timestampView.text = formatted
            } ?: run {
                timestampView.text = ""
            }

            itemView.setOnClickListener {
                val otherUserId = if (chat.user1Id == currentUser?.userId) chat.user2Id else chat.user1Id
                onChatClicked(chat.chatId, otherUserId)
            }
    }
    }

}
