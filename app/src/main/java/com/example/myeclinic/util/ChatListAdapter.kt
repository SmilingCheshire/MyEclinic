package com.example.myeclinic.util

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myeclinic.R


import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myeclinic.R
import com.example.myeclinic.ui.chat.ChatSelectionPresenter.ChatItem

class ChatListAdapter : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    private val chatList = mutableListOf<ChatItem>()

    fun submitList(newList: List<ChatItem>) {
        chatList.clear()
        chatList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chatList[position])
    }

    override fun getItemCount() = chatList.size

    inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val lastMessageText: TextView = view.findViewById(R.id.lastMessageText)
        private val participantText: TextView = view.findViewById(R.id.participantText)

        fun bind(item: ChatItem) {
            lastMessageText.text = item.lastMessage
            participantText.text = "Chat with: ${item.otherParticipantId}"

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, ChatActivity::class.java)
                intent.putExtra("chatId", item.chatId)
                itemView.context.startActivity(intent)
            }
        }
    }
}