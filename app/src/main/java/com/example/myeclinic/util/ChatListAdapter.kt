package com.example.myeclinic.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myeclinic.R
import com.example.myeclinic.presenter.CommunicationOversightPresenter.ChatInfo

class ChatListAdapter(
    private val onChatClick: (ChatInfo) -> Unit
) : ListAdapter<ChatInfo, ChatListAdapter.ChatViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_summary, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val namesText: TextView = itemView.findViewById(R.id.chatNames)

        fun bind(chat: ChatInfo) {
            namesText.text = "${chat.user1Name} ↔ ${chat.user2Name}"
            itemView.setOnClickListener { onChatClick(chat) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ChatInfo>() {
        override fun areItemsTheSame(oldItem: ChatInfo, newItem: ChatInfo): Boolean =
            oldItem.chatId == newItem.chatId

        override fun areContentsTheSame(oldItem: ChatInfo, newItem: ChatInfo): Boolean =
            oldItem == newItem
    }
}
